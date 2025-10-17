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
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.data.GoalSelectorDebugData;

@Environment(value=EnvType.CLIENT)
public class GoalSelectorDebugRenderer
implements DebugRenderer.Renderer {
    private static final int RANGE = 160;
    private final MinecraftClient client;

    public GoalSelectorDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        Camera lv = this.client.gameRenderer.getCamera();
        BlockPos lv2 = BlockPos.ofFloored(lv.getPos().x, 0.0, lv.getPos().z);
        store.forEachEntityData(DebugSubscriptionTypes.GOAL_SELECTORS, (arg4, arg5) -> {
            if (lv2.isWithinDistance(arg4.getBlockPos(), 160.0)) {
                for (int i = 0; i < arg5.goals().size(); ++i) {
                    GoalSelectorDebugData.Goal lv = arg5.goals().get(i);
                    double d = (double)arg4.getBlockX() + 0.5;
                    double e = arg4.getY() + 2.0 + (double)i * 0.25;
                    double f = (double)arg4.getBlockZ() + 0.5;
                    int j = lv.isRunning() ? Colors.GREEN : -3355444;
                    DebugRenderer.drawString(matrices, vertexConsumers, lv.name(), d, e, f, j);
                }
            }
        });
    }
}

