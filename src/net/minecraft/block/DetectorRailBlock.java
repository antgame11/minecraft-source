/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RailPlacementHelper;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DetectorRailBlock
extends AbstractRailBlock {
    public static final MapCodec<DetectorRailBlock> CODEC = DetectorRailBlock.createCodec(DetectorRailBlock::new);
    public static final EnumProperty<RailShape> SHAPE = Properties.STRAIGHT_RAIL_SHAPE;
    public static final BooleanProperty POWERED = Properties.POWERED;
    private static final int SCHEDULED_TICK_DELAY = 20;

    public MapCodec<DetectorRailBlock> getCodec() {
        return CODEC;
    }

    public DetectorRailBlock(AbstractBlock.Settings arg) {
        super(true, arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWERED, false)).with(SHAPE, RailShape.NORTH_SOUTH)).with(WATERLOGGED, false));
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (world.isClient()) {
            return;
        }
        if (state.get(POWERED).booleanValue()) {
            return;
        }
        this.updatePoweredStatus(world, pos, state);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.get(POWERED).booleanValue()) {
            return;
        }
        this.updatePoweredStatus(world, pos, state);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) != false ? 15 : 0;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!state.get(POWERED).booleanValue()) {
            return 0;
        }
        return direction == Direction.UP ? 15 : 0;
    }

    private void updatePoweredStatus(World world, BlockPos pos, BlockState state) {
        BlockState lv;
        if (!this.canPlaceAt(state, world, pos)) {
            return;
        }
        boolean bl = state.get(POWERED);
        boolean bl2 = false;
        List<AbstractMinecartEntity> list = this.getCarts(world, pos, AbstractMinecartEntity.class, entity -> true);
        if (!list.isEmpty()) {
            bl2 = true;
        }
        if (bl2 && !bl) {
            lv = (BlockState)state.with(POWERED, true);
            world.setBlockState(pos, lv, Block.NOTIFY_ALL);
            this.updateNearbyRails(world, pos, lv, true);
            world.updateNeighbors(pos, this);
            world.updateNeighbors(pos.down(), this);
            world.scheduleBlockRerenderIfNeeded(pos, state, lv);
        }
        if (!bl2 && bl) {
            lv = (BlockState)state.with(POWERED, false);
            world.setBlockState(pos, lv, Block.NOTIFY_ALL);
            this.updateNearbyRails(world, pos, lv, false);
            world.updateNeighbors(pos, this);
            world.updateNeighbors(pos.down(), this);
            world.scheduleBlockRerenderIfNeeded(pos, state, lv);
        }
        if (bl2) {
            world.scheduleBlockTick(pos, this, 20);
        }
        world.updateComparators(pos, this);
    }

    protected void updateNearbyRails(World world, BlockPos pos, BlockState state, boolean unpowering) {
        RailPlacementHelper lv = new RailPlacementHelper(world, pos, state);
        List<BlockPos> list = lv.getNeighbors();
        for (BlockPos lv2 : list) {
            BlockState lv3 = world.getBlockState(lv2);
            world.updateNeighbor(lv3, lv2, lv3.getBlock(), null, false);
        }
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        BlockState lv = this.updateCurves(state, world, pos, notify);
        this.updatePoweredStatus(world, pos, lv);
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        if (state.get(POWERED).booleanValue()) {
            List<CommandBlockMinecartEntity> list = this.getCarts(world, pos, CommandBlockMinecartEntity.class, cart -> true);
            if (!list.isEmpty()) {
                return list.get(0).getCommandExecutor().getSuccessCount();
            }
            List<AbstractMinecartEntity> list2 = this.getCarts(world, pos, AbstractMinecartEntity.class, EntityPredicates.VALID_INVENTORIES);
            if (!list2.isEmpty()) {
                return ScreenHandler.calculateComparatorOutput((Inventory)((Object)list2.get(0)));
            }
        }
        return 0;
    }

    private <T extends AbstractMinecartEntity> List<T> getCarts(World world, BlockPos pos, Class<T> entityClass, Predicate<Entity> entityPredicate) {
        return world.getEntitiesByClass(entityClass, this.getCartDetectionBox(pos), entityPredicate);
    }

    private Box getCartDetectionBox(BlockPos pos) {
        double d = 0.2;
        return new Box((double)pos.getX() + 0.2, pos.getY(), (double)pos.getZ() + 0.2, (double)(pos.getX() + 1) - 0.2, (double)(pos.getY() + 1) - 0.2, (double)(pos.getZ() + 1) - 0.2);
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
        builder.add(SHAPE, POWERED, WATERLOGGED);
    }
}

