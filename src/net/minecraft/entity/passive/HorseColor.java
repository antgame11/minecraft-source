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

public enum HorseColor implements StringIdentifiable
{
    WHITE(0, "white"),
    CREAMY(1, "creamy"),
    CHESTNUT(2, "chestnut"),
    BROWN(3, "brown"),
    BLACK(4, "black"),
    GRAY(5, "gray"),
    DARK_BROWN(6, "dark_brown");

    public static final Codec<HorseColor> CODEC;
    private static final IntFunction<HorseColor> INDEX_MAPPER;
    public static final PacketCodec<ByteBuf, HorseColor> PACKET_CODEC;
    private final int index;
    private final String id;

    private HorseColor(int index, String id) {
        this.index = index;
        this.id = id;
    }

    public int getIndex() {
        return this.index;
    }

    public static HorseColor byIndex(int index) {
        return INDEX_MAPPER.apply(index);
    }

    @Override
    public String asString() {
        return this.id;
    }

    static {
        CODEC = StringIdentifiable.createCodec(HorseColor::values);
        INDEX_MAPPER = ValueLists.createIndexToValueFunction(HorseColor::getIndex, HorseColor.values(), ValueLists.OutOfBoundsHandling.WRAP);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, HorseColor::getIndex);
    }
}

