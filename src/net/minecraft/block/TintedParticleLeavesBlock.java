/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class TintedParticleLeavesBlock
extends LeavesBlock {
    public static final MapCodec<TintedParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.rangedInclusiveFloat(0.0f, 1.0f).fieldOf("leaf_particle_chance")).forGetter(arg -> Float.valueOf(arg.leafParticleChance)), TintedParticleLeavesBlock.createSettingsCodec()).apply((Applicative<TintedParticleLeavesBlock, ?>)instance, TintedParticleLeavesBlock::new));

    public TintedParticleLeavesBlock(float f, AbstractBlock.Settings arg) {
        super(f, arg);
    }

    @Override
    protected void spawnLeafParticle(World world, BlockPos pos, Random random) {
        TintedParticleEffect lv = TintedParticleEffect.create(ParticleTypes.TINTED_LEAVES, world.getBlockColor(pos));
        ParticleUtil.spawnParticle(world, pos, random, lv);
    }

    public MapCodec<? extends TintedParticleLeavesBlock> getCodec() {
        return CODEC;
    }
}

