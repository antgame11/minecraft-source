/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.AbstractChunkRenderData;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRenderData;
import net.minecraft.client.render.chunk.Octree;
import net.minecraft.server.network.ChunkFilter;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HeightLimitView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ChunkRenderingDataPreparer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int DEFAULT_SECTION_DISTANCE = 60;
    private static final int SECTION_DISTANCE = ChunkSectionPos.getSectionCoord(60);
    private static final double CHUNK_INNER_DIAGONAL_LENGTH = Math.ceil(Math.sqrt(3.0) * 16.0);
    private boolean terrainUpdateScheduled = true;
    @Nullable
    private Future<?> terrainUpdateFuture;
    @Nullable
    private BuiltChunkStorage builtChunkStorage;
    private final AtomicReference<PreparerState> state = new AtomicReference();
    private final AtomicReference<Events> events = new AtomicReference();
    private final AtomicBoolean needsUpdate = new AtomicBoolean(false);

    public void setStorage(@Nullable BuiltChunkStorage storage) {
        if (this.terrainUpdateFuture != null) {
            try {
                this.terrainUpdateFuture.get();
                this.terrainUpdateFuture = null;
            } catch (Exception exception) {
                LOGGER.warn("Full update failed", exception);
            }
        }
        this.builtChunkStorage = storage;
        if (storage != null) {
            this.state.set(new PreparerState(storage));
            this.scheduleTerrainUpdate();
        } else {
            this.state.set(null);
        }
    }

    public void scheduleTerrainUpdate() {
        this.terrainUpdateScheduled = true;
    }

    public void collectChunks(Frustum frustum, List<ChunkBuilder.BuiltChunk> builtChunks, List<ChunkBuilder.BuiltChunk> nearbyChunks) {
        this.state.get().storage().octree.visit((node, skipVisibilityCheck, depth, nearCenter) -> {
            ChunkBuilder.BuiltChunk lv = node.getBuiltChunk();
            if (lv != null) {
                builtChunks.add(lv);
                if (nearCenter) {
                    nearbyChunks.add(lv);
                }
            }
        }, frustum, 32);
    }

    public boolean updateFrustum() {
        return this.needsUpdate.compareAndSet(true, false);
    }

    public void addNeighbors(ChunkPos chunkPos) {
        Events lv2;
        Events lv = this.events.get();
        if (lv != null) {
            this.addNeighbors(lv, chunkPos);
        }
        if ((lv2 = this.state.get().events) != lv) {
            this.addNeighbors(lv2, chunkPos);
        }
    }

    public void schedulePropagationFrom(ChunkBuilder.BuiltChunk builtChunk) {
        Events lv2;
        Events lv = this.events.get();
        if (lv != null) {
            lv.sectionsToPropagateFrom.add(builtChunk);
        }
        if ((lv2 = this.state.get().events) != lv) {
            lv2.sectionsToPropagateFrom.add(builtChunk);
        }
    }

    public void updateSectionOcclusionGraph(boolean cullChunks, Camera camera, Frustum frustum, List<ChunkBuilder.BuiltChunk> builtChunk, LongOpenHashSet activeSections) {
        Vec3d lv = camera.getPos();
        if (this.terrainUpdateScheduled && (this.terrainUpdateFuture == null || this.terrainUpdateFuture.isDone())) {
            this.updateTerrain(cullChunks, camera, lv, activeSections);
        }
        this.updateNow(cullChunks, frustum, builtChunk, lv, activeSections);
    }

    private void updateTerrain(boolean cullChunks, Camera camera, Vec3d cameraPos, LongOpenHashSet activeSections) {
        this.terrainUpdateScheduled = false;
        LongOpenHashSet longOpenHashSet2 = activeSections.clone();
        this.terrainUpdateFuture = CompletableFuture.runAsync(() -> {
            PreparerState lv = new PreparerState(this.builtChunkStorage);
            this.events.set(lv.events);
            ArrayDeque<ChunkInfo> queue = Queues.newArrayDeque();
            this.scheduleLater(camera, queue);
            queue.forEach(info -> arg.storage.infoList.setInfo(info.chunk, (ChunkInfo)info));
            this.update(lv.storage, cameraPos, queue, cullChunks, arg -> {}, longOpenHashSet2);
            this.state.set(lv);
            this.events.set(null);
            this.needsUpdate.set(true);
        }, Util.getMainWorkerExecutor());
    }

    private void updateNow(boolean cullChunks, Frustum frustum, List<ChunkBuilder.BuiltChunk> builtChunks, Vec3d cameraPos, LongOpenHashSet activeSections) {
        PreparerState lv = this.state.get();
        this.scheduleNew(lv);
        if (!lv.events.sectionsToPropagateFrom.isEmpty()) {
            ArrayDeque<ChunkInfo> queue = Queues.newArrayDeque();
            while (!lv.events.sectionsToPropagateFrom.isEmpty()) {
                ChunkBuilder.BuiltChunk lv2 = (ChunkBuilder.BuiltChunk)lv.events.sectionsToPropagateFrom.poll();
                ChunkInfo lv3 = lv.storage.infoList.getInfo(lv2);
                if (lv3 == null || lv3.chunk != lv2) continue;
                queue.add(lv3);
            }
            Frustum lv4 = WorldRenderer.offsetFrustum(frustum);
            Consumer<ChunkBuilder.BuiltChunk> consumer = arg2 -> {
                if (lv4.isVisible(arg2.getBoundingBox())) {
                    this.needsUpdate.set(true);
                }
            };
            this.update(lv.storage, cameraPos, queue, cullChunks, consumer, activeSections);
        }
    }

    private void scheduleNew(PreparerState preparerState) {
        LongIterator longIterator = preparerState.events.chunksWhichReceivedNeighbors.iterator();
        while (longIterator.hasNext()) {
            long l = longIterator.nextLong();
            List list = (List)preparerState.storage.queue.get(l);
            if (list == null || !((ChunkBuilder.BuiltChunk)list.get(0)).shouldBuild()) continue;
            preparerState.events.sectionsToPropagateFrom.addAll(list);
            preparerState.storage.queue.remove(l);
        }
        preparerState.events.chunksWhichReceivedNeighbors.clear();
    }

    private void addNeighbors(Events events, ChunkPos chunkPos) {
        events.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x - 1, chunkPos.z));
        events.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x, chunkPos.z - 1));
        events.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x + 1, chunkPos.z));
        events.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x, chunkPos.z + 1));
        events.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x - 1, chunkPos.z - 1));
        events.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x - 1, chunkPos.z + 1));
        events.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x + 1, chunkPos.z - 1));
        events.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x + 1, chunkPos.z + 1));
    }

    private void scheduleLater(Camera camera, Queue<ChunkInfo> queue) {
        BlockPos lv = camera.getBlockPos();
        long l = ChunkSectionPos.toLong(lv);
        int i = ChunkSectionPos.unpackY(l);
        ChunkBuilder.BuiltChunk lv2 = this.builtChunkStorage.getRenderedChunk(l);
        if (lv2 == null) {
            HeightLimitView lv3 = this.builtChunkStorage.getWorld();
            boolean bl = i < lv3.getBottomSectionCoord();
            int j = bl ? lv3.getBottomSectionCoord() : lv3.getTopSectionCoord();
            int k = this.builtChunkStorage.getViewDistance();
            ArrayList<ChunkInfo> list = Lists.newArrayList();
            int m = ChunkSectionPos.unpackX(l);
            int n = ChunkSectionPos.unpackZ(l);
            for (int o = -k; o <= k; ++o) {
                for (int p = -k; p <= k; ++p) {
                    ChunkBuilder.BuiltChunk lv4 = this.builtChunkStorage.getRenderedChunk(ChunkSectionPos.asLong(o + m, j, p + n));
                    if (lv4 == null || !this.isWithinViewDistance(l, lv4.getSectionPos())) continue;
                    Direction lv5 = bl ? Direction.UP : Direction.DOWN;
                    ChunkInfo lv6 = new ChunkInfo(lv4, lv5, 0);
                    lv6.updateCullingState(lv6.cullingState, lv5);
                    if (o > 0) {
                        lv6.updateCullingState(lv6.cullingState, Direction.EAST);
                    } else if (o < 0) {
                        lv6.updateCullingState(lv6.cullingState, Direction.WEST);
                    }
                    if (p > 0) {
                        lv6.updateCullingState(lv6.cullingState, Direction.SOUTH);
                    } else if (p < 0) {
                        lv6.updateCullingState(lv6.cullingState, Direction.NORTH);
                    }
                    list.add(lv6);
                }
            }
            list.sort(Comparator.comparingDouble(chunkInfo -> lv.getSquaredDistance(ChunkSectionPos.from(chunkInfo.chunk.getSectionPos()).getCenterPos())));
            queue.addAll(list);
        } else {
            queue.add(new ChunkInfo(lv2, null, 0));
        }
    }

    private void update(RenderableChunks renderableChunks, Vec3d pos, Queue<ChunkInfo> queue, boolean cullChunks, Consumer<ChunkBuilder.BuiltChunk> consumer, LongOpenHashSet longOpenHashSet) {
        ChunkSectionPos lv = ChunkSectionPos.from(pos);
        long l2 = lv.asLong();
        BlockPos lv2 = lv.getCenterPos();
        while (!queue.isEmpty()) {
            long m;
            ChunkInfo lv3 = queue.poll();
            ChunkBuilder.BuiltChunk lv4 = lv3.chunk;
            if (!longOpenHashSet.contains(lv3.chunk.getSectionPos())) {
                if (renderableChunks.octree.add(lv3.chunk)) {
                    consumer.accept(lv3.chunk);
                }
            } else {
                lv3.chunk.currentRenderData.compareAndSet(ChunkRenderData.HIDDEN, ChunkRenderData.READY);
            }
            boolean bl2 = Math.abs(ChunkSectionPos.unpackX(m = lv4.getSectionPos()) - lv.getSectionX()) > SECTION_DISTANCE || Math.abs(ChunkSectionPos.unpackY(m) - lv.getSectionY()) > SECTION_DISTANCE || Math.abs(ChunkSectionPos.unpackZ(m) - lv.getSectionZ()) > SECTION_DISTANCE;
            for (Direction lv5 : DIRECTIONS) {
                ChunkInfo lv10;
                int i;
                ChunkBuilder.BuiltChunk lv6 = this.getRenderedChunk(l2, lv4, lv5);
                if (lv6 == null || cullChunks && lv3.canCull(lv5.getOpposite())) continue;
                if (cullChunks && lv3.hasAnyDirection()) {
                    AbstractChunkRenderData lv7 = lv4.getCurrentRenderData();
                    boolean bl3 = false;
                    for (i = 0; i < DIRECTIONS.length; ++i) {
                        if (!lv3.hasDirection(i) || !lv7.isVisibleThrough(DIRECTIONS[i].getOpposite(), lv5)) continue;
                        bl3 = true;
                        break;
                    }
                    if (!bl3) continue;
                }
                if (cullChunks && bl2) {
                    boolean bl5;
                    boolean bl4;
                    int j = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(m));
                    int k = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(m));
                    i = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(m));
                    boolean bl = lv5.getAxis() == Direction.Axis.X ? lv2.getX() > j : (bl4 = lv2.getX() < j);
                    boolean bl3 = lv5.getAxis() == Direction.Axis.Y ? lv2.getY() > k : (bl5 = lv2.getY() < k);
                    boolean bl6 = lv5.getAxis() == Direction.Axis.Z ? lv2.getZ() > i : lv2.getZ() < i;
                    Vector3d vector3d = new Vector3d(j + (bl4 ? 16 : 0), k + (bl5 ? 16 : 0), i + (bl6 ? 16 : 0));
                    Vector3d vector3d2 = new Vector3d(pos.x, pos.y, pos.z).sub(vector3d).normalize().mul(CHUNK_INNER_DIAGONAL_LENGTH);
                    boolean bl7 = true;
                    while (vector3d.distanceSquared(pos.x, pos.y, pos.z) > 3600.0) {
                        vector3d.add(vector3d2);
                        HeightLimitView lv8 = this.builtChunkStorage.getWorld();
                        if (vector3d.y > (double)lv8.getTopYInclusive() || vector3d.y < (double)lv8.getBottomY()) break;
                        ChunkBuilder.BuiltChunk lv9 = this.builtChunkStorage.getRenderedChunk(BlockPos.ofFloored(vector3d.x, vector3d.y, vector3d.z));
                        if (lv9 != null && renderableChunks.infoList.getInfo(lv9) != null) continue;
                        bl7 = false;
                        break;
                    }
                    if (!bl7) continue;
                }
                if ((lv10 = renderableChunks.infoList.getInfo(lv6)) != null) {
                    lv10.addDirection(lv5);
                    continue;
                }
                ChunkInfo lv11 = new ChunkInfo(lv6, lv5, lv3.propagationLevel + 1);
                lv11.updateCullingState(lv3.cullingState, lv5);
                if (lv6.shouldBuild()) {
                    queue.add(lv11);
                    renderableChunks.infoList.setInfo(lv6, lv11);
                    continue;
                }
                if (!this.isWithinViewDistance(l2, lv6.getSectionPos())) continue;
                renderableChunks.infoList.setInfo(lv6, lv11);
                long n = ChunkSectionPos.toChunkPos(lv6.getSectionPos());
                renderableChunks.queue.computeIfAbsent(n, l -> new ArrayList()).add(lv6);
            }
        }
    }

    private boolean isWithinViewDistance(long centerSectionPos, long otherSectionPos) {
        return ChunkFilter.isWithinDistanceExcludingEdge(ChunkSectionPos.unpackX(centerSectionPos), ChunkSectionPos.unpackZ(centerSectionPos), this.builtChunkStorage.getViewDistance(), ChunkSectionPos.unpackX(otherSectionPos), ChunkSectionPos.unpackZ(otherSectionPos));
    }

    @Nullable
    private ChunkBuilder.BuiltChunk getRenderedChunk(long sectionPos, ChunkBuilder.BuiltChunk chunk, Direction direction) {
        long m = chunk.getOffsetSectionPos(direction);
        if (!this.isWithinViewDistance(sectionPos, m)) {
            return null;
        }
        if (MathHelper.abs(ChunkSectionPos.unpackY(sectionPos) - ChunkSectionPos.unpackY(m)) > this.builtChunkStorage.getViewDistance()) {
            return null;
        }
        return this.builtChunkStorage.getRenderedChunk(m);
    }

    @Nullable
    @Debug
    public ChunkInfo getInfo(ChunkBuilder.BuiltChunk chunk) {
        return this.state.get().storage.infoList.getInfo(chunk);
    }

    public Octree getOctree() {
        return this.state.get().storage.octree;
    }

    @Environment(value=EnvType.CLIENT)
    record PreparerState(RenderableChunks storage, Events events) {
        PreparerState(BuiltChunkStorage storage) {
            this(new RenderableChunks(storage), new Events());
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class RenderableChunks {
        public final ChunkInfoList infoList;
        public final Octree octree;
        public final Long2ObjectMap<List<ChunkBuilder.BuiltChunk>> queue;

        public RenderableChunks(BuiltChunkStorage storage) {
            this.infoList = new ChunkInfoList(storage.chunks.length);
            this.octree = new Octree(storage.getSectionPos(), storage.getViewDistance(), storage.sizeY, storage.world.getBottomY());
            this.queue = new Long2ObjectOpenHashMap<List<ChunkBuilder.BuiltChunk>>();
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Events(LongSet chunksWhichReceivedNeighbors, BlockingQueue<ChunkBuilder.BuiltChunk> sectionsToPropagateFrom) {
        Events() {
            this(new LongOpenHashSet(), new LinkedBlockingQueue<ChunkBuilder.BuiltChunk>());
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ChunkInfoList {
        private final ChunkInfo[] current;

        ChunkInfoList(int size) {
            this.current = new ChunkInfo[size];
        }

        public void setInfo(ChunkBuilder.BuiltChunk chunk, ChunkInfo info) {
            this.current[chunk.index] = info;
        }

        @Nullable
        public ChunkInfo getInfo(ChunkBuilder.BuiltChunk chunk) {
            int i = chunk.index;
            if (i < 0 || i >= this.current.length) {
                return null;
            }
            return this.current[i];
        }
    }

    @Environment(value=EnvType.CLIENT)
    @Debug
    public static class ChunkInfo {
        @Debug
        protected final ChunkBuilder.BuiltChunk chunk;
        private byte direction;
        byte cullingState;
        @Debug
        public final int propagationLevel;

        ChunkInfo(ChunkBuilder.BuiltChunk chunk, @Nullable Direction direction, int propagationLevel) {
            this.chunk = chunk;
            if (direction != null) {
                this.addDirection(direction);
            }
            this.propagationLevel = propagationLevel;
        }

        void updateCullingState(byte parentCullingState, Direction from) {
            this.cullingState = (byte)(this.cullingState | (parentCullingState | 1 << from.ordinal()));
        }

        boolean canCull(Direction from) {
            return (this.cullingState & 1 << from.ordinal()) > 0;
        }

        void addDirection(Direction direction) {
            this.direction = (byte)(this.direction | (this.direction | 1 << direction.ordinal()));
        }

        @Debug
        public boolean hasDirection(int ordinal) {
            return (this.direction & 1 << ordinal) > 0;
        }

        boolean hasAnyDirection() {
            return this.direction != 0;
        }

        public int hashCode() {
            return Long.hashCode(this.chunk.getSectionPos());
        }

        public boolean equals(Object o) {
            if (!(o instanceof ChunkInfo)) {
                return false;
            }
            ChunkInfo lv = (ChunkInfo)o;
            return this.chunk.getSectionPos() == lv.chunk.getSectionPos();
        }
    }
}

