/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.math.Vec3d;

public record VehicleMoveS2CPacket(Vec3d position, float yaw, float pitch) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, VehicleMoveS2CPacket> CODEC = PacketCodec.tuple(Vec3d.PACKET_CODEC, VehicleMoveS2CPacket::position, PacketCodecs.FLOAT, VehicleMoveS2CPacket::yaw, PacketCodecs.FLOAT, VehicleMoveS2CPacket::pitch, VehicleMoveS2CPacket::new);

    public static VehicleMoveS2CPacket fromVehicle(Entity vehicle) {
        return new VehicleMoveS2CPacket(vehicle.getEntityPos(), vehicle.getYaw(), vehicle.getPitch());
    }

    @Override
    public PacketType<VehicleMoveS2CPacket> getPacketType() {
        return PlayPackets.MOVE_VEHICLE_S2C;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onVehicleMove(this);
    }
}

