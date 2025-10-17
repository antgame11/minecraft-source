/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.AssetInfo;

public record CatVariant(AssetInfo.TextureAssetInfo assetInfo, SpawnConditionSelectors spawnConditions) implements VariantSelectorProvider<SpawnContext, SpawnCondition>
{
    public static final Codec<CatVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(AssetInfo.TextureAssetInfo.MAP_CODEC.forGetter(CatVariant::assetInfo), ((MapCodec)SpawnConditionSelectors.CODEC.fieldOf("spawn_conditions")).forGetter(CatVariant::spawnConditions)).apply((Applicative<CatVariant, ?>)instance, CatVariant::new));
    public static final Codec<CatVariant> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(AssetInfo.TextureAssetInfo.MAP_CODEC.forGetter(CatVariant::assetInfo)).apply((Applicative<CatVariant, ?>)instance, CatVariant::new));
    public static final Codec<RegistryEntry<CatVariant>> ENTRY_CODEC = RegistryFixedCodec.of(RegistryKeys.CAT_VARIANT);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<CatVariant>> PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.CAT_VARIANT);

    private CatVariant(AssetInfo.TextureAssetInfo assetInfo) {
        this(assetInfo, SpawnConditionSelectors.EMPTY);
    }

    @Override
    public List<VariantSelectorProvider.Selector<SpawnContext, SpawnCondition>> getSelectors() {
        return this.spawnConditions.selectors();
    }
}

