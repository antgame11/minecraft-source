/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.command;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Environment(value=EnvType.CLIENT)
public class ItemCommandRenderer {
    private final MatrixStack matrices = new MatrixStack();

    public void render(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers, OutlineVertexConsumerProvider outlineVertexConsumers) {
        for (OrderedRenderCommandQueueImpl.ItemCommand lv : queue.getItemCommands()) {
            this.matrices.push();
            this.matrices.peek().copy(lv.positionMatrix());
            ItemRenderer.renderItem(lv.displayContext(), this.matrices, vertexConsumers, lv.lightCoords(), lv.overlayCoords(), lv.tintLayers(), lv.quads(), lv.renderLayer(), lv.glintType());
            if (lv.outlineColor() != 0) {
                outlineVertexConsumers.setColor(lv.outlineColor());
                ItemRenderer.renderItem(lv.displayContext(), this.matrices, outlineVertexConsumers, lv.lightCoords(), lv.overlayCoords(), lv.tintLayers(), lv.quads(), lv.renderLayer(), ItemRenderState.Glint.NONE);
            }
            this.matrices.pop();
        }
    }
}

