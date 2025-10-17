/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util;

import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BufferAllocator
implements AutoCloseable {
    private static final MemoryPool MEMORY_POOL = TracyClient.createMemoryPool("ByteBufferBuilder");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);
    private static final long field_61052 = 0xFFFFFFFFL;
    private static final int MIN_GROWTH = 0x200000;
    private static final int CLOSED = -1;
    long pointer;
    private long size;
    private final long maxSize;
    private long offset;
    private long lastOffset;
    private int refCount;
    private int clearCount;

    public BufferAllocator(int size, long maxSize) {
        this.size = size;
        this.maxSize = maxSize;
        this.pointer = ALLOCATOR.malloc(size);
        MEMORY_POOL.malloc(this.pointer, size);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
        }
    }

    public BufferAllocator(int size) {
        this(size, 0xFFFFFFFFL);
    }

    public static BufferAllocator fixedSized(int size) {
        return new BufferAllocator(size, size);
    }

    public long allocate(int size) {
        long l = this.offset;
        long m = Math.addExact(l, (long)size);
        this.growIfNecessary(m);
        this.offset = m;
        return Math.addExact(this.pointer, l);
    }

    private void growIfNecessary(long newSize) {
        if (newSize > this.size) {
            if (newSize > this.maxSize) {
                throw new IllegalArgumentException("Maximum capacity of ByteBufferBuilder (" + this.maxSize + ") exceeded, required " + newSize);
            }
            long m = Math.min(this.size, 0x200000L);
            long n = MathHelper.clamp(this.size + m, newSize, this.maxSize);
            this.grow(n);
        }
    }

    private void grow(long newSize) {
        MEMORY_POOL.free(this.pointer);
        this.pointer = ALLOCATOR.realloc(this.pointer, newSize);
        MEMORY_POOL.malloc(this.pointer, (int)Math.min(newSize, Integer.MAX_VALUE));
        LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", (Object)this.size, (Object)newSize);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to resize buffer from " + this.size + " bytes to " + newSize + " bytes");
        }
        this.size = newSize;
    }

    @Nullable
    public CloseableBuffer getAllocated() {
        this.ensureNotFreed();
        long l = this.lastOffset;
        long m = this.offset - l;
        if (m == 0L) {
            return null;
        }
        if (m > Integer.MAX_VALUE) {
            throw new IllegalStateException("Cannot build buffer larger than 2147483647 bytes (was " + m + ")");
        }
        this.lastOffset = this.offset;
        ++this.refCount;
        return new CloseableBuffer(l, (int)m, this.clearCount);
    }

    public void clear() {
        if (this.refCount > 0) {
            LOGGER.warn("Clearing BufferBuilder with unused batches");
        }
        this.reset();
    }

    public void reset() {
        this.ensureNotFreed();
        if (this.refCount > 0) {
            this.forceClear();
            this.refCount = 0;
        }
    }

    boolean clearCountEquals(int clearCount) {
        return clearCount == this.clearCount;
    }

    void clearIfUnreferenced() {
        if (--this.refCount <= 0) {
            this.forceClear();
        }
    }

    private void forceClear() {
        long l = this.offset - this.lastOffset;
        if (l > 0L) {
            MemoryUtil.memCopy(this.pointer + this.lastOffset, this.pointer, l);
        }
        this.offset = l;
        this.lastOffset = 0L;
        ++this.clearCount;
    }

    @Override
    public void close() {
        if (this.pointer != 0L) {
            MEMORY_POOL.free(this.pointer);
            ALLOCATOR.free(this.pointer);
            this.pointer = 0L;
            this.clearCount = -1;
        }
    }

    private void ensureNotFreed() {
        if (this.pointer == 0L) {
            throw new IllegalStateException("Buffer has been freed");
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class CloseableBuffer
    implements AutoCloseable {
        private final long offset;
        private final int size;
        private final int clearCount;
        private boolean closed;

        CloseableBuffer(long offset, int size, int clearCount) {
            this.offset = offset;
            this.size = size;
            this.clearCount = clearCount;
        }

        public ByteBuffer getBuffer() {
            if (!BufferAllocator.this.clearCountEquals(this.clearCount)) {
                throw new IllegalStateException("Buffer is no longer valid");
            }
            return MemoryUtil.memByteBuffer(BufferAllocator.this.pointer + this.offset, this.size);
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            if (BufferAllocator.this.clearCountEquals(this.clearCount)) {
                BufferAllocator.this.clearIfUnreferenced();
            }
        }
    }
}

