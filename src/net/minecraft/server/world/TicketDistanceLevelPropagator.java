/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelManager;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ChunkPosDistanceLevelPropagator;
import net.minecraft.server.world.ChunkTicketManager;

class TicketDistanceLevelPropagator
extends ChunkPosDistanceLevelPropagator {
    private static final int UNLOADED = ChunkLevels.INACCESSIBLE + 1;
    private final ChunkLevelManager levelManager;
    private final ChunkTicketManager ticketManager;

    public TicketDistanceLevelPropagator(ChunkLevelManager levelManager, ChunkTicketManager ticketManager) {
        super(UNLOADED + 1, 16, 256);
        this.levelManager = levelManager;
        this.ticketManager = ticketManager;
        ticketManager.setLoadingLevelUpdater(this::updateLevel);
    }

    @Override
    protected int getInitialLevel(long id) {
        return this.ticketManager.getLevel(id, false);
    }

    @Override
    protected int getLevel(long id) {
        ChunkHolder lv;
        if (!this.levelManager.isUnloaded(id) && (lv = this.levelManager.getChunkHolder(id)) != null) {
            return lv.getLevel();
        }
        return UNLOADED;
    }

    @Override
    protected void setLevel(long id, int level) {
        int j;
        ChunkHolder lv = this.levelManager.getChunkHolder(id);
        int n = j = lv == null ? UNLOADED : lv.getLevel();
        if (j == level) {
            return;
        }
        if ((lv = this.levelManager.setLevel(id, level, lv, j)) != null) {
            this.levelManager.chunkHoldersWithPendingUpdates.add(lv);
        }
    }

    public int update(int distance) {
        return this.applyPendingUpdates(distance);
    }
}

