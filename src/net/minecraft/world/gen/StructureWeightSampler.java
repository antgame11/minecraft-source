/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.Nullable;

public class StructureWeightSampler
implements DensityFunctionTypes.Beardifying {
    public static final int INDEX_OFFSET = 12;
    private static final int EDGE_LENGTH = 24;
    private static final float[] STRUCTURE_WEIGHT_TABLE = Util.make(new float[13824], array -> {
        for (int i = 0; i < 24; ++i) {
            for (int j = 0; j < 24; ++j) {
                for (int k = 0; k < 24; ++k) {
                    array[i * 24 * 24 + j * 24 + k] = (float)StructureWeightSampler.calculateStructureWeight(j - 12, k - 12, i - 12);
                }
            }
        }
    });
    public static final StructureWeightSampler field_61464 = new StructureWeightSampler(List.of(), List.of(), null);
    private final List<Piece> field_61465;
    private final List<JigsawJunction> field_61466;
    @Nullable
    private final BlockBox field_61467;

    public static StructureWeightSampler createStructureWeightSampler(StructureAccessor arg, ChunkPos arg2) {
        List<StructureStart> list = arg.getStructureStarts(arg2, structure -> structure.getTerrainAdaptation() != StructureTerrainAdaptation.NONE);
        if (list.isEmpty()) {
            return field_61464;
        }
        int i = arg2.getStartX();
        int j = arg2.getStartZ();
        ArrayList<Piece> list2 = new ArrayList<Piece>();
        ArrayList<JigsawJunction> list3 = new ArrayList<JigsawJunction>();
        BlockBox lv = null;
        for (StructureStart lv2 : list) {
            StructureTerrainAdaptation lv3 = lv2.getStructure().getTerrainAdaptation();
            for (StructurePiece lv4 : lv2.getChildren()) {
                if (!lv4.intersectsChunk(arg2, 12)) continue;
                if (lv4 instanceof PoolStructurePiece) {
                    PoolStructurePiece lv5 = (PoolStructurePiece)lv4;
                    StructurePool.Projection lv6 = lv5.getPoolElement().getProjection();
                    if (lv6 == StructurePool.Projection.RIGID) {
                        list2.add(new Piece(lv5.getBoundingBox(), lv3, lv5.getGroundLevelDelta()));
                        lv = StructureWeightSampler.method_72681(lv, lv4.getBoundingBox());
                    }
                    for (JigsawJunction lv7 : lv5.getJunctions()) {
                        int k = lv7.getSourceX();
                        int l = lv7.getSourceZ();
                        if (k <= i - 12 || l <= j - 12 || k >= i + 15 + 12 || l >= j + 15 + 12) continue;
                        list3.add(lv7);
                        BlockBox lv8 = new BlockBox(new BlockPos(k, lv7.getSourceGroundY(), l));
                        lv = StructureWeightSampler.method_72681(lv, lv8);
                    }
                    continue;
                }
                list2.add(new Piece(lv4.getBoundingBox(), lv3, 0));
                lv = StructureWeightSampler.method_72681(lv, lv4.getBoundingBox());
            }
        }
        if (lv == null) {
            return field_61464;
        }
        BlockBox lv9 = lv.expand(24);
        return new StructureWeightSampler(List.copyOf(list2), List.copyOf(list3), lv9);
    }

    private static BlockBox method_72681(@Nullable BlockBox arg, BlockBox arg2) {
        if (arg == null) {
            return arg2;
        }
        return BlockBox.createEncompassing(arg, arg2);
    }

    @VisibleForTesting
    public StructureWeightSampler(List<Piece> list, List<JigsawJunction> list2, @Nullable BlockBox arg) {
        this.field_61465 = list;
        this.field_61466 = list2;
        this.field_61467 = arg;
    }

    @Override
    public void fill(double[] densities, DensityFunction.EachApplier applier) {
        if (this.field_61467 == null) {
            Arrays.fill(densities, 0.0);
        } else {
            DensityFunctionTypes.Beardifying.super.fill(densities, applier);
        }
    }

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        int m;
        int l;
        int k;
        int j;
        if (this.field_61467 == null) {
            return 0.0;
        }
        int i = pos.blockX();
        if (!this.field_61467.contains(i, j = pos.blockY(), k = pos.blockZ())) {
            return 0.0;
        }
        double d = 0.0;
        for (Piece lv : this.field_61465) {
            BlockBox lv2 = lv.box();
            l = lv.groundLevelDelta();
            m = Math.max(0, Math.max(lv2.getMinX() - i, i - lv2.getMaxX()));
            int n = Math.max(0, Math.max(lv2.getMinZ() - k, k - lv2.getMaxZ()));
            int o = lv2.getMinY() + l;
            int p = j - o;
            int q = switch (lv.terrainAdjustment()) {
                default -> throw new MatchException(null, null);
                case StructureTerrainAdaptation.NONE -> 0;
                case StructureTerrainAdaptation.BURY, StructureTerrainAdaptation.BEARD_THIN -> p;
                case StructureTerrainAdaptation.BEARD_BOX -> Math.max(0, Math.max(o - j, j - lv2.getMaxY()));
                case StructureTerrainAdaptation.ENCAPSULATE -> Math.max(0, Math.max(lv2.getMinY() - j, j - lv2.getMaxY()));
            };
            d += (switch (lv.terrainAdjustment()) {
                default -> throw new MatchException(null, null);
                case StructureTerrainAdaptation.NONE -> 0.0;
                case StructureTerrainAdaptation.BURY -> StructureWeightSampler.getMagnitudeWeight(m, (double)q / 2.0, n);
                case StructureTerrainAdaptation.BEARD_THIN, StructureTerrainAdaptation.BEARD_BOX -> StructureWeightSampler.getStructureWeight(m, q, n, p) * 0.8;
                case StructureTerrainAdaptation.ENCAPSULATE -> StructureWeightSampler.getMagnitudeWeight((double)m / 2.0, (double)q / 2.0, (double)n / 2.0) * 0.8;
            });
        }
        for (JigsawJunction lv3 : this.field_61466) {
            int r = i - lv3.getSourceX();
            l = j - lv3.getSourceGroundY();
            m = k - lv3.getSourceZ();
            d += StructureWeightSampler.getStructureWeight(r, l, m, l) * 0.4;
        }
        return d;
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    private static double getMagnitudeWeight(double x, double y, double z) {
        double g = MathHelper.magnitude(x, y, z);
        return MathHelper.clampedMap(g, 0.0, 6.0, 1.0, 0.0);
    }

    private static double getStructureWeight(int x, int y, int z, int yy) {
        int m = x + 12;
        int n = y + 12;
        int o = z + 12;
        if (!(StructureWeightSampler.indexInBounds(m) && StructureWeightSampler.indexInBounds(n) && StructureWeightSampler.indexInBounds(o))) {
            return 0.0;
        }
        double d = (double)yy + 0.5;
        double e = MathHelper.squaredMagnitude(x, d, z);
        double f = -d * MathHelper.fastInverseSqrt(e / 2.0) / 2.0;
        return f * (double)STRUCTURE_WEIGHT_TABLE[o * 24 * 24 + m * 24 + n];
    }

    private static boolean indexInBounds(int i) {
        return i >= 0 && i < 24;
    }

    private static double calculateStructureWeight(int x, int y, int z) {
        return StructureWeightSampler.structureWeight(x, (double)y + 0.5, z);
    }

    private static double structureWeight(int x, double y, int z) {
        double e = MathHelper.squaredMagnitude(x, y, z);
        double f = Math.pow(Math.E, -e / 16.0);
        return f;
    }

    @VisibleForTesting
    public record Piece(BlockBox box, StructureTerrainAdaptation terrainAdjustment, int groundLevelDelta) {
    }
}

