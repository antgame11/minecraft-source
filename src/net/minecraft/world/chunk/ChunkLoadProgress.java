/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public interface ChunkLoadProgress {
    public static ChunkLoadProgress compose(final ChunkLoadProgress first, final ChunkLoadProgress second) {
        return new ChunkLoadProgress(){

            @Override
            public void init(Stage stage, int chunks) {
                first.init(stage, chunks);
                second.init(stage, chunks);
            }

            @Override
            public void progress(Stage stage, int fullChunks, int totalChunks) {
                first.progress(stage, fullChunks, totalChunks);
                second.progress(stage, fullChunks, totalChunks);
            }

            @Override
            public void finish(Stage stage) {
                first.finish(stage);
                second.finish(stage);
            }

            @Override
            public void initSpawnPos(RegistryKey<World> worldKey, ChunkPos spawnChunk) {
                first.initSpawnPos(worldKey, spawnChunk);
                second.initSpawnPos(worldKey, spawnChunk);
            }
        };
    }

    public void init(Stage var1, int var2);

    public void progress(Stage var1, int var2, int var3);

    public void finish(Stage var1);

    public void initSpawnPos(RegistryKey<World> var1, ChunkPos var2);

    public static enum Stage {
        START_SERVER,
        PREPARE_GLOBAL_SPAWN,
        LOAD_INITIAL_CHUNKS,
        LOAD_PLAYER_CHUNKS;

    }
}

