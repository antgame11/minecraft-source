/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TallSeagrassBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class SeagrassBlock
extends PlantBlock
implements Fertilizable,
FluidFillable {
    public static final MapCodec<SeagrassBlock> CODEC = SeagrassBlock.createCodec(SeagrassBlock::new);
    private static final VoxelShape SHAPE = Block.createColumnShape(12.0, 0.0, 12.0);

    public MapCodec<SeagrassBlock> getCodec() {
        return CODEC;
    }

    protected SeagrassBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isSideSolidFullSquare(world, pos, Direction.UP) && !floor.isOf(Blocks.MAGMA_BLOCK);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
        if (lv.isIn(FluidTags.WATER) && lv.getLevel() == 8) {
            return super.getPlacementState(ctx);
        }
        return null;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        BlockState lv = super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        if (!lv.isAir()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return lv;
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return world.getBlockState(pos.up()).isOf(Blocks.WATER);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getStill(false);
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        BlockState lv = Blocks.TALL_SEAGRASS.getDefaultState();
        BlockState lv2 = (BlockState)lv.with(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER);
        BlockPos lv3 = pos.up();
        world.setBlockState(pos, lv, Block.NOTIFY_LISTENERS);
        world.setBlockState(lv3, lv2, Block.NOTIFY_LISTENERS);
    }

    @Override
    public boolean canFillWithFluid(@Nullable LivingEntity filler, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }
}

