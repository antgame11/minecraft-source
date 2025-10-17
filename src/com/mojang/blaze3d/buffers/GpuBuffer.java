/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.blaze3d.buffers;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.annotation.DeobfuscateClass;

@Environment(value=EnvType.CLIENT)
@DeobfuscateClass
public abstract class GpuBuffer
implements AutoCloseable {
    public static final int USAGE_MAP_READ = 1;
    public static final int USAGE_MAP_WRITE = 2;
    public static final int USAGE_HINT_CLIENT_STORAGE = 4;
    public static final int USAGE_COPY_DST = 8;
    public static final int USAGE_COPY_SRC = 16;
    public static final int USAGE_VERTEX = 32;
    public static final int USAGE_INDEX = 64;
    public static final int USAGE_UNIFORM = 128;
    public static final int USAGE_UNIFORM_TEXEL_BUFFER = 256;
    private final int usage;
    private final int size;

    public GpuBuffer(int usage, int size) {
        this.size = size;
        this.usage = usage;
    }

    public int size() {
        return this.size;
    }

    public int usage() {
        return this.usage;
    }

    public abstract boolean isClosed();

    @Override
    public abstract void close();

    public GpuBufferSlice slice(int offset, int length) {
        if (offset < 0 || length < 0 || offset + length > this.size) {
            throw new IllegalArgumentException("Offset of " + offset + " and length " + length + " would put new slice outside buffer's range (of 0," + length + ")");
        }
        return new GpuBufferSlice(this, offset, length);
    }

    public GpuBufferSlice slice() {
        return new GpuBufferSlice(this, 0, this.size);
    }

    @Environment(value=EnvType.CLIENT)
    @DeobfuscateClass
    public static interface MappedView
    extends AutoCloseable {
        public ByteBuffer data();

        @Override
        public void close();
    }
}

