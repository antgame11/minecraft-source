/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item.equipment.trim;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.equipment.trim.ArmorTrimAssets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record ArmorTrimMaterial(ArmorTrimAssets assets, Text description) {
    public static final Codec<ArmorTrimMaterial> CODEC = RecordCodecBuilder.create(instance -> instance.group(ArmorTrimAssets.CODEC.forGetter(ArmorTrimMaterial::assets), ((MapCodec)TextCodecs.CODEC.fieldOf("description")).forGetter(ArmorTrimMaterial::description)).apply((Applicative<ArmorTrimMaterial, ?>)instance, ArmorTrimMaterial::new));
    public static final PacketCodec<RegistryByteBuf, ArmorTrimMaterial> PACKET_CODEC = PacketCodec.tuple(ArmorTrimAssets.PACKET_CODEC, ArmorTrimMaterial::assets, TextCodecs.REGISTRY_PACKET_CODEC, ArmorTrimMaterial::description, ArmorTrimMaterial::new);
    public static final Codec<RegistryEntry<ArmorTrimMaterial>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.TRIM_MATERIAL, CODEC);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<ArmorTrimMaterial>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.TRIM_MATERIAL, PACKET_CODEC);
}

