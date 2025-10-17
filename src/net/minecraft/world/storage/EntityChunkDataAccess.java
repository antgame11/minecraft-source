/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.SimpleConsecutiveExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ChunkDataAccess;
import net.minecraft.world.storage.ChunkDataList;
import net.minecraft.world.storage.ChunkPosKeyedStorage;
import org.slf4j.Logger;

public class EntityChunkDataAccess
implements ChunkDataAccess<Entity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ENTITIES_KEY = "Entities";
    private static final String POSITION_KEY = "Position";
    private final ServerWorld world;
    private final ChunkPosKeyedStorage storage;
    private final LongSet emptyChunks = new LongOpenHashSet();
    private final SimpleConsecutiveExecutor taskExecutor;

    public EntityChunkDataAccess(ChunkPosKeyedStorage storage, ServerWorld world, Executor executor) {
        this.storage = storage;
        this.world = world;
        this.taskExecutor = new SimpleConsecutiveExecutor(executor, "entity-deserializer");
    }

    @Override
    public CompletableFuture<ChunkDataList<Entity>> readChunkData(ChunkPos pos) {
        if (this.emptyChunks.contains(pos.toLong())) {
            return CompletableFuture.completedFuture(EntityChunkDataAccess.emptyDataList(pos));
        }
        CompletableFuture<Optional<NbtCompound>> completableFuture = this.storage.read(pos);
        this.handleLoadFailure(completableFuture, pos);
        return completableFuture.thenApplyAsync(nbt -> {
            if (nbt.isEmpty()) {
                this.emptyChunks.add(pos.toLong());
                return EntityChunkDataAccess.emptyDataList(pos);
            }
            try {
                ChunkPos lv = ((NbtCompound)nbt.get()).get(POSITION_KEY, ChunkPos.CODEC).orElseThrow();
                if (!Objects.equals(pos, lv)) {
                    LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", pos, pos, lv);
                    this.world.getServer().onChunkMisplacement(lv, pos, this.storage.getStorageKey());
                }
            } catch (Exception exception) {
                LOGGER.warn("Failed to parse chunk {} position info", (Object)pos, (Object)exception);
                this.world.getServer().onChunkLoadFailure(exception, this.storage.getStorageKey(), pos);
            }
            NbtCompound lv2 = this.storage.update((NbtCompound)nbt.get(), -1);
            try (ErrorReporter.Logging lv3 = new ErrorReporter.Logging(Chunk.createErrorReporterContext(pos), LOGGER);){
                ReadView lv4 = NbtReadView.create(lv3, this.world.getRegistryManager(), lv2);
                ReadView.ListReadView lv5 = lv4.getListReadView(ENTITIES_KEY);
                List<Entity> list = EntityType.streamFromData(lv5, this.world, SpawnReason.LOAD).toList();
                ChunkDataList<Entity> chunkDataList = new ChunkDataList<Entity>(pos, list);
                return chunkDataList;
            }
        }, this.taskExecutor::send);
    }

    private static ChunkDataList<Entity> emptyDataList(ChunkPos pos) {
        return new ChunkDataList<Entity>(pos, List.of());
    }

    @Override
    public void writeChunkData(ChunkDataList<Entity> dataList) {
        ChunkPos lv = dataList.getChunkPos();
        if (dataList.isEmpty()) {
            if (this.emptyChunks.add(lv.toLong())) {
                this.handleSaveFailure(this.storage.set(lv, null), lv);
            }
            return;
        }
        try (ErrorReporter.Logging lv2 = new ErrorReporter.Logging(Chunk.createErrorReporterContext(lv), LOGGER);){
            NbtList lv3 = new NbtList();
            dataList.stream().forEach(arg3 -> {
                NbtWriteView lv = NbtWriteView.create(lv2.makeChild(arg3.getErrorReporterContext()), arg3.getRegistryManager());
                if (arg3.saveData(lv)) {
                    NbtCompound lv2 = lv.getNbt();
                    lv3.add(lv2);
                }
            });
            NbtCompound lv4 = NbtHelper.putDataVersion(new NbtCompound());
            lv4.put(ENTITIES_KEY, lv3);
            lv4.put(POSITION_KEY, ChunkPos.CODEC, lv);
            this.handleSaveFailure(this.storage.set(lv, lv4), lv);
            this.emptyChunks.remove(lv.toLong());
        }
    }

    private void handleSaveFailure(CompletableFuture<?> future, ChunkPos pos) {
        future.exceptionally(throwable -> {
            LOGGER.error("Failed to store entity chunk {}", (Object)pos, throwable);
            this.world.getServer().onChunkSaveFailure((Throwable)throwable, this.storage.getStorageKey(), pos);
            return null;
        });
    }

    private void handleLoadFailure(CompletableFuture<?> future, ChunkPos pos) {
        future.exceptionally(throwable -> {
            LOGGER.error("Failed to load entity chunk {}", (Object)pos, throwable);
            this.world.getServer().onChunkLoadFailure((Throwable)throwable, this.storage.getStorageKey(), pos);
            return null;
        });
    }

    @Override
    public void awaitAll(boolean sync) {
        this.storage.completeAll(sync).join();
        this.taskExecutor.runAll();
    }

    @Override
    public void close() throws IOException {
        this.storage.close();
    }
}

