/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class PotatoesBlock
extends CropBlock {
    public static final MapCodec<PotatoesBlock> CODEC = PotatoesBlock.createCodec(PotatoesBlock::new);
    private static final VoxelShape[] SHAPES_BY_AGE = Block.createShapeArray(7, age -> Block.createColumnShape(16.0, 0.0, 2 + age));

    public MapCodec<PotatoesBlock> getCodec() {
        return CODEC;
    }

    public PotatoesBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected ItemConvertible getSeedsItem() {
        return Items.POTATO;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES_BY_AGE[this.getAge(state)];
    }
}

