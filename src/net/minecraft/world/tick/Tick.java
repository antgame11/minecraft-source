/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.tick;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Hash;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

public record Tick<T>(T type, BlockPos pos, int delay, TickPriority priority) {
    public static final Hash.Strategy<Tick<?>> HASH_STRATEGY = new Hash.Strategy<Tick<?>>(){

        @Override
        public int hashCode(Tick<?> arg) {
            return 31 * arg.pos().hashCode() + arg.type().hashCode();
        }

        @Override
        public boolean equals(@Nullable Tick<?> arg, @Nullable Tick<?> arg2) {
            if (arg == arg2) {
                return true;
            }
            if (arg == null || arg2 == null) {
                return false;
            }
            return arg.type() == arg2.type() && arg.pos().equals(arg2.pos());
        }

        @Override
        public /* synthetic */ boolean equals(@Nullable Object first, @Nullable Object second) {
            return this.equals((Tick)first, (Tick)second);
        }

        @Override
        public /* synthetic */ int hashCode(Object tick) {
            return this.hashCode((Tick)tick);
        }
    };

    public static <T> Codec<Tick<T>> createCodec(Codec<T> typeCodec) {
        MapCodec mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("x")).forGetter(Vec3i::getX), ((MapCodec)Codec.INT.fieldOf("y")).forGetter(Vec3i::getY), ((MapCodec)Codec.INT.fieldOf("z")).forGetter(Vec3i::getZ)).apply((Applicative<BlockPos, ?>)instance, BlockPos::new));
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)typeCodec.fieldOf("i")).forGetter(Tick::type), mapCodec.forGetter(Tick::pos), ((MapCodec)Codec.INT.fieldOf("t")).forGetter(Tick::delay), ((MapCodec)TickPriority.CODEC.fieldOf("p")).forGetter(Tick::priority)).apply((Applicative<Tick, ?>)instance, Tick::new));
    }

    public static <T> List<Tick<T>> filter(List<Tick<T>> ticks, ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        return ticks.stream().filter(tick -> ChunkPos.toLong(tick.pos()) == l).toList();
    }

    public OrderedTick<T> createOrderedTick(long time, long subTickOrder) {
        return new OrderedTick<T>(this.type, this.pos, time + (long)this.delay, this.priority, subTickOrder);
    }

    public static <T> Tick<T> create(T type, BlockPos pos) {
        return new Tick<T>(type, pos, 0, TickPriority.NORMAL);
    }
}

