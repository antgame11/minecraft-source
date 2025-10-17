/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.world.WorldProperties;

public record PlayerSpawnPositionS2CPacket(WorldProperties.SpawnPoint respawnData) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, PlayerSpawnPositionS2CPacket> CODEC = PacketCodec.tuple(WorldProperties.SpawnPoint.PACKET_CODEC, PlayerSpawnPositionS2CPacket::respawnData, PlayerSpawnPositionS2CPacket::new);

    @Override
    public PacketType<PlayerSpawnPositionS2CPacket> getPacketType() {
        return PlayPackets.SET_DEFAULT_SPAWN_POSITION;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onPlayerSpawnPosition(this);
    }
}

