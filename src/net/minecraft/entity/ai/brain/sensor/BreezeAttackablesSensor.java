/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestLivingEntitiesSensor;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;

public class BreezeAttackablesSensor
extends NearestLivingEntitiesSensor<BreezeEntity> {
    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.copyOf(Iterables.concat(super.getOutputMemoryModules(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
    }

    @Override
    protected void sense(ServerWorld arg, BreezeEntity arg2) {
        super.sense(arg, arg2);
        arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.MOBS).stream().flatMap(Collection::stream).filter(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).filter(target -> Sensor.testAttackableTargetPredicate(arg, arg2, target)).findFirst().ifPresentOrElse(target -> arg2.getBrain().remember(MemoryModuleType.NEAREST_ATTACKABLE, target), () -> arg2.getBrain().forget(MemoryModuleType.NEAREST_ATTACKABLE));
    }
}

