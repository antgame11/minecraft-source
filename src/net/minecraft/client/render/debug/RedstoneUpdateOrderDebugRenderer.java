/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class RedstoneUpdateOrderDebugRenderer
implements DebugRenderer.Renderer {
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getLines());
        store.forEachBlockData(DebugSubscriptionTypes.REDSTONE_WIRE_ORIENTATIONS, (pos, data) -> {
            Vector3f vector3f = pos.toBottomCenterPos().subtract(cameraX, cameraY - 0.1, cameraZ).toVector3f();
            VertexRendering.drawVector(matrices, lv, vector3f, data.getFront().getDoubleVector().multiply(0.5), -16776961);
            VertexRendering.drawVector(matrices, lv, vector3f, data.getUp().getDoubleVector().multiply(0.4), -65536);
            VertexRendering.drawVector(matrices, lv, vector3f, data.getRight().getDoubleVector().multiply(0.3), -256);
        });
    }
}

