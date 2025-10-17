/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.BirdPathNodeMaker;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BirdNavigation
extends EntityNavigation {
    public BirdNavigation(MobEntity arg, World arg2) {
        super(arg, arg2);
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new BirdPathNodeMaker();
        return new PathNodeNavigator(this.nodeMaker, range);
    }

    @Override
    protected boolean canPathDirectlyThrough(Vec3d origin, Vec3d target) {
        return BirdNavigation.doesNotCollide(this.entity, origin, target, true);
    }

    @Override
    protected boolean isAtValidPosition() {
        return this.canSwim() && this.entity.isInFluid() || !this.entity.hasVehicle();
    }

    @Override
    protected Vec3d getPos() {
        return this.entity.getEntityPos();
    }

    @Override
    public Path findPathTo(Entity entity, int distance) {
        return this.findPathTo(entity.getBlockPos(), distance);
    }

    @Override
    public void tick() {
        Vec3d lv;
        ++this.tickCount;
        if (this.inRecalculationCooldown) {
            this.recalculatePath();
        }
        if (this.isIdle()) {
            return;
        }
        if (this.isAtValidPosition()) {
            this.continueFollowingPath();
        } else if (this.currentPath != null && !this.currentPath.isFinished()) {
            lv = this.currentPath.getNodePosition(this.entity);
            if (this.entity.getBlockX() == MathHelper.floor(lv.x) && this.entity.getBlockY() == MathHelper.floor(lv.y) && this.entity.getBlockZ() == MathHelper.floor(lv.z)) {
                this.currentPath.next();
            }
        }
        if (this.isIdle()) {
            return;
        }
        lv = this.currentPath.getNodePosition(this.entity);
        this.entity.getMoveControl().moveTo(lv.x, lv.y, lv.z, this.speed);
    }

    @Override
    public boolean isValidPosition(BlockPos pos) {
        return this.world.getBlockState(pos).hasSolidTopSurface(this.world, pos, this.entity);
    }

    @Override
    public boolean canControlOpeningDoors() {
        return false;
    }
}

