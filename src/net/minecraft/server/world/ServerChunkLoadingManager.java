/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtException;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkBiomeDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ChunkFilter;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelManager;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ChunkTaskScheduler;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.LevelPrioritizedQueue;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.CsvWriter;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.thread.SimpleConsecutiveExecutor;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.ChunkLoadingManager;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkLoader;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.chunk.ChunkType;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.SerializedChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerChunkLoadingManager
extends VersionedChunkStorage
implements ChunkHolder.PlayersWatchingChunkProvider,
ChunkLoadingManager {
    private static final OptionalChunk<List<Chunk>> UNLOADED_CHUNKS = OptionalChunk.of("Unloaded chunks found in range");
    private static final CompletableFuture<OptionalChunk<List<Chunk>>> UNLOADED_CHUNKS_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNKS);
    private static final byte PROTO_CHUNK = -1;
    private static final byte UNMARKED_CHUNK = 0;
    private static final byte LEVEL_CHUNK = 1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_29674 = 200;
    private static final int field_36291 = 20;
    private static final int field_36384 = 10000;
    private static final int field_54966 = 128;
    public static final int DEFAULT_VIEW_DISTANCE = 2;
    public static final int field_29669 = 32;
    public static final int FORCED_CHUNK_LEVEL = ChunkLevels.getLevelFromType(ChunkLevelType.ENTITY_TICKING);
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders = new Long2ObjectLinkedOpenHashMap();
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders = this.currentChunkHolders.clone();
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> chunksToUnload = new Long2ObjectLinkedOpenHashMap();
    private final List<ChunkLoader> loaders = new ArrayList<ChunkLoader>();
    final ServerWorld world;
    private final ServerLightingProvider lightingProvider;
    private final ThreadExecutor<Runnable> mainThreadExecutor;
    private final NoiseConfig noiseConfig;
    private final StructurePlacementCalculator structurePlacementCalculator;
    private final Supplier<PersistentStateManager> persistentStateManagerFactory;
    private final ChunkTicketManager ticketManager;
    private final PointOfInterestStorage pointOfInterestStorage;
    final LongSet unloadedChunks = new LongOpenHashSet();
    private boolean chunkHolderListDirty;
    private final ChunkTaskScheduler worldGenScheduler;
    private final ChunkTaskScheduler lightScheduler;
    private final ChunkStatusChangeListener chunkStatusChangeListener;
    private final LevelManager levelManager;
    private final String saveDir;
    private final PlayerChunkWatchingManager playerChunkWatchingManager = new PlayerChunkWatchingManager();
    private final Int2ObjectMap<EntityTracker> entityTrackers = new Int2ObjectOpenHashMap<EntityTracker>();
    private final Long2ByteMap chunkToType = new Long2ByteOpenHashMap();
    private final Long2LongMap chunkToNextSaveTimeMs = new Long2LongOpenHashMap();
    private final LongSet chunksToSave = new LongLinkedOpenHashSet();
    private final Queue<Runnable> unloadTaskQueue = Queues.newConcurrentLinkedQueue();
    private final AtomicInteger chunksBeingSavedCount = new AtomicInteger();
    private int watchDistance;
    private final ChunkGenerationContext generationContext;

    public ServerChunkLoadingManager(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, ThreadExecutor<Runnable> mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, ChunkStatusChangeListener chunkStatusChangeListener, Supplier<PersistentStateManager> persistentStateManagerFactory, ChunkTicketManager ticketManager, int viewDistance, boolean dsync) {
        super(new StorageKey(session.getDirectoryName(), world.getRegistryKey(), "chunk"), session.getWorldDirectory(world.getRegistryKey()).resolve("region"), dataFixer, dsync);
        Path path = session.getWorldDirectory(world.getRegistryKey());
        this.saveDir = path.getFileName().toString();
        this.world = world;
        DynamicRegistryManager lv = world.getRegistryManager();
        long l = world.getSeed();
        if (chunkGenerator instanceof NoiseChunkGenerator) {
            NoiseChunkGenerator lv2 = (NoiseChunkGenerator)chunkGenerator;
            this.noiseConfig = NoiseConfig.create(lv2.getSettings().value(), lv.getOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
        } else {
            this.noiseConfig = NoiseConfig.create(ChunkGeneratorSettings.createMissingSettings(), lv.getOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
        }
        this.structurePlacementCalculator = chunkGenerator.createStructurePlacementCalculator(lv.getOrThrow(RegistryKeys.STRUCTURE_SET), this.noiseConfig, l);
        this.mainThreadExecutor = mainThreadExecutor;
        SimpleConsecutiveExecutor lv3 = new SimpleConsecutiveExecutor(executor, "worldgen");
        this.chunkStatusChangeListener = chunkStatusChangeListener;
        SimpleConsecutiveExecutor lv4 = new SimpleConsecutiveExecutor(executor, "light");
        this.worldGenScheduler = new ChunkTaskScheduler(lv3, executor);
        this.lightScheduler = new ChunkTaskScheduler(lv4, executor);
        this.lightingProvider = new ServerLightingProvider(chunkProvider, this, this.world.getDimension().hasSkyLight(), lv4, this.lightScheduler);
        this.levelManager = new LevelManager(ticketManager, executor, mainThreadExecutor);
        this.persistentStateManagerFactory = persistentStateManagerFactory;
        this.ticketManager = ticketManager;
        this.pointOfInterestStorage = new PointOfInterestStorage(new StorageKey(session.getDirectoryName(), world.getRegistryKey(), "poi"), path.resolve("poi"), dataFixer, dsync, lv, world.getServer(), world);
        this.setViewDistance(viewDistance);
        this.generationContext = new ChunkGenerationContext(world, chunkGenerator, structureTemplateManager, this.lightingProvider, mainThreadExecutor, this::markChunkNeedsSaving);
    }

    private void markChunkNeedsSaving(ChunkPos pos) {
        this.chunksToSave.add(pos.toLong());
    }

    protected ChunkGenerator getChunkGenerator() {
        return this.generationContext.generator();
    }

    protected StructurePlacementCalculator getStructurePlacementCalculator() {
        return this.structurePlacementCalculator;
    }

    protected NoiseConfig getNoiseConfig() {
        return this.noiseConfig;
    }

    public boolean isTracked(ServerPlayerEntity player, int chunkX, int chunkZ) {
        return player.getChunkFilter().isWithinDistance(chunkX, chunkZ) && !player.networkHandler.chunkDataSender.isInNextBatch(ChunkPos.toLong(chunkX, chunkZ));
    }

    private boolean isOnTrackEdge(ServerPlayerEntity player, int chunkX, int chunkZ) {
        if (!this.isTracked(player, chunkX, chunkZ)) {
            return false;
        }
        for (int k = -1; k <= 1; ++k) {
            for (int l = -1; l <= 1; ++l) {
                if (k == 0 && l == 0 || this.isTracked(player, chunkX + k, chunkZ + l)) continue;
                return true;
            }
        }
        return false;
    }

    protected ServerLightingProvider getLightingProvider() {
        return this.lightingProvider;
    }

    @Nullable
    public ChunkHolder getCurrentChunkHolder(long pos) {
        return this.currentChunkHolders.get(pos);
    }

    @Nullable
    protected ChunkHolder getChunkHolder(long pos) {
        return this.chunkHolders.get(pos);
    }

    @Nullable
    public ChunkStatus getStatus(long chunkPos) {
        ChunkHolder lv = this.getChunkHolder(chunkPos);
        return lv != null ? lv.getLatestStatus() : null;
    }

    protected IntSupplier getCompletedLevelSupplier(long pos) {
        return () -> {
            ChunkHolder lv = this.getChunkHolder(pos);
            if (lv == null) {
                return LevelPrioritizedQueue.LEVEL_COUNT - 1;
            }
            return Math.min(lv.getCompletedLevel(), LevelPrioritizedQueue.LEVEL_COUNT - 1);
        };
    }

    public String getChunkLoadingDebugInfo(ChunkPos chunkPos) {
        ChunkHolder lv = this.getChunkHolder(chunkPos.toLong());
        if (lv == null) {
            return "null";
        }
        String string = lv.getLevel() + "\n";
        ChunkStatus lv2 = lv.getLatestStatus();
        Chunk lv3 = lv.getLatest();
        if (lv2 != null) {
            string = string + "St: \u00a7" + lv2.getIndex() + String.valueOf(lv2) + "\u00a7r\n";
        }
        if (lv3 != null) {
            string = string + "Ch: \u00a7" + lv3.getStatus().getIndex() + String.valueOf(lv3.getStatus()) + "\u00a7r\n";
        }
        ChunkLevelType lv4 = lv.getLevelType();
        string = string + String.valueOf('\u00a7') + lv4.ordinal() + String.valueOf((Object)lv4);
        return string + "\u00a7r";
    }

    CompletableFuture<OptionalChunk<List<Chunk>>> getRegion(ChunkHolder centerChunk, int margin, IntFunction<ChunkStatus> distanceToStatus) {
        if (margin == 0) {
            ChunkStatus lv = distanceToStatus.apply(0);
            return centerChunk.load(lv, this).thenApply(chunk -> chunk.map(List::of));
        }
        int j = MathHelper.square(margin * 2 + 1);
        ArrayList<CompletableFuture<OptionalChunk<Chunk>>> list = new ArrayList<CompletableFuture<OptionalChunk<Chunk>>>(j);
        ChunkPos lv2 = centerChunk.getPos();
        for (int k = -margin; k <= margin; ++k) {
            for (int l = -margin; l <= margin; ++l) {
                int m = Math.max(Math.abs(l), Math.abs(k));
                long n = ChunkPos.toLong(lv2.x + l, lv2.z + k);
                ChunkHolder lv3 = this.getCurrentChunkHolder(n);
                if (lv3 == null) {
                    return UNLOADED_CHUNKS_FUTURE;
                }
                ChunkStatus lv4 = distanceToStatus.apply(m);
                list.add(lv3.load(lv4, this));
            }
        }
        return Util.combineSafe(list).thenApply(chunks -> {
            ArrayList<Chunk> list2 = new ArrayList<Chunk>(chunks.size());
            for (OptionalChunk lv : chunks) {
                if (lv == null) {
                    throw this.crash(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                }
                Chunk lv2 = lv.orElse(null);
                if (lv2 == null) {
                    return UNLOADED_CHUNKS;
                }
                list2.add(lv2);
            }
            return OptionalChunk.of(list2);
        });
    }

    public CrashException crash(IllegalStateException exception, String details) {
        StringBuilder stringBuilder = new StringBuilder();
        Consumer<ChunkHolder> consumer = chunkHolder -> chunkHolder.enumerateFutures().forEach(pair -> {
            ChunkStatus lv = (ChunkStatus)pair.getFirst();
            CompletableFuture completableFuture = (CompletableFuture)pair.getSecond();
            if (completableFuture != null && completableFuture.isDone() && completableFuture.join() == null) {
                stringBuilder.append(chunkHolder.getPos()).append(" - status: ").append(lv).append(" future: ").append(completableFuture).append(System.lineSeparator());
            }
        });
        stringBuilder.append("Updating:").append(System.lineSeparator());
        this.currentChunkHolders.values().forEach(consumer);
        stringBuilder.append("Visible:").append(System.lineSeparator());
        this.chunkHolders.values().forEach(consumer);
        CrashReport lv = CrashReport.create(exception, "Chunk loading");
        CrashReportSection lv2 = lv.addElement("Chunk loading");
        lv2.add("Details", details);
        lv2.add("Futures", stringBuilder);
        return new CrashException(lv);
    }

    public CompletableFuture<OptionalChunk<WorldChunk>> makeChunkEntitiesTickable(ChunkHolder holder) {
        return this.getRegion(holder, 2, distance -> ChunkStatus.FULL).thenApply(chunk -> chunk.map(chunks -> (WorldChunk)chunks.get(chunks.size() / 2)));
    }

    @Nullable
    ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int j) {
        if (!ChunkLevels.isAccessible(j) && !ChunkLevels.isAccessible(level)) {
            return holder;
        }
        if (holder != null) {
            holder.setLevel(level);
        }
        if (holder != null) {
            if (!ChunkLevels.isAccessible(level)) {
                this.unloadedChunks.add(pos);
            } else {
                this.unloadedChunks.remove(pos);
            }
        }
        if (ChunkLevels.isAccessible(level) && holder == null) {
            holder = this.chunksToUnload.remove(pos);
            if (holder != null) {
                holder.setLevel(level);
            } else {
                holder = new ChunkHolder(new ChunkPos(pos), level, this.world, this.lightingProvider, this::updateLevel, this);
            }
            this.currentChunkHolders.put(pos, holder);
            this.chunkHolderListDirty = true;
        }
        return holder;
    }

    private void updateLevel(ChunkPos pos, IntSupplier levelGetter, int targetLevel, IntConsumer levelSetter) {
        this.worldGenScheduler.updateLevel(pos, levelGetter, targetLevel, levelSetter);
        this.lightScheduler.updateLevel(pos, levelGetter, targetLevel, levelSetter);
    }

    @Override
    public void close() throws IOException {
        try {
            this.worldGenScheduler.close();
            this.lightScheduler.close();
            this.pointOfInterestStorage.close();
        } finally {
            super.close();
        }
    }

    protected void save(boolean flush) {
        if (flush) {
            List<ChunkHolder> list = this.chunkHolders.values().stream().filter(ChunkHolder::isAccessible).peek(ChunkHolder::updateAccessibleStatus).toList();
            MutableBoolean mutableBoolean = new MutableBoolean();
            do {
                mutableBoolean.setFalse();
                list.stream().map(holder -> {
                    this.mainThreadExecutor.runTasks(holder::isSavable);
                    return holder.getLatest();
                }).filter(chunk -> chunk instanceof WrapperProtoChunk || chunk instanceof WorldChunk).filter(this::save).forEach(chunk -> mutableBoolean.setTrue());
            } while (mutableBoolean.isTrue());
            this.pointOfInterestStorage.save();
            this.unloadChunks(() -> true);
            this.completeAll();
        } else {
            this.chunkToNextSaveTimeMs.clear();
            long l = Util.getMeasuringTimeMs();
            for (ChunkHolder lv : this.chunkHolders.values()) {
                this.save(lv, l);
            }
        }
    }

    protected void tick(BooleanSupplier shouldKeepTicking) {
        Profiler lv = Profilers.get();
        lv.push("poi");
        this.pointOfInterestStorage.tick(shouldKeepTicking);
        lv.swap("chunk_unload");
        if (!this.world.isSavingDisabled()) {
            this.unloadChunks(shouldKeepTicking);
        }
        lv.pop();
    }

    public boolean shouldDelayShutdown() {
        return this.lightingProvider.hasUpdates() || !this.chunksToUnload.isEmpty() || !this.currentChunkHolders.isEmpty() || this.pointOfInterestStorage.hasUnsavedElements() || !this.unloadedChunks.isEmpty() || !this.unloadTaskQueue.isEmpty() || this.worldGenScheduler.shouldDelayShutdown() || this.lightScheduler.shouldDelayShutdown() || this.levelManager.shouldDelayShutdown();
    }

    private void unloadChunks(BooleanSupplier shouldKeepTicking) {
        Runnable runnable;
        LongIterator longIterator = this.unloadedChunks.iterator();
        while (longIterator.hasNext()) {
            long l = longIterator.nextLong();
            ChunkHolder lv = this.currentChunkHolders.get(l);
            if (lv != null) {
                this.currentChunkHolders.remove(l);
                this.chunksToUnload.put(l, lv);
                this.chunkHolderListDirty = true;
                this.tryUnloadChunk(l, lv);
            }
            longIterator.remove();
        }
        for (int i = Math.max(0, this.unloadTaskQueue.size() - 2000); (i > 0 || shouldKeepTicking.getAsBoolean()) && (runnable = this.unloadTaskQueue.poll()) != null; --i) {
            runnable.run();
        }
        this.saveChunks(shouldKeepTicking);
    }

    private void saveChunks(BooleanSupplier shouldKeepTicking) {
        long l = Util.getMeasuringTimeMs();
        int i = 0;
        LongIterator longIterator = this.chunksToSave.iterator();
        while (i < 20 && this.chunksBeingSavedCount.get() < 128 && shouldKeepTicking.getAsBoolean() && longIterator.hasNext()) {
            Chunk lv2;
            long m = longIterator.nextLong();
            ChunkHolder lv = this.chunkHolders.get(m);
            Chunk chunk = lv2 = lv != null ? lv.getLatest() : null;
            if (lv2 == null || !lv2.needsSaving()) {
                longIterator.remove();
                continue;
            }
            if (!this.save(lv, l)) continue;
            ++i;
            longIterator.remove();
        }
    }

    private void tryUnloadChunk(long pos, ChunkHolder chunk) {
        CompletableFuture<?> completableFuture = chunk.getSavingFuture();
        ((CompletableFuture)completableFuture.thenRunAsync(() -> {
            CompletableFuture<?> completableFuture2 = chunk.getSavingFuture();
            if (completableFuture2 != completableFuture) {
                this.tryUnloadChunk(pos, chunk);
                return;
            }
            Chunk lv = chunk.getLatest();
            if (this.chunksToUnload.remove(pos, (Object)chunk) && lv != null) {
                WorldChunk lv2;
                if (lv instanceof WorldChunk) {
                    lv2 = (WorldChunk)lv;
                    lv2.setLoadedToWorld(false);
                }
                this.save(lv);
                if (lv instanceof WorldChunk) {
                    lv2 = (WorldChunk)lv;
                    this.world.unloadEntities(lv2);
                }
                this.lightingProvider.updateChunkStatus(lv.getPos());
                this.lightingProvider.tick();
                this.chunkToNextSaveTimeMs.remove(lv.getPos().toLong());
            }
        }, this.unloadTaskQueue::add)).whenComplete((void_, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Failed to save chunk {}", (Object)chunk.getPos(), throwable);
            }
        });
    }

    protected boolean updateHolderMap() {
        if (!this.chunkHolderListDirty) {
            return false;
        }
        this.chunkHolders = this.currentChunkHolders.clone();
        this.chunkHolderListDirty = false;
        return true;
    }

    private CompletableFuture<Chunk> loadChunk(ChunkPos pos) {
        CompletionStage completableFuture = this.getUpdatedChunkNbt(pos).thenApplyAsync(optional -> optional.map(arg2 -> {
            SerializedChunk lv = SerializedChunk.fromNbt(this.world, this.world.getPalettesFactory(), arg2);
            if (lv == null) {
                LOGGER.error("Chunk file at {} is missing level data, skipping", (Object)pos);
            }
            return lv;
        }), Util.getMainWorkerExecutor().named("parseChunk"));
        CompletableFuture<?> completableFuture2 = this.pointOfInterestStorage.load(pos);
        return ((CompletableFuture)((CompletableFuture)((CompletableFuture)completableFuture).thenCombine(completableFuture2, (optional, object) -> optional)).thenApplyAsync(nbt -> {
            Profilers.get().visit("chunkLoad");
            if (nbt.isPresent()) {
                ProtoChunk lv = ((SerializedChunk)nbt.get()).convert(this.world, this.pointOfInterestStorage, this.getStorageKey(), pos);
                this.mark(pos, ((Chunk)lv).getStatus().getChunkType());
                return lv;
            }
            return this.getProtoChunk(pos);
        }, (Executor)this.mainThreadExecutor)).exceptionallyAsync(throwable -> this.recoverFromException((Throwable)throwable, pos), (Executor)this.mainThreadExecutor);
    }

    private Chunk recoverFromException(Throwable throwable, ChunkPos chunkPos) {
        boolean bl2;
        Throwable throwable2;
        Throwable throwable22;
        if (throwable instanceof CompletionException) {
            CompletionException completionException = (CompletionException)throwable;
            v0 = completionException.getCause();
        } else {
            v0 = throwable22 = throwable;
        }
        if (throwable22 instanceof CrashException) {
            CrashException lv = (CrashException)throwable22;
            throwable2 = lv.getCause();
        } else {
            throwable2 = throwable22;
        }
        Throwable throwable3 = throwable2;
        boolean bl = throwable3 instanceof Error;
        boolean bl3 = bl2 = throwable3 instanceof IOException || throwable3 instanceof NbtException;
        if (!bl) {
            if (!bl2) {
                // empty if block
            }
        } else {
            CrashReport lv2 = CrashReport.create(throwable, "Exception loading chunk");
            CrashReportSection lv3 = lv2.addElement("Chunk being loaded");
            lv3.add("pos", chunkPos);
            this.markAsProtoChunk(chunkPos);
            throw new CrashException(lv2);
        }
        this.world.getServer().onChunkLoadFailure(throwable3, this.getStorageKey(), chunkPos);
        return this.getProtoChunk(chunkPos);
    }

    private Chunk getProtoChunk(ChunkPos chunkPos) {
        this.markAsProtoChunk(chunkPos);
        return new ProtoChunk(chunkPos, UpgradeData.NO_UPGRADE_DATA, this.world, this.world.getPalettesFactory(), null);
    }

    private void markAsProtoChunk(ChunkPos pos) {
        this.chunkToType.put(pos.toLong(), (byte)-1);
    }

    private byte mark(ChunkPos pos, ChunkType type) {
        return this.chunkToType.put(pos.toLong(), type == ChunkType.PROTOCHUNK ? (byte)-1 : 1);
    }

    @Override
    public AbstractChunkHolder acquire(long pos) {
        ChunkHolder lv = this.currentChunkHolders.get(pos);
        lv.incrementRefCount();
        return lv;
    }

    @Override
    public void release(AbstractChunkHolder chunkHolder) {
        chunkHolder.decrementRefCount();
    }

    @Override
    public CompletableFuture<Chunk> generate(AbstractChunkHolder chunkHolder, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks) {
        ChunkPos lv = chunkHolder.getPos();
        if (step.targetStatus() == ChunkStatus.EMPTY) {
            return this.loadChunk(lv);
        }
        try {
            AbstractChunkHolder lv2 = chunks.get(lv.x, lv.z);
            Chunk lv3 = lv2.getUncheckedOrNull(step.targetStatus().getPrevious());
            if (lv3 == null) {
                throw new IllegalStateException("Parent chunk missing");
            }
            return step.run(this.generationContext, chunks, lv3);
        } catch (Exception exception) {
            exception.getStackTrace();
            CrashReport lv4 = CrashReport.create(exception, "Exception generating new chunk");
            CrashReportSection lv5 = lv4.addElement("Chunk to be generated");
            lv5.add("Status being generated", () -> step.targetStatus().getId());
            lv5.add("Location", String.format(Locale.ROOT, "%d,%d", lv.x, lv.z));
            lv5.add("Position hash", ChunkPos.toLong(lv.x, lv.z));
            lv5.add("Generator", this.getChunkGenerator());
            this.mainThreadExecutor.execute(() -> {
                throw new CrashException(lv4);
            });
            throw new CrashException(lv4);
        }
    }

    @Override
    public ChunkLoader createLoader(ChunkStatus requestedStatus, ChunkPos pos) {
        ChunkLoader lv = ChunkLoader.create(this, requestedStatus, pos);
        this.loaders.add(lv);
        return lv;
    }

    private void schedule(ChunkLoader loader) {
        AbstractChunkHolder lv = loader.getHolder();
        this.worldGenScheduler.add(() -> {
            CompletableFuture<?> completableFuture = loader.run();
            if (completableFuture == null) {
                return;
            }
            completableFuture.thenRun(() -> this.schedule(loader));
        }, lv.getPos().toLong(), lv::getCompletedLevel);
    }

    @Override
    public void updateChunks() {
        this.loaders.forEach(this::schedule);
        this.loaders.clear();
    }

    public CompletableFuture<OptionalChunk<WorldChunk>> makeChunkTickable(ChunkHolder holder) {
        CompletableFuture<OptionalChunk<List<Chunk>>> completableFuture = this.getRegion(holder, 1, distance -> ChunkStatus.FULL);
        return completableFuture.thenApplyAsync(optionalChunk -> optionalChunk.map(chunks -> {
            WorldChunk lv = (WorldChunk)chunks.get(chunks.size() / 2);
            lv.runPostProcessing(this.world);
            this.world.disableTickSchedulers(lv);
            CompletableFuture<?> completableFuture = holder.getPostProcessingFuture();
            if (completableFuture.isDone()) {
                this.sendToPlayers(holder, lv);
            } else {
                completableFuture.thenAcceptAsync(object -> this.sendToPlayers(holder, lv), (Executor)this.mainThreadExecutor);
            }
            return lv;
        }), (Executor)this.mainThreadExecutor);
    }

    private void sendToPlayers(ChunkHolder chunkHolder, WorldChunk chunk) {
        ChunkPos lv = chunk.getPos();
        for (ServerPlayerEntity lv2 : this.playerChunkWatchingManager.getPlayersWatchingChunk()) {
            if (!lv2.getChunkFilter().isWithinDistance(lv)) continue;
            ServerChunkLoadingManager.track(lv2, chunk);
        }
        this.world.getChunkManager().markForUpdate(chunkHolder);
        this.world.getSubscriptionTracker().trackChunk(chunk);
    }

    public CompletableFuture<OptionalChunk<WorldChunk>> makeChunkAccessible(ChunkHolder holder) {
        return this.getRegion(holder, 1, ChunkLevels::getStatusForAdditionalLevel).thenApply(optionalChunks -> optionalChunks.map(chunks -> (WorldChunk)chunks.get(chunks.size() / 2)));
    }

    Stream<ChunkHolder> getChunkHolders(ChunkStatus status) {
        int i = ChunkLevels.getLevelFromStatus(status);
        return this.chunkHolders.values().stream().filter(holder -> holder.getLevel() <= i);
    }

    private boolean save(ChunkHolder chunkHolder, long currentTime) {
        if (!chunkHolder.isAccessible() || !chunkHolder.isSavable()) {
            return false;
        }
        Chunk lv = chunkHolder.getLatest();
        if (lv instanceof WrapperProtoChunk || lv instanceof WorldChunk) {
            if (!lv.needsSaving()) {
                return false;
            }
            long m = lv.getPos().toLong();
            long n = this.chunkToNextSaveTimeMs.getOrDefault(m, -1L);
            if (currentTime < n) {
                return false;
            }
            boolean bl = this.save(lv);
            chunkHolder.updateAccessibleStatus();
            if (bl) {
                this.chunkToNextSaveTimeMs.put(m, currentTime + 10000L);
            }
            return bl;
        }
        return false;
    }

    private boolean save(Chunk chunk) {
        this.pointOfInterestStorage.saveChunk(chunk.getPos());
        if (!chunk.tryMarkSaved()) {
            return false;
        }
        ChunkPos lv = chunk.getPos();
        try {
            ChunkStatus lv2 = chunk.getStatus();
            if (lv2.getChunkType() != ChunkType.LEVELCHUNK) {
                if (this.isLevelChunk(lv)) {
                    return false;
                }
                if (lv2 == ChunkStatus.EMPTY && chunk.getStructureStarts().values().stream().noneMatch(StructureStart::hasChildren)) {
                    return false;
                }
            }
            Profilers.get().visit("chunkSave");
            this.chunksBeingSavedCount.incrementAndGet();
            SerializedChunk lv3 = SerializedChunk.fromChunk(this.world, chunk);
            CompletableFuture<NbtCompound> completableFuture = CompletableFuture.supplyAsync(lv3::serialize, Util.getMainWorkerExecutor());
            this.setNbt(lv, completableFuture::join).handle((void_, exception) -> {
                if (exception != null) {
                    this.world.getServer().onChunkSaveFailure((Throwable)exception, this.getStorageKey(), lv);
                }
                this.chunksBeingSavedCount.decrementAndGet();
                return null;
            });
            this.mark(lv, lv2.getChunkType());
            return true;
        } catch (Exception exception2) {
            this.world.getServer().onChunkSaveFailure(exception2, this.getStorageKey(), lv);
            return false;
        }
    }

    private boolean isLevelChunk(ChunkPos pos) {
        NbtCompound lv;
        byte b = this.chunkToType.get(pos.toLong());
        if (b != 0) {
            return b == 1;
        }
        try {
            lv = this.getUpdatedChunkNbt(pos).join().orElse(null);
            if (lv == null) {
                this.markAsProtoChunk(pos);
                return false;
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to read chunk {}", (Object)pos, (Object)exception);
            this.markAsProtoChunk(pos);
            return false;
        }
        ChunkType lv2 = SerializedChunk.getChunkStatus(lv).getChunkType();
        return this.mark(pos, lv2) == 1;
    }

    protected void setViewDistance(int watchDistance) {
        int j = MathHelper.clamp(watchDistance, 2, 32);
        if (j != this.watchDistance) {
            this.watchDistance = j;
            this.levelManager.setWatchDistance(this.watchDistance);
            for (ServerPlayerEntity lv : this.playerChunkWatchingManager.getPlayersWatchingChunk()) {
                this.sendWatchPackets(lv);
            }
        }
    }

    int getViewDistance(ServerPlayerEntity player) {
        return MathHelper.clamp(player.getViewDistance(), 2, this.watchDistance);
    }

    private void track(ServerPlayerEntity player, ChunkPos pos) {
        WorldChunk lv = this.getPostProcessedChunk(pos.toLong());
        if (lv != null) {
            ServerChunkLoadingManager.track(player, lv);
        }
    }

    private static void track(ServerPlayerEntity player, WorldChunk chunk) {
        player.networkHandler.chunkDataSender.add(chunk);
    }

    private static void untrack(ServerPlayerEntity player, ChunkPos pos) {
        player.networkHandler.chunkDataSender.unload(player, pos);
    }

    @Nullable
    public WorldChunk getPostProcessedChunk(long pos) {
        ChunkHolder lv = this.getChunkHolder(pos);
        if (lv == null) {
            return null;
        }
        return lv.getPostProcessedChunk();
    }

    public int getLoadedChunkCount() {
        return this.chunkHolders.size();
    }

    public ChunkLevelManager getLevelManager() {
        return this.levelManager;
    }

    void dump(Writer writer) throws IOException {
        CsvWriter lv = CsvWriter.makeHeader().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("block_entity_count").addColumn("ticking_ticket").addColumn("ticking_level").addColumn("block_ticks").addColumn("fluid_ticks").startBody(writer);
        for (Long2ObjectMap.Entry entry : this.chunkHolders.long2ObjectEntrySet()) {
            long l = entry.getLongKey();
            ChunkPos lv2 = new ChunkPos(l);
            ChunkHolder lv3 = (ChunkHolder)entry.getValue();
            Optional<Chunk> optional = Optional.ofNullable(lv3.getLatest());
            Optional<Object> optional2 = optional.flatMap(chunk -> chunk instanceof WorldChunk ? Optional.of((WorldChunk)chunk) : Optional.empty());
            lv.printRow(lv2.x, lv2.z, lv3.getLevel(), optional.isPresent(), optional.map(Chunk::getStatus).orElse(null), optional2.map(WorldChunk::getLevelType).orElse(null), ServerChunkLoadingManager.getFutureStatus(lv3.getAccessibleFuture()), ServerChunkLoadingManager.getFutureStatus(lv3.getTickingFuture()), ServerChunkLoadingManager.getFutureStatus(lv3.getEntityTickingFuture()), this.ticketManager.getDebugString(l, false), this.shouldTick(lv2), optional2.map(chunk -> chunk.getBlockEntities().size()).orElse(0), this.ticketManager.getDebugString(l, true), this.levelManager.getLevel(l, true), optional2.map(chunk -> chunk.getBlockTickScheduler().getTickCount()).orElse(0), optional2.map(chunk -> chunk.getFluidTickScheduler().getTickCount()).orElse(0));
        }
    }

    private static String getFutureStatus(CompletableFuture<OptionalChunk<WorldChunk>> future) {
        try {
            OptionalChunk lv = future.getNow(null);
            if (lv != null) {
                return lv.isPresent() ? "done" : "unloaded";
            }
            return "not completed";
        } catch (CompletionException completionException) {
            return "failed " + completionException.getCause().getMessage();
        } catch (CancellationException cancellationException) {
            return "cancelled";
        }
    }

    private CompletableFuture<Optional<NbtCompound>> getUpdatedChunkNbt(ChunkPos chunkPos) {
        return this.getNbt(chunkPos).thenApplyAsync(nbt -> nbt.map(this::updateChunkNbt), Util.getMainWorkerExecutor().named("upgradeChunk"));
    }

    private NbtCompound updateChunkNbt(NbtCompound nbt) {
        return this.updateChunkNbt(this.world.getRegistryKey(), this.persistentStateManagerFactory, nbt, this.getChunkGenerator().getCodecKey());
    }

    void collectSpawningChunks(List<WorldChunk> chunks) {
        LongIterator longIterator = this.levelManager.iterateChunkPosToTick();
        while (longIterator.hasNext()) {
            WorldChunk lv2;
            ChunkHolder lv = this.chunkHolders.get(longIterator.nextLong());
            if (lv == null || (lv2 = lv.getWorldChunk()) == null || !this.isAnyPlayerTicking(lv.getPos())) continue;
            chunks.add(lv2);
        }
    }

    void forEachBlockTickingChunk(Consumer<WorldChunk> chunkConsumer) {
        this.levelManager.forEachBlockTickingChunk(chunkPos -> {
            ChunkHolder lv = this.chunkHolders.get(chunkPos);
            if (lv == null) {
                return;
            }
            WorldChunk lv2 = lv.getWorldChunk();
            if (lv2 == null) {
                return;
            }
            chunkConsumer.accept(lv2);
        });
    }

    boolean shouldTick(ChunkPos pos) {
        TriState lv = this.levelManager.shouldTick(pos.toLong());
        if (lv == TriState.DEFAULT) {
            return this.isAnyPlayerTicking(pos);
        }
        return lv.asBoolean(true);
    }

    private boolean isAnyPlayerTicking(ChunkPos pos) {
        for (ServerPlayerEntity lv : this.playerChunkWatchingManager.getPlayersWatchingChunk()) {
            if (!this.canTickChunk(lv, pos)) continue;
            return true;
        }
        return false;
    }

    public List<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos pos) {
        long l = pos.toLong();
        if (!this.levelManager.shouldTick(l).asBoolean(true)) {
            return List.of();
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        for (ServerPlayerEntity lv : this.playerChunkWatchingManager.getPlayersWatchingChunk()) {
            if (!this.canTickChunk(lv, pos)) continue;
            builder.add(lv);
        }
        return builder.build();
    }

    private boolean canTickChunk(ServerPlayerEntity player, ChunkPos pos) {
        if (player.isSpectator()) {
            return false;
        }
        double d = ServerChunkLoadingManager.getSquaredDistance(pos, player.getEntityPos());
        return d < 16384.0;
    }

    private static double getSquaredDistance(ChunkPos pos, Vec3d arg2) {
        double d = ChunkSectionPos.getOffsetPos(pos.x, 8);
        double e = ChunkSectionPos.getOffsetPos(pos.z, 8);
        double f = d - arg2.x;
        double g = e - arg2.z;
        return f * f + g * g;
    }

    private boolean doesNotGenerateChunks(ServerPlayerEntity player) {
        return player.isSpectator() && !this.world.getGameRules().getBoolean(GameRules.SPECTATORS_GENERATE_CHUNKS);
    }

    void handlePlayerAddedOrRemoved(ServerPlayerEntity player, boolean added) {
        boolean bl2 = this.doesNotGenerateChunks(player);
        boolean bl3 = this.playerChunkWatchingManager.isWatchInactive(player);
        if (added) {
            this.playerChunkWatchingManager.add(player, bl2);
            this.updateWatchedSection(player);
            if (!bl2) {
                this.levelManager.handleChunkEnter(ChunkSectionPos.from(player), player);
            }
            player.setChunkFilter(ChunkFilter.IGNORE_ALL);
            this.sendWatchPackets(player);
        } else {
            ChunkSectionPos lv = player.getWatchedSection();
            this.playerChunkWatchingManager.remove(player);
            if (!bl3) {
                this.levelManager.handleChunkLeave(lv, player);
            }
            this.sendWatchPackets(player, ChunkFilter.IGNORE_ALL);
        }
    }

    private void updateWatchedSection(ServerPlayerEntity player) {
        ChunkSectionPos lv = ChunkSectionPos.from(player);
        player.setWatchedSection(lv);
    }

    public void updatePosition(ServerPlayerEntity player) {
        boolean bl3;
        for (EntityTracker lv : this.entityTrackers.values()) {
            if (lv.entity == player) {
                lv.updateTrackedStatus(this.world.getPlayers());
                continue;
            }
            lv.updateTrackedStatus(player);
        }
        ChunkSectionPos lv2 = player.getWatchedSection();
        ChunkSectionPos lv3 = ChunkSectionPos.from(player);
        boolean bl = this.playerChunkWatchingManager.isWatchDisabled(player);
        boolean bl2 = this.doesNotGenerateChunks(player);
        boolean bl4 = bl3 = lv2.asLong() != lv3.asLong();
        if (bl3 || bl != bl2) {
            this.updateWatchedSection(player);
            if (!bl) {
                this.levelManager.handleChunkLeave(lv2, player);
            }
            if (!bl2) {
                this.levelManager.handleChunkEnter(lv3, player);
            }
            if (!bl && bl2) {
                this.playerChunkWatchingManager.disableWatch(player);
            }
            if (bl && !bl2) {
                this.playerChunkWatchingManager.enableWatch(player);
            }
            this.sendWatchPackets(player);
        }
    }

    private void sendWatchPackets(ServerPlayerEntity player) {
        ChunkFilter.Cylindrical lv2;
        ChunkPos lv = player.getChunkPos();
        int i = this.getViewDistance(player);
        ChunkFilter chunkFilter = player.getChunkFilter();
        if (chunkFilter instanceof ChunkFilter.Cylindrical && (lv2 = (ChunkFilter.Cylindrical)chunkFilter).center().equals(lv) && lv2.viewDistance() == i) {
            return;
        }
        this.sendWatchPackets(player, ChunkFilter.cylindrical(lv, i));
    }

    private void sendWatchPackets(ServerPlayerEntity player, ChunkFilter chunkFilter) {
        if (player.getEntityWorld() != this.world) {
            return;
        }
        ChunkFilter lv = player.getChunkFilter();
        if (chunkFilter instanceof ChunkFilter.Cylindrical) {
            ChunkFilter.Cylindrical lv3;
            ChunkFilter.Cylindrical lv2 = (ChunkFilter.Cylindrical)chunkFilter;
            if (!(lv instanceof ChunkFilter.Cylindrical) || !(lv3 = (ChunkFilter.Cylindrical)lv).center().equals(lv2.center())) {
                player.networkHandler.sendPacket(new ChunkRenderDistanceCenterS2CPacket(lv2.center().x, lv2.center().z));
            }
        }
        ChunkFilter.forEachChangedChunk(lv, chunkFilter, chunkPos -> this.track(player, (ChunkPos)chunkPos), chunkPos -> ServerChunkLoadingManager.untrack(player, chunkPos));
        player.setChunkFilter(chunkFilter);
    }

    @Override
    public List<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge) {
        Set<ServerPlayerEntity> set = this.playerChunkWatchingManager.getPlayersWatchingChunk();
        ImmutableList.Builder builder = ImmutableList.builder();
        for (ServerPlayerEntity lv : set) {
            if ((!onlyOnWatchDistanceEdge || !this.isOnTrackEdge(lv, chunkPos.x, chunkPos.z)) && (onlyOnWatchDistanceEdge || !this.isTracked(lv, chunkPos.x, chunkPos.z))) continue;
            builder.add(lv);
        }
        return builder.build();
    }

    protected void loadEntity(Entity entity) {
        if (entity instanceof EnderDragonPart) {
            return;
        }
        EntityType<?> lv = entity.getType();
        int i = lv.getMaxTrackDistance() * 16;
        if (i == 0) {
            return;
        }
        int j = lv.getTrackTickInterval();
        if (this.entityTrackers.containsKey(entity.getId())) {
            throw Util.getFatalOrPause(new IllegalStateException("Entity is already tracked!"));
        }
        EntityTracker lv2 = new EntityTracker(entity, i, j, lv.alwaysUpdateVelocity());
        this.entityTrackers.put(entity.getId(), lv2);
        lv2.updateTrackedStatus(this.world.getPlayers());
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv3 = (ServerPlayerEntity)entity;
            this.handlePlayerAddedOrRemoved(lv3, true);
            for (EntityTracker lv4 : this.entityTrackers.values()) {
                if (lv4.entity == lv3) continue;
                lv4.updateTrackedStatus(lv3);
            }
        }
    }

    protected void unloadEntity(Entity entity) {
        EntityTracker lv3;
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            this.handlePlayerAddedOrRemoved(lv, false);
            for (EntityTracker lv2 : this.entityTrackers.values()) {
                lv2.stopTracking(lv);
            }
        }
        if ((lv3 = (EntityTracker)this.entityTrackers.remove(entity.getId())) != null) {
            lv3.stopTracking();
        }
    }

    protected void tickEntityMovement() {
        for (ServerPlayerEntity lv : this.playerChunkWatchingManager.getPlayersWatchingChunk()) {
            this.sendWatchPackets(lv);
        }
        ArrayList<ServerPlayerEntity> list = Lists.newArrayList();
        List<ServerPlayerEntity> list2 = this.world.getPlayers();
        for (EntityTracker lv2 : this.entityTrackers.values()) {
            boolean bl;
            ChunkSectionPos lv3 = lv2.trackedSection;
            ChunkSectionPos lv4 = ChunkSectionPos.from(lv2.entity);
            boolean bl2 = bl = !Objects.equals(lv3, lv4);
            if (bl) {
                lv2.updateTrackedStatus(list2);
                Entity lv5 = lv2.entity;
                if (lv5 instanceof ServerPlayerEntity) {
                    list.add((ServerPlayerEntity)lv5);
                }
                lv2.trackedSection = lv4;
            }
            if (!bl && !this.levelManager.shouldTickEntities(lv4.toChunkPos().toLong())) continue;
            lv2.entry.tick();
        }
        if (!list.isEmpty()) {
            for (EntityTracker lv2 : this.entityTrackers.values()) {
                lv2.updateTrackedStatus(list);
            }
        }
    }

    public void sendToOtherNearbyPlayers(Entity entity, Packet<? super ClientPlayPacketListener> packet) {
        EntityTracker lv = (EntityTracker)this.entityTrackers.get(entity.getId());
        if (lv != null) {
            lv.sendToListeners(packet);
        }
    }

    public void sendToOtherNearbyPlayersIf(Entity entity, Packet<? super ClientPlayPacketListener> packet, Predicate<ServerPlayerEntity> predicate) {
        EntityTracker lv = (EntityTracker)this.entityTrackers.get(entity.getId());
        if (lv != null) {
            lv.sendToListenersIf(packet, predicate);
        }
    }

    protected void sendToNearbyPlayers(Entity entity, Packet<? super ClientPlayPacketListener> packet) {
        EntityTracker lv = (EntityTracker)this.entityTrackers.get(entity.getId());
        if (lv != null) {
            lv.sendToSelfAndListeners(packet);
        }
    }

    public boolean hasTrackingPlayer(Entity entity) {
        EntityTracker lv = (EntityTracker)this.entityTrackers.get(entity.getId());
        if (lv != null) {
            return !lv.listeners.isEmpty();
        }
        return false;
    }

    public void forEachEntityTrackedBy(ServerPlayerEntity player, Consumer<Entity> action) {
        for (EntityTracker lv : this.entityTrackers.values()) {
            if (!lv.listeners.contains(player.networkHandler)) continue;
            action.accept(lv.entity);
        }
    }

    public void sendChunkBiomePackets(List<Chunk> chunks) {
        HashMap<ServerPlayerEntity, List> map = new HashMap<ServerPlayerEntity, List>();
        for (Chunk lv : chunks) {
            WorldChunk lv3;
            ChunkPos lv2 = lv.getPos();
            WorldChunk lv4 = lv instanceof WorldChunk ? (lv3 = (WorldChunk)lv) : this.world.getChunk(lv2.x, lv2.z);
            for (ServerPlayerEntity lv5 : this.getPlayersWatchingChunk(lv2, false)) {
                map.computeIfAbsent(lv5, player -> new ArrayList()).add(lv4);
            }
        }
        map.forEach((player, chunksx) -> player.networkHandler.sendPacket(ChunkBiomeDataS2CPacket.create(chunksx)));
    }

    protected PointOfInterestStorage getPointOfInterestStorage() {
        return this.pointOfInterestStorage;
    }

    public String getSaveDir() {
        return this.saveDir;
    }

    void onChunkStatusChange(ChunkPos chunkPos, ChunkLevelType levelType) {
        this.chunkStatusChangeListener.onChunkStatusChange(chunkPos, levelType);
    }

    public void forceLighting(ChunkPos centerPos, int radius) {
        int j = radius + 1;
        ChunkPos.stream(centerPos, j).forEach(pos -> {
            ChunkHolder lv = this.getChunkHolder(pos.toLong());
            if (lv != null) {
                lv.combinePostProcessingFuture(this.lightingProvider.enqueue(pos.x, pos.z));
            }
        });
    }

    public void forEachChunk(Consumer<WorldChunk> action) {
        for (ChunkHolder lv : this.chunkHolders.values()) {
            WorldChunk lv2 = lv.getPostProcessedChunk();
            if (lv2 == null) continue;
            action.accept(lv2);
        }
    }

    class LevelManager
    extends ChunkLevelManager {
        protected LevelManager(ChunkTicketManager ticketManager, Executor executor, Executor mainThreadExecutor) {
            super(ticketManager, executor, mainThreadExecutor);
        }

        @Override
        protected boolean isUnloaded(long pos) {
            return ServerChunkLoadingManager.this.unloadedChunks.contains(pos);
        }

        @Override
        @Nullable
        protected ChunkHolder getChunkHolder(long pos) {
            return ServerChunkLoadingManager.this.getCurrentChunkHolder(pos);
        }

        @Override
        @Nullable
        protected ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int j) {
            return ServerChunkLoadingManager.this.setLevel(pos, level, holder, j);
        }
    }

    class EntityTracker
    implements EntityTrackerEntry.TrackerPacketSender {
        final EntityTrackerEntry entry;
        final Entity entity;
        private final int maxDistance;
        ChunkSectionPos trackedSection;
        final Set<PlayerAssociatedNetworkHandler> listeners = Sets.newIdentityHashSet();

        public EntityTracker(Entity entity, int maxDistance, int tickInterval, boolean alwaysUpdateVelocity) {
            this.entry = new EntityTrackerEntry(ServerChunkLoadingManager.this.world, entity, tickInterval, alwaysUpdateVelocity, this);
            this.entity = entity;
            this.maxDistance = maxDistance;
            this.trackedSection = ChunkSectionPos.from(entity);
        }

        public boolean equals(Object o) {
            if (o instanceof EntityTracker) {
                return ((EntityTracker)o).entity.getId() == this.entity.getId();
            }
            return false;
        }

        public int hashCode() {
            return this.entity.getId();
        }

        @Override
        public void sendToListeners(Packet<? super ClientPlayPacketListener> packet) {
            for (PlayerAssociatedNetworkHandler lv : this.listeners) {
                lv.sendPacket(packet);
            }
        }

        @Override
        public void sendToSelfAndListeners(Packet<? super ClientPlayPacketListener> packet) {
            this.sendToListeners(packet);
            Entity entity = this.entity;
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv = (ServerPlayerEntity)entity;
                lv.networkHandler.sendPacket(packet);
            }
        }

        @Override
        public void sendToListenersIf(Packet<? super ClientPlayPacketListener> packet, Predicate<ServerPlayerEntity> predicate) {
            for (PlayerAssociatedNetworkHandler lv : this.listeners) {
                if (!predicate.test(lv.getPlayer())) continue;
                lv.sendPacket(packet);
            }
        }

        public void stopTracking() {
            for (PlayerAssociatedNetworkHandler lv : this.listeners) {
                this.entry.stopTracking(lv.getPlayer());
            }
        }

        public void stopTracking(ServerPlayerEntity player) {
            if (this.listeners.remove(player.networkHandler)) {
                this.entry.stopTracking(player);
                if (this.listeners.isEmpty()) {
                    ServerChunkLoadingManager.this.world.getSubscriptionTracker().untrackEntity(this.entity);
                }
            }
        }

        public void updateTrackedStatus(ServerPlayerEntity player) {
            boolean bl;
            if (player == this.entity) {
                return;
            }
            Vec3d lv = player.getEntityPos().subtract(this.entity.getEntityPos());
            int i = ServerChunkLoadingManager.this.getViewDistance(player);
            double e = lv.x * lv.x + lv.z * lv.z;
            double d = Math.min(this.getMaxTrackDistance(), i * 16);
            double f = d * d;
            boolean bl2 = bl = e <= f && this.entity.canBeSpectated(player) && ServerChunkLoadingManager.this.isTracked(player, this.entity.getChunkPos().x, this.entity.getChunkPos().z);
            if (bl) {
                if (this.listeners.add(player.networkHandler)) {
                    this.entry.startTracking(player);
                    if (this.listeners.size() == 1) {
                        ServerChunkLoadingManager.this.world.getSubscriptionTracker().trackEntity(this.entity);
                    }
                    ServerChunkLoadingManager.this.world.getSubscriptionTracker().sendInitialIfSubscribed(player, this.entity);
                }
            } else {
                this.stopTracking(player);
            }
        }

        private int adjustTrackingDistance(int initialDistance) {
            return ServerChunkLoadingManager.this.world.getServer().adjustTrackingDistance(initialDistance);
        }

        private int getMaxTrackDistance() {
            int i = this.maxDistance;
            for (Entity lv : this.entity.getPassengersDeep()) {
                int j = lv.getType().getMaxTrackDistance() * 16;
                if (j <= i) continue;
                i = j;
            }
            return this.adjustTrackingDistance(i);
        }

        public void updateTrackedStatus(List<ServerPlayerEntity> players) {
            for (ServerPlayerEntity lv : players) {
                this.updateTrackedStatus(lv);
            }
        }
    }
}

