/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk.light;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.BlockLightStorage;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.LightSourceView;

public final class ChunkBlockLightProvider
extends ChunkLightProvider<BlockLightStorage.Data, BlockLightStorage> {
    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

    public ChunkBlockLightProvider(ChunkProvider chunkProvider) {
        this(chunkProvider, new BlockLightStorage(chunkProvider));
    }

    @VisibleForTesting
    public ChunkBlockLightProvider(ChunkProvider chunkProvider, BlockLightStorage blockLightStorage) {
        super(chunkProvider, blockLightStorage);
    }

    @Override
    protected void checkForLightUpdate(long blockPos) {
        int j;
        long m = ChunkSectionPos.fromBlockPos(blockPos);
        if (!((BlockLightStorage)this.lightStorage).hasSection(m)) {
            return;
        }
        BlockState lv = this.getStateForLighting(this.mutablePos.set(blockPos));
        int i = this.getLightSourceLuminance(blockPos, lv);
        if (i < (j = ((BlockLightStorage)this.lightStorage).get(blockPos))) {
            ((BlockLightStorage)this.lightStorage).set(blockPos, 0);
            this.queueLightDecrease(blockPos, ChunkLightProvider.PackedInfo.packWithAllDirectionsSet(j));
        } else {
            this.queueLightDecrease(blockPos, field_44731);
        }
        if (i > 0) {
            this.queueLightIncrease(blockPos, ChunkLightProvider.PackedInfo.packWithForce(i, ChunkBlockLightProvider.isTrivialForLighting(lv)));
        }
    }

    @Override
    protected void propagateLightIncrease(long blockPos, long packed, int lightLevel) {
        BlockState lv = null;
        for (Direction lv2 : DIRECTIONS) {
            int j;
            int k;
            long n;
            if (!ChunkLightProvider.PackedInfo.isDirectionBitSet(packed, lv2) || !((BlockLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n = BlockPos.offset(blockPos, lv2))) || (k = lightLevel - 1) <= (j = ((BlockLightStorage)this.lightStorage).get(n))) continue;
            this.mutablePos.set(n);
            BlockState lv3 = this.getStateForLighting(this.mutablePos);
            int o = lightLevel - this.getOpacity(lv3);
            if (o <= j) continue;
            if (lv == null) {
                BlockState blockState = lv = ChunkLightProvider.PackedInfo.isTrivial(packed) ? Blocks.AIR.getDefaultState() : this.getStateForLighting(this.mutablePos.set(blockPos));
            }
            if (this.shapesCoverFullCube(lv, lv3, lv2)) continue;
            ((BlockLightStorage)this.lightStorage).set(n, o);
            if (o <= 1) continue;
            this.queueLightIncrease(n, ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(o, ChunkBlockLightProvider.isTrivialForLighting(lv3), lv2.getOpposite()));
        }
    }

    @Override
    protected void propagateLightDecrease(long blockPos, long packed) {
        int i = ChunkLightProvider.PackedInfo.getLightLevel(packed);
        for (Direction lv : DIRECTIONS) {
            int j;
            long n;
            if (!ChunkLightProvider.PackedInfo.isDirectionBitSet(packed, lv) || !((BlockLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n = BlockPos.offset(blockPos, lv))) || (j = ((BlockLightStorage)this.lightStorage).get(n)) == 0) continue;
            if (j <= i - 1) {
                BlockState lv2 = this.getStateForLighting(this.mutablePos.set(n));
                int k = this.getLightSourceLuminance(n, lv2);
                ((BlockLightStorage)this.lightStorage).set(n, 0);
                if (k < j) {
                    this.queueLightDecrease(n, ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(j, lv.getOpposite()));
                }
                if (k <= 0) continue;
                this.queueLightIncrease(n, ChunkLightProvider.PackedInfo.packWithForce(k, ChunkBlockLightProvider.isTrivialForLighting(lv2)));
                continue;
            }
            this.queueLightIncrease(n, ChunkLightProvider.PackedInfo.packWithRepropagate(j, false, lv.getOpposite()));
        }
    }

    private int getLightSourceLuminance(long blockPos, BlockState blockState) {
        int i = blockState.getLuminance();
        if (i > 0 && ((BlockLightStorage)this.lightStorage).isSectionInEnabledColumn(ChunkSectionPos.fromBlockPos(blockPos))) {
            return i;
        }
        return 0;
    }

    @Override
    public void propagateLight(ChunkPos chunkPos) {
        this.setColumnEnabled(chunkPos, true);
        LightSourceView lv = this.chunkProvider.getChunk(chunkPos.x, chunkPos.z);
        if (lv != null) {
            lv.forEachLightSource((blockPos, blockState) -> {
                int i = blockState.getLuminance();
                this.queueLightIncrease(blockPos.asLong(), ChunkLightProvider.PackedInfo.packWithForce(i, ChunkBlockLightProvider.isTrivialForLighting(blockState)));
            });
        }
    }
}

