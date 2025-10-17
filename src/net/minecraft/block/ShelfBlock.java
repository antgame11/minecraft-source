/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.InteractibleSlotContainer;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SideChaining;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShelfBlockEntity;
import net.minecraft.block.enums.SideChainPart;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class ShelfBlock
extends BlockWithEntity
implements InteractibleSlotContainer,
SideChaining,
Waterloggable {
    public static final MapCodec<ShelfBlock> CODEC = ShelfBlock.createCodec(ShelfBlock::new);
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<SideChainPart> SIDE_CHAIN = Properties.SIDE_CHAIN;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final Map<Direction, VoxelShape> SHAPES = VoxelShapes.createHorizontalFacingShapeMap(VoxelShapes.union(Block.createCuboidShape(0.0, 12.0, 11.0, 16.0, 16.0, 13.0), Block.createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0), Block.createCuboidShape(0.0, 0.0, 11.0, 16.0, 4.0, 13.0)));

    public MapCodec<ShelfBlock> getCodec() {
        return CODEC;
    }

    public ShelfBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false)).with(SIDE_CHAIN, SideChainPart.UNCONNECTED)).with(WATERLOGGED, false));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(FACING));
    }

    @Override
    protected boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return type == NavigationType.WATER && state.getFluidState().isIn(FluidTags.WATER);
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShelfBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, SIDE_CHAIN, WATERLOGGED);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
        this.disconnectNeighbors(world, pos, state);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        if (world.isClient()) {
            return;
        }
        boolean bl2 = world.isReceivingRedstonePower(pos);
        if (state.get(POWERED) != bl2) {
            BlockState lv = (BlockState)state.with(POWERED, bl2);
            if (!bl2) {
                lv = (BlockState)lv.with(SIDE_CHAIN, SideChainPart.UNCONNECTED);
            }
            world.setBlockState(pos, lv, Block.NOTIFY_ALL);
            this.playSound(world, pos, bl2 ? SoundEvents.BLOCK_SHELF_ACTIVATE : SoundEvents.BLOCK_SHELF_DEACTIVATE);
            world.emitGameEvent(bl2 ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Emitter.of(lv));
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())).with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()))).with(WATERLOGGED, lv.getFluid() == Fluids.WATER);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public int getRows() {
        return 1;
    }

    @Override
    public int getColumns() {
        return 3;
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ShelfBlockEntity lv;
        block13: {
            block12: {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (!(blockEntity instanceof ShelfBlockEntity)) break block12;
                lv = (ShelfBlockEntity)blockEntity;
                if (!hand.equals((Object)Hand.OFF_HAND)) break block13;
            }
            return ActionResult.PASS;
        }
        OptionalInt optionalInt = this.getHitSlot(hit, state.get(FACING));
        if (optionalInt.isEmpty()) {
            return ActionResult.PASS;
        }
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        PlayerInventory lv2 = player.getInventory();
        if (!state.get(POWERED).booleanValue()) {
            boolean bl = ShelfBlock.swapSingleStack(stack, player, lv, optionalInt.getAsInt(), lv2);
            if (bl) {
                this.playSound(world, pos, stack.isEmpty() ? SoundEvents.BLOCK_SHELF_TAKE_ITEM : SoundEvents.BLOCK_SHELF_SINGLE_SWAP);
            } else if (!stack.isEmpty()) {
                this.playSound(world, pos, SoundEvents.BLOCK_SHELF_PLACE_ITEM);
            } else {
                return ActionResult.PASS;
            }
            return ActionResult.SUCCESS.withNewHandStack(stack);
        }
        ItemStack lv3 = lv2.getSelectedStack();
        boolean bl2 = this.swapAllStacks(world, pos, lv2);
        if (!bl2) {
            return ActionResult.CONSUME;
        }
        this.playSound(world, pos, SoundEvents.BLOCK_SHELF_MULTI_SWAP);
        if (lv3 == lv2.getSelectedStack()) {
            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS.withNewHandStack(lv2.getSelectedStack());
    }

    private static boolean swapSingleStack(ItemStack stack, PlayerEntity player, ShelfBlockEntity blockEntity, int hitSlot, PlayerInventory playerInventory) {
        ItemStack lv = blockEntity.swapStackNoMarkDirty(hitSlot, stack);
        ItemStack lv2 = player.isInCreativeMode() && lv.isEmpty() ? stack.copy() : lv;
        playerInventory.setStack(playerInventory.getSelectedSlot(), lv2);
        playerInventory.markDirty();
        blockEntity.markDirty(GameEvent.ITEM_INTERACT_FINISH);
        return !lv.isEmpty();
    }

    private boolean swapAllStacks(World world, BlockPos pos, PlayerInventory playerInventory) {
        List<BlockPos> list = this.getPositionsInChain(world, pos);
        if (list.isEmpty()) {
            return false;
        }
        boolean bl = false;
        for (int i = 0; i < list.size(); ++i) {
            ShelfBlockEntity lv = (ShelfBlockEntity)world.getBlockEntity(list.get(i));
            if (lv == null) continue;
            for (int j = 0; j < lv.size(); ++j) {
                int k = 9 - (list.size() - i) * lv.size() + j;
                if (k < 0 || k > playerInventory.size()) continue;
                ItemStack lv2 = playerInventory.removeStack(k);
                ItemStack lv3 = lv.swapStackNoMarkDirty(j, lv2);
                if (lv2.isEmpty() && lv3.isEmpty()) continue;
                playerInventory.setStack(k, lv3);
                bl = true;
            }
            playerInventory.markDirty();
            lv.markDirty(GameEvent.ENTITY_INTERACT);
        }
        return bl;
    }

    @Override
    public SideChainPart getSideChainPart(BlockState state) {
        return state.get(SIDE_CHAIN);
    }

    @Override
    public BlockState withSideChainPart(BlockState state, SideChainPart sideChainPart) {
        return (BlockState)state.with(SIDE_CHAIN, sideChainPart);
    }

    @Override
    public Direction getFacing(BlockState state) {
        return state.get(FACING);
    }

    @Override
    public boolean canChainWith(BlockState state) {
        return state.isIn(BlockTags.WOODEN_SHELVES) && state.contains(POWERED) && state.get(POWERED) != false;
    }

    @Override
    public int getMaxSideChainLength() {
        return 3;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.get(POWERED).booleanValue()) {
            this.connectNeighbors(world, pos, state, oldState);
        } else {
            this.disconnectNeighbors(world, pos, state);
        }
    }

    private void playSound(WorldAccess world, BlockPos pos, SoundEvent sound) {
        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        if (world.isClient()) {
            return 0;
        }
        if (direction != state.get(FACING).getOpposite()) {
            return 0;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ShelfBlockEntity) {
            ShelfBlockEntity lv = (ShelfBlockEntity)blockEntity;
            int i = lv.getStack(0).isEmpty() ? 0 : 1;
            int j = lv.getStack(1).isEmpty() ? 0 : 1;
            int k = lv.getStack(2).isEmpty() ? 0 : 1;
            return i | j << 1 | k << 2;
        }
        return 0;
    }
}

