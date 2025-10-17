/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class WalkTowardsFuzzyPosTask {
    private static BlockPos fuzz(MobEntity mob, BlockPos pos) {
        Random lv = mob.getEntityWorld().random;
        return pos.add(WalkTowardsFuzzyPosTask.fuzz(lv), 0, WalkTowardsFuzzyPosTask.fuzz(lv));
    }

    private static int fuzz(Random random) {
        return random.nextInt(3) - 1;
    }

    public static <E extends MobEntity> SingleTickTask<E> create(MemoryModuleType<BlockPos> posModule, int completionRange, float speed) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(posModule), context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET)).apply(context, (pos, attackTarget, walkTarget, lookTarget) -> (world, entity, time) -> {
            BlockPos lv = (BlockPos)context.getValue(pos);
            boolean bl = lv.isWithinDistance(entity.getBlockPos(), (double)completionRange);
            if (!bl) {
                TargetUtil.walkTowards(entity, WalkTowardsFuzzyPosTask.fuzz(entity, lv), speed, completionRange);
            }
            return true;
        }));
    }
}

