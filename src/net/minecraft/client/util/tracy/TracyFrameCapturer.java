/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util.tracy;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.jtracy.TracyClient;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;

@Environment(value=EnvType.CLIENT)
public class TracyFrameCapturer
implements AutoCloseable {
    private static final int MAX_WIDTH = 320;
    private static final int MAX_HEIGHT = 180;
    private static final int field_54254 = 4;
    private int framebufferWidth;
    private int framebufferHeight;
    private int width = 320;
    private int height = 180;
    private GpuTexture texture;
    private GpuTextureView textureView;
    private GpuBuffer buffer;
    private int offset;
    private boolean captured;
    private Status status = Status.WAITING_FOR_CAPTURE;

    public TracyFrameCapturer() {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.texture = gpuDevice.createTexture("Tracy Frame Capture", 10, TextureFormat.RGBA8, this.width, this.height, 1, 1);
        this.textureView = gpuDevice.createTextureView(this.texture);
        this.buffer = gpuDevice.createBuffer(() -> "Tracy Frame Capture buffer", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, this.width * this.height * 4);
    }

    private void resize(int framebufferWidth, int framebufferHeight) {
        float f = (float)framebufferWidth / (float)framebufferHeight;
        if (framebufferWidth > 320) {
            framebufferWidth = 320;
            framebufferHeight = (int)(320.0f / f);
        }
        if (framebufferHeight > 180) {
            framebufferWidth = (int)(180.0f * f);
            framebufferHeight = 180;
        }
        framebufferWidth = framebufferWidth / 4 * 4;
        framebufferHeight = framebufferHeight / 4 * 4;
        if (this.width != framebufferWidth || this.height != framebufferHeight) {
            this.width = framebufferWidth;
            this.height = framebufferHeight;
            GpuDevice gpuDevice = RenderSystem.getDevice();
            this.texture.close();
            this.texture = gpuDevice.createTexture("Tracy Frame Capture", 10, TextureFormat.RGBA8, framebufferWidth, framebufferHeight, 1, 1);
            this.textureView.close();
            this.textureView = gpuDevice.createTextureView(this.texture);
            this.buffer.close();
            this.buffer = gpuDevice.createBuffer(() -> "Tracy Frame Capture buffer", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, framebufferWidth * framebufferHeight * 4);
        }
    }

    public void capture(Framebuffer framebuffer) {
        if (this.status != Status.WAITING_FOR_CAPTURE || this.captured || framebuffer.getColorAttachment() == null) {
            return;
        }
        this.captured = true;
        if (framebuffer.textureWidth != this.framebufferWidth || framebuffer.textureHeight != this.framebufferHeight) {
            this.framebufferWidth = framebuffer.textureWidth;
            this.framebufferHeight = framebuffer.textureHeight;
            this.resize(this.framebufferWidth, this.framebufferHeight);
        }
        this.status = Status.WAITING_FOR_COPY;
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Tracy blit", this.textureView, OptionalInt.empty());){
            renderPass.setPipeline(RenderPipelines.TRACY_BLIT);
            renderPass.bindSampler("InSampler", framebuffer.getColorAttachmentView());
            renderPass.draw(0, 3);
        }
        commandEncoder.copyTextureToBuffer(this.texture, this.buffer, 0, () -> {
            this.status = Status.WAITING_FOR_UPLOAD;
        }, 0);
        this.offset = 0;
    }

    public void upload() {
        if (this.status != Status.WAITING_FOR_UPLOAD) {
            return;
        }
        this.status = Status.WAITING_FOR_CAPTURE;
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.buffer, true, false);){
            TracyClient.frameImage(mappedView.data(), this.width, this.height, this.offset, true);
        }
    }

    public void markFrame() {
        ++this.offset;
        this.captured = false;
        TracyClient.markFrame();
    }

    @Override
    public void close() {
        this.texture.close();
        this.textureView.close();
        this.buffer.close();
    }

    @Environment(value=EnvType.CLIENT)
    static enum Status {
        WAITING_FOR_CAPTURE,
        WAITING_FOR_COPY,
        WAITING_FOR_UPLOAD;

    }
}

