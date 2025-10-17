/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;

@Environment(value=EnvType.CLIENT)
public interface VertexConsumer {
    public VertexConsumer vertex(float var1, float var2, float var3);

    public VertexConsumer color(int var1, int var2, int var3, int var4);

    public VertexConsumer texture(float var1, float var2);

    public VertexConsumer overlay(int var1, int var2);

    public VertexConsumer light(int var1, int var2);

    public VertexConsumer normal(float var1, float var2, float var3);

    default public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        this.vertex(x, y, z);
        this.color(color);
        this.texture(u, v);
        this.overlay(overlay);
        this.light(light);
        this.normal(normalX, normalY, normalZ);
    }

    default public VertexConsumer color(float red, float green, float blue, float alpha) {
        return this.color((int)(red * 255.0f), (int)(green * 255.0f), (int)(blue * 255.0f), (int)(alpha * 255.0f));
    }

    default public VertexConsumer color(int argb) {
        return this.color(ColorHelper.getRed(argb), ColorHelper.getGreen(argb), ColorHelper.getBlue(argb), ColorHelper.getAlpha(argb));
    }

    default public VertexConsumer light(int uv) {
        return this.light(uv & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xFF0F), uv >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xFF0F));
    }

    default public VertexConsumer overlay(int uv) {
        return this.overlay(uv & 0xFFFF, uv >> 16 & 0xFFFF);
    }

    default public void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay) {
        this.quad(matrixEntry, quad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, red, green, blue, alpha, new int[]{light, light, light, light}, overlay, false);
    }

    default public void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green, float blue, float alpha, int[] lights, int overlay, boolean colorize) {
        int[] js = quad.vertexData();
        Vector3fc vector3fc = quad.face().getFloatVector();
        Matrix4f matrix4f = matrixEntry.getPositionMatrix();
        Vector3f vector3f = matrixEntry.transformNormal(vector3fc, new Vector3f());
        int k = 8;
        int l = js.length / 8;
        int m = (int)(alpha * 255.0f);
        int n = quad.lightEmission();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSize());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            for (int o = 0; o < l; ++o) {
                float x;
                float w;
                float v;
                float u;
                intBuffer.clear();
                intBuffer.put(js, o * 8, 8);
                float p = byteBuffer.getFloat(0);
                float q = byteBuffer.getFloat(4);
                float r = byteBuffer.getFloat(8);
                if (colorize) {
                    float s = byteBuffer.get(12) & 0xFF;
                    float t = byteBuffer.get(13) & 0xFF;
                    u = byteBuffer.get(14) & 0xFF;
                    v = s * brightnesses[o] * red;
                    w = t * brightnesses[o] * green;
                    x = u * brightnesses[o] * blue;
                } else {
                    v = brightnesses[o] * red * 255.0f;
                    w = brightnesses[o] * green * 255.0f;
                    x = brightnesses[o] * blue * 255.0f;
                }
                int y = ColorHelper.getArgb(m, (int)v, (int)w, (int)x);
                int z = LightmapTextureManager.applyEmission(lights[o], n);
                u = byteBuffer.getFloat(16);
                float aa = byteBuffer.getFloat(20);
                Vector3f vector3f2 = matrix4f.transformPosition(p, q, r, new Vector3f());
                this.vertex(vector3f2.x(), vector3f2.y(), vector3f2.z(), y, u, aa, overlay, z, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }
    }

    default public VertexConsumer vertex(Vector3f vec) {
        return this.vertex(vec.x(), vec.y(), vec.z());
    }

    default public VertexConsumer vertex(MatrixStack.Entry matrix, Vector3f vec) {
        return this.vertex(matrix, vec.x(), vec.y(), vec.z());
    }

    default public VertexConsumer vertex(MatrixStack.Entry matrix, float x, float y, float z) {
        return this.vertex(matrix.getPositionMatrix(), x, y, z);
    }

    default public VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
        Vector3f vector3f = matrix.transformPosition(x, y, z, new Vector3f());
        return this.vertex(vector3f.x(), vector3f.y(), vector3f.z());
    }

    default public VertexConsumer vertex(Matrix3x2f matrix, float x, float y) {
        Vector2f vector2f = matrix.transformPosition(x, y, new Vector2f());
        return this.vertex(vector2f.x(), vector2f.y(), 0.0f);
    }

    default public VertexConsumer normal(MatrixStack.Entry matrix, float x, float y, float z) {
        Vector3f vector3f = matrix.transformNormal(x, y, z, new Vector3f());
        return this.normal(vector3f.x(), vector3f.y(), vector3f.z());
    }

    default public VertexConsumer normal(MatrixStack.Entry matrix, Vector3f vec) {
        return this.normal(matrix, vec.x(), vec.y(), vec.z());
    }
}

