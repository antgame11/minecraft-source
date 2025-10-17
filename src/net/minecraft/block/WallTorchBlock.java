/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TorchBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class WallTorchBlock
extends TorchBlock {
    public static final MapCodec<WallTorchBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(PARTICLE_TYPE_CODEC.forGetter(block -> block.particle), WallTorchBlock.createSettingsCodec()).apply((Applicative<WallTorchBlock, ?>)instance, WallTorchBlock::new));
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    private static final Map<Direction, VoxelShape> SHAPES_BY_DIRECTION = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidZShape(5.0, 3.0, 13.0, 11.0, 16.0));

    public MapCodec<WallTorchBlock> getCodec() {
        return CODEC;
    }

    protected WallTorchBlock(SimpleParticleType arg, AbstractBlock.Settings arg2) {
        super(arg, arg2);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return WallTorchBlock.getBoundingShape(state);
    }

    public static VoxelShape getBoundingShape(BlockState state) {
        return SHAPES_BY_DIRECTION.get(state.get(FACING));
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return WallTorchBlock.canPlaceAt(world, pos, state.get(FACING));
    }

    public static boolean canPlaceAt(WorldView world, BlockPos pos, Direction facing) {
        BlockPos lv = pos.offset(facing.getOpposite());
        BlockState lv2 = world.getBlockState(lv);
        return lv2.isSideSolidFullSquare(world, lv, facing);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction[] lvs;
        BlockState lv = this.getDefaultState();
        World lv2 = ctx.getWorld();
        BlockPos lv3 = ctx.getBlockPos();
        for (Direction lv4 : lvs = ctx.getPlacementDirections()) {
            Direction lv5;
            if (!lv4.getAxis().isHorizontal() || !(lv = (BlockState)lv.with(FACING, lv5 = lv4.getOpposite())).canPlaceAt(lv2, lv3)) continue;
            return lv;
        }
        return null;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        Direction lv = state.get(FACING);
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getY() + 0.7;
        double f = (double)pos.getZ() + 0.5;
        double g = 0.22;
        double h = 0.27;
        Direction lv2 = lv.getOpposite();
        world.addParticleClient(ParticleTypes.SMOKE, d + 0.27 * (double)lv2.getOffsetX(), e + 0.22, f + 0.27 * (double)lv2.getOffsetZ(), 0.0, 0.0, 0.0);
        world.addParticleClient(this.particle, d + 0.27 * (double)lv2.getOffsetX(), e + 0.22, f + 0.27 * (double)lv2.getOffsetZ(), 0.0, 0.0, 0.0);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}

