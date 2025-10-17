/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.play;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.math.BlockPos;

public record PickItemFromBlockC2SPacket(BlockPos pos, boolean includeData) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<ByteBuf, PickItemFromBlockC2SPacket> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, PickItemFromBlockC2SPacket::pos, PacketCodecs.BOOLEAN, PickItemFromBlockC2SPacket::includeData, PickItemFromBlockC2SPacket::new);

    @Override
    public PacketType<PickItemFromBlockC2SPacket> getPacketType() {
        return PlayPackets.PICK_ITEM_FROM_BLOCK;
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onPickItemFromBlock(this);
    }
}

