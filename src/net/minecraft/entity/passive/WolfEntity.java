/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Variants;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.UntamedActiveTargetGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.WolfBegGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.Cracks;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfSoundVariant;
import net.minecraft.entity.passive.WolfSoundVariants;
import net.minecraft.entity.passive.WolfVariant;
import net.minecraft.entity.passive.WolfVariants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class WolfEntity
extends TameableEntity
implements Angerable {
    private static final TrackedData<Boolean> BEGGING = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> COLLAR_COLOR = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<RegistryEntry<WolfVariant>> VARIANT = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.WOLF_VARIANT);
    private static final TrackedData<RegistryEntry<WolfSoundVariant>> SOUND_VARIANT = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.WOLF_SOUND_VARIANT);
    public static final TargetPredicate.EntityPredicate FOLLOW_TAMED_PREDICATE = (entity, world) -> {
        EntityType<?> lv = entity.getType();
        return lv == EntityType.SHEEP || lv == EntityType.RABBIT || lv == EntityType.FOX;
    };
    private static final float WILD_MAX_HEALTH = 8.0f;
    private static final float TAMED_MAX_HEALTH = 40.0f;
    private static final float field_49237 = 0.125f;
    public static final float field_52477 = 0.62831855f;
    private static final DyeColor DEFAULT_COLLAR_COLOR = DyeColor.RED;
    private float begAnimationProgress;
    private float lastBegAnimationProgress;
    private boolean furWet;
    private boolean canShakeWaterOff;
    private float shakeProgress;
    private float lastShakeProgress;
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    @Nullable
    private UUID angryAt;

    public WolfEntity(EntityType<? extends WolfEntity> arg, World arg2) {
        super((EntityType<? extends TameableEntity>)arg, arg2);
        this.setTamed(false, false);
        this.setPathfindingPenalty(PathNodeType.POWDER_SNOW, -1.0f);
        this.setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW, -1.0f);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(1, new TameableEntity.TameableEscapeDangerGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new AvoidLlamaGoal<LlamaEntity>(this, LlamaEntity.class, 24.0f, 1.5, 1.5));
        this.goalSelector.add(4, new PounceAtTargetGoal(this, 0.4f));
        this.goalSelector.add(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.add(7, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(9, new WolfBegGoal(this, 8.0f));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(10, new LookAroundGoal(this));
        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, new RevengeGoal(this, new Class[0]).setGroupRevenge(new Class[0]));
        this.targetSelector.add(4, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
        this.targetSelector.add(5, new UntamedActiveTargetGoal<AnimalEntity>(this, AnimalEntity.class, false, FOLLOW_TAMED_PREDICATE));
        this.targetSelector.add(6, new UntamedActiveTargetGoal<TurtleEntity>(this, TurtleEntity.class, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
        this.targetSelector.add(7, new ActiveTargetGoal<AbstractSkeletonEntity>((MobEntity)this, AbstractSkeletonEntity.class, false));
        this.targetSelector.add(8, new UniversalAngerGoal<WolfEntity>(this, true));
    }

    public Identifier getTextureId() {
        WolfVariant lv = this.getVariant().value();
        if (this.isTamed()) {
            return lv.assetInfo().tame().texturePath();
        }
        if (this.hasAngerTime()) {
            return lv.assetInfo().angry().texturePath();
        }
        return lv.assetInfo().wild().texturePath();
    }

    private RegistryEntry<WolfVariant> getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    private void setVariant(RegistryEntry<WolfVariant> variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    private RegistryEntry<WolfSoundVariant> getSoundVariant() {
        return this.dataTracker.get(SOUND_VARIANT);
    }

    private void setSoundVariant(RegistryEntry<WolfSoundVariant> soundVariant) {
        this.dataTracker.set(SOUND_VARIANT, soundVariant);
    }

    @Override
    @Nullable
    public <T> T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.WOLF_VARIANT) {
            return WolfEntity.castComponentValue(type, this.getVariant());
        }
        if (type == DataComponentTypes.WOLF_SOUND_VARIANT) {
            return WolfEntity.castComponentValue(type, this.getSoundVariant());
        }
        if (type == DataComponentTypes.WOLF_COLLAR) {
            return WolfEntity.castComponentValue(type, this.getCollarColor());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.WOLF_VARIANT);
        this.copyComponentFrom(from, DataComponentTypes.WOLF_SOUND_VARIANT);
        this.copyComponentFrom(from, DataComponentTypes.WOLF_COLLAR);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.WOLF_VARIANT) {
            this.setVariant(WolfEntity.castComponentValue(DataComponentTypes.WOLF_VARIANT, value));
            return true;
        }
        if (type == DataComponentTypes.WOLF_SOUND_VARIANT) {
            this.setSoundVariant(WolfEntity.castComponentValue(DataComponentTypes.WOLF_SOUND_VARIANT, value));
            return true;
        }
        if (type == DataComponentTypes.WOLF_COLLAR) {
            this.setCollarColor(WolfEntity.castComponentValue(DataComponentTypes.WOLF_COLLAR, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    public static DefaultAttributeContainer.Builder createWolfAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MOVEMENT_SPEED, 0.3f).add(EntityAttributes.MAX_HEALTH, 8.0).add(EntityAttributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        RegistryWrapper.Impl lv = this.getRegistryManager().getOrThrow(RegistryKeys.WOLF_SOUND_VARIANT);
        builder.add(VARIANT, Variants.getOrDefaultOrThrow(this.getRegistryManager(), WolfVariants.DEFAULT));
        builder.add(SOUND_VARIANT, (RegistryEntry)lv.getOptional(WolfSoundVariants.CLASSIC).or(((Registry)lv)::getDefaultEntry).orElseThrow());
        builder.add(BEGGING, false);
        builder.add(COLLAR_COLOR, DEFAULT_COLLAR_COLOR.getIndex());
        builder.add(ANGER_TIME, 0);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_WOLF_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("CollarColor", DyeColor.INDEX_CODEC, this.getCollarColor());
        Variants.writeData(view, this.getVariant());
        this.writeAngerToData(view);
        this.getSoundVariant().getKey().ifPresent(soundVariant -> view.put("sound_variant", RegistryKey.createCodec(RegistryKeys.WOLF_SOUND_VARIANT), soundVariant));
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        Variants.fromData(view, RegistryKeys.WOLF_VARIANT).ifPresent(this::setVariant);
        this.setCollarColor(view.read("CollarColor", DyeColor.INDEX_CODEC).orElse(DEFAULT_COLLAR_COLOR));
        this.readAngerFromData(this.getEntityWorld(), view);
        view.read("sound_variant", RegistryKey.createCodec(RegistryKeys.WOLF_SOUND_VARIANT)).flatMap(soundVariantKey -> this.getRegistryManager().getOrThrow(RegistryKeys.WOLF_SOUND_VARIANT).getOptional(soundVariantKey)).ifPresent(this::setSoundVariant);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        if (entityData instanceof WolfData) {
            WolfData lv = (WolfData)entityData;
            this.setVariant(lv.variant);
        } else {
            Optional optional = Variants.select(SpawnContext.of(world, this.getBlockPos()), RegistryKeys.WOLF_VARIANT);
            if (optional.isPresent()) {
                this.setVariant(optional.get());
                entityData = new WolfData(optional.get());
            }
        }
        this.setSoundVariant(WolfSoundVariants.select(this.getRegistryManager(), world.getRandom()));
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.hasAngerTime()) {
            return this.getSoundVariant().value().growlSound().value();
        }
        if (this.random.nextInt(3) == 0) {
            if (this.isTamed() && this.getHealth() < 20.0f) {
                return this.getSoundVariant().value().whineSound().value();
            }
            return this.getSoundVariant().value().pantSound().value();
        }
        return this.getSoundVariant().value().ambientSound().value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.shouldArmorAbsorbDamage(source)) {
            return SoundEvents.ITEM_WOLF_ARMOR_DAMAGE;
        }
        return this.getSoundVariant().value().hurtSound().value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.getSoundVariant().value().deathSound().value();
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getEntityWorld().isClient() && this.furWet && !this.canShakeWaterOff && !this.isNavigating() && this.isOnGround()) {
            this.canShakeWaterOff = true;
            this.shakeProgress = 0.0f;
            this.lastShakeProgress = 0.0f;
            this.getEntityWorld().sendEntityStatus(this, EntityStatuses.SHAKE_OFF_WATER);
        }
        if (!this.getEntityWorld().isClient()) {
            this.tickAngerLogic((ServerWorld)this.getEntityWorld(), true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isAlive()) {
            return;
        }
        this.lastBegAnimationProgress = this.begAnimationProgress;
        this.begAnimationProgress = this.isBegging() ? (this.begAnimationProgress += (1.0f - this.begAnimationProgress) * 0.4f) : (this.begAnimationProgress += (0.0f - this.begAnimationProgress) * 0.4f);
        if (this.isTouchingWaterOrRain()) {
            this.furWet = true;
            if (this.canShakeWaterOff && !this.getEntityWorld().isClient()) {
                this.getEntityWorld().sendEntityStatus(this, EntityStatuses.RESET_WOLF_SHAKE);
                this.resetShake();
            }
        } else if ((this.furWet || this.canShakeWaterOff) && this.canShakeWaterOff) {
            if (this.shakeProgress == 0.0f) {
                this.playSound(SoundEvents.ENTITY_WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                this.emitGameEvent(GameEvent.ENTITY_ACTION);
            }
            this.lastShakeProgress = this.shakeProgress;
            this.shakeProgress += 0.05f;
            if (this.lastShakeProgress >= 2.0f) {
                this.furWet = false;
                this.canShakeWaterOff = false;
                this.lastShakeProgress = 0.0f;
                this.shakeProgress = 0.0f;
            }
            if (this.shakeProgress > 0.4f) {
                float f = (float)this.getY();
                int i = (int)(MathHelper.sin((this.shakeProgress - 0.4f) * (float)Math.PI) * 7.0f);
                Vec3d lv = this.getVelocity();
                for (int j = 0; j < i; ++j) {
                    float g = (this.random.nextFloat() * 2.0f - 1.0f) * this.getWidth() * 0.5f;
                    float h = (this.random.nextFloat() * 2.0f - 1.0f) * this.getWidth() * 0.5f;
                    this.getEntityWorld().addParticleClient(ParticleTypes.SPLASH, this.getX() + (double)g, f + 0.8f, this.getZ() + (double)h, lv.x, lv.y, lv.z);
                }
            }
        }
    }

    private void resetShake() {
        this.canShakeWaterOff = false;
        this.shakeProgress = 0.0f;
        this.lastShakeProgress = 0.0f;
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        this.furWet = false;
        this.canShakeWaterOff = false;
        this.lastShakeProgress = 0.0f;
        this.shakeProgress = 0.0f;
        super.onDeath(damageSource);
    }

    public float getFurWetBrightnessMultiplier(float tickProgress) {
        if (!this.furWet) {
            return 1.0f;
        }
        return Math.min(0.75f + MathHelper.lerp(tickProgress, this.lastShakeProgress, this.shakeProgress) / 2.0f * 0.25f, 1.0f);
    }

    public float getShakeProgress(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastShakeProgress, this.shakeProgress);
    }

    public float getBegAnimationProgress(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastBegAnimationProgress, this.begAnimationProgress) * 0.15f * (float)Math.PI;
    }

    @Override
    public int getMaxLookPitchChange() {
        if (this.isInSittingPose()) {
            return 20;
        }
        return super.getMaxLookPitchChange();
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) {
            return false;
        }
        this.setSitting(false);
        return super.damage(world, source, amount);
    }

    @Override
    protected void applyDamage(ServerWorld world, DamageSource source, float amount) {
        if (!this.shouldArmorAbsorbDamage(source)) {
            super.applyDamage(world, source, amount);
            return;
        }
        ItemStack lv = this.getBodyArmor();
        int i = lv.getDamage();
        int j = lv.getMaxDamage();
        lv.damage(MathHelper.ceil(amount), (LivingEntity)this, EquipmentSlot.BODY);
        if (Cracks.WOLF_ARMOR.getCrackLevel(i, j) != Cracks.WOLF_ARMOR.getCrackLevel(this.getBodyArmor())) {
            this.playSoundIfNotSilent(SoundEvents.ITEM_WOLF_ARMOR_CRACK);
            world.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, Items.ARMADILLO_SCUTE.getDefaultStack()), this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.2, 0.1, 0.2, 0.1);
        }
    }

    private boolean shouldArmorAbsorbDamage(DamageSource source) {
        return this.getBodyArmor().isOf(Items.WOLF_ARMOR) && !source.isIn(DamageTypeTags.BYPASSES_WOLF_ARMOR);
    }

    @Override
    protected void updateAttributesForTamed() {
        if (this.isTamed()) {
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(40.0);
            this.setHealth(40.0f);
        } else {
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(8.0);
        }
    }

    @Override
    protected void damageArmor(DamageSource source, float amount) {
        this.damageEquipment(source, amount, EquipmentSlot.BODY);
    }

    @Override
    protected boolean canRemoveSaddle(PlayerEntity player) {
        return this.isOwner(player);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        Item lv2 = lv.getItem();
        if (this.isTamed()) {
            if (this.isBreedingItem(lv) && this.getHealth() < this.getMaxHealth()) {
                this.eat(player, hand, lv);
                FoodComponent lv3 = lv.get(DataComponentTypes.FOOD);
                float f = lv3 != null ? (float)lv3.nutrition() : 1.0f;
                this.heal(2.0f * f);
                return ActionResult.SUCCESS;
            }
            if (lv2 instanceof DyeItem) {
                DyeItem lv4 = (DyeItem)lv2;
                if (this.isOwner(player)) {
                    DyeColor lv5 = lv4.getColor();
                    if (lv5 == this.getCollarColor()) return super.interactMob(player, hand);
                    this.setCollarColor(lv5);
                    lv.decrementUnlessCreative(1, player);
                    return ActionResult.SUCCESS;
                }
            }
            if (this.canEquip(lv, EquipmentSlot.BODY) && !this.isWearingBodyArmor() && this.isOwner(player) && !this.isBaby()) {
                this.equipBodyArmor(lv.copyWithCount(1));
                lv.decrementUnlessCreative(1, player);
                return ActionResult.SUCCESS;
            }
            if (this.isInSittingPose() && this.isWearingBodyArmor() && this.isOwner(player) && this.getBodyArmor().isDamaged() && this.getBodyArmor().canRepairWith(lv)) {
                lv.decrement(1);
                this.playSoundIfNotSilent(SoundEvents.ITEM_WOLF_ARMOR_REPAIR);
                ItemStack lv6 = this.getBodyArmor();
                int i = (int)((float)lv6.getMaxDamage() * 0.125f);
                lv6.setDamage(Math.max(0, lv6.getDamage() - i));
                return ActionResult.SUCCESS;
            }
            ActionResult lv7 = super.interactMob(player, hand);
            if (lv7.isAccepted() || !this.isOwner(player)) return lv7;
            this.setSitting(!this.isSitting());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return ActionResult.SUCCESS.noIncrementStat();
        }
        if (this.getEntityWorld().isClient() || !lv.isOf(Items.BONE) || this.hasAngerTime()) return super.interactMob(player, hand);
        lv.decrementUnlessCreative(1, player);
        this.tryTame(player);
        return ActionResult.SUCCESS_SERVER;
    }

    private void tryTame(PlayerEntity player) {
        if (this.random.nextInt(3) == 0) {
            this.setTamedBy(player);
            this.navigation.stop();
            this.setTarget(null);
            this.setSitting(true);
            this.getEntityWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
        } else {
            this.getEntityWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.SHAKE_OFF_WATER) {
            this.canShakeWaterOff = true;
            this.shakeProgress = 0.0f;
            this.lastShakeProgress = 0.0f;
        } else if (status == EntityStatuses.RESET_WOLF_SHAKE) {
            this.resetShake();
        } else {
            super.handleStatus(status);
        }
    }

    public float getTailAngle() {
        if (this.hasAngerTime()) {
            return 1.5393804f;
        }
        if (this.isTamed()) {
            float f = this.getMaxHealth();
            float g = (f - this.getHealth()) / f;
            return (0.55f - g * 0.4f) * (float)Math.PI;
        }
        return 0.62831855f;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.WOLF_FOOD);
    }

    @Override
    public int getLimitPerChunk() {
        return 8;
    }

    @Override
    public int getAngerTime() {
        return this.dataTracker.get(ANGER_TIME);
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.dataTracker.set(ANGER_TIME, angerTime);
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }

    @Override
    @Nullable
    public UUID getAngryAt() {
        return this.angryAt;
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    public DyeColor getCollarColor() {
        return DyeColor.byIndex(this.dataTracker.get(COLLAR_COLOR));
    }

    private void setCollarColor(DyeColor color) {
        this.dataTracker.set(COLLAR_COLOR, color.getIndex());
    }

    @Override
    @Nullable
    public WolfEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        WolfEntity lv = EntityType.WOLF.create(arg, SpawnReason.BREEDING);
        if (lv != null && arg2 instanceof WolfEntity) {
            WolfEntity lv2 = (WolfEntity)arg2;
            if (this.random.nextBoolean()) {
                lv.setVariant(this.getVariant());
            } else {
                lv.setVariant(lv2.getVariant());
            }
            if (this.isTamed()) {
                lv.setOwner(this.getOwnerReference());
                lv.setTamed(true, true);
                DyeColor lv3 = this.getCollarColor();
                DyeColor lv4 = lv2.getCollarColor();
                lv.setCollarColor(DyeColor.mixColors(arg, lv3, lv4));
            }
            lv.setSoundVariant(WolfSoundVariants.select(this.getRegistryManager(), this.random));
        }
        return lv;
    }

    public void setBegging(boolean begging) {
        this.dataTracker.set(BEGGING, begging);
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (other == this) {
            return false;
        }
        if (!this.isTamed()) {
            return false;
        }
        if (!(other instanceof WolfEntity)) {
            return false;
        }
        WolfEntity lv = (WolfEntity)other;
        if (!lv.isTamed()) {
            return false;
        }
        if (lv.isInSittingPose()) {
            return false;
        }
        return this.isInLove() && lv.isInLove();
    }

    public boolean isBegging() {
        return this.dataTracker.get(BEGGING);
    }

    @Override
    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        TameableEntity lv5;
        AbstractHorseEntity lv4;
        if (target instanceof CreeperEntity || target instanceof GhastEntity || target instanceof ArmorStandEntity) {
            return false;
        }
        if (target instanceof WolfEntity) {
            WolfEntity lv = (WolfEntity)target;
            return !lv.isTamed() || lv.getOwner() != owner;
        }
        if (target instanceof PlayerEntity) {
            PlayerEntity lv3;
            PlayerEntity lv2 = (PlayerEntity)target;
            if (owner instanceof PlayerEntity && !(lv3 = (PlayerEntity)owner).shouldDamagePlayer(lv2)) {
                return false;
            }
        }
        if (target instanceof AbstractHorseEntity && (lv4 = (AbstractHorseEntity)target).isTame()) {
            return false;
        }
        return !(target instanceof TameableEntity) || !(lv5 = (TameableEntity)target).isTamed();
    }

    @Override
    public boolean canBeLeashed() {
        return !this.hasAngerTime();
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.6f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }

    public static boolean canSpawn(EntityType<WolfEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.WOLVES_SPAWNABLE_ON) && WolfEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Override
    @Nullable
    public /* synthetic */ PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    class AvoidLlamaGoal<T extends LivingEntity>
    extends FleeEntityGoal<T> {
        private final WolfEntity wolf;

        public AvoidLlamaGoal(WolfEntity wolf, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed) {
            super(wolf, fleeFromType, distance, slowSpeed, fastSpeed);
            this.wolf = wolf;
        }

        @Override
        public boolean canStart() {
            if (super.canStart() && this.targetEntity instanceof LlamaEntity) {
                return !this.wolf.isTamed() && this.isScaredOf((LlamaEntity)this.targetEntity);
            }
            return false;
        }

        private boolean isScaredOf(LlamaEntity llama) {
            return llama.getStrength() >= WolfEntity.this.random.nextInt(5);
        }

        @Override
        public void start() {
            WolfEntity.this.setTarget(null);
            super.start();
        }

        @Override
        public void tick() {
            WolfEntity.this.setTarget(null);
            super.tick();
        }
    }

    public static class WolfData
    extends PassiveEntity.PassiveData {
        public final RegistryEntry<WolfVariant> variant;

        public WolfData(RegistryEntry<WolfVariant> variant) {
            super(false);
            this.variant = variant;
        }
    }
}

