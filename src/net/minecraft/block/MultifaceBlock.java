/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class MultifaceBlock
extends Block
implements Waterloggable {
    public static final MapCodec<MultifaceBlock> CODEC = MultifaceBlock.createCodec(MultifaceBlock::new);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES;
    protected static final Direction[] DIRECTIONS = Direction.values();
    private final Function<BlockState, VoxelShape> shapeFunction;
    private final boolean hasAllHorizontalDirections;
    private final boolean canMirrorX;
    private final boolean canMirrorZ;

    protected MapCodec<? extends MultifaceBlock> getCodec() {
        return CODEC;
    }

    public MultifaceBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState(MultifaceBlock.withAllDirections(this.stateManager));
        this.shapeFunction = this.createShapeFunction();
        this.hasAllHorizontalDirections = Direction.Type.HORIZONTAL.stream().allMatch(this::canHaveDirection);
        this.canMirrorX = Direction.Type.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::canHaveDirection).count() % 2L == 0L;
        this.canMirrorZ = Direction.Type.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::canHaveDirection).count() % 2L == 0L;
    }

    private Function<BlockState, VoxelShape> createShapeFunction() {
        Map<Direction, VoxelShape> map = VoxelShapes.createFacingShapeMap(Block.createCuboidZShape(16.0, 0.0, 1.0));
        return this.createShapeFunction(state -> {
            VoxelShape lv = VoxelShapes.empty();
            for (Direction lv2 : DIRECTIONS) {
                if (!MultifaceBlock.hasDirection(state, lv2)) continue;
                lv = VoxelShapes.union(lv, (VoxelShape)map.get(lv2));
            }
            return lv.isEmpty() ? VoxelShapes.fullCube() : lv;
        }, WATERLOGGED);
    }

    public static Set<Direction> collectDirections(BlockState state) {
        if (!(state.getBlock() instanceof MultifaceBlock)) {
            return Set.of();
        }
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        for (Direction lv : Direction.values()) {
            if (!MultifaceBlock.hasDirection(state, lv)) continue;
            set.add(lv);
        }
        return set;
    }

    public static Set<Direction> flagToDirections(byte flag) {
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        for (Direction lv : Direction.values()) {
            if ((flag & (byte)(1 << lv.ordinal())) <= 0) continue;
            set.add(lv);
        }
        return set;
    }

    public static byte directionsToFlag(Collection<Direction> directions) {
        byte b = 0;
        for (Direction lv : directions) {
            b = (byte)(b | 1 << lv.ordinal());
        }
        return b;
    }

    protected boolean canHaveDirection(Direction direction) {
        return true;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        for (Direction lv : DIRECTIONS) {
            if (!this.canHaveDirection(lv)) continue;
            builder.add(MultifaceBlock.getProperty(lv));
        }
        builder.add(WATERLOGGED);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (!MultifaceBlock.hasAnyDirection(state)) {
            return Blocks.AIR.getDefaultState();
        }
        if (!MultifaceBlock.hasDirection(state, direction) || MultifaceBlock.canGrowOn(world, direction, neighborPos, neighborState)) {
            return state;
        }
        return MultifaceBlock.disableDirection(state, MultifaceBlock.getProperty(direction));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        boolean bl = false;
        for (Direction lv : DIRECTIONS) {
            if (!MultifaceBlock.hasDirection(state, lv)) continue;
            if (!MultifaceBlock.canGrowOn(world, pos, lv)) {
                return false;
            }
            bl = true;
        }
        return bl;
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        return !context.getStack().isOf(this.asItem()) || MultifaceBlock.isNotFullBlock(state);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World lv = ctx.getWorld();
        BlockPos lv2 = ctx.getBlockPos();
        BlockState lv3 = lv.getBlockState(lv2);
        return Arrays.stream(ctx.getPlacementDirections()).map(direction -> this.withDirection(lv3, lv, lv2, (Direction)direction)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public boolean canGrowWithDirection(BlockView world, BlockState state, BlockPos pos, Direction direction) {
        if (!this.canHaveDirection(direction) || state.isOf(this) && MultifaceBlock.hasDirection(state, direction)) {
            return false;
        }
        BlockPos lv = pos.offset(direction);
        return MultifaceBlock.canGrowOn(world, direction, lv, world.getBlockState(lv));
    }

    @Nullable
    public BlockState withDirection(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!this.canGrowWithDirection(world, state, pos, direction)) {
            return null;
        }
        BlockState lv = state.isOf(this) ? state : (state.getFluidState().isEqualAndStill(Fluids.WATER) ? (BlockState)this.getDefaultState().with(Properties.WATERLOGGED, true) : this.getDefaultState());
        return (BlockState)lv.with(MultifaceBlock.getProperty(direction), true);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        if (!this.hasAllHorizontalDirections) {
            return state;
        }
        return this.mirror(state, rotation::rotate);
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        if (mirror == BlockMirror.FRONT_BACK && !this.canMirrorX) {
            return state;
        }
        if (mirror == BlockMirror.LEFT_RIGHT && !this.canMirrorZ) {
            return state;
        }
        return this.mirror(state, mirror::apply);
    }

    private BlockState mirror(BlockState state, Function<Direction, Direction> mirror) {
        BlockState lv = state;
        for (Direction lv2 : DIRECTIONS) {
            if (!this.canHaveDirection(lv2)) continue;
            lv = (BlockState)lv.with(MultifaceBlock.getProperty(mirror.apply(lv2)), state.get(MultifaceBlock.getProperty(lv2)));
        }
        return lv;
    }

    public static boolean hasDirection(BlockState state, Direction direction) {
        BooleanProperty lv = MultifaceBlock.getProperty(direction);
        return state.get(lv, false);
    }

    public static boolean canGrowOn(BlockView world, BlockPos pos, Direction direction) {
        BlockPos lv = pos.offset(direction);
        BlockState lv2 = world.getBlockState(lv);
        return MultifaceBlock.canGrowOn(world, direction, lv, lv2);
    }

    public static boolean canGrowOn(BlockView world, Direction direction, BlockPos pos, BlockState state) {
        return Block.isFaceFullSquare(state.getSidesShape(world, pos), direction.getOpposite()) || Block.isFaceFullSquare(state.getCollisionShape(world, pos), direction.getOpposite());
    }

    private static BlockState disableDirection(BlockState state, BooleanProperty direction) {
        BlockState lv = (BlockState)state.with(direction, false);
        if (MultifaceBlock.hasAnyDirection(lv)) {
            return lv;
        }
        return Blocks.AIR.getDefaultState();
    }

    public static BooleanProperty getProperty(Direction direction) {
        return FACING_PROPERTIES.get(direction);
    }

    private static BlockState withAllDirections(StateManager<Block, BlockState> stateManager) {
        BlockState lv = (BlockState)stateManager.getDefaultState().with(WATERLOGGED, false);
        for (BooleanProperty lv2 : FACING_PROPERTIES.values()) {
            lv = (BlockState)lv.withIfExists(lv2, false);
        }
        return lv;
    }

    protected static boolean hasAnyDirection(BlockState state) {
        for (Direction lv : DIRECTIONS) {
            if (!MultifaceBlock.hasDirection(state, lv)) continue;
            return true;
        }
        return false;
    }

    private static boolean isNotFullBlock(BlockState state) {
        for (Direction lv : DIRECTIONS) {
            if (MultifaceBlock.hasDirection(state, lv)) continue;
            return true;
        }
        return false;
    }
}

