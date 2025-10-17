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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;

@Environment(value=EnvType.CLIENT)
public class RaidCenterDebugRenderer
implements DebugRenderer.Renderer {
    private static final int RANGE = 160;
    private static final float DRAWN_STRING_SIZE = 0.04f;
    private final MinecraftClient client;

    public RaidCenterDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        BlockPos lv = this.getCamera().getBlockPos();
        store.forEachChunkData(DebugSubscriptionTypes.RAIDS, (chunkPos, raids) -> {
            for (BlockPos lv : raids) {
                if (!lv.isWithinDistance(lv, 160.0)) continue;
                RaidCenterDebugRenderer.drawRaidCenter(matrices, vertexConsumers, lv);
            }
        });
    }

    private static void drawRaidCenter(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos) {
        DebugRenderer.drawBlockBox(matrices, vertexConsumers, pos, 1.0f, 0.0f, 0.0f, 0.15f);
        RaidCenterDebugRenderer.drawString(matrices, vertexConsumers, "Raid center", pos, -65536);
    }

    private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, BlockPos pos, int color) {
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getY() + 1.3;
        double f = (double)pos.getZ() + 0.5;
        DebugRenderer.drawString(matrices, vertexConsumers, string, d, e, f, color, 0.04f, true, 0.0f, true);
    }

    private Camera getCamera() {
        return this.client.gameRenderer.getCamera();
    }
}

