/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.jetbrains.annotations.NotNull;

public enum CopperGolemState implements StringIdentifiable
{
    IDLE("idle", 0),
    GETTING_ITEM("getting_item", 1),
    GETTING_NO_ITEM("getting_no_item", 2),
    DROPPING_ITEM("dropping_item", 3),
    DROPPING_NO_ITEM("dropping_no_item", 4);

    public static final Codec<CopperGolemState> CODEC;
    private static final IntFunction<CopperGolemState> INDEX_MAPPER;
    public static final PacketCodec<ByteBuf, CopperGolemState> PACKET_CODEC;
    private final String id;
    private final int index;

    private CopperGolemState(String id, int index) {
        this.id = id;
        this.index = index;
    }

    @Override
    @NotNull
    public String asString() {
        return this.id;
    }

    private int getIndex() {
        return this.index;
    }

    static {
        CODEC = StringIdentifiable.createCodec(CopperGolemState::values);
        INDEX_MAPPER = ValueLists.createIndexToValueFunction(CopperGolemState::getIndex, CopperGolemState.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, CopperGolemState::getIndex);
    }
}

