/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.debug.DebugDataStore;

@Environment(value=EnvType.CLIENT)
public class CollisionDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;
    private double lastUpdateTime = Double.MIN_VALUE;
    private List<VoxelShape> collisions = Collections.emptyList();

    public CollisionDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        double g = Util.getMeasuringTimeNano();
        if (g - this.lastUpdateTime > 1.0E8) {
            this.lastUpdateTime = g;
            Entity lv = this.client.gameRenderer.getCamera().getFocusedEntity();
            this.collisions = ImmutableList.copyOf(lv.getEntityWorld().getCollisions(lv, lv.getBoundingBox().expand(6.0)));
        }
        VertexConsumer lv2 = vertexConsumers.getBuffer(RenderLayer.getLines());
        for (VoxelShape lv3 : this.collisions) {
            DebugRenderer.drawVoxelShapeOutlines(matrices, lv2, lv3, -cameraX, -cameraY, -cameraZ, 1.0f, 1.0f, 1.0f, 1.0f, true);
        }
    }
}

