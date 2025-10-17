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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;

@Environment(value=EnvType.CLIENT)
public class VillageSectionsDebugRenderer
implements DebugRenderer.Renderer {
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        store.forEachBlockData(DebugSubscriptionTypes.VILLAGE_SECTIONS, (pos, data) -> {
            ChunkSectionPos lv = ChunkSectionPos.from(pos);
            DebugRenderer.drawBlockBox(matrices, vertexConsumers, lv.getCenterPos(), 0.2f, 1.0f, 0.2f, 0.15f);
        });
    }
}

