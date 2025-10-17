/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.command;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class LeashCommandRenderer {
    private static final int LEASH_SEGMENTS = 24;
    private static final float LEASH_WIDTH = 0.05f;

    public void render(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers) {
        for (OrderedRenderCommandQueueImpl.LeashCommand lv : queue.getLeashCommands()) {
            LeashCommandRenderer.render(lv.matricesEntry(), vertexConsumers, lv.leashState());
        }
    }

    private static void render(Matrix4f matrix, VertexConsumerProvider vertexConsumers, EntityRenderState.LeashData data) {
        int l;
        float f = (float)(data.endPos.x - data.startPos.x);
        float g = (float)(data.endPos.y - data.startPos.y);
        float h = (float)(data.endPos.z - data.startPos.z);
        float i = MathHelper.inverseSqrt(f * f + h * h) * 0.05f / 2.0f;
        float j = h * i;
        float k = f * i;
        matrix.translate((float)data.offset.x, (float)data.offset.y, (float)data.offset.z);
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getLeash());
        for (l = 0; l <= 24; ++l) {
            LeashCommandRenderer.render(lv, matrix, f, g, h, 0.05f, j, k, l, false, data);
        }
        for (l = 24; l >= 0; --l) {
            LeashCommandRenderer.render(lv, matrix, f, g, h, 0.0f, j, k, l, true, data);
        }
    }

    private static void render(VertexConsumer vertexConsumer, Matrix4f matrix, float offsetX, float offsetY, float offsetZ, float yOffset, float sideOffset, float perpendicularOffset, int segmentIndex, boolean backside, EntityRenderState.LeashData data) {
        float m = (float)segmentIndex / 24.0f;
        int n = (int)MathHelper.lerp(m, (float)data.leashedEntityBlockLight, (float)data.leashHolderBlockLight);
        int o = (int)MathHelper.lerp(m, (float)data.leashedEntitySkyLight, (float)data.leashHolderSkyLight);
        int p = LightmapTextureManager.pack(n, o);
        float q = segmentIndex % 2 == (backside ? 1 : 0) ? 0.7f : 1.0f;
        float r = 0.5f * q;
        float s = 0.4f * q;
        float t = 0.3f * q;
        float u = offsetX * m;
        float v = data.slack ? (offsetY > 0.0f ? offsetY * m * m : offsetY - offsetY * (1.0f - m) * (1.0f - m)) : offsetY * m;
        float w = offsetZ * m;
        vertexConsumer.vertex(matrix, u - sideOffset, v + yOffset, w + perpendicularOffset).color(r, s, t, 1.0f).light(p);
        vertexConsumer.vertex(matrix, u + sideOffset, v + 0.05f - yOffset, w - perpendicularOffset).color(r, s, t, 1.0f).light(p);
    }
}

