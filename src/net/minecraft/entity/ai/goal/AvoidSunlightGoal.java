/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.mob.PathAwareEntity;

public class AvoidSunlightGoal
extends Goal {
    private final PathAwareEntity mob;

    public AvoidSunlightGoal(PathAwareEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        return this.mob.getEntityWorld().isDay() && this.mob.getEquippedStack(EquipmentSlot.HEAD).isEmpty() && NavigationConditions.hasMobNavigation(this.mob);
    }

    @Override
    public void start() {
        EntityNavigation entityNavigation = this.mob.getNavigation();
        if (entityNavigation instanceof MobNavigation) {
            MobNavigation lv = (MobNavigation)entityNavigation;
            lv.setAvoidSunlight(true);
        }
    }

    @Override
    public void stop() {
        EntityNavigation entityNavigation;
        if (NavigationConditions.hasMobNavigation(this.mob) && (entityNavigation = this.mob.getNavigation()) instanceof MobNavigation) {
            MobNavigation lv = (MobNavigation)entityNavigation;
            lv.setAvoidSunlight(false);
        }
    }
}

