/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.pool.alias;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.alias.StructurePoolAliasBinding;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.random.Random;

public record RandomStructurePoolAliasBinding(RegistryKey<StructurePool> alias, Pool<RegistryKey<StructurePool>> targets) implements StructurePoolAliasBinding
{
    static MapCodec<RandomStructurePoolAliasBinding> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryKey.createCodec(RegistryKeys.TEMPLATE_POOL).fieldOf("alias")).forGetter(RandomStructurePoolAliasBinding::alias), ((MapCodec)Pool.createNonEmptyCodec(RegistryKey.createCodec(RegistryKeys.TEMPLATE_POOL)).fieldOf("targets")).forGetter(RandomStructurePoolAliasBinding::targets)).apply((Applicative<RandomStructurePoolAliasBinding, ?>)instance, RandomStructurePoolAliasBinding::new));

    @Override
    public void forEach(Random random, BiConsumer<RegistryKey<StructurePool>, RegistryKey<StructurePool>> aliasConsumer) {
        this.targets.getOrEmpty(random).ifPresent(target -> aliasConsumer.accept(this.alias, (RegistryKey<StructurePool>)target));
    }

    @Override
    public Stream<RegistryKey<StructurePool>> streamTargets() {
        return this.targets.getEntries().stream().map(Weighted::value);
    }

    public MapCodec<RandomStructurePoolAliasBinding> getCodec() {
        return CODEC;
    }
}

