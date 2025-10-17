/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block;

import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

@Environment(value=EnvType.CLIENT)
public class BlockModelRenderer {
    private static final Direction[] DIRECTIONS = Direction.values();
    private final BlockColors colors;
    private static final int BRIGHTNESS_CACHE_MAX_SIZE = 100;
    static final ThreadLocal<BrightnessCache> BRIGHTNESS_CACHE = ThreadLocal.withInitial(BrightnessCache::new);

    public BlockModelRenderer(BlockColors colors) {
        this.colors = colors;
    }

    public void render(BlockRenderView world, List<BlockModelPart> parts, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, int overlay) {
        if (parts.isEmpty()) {
            return;
        }
        boolean bl2 = MinecraftClient.isAmbientOcclusionEnabled() && state.getLuminance() == 0 && parts.getFirst().useAmbientOcclusion();
        matrices.translate(state.getModelOffset(pos));
        try {
            if (bl2) {
                this.renderSmooth(world, parts, state, pos, matrices, vertexConsumer, cull, overlay);
            } else {
                this.renderFlat(world, parts, state, pos, matrices, vertexConsumer, cull, overlay);
            }
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Tesselating block model");
            CrashReportSection lv2 = lv.addElement("Block model being tesselated");
            CrashReportSection.addBlockInfo(lv2, world, pos, state);
            lv2.add("Using AO", bl2);
            throw new CrashException(lv);
        }
    }

    private static boolean shouldDrawFace(BlockRenderView world, BlockState state, boolean cull, Direction side, BlockPos pos) {
        if (!cull) {
            return true;
        }
        BlockState lv = world.getBlockState(pos);
        return Block.shouldDrawSide(state, lv, side);
    }

    public void renderSmooth(BlockRenderView world, List<BlockModelPart> parts, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, int overlay) {
        AmbientOcclusionCalculator lv = new AmbientOcclusionCalculator();
        int j = 0;
        int k = 0;
        for (BlockModelPart lv2 : parts) {
            for (Direction lv3 : DIRECTIONS) {
                List<BakedQuad> list2;
                boolean bl3;
                int l = 1 << lv3.ordinal();
                boolean bl2 = (j & l) == 1;
                boolean bl = bl3 = (k & l) == 1;
                if (bl2 && !bl3 || (list2 = lv2.getQuads(lv3)).isEmpty()) continue;
                if (!bl2) {
                    bl3 = BlockModelRenderer.shouldDrawFace(world, state, cull, lv3, lv.pos.set((Vec3i)pos, lv3));
                    j |= l;
                    if (bl3) {
                        k |= l;
                    }
                }
                if (!bl3) continue;
                this.renderQuadsSmooth(world, state, pos, matrices, vertexConsumer, list2, lv, overlay);
            }
            List<BakedQuad> list3 = lv2.getQuads(null);
            if (list3.isEmpty()) continue;
            this.renderQuadsSmooth(world, state, pos, matrices, vertexConsumer, list3, lv, overlay);
        }
    }

    public void renderFlat(BlockRenderView world, List<BlockModelPart> parts, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, int overlay) {
        LightmapCache lv = new LightmapCache();
        int j = 0;
        int k = 0;
        for (BlockModelPart lv2 : parts) {
            for (Direction lv3 : DIRECTIONS) {
                List<BakedQuad> list2;
                boolean bl3;
                int l = 1 << lv3.ordinal();
                boolean bl2 = (j & l) == 1;
                boolean bl = bl3 = (k & l) == 1;
                if (bl2 && !bl3 || (list2 = lv2.getQuads(lv3)).isEmpty()) continue;
                BlockPos.Mutable lv4 = lv.pos.set((Vec3i)pos, lv3);
                if (!bl2) {
                    bl3 = BlockModelRenderer.shouldDrawFace(world, state, cull, lv3, lv4);
                    j |= l;
                    if (bl3) {
                        k |= l;
                    }
                }
                if (!bl3) continue;
                int m = lv.brightnessCache.getInt(state, world, lv4);
                this.renderQuadsFlat(world, state, pos, m, overlay, false, matrices, vertexConsumer, list2, lv);
            }
            List<BakedQuad> list3 = lv2.getQuads(null);
            if (list3.isEmpty()) continue;
            this.renderQuadsFlat(world, state, pos, -1, overlay, true, matrices, vertexConsumer, list3, lv);
        }
    }

