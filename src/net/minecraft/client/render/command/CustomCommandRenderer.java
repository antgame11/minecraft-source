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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.util.math.MatrixStack;

@Environment(value=EnvType.CLIENT)
public class CustomCommandRenderer {
    public void render(BatchingRenderCommandQueue queue, VertexConsumerProvider.Immediate vertexConsumers) {
        Commands lv = queue.getCustomCommands();
        for (Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.CustomCommand>> entry : lv.customCommands.entrySet()) {
            VertexConsumer lv2 = vertexConsumers.getBuffer(entry.getKey());
            for (OrderedRenderCommandQueueImpl.CustomCommand lv3 : entry.getValue()) {
                lv3.customRenderer().render(lv3.matricesEntry(), lv2);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Commands {
        final Map<RenderLayer, List<OrderedRenderCommandQueueImpl.CustomCommand>> customCommands = new HashMap<RenderLayer, List<OrderedRenderCommandQueueImpl.CustomCommand>>();
        private final Set<RenderLayer> customRenderLayers = new ObjectOpenHashSet<RenderLayer>();

        public void add(MatrixStack matrices, RenderLayer renderLayer, OrderedRenderCommandQueue.Custom custom) {
            List list = this.customCommands.computeIfAbsent(renderLayer, arg -> new ArrayList());
            list.add(new OrderedRenderCommandQueueImpl.CustomCommand(matrices.peek().copy(), custom));
        }

        public void clear() {
            for (Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.CustomCommand>> entry : this.customCommands.entrySet()) {
                if (entry.getValue().isEmpty()) continue;
                this.customRenderLayers.add(entry.getKey());
                entry.getValue().clear();
            }
        }

        public void nextFrame() {
            this.customCommands.keySet().removeIf(renderLayer -> !this.customRenderLayers.contains(renderLayer));
            this.customRenderLayers.clear();
        }
    }
}

