/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class KelpBlock
extends AbstractPlantStemBlock
implements FluidFillable {
    public static final MapCodec<KelpBlock> CODEC = KelpBlock.createCodec(KelpBlock::new);
    private static final double GROWTH_CHANCE = 0.14;
    private static final VoxelShape SHAPE = Block.createColumnShape(16.0, 0.0, 9.0);

    public MapCodec<KelpBlock> getCodec() {
        return CODEC;
    }

    protected KelpBlock(AbstractBlock.Settings arg) {
        super(arg, Direction.UP, SHAPE, true, 0.14);
    }

    @Override
    protected boolean chooseStemState(BlockState state) {
        return state.isOf(Blocks.WATER);
    }

    @Override
    protected Block getPlant() {
        return Blocks.KELP_PLANT;
    }

    @Override
    protected boolean canAttachTo(BlockState state) {
        return !state.isOf(Blocks.MAGMA_BLOCK);
    }

    @Override
    public boolean canFillWithFluid(@Nullable LivingEntity filler, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    protected int getGrowthLength(Random random) {
        return 1;
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
    protected FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getStill(false);
    }
}

