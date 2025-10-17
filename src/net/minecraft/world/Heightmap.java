/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import org.slf4j.Logger;

public class Heightmap {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Predicate<BlockState> NOT_AIR = state -> !state.isAir();
    static final Predicate<BlockState> SUFFOCATES = AbstractBlock.AbstractBlockState::blocksMovement;
    private final PaletteStorage storage;
    private final Predicate<BlockState> blockPredicate;
    private final Chunk chunk;

    public Heightmap(Chunk chunk, Type type) {
        this.blockPredicate = type.getBlockPredicate();
        this.chunk = chunk;
        int i = MathHelper.ceilLog2(chunk.getHeight() + 1);
        this.storage = new PackedIntegerArray(i, 256);
    }

    public static void populateHeightmaps(Chunk chunk, Set<Type> types) {
        if (types.isEmpty()) {
            return;
        }
        int i = types.size();
        ObjectArrayList objectList = new ObjectArrayList(i);
        Iterator objectListIterator = objectList.iterator();
        int j = chunk.getHighestNonEmptySectionYOffset() + 16;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int k = 0; k < 16; ++k) {
            block1: for (int l = 0; l < 16; ++l) {
                for (Type lv2 : types) {
                    objectList.add(chunk.getHeightmap(lv2));
                }
                for (int m = j - 1; m >= chunk.getBottomY(); --m) {
                    lv.set(k, m, l);
                    BlockState lv3 = chunk.getBlockState(lv);
                    if (lv3.isOf(Blocks.AIR)) continue;
                    while (objectListIterator.hasNext()) {
                        Heightmap lv4 = (Heightmap)objectListIterator.next();
                        if (!lv4.blockPredicate.test(lv3)) continue;
                        lv4.set(k, l, m + 1);
                        objectListIterator.remove();
                    }
                    if (objectList.isEmpty()) continue block1;
                    objectListIterator.back(i);
                }
            }
        }
    }

    public boolean trackUpdate(int x, int y, int z, BlockState state) {
        int l = this.get(x, z);
        if (y <= l - 2) {
            return false;
        }
        if (this.blockPredicate.test(state)) {
            if (y >= l) {
                this.set(x, z, y + 1);
                return true;
            }
        } else if (l - 1 == y) {
            BlockPos.Mutable lv = new BlockPos.Mutable();
            for (int m = y - 1; m >= this.chunk.getBottomY(); --m) {
                lv.set(x, m, z);
                if (!this.blockPredicate.test(this.chunk.getBlockState(lv))) continue;
                this.set(x, z, m + 1);
                return true;
            }
            this.set(x, z, this.chunk.getBottomY());
            return true;
        }
        return false;
    }

    public int get(int x, int z) {
        return this.get(Heightmap.toIndex(x, z));
    }

    public int getOneLower(int x, int z) {
        return this.get(Heightmap.toIndex(x, z)) - 1;
    }

    private int get(int index) {
        return this.storage.get(index) + this.chunk.getBottomY();
    }

    private void set(int x, int z, int height) {
        this.storage.set(Heightmap.toIndex(x, z), height - this.chunk.getBottomY());
    }

    public void setTo(Chunk chunk, Type type, long[] values) {
        long[] ms = this.storage.getData();
        if (ms.length == values.length) {
            System.arraycopy(values, 0, ms, 0, values.length);
            return;
        }
        LOGGER.warn("Ignoring heightmap data for chunk {}, size does not match; expected: {}, got: {}", chunk.getPos(), ms.length, values.length);
        Heightmap.populateHeightmaps(chunk, EnumSet.of(type));
    }

    public long[] asLongArray() {
        return this.storage.getData();
    }

    private static int toIndex(int x, int z) {
        return x + z * 16;
    }

    public static enum Type implements StringIdentifiable
    {
        WORLD_SURFACE_WG(0, "WORLD_SURFACE_WG", Purpose.WORLDGEN, NOT_AIR),
        WORLD_SURFACE(1, "WORLD_SURFACE", Purpose.CLIENT, NOT_AIR),
        OCEAN_FLOOR_WG(2, "OCEAN_FLOOR_WG", Purpose.WORLDGEN, SUFFOCATES),
        OCEAN_FLOOR(3, "OCEAN_FLOOR", Purpose.LIVE_WORLD, SUFFOCATES),
        MOTION_BLOCKING(4, "MOTION_BLOCKING", Purpose.CLIENT, state -> state.blocksMovement() || !state.getFluidState().isEmpty()),
        MOTION_BLOCKING_NO_LEAVES(5, "MOTION_BLOCKING_NO_LEAVES", Purpose.CLIENT, state -> (state.blocksMovement() || !state.getFluidState().isEmpty()) && !(state.getBlock() instanceof LeavesBlock));

        public static final Codec<Type> CODEC;
        private static final IntFunction<Type> INDEX_MAPPER;
        public static final PacketCodec<ByteBuf, Type> PACKET_CODEC;
        private final int index;
        private final String id;
        private final Purpose purpose;
        private final Predicate<BlockState> blockPredicate;

        private Type(int index, String id, Purpose purpose, Predicate<BlockState> blockPredicate) {
            this.index = index;
            this.id = id;
            this.purpose = purpose;
            this.blockPredicate = blockPredicate;
        }

        public String getId() {
            return this.id;
        }

        public boolean shouldSendToClient() {
            return this.purpose == Purpose.CLIENT;
        }

        public boolean isStoredServerSide() {
            return this.purpose != Purpose.WORLDGEN;
        }

        public Predicate<BlockState> getBlockPredicate() {
            return this.blockPredicate;
        }

        @Override
        public String asString() {
            return this.id;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Type::values);
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(type -> type.index, Type.values(), ValueLists.OutOfBoundsHandling.ZERO);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, type -> type.index);
        }
    }

    public static enum Purpose {
        WORLDGEN,
        LIVE_WORLD,
        CLIENT;

    }
}

