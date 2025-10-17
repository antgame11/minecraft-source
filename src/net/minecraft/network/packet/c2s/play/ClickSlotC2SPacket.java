/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.play;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;

public record ClickSlotC2SPacket(int syncId, int revision, short slot, byte button, SlotActionType actionType, Int2ObjectMap<ItemStackHash> modifiedStacks, ItemStackHash cursor) implements Packet<ServerPlayPacketListener>
{
    private static final int MAX_MODIFIED_STACKS = 128;
    private static final PacketCodec<RegistryByteBuf, Int2ObjectMap<ItemStackHash>> STACK_MAP_CODEC = PacketCodecs.map(Int2ObjectOpenHashMap::new, PacketCodecs.SHORT.xmap(Short::intValue, Integer::shortValue), ItemStackHash.PACKET_CODEC, 128);
    public static final PacketCodec<RegistryByteBuf, ClickSlotC2SPacket> CODEC = PacketCodec.tuple(PacketCodecs.SYNC_ID, ClickSlotC2SPacket::syncId, PacketCodecs.VAR_INT, ClickSlotC2SPacket::revision, PacketCodecs.SHORT, ClickSlotC2SPacket::slot, PacketCodecs.BYTE, ClickSlotC2SPacket::button, SlotActionType.PACKET_CODEC, ClickSlotC2SPacket::actionType, STACK_MAP_CODEC, ClickSlotC2SPacket::modifiedStacks, ItemStackHash.PACKET_CODEC, ClickSlotC2SPacket::cursor, ClickSlotC2SPacket::new);

    public ClickSlotC2SPacket {
        int2ObjectMap = Int2ObjectMaps.unmodifiable(int2ObjectMap);
    }

    @Override
    public PacketType<ClickSlotC2SPacket> getPacketType() {
        return PlayPackets.CONTAINER_CLICK;
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onClickSlot(this);
    }
}

