/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class WallPiglinHeadBlock
extends WallSkullBlock {
    public static final MapCodec<WallPiglinHeadBlock> CODEC = WallPiglinHeadBlock.createCodec(WallPiglinHeadBlock::new);
    private static final Map<Direction, VoxelShape> SHAPES = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidZShape(10.0, 8.0, 8.0, 16.0));

    public MapCodec<WallPiglinHeadBlock> getCodec() {
        return CODEC;
    }

    public WallPiglinHeadBlock(AbstractBlock.Settings arg) {
        super(SkullBlock.Type.PIGLIN, arg);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(FACING));
    }
}

