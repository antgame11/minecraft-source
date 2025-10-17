/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class LabelCommandRenderer {
    public void render(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers, TextRenderer renderer) {
        Commands lv = queue.getLabelCommands();
        lv.seethroughLabels.sort(Comparator.comparing(OrderedRenderCommandQueueImpl.LabelCommand::distanceToCameraSq).reversed());
        for (OrderedRenderCommandQueueImpl.LabelCommand lv2 : lv.seethroughLabels) {
            renderer.draw(lv2.text(), lv2.x(), lv2.y(), lv2.color(), false, lv2.matricesEntry(), (VertexConsumerProvider)vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, lv2.backgroundColor(), lv2.lightCoords());
        }
        for (OrderedRenderCommandQueueImpl.LabelCommand lv2 : lv.normalLabels) {
            renderer.draw(lv2.text(), lv2.x(), lv2.y(), lv2.color(), false, lv2.matricesEntry(), (VertexConsumerProvider)vertexConsumers, TextRenderer.TextLayerType.NORMAL, lv2.backgroundColor(), lv2.lightCoords());
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Commands {
        final List<OrderedRenderCommandQueueImpl.LabelCommand> seethroughLabels = new ArrayList<OrderedRenderCommandQueueImpl.LabelCommand>();
        final List<OrderedRenderCommandQueueImpl.LabelCommand> normalLabels = new ArrayList<OrderedRenderCommandQueueImpl.LabelCommand>();

        public void add(MatrixStack matrices, @Nullable Vec3d pos, int y, Text label, boolean notSneaking, int light, double squaredDistanceToCamera, CameraRenderState cameraState) {
            if (pos == null) {
                return;
            }
            MinecraftClient lv = MinecraftClient.getInstance();
            matrices.push();
            matrices.translate(pos.x, pos.y + 0.5, pos.z);
            matrices.multiply(cameraState.orientation);
            matrices.scale(0.025f, -0.025f, 0.025f);
            Matrix4f matrix4f = new Matrix4f(matrices.peek().getPositionMatrix());
            float f = (float)(-lv.textRenderer.getWidth(label)) / 2.0f;
            int k = (int)(lv.options.getTextBackgroundOpacity(0.25f) * 255.0f) << 24;
            if (notSneaking) {
                this.normalLabels.add(new OrderedRenderCommandQueueImpl.LabelCommand(matrix4f, f, y, label, LightmapTextureManager.applyEmission(light, 2), -1, 0, squaredDistanceToCamera));
                this.seethroughLabels.add(new OrderedRenderCommandQueueImpl.LabelCommand(matrix4f, f, y, label, light, -2130706433, k, squaredDistanceToCamera));
            } else {
                this.normalLabels.add(new OrderedRenderCommandQueueImpl.LabelCommand(matrix4f, f, y, label, light, -2130706433, k, squaredDistanceToCamera));
            }
            matrices.pop();
        }

        public void clear() {
            this.normalLabels.clear();
            this.seethroughLabels.clear();
        }
    }
}

