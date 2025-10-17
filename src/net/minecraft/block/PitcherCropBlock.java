/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class PitcherCropBlock
extends TallPlantBlock
implements Fertilizable {
    public static final MapCodec<PitcherCropBlock> CODEC = PitcherCropBlock.createCodec(PitcherCropBlock::new);
    public static final int field_43240 = 4;
    public static final IntProperty AGE = Properties.AGE_4;
    public static final EnumProperty<DoubleBlockHalf> HALF = TallPlantBlock.HALF;
    private static final int field_43241 = 3;
    private static final int field_43391 = 1;
    private static final VoxelShape AGE_0_SHAPE = Block.createColumnShape(6.0, -1.0, 3.0);
    private static final VoxelShape LOWER_COLLISION_SHAPE = Block.createColumnShape(10.0, -1.0, 5.0);
    private final Function<BlockState, VoxelShape> shapeFunction = this.createShapeFunction();

    public MapCodec<PitcherCropBlock> getCodec() {
        return CODEC;
    }

    public PitcherCropBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    private Function<BlockState, VoxelShape> createShapeFunction() {
        int[] is = new int[]{0, 9, 11, 22, 26};
        return this.createShapeFunction(state -> {
            int i = (state.get(AGE) == 0 ? 4 : 6) + is[state.get(AGE)];
            int j = state.get(AGE) == 0 ? 6 : 10;
            return switch (state.get(HALF)) {
                default -> throw new MatchException(null, null);
                case DoubleBlockHalf.LOWER -> Block.createColumnShape(j, -1.0, Math.min(16, -1 + i));
                case DoubleBlockHalf.UPPER -> Block.createColumnShape(j, 0.0, Math.max(0, -1 + i - 16));
            };
        });
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return state.get(AGE) == 0 ? AGE_0_SHAPE : LOWER_COLLISION_SHAPE;
        }
        return VoxelShapes.empty();
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (PitcherCropBlock.isDoubleTallAtAge(state.get(AGE))) {
            return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        }
        return state.canPlaceAt(world, pos) ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (PitcherCropBlock.isLowerHalf(state) && !PitcherCropBlock.canPlaceAt(world, pos)) {
            return false;
        }
        return super.canPlaceAt(state, world, pos);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
        super.appendProperties(builder);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            if (entity instanceof RavagerEntity && lv.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                lv.breakBlock(pos, true, entity);
            }
        }
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return false;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return state.get(HALF) == DoubleBlockHalf.LOWER && !this.isFullyGrown(state);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean bl;
        float f = CropBlock.getAvailableMoisture(this, world, pos);
        boolean bl2 = bl = random.nextInt((int)(25.0f / f) + 1) == 0;
        if (bl) {
            this.tryGrow(world, state, pos, 1);
        }
    }

    private void tryGrow(ServerWorld world, BlockState state, BlockPos pos, int amount) {
        int j = Math.min(state.get(AGE) + amount, 4);
        if (!this.canGrow((WorldView)world, pos, state, j)) {
            return;
        }
        BlockState lv = (BlockState)state.with(AGE, j);
        world.setBlockState(pos, lv, Block.NOTIFY_LISTENERS);
        if (PitcherCropBlock.isDoubleTallAtAge(j)) {
            world.setBlockState(pos.up(), (BlockState)lv.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
        }
    }

    private static boolean canGrowAt(WorldView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        return lv.isAir() || lv.isOf(Blocks.PITCHER_CROP);
    }

    private static boolean canPlaceAt(WorldView world, BlockPos pos) {
        return CropBlock.hasEnoughLightAt(world, pos);
    }

    private static boolean isLowerHalf(BlockState state) {
        return state.isOf(Blocks.PITCHER_CROP) && state.get(HALF) == DoubleBlockHalf.LOWER;
    }

    private static boolean isDoubleTallAtAge(int age) {
        return age >= 3;
    }

    private boolean canGrow(WorldView world, BlockPos pos, BlockState state, int age) {
        return !this.isFullyGrown(state) && PitcherCropBlock.canPlaceAt(world, pos) && (!PitcherCropBlock.isDoubleTallAtAge(age) || PitcherCropBlock.canGrowAt(world, pos.up()));
    }

    private boolean isFullyGrown(BlockState state) {
        return state.get(AGE) >= 4;
    }

    @Nullable
    private LowerHalfContext getLowerHalfContext(WorldView world, BlockPos pos, BlockState state) {
        if (PitcherCropBlock.isLowerHalf(state)) {
            return new LowerHalfContext(pos, state);
        }
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        if (PitcherCropBlock.isLowerHalf(lv2)) {
            return new LowerHalfContext(lv, lv2);
        }
        return null;
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        LowerHalfContext lv = this.getLowerHalfContext(world, pos, state);
        if (lv == null) {
            return false;
        }
        return this.canGrow(world, lv.pos, lv.state, lv.state.get(AGE) + 1);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        LowerHalfContext lv = this.getLowerHalfContext(world, pos, state);
        if (lv == null) {
            return;
        }
        this.tryGrow(world, lv.state, lv.pos, 1);
    }

    record LowerHalfContext(BlockPos pos, BlockState state) {
    }
}

