/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.debug;

import java.util.function.BiConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.debug.DebugSubscriptionType;
import org.jetbrains.annotations.Nullable;

public interface DebugDataStore {
    public <T> void forEachChunkData(DebugSubscriptionType<T> var1, BiConsumer<ChunkPos, T> var2);

    @Nullable
    public <T> T getChunkData(DebugSubscriptionType<T> var1, ChunkPos var2);

    public <T> void forEachBlockData(DebugSubscriptionType<T> var1, BiConsumer<BlockPos, T> var2);

    @Nullable
    public <T> T getBlockData(DebugSubscriptionType<T> var1, BlockPos var2);

    public <T> void forEachEntityData(DebugSubscriptionType<T> var1, BiConsumer<Entity, T> var2);

    @Nullable
    public <T> T getEntityData(DebugSubscriptionType<T> var1, Entity var2);

    public <T> void forEachEvent(DebugSubscriptionType<T> var1, EventConsumer<T> var2);

    @FunctionalInterface
    public static interface EventConsumer<T> {
        public void accept(T var1, int var2, int var3);
    }
}

