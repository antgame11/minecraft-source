/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.waypoint;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.waypoint.EntityTickProgress;
import net.minecraft.world.waypoint.Waypoint;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;

public abstract class TrackedWaypoint
implements Waypoint {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final PacketCodec<ByteBuf, TrackedWaypoint> PACKET_CODEC = PacketCodec.of(TrackedWaypoint::writeBuf, TrackedWaypoint::fromBuf);
    protected final Either<UUID, String> source;
    private final Waypoint.Config config;
    private final Type type;

    TrackedWaypoint(Either<UUID, String> source, Waypoint.Config config, Type type) {
        this.source = source;
        this.config = config;
        this.type = type;
    }

    public Either<UUID, String> getSource() {
        return this.source;
    }

    public abstract void handleUpdate(TrackedWaypoint var1);

    public void writeBuf(ByteBuf buf) {
        PacketByteBuf lv = new PacketByteBuf(buf);
        lv.writeEither(this.source, Uuids.PACKET_CODEC, PacketByteBuf::writeString);
        Waypoint.Config.PACKET_CODEC.encode(lv, this.config);
        lv.writeEnumConstant(this.type);
        this.writeAdditionalDataToBuf(buf);
    }

    public abstract void writeAdditionalDataToBuf(ByteBuf var1);

    private static TrackedWaypoint fromBuf(ByteBuf buf) {
        PacketByteBuf lv = new PacketByteBuf(buf);
        Either<UUID, String> either = lv.readEither(Uuids.PACKET_CODEC, PacketByteBuf::readString);
        Waypoint.Config lv2 = (Waypoint.Config)Waypoint.Config.PACKET_CODEC.decode(lv);
        Type lv3 = lv.readEnumConstant(Type.class);
        return lv3.factory.apply(either, lv2, lv);
    }

    public static TrackedWaypoint ofPos(UUID source, Waypoint.Config config, Vec3i pos) {
        return new Positional(source, config, pos);
    }

    public static TrackedWaypoint ofChunk(UUID source, Waypoint.Config config, ChunkPos chunkPos) {
        return new ChunkBased(source, config, chunkPos);
    }

    public static TrackedWaypoint ofAzimuth(UUID source, Waypoint.Config config, float azimuth) {
        return new Azimuth(source, config, azimuth);
    }

    public static TrackedWaypoint empty(UUID uuid) {
        return new Empty(uuid);
    }

    public abstract double getRelativeYaw(World var1, YawProvider var2, EntityTickProgress var3);

    public abstract Pitch getPitch(World var1, PitchProvider var2, EntityTickProgress var3);

    public abstract double squaredDistanceTo(Entity var1);

    public Waypoint.Config getConfig() {
        return this.config;
    }

    static enum Type {
        EMPTY(Empty::new),
        VEC3I(Positional::new),
        CHUNK(ChunkBased::new),
        AZIMUTH(Azimuth::new);

        final TriFunction<Either<UUID, String>, Waypoint.Config, PacketByteBuf, TrackedWaypoint> factory;

        private Type(TriFunction<Either<UUID, String>, Waypoint.Config, PacketByteBuf, TrackedWaypoint> factory) {
            this.factory = factory;
        }
    }

    static class Positional
    extends TrackedWaypoint {
        private Vec3i pos;

        public Positional(UUID uuid, Waypoint.Config config, Vec3i pos) {
            super(Either.left(uuid), config, Type.VEC3I);
            this.pos = pos;
        }

        public Positional(Either<UUID, String> source, Waypoint.Config config, PacketByteBuf buf) {
            super(source, config, Type.VEC3I);
            this.pos = new Vec3i(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public void handleUpdate(TrackedWaypoint waypoint) {
            if (waypoint instanceof Positional) {
                Positional lv = (Positional)waypoint;
                this.pos = lv.pos;
            } else {
                LOGGER.warn("Unsupported Waypoint update operation: {}", (Object)waypoint.getClass());
            }
        }

        @Override
        public void writeAdditionalDataToBuf(ByteBuf buf) {
            VarInts.write(buf, this.pos.getX());
            VarInts.write(buf, this.pos.getY());
            VarInts.write(buf, this.pos.getZ());
        }

        private Vec3d getSourcePos(World world, EntityTickProgress tickProgress) {
            return this.source.left().map(world::getEntity).map(entity -> {
                if (entity.getBlockPos().getManhattanDistance(this.pos) > 3) {
                    return null;
                }
                return entity.getCameraPosVec(tickProgress.getTickProgress((Entity)entity));
            }).orElseGet(() -> Vec3d.ofCenter(this.pos));
        }

        @Override
        public double getRelativeYaw(World world, YawProvider yawProvider, EntityTickProgress tickProgress) {
            Vec3d lv = yawProvider.getCameraPos().subtract(this.getSourcePos(world, tickProgress)).rotateYClockwise();
            float f = (float)MathHelper.atan2(lv.getZ(), lv.getX()) * 57.295776f;
            return MathHelper.subtractAngles(yawProvider.getCameraYaw(), f);
        }

        @Override
        public Pitch getPitch(World world, PitchProvider cameraProvider, EntityTickProgress tickProgress) {
            double d;
            Vec3d lv = cameraProvider.project(this.getSourcePos(world, tickProgress));
            boolean bl = lv.z > 1.0;
            double d2 = d = bl ? -lv.y : lv.y;
            if (d < -1.0) {
                return Pitch.DOWN;
            }
            if (d > 1.0) {
                return Pitch.UP;
            }
            if (bl) {
                if (lv.y > 0.0) {
                    return Pitch.UP;
                }
                if (lv.y < 0.0) {
                    return Pitch.DOWN;
                }
            }
            return Pitch.NONE;
        }

        @Override
        public double squaredDistanceTo(Entity receiver) {
            return receiver.squaredDistanceTo(Vec3d.ofCenter(this.pos));
        }
    }

    static class ChunkBased
    extends TrackedWaypoint {
        private ChunkPos chunkPos;

        public ChunkBased(UUID source, Waypoint.Config config, ChunkPos chunkPos) {
            super(Either.left(source), config, Type.CHUNK);
            this.chunkPos = chunkPos;
        }

        public ChunkBased(Either<UUID, String> source, Waypoint.Config config, PacketByteBuf buf) {
            super(source, config, Type.CHUNK);
            this.chunkPos = new ChunkPos(buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public void handleUpdate(TrackedWaypoint waypoint) {
            if (waypoint instanceof ChunkBased) {
                ChunkBased lv = (ChunkBased)waypoint;
                this.chunkPos = lv.chunkPos;
            } else {
                LOGGER.warn("Unsupported Waypoint update operation: {}", (Object)waypoint.getClass());
            }
        }

        @Override
        public void writeAdditionalDataToBuf(ByteBuf buf) {
            VarInts.write(buf, this.chunkPos.x);
            VarInts.write(buf, this.chunkPos.z);
        }

        private Vec3d getChunkCenterPos(double y) {
            return Vec3d.ofCenter(this.chunkPos.getCenterAtY((int)y));
        }

        @Override
        public double getRelativeYaw(World world, YawProvider yawProvider, EntityTickProgress tickProgress) {
            Vec3d lv = yawProvider.getCameraPos();
            Vec3d lv2 = lv.subtract(this.getChunkCenterPos(lv.getY())).rotateYClockwise();
            float f = (float)MathHelper.atan2(lv2.getZ(), lv2.getX()) * 57.295776f;
            return MathHelper.subtractAngles(yawProvider.getCameraYaw(), f);
        }

        @Override
        public Pitch getPitch(World world, PitchProvider cameraProvider, EntityTickProgress tickProgress) {
            double d = cameraProvider.getPitch();
            if (d < -1.0) {
                return Pitch.DOWN;
            }
            if (d > 1.0) {
                return Pitch.UP;
            }
            return Pitch.NONE;
        }

        @Override
        public double squaredDistanceTo(Entity receiver) {
            return receiver.squaredDistanceTo(Vec3d.ofCenter(this.chunkPos.getCenterAtY(receiver.getBlockY())));
        }
    }

    static class Azimuth
    extends TrackedWaypoint {
        private float azimuth;

        public Azimuth(UUID source, Waypoint.Config config, float azimuth) {
            super(Either.left(source), config, Type.AZIMUTH);
            this.azimuth = azimuth;
        }

        public Azimuth(Either<UUID, String> source, Waypoint.Config config, PacketByteBuf buf) {
            super(source, config, Type.AZIMUTH);
            this.azimuth = buf.readFloat();
        }

        @Override
        public void handleUpdate(TrackedWaypoint waypoint) {
            if (waypoint instanceof Azimuth) {
                Azimuth lv = (Azimuth)waypoint;
                this.azimuth = lv.azimuth;
            } else {
                LOGGER.warn("Unsupported Waypoint update operation: {}", (Object)waypoint.getClass());
            }
        }

        @Override
        public void writeAdditionalDataToBuf(ByteBuf buf) {
            buf.writeFloat(this.azimuth);
        }

        @Override
        public double getRelativeYaw(World world, YawProvider yawProvider, EntityTickProgress tickProgress) {
            return MathHelper.subtractAngles(yawProvider.getCameraYaw(), this.azimuth * 57.295776f);
        }

        @Override
        public Pitch getPitch(World world, PitchProvider cameraProvider, EntityTickProgress tickProgress) {
            double d = cameraProvider.getPitch();
            if (d < -1.0) {
                return Pitch.DOWN;
            }
            if (d > 1.0) {
                return Pitch.UP;
            }
            return Pitch.NONE;
        }

        @Override
        public double squaredDistanceTo(Entity receiver) {
            return Double.POSITIVE_INFINITY;
        }
    }

    static class Empty
    extends TrackedWaypoint {
        private Empty(Either<UUID, String> source, Waypoint.Config config, PacketByteBuf buf) {
            super(source, config, Type.EMPTY);
        }

        Empty(UUID source) {
            super(Either.left(source), Waypoint.Config.DEFAULT, Type.EMPTY);
        }

        @Override
        public void handleUpdate(TrackedWaypoint waypoint) {
        }

        @Override
        public void writeAdditionalDataToBuf(ByteBuf buf) {
        }

        @Override
        public double getRelativeYaw(World world, YawProvider yawProvider, EntityTickProgress tickProgress) {
            return Double.NaN;
        }

        @Override
        public Pitch getPitch(World world, PitchProvider cameraProvider, EntityTickProgress tickProgress) {
            return Pitch.NONE;
        }

        @Override
        public double squaredDistanceTo(Entity receiver) {
            return Double.POSITIVE_INFINITY;
        }
    }

    public static interface YawProvider {
        public float getCameraYaw();

        public Vec3d getCameraPos();
    }

    public static interface PitchProvider {
        public Vec3d project(Vec3d var1);

        public double getPitch();
    }

    public static enum Pitch {
        NONE,
        UP,
        DOWN;

    }
}

