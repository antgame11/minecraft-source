/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.ProjectionMatrix3;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.CubemapTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public class CubeMapRenderer
implements AutoCloseable {
    private static final int FACES_COUNT = 6;
    private final GpuBuffer buffer;
    private final ProjectionMatrix3 projectionMatrix;
    private final Identifier id;

    public CubeMapRenderer(Identifier id) {
        this.id = id;
        this.projectionMatrix = new ProjectionMatrix3("cubemap", 0.05f, 10.0f);
        this.buffer = CubeMapRenderer.upload();
    }

    public void draw(MinecraftClient client, float x, float y) {
        RenderSystem.setProjectionMatrix(this.projectionMatrix.set(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight(), 85.0f), ProjectionType.PERSPECTIVE);
        RenderPipeline renderPipeline = RenderPipelines.POSITION_TEX_PANORAMA;
        Framebuffer lv = MinecraftClient.getInstance().getFramebuffer();
        GpuTextureView gpuTextureView = lv.getColorAttachmentView();
        GpuTextureView gpuTextureView2 = lv.getDepthAttachmentView();
        RenderSystem.ShapeIndexBuffer lv2 = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        GpuBuffer gpuBuffer = lv2.getIndexBuffer(36);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.rotationX((float)Math.PI);
        matrix4fStack.rotateX(x * ((float)Math.PI / 180));
        matrix4fStack.rotateY(y * ((float)Math.PI / 180));
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(new Matrix4f(matrix4fStack), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector3f(), new Matrix4f(), 0.0f);
        matrix4fStack.popMatrix();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Cubemap", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setVertexBuffer(0, this.buffer);
            renderPass.setIndexBuffer(gpuBuffer, lv2.getIndexType());
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.bindSampler("Sampler0", client.getTextureManager().getTexture(this.id).getGlTextureView());
            renderPass.drawIndexed(0, 0, 36, 1);
        }
    }

    private static GpuBuffer upload() {
        try (BufferAllocator lv = BufferAllocator.fixedSized(VertexFormats.POSITION.getVertexSize() * 4 * 6);){
            BufferBuilder lv2 = new BufferBuilder(lv, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            lv2.vertex(-1.0f, -1.0f, 1.0f);
            lv2.vertex(-1.0f, 1.0f, 1.0f);
            lv2.vertex(1.0f, 1.0f, 1.0f);
            lv2.vertex(1.0f, -1.0f, 1.0f);
            lv2.vertex(1.0f, -1.0f, 1.0f);
            lv2.vertex(1.0f, 1.0f, 1.0f);
            lv2.vertex(1.0f, 1.0f, -1.0f);
            lv2.vertex(1.0f, -1.0f, -1.0f);
            lv2.vertex(1.0f, -1.0f, -1.0f);
            lv2.vertex(1.0f, 1.0f, -1.0f);
            lv2.vertex(-1.0f, 1.0f, -1.0f);
            lv2.vertex(-1.0f, -1.0f, -1.0f);
            lv2.vertex(-1.0f, -1.0f, -1.0f);
            lv2.vertex(-1.0f, 1.0f, -1.0f);
            lv2.vertex(-1.0f, 1.0f, 1.0f);
            lv2.vertex(-1.0f, -1.0f, 1.0f);
            lv2.vertex(-1.0f, -1.0f, -1.0f);
            lv2.vertex(-1.0f, -1.0f, 1.0f);
            lv2.vertex(1.0f, -1.0f, 1.0f);
            lv2.vertex(1.0f, -1.0f, -1.0f);
            lv2.vertex(-1.0f, 1.0f, 1.0f);
            lv2.vertex(-1.0f, 1.0f, -1.0f);
            lv2.vertex(1.0f, 1.0f, -1.0f);
            lv2.vertex(1.0f, 1.0f, 1.0f);
            BuiltBuffer lv3 = lv2.end();
            try {
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Cube map vertex buffer", GpuBuffer.USAGE_VERTEX, lv3.getBuffer());
                if (lv3 != null) {
                    lv3.close();
                }
                return gpuBuffer;
            } catch (Throwable throwable) {
                if (lv3 != null) {
                    try {
                        lv3.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    public void registerTextures(TextureManager textureManager) {
        textureManager.registerTexture(this.id, (AbstractTexture)new CubemapTexture(this.id));
    }

    @Override
    public void close() {
        this.buffer.close();
        this.projectionMatrix.close();
    }
}

