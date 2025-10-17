/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.poi.PointOfInterestType;

public class ForgetCompletedPointOfInterestTask {
    private static final int MAX_RANGE = 16;

    public static Task<LivingEntity> create(Predicate<RegistryEntry<PointOfInterestType>> poiTypePredicate, MemoryModuleType<GlobalPos> poiPosModule) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(poiPosModule)).apply(context, poiPos -> (world, entity, time) -> {
            GlobalPos lv = (GlobalPos)context.getValue(poiPos);
            BlockPos lv2 = lv.pos();
            if (world.getRegistryKey() != lv.dimension() || !lv2.isWithinDistance(entity.getEntityPos(), 16.0)) {
                return false;
            }
            ServerWorld lv3 = world.getServer().getWorld(lv.dimension());
            if (lv3 == null || !lv3.getPointOfInterestStorage().test(lv2, poiTypePredicate)) {
                poiPos.forget();
            } else if (ForgetCompletedPointOfInterestTask.isBedOccupiedByOthers(lv3, lv2, entity)) {
                poiPos.forget();
                if (!ForgetCompletedPointOfInterestTask.isSleepingVillagerAt(lv3, lv2)) {
                    world.getPointOfInterestStorage().releaseTicket(lv2);
                    world.getSubscriptionTracker().onPoiUpdated(lv2);
                }
            }
            return true;
        }));
    }

    private static boolean isBedOccupiedByOthers(ServerWorld world, BlockPos pos, LivingEntity entity) {
        BlockState lv = world.getBlockState(pos);
        return lv.isIn(BlockTags.BEDS) && lv.get(BedBlock.OCCUPIED) != false && !entity.isSleeping();
    }

    private static boolean isSleepingVillagerAt(ServerWorld world, BlockPos pos) {
        List<VillagerEntity> list = world.getEntitiesByClass(VillagerEntity.class, new Box(pos), LivingEntity::isSleeping);
        return !list.isEmpty();
    }
}

