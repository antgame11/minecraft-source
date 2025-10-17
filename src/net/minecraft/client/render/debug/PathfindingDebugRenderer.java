/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.data.EntityPathDebugData;

@Environment(value=EnvType.CLIENT)
public class PathfindingDebugRenderer
implements DebugRenderer.Renderer {
    private static final float RANGE = 80.0f;
    private static final int field_62974 = 8;
    private static final boolean field_62975 = false;
    private static final boolean field_32908 = true;
    private static final boolean field_32909 = false;
    private static final boolean field_32910 = false;
    private static final boolean field_32911 = true;
    private static final boolean field_32912 = true;
    private static final float DRAWN_STRING_SIZE = 0.02f;

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        store.forEachEntityData(DebugSubscriptionTypes.ENTITY_PATHS, (entity, data) -> PathfindingDebugRenderer.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ, data.path(), data.maxNodeDistance()));
    }

    private static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, Path path, float maxNodeDistance) {
        PathfindingDebugRenderer.drawPath(matrices, vertexConsumers, path, maxNodeDistance, true, true, cameraX, cameraY, cameraZ);
    }

    public static void drawPath(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Path path, float nodeSize, boolean drawDebugNodes, boolean drawLabels, double cameraX, double cameraY, double cameraZ) {
        PathfindingDebugRenderer.drawPathLines(matrices, vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(6.0)), path, cameraX, cameraY, cameraZ);
        BlockPos lv = path.getTarget();
        if (PathfindingDebugRenderer.getManhattanDistance(lv, cameraX, cameraY, cameraZ) <= 80.0f) {
            DebugRenderer.drawBox(matrices, vertexConsumers, new Box((float)lv.getX() + 0.25f, (float)lv.getY() + 0.25f, (double)lv.getZ() + 0.25, (float)lv.getX() + 0.75f, (float)lv.getY() + 0.75f, (float)lv.getZ() + 0.75f).offset(-cameraX, -cameraY, -cameraZ), 0.0f, 1.0f, 0.0f, 0.5f);
            for (int i = 0; i < path.getLength(); ++i) {
                PathNode lv2 = path.getNode(i);
                if (!(PathfindingDebugRenderer.getManhattanDistance(lv2.getBlockPos(), cameraX, cameraY, cameraZ) <= 80.0f)) continue;
                float h = i == path.getCurrentNodeIndex() ? 1.0f : 0.0f;
                float j = i == path.getCurrentNodeIndex() ? 0.0f : 1.0f;
                DebugRenderer.drawBox(matrices, vertexConsumers, new Box((float)lv2.x + 0.5f - nodeSize, (float)lv2.y + 0.01f * (float)i, (float)lv2.z + 0.5f - nodeSize, (float)lv2.x + 0.5f + nodeSize, (float)lv2.y + 0.25f + 0.01f * (float)i, (float)lv2.z + 0.5f + nodeSize).offset(-cameraX, -cameraY, -cameraZ), h, 0.0f, j, 0.5f);
            }
        }
        Path.DebugNodeInfo lv3 = path.getDebugNodeInfos();
        if (drawDebugNodes && lv3 != null) {
            for (PathNode lv4 : lv3.closedSet()) {
                if (!(PathfindingDebugRenderer.getManhattanDistance(lv4.getBlockPos(), cameraX, cameraY, cameraZ) <= 80.0f)) continue;
                DebugRenderer.drawBox(matrices, vertexConsumers, new Box((float)lv4.x + 0.5f - nodeSize / 2.0f, (float)lv4.y + 0.01f, (float)lv4.z + 0.5f - nodeSize / 2.0f, (float)lv4.x + 0.5f + nodeSize / 2.0f, (double)lv4.y + 0.1, (float)lv4.z + 0.5f + nodeSize / 2.0f).offset(-cameraX, -cameraY, -cameraZ), 1.0f, 0.8f, 0.8f, 0.5f);
            }
            for (PathNode lv4 : lv3.openSet()) {
                if (!(PathfindingDebugRenderer.getManhattanDistance(lv4.getBlockPos(), cameraX, cameraY, cameraZ) <= 80.0f)) continue;
                DebugRenderer.drawBox(matrices, vertexConsumers, new Box((float)lv4.x + 0.5f - nodeSize / 2.0f, (float)lv4.y + 0.01f, (float)lv4.z + 0.5f - nodeSize / 2.0f, (float)lv4.x + 0.5f + nodeSize / 2.0f, (double)lv4.y + 0.1, (float)lv4.z + 0.5f + nodeSize / 2.0f).offset(-cameraX, -cameraY, -cameraZ), 0.8f, 1.0f, 1.0f, 0.5f);
            }
        }
        if (drawLabels) {
            for (int k = 0; k < path.getLength(); ++k) {
                PathNode lv5 = path.getNode(k);
                if (!(PathfindingDebugRenderer.getManhattanDistance(lv5.getBlockPos(), cameraX, cameraY, cameraZ) <= 80.0f)) continue;
                DebugRenderer.drawString(matrices, vertexConsumers, String.valueOf((Object)lv5.type), (double)lv5.x + 0.5, (double)lv5.y + 0.75, (double)lv5.z + 0.5, Colors.WHITE, 0.02f, true, 0.0f, true);
                DebugRenderer.drawString(matrices, vertexConsumers, String.format(Locale.ROOT, "%.2f", Float.valueOf(lv5.penalty)), (double)lv5.x + 0.5, (double)lv5.y + 0.25, (double)lv5.z + 0.5, Colors.WHITE, 0.02f, true, 0.0f, true);
            }
        }
    }

    public static void drawPathLines(MatrixStack matrices, VertexConsumer vertexConsumers, Path path, double cameraX, double cameraY, double cameraZ) {
        for (int i = 0; i < path.getLength(); ++i) {
            PathNode lv = path.getNode(i);
            if (PathfindingDebugRenderer.getManhattanDistance(lv.getBlockPos(), cameraX, cameraY, cameraZ) > 80.0f) continue;
            float g = (float)i / (float)path.getLength() * 0.33f;
            int j = i == 0 ? Colors.BLACK : ColorHelper.fullAlpha(MathHelper.hsvToRgb(g, 0.9f, 0.9f));
            vertexConsumers.vertex(matrices.peek(), (float)((double)lv.x - cameraX + 0.5), (float)((double)lv.y - cameraY + 0.5), (float)((double)lv.z - cameraZ + 0.5)).color(j);
        }
    }

    private static float getManhattanDistance(BlockPos pos, double x, double y, double z) {
        return (float)(Math.abs((double)pos.getX() - x) + Math.abs((double)pos.getY() - y) + Math.abs((double)pos.getZ() - z));
    }

    private static /* synthetic */ void method_74796(DebugDataStore arg, MatrixStack arg2, VertexConsumerProvider arg3, double d, double e, double f, Entity arg4) {
        EntityPathDebugData lv = arg.getEntityData(DebugSubscriptionTypes.ENTITY_PATHS, arg4);
        if (lv != null) {
            PathfindingDebugRenderer.render(arg2, arg3, d, e, f, lv.path(), lv.maxNodeDistance());
        }
    }
}

