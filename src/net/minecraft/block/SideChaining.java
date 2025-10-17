/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.SideChainPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public interface SideChaining {
    public SideChainPart getSideChainPart(BlockState var1);

    public BlockState withSideChainPart(BlockState var1, SideChainPart var2);

    public Direction getFacing(BlockState var1);

    public boolean canChainWith(BlockState var1);

    public int getMaxSideChainLength();

    default public List<BlockPos> getPositionsInChain(WorldAccess world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        if (!this.canChainWith(lv)) {
            return List.of();
        }
        Neighbors lv2 = this.getNeighbors(world, pos, this.getFacing(lv));
        LinkedList<BlockPos> list = new LinkedList<BlockPos>();
        list.add(pos);
        this.forEachNeighborTowards(lv2::getLeftNeighbor, SideChainPart.LEFT, list::addFirst);
        this.forEachNeighborTowards(lv2::getRightNeighbor, SideChainPart.RIGHT, list::addLast);
        return list;
    }

    private void forEachNeighborTowards(IntFunction<Neighbor> neighborGetter, SideChainPart part, Consumer<BlockPos> posConsumer) {
        for (int i = 1; i < this.getMaxSideChainLength(); ++i) {
            Neighbor lv = neighborGetter.apply(i);
            if (lv.isCenterOr(part)) {
                posConsumer.accept(lv.pos());
            }
            if (lv.isNotCenter()) break;
        }
    }

    default public void disconnectNeighbors(WorldAccess world, BlockPos pos, BlockState state) {
        Neighbors lv = this.getNeighbors(world, pos, this.getFacing(state));
        lv.getLeftNeighbor().disconnectFromRight();
        lv.getRightNeighbor().disconnectFromLeft();
    }

    default public void connectNeighbors(WorldAccess world, BlockPos pos, BlockState state, BlockState oldState) {
        if (!this.canChainWith(state)) {
            return;
        }
        if (this.isAlreadyConnected(state, oldState)) {
            return;
        }
        Neighbors lv = this.getNeighbors(world, pos, this.getFacing(state));
        SideChainPart lv2 = SideChainPart.UNCONNECTED;
        int i = lv.getLeftNeighbor().isChained() ? this.getPositionsInChain(world, lv.getLeftNeighbor().pos()).size() : 0;
        int j = lv.getRightNeighbor().isChained() ? this.getPositionsInChain(world, lv.getRightNeighbor().pos()).size() : 0;
        int k = 1;
        if (this.canAddChainLength(i, k)) {
            lv2 = lv2.connectToLeft();
            lv.getLeftNeighbor().connectToRight();
            k += i;
        }
        if (this.canAddChainLength(j, k)) {
            lv2 = lv2.connectToRight();
            lv.getRightNeighbor().connectToLeft();
        }
        this.setSideChainPart(world, pos, lv2);
    }

    private boolean canAddChainLength(int chainLength, int toAdd) {
        return chainLength > 0 && toAdd + chainLength <= this.getMaxSideChainLength();
    }

    private boolean isAlreadyConnected(BlockState state, BlockState oldState) {
        boolean bl = this.getSideChainPart(state).isConnected();
        boolean bl2 = this.canChainWith(oldState) && this.getSideChainPart(oldState).isConnected();
        return bl || bl2;
    }

    private Neighbors getNeighbors(WorldAccess world, BlockPos pos, Direction facing) {
        return new Neighbors(this, world, facing, pos, new HashMap<BlockPos, Neighbor>());
    }

    default public void setSideChainPart(WorldAccess world, BlockPos pos, SideChainPart part) {
        BlockState lv = world.getBlockState(pos);
        if (this.getSideChainPart(lv) != part) {
            world.setBlockState(pos, this.withSideChainPart(lv, part), Block.NOTIFY_ALL);
        }
    }

    public record Neighbors(SideChaining block, WorldAccess world, Direction facing, BlockPos center, Map<BlockPos, Neighbor> cache) {
        private boolean canChainWith(BlockState state) {
            return this.block.canChainWith(state) && this.block.getFacing(state) == this.facing;
        }

        private Neighbor createNeighbor(BlockPos pos) {
            BlockState lv = this.world.getBlockState(pos);
            SideChainPart lv2 = this.canChainWith(lv) ? this.block.getSideChainPart(lv) : null;
            return lv2 == null ? new EmptyNeighbor(pos) : new SideChainNeighbor(this.world, this.block, pos, lv2);
        }

        private Neighbor getOrCreateNeighbor(Direction direction, Integer distance) {
            return this.cache.computeIfAbsent(this.center.offset(direction, (int)distance), this::createNeighbor);
        }

        public Neighbor getLeftNeighbor(int distance) {
            return this.getOrCreateNeighbor(this.facing.rotateYClockwise(), distance);
        }

        public Neighbor getRightNeighbor(int distance) {
            return this.getOrCreateNeighbor(this.facing.rotateYCounterclockwise(), distance);
        }

        public Neighbor getLeftNeighbor() {
            return this.getLeftNeighbor(1);
        }

        public Neighbor getRightNeighbor() {
            return this.getRightNeighbor(1);
        }
    }

    public static sealed interface Neighbor
    permits EmptyNeighbor, SideChainNeighbor {
        public BlockPos pos();

        public boolean isChained();

        public boolean isNotCenter();

        public boolean isCenterOr(SideChainPart var1);

        default public void connectToRight() {
        }

        default public void connectToLeft() {
        }

        default public void disconnectFromRight() {
        }

        default public void disconnectFromLeft() {
        }
    }

    public record SideChainNeighbor(WorldAccess level, SideChaining block, BlockPos pos, SideChainPart part) implements Neighbor
    {
        @Override
        public boolean isChained() {
            return true;
        }

        @Override
        public boolean isNotCenter() {
            return this.part.isNotCenter();
        }

        @Override
        public boolean isCenterOr(SideChainPart part) {
            return this.part.isCenterOr(part);
        }

        @Override
        public void connectToRight() {
            this.block.setSideChainPart(this.level, this.pos, this.part.connectToRight());
        }

        @Override
        public void connectToLeft() {
            this.block.setSideChainPart(this.level, this.pos, this.part.connectToLeft());
        }

        @Override
        public void disconnectFromRight() {
            this.block.setSideChainPart(this.level, this.pos, this.part.disconnectFromRight());
        }

        @Override
        public void disconnectFromLeft() {
            this.block.setSideChainPart(this.level, this.pos, this.part.disconnectFromLeft());
        }
    }

    public record EmptyNeighbor(BlockPos pos) implements Neighbor
    {
        @Override
        public boolean isChained() {
            return false;
        }

        @Override
        public boolean isNotCenter() {
            return true;
        }

        @Override
        public boolean isCenterOr(SideChainPart part) {
            return false;
        }
    }
}

