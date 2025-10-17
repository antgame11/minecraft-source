/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.OptionalInt;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public abstract class LeavesBlock
extends Block
implements Waterloggable {
    public static final int MAX_DISTANCE = 7;
    public static final IntProperty DISTANCE = Properties.DISTANCE_1_7;
    public static final BooleanProperty PERSISTENT = Properties.PERSISTENT;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    protected final float leafParticleChance;
    private static final int field_31112 = 1;

    public abstract MapCodec<? extends LeavesBlock> getCodec();

    public LeavesBlock(float leafParticleChance, AbstractBlock.Settings settings) {
        super(settings);
        this.leafParticleChance = leafParticleChance;
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DISTANCE, 7)).with(PERSISTENT, false)).with(WATERLOGGED, false));
    }

    @Override
    protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return state.get(DISTANCE) == 7 && state.get(PERSISTENT) == false;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (this.shouldDecay(state)) {
            LeavesBlock.dropStacks(state, world, pos);
            world.removeBlock(pos, false);
        }
    }

    protected boolean shouldDecay(BlockState state) {
        return state.get(PERSISTENT) == false && state.get(DISTANCE) == 7;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, LeavesBlock.updateDistanceFromLogs(state, world, pos), Block.NOTIFY_ALL);
    }

    @Override
    protected int getOpacity(BlockState state) {
        return 1;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        int i;
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if ((i = LeavesBlock.getDistanceFromLog(neighborState) + 1) != 1 || state.get(DISTANCE) != i) {
            tickView.scheduleBlockTick(pos, this, 1);
        }
        return state;
    }

    private static BlockState updateDistanceFromLogs(BlockState state, WorldAccess world, BlockPos pos) {
        int i = 7;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (Direction lv2 : Direction.values()) {
            lv.set((Vec3i)pos, lv2);
            i = Math.min(i, LeavesBlock.getDistanceFromLog(world.getBlockState(lv)) + 1);
            if (i == 1) break;
        }
        return (BlockState)state.with(DISTANCE, i);
    }

    private static int getDistanceFromLog(BlockState state) {
        return LeavesBlock.getOptionalDistanceFromLog(state).orElse(7);
    }

    public static OptionalInt getOptionalDistanceFromLog(BlockState state) {
        if (state.isIn(BlockTags.LOGS)) {
            return OptionalInt.of(0);
        }
        if (state.contains(DISTANCE)) {
            return OptionalInt.of(state.get(DISTANCE));
        }
        return OptionalInt.empty();
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        LeavesBlock.spawnWaterParticle(world, pos, random, lv2, lv);
        this.spawnLeafParticle(world, pos, random, lv2, lv);
    }

    private static void spawnWaterParticle(World world, BlockPos pos, Random random, BlockState state, BlockPos posBelow) {
        if (!world.hasRain(pos.up())) {
            return;
        }
        if (random.nextInt(15) != 1) {
            return;
        }
        if (state.isOpaque() && state.isSideSolidFullSquare(world, posBelow, Direction.UP)) {
            return;
        }
        ParticleUtil.spawnParticle(world, pos, random, ParticleTypes.DRIPPING_WATER);
    }

    private void spawnLeafParticle(World world, BlockPos pos, Random random, BlockState state, BlockPos posBelow) {
        if (random.nextFloat() >= this.leafParticleChance) {
            return;
        }
        if (LeavesBlock.isFaceFullSquare(state.getCollisionShape(world, posBelow), Direction.UP)) {
            return;
        }
        this.spawnLeafParticle(world, pos, random);
    }

    protected abstract void spawnLeafParticle(World var1, BlockPos var2, Random var3);

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, PERSISTENT, WATERLOGGED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockState lv2 = (BlockState)((BlockState)this.getDefaultState().with(PERSISTENT, true)).with(WATERLOGGED, lv.getFluid() == Fluids.WATER);
        return LeavesBlock.updateDistanceFromLogs(lv2, ctx.getWorld(), ctx.getBlockPos());
    }
}

