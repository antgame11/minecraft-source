/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.particle;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

public record BlockParticleEffect(ParticleEffect particle, float scaling, float speed) {
    public static final MapCodec<BlockParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ParticleTypes.TYPE_CODEC.fieldOf("particle")).forGetter(BlockParticleEffect::particle), Codec.FLOAT.optionalFieldOf("scaling", Float.valueOf(1.0f)).forGetter(BlockParticleEffect::scaling), Codec.FLOAT.optionalFieldOf("speed", Float.valueOf(1.0f)).forGetter(BlockParticleEffect::speed)).apply((Applicative<BlockParticleEffect, ?>)instance, BlockParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, BlockParticleEffect> PACKET_CODEC = PacketCodec.tuple(ParticleTypes.PACKET_CODEC, BlockParticleEffect::particle, PacketCodecs.FLOAT, BlockParticleEffect::scaling, PacketCodecs.FLOAT, BlockParticleEffect::speed, BlockParticleEffect::new);
}

