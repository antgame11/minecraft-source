/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.PaletteProvider;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;

public record PalettesFactory(PaletteProvider<BlockState> blockStatesStrategy, BlockState defaultBlockState, Codec<PalettedContainer<BlockState>> blockStatesContainerCodec, PaletteProvider<RegistryEntry<Biome>> biomeStrategy, RegistryEntry<Biome> defaultBiome, Codec<ReadableContainer<RegistryEntry<Biome>>> biomeContainerCodec) {
    public static PalettesFactory fromRegistryManager(DynamicRegistryManager registryManager) {
        PaletteProvider<BlockState> lv = PaletteProvider.forBlockStates(Block.STATE_IDS);
        BlockState lv2 = Blocks.AIR.getDefaultState();
        RegistryWrapper.Impl lv3 = registryManager.getOrThrow(RegistryKeys.BIOME);
        PaletteProvider<RegistryEntry<Biome>> lv4 = PaletteProvider.forBiomes(lv3.getIndexedEntries());
        RegistryEntry.Reference<Biome> lv5 = lv3.getOrThrow(BiomeKeys.PLAINS);
        return new PalettesFactory(lv, lv2, PalettedContainer.createPalettedContainerCodec(BlockState.CODEC, lv, lv2), lv4, lv5, PalettedContainer.createReadableContainerCodec(lv3.getEntryCodec(), lv4, lv5));
    }

    public PalettedContainer<BlockState> getBlockStateContainer() {
        return new PalettedContainer<BlockState>(this.defaultBlockState, this.blockStatesStrategy);
    }

    public PalettedContainer<RegistryEntry<Biome>> getBiomeContainer() {
        return new PalettedContainer<RegistryEntry<Biome>>(this.defaultBiome, this.biomeStrategy);
    }
}

