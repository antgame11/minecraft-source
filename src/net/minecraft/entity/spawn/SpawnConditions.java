/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.spawn;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.spawn.BiomeSpawnCondition;
import net.minecraft.entity.spawn.MoonBrightnessSpawnCondition;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.StructureSpawnCondition;
import net.minecraft.registry.Registry;

public class SpawnConditions {
    public static MapCodec<? extends SpawnCondition> registerAndGetDefault(Registry<MapCodec<? extends SpawnCondition>> registry) {
        Registry.register(registry, "structure", StructureSpawnCondition.CODEC);
        Registry.register(registry, "moon_brightness", MoonBrightnessSpawnCondition.CODEC);
        return Registry.register(registry, "biome", BiomeSpawnCondition.CODEC);
    }
}

