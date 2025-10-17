/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.block;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.NeighborUpdater;
import net.minecraft.world.block.OrientationHelper;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChainRestrictedNeighborUpdater
implements NeighborUpdater {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final World world;
    private final int maxChainDepth;
    private final ArrayDeque<Entry> queue = new ArrayDeque();
    private final List<Entry> pending = new ArrayList<Entry>();
    private int depth = 0;
    @Nullable
    private Consumer<BlockPos> neighborUpdateCallback;

    public ChainRestrictedNeighborUpdater(World world, int maxChainDepth) {
        this.world = world;
        this.maxChainDepth = maxChainDepth;
    }

    public void setNeighborUpdateCallback(@Nullable Consumer<BlockPos> neighborUpdateCallback) {
        this.neighborUpdateCallback = neighborUpdateCallback;
    }

    @Override
    public void replaceWithStateForNeighborUpdate(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth) {
        this.enqueue(pos, new StateReplacementEntry(direction, neighborState, pos.toImmutable(), neighborPos.toImmutable(), flags, maxUpdateDepth));
    }

    @Override
    public void updateNeighbor(BlockPos pos, Block sourceBlock, @Nullable WireOrientation orientation) {
        this.enqueue(pos, new SimpleEntry(pos, sourceBlock, orientation));
    }

    @Override
    public void updateNeighbor(BlockState state, BlockPos pos, Block sourceBlock, @Nullable WireOrientation orientation, boolean notify) {
        this.enqueue(pos, new StatefulEntry(state, pos.toImmutable(), sourceBlock, orientation, notify));
    }

    @Override
    public void updateNeighbors(BlockPos pos, Block sourceBlock, @Nullable Direction except, @Nullable WireOrientation orientation) {
        this.enqueue(pos, new SixWayEntry(pos.toImmutable(), sourceBlock, orientation, except));
    }

    private void enqueue(BlockPos pos, Entry entry) {
        boolean bl = this.depth > 0;
        boolean bl2 = this.maxChainDepth >= 0 && this.depth >= this.maxChainDepth;
        ++this.depth;
        if (!bl2) {
            if (bl) {
                this.pending.add(entry);
            } else {
                this.queue.push(entry);
            }
        } else if (this.depth - 1 == this.maxChainDepth) {
            LOGGER.error("Too many chained neighbor updates. Skipping the rest. First skipped position: {}", (Object)pos.toShortString());
        }
        if (!bl) {
            this.runQueuedUpdates();
        }
    }

    private void runQueuedUpdates() {
        try {
            block3: while (!this.queue.isEmpty() || !this.pending.isEmpty()) {
                for (int i = this.pending.size() - 1; i >= 0; --i) {
                    this.queue.push(this.pending.get(i));
                }
                this.pending.clear();
                Entry lv = this.queue.peek();
                if (this.neighborUpdateCallback != null) {
                    lv.runCallback(this.neighborUpdateCallback);
                }
                while (this.pending.isEmpty()) {
                    if (lv.update(this.world)) continue;
                    this.queue.pop();
                    continue block3;
                }
            }
        } finally {
            this.queue.clear();
            this.pending.clear();
            this.depth = 0;
        }
    }

    record StateReplacementEntry(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int updateFlags, int updateLimit) implements Entry
    {
        @Override
        public boolean update(World world) {
            NeighborUpdater.replaceWithStateForNeighborUpdate(world, this.direction, this.pos, this.neighborPos, this.neighborState, this.updateFlags, this.updateLimit);
            return false;
        }

        @Override
        public void runCallback(Consumer<BlockPos> callback) {
            callback.accept(this.pos);
        }
    }

    static interface Entry {
        public boolean update(World var1);

        public void runCallback(Consumer<BlockPos> var1);
    }

    record SimpleEntry(BlockPos pos, Block sourceBlock, @Nullable WireOrientation orientation) implements Entry
    {
        @Override
        public boolean update(World world) {
            BlockState lv = world.getBlockState(this.pos);
            NeighborUpdater.tryNeighborUpdate(world, lv, this.pos, this.sourceBlock, this.orientation, false);
            return false;
        }

        @Override
        public void runCallback(Consumer<BlockPos> callback) {
            callback.accept(this.pos);
        }

        @Nullable
        public WireOrientation orientation() {
            return this.orientation;
        }
    }

    record StatefulEntry(BlockState state, BlockPos pos, Block sourceBlock, @Nullable WireOrientation orientation, boolean movedByPiston) implements Entry
    {
        @Override
        public boolean update(World world) {
            NeighborUpdater.tryNeighborUpdate(world, this.state, this.pos, this.sourceBlock, this.orientation, this.movedByPiston);
            return false;
        }

        @Override
        public void runCallback(Consumer<BlockPos> callback) {
            callback.accept(this.pos);
        }

        @Nullable
        public WireOrientation orientation() {
            return this.orientation;
        }
    }

    static final class SixWayEntry
    implements Entry {
        private final BlockPos pos;
        private final Block sourceBlock;
        @Nullable
        private WireOrientation orientation;
        @Nullable
        private final Direction except;
        private int currentDirectionIndex = 0;

        SixWayEntry(BlockPos pos, Block sourceBlock, @Nullable WireOrientation orientation, @Nullable Direction except) {
            this.pos = pos;
            this.sourceBlock = sourceBlock;
            this.orientation = orientation;
            this.except = except;
            if (NeighborUpdater.UPDATE_ORDER[this.currentDirectionIndex] == except) {
                ++this.currentDirectionIndex;
            }
        }

        @Override
        public boolean update(World world) {
            Direction lv = NeighborUpdater.UPDATE_ORDER[this.currentDirectionIndex++];
            BlockPos lv2 = this.pos.offset(lv);
            BlockState lv3 = world.getBlockState(lv2);
            WireOrientation lv4 = null;
            if (world.getEnabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS)) {
                if (this.orientation == null) {
                    this.orientation = OrientationHelper.getEmissionOrientation(world, this.except == null ? null : this.except.getOpposite(), null);
                }
                lv4 = this.orientation.withFront(lv);
            }
            NeighborUpdater.tryNeighborUpdate(world, lv3, lv2, this.sourceBlock, lv4, false);
            if (this.currentDirectionIndex < NeighborUpdater.UPDATE_ORDER.length && NeighborUpdater.UPDATE_ORDER[this.currentDirectionIndex] == this.except) {
                ++this.currentDirectionIndex;
            }
            return this.currentDirectionIndex < NeighborUpdater.UPDATE_ORDER.length;
        }

        @Override
        public void runCallback(Consumer<BlockPos> callback) {
            for (Direction lv : NeighborUpdater.UPDATE_ORDER) {
                if (lv == this.except) continue;
                BlockPos lv2 = this.pos.offset(lv);
                callback.accept(lv2);
            }
        }
    }
}

