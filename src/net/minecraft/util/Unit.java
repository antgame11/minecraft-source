/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;

public enum Unit {
    INSTANCE;

    public static final Codec<Unit> CODEC;
    public static final PacketCodec<ByteBuf, Unit> PACKET_CODEC;

    static {
        CODEC = Codec.unit(INSTANCE);
        PACKET_CODEC = PacketCodec.unit(INSTANCE);
    }
}

