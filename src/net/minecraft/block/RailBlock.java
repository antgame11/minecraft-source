/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RailPlacementHelper;
import net.minecraft.block.enums.RailShape;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RailBlock
extends AbstractRailBlock {
    public static final MapCodec<RailBlock> CODEC = RailBlock.createCodec(RailBlock::new);
    public static final EnumProperty<RailShape> SHAPE = Properties.RAIL_SHAPE;

    public MapCodec<RailBlock> getCodec() {
        return CODEC;
    }

    protected RailBlock(AbstractBlock.Settings arg) {
        super(false, arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(SHAPE, RailShape.NORTH_SOUTH)).with(WATERLOGGED, false));
    }

    @Override
    protected void updateBlockState(BlockState state, World world, BlockPos pos, Block neighbor) {
        if (neighbor.getDefaultState().emitsRedstonePower() && new RailPlacementHelper(world, pos, state).getNeighborCount() == 3) {
            this.updateBlockState(world, pos, state, false);
        }
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        RailShape lv = state.get(SHAPE);
        RailShape lv2 = this.rotateShape(lv, rotation);
        return (BlockState)state.with(SHAPE, lv2);
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        RailShape lv = state.get(SHAPE);
        RailShape lv2 = this.mirrorShape(lv, mirror);
        return (BlockState)state.with(SHAPE, lv2);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED);
    }
}

