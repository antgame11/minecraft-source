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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;

@Environment(value=EnvType.CLIENT)
public class TextCommandRenderer {
    public void render(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers) {
        TextRenderer lv = MinecraftClient.getInstance().textRenderer;
        for (OrderedRenderCommandQueueImpl.TextCommand lv2 : queue.getTextCommands()) {
            if (lv2.outlineColor() == 0) {
                lv.draw(lv2.text(), lv2.x(), lv2.y(), lv2.color(), lv2.dropShadow(), lv2.matricesEntry(), (VertexConsumerProvider)vertexConsumers, lv2.layerType(), lv2.backgroundColor(), lv2.lightCoords());
                continue;
            }
            lv.drawWithOutline(lv2.text(), lv2.x(), lv2.y(), lv2.color(), lv2.outlineColor(), lv2.matricesEntry(), vertexConsumers, lv2.lightCoords());
        }
    }
}

