/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CamelBrain;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class CamelEntity
extends AbstractHorseEntity {
    public static final float field_45127 = 0.45f;
    public static final int field_40132 = 55;
    public static final int field_41764 = 30;
    private static final float field_40146 = 0.1f;
    private static final float field_40147 = 1.4285f;
    private static final float field_40148 = 22.2222f;
    private static final int field_43388 = 5;
    private static final int field_40149 = 40;
    private static final int field_40133 = 52;
    private static final int field_40134 = 80;
    private static final float field_40135 = 1.43f;
    private static final long DEFAULT_LAST_POSE_TICK = 0L;
    public static final TrackedData<Boolean> DASHING = DataTracker.registerData(CamelEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Long> LAST_POSE_TICK = DataTracker.registerData(CamelEntity.class, TrackedDataHandlerRegistry.LONG);
    public final AnimationState sittingTransitionAnimationState = new AnimationState();
    public final AnimationState sittingAnimationState = new AnimationState();
    public final AnimationState standingTransitionAnimationState = new AnimationState();
    public final AnimationState idlingAnimationState = new AnimationState();
    public final AnimationState dashingAnimationState = new AnimationState();
    private static final EntityDimensions SITTING_DIMENSIONS = EntityDimensions.changing(EntityType.CAMEL.getWidth(), EntityType.CAMEL.getHeight() - 1.43f).withEyeHeight(0.845f);
    private int dashCooldown = 0;
    private int idleAnimationCooldown = 0;

    public CamelEntity(EntityType<? extends CamelEntity> arg, World arg2) {
        super((EntityType<? extends AbstractHorseEntity>)arg, arg2);
        this.moveControl = new CamelMoveControl();
        this.lookControl = new CamelLookControl();
        MobNavigation lv = (MobNavigation)this.getNavigation();
        lv.setCanSwim(true);
        lv.setCanWalkOverFences(true);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putLong("LastPoseTick", this.dataTracker.get(LAST_POSE_TICK));
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        long l = view.getLong("LastPoseTick", 0L);
        if (l < 0L) {
            this.setPose(EntityPose.SITTING);
        }
        this.setLastPoseTick(l);
    }

    public static DefaultAttributeContainer.Builder createCamelAttributes() {
        return CamelEntity.createBaseHorseAttributes().add(EntityAttributes.MAX_HEALTH, 32.0).add(EntityAttributes.MOVEMENT_SPEED, 0.09f).add(EntityAttributes.JUMP_STRENGTH, 0.42f).add(EntityAttributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DASHING, false);
        builder.add(LAST_POSE_TICK, 0L);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        CamelBrain.initialize(this, world.getRandom());
        this.initLastPoseTick(world.toServerWorld().getTime());
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public static boolean canSpawn(EntityType<CamelEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.CAMELS_SPAWNABLE_ON) && CamelEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    protected Brain.Profile<CamelEntity> createBrainProfile() {
        return CamelBrain.createBrainProfile();
    }

    @Override
    protected void initGoals() {
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return CamelBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return pose == EntityPose.SITTING ? SITTING_DIMENSIONS.scaled(this.getScaleFactor()) : super.getBaseDimensions(pose);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Profiler lv = Profilers.get();
        lv.push("camelBrain");
        Brain<?> lv2 = this.getBrain();
        lv2.tick(world, this);
        lv.pop();
        lv.push("camelActivityUpdate");
        CamelBrain.updateActivities(this);
        lv.pop();
        super.mobTick(world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isDashing() && this.dashCooldown < 50 && (this.isOnGround() || this.isInFluid() || this.hasVehicle())) {
            this.setDashing(false);
        }
        if (this.dashCooldown > 0) {
            --this.dashCooldown;
            if (this.dashCooldown == 0) {
                this.getEntityWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_CAMEL_DASH_READY, SoundCategory.NEUTRAL, 1.0f, 1.0f);
            }
        }
        if (this.getEntityWorld().isClient()) {
            this.updateAnimations();
        }
        if (this.isStationary()) {
            this.clampHeadYaw();
        }
        if (this.isSitting() && this.isTouchingWater()) {
            this.setStanding();
        }
    }

    private void updateAnimations() {
        if (this.idleAnimationCooldown <= 0) {
            this.idleAnimationCooldown = this.random.nextInt(40) + 80;
            this.idlingAnimationState.start(this.age);
        } else {
            --this.idleAnimationCooldown;
        }
        if (this.shouldUpdateSittingAnimations()) {
            this.standingTransitionAnimationState.stop();
            this.dashingAnimationState.stop();
            if (this.shouldPlaySittingTransitionAnimation()) {
                this.sittingTransitionAnimationState.startIfNotRunning(this.age);
                this.sittingAnimationState.stop();
            } else {
                this.sittingTransitionAnimationState.stop();
                this.sittingAnimationState.startIfNotRunning(this.age);
            }
        } else {
            this.sittingTransitionAnimationState.stop();
            this.sittingAnimationState.stop();
            this.dashingAnimationState.setRunning(this.isDashing(), this.age);
            this.standingTransitionAnimationState.setRunning(this.isChangingPose() && this.getTimeSinceLastPoseTick() >= 0L, this.age);
        }
    }

    @Override
    protected void updateLimbs(float posDelta) {
        float g = this.getPose() == EntityPose.STANDING && !this.dashingAnimationState.isRunning() ? Math.min(posDelta * 6.0f, 1.0f) : 0.0f;
        this.limbAnimator.updateLimbs(g, 0.2f, this.isBaby() ? 3.0f : 1.0f);
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isStationary() && this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.0, 1.0, 0.0));
            movementInput = movementInput.multiply(0.0, 1.0, 0.0);
        }
        super.travel(movementInput);
    }

    @Override
    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);
        if (controllingPlayer.forwardSpeed > 0.0f && this.isSitting() && !this.isChangingPose()) {
            this.startStanding();
        }
    }

    public boolean isStationary() {
        return this.isSitting() || this.isChangingPose();
    }

    @Override
    protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
        float f = controllingPlayer.isSprinting() && this.getJumpCooldown() == 0 ? 0.1f : 0.0f;
        return (float)this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) + f;
    }

    @Override
    protected Vec2f getControlledRotation(LivingEntity controllingPassenger) {
        if (this.isStationary()) {
            return new Vec2f(this.getPitch(), this.getYaw());
        }
        return super.getControlledRotation(controllingPassenger);
    }

    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        if (this.isStationary()) {
            return Vec3d.ZERO;
        }
        return super.getControlledMovementInput(controllingPlayer, movementInput);
    }

    @Override
    public boolean canJump() {
        return !this.isStationary() && super.canJump();
    }

    @Override
    public void setJumpStrength(int strength) {
        if (!this.hasSaddleEquipped() || this.dashCooldown > 0 || !this.isOnGround()) {
            return;
        }
        super.setJumpStrength(strength);
    }

    @Override
    public boolean canSprintAsVehicle() {
        return true;
    }

    @Override
    protected void jump(float strength, Vec3d movementInput) {
        double d = this.getJumpVelocity();
        this.addVelocityInternal(this.getRotationVector().multiply(1.0, 0.0, 1.0).normalize().multiply((double)(22.2222f * strength) * this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) * (double)this.getVelocityMultiplier()).add(0.0, (double)(1.4285f * strength) * d, 0.0));
        this.dashCooldown = 55;
        this.setDashing(true);
        this.velocityDirty = true;
    }

    public boolean isDashing() {
        return this.dataTracker.get(DASHING);
    }

    public void setDashing(boolean dashing) {
        this.dataTracker.set(DASHING, dashing);
    }

    @Override
    public void startJumping(int height) {
        this.playSound(SoundEvents.ENTITY_CAMEL_DASH);
        this.emitGameEvent(GameEvent.ENTITY_ACTION);
        this.setDashing(true);
    }

    @Override
    public void stopJumping() {
    }

    @Override
    public int getJumpCooldown() {
        return this.dashCooldown;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_CAMEL_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CAMEL_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_CAMEL_HURT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (state.isIn(BlockTags.CAMEL_SAND_STEP_SOUND_BLOCKS)) {
            this.playSound(SoundEvents.ENTITY_CAMEL_STEP_SAND, 1.0f, 1.0f);
        } else {
            this.playSound(SoundEvents.ENTITY_CAMEL_STEP, 1.0f, 1.0f);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.CAMEL_FOOD);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (player.shouldCancelInteraction() && !this.isBaby()) {
            this.openInventory(player);
            return ActionResult.SUCCESS;
        }
        ActionResult lv2 = lv.useOnEntity(player, this, hand);
        if (lv2.isAccepted()) {
            return lv2;
        }
        if (this.isBreedingItem(lv)) {
            return this.interactHorse(player, lv);
        }
        if (this.getPassengerList().size() < 2 && !this.isBaby()) {
            this.putPlayerOnBack(player);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onLongLeashTick() {
        super.onLongLeashTick();
        if (this.isSitting() && !this.isChangingPose() && this.canChangePose()) {
            this.startStanding();
        }
    }

    @Override
    public Vec3d[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.02, 0.48, 0.25, 0.82);
    }

    public boolean canChangePose() {
        return this.wouldNotSuffocateInPose(this.isSitting() ? EntityPose.STANDING : EntityPose.SITTING);
    }

    @Override
    protected boolean receiveFood(PlayerEntity player, ItemStack item) {
        boolean bl3;
        boolean bl2;
        boolean bl;
        if (!this.isBreedingItem(item)) {
            return false;
        }
        boolean bl4 = bl = this.getHealth() < this.getMaxHealth();
        if (bl) {
            this.heal(2.0f);
        }
        boolean bl5 = bl2 = this.isTame() && this.getBreedingAge() == 0 && this.canEat();
        if (bl2) {
            this.lovePlayer(player);
        }
        if (bl3 = this.isBaby()) {
            this.getEntityWorld().addParticleClient(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 0.0, 0.0, 0.0);
            if (!this.getEntityWorld().isClient()) {
                this.growUp(10);
            }
        }
        if (bl || bl2 || bl3) {
            SoundEvent lv;
            if (!this.isSilent() && (lv = this.getEatSound()) != null) {
                this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), lv, this.getSoundCategory(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            }
            this.emitGameEvent(GameEvent.EAT);
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldAmbientStand() {
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (other == this) return false;
        if (!(other instanceof CamelEntity)) return false;
        CamelEntity lv = (CamelEntity)other;
        if (!this.canBreed()) return false;
        if (!lv.canBreed()) return false;
        return true;
    }

    @Override
    @Nullable
    public CamelEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        return EntityType.CAMEL.create(arg, SpawnReason.BREEDING);
    }

    @Override
    @Nullable
    protected SoundEvent getEatSound() {
        return SoundEvents.ENTITY_CAMEL_EAT;
    }

    @Override
    protected void applyDamage(ServerWorld world, DamageSource source, float amount) {
        this.setStanding();
        super.applyDamage(world, source, amount);
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        int i = Math.max(this.getPassengerList().indexOf(passenger), 0);
        boolean bl = i == 0;
        float g = 0.5f;
        float h = (float)(this.isRemoved() ? (double)0.01f : this.getPassengerAttachmentY(bl, 0.0f, dimensions, scaleFactor));
        if (this.getPassengerList().size() > 1) {
            if (!bl) {
                g = -0.7f;
            }
            if (passenger instanceof AnimalEntity) {
                g += 0.2f;
            }
        }
        return new Vec3d(0.0, h, g * scaleFactor).rotateY(-this.getYaw() * ((float)Math.PI / 180));
    }

    @Override
    public float getScaleFactor() {
        return this.isBaby() ? 0.45f : 1.0f;
    }

    private double getPassengerAttachmentY(boolean primaryPassenger, float tickProgress, EntityDimensions dimensions, float scaleFactor) {
        double d = dimensions.height() - 0.375f * scaleFactor;
        float h = scaleFactor * 1.43f;
        float i = h - scaleFactor * 0.2f;
        float j = h - i;
        boolean bl2 = this.isChangingPose();
        boolean bl3 = this.isSitting();
        if (bl2) {
            float m;
            int l;
            int k;
            int n = k = bl3 ? 40 : 52;
            if (bl3) {
                l = 28;
                m = primaryPassenger ? 0.5f : 0.1f;
            } else {
                l = primaryPassenger ? 24 : 32;
                m = primaryPassenger ? 0.6f : 0.35f;
            }
            float n2 = MathHelper.clamp((float)this.getTimeSinceLastPoseTick() + tickProgress, 0.0f, (float)k);
            boolean bl4 = n2 < (float)l;
            float o = bl4 ? n2 / (float)l : (n2 - (float)l) / (float)(k - l);
            float p = h - m * i;
            d += bl3 ? (double)MathHelper.lerp(o, bl4 ? h : p, bl4 ? p : j) : (double)MathHelper.lerp(o, bl4 ? j - h : j - p, bl4 ? j - p : 0.0f);
        }
        if (bl3 && !bl2) {
            d += (double)j;
        }
        return d;
    }

    @Override
    public Vec3d getLeashOffset(float tickProgress) {
        EntityDimensions lv = this.getDimensions(this.getPose());
        float g = this.getScaleFactor();
        return new Vec3d(0.0, this.getPassengerAttachmentY(true, tickProgress, lv, g) - (double)(0.2f * g), lv.width() * 0.56f);
    }

    @Override
    public int getMaxHeadRotation() {
        return 30;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() <= 2;
    }

    public boolean isSitting() {
        return this.dataTracker.get(LAST_POSE_TICK) < 0L;
    }

    public boolean shouldUpdateSittingAnimations() {
        return this.getTimeSinceLastPoseTick() < 0L != this.isSitting();
    }

    public boolean isChangingPose() {
        long l = this.getTimeSinceLastPoseTick();
        return l < (long)(this.isSitting() ? 40 : 52);
    }

    private boolean shouldPlaySittingTransitionAnimation() {
        return this.isSitting() && this.getTimeSinceLastPoseTick() < 40L && this.getTimeSinceLastPoseTick() >= 0L;
    }

    public void startSitting() {
        if (this.isSitting()) {
            return;
        }
        this.playSound(SoundEvents.ENTITY_CAMEL_SIT);
        this.setPose(EntityPose.SITTING);
        this.emitGameEvent(GameEvent.ENTITY_ACTION);
        this.setLastPoseTick(-this.getEntityWorld().getTime());
    }

    public void startStanding() {
        if (!this.isSitting()) {
            return;
        }
        this.playSound(SoundEvents.ENTITY_CAMEL_STAND);
        this.setPose(EntityPose.STANDING);
        this.emitGameEvent(GameEvent.ENTITY_ACTION);
        this.setLastPoseTick(this.getEntityWorld().getTime());
    }

    public void setStanding() {
        this.setPose(EntityPose.STANDING);
        this.emitGameEvent(GameEvent.ENTITY_ACTION);
        this.initLastPoseTick(this.getEntityWorld().getTime());
    }

    @VisibleForTesting
    public void setLastPoseTick(long lastPoseTick) {
        this.dataTracker.set(LAST_POSE_TICK, lastPoseTick);
    }

    private void initLastPoseTick(long time) {
        this.setLastPoseTick(Math.max(0L, time - 52L - 1L));
    }

    public long getTimeSinceLastPoseTick() {
        return this.getEntityWorld().getTime() - Math.abs(this.dataTracker.get(LAST_POSE_TICK));
    }

    @Override
    protected RegistryEntry<SoundEvent> getEquipSound(EquipmentSlot slot, ItemStack stack, EquippableComponent equippableComponent) {
        if (slot == EquipmentSlot.SADDLE) {
            return SoundEvents.ENTITY_CAMEL_SADDLE;
        }
        return super.getEquipSound(slot, stack, equippableComponent);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (!this.firstUpdate && DASHING.equals(data)) {
            this.dashCooldown = this.dashCooldown == 0 ? 55 : this.dashCooldown;
        }
        super.onTrackedDataSet(data);
    }

    @Override
    public boolean isTame() {
        return true;
    }

    @Override
    public void openInventory(PlayerEntity player) {
        if (!this.getEntityWorld().isClient()) {
            player.openHorseInventory(this, this.items);
        }
    }

    @Override
    protected BodyControl createBodyControl() {
        return new CamelBodyControl(this);
    }

    @Override
    @Nullable
    public /* synthetic */ PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    class CamelMoveControl
    extends MoveControl {
        public CamelMoveControl() {
            super(CamelEntity.this);
        }

        @Override
        public void tick() {
            if (this.state == MoveControl.State.MOVE_TO && !CamelEntity.this.isLeashed() && CamelEntity.this.isSitting() && !CamelEntity.this.isChangingPose() && CamelEntity.this.canChangePose()) {
                CamelEntity.this.startStanding();
            }
            super.tick();
        }
    }

    class CamelLookControl
    extends LookControl {
        CamelLookControl() {
            super(CamelEntity.this);
        }

        @Override
        public void tick() {
            if (!CamelEntity.this.hasControllingPassenger()) {
                super.tick();
            }
        }
    }

    class CamelBodyControl
    extends BodyControl {
        public CamelBodyControl(CamelEntity camel) {
            super(camel);
        }

        @Override
        public void tick() {
            if (!CamelEntity.this.isStationary()) {
                super.tick();
            }
        }
    }
}

