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
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class FireCommandRenderer {
    public void render(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers, AtlasManager atlasManager) {
        for (OrderedRenderCommandQueueImpl.FireCommand lv : queue.getFireCommands()) {
            this.render(lv.matricesEntry(), vertexConsumers, lv.renderState(), lv.rotation(), atlasManager);
        }
    }

    private void render(MatrixStack.Entry matricesEntry, VertexConsumerProvider vertexConsumers, EntityRenderState renderState, Quaternionf rotation, AtlasManager atlasManager) {
        Sprite lv = atlasManager.getSprite(ModelBaker.FIRE_0);
        Sprite lv2 = atlasManager.getSprite(ModelBaker.FIRE_1);
        float f = renderState.width * 1.4f;
        matricesEntry.scale(f, f, f);
        float g = 0.5f;
        float h = 0.0f;
        float i = renderState.height / f;
        float j = 0.0f;
        matricesEntry.rotate(rotation);
        matricesEntry.translate(0.0f, 0.0f, 0.3f - (float)((int)i) * 0.02f);
        float k = 0.0f;
        int l = 0;
        VertexConsumer lv3 = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        while (i > 0.0f) {
            Sprite lv4 = l % 2 == 0 ? lv : lv2;
            float m = lv4.getMinU();
            float n = lv4.getMinV();
            float o = lv4.getMaxU();
            float p = lv4.getMaxV();
            if (l / 2 % 2 == 0) {
                float q = o;
                o = m;
                m = q;
            }
            FireCommandRenderer.vertex(matricesEntry, lv3, -g - 0.0f, 0.0f - j, k, o, p);
            FireCommandRenderer.vertex(matricesEntry, lv3, g - 0.0f, 0.0f - j, k, m, p);
            FireCommandRenderer.vertex(matricesEntry, lv3, g - 0.0f, 1.4f - j, k, m, n);
            FireCommandRenderer.vertex(matricesEntry, lv3, -g - 0.0f, 1.4f - j, k, o, n);
            i -= 0.45f;
            j -= 0.45f;
            g *= 0.9f;
            k -= 0.03f;
            ++l;
        }
    }

    private static void vertex(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v) {
        vertexConsumer.vertex(matricesEntry, x, y, z).color(Colors.WHITE).texture(u, v).overlay(0, 10).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).normal(matricesEntry, 0.0f, 1.0f, 0.0f);
    }
}

