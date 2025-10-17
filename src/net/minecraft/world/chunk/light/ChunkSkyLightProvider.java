/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.VisibleForTesting
 */
package net.minecraft.world.chunk.light;

import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.ChunkSkyLight;
import net.minecraft.world.chunk.light.LightSourceView;
import net.minecraft.world.chunk.light.SkyLightStorage;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public final class ChunkSkyLightProvider
extends ChunkLightProvider<SkyLightStorage.Data, SkyLightStorage> {
    private static final long field_44743 = ChunkLightProvider.PackedInfo.packWithAllDirectionsSet(15);
    private static final long field_44744 = ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(15, Direction.UP);
    private static final long field_44745 = ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(15, false, Direction.UP);
    private final BlockPos.Mutable field_44746 = new BlockPos.Mutable();
    private final ChunkSkyLight defaultSkyLight;

    public ChunkSkyLightProvider(ChunkProvider chunkProvider) {
        this(chunkProvider, new SkyLightStorage(chunkProvider));
    }

    @VisibleForTesting
    protected ChunkSkyLightProvider(ChunkProvider chunkProvider, SkyLightStorage lightStorage) {
        super(chunkProvider, lightStorage);
        this.defaultSkyLight = new ChunkSkyLight(chunkProvider.getWorld());
    }

    private static boolean isMaxLightLevel(int lightLevel) {
        return lightLevel == 15;
    }

    private int getSkyLightOrDefault(int x, int z, int defaultValue) {
        ChunkSkyLight lv = this.getSkyLight(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
        if (lv == null) {
            return defaultValue;
        }
        return lv.get(ChunkSectionPos.getLocalCoord(x), ChunkSectionPos.getLocalCoord(z));
    }

    @Nullable
    private ChunkSkyLight getSkyLight(int chunkX, int chunkZ) {
        LightSourceView lv = this.chunkProvider.getChunk(chunkX, chunkZ);
        return lv != null ? lv.getChunkSkyLight() : null;
    }

    @Override
    protected void checkForLightUpdate(long blockPos) {
        boolean bl;
        int n;
        int i = BlockPos.unpackLongX(blockPos);
        int j = BlockPos.unpackLongY(blockPos);
        int k = BlockPos.unpackLongZ(blockPos);
        long m = ChunkSectionPos.fromBlockPos(blockPos);
        int n2 = n = ((SkyLightStorage)this.lightStorage).isSectionInEnabledColumn(m) ? this.getSkyLightOrDefault(i, k, Integer.MAX_VALUE) : Integer.MAX_VALUE;
        if (n != Integer.MAX_VALUE) {
            this.method_51590(i, k, n);
        }
        if (!((SkyLightStorage)this.lightStorage).hasSection(m)) {
            return;
        }
        boolean bl2 = bl = j >= n;
        if (bl) {
            this.queueLightDecrease(blockPos, field_44744);
            this.queueLightIncrease(blockPos, field_44745);
        } else {
            int o = ((SkyLightStorage)this.lightStorage).get(blockPos);
            if (o > 0) {
                ((SkyLightStorage)this.lightStorage).set(blockPos, 0);
                this.queueLightDecrease(blockPos, ChunkLightProvider.PackedInfo.packWithAllDirectionsSet(o));
            } else {
                this.queueLightDecrease(blockPos, field_44731);
            }
        }
    }

    private void method_51590(int i, int j, int k) {
        int l = ChunkSectionPos.getBlockCoord(((SkyLightStorage)this.lightStorage).getMinSectionY());
        this.method_51586(i, j, k, l);
        this.method_51591(i, j, k, l);
    }

    private void method_51586(int x, int z, int k, int l) {
        if (k <= l) {
            return;
        }
        int m = ChunkSectionPos.getSectionCoord(x);
        int n = ChunkSectionPos.getSectionCoord(z);
        int o = k - 1;
        int p = ChunkSectionPos.getSectionCoord(o);
        while (((SkyLightStorage)this.lightStorage).isAboveMinHeight(p)) {
            if (((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(m, p, n))) {
                int q = ChunkSectionPos.getBlockCoord(p);
                int r = q + 15;
                for (int s = Math.min(r, o); s >= q; --s) {
                    long t = BlockPos.asLong(x, s, z);
                    if (!ChunkSkyLightProvider.isMaxLightLevel(((SkyLightStorage)this.lightStorage).get(t))) {
                        return;
                    }
                    ((SkyLightStorage)this.lightStorage).set(t, 0);
                    this.queueLightDecrease(t, s == k - 1 ? field_44743 : field_44744);
                }
            }
            --p;
        }
    }

    private void method_51591(int x, int z, int k, int l) {
        int m = ChunkSectionPos.getSectionCoord(x);
        int n = ChunkSectionPos.getSectionCoord(z);
        int o = Math.max(Math.max(this.getSkyLightOrDefault(x - 1, z, Integer.MIN_VALUE), this.getSkyLightOrDefault(x + 1, z, Integer.MIN_VALUE)), Math.max(this.getSkyLightOrDefault(x, z - 1, Integer.MIN_VALUE), this.getSkyLightOrDefault(x, z + 1, Integer.MIN_VALUE)));
        int p = Math.max(k, l);
        long q = ChunkSectionPos.asLong(m, ChunkSectionPos.getSectionCoord(p), n);
        while (!((SkyLightStorage)this.lightStorage).isAtOrAboveTopmostSection(q)) {
            if (((SkyLightStorage)this.lightStorage).hasSection(q)) {
                int r = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(q));
                int s = r + 15;
                for (int t = Math.max(r, p); t <= s; ++t) {
                    long u = BlockPos.asLong(x, t, z);
                    if (ChunkSkyLightProvider.isMaxLightLevel(((SkyLightStorage)this.lightStorage).get(u))) {
                        return;
                    }
                    ((SkyLightStorage)this.lightStorage).set(u, 15);
                    if (t >= o && t != k) continue;
                    this.queueLightIncrease(u, field_44745);
                }
            }
            q = ChunkSectionPos.offset(q, Direction.UP);
        }
    }

    @Override
    protected void propagateLightIncrease(long blockPos, long packed, int lightLevel) {
        BlockState lv = null;
        int j = this.getNumberOfSectionsBelowPos(blockPos);
        for (Direction lv2 : DIRECTIONS) {
            int k;
            int o;
            long n;
            if (!ChunkLightProvider.PackedInfo.isDirectionBitSet(packed, lv2) || !((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n = BlockPos.offset(blockPos, lv2))) || (o = lightLevel - 1) <= (k = ((SkyLightStorage)this.lightStorage).get(n))) continue;
            this.field_44746.set(n);
            BlockState lv3 = this.getStateForLighting(this.field_44746);
            int p = lightLevel - this.getOpacity(lv3);
            if (p <= k) continue;
            if (lv == null) {
                BlockState blockState = lv = ChunkLightProvider.PackedInfo.isTrivial(packed) ? Blocks.AIR.getDefaultState() : this.getStateForLighting(this.field_44746.set(blockPos));
            }
            if (this.shapesCoverFullCube(lv, lv3, lv2)) continue;
            ((SkyLightStorage)this.lightStorage).set(n, p);
            if (p > 1) {
                this.queueLightIncrease(n, ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(p, ChunkSkyLightProvider.isTrivialForLighting(lv3), lv2.getOpposite()));
            }
            this.method_51587(n, lv2, p, true, j);
        }
    }

    @Override
    protected void propagateLightDecrease(long blockPos, long packed) {
        int i = this.getNumberOfSectionsBelowPos(blockPos);
        int j = ChunkLightProvider.PackedInfo.getLightLevel(packed);
        for (Direction lv : DIRECTIONS) {
            int k;
            long n;
            if (!ChunkLightProvider.PackedInfo.isDirectionBitSet(packed, lv) || !((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n = BlockPos.offset(blockPos, lv))) || (k = ((SkyLightStorage)this.lightStorage).get(n)) == 0) continue;
            if (k <= j - 1) {
                ((SkyLightStorage)this.lightStorage).set(n, 0);
                this.queueLightDecrease(n, ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(k, lv.getOpposite()));
                this.method_51587(n, lv, k, false, i);
                continue;
            }
            this.queueLightIncrease(n, ChunkLightProvider.PackedInfo.packWithRepropagate(k, false, lv.getOpposite()));
        }
    }

    private int getNumberOfSectionsBelowPos(long blockPos) {
        int i = BlockPos.unpackLongY(blockPos);
        int j = ChunkSectionPos.getLocalCoord(i);
        if (j != 0) {
            return 0;
        }
        int k = BlockPos.unpackLongX(blockPos);
        int m = BlockPos.unpackLongZ(blockPos);
        int n = ChunkSectionPos.getLocalCoord(k);
        int o = ChunkSectionPos.getLocalCoord(m);
        if (n == 0 || n == 15 || o == 0 || o == 15) {
            int p = ChunkSectionPos.getSectionCoord(k);
            int q = ChunkSectionPos.getSectionCoord(i);
            int r = ChunkSectionPos.getSectionCoord(m);
            int s = 0;
            while (!((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(p, q - s - 1, r)) && ((SkyLightStorage)this.lightStorage).isAboveMinHeight(q - s - 1)) {
                ++s;
            }
            return s;
        }
        return 0;
    }

    private void method_51587(long blockPos, Direction direction, int lightLevel, boolean bl, int j) {
        if (j == 0) {
            return;
        }
        int k = BlockPos.unpackLongX(blockPos);
        int m = BlockPos.unpackLongZ(blockPos);
        if (!ChunkSkyLightProvider.exitsChunkXZ(direction, ChunkSectionPos.getLocalCoord(k), ChunkSectionPos.getLocalCoord(m))) {
            return;
        }
        int n = BlockPos.unpackLongY(blockPos);
        int o = ChunkSectionPos.getSectionCoord(k);
        int p = ChunkSectionPos.getSectionCoord(m);
        int q = ChunkSectionPos.getSectionCoord(n) - 1;
        int r = q - j + 1;
        while (q >= r) {
            if (!((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(o, q, p))) {
                --q;
                continue;
            }
            int s = ChunkSectionPos.getBlockCoord(q);
            for (int t = 15; t >= 0; --t) {
                long u = BlockPos.asLong(k, s + t, m);
                if (bl) {
                    ((SkyLightStorage)this.lightStorage).set(u, lightLevel);
                    if (lightLevel <= 1) continue;
                    this.queueLightIncrease(u, ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(lightLevel, true, direction.getOpposite()));
                    continue;
                }
                ((SkyLightStorage)this.lightStorage).set(u, 0);
                this.queueLightDecrease(u, ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(lightLevel, direction.getOpposite()));
            }
            --q;
        }
    }

    private static boolean exitsChunkXZ(Direction direction, int localX, int localZ) {
        return switch (direction) {
            case Direction.NORTH -> {
                if (localZ == 15) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (localZ == 0) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (localX == 15) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> {
                if (localX == 0) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public void setColumnEnabled(ChunkPos pos, boolean retainData) {
        super.setColumnEnabled(pos, retainData);
        if (retainData) {
            ChunkSkyLight lv = Objects.requireNonNullElse(this.getSkyLight(pos.x, pos.z), this.defaultSkyLight);
            int i = lv.getMaxSurfaceY() - 1;
            int j = ChunkSectionPos.getSectionCoord(i) + 1;
            long l = ChunkSectionPos.withZeroY(pos.x, pos.z);
            int k = ((SkyLightStorage)this.lightStorage).getTopSectionForColumn(l);
            int m = Math.max(((SkyLightStorage)this.lightStorage).getMinSectionY(), j);
            for (int n = k - 1; n >= m; --n) {
                ChunkNibbleArray lv2 = ((SkyLightStorage)this.lightStorage).method_51547(ChunkSectionPos.asLong(pos.x, n, pos.z));
                if (lv2 == null || !lv2.isUninitialized()) continue;
                lv2.clear(15);
            }
        }
    }

    @Override
    public void propagateLight(ChunkPos chunkPos) {
        long l = ChunkSectionPos.withZeroY(chunkPos.x, chunkPos.z);
        ((SkyLightStorage)this.lightStorage).setColumnEnabled(l, true);
        ChunkSkyLight lv = Objects.requireNonNullElse(this.getSkyLight(chunkPos.x, chunkPos.z), this.defaultSkyLight);
        ChunkSkyLight lv2 = Objects.requireNonNullElse(this.getSkyLight(chunkPos.x, chunkPos.z - 1), this.defaultSkyLight);
        ChunkSkyLight lv3 = Objects.requireNonNullElse(this.getSkyLight(chunkPos.x, chunkPos.z + 1), this.defaultSkyLight);
        ChunkSkyLight lv4 = Objects.requireNonNullElse(this.getSkyLight(chunkPos.x - 1, chunkPos.z), this.defaultSkyLight);
        ChunkSkyLight lv5 = Objects.requireNonNullElse(this.getSkyLight(chunkPos.x + 1, chunkPos.z), this.defaultSkyLight);
        int i = ((SkyLightStorage)this.lightStorage).getTopSectionForColumn(l);
        int j = ((SkyLightStorage)this.lightStorage).getMinSectionY();
        int k = ChunkSectionPos.getBlockCoord(chunkPos.x);
        int m = ChunkSectionPos.getBlockCoord(chunkPos.z);
        for (int n = i - 1; n >= j; --n) {
            long o = ChunkSectionPos.asLong(chunkPos.x, n, chunkPos.z);
            ChunkNibbleArray lv6 = ((SkyLightStorage)this.lightStorage).method_51547(o);
            if (lv6 == null) continue;
            int p = ChunkSectionPos.getBlockCoord(n);
            int q = p + 15;
            boolean bl = false;
            for (int r = 0; r < 16; ++r) {
                for (int s = 0; s < 16; ++s) {
                    int t = lv.get(s, r);
                    if (t > q) continue;
                    int u = r == 0 ? lv2.get(s, 15) : lv.get(s, r - 1);
                    int v = r == 15 ? lv3.get(s, 0) : lv.get(s, r + 1);
                    int w = s == 0 ? lv4.get(15, r) : lv.get(s - 1, r);
                    int x = s == 15 ? lv5.get(0, r) : lv.get(s + 1, r);
                    int y = Math.max(Math.max(u, v), Math.max(w, x));
                    for (int z = q; z >= Math.max(p, t); --z) {
                        lv6.set(s, ChunkSectionPos.getLocalCoord(z), r, 15);
                        if (z != t && z >= y) continue;
                        long aa = BlockPos.asLong(k + s, z, m + r);
                        this.queueLightIncrease(aa, ChunkLightProvider.PackedInfo.packSkyLightPropagation(z == t, z < u, z < v, z < w, z < x));
                    }
                    if (t >= p) continue;
                    bl = true;
                }
            }
            if (!bl) break;
        }
    }
}

