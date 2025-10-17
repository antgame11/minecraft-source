/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;

public interface WorldProperties {
    public SpawnPoint getSpawnPoint();

    public long getTime();

    public long getTimeOfDay();

    public boolean isThundering();

    public boolean isRaining();

    public void setRaining(boolean var1);

    public boolean isHardcore();

    public Difficulty getDifficulty();

    public boolean isDifficultyLocked();

    default public void populateCrashReport(CrashReportSection reportSection, HeightLimitView world) {
        reportSection.add("Level spawn location", () -> CrashReportSection.createPositionString(world, this.getSpawnPoint().getPos()));
        reportSection.add("Level time", () -> String.format(Locale.ROOT, "%d game time, %d day time", this.getTime(), this.getTimeOfDay()));
    }

    public record SpawnPoint(GlobalPos globalPos, float yaw, float pitch) {
        public static final SpawnPoint DEFAULT = new SpawnPoint(GlobalPos.create(World.OVERWORLD, BlockPos.ORIGIN), 0.0f, 0.0f);
        public static final MapCodec<SpawnPoint> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(GlobalPos.MAP_CODEC.forGetter(SpawnPoint::globalPos), ((MapCodec)Codec.floatRange(-180.0f, 180.0f).fieldOf("yaw")).forGetter(SpawnPoint::yaw), ((MapCodec)Codec.floatRange(-90.0f, 90.0f).fieldOf("pitch")).forGetter(SpawnPoint::pitch)).apply((Applicative<SpawnPoint, ?>)instance, SpawnPoint::new));
        public static final Codec<SpawnPoint> CODEC = MAP_CODEC.codec();
        public static final PacketCodec<ByteBuf, SpawnPoint> PACKET_CODEC = PacketCodec.tuple(GlobalPos.PACKET_CODEC, SpawnPoint::globalPos, PacketCodecs.FLOAT, SpawnPoint::yaw, PacketCodecs.FLOAT, SpawnPoint::pitch, SpawnPoint::new);

        public static SpawnPoint create(RegistryKey<World> dimension, BlockPos pos, float yaw, float pitch) {
            return new SpawnPoint(GlobalPos.create(dimension, pos.toImmutable()), yaw, pitch);
        }

        public RegistryKey<World> getDimension() {
            return this.globalPos.dimension();
        }

        public BlockPos getPos() {
            return this.globalPos.pos();
        }
    }
}

