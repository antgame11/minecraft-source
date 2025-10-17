/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public class ClearDialogS2CPacket
implements Packet<ClientCommonPacketListener> {
    public static final ClearDialogS2CPacket INSTANCE = new ClearDialogS2CPacket();
    public static final PacketCodec<ByteBuf, ClearDialogS2CPacket> CODEC = PacketCodec.unit(INSTANCE);

    private ClearDialogS2CPacket() {
    }

    @Override
    public PacketType<ClearDialogS2CPacket> getPacketType() {
        return CommonPackets.CLEAR_DIALOG;
    }

    @Override
    public void apply(ClientCommonPacketListener arg) {
        arg.onClearDialog(this);
    }
}

