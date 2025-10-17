/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.SequencedCollection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BlockRenderLayerGroup;

@Environment(value=EnvType.CLIENT)
public record SectionRenderState(EnumMap<BlockRenderLayer, List<RenderPass.RenderObject<GpuBufferSlice[]>>> drawsPerLayer, int maxIndicesRequired, GpuBufferSlice[] dynamicTransforms) {
    public void renderSection(BlockRenderLayerGroup group) {
        RenderSystem.ShapeIndexBuffer lv = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        GpuBuffer gpuBuffer = this.maxIndicesRequired == 0 ? null : lv.getIndexBuffer(this.maxIndicesRequired);
        VertexFormat.IndexType lv2 = this.maxIndicesRequired == 0 ? null : lv.getIndexType();
        BlockRenderLayer[] lvs = group.getLayers();
        MinecraftClient lv3 = MinecraftClient.getInstance();
        boolean bl = SharedConstants.HOTKEYS && lv3.wireFrame;
        Framebuffer lv4 = group.getFramebuffer();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Section layers for " + group.getName(), lv4.getColorAttachmentView(), OptionalInt.empty(), lv4.getDepthAttachmentView(), OptionalDouble.empty());){
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.bindSampler("Sampler2", lv3.gameRenderer.getLightmapTextureManager().getGlTextureView());
            for (BlockRenderLayer lv5 : lvs) {
                SequencedCollection<RenderPass.RenderObject<Object>> list = this.drawsPerLayer.get((Object)lv5);
                if (list.isEmpty()) continue;
                if (lv5 == BlockRenderLayer.TRANSLUCENT) {
                    list = list.reversed();
                }
                renderPass.setPipeline(bl ? RenderPipelines.WIREFRAME : lv5.getPipeline());
                renderPass.bindSampler("Sampler0", lv5.getTextureView());
                renderPass.drawMultipleIndexed(list, gpuBuffer, lv2, List.of("DynamicTransforms"), this.dynamicTransforms);
            }
        }
    }
}

