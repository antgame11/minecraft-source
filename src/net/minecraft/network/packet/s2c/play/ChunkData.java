/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class ChunkData {
    private static final PacketCodec<ByteBuf, Map<Heightmap.Type, long[]>> HEIGHTMAPS_PACKET_CODEC = PacketCodecs.map(size -> new EnumMap(Heightmap.Type.class), Heightmap.Type.PACKET_CODEC, PacketCodecs.LONG_ARRAY);
    private static final int MAX_SECTIONS_DATA_SIZE = 0x200000;
    private final Map<Heightmap.Type, long[]> heightmap;
    private final byte[] sectionsData;
    private final List<BlockEntityData> blockEntities;

    public ChunkData(WorldChunk chunk) {
        this.heightmap = chunk.getHeightmaps().stream().filter(entry -> ((Heightmap.Type)entry.getKey()).shouldSendToClient()).collect(Collectors.toMap(Map.Entry::getKey, entry -> (long[])((Heightmap)entry.getValue()).asLongArray().clone()));
        this.sectionsData = new byte[ChunkData.getSectionsPacketSize(chunk)];
        ChunkData.writeSections(new PacketByteBuf(this.getWritableSectionsDataBuf()), chunk);
        this.blockEntities = Lists.newArrayList();
        for (Map.Entry<BlockPos, BlockEntity> entry2 : chunk.getBlockEntities().entrySet()) {
            this.blockEntities.add(BlockEntityData.of(entry2.getValue()));
        }
    }

    public ChunkData(RegistryByteBuf buf, int x, int z) {
        this.heightmap = (Map)HEIGHTMAPS_PACKET_CODEC.decode(buf);
        int k = buf.readVarInt();
        if (k > 0x200000) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        }
        this.sectionsData = new byte[k];
        buf.readBytes(this.sectionsData);
        this.blockEntities = (List)BlockEntityData.LIST_PACKET_CODEC.decode(buf);
    }

    public void write(RegistryByteBuf buf) {
        HEIGHTMAPS_PACKET_CODEC.encode(buf, this.heightmap);
        buf.writeVarInt(this.sectionsData.length);
        buf.writeBytes(this.sectionsData);
        BlockEntityData.LIST_PACKET_CODEC.encode(buf, this.blockEntities);
    }

    private static int getSectionsPacketSize(WorldChunk chunk) {
        int i = 0;
        for (ChunkSection lv : chunk.getSectionArray()) {
            i += lv.getPacketSize();
        }
        return i;
    }

    private ByteBuf getWritableSectionsDataBuf() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(this.sectionsData);
        byteBuf.writerIndex(0);
        return byteBuf;
    }

    public static void writeSections(PacketByteBuf buf, WorldChunk chunk) {
        for (ChunkSection lv : chunk.getSectionArray()) {
            lv.toPacket(buf);
        }
        if (buf.writerIndex() != buf.capacity()) {
            throw new IllegalStateException("Didn't fill chunk buffer: expected " + buf.capacity() + " bytes, got " + buf.writerIndex());
        }
    }

    public Consumer<BlockEntityVisitor> getBlockEntities(int x, int z) {
        return visitor -> this.iterateBlockEntities((BlockEntityVisitor)visitor, x, z);
    }

    private void iterateBlockEntities(BlockEntityVisitor consumer, int x, int z) {
        int k = 16 * x;
        int l = 16 * z;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (BlockEntityData lv2 : this.blockEntities) {
            int m = k + ChunkSectionPos.getLocalCoord(lv2.localXz >> 4);
            int n = l + ChunkSectionPos.getLocalCoord(lv2.localXz);
            lv.set(m, lv2.y, n);
            consumer.accept(lv, lv2.type, lv2.nbt);
        }
    }

    public PacketByteBuf getSectionsDataBuf() {
        return new PacketByteBuf(Unpooled.wrappedBuffer(this.sectionsData));
    }

    public Map<Heightmap.Type, long[]> getHeightmap() {
        return this.heightmap;
    }

    static class BlockEntityData {
        public static final PacketCodec<RegistryByteBuf, BlockEntityData> PACKET_CODEC = PacketCodec.of(BlockEntityData::write, BlockEntityData::new);
        public static final PacketCodec<RegistryByteBuf, List<BlockEntityData>> LIST_PACKET_CODEC = PACKET_CODEC.collect(PacketCodecs.toList());
        final int localXz;
        final int y;
        final BlockEntityType<?> type;
        @Nullable
        final NbtCompound nbt;

        private BlockEntityData(int localXz, int y, BlockEntityType<?> type, @Nullable NbtCompound nbt) {
            this.localXz = localXz;
            this.y = y;
            this.type = type;
            this.nbt = nbt;
        }

        private BlockEntityData(RegistryByteBuf buf) {
            this.localXz = buf.readByte();
            this.y = buf.readShort();
            this.type = (BlockEntityType)PacketCodecs.registryValue(RegistryKeys.BLOCK_ENTITY_TYPE).decode(buf);
            this.nbt = buf.readNbt();
        }

        private void write(RegistryByteBuf buf) {
            buf.writeByte(this.localXz);
            buf.writeShort(this.y);
            PacketCodecs.registryValue(RegistryKeys.BLOCK_ENTITY_TYPE).encode(buf, this.type);
            buf.writeNbt(this.nbt);
        }

        static BlockEntityData of(BlockEntity blockEntity) {
            NbtCompound lv = blockEntity.toInitialChunkDataNbt(blockEntity.getWorld().getRegistryManager());
            BlockPos lv2 = blockEntity.getPos();
            int i = ChunkSectionPos.getLocalCoord(lv2.getX()) << 4 | ChunkSectionPos.getLocalCoord(lv2.getZ());
            return new BlockEntityData(i, lv2.getY(), blockEntity.getType(), lv.isEmpty() ? null : lv);
        }
    }

    @FunctionalInterface
    public static interface BlockEntityVisitor {
        public void accept(BlockPos var1, BlockEntityType<?> var2, @Nullable NbtCompound var3);
    }
}

