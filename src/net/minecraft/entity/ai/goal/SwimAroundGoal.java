/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SwimAroundGoal
extends WanderAroundGoal {
    public SwimAroundGoal(PathAwareEntity arg, double d, int i) {
        super(arg, d, i);
    }

    @Override
    @Nullable
    protected Vec3d getWanderTarget() {
        return TargetUtil.find(this.mob, 10, 7);
    }
}

