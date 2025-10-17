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
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.BufferManager;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GlGpuBuffer
extends GpuBuffer {
    protected static final MemoryPool POOL = TracyClient.createMemoryPool("GPU Buffers");
    protected boolean closed;
    @Nullable
    protected final Supplier<String> debugLabelSupplier;
    private final BufferManager bufferManager;
    protected final int id;
    @Nullable
    protected ByteBuffer backingBuffer;

    protected GlGpuBuffer(@Nullable Supplier<String> debugLabelSupplier, BufferManager bufferManager, int usage, int size, int id, @Nullable ByteBuffer backingBuffer) {
        super(usage, size);
        this.debugLabelSupplier = debugLabelSupplier;
        this.bufferManager = bufferManager;
        this.id = id;
        this.backingBuffer = backingBuffer;
        POOL.malloc(id, size);
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.backingBuffer != null) {
            this.bufferManager.unmapBuffer(this.id, this.usage());
            this.backingBuffer = null;
        }
        GlStateManager._glDeleteBuffers(this.id);
        POOL.free(this.id);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Mapped
    implements GpuBuffer.MappedView {
        private final Runnable closer;
        private final GlGpuBuffer backingBuffer;
        private final ByteBuffer data;
        private boolean closed;

        protected Mapped(Runnable closer, GlGpuBuffer backingBuffer, ByteBuffer data) {
            this.closer = closer;
            this.backingBuffer = backingBuffer;
            this.data = data;
        }

        @Override
        public ByteBuffer data() {
            return this.data;
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.closer.run();
        }
    }
}

