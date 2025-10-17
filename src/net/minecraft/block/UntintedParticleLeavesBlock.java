/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class UntintedParticleLeavesBlock
extends LeavesBlock {
    public static final MapCodec<UntintedParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.rangedInclusiveFloat(0.0f, 1.0f).fieldOf("leaf_particle_chance")).forGetter(arg -> Float.valueOf(arg.leafParticleChance)), ((MapCodec)ParticleTypes.TYPE_CODEC.fieldOf("leaf_particle")).forGetter(arg -> arg.leafParticleEffect), UntintedParticleLeavesBlock.createSettingsCodec()).apply((Applicative<UntintedParticleLeavesBlock, ?>)instance, UntintedParticleLeavesBlock::new));
    protected final ParticleEffect leafParticleEffect;

    public UntintedParticleLeavesBlock(float leafParticleChance, ParticleEffect leafParticleEffect, AbstractBlock.Settings settings) {
        super(leafParticleChance, settings);
        this.leafParticleEffect = leafParticleEffect;
    }

    @Override
    protected void spawnLeafParticle(World world, BlockPos pos, Random random) {
        ParticleUtil.spawnParticle(world, pos, random, this.leafParticleEffect);
    }

    public MapCodec<UntintedParticleLeavesBlock> getCodec() {
        return CODEC;
    }
}

