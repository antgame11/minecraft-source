/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlStateManager;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.BufferManager;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.gl.GlGpuBuffer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public abstract class GpuBufferManager {
    public static GpuBufferManager create(GLCapabilities capabilities, Set<String> usedCapabilities) {
        if (capabilities.GL_ARB_buffer_storage && GlBackend.allowGlBufferStorage) {
            usedCapabilities.add("GL_ARB_buffer_storage");
            return new ARBGpuBufferManager();
        }
        return new DirectGpuBufferManager();
    }

    public abstract GlGpuBuffer createBuffer(BufferManager var1, @Nullable Supplier<String> var2, int var3, int var4);

    public abstract GlGpuBuffer createBuffer(BufferManager var1, @Nullable Supplier<String> var2, int var3, ByteBuffer var4);

    public abstract GlGpuBuffer.Mapped mapBufferRange(BufferManager var1, GlGpuBuffer var2, int var3, int var4, int var5);

    @Environment(value=EnvType.CLIENT)
    static class ARBGpuBufferManager
    extends GpuBufferManager {
        ARBGpuBufferManager() {
        }

        @Override
        public GlGpuBuffer createBuffer(BufferManager bufferManager, @Nullable Supplier<String> debugLabelSupplier, int usage, int size) {
            int k = bufferManager.createBuffer();
            bufferManager.setBufferStorage(k, size, usage);
            ByteBuffer byteBuffer = this.mapBufferRange(bufferManager, usage, k, size);
            return new GlGpuBuffer(debugLabelSupplier, bufferManager, usage, size, k, byteBuffer);
        }

        @Override
        public GlGpuBuffer createBuffer(BufferManager bufferManager, @Nullable Supplier<String> debugLabelSupplier, int usage, ByteBuffer data) {
            int j = bufferManager.createBuffer();
            int k = data.remaining();
            bufferManager.setBufferStorage(j, data, usage);
            ByteBuffer byteBuffer2 = this.mapBufferRange(bufferManager, usage, j, k);
            return new GlGpuBuffer(debugLabelSupplier, bufferManager, usage, k, j, byteBuffer2);
        }

        @Nullable
        private ByteBuffer mapBufferRange(BufferManager bufferManager, int usage, int buffer, int length) {
            ByteBuffer byteBuffer;
            int l = 0;
            if ((usage & GpuBuffer.USAGE_MAP_READ) != 0) {
                l |= GL30.GL_MAP_READ_BIT;
            }
            if ((usage & GpuBuffer.USAGE_MAP_WRITE) != 0) {
                l |= 0x12;
            }
            if (l != 0) {
                GlStateManager.clearGlErrors();
                byteBuffer = bufferManager.mapBufferRange(buffer, 0, length, l | 0x40, usage);
                if (byteBuffer == null) {
                    throw new IllegalStateException("Can't persistently map buffer, opengl error " + GlStateManager._getError());
                }
            } else {
                byteBuffer = null;
            }
            return byteBuffer;
        }

        @Override
        public GlGpuBuffer.Mapped mapBufferRange(BufferManager bufferManager, GlGpuBuffer buffer, int offset, int length, int flags) {
            if (buffer.backingBuffer == null) {
                throw new IllegalStateException("Somehow trying to map an unmappable buffer");
            }
            return new GlGpuBuffer.Mapped(() -> {
                if ((flags & 2) != 0) {
                    bufferManager.flushMappedBufferRange(arg2.id, offset, length, buffer.usage());
                }
            }, buffer, MemoryUtil.memSlice(buffer.backingBuffer, offset, length));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DirectGpuBufferManager
    extends GpuBufferManager {
        DirectGpuBufferManager() {
        }

        @Override
        public GlGpuBuffer createBuffer(BufferManager bufferManager, @Nullable Supplier<String> debugLabelSupplier, int usage, int size) {
            int k = bufferManager.createBuffer();
            bufferManager.setBufferData(k, size, usage);
            return new GlGpuBuffer(debugLabelSupplier, bufferManager, usage, size, k, null);
        }

        @Override
        public GlGpuBuffer createBuffer(BufferManager bufferManager, @Nullable Supplier<String> debugLabelSupplier, int usage, ByteBuffer data) {
            int j = bufferManager.createBuffer();
            int k = data.remaining();
            bufferManager.setBufferData(j, data, usage);
            return new GlGpuBuffer(debugLabelSupplier, bufferManager, usage, k, j, null);
        }

        @Override
        public GlGpuBuffer.Mapped mapBufferRange(BufferManager bufferManager, GlGpuBuffer buffer, int offset, int length, int flags) {
            GlStateManager.clearGlErrors();
            ByteBuffer byteBuffer = bufferManager.mapBufferRange(buffer.id, offset, length, flags, buffer.usage());
            if (byteBuffer == null) {
                throw new IllegalStateException("Can't map buffer, opengl error " + GlStateManager._getError());
            }
            return new GlGpuBuffer.Mapped(() -> bufferManager.unmapBuffer(arg2.id, buffer.usage()), buffer, byteBuffer);
        }
    }
}

