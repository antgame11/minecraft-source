/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.spawn;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.gen.structure.Structure;

public record StructureSpawnCondition(RegistryEntryList<Structure> requiredStructures) implements SpawnCondition
{
    public static final MapCodec<StructureSpawnCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryCodecs.entryList(RegistryKeys.STRUCTURE).fieldOf("structures")).forGetter(StructureSpawnCondition::requiredStructures)).apply((Applicative<StructureSpawnCondition, ?>)instance, StructureSpawnCondition::new));

    @Override
    public boolean test(SpawnContext arg) {
        return arg.world().toServerWorld().getStructureAccessor().getStructureContaining(arg.pos(), this.requiredStructures).hasChildren();
    }

    public MapCodec<StructureSpawnCondition> getCodec() {
        return CODEC;
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((SpawnContext)context);
    }
}

