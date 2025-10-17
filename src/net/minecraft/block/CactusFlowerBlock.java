/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SideShapeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class CactusFlowerBlock
extends PlantBlock {
    public static final MapCodec<CactusFlowerBlock> CODEC = CactusFlowerBlock.createCodec(CactusFlowerBlock::new);
    private static final VoxelShape SHAPE = Block.createColumnShape(14.0, 0.0, 12.0);

    public MapCodec<? extends CactusFlowerBlock> getCodec() {
        return CODEC;
    }

    public CactusFlowerBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        return lv.isOf(Blocks.CACTUS) || lv.isOf(Blocks.FARMLAND) || lv.isSideSolid(world, pos, Direction.UP, SideShapeType.CENTER);
    }
}

