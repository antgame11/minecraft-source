/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.spawn;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.biome.Biome;

public record BiomeSpawnCondition(RegistryEntryList<Biome> requiredBiomes) implements SpawnCondition
{
    public static final MapCodec<BiomeSpawnCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryCodecs.entryList(RegistryKeys.BIOME).fieldOf("biomes")).forGetter(BiomeSpawnCondition::requiredBiomes)).apply((Applicative<BiomeSpawnCondition, ?>)instance, BiomeSpawnCondition::new));

    @Override
    public boolean test(SpawnContext arg) {
        return this.requiredBiomes.contains(arg.biome());
    }

    public MapCodec<BiomeSpawnCondition> getCodec() {
        return CODEC;
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((SpawnContext)context);
    }
}

