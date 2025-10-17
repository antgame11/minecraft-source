/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;

@Environment(value=EnvType.CLIENT)
public class NeighborUpdateDebugRenderer
implements DebugRenderer.Renderer {
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        Update lv3;
        BlockPos lv2;
        int i = DebugSubscriptionTypes.NEIGHBOR_UPDATES.getExpiry();
        double g = 1.0 / (double)(i * 2);
        HashMap map = new HashMap();
        store.forEachEvent(DebugSubscriptionTypes.NEIGHBOR_UPDATES, (value, remainingTime, expiry) -> {
            long l = expiry - remainingTime;
            Update lv = map.getOrDefault(value, Update.EMPTY);
            map.put(value, lv.withAge((int)l));
        });
        VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getLines());
        for (Map.Entry entry : map.entrySet()) {
            lv2 = (BlockPos)entry.getKey();
            lv3 = (Update)entry.getValue();
            Box lv4 = new Box(BlockPos.ORIGIN).expand(0.002).contract(g * (double)lv3.age).offset(lv2.getX(), lv2.getY(), lv2.getZ()).offset(-cameraX, -cameraY, -cameraZ);
            VertexRendering.drawBox(matrices.peek(), lv, lv4.minX, lv4.minY, lv4.minZ, lv4.maxX, lv4.maxY, lv4.maxZ, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        for (Map.Entry entry : map.entrySet()) {
            lv2 = (BlockPos)entry.getKey();
            lv3 = (Update)entry.getValue();
            DebugRenderer.drawString(matrices, vertexConsumers, String.valueOf(lv3.count), lv2.getX(), lv2.getY(), lv2.getZ(), Colors.WHITE);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Update(int count, int age) {
        static final Update EMPTY = new Update(0, Integer.MAX_VALUE);

        public Update withAge(int age) {
            if (age == this.age) {
                return new Update(this.count + 1, age);
            }
            if (age < this.age) {
                return new Update(1, age);
            }
            return this;
        }
    }
}

