/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.command;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class FallingBlockCommandRenderer {
    private final MatrixStack matrices = new MatrixStack();

    public void render(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers, BlockRenderManager blockRenderManager, OutlineVertexConsumerProvider outlineVertexConsumers) {
        for (OrderedRenderCommandQueueImpl.MovingBlockCommand lv : queue.getMovingBlockCommands()) {
            MovingBlockRenderState lv2 = lv.movingBlockRenderState();
            BlockState lv3 = lv2.blockState;
            List<BlockModelPart> list = blockRenderManager.getModel(lv3).getParts(Random.create(lv3.getRenderingSeed(lv2.fallingBlockPos)));
            MatrixStack lv4 = new MatrixStack();
            lv4.multiplyPositionMatrix(lv.matricesEntry());
            blockRenderManager.getModelRenderer().render(lv2, list, lv3, lv2.entityBlockPos, lv4, vertexConsumers.getBuffer(RenderLayers.getMovingBlockLayer(lv3)), false, OverlayTexture.DEFAULT_UV);
        }
        for (OrderedRenderCommandQueueImpl.BlockCommand lv5 : queue.getBlockCommands()) {
            this.matrices.push();
            this.matrices.peek().copy(lv5.matricesEntry());
            blockRenderManager.renderBlockAsEntity(lv5.state(), this.matrices, vertexConsumers, lv5.lightCoords(), lv5.overlayCoords());
            if (lv5.outlineColor() != 0) {
                outlineVertexConsumers.setColor(lv5.outlineColor());
                blockRenderManager.renderBlockAsEntity(lv5.state(), this.matrices, outlineVertexConsumers, lv5.lightCoords(), lv5.overlayCoords());
            }
            this.matrices.pop();
        }
        for (OrderedRenderCommandQueueImpl.BlockStateModelCommand lv6 : queue.getBlockStateModelCommands()) {
            BlockModelRenderer.render(lv6.matricesEntry(), vertexConsumers.getBuffer(lv6.renderLayer()), lv6.model(), lv6.r(), lv6.g(), lv6.b(), lv6.lightCoords(), lv6.overlayCoords());
            if (lv6.outlineColor() == 0) continue;
            outlineVertexConsumers.setColor(lv6.outlineColor());
            BlockModelRenderer.render(lv6.matricesEntry(), outlineVertexConsumers.getBuffer(lv6.renderLayer()), lv6.model(), lv6.r(), lv6.g(), lv6.b(), lv6.lightCoords(), lv6.overlayCoords());
        }
    }
}

