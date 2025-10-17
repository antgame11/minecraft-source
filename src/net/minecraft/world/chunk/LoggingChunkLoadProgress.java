/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import com.mojang.logging.LogUtils;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkLoadProgress;
import net.minecraft.world.chunk.DeltaChunkLoadProgress;
import org.slf4j.Logger;

public class LoggingChunkLoadProgress
implements ChunkLoadProgress {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final boolean player;
    private final DeltaChunkLoadProgress delegate;
    private boolean done;
    private long startTimeMs = Long.MAX_VALUE;
    private long nextLogTimeMs = Long.MAX_VALUE;

    public LoggingChunkLoadProgress(boolean player) {
        this.player = player;
        this.delegate = new DeltaChunkLoadProgress(player);
    }

    public static LoggingChunkLoadProgress withoutPlayer() {
        return new LoggingChunkLoadProgress(false);
    }

    public static LoggingChunkLoadProgress withPlayer() {
        return new LoggingChunkLoadProgress(true);
    }

    @Override
    public void init(ChunkLoadProgress.Stage stage, int chunks) {
        if (this.done) {
            return;
        }
        if (this.startTimeMs == Long.MAX_VALUE) {
            long l;
            this.startTimeMs = l = Util.getMeasuringTimeMs();
            this.nextLogTimeMs = l;
        }
        this.delegate.init(stage, chunks);
        switch (stage) {
            case PREPARE_GLOBAL_SPAWN: {
                LOGGER.info("Selecting global world spawn...");
                break;
            }
            case LOAD_INITIAL_CHUNKS: {
                LOGGER.info("Loading {} persistent chunks...", (Object)chunks);
                break;
            }
            case LOAD_PLAYER_CHUNKS: {
                LOGGER.info("Loading {} chunks for player spawn...", (Object)chunks);
            }
        }
    }

    @Override
    public void progress(ChunkLoadProgress.Stage stage, int fullChunks, int totalChunks) {
        if (this.done) {
            return;
        }
        this.delegate.progress(stage, fullChunks, totalChunks);
        if (Util.getMeasuringTimeMs() > this.nextLogTimeMs) {
            this.nextLogTimeMs += 500L;
            int k = MathHelper.floor(this.delegate.getLoadProgress() * 100.0f);
            LOGGER.info(Text.translatable("menu.preparingSpawn", k).getString());
        }
    }

    @Override
    public void finish(ChunkLoadProgress.Stage stage) {
        ChunkLoadProgress.Stage lv;
        if (this.done) {
            return;
        }
        this.delegate.finish(stage);
        ChunkLoadProgress.Stage stage2 = lv = this.player ? ChunkLoadProgress.Stage.LOAD_PLAYER_CHUNKS : ChunkLoadProgress.Stage.LOAD_INITIAL_CHUNKS;
        if (stage == lv) {
            LOGGER.info("Time elapsed: {} ms", (Object)(Util.getMeasuringTimeMs() - this.startTimeMs));
            this.nextLogTimeMs = Long.MAX_VALUE;
            this.done = true;
        }
    }

    @Override
    public void initSpawnPos(RegistryKey<World> worldKey, ChunkPos spawnChunk) {
    }
}

