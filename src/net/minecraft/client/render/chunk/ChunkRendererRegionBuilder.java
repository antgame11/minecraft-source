/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.RenderedChunk;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

@Environment(value=EnvType.CLIENT)
public class ChunkRendererRegionBuilder {
    private final Long2ObjectMap<RenderedChunk> renderedChunksByPos = new Long2ObjectOpenHashMap<RenderedChunk>();

    public ChunkRendererRegion build(World world, long sectionPos) {
        int i = ChunkSectionPos.unpackX(sectionPos);
        int j = ChunkSectionPos.unpackY(sectionPos);
        int k = ChunkSectionPos.unpackZ(sectionPos);
        int m = i - 1;
        int n = j - 1;
        int o = k - 1;
        int p = i + 1;
        int q = j + 1;
        int r = k + 1;
        RenderedChunk[] lvs = new RenderedChunk[27];
        for (int s = o; s <= r; ++s) {
            for (int t = n; t <= q; ++t) {
                for (int u = m; u <= p; ++u) {
                    int v = ChunkRendererRegion.getIndex(m, n, o, u, t, s);
                    lvs[v] = this.getRenderedChunk(world, u, t, s);
                }
            }
        }
        return new ChunkRendererRegion(world, m, n, o, lvs);
    }

    private RenderedChunk getRenderedChunk(World world, int sectionX, int sectionY, int sectionZ) {
        return this.renderedChunksByPos.computeIfAbsent(ChunkSectionPos.asLong(sectionX, sectionY, sectionZ), pos -> {
            WorldChunk lv = world.getChunk(sectionX, sectionZ);
            return new RenderedChunk(lv, lv.sectionCoordToIndex(sectionY));
        });
    }
}

