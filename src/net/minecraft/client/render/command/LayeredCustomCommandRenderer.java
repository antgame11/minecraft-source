/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.command;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.texture.TextureManager;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LayeredCustomCommandRenderer
implements AutoCloseable {
    private final Queue<VerticesCache> availableBuffers = new ArrayDeque<VerticesCache>();
    private final List<VerticesCache> usedBuffers = new ArrayList<VerticesCache>();

    public void render(BatchingRenderCommandQueue queue) {
        if (queue.getLayeredCustomCommands().isEmpty()) {
            return;
        }
        GpuDevice gpuDevice = RenderSystem.getDevice();
        MinecraftClient lv = MinecraftClient.getInstance();
        TextureManager lv2 = lv.getTextureManager();
        Framebuffer lv3 = lv.getFramebuffer();
        Framebuffer lv4 = lv.worldRenderer.getParticlesFramebuffer();
        for (OrderedRenderCommandQueue.LayeredCustom lv5 : queue.getLayeredCustomCommands()) {
            VerticesCache lv6 = this.availableBuffers.poll();
            if (lv6 == null) {
                lv6 = new VerticesCache();
            }
            this.usedBuffers.add(lv6);
            BillboardParticleSubmittable.Buffers lv7 = lv5.submit(lv6);
            if (lv7 == null) continue;
            try (RenderPass renderPass = gpuDevice.createCommandEncoder().createRenderPass(() -> "Particles - Main", lv3.getColorAttachmentView(), OptionalInt.empty(), lv3.getDepthAttachmentView(), OptionalDouble.empty());){
                this.submitRenderPass(renderPass);
                lv5.render(lv7, lv6, renderPass, lv2, false);
                if (lv4 == null) {
                    lv5.render(lv7, lv6, renderPass, lv2, true);
                }
            }
            if (lv4 == null) continue;
            renderPass = gpuDevice.createCommandEncoder().createRenderPass(() -> "Particles - Transparent", lv4.getColorAttachmentView(), OptionalInt.empty(), lv4.getDepthAttachmentView(), OptionalDouble.empty());
            try {
                this.submitRenderPass(renderPass);
                lv5.render(lv7, lv6, renderPass, lv2, true);
            } finally {
                if (renderPass == null) continue;
                renderPass.close();
            }
        }
    }

    public void end() {
        for (VerticesCache lv : this.usedBuffers) {
            lv.rotate();
        }
        this.availableBuffers.addAll(this.usedBuffers);
        this.usedBuffers.clear();
    }

    private void submitRenderPass(RenderPass renderPass) {
        renderPass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
        renderPass.setUniform("Fog", RenderSystem.getShaderFog());
        renderPass.bindSampler("Sampler2", MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().getGlTextureView());
    }

    @Override
    public void close() {
        this.availableBuffers.forEach(VerticesCache::close);
    }

    @Environment(value=EnvType.CLIENT)
    public static class VerticesCache
    implements AutoCloseable {
        @Nullable
        private MappableRingBuffer ringBuffer;

        public void write(ByteBuffer byteBuffer) {
            if (this.ringBuffer == null || this.ringBuffer.size() < byteBuffer.remaining()) {
                if (this.ringBuffer != null) {
                    this.ringBuffer.close();
                }
                this.ringBuffer = new MappableRingBuffer(() -> "Particle Vertices", 34, byteBuffer.remaining());
            }
            try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.ringBuffer.getBlocking().slice(), false, true);){
                mappedView.data().put(byteBuffer);
            }
        }

        public GpuBuffer get() {
            if (this.ringBuffer == null) {
                throw new IllegalStateException("Can't get buffer before it's made");
            }
            return this.ringBuffer.getBlocking();
        }

        void rotate() {
            if (this.ringBuffer != null) {
                this.ringBuffer.rotate();
            }
        }

        @Override
        public void close() {
            if (this.ringBuffer != null) {
                this.ringBuffer.close();
            }
        }
    }
}

