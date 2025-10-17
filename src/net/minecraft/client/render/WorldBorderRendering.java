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
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.state.WorldBorderRenderState;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.border.WorldBorder;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public class WorldBorderRendering {
    public static final Identifier FORCEFIELD = Identifier.ofVanilla("textures/misc/forcefield.png");
    private boolean forceRefreshBuffers = true;
    private double lastUploadedBoundWest;
    private double lastUploadedBoundNorth;
    private double lastXMin;
    private double lastXMax;
    private double lastZMin;
    private double lastZMax;
    private final GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "World border vertex buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, 16 * VertexFormats.POSITION_TEXTURE.getVertexSize());
    private final RenderSystem.ShapeIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);

    private void refreshDirectionBuffer(WorldBorderRenderState state, double viewDistanceBlocks, double z, double x, float farPlaneDistance, float vMin, float vMax) {
        try (BufferAllocator lv = BufferAllocator.fixedSized(VertexFormats.POSITION_TEXTURE.getVertexSize() * 4 * 4);){
            double j = state.minX;
            double k = state.maxX;
            double l = state.minZ;
            double m = state.maxZ;
            double n = Math.max((double)MathHelper.floor(z - viewDistanceBlocks), l);
            double o = Math.min((double)MathHelper.ceil(z + viewDistanceBlocks), m);
            float p = (float)(MathHelper.floor(n) & 1) * 0.5f;
            float q = (float)(o - n) / 2.0f;
            double r = Math.max((double)MathHelper.floor(x - viewDistanceBlocks), j);
            double s = Math.min((double)MathHelper.ceil(x + viewDistanceBlocks), k);
            float t = (float)(MathHelper.floor(r) & 1) * 0.5f;
            float u = (float)(s - r) / 2.0f;
            BufferBuilder lv2 = new BufferBuilder(lv, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            lv2.vertex(0.0f, -farPlaneDistance, (float)(m - n)).texture(t, vMin);
            lv2.vertex((float)(s - r), -farPlaneDistance, (float)(m - n)).texture(u + t, vMin);
            lv2.vertex((float)(s - r), farPlaneDistance, (float)(m - n)).texture(u + t, vMax);
            lv2.vertex(0.0f, farPlaneDistance, (float)(m - n)).texture(t, vMax);
            lv2.vertex(0.0f, -farPlaneDistance, 0.0f).texture(p, vMin);
            lv2.vertex(0.0f, -farPlaneDistance, (float)(o - n)).texture(q + p, vMin);
            lv2.vertex(0.0f, farPlaneDistance, (float)(o - n)).texture(q + p, vMax);
            lv2.vertex(0.0f, farPlaneDistance, 0.0f).texture(p, vMax);
            lv2.vertex((float)(s - r), -farPlaneDistance, 0.0f).texture(t, vMin);
            lv2.vertex(0.0f, -farPlaneDistance, 0.0f).texture(u + t, vMin);
            lv2.vertex(0.0f, farPlaneDistance, 0.0f).texture(u + t, vMax);
            lv2.vertex((float)(s - r), farPlaneDistance, 0.0f).texture(t, vMax);
            lv2.vertex((float)(k - r), -farPlaneDistance, (float)(o - n)).texture(p, vMin);
            lv2.vertex((float)(k - r), -farPlaneDistance, 0.0f).texture(q + p, vMin);
            lv2.vertex((float)(k - r), farPlaneDistance, 0.0f).texture(q + p, vMax);
            lv2.vertex((float)(k - r), farPlaneDistance, (float)(o - n)).texture(p, vMax);
            try (BuiltBuffer lv3 = lv2.end();){
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.vertexBuffer.slice(), lv3.getBuffer());
            }
            this.lastXMin = j;
            this.lastXMax = k;
            this.lastZMin = l;
            this.lastZMax = m;
            this.lastUploadedBoundWest = r;
            this.lastUploadedBoundNorth = n;
            this.forceRefreshBuffers = false;
        }
    }

    public void updateRenderState(WorldBorder arg, Vec3d arg2, double d, WorldBorderRenderState arg3) {
        arg3.minX = arg.getBoundWest();
        arg3.maxX = arg.getBoundEast();
        arg3.minZ = arg.getBoundNorth();
        arg3.maxZ = arg.getBoundSouth();
        if (arg2.x < arg3.maxX - d && arg2.x > arg3.minX + d && arg2.z < arg3.maxZ - d && arg2.z > arg3.minZ + d || arg2.x < arg3.minX - d || arg2.x > arg3.maxX + d || arg2.z < arg3.minZ - d || arg2.z > arg3.maxZ + d) {
            arg3.alpha = 0.0;
            return;
        }
        arg3.alpha = 1.0 - arg.getDistanceInsideBorder(arg2.x, arg2.z) / d;
        arg3.alpha = Math.pow(arg3.alpha, 4.0);
        arg3.alpha = MathHelper.clamp(arg3.alpha, 0.0, 1.0);
        arg3.tint = arg.getStage().getColor();
    }

    public void render(WorldBorderRenderState state, Vec3d cameraPos, double viewDistanceBlocks, double farPlaneDistance) {
        GpuTextureView gpuTextureView2;
        GpuTextureView gpuTextureView;
        if (state.alpha <= 0.0) {
            return;
        }
        double f = cameraPos.x;
        double g = cameraPos.z;
        float h = (float)farPlaneDistance;
        float i = (float)ColorHelper.getRed(state.tint) / 255.0f;
        float j = (float)ColorHelper.getGreen(state.tint) / 255.0f;
        float k = (float)ColorHelper.getBlue(state.tint) / 255.0f;
        float l = (float)(Util.getMeasuringTimeMs() % 3000L) / 3000.0f;
        float m = (float)(-MathHelper.fractionalPart(cameraPos.y * 0.5));
        float n = m + h;
        if (this.shouldRefreshBuffers(state)) {
            this.refreshDirectionBuffer(state, viewDistanceBlocks, g, f, h, n, m);
        }
        TextureManager lv = MinecraftClient.getInstance().getTextureManager();
        AbstractTexture lv2 = lv.getTexture(FORCEFIELD);
        lv2.setUseMipmaps(false);
        RenderPipeline renderPipeline = RenderPipelines.RENDERTYPE_WORLD_BORDER;
        Framebuffer lv3 = MinecraftClient.getInstance().getFramebuffer();
        Framebuffer lv4 = MinecraftClient.getInstance().worldRenderer.getWeatherFramebuffer();
        if (lv4 != null) {
            gpuTextureView = lv4.getColorAttachmentView();
            gpuTextureView2 = lv4.getDepthAttachmentView();
        } else {
            gpuTextureView = lv3.getColorAttachmentView();
            gpuTextureView2 = lv3.getDepthAttachmentView();
        }
        GpuBuffer gpuBuffer = this.indexBuffer.getIndexBuffer(6);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(), new Vector4f(i, j, k, (float)state.alpha), new Vector3f((float)(this.lastUploadedBoundWest - f), (float)(-cameraPos.y), (float)(this.lastUploadedBoundNorth - g)), new Matrix4f().translation(l, l, 0.0f), 0.0f);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "World border", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setIndexBuffer(gpuBuffer, this.indexBuffer.getIndexType());
            renderPass.bindSampler("Sampler0", lv2.getGlTextureView());
            renderPass.setVertexBuffer(0, this.vertexBuffer);
            ArrayList arrayList = new ArrayList();
            for (WorldBorderRenderState.Distance lv5 : state.nearestBorder(f, g)) {
                if (!(lv5.value() < viewDistanceBlocks)) continue;
                int o = lv5.direction().getHorizontalQuarterTurns();
                arrayList.add(new RenderPass.RenderObject(0, this.vertexBuffer, gpuBuffer, this.indexBuffer.getIndexType(), 6 * o, 6));
            }
            renderPass.drawMultipleIndexed(arrayList, null, null, Collections.emptyList(), this);
        }
    }

    public void markBuffersDirty() {
        this.forceRefreshBuffers = true;
    }

    private boolean shouldRefreshBuffers(WorldBorderRenderState state) {
        return this.forceRefreshBuffers || state.minX != this.lastXMin || state.minZ != this.lastZMin || state.maxX != this.lastXMax || state.maxZ != this.lastZMax;
    }
}