    private void renderQuadsSmooth(BlockRenderView world, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, AmbientOcclusionCalculator ambientOcclusionCalculator, int overlay) {
        for (BakedQuad lv : quads) {
            BlockModelRenderer.getQuadDimensions(world, state, pos, lv.vertexData(), lv.face(), ambientOcclusionCalculator);
            ambientOcclusionCalculator.apply(world, state, pos, lv.face(), lv.shade());
            this.renderQuad(world, state, pos, vertexConsumer, matrices.peek(), lv, ambientOcclusionCalculator, overlay);
        }
    }

    private void renderQuad(BlockRenderView world, BlockState state, BlockPos pos, VertexConsumer vertexConsumer, MatrixStack.Entry matrixEntry, BakedQuad quad, LightmapCache lightmap, int light) {
        float h;
        float g;
        float f;
        int j = quad.tintIndex();
        if (j != -1) {
            int k;
            if (lightmap.lastTintIndex == j) {
                k = lightmap.colorOfLastTintIndex;
            } else {
                k = this.colors.getColor(state, world, pos, j);
                lightmap.lastTintIndex = j;
                lightmap.colorOfLastTintIndex = k;
            }
            f = ColorHelper.getRedFloat(k);
            g = ColorHelper.getGreenFloat(k);
            h = ColorHelper.getBlueFloat(k);
        } else {
            f = 1.0f;
            g = 1.0f;
            h = 1.0f;
        }
        vertexConsumer.quad(matrixEntry, quad, lightmap.fs, f, g, h, 1.0f, lightmap.is, light, true);
    }

