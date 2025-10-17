/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.systems.VertexSorter;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.Vec3fArray;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class BuiltBuffer
implements AutoCloseable {
    private final BufferAllocator.CloseableBuffer buffer;
    @Nullable
    private BufferAllocator.CloseableBuffer sortedBuffer;
    private final DrawParameters drawParameters;

    public BuiltBuffer(BufferAllocator.CloseableBuffer buffer, DrawParameters drawParameters) {
        this.buffer = buffer;
        this.drawParameters = drawParameters;
    }

    private static Vec3fArray collectCentroids(ByteBuffer buffer, int vertexCount, VertexFormat format) {
        int j = format.getOffset(VertexFormatElement.POSITION);
        if (j == -1) {
            throw new IllegalArgumentException("Cannot identify quad centers with no position element");
        }
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        int k = format.getVertexSize() / 4;
        int l = k * 4;
        int m = vertexCount / 4;
        Vec3fArray lv = new Vec3fArray(m);
        for (int n = 0; n < m; ++n) {
            int o = n * l + j;
            int p = o + k * 2;
            float f = floatBuffer.get(o + 0);
            float g = floatBuffer.get(o + 1);
            float h = floatBuffer.get(o + 2);
            float q = floatBuffer.get(p + 0);
            float r = floatBuffer.get(p + 1);
            float s = floatBuffer.get(p + 2);
            float t = (f + q) / 2.0f;
            float u = (g + r) / 2.0f;
            float v = (h + s) / 2.0f;
            lv.set(n, t, u, v);
        }
        return lv;
    }

    public ByteBuffer getBuffer() {
        return this.buffer.getBuffer();
    }

    @Nullable
    public ByteBuffer getSortedBuffer() {
        return this.sortedBuffer != null ? this.sortedBuffer.getBuffer() : null;
    }

    public DrawParameters getDrawParameters() {
        return this.drawParameters;
    }

    @Nullable
    public SortState sortQuads(BufferAllocator allocator, VertexSorter sorter) {
        if (this.drawParameters.mode() != VertexFormat.DrawMode.QUADS) {
            return null;
        }
        Vec3fArray lv = BuiltBuffer.collectCentroids(this.buffer.getBuffer(), this.drawParameters.vertexCount(), this.drawParameters.format());
        SortState lv2 = new SortState(lv, this.drawParameters.indexType());
        this.sortedBuffer = lv2.sortAndStore(allocator, sorter);
        return lv2;
    }

    @Override
    public void close() {
        this.buffer.close();
        if (this.sortedBuffer != null) {
            this.sortedBuffer.close();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record DrawParameters(VertexFormat format, int vertexCount, int indexCount, VertexFormat.DrawMode mode, VertexFormat.IndexType indexType) {
    }

    @Environment(value=EnvType.CLIENT)
    public record SortState(Vec3fArray centroids, VertexFormat.IndexType indexType) {
        @Nullable
        public BufferAllocator.CloseableBuffer sortAndStore(BufferAllocator allocator, VertexSorter sorter) {
            int[] is = sorter.sort(this.centroids);
            long l = allocator.allocate(is.length * 6 * this.indexType.size);
            IntConsumer intConsumer = this.getStorer(l, this.indexType);
            for (int i : is) {
                intConsumer.accept(i * 4 + 0);
                intConsumer.accept(i * 4 + 1);
                intConsumer.accept(i * 4 + 2);
                intConsumer.accept(i * 4 + 2);
                intConsumer.accept(i * 4 + 3);
                intConsumer.accept(i * 4 + 0);
            }
            return allocator.getAllocated();
        }

        private IntConsumer getStorer(long pointer, VertexFormat.IndexType indexType) {
            MutableLong mutableLong = new MutableLong(pointer);
            return switch (indexType) {
                default -> throw new MatchException(null, null);
                case VertexFormat.IndexType.SHORT -> i -> MemoryUtil.memPutShort(mutableLong.getAndAdd(2L), (short)i);
                case VertexFormat.IndexType.INT -> i -> MemoryUtil.memPutInt(mutableLong.getAndAdd(4L), i);
            };
        }
    }
}

