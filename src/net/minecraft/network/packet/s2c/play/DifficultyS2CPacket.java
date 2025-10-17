/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.world.Difficulty;

public record DifficultyS2CPacket(Difficulty difficulty, boolean difficultyLocked) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<ByteBuf, DifficultyS2CPacket> CODEC = PacketCodec.tuple(Difficulty.PACKET_CODEC, DifficultyS2CPacket::difficulty, PacketCodecs.BOOLEAN, DifficultyS2CPacket::difficultyLocked, DifficultyS2CPacket::new);

    @Override
    public PacketType<DifficultyS2CPacket> getPacketType() {
        return PlayPackets.CHANGE_DIFFICULTY_S2C;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onDifficulty(this);
    }
}

