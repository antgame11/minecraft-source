/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.chunk;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.chunk.AbstractChunkRenderData;
import net.minecraft.client.render.chunk.Buffers;
import net.minecraft.client.render.chunk.ChunkOcclusionData;
import net.minecraft.client.render.chunk.NormalizedRelativePos;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChunkRenderData
implements AbstractChunkRenderData {
    public static final AbstractChunkRenderData HIDDEN = new AbstractChunkRenderData(){

        @Override
        public boolean isVisibleThrough(Direction from, Direction to) {
            return false;
        }
    };
    public static final AbstractChunkRenderData READY = new AbstractChunkRenderData(){

        @Override
        public boolean isVisibleThrough(Direction from, Direction to) {
            return true;
        }
    };
    private final List<BlockEntity> blockEntities;
    private final ChunkOcclusionData chunkOcclusionData;
    @Nullable
    private final BuiltBuffer.SortState translucencySortingData;
    @Nullable
    private NormalizedRelativePos pos;
    private final Map<BlockRenderLayer, Buffers> buffersByLayer = new EnumMap<BlockRenderLayer, Buffers>(BlockRenderLayer.class);

    public ChunkRenderData(NormalizedRelativePos pos, SectionBuilder.RenderData renderData) {
        this.pos = pos;
        this.chunkOcclusionData = renderData.chunkOcclusionData;
        this.blockEntities = renderData.blockEntities;
        this.translucencySortingData = renderData.translucencySortingData;
    }

    public void setPos(NormalizedRelativePos pos) {
        this.pos = pos;
    }

    @Override
    public boolean hasPosition(NormalizedRelativePos pos) {
        return !pos.equals(this.pos);
    }

    @Override
    public boolean hasData() {
        return !this.buffersByLayer.isEmpty();
    }

    @Override
    public boolean containsLayer(BlockRenderLayer layer) {
        return !this.buffersByLayer.containsKey((Object)layer);
    }

    @Override
    public List<BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    @Override
    public boolean isVisibleThrough(Direction from, Direction to) {
        return this.chunkOcclusionData.isVisibleThrough(from, to);
    }

    @Override
    @Nullable
    public Buffers getBuffersForLayer(BlockRenderLayer layer) {
        return this.buffersByLayer.get((Object)layer);
    }

    public void upload(BlockRenderLayer layer, BuiltBuffer builtBuffer, long sectionPos) {
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        Buffers lv = this.getBuffersForLayer(layer);
        if (lv != null) {
            if (lv.getVertexBuffer().size() < builtBuffer.getBuffer().remaining()) {
                lv.getVertexBuffer().close();
                lv.setVertexBuffer(RenderSystem.getDevice().createBuffer(() -> "Section vertex buffer - layer: " + layer.getName() + "; cords: " + ChunkSectionPos.unpackX(sectionPos) + ", " + ChunkSectionPos.unpackY(sectionPos) + ", " + ChunkSectionPos.unpackZ(sectionPos), GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, builtBuffer.getBuffer()));
            } else if (!lv.getVertexBuffer().isClosed()) {
                commandEncoder.writeToBuffer(lv.getVertexBuffer().slice(), builtBuffer.getBuffer());
            }
            ByteBuffer byteBuffer = builtBuffer.getSortedBuffer();
            if (byteBuffer != null) {
                if (lv.getIndexBuffer() == null || lv.getIndexBuffer().size() < byteBuffer.remaining()) {
                    if (lv.getIndexBuffer() != null) {
                        lv.getIndexBuffer().close();
                    }
                    lv.setIndexBuffer(RenderSystem.getDevice().createBuffer(() -> "Section index buffer - layer: " + layer.getName() + "; cords: " + ChunkSectionPos.unpackX(sectionPos) + ", " + ChunkSectionPos.unpackY(sectionPos) + ", " + ChunkSectionPos.unpackZ(sectionPos), GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, byteBuffer));
                } else if (!lv.getIndexBuffer().isClosed()) {
                    commandEncoder.writeToBuffer(lv.getIndexBuffer().slice(), byteBuffer);
                }
            } else if (lv.getIndexBuffer() != null) {
                lv.getIndexBuffer().close();
                lv.setIndexBuffer(null);
            }
            lv.setIndexCount(builtBuffer.getDrawParameters().indexCount());
            lv.setIndexType(builtBuffer.getDrawParameters().indexType());
        } else {
            GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Section vertex buffer - layer: " + layer.getName() + "; cords: " + ChunkSectionPos.unpackX(sectionPos) + ", " + ChunkSectionPos.unpackY(sectionPos) + ", " + ChunkSectionPos.unpackZ(sectionPos), GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, builtBuffer.getBuffer());
            ByteBuffer byteBuffer2 = builtBuffer.getSortedBuffer();
            GpuBuffer gpuBuffer2 = byteBuffer2 != null ? RenderSystem.getDevice().createBuffer(() -> "Section index buffer - layer: " + layer.getName() + "; cords: " + ChunkSectionPos.unpackX(sectionPos) + ", " + ChunkSectionPos.unpackY(sectionPos) + ", " + ChunkSectionPos.unpackZ(sectionPos), GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, byteBuffer2) : null;
            Buffers lv2 = new Buffers(gpuBuffer, gpuBuffer2, builtBuffer.getDrawParameters().indexCount(), builtBuffer.getDrawParameters().indexType());
            this.buffersByLayer.put(layer, lv2);
        }
    }

    public void uploadIndexBuffer(BlockRenderLayer layer, BufferAllocator.CloseableBuffer buffer, long sectionPos) {
        Buffers lv = this.getBuffersForLayer(layer);
        if (lv == null) {
            return;
        }
        if (lv.getIndexBuffer() == null) {
            lv.setIndexBuffer(RenderSystem.getDevice().createBuffer(() -> "Section index buffer - layer: " + layer.getName() + "; cords: " + ChunkSectionPos.unpackX(sectionPos) + ", " + ChunkSectionPos.unpackY(sectionPos) + ", " + ChunkSectionPos.unpackZ(sectionPos), GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, buffer.getBuffer()));
        } else {
            CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
            if (!lv.getIndexBuffer().isClosed()) {
                commandEncoder.writeToBuffer(lv.getIndexBuffer().slice(), buffer.getBuffer());
            }
        }
    }

    @Override
    public boolean hasTranslucentLayers() {
        return this.buffersByLayer.containsKey((Object)BlockRenderLayer.TRANSLUCENT);
    }

    @Nullable
    public BuiltBuffer.SortState getTranslucencySortingData() {
        return this.translucencySortingData;
    }

    @Override
    public void close() {
        this.buffersByLayer.values().forEach(Buffers::close);
        this.buffersByLayer.clear();
    }
}

