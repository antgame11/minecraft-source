/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Variants;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PigVariant;
import net.minecraft.entity.passive.PigVariants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PigEntity
extends AnimalEntity
implements ItemSteerable {
    private static final TrackedData<Integer> BOOST_TIME = DataTracker.registerData(PigEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<RegistryEntry<PigVariant>> VARIANT = DataTracker.registerData(PigEntity.class, TrackedDataHandlerRegistry.PIG_VARIANT);
    private final SaddledComponent saddledComponent;

    public PigEntity(EntityType<? extends PigEntity> arg, World arg2) {
        super((EntityType<? extends AnimalEntity>)arg, arg2);
        this.saddledComponent = new SaddledComponent(this.dataTracker, BOOST_TIME);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25));
        this.goalSelector.add(3, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(4, new TemptGoal(this, 1.2, stack -> stack.isOf(Items.CARROT_ON_A_STICK), false));
        this.goalSelector.add(4, new TemptGoal(this, 1.2, stack -> stack.isIn(ItemTags.PIG_FOOD), false));
        this.goalSelector.add(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
    }

    public static DefaultAttributeContainer.Builder createPigAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 10.0).add(EntityAttributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        PlayerEntity lv;
        Entity entity;
        if (this.hasSaddleEquipped() && (entity = this.getFirstPassenger()) instanceof PlayerEntity && (lv = (PlayerEntity)entity).isHolding(Items.CARROT_ON_A_STICK)) {
            return lv;
        }
        return super.getControllingPassenger();
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (BOOST_TIME.equals(data) && this.getEntityWorld().isClient()) {
            this.saddledComponent.boost();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(BOOST_TIME, 0);
        builder.add(VARIANT, Variants.getOrDefaultOrThrow(this.getRegistryManager(), PigVariants.DEFAULT));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        Variants.writeData(view, this.getVariant());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        Variants.fromData(view, RegistryKeys.PIG_VARIANT).ifPresent(this::setVariant);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PIG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PIG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PIG_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_PIG_STEP, 0.15f, 1.0f);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        boolean bl = this.isBreedingItem(player.getStackInHand(hand));
        if (!bl && this.hasSaddleEquipped() && !this.hasPassengers() && !player.shouldCancelInteraction()) {
            if (!this.getEntityWorld().isClient()) {
                player.startRiding(this);
            }
            return ActionResult.SUCCESS;
        }
        ActionResult lv = super.interactMob(player, hand);
        if (!lv.isAccepted()) {
            ItemStack lv2 = player.getStackInHand(hand);
            if (this.canEquip(lv2, EquipmentSlot.SADDLE)) {
                return lv2.useOnEntity(player, this, hand);
            }
            return ActionResult.PASS;
        }
        return lv;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        if (slot == EquipmentSlot.SADDLE) {
            return this.isAlive() && !this.isBaby();
        }
        return super.canUseSlot(slot);
    }

    @Override
    protected boolean canDispenserEquipSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.SADDLE || super.canDispenserEquipSlot(slot);
    }

    @Override
    protected RegistryEntry<SoundEvent> getEquipSound(EquipmentSlot slot, ItemStack stack, EquippableComponent equippableComponent) {
        if (slot == EquipmentSlot.SADDLE) {
            return SoundEvents.ENTITY_PIG_SADDLE;
        }
        return super.getEquipSound(slot, stack, equippableComponent);
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        Direction lv = this.getMovementDirection();
        if (lv.getAxis() == Direction.Axis.Y) {
            return super.updatePassengerForDismount(passenger);
        }
        int[][] is = Dismounting.getDismountOffsets(lv);
        BlockPos lv2 = this.getBlockPos();
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        for (EntityPose lv4 : passenger.getPoses()) {
            Box lv5 = passenger.getBoundingBox(lv4);
            for (int[] js : is) {
                lv3.set(lv2.getX() + js[0], lv2.getY(), lv2.getZ() + js[1]);
                double d = this.getEntityWorld().getDismountHeight(lv3);
                if (!Dismounting.canDismountInBlock(d)) continue;
                Vec3d lv6 = Vec3d.ofCenter(lv3, d);
                if (!Dismounting.canPlaceEntityAt(this.getEntityWorld(), passenger, lv5.offset(lv6))) continue;
                passenger.setPose(lv4);
                return lv6;
            }
        }
        return super.updatePassengerForDismount(passenger);
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        if (world.getDifficulty() != Difficulty.PEACEFUL) {
            ZombifiedPiglinEntity lv = this.convertTo(EntityType.ZOMBIFIED_PIGLIN, EntityConversionContext.create(this, false, true), zombifiedPiglin -> {
                if (this.getMainHandStack().isEmpty()) {
                    zombifiedPiglin.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                }
                zombifiedPiglin.setPersistent();
            });
            if (lv == null) {
                super.onStruckByLightning(world, lightning);
            }
        } else {
            super.onStruckByLightning(world, lightning);
        }
    }

    @Override
    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);
        this.setRotation(controllingPlayer.getYaw(), controllingPlayer.getPitch() * 0.5f);
        this.bodyYaw = this.headYaw = this.getYaw();
        this.lastYaw = this.headYaw;
        this.saddledComponent.tickBoost();
    }

    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        return new Vec3d(0.0, 0.0, 1.0);
    }

    @Override
    protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
        return (float)(this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) * 0.225 * (double)this.saddledComponent.getMovementSpeedMultiplier());
    }

    @Override
    public boolean consumeOnAStickItem() {
        return this.saddledComponent.boost(this.getRandom());
    }

    @Override
    @Nullable
    public PigEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        PigEntity lv = EntityType.PIG.create(arg, SpawnReason.BREEDING);
        if (lv != null && arg2 instanceof PigEntity) {
            PigEntity lv2 = (PigEntity)arg2;
            lv.setVariant(this.random.nextBoolean() ? this.getVariant() : lv2.getVariant());
        }
        return lv;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.PIG_FOOD);
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.6f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }

    private void setVariant(RegistryEntry<PigVariant> variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    public RegistryEntry<PigVariant> getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    @Override
    @Nullable
    public <T> T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.PIG_VARIANT) {
            return PigEntity.castComponentValue(type, this.getVariant());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.PIG_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.PIG_VARIANT) {
            this.setVariant(PigEntity.castComponentValue(DataComponentTypes.PIG_VARIANT, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Variants.select(SpawnContext.of(world, this.getBlockPos()), RegistryKeys.PIG_VARIANT).ifPresent(this::setVariant);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    @Nullable
    public /* synthetic */ PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }
}

