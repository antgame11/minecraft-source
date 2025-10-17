/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.Set;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.FleeTask;
import net.minecraft.entity.ai.brain.task.GoToLookTargetTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TemptTask;
import net.minecraft.entity.ai.brain.task.TickCooldownTask;
import net.minecraft.entity.ai.brain.task.UpdateLookControlTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsEntityTask;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class HappyGhastBrain {
    private static final float field_59695 = 1.0f;
    private static final float TEMPT_SPEED = 1.25f;
    private static final float MOVE_TOWARDS_FRIENDLY_ENTITY_SPEED = 1.1f;
    private static final double field_59698 = 3.0;
    private static final UniformIntProvider MOVE_TOWARDS_FRIENDLY_ENTITY_RANGE = UniformIntProvider.create(3, 16);
    private static final ImmutableList<SensorType<? extends Sensor<? super HappyGhastEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.HAPPY_GHAST_TEMPTATIONS, SensorType.NEAREST_ADULT_ANY_TYPE, SensorType.NEAREST_PLAYERS);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.BREED_TARGET, MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_VISIBLE_ADULT, new MemoryModuleType[]{MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYERS});

    public static Brain.Profile<HappyGhastEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    protected static Brain<?> create(Brain<HappyGhastEntity> brain) {
        HappyGhastBrain.addCoreActivities(brain);
        HappyGhastBrain.addIdleActivities(brain);
        HappyGhastBrain.addPanicActivities(brain);
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<HappyGhastEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new StayAboveWaterTask(0.8f), new FleeTask(2.0f, 0), new UpdateLookControlTask(45, 90), new MoveToTargetTask(), new TickCooldownTask(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
    }

    private static void addIdleActivities(Brain<HappyGhastEntity> brain) {
        brain.setTaskList(Activity.IDLE, ImmutableList.of(Pair.of(1, new TemptTask(entity -> Float.valueOf(1.25f), entity -> 3.0, true)), Pair.of(2, WalkTowardsEntityTask.create(MOVE_TOWARDS_FRIENDLY_ENTITY_RANGE, player -> Float.valueOf(1.1f), MemoryModuleType.NEAREST_VISIBLE_PLAYER, true)), Pair.of(3, WalkTowardsEntityTask.create(MOVE_TOWARDS_FRIENDLY_ENTITY_RANGE, adult -> Float.valueOf(1.1f), MemoryModuleType.NEAREST_VISIBLE_ADULT, true)), Pair.of(4, new RandomTask(ImmutableList.of(Pair.of(StrollTask.createSolidTargeting(1.0f), 1), Pair.of(GoToLookTargetTask.create(1.0f, 3), 1))))));
    }

    private static void addPanicActivities(Brain<HappyGhastEntity> brain) {
        brain.setTaskList(Activity.PANIC, ImmutableList.of(), Set.of(Pair.of(MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_PRESENT)));
    }

    public static void updateActivities(HappyGhastEntity happyGhast) {
        happyGhast.getBrain().resetPossibleActivities(ImmutableList.of(Activity.PANIC, Activity.IDLE));
    }
}

