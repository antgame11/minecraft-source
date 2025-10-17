/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.goal.ChaseBoatState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ChaseBoatGoal
extends Goal {
    private int updateCountdownTicks;
    private final PathAwareEntity mob;
    @Nullable
    private PlayerEntity passenger;
    private ChaseBoatState state;

    public ChaseBoatGoal(PathAwareEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        List<AbstractBoatEntity> list = this.mob.getEntityWorld().getNonSpectatingEntities(AbstractBoatEntity.class, this.mob.getBoundingBox().expand(5.0));
        boolean bl = false;
        for (AbstractBoatEntity lv : list) {
            LivingEntity lv2 = lv.getControllingPassenger();
            if (!(lv2 instanceof PlayerEntity)) continue;
            PlayerEntity lv3 = (PlayerEntity)lv2;
            if (!(MathHelper.abs(lv3.sidewaysSpeed) > 0.0f) && !(MathHelper.abs(lv3.forwardSpeed) > 0.0f)) continue;
            bl = true;
            break;
        }
        return this.passenger != null && (MathHelper.abs(this.passenger.sidewaysSpeed) > 0.0f || MathHelper.abs(this.passenger.forwardSpeed) > 0.0f) || bl;
    }

    @Override
    public boolean canStop() {
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return this.passenger != null && this.passenger.hasVehicle() && (MathHelper.abs(this.passenger.sidewaysSpeed) > 0.0f || MathHelper.abs(this.passenger.forwardSpeed) > 0.0f);
    }

    @Override
    public void start() {
        List<AbstractBoatEntity> list = this.mob.getEntityWorld().getNonSpectatingEntities(AbstractBoatEntity.class, this.mob.getBoundingBox().expand(5.0));
        for (AbstractBoatEntity lv : list) {
            PlayerEntity lv2;
            LivingEntity livingEntity = lv.getControllingPassenger();
            if (!(livingEntity instanceof PlayerEntity)) continue;
            this.passenger = lv2 = (PlayerEntity)livingEntity;
            break;
        }
        this.updateCountdownTicks = 0;
        this.state = ChaseBoatState.GO_TO_BOAT;
    }

    @Override
    public void stop() {
        this.passenger = null;
    }

    @Override
    public void tick() {
        boolean bl;
        boolean bl2 = bl = MathHelper.abs(this.passenger.sidewaysSpeed) > 0.0f || MathHelper.abs(this.passenger.forwardSpeed) > 0.0f;
        float f = this.state == ChaseBoatState.GO_IN_BOAT_DIRECTION ? (bl ? 0.01f : 0.0f) : 0.015f;
        this.mob.updateVelocity(f, new Vec3d(this.mob.sidewaysSpeed, this.mob.upwardSpeed, this.mob.forwardSpeed));
        this.mob.move(MovementType.SELF, this.mob.getVelocity());
        if (--this.updateCountdownTicks > 0) {
            return;
        }
        this.updateCountdownTicks = this.getTickCount(10);
        if (this.state == ChaseBoatState.GO_TO_BOAT) {
            BlockPos lv = this.passenger.getBlockPos().offset(this.passenger.getHorizontalFacing().getOpposite());
            lv = lv.add(0, -1, 0);
            this.mob.getNavigation().startMovingTo(lv.getX(), lv.getY(), lv.getZ(), 1.0);
            if (this.mob.distanceTo(this.passenger) < 4.0f) {
                this.updateCountdownTicks = 0;
                this.state = ChaseBoatState.GO_IN_BOAT_DIRECTION;
            }
        } else if (this.state == ChaseBoatState.GO_IN_BOAT_DIRECTION) {
            Direction lv2 = this.passenger.getMovementDirection();
            BlockPos lv3 = this.passenger.getBlockPos().offset(lv2, 10);
            this.mob.getNavigation().startMovingTo(lv3.getX(), lv3.getY() - 1, lv3.getZ(), 1.0);
            if (this.mob.distanceTo(this.passenger) > 12.0f) {
                this.updateCountdownTicks = 0;
                this.state = ChaseBoatState.GO_TO_BOAT;
            }
        }
    }
}

