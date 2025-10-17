/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public interface ChunkLoadMap {
    public void initSpawnPos(RegistryKey<World> var1, ChunkPos var2);

    @Nullable
    public ChunkStatus getStatus(int var1, int var2);

    public int getRadius();
}

