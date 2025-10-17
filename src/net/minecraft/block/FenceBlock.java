/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.LeadItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class FenceBlock
extends HorizontalConnectingBlock {
    public static final MapCodec<FenceBlock> CODEC = FenceBlock.createCodec(FenceBlock::new);
    private final Function<BlockState, VoxelShape> cullingShapeFunction;

    public MapCodec<FenceBlock> getCodec() {
        return CODEC;
    }

    public FenceBlock(AbstractBlock.Settings arg) {
        super(4.0f, 16.0f, 4.0f, 16.0f, 24.0f, arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false)).with(WATERLOGGED, false));
        this.cullingShapeFunction = this.createShapeFunction(4.0f, 16.0f, 2.0f, 6.0f, 15.0f);
    }

    @Override
    protected VoxelShape getCullingShape(BlockState state) {
        return this.cullingShapeFunction.apply(state);
    }

    @Override
    protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getOutlineShape(state, world, pos, context);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    public boolean canConnect(BlockState state, boolean neighborIsFullSquare, Direction dir) {
        Block lv = state.getBlock();
        boolean bl2 = this.canConnectToFence(state);
        boolean bl3 = lv instanceof FenceGateBlock && FenceGateBlock.canWallConnect(state, dir);
        return !FenceBlock.cannotConnect(state) && neighborIsFullSquare || bl2 || bl3;
    }

    private boolean canConnectToFence(BlockState state) {
        return state.isIn(BlockTags.FENCES) && state.isIn(BlockTags.WOODEN_FENCES) == this.getDefaultState().isIn(BlockTags.WOODEN_FENCES);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return !world.isClient() ? LeadItem.attachHeldMobsToBlock(player, world, pos) : ActionResult.PASS;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World lv = ctx.getWorld();
        BlockPos lv2 = ctx.getBlockPos();
        FluidState lv3 = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockPos lv4 = lv2.north();
        BlockPos lv5 = lv2.east();
        BlockPos lv6 = lv2.south();
        BlockPos lv7 = lv2.west();
        BlockState lv8 = lv.getBlockState(lv4);
        BlockState lv9 = lv.getBlockState(lv5);
        BlockState lv10 = lv.getBlockState(lv6);
        BlockState lv11 = lv.getBlockState(lv7);
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)super.getPlacementState(ctx).with(NORTH, this.canConnect(lv8, lv8.isSideSolidFullSquare(lv, lv4, Direction.SOUTH), Direction.SOUTH))).with(EAST, this.canConnect(lv9, lv9.isSideSolidFullSquare(lv, lv5, Direction.WEST), Direction.WEST))).with(SOUTH, this.canConnect(lv10, lv10.isSideSolidFullSquare(lv, lv6, Direction.NORTH), Direction.NORTH))).with(WEST, this.canConnect(lv11, lv11.isSideSolidFullSquare(lv, lv7, Direction.EAST), Direction.EAST))).with(WATERLOGGED, lv3.getFluid() == Fluids.WATER);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction.getAxis().isHorizontal()) {
            return (BlockState)state.with((Property)FACING_PROPERTIES.get(direction), this.canConnect(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite()), direction.getOpposite()));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }
}

