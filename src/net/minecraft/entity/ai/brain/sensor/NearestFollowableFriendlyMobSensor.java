/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestVisibleAdultSensor;
import net.minecraft.registry.tag.EntityTypeTags;

public class NearestFollowableFriendlyMobSensor
extends NearestVisibleAdultSensor {
    @Override
    protected void find(LivingEntity entity, LivingTargetCache targetCache) {
        Optional<LivingEntity> optional = targetCache.findFirst(potentialFriend -> potentialFriend.getType().isIn(EntityTypeTags.FOLLOWABLE_FRIENDLY_MOBS) && !potentialFriend.isBaby()).map(LivingEntity.class::cast);
        entity.getBrain().remember(MemoryModuleType.NEAREST_VISIBLE_ADULT, optional);
    }
}

