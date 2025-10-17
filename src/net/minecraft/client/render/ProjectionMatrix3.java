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
public class ProjectionMatrix3
implements AutoCloseable {
    private final GpuBuffer buffer;
    private final GpuBufferSlice slice;
    private final float nearZ;
    private final float farZ;
    private int width;
    private int height;
    private float fov;

    public ProjectionMatrix3(String name, float nearZ, float farZ) {
        this.nearZ = nearZ;
        this.farZ = farZ;
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.buffer = gpuDevice.createBuffer(() -> "Projection matrix UBO " + name, GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
        this.slice = this.buffer.slice(0, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
    }

    public GpuBufferSlice set(int width, int height, float fov) {
        if (this.width != width || this.height != height || this.fov != fov) {
            Matrix4f matrix4f = this.getMatrix(width, height, fov);
            try (MemoryStack memoryStack = MemoryStack.stackPush();){
                ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, RenderSystem.PROJECTION_MATRIX_UBO_SIZE).putMat4f(matrix4f).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer.slice(), byteBuffer);
            }
            this.width = width;
            this.height = height;
            this.fov = fov;
        }
        return this.slice;
    }

    private Matrix4f getMatrix(int width, int height, float fov) {
        return new Matrix4f().perspective(fov * ((float)Math.PI / 180), (float)width / (float)height, this.nearZ, this.farZ);
    }

    @Override
    public void close() {
        this.buffer.close();
    }
}

