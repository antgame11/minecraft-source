/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.YawAdjustingLookControl;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlBrain;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.InterpolatedFlipFlop;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class AxolotlEntity
extends AnimalEntity
implements Bucketable {
    public static final int PLAY_DEAD_TICKS = 200;
    private static final int field_52482 = 10;
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super AxolotlEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.AXOLOTL_ATTACKABLES, SensorType.AXOLOTL_TEMPTATIONS);
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.BREED_TARGET, MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_VISIBLE_ADULT, new MemoryModuleType[]{MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.PLAY_DEAD_TICKS, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.HAS_HUNTING_COOLDOWN, MemoryModuleType.IS_PANICKING});
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(AxolotlEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> PLAYING_DEAD = DataTracker.registerData(AxolotlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> FROM_BUCKET = DataTracker.registerData(AxolotlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final double BUFF_RANGE = 20.0;
    public static final int BLUE_BABY_CHANCE = 1200;
    private static final int MAX_AIR = 6000;
    public static final String VARIANT_KEY = "Variant";
    private static final int HYDRATION_BY_POTION = 1800;
    private static final int MAX_REGENERATION_BUFF_DURATION = 2400;
    private static final boolean DEFAULT_FROM_BUCKET = false;
    public final InterpolatedFlipFlop playingDeadFf = new InterpolatedFlipFlop(10, MathHelper::easeInOutSine);
    public final InterpolatedFlipFlop inWaterFf = new InterpolatedFlipFlop(10, MathHelper::easeInOutSine);
    public final InterpolatedFlipFlop onGroundFf = new InterpolatedFlipFlop(10, MathHelper::easeInOutSine);
    public final InterpolatedFlipFlop isMovingFf = new InterpolatedFlipFlop(10, MathHelper::easeInOutSine);
    private static final int BUFF_DURATION = 100;

    public AxolotlEntity(EntityType<? extends AxolotlEntity> arg, World arg2) {
        super((EntityType<? extends AnimalEntity>)arg, arg2);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        this.moveControl = new AxolotlMoveControl(this);
        this.lookControl = new AxolotlLookControl(this, 20);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return 0.0f;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0);
        builder.add(PLAYING_DEAD, false);
        builder.add(FROM_BUCKET, false);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put(VARIANT_KEY, Variant.INDEX_CODEC, this.getVariant());
        view.putBoolean("FromBucket", this.isFromBucket());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setVariant(view.read(VARIANT_KEY, Variant.INDEX_CODEC).orElse(Variant.DEFAULT));
        this.setFromBucket(view.getBoolean("FromBucket", false));
    }

    @Override
    public void playAmbientSound() {
        if (this.isPlayingDead()) {
            return;
        }
        super.playAmbientSound();
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        boolean bl = false;
        if (spawnReason == SpawnReason.BUCKET) {
            return entityData;
        }
        Random lv = world.getRandom();
        if (entityData instanceof AxolotlData) {
            if (((AxolotlData)entityData).getSpawnedCount() >= 2) {
                bl = true;
            }
        } else {
            entityData = new AxolotlData(Variant.getRandomNatural(lv), Variant.getRandomNatural(lv));
        }
        this.setVariant(((AxolotlData)entityData).getRandomVariant(lv));
        if (bl) {
            this.setBreedingAge(-24000);
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public void baseTick() {
        World world;
        int i = this.getAir();
        super.baseTick();
        if (!this.isAiDisabled() && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            this.tickAir(lv, i);
        }
        if (this.getEntityWorld().isClient()) {
            this.tickClient();
        }
    }

    private void tickClient() {
        State lv = this.isPlayingDead() ? State.PLAYING_DEAD : (this.isTouchingWater() ? State.IN_WATER : (this.isOnGround() ? State.ON_GROUND : State.IN_AIR));
        this.playingDeadFf.tick(lv == State.PLAYING_DEAD);
        this.inWaterFf.tick(lv == State.IN_WATER);
        this.onGroundFf.tick(lv == State.ON_GROUND);
        boolean bl = this.limbAnimator.isLimbMoving() || this.getPitch() != this.lastPitch || this.getYaw() != this.lastYaw;
        this.isMovingFf.tick(bl);
    }

    protected void tickAir(ServerWorld world, int air) {
        if (this.isAlive() && !this.isTouchingWaterOrRain()) {
            this.setAir(air - 1);
            if (this.shouldDrown()) {
                this.setAir(0);
                this.damage(world, this.getDamageSources().dryOut(), 2.0f);
            }
        } else {
            this.setAir(this.getMaxAir());
        }
    }

    public void hydrateFromPotion() {
        int i = this.getAir() + 1800;
        this.setAir(Math.min(i, this.getMaxAir()));
    }

    @Override
    public int getMaxAir() {
        return 6000;
    }

    public Variant getVariant() {
        return Variant.byIndex(this.dataTracker.get(VARIANT));
    }

    private void setVariant(Variant variant) {
        this.dataTracker.set(VARIANT, variant.getIndex());
    }

    @Override
    @Nullable
    public <T> T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.AXOLOTL_VARIANT) {
            return AxolotlEntity.castComponentValue(type, this.getVariant());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.AXOLOTL_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.AXOLOTL_VARIANT) {
            this.setVariant(AxolotlEntity.castComponentValue(DataComponentTypes.AXOLOTL_VARIANT, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    private static boolean shouldBabyBeDifferent(Random random) {
        return random.nextInt(1200) == 0;
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.doesNotIntersectEntities(this);
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    public void setPlayingDead(boolean playingDead) {
        this.dataTracker.set(PLAYING_DEAD, playingDead);
    }

    public boolean isPlayingDead() {
        return this.dataTracker.get(PLAYING_DEAD);
    }

    @Override
    public boolean isFromBucket() {
        return this.dataTracker.get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
        this.dataTracker.set(FROM_BUCKET, fromBucket);
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        AxolotlEntity lv = EntityType.AXOLOTL.create(world, SpawnReason.BREEDING);
        if (lv != null) {
            Variant lv2 = AxolotlEntity.shouldBabyBeDifferent(this.random) ? Variant.getRandomUnnatural(this.random) : (this.random.nextBoolean() ? this.getVariant() : ((AxolotlEntity)entity).getVariant());
            lv.setVariant(lv2);
            lv.setPersistent();
        }
        return lv;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.AXOLOTL_FOOD);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Profiler lv = Profilers.get();
        lv.push("axolotlBrain");
        this.getBrain().tick(world, this);
        lv.pop();
        lv.push("axolotlActivityUpdate");
        AxolotlBrain.updateActivities(this);
        lv.pop();
        if (!this.isAiDisabled()) {
            Optional<Integer> optional = this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.PLAY_DEAD_TICKS);
            this.setPlayingDead(optional.isPresent() && optional.get() > 0);
        }
    }

    public static DefaultAttributeContainer.Builder createAxolotlAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 14.0).add(EntityAttributes.MOVEMENT_SPEED, 1.0).add(EntityAttributes.ATTACK_DAMAGE, 2.0).add(EntityAttributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new AmphibiousSwimNavigation(this, world);
    }

    @Override
    public void playAttackSound() {
        this.playSound(SoundEvents.ENTITY_AXOLOTL_ATTACK, 1.0f, 1.0f);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        float g = this.getHealth();
        if (!this.isAiDisabled() && this.getEntityWorld().random.nextInt(3) == 0 && ((float)this.getEntityWorld().random.nextInt(3) < amount || g / this.getMaxHealth() < 0.5f) && amount < g && this.isTouchingWater() && (source.getAttacker() != null || source.getSource() != null) && !this.isPlayingDead()) {
            this.brain.remember(MemoryModuleType.PLAY_DEAD_TICKS, 200);
        }
        return super.damage(world, source, amount);
    }

    @Override
    public int getMaxLookPitchChange() {
        return 1;
    }

    @Override
    public int getMaxHeadRotation() {
        return 1;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        return Bucketable.tryBucket(player, hand, this).orElse(super.interactMob(player, hand));
    }

    @Override
    public void copyDataToStack(ItemStack stack) {
        Bucketable.copyDataToStack(this, stack);
        stack.copy(DataComponentTypes.AXOLOTL_VARIANT, this);
        NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbt -> {
            nbt.putInt("Age", this.getBreedingAge());
            Brain<AxolotlEntity> lv = this.getBrain();
            if (lv.hasMemoryModule(MemoryModuleType.HAS_HUNTING_COOLDOWN)) {
                nbt.putLong("HuntingCooldown", lv.getMemoryExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN));
            }
        });
    }

    @Override
    public void copyDataFromNbt(NbtCompound nbt) {
        Bucketable.copyDataFromNbt(this, nbt);
        this.setBreedingAge(nbt.getInt("Age", 0));
        nbt.getLong("HuntingCooldown").ifPresentOrElse(huntingCooldown -> this.getBrain().remember(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, nbt.getLong("HuntingCooldown", 0L)), () -> this.getBrain().remember(MemoryModuleType.HAS_HUNTING_COOLDOWN, Optional.empty()));
    }

    @Override
    public ItemStack getBucketItem() {
        return new ItemStack(Items.AXOLOTL_BUCKET);
    }

    @Override
    public SoundEvent getBucketFillSound() {
        return SoundEvents.ITEM_BUCKET_FILL_AXOLOTL;
    }

    @Override
    public boolean canTakeDamage() {
        return !this.isPlayingDead() && super.canTakeDamage();
    }

    public static void appreciatePlayer(ServerWorld world, AxolotlEntity axolotl, LivingEntity target) {
        Entity lv2;
        DamageSource lv;
        if (target.isDead() && (lv = target.getRecentDamageSource()) != null && (lv2 = lv.getAttacker()) != null && lv2.getType() == EntityType.PLAYER) {
            PlayerEntity lv3 = (PlayerEntity)lv2;
            List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, axolotl.getBoundingBox().expand(20.0));
            if (list.contains(lv3)) {
                axolotl.buffPlayer(lv3);
            }
        }
    }

    public void buffPlayer(PlayerEntity player) {
        StatusEffectInstance lv = player.getStatusEffect(StatusEffects.REGENERATION);
        if (lv == null || lv.isDurationBelow(2399)) {
            int i = lv != null ? lv.getDuration() : 0;
            int j = Math.min(2400, 100 + i);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, j, 0), this);
        }
        player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
    }

    @Override
    public boolean cannotDespawn() {
        return super.cannotDespawn() || this.isFromBucket();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_AXOLOTL_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_AXOLOTL_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return this.isTouchingWater() ? SoundEvents.ENTITY_AXOLOTL_IDLE_WATER : SoundEvents.ENTITY_AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_AXOLOTL_SPLASH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_AXOLOTL_SWIM;
    }

    protected Brain.Profile<AxolotlEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return AxolotlBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    public Brain<AxolotlEntity> getBrain() {
        return super.getBrain();
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isTouchingWater()) {
            this.updateVelocity(this.getMovementSpeed(), movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9));
        } else {
            super.travel(movementInput);
        }
    }

    @Override
    protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
        if (stack.isOf(Items.TROPICAL_FISH_BUCKET)) {
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.WATER_BUCKET)));
        } else {
            super.eat(player, hand, stack);
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !this.isFromBucket() && !this.hasCustomName();
    }

    @Override
    @Nullable
    public LivingEntity getTarget() {
        return this.getTargetInBrain();
    }

    public static boolean canSpawn(EntityType<? extends LivingEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.AXOLOTLS_SPAWNABLE_ON);
    }

    static class AxolotlMoveControl
    extends AquaticMoveControl {
        private final AxolotlEntity axolotl;

        public AxolotlMoveControl(AxolotlEntity axolotl) {
            super(axolotl, 85, 10, 0.1f, 0.5f, false);
            this.axolotl = axolotl;
        }

        @Override
        public void tick() {
            if (!this.axolotl.isPlayingDead()) {
                super.tick();
            }
        }
    }

    class AxolotlLookControl
    extends YawAdjustingLookControl {
        public AxolotlLookControl(AxolotlEntity axolotl, int yawAdjustThreshold) {
            super(axolotl, yawAdjustThreshold);
        }

        @Override
        public void tick() {
            if (!AxolotlEntity.this.isPlayingDead()) {
                super.tick();
            }
        }
    }

    public static enum Variant implements StringIdentifiable
    {
        LUCY(0, "lucy", true),
        WILD(1, "wild", true),
        GOLD(2, "gold", true),
        CYAN(3, "cyan", true),
        BLUE(4, "blue", false);

        public static final Variant DEFAULT;
        private static final IntFunction<Variant> INDEX_MAPPER;
        public static final PacketCodec<ByteBuf, Variant> PACKET_CODEC;
        public static final Codec<Variant> CODEC;
        @Deprecated
        public static final Codec<Variant> INDEX_CODEC;
        private final int index;
        private final String id;
        private final boolean natural;

        private Variant(int index, String id, boolean natural) {
            this.index = index;
            this.id = id;
            this.natural = natural;
        }

        public int getIndex() {
            return this.index;
        }

        public String getId() {
            return this.id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        public static Variant byIndex(int index) {
            return INDEX_MAPPER.apply(index);
        }

        public static Variant getRandomNatural(Random random) {
            return Variant.getRandom(random, true);
        }

        public static Variant getRandomUnnatural(Random random) {
            return Variant.getRandom(random, false);
        }

        private static Variant getRandom(Random random, boolean natural) {
            Variant[] lvs = (Variant[])Arrays.stream(Variant.values()).filter(variant -> variant.natural == natural).toArray(Variant[]::new);
            return Util.getRandom(lvs, random);
        }

        static {
            DEFAULT = LUCY;
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(Variant::getIndex, Variant.values(), ValueLists.OutOfBoundsHandling.ZERO);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, Variant::getIndex);
            CODEC = StringIdentifiable.createCodec(Variant::values);
            INDEX_CODEC = Codec.INT.xmap(INDEX_MAPPER::apply, Variant::getIndex);
        }
    }

    public static class AxolotlData
    extends PassiveEntity.PassiveData {
        public final Variant[] variants;

        public AxolotlData(Variant ... variants) {
            super(false);
            this.variants = variants;
        }

        public Variant getRandomVariant(Random random) {
            return this.variants[random.nextInt(this.variants.length)];
        }
    }

    public static enum State {
        PLAYING_DEAD,
        IN_WATER,
        ON_GROUND,
        IN_AIR;

    }
}

