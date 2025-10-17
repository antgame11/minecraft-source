/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientConfigurationPacketListener;
import net.minecraft.network.packet.ConfigPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record CodeOfConductS2CPacket(String codeOfConduct) implements Packet<ClientConfigurationPacketListener>
{
    public static final PacketCodec<ByteBuf, CodeOfConductS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.STRING, CodeOfConductS2CPacket::codeOfConduct, CodeOfConductS2CPacket::new);

    @Override
    public PacketType<CodeOfConductS2CPacket> getPacketType() {
        return ConfigPackets.CODE_OF_CONDUCT;
    }

    @Override
    public void apply(ClientConfigurationPacketListener arg) {
        arg.onCodeOfConduct(this);
    }
}

