/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;

public class LoseJobOnSiteLossTask {
    public static Task<VillagerEntity> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.JOB_SITE)).apply(context, jobSite -> (world, entity, time) -> {
            boolean bl;
            VillagerData lv = entity.getVillagerData();
            boolean bl2 = bl = !lv.profession().matchesKey(VillagerProfession.NONE) && !lv.profession().matchesKey(VillagerProfession.NITWIT);
            if (bl && entity.getExperience() == 0 && lv.level() <= 1) {
                entity.setVillagerData(entity.getVillagerData().withProfession(world.getRegistryManager(), VillagerProfession.NONE));
                entity.reinitializeBrain(world);
                return true;
            }
            return false;
        }));
    }
}

