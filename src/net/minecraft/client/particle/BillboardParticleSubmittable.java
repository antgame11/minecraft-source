/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Submittable;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.command.LayeredCustomCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.BufferAllocator;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public class BillboardParticleSubmittable
implements OrderedRenderCommandQueue.LayeredCustom,
Submittable {
    private static final int INITIAL_BUFFER_MAX_LENGTH = 1024;
    private static final int BUFFER_FLOAT_FIELDS = 12;
    private static final int BUFFER_INT_FIELDS = 2;
    private final Map<BillboardParticle.RenderType, Vertices> bufferByType = new HashMap<BillboardParticle.RenderType, Vertices>();
    private int particles;

    public void render(BillboardParticle.RenderType renderType, float x, float y, float z, float rotationX, float rotationY, float rotationZ, float rotationW, float size, float minU, float maxU, float minV, float maxV, int color, int brightness) {
        this.bufferByType.computeIfAbsent(renderType, arg -> new Vertices()).vertex(x, y, z, rotationX, rotationY, rotationZ, rotationW, size, minU, maxU, minV, maxV, color, brightness);
        ++this.particles;
    }

    @Override
    public void onFrameEnd() {
        this.bufferByType.values().forEach(Vertices::reset);
        this.particles = 0;
    }

    @Override
    @Nullable
    public Buffers submit(LayeredCustomCommandRenderer.VerticesCache cache) {
        int i = this.particles * 4;
        try (BufferAllocator lv = BufferAllocator.fixedSized(i * VertexFormats.POSITION_TEXTURE_COLOR_LIGHT.getVertexSize());){
            BufferBuilder lv2 = new BufferBuilder(lv, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
            HashMap<BillboardParticle.RenderType, Layer> map = new HashMap<BillboardParticle.RenderType, Layer>();
            int j = 0;
            for (Map.Entry<BillboardParticle.RenderType, Vertices> entry : this.bufferByType.entrySet()) {
                entry.getValue().render((x, y, z, rotationX, rotationY, rotationZ, rotationW, size, minU, maxU, minV, maxV, color, brightness) -> this.drawFace(lv2, x, y, z, rotationX, rotationY, rotationZ, rotationW, size, minU, maxU, minV, maxV, color, brightness));
                if (entry.getValue().nextVertexIndex() > 0) {
                    map.put(entry.getKey(), new Layer(j, entry.getValue().nextVertexIndex() * 6));
                }
                j += entry.getValue().nextVertexIndex() * 4;
            }
            BuiltBuffer lv3 = lv2.endNullable();
            if (lv3 != null) {
                cache.write(lv3.getBuffer());
                RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS).getIndexBuffer(lv3.getDrawParameters().indexCount());
                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector3f(), RenderSystem.getTextureMatrix(), RenderSystem.getShaderLineWidth());
                Buffers buffers = new Buffers(lv3.getDrawParameters().indexCount(), gpuBufferSlice, map);
                return buffers;
            }
            Buffers buffers = null;
            return buffers;
        }
    }

    @Override
    public void render(Buffers buffers, LayeredCustomCommandRenderer.VerticesCache cache, RenderPass renderPass, TextureManager manager, boolean translucent) {
        RenderSystem.ShapeIndexBuffer lv = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        renderPass.setVertexBuffer(0, cache.get());
        renderPass.setIndexBuffer(lv.getIndexBuffer(buffers.indexCount), lv.getIndexType());
        renderPass.setUniform("DynamicTransforms", buffers.dynamicTransforms);
        for (Map.Entry<BillboardParticle.RenderType, Layer> entry : buffers.layers.entrySet()) {
            if (translucent != entry.getKey().translucent()) continue;
            renderPass.setPipeline(entry.getKey().pipeline());
            renderPass.bindSampler("Sampler0", manager.getTexture(entry.getKey().textureAtlasLocation()).getGlTextureView());
            renderPass.drawIndexed(entry.getValue().vertexOffset, 0, entry.getValue().indexCount, 1);
        }
    }

    protected void drawFace(VertexConsumer vertexConsumer, float x, float y, float z, float rotationX, float rotationY, float rotationZ, float rotationW, float size, float minU, float maxU, float minV, float maxV, int color, int brightness) {
        Quaternionf quaternionf = new Quaternionf(rotationX, rotationY, rotationZ, rotationW);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, 1.0f, -1.0f, size, maxU, maxV, color, brightness);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, 1.0f, 1.0f, size, maxU, minV, color, brightness);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, -1.0f, 1.0f, size, minU, minV, color, brightness);
        this.renderVertex(vertexConsumer, quaternionf, x, y, z, -1.0f, -1.0f, size, minU, maxV, color, brightness);
    }

    private void renderVertex(VertexConsumer vertexConsumer, Quaternionf rotation, float x, float y, float z, float localX, float localY, float size, float maxU, float maxV, int color, int brightness) {
        Vector3f vector3f = new Vector3f(localX, localY, 0.0f).rotate(rotation).mul(size).add(x, y, z);
        vertexConsumer.vertex(vector3f.x(), vector3f.y(), vector3f.z()).texture(maxU, maxV).color(color).light(brightness);
    }

    @Override
    public void submit(OrderedRenderCommandQueue arg, CameraRenderState arg2) {
        if (this.particles > 0) {
            arg.submitCustom(this);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Vertices {
        private int maxVertices = 1024;
        private float[] floatData = new float[12288];
        private int[] intData = new int[2048];
        private int nextVertexIndex;

        Vertices() {
        }

        public void vertex(float x, float y, float z, float rotationX, float rotationY, float rotationZ, float rotationW, float size, float minU, float maxU, float minV, float maxV, int color, int brightness) {
            if (this.nextVertexIndex >= this.maxVertices) {
                this.increaseCapacity();
            }
            int t = this.nextVertexIndex * 12;
            this.floatData[t++] = x;
            this.floatData[t++] = y;
            this.floatData[t++] = z;
            this.floatData[t++] = rotationX;
            this.floatData[t++] = rotationY;
            this.floatData[t++] = rotationZ;
            this.floatData[t++] = rotationW;
            this.floatData[t++] = size;
            this.floatData[t++] = minU;
            this.floatData[t++] = maxU;
            this.floatData[t++] = minV;
            this.floatData[t] = maxV;
            t = this.nextVertexIndex * 2;
            this.intData[t++] = color;
            this.intData[t] = brightness;
            ++this.nextVertexIndex;
        }

        public void render(Consumer vertexConsumer) {
            for (int i = 0; i < this.nextVertexIndex; ++i) {
                int j = i * 12;
                int k = i * 2;
                vertexConsumer.consume(this.floatData[j++], this.floatData[j++], this.floatData[j++], this.floatData[j++], this.floatData[j++], this.floatData[j++], this.floatData[j++], this.floatData[j++], this.floatData[j++], this.floatData[j++], this.floatData[j++], this.floatData[j], this.intData[k++], this.intData[k]);
            }
        }

        public void reset() {
            this.nextVertexIndex = 0;
        }

        private void increaseCapacity() {
            this.maxVertices *= 2;
            this.floatData = Arrays.copyOf(this.floatData, this.maxVertices * 12);
            this.intData = Arrays.copyOf(this.intData, this.maxVertices * 2);
        }

        public int nextVertexIndex() {
            return this.nextVertexIndex;
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface Consumer {
        public void consume(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, int var13, int var14);
    }

    @Environment(value=EnvType.CLIENT)
    public record Layer(int vertexOffset, int indexCount) {
    }

    @Environment(value=EnvType.CLIENT)
    public record Buffers(int indexCount, GpuBufferSlice dynamicTransforms, Map<BillboardParticle.RenderType, Layer> layers) {
    }
}

