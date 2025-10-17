/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.DynamicUniformStorage;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

@Environment(value=EnvType.CLIENT)
public class DynamicUniforms
implements AutoCloseable {
    public static final int SIZE = new Std140SizeCalculator().putMat4f().putVec4().putVec3().putMat4f().putFloat().get();
    private static final int DEFAULT_CAPACITY = 2;
    private final DynamicUniformStorage<UniformValue> storage = new DynamicUniformStorage("Dynamic Transforms UBO", SIZE, 2);

    public void clear() {
        this.storage.clear();
    }

    @Override
    public void close() {
        this.storage.close();
    }

    public GpuBufferSlice write(Matrix4fc modelView, Vector4fc colorModulator, Vector3fc modelOffset, Matrix4fc textureMatrix, float lineWidth) {
        return this.storage.write(new UniformValue(new Matrix4f(modelView), new Vector4f(colorModulator), new Vector3f(modelOffset), new Matrix4f(textureMatrix), lineWidth));
    }

    public GpuBufferSlice[] writeAll(UniformValue ... values) {
        return this.storage.writeAll(values);
    }

    @Environment(value=EnvType.CLIENT)
    public record UniformValue(Matrix4fc modelView, Vector4fc colorModulator, Vector3fc modelOffset, Matrix4fc textureMatrix, float lineWidth) implements DynamicUniformStorage.Uploadable
    {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer).putMat4f(this.modelView).putVec4(this.colorModulator).putVec3(this.modelOffset).putMat4f(this.textureMatrix).putFloat(this.lineWidth);
        }
    }
}

