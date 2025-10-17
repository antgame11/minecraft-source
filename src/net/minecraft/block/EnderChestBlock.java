/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class EnderChestBlock
extends AbstractChestBlock<EnderChestBlockEntity>
implements Waterloggable {
    public static final MapCodec<EnderChestBlock> CODEC = EnderChestBlock.createCodec(EnderChestBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.createColumnShape(14.0, 0.0, 14.0);
    private static final Text CONTAINER_NAME = Text.translatable("container.enderchest");

    @Override
    public MapCodec<EnderChestBlock> getCodec() {
        return CODEC;
    }

    protected EnderChestBlock(AbstractBlock.Settings arg) {
        super(arg, () -> BlockEntityType.ENDER_CHEST);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, false));
    }

    @Override
    public DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> getBlockEntitySource(BlockState state, World world, BlockPos pos, boolean ignoreBlocked) {
        return DoubleBlockProperties.PropertyRetriever::getFallback;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())).with(WATERLOGGED, lv.getFluid() == Fluids.WATER);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player2, BlockHitResult hit) {
        EnderChestInventory lv = player2.getEnderChestInventory();
        BlockEntity lv2 = world.getBlockEntity(pos);
        if (lv == null || !(lv2 instanceof EnderChestBlockEntity)) {
            return ActionResult.SUCCESS;
        }
        EnderChestBlockEntity lv3 = (EnderChestBlockEntity)lv2;
        BlockPos lv4 = pos.up();
        if (world.getBlockState(lv4).isSolidBlock(world, lv4)) {
            return ActionResult.SUCCESS;
        }
        if (world instanceof ServerWorld) {
            ServerWorld lv5 = (ServerWorld)world;
            lv.setActiveBlockEntity(lv3);
            player2.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, lv), CONTAINER_NAME));
            player2.incrementStat(Stats.OPEN_ENDERCHEST);
            PiglinBrain.onGuardedBlockInteracted(lv5, player2, true);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnderChestBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient() ? EnderChestBlock.validateTicker(type, BlockEntityType.ENDER_CHEST, EnderChestBlockEntity::clientTick) : null;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        for (int i = 0; i < 3; ++i) {
            int j = random.nextInt(2) * 2 - 1;
            int k = random.nextInt(2) * 2 - 1;
            double d = (double)pos.getX() + 0.5 + 0.25 * (double)j;
            double e = (float)pos.getY() + random.nextFloat();
            double f = (double)pos.getZ() + 0.5 + 0.25 * (double)k;
            double g = random.nextFloat() * (float)j;
            double h = ((double)random.nextFloat() - 0.5) * 0.125;
            double l = random.nextFloat() * (float)k;
            world.addParticleClient(ParticleTypes.PORTAL, d, e, f, g, h, l);
        }
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
        builder.add(FACING, WATERLOGGED);
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
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof EnderChestBlockEntity) {
            ((EnderChestBlockEntity)lv).onScheduledTick();
        }
    }
}

