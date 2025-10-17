/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

public enum EntityBlockIntersectionType {
    IN_BLOCK(0, 0x6000FF00),
    IN_FLUID(1, 0x600000FF),
    IN_AIR(2, 0x60333333);

    private static final IntFunction<EntityBlockIntersectionType> BY_ID;
    public static final PacketCodec<ByteBuf, EntityBlockIntersectionType> PACKET_CODEC;
    private final int id;
    private final int color;

    private EntityBlockIntersectionType(int id, int color) {
        this.id = id;
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    static {
        BY_ID = ValueLists.createIndexToValueFunction(type -> type.id, EntityBlockIntersectionType.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(BY_ID, type -> type.id);
    }
}

