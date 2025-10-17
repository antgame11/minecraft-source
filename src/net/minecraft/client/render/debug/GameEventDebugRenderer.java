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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;

@Environment(value=EnvType.CLIENT)
public class GameEventDebugRenderer
implements DebugRenderer.Renderer {
    private static final float field_32900 = 1.0f;

    private void forEachEventData(DebugDataStore dataStore, EventConsumer consumer) {
        dataStore.forEachBlockData(DebugSubscriptionTypes.GAME_EVENT_LISTENERS, (pos, data) -> consumer.accept(pos.toCenterPos(), data.listenerRadius()));
        dataStore.forEachEntityData(DebugSubscriptionTypes.GAME_EVENT_LISTENERS, (entity, data) -> consumer.accept(entity.getEntityPos(), data.listenerRadius()));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getLines());
        this.forEachEventData(store, (pos, radius) -> {
            double g = (double)radius * 2.0;
            DebugRenderer.drawVoxelShapeOutlines(matrices, lv, VoxelShapes.cuboid(Box.of(pos, g, g, g)), -cameraX, -cameraY, -cameraZ, 1.0f, 1.0f, 0.0f, 0.35f, true);
        });
        VertexConsumer lv2 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
        this.forEachEventData(store, (pos, radius) -> VertexRendering.drawFilledBox(matrices, lv2, pos.getX() - 0.25 - cameraX, pos.getY() - cameraY, pos.getZ() - 0.25 - cameraZ, pos.getX() + 0.25 - cameraX, pos.getY() - cameraY + 1.0, pos.getZ() + 0.25 - cameraZ, 1.0f, 1.0f, 0.0f, 0.35f));
        this.forEachEventData(store, (pos, radius) -> {
            DebugRenderer.drawString(matrices, vertexConsumers, "Listener Origin", pos.getX(), pos.getY() + (double)1.8f, pos.getZ(), Colors.WHITE, 0.025f);
            DebugRenderer.drawString(matrices, vertexConsumers, BlockPos.ofFloored(pos).toString(), pos.getX(), pos.getY() + 1.5, pos.getZ(), -6959665, 0.025f);
        });
        store.forEachEvent(DebugSubscriptionTypes.GAME_EVENTS, (value, remainingTime, expiry) -> {
            Vec3d lv = value.pos();
            double d = 0.4;
            Box lv2 = Box.of(lv.add(0.0, 0.5, 0.0), 0.4, 0.9, 0.4);
            GameEventDebugRenderer.drawBoxIfCameraReady(matrices, vertexConsumers, lv2, 1.0f, 1.0f, 1.0f, 0.2f);
            DebugRenderer.drawString(matrices, vertexConsumers, value.event().getIdAsString(), lv.x, lv.y + (double)0.85f, lv.z, -7564911, 0.0075f);
        });
    }

    private static void drawBoxIfCameraReady(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Box box, float red, float green, float blue, float alpha) {
        Camera lv = MinecraftClient.getInstance().gameRenderer.getCamera();
        if (!lv.isReady()) {
            return;
        }
        Vec3d lv2 = lv.getPos().negate();
        DebugRenderer.drawBox(matrices, vertexConsumers, box.offset(lv2), red, green, blue, alpha);
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    static interface EventConsumer {
        public void accept(Vec3d var1, int var2);
    }
}

