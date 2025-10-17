/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class PathAwareEntity
extends MobEntity {
    protected static final float DEFAULT_PATHFINDING_FAVOR = 0.0f;

    protected PathAwareEntity(EntityType<? extends PathAwareEntity> arg, World arg2) {
        super((EntityType<? extends MobEntity>)arg, arg2);
    }

    public float getPathfindingFavor(BlockPos pos) {
        return this.getPathfindingFavor(pos, this.getEntityWorld());
    }

    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return 0.0f;
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return this.getPathfindingFavor(this.getBlockPos(), world) >= 0.0f;
    }

    public boolean isNavigating() {
        return !this.getNavigation().isIdle();
    }

    public boolean isPanicking() {
        if (this.brain.hasMemoryModule(MemoryModuleType.IS_PANICKING)) {
            return this.brain.getOptionalRegisteredMemory(MemoryModuleType.IS_PANICKING).isPresent();
        }
        for (PrioritizedGoal lv : this.goalSelector.getGoals()) {
            if (!lv.isRunning() || !(lv.getGoal() instanceof EscapeDangerGoal)) continue;
            return true;
        }
        return false;
    }

    protected boolean shouldFollowLeash() {
        return true;
    }

    @Override
    public void onShortLeashTick(Entity entity) {
        super.onShortLeashTick(entity);
        if (this.shouldFollowLeash() && !this.isPanicking()) {
            this.goalSelector.enableControl(Goal.Control.MOVE);
            float f = 2.0f;
            float g = this.distanceTo(entity);
            Vec3d lv = new Vec3d(entity.getX() - this.getX(), entity.getY() - this.getY(), entity.getZ() - this.getZ()).normalize().multiply(Math.max(g - 2.0f, 0.0f));
            this.getNavigation().startMovingTo(this.getX() + lv.x, this.getY() + lv.y, this.getZ() + lv.z, this.getFollowLeashSpeed());
        }
    }

    @Override
    public void beforeLeashTick(Entity leashHolder) {
        this.setPositionTarget(leashHolder.getBlockPos(), (int)this.getElasticLeashDistance() - 1);
        super.beforeLeashTick(leashHolder);
    }

    protected double getFollowLeashSpeed() {
        return 1.0;
    }
}

