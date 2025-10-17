/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.WallShape;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
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
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class PaleMossCarpetBlock
extends Block
implements Fertilizable {
    public static final MapCodec<PaleMossCarpetBlock> CODEC = PaleMossCarpetBlock.createCodec(PaleMossCarpetBlock::new);
    public static final BooleanProperty BOTTOM = Properties.BOTTOM;
    public static final EnumProperty<WallShape> NORTH = Properties.NORTH_WALL_SHAPE;
    public static final EnumProperty<WallShape> EAST = Properties.EAST_WALL_SHAPE;
    public static final EnumProperty<WallShape> SOUTH = Properties.SOUTH_WALL_SHAPE;
    public static final EnumProperty<WallShape> WEST = Properties.WEST_WALL_SHAPE;
    public static final Map<Direction, EnumProperty<WallShape>> WALL_SHAPE_PROPERTIES_BY_DIRECTION = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST)));
    private final Function<BlockState, VoxelShape> shapeFunction;

    public MapCodec<PaleMossCarpetBlock> getCodec() {
        return CODEC;
    }

    public PaleMossCarpetBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(BOTTOM, true)).with(NORTH, WallShape.NONE)).with(EAST, WallShape.NONE)).with(SOUTH, WallShape.NONE)).with(WEST, WallShape.NONE));
        this.shapeFunction = this.createShapeFunction();
    }

    @Override
    protected VoxelShape getCullingShape(BlockState state) {
        return VoxelShapes.empty();
    }

    public Function<BlockState, VoxelShape> createShapeFunction() {
        Map<Direction, VoxelShape> map = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidZShape(16.0, 0.0, 10.0, 0.0, 1.0));
        Map<Direction, VoxelShape> map2 = VoxelShapes.createFacingShapeMap(Block.createCuboidZShape(16.0, 0.0, 1.0));
        return this.createShapeFunction(state -> {
            VoxelShape lv = state.get(BOTTOM) != false ? (VoxelShape)map2.get(Direction.DOWN) : VoxelShapes.empty();
            for (Map.Entry<Direction, EnumProperty<WallShape>> entry : WALL_SHAPE_PROPERTIES_BY_DIRECTION.entrySet()) {
                switch ((WallShape)state.get(entry.getValue())) {
                    case NONE: {
                        break;
                    }
                    case LOW: {
                        lv = VoxelShapes.union(lv, (VoxelShape)map.get(entry.getKey()));
                        break;
                    }
                    case TALL: {
                        lv = VoxelShapes.union(lv, (VoxelShape)map2.get(entry.getKey()));
                    }
                }
            }
            return lv.isEmpty() ? VoxelShapes.fullCube() : lv;
        });
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(BOTTOM) != false ? this.shapeFunction.apply(this.getDefaultState()) : VoxelShapes.empty();
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return true;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos.down());
        if (state.get(BOTTOM).booleanValue()) {
            return !lv.isAir();
        }
        return lv.isOf(this) && lv.get(BOTTOM) != false;
    }

    private static boolean hasAnyShape(BlockState state) {
        if (state.get(BOTTOM).booleanValue()) {
            return true;
        }
        for (EnumProperty<WallShape> lv : WALL_SHAPE_PROPERTIES_BY_DIRECTION.values()) {
            if (state.get(lv) == WallShape.NONE) continue;
            return true;
        }
        return false;
    }

    private static boolean canGrowOnFace(BlockView world, BlockPos pos, Direction direction) {
        if (direction == Direction.UP) {
            return false;
        }
        return MultifaceBlock.canGrowOn(world, pos, direction);
    }

    private static BlockState updateState(BlockState state, BlockView world, BlockPos pos, boolean bl) {
        AbstractBlock.AbstractBlockState lv = null;
        AbstractBlock.AbstractBlockState lv2 = null;
        bl |= state.get(BOTTOM).booleanValue();
        for (Direction lv3 : Direction.Type.HORIZONTAL) {
            WallShape lv5;
            EnumProperty<WallShape> lv4 = PaleMossCarpetBlock.getWallShape(lv3);
            WallShape wallShape = PaleMossCarpetBlock.canGrowOnFace(world, pos, lv3) ? (bl ? WallShape.LOW : state.get(lv4)) : (lv5 = WallShape.NONE);
            if (lv5 == WallShape.LOW) {
                if (lv == null) {
                    lv = world.getBlockState(pos.up());
                }
                if (lv.isOf(Blocks.PALE_MOSS_CARPET) && lv.get(lv4) != WallShape.NONE && !lv.get(BOTTOM).booleanValue()) {
                    lv5 = WallShape.TALL;
                }
                if (!state.get(BOTTOM).booleanValue()) {
                    if (lv2 == null) {
                        lv2 = world.getBlockState(pos.down());
                    }
                    if (lv2.isOf(Blocks.PALE_MOSS_CARPET) && lv2.get(lv4) == WallShape.NONE) {
                        lv5 = WallShape.NONE;
                    }
                }
            }
            state = (BlockState)state.with(lv4, lv5);
        }
        return state;
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return PaleMossCarpetBlock.updateState(this.getDefaultState(), ctx.getWorld(), ctx.getBlockPos(), true);
    }

    public static void placeAt(WorldAccess world, BlockPos pos, Random random, int flags) {
        BlockState lv = Blocks.PALE_MOSS_CARPET.getDefaultState();
        BlockState lv2 = PaleMossCarpetBlock.updateState(lv, world, pos, true);
        world.setBlockState(pos, lv2, flags);
        BlockState lv3 = PaleMossCarpetBlock.createUpperState(world, pos, random::nextBoolean);
        if (!lv3.isAir()) {
            world.setBlockState(pos.up(), lv3, flags);
            BlockState lv4 = PaleMossCarpetBlock.updateState(lv2, world, pos, true);
            world.setBlockState(pos, lv4, flags);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.isClient()) {
            return;
        }
        Random lv = world.getRandom();
        BlockState lv2 = PaleMossCarpetBlock.createUpperState(world, pos, lv::nextBoolean);
        if (!lv2.isAir()) {
            world.setBlockState(pos.up(), lv2, Block.NOTIFY_ALL);
        }
    }

    private static BlockState createUpperState(BlockView world, BlockPos pos, BooleanSupplier booleanSupplier) {
        BlockPos lv = pos.up();
        BlockState lv2 = world.getBlockState(lv);
        boolean bl = lv2.isOf(Blocks.PALE_MOSS_CARPET);
        if (bl && lv2.get(BOTTOM).booleanValue() || !bl && !lv2.isReplaceable()) {
            return Blocks.AIR.getDefaultState();
        }
        BlockState lv3 = (BlockState)Blocks.PALE_MOSS_CARPET.getDefaultState().with(BOTTOM, false);
        BlockState lv4 = PaleMossCarpetBlock.updateState(lv3, world, pos.up(), true);
        for (Direction lv5 : Direction.Type.HORIZONTAL) {
            EnumProperty<WallShape> lv6 = PaleMossCarpetBlock.getWallShape(lv5);
            if (lv4.get(lv6) == WallShape.NONE || booleanSupplier.getAsBoolean()) continue;
            lv4 = (BlockState)lv4.with(lv6, WallShape.NONE);
        }
        if (PaleMossCarpetBlock.hasAnyShape(lv4) && lv4 != lv2) {
            return lv4;
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        BlockState lv = PaleMossCarpetBlock.updateState(state, world, pos, false);
        if (!PaleMossCarpetBlock.hasAnyShape(lv)) {
            return Blocks.AIR.getDefaultState();
        }
        return lv;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(BOTTOM, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return switch (rotation) {
            case BlockRotation.CLOCKWISE_180 -> (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(EAST, state.get(WEST))).with(SOUTH, state.get(NORTH))).with(WEST, state.get(EAST));
            case BlockRotation.COUNTERCLOCKWISE_90 -> (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(EAST))).with(EAST, state.get(SOUTH))).with(SOUTH, state.get(WEST))).with(WEST, state.get(NORTH));
            case BlockRotation.CLOCKWISE_90 -> (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(WEST))).with(EAST, state.get(NORTH))).with(SOUTH, state.get(EAST))).with(WEST, state.get(SOUTH));
            default -> state;
        };
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return switch (mirror) {
            case BlockMirror.LEFT_RIGHT -> (BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(SOUTH, state.get(NORTH));
            case BlockMirror.FRONT_BACK -> (BlockState)((BlockState)state.with(EAST, state.get(WEST))).with(WEST, state.get(EAST));
            default -> super.mirror(state, mirror);
        };
    }

    @Nullable
    public static EnumProperty<WallShape> getWallShape(Direction face) {
        return WALL_SHAPE_PROPERTIES_BY_DIRECTION.get(face);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return state.get(BOTTOM) != false && !PaleMossCarpetBlock.createUpperState(world, pos, () -> true).isAir();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        BlockState lv = PaleMossCarpetBlock.createUpperState(world, pos, () -> true);
        if (!lv.isAir()) {
            world.setBlockState(pos.up(), lv, Block.NOTIFY_ALL);
        }
    }
}

