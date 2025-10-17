/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen.slot;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

public enum SlotActionType {
    PICKUP(0),
    QUICK_MOVE(1),
    SWAP(2),
    CLONE(3),
    THROW(4),
    QUICK_CRAFT(5),
    PICKUP_ALL(6);

    private static final IntFunction<SlotActionType> INDEX_MAPPER;
    public static final PacketCodec<ByteBuf, SlotActionType> PACKET_CODEC;
    private final int index;

    private SlotActionType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    static {
        INDEX_MAPPER = ValueLists.createIndexToValueFunction(SlotActionType::getIndex, SlotActionType.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, SlotActionType::getIndex);
    }
}

