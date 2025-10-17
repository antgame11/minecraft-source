/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.spawn;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.predicate.NumberRange;

public record MoonBrightnessSpawnCondition(NumberRange.DoubleRange range) implements SpawnCondition
{
    public static final MapCodec<MoonBrightnessSpawnCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)NumberRange.DoubleRange.CODEC.fieldOf("range")).forGetter(MoonBrightnessSpawnCondition::range)).apply((Applicative<MoonBrightnessSpawnCondition, ?>)instance, MoonBrightnessSpawnCondition::new));

    @Override
    public boolean test(SpawnContext arg) {
        return this.range.test(arg.world().toServerWorld().getMoonSize());
    }

    public MapCodec<MoonBrightnessSpawnCondition> getCodec() {
        return CODEC;
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((SpawnContext)context);
    }
}

