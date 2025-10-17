/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.decoration;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;

public record Brightness(int block, int sky) {
    public static final Codec<Integer> LIGHT_LEVEL_CODEC = Codecs.rangedInt(0, 15);
    public static final Codec<Brightness> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)LIGHT_LEVEL_CODEC.fieldOf("block")).forGetter(Brightness::block), ((MapCodec)LIGHT_LEVEL_CODEC.fieldOf("sky")).forGetter(Brightness::sky)).apply((Applicative<Brightness, ?>)instance, Brightness::new));
    public static final Brightness FULL = new Brightness(15, 15);

    public static int pack(int block, int sky) {
        return block << 4 | sky << 20;
    }

    public int pack() {
        return Brightness.pack(this.block, this.sky);
    }

    public static int unpackBlock(int packed) {
        return packed >> 4 & 0xFFFF;
    }

    public static int unpackSky(int packed) {
        return packed >> 20 & 0xFFFF;
    }

    public static Brightness unpack(int packed) {
        return new Brightness(Brightness.unpackBlock(packed), Brightness.unpackSky(packed));
    }
}

