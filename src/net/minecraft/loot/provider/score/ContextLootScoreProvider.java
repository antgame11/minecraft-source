/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.provider.score;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.score.LootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProviderType;
import net.minecraft.loot.provider.score.LootScoreProviderTypes;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.util.context.ContextParameter;
import org.jetbrains.annotations.Nullable;

public record ContextLootScoreProvider(LootContext.EntityReference target) implements LootScoreProvider
{
    public static final MapCodec<ContextLootScoreProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)LootContext.EntityReference.CODEC.fieldOf("target")).forGetter(ContextLootScoreProvider::target)).apply((Applicative<ContextLootScoreProvider, ?>)instance, ContextLootScoreProvider::new));
    public static final Codec<ContextLootScoreProvider> INLINE_CODEC = LootContext.EntityReference.CODEC.xmap(ContextLootScoreProvider::new, ContextLootScoreProvider::target);

    public static LootScoreProvider create(LootContext.EntityReference target) {
        return new ContextLootScoreProvider(target);
    }

    @Override
    public LootScoreProviderType getType() {
        return LootScoreProviderTypes.CONTEXT;
    }

    @Override
    @Nullable
    public ScoreHolder getScoreHolder(LootContext context) {
        return context.get(this.target.getParameter());
    }

    @Override
    public Set<ContextParameter<?>> getRequiredParameters() {
        return Set.of(this.target.getParameter());
    }
}

