/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record SetPlayerInventoryS2CPacket(int slot, ItemStack contents) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, SetPlayerInventoryS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, SetPlayerInventoryS2CPacket::slot, ItemStack.OPTIONAL_PACKET_CODEC, SetPlayerInventoryS2CPacket::contents, SetPlayerInventoryS2CPacket::new);

    @Override
    public PacketType<SetPlayerInventoryS2CPacket> getPacketType() {
        return PlayPackets.SET_PLAYER_INVENTORY;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onSetPlayerInventory(this);
    }
}

