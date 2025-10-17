/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.world.debug.DebugSubscriptionType;

public record EntityValueDebugS2CPacket(int entityId, DebugSubscriptionType.OptionalValue<?> update) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, EntityValueDebugS2CPacket> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, EntityValueDebugS2CPacket::entityId, DebugSubscriptionType.OptionalValue.PACKET_CODEC, EntityValueDebugS2CPacket::update, EntityValueDebugS2CPacket::new);

    @Override
    public PacketType<EntityValueDebugS2CPacket> getPacketType() {
        return PlayPackets.ENTITY_VALUE_DEBUG;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onEntityValueDebug(this);
    }
}

