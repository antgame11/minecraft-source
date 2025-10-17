/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelManager;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.path.PathUtil;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightSourceView;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.NbtScannable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerChunkManager
extends ChunkManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ChunkLevelManager levelManager;
    private final ServerWorld world;
    final Thread serverThread;
    final ServerLightingProvider lightingProvider;
    private final MainThreadExecutor mainThreadExecutor;
    public final ServerChunkLoadingManager chunkLoadingManager;
    private final PersistentStateManager persistentStateManager;
    private final ChunkTicketManager ticketManager;
    private long lastTickTime;
    private boolean spawnMonsters = true;
    private static final int CACHE_SIZE = 4;
    private final long[] chunkPosCache = new long[4];
    private final ChunkStatus[] chunkStatusCache = new ChunkStatus[4];
    private final Chunk[] chunkCache = new Chunk[4];
    private final List<WorldChunk> spawningChunks = new ObjectArrayList<WorldChunk>();
    private final Set<ChunkHolder> chunksToBroadcastUpdate = new ReferenceOpenHashSet<ChunkHolder>();
    @Nullable
    @Debug
    private SpawnHelper.Info spawnInfo;

    public ServerChunkManager(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor workerExecutor, ChunkGenerator chunkGenerator, int viewDistance, int simulationDistance, boolean dsync, ChunkStatusChangeListener arg5, Supplier<PersistentStateManager> supplier) {
        this.world = world;
        this.mainThreadExecutor = new MainThreadExecutor(world);
        this.serverThread = Thread.currentThread();
        Path path = session.getWorldDirectory(world.getRegistryKey()).resolve("data");
        try {
            PathUtil.createDirectories(path);
        } catch (IOException iOException) {
            LOGGER.error("Failed to create dimension data storage directory", iOException);
        }
        this.persistentStateManager = new PersistentStateManager(new PersistentState.Context(world), path, dataFixer, world.getRegistryManager());
        this.ticketManager = this.persistentStateManager.getOrCreate(ChunkTicketManager.STATE_TYPE);
        this.chunkLoadingManager = new ServerChunkLoadingManager(world, session, dataFixer, structureTemplateManager, workerExecutor, this.mainThreadExecutor, this, chunkGenerator, arg5, supplier, this.ticketManager, viewDistance, dsync);
        this.lightingProvider = this.chunkLoadingManager.getLightingProvider();
        this.levelManager = this.chunkLoadingManager.getLevelManager();
        this.levelManager.setSimulationDistance(simulationDistance);
        this.initChunkCaches();
    }

    @Override
    public ServerLightingProvider getLightingProvider() {
        return this.lightingProvider;
    }

    @Nullable
    private ChunkHolder getChunkHolder(long pos) {
        return this.chunkLoadingManager.getChunkHolder(pos);
    }

    private void putInCache(long pos, @Nullable Chunk chunk, ChunkStatus status) {
        for (int i = 3; i > 0; --i) {
            this.chunkPosCache[i] = this.chunkPosCache[i - 1];
            this.chunkStatusCache[i] = this.chunkStatusCache[i - 1];
            this.chunkCache[i] = this.chunkCache[i - 1];
        }
        this.chunkPosCache[0] = pos;
        this.chunkStatusCache[0] = status;
        this.chunkCache[0] = chunk;
    }

    @Override
    @Nullable
    public Chunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
        if (Thread.currentThread() != this.serverThread) {
            return CompletableFuture.supplyAsync(() -> this.getChunk(x, z, leastStatus, create), this.mainThreadExecutor).join();
        }
        Profiler lv = Profilers.get();
        lv.visit("getChunk");
        long l = ChunkPos.toLong(x, z);
        for (int k = 0; k < 4; ++k) {
            Chunk lv2;
            if (l != this.chunkPosCache[k] || leastStatus != this.chunkStatusCache[k] || (lv2 = this.chunkCache[k]) == null && create) continue;
            return lv2;
        }
        lv.visit("getChunkCacheMiss");
        CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.getChunkFuture(x, z, leastStatus, create);
        this.mainThreadExecutor.runTasks(completableFuture::isDone);
        OptionalChunk<Chunk> lv3 = completableFuture.join();
        Chunk lv4 = lv3.orElse(null);
        if (lv4 == null && create) {
            throw Util.getFatalOrPause(new IllegalStateException("Chunk not there when requested: " + lv3.getError()));
        }
        this.putInCache(l, lv4, leastStatus);
        return lv4;
    }

    @Override
    @Nullable
    public WorldChunk getWorldChunk(int chunkX, int chunkZ) {
        if (Thread.currentThread() != this.serverThread) {
            return null;
        }
        Profilers.get().visit("getChunkNow");
        long l = ChunkPos.toLong(chunkX, chunkZ);
        for (int k = 0; k < 4; ++k) {
            if (l != this.chunkPosCache[k] || this.chunkStatusCache[k] != ChunkStatus.FULL) continue;
            Chunk lv = this.chunkCache[k];
            return lv instanceof WorldChunk ? (WorldChunk)lv : null;
        }
        ChunkHolder lv2 = this.getChunkHolder(l);
        if (lv2 == null) {
            return null;
        }
        Chunk lv = lv2.getOrNull(ChunkStatus.FULL);
        if (lv != null) {
            this.putInCache(l, lv, ChunkStatus.FULL);
            if (lv instanceof WorldChunk) {
                return (WorldChunk)lv;
            }
        }
        return null;
    }

    private void initChunkCaches() {
        Arrays.fill(this.chunkPosCache, ChunkPos.MARKER);
        Arrays.fill(this.chunkStatusCache, null);
        Arrays.fill(this.chunkCache, null);
    }

    public CompletableFuture<OptionalChunk<Chunk>> getChunkFutureSyncOnMainThread(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        CompletionStage<OptionalChunk<Chunk>> completableFuture;
        boolean bl2;
        boolean bl = bl2 = Thread.currentThread() == this.serverThread;
        if (bl2) {
            completableFuture = this.getChunkFuture(chunkX, chunkZ, leastStatus, create);
            this.mainThreadExecutor.runTasks(() -> completableFuture.isDone());
        } else {
            completableFuture = CompletableFuture.supplyAsync(() -> this.getChunkFuture(chunkX, chunkZ, leastStatus, create), this.mainThreadExecutor).thenCompose(future -> future);
        }
        return completableFuture;
    }

    private CompletableFuture<OptionalChunk<Chunk>> getChunkFuture(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        ChunkPos lv = new ChunkPos(chunkX, chunkZ);
        long l = lv.toLong();
        int k = ChunkLevels.getLevelFromStatus(leastStatus);
        ChunkHolder lv2 = this.getChunkHolder(l);
        if (create) {
            this.addTicket(new ChunkTicket(ChunkTicketType.UNKNOWN, k), lv);
            if (this.isMissingForLevel(lv2, k)) {
                Profiler lv3 = Profilers.get();
                lv3.push("chunkLoad");
                this.updateChunks();
                lv2 = this.getChunkHolder(l);
                lv3.pop();
                if (this.isMissingForLevel(lv2, k)) {
                    throw Util.getFatalOrPause(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            }
        }
        if (this.isMissingForLevel(lv2, k)) {
            return AbstractChunkHolder.UNLOADED_FUTURE;
        }
        return lv2.load(leastStatus, this.chunkLoadingManager);
    }

    private boolean isMissingForLevel(@Nullable ChunkHolder holder, int maxLevel) {
        return holder == null || holder.getLevel() > maxLevel;
    }

    @Override
    public boolean isChunkLoaded(int x, int z) {
        int k;
        ChunkHolder lv = this.getChunkHolder(new ChunkPos(x, z).toLong());
        return !this.isMissingForLevel(lv, k = ChunkLevels.getLevelFromStatus(ChunkStatus.FULL));
    }

    @Override
    @Nullable
    public LightSourceView getChunk(int chunkX, int chunkZ) {
        long l = ChunkPos.toLong(chunkX, chunkZ);
        ChunkHolder lv = this.getChunkHolder(l);
        if (lv == null) {
            return null;
        }
        return lv.getUncheckedOrNull(ChunkStatus.INITIALIZE_LIGHT.getPrevious());
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    public boolean executeQueuedTasks() {
        return this.mainThreadExecutor.runTask();
    }

    boolean updateChunks() {
        boolean bl = this.levelManager.update(this.chunkLoadingManager);
        boolean bl2 = this.chunkLoadingManager.updateHolderMap();
        this.chunkLoadingManager.updateChunks();
        if (bl || bl2) {
            this.initChunkCaches();
            return true;
        }
        return false;
    }

    public boolean isTickingFutureReady(long pos) {
        if (!this.world.shouldTickBlocksInChunk(pos)) {
            return false;
        }
        ChunkHolder lv = this.getChunkHolder(pos);
        if (lv == null) {
            return false;
        }
        return lv.getTickingFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).isPresent();
    }

    public void save(boolean flush) {
        this.updateChunks();
        this.chunkLoadingManager.save(flush);
    }

    @Override
    public void close() throws IOException {
        this.save(true);
        this.persistentStateManager.close();
        this.lightingProvider.close();
        this.chunkLoadingManager.close();
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking, boolean tickChunks) {
        Profiler lv = Profilers.get();
        lv.push("purge");
        if (this.world.getTickManager().shouldTick() || !tickChunks) {
            this.ticketManager.tick(this.chunkLoadingManager);
        }
        this.updateChunks();
        lv.swap("chunks");
        if (tickChunks) {
            this.tickChunks();
            this.chunkLoadingManager.tickEntityMovement();
        }
        lv.swap("unload");
        this.chunkLoadingManager.tick(shouldKeepTicking);
        lv.pop();
        this.initChunkCaches();
    }

    private void tickChunks() {
        long l = this.world.getTime();
        long m = l - this.lastTickTime;
        this.lastTickTime = l;
        if (this.world.isDebugWorld()) {
            return;
        }
        Profiler lv = Profilers.get();
        lv.push("pollingChunks");
        if (this.world.getTickManager().shouldTick()) {
            lv.push("tickingChunks");
            this.tickChunks(lv, m);
            lv.pop();
        }
        this.broadcastUpdates(lv);
        lv.pop();
    }

    private void broadcastUpdates(Profiler profiler) {
        profiler.push("broadcast");
        for (ChunkHolder lv : this.chunksToBroadcastUpdate) {
            WorldChunk lv2 = lv.getWorldChunk();
            if (lv2 == null) continue;
            lv.flushUpdates(lv2);
        }
        this.chunksToBroadcastUpdate.clear();
        profiler.pop();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void tickChunks(Profiler profiler, long timeDelta) {
        List<SpawnGroup> list;
        SpawnHelper.Info lv;
        profiler.push("naturalSpawnCount");
        int i = this.levelManager.getTickedChunkCount();
        this.spawnInfo = lv = SpawnHelper.setupSpawn(i, this.world.iterateEntities(), this::ifChunkLoaded, new SpawnDensityCapper(this.chunkLoadingManager));
        boolean bl = this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
        int j = this.world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        if (bl) {
            boolean bl2 = this.world.getLevelProperties().getTime() % 400L == 0L;
            list = SpawnHelper.collectSpawnableGroups(lv, true, this.spawnMonsters, bl2);
        } else {
            list = List.of();
        }
        List<WorldChunk> list2 = this.spawningChunks;
        try {
            profiler.swap("filteringSpawningChunks");
            this.chunkLoadingManager.collectSpawningChunks(list2);
            profiler.swap("shuffleSpawningChunks");
            Util.shuffle(list2, this.world.random);
            profiler.swap("tickSpawningChunks");
            for (WorldChunk lv2 : list2) {
                this.tickSpawningChunk(lv2, timeDelta, list, lv);
            }
        } finally {
            list2.clear();
        }
        profiler.swap("tickTickingChunks");
        this.chunkLoadingManager.forEachBlockTickingChunk(chunk -> this.world.tickChunk((WorldChunk)chunk, j));
        if (bl) {
            profiler.swap("customSpawners");
            this.world.tickSpawners(this.spawnMonsters);
        }
        profiler.pop();
    }

    private void tickSpawningChunk(WorldChunk chunk, long timeDelta, List<SpawnGroup> spawnableGroups, SpawnHelper.Info info) {
        ChunkPos lv = chunk.getPos();
        chunk.increaseInhabitedTime(timeDelta);
        if (this.levelManager.shouldTickEntities(lv.toLong())) {
            this.world.tickThunder(chunk);
        }
        if (spawnableGroups.isEmpty()) {
            return;
        }
        if (this.world.canSpawnEntitiesAt(lv)) {
            SpawnHelper.spawn(this.world, chunk, info, spawnableGroups);
        }
    }

    private void ifChunkLoaded(long pos, Consumer<WorldChunk> chunkConsumer) {
        ChunkHolder lv = this.getChunkHolder(pos);
        if (lv != null) {
            lv.getAccessibleFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).ifPresent(chunkConsumer);
        }
    }

    @Override
    public String getDebugString() {
        return Integer.toString(this.getLoadedChunkCount());
    }

    @VisibleForTesting
    public int getPendingTasks() {
        return this.mainThreadExecutor.getTaskCount();
    }

    public ChunkGenerator getChunkGenerator() {
        return this.chunkLoadingManager.getChunkGenerator();
    }

    public StructurePlacementCalculator getStructurePlacementCalculator() {
        return this.chunkLoadingManager.getStructurePlacementCalculator();
    }

    public NoiseConfig getNoiseConfig() {
        return this.chunkLoadingManager.getNoiseConfig();
    }

    @Override
    public int getLoadedChunkCount() {
        return this.chunkLoadingManager.getLoadedChunkCount();
    }

    public void markForUpdate(BlockPos pos) {
        int j;
        int i = ChunkSectionPos.getSectionCoord(pos.getX());
        ChunkHolder lv = this.getChunkHolder(ChunkPos.toLong(i, j = ChunkSectionPos.getSectionCoord(pos.getZ())));
        if (lv != null && lv.markForBlockUpdate(pos)) {
            this.chunksToBroadcastUpdate.add(lv);
        }
    }

    @Override
    public void onLightUpdate(LightType type, ChunkSectionPos pos) {
        this.mainThreadExecutor.execute(() -> {
            ChunkHolder lv = this.getChunkHolder(pos.toChunkPos().toLong());
            if (lv != null && lv.markForLightUpdate(type, pos.getSectionY())) {
                this.chunksToBroadcastUpdate.add(lv);
            }
        });
    }

    public boolean shouldResetIdleTimeout() {
        return this.ticketManager.shouldResetIdleTimeout();
    }

    public void addTicket(ChunkTicket ticket, ChunkPos pos) {
        this.ticketManager.addTicket(ticket, pos);
    }

    public CompletableFuture<?> addChunkLoadingTicket(ChunkTicketType ticketType, ChunkPos pos, int radius) {
        if (!ticketType.isForLoading()) {
            throw new IllegalStateException("Ticket type " + String.valueOf(ticketType) + " does not trigger chunk loading");
        }
        if (ticketType.canExpireBeforeLoad()) {
            throw new IllegalStateException("Ticket type " + String.valueOf(ticketType) + " can expire before it loads, cannot fetch asynchronously");
        }
        this.addTicket(ticketType, pos, radius);
        this.updateChunks();
        ChunkHolder lv = this.getChunkHolder(pos.toLong());
        Objects.requireNonNull(lv, "No chunk was scheduled for loading");
        return this.chunkLoadingManager.getRegion(lv, radius, distance -> ChunkStatus.FULL);
    }

    public void addTicket(ChunkTicketType type, ChunkPos pos, int radius) {
        this.ticketManager.addTicket(type, pos, radius);
    }

    public void removeTicket(ChunkTicketType type, ChunkPos pos, int radius) {
        this.ticketManager.removeTicket(type, pos, radius);
    }

    @Override
    public boolean setChunkForced(ChunkPos pos, boolean forced) {
        return this.ticketManager.setChunkForced(pos, forced);
    }

    @Override
    public LongSet getForcedChunks() {
        return this.ticketManager.getForcedChunks();
    }

    public void updatePosition(ServerPlayerEntity player) {
        if (!player.isRemoved()) {
            this.chunkLoadingManager.updatePosition(player);
            if (player.canReceiveWaypoints()) {
                this.world.getWaypointHandler().updatePlayerPos(player);
            }
        }
    }

    public void unloadEntity(Entity entity) {
        this.chunkLoadingManager.unloadEntity(entity);
    }

    public void loadEntity(Entity entity) {
        this.chunkLoadingManager.loadEntity(entity);
    }

    public void sendToNearbyPlayers(Entity entity, Packet<? super ClientPlayPacketListener> packet) {
        this.chunkLoadingManager.sendToNearbyPlayers(entity, packet);
    }

    public void sendToOtherNearbyPlayers(Entity entity, Packet<? super ClientPlayPacketListener> packet) {
        this.chunkLoadingManager.sendToOtherNearbyPlayers(entity, packet);
    }

    public void applyViewDistance(int watchDistance) {
        this.chunkLoadingManager.setViewDistance(watchDistance);
    }

    public void applySimulationDistance(int simulationDistance) {
        this.levelManager.setSimulationDistance(simulationDistance);
    }

    @Override
    public void setMobSpawnOptions(boolean spawnMonsters) {
        this.spawnMonsters = spawnMonsters;
    }

    public String getChunkLoadingDebugInfo(ChunkPos pos) {
        return this.chunkLoadingManager.getChunkLoadingDebugInfo(pos);
    }

    public PersistentStateManager getPersistentStateManager() {
        return this.persistentStateManager;
    }

    public PointOfInterestStorage getPointOfInterestStorage() {
        return this.chunkLoadingManager.getPointOfInterestStorage();
    }

    public NbtScannable getChunkIoWorker() {
        return this.chunkLoadingManager.getWorker();
    }

    @Nullable
    @Debug
    public SpawnHelper.Info getSpawnInfo() {
        return this.spawnInfo;
    }

    public void shutdown() {
        this.ticketManager.shutdown();
    }

    public void markForUpdate(ChunkHolder chunkHolder) {
        if (chunkHolder.hasPendingUpdates()) {
            this.chunksToBroadcastUpdate.add(chunkHolder);
        }
    }

    @Override
    public /* synthetic */ LightingProvider getLightingProvider() {
        return this.getLightingProvider();
    }

    @Override
    public /* synthetic */ BlockView getWorld() {
        return this.getWorld();
    }

    final class MainThreadExecutor
    extends ThreadExecutor<Runnable> {
        MainThreadExecutor(World world) {
            super("Chunk source main thread executor for " + String.valueOf(world.getRegistryKey().getValue()));
        }

        @Override
        public void runTasks(BooleanSupplier stopCondition) {
            super.runTasks(() -> MinecraftServer.checkWorldGenException() && stopCondition.getAsBoolean());
        }

        @Override
        public Runnable createTask(Runnable runnable) {
            return runnable;
        }

        @Override
        protected boolean canExecute(Runnable task) {
            return true;
        }

        @Override
        protected boolean shouldExecuteAsync() {
            return true;
        }

        @Override
        protected Thread getThread() {
            return ServerChunkManager.this.serverThread;
        }

        @Override
        protected void executeTask(Runnable task) {
            Profilers.get().visit("runTask");
            super.executeTask(task);
        }

        @Override
        protected boolean runTask() {
            if (ServerChunkManager.this.updateChunks()) {
                return true;
            }
            ServerChunkManager.this.lightingProvider.tick();
            return super.runTask();
        }
    }
}

