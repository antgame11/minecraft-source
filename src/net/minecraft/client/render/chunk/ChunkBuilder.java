/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.chunk;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.VertexSorter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.chunk.AbstractChunkRenderData;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.BlockBufferBuilderPool;
import net.minecraft.client.render.chunk.ChunkRenderData;
import net.minecraft.client.render.chunk.ChunkRenderTaskScheduler;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.client.render.chunk.NormalizedRelativePos;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.profiler.ScopedProfiler;
import net.minecraft.util.thread.NameableExecutor;
import net.minecraft.util.thread.SimpleConsecutiveExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChunkBuilder {
    private final ChunkRenderTaskScheduler scheduler = new ChunkRenderTaskScheduler();
    private final Queue<Runnable> uploadQueue = Queues.newConcurrentLinkedQueue();
    final Executor uploadExecutor = this.uploadQueue::add;
    final Queue<AbstractChunkRenderData> renderQueue = Queues.newConcurrentLinkedQueue();
    final BlockBufferAllocatorStorage buffers;
    private final BlockBufferBuilderPool buffersPool;
    volatile boolean stopped;
    private final SimpleConsecutiveExecutor consecutiveExecutor;
    private final NameableExecutor executor;
    ClientWorld world;
    final WorldRenderer worldRenderer;
    Vec3d cameraPosition = Vec3d.ZERO;
    final SectionBuilder sectionBuilder;

    public ChunkBuilder(ClientWorld world, WorldRenderer worldRenderer, NameableExecutor executor, BufferBuilderStorage bufferBuilderStorage, BlockRenderManager blockRenderManager, BlockEntityRenderManager blockEntityRenderDispatcher) {
        this.world = world;
        this.worldRenderer = worldRenderer;
        this.buffers = bufferBuilderStorage.getBlockBufferBuilders();
        this.buffersPool = bufferBuilderStorage.getBlockBufferBuildersPool();
        this.executor = executor;
        this.consecutiveExecutor = new SimpleConsecutiveExecutor(executor, "Section Renderer");
        this.consecutiveExecutor.send(this::scheduleRunTasks);
        this.sectionBuilder = new SectionBuilder(blockRenderManager, blockEntityRenderDispatcher);
    }

    public void setWorld(ClientWorld world) {
        this.world = world;
    }

    private void scheduleRunTasks() {
        if (this.stopped || this.buffersPool.hasNoAvailableBuilder()) {
            return;
        }
        BuiltChunk.Task lv = this.scheduler.dequeueNearest(this.cameraPosition);
        if (lv == null) {
            return;
        }
        BlockBufferAllocatorStorage lv2 = Objects.requireNonNull(this.buffersPool.acquire());
        ((CompletableFuture)CompletableFuture.supplyAsync(() -> lv.run(lv2), this.executor.named(lv.getName())).thenCompose(future -> future)).whenComplete((result, throwable) -> {
            if (throwable != null) {
                MinecraftClient.getInstance().setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Batching sections"));
                return;
            }
            arg.finished.set(true);
            this.consecutiveExecutor.send(() -> {
                if (result == Result.SUCCESSFUL) {
                    lv2.clear();
                } else {
                    lv2.reset();
                }
                this.buffersPool.release(lv2);
                this.scheduleRunTasks();
            });
        });
    }

    public void setCameraPosition(Vec3d cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

    public void upload() {
        AbstractChunkRenderData lv;
        Runnable runnable;
        while ((runnable = this.uploadQueue.poll()) != null) {
            runnable.run();
        }
        while ((lv = this.renderQueue.poll()) != null) {
            lv.close();
        }
    }

    public void rebuild(BuiltChunk chunk, ChunkRendererRegionBuilder builder) {
        chunk.rebuild(builder);
    }

    public void send(BuiltChunk.Task task) {
        if (this.stopped) {
            return;
        }
        this.consecutiveExecutor.send(() -> {
            if (this.stopped) {
                return;
            }
            this.scheduler.enqueue(task);
            this.scheduleRunTasks();
        });
    }

    public void cancelAllTasks() {
        this.scheduler.cancelAll();
    }

    public boolean isEmpty() {
        return this.scheduler.size() == 0 && this.uploadQueue.isEmpty();
    }

    public void stop() {
        this.stopped = true;
        this.cancelAllTasks();
        this.upload();
    }

    @Debug
    public String getDebugString() {
        return String.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.scheduler.size(), this.uploadQueue.size(), this.buffersPool.getAvailableBuilderCount());
    }

    @Debug
    public int getScheduledTaskCount() {
        return this.scheduler.size();
    }

    @Debug
    public int getChunksToUpload() {
        return this.uploadQueue.size();
    }

    @Debug
    public int getFreeBufferCount() {
        return this.buffersPool.getAvailableBuilderCount();
    }

    @Environment(value=EnvType.CLIENT)
    public class BuiltChunk {
        public static final int CHUNK_SIZE = 16;
        public final int index;
        public final AtomicReference<AbstractChunkRenderData> currentRenderData = new AtomicReference<AbstractChunkRenderData>(ChunkRenderData.HIDDEN);
        @Nullable
        private RebuildTask rebuildTask;
        @Nullable
        private SortTask sortTask;
        private Box boundingBox;
        private boolean needsRebuild = true;
        volatile long sectionPos = ChunkSectionPos.asLong(-1, -1, -1);
        final BlockPos.Mutable origin = new BlockPos.Mutable(-1, -1, -1);
        private boolean needsImportantRebuild;

        public BuiltChunk(int index, long sectionPos) {
            this.index = index;
            this.setSectionPos(sectionPos);
        }

        private boolean isChunkNonEmpty(long sectionPos) {
            Chunk lv = ChunkBuilder.this.world.getChunk(ChunkSectionPos.unpackX(sectionPos), ChunkSectionPos.unpackZ(sectionPos), ChunkStatus.FULL, false);
            return lv != null && ChunkBuilder.this.world.getLightingProvider().isLightingEnabled(ChunkSectionPos.withZeroY(sectionPos));
        }

        public boolean shouldBuild() {
            return this.isChunkNonEmpty(ChunkSectionPos.offset(this.sectionPos, Direction.WEST)) && this.isChunkNonEmpty(ChunkSectionPos.offset(this.sectionPos, Direction.NORTH)) && this.isChunkNonEmpty(ChunkSectionPos.offset(this.sectionPos, Direction.EAST)) && this.isChunkNonEmpty(ChunkSectionPos.offset(this.sectionPos, Direction.SOUTH)) && this.isChunkNonEmpty(ChunkSectionPos.offset(this.sectionPos, -1, 0, -1)) && this.isChunkNonEmpty(ChunkSectionPos.offset(this.sectionPos, -1, 0, 1)) && this.isChunkNonEmpty(ChunkSectionPos.offset(this.sectionPos, 1, 0, -1)) && this.isChunkNonEmpty(ChunkSectionPos.offset(this.sectionPos, 1, 0, 1));
        }

        public Box getBoundingBox() {
            return this.boundingBox;
        }

        public CompletableFuture<Void> uploadLayer(Map<BlockRenderLayer, BuiltBuffer> buffersByLayer, ChunkRenderData renderData) {
            if (ChunkBuilder.this.stopped) {
                buffersByLayer.values().forEach(BuiltBuffer::close);
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.runAsync(() -> buffersByLayer.forEach((layer, buffer) -> {
                try (ScopedProfiler lv = Profilers.get().scoped("Upload Section Layer");){
                    renderData.upload((BlockRenderLayer)((Object)((Object)layer)), (BuiltBuffer)buffer, this.sectionPos);
                    buffer.close();
                }
            }), ChunkBuilder.this.uploadExecutor);
        }

        public CompletableFuture<Void> uploadIndices(ChunkRenderData data, BufferAllocator.CloseableBuffer buffer, BlockRenderLayer layer) {
            if (ChunkBuilder.this.stopped) {
                buffer.close();
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.runAsync(() -> {
                try (ScopedProfiler lv = Profilers.get().scoped("Upload Section Indices");){
                    data.uploadIndexBuffer(layer, buffer, this.sectionPos);
                    buffer.close();
                }
            }, ChunkBuilder.this.uploadExecutor);
        }

        public void setSectionPos(long sectionPos) {
            this.clear();
            this.sectionPos = sectionPos;
            int i = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(sectionPos));
            int j = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(sectionPos));
            int k = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(sectionPos));
            this.origin.set(i, j, k);
            this.boundingBox = new Box(i, j, k, i + 16, j + 16, k + 16);
        }

        public AbstractChunkRenderData getCurrentRenderData() {
            return this.currentRenderData.get();
        }

        public void clear() {
            this.cancel();
            this.currentRenderData.getAndSet(ChunkRenderData.HIDDEN).close();
            this.needsRebuild = true;
        }

        public BlockPos getOrigin() {
            return this.origin;
        }

        public long getSectionPos() {
            return this.sectionPos;
        }

        public void scheduleRebuild(boolean important) {
            boolean bl2 = this.needsRebuild;
            this.needsRebuild = true;
            this.needsImportantRebuild = important | (bl2 && this.needsImportantRebuild);
        }

        public void cancelRebuild() {
            this.needsRebuild = false;
            this.needsImportantRebuild = false;
        }

        public boolean needsRebuild() {
            return this.needsRebuild;
        }

        public boolean needsImportantRebuild() {
            return this.needsRebuild && this.needsImportantRebuild;
        }

        public long getOffsetSectionPos(Direction direction) {
            return ChunkSectionPos.offset(this.sectionPos, direction);
        }

        public void scheduleSort(ChunkBuilder builder) {
            AbstractChunkRenderData abstractChunkRenderData = this.getCurrentRenderData();
            if (abstractChunkRenderData instanceof ChunkRenderData) {
                ChunkRenderData lv = (ChunkRenderData)abstractChunkRenderData;
                this.sortTask = new SortTask(lv);
                builder.send(this.sortTask);
            }
        }

        public boolean hasTranslucentLayer() {
            return this.getCurrentRenderData().hasTranslucentLayers();
        }

        public boolean isCurrentlySorting() {
            return this.sortTask != null && !this.sortTask.finished.get();
        }

        protected void cancel() {
            if (this.rebuildTask != null) {
                this.rebuildTask.cancel();
                this.rebuildTask = null;
            }
            if (this.sortTask != null) {
                this.sortTask.cancel();
                this.sortTask = null;
            }
        }

        public Task createRebuildTask(ChunkRendererRegionBuilder builder) {
            this.cancel();
            ChunkRendererRegion lv = builder.build(ChunkBuilder.this.world, this.sectionPos);
            boolean bl = this.currentRenderData.get() != ChunkRenderData.HIDDEN;
            this.rebuildTask = new RebuildTask(lv, bl);
            return this.rebuildTask;
        }

        public void scheduleRebuild(ChunkRendererRegionBuilder builder) {
            Task lv = this.createRebuildTask(builder);
            ChunkBuilder.this.send(lv);
        }

        public void rebuild(ChunkRendererRegionBuilder builder) {
            Task lv = this.createRebuildTask(builder);
            lv.run(ChunkBuilder.this.buffers);
        }

        void setCurrentRenderData(AbstractChunkRenderData data) {
            AbstractChunkRenderData lv = this.currentRenderData.getAndSet(data);
            ChunkBuilder.this.renderQueue.add(lv);
            ChunkBuilder.this.worldRenderer.addBuiltChunk(this);
        }

        VertexSorter getVertexSorter(ChunkSectionPos sectionPos) {
            Vec3d lv = ChunkBuilder.this.cameraPosition;
            return VertexSorter.byDistance((float)(lv.x - (double)sectionPos.getMinX()), (float)(lv.y - (double)sectionPos.getMinY()), (float)(lv.z - (double)sectionPos.getMinZ()));
        }

        @Environment(value=EnvType.CLIENT)
        class SortTask
        extends Task {
            private final ChunkRenderData renderData;

            public SortTask(ChunkRenderData data) {
                super(true);
                this.renderData = data;
            }

            @Override
            protected String getName() {
                return "rend_chk_sort";
            }

            @Override
            public CompletableFuture<Result> run(BlockBufferAllocatorStorage buffers) {
                if (this.cancelled.get()) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                BuiltBuffer.SortState lv = this.renderData.getTranslucencySortingData();
                if (lv == null || this.renderData.containsLayer(BlockRenderLayer.TRANSLUCENT)) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                long l = BuiltChunk.this.sectionPos;
                VertexSorter lv2 = BuiltChunk.this.getVertexSorter(ChunkSectionPos.from(l));
                NormalizedRelativePos lv3 = NormalizedRelativePos.of(ChunkBuilder.this.cameraPosition, l);
                if (!this.renderData.hasPosition(lv3) && !lv3.isOnCameraAxis()) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                BufferAllocator.CloseableBuffer lv4 = lv.sortAndStore(buffers.get(BlockRenderLayer.TRANSLUCENT), lv2);
                if (lv4 == null) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                if (this.cancelled.get()) {
                    lv4.close();
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                CompletableFuture<Void> completableFuture = BuiltChunk.this.uploadIndices(this.renderData, lv4, BlockRenderLayer.TRANSLUCENT);
                return completableFuture.handle((void_, throwable) -> {
                    if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                        MinecraftClient.getInstance().setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Rendering section"));
                    }
                    if (this.cancelled.get()) {
                        return Result.CANCELLED;
                    }
                    this.renderData.setPos(lv3);
                    return Result.SUCCESSFUL;
                });
            }

            @Override
            public void cancel() {
                this.cancelled.set(true);
            }
        }

        @Environment(value=EnvType.CLIENT)
        public abstract class Task {
            protected final AtomicBoolean cancelled = new AtomicBoolean(false);
            protected final AtomicBoolean finished = new AtomicBoolean(false);
            protected final boolean prioritized;

            public Task(boolean prioritized) {
                this.prioritized = prioritized;
            }

            public abstract CompletableFuture<Result> run(BlockBufferAllocatorStorage var1);

            public abstract void cancel();

            protected abstract String getName();

            public boolean isPrioritized() {
                return this.prioritized;
            }

            public BlockPos getOrigin() {
                return BuiltChunk.this.origin;
            }
        }

        @Environment(value=EnvType.CLIENT)
        class RebuildTask
        extends Task {
            protected final ChunkRendererRegion region;

            public RebuildTask(ChunkRendererRegion region, boolean prioritized) {
                super(prioritized);
                this.region = region;
            }

            @Override
            protected String getName() {
                return "rend_chk_rebuild";
            }

            @Override
            public CompletableFuture<Result> run(BlockBufferAllocatorStorage buffers) {
                SectionBuilder.RenderData lv3;
                if (this.cancelled.get()) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                long l = BuiltChunk.this.sectionPos;
                ChunkSectionPos lv = ChunkSectionPos.from(l);
                if (this.cancelled.get()) {
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                try (ScopedProfiler lv2 = Profilers.get().scoped("Compile Section");){
                    lv3 = ChunkBuilder.this.sectionBuilder.build(lv, this.region, BuiltChunk.this.getVertexSorter(lv), buffers);
                }
                NormalizedRelativePos lv4 = NormalizedRelativePos.of(ChunkBuilder.this.cameraPosition, l);
                if (this.cancelled.get()) {
                    lv3.close();
                    return CompletableFuture.completedFuture(Result.CANCELLED);
                }
                ChunkRenderData lv5 = new ChunkRenderData(lv4, lv3);
                CompletableFuture<Void> completableFuture = BuiltChunk.this.uploadLayer(lv3.buffers, lv5);
                return completableFuture.handle((void_, throwable) -> {
                    if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                        MinecraftClient.getInstance().setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Rendering section"));
                    }
                    if (this.cancelled.get() || ChunkBuilder.this.stopped) {
                        ChunkBuilder.this.renderQueue.add(lv5);
                        return Result.CANCELLED;
                    }
                    BuiltChunk.this.setCurrentRenderData(lv5);
                    return Result.SUCCESSFUL;
                });
            }

            @Override
            public void cancel() {
                if (this.cancelled.compareAndSet(false, true)) {
                    BuiltChunk.this.scheduleRebuild(false);
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum Result {
        SUCCESSFUL,
        CANCELLED;

    }
}

