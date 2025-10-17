/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.PlayerInput;

public record PlayerInputC2SPacket(PlayerInput input) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, PlayerInputC2SPacket> CODEC = PacketCodec.tuple(PlayerInput.PACKET_CODEC, PlayerInputC2SPacket::input, PlayerInputC2SPacket::new);

    @Override
    public PacketType<PlayerInputC2SPacket> getPacketType() {
        return PlayPackets.PLAYER_INPUT;
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onPlayerInput(this);
    }
}

