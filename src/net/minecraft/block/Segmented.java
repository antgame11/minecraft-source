/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import java.util.Map;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public interface Segmented {
    public static final int SEGMENTS_PER_PLACEMENT = 1;
    public static final int MAX_SEGMENTS = 4;
    public static final IntProperty SEGMENT_AMOUNT = Properties.SEGMENT_AMOUNT;

    default public Function<BlockState, VoxelShape> createShapeFunction(EnumProperty<Direction> directionProperty, IntProperty segmentAmountProperty) {
        Map<Direction, VoxelShape> map = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidShape(0.0, 0.0, 0.0, 8.0, this.getHeight(), 8.0));
        return state -> {
            VoxelShape lv = VoxelShapes.empty();
            Direction lv2 = (Direction)state.get(directionProperty);
            int i = state.get(segmentAmountProperty);
            for (int j = 0; j < i; ++j) {
                lv = VoxelShapes.union(lv, (VoxelShape)map.get(lv2));
                lv2 = lv2.rotateYCounterclockwise();
            }
            return lv.asCuboid();
        };
    }

    default public IntProperty getAmountProperty() {
        return SEGMENT_AMOUNT;
    }

    default public double getHeight() {
        return 1.0;
    }

    default public boolean shouldAddSegment(BlockState state, ItemPlacementContext context, IntProperty property) {
        return !context.shouldCancelInteraction() && context.getStack().isOf(state.getBlock().asItem()) && state.get(property) < 4;
    }

    default public BlockState getPlacementState(ItemPlacementContext context, Block block, IntProperty amountProperty, EnumProperty<Direction> directionProperty) {
        BlockState lv = context.getWorld().getBlockState(context.getBlockPos());
        if (lv.isOf(block)) {
            return (BlockState)lv.with(amountProperty, Math.min(4, lv.get(amountProperty) + 1));
        }
        return (BlockState)block.getDefaultState().with(directionProperty, context.getHorizontalPlayerFacing().getOpposite());
    }
}

