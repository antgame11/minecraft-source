/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.command;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.util.math.MatrixStack;

@Environment(value=EnvType.CLIENT)
public class ModelPartCommandRenderer {
    private final MatrixStack matrices = new MatrixStack();

    public void render(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers, OutlineVertexConsumerProvider arg3, VertexConsumerProvider.Immediate arg4) {
        Commands lv = queue.getModelPartCommands();
        for (Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelPartCommand>> entry : lv.modelPartCommands.entrySet()) {
            RenderLayer lv2 = entry.getKey();
            List<OrderedRenderCommandQueueImpl.ModelPartCommand> list = entry.getValue();
            VertexConsumer lv3 = vertexConsumers.getBuffer(lv2);
            for (OrderedRenderCommandQueueImpl.ModelPartCommand lv4 : list) {
                VertexConsumer lv6;
                VertexConsumer lv5 = lv4.sprite() != null ? (lv4.hasGlint() ? lv4.sprite().getTextureSpecificVertexConsumer(ItemRenderer.getItemGlintConsumer(vertexConsumers, lv2, lv4.sheeted(), true)) : lv4.sprite().getTextureSpecificVertexConsumer(lv3)) : (lv4.hasGlint() ? ItemRenderer.getItemGlintConsumer(vertexConsumers, lv2, lv4.sheeted(), true) : lv3);
                this.matrices.peek().copy(lv4.matricesEntry());
                lv4.modelPart().render(this.matrices, lv5, lv4.lightCoords(), lv4.overlayCoords(), lv4.tintedColor());
                if (lv4.outlineColor() != 0 && (lv2.getAffectedOutline().isPresent() || lv2.isOutline())) {
                    arg3.setColor(lv4.outlineColor());
                    lv6 = arg3.getBuffer(lv2);
                    lv4.modelPart().render(this.matrices, lv4.sprite() == null ? lv6 : lv4.sprite().getTextureSpecificVertexConsumer(lv6), lv4.lightCoords(), lv4.overlayCoords(), lv4.tintedColor());
                }
                if (lv4.crumblingOverlay() == null) continue;
                lv6 = new OverlayVertexConsumer(arg4.getBuffer(ModelBaker.BLOCK_DESTRUCTION_RENDER_LAYERS.get(lv4.crumblingOverlay().progress())), lv4.crumblingOverlay().cameraMatricesEntry(), 1.0f);
                lv4.modelPart().render(this.matrices, lv6, lv4.lightCoords(), lv4.overlayCoords(), lv4.tintedColor());
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Commands {
        final Map<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelPartCommand>> modelPartCommands = new HashMap<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelPartCommand>>();
        private final Set<RenderLayer> modelPartLayers = new ObjectOpenHashSet<RenderLayer>();

        public void add(RenderLayer renderLayer, OrderedRenderCommandQueueImpl.ModelPartCommand command) {
            this.modelPartCommands.computeIfAbsent(renderLayer, arg -> new ArrayList()).add(command);
        }

        public void clear() {
            for (Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelPartCommand>> entry : this.modelPartCommands.entrySet()) {
                if (entry.getValue().isEmpty()) continue;
                this.modelPartLayers.add(entry.getKey());
                entry.getValue().clear();
            }
        }

        public void nextFrame() {
            this.modelPartCommands.keySet().removeIf(renderLayer -> !this.modelPartLayers.contains(renderLayer));
            this.modelPartLayers.clear();
        }
    }
}

