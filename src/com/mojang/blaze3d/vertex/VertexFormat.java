/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.GpuDeviceInfo;
import net.minecraft.util.annotation.DeobfuscateClass;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
@DeobfuscateClass
public class VertexFormat {
    public static final int UNKNOWN_ELEMENT = -1;
    private final List<VertexFormatElement> elements;
    private final List<String> names;
    private final int vertexSize;
    private final int elementsMask;
    private final int[] offsetsByElement = new int[32];
    @Nullable
    private GpuBuffer immediateDrawVertexBuffer;
    @Nullable
    private GpuBuffer immediateDrawIndexBuffer;

    VertexFormat(List<VertexFormatElement> elements, List<String> names, IntList offsets, int vertexSize) {
        this.elements = elements;
        this.names = names;
        this.vertexSize = vertexSize;
        this.elementsMask = elements.stream().mapToInt(VertexFormatElement::mask).reduce(0, (a, b) -> a | b);
        for (int j = 0; j < this.offsetsByElement.length; ++j) {
            VertexFormatElement vertexFormatElement = VertexFormatElement.byId(j);
            int k = vertexFormatElement != null ? elements.indexOf(vertexFormatElement) : -1;
            this.offsetsByElement[j] = k != -1 ? offsets.getInt(k) : -1;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String toString() {
        return "VertexFormat" + String.valueOf(this.names);
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public List<VertexFormatElement> getElements() {
        return this.elements;
    }

    public List<String> getElementAttributeNames() {
        return this.names;
    }

    public int[] getOffsetsByElement() {
        return this.offsetsByElement;
    }

    public int getOffset(VertexFormatElement element) {
        return this.offsetsByElement[element.id()];
    }

    public boolean contains(VertexFormatElement element) {
        return (this.elementsMask & element.mask()) != 0;
    }

    public int getElementsMask() {
        return this.elementsMask;
    }

    public String getElementName(VertexFormatElement element) {
        int i = this.elements.indexOf(element);
        if (i == -1) {
            throw new IllegalArgumentException(String.valueOf(element) + " is not contained in format");
        }
        return this.names.get(i);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VertexFormat)) return false;
        VertexFormat vertexFormat = (VertexFormat)o;
        if (this.elementsMask != vertexFormat.elementsMask) return false;
        if (this.vertexSize != vertexFormat.vertexSize) return false;
        if (!this.names.equals(vertexFormat.names)) return false;
        if (!Arrays.equals(this.offsetsByElement, vertexFormat.offsetsByElement)) return false;
        return true;
    }

    public int hashCode() {
        return this.elementsMask * 31 + Arrays.hashCode(this.offsetsByElement);
    }

    private static GpuBuffer uploadToBuffer(@Nullable GpuBuffer gpuBuffer, ByteBuffer data, int usage, Supplier<String> labelGetter) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        if (GpuDeviceInfo.get(gpuDevice).requiresRecreateOnUploadToBuffer()) {
            if (gpuBuffer != null) {
                gpuBuffer.close();
            }
            return gpuDevice.createBuffer(labelGetter, usage, data);
        }
        if (gpuBuffer == null) {
            gpuBuffer = gpuDevice.createBuffer(labelGetter, usage, data);
        } else {
            CommandEncoder commandEncoder = gpuDevice.createCommandEncoder();
            if (gpuBuffer.size() < data.remaining()) {
                gpuBuffer.close();
                gpuBuffer = gpuDevice.createBuffer(labelGetter, usage, data);
            } else {
                commandEncoder.writeToBuffer(gpuBuffer.slice(), data);
            }
        }
        return gpuBuffer;
    }

    public GpuBuffer uploadImmediateVertexBuffer(ByteBuffer data) {
        this.immediateDrawVertexBuffer = VertexFormat.uploadToBuffer(this.immediateDrawVertexBuffer, data, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, () -> "Immediate vertex buffer for " + String.valueOf(this));
        return this.immediateDrawVertexBuffer;
    }

    public GpuBuffer uploadImmediateIndexBuffer(ByteBuffer data) {
        this.immediateDrawIndexBuffer = VertexFormat.uploadToBuffer(this.immediateDrawIndexBuffer, data, GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, () -> "Immediate index buffer for " + String.valueOf(this));
        return this.immediateDrawIndexBuffer;
    }

    @Environment(value=EnvType.CLIENT)
    @DeobfuscateClass
    public static class Builder {
        private final ImmutableMap.Builder<String, VertexFormatElement> elements = ImmutableMap.builder();
        private final IntList offsets = new IntArrayList();
        private int offset;

        Builder() {
        }

        public Builder add(String name, VertexFormatElement element) {
            this.elements.put(name, element);
            this.offsets.add(this.offset);
            this.offset += element.byteSize();
            return this;
        }

        public Builder padding(int padding) {
            this.offset += padding;
            return this;
        }

        public VertexFormat build() {
            ImmutableMap<String, VertexFormatElement> immutableMap = this.elements.buildOrThrow();
            ImmutableList<VertexFormatElement> immutableList = ((ImmutableCollection)immutableMap.values()).asList();
            ImmutableList<String> immutableList2 = ((ImmutableCollection)((Object)immutableMap.keySet())).asList();
            return new VertexFormat(immutableList, immutableList2, this.offsets, this.offset);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum DrawMode {
        LINES(2, 2, false),
        LINE_STRIP(2, 1, true),
        DEBUG_LINES(2, 2, false),
        DEBUG_LINE_STRIP(2, 1, true),
        TRIANGLES(3, 3, false),
        TRIANGLE_STRIP(3, 1, true),
        TRIANGLE_FAN(3, 1, true),
        QUADS(4, 4, false);

        public final int firstVertexCount;
        public final int additionalVertexCount;
        public final boolean shareVertices;

        private DrawMode(int firstVertexCount, int additionalVertexCount, boolean shareVertices) {
            this.firstVertexCount = firstVertexCount;
            this.additionalVertexCount = additionalVertexCount;
            this.shareVertices = shareVertices;
        }

        public int getIndexCount(int vertexCount) {
            return switch (this.ordinal()) {
                case 1, 2, 3, 4, 5, 6 -> vertexCount;
                case 0, 7 -> vertexCount / 4 * 6;
                default -> 0;
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum IndexType {
        SHORT(2),
        INT(4);

        public final int size;

        private IndexType(int size) {
            this.size = size;
        }

        public static IndexType smallestFor(int i) {
            if ((i & 0xFFFF0000) != 0) {
                return INT;
            }
            return SHORT;
        }
    }
}

