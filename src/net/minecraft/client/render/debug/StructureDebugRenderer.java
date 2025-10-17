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
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.data.StructureDebugData;

@Environment(value=EnvType.CLIENT)
public class StructureDebugRenderer
implements DebugRenderer.Renderer {
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getLines());
        store.forEachChunkData(DebugSubscriptionTypes.STRUCTURES, (chunkPos, structures) -> {
            for (StructureDebugData lv : structures) {
                StructureDebugRenderer.renderPiece(matrices, cameraX, cameraY, cameraZ, lv, lv.boundingBox(), 1.0f, 1.0f, 1.0f, 1.0f);
                for (StructureDebugData.Piece lv2 : lv.pieces()) {
                    if (lv2.isStart()) {
                        StructureDebugRenderer.renderPiece(matrices, cameraX, cameraY, cameraZ, lv, lv2.boundingBox(), 0.0f, 1.0f, 0.0f, 1.0f);
                        continue;
                    }
                    StructureDebugRenderer.renderPiece(matrices, cameraX, cameraY, cameraZ, lv, lv2.boundingBox(), 0.0f, 0.0f, 1.0f, 1.0f);
                }
            }
        });
    }

    private static void renderPiece(MatrixStack matrices, double cameraX, double cameraY, double cameraZ, VertexConsumer vertexConsumer, BlockBox box, float red, float green, float blue, float alpha) {
        VertexRendering.drawBox(matrices.peek(), vertexConsumer, (double)box.getMinX() - cameraX, (double)box.getMinY() - cameraY, (double)box.getMinZ() - cameraZ, (double)(box.getMaxX() + 1) - cameraX, (double)(box.getMaxY() + 1) - cameraY, (double)(box.getMaxZ() + 1) - cameraZ, red, green, blue, alpha, red, green, blue);
    }
}

