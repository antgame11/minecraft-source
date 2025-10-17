/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public record EntityPosition(Vec3d position, Vec3d deltaMovement, float yaw, float pitch) {
    public static final PacketCodec<PacketByteBuf, EntityPosition> PACKET_CODEC = PacketCodec.tuple(Vec3d.PACKET_CODEC, EntityPosition::position, Vec3d.PACKET_CODEC, EntityPosition::deltaMovement, PacketCodecs.FLOAT, EntityPosition::yaw, PacketCodecs.FLOAT, EntityPosition::pitch, EntityPosition::new);

    public static EntityPosition fromEntity(Entity entity) {
        if (entity.isInterpolating()) {
            return new EntityPosition(entity.getInterpolator().getLerpedPos(), entity.getMovement(), entity.getInterpolator().getLerpedYaw(), entity.getInterpolator().getLerpedPitch());
        }
        return new EntityPosition(entity.getEntityPos(), entity.getMovement(), entity.getYaw(), entity.getPitch());
    }

    public EntityPosition withRotation(float yaw, float pitch) {
        return new EntityPosition(this.position(), this.deltaMovement(), yaw, pitch);
    }

    public static EntityPosition fromTeleportTarget(TeleportTarget teleportTarget) {
        return new EntityPosition(teleportTarget.position(), teleportTarget.velocity(), teleportTarget.yaw(), teleportTarget.pitch());
    }

    public static EntityPosition apply(EntityPosition currentPos, EntityPosition newPos, Set<PositionFlag> flags) {
        double d = flags.contains((Object)PositionFlag.X) ? currentPos.position.x : 0.0;
        double e = flags.contains((Object)PositionFlag.Y) ? currentPos.position.y : 0.0;
        double f = flags.contains((Object)PositionFlag.Z) ? currentPos.position.z : 0.0;
        float g = flags.contains((Object)PositionFlag.Y_ROT) ? currentPos.yaw : 0.0f;
        float h = flags.contains((Object)PositionFlag.X_ROT) ? currentPos.pitch : 0.0f;
        Vec3d lv = new Vec3d(d + newPos.position.x, e + newPos.position.y, f + newPos.position.z);
        float i = g + newPos.yaw;
        float j = MathHelper.clamp(h + newPos.pitch, -90.0f, 90.0f);
        Vec3d lv2 = currentPos.deltaMovement;
        if (flags.contains((Object)PositionFlag.ROTATE_DELTA)) {
            float k = currentPos.yaw - i;
            float l = currentPos.pitch - j;
            lv2 = lv2.rotateX((float)Math.toRadians(l));
            lv2 = lv2.rotateY((float)Math.toRadians(k));
        }
        Vec3d lv3 = new Vec3d(EntityPosition.resolve(lv2.x, newPos.deltaMovement.x, flags, PositionFlag.DELTA_X), EntityPosition.resolve(lv2.y, newPos.deltaMovement.y, flags, PositionFlag.DELTA_Y), EntityPosition.resolve(lv2.z, newPos.deltaMovement.z, flags, PositionFlag.DELTA_Z));
        return new EntityPosition(lv, lv3, i, j);
    }

    private static double resolve(double delta, double value, Set<PositionFlag> flags, PositionFlag deltaFlag) {
        return flags.contains((Object)deltaFlag) ? delta + value : value;
    }
}