    private static void getQuadDimensions(BlockRenderView world, BlockState state, BlockPos pos, int[] vertexData, Direction face, LightmapCache lightmap) {
        float m;
        float f = 32.0f;
        float g = 32.0f;
        float h = 32.0f;
        float i = -32.0f;
        float j = -32.0f;
        float k = -32.0f;
        for (int l = 0; l < 4; ++l) {
            m = Float.intBitsToFloat(vertexData[l * 8]);
            float n = Float.intBitsToFloat(vertexData[l * 8 + 1]);
            float o = Float.intBitsToFloat(vertexData[l * 8 + 2]);
            f = Math.min(f, m);
            g = Math.min(g, n);
            h = Math.min(h, o);
            i = Math.max(i, m);
            j = Math.max(j, n);
            k = Math.max(k, o);
        }
        if (lightmap instanceof AmbientOcclusionCalculator) {
            AmbientOcclusionCalculator lv = (AmbientOcclusionCalculator)lightmap;
            lv.field_58158[NeighborOrientation.WEST.index] = f;
            lv.field_58158[NeighborOrientation.EAST.index] = i;
            lv.field_58158[NeighborOrientation.DOWN.index] = g;
            lv.field_58158[NeighborOrientation.UP.index] = j;
            lv.field_58158[NeighborOrientation.NORTH.index] = h;
            lv.field_58158[NeighborOrientation.SOUTH.index] = k;
            lv.field_58158[NeighborOrientation.FLIP_WEST.index] = 1.0f - f;
            lv.field_58158[NeighborOrientation.FLIP_EAST.index] = 1.0f - i;
            lv.field_58158[NeighborOrientation.FLIP_DOWN.index] = 1.0f - g;
            lv.field_58158[NeighborOrientation.FLIP_UP.index] = 1.0f - j;
            lv.field_58158[NeighborOrientation.FLIP_NORTH.index] = 1.0f - h;
            lv.field_58158[NeighborOrientation.FLIP_SOUTH.index] = 1.0f - k;
        }
        float p = 1.0E-4f;
        m = 0.9999f;
        lightmap.field_58161 = switch (face) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN, Direction.UP -> {
                if (f >= 1.0E-4f || h >= 1.0E-4f || i <= 0.9999f || k <= 0.9999f) {
                    yield true;
                }
                yield false;
            }
            case Direction.NORTH, Direction.SOUTH -> {
                if (f >= 1.0E-4f || g >= 1.0E-4f || i <= 0.9999f || j <= 0.9999f) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST, Direction.EAST -> g >= 1.0E-4f || h >= 1.0E-4f || j <= 0.9999f || k <= 0.9999f;
        };
        lightmap.field_58160 = switch (face) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN -> {
                if (g == j && (g < 1.0E-4f || state.isFullCube(world, pos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.UP -> {
                if (g == j && (j > 0.9999f || state.isFullCube(world, pos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.NORTH -> {
                if (h == k && (h < 1.0E-4f || state.isFullCube(world, pos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (h == k && (k > 0.9999f || state.isFullCube(world, pos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (f == i && (f < 1.0E-4f || state.isFullCube(world, pos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> f == i && (i > 0.9999f || state.isFullCube(world, pos));
        };
    }

    private void renderQuadsFlat(BlockRenderView world, BlockState state, BlockPos pos, int light, int overlay, boolean useWorldLight, MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, LightmapCache lightmap) {
        for (BakedQuad lv : quads) {
            float f;
            if (useWorldLight) {
                BlockModelRenderer.getQuadDimensions(world, state, pos, lv.vertexData(), lv.face(), lightmap);
                BlockPos lv2 = lightmap.field_58160 ? lightmap.pos.set((Vec3i)pos, lv.face()) : pos;
                light = lightmap.brightnessCache.getInt(state, world, lv2);
            }
            lightmap.fs[0] = f = world.getBrightness(lv.face(), lv.shade());
            lightmap.fs[1] = f;
            lightmap.fs[2] = f;
            lightmap.fs[3] = f;
            lightmap.is[0] = light;
            lightmap.is[1] = light;
            lightmap.is[2] = light;
            lightmap.is[3] = light;
            this.renderQuad(world, state, pos, vertexConsumer, matrices.peek(), lv, lightmap, overlay);
        }
    }

    public static void render(MatrixStack.Entry entry, VertexConsumer vertexConsumer, BlockStateModel model, float red, float green, float blue, int light, int overlay) {
        for (BlockModelPart lv : model.getParts(Random.create(42L))) {
            for (Direction lv2 : DIRECTIONS) {
                BlockModelRenderer.renderQuads(entry, vertexConsumer, red, green, blue, lv.getQuads(lv2), light, overlay);
            }
            BlockModelRenderer.renderQuads(entry, vertexConsumer, red, green, blue, lv.getQuads(null), light, overlay);
        }
    }

    private static void renderQuads(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float red, float green, float blue, List<BakedQuad> quads, int light, int overlay) {
        for (BakedQuad lv : quads) {
            float m;
            float l;
            float k;
            if (lv.hasTint()) {
                k = MathHelper.clamp(red, 0.0f, 1.0f);
                l = MathHelper.clamp(green, 0.0f, 1.0f);
                m = MathHelper.clamp(blue, 0.0f, 1.0f);
            } else {
                k = 1.0f;
                l = 1.0f;
                m = 1.0f;
            }
            vertexConsumer.quad(entry, lv, k, l, m, 1.0f, light, overlay);
        }
    }

    public static void enableBrightnessCache() {
        BRIGHTNESS_CACHE.get().enable();
    }

    public static void disableBrightnessCache() {
        BRIGHTNESS_CACHE.get().disable();
    }

    @Environment(value=EnvType.CLIENT)
    static class AmbientOcclusionCalculator
    extends LightmapCache {
        final float[] field_58158 = new float[NeighborOrientation.SIZE];

        public void apply(BlockRenderView world, BlockState state, BlockPos pos, Direction direction, boolean bl) {
            float x;
            int u;
            float t;
            int s;
            float r;
            int q;
            float p;
            int o;
            float n;
            BlockState lv12;
            boolean bl5;
            BlockPos lv = this.field_58160 ? pos.offset(direction) : pos;
            NeighborData lv2 = NeighborData.getData(direction);
            BlockPos.Mutable lv3 = this.pos;
            lv3.set((Vec3i)lv, lv2.faces[0]);
            BlockState lv4 = world.getBlockState(lv3);
            int i = this.brightnessCache.getInt(lv4, world, lv3);
            float f = this.brightnessCache.getFloat(lv4, world, lv3);
            lv3.set((Vec3i)lv, lv2.faces[1]);
            BlockState lv5 = world.getBlockState(lv3);
            int j = this.brightnessCache.getInt(lv5, world, lv3);
            float g = this.brightnessCache.getFloat(lv5, world, lv3);
            lv3.set((Vec3i)lv, lv2.faces[2]);
            BlockState lv6 = world.getBlockState(lv3);
            int k = this.brightnessCache.getInt(lv6, world, lv3);
            float h = this.brightnessCache.getFloat(lv6, world, lv3);
            lv3.set((Vec3i)lv, lv2.faces[3]);
            BlockState lv7 = world.getBlockState(lv3);
            int l = this.brightnessCache.getInt(lv7, world, lv3);
            float m = this.brightnessCache.getFloat(lv7, world, lv3);
            BlockState lv8 = world.getBlockState(lv3.set((Vec3i)lv, lv2.faces[0]).move(direction));
            boolean bl2 = !lv8.shouldBlockVision(world, lv3) || lv8.getOpacity() == 0;
            BlockState lv9 = world.getBlockState(lv3.set((Vec3i)lv, lv2.faces[1]).move(direction));
            boolean bl3 = !lv9.shouldBlockVision(world, lv3) || lv9.getOpacity() == 0;
            BlockState lv10 = world.getBlockState(lv3.set((Vec3i)lv, lv2.faces[2]).move(direction));
            boolean bl4 = !lv10.shouldBlockVision(world, lv3) || lv10.getOpacity() == 0;
            BlockState lv11 = world.getBlockState(lv3.set((Vec3i)lv, lv2.faces[3]).move(direction));
            boolean bl6 = bl5 = !lv11.shouldBlockVision(world, lv3) || lv11.getOpacity() == 0;
            if (bl4 || bl2) {
                lv3.set((Vec3i)lv, lv2.faces[0]).move(lv2.faces[2]);
                lv12 = world.getBlockState(lv3);
                n = this.brightnessCache.getFloat(lv12, world, lv3);
                o = this.brightnessCache.getInt(lv12, world, lv3);
            } else {
                n = f;
                o = i;
            }
            if (bl5 || bl2) {
                lv3.set((Vec3i)lv, lv2.faces[0]).move(lv2.faces[3]);
                lv12 = world.getBlockState(lv3);
                p = this.brightnessCache.getFloat(lv12, world, lv3);
                q = this.brightnessCache.getInt(lv12, world, lv3);
            } else {
                p = f;
                q = i;
            }
            if (bl4 || bl3) {
                lv3.set((Vec3i)lv, lv2.faces[1]).move(lv2.faces[2]);
                lv12 = world.getBlockState(lv3);
                r = this.brightnessCache.getFloat(lv12, world, lv3);
                s = this.brightnessCache.getInt(lv12, world, lv3);
            } else {
                r = f;
                s = i;
            }
            if (bl5 || bl3) {
                lv3.set((Vec3i)lv, lv2.faces[1]).move(lv2.faces[3]);
                lv12 = world.getBlockState(lv3);
                t = this.brightnessCache.getFloat(lv12, world, lv3);
                u = this.brightnessCache.getInt(lv12, world, lv3);
            } else {
                t = f;
                u = i;
            }
            int v = this.brightnessCache.getInt(state, world, pos);
            lv3.set((Vec3i)pos, direction);
            BlockState lv13 = world.getBlockState(lv3);
            if (this.field_58160 || !lv13.isOpaqueFullCube()) {
                v = this.brightnessCache.getInt(lv13, world, lv3);
            }
            float w = this.field_58160 ? this.brightnessCache.getFloat(world.getBlockState(lv), world, lv) : this.brightnessCache.getFloat(world.getBlockState(pos), world, pos);
            Translation lv14 = Translation.getTranslations(direction);
            if (!this.field_58161 || !lv2.nonCubicWeight) {
                x = (m + f + p + w) * 0.25f;
                y = (h + f + n + w) * 0.25f;
                z = (h + g + r + w) * 0.25f;
                aa = (m + g + t + w) * 0.25f;
                this.is[lv14.firstCorner] = AmbientOcclusionCalculator.getAmbientOcclusionBrightness(l, i, q, v);
                this.is[lv14.secondCorner] = AmbientOcclusionCalculator.getAmbientOcclusionBrightness(k, i, o, v);
                this.is[lv14.thirdCorner] = AmbientOcclusionCalculator.getAmbientOcclusionBrightness(k, j, s, v);
                this.is[lv14.fourthCorner] = AmbientOcclusionCalculator.getAmbientOcclusionBrightness(l, j, u, v);
                this.fs[lv14.firstCorner] = x;
                this.fs[lv14.secondCorner] = y;
                this.fs[lv14.thirdCorner] = z;
                this.fs[lv14.fourthCorner] = aa;
            } else {
                x = (m + f + p + w) * 0.25f;
                y = (h + f + n + w) * 0.25f;
                z = (h + g + r + w) * 0.25f;
                aa = (m + g + t + w) * 0.25f;
                float ab = this.field_58158[lv2.field_4192[0].index] * this.field_58158[lv2.field_4192[1].index];
                float ac = this.field_58158[lv2.field_4192[2].index] * this.field_58158[lv2.field_4192[3].index];
                float ad = this.field_58158[lv2.field_4192[4].index] * this.field_58158[lv2.field_4192[5].index];
                float ae = this.field_58158[lv2.field_4192[6].index] * this.field_58158[lv2.field_4192[7].index];
                float af = this.field_58158[lv2.field_4185[0].index] * this.field_58158[lv2.field_4185[1].index];
                float ag = this.field_58158[lv2.field_4185[2].index] * this.field_58158[lv2.field_4185[3].index];
                float ah = this.field_58158[lv2.field_4185[4].index] * this.field_58158[lv2.field_4185[5].index];
                float ai = this.field_58158[lv2.field_4185[6].index] * this.field_58158[lv2.field_4185[7].index];
                float aj = this.field_58158[lv2.field_4180[0].index] * this.field_58158[lv2.field_4180[1].index];
                float ak = this.field_58158[lv2.field_4180[2].index] * this.field_58158[lv2.field_4180[3].index];
                float al = this.field_58158[lv2.field_4180[4].index] * this.field_58158[lv2.field_4180[5].index];
                float am = this.field_58158[lv2.field_4180[6].index] * this.field_58158[lv2.field_4180[7].index];
                float an = this.field_58158[lv2.field_4188[0].index] * this.field_58158[lv2.field_4188[1].index];
                float ao = this.field_58158[lv2.field_4188[2].index] * this.field_58158[lv2.field_4188[3].index];
                float ap = this.field_58158[lv2.field_4188[4].index] * this.field_58158[lv2.field_4188[5].index];
                float aq = this.field_58158[lv2.field_4188[6].index] * this.field_58158[lv2.field_4188[7].index];
                this.fs[lv14.firstCorner] = Math.clamp(x * ab + y * ac + z * ad + aa * ae, 0.0f, 1.0f);
                this.fs[lv14.secondCorner] = Math.clamp(x * af + y * ag + z * ah + aa * ai, 0.0f, 1.0f);
                this.fs[lv14.thirdCorner] = Math.clamp(x * aj + y * ak + z * al + aa * am, 0.0f, 1.0f);
                this.fs[lv14.fourthCorner] = Math.clamp(x * an + y * ao + z * ap + aa * aq, 0.0f, 1.0f);
                int ar = AmbientOcclusionCalculator.getAmbientOcclusionBrightness(l, i, q, v);
                int as = AmbientOcclusionCalculator.getAmbientOcclusionBrightness(k, i, o, v);
                int at = AmbientOcclusionCalculator.getAmbientOcclusionBrightness(k, j, s, v);
                int au = AmbientOcclusionCalculator.getAmbientOcclusionBrightness(l, j, u, v);
                this.is[lv14.firstCorner] = AmbientOcclusionCalculator.getBrightness(ar, as, at, au, ab, ac, ad, ae);
                this.is[lv14.secondCorner] = AmbientOcclusionCalculator.getBrightness(ar, as, at, au, af, ag, ah, ai);
                this.is[lv14.thirdCorner] = AmbientOcclusionCalculator.getBrightness(ar, as, at, au, aj, ak, al, am);
                this.is[lv14.fourthCorner] = AmbientOcclusionCalculator.getBrightness(ar, as, at, au, an, ao, ap, aq);
            }
            x = world.getBrightness(direction, bl);
            int av = 0;
            while (av < this.fs.length) {
                int n2 = av++;
                this.fs[n2] = this.fs[n2] * x;
            }
        }

        private static int getAmbientOcclusionBrightness(int i, int j, int k, int l) {
            if (i == 0) {
                i = l;
            }
            if (j == 0) {
                j = l;
            }
            if (k == 0) {
                k = l;
            }
            return i + j + k + l >> 2 & 0xFF00FF;
        }

        private static int getBrightness(int i, int j, int k, int l, float f, float g, float h, float m) {
            int n = (int)((float)(i >> 16 & 0xFF) * f + (float)(j >> 16 & 0xFF) * g + (float)(k >> 16 & 0xFF) * h + (float)(l >> 16 & 0xFF) * m) & 0xFF;
            int o = (int)((float)(i & 0xFF) * f + (float)(j & 0xFF) * g + (float)(k & 0xFF) * h + (float)(l & 0xFF) * m) & 0xFF;
            return n << 16 | o;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class LightmapCache {
        public final BlockPos.Mutable pos = new BlockPos.Mutable();
        public boolean field_58160;
        public boolean field_58161;
        public final float[] fs = new float[4];
        public final int[] is = new int[4];
        public int lastTintIndex = -1;
        public int colorOfLastTintIndex;
        public final BrightnessCache brightnessCache = BRIGHTNESS_CACHE.get();

        LightmapCache() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class BrightnessCache {
        private boolean enabled;
        private final Long2IntLinkedOpenHashMap intCache = Util.make(() -> {
            Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap = new Long2IntLinkedOpenHashMap(100, 0.25f){

                @Override
                protected void rehash(int newN) {
                }
            };
            long2IntLinkedOpenHashMap.defaultReturnValue(Integer.MAX_VALUE);
            return long2IntLinkedOpenHashMap;
        });
        private final Long2FloatLinkedOpenHashMap floatCache = Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(100, 0.25f){

                @Override
                protected void rehash(int newN) {
                }
            };
            long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
            return long2FloatLinkedOpenHashMap;
        });
        private final WorldRenderer.BrightnessGetter brightnessCache = (world, pos) -> {
            long l = pos.asLong();
            int i = this.intCache.get(l);
            if (i != Integer.MAX_VALUE) {
                return i;
            }
            int j = WorldRenderer.BrightnessGetter.DEFAULT.packedBrightness(world, pos);
            if (this.intCache.size() == 100) {
                this.intCache.removeFirstInt();
            }
            this.intCache.put(l, j);
            return j;
        };

        private BrightnessCache() {
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
            this.intCache.clear();
            this.floatCache.clear();
        }

        public int getInt(BlockState state, BlockRenderView world, BlockPos pos) {
            return WorldRenderer.getLightmapCoordinates(this.enabled ? this.brightnessCache : WorldRenderer.BrightnessGetter.DEFAULT, world, state, pos);
        }

        public float getFloat(BlockState state, BlockRenderView blockView, BlockPos pos) {
            float f;
            long l = pos.asLong();
            if (this.enabled && !Float.isNaN(f = this.floatCache.get(l))) {
                return f;
            }
            f = state.getAmbientOcclusionLightLevel(blockView, pos);
            if (this.enabled) {
                if (this.floatCache.size() == 100) {
                    this.floatCache.removeFirstFloat();
                }
                this.floatCache.put(l, f);
            }
            return f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum NeighborOrientation {
        DOWN(0),
        UP(1),
        NORTH(2),
        SOUTH(3),
        WEST(4),
        EAST(5),
        FLIP_DOWN(6),
        FLIP_UP(7),
        FLIP_NORTH(8),
        FLIP_SOUTH(9),
        FLIP_WEST(10),
        FLIP_EAST(11);

        public static final int SIZE;
        final int index;

        private NeighborOrientation(int index) {
            this.index = index;
        }

        static {
            SIZE = NeighborOrientation.values().length;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum NeighborData {
        DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5f, true, new NeighborOrientation[]{NeighborOrientation.FLIP_WEST, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.WEST, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_WEST, NeighborOrientation.NORTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.WEST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_EAST, NeighborOrientation.NORTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.EAST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_EAST, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.EAST, NeighborOrientation.SOUTH}),
        UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0f, true, new NeighborOrientation[]{NeighborOrientation.EAST, NeighborOrientation.SOUTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.EAST, NeighborOrientation.NORTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.WEST, NeighborOrientation.NORTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.WEST, NeighborOrientation.SOUTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.SOUTH}),
        NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8f, true, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_WEST, NeighborOrientation.UP, NeighborOrientation.WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_WEST}, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_EAST, NeighborOrientation.UP, NeighborOrientation.EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_EAST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_EAST, NeighborOrientation.DOWN, NeighborOrientation.EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_EAST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_WEST, NeighborOrientation.DOWN, NeighborOrientation.WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_WEST}),
        SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8f, true, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.WEST, NeighborOrientation.UP, NeighborOrientation.WEST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.WEST, NeighborOrientation.DOWN, NeighborOrientation.WEST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.EAST, NeighborOrientation.DOWN, NeighborOrientation.EAST}, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.EAST, NeighborOrientation.UP, NeighborOrientation.EAST}),
        WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.SOUTH, NeighborOrientation.UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_UP, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.NORTH, NeighborOrientation.UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_UP, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.NORTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.SOUTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.SOUTH}),
        EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new NeighborOrientation[]{NeighborOrientation.FLIP_DOWN, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.DOWN, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_DOWN, NeighborOrientation.NORTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.DOWN, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_UP, NeighborOrientation.NORTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.UP, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_UP, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.UP, NeighborOrientation.SOUTH});

        final Direction[] faces;
        final boolean nonCubicWeight;
        final NeighborOrientation[] field_4192;
        final NeighborOrientation[] field_4185;
        final NeighborOrientation[] field_4180;
        final NeighborOrientation[] field_4188;
        private static final NeighborData[] VALUES;

        private NeighborData(Direction[] faces, float f, boolean nonCubicWeight, NeighborOrientation[] args2, NeighborOrientation[] args3, NeighborOrientation[] args4, NeighborOrientation[] args5) {
            this.faces = faces;
            this.nonCubicWeight = nonCubicWeight;
            this.field_4192 = args2;
            this.field_4185 = args3;
            this.field_4180 = args4;
            this.field_4188 = args5;
        }

        public static NeighborData getData(Direction direction) {
            return VALUES[direction.getIndex()];
        }

        static {
            VALUES = Util.make(new NeighborData[6], values -> {
                values[Direction.DOWN.getIndex()] = DOWN;
                values[Direction.UP.getIndex()] = UP;
                values[Direction.NORTH.getIndex()] = NORTH;
                values[Direction.SOUTH.getIndex()] = SOUTH;
                values[Direction.WEST.getIndex()] = WEST;
                values[Direction.EAST.getIndex()] = EAST;
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum Translation {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        final int firstCorner;
        final int secondCorner;
        final int thirdCorner;
        final int fourthCorner;
        private static final Translation[] VALUES;

        private Translation(int firstCorner, int secondCorner, int thirdCorner, int fourthCorner) {
            this.firstCorner = firstCorner;
            this.secondCorner = secondCorner;
            this.thirdCorner = thirdCorner;
            this.fourthCorner = fourthCorner;
        }

        public static Translation getTranslations(Direction direction) {
            return VALUES[direction.getIndex()];
        }

        static {
            VALUES = Util.make(new Translation[6], values -> {
                values[Direction.DOWN.getIndex()] = DOWN;
                values[Direction.UP.getIndex()] = UP;
                values[Direction.NORTH.getIndex()] = NORTH;
                values[Direction.SOUTH.getIndex()] = SOUTH;
                values[Direction.WEST.getIndex()] = WEST;
                values[Direction.EAST.getIndex()] = EAST;
            });
        }
    }
}

