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
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

@Environment(value=EnvType.CLIENT)
public class ProjectionMatrix2
implements AutoCloseable {
    private final GpuBuffer buffer;
    private final GpuBufferSlice slice;
    private final float nearZ;
    private final float farZ;
    private final boolean invertY;
    private float width;
    private float height;

    public ProjectionMatrix2(String name, float nearZ, float farZ, boolean invertY) {
        this.nearZ = nearZ;
        this.farZ = farZ;
        this.invertY = invertY;
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.buffer = gpuDevice.createBuffer(() -> "Projection matrix UBO " + name, GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
        this.slice = this.buffer.slice(0, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
    }

    public GpuBufferSlice set(float width, float height) {
        if (this.width != width || this.height != height) {
            Matrix4f matrix4f = this.getMatrix(width, height);
            try (MemoryStack memoryStack = MemoryStack.stackPush();){
                ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, RenderSystem.PROJECTION_MATRIX_UBO_SIZE).putMat4f(matrix4f).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer.slice(), byteBuffer);
            }
            this.width = width;
            this.height = height;
        }
        return this.slice;
    }

    private Matrix4f getMatrix(float width, float height) {
        return new Matrix4f().setOrtho(0.0f, width, this.invertY ? height : 0.0f, this.invertY ? 0.0f : height, this.nearZ, this.farZ);
    }

    @Override
    public void close() {
        this.buffer.close();
    }
}

