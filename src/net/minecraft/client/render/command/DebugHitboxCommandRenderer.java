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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.state.EntityDebugInfo;
import net.minecraft.client.render.entity.state.EntityHitbox;
import net.minecraft.client.render.entity.state.EntityHitboxAndView;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class DebugHitboxCommandRenderer {
    public void render(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers) {
        for (OrderedRenderCommandQueueImpl.DebugHitboxCommand lv : queue.getDebugHitboxCommands()) {
            VertexConsumer lv2 = vertexConsumers.getBuffer(RenderLayer.getLines());
            MatrixStack lv3 = new MatrixStack();
            lv3.multiplyPositionMatrix(lv.pose());
            DebugHitboxCommandRenderer.renderDebugHitbox(lv3, lv.debugHitbox(), lv2, lv.renderState().standingEyeHeight);
            EntityDebugInfo lv4 = lv.renderState().debugInfo;
            if (lv4 == null) continue;
            if (lv4.missing()) {
                EntityHitbox lv5 = (EntityHitbox)lv.debugHitbox().hitboxes().getFirst();
                DebugRenderer.drawString(lv3, (VertexConsumerProvider)vertexConsumers, "Missing", lv.renderState().x, lv5.y1() + 1.5, lv.renderState().z, Colors.RED);
                continue;
            }
            if (lv4.hitboxes() == null) continue;
            lv3.translate(lv4.serverEntityX() - lv.renderState().x, lv4.serverEntityY() - lv.renderState().y, lv4.serverEntityZ() - lv.renderState().z);
            DebugHitboxCommandRenderer.renderDebugHitbox(lv3, lv4.hitboxes(), lv2, lv4.eyeHeight());
            Vec3d lv6 = new Vec3d(lv4.deltaMovementX(), lv4.deltaMovementY(), lv4.deltaMovementZ());
            VertexRendering.drawVector(lv3, lv2, new Vector3f(), lv6, -256);
        }
    }

    private static void renderDebugHitbox(MatrixStack matrices, EntityHitboxAndView debugHitbox, VertexConsumer vertexConsumer, float standingEyeHeight) {
        for (EntityHitbox lv : debugHitbox.hitboxes()) {
            DebugHitboxCommandRenderer.renderHitbox(matrices, vertexConsumer, lv);
        }
        Vec3d lv2 = new Vec3d(debugHitbox.viewX(), debugHitbox.viewY(), debugHitbox.viewZ());
        VertexRendering.drawVector(matrices, vertexConsumer, new Vector3f(0.0f, standingEyeHeight, 0.0f), lv2.multiply(2.0), -16776961);
    }

    private static void renderHitbox(MatrixStack matrices, VertexConsumer vertexConsumer, EntityHitbox hitbox) {
        matrices.push();
        matrices.translate(hitbox.offsetX(), hitbox.offsetY(), hitbox.offsetZ());
        VertexRendering.drawBox(matrices.peek(), vertexConsumer, hitbox.x0(), hitbox.y0(), hitbox.z0(), hitbox.x1(), hitbox.y1(), hitbox.z1(), hitbox.red(), hitbox.green(), hitbox.blue(), 1.0f);
        matrices.pop();
    }
}

