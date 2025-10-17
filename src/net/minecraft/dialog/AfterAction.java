/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.dialog;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum AfterAction implements StringIdentifiable
{
    CLOSE(0, "close"),
    NONE(1, "none"),
    WAIT_FOR_RESPONSE(2, "wait_for_response");

    public static final IntFunction<AfterAction> INDEX_MAPPER;
    public static final StringIdentifiable.EnumCodec<AfterAction> CODEC;
    public static final PacketCodec<ByteBuf, AfterAction> PACKET_CODEC;
    private final int index;
    private final String id;

    private AfterAction(int index, String id) {
        this.index = index;
        this.id = id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    public boolean canUnpause() {
        return this == CLOSE || this == WAIT_FOR_RESPONSE;
    }

    static {
        INDEX_MAPPER = ValueLists.createIndexToValueFunction(afterAction -> afterAction.index, AfterAction.values(), ValueLists.OutOfBoundsHandling.ZERO);
        CODEC = StringIdentifiable.createCodec(AfterAction::values);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, afterAction -> afterAction.index);
    }
}

