/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.world;

import com.mojang.logging.LogUtils;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkLoadMap;
import net.minecraft.world.chunk.ChunkLoadProgress;
import net.minecraft.world.chunk.DeltaChunkLoadProgress;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientChunkLoadProgress
implements ChunkLoadProgress {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final long THIRTY_SECONDS = TimeUnit.SECONDS.toMillis(30L);
    public static final long WAIT_UNTIL_READY_MILLIS = 500L;
    private final DeltaChunkLoadProgress delegate = new DeltaChunkLoadProgress(true);
    @Nullable
    private ChunkLoadMap chunkLoadMap;
    @Nullable
    private volatile ChunkLoadProgress.Stage stage;
    @Nullable
    private State state;
    private final long field_61937;

    public ClientChunkLoadProgress() {
        this(0L);
    }

    public ClientChunkLoadProgress(long l) {
        this.field_61937 = l;
    }

    public void setChunkLoadMap(ChunkLoadMap map) {
        this.chunkLoadMap = map;
    }

    public void startWorldLoading(ClientPlayerEntity player, ClientWorld world, WorldRenderer renderer) {
        this.state = new Start(player, world, renderer, Util.getMeasuringTimeMs() + THIRTY_SECONDS);
    }

    public void tick() {
        if (this.state != null) {
            this.state = this.state.next();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean isDone() {
        long l;
        State state = this.state;
        if (!(state instanceof Wait)) return false;
        Wait wait = (Wait)state;
        try {
            long l2;
            l = l2 = wait.readyAt();
        } catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        if (Util.getMeasuringTimeMs() < l + this.field_61937) return false;
        return true;
    }

    public void initialChunksComing() {
        if (this.state != null) {
            this.state = this.state.initialChunksComing();
        }
    }

    @Override
    public void init(ChunkLoadProgress.Stage stage, int chunks) {
        this.delegate.init(stage, chunks);
        this.stage = stage;
    }

    @Override
    public void progress(ChunkLoadProgress.Stage stage, int fullChunks, int totalChunks) {
        this.delegate.progress(stage, fullChunks, totalChunks);
    }

    @Override
    public void finish(ChunkLoadProgress.Stage stage) {
        this.delegate.finish(stage);
    }

    @Override
    public void initSpawnPos(RegistryKey<World> worldKey, ChunkPos spawnChunk) {
        if (this.chunkLoadMap != null) {
            this.chunkLoadMap.initSpawnPos(worldKey, spawnChunk);
        }
    }

    @Nullable
    public ChunkLoadMap getChunkLoadMap() {
        return this.chunkLoadMap;
    }

    public float getLoadProgress() {
        return this.delegate.getLoadProgress();
    }

    public boolean hasProgress() {
        return this.stage != null;
    }

    @Environment(value=EnvType.CLIENT)
    record Start(ClientPlayerEntity player, ClientWorld world, WorldRenderer worldRenderer, long timeoutAfter) implements State
    {
        @Override
        public State initialChunksComing() {
            return new LoadChunks(this.player, this.world, this.worldRenderer, this.timeoutAfter);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static sealed interface State
    permits Start, LoadChunks, Wait {
        default public State next() {
            return this;
        }

        default public State initialChunksComing() {
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Wait(long readyAt) implements State
    {
    }

    @Environment(value=EnvType.CLIENT)
    record LoadChunks(ClientPlayerEntity player, ClientWorld world, WorldRenderer worldRenderer, long timeoutAfter) implements State
    {
        @Override
        public State next() {
            return this.isReady() ? new Wait(Util.getMeasuringTimeMs()) : this;
        }

        private boolean isReady() {
            if (Util.getMeasuringTimeMs() > this.timeoutAfter) {
                LOGGER.warn("Timed out while waiting for the client to load chunks, letting the player into the world anyway");
                return true;
            }
            BlockPos lv = this.player.getBlockPos();
            if (this.world.isOutOfHeightLimit(lv.getY()) || this.player.isSpectator() || !this.player.isAlive()) {
                return true;
            }
            return this.worldRenderer.isRenderingReady(lv);
        }
    }
}

