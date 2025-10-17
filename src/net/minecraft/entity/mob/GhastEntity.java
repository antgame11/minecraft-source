/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class GhastEntity
extends MobEntity
implements Monster {
    private static final TrackedData<Boolean> SHOOTING = DataTracker.registerData(GhastEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final byte DEFAULT_FIREBALL_STRENGTH = 1;
    private int fireballStrength = 1;

    public GhastEntity(EntityType<? extends GhastEntity> arg, World arg2) {
        super((EntityType<? extends MobEntity>)arg, arg2);
        this.experiencePoints = 5;
        this.moveControl = new GhastMoveControl(this, false, () -> false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(5, new FlyRandomlyGoal(this));
        this.goalSelector.add(7, new LookAtTargetGoal(this));
        this.goalSelector.add(7, new ShootFireballGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, (entity, world) -> Math.abs(entity.getY() - this.getY()) <= 4.0));
    }

    public boolean isShooting() {
        return this.dataTracker.get(SHOOTING);
    }

    public void setShooting(boolean shooting) {
        this.dataTracker.set(SHOOTING, shooting);
    }

    public int getFireballStrength() {
        return this.fireballStrength;
    }

    private static boolean isFireballFromPlayer(DamageSource damageSource) {
        return damageSource.getSource() instanceof FireballEntity && damageSource.getAttacker() instanceof PlayerEntity;
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        return this.isInvulnerable() && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || !GhastEntity.isFireballFromPlayer(source) && super.isInvulnerableTo(world, source);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public void travel(Vec3d movementInput) {
        this.travelFlying(movementInput, 0.02f);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (GhastEntity.isFireballFromPlayer(source)) {
            super.damage(world, source, 1000.0f);
            return true;
        }
        if (this.isInvulnerableTo(world, source)) {
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SHOOTING, false);
    }

    public static DefaultAttributeContainer.Builder createGhastAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.MAX_HEALTH, 10.0).add(EntityAttributes.FOLLOW_RANGE, 100.0).add(EntityAttributes.CAMERA_DISTANCE, 8.0).add(EntityAttributes.FLYING_SPEED, 0.06);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GHAST_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0f;
    }

    public static boolean canSpawn(EntityType<GhastEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(20) == 0 && GhastEntity.canMobSpawn(type, world, spawnReason, pos, random);
    }

    @Override
    public int getLimitPerChunk() {
        return 1;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putByte("ExplosionPower", (byte)this.fireballStrength);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.fireballStrength = view.getByte("ExplosionPower", (byte)1);
    }

    @Override
    public boolean hasQuadLeashAttachmentPoints() {
        return true;
    }

    @Override
    public double getElasticLeashDistance() {
        return 10.0;
    }

    @Override
    public double getLeashSnappingDistance() {
        return 16.0;
    }

    public static void updateYaw(MobEntity ghast) {
        if (ghast.getTarget() == null) {
            Vec3d lv = ghast.getVelocity();
            ghast.setYaw(-((float)MathHelper.atan2(lv.x, lv.z)) * 57.295776f);
            ghast.bodyYaw = ghast.getYaw();
        } else {
            LivingEntity lv2 = ghast.getTarget();
            double d = 64.0;
            if (lv2.squaredDistanceTo(ghast) < 4096.0) {
                double e = lv2.getX() - ghast.getX();
                double f = lv2.getZ() - ghast.getZ();
                ghast.setYaw(-((float)MathHelper.atan2(e, f)) * 57.295776f);
                ghast.bodyYaw = ghast.getYaw();
            }
        }
    }

    public static class GhastMoveControl
    extends MoveControl {
        private final MobEntity ghast;
        private int collisionCheckCooldown;
        private final boolean happy;
        private final BooleanSupplier shouldStayStill;

        public GhastMoveControl(MobEntity ghast, boolean happy, BooleanSupplier shouldStayStill) {
            super(ghast);
            this.ghast = ghast;
            this.happy = happy;
            this.shouldStayStill = shouldStayStill;
        }

        @Override
        public void tick() {
            if (this.shouldStayStill.getAsBoolean()) {
                this.state = MoveControl.State.WAIT;
                this.ghast.stopMovement();
            }
            if (this.state != MoveControl.State.MOVE_TO) {
                return;
            }
            if (this.collisionCheckCooldown-- <= 0) {
                this.collisionCheckCooldown += this.ghast.getRandom().nextInt(5) + 2;
                Vec3d lv = new Vec3d(this.targetX - this.ghast.getX(), this.targetY - this.ghast.getY(), this.targetZ - this.ghast.getZ());
                if (this.willCollide(lv)) {
                    this.ghast.setVelocity(this.ghast.getVelocity().add(lv.normalize().multiply(this.ghast.getAttributeValue(EntityAttributes.FLYING_SPEED) * 5.0 / 3.0)));
                } else {
                    this.state = MoveControl.State.WAIT;
                }
            }
        }

        private boolean willCollide(Vec3d movement) {
            Box lv = this.ghast.getBoundingBox();
            Box lv2 = lv.offset(movement);
            if (this.happy) {
                for (BlockPos lv3 : BlockPos.iterate(lv2.expand(1.0))) {
                    if (this.canPassThrough(this.ghast.getEntityWorld(), null, null, lv3, false, false)) continue;
                    return false;
                }
            }
            boolean bl = this.ghast.isTouchingWater();
            boolean bl2 = this.ghast.isInLava();
            Vec3d lv4 = this.ghast.getEntityPos();
            Vec3d lv5 = lv4.add(movement);
            return BlockView.collectCollisionsBetween(lv4, lv5, lv2, (pos, version) -> {
                if (lv.contains(pos)) {
                    return true;
                }
                return this.canPassThrough(this.ghast.getEntityWorld(), lv4, lv5, pos, bl, bl2);
            });
        }

        private boolean canPassThrough(BlockView world, @Nullable Vec3d oldPos, @Nullable Vec3d newPos, BlockPos blockPos, boolean waterAllowed, boolean lavaAllowed) {
            boolean bl4;
            boolean bl3;
            BlockState lv = world.getBlockState(blockPos);
            if (lv.isAir()) {
                return true;
            }
            boolean bl = bl3 = oldPos != null && newPos != null;
            boolean bl2 = bl3 ? !this.ghast.collides(oldPos, newPos, lv.getCollisionShape(world, blockPos).offset(new Vec3d(blockPos)).getBoundingBoxes()) : (bl4 = lv.getCollisionShape(world, blockPos).isEmpty());
            if (!this.happy) {
                return bl4;
            }
            if (lv.isIn(BlockTags.HAPPY_GHAST_AVOIDS)) {
                return false;
            }
            FluidState lv2 = world.getFluidState(blockPos);
            if (!(lv2.isEmpty() || bl3 && !this.ghast.collidesWithFluid(lv2, blockPos, oldPos, newPos))) {
                if (lv2.isIn(FluidTags.WATER)) {
                    return waterAllowed;
                }
                if (lv2.isIn(FluidTags.LAVA)) {
                    return lavaAllowed;
                }
            }
            return bl4;
        }
    }

    public static class FlyRandomlyGoal
    extends Goal {
        private static final int field_59707 = 64;
        private final MobEntity ghast;
        private final int blockCheckDistance;

        public FlyRandomlyGoal(MobEntity ghast) {
            this(ghast, 0);
        }

        public FlyRandomlyGoal(MobEntity ghast, int blockCheckDistance) {
            this.ghast = ghast;
            this.blockCheckDistance = blockCheckDistance;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            double f;
            double e;
            MoveControl lv = this.ghast.getMoveControl();
            if (!lv.isMoving()) {
                return true;
            }
            double d = lv.getTargetX() - this.ghast.getX();
            double g = d * d + (e = lv.getTargetY() - this.ghast.getY()) * e + (f = lv.getTargetZ() - this.ghast.getZ()) * f;
            return g < 1.0 || g > 3600.0;
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            Vec3d lv = FlyRandomlyGoal.locateTarget(this.ghast, this.blockCheckDistance);
            this.ghast.getMoveControl().moveTo(lv.getX(), lv.getY(), lv.getZ(), 1.0);
        }

        public static Vec3d locateTarget(MobEntity ghast, int blockCheckDistance) {
            BlockPos lv5;
            int k;
            World lv = ghast.getEntityWorld();
            Random lv2 = ghast.getRandom();
            Vec3d lv3 = ghast.getEntityPos();
            Vec3d lv4 = null;
            for (int j = 0; j < 64; ++j) {
                lv4 = FlyRandomlyGoal.getTargetPos(ghast, lv3, lv2);
                if (lv4 == null || !FlyRandomlyGoal.isTargetValid(lv, lv4, blockCheckDistance)) continue;
                return lv4;
            }
            if (lv4 == null) {
                lv4 = FlyRandomlyGoal.addRandom(lv3, lv2);
            }
            if ((k = lv.getTopY(Heightmap.Type.MOTION_BLOCKING, (lv5 = BlockPos.ofFloored(lv4)).getX(), lv5.getZ())) < lv5.getY() && k > lv.getBottomY()) {
                lv4 = new Vec3d(lv4.getX(), ghast.getY() - Math.abs(ghast.getY() - lv4.getY()), lv4.getZ());
            }
            return lv4;
        }

        private static boolean isTargetValid(World world, Vec3d pos, int blockCheckDistance) {
            if (blockCheckDistance <= 0) {
                return true;
            }
            BlockPos lv = BlockPos.ofFloored(pos);
            if (!world.getBlockState(lv).isAir()) {
                return false;
            }
            for (Direction lv2 : Direction.values()) {
                for (int j = 1; j < blockCheckDistance; ++j) {
                    BlockPos lv3 = lv.offset(lv2, j);
                    if (world.getBlockState(lv3).isAir()) continue;
                    return true;
                }
            }
            return false;
        }

        private static Vec3d addRandom(Vec3d pos, Random random) {
            double d = pos.getX() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            double e = pos.getY() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            double f = pos.getZ() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            return new Vec3d(d, e, f);
        }

        @Nullable
        private static Vec3d getTargetPos(MobEntity ghast, Vec3d pos, Random random) {
            Vec3d lv = FlyRandomlyGoal.addRandom(pos, random);
            if (ghast.hasPositionTarget() && !ghast.isInPositionTargetRange(lv)) {
                return null;
            }
            return lv;
        }
    }

    public static class LookAtTargetGoal
    extends Goal {
        private final MobEntity ghast;

        public LookAtTargetGoal(MobEntity ghast) {
            this.ghast = ghast;
            this.setControls(EnumSet.of(Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            GhastEntity.updateYaw(this.ghast);
        }
    }

    static class ShootFireballGoal
    extends Goal {
        private final GhastEntity ghast;
        public int cooldown;

        public ShootFireballGoal(GhastEntity ghast) {
            this.ghast = ghast;
        }

        @Override
        public boolean canStart() {
            return this.ghast.getTarget() != null;
        }

        @Override
        public void start() {
            this.cooldown = 0;
        }

        @Override
        public void stop() {
            this.ghast.setShooting(false);
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity lv = this.ghast.getTarget();
            if (lv == null) {
                return;
            }
            double d = 64.0;
            if (lv.squaredDistanceTo(this.ghast) < 4096.0 && this.ghast.canSee(lv)) {
                World lv2 = this.ghast.getEntityWorld();
                ++this.cooldown;
                if (this.cooldown == 10 && !this.ghast.isSilent()) {
                    lv2.syncWorldEvent(null, WorldEvents.GHAST_WARNS, this.ghast.getBlockPos(), 0);
                }
                if (this.cooldown == 20) {
                    double e = 4.0;
                    Vec3d lv3 = this.ghast.getRotationVec(1.0f);
                    double f = lv.getX() - (this.ghast.getX() + lv3.x * 4.0);
                    double g = lv.getBodyY(0.5) - (0.5 + this.ghast.getBodyY(0.5));
                    double h = lv.getZ() - (this.ghast.getZ() + lv3.z * 4.0);
                    Vec3d lv4 = new Vec3d(f, g, h);
                    if (!this.ghast.isSilent()) {
                        lv2.syncWorldEvent(null, WorldEvents.GHAST_SHOOTS, this.ghast.getBlockPos(), 0);
                    }
                    FireballEntity lv5 = new FireballEntity(lv2, (LivingEntity)this.ghast, lv4.normalize(), this.ghast.getFireballStrength());
                    lv5.setPosition(this.ghast.getX() + lv3.x * 4.0, this.ghast.getBodyY(0.5) + 0.5, lv5.getZ() + lv3.z * 4.0);
                    lv2.spawnEntity(lv5);
                    this.cooldown = -40;
                }
            } else if (this.cooldown > 0) {
                --this.cooldown;
            }
            this.ghast.setShooting(this.cooldown > 10);
        }
    }
}

