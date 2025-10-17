/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.chunk;

import java.util.Arrays;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class BlockBufferAllocatorStorage
implements AutoCloseable {
    public static final int EXPECTED_TOTAL_SIZE = Arrays.stream(BlockRenderLayer.values()).mapToInt(BlockRenderLayer::getBufferSize).sum();
    private final Map<BlockRenderLayer, BufferAllocator> allocators = Util.mapEnum(BlockRenderLayer.class, arg -> new BufferAllocator(arg.getBufferSize()));

    public BufferAllocator get(BlockRenderLayer layer) {
        return this.allocators.get((Object)layer);
    }

    public void clear() {
        this.allocators.values().forEach(BufferAllocator::clear);
    }

    public void reset() {
        this.allocators.values().forEach(BufferAllocator::reset);
    }

    @Override
    public void close() {
        this.allocators.values().forEach(BufferAllocator::close);
    }
}

