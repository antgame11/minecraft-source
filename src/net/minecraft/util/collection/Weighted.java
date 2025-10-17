/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.collection;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import org.slf4j.Logger;

public record Weighted<T>(T value, int weight) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public Weighted {
        if (weight < 0) {
            throw Util.getFatalOrPause(new IllegalArgumentException("Weight should be >= 0"));
        }
        if (weight == 0 && SharedConstants.isDevelopment) {
            LOGGER.warn("Found 0 weight, make sure this is intentional!");
        }
    }

    public static <E> Codec<Weighted<E>> createCodec(Codec<E> dataCodec) {
        return Weighted.createCodec(dataCodec.fieldOf("data"));
    }

    public static <E> Codec<Weighted<E>> createCodec(MapCodec<E> dataCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(dataCodec.forGetter(Weighted::value), ((MapCodec)Codecs.NON_NEGATIVE_INT.fieldOf("weight")).forGetter(Weighted::weight)).apply((Applicative<Weighted, ?>)instance, Weighted::new));
    }

    public static <B extends ByteBuf, T> PacketCodec<B, Weighted<T>> createPacketCodec(PacketCodec<B, T> dataCodec) {
        return PacketCodec.tuple(dataCodec, Weighted::value, PacketCodecs.VAR_INT, Weighted::weight, Weighted::new);
    }

    public <U> Weighted<U> transform(Function<T, U> function) {
        return new Weighted<U>(function.apply(this.value()), this.weight);
    }
}

