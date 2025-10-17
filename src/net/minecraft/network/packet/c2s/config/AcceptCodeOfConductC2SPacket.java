/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.packet.ConfigPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record AcceptCodeOfConductC2SPacket() implements Packet<ServerConfigurationPacketListener>
{
    public static final AcceptCodeOfConductC2SPacket INSTANCE = new AcceptCodeOfConductC2SPacket();
    public static final PacketCodec<ByteBuf, AcceptCodeOfConductC2SPacket> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public PacketType<AcceptCodeOfConductC2SPacket> getPacketType() {
        return ConfigPackets.ACCEPT_CODE_OF_CONDUCT;
    }

    @Override
    public void apply(ServerConfigurationPacketListener arg) {
        arg.onAcceptCodeOfConduct(this);
    }
}

