/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPlantBlock;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class WeepingVinesPlantBlock
extends AbstractPlantBlock {
    public static final MapCodec<WeepingVinesPlantBlock> CODEC = WeepingVinesPlantBlock.createCodec(WeepingVinesPlantBlock::new);
    private static final VoxelShape SHAPE = Block.createColumnShape(14.0, 0.0, 16.0);

    public MapCodec<WeepingVinesPlantBlock> getCodec() {
        return CODEC;
    }

    public WeepingVinesPlantBlock(AbstractBlock.Settings arg) {
        super(arg, Direction.DOWN, SHAPE, false);
    }

    @Override
    protected AbstractPlantStemBlock getStem() {
        return (AbstractPlantStemBlock)Blocks.WEEPING_VINES;
    }
}

