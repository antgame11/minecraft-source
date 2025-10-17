/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PaintingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.util.Atlases;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class PaintingEntityRenderer
extends EntityRenderer<PaintingEntity, PaintingEntityRenderState> {
    private static final Identifier field_61800 = Identifier.ofVanilla("back");
    private final SpriteAtlasTexture field_61801;

    public PaintingEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.field_61801 = arg.getSpriteAtlasTexture(Atlases.PAINTINGS);
    }

    @Override
    public void render(PaintingEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        PaintingVariant lv = arg.variant;
        if (lv == null) {
            return;
        }
        arg2.push();
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 - arg.facing.getHorizontalQuarterTurns() * 90));
        Sprite lv2 = this.field_61801.getSprite(lv.assetId());
        Sprite lv3 = this.field_61801.getSprite(field_61800);
        this.renderPainting(arg2, arg3, RenderLayer.getEntitySolidZOffsetForward(lv3.getAtlasId()), arg.lightmapCoordinates, lv.width(), lv.height(), lv2, lv3);
        arg2.pop();
        super.render(arg, arg2, arg3, arg4);
    }

    @Override
    public PaintingEntityRenderState createRenderState() {
        return new PaintingEntityRenderState();
    }

    @Override
    public void updateRenderState(PaintingEntity arg, PaintingEntityRenderState arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        Direction lv = arg.getHorizontalFacing();
        PaintingVariant lv2 = arg.getVariant().value();
        arg2.facing = lv;
        arg2.variant = lv2;
        int i = lv2.width();
        int j = lv2.height();
        if (arg2.lightmapCoordinates.length != i * j) {
            arg2.lightmapCoordinates = new int[i * j];
        }
        float g = (float)(-i) / 2.0f;
        float h = (float)(-j) / 2.0f;
        World lv3 = arg.getEntityWorld();
        for (int k = 0; k < j; ++k) {
            for (int l = 0; l < i; ++l) {
                float m = (float)l + g + 0.5f;
                float n = (float)k + h + 0.5f;
                int o = arg.getBlockX();
                int p = MathHelper.floor(arg.getY() + (double)n);
                int q = arg.getBlockZ();
                switch (lv) {
                    case NORTH: {
                        o = MathHelper.floor(arg.getX() + (double)m);
                        break;
                    }
                    case WEST: {
                        q = MathHelper.floor(arg.getZ() - (double)m);
                        break;
                    }
                    case SOUTH: {
                        o = MathHelper.floor(arg.getX() - (double)m);
                        break;
                    }
                    case EAST: {
                        q = MathHelper.floor(arg.getZ() + (double)m);
                    }
                }
                arg2.lightmapCoordinates[l + k * i] = WorldRenderer.getLightmapCoordinates(lv3, new BlockPos(o, p, q));
            }
        }
    }

    private void renderPainting(MatrixStack arg, OrderedRenderCommandQueue arg2, RenderLayer arg32, int[] is, int i, int j, Sprite arg42, Sprite arg5) {
        arg2.submitCustom(arg, arg32, (arg3, arg4) -> {
            float f = (float)(-i) / 2.0f;
            float g = (float)(-j) / 2.0f;
            float h = 0.03125f;
            float k = arg5.getMinU();
            float l = arg5.getMaxU();
            float m = arg5.getMinV();
            float n = arg5.getMaxV();
            float o = arg5.getMinU();
            float p = arg5.getMaxU();
            float q = arg5.getMinV();
            float r = arg5.getFrameV(0.0625f);
            float s = arg5.getMinU();
            float t = arg5.getFrameU(0.0625f);
            float u = arg5.getMinV();
            float v = arg5.getMaxV();
            double d = 1.0 / (double)i;
            double e = 1.0 / (double)j;
            for (int w = 0; w < i; ++w) {
                for (int x = 0; x < j; ++x) {
                    float y = f + (float)(w + 1);
                    float z = f + (float)w;
                    float aa = g + (float)(x + 1);
                    float ab = g + (float)x;
                    int ac = is[w + x * i];
                    float ad = arg42.getFrameU((float)(d * (double)(i - w)));
                    float ae = arg42.getFrameU((float)(d * (double)(i - (w + 1))));
                    float af = arg42.getFrameV((float)(e * (double)(j - x)));
                    float ag = arg42.getFrameV((float)(e * (double)(j - (x + 1))));
                    this.vertex(arg3, arg4, y, ab, ae, af, -0.03125f, 0, 0, -1, ac);
                    this.vertex(arg3, arg4, z, ab, ad, af, -0.03125f, 0, 0, -1, ac);
                    this.vertex(arg3, arg4, z, aa, ad, ag, -0.03125f, 0, 0, -1, ac);
                    this.vertex(arg3, arg4, y, aa, ae, ag, -0.03125f, 0, 0, -1, ac);
                    this.vertex(arg3, arg4, y, aa, l, m, 0.03125f, 0, 0, 1, ac);
                    this.vertex(arg3, arg4, z, aa, k, m, 0.03125f, 0, 0, 1, ac);
                    this.vertex(arg3, arg4, z, ab, k, n, 0.03125f, 0, 0, 1, ac);
                    this.vertex(arg3, arg4, y, ab, l, n, 0.03125f, 0, 0, 1, ac);
                    this.vertex(arg3, arg4, y, aa, o, q, -0.03125f, 0, 1, 0, ac);
                    this.vertex(arg3, arg4, z, aa, p, q, -0.03125f, 0, 1, 0, ac);
                    this.vertex(arg3, arg4, z, aa, p, r, 0.03125f, 0, 1, 0, ac);
                    this.vertex(arg3, arg4, y, aa, o, r, 0.03125f, 0, 1, 0, ac);
                    this.vertex(arg3, arg4, y, ab, o, q, 0.03125f, 0, -1, 0, ac);
                    this.vertex(arg3, arg4, z, ab, p, q, 0.03125f, 0, -1, 0, ac);
                    this.vertex(arg3, arg4, z, ab, p, r, -0.03125f, 0, -1, 0, ac);
                    this.vertex(arg3, arg4, y, ab, o, r, -0.03125f, 0, -1, 0, ac);
                    this.vertex(arg3, arg4, y, aa, t, u, 0.03125f, -1, 0, 0, ac);
                    this.vertex(arg3, arg4, y, ab, t, v, 0.03125f, -1, 0, 0, ac);
                    this.vertex(arg3, arg4, y, ab, s, v, -0.03125f, -1, 0, 0, ac);
                    this.vertex(arg3, arg4, y, aa, s, u, -0.03125f, -1, 0, 0, ac);
                    this.vertex(arg3, arg4, z, aa, t, u, -0.03125f, 1, 0, 0, ac);
                    this.vertex(arg3, arg4, z, ab, t, v, -0.03125f, 1, 0, 0, ac);
                    this.vertex(arg3, arg4, z, ab, s, v, 0.03125f, 1, 0, 0, ac);
                    this.vertex(arg3, arg4, z, aa, s, u, 0.03125f, 1, 0, 0, ac);
                }
            }
        });
    }

    private void vertex(MatrixStack.Entry matrix, VertexConsumer vertexConsumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int light) {
        vertexConsumer.vertex(matrix, x, y, z).color(Colors.WHITE).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix, normalX, normalY, normalZ);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

