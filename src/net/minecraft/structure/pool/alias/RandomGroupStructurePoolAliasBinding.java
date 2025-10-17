/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.pool.alias;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.alias.StructurePoolAliasBinding;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.random.Random;

public record RandomGroupStructurePoolAliasBinding(Pool<List<StructurePoolAliasBinding>> groups) implements StructurePoolAliasBinding
{
    static MapCodec<RandomGroupStructurePoolAliasBinding> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Pool.createNonEmptyCodec(Codec.list(StructurePoolAliasBinding.CODEC)).fieldOf("groups")).forGetter(RandomGroupStructurePoolAliasBinding::groups)).apply((Applicative<RandomGroupStructurePoolAliasBinding, ?>)instance, RandomGroupStructurePoolAliasBinding::new));

    @Override
    public void forEach(Random random, BiConsumer<RegistryKey<StructurePool>, RegistryKey<StructurePool>> aliasConsumer) {
        this.groups.getOrEmpty(random).ifPresent(group -> group.forEach(binding -> binding.forEach(random, aliasConsumer)));
    }

    @Override
    public Stream<RegistryKey<StructurePool>> streamTargets() {
        return this.groups.getEntries().stream().flatMap(present -> ((List)present.value()).stream()).flatMap(StructurePoolAliasBinding::streamTargets);
    }

    public MapCodec<RandomGroupStructurePoolAliasBinding> getCodec() {
        return CODEC;
    }
}

