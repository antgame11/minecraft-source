/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;

public class TemptationsSensor
extends Sensor<PathAwareEntity> {
    private static final TargetPredicate TEMPTER_PREDICATE = TargetPredicate.createNonAttackable().ignoreVisibility();
    private final Predicate<ItemStack> predicate;

    public TemptationsSensor(Predicate<ItemStack> predicate) {
        this.predicate = predicate;
    }

    @Override
    protected void sense(ServerWorld arg, PathAwareEntity arg2) {
        Brain<?> lv = arg2.getBrain();
        TargetPredicate lv2 = TEMPTER_PREDICATE.copy().setBaseMaxDistance((float)arg2.getAttributeValue(EntityAttributes.TEMPT_RANGE));
        List list = arg.getPlayers().stream().filter(EntityPredicates.EXCEPT_SPECTATOR).filter(player -> lv2.test(arg, arg2, (LivingEntity)player)).filter(this::test).filter(playerx -> !arg2.hasPassenger((Entity)playerx)).sorted(Comparator.comparingDouble(arg2::squaredDistanceTo)).collect(Collectors.toList());
        if (!list.isEmpty()) {
            PlayerEntity lv3 = (PlayerEntity)list.get(0);
            lv.remember(MemoryModuleType.TEMPTING_PLAYER, lv3);
        } else {
            lv.forget(MemoryModuleType.TEMPTING_PLAYER);
        }
    }

    private boolean test(PlayerEntity player) {
        return this.test(player.getMainHandStack()) || this.test(player.getOffHandStack());
    }

    private boolean test(ItemStack stack) {
        return this.predicate.test(stack);
    }

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
    }
}

