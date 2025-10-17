/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChainBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class OxidizableChainBlock
extends ChainBlock
implements Oxidizable {
    public static final MapCodec<OxidizableChainBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Oxidizable.OxidationLevel.CODEC.fieldOf("weathering_state")).forGetter(OxidizableChainBlock::getDegradationLevel), OxidizableChainBlock.createSettingsCodec()).apply((Applicative<OxidizableChainBlock, ?>)instance, OxidizableChainBlock::new));
    private final Oxidizable.OxidationLevel oxidationLevel;

    public MapCodec<OxidizableChainBlock> getCodec() {
        return CODEC;
    }

    protected OxidizableChainBlock(Oxidizable.OxidationLevel oxidationLevel, AbstractBlock.Settings settings) {
        super(settings);
        this.oxidationLevel = oxidationLevel;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.tickDegradation(state, world, pos, random);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return Oxidizable.getIncreasedOxidationBlock(state.getBlock()).isPresent();
    }

    @Override
    public Oxidizable.OxidationLevel getDegradationLevel() {
        return this.oxidationLevel;
    }

    @Override
    public /* synthetic */ Enum getDegradationLevel() {
        return this.getDegradationLevel();
    }
}

