/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class CactusBlock
extends Block {
    public static final MapCodec<CactusBlock> CODEC = CactusBlock.createCodec(CactusBlock::new);
    public static final IntProperty AGE = Properties.AGE_15;
    public static final int MAX_AGE = 15;
    private static final VoxelShape OUTLINE_SHAPE = Block.createColumnShape(14.0, 0.0, 16.0);
    private static final VoxelShape COLLISION_SHAPE = Block.createColumnShape(14.0, 0.0, 15.0);
    private static final int TALL_THRESHOLD = 3;
    private static final int FLOWER_GROWTH_AGE = 8;
    private static final double FLOWER_CHANCE_WHEN_SHORT = 0.1;
    private static final double FLOWER_CHANCE_WHEN_TALL = 0.25;

    public MapCodec<CactusBlock> getCodec() {
        return CODEC;
    }

    protected CactusBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockPos lv = pos.up();
        if (!world.isAir(lv)) {
            return;
        }
        int i = 1;
        int j = state.get(AGE);
        while (world.getBlockState(pos.down(i)).isOf(this)) {
            if (++i != 3 || j != 15) continue;
            return;
        }
        if (j == 8 && this.canPlaceAt(this.getDefaultState(), world, pos.up())) {
            double d;
            double d2 = d = i >= 3 ? 0.25 : 0.1;
            if (random.nextDouble() <= d) {
                world.setBlockState(lv, Blocks.CACTUS_FLOWER.getDefaultState());
            }
        } else if (j == 15 && i < 3) {
            world.setBlockState(lv, this.getDefaultState());
            BlockState lv2 = (BlockState)state.with(AGE, 0);
            world.setBlockState(pos, lv2, Block.SKIP_REDRAW_AND_BLOCK_ENTITY_REPLACED_CALLBACK);
            world.updateNeighbor(lv2, lv, this, null, false);
        }
        if (j < 15) {
            world.setBlockState(pos, (BlockState)state.with(AGE, j + 1), Block.SKIP_REDRAW_AND_BLOCK_ENTITY_REPLACED_CALLBACK);
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            tickView.scheduleBlockTick(pos, this, 1);
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        for (Direction lv : Direction.Type.HORIZONTAL) {
            BlockState lv2 = world.getBlockState(pos.offset(lv));
            if (!lv2.isSolid() && !world.getFluidState(pos.offset(lv)).isIn(FluidTags.LAVA)) continue;
            return false;
        }
        BlockState lv3 = world.getBlockState(pos.down());
        return (lv3.isOf(Blocks.CACTUS) || lv3.isIn(BlockTags.SAND)) && !world.getBlockState(pos.up()).isLiquid();
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        entity.serverDamage(world.getDamageSources().cactus(), 1.0f);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }
}

