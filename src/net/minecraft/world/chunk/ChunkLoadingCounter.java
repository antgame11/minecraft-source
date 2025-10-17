/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ChunkStatus;

public class ChunkLoadingCounter {
    private final List<ChunkHolder> nonFullChunks = new ArrayList<ChunkHolder>();
    private int totalChunks;

    public void load(ServerWorld world, Runnable runnable) {
        ServerChunkManager lv = world.getChunkManager();
        LongOpenHashSet longSet = new LongOpenHashSet();
        lv.updateChunks();
        lv.chunkLoadingManager.getChunkHolders(ChunkStatus.FULL).forEach(holder -> longSet.add(holder.getPos().toLong()));
        runnable.run();
        lv.updateChunks();
        lv.chunkLoadingManager.getChunkHolders(ChunkStatus.FULL).forEach(holder -> {
            if (!longSet.contains(holder.getPos().toLong())) {
                this.nonFullChunks.add((ChunkHolder)holder);
                ++this.totalChunks;
            }
        });
    }

    public int getFullChunks() {
        return this.totalChunks - this.getNonFullChunks();
    }

    public int getNonFullChunks() {
        this.nonFullChunks.removeIf(holder -> holder.getLatestStatus() == ChunkStatus.FULL);
        return this.nonFullChunks.size();
    }

    public int getTotalChunks() {
        return this.totalChunks;
    }
}

