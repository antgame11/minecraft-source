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

public record WolfVariant(WolfAssetInfo assetInfo, SpawnConditionSelectors spawnConditions) implements VariantSelectorProvider<SpawnContext, SpawnCondition>
{
    public static final Codec<WolfVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)WolfAssetInfo.CODEC.fieldOf("assets")).forGetter(WolfVariant::assetInfo), ((MapCodec)SpawnConditionSelectors.CODEC.fieldOf("spawn_conditions")).forGetter(WolfVariant::spawnConditions)).apply((Applicative<WolfVariant, ?>)instance, WolfVariant::new));
    public static final Codec<WolfVariant> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)WolfAssetInfo.CODEC.fieldOf("assets")).forGetter(WolfVariant::assetInfo)).apply((Applicative<WolfVariant, ?>)instance, WolfVariant::new));
    public static final Codec<RegistryEntry<WolfVariant>> ENTRY_CODEC = RegistryFixedCodec.of(RegistryKeys.WOLF_VARIANT);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<WolfVariant>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.WOLF_VARIANT);

    private WolfVariant(WolfAssetInfo assetInfo) {
        this(assetInfo, SpawnConditionSelectors.EMPTY);
    }

    @Override
    public List<VariantSelectorProvider.Selector<SpawnContext, SpawnCondition>> getSelectors() {
        return this.spawnConditions.selectors();
    }

    public record WolfAssetInfo(AssetInfo.TextureAssetInfo wild, AssetInfo.TextureAssetInfo tame, AssetInfo.TextureAssetInfo angry) {
        public static final Codec<WolfAssetInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)AssetInfo.TextureAssetInfo.CODEC.fieldOf("wild")).forGetter(WolfAssetInfo::wild), ((MapCodec)AssetInfo.TextureAssetInfo.CODEC.fieldOf("tame")).forGetter(WolfAssetInfo::tame), ((MapCodec)AssetInfo.TextureAssetInfo.CODEC.fieldOf("angry")).forGetter(WolfAssetInfo::angry)).apply((Applicative<WolfAssetInfo, ?>)instance, WolfAssetInfo::new));
    }
}

