/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.spawn;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.biome.Biome;

public record SpawnContext(BlockPos pos, ServerWorldAccess world, RegistryEntry<Biome> biome) {
    public static SpawnContext of(ServerWorldAccess world, BlockPos pos) {
        RegistryEntry<Biome> lv = world.getBiome(pos);
        return new SpawnContext(pos, world, lv);
    }
}

