/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.function.Function;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class WalkTowardsEntityTask {
    public static SingleTickTask<LivingEntity> createNearestVisibleAdult(UniformIntProvider executionRange, float speed) {
        return WalkTowardsEntityTask.create(executionRange, entity -> Float.valueOf(speed), MemoryModuleType.NEAREST_VISIBLE_ADULT, false);
    }

    public static SingleTickTask<LivingEntity> create(UniformIntProvider executionRange, Function<LivingEntity, Float> speed, MemoryModuleType<? extends LivingEntity> targetType, boolean eyeHeight) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(targetType), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, (target, lookTarget, walkTarget) -> (world, entity, time) -> {
            if (!entity.isBaby()) {
                return false;
            }
            LivingEntity lv = (LivingEntity)context.getValue(target);
            if (entity.isInRange(lv, executionRange.getMax() + 1) && !entity.isInRange(lv, executionRange.getMin())) {
                WalkTarget lv2 = new WalkTarget(new EntityLookTarget(lv, eyeHeight, eyeHeight), ((Float)speed.apply(entity)).floatValue(), executionRange.getMin() - 1);
                lookTarget.remember(new EntityLookTarget(lv, true, eyeHeight));
                walkTarget.remember(lv2);
                return true;
            }
            return false;
        }));
    }
}

