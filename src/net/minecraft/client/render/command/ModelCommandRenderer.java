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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ModelCommandRenderer {
    private final MatrixStack matrices = new MatrixStack();

    public void render(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers, OutlineVertexConsumerProvider outlineVertexConsumers, VertexConsumerProvider.Immediate crumblingOverlayVertexConsumers) {
        Commands lv = queue.getModelCommands();
        this.renderAll(vertexConsumers, outlineVertexConsumers, lv.opaqueModelCommands, crumblingOverlayVertexConsumers);
        lv.blendedModelCommands.sort(Comparator.comparingDouble(modelCommand -> -modelCommand.position().lengthSquared()));
        this.renderAllBlended(vertexConsumers, outlineVertexConsumers, lv.blendedModelCommands, crumblingOverlayVertexConsumers);
    }

    private void renderAllBlended(VertexConsumerProvider.Immediate vertexConsumers, OutlineVertexConsumerProvider outlineVertexConsumers, List<OrderedRenderCommandQueueImpl.BlendedModelCommand<?>> blendedModelCommands, VertexConsumerProvider.Immediate crumblingOverlayVertexConsumers) {
        for (OrderedRenderCommandQueueImpl.BlendedModelCommand<?> lv : blendedModelCommands) {
            this.render(lv.model(), lv.renderType(), vertexConsumers.getBuffer(lv.renderType()), outlineVertexConsumers, crumblingOverlayVertexConsumers);
        }
    }

    private void renderAll(VertexConsumerProvider.Immediate vertexConsumers, OutlineVertexConsumerProvider outlineVertexConsumers, Map<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelCommand<?>>> modelCommands, VertexConsumerProvider.Immediate crumblingOverlayVertexConsumers) {
        Collection<Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelCommand<?>>>> iterable;
        if (SharedConstants.SHUFFLE_MODELS) {
            ArrayList list = new ArrayList(modelCommands.entrySet());
            Collections.shuffle(list);
            iterable = list;
        } else {
            iterable = modelCommands.entrySet();
        }
        for (Map.Entry entry : iterable) {
            VertexConsumer lv = vertexConsumers.getBuffer((RenderLayer)entry.getKey());
            for (OrderedRenderCommandQueueImpl.ModelCommand lv2 : (List)entry.getValue()) {
                this.render(lv2, (RenderLayer)entry.getKey(), lv, outlineVertexConsumers, crumblingOverlayVertexConsumers);
            }
        }
    }

    private <S> void render(OrderedRenderCommandQueueImpl.ModelCommand<S> model, RenderLayer renderLayer, VertexConsumer vertexConsumer, OutlineVertexConsumerProvider outlineVertexConsumers, VertexConsumerProvider.Immediate crumblingOverlayVertexConsumers) {
        VertexConsumer lv3;
        this.matrices.push();
        this.matrices.peek().copy(model.matricesEntry());
        Model<S> lv = model.model();
        VertexConsumer lv2 = model.sprite() == null ? vertexConsumer : model.sprite().getTextureSpecificVertexConsumer(vertexConsumer);
        lv.setAngles(model.state());
        lv.render(this.matrices, lv2, model.lightCoords(), model.overlayCoords(), model.tintedColor());
        if (model.outlineColor() != 0 && (renderLayer.getAffectedOutline().isPresent() || renderLayer.isOutline())) {
            outlineVertexConsumers.setColor(model.outlineColor());
            lv3 = outlineVertexConsumers.getBuffer(renderLayer);
            lv.render(this.matrices, model.sprite() == null ? lv3 : model.sprite().getTextureSpecificVertexConsumer(lv3), model.lightCoords(), model.overlayCoords(), model.tintedColor());
        }
        if (model.crumblingOverlay() != null && renderLayer.hasCrumbling()) {
            lv3 = new OverlayVertexConsumer(crumblingOverlayVertexConsumers.getBuffer(ModelBaker.BLOCK_DESTRUCTION_RENDER_LAYERS.get(model.crumblingOverlay().progress())), model.crumblingOverlay().cameraMatricesEntry(), 1.0f);
            lv.render(this.matrices, model.sprite() == null ? lv3 : model.sprite().getTextureSpecificVertexConsumer(lv3), model.lightCoords(), model.overlayCoords(), model.tintedColor());
        }
        this.matrices.pop();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Commands {
        final Map<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelCommand<?>>> opaqueModelCommands = new HashMap();
        final List<OrderedRenderCommandQueueImpl.BlendedModelCommand<?>> blendedModelCommands = new ArrayList();
        private final Set<RenderLayer> usedModelRenderLayers = new ObjectOpenHashSet<RenderLayer>();

        public void add(RenderLayer renderLayer, OrderedRenderCommandQueueImpl.ModelCommand<?> modelCommand) {
            if (renderLayer.getRenderPipeline().getBlendFunction().isEmpty()) {
                this.opaqueModelCommands.computeIfAbsent(renderLayer, arg -> new ArrayList()).add(modelCommand);
            } else {
                Vector3f vector3f = modelCommand.matricesEntry().getPositionMatrix().transformPosition(new Vector3f());
                this.blendedModelCommands.add(new OrderedRenderCommandQueueImpl.BlendedModelCommand(modelCommand, renderLayer, vector3f));
            }
        }

        public void clear() {
            this.blendedModelCommands.clear();
            for (Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelCommand<?>>> entry : this.opaqueModelCommands.entrySet()) {
                List<OrderedRenderCommandQueueImpl.ModelCommand<?>> list = entry.getValue();
                if (list.isEmpty()) continue;
                this.usedModelRenderLayers.add(entry.getKey());
                list.clear();
            }
        }

        public void nextFrame() {
            this.opaqueModelCommands.keySet().removeIf(renderLayer -> !this.usedModelRenderLayers.contains(renderLayer));
            this.usedModelRenderLayers.clear();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record CrumblingOverlayCommand(int progress, MatrixStack.Entry cameraMatricesEntry) {
    }
}

