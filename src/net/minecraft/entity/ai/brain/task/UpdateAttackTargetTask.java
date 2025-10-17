/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

public class UpdateAttackTargetTask {
    public static <E extends MobEntity> Task<E> create(TargetGetter<E> targetGetter) {
        return UpdateAttackTargetTask.create((world, entity) -> true, targetGetter);
    }

    public static <E extends MobEntity> Task<E> create(StartCondition<E> condition, TargetGetter<E> targetGetter) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryOptional(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(context, (attackTarget, cantReachWalkTargetSince) -> (world, entity, time) -> {
            if (!condition.test(world, entity)) {
                return false;
            }
            Optional<LivingEntity> optional = targetGetter.get(world, entity);
            if (optional.isEmpty()) {
                return false;
            }
            LivingEntity lv = optional.get();
            if (!entity.canTarget(lv)) {
                return false;
            }
            attackTarget.remember(lv);
            cantReachWalkTargetSince.forget();
            return true;
        }));
    }

    @FunctionalInterface
    public static interface StartCondition<E> {
        public boolean test(ServerWorld var1, E var2);
    }

    @FunctionalInterface
    public static interface TargetGetter<E> {
        public Optional<? extends LivingEntity> get(ServerWorld var1, E var2);
    }
}

