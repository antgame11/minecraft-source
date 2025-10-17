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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ShadowPiecesCommandRenderer {
    private static final RenderLayer renderLayer = RenderLayer.getEntityShadow(Identifier.ofVanilla("textures/misc/shadow.png"));

    public void render(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers) {
        VertexConsumer lv = vertexConsumers.getBuffer(renderLayer);
        for (OrderedRenderCommandQueueImpl.ShadowPiecesCommand lv2 : queue.getShadowPiecesCommands()) {
            for (EntityRenderState.ShadowPiece lv3 : lv2.pieces()) {
                Box lv4 = lv3.shapeBelow().getBoundingBox();
                float f = lv3.relativeX() + (float)lv4.minX;
                float g = lv3.relativeX() + (float)lv4.maxX;
                float h = lv3.relativeY() + (float)lv4.minY;
                float i = lv3.relativeZ() + (float)lv4.minZ;
                float j = lv3.relativeZ() + (float)lv4.maxZ;
                float k = lv2.radius();
                float l = -f / 2.0f / k + 0.5f;
                float m = -g / 2.0f / k + 0.5f;
                float n = -i / 2.0f / k + 0.5f;
                float o = -j / 2.0f / k + 0.5f;
                int p = ColorHelper.getWhite(lv3.alpha());
                ShadowPiecesCommandRenderer.vertex(lv2.matricesEntry(), lv, p, f, h, i, l, n);
                ShadowPiecesCommandRenderer.vertex(lv2.matricesEntry(), lv, p, f, h, j, l, o);
                ShadowPiecesCommandRenderer.vertex(lv2.matricesEntry(), lv, p, g, h, j, m, o);
                ShadowPiecesCommandRenderer.vertex(lv2.matricesEntry(), lv, p, g, h, i, m, n);
            }
        }
    }

    private static void vertex(Matrix4f matrix, VertexConsumer vertexConsumer, int color, float x, float y, float z, float u, float v) {
        Vector3f vector3f = matrix.transformPosition(x, y, z, new Vector3f());
        vertexConsumer.vertex(vector3f.x(), vector3f.y(), vector3f.z(), color, u, v, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, 0.0f, 1.0f, 0.0f);
    }
}

