/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JavaOps;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.BlocksAttacksComponent;
import net.minecraft.component.type.DeathProtectionComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.WeaponComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.PositionInterpolator;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ElytraFlightController;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerWaypointHandler;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.CollisionView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.waypoint.ServerWaypoint;
import net.minecraft.world.waypoint.Waypoint;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class LivingEntity
extends Entity
implements Attackable,
ServerWaypoint {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ACTIVE_EFFECTS_KEY = "active_effects";
    public static final String ATTRIBUTES_KEY = "attributes";
    public static final String SLEEPING_POS_KEY = "sleeping_pos";
    public static final String EQUIPMENT_KEY = "equipment";
    public static final String BRAIN_KEY = "Brain";
    public static final String FALL_FLYING_KEY = "FallFlying";
    public static final String HURT_TIME_KEY = "HurtTime";
    public static final String DEATH_TIME_KEY = "DeathTime";
    public static final String HURT_BY_TIMESTAMP_KEY = "HurtByTimestamp";
    public static final String HEALTH_KEY = "Health";
    private static final Identifier POWDER_SNOW_SPEED_MODIFIER_ID = Identifier.ofVanilla("powder_snow");
    private static final Identifier SPRINTING_SPEED_MODIFIER_ID = Identifier.ofVanilla("sprinting");
    private static final EntityAttributeModifier SPRINTING_SPEED_BOOST = new EntityAttributeModifier(SPRINTING_SPEED_MODIFIER_ID, 0.3f, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final int EQUIPMENT_SLOT_ID = 98;
    public static final int field_30072 = 100;
    public static final int field_48827 = 105;
    public static final int field_55952 = 106;
    public static final int GLOWING_FLAG = 6;
    public static final int field_30074 = 100;
    private static final int field_30078 = 40;
    public static final double field_30075 = 0.003;
    public static final double GRAVITY = 0.08;
    public static final int DEATH_TICKS = 20;
    protected static final float field_56256 = 0.98f;
    private static final int field_30080 = 10;
    private static final int field_30081 = 2;
    public static final float field_44874 = 0.42f;
    private static final double MAX_ENTITY_VIEWING_DISTANCE = 128.0;
    protected static final int USING_ITEM_FLAG = 1;
    protected static final int OFF_HAND_ACTIVE_FLAG = 2;
    protected static final int USING_RIPTIDE_FLAG = 4;
    protected static final TrackedData<Byte> LIVING_FLAGS = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Float> HEALTH = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<List<ParticleEffect>> POTION_SWIRLS = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.PARTICLE_LIST);
    private static final TrackedData<Boolean> POTION_SWIRLS_AMBIENT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> STUCK_ARROW_COUNT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> STINGER_COUNT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Optional<BlockPos>> SLEEPING_POSITION = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    private static final int field_49793 = 15;
    protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2f, 0.2f).withEyeHeight(0.2f);
    public static final float BABY_SCALE_FACTOR = 0.5f;
    public static final float field_47756 = 0.5f;
    public static final Predicate<LivingEntity> NOT_WEARING_GAZE_DISGUISE_PREDICATE = entity -> {
        if (!(entity instanceof PlayerEntity)) {
            return true;
        }
        PlayerEntity lv = (PlayerEntity)entity;
        ItemStack lv2 = lv.getEquippedStack(EquipmentSlot.HEAD);
        return !lv2.isIn(ItemTags.GAZE_DISGUISE_EQUIPMENT);
    };
    private static final Dynamic<?> BRAIN = new Dynamic(JavaOps.INSTANCE, Map.of("memories", Map.of()));
    private final AttributeContainer attributes;
    private final DamageTracker damageTracker = new DamageTracker(this);
    private final Map<RegistryEntry<StatusEffect>, StatusEffectInstance> activeStatusEffects = Maps.newHashMap();
    private final Map<EquipmentSlot, ItemStack> lastEquipmentStacks = Util.mapEnum(EquipmentSlot.class, slot -> ItemStack.EMPTY);
    public boolean handSwinging;
    private boolean noDrag = false;
    public Hand preferredHand;
    public int handSwingTicks;
    public int stuckArrowTimer;
    public int stuckStingerTimer;
    public int hurtTime;
    public int maxHurtTime;
    public int deathTime;
    public float lastHandSwingProgress;
    public float handSwingProgress;
    protected int lastAttackedTicks;
    public final LimbAnimator limbAnimator = new LimbAnimator();
    public final int defaultMaxHealth = 20;
    public float bodyYaw;
    public float lastBodyYaw;
    public float headYaw;
    public float lastHeadYaw;
    public final ElytraFlightController elytraFlightController = new ElytraFlightController(this);
    @Nullable
    protected LazyEntityReference<PlayerEntity> attackingPlayer;
    protected int playerHitTimer;
    protected boolean dead;
    protected int despawnCounter;
    protected float lastDamageTaken;
    protected boolean jumping;
    public float sidewaysSpeed;
    public float upwardSpeed;
    public float forwardSpeed;
    protected PositionInterpolator interpolator = new PositionInterpolator(this);
    protected double serverHeadYaw;
    protected int headTrackingIncrements;
    private boolean effectsChanged = true;
    @Nullable
    private LazyEntityReference<LivingEntity> attackerReference;
    private int lastAttackedTime;
    @Nullable
    private LivingEntity attacking;
    private int lastAttackTime;
    private float movementSpeed;
    private int jumpingCooldown;
    private float absorptionAmount;
    protected ItemStack activeItemStack = ItemStack.EMPTY;
    protected int itemUseTimeLeft;
    protected int glidingTicks;
    private BlockPos lastBlockPos;
    private Optional<BlockPos> climbingPos = Optional.empty();
    @Nullable
    private DamageSource lastDamageSource;
    private long lastDamageTime;
    protected int riptideTicks;
    protected float riptideAttackDamage;
    @Nullable
    protected ItemStack riptideStack;
    private float leaningPitch;
    private float lastLeaningPitch;
    protected Brain<?> brain;
    private boolean experienceDroppingDisabled;
    private final EnumMap<EquipmentSlot, Reference2ObjectMap<Enchantment, Set<EnchantmentLocationBasedEffect>>> locationBasedEnchantmentEffects = new EnumMap(EquipmentSlot.class);
    protected final EntityEquipment equipment;
    private Waypoint.Config waypointConfig = new Waypoint.Config();

    protected LivingEntity(EntityType<? extends LivingEntity> arg, World arg2) {
        super(arg, arg2);
        this.attributes = new AttributeContainer(DefaultAttributeRegistry.get(arg));
        this.setHealth(this.getMaxHealth());
        this.equipment = this.createEquipment();
        this.intersectionChecked = true;
        this.refreshPosition();
        this.setYaw((float)(Math.random() * 6.2831854820251465));
        this.headYaw = this.getYaw();
        this.brain = this.deserializeBrain(BRAIN);
    }

    @Override
    @Nullable
    public LivingEntity getEntity() {
        return this;
    }

    @Contract(pure=true)
    protected EntityEquipment createEquipment() {
        return new EntityEquipment();
    }

    public Brain<?> getBrain() {
        return this.brain;
    }

    protected Brain.Profile<?> createBrainProfile() {
        return Brain.createProfile(ImmutableList.of(), ImmutableList.of());
    }

    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return this.createBrainProfile().deserialize(dynamic);
    }

    @Override
    public void kill(ServerWorld world) {
        this.damage(world, this.getDamageSources().genericKill(), Float.MAX_VALUE);
    }

    public boolean canTarget(EntityType<?> type) {
        return true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(LIVING_FLAGS, (byte)0);
        builder.add(POTION_SWIRLS, List.of());
        builder.add(POTION_SWIRLS_AMBIENT, false);
        builder.add(STUCK_ARROW_COUNT, 0);
        builder.add(STINGER_COUNT, 0);
        builder.add(HEALTH, Float.valueOf(1.0f));
        builder.add(SLEEPING_POSITION, Optional.empty());
    }

    public static DefaultAttributeContainer.Builder createLivingAttributes() {
        return DefaultAttributeContainer.builder().add(EntityAttributes.MAX_HEALTH).add(EntityAttributes.KNOCKBACK_RESISTANCE).add(EntityAttributes.MOVEMENT_SPEED).add(EntityAttributes.ARMOR).add(EntityAttributes.ARMOR_TOUGHNESS).add(EntityAttributes.MAX_ABSORPTION).add(EntityAttributes.STEP_HEIGHT).add(EntityAttributes.SCALE).add(EntityAttributes.GRAVITY).add(EntityAttributes.SAFE_FALL_DISTANCE).add(EntityAttributes.FALL_DAMAGE_MULTIPLIER).add(EntityAttributes.JUMP_STRENGTH).add(EntityAttributes.OXYGEN_BONUS).add(EntityAttributes.BURNING_TIME).add(EntityAttributes.EXPLOSION_KNOCKBACK_RESISTANCE).add(EntityAttributes.WATER_MOVEMENT_EFFICIENCY).add(EntityAttributes.MOVEMENT_EFFICIENCY).add(EntityAttributes.ATTACK_KNOCKBACK).add(EntityAttributes.CAMERA_DISTANCE).add(EntityAttributes.WAYPOINT_TRANSMIT_RANGE);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        World world;
        if (!this.isTouchingWater()) {
            this.checkWaterState();
        }
        if ((world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            if (onGround && this.fallDistance > 0.0) {
                this.applyMovementEffects(lv, landedPosition);
                double e = Math.max(0, MathHelper.floor(this.getUnsafeFallDistance(this.fallDistance)));
                if (e > 0.0 && !state.isAir()) {
                    double i;
                    double f = this.getX();
                    double g = this.getY();
                    double h = this.getZ();
                    BlockPos lv2 = this.getBlockPos();
                    if (landedPosition.getX() != lv2.getX() || landedPosition.getZ() != lv2.getZ()) {
                        i = f - (double)landedPosition.getX() - 0.5;
                        double j = h - (double)landedPosition.getZ() - 0.5;
                        double k = Math.max(Math.abs(i), Math.abs(j));
                        f = (double)landedPosition.getX() + 0.5 + i / k * 0.5;
                        h = (double)landedPosition.getZ() + 0.5 + j / k * 0.5;
                    }
                    i = Math.min((double)0.2f + e / 15.0, 2.5);
                    int l = (int)(150.0 * i);
                    lv.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), f, g, h, l, 0.0, 0.0, 0.0, 0.15f);
                }
            }
        }
        super.fall(heightDifference, onGround, state, landedPosition);
        if (onGround) {
            this.climbingPos = Optional.empty();
        }
    }

    public boolean canBreatheInWater() {
        return this.getType().isIn(EntityTypeTags.CAN_BREATHE_UNDER_WATER);
    }

    public float getLeaningPitch(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastLeaningPitch, this.leaningPitch);
    }

    public boolean hasLandedInFluid() {
        return this.getVelocity().getY() < (double)1.0E-5f && this.isInFluid();
    }

    @Override
    public void baseTick() {
        LivingEntity lv5;
        World world;
        World world2;
        this.lastHandSwingProgress = this.handSwingProgress;
        if (this.firstUpdate) {
            this.getSleepingPosition().ifPresent(this::setPositionInBed);
        }
        if ((world2 = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world2;
            EnchantmentHelper.onTick(lv, this);
        }
        super.baseTick();
        Profiler lv2 = Profilers.get();
        lv2.push("livingEntityBaseTick");
        if (this.isAlive() && (world = this.getEntityWorld()) instanceof ServerWorld) {
            double e;
            double d;
            ServerWorld lv3 = (ServerWorld)world;
            boolean bl = this instanceof PlayerEntity;
            if (this.isInsideWall()) {
                this.damage(lv3, this.getDamageSources().inWall(), 1.0f);
            } else if (bl && !lv3.getWorldBorder().contains(this.getBoundingBox()) && (d = lv3.getWorldBorder().getDistanceInsideBorder(this) + lv3.getWorldBorder().getSafeZone()) < 0.0 && (e = lv3.getWorldBorder().getDamagePerBlock()) > 0.0) {
                this.damage(lv3, this.getDamageSources().outsideBorder(), Math.max(1, MathHelper.floor(-d * e)));
            }
            if (this.isSubmergedIn(FluidTags.WATER) && !lv3.getBlockState(BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ())).isOf(Blocks.BUBBLE_COLUMN)) {
                boolean bl2;
                boolean bl3 = bl2 = !this.canBreatheInWater() && !StatusEffectUtil.hasWaterBreathing(this) && (!bl || !((PlayerEntity)this).getAbilities().invulnerable);
                if (bl2) {
                    this.setAir(this.getNextAirUnderwater(this.getAir()));
                    if (this.shouldDrown()) {
                        this.setAir(0);
                        lv3.sendEntityStatus(this, EntityStatuses.ADD_BUBBLE_PARTICLES);
                        this.damage(lv3, this.getDamageSources().drown(), 2.0f);
                    }
                } else if (this.getAir() < this.getMaxAir()) {
                    this.setAir(this.getNextAirOnLand(this.getAir()));
                }
                if (this.hasVehicle() && this.getVehicle() != null && this.getVehicle().shouldDismountUnderwater()) {
                    this.stopRiding();
                }
            } else if (this.getAir() < this.getMaxAir()) {
                this.setAir(this.getNextAirOnLand(this.getAir()));
            }
            BlockPos lv4 = this.getBlockPos();
            if (!Objects.equal(this.lastBlockPos, lv4)) {
                this.lastBlockPos = lv4;
                this.applyMovementEffects(lv3, lv4);
            }
        }
        if (this.hurtTime > 0) {
            --this.hurtTime;
        }
        if (this.timeUntilRegen > 0 && !(this instanceof ServerPlayerEntity)) {
            --this.timeUntilRegen;
        }
        if (this.isDead() && this.getEntityWorld().shouldUpdatePostDeath(this)) {
            this.updatePostDeath();
        }
        if (this.playerHitTimer > 0) {
            --this.playerHitTimer;
        } else {
            this.attackingPlayer = null;
        }
        if (this.attacking != null && !this.attacking.isAlive()) {
            this.attacking = null;
        }
        if ((lv5 = this.getAttacker()) != null) {
            if (!lv5.isAlive()) {
                this.setAttacker(null);
            } else if (this.age - this.lastAttackedTime > 100) {
                this.setAttacker(null);
            }
        }
        this.tickStatusEffects();
        this.lastHeadYaw = this.headYaw;
        this.lastBodyYaw = this.bodyYaw;
        this.lastYaw = this.getYaw();
        this.lastPitch = this.getPitch();
        lv2.pop();
    }

    protected boolean shouldDrown() {
        return this.getAir() <= -20;
    }

    @Override
    protected float getVelocityMultiplier() {
        return MathHelper.lerp((float)this.getAttributeValue(EntityAttributes.MOVEMENT_EFFICIENCY), super.getVelocityMultiplier(), 1.0f);
    }

    public float getLuck() {
        return 0.0f;
    }

    protected void removePowderSnowSlow() {
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (lv == null) {
            return;
        }
        if (lv.getModifier(POWDER_SNOW_SPEED_MODIFIER_ID) != null) {
            lv.removeModifier(POWDER_SNOW_SPEED_MODIFIER_ID);
        }
    }

    protected void addPowderSnowSlowIfNeeded() {
        int i;
        if (!this.getLandingBlockState().isAir() && (i = this.getFrozenTicks()) > 0) {
            EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
            if (lv == null) {
                return;
            }
            float f = -0.05f * this.getFreezingScale();
            lv.addTemporaryModifier(new EntityAttributeModifier(POWDER_SNOW_SPEED_MODIFIER_ID, f, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    protected void applyMovementEffects(ServerWorld world, BlockPos pos) {
        EnchantmentHelper.applyLocationBasedEffects(world, this);
    }

    public boolean isBaby() {
        return false;
    }

    public float getScaleFactor() {
        return this.isBaby() ? 0.5f : 1.0f;
    }

    public final float getScale() {
        AttributeContainer lv = this.getAttributes();
        if (lv == null) {
            return 1.0f;
        }
        return this.clampScale((float)lv.getValue(EntityAttributes.SCALE));
    }

    protected float clampScale(float scale) {
        return scale;
    }

    public boolean shouldSwimInFluids() {
        return true;
    }

    protected void updatePostDeath() {
        ++this.deathTime;
        if (this.deathTime >= 20 && !this.getEntityWorld().isClient() && !this.isRemoved()) {
            this.getEntityWorld().sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public boolean shouldDropExperience() {
        return !this.isBaby();
    }

    protected boolean shouldDropLoot(ServerWorld world) {
        return !this.isBaby() && world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT);
    }

    protected int getNextAirUnderwater(int air) {
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.OXYGEN_BONUS);
        double d = lv != null ? lv.getValue() : 0.0;
        if (d > 0.0 && this.random.nextDouble() >= 1.0 / (d + 1.0)) {
            return air;
        }
        return air - 1;
    }

    protected int getNextAirOnLand(int air) {
        return Math.min(air + 4, this.getMaxAir());
    }

    public final int getExperienceToDrop(ServerWorld world, @Nullable Entity attacker) {
        return EnchantmentHelper.getMobExperience(world, attacker, this, this.getExperienceToDrop(world));
    }

    protected int getExperienceToDrop(ServerWorld world) {
        return 0;
    }

    protected boolean shouldAlwaysDropExperience() {
        return false;
    }

    @Nullable
    public LivingEntity getAttacker() {
        return LazyEntityReference.getLivingEntity(this.attackerReference, this.getEntityWorld());
    }

    @Nullable
    public PlayerEntity getAttackingPlayer() {
        return LazyEntityReference.getPlayerEntity(this.attackingPlayer, this.getEntityWorld());
    }

    @Override
    public LivingEntity getLastAttacker() {
        return this.getAttacker();
    }

    public int getLastAttackedTime() {
        return this.lastAttackedTime;
    }

    public void setAttacking(PlayerEntity attackingPlayer, int playerHitTimer) {
        this.setAttacking(LazyEntityReference.of(attackingPlayer), playerHitTimer);
    }

    public void setAttacking(UUID attackingPlayer, int playerHitTimer) {
        this.setAttacking(LazyEntityReference.ofUUID(attackingPlayer), playerHitTimer);
    }

    private void setAttacking(LazyEntityReference<PlayerEntity> attackingPlayer, int playerHitTimer) {
        this.attackingPlayer = attackingPlayer;
        this.playerHitTimer = playerHitTimer;
    }

    public void setAttacker(@Nullable LivingEntity attacker) {
        this.attackerReference = LazyEntityReference.of(attacker);
        this.lastAttackedTime = this.age;
    }

    @Nullable
    public LivingEntity getAttacking() {
        return this.attacking;
    }

    public int getLastAttackTime() {
        return this.lastAttackTime;
    }

    public void onAttacking(Entity target) {
        this.attacking = target instanceof LivingEntity ? (LivingEntity)target : null;
        this.lastAttackTime = this.age;
    }

    public int getDespawnCounter() {
        return this.despawnCounter;
    }

    public void setDespawnCounter(int despawnCounter) {
        this.despawnCounter = despawnCounter;
    }

    public boolean hasNoDrag() {
        return this.noDrag;
    }

    public void setNoDrag(boolean noDrag) {
        this.noDrag = noDrag;
    }

    protected boolean isArmorSlot(EquipmentSlot slot) {
        return true;
    }

    public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        if (this.getEntityWorld().isClient() || this.isSpectator()) {
            return;
        }
        if (ItemStack.areItemsAndComponentsEqual(oldStack, newStack) || this.firstUpdate) {
            return;
        }
        EquippableComponent lv = newStack.get(DataComponentTypes.EQUIPPABLE);
        if (!this.isSilent() && lv != null && slot == lv.slot()) {
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), this.getEquipSound(slot, newStack, lv), this.getSoundCategory(), 1.0f, 1.0f, this.random.nextLong());
        }
        if (this.isArmorSlot(slot)) {
            this.emitGameEvent(lv != null ? GameEvent.EQUIP : GameEvent.UNEQUIP);
        }
    }

    protected RegistryEntry<SoundEvent> getEquipSound(EquipmentSlot slot, ItemStack stack, EquippableComponent equippableComponent) {
        return equippableComponent.equipSound();
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        World world;
        if ((reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED) && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            this.onRemoval(lv, reason);
        }
        super.remove(reason);
        this.brain.forgetAll();
    }

    @Override
    public void onRemove(Entity.RemovalReason reason) {
        super.onRemove(reason);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            lv.getWaypointHandler().onUntrack(this);
        }
    }

    protected void onRemoval(ServerWorld world, Entity.RemovalReason reason) {
        for (StatusEffectInstance lv : this.getStatusEffects()) {
            lv.onEntityRemoval(world, this, reason);
        }
        this.activeStatusEffects.clear();
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putFloat(HEALTH_KEY, this.getHealth());
        view.putShort(HURT_TIME_KEY, (short)this.hurtTime);
        view.putInt(HURT_BY_TIMESTAMP_KEY, this.lastAttackedTime);
        view.putShort(DEATH_TIME_KEY, (short)this.deathTime);
        view.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
        view.put(ATTRIBUTES_KEY, EntityAttributeInstance.Packed.LIST_CODEC, this.getAttributes().pack());
        if (!this.activeStatusEffects.isEmpty()) {
            view.put(ACTIVE_EFFECTS_KEY, StatusEffectInstance.CODEC.listOf(), List.copyOf(this.activeStatusEffects.values()));
        }
        view.putBoolean(FALL_FLYING_KEY, this.isGliding());
        this.getSleepingPosition().ifPresent(pos -> view.put(SLEEPING_POS_KEY, BlockPos.CODEC, pos));
        DataResult<Dynamic> dataResult = this.brain.encode(NbtOps.INSTANCE).map(arg -> new Dynamic<NbtElement>(NbtOps.INSTANCE, (NbtElement)arg));
        dataResult.resultOrPartial(LOGGER::error).ifPresent(brain -> view.put(BRAIN_KEY, Codec.PASSTHROUGH, brain));
        if (this.attackingPlayer != null) {
            this.attackingPlayer.writeData(view, "last_hurt_by_player");
            view.putInt("last_hurt_by_player_memory_time", this.playerHitTimer);
        }
        if (this.attackerReference != null) {
            this.attackerReference.writeData(view, "last_hurt_by_mob");
            view.putInt("ticks_since_last_hurt_by_mob", this.age - this.lastAttackedTime);
        }
        if (!this.equipment.isEmpty()) {
            view.put(EQUIPMENT_KEY, EntityEquipment.CODEC, this.equipment);
        }
        if (this.waypointConfig.hasCustomStyle()) {
            view.put("locator_bar_icon", Waypoint.Config.CODEC, this.waypointConfig);
        }
    }

    @Nullable
    public ItemEntity dropItem(ItemStack stack, boolean dropAtSelf, boolean retainOwnership) {
        if (stack.isEmpty()) {
            return null;
        }
        if (this.getEntityWorld().isClient()) {
            this.swingHand(Hand.MAIN_HAND);
            return null;
        }
        ItemEntity lv = this.createItemEntity(stack, dropAtSelf, retainOwnership);
        if (lv != null) {
            this.getEntityWorld().spawnEntity(lv);
        }
        return lv;
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setAbsorptionAmountUnclamped(view.getFloat("AbsorptionAmount", 0.0f));
        if (this.getEntityWorld() != null && !this.getEntityWorld().isClient()) {
            view.read(ATTRIBUTES_KEY, EntityAttributeInstance.Packed.LIST_CODEC).ifPresent(this.getAttributes()::unpack);
        }
        List list = view.read(ACTIVE_EFFECTS_KEY, StatusEffectInstance.CODEC.listOf()).orElse(List.of());
        this.activeStatusEffects.clear();
        for (StatusEffectInstance lv : list) {
            this.activeStatusEffects.put(lv.getEffectType(), lv);
        }
        this.setHealth(view.getFloat(HEALTH_KEY, this.getMaxHealth()));
        this.hurtTime = view.getShort(HURT_TIME_KEY, (short)0);
        this.deathTime = view.getShort(DEATH_TIME_KEY, (short)0);
        this.lastAttackedTime = view.getInt(HURT_BY_TIMESTAMP_KEY, 0);
        view.getOptionalString("Team").ifPresent(team -> {
            boolean bl;
            Scoreboard lv = this.getEntityWorld().getScoreboard();
            Team lv2 = lv.getTeam((String)team);
            boolean bl2 = bl = lv2 != null && lv.addScoreHolderToTeam(this.getUuidAsString(), lv2);
            if (!bl) {
                LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", team);
            }
        });
        this.setFlag(Entity.GLIDING_FLAG_INDEX, view.getBoolean(FALL_FLYING_KEY, false));
        view.read(SLEEPING_POS_KEY, BlockPos.CODEC).ifPresentOrElse(pos -> {
            this.setSleepingPosition((BlockPos)pos);
            this.dataTracker.set(POSE, EntityPose.SLEEPING);
            if (!this.firstUpdate) {
                this.setPositionInBed((BlockPos)pos);
            }
        }, this::clearSleepingPosition);
        view.read(BRAIN_KEY, Codec.PASSTHROUGH).ifPresent(brain -> {
            this.brain = this.deserializeBrain((Dynamic<?>)brain);
        });
        this.attackingPlayer = LazyEntityReference.fromData(view, "last_hurt_by_player");
        this.playerHitTimer = view.getInt("last_hurt_by_player_memory_time", 0);
        this.attackerReference = LazyEntityReference.fromData(view, "last_hurt_by_mob");
        this.lastAttackedTime = view.getInt("ticks_since_last_hurt_by_mob", 0) + this.age;
        this.equipment.copyFrom(view.read(EQUIPMENT_KEY, EntityEquipment.CODEC).orElseGet(EntityEquipment::new));
        this.waypointConfig = view.read("locator_bar_icon", Waypoint.Config.CODEC).orElseGet(Waypoint.Config::new);
    }

    protected void tickStatusEffects() {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            Iterator<Object> iterator = this.activeStatusEffects.keySet().iterator();
            try {
                while (iterator.hasNext()) {
                    RegistryEntry lv2 = (RegistryEntry)iterator.next();
                    StatusEffectInstance lv3 = this.activeStatusEffects.get(lv2);
                    if (!lv3.update(lv, this, () -> this.onStatusEffectUpgraded(lv3, true, null))) {
                        iterator.remove();
                        this.onStatusEffectsRemoved(List.of(lv3));
                        continue;
                    }
                    if (lv3.getDuration() % 600 != 0) continue;
                    this.onStatusEffectUpgraded(lv3, false, null);
                }
            } catch (ConcurrentModificationException lv2) {
                // empty catch block
            }
            if (this.effectsChanged) {
                this.updatePotionVisibility();
                this.updateGlowing();
                this.effectsChanged = false;
            }
        } else {
            for (StatusEffectInstance lv4 : this.activeStatusEffects.values()) {
                lv4.tickClient();
            }
            List<ParticleEffect> list = this.dataTracker.get(POTION_SWIRLS);
            if (!list.isEmpty()) {
                int j;
                boolean bl = this.dataTracker.get(POTION_SWIRLS_AMBIENT);
                int i = this.isInvisible() ? 15 : 4;
                int n = j = bl ? 5 : 1;
                if (this.random.nextInt(i * j) == 0) {
                    this.getEntityWorld().addParticleClient(Util.getRandom(list, this.random), this.getParticleX(0.5), this.getRandomBodyY(), this.getParticleZ(0.5), 1.0, 1.0, 1.0);
                }
            }
        }
    }

    protected void updatePotionVisibility() {
        if (this.activeStatusEffects.isEmpty()) {
            this.clearPotionSwirls();
            this.setInvisible(false);
            return;
        }
        this.setInvisible(this.hasStatusEffect(StatusEffects.INVISIBILITY));
        this.updatePotionSwirls();
    }

    private void updatePotionSwirls() {
        List<ParticleEffect> list = this.activeStatusEffects.values().stream().filter(StatusEffectInstance::shouldShowParticles).map(StatusEffectInstance::createParticle).toList();
        this.dataTracker.set(POTION_SWIRLS, list);
        this.dataTracker.set(POTION_SWIRLS_AMBIENT, LivingEntity.containsOnlyAmbientEffects(this.activeStatusEffects.values()));
    }

    private void updateGlowing() {
        boolean bl = this.isGlowing();
        if (this.getFlag(Entity.GLOWING_FLAG_INDEX) != bl) {
            this.setFlag(Entity.GLOWING_FLAG_INDEX, bl);
        }
    }

    public double getAttackDistanceScalingFactor(@Nullable Entity entity) {
        double d = 1.0;
        if (this.isSneaky()) {
            d *= 0.8;
        }
        if (this.isInvisible()) {
            float f = this.getArmorVisibility();
            if (f < 0.1f) {
                f = 0.1f;
            }
            d *= 0.7 * (double)f;
        }
        if (entity != null) {
            ItemStack lv = this.getEquippedStack(EquipmentSlot.HEAD);
            EntityType<?> lv2 = entity.getType();
            if (lv2 == EntityType.SKELETON && lv.isOf(Items.SKELETON_SKULL) || lv2 == EntityType.ZOMBIE && lv.isOf(Items.ZOMBIE_HEAD) || lv2 == EntityType.PIGLIN && lv.isOf(Items.PIGLIN_HEAD) || lv2 == EntityType.PIGLIN_BRUTE && lv.isOf(Items.PIGLIN_HEAD) || lv2 == EntityType.CREEPER && lv.isOf(Items.CREEPER_HEAD)) {
                d *= 0.5;
            }
        }
        return d;
    }

    public boolean canTarget(LivingEntity target) {
        if (target instanceof PlayerEntity && this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return target.canTakeDamage();
    }

    public boolean canTakeDamage() {
        return !this.isInvulnerable() && this.isPartOfGame();
    }

    public boolean isPartOfGame() {
        return !this.isSpectator() && this.isAlive();
    }

    public static boolean containsOnlyAmbientEffects(Collection<StatusEffectInstance> effects) {
        for (StatusEffectInstance lv : effects) {
            if (!lv.shouldShowParticles() || lv.isAmbient()) continue;
            return false;
        }
        return true;
    }

    protected void clearPotionSwirls() {
        this.dataTracker.set(POTION_SWIRLS, List.of());
    }

    public boolean clearStatusEffects() {
        if (this.getEntityWorld().isClient()) {
            return false;
        }
        if (this.activeStatusEffects.isEmpty()) {
            return false;
        }
        HashMap<RegistryEntry<StatusEffect>, StatusEffectInstance> map = Maps.newHashMap(this.activeStatusEffects);
        this.activeStatusEffects.clear();
        this.onStatusEffectsRemoved(map.values());
        return true;
    }

    public Collection<StatusEffectInstance> getStatusEffects() {
        return this.activeStatusEffects.values();
    }

    public Map<RegistryEntry<StatusEffect>, StatusEffectInstance> getActiveStatusEffects() {
        return this.activeStatusEffects;
    }

    public boolean hasStatusEffect(RegistryEntry<StatusEffect> effect) {
        return this.activeStatusEffects.containsKey(effect);
    }

    @Nullable
    public StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect) {
        return this.activeStatusEffects.get(effect);
    }

    public float getEffectFadeFactor(RegistryEntry<StatusEffect> effect, float tickProgress) {
        StatusEffectInstance lv = this.getStatusEffect(effect);
        if (lv != null) {
            return lv.getFadeFactor(this, tickProgress);
        }
        return 0.0f;
    }

    public final boolean addStatusEffect(StatusEffectInstance effect) {
        return this.addStatusEffect(effect, null);
    }

    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (!this.canHaveStatusEffect(effect)) {
            return false;
        }
        StatusEffectInstance lv = this.activeStatusEffects.get(effect.getEffectType());
        boolean bl = false;
        if (lv == null) {
            this.activeStatusEffects.put(effect.getEffectType(), effect);
            this.onStatusEffectApplied(effect, source);
            bl = true;
            effect.playApplySound(this);
        } else if (lv.upgrade(effect)) {
            this.onStatusEffectUpgraded(lv, true, source);
            bl = true;
        }
        effect.onApplied(this);
        return bl;
    }

    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        if (this.getType().isIn(EntityTypeTags.IMMUNE_TO_INFESTED)) {
            return !effect.equals(StatusEffects.INFESTED);
        }
        if (this.getType().isIn(EntityTypeTags.IMMUNE_TO_OOZING)) {
            return !effect.equals(StatusEffects.OOZING);
        }
        if (this.getType().isIn(EntityTypeTags.IGNORES_POISON_AND_REGEN)) {
            return !effect.equals(StatusEffects.REGENERATION) && !effect.equals(StatusEffects.POISON);
        }
        return true;
    }

    public void setStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (!this.canHaveStatusEffect(effect)) {
            return;
        }
        StatusEffectInstance lv = this.activeStatusEffects.put(effect.getEffectType(), effect);
        if (lv == null) {
            this.onStatusEffectApplied(effect, source);
        } else {
            effect.copyFadingFrom(lv);
            this.onStatusEffectUpgraded(effect, true, source);
        }
    }

    public boolean hasInvertedHealingAndHarm() {
        return this.getType().isIn(EntityTypeTags.INVERTED_HEALING_AND_HARM);
    }

    @Nullable
    public final StatusEffectInstance removeStatusEffectInternal(RegistryEntry<StatusEffect> effect) {
        return this.activeStatusEffects.remove(effect);
    }

    public boolean removeStatusEffect(RegistryEntry<StatusEffect> effect) {
        StatusEffectInstance lv = this.removeStatusEffectInternal(effect);
        if (lv != null) {
            this.onStatusEffectsRemoved(List.of(lv));
            return true;
        }
        return false;
    }

    protected void onStatusEffectApplied(StatusEffectInstance effect, @Nullable Entity source) {
        if (!this.getEntityWorld().isClient()) {
            this.effectsChanged = true;
            effect.getEffectType().value().onApplied(this.getAttributes(), effect.getAmplifier());
            this.sendEffectToControllingPlayer(effect);
        }
    }

    public void sendEffectToControllingPlayer(StatusEffectInstance effect) {
        for (Entity lv : this.getPassengerList()) {
            if (!(lv instanceof ServerPlayerEntity)) continue;
            ServerPlayerEntity lv2 = (ServerPlayerEntity)lv;
            lv2.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.getId(), effect, false));
        }
    }

    protected void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, @Nullable Entity source) {
        if (this.getEntityWorld().isClient()) {
            return;
        }
        this.effectsChanged = true;
        if (reapplyEffect) {
            StatusEffect lv = effect.getEffectType().value();
            lv.onRemoved(this.getAttributes());
            lv.onApplied(this.getAttributes(), effect.getAmplifier());
            this.updateAttributes();
        }
        this.sendEffectToControllingPlayer(effect);
    }

    protected void onStatusEffectsRemoved(Collection<StatusEffectInstance> effects) {
        if (this.getEntityWorld().isClient()) {
            return;
        }
        this.effectsChanged = true;
        for (StatusEffectInstance lv : effects) {
            lv.getEffectType().value().onRemoved(this.getAttributes());
            for (Entity lv2 : this.getPassengerList()) {
                if (!(lv2 instanceof ServerPlayerEntity)) continue;
                ServerPlayerEntity lv3 = (ServerPlayerEntity)lv2;
                lv3.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(this.getId(), lv.getEffectType()));
            }
        }
        this.updateAttributes();
    }

    private void updateAttributes() {
        Set<EntityAttributeInstance> set = this.getAttributes().getPendingUpdate();
        for (EntityAttributeInstance lv : set) {
            this.updateAttribute(lv.getAttribute());
        }
        set.clear();
    }

    protected void updateAttribute(RegistryEntry<EntityAttribute> attribute) {
        World world;
        if (attribute.matches(EntityAttributes.MAX_HEALTH)) {
            float f = this.getMaxHealth();
            if (this.getHealth() > f) {
                this.setHealth(f);
            }
        } else if (attribute.matches(EntityAttributes.MAX_ABSORPTION)) {
            float f = this.getMaxAbsorption();
            if (this.getAbsorptionAmount() > f) {
                this.setAbsorptionAmount(f);
            }
        } else if (attribute.matches(EntityAttributes.SCALE)) {
            this.calculateDimensions();
        } else if (attribute.matches(EntityAttributes.WAYPOINT_TRANSMIT_RANGE) && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            ServerWaypointHandler lv2 = lv.getWaypointHandler();
            if (this.attributes.getValue(attribute) > 0.0) {
                lv2.onTrack(this);
            } else {
                lv2.onUntrack(this);
            }
        }
    }

    public void heal(float amount) {
        float g = this.getHealth();
        if (g > 0.0f) {
            this.setHealth(g + amount);
        }
    }

    public float getHealth() {
        return this.dataTracker.get(HEALTH).floatValue();
    }

    public void setHealth(float health) {
        this.dataTracker.set(HEALTH, Float.valueOf(MathHelper.clamp(health, 0.0f, this.getMaxHealth())));
    }

    public boolean isDead() {
        return this.getHealth() <= 0.0f;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        Entity entity;
        boolean bl3;
        boolean bl;
        if (this.isInvulnerableTo(world, source)) {
            return false;
        }
        if (this.isDead()) {
            return false;
        }
        if (source.isIn(DamageTypeTags.IS_FIRE) && this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            return false;
        }
        if (this.isSleeping()) {
            this.wakeUp();
        }
        this.despawnCounter = 0;
        if (amount < 0.0f) {
            amount = 0.0f;
        }
        float g = amount;
        ItemStack lv = this.getActiveItem();
        float h = this.getDamageBlockedAmount(world, source, amount);
        amount -= h;
        boolean bl2 = bl = h > 0.0f;
        if (source.isIn(DamageTypeTags.IS_FREEZING) && this.getType().isIn(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
            amount *= 5.0f;
        }
        if (source.isIn(DamageTypeTags.DAMAGES_HELMET) && !this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            this.damageHelmet(source, amount);
            amount *= 0.75f;
        }
        if (Float.isNaN(amount) || Float.isInfinite(amount)) {
            amount = Float.MAX_VALUE;
        }
        boolean bl22 = true;
        if ((float)this.timeUntilRegen > 10.0f && !source.isIn(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if (amount <= this.lastDamageTaken) {
                return false;
            }
            this.applyDamage(world, source, amount - this.lastDamageTaken);
            this.lastDamageTaken = amount;
            bl22 = false;
        } else {
            this.lastDamageTaken = amount;
            this.timeUntilRegen = 20;
            this.applyDamage(world, source, amount);
            this.hurtTime = this.maxHurtTime = 10;
        }
        this.becomeAngry(source);
        this.setAttackingPlayer(source);
        if (bl22) {
            BlocksAttacksComponent lv2 = lv.get(DataComponentTypes.BLOCKS_ATTACKS);
            if (bl && lv2 != null) {
                lv2.playBlockSound(world, this);
            } else {
                world.sendEntityDamage(this, source);
            }
            if (!(source.isIn(DamageTypeTags.NO_IMPACT) || bl && !(amount > 0.0f))) {
                this.scheduleVelocityUpdate();
            }
            if (!source.isIn(DamageTypeTags.NO_KNOCKBACK)) {
                double d = 0.0;
                double e = 0.0;
                Entity entity2 = source.getSource();
                if (entity2 instanceof ProjectileEntity) {
                    ProjectileEntity lv3 = (ProjectileEntity)entity2;
                    DoubleDoubleImmutablePair doubleDoubleImmutablePair = lv3.getKnockback(this, source);
                    d = -doubleDoubleImmutablePair.leftDouble();
                    e = -doubleDoubleImmutablePair.rightDouble();
                } else if (source.getPosition() != null) {
                    d = source.getPosition().getX() - this.getX();
                    e = source.getPosition().getZ() - this.getZ();
                }
                this.takeKnockback(0.4f, d, e);
                if (!bl) {
                    this.tiltScreen(d, e);
                }
            }
        }
        if (this.isDead()) {
            if (!this.tryUseDeathProtector(source)) {
                if (bl22) {
                    this.playSound(this.getDeathSound());
                    this.playThornsSound(source);
                }
                this.onDeath(source);
            }
        } else if (bl22) {
            this.playHurtSound(source);
            this.playThornsSound(source);
        }
        boolean bl4 = bl3 = !bl || amount > 0.0f;
        if (bl3) {
            this.lastDamageSource = source;
            this.lastDamageTime = this.getEntityWorld().getTime();
            for (StatusEffectInstance lv4 : this.getStatusEffects()) {
                lv4.onEntityDamage(world, this, source, amount);
            }
        }
        if ((entity = this) instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv5 = (ServerPlayerEntity)entity;
            Criteria.ENTITY_HURT_PLAYER.trigger(lv5, source, g, amount, bl);
            if (h > 0.0f && h < 3.4028235E37f) {
                lv5.increaseStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(h * 10.0f));
            }
        }
        if ((entity = source.getAttacker()) instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv5 = (ServerPlayerEntity)entity;
            Criteria.PLAYER_HURT_ENTITY.trigger(lv5, this, source, g, amount, bl);
        }
        return bl3;
    }

    public float getDamageBlockedAmount(ServerWorld world, DamageSource source, float amount) {
        Entity lv7;
        double d;
        PersistentProjectileEntity lv3;
        BlocksAttacksComponent lv2;
        ItemStack lv;
        block10: {
            block9: {
                if (amount <= 0.0f) {
                    return 0.0f;
                }
                lv = this.getBlockingItem();
                if (lv == null) {
                    return 0.0f;
                }
                lv2 = lv.get(DataComponentTypes.BLOCKS_ATTACKS);
                if (lv2 == null) break block9;
                if (!lv2.bypassedBy().map(source::isIn).orElse(false).booleanValue()) break block10;
            }
            return 0.0f;
        }
        Entity entity = source.getSource();
        if (entity instanceof PersistentProjectileEntity && (lv3 = (PersistentProjectileEntity)entity).getPierceLevel() > 0) {
            return 0.0f;
        }
        Vec3d lv4 = source.getPosition();
        if (lv4 != null) {
            Vec3d lv5 = this.getRotationVector(0.0f, this.getHeadYaw());
            Vec3d lv6 = lv4.subtract(this.getEntityPos());
            lv6 = new Vec3d(lv6.x, 0.0, lv6.z).normalize();
            d = Math.acos(lv6.dotProduct(lv5));
        } else {
            d = 3.1415927410125732;
        }
        float g = lv2.getDamageReductionAmount(source, amount, d);
        lv2.onShieldHit(this.getEntityWorld(), lv, this, this.getActiveHand(), g);
        if (g > 0.0f && !source.isIn(DamageTypeTags.IS_PROJECTILE) && (lv7 = source.getSource()) instanceof LivingEntity) {
            LivingEntity lv8 = (LivingEntity)lv7;
            this.takeShieldHit(world, lv8);
        }
        return g;
    }

    private void playThornsSound(DamageSource damageSource) {
        if (damageSource.isOf(DamageTypes.THORNS)) {
            SoundCategory lv = this instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE;
            this.getEntityWorld().playSound(null, this.getEntityPos().x, this.getEntityPos().y, this.getEntityPos().z, SoundEvents.ENCHANT_THORNS_HIT, lv);
        }
    }

    protected void becomeAngry(DamageSource damageSource) {
        Entity entity = damageSource.getAttacker();
        if (entity instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            if (!(damageSource.isIn(DamageTypeTags.NO_ANGER) || damageSource.isOf(DamageTypes.WIND_CHARGE) && this.getType().isIn(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE))) {
                this.setAttacker(lv);
            }
        }
    }

    @Nullable
    protected PlayerEntity setAttackingPlayer(DamageSource damageSource) {
        WolfEntity lv3;
        Entity lv = damageSource.getAttacker();
        if (lv instanceof PlayerEntity) {
            PlayerEntity lv2 = (PlayerEntity)lv;
            this.setAttacking(lv2, 100);
        } else if (lv instanceof WolfEntity && (lv3 = (WolfEntity)lv).isTamed()) {
            if (lv3.getOwnerReference() != null) {
                this.setAttacking(lv3.getOwnerReference().getUuid(), 100);
            } else {
                this.attackingPlayer = null;
                this.playerHitTimer = 0;
            }
        }
        return LazyEntityReference.getPlayerEntity(this.attackingPlayer, this.getEntityWorld());
    }

    protected void takeShieldHit(ServerWorld world, LivingEntity attacker) {
        attacker.knockback(this);
    }

    protected void knockback(LivingEntity target) {
        target.takeKnockback(0.5, target.getX() - this.getX(), target.getZ() - this.getZ());
    }

    private boolean tryUseDeathProtector(DamageSource source) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        ItemStack lv = null;
        DeathProtectionComponent lv2 = null;
        for (Hand lv3 : Hand.values()) {
            ItemStack lv4 = this.getStackInHand(lv3);
            lv2 = lv4.get(DataComponentTypes.DEATH_PROTECTION);
            if (lv2 == null) continue;
            lv = lv4.copy();
            lv4.decrement(1);
            break;
        }
        if (lv != null) {
            LivingEntity livingEntity = this;
            if (livingEntity instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv5 = (ServerPlayerEntity)livingEntity;
                lv5.incrementStat(Stats.USED.getOrCreateStat(lv.getItem()));
                Criteria.USED_TOTEM.trigger(lv5, lv);
                this.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
            this.setHealth(1.0f);
            lv2.applyDeathEffects(lv, this);
            this.getEntityWorld().sendEntityStatus(this, EntityStatuses.USE_TOTEM_OF_UNDYING);
        }
        return lv2 != null;
    }

    @Nullable
    public DamageSource getRecentDamageSource() {
        if (this.getEntityWorld().getTime() - this.lastDamageTime > 40L) {
            this.lastDamageSource = null;
        }
        return this.lastDamageSource;
    }

    protected void playHurtSound(DamageSource damageSource) {
        this.playSound(this.getHurtSound(damageSource));
    }

    public void playSound(@Nullable SoundEvent sound) {
        if (sound != null) {
            this.playSound(sound, this.getSoundVolume(), this.getSoundPitch());
        }
    }

    private void playEquipmentBreakEffects(ItemStack stack) {
        if (!stack.isEmpty()) {
            RegistryEntry<SoundEvent> lv = stack.get(DataComponentTypes.BREAK_SOUND);
            if (lv != null && !this.isSilent()) {
                this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), lv.value(), this.getSoundCategory(), 0.8f, 0.8f + this.getEntityWorld().random.nextFloat() * 0.4f, false);
            }
            this.spawnItemParticles(stack, 5);
        }
    }

    public void onDeath(DamageSource damageSource) {
        if (this.isRemoved() || this.dead) {
            return;
        }
        Entity lv = damageSource.getAttacker();
        LivingEntity lv2 = this.getPrimeAdversary();
        if (lv2 != null) {
            lv2.updateKilledAdvancementCriterion(this, damageSource);
        }
        if (this.isSleeping()) {
            this.wakeUp();
        }
        if (!this.getEntityWorld().isClient() && this.hasCustomName()) {
            LOGGER.info("Named entity {} died: {}", (Object)this, (Object)this.getDamageTracker().getDeathMessage().getString());
        }
        this.dead = true;
        this.getDamageTracker().update();
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv3 = (ServerWorld)world;
            if (lv == null || lv.onKilledOther(lv3, this, damageSource)) {
                this.emitGameEvent(GameEvent.ENTITY_DIE);
                this.drop(lv3, damageSource);
                this.onKilledBy(lv2);
            }
            this.getEntityWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
        }
        this.setPose(EntityPose.DYING);
    }

    protected void onKilledBy(@Nullable LivingEntity adversary) {
        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld lv = (ServerWorld)world;
        boolean bl = false;
        if (adversary instanceof WitherEntity) {
            if (lv.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                BlockPos lv2 = this.getBlockPos();
                BlockState lv3 = Blocks.WITHER_ROSE.getDefaultState();
                if (this.getEntityWorld().getBlockState(lv2).isAir() && lv3.canPlaceAt(this.getEntityWorld(), lv2)) {
                    this.getEntityWorld().setBlockState(lv2, lv3, Block.NOTIFY_ALL);
                    bl = true;
                }
            }
            if (!bl) {
                ItemEntity lv4 = new ItemEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
                this.getEntityWorld().spawnEntity(lv4);
            }
        }
    }

    protected void drop(ServerWorld world, DamageSource damageSource) {
        boolean bl;
        boolean bl2 = bl = this.playerHitTimer > 0;
        if (this.shouldDropLoot(world)) {
            this.dropLoot(world, damageSource, bl);
            this.dropEquipment(world, damageSource, bl);
        }
        this.dropInventory(world);
        this.dropExperience(world, damageSource.getAttacker());
    }

    protected void dropInventory(ServerWorld world) {
    }

    protected void dropExperience(ServerWorld world, @Nullable Entity attacker) {
        if (!this.isExperienceDroppingDisabled() && (this.shouldAlwaysDropExperience() || this.playerHitTimer > 0 && this.shouldDropExperience() && world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT))) {
            ExperienceOrbEntity.spawn(world, this.getEntityPos(), this.getExperienceToDrop(world, attacker));
        }
    }

    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
    }

    public long getLootTableSeed() {
        return 0L;
    }

    protected float getAttackKnockbackAgainst(Entity target, DamageSource damageSource) {
        float f = (float)this.getAttributeValue(EntityAttributes.ATTACK_KNOCKBACK);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            return EnchantmentHelper.modifyKnockback(lv, this.getWeaponStack(), target, damageSource, f);
        }
        return f;
    }

    protected void dropLoot(ServerWorld world, DamageSource damageSource, boolean causedByPlayer) {
        Optional<RegistryKey<LootTable>> optional = this.getLootTableKey();
        if (optional.isEmpty()) {
            return;
        }
        this.dropLoot(world, damageSource, causedByPlayer, optional.get());
    }

    public void dropLoot(ServerWorld world, DamageSource damageSource, boolean causedByPlayer, RegistryKey<LootTable> lootTableKey) {
        this.generateLoot(world, damageSource, causedByPlayer, lootTableKey, stack -> this.dropStack(world, (ItemStack)stack));
    }

    public void generateLoot(ServerWorld world, DamageSource damageSource, boolean causedByPlayer, RegistryKey<LootTable> lootTableKey, Consumer<ItemStack> lootConsumer) {
        LootTable lv = world.getServer().getReloadableRegistries().getLootTable(lootTableKey);
        LootWorldContext.Builder lv2 = new LootWorldContext.Builder(world).add(LootContextParameters.THIS_ENTITY, this).add(LootContextParameters.ORIGIN, this.getEntityPos()).add(LootContextParameters.DAMAGE_SOURCE, damageSource).addOptional(LootContextParameters.ATTACKING_ENTITY, damageSource.getAttacker()).addOptional(LootContextParameters.DIRECT_ATTACKING_ENTITY, damageSource.getSource());
        PlayerEntity lv3 = this.getAttackingPlayer();
        if (causedByPlayer && lv3 != null) {
            lv2 = lv2.add(LootContextParameters.LAST_DAMAGE_PLAYER, lv3).luck(lv3.getLuck());
        }
        LootWorldContext lv4 = lv2.build(LootContextTypes.ENTITY);
        lv.generateLoot(lv4, this.getLootTableSeed(), lootConsumer);
    }

    public boolean forEachBrushedItem(ServerWorld world, RegistryKey<LootTable> lootTableKey, @Nullable Entity interactingEntity, ItemStack tool, BiConsumer<ServerWorld, ItemStack> lootConsumer) {
        return this.forEachGeneratedItem(world, lootTableKey, parameterSetBuilder -> parameterSetBuilder.add(LootContextParameters.TARGET_ENTITY, this).addOptional(LootContextParameters.INTERACTING_ENTITY, interactingEntity).add(LootContextParameters.TOOL, tool).build(LootContextTypes.ENTITY_INTERACT), lootConsumer);
    }

    public boolean forEachGiftedItem(ServerWorld world, RegistryKey<LootTable> lootTableKey, BiConsumer<ServerWorld, ItemStack> lootConsumer) {
        return this.forEachGeneratedItem(world, lootTableKey, parameterSetBuilder -> parameterSetBuilder.add(LootContextParameters.ORIGIN, this.getEntityPos()).add(LootContextParameters.THIS_ENTITY, this).build(LootContextTypes.GIFT), lootConsumer);
    }

    protected void forEachShearedItem(ServerWorld world, RegistryKey<LootTable> lootTableKey, ItemStack tool, BiConsumer<ServerWorld, ItemStack> lootConsumer) {
        this.forEachGeneratedItem(world, lootTableKey, parameterSetBuilder -> parameterSetBuilder.add(LootContextParameters.ORIGIN, this.getEntityPos()).add(LootContextParameters.THIS_ENTITY, this).add(LootContextParameters.TOOL, tool).build(LootContextTypes.SHEARING), lootConsumer);
    }

    protected boolean forEachGeneratedItem(ServerWorld world, RegistryKey<LootTable> lootTableKey, Function<LootWorldContext.Builder, LootWorldContext> lootContextParametersFactory, BiConsumer<ServerWorld, ItemStack> lootConsumer) {
        LootWorldContext lv2;
        LootTable lv = world.getServer().getReloadableRegistries().getLootTable(lootTableKey);
        ObjectArrayList<ItemStack> list = lv.generateLoot(lv2 = lootContextParametersFactory.apply(new LootWorldContext.Builder(world)));
        if (!list.isEmpty()) {
            list.forEach(stack -> lootConsumer.accept(world, (ItemStack)stack));
            return true;
        }
        return false;
    }

    public void takeKnockback(double strength, double x, double z) {
        if ((strength *= 1.0 - this.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE)) <= 0.0) {
            return;
        }
        this.velocityDirty = true;
        Vec3d lv = this.getVelocity();
        while (x * x + z * z < (double)1.0E-5f) {
            x = (Math.random() - Math.random()) * 0.01;
            z = (Math.random() - Math.random()) * 0.01;
        }
        Vec3d lv2 = new Vec3d(x, 0.0, z).normalize().multiply(strength);
        this.setVelocity(lv.x / 2.0 - lv2.x, this.isOnGround() ? Math.min(0.4, lv.y / 2.0 + strength) : lv.y, lv.z / 2.0 - lv2.z);
    }

    public void tiltScreen(double deltaX, double deltaZ) {
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_GENERIC_HURT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GENERIC_DEATH;
    }

    private SoundEvent getFallSound(int distance) {
        return distance > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
    }

    public void disableExperienceDropping() {
        this.experienceDroppingDisabled = true;
    }

    public boolean isExperienceDroppingDisabled() {
        return this.experienceDroppingDisabled;
    }

    public float getDamageTiltYaw() {
        return 0.0f;
    }

    protected Box getHitbox() {
        Box lv = this.getBoundingBox();
        Entity lv2 = this.getVehicle();
        if (lv2 != null) {
            Vec3d lv3 = lv2.getPassengerRidingPos(this);
            return lv.withMinY(Math.max(lv3.y, lv.minY));
        }
        return lv;
    }

    public Map<Enchantment, Set<EnchantmentLocationBasedEffect>> getLocationBasedEnchantmentEffects(EquipmentSlot slot) {
        return this.locationBasedEnchantmentEffects.computeIfAbsent(slot, arg -> new Reference2ObjectArrayMap());
    }

    public FallSounds getFallSounds() {
        return new FallSounds(SoundEvents.ENTITY_GENERIC_SMALL_FALL, SoundEvents.ENTITY_GENERIC_BIG_FALL);
    }

    public Optional<BlockPos> getClimbingPos() {
        return this.climbingPos;
    }

    public boolean isClimbing() {
        if (this.isSpectator()) {
            return false;
        }
        BlockPos lv = this.getBlockPos();
        BlockState lv2 = this.getBlockStateAtPos();
        if (lv2.isIn(BlockTags.CLIMBABLE)) {
            this.climbingPos = Optional.of(lv);
            return true;
        }
        if (lv2.getBlock() instanceof TrapdoorBlock && this.canEnterTrapdoor(lv, lv2)) {
            this.climbingPos = Optional.of(lv);
            return true;
        }
        return false;
    }

    private boolean canEnterTrapdoor(BlockPos pos, BlockState state) {
        if (state.get(TrapdoorBlock.OPEN).booleanValue()) {
            BlockState lv = this.getEntityWorld().getBlockState(pos.down());
            return lv.isOf(Blocks.LADDER) && lv.get(LadderBlock.FACING) == state.get(TrapdoorBlock.FACING);
        }
        return false;
    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved() && this.getHealth() > 0.0f;
    }

    public boolean isEntityLookingAtMe(LivingEntity entity, double d, boolean bl, boolean visualShape, double ... checkedYs) {
        Vec3d lv = entity.getRotationVec(1.0f).normalize();
        for (double e : checkedYs) {
            Vec3d lv2 = new Vec3d(this.getX() - entity.getX(), e - entity.getEyeY(), this.getZ() - entity.getZ());
            double f = lv2.length();
            lv2 = lv2.normalize();
            double g = lv.dotProduct(lv2);
            double d2 = bl ? f : 1.0;
            if (!(g > 1.0 - d / d2) || !entity.canSee(this, visualShape ? RaycastContext.ShapeType.VISUAL : RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, e)) continue;
            return true;
        }
        return false;
    }

    @Override
    public int getSafeFallDistance() {
        return this.getSafeFallDistance(0.0f);
    }

    protected final int getSafeFallDistance(float health) {
        return MathHelper.floor(health + 3.0f);
    }

    @Override
    public boolean handleFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        boolean bl = super.handleFallDamage(fallDistance, damagePerDistance, damageSource);
        int i = this.computeFallDamage(fallDistance, damagePerDistance);
        if (i > 0) {
            this.playSound(this.getFallSound(i), 1.0f, 1.0f);
            this.playBlockFallSound();
            this.serverDamage(damageSource, i);
            return true;
        }
        return bl;
    }

    protected int computeFallDamage(double fallDistance, float damagePerDistance) {
        if (this.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return 0;
        }
        double e = this.getUnsafeFallDistance(fallDistance);
        return MathHelper.floor(e * (double)damagePerDistance * this.getAttributeValue(EntityAttributes.FALL_DAMAGE_MULTIPLIER));
    }

    private double getUnsafeFallDistance(double fallDistance) {
        return fallDistance + 1.0E-6 - this.getAttributeValue(EntityAttributes.SAFE_FALL_DISTANCE);
    }

    protected void playBlockFallSound() {
        if (this.isSilent()) {
            return;
        }
        int i = MathHelper.floor(this.getX());
        int j = MathHelper.floor(this.getY() - (double)0.2f);
        int k = MathHelper.floor(this.getZ());
        BlockState lv = this.getEntityWorld().getBlockState(new BlockPos(i, j, k));
        if (!lv.isAir()) {
            BlockSoundGroup lv2 = lv.getSoundGroup();
            this.playSound(lv2.getFallSound(), lv2.getVolume() * 0.5f, lv2.getPitch() * 0.75f);
        }
    }

    @Override
    public void animateDamage(float yaw) {
        this.hurtTime = this.maxHurtTime = 10;
    }

    public int getArmor() {
        return MathHelper.floor(this.getAttributeValue(EntityAttributes.ARMOR));
    }

    protected void damageArmor(DamageSource source, float amount) {
    }

    protected void damageHelmet(DamageSource source, float amount) {
    }

    protected void damageEquipment(DamageSource source, float amount, EquipmentSlot ... slots) {
        if (amount <= 0.0f) {
            return;
        }
        int i = (int)Math.max(1.0f, amount / 4.0f);
        for (EquipmentSlot lv : slots) {
            ItemStack lv2 = this.getEquippedStack(lv);
            EquippableComponent lv3 = lv2.get(DataComponentTypes.EQUIPPABLE);
            if (lv3 == null || !lv3.damageOnHurt() || !lv2.isDamageable() || !lv2.takesDamageFrom(source)) continue;
            lv2.damage(i, this, lv);
        }
    }

    protected float applyArmorToDamage(DamageSource source, float amount) {
        if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            this.damageArmor(source, amount);
            amount = DamageUtil.getDamageLeft(this, amount, source, this.getArmor(), (float)this.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));
        }
        return amount;
    }

    protected float modifyAppliedDamage(DamageSource source, float amount) {
        float l;
        int i;
        int j;
        float g;
        float h;
        float k;
        if (source.isIn(DamageTypeTags.BYPASSES_EFFECTS)) {
            return amount;
        }
        if (this.hasStatusEffect(StatusEffects.RESISTANCE) && !source.isIn(DamageTypeTags.BYPASSES_RESISTANCE) && (k = (h = amount) - (amount = Math.max((g = amount * (float)(j = 25 - (i = (this.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5))) / 25.0f, 0.0f))) > 0.0f && k < 3.4028235E37f) {
            if (this instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)this).increaseStat(Stats.DAMAGE_RESISTED, Math.round(k * 10.0f));
            } else if (source.getAttacker() instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)source.getAttacker()).increaseStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(k * 10.0f));
            }
        }
        if (amount <= 0.0f) {
            return 0.0f;
        }
        if (source.isIn(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return amount;
        }
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            l = EnchantmentHelper.getProtectionAmount(lv, this, source);
        } else {
            l = 0.0f;
        }
        if (l > 0.0f) {
            amount = DamageUtil.getInflictedDamage(amount, l);
        }
        return amount;
    }

    protected void applyDamage(ServerWorld world, DamageSource source, float amount) {
        Entity entity;
        if (this.isInvulnerableTo(world, source)) {
            return;
        }
        amount = this.applyArmorToDamage(source, amount);
        float g = amount = this.modifyAppliedDamage(source, amount);
        amount = Math.max(amount - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (g - amount));
        float h = g - amount;
        if (h > 0.0f && h < 3.4028235E37f && (entity = source.getAttacker()) instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            lv.increaseStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(h * 10.0f));
        }
        if (amount == 0.0f) {
            return;
        }
        this.getDamageTracker().onDamage(source, amount);
        this.setHealth(this.getHealth() - amount);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - amount);
        this.emitGameEvent(GameEvent.ENTITY_DAMAGE);
    }

    public DamageTracker getDamageTracker() {
        return this.damageTracker;
    }

    @Nullable
    public LivingEntity getPrimeAdversary() {
        if (this.attackingPlayer != null) {
            return this.attackingPlayer.getEntityByClass(this.getEntityWorld(), PlayerEntity.class);
        }
        if (this.attackerReference != null) {
            return this.attackerReference.getEntityByClass(this.getEntityWorld(), LivingEntity.class);
        }
        return null;
    }

    public final float getMaxHealth() {
        return (float)this.getAttributeValue(EntityAttributes.MAX_HEALTH);
    }

    public final float getMaxAbsorption() {
        return (float)this.getAttributeValue(EntityAttributes.MAX_ABSORPTION);
    }

    public final int getStuckArrowCount() {
        return this.dataTracker.get(STUCK_ARROW_COUNT);
    }

    public final void setStuckArrowCount(int stuckArrowCount) {
        this.dataTracker.set(STUCK_ARROW_COUNT, stuckArrowCount);
    }

    public final int getStingerCount() {
        return this.dataTracker.get(STINGER_COUNT);
    }

    public final void setStingerCount(int stingerCount) {
        this.dataTracker.set(STINGER_COUNT, stingerCount);
    }

    private int getHandSwingDuration() {
        if (StatusEffectUtil.hasHaste(this)) {
            return 6 - (1 + StatusEffectUtil.getHasteAmplifier(this));
        }
        if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            return 6 + (1 + this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2;
        }
        return 6;
    }

    public void swingHand(Hand hand) {
        this.swingHand(hand, false);
    }

    public void swingHand(Hand hand, boolean fromServerPlayer) {
        if (!this.handSwinging || this.handSwingTicks >= this.getHandSwingDuration() / 2 || this.handSwingTicks < 0) {
            this.handSwingTicks = -1;
            this.handSwinging = true;
            this.preferredHand = hand;
            if (this.getEntityWorld() instanceof ServerWorld) {
                EntityAnimationS2CPacket lv = new EntityAnimationS2CPacket(this, hand == Hand.MAIN_HAND ? EntityAnimationS2CPacket.SWING_MAIN_HAND : EntityAnimationS2CPacket.SWING_OFF_HAND);
                ServerChunkManager lv2 = ((ServerWorld)this.getEntityWorld()).getChunkManager();
                if (fromServerPlayer) {
                    lv2.sendToNearbyPlayers(this, lv);
                } else {
                    lv2.sendToOtherNearbyPlayers(this, lv);
                }
            }
        }
    }

    @Override
    public void onDamaged(DamageSource damageSource) {
        this.limbAnimator.setSpeed(1.5f);
        this.timeUntilRegen = 20;
        this.hurtTime = this.maxHurtTime = 10;
        SoundEvent lv = this.getHurtSound(damageSource);
        if (lv != null) {
            this.playSound(lv, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
        this.lastDamageSource = damageSource;
        this.lastDamageTime = this.getEntityWorld().getTime();
    }

    @Override
    public void handleStatus(byte status) {
        switch (status) {
            case 3: {
                SoundEvent lv = this.getDeathSound();
                if (lv != null) {
                    this.playSound(lv, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                }
                if (this instanceof PlayerEntity) break;
                this.setHealth(0.0f);
                this.onDeath(this.getDamageSources().generic());
                break;
            }
            case 46: {
                int i = 128;
                for (int j = 0; j < 128; ++j) {
                    double d = (double)j / 127.0;
                    float f = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float g = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float h = (this.random.nextFloat() - 0.5f) * 0.2f;
                    double e = MathHelper.lerp(d, this.lastX, this.getX()) + (this.random.nextDouble() - 0.5) * (double)this.getWidth() * 2.0;
                    double k = MathHelper.lerp(d, this.lastY, this.getY()) + this.random.nextDouble() * (double)this.getHeight();
                    double l = MathHelper.lerp(d, this.lastZ, this.getZ()) + (this.random.nextDouble() - 0.5) * (double)this.getWidth() * 2.0;
                    this.getEntityWorld().addParticleClient(ParticleTypes.PORTAL, e, k, l, f, g, h);
                }
                break;
            }
            case 47: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.MAINHAND));
                break;
            }
            case 48: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.OFFHAND));
                break;
            }
            case 49: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.HEAD));
                break;
            }
            case 50: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.CHEST));
                break;
            }
            case 51: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.LEGS));
                break;
            }
            case 52: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.FEET));
                break;
            }
            case 65: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.BODY));
                break;
            }
            case 68: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.SADDLE));
                break;
            }
            case 54: {
                HoneyBlock.addRichParticles(this);
                break;
            }
            case 55: {
                this.swapHandStacks();
                break;
            }
            case 60: {
                this.addDeathParticles();
                break;
            }
            case 67: {
                this.addBubbleParticles();
                break;
            }
            default: {
                super.handleStatus(status);
            }
        }
    }

    public void addDeathParticles() {
        for (int i = 0; i < 20; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            double g = 10.0;
            this.getEntityWorld().addParticleClient(ParticleTypes.POOF, this.getParticleX(1.0) - d * 10.0, this.getRandomBodyY() - e * 10.0, this.getParticleZ(1.0) - f * 10.0, d, e, f);
        }
    }

    private void addBubbleParticles() {
        Vec3d lv = this.getVelocity();
        for (int i = 0; i < 8; ++i) {
            double d = this.random.nextTriangular(0.0, 1.0);
            double e = this.random.nextTriangular(0.0, 1.0);
            double f = this.random.nextTriangular(0.0, 1.0);
            this.getEntityWorld().addParticleClient(ParticleTypes.BUBBLE, this.getX() + d, this.getY() + e, this.getZ() + f, lv.x, lv.y, lv.z);
        }
    }

    private void swapHandStacks() {
        ItemStack lv = this.getEquippedStack(EquipmentSlot.OFFHAND);
        this.equipStack(EquipmentSlot.OFFHAND, this.getEquippedStack(EquipmentSlot.MAINHAND));
        this.equipStack(EquipmentSlot.MAINHAND, lv);
    }

    @Override
    protected void tickInVoid() {
        this.serverDamage(this.getDamageSources().outOfWorld(), 4.0f);
    }

    protected void tickHandSwing() {
        int i = this.getHandSwingDuration();
        if (this.handSwinging) {
            ++this.handSwingTicks;
            if (this.handSwingTicks >= i) {
                this.handSwingTicks = 0;
                this.handSwinging = false;
            }
        } else {
            this.handSwingTicks = 0;
        }
        this.handSwingProgress = (float)this.handSwingTicks / (float)i;
    }

    @Nullable
    public EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributes().getCustomInstance(attribute);
    }

    public double getAttributeValue(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributes().getValue(attribute);
    }

    public double getAttributeBaseValue(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributes().getBaseValue(attribute);
    }

    public AttributeContainer getAttributes() {
        return this.attributes;
    }

    public ItemStack getMainHandStack() {
        return this.getEquippedStack(EquipmentSlot.MAINHAND);
    }

    public ItemStack getOffHandStack() {
        return this.getEquippedStack(EquipmentSlot.OFFHAND);
    }

    public ItemStack getStackInArm(Arm arm) {
        return this.getMainArm() == arm ? this.getMainHandStack() : this.getOffHandStack();
    }

    @Override
    @NotNull
    public ItemStack getWeaponStack() {
        return this.getMainHandStack();
    }

    public boolean isHolding(Item item) {
        return this.isHolding((ItemStack stack) -> stack.isOf(item));
    }

    public boolean isHolding(Predicate<ItemStack> predicate) {
        return predicate.test(this.getMainHandStack()) || predicate.test(this.getOffHandStack());
    }

    public ItemStack getStackInHand(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return this.getEquippedStack(EquipmentSlot.MAINHAND);
        }
        if (hand == Hand.OFF_HAND) {
            return this.getEquippedStack(EquipmentSlot.OFFHAND);
        }
        throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)hand));
    }

    public void setStackInHand(Hand hand, ItemStack stack) {
        if (hand == Hand.MAIN_HAND) {
            this.equipStack(EquipmentSlot.MAINHAND, stack);
        } else if (hand == Hand.OFF_HAND) {
            this.equipStack(EquipmentSlot.OFFHAND, stack);
        } else {
            throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)hand));
        }
    }

    public boolean hasStackEquipped(EquipmentSlot slot) {
        return !this.getEquippedStack(slot).isEmpty();
    }

    public boolean canUseSlot(EquipmentSlot slot) {
        return true;
    }

    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return this.equipment.get(slot);
    }

    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.onEquipStack(slot, this.equipment.put(slot, stack), stack);
    }

    public float getArmorVisibility() {
        int i = 0;
        int j = 0;
        for (EquipmentSlot lv : AttributeModifierSlot.ARMOR) {
            if (lv.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack lv2 = this.getEquippedStack(lv);
            if (!lv2.isEmpty()) {
                ++j;
            }
            ++i;
        }
        return i > 0 ? (float)j / (float)i : 0.0f;
    }

    @Override
    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        lv.removeModifier(SPRINTING_SPEED_BOOST.id());
        if (sprinting) {
            lv.addTemporaryModifier(SPRINTING_SPEED_BOOST);
        }
    }

    protected float getSoundVolume() {
        return 1.0f;
    }

    public float getSoundPitch() {
        if (this.isBaby()) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.5f;
        }
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    protected boolean isImmobile() {
        return this.isDead();
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        if (!this.isSleeping()) {
            super.pushAwayFrom(entity);
        }
    }

    private void onDismounted(Entity vehicle) {
        Vec3d lv;
        if (this.isRemoved()) {
            lv = this.getEntityPos();
        } else if (vehicle.isRemoved() || this.getEntityWorld().getBlockState(vehicle.getBlockPos()).isIn(BlockTags.PORTALS)) {
            boolean bl;
            double d = Math.max(this.getY(), vehicle.getY());
            lv = new Vec3d(this.getX(), d, this.getZ());
            boolean bl2 = bl = this.getWidth() <= 4.0f && this.getHeight() <= 4.0f;
            if (bl) {
                double e = (double)this.getHeight() / 2.0;
                Vec3d lv2 = lv.add(0.0, e, 0.0);
                VoxelShape lv3 = VoxelShapes.cuboid(Box.of(lv2, this.getWidth(), this.getHeight(), this.getWidth()));
                lv = this.getEntityWorld().findClosestCollision(this, lv3, lv2, this.getWidth(), this.getHeight(), this.getWidth()).map(pos -> pos.add(0.0, -e, 0.0)).orElse(lv);
            }
        } else {
            lv = vehicle.updatePassengerForDismount(this);
        }
        this.requestTeleportAndDismount(lv.x, lv.y, lv.z);
    }

    @Override
    public boolean shouldRenderName() {
        return this.isCustomNameVisible();
    }

    protected float getJumpVelocity() {
        return this.getJumpVelocity(1.0f);
    }

    protected float getJumpVelocity(float strength) {
        return (float)this.getAttributeValue(EntityAttributes.JUMP_STRENGTH) * strength * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
    }

    public float getJumpBoostVelocityModifier() {
        return this.hasStatusEffect(StatusEffects.JUMP_BOOST) ? 0.1f * ((float)this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1.0f) : 0.0f;
    }

    @VisibleForTesting
    public void jump() {
        float f = this.getJumpVelocity();
        if (f <= 1.0E-5f) {
            return;
        }
        Vec3d lv = this.getVelocity();
        this.setVelocity(lv.x, Math.max((double)f, lv.y), lv.z);
        if (this.isSprinting()) {
            float g = this.getYaw() * ((float)Math.PI / 180);
            this.addVelocityInternal(new Vec3d((double)(-MathHelper.sin(g)) * 0.2, 0.0, (double)MathHelper.cos(g) * 0.2));
        }
        this.velocityDirty = true;
    }

    protected void knockDownwards() {
        this.setVelocity(this.getVelocity().add(0.0, -0.04f, 0.0));
    }

    protected void swimUpward(TagKey<Fluid> fluid) {
        this.setVelocity(this.getVelocity().add(0.0, 0.04f, 0.0));
    }

    protected float getBaseWaterMovementSpeedMultiplier() {
        return 0.8f;
    }

    public boolean canWalkOnFluid(FluidState state) {
        return false;
    }

    @Override
    protected double getGravity() {
        return this.getAttributeValue(EntityAttributes.GRAVITY);
    }

    protected double getEffectiveGravity() {
        boolean bl;
        boolean bl2 = bl = this.getVelocity().y <= 0.0;
        if (bl && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            return Math.min(this.getFinalGravity(), 0.01);
        }
        return this.getFinalGravity();
    }

    public void travel(Vec3d movementInput) {
        FluidState lv = this.getEntityWorld().getFluidState(this.getBlockPos());
        if ((this.isTouchingWater() || this.isInLava()) && this.shouldSwimInFluids() && !this.canWalkOnFluid(lv)) {
            this.travelInFluid(movementInput);
        } else if (this.isGliding()) {
            this.travelGliding(movementInput);
        } else {
            this.travelMidAir(movementInput);
        }
    }

    protected void travelFlying(Vec3d movementInput, float speed) {
        this.travelFlying(movementInput, 0.02f, 0.02f, speed);
    }

    protected void travelFlying(Vec3d movementInput, float inWaterSpeed, float inLavaSpeed, float regularSpeed) {
        if (this.isTouchingWater()) {
            this.updateVelocity(inWaterSpeed, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.8f));
        } else if (this.isInLava()) {
            this.updateVelocity(inLavaSpeed, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.5));
        } else {
            this.updateVelocity(regularSpeed, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.91f));
        }
    }

    private void travelMidAir(Vec3d movementInput) {
        BlockPos lv = this.getVelocityAffectingPos();
        float f = this.isOnGround() ? this.getEntityWorld().getBlockState(lv).getBlock().getSlipperiness() : 1.0f;
        float g = f * 0.91f;
        Vec3d lv2 = this.applyMovementInput(movementInput, f);
        double d = lv2.y;
        StatusEffectInstance lv3 = this.getStatusEffect(StatusEffects.LEVITATION);
        d = lv3 != null ? (d += (0.05 * (double)(lv3.getAmplifier() + 1) - lv2.y) * 0.2) : (!this.getEntityWorld().isClient() || this.getEntityWorld().isChunkLoaded(lv) ? (d -= this.getEffectiveGravity()) : (this.getY() > (double)this.getEntityWorld().getBottomY() ? -0.1 : 0.0));
        if (this.hasNoDrag()) {
            this.setVelocity(lv2.x, d, lv2.z);
        } else {
            float h = this instanceof Flutterer ? g : 0.98f;
            this.setVelocity(lv2.x * (double)g, d * (double)h, lv2.z * (double)g);
        }
    }

    private void travelInFluid(Vec3d movementInput) {
        boolean bl = this.getVelocity().y <= 0.0;
        double d = this.getY();
        double e = this.getEffectiveGravity();
        if (this.isTouchingWater()) {
            float f = this.isSprinting() ? 0.9f : this.getBaseWaterMovementSpeedMultiplier();
            float g = 0.02f;
            float h = (float)this.getAttributeValue(EntityAttributes.WATER_MOVEMENT_EFFICIENCY);
            if (!this.isOnGround()) {
                h *= 0.5f;
            }
            if (h > 0.0f) {
                f += (0.54600006f - f) * h;
                g += (this.getMovementSpeed() - g) * h;
            }
            if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
                f = 0.96f;
            }
            this.updateVelocity(g, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            Vec3d lv = this.getVelocity();
            if (this.horizontalCollision && this.isClimbing()) {
                lv = new Vec3d(lv.x, 0.2, lv.z);
            }
            lv = lv.multiply(f, 0.8f, f);
            this.setVelocity(this.applyFluidMovingSpeed(e, bl, lv));
        } else {
            this.updateVelocity(0.02f, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            if (this.getFluidHeight(FluidTags.LAVA) <= this.getSwimHeight()) {
                this.setVelocity(this.getVelocity().multiply(0.5, 0.8f, 0.5));
                Vec3d lv2 = this.applyFluidMovingSpeed(e, bl, this.getVelocity());
                this.setVelocity(lv2);
            } else {
                this.setVelocity(this.getVelocity().multiply(0.5));
            }
            if (e != 0.0) {
                this.setVelocity(this.getVelocity().add(0.0, -e / 4.0, 0.0));
            }
        }
        Vec3d lv2 = this.getVelocity();
        if (this.horizontalCollision && this.doesNotCollide(lv2.x, lv2.y + (double)0.6f - this.getY() + d, lv2.z)) {
            this.setVelocity(lv2.x, 0.3f, lv2.z);
        }
    }

    private void travelGliding(Vec3d movementInput) {
        if (this.isClimbing()) {
            this.travelMidAir(movementInput);
            this.stopGliding();
            return;
        }
        Vec3d lv = this.getVelocity();
        double d = lv.horizontalLength();
        this.setVelocity(this.calcGlidingVelocity(lv));
        this.move(MovementType.SELF, this.getVelocity());
        if (!this.getEntityWorld().isClient()) {
            double e = this.getVelocity().horizontalLength();
            this.checkGlidingCollision(d, e);
        }
    }

    public void stopGliding() {
        this.setFlag(Entity.GLIDING_FLAG_INDEX, true);
        this.setFlag(Entity.GLIDING_FLAG_INDEX, false);
    }

    private Vec3d calcGlidingVelocity(Vec3d oldVelocity) {
        double i;
        Vec3d lv = this.getRotationVector();
        float f = this.getPitch() * ((float)Math.PI / 180);
        double d = Math.sqrt(lv.x * lv.x + lv.z * lv.z);
        double e = oldVelocity.horizontalLength();
        double g = this.getEffectiveGravity();
        double h = MathHelper.square(Math.cos(f));
        oldVelocity = oldVelocity.add(0.0, g * (-1.0 + h * 0.75), 0.0);
        if (oldVelocity.y < 0.0 && d > 0.0) {
            i = oldVelocity.y * -0.1 * h;
            oldVelocity = oldVelocity.add(lv.x * i / d, i, lv.z * i / d);
        }
        if (f < 0.0f && d > 0.0) {
            i = e * (double)(-MathHelper.sin(f)) * 0.04;
            oldVelocity = oldVelocity.add(-lv.x * i / d, i * 3.2, -lv.z * i / d);
        }
        if (d > 0.0) {
            oldVelocity = oldVelocity.add((lv.x / d * e - oldVelocity.x) * 0.1, 0.0, (lv.z / d * e - oldVelocity.z) * 0.1);
        }
        return oldVelocity.multiply(0.99f, 0.98f, 0.99f);
    }

    private void checkGlidingCollision(double oldSpeed, double newSpeed) {
        double f;
        float g;
        if (this.horizontalCollision && (g = (float)((f = oldSpeed - newSpeed) * 10.0 - 3.0)) > 0.0f) {
            this.playSound(this.getFallSound((int)g), 1.0f, 1.0f);
            this.serverDamage(this.getDamageSources().flyIntoWall(), g);
        }
    }

    private void travelControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        Vec3d lv = this.getControlledMovementInput(controllingPlayer, movementInput);
        this.tickControlled(controllingPlayer, lv);
        if (this.canMoveVoluntarily()) {
            this.setMovementSpeed(this.getSaddledSpeed(controllingPlayer));
            this.travel(lv);
        } else {
            this.setVelocity(Vec3d.ZERO);
        }
    }

    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
    }

    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        return movementInput;
    }

    protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
        return this.getMovementSpeed();
    }

    public void updateLimbs(boolean flutter) {
        float f = (float)MathHelper.magnitude(this.getX() - this.lastX, flutter ? this.getY() - this.lastY : 0.0, this.getZ() - this.lastZ);
        if (this.hasVehicle() || !this.isAlive()) {
            this.limbAnimator.reset();
        } else {
            this.updateLimbs(f);
        }
    }

    protected void updateLimbs(float posDelta) {
        float g = Math.min(posDelta * 4.0f, 1.0f);
        this.limbAnimator.updateLimbs(g, 0.4f, this.isBaby() ? 3.0f : 1.0f);
    }

    private Vec3d applyMovementInput(Vec3d movementInput, float slipperiness) {
        this.updateVelocity(this.getMovementSpeed(slipperiness), movementInput);
        this.setVelocity(this.applyClimbingSpeed(this.getVelocity()));
        this.move(MovementType.SELF, this.getVelocity());
        Vec3d lv = this.getVelocity();
        if ((this.horizontalCollision || this.jumping) && (this.isClimbing() || this.wasInPowderSnow && PowderSnowBlock.canWalkOnPowderSnow(this))) {
            lv = new Vec3d(lv.x, 0.2, lv.z);
        }
        return lv;
    }

    public Vec3d applyFluidMovingSpeed(double gravity, boolean falling, Vec3d motion) {
        if (gravity != 0.0 && !this.isSprinting()) {
            double e = falling && Math.abs(motion.y - 0.005) >= 0.003 && Math.abs(motion.y - gravity / 16.0) < 0.003 ? -0.003 : motion.y - gravity / 16.0;
            return new Vec3d(motion.x, e, motion.z);
        }
        return motion;
    }

    private Vec3d applyClimbingSpeed(Vec3d motion) {
        if (this.isClimbing()) {
            this.onLanding();
            float f = 0.15f;
            double d = MathHelper.clamp(motion.x, (double)-0.15f, (double)0.15f);
            double e = MathHelper.clamp(motion.z, (double)-0.15f, (double)0.15f);
            double g = Math.max(motion.y, (double)-0.15f);
            if (g < 0.0 && !this.getBlockStateAtPos().isOf(Blocks.SCAFFOLDING) && this.isHoldingOntoLadder() && this instanceof PlayerEntity) {
                g = 0.0;
            }
            motion = new Vec3d(d, g, e);
        }
        return motion;
    }

    private float getMovementSpeed(float slipperiness) {
        if (this.isOnGround()) {
            return this.getMovementSpeed() * (0.21600002f / (slipperiness * slipperiness * slipperiness));
        }
        return this.getOffGroundSpeed();
    }

    protected float getOffGroundSpeed() {
        return this.getControllingPassenger() instanceof PlayerEntity ? this.getMovementSpeed() * 0.1f : 0.02f;
    }

    public float getMovementSpeed() {
        return this.movementSpeed;
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public boolean tryAttack(ServerWorld world, Entity target) {
        this.onAttacking(target);
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.tickActiveItemStack();
        this.updateLeaningPitch();
        if (!this.getEntityWorld().isClient()) {
            int j;
            int i = this.getStuckArrowCount();
            if (i > 0) {
                if (this.stuckArrowTimer <= 0) {
                    this.stuckArrowTimer = 20 * (30 - i);
                }
                --this.stuckArrowTimer;
                if (this.stuckArrowTimer <= 0) {
                    this.setStuckArrowCount(i - 1);
                }
            }
            if ((j = this.getStingerCount()) > 0) {
                if (this.stuckStingerTimer <= 0) {
                    this.stuckStingerTimer = 20 * (30 - j);
                }
                --this.stuckStingerTimer;
                if (this.stuckStingerTimer <= 0) {
                    this.setStingerCount(j - 1);
                }
            }
            this.sendEquipmentChanges();
            if (this.age % 20 == 0) {
                this.getDamageTracker().update();
            }
            if (!(!this.isSleeping() || this.isInteractable() && this.isSleepingInBed())) {
                this.wakeUp();
            }
        }
        if (!this.isRemoved()) {
            this.tickMovement();
        }
        double d = this.getX() - this.lastX;
        double e = this.getZ() - this.lastZ;
        float f = (float)(d * d + e * e);
        float g = this.bodyYaw;
        if (f > 0.0025000002f) {
            float h = (float)MathHelper.atan2(e, d) * 57.295776f - 90.0f;
            float k = MathHelper.abs(MathHelper.wrapDegrees(this.getYaw()) - h);
            g = 95.0f < k && k < 265.0f ? h - 180.0f : h;
        }
        if (this.handSwingProgress > 0.0f) {
            g = this.getYaw();
        }
        Profiler lv = Profilers.get();
        lv.push("headTurn");
        this.turnHead(g);
        lv.pop();
        lv.push("rangeChecks");
        while (this.getYaw() - this.lastYaw < -180.0f) {
            this.lastYaw -= 360.0f;
        }
        while (this.getYaw() - this.lastYaw >= 180.0f) {
            this.lastYaw += 360.0f;
        }
        while (this.bodyYaw - this.lastBodyYaw < -180.0f) {
            this.lastBodyYaw -= 360.0f;
        }
        while (this.bodyYaw - this.lastBodyYaw >= 180.0f) {
            this.lastBodyYaw += 360.0f;
        }
        while (this.getPitch() - this.lastPitch < -180.0f) {
            this.lastPitch -= 360.0f;
        }
        while (this.getPitch() - this.lastPitch >= 180.0f) {
            this.lastPitch += 360.0f;
        }
        while (this.headYaw - this.lastHeadYaw < -180.0f) {
            this.lastHeadYaw -= 360.0f;
        }
        while (this.headYaw - this.lastHeadYaw >= 180.0f) {
            this.lastHeadYaw += 360.0f;
        }
        lv.pop();
        this.glidingTicks = this.isGliding() ? ++this.glidingTicks : 0;
        if (this.isSleeping()) {
            this.setPitch(0.0f);
        }
        this.updateAttributes();
        this.elytraFlightController.update();
    }

    private void sendEquipmentChanges() {
        Map<EquipmentSlot, ItemStack> map = this.getEquipmentChanges();
        if (map != null) {
            this.checkHandStackSwap(map);
            if (!map.isEmpty()) {
                this.sendEquipmentChanges(map);
            }
        }
    }

    @Nullable
    private Map<EquipmentSlot, ItemStack> getEquipmentChanges() {
        ItemStack lv3;
        EnumMap<EquipmentSlot, ItemStack> map = null;
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack lv2 = this.lastEquipmentStacks.get(equipmentSlot);
            if (!this.areItemsDifferent(lv2, lv3 = this.getEquippedStack(equipmentSlot))) continue;
            if (map == null) {
                map = Maps.newEnumMap(EquipmentSlot.class);
            }
            map.put(equipmentSlot, lv3);
            AttributeContainer lv4 = this.getAttributes();
            if (lv2.isEmpty()) continue;
            this.onEquipmentRemoved(lv2, equipmentSlot, lv4);
        }
        if (map != null) {
            for (Map.Entry entry : map.entrySet()) {
                EquipmentSlot lv5 = (EquipmentSlot)entry.getKey();
                lv3 = (ItemStack)entry.getValue();
                if (lv3.isEmpty() || lv3.shouldBreak()) continue;
                lv3.applyAttributeModifiers(lv5, (attribute, modifier) -> {
                    EntityAttributeInstance lv = this.attributes.getCustomInstance((RegistryEntry<EntityAttribute>)attribute);
                    if (lv != null) {
                        lv.removeModifier(modifier.id());
                        lv.addTemporaryModifier((EntityAttributeModifier)modifier);
                    }
                });
                World world = this.getEntityWorld();
                if (!(world instanceof ServerWorld)) continue;
                ServerWorld lv6 = (ServerWorld)world;
                EnchantmentHelper.applyLocationBasedEffects(lv6, lv3, this, lv5);
            }
        }
        return map;
    }

    public boolean areItemsDifferent(ItemStack stack, ItemStack stack2) {
        return !ItemStack.areEqual(stack2, stack);
    }

    private void checkHandStackSwap(Map<EquipmentSlot, ItemStack> equipmentChanges) {
        ItemStack lv = equipmentChanges.get(EquipmentSlot.MAINHAND);
        ItemStack lv2 = equipmentChanges.get(EquipmentSlot.OFFHAND);
        if (lv != null && lv2 != null && ItemStack.areEqual(lv, this.lastEquipmentStacks.get(EquipmentSlot.OFFHAND)) && ItemStack.areEqual(lv2, this.lastEquipmentStacks.get(EquipmentSlot.MAINHAND))) {
            ((ServerWorld)this.getEntityWorld()).getChunkManager().sendToOtherNearbyPlayers(this, new EntityStatusS2CPacket(this, EntityStatuses.SWAP_HANDS));
            equipmentChanges.remove(EquipmentSlot.MAINHAND);
            equipmentChanges.remove(EquipmentSlot.OFFHAND);
            this.lastEquipmentStacks.put(EquipmentSlot.MAINHAND, lv.copy());
            this.lastEquipmentStacks.put(EquipmentSlot.OFFHAND, lv2.copy());
        }
    }

    private void sendEquipmentChanges(Map<EquipmentSlot, ItemStack> equipmentChanges) {
        ArrayList<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayListWithCapacity(equipmentChanges.size());
        equipmentChanges.forEach((slot, stack) -> {
            ItemStack lv = stack.copy();
            list.add(Pair.of(slot, lv));
            this.lastEquipmentStacks.put((EquipmentSlot)slot, lv);
        });
        ((ServerWorld)this.getEntityWorld()).getChunkManager().sendToOtherNearbyPlayers(this, new EntityEquipmentUpdateS2CPacket(this.getId(), list));
    }

    protected void turnHead(float bodyRotation) {
        float g = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);
        this.bodyYaw += g * 0.3f;
        float h = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
        float i = this.getMaxRelativeHeadRotation();
        if (Math.abs(h) > i) {
            this.bodyYaw += h - (float)MathHelper.sign(h) * i;
        }
    }

    protected float getMaxRelativeHeadRotation() {
        return 50.0f;
    }

    /*
     * Unable to fully structure code
     */
    public void tickMovement() {
        if (this.jumpingCooldown > 0) {
            --this.jumpingCooldown;
        }
        if (this.isInterpolating()) {
            this.getInterpolator().tick();
        } else if (!this.canMoveVoluntarily()) {
            this.setVelocity(this.getVelocity().multiply(0.98));
        }
        if (this.headTrackingIncrements > 0) {
            this.lerpHeadYaw(this.headTrackingIncrements, this.serverHeadYaw);
            --this.headTrackingIncrements;
        }
        this.equipment.tick(this);
        lv = this.getVelocity();
        d = lv.x;
        e = lv.y;
        f = lv.z;
        if (this.getType().equals(EntityType.PLAYER)) {
            if (lv.horizontalLengthSquared() < 9.0E-6) {
                d = 0.0;
                f = 0.0;
            }
        } else {
            if (Math.abs(lv.x) < 0.003) {
                d = 0.0;
            }
            if (Math.abs(lv.z) < 0.003) {
                f = 0.0;
            }
        }
        if (Math.abs(lv.y) < 0.003) {
            e = 0.0;
        }
        this.setVelocity(d, e, f);
        lv2 = Profilers.get();
        lv2.push("ai");
        this.tickMovementInput();
        if (this.isImmobile()) {
            this.jumping = false;
            this.sidewaysSpeed = 0.0f;
            this.forwardSpeed = 0.0f;
        } else if (this.canActVoluntarily() && !this.getEntityWorld().isClient()) {
            lv2.push("newAi");
            this.tickNewAi();
            lv2.pop();
        }
        lv2.pop();
        lv2.push("jump");
        if (this.jumping && this.shouldSwimInFluids()) {
            g = this.isInLava() != false ? this.getFluidHeight(FluidTags.LAVA) : this.getFluidHeight(FluidTags.WATER);
            bl = this.isTouchingWater() != false && g > 0.0;
            h = this.getSwimHeight();
            if (bl && (!this.isOnGround() || g > h)) {
                this.swimUpward(FluidTags.WATER);
            } else if (this.isInLava() && (!this.isOnGround() || g > h)) {
                this.swimUpward(FluidTags.LAVA);
            } else if ((this.isOnGround() || bl && g <= h) && this.jumpingCooldown == 0) {
                this.jump();
                this.jumpingCooldown = 10;
            }
        } else {
            this.jumpingCooldown = 0;
        }
        lv2.pop();
        lv2.push("travel");
        if (this.isGliding()) {
            this.tickGliding();
        }
        lv3 = this.getBoundingBox();
        lv4 = new Vec3d(this.sidewaysSpeed, this.upwardSpeed, this.forwardSpeed);
        if (this.hasStatusEffect(StatusEffects.SLOW_FALLING) || this.hasStatusEffect(StatusEffects.LEVITATION)) {
            this.onLanding();
        }
        if (!((var12_13 = this.getControllingPassenger()) instanceof PlayerEntity)) ** GOTO lbl-1000
        lv5 = (PlayerEntity)var12_13;
        if (this.isAlive()) {
            this.travelControlled(lv5, lv4);
        } else if (this.canMoveVoluntarily() && this.canActVoluntarily()) {
            this.travel(lv4);
        }
        if (!this.getEntityWorld().isClient() || this.isLogicalSideForUpdatingMovement()) {
            this.tickBlockCollision();
        }
        if (this.getEntityWorld().isClient()) {
            this.updateLimbs(this instanceof Flutterer);
        }
        lv2.pop();
        var12_13 = this.getEntityWorld();
        if (var12_13 instanceof ServerWorld) {
            lv6 = (ServerWorld)var12_13;
            lv2.push("freezing");
            if (!this.inPowderSnow || !this.canFreeze()) {
                this.setFrozenTicks(Math.max(0, this.getFrozenTicks() - 2));
            }
            this.removePowderSnowSlow();
            this.addPowderSnowSlowIfNeeded();
            if (this.age % 40 == 0 && this.isFrozen() && this.canFreeze()) {
                this.damage(lv6, this.getDamageSources().freeze(), 1.0f);
            }
            lv2.pop();
        }
        lv2.push("push");
        if (this.riptideTicks > 0) {
            --this.riptideTicks;
            this.tickRiptide(lv3, this.getBoundingBox());
        }
        this.tickCramming();
        lv2.pop();
        var12_13 = this.getEntityWorld();
        if (var12_13 instanceof ServerWorld) {
            lv6 = (ServerWorld)var12_13;
            if (this.hurtByWater() && this.isTouchingWaterOrRain()) {
                this.damage(lv6, this.getDamageSources().drown(), 1.0f);
            }
        }
    }

    protected void tickMovementInput() {
        this.sidewaysSpeed *= 0.98f;
        this.forwardSpeed *= 0.98f;
    }

    public boolean hurtByWater() {
        return false;
    }

    public boolean isJumping() {
        return this.jumping;
    }

    protected void tickGliding() {
        this.limitFallDistance();
        if (!this.getEntityWorld().isClient()) {
            if (!this.canGlide()) {
                this.setFlag(Entity.GLIDING_FLAG_INDEX, false);
                return;
            }
            int i = this.glidingTicks + 1;
            if (i % 10 == 0) {
                int j = i / 10;
                if (j % 2 == 0) {
                    List<EquipmentSlot> list = EquipmentSlot.VALUES.stream().filter(slot -> LivingEntity.canGlideWith(this.getEquippedStack((EquipmentSlot)slot), slot)).toList();
                    EquipmentSlot lv = Util.getRandom(list, this.random);
                    this.getEquippedStack(lv).damage(1, this, lv);
                }
                this.emitGameEvent(GameEvent.ELYTRA_GLIDE);
            }
        }
    }

    protected boolean canGlide() {
        if (this.isOnGround() || this.hasVehicle() || this.hasStatusEffect(StatusEffects.LEVITATION)) {
            return false;
        }
        for (EquipmentSlot lv : EquipmentSlot.VALUES) {
            if (!LivingEntity.canGlideWith(this.getEquippedStack(lv), lv)) continue;
            return true;
        }
        return false;
    }

    protected void tickNewAi() {
    }

    protected void tickCramming() {
        ServerWorld lv;
        int i;
        List<Entity> list = this.getEntityWorld().getCrammedEntities(this, this.getBoundingBox());
        if (list.isEmpty()) {
            return;
        }
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld && (i = (lv = (ServerWorld)world).getGameRules().getInt(GameRules.MAX_ENTITY_CRAMMING)) > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
            int j = 0;
            for (Entity lv2 : list) {
                if (lv2.hasVehicle()) continue;
                ++j;
            }
            if (j > i - 1) {
                this.damage(lv, this.getDamageSources().cramming(), 6.0f);
            }
        }
        for (Entity lv3 : list) {
            this.pushAway(lv3);
        }
    }

    protected void tickRiptide(Box a, Box b) {
        Box lv = a.union(b);
        List<Entity> list = this.getEntityWorld().getOtherEntities(this, lv);
        if (!list.isEmpty()) {
            for (Entity lv2 : list) {
                if (!(lv2 instanceof LivingEntity)) continue;
                this.attackLivingEntity((LivingEntity)lv2);
                this.riptideTicks = 0;
                this.setVelocity(this.getVelocity().multiply(-0.2));
                break;
            }
        } else if (this.horizontalCollision) {
            this.riptideTicks = 0;
        }
        if (!this.getEntityWorld().isClient() && this.riptideTicks <= 0) {
            this.setLivingFlag(USING_RIPTIDE_FLAG, false);
            this.riptideAttackDamage = 0.0f;
            this.riptideStack = null;
        }
    }

    protected void pushAway(Entity entity) {
        entity.pushAwayFrom(this);
    }

    protected void attackLivingEntity(LivingEntity target) {
    }

    public boolean isUsingRiptide() {
        return (this.dataTracker.get(LIVING_FLAGS) & 4) != 0;
    }

    @Override
    public void stopRiding() {
        Entity lv = this.getVehicle();
        super.stopRiding();
        if (lv != null && lv != this.getVehicle() && !this.getEntityWorld().isClient()) {
            this.onDismounted(lv);
        }
    }

    @Override
    public void tickRiding() {
        super.tickRiding();
        this.onLanding();
    }

    @Override
    public PositionInterpolator getInterpolator() {
        return this.interpolator;
    }

    @Override
    public void updateTrackedHeadRotation(float yaw, int interpolationSteps) {
        this.serverHeadYaw = yaw;
        this.headTrackingIncrements = interpolationSteps;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public void triggerItemPickedUpByEntityCriteria(ItemEntity item) {
        Entity lv = item.getOwner();
        if (lv instanceof ServerPlayerEntity) {
            Criteria.THROWN_ITEM_PICKED_UP_BY_ENTITY.trigger((ServerPlayerEntity)lv, item.getStack(), this);
        }
    }

    public void sendPickup(Entity item, int count) {
        if (!item.isRemoved() && !this.getEntityWorld().isClient() && (item instanceof ItemEntity || item instanceof PersistentProjectileEntity || item instanceof ExperienceOrbEntity)) {
            ((ServerWorld)this.getEntityWorld()).getChunkManager().sendToOtherNearbyPlayers(item, new ItemPickupAnimationS2CPacket(item.getId(), this.getId(), count));
        }
    }

    public boolean canSee(Entity entity) {
        return this.canSee(entity, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity.getEyeY());
    }

    public boolean canSee(Entity entity, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling, double entityY) {
        if (entity.getEntityWorld() != this.getEntityWorld()) {
            return false;
        }
        Vec3d lv = new Vec3d(this.getX(), this.getEyeY(), this.getZ());
        Vec3d lv2 = new Vec3d(entity.getX(), entityY, entity.getZ());
        if (lv2.distanceTo(lv) > 128.0) {
            return false;
        }
        return this.getEntityWorld().raycast(new RaycastContext(lv, lv2, shapeType, fluidHandling, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public float getYaw(float tickProgress) {
        if (tickProgress == 1.0f) {
            return this.headYaw;
        }
        return MathHelper.lerpAngleDegrees(tickProgress, this.lastHeadYaw, this.headYaw);
    }

    public float getHandSwingProgress(float tickProgress) {
        float g = this.handSwingProgress - this.lastHandSwingProgress;
        if (g < 0.0f) {
            g += 1.0f;
        }
        return this.lastHandSwingProgress + g * tickProgress;
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return this.isAlive() && !this.isSpectator() && !this.isClimbing();
    }

    @Override
    public float getHeadYaw() {
        return this.headYaw;
    }

    @Override
    public void setHeadYaw(float headYaw) {
        this.headYaw = headYaw;
    }

    @Override
    public void setBodyYaw(float bodyYaw) {
        this.bodyYaw = bodyYaw;
    }

    @Override
    public Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        return LivingEntity.positionInPortal(super.positionInPortal(portalAxis, portalRect));
    }

    public static Vec3d positionInPortal(Vec3d pos) {
        return new Vec3d(pos.x, pos.y, 0.0);
    }

    public float getAbsorptionAmount() {
        return this.absorptionAmount;
    }

    public final void setAbsorptionAmount(float absorptionAmount) {
        this.setAbsorptionAmountUnclamped(MathHelper.clamp(absorptionAmount, 0.0f, this.getMaxAbsorption()));
    }

    protected void setAbsorptionAmountUnclamped(float absorptionAmount) {
        this.absorptionAmount = absorptionAmount;
    }

    public void enterCombat() {
    }

    public void endCombat() {
    }

    protected void markEffectsDirty() {
        this.effectsChanged = true;
    }

    public abstract Arm getMainArm();

    public boolean isUsingItem() {
        return (this.dataTracker.get(LIVING_FLAGS) & 1) > 0;
    }

    public Hand getActiveHand() {
        return (this.dataTracker.get(LIVING_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    private void tickActiveItemStack() {
        if (this.isUsingItem()) {
            if (ItemStack.areItemsEqual(this.getStackInHand(this.getActiveHand()), this.activeItemStack)) {
                this.activeItemStack = this.getStackInHand(this.getActiveHand());
                this.tickItemStackUsage(this.activeItemStack);
            } else {
                this.clearActiveItem();
            }
        }
    }

    @Nullable
    private ItemEntity createItemEntity(ItemStack stack, boolean atSelf, boolean retainOwnership) {
        if (stack.isEmpty()) {
            return null;
        }
        double d = this.getEyeY() - (double)0.3f;
        ItemEntity lv = new ItemEntity(this.getEntityWorld(), this.getX(), d, this.getZ(), stack);
        lv.setPickupDelay(40);
        if (retainOwnership) {
            lv.setThrower(this);
        }
        if (atSelf) {
            float f = this.random.nextFloat() * 0.5f;
            float g = this.random.nextFloat() * ((float)Math.PI * 2);
            lv.setVelocity(-MathHelper.sin(g) * f, 0.2f, MathHelper.cos(g) * f);
        } else {
            float f = 0.3f;
            float g = MathHelper.sin(this.getPitch() * ((float)Math.PI / 180));
            float h = MathHelper.cos(this.getPitch() * ((float)Math.PI / 180));
            float i = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180));
            float j = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180));
            float k = this.random.nextFloat() * ((float)Math.PI * 2);
            float l = 0.02f * this.random.nextFloat();
            lv.setVelocity((double)(-i * h * 0.3f) + Math.cos(k) * (double)l, -g * 0.3f + 0.1f + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f, (double)(j * h * 0.3f) + Math.sin(k) * (double)l);
        }
        return lv;
    }

    protected void tickItemStackUsage(ItemStack stack) {
        stack.usageTick(this.getEntityWorld(), this, this.getItemUseTimeLeft());
        if (--this.itemUseTimeLeft == 0 && !this.getEntityWorld().isClient() && !stack.isUsedOnRelease()) {
            this.consumeItem();
        }
    }

    private void updateLeaningPitch() {
        this.lastLeaningPitch = this.leaningPitch;
        this.leaningPitch = this.isInSwimmingPose() ? Math.min(1.0f, this.leaningPitch + 0.09f) : Math.max(0.0f, this.leaningPitch - 0.09f);
    }

    protected void setLivingFlag(int mask, boolean value) {
        int j = this.dataTracker.get(LIVING_FLAGS).byteValue();
        j = value ? (j |= mask) : (j &= ~mask);
        this.dataTracker.set(LIVING_FLAGS, (byte)j);
    }

    public void setCurrentHand(Hand hand) {
        ItemStack lv = this.getStackInHand(hand);
        if (lv.isEmpty() || this.isUsingItem()) {
            return;
        }
        this.activeItemStack = lv;
        this.itemUseTimeLeft = lv.getMaxUseTime(this);
        if (!this.getEntityWorld().isClient()) {
            this.setLivingFlag(USING_ITEM_FLAG, true);
            this.setLivingFlag(OFF_HAND_ACTIVE_FLAG, hand == Hand.OFF_HAND);
            this.emitGameEvent(GameEvent.ITEM_INTERACT_START);
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (SLEEPING_POSITION.equals(data)) {
            if (this.getEntityWorld().isClient()) {
                this.getSleepingPosition().ifPresent(this::setPositionInBed);
            }
        } else if (LIVING_FLAGS.equals(data) && this.getEntityWorld().isClient()) {
            if (this.isUsingItem() && this.activeItemStack.isEmpty()) {
                this.activeItemStack = this.getStackInHand(this.getActiveHand());
                if (!this.activeItemStack.isEmpty()) {
                    this.itemUseTimeLeft = this.activeItemStack.getMaxUseTime(this);
                }
            } else if (!this.isUsingItem() && !this.activeItemStack.isEmpty()) {
                this.activeItemStack = ItemStack.EMPTY;
                this.itemUseTimeLeft = 0;
            }
        }
    }

    @Override
    public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        super.lookAt(anchorPoint, target);
        this.lastHeadYaw = this.headYaw;
        this.lastBodyYaw = this.bodyYaw = this.headYaw;
    }

    @Override
    public float lerpYaw(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastBodyYaw, this.bodyYaw);
    }

    public void spawnItemParticles(ItemStack stack, int count) {
        for (int j = 0; j < count; ++j) {
            Vec3d lv = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            lv = lv.rotateX(-this.getPitch() * ((float)Math.PI / 180));
            lv = lv.rotateY(-this.getYaw() * ((float)Math.PI / 180));
            double d = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
            Vec3d lv2 = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.3, d, 0.6);
            lv2 = lv2.rotateX(-this.getPitch() * ((float)Math.PI / 180));
            lv2 = lv2.rotateY(-this.getYaw() * ((float)Math.PI / 180));
            lv2 = lv2.add(this.getX(), this.getEyeY(), this.getZ());
            this.getEntityWorld().addParticleClient(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), lv2.x, lv2.y, lv2.z, lv.x, lv.y + 0.05, lv.z);
        }
    }

    protected void consumeItem() {
        if (this.getEntityWorld().isClient() && !this.isUsingItem()) {
            return;
        }
        Hand lv = this.getActiveHand();
        if (!this.activeItemStack.equals(this.getStackInHand(lv))) {
            this.stopUsingItem();
            return;
        }
        if (!this.activeItemStack.isEmpty() && this.isUsingItem()) {
            ItemStack lv2 = this.activeItemStack.finishUsing(this.getEntityWorld(), this);
            if (lv2 != this.activeItemStack) {
                this.setStackInHand(lv, lv2);
            }
            this.clearActiveItem();
        }
    }

    public void giveOrDropStack(ItemStack stack) {
    }

    public ItemStack getActiveItem() {
        return this.activeItemStack;
    }

    public int getItemUseTimeLeft() {
        return this.itemUseTimeLeft;
    }

    public int getItemUseTime() {
        if (this.isUsingItem()) {
            return this.activeItemStack.getMaxUseTime(this) - this.getItemUseTimeLeft();
        }
        return 0;
    }

    public void stopUsingItem() {
        ItemStack lv = this.getStackInHand(this.getActiveHand());
        if (!this.activeItemStack.isEmpty() && ItemStack.areItemsEqual(lv, this.activeItemStack)) {
            this.activeItemStack = lv;
            this.activeItemStack.onStoppedUsing(this.getEntityWorld(), this, this.getItemUseTimeLeft());
            if (this.activeItemStack.isUsedOnRelease()) {
                this.tickActiveItemStack();
            }
        }
        this.clearActiveItem();
    }

    public void clearActiveItem() {
        if (!this.getEntityWorld().isClient()) {
            boolean bl = this.isUsingItem();
            this.setLivingFlag(USING_ITEM_FLAG, false);
            if (bl) {
                this.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
        }
        this.activeItemStack = ItemStack.EMPTY;
        this.itemUseTimeLeft = 0;
    }

    public boolean isBlocking() {
        return this.getBlockingItem() != null;
    }

    @Nullable
    public ItemStack getBlockingItem() {
        int i;
        if (!this.isUsingItem()) {
            return null;
        }
        BlocksAttacksComponent lv = this.activeItemStack.get(DataComponentTypes.BLOCKS_ATTACKS);
        if (lv != null && (i = this.activeItemStack.getItem().getMaxUseTime(this.activeItemStack, this) - this.itemUseTimeLeft) >= lv.getBlockDelayTicks()) {
            return this.activeItemStack;
        }
        return null;
    }

    public boolean isHoldingOntoLadder() {
        return this.isSneaking();
    }

    public boolean isGliding() {
        return this.getFlag(Entity.GLIDING_FLAG_INDEX);
    }

    @Override
    public boolean isInSwimmingPose() {
        return super.isInSwimmingPose() || !this.isGliding() && this.isInPose(EntityPose.GLIDING);
    }

    public int getGlidingTicks() {
        return this.glidingTicks;
    }

    public boolean teleport(double x, double y, double z, boolean particleEffects) {
        LivingEntity livingEntity;
        double g = this.getX();
        double h = this.getY();
        double i = this.getZ();
        double j = y;
        boolean bl2 = false;
        BlockPos lv = BlockPos.ofFloored(x, j, z);
        World lv2 = this.getEntityWorld();
        if (lv2.isChunkLoaded(lv)) {
            boolean bl3 = false;
            while (!bl3 && lv.getY() > lv2.getBottomY()) {
                BlockPos lv3 = lv.down();
                BlockState lv4 = lv2.getBlockState(lv3);
                if (lv4.blocksMovement()) {
                    bl3 = true;
                    continue;
                }
                j -= 1.0;
                lv = lv3;
            }
            if (bl3) {
                this.requestTeleport(x, j, z);
                if (lv2.isSpaceEmpty(this) && !lv2.containsFluid(this.getBoundingBox())) {
                    bl2 = true;
                }
            }
        }
        if (!bl2) {
            this.requestTeleport(g, h, i);
            return false;
        }
        if (particleEffects) {
            lv2.sendEntityStatus(this, EntityStatuses.ADD_PORTAL_PARTICLES);
        }
        if ((livingEntity = this) instanceof PathAwareEntity) {
            PathAwareEntity lv5 = (PathAwareEntity)livingEntity;
            lv5.getNavigation().stop();
        }
        return true;
    }

    public boolean isAffectedBySplashPotions() {
        return !this.isDead();
    }

    public boolean isMobOrPlayer() {
        return true;
    }

    public void setNearbySongPlaying(BlockPos songPosition, boolean playing) {
    }

    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public final EntityDimensions getDimensions(EntityPose pose) {
        return pose == EntityPose.SLEEPING ? SLEEPING_DIMENSIONS : this.getBaseDimensions(pose).scaled(this.getScale());
    }

    protected EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.getType().getDimensions().scaled(this.getScaleFactor());
    }

    public ImmutableList<EntityPose> getPoses() {
        return ImmutableList.of(EntityPose.STANDING);
    }

    public Box getBoundingBox(EntityPose pose) {
        EntityDimensions lv = this.getDimensions(pose);
        return new Box(-lv.width() / 2.0f, 0.0, -lv.width() / 2.0f, lv.width() / 2.0f, lv.height(), lv.width() / 2.0f);
    }

    protected boolean wouldNotSuffocateInPose(EntityPose pose) {
        Box lv = this.getDimensions(pose).getBoxAt(this.getEntityPos());
        return this.getEntityWorld().isBlockSpaceEmpty(this, lv);
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return super.canUsePortals(allowVehicles) && !this.isSleeping();
    }

    public Optional<BlockPos> getSleepingPosition() {
        return this.dataTracker.get(SLEEPING_POSITION);
    }

    public void setSleepingPosition(BlockPos pos) {
        this.dataTracker.set(SLEEPING_POSITION, Optional.of(pos));
    }

    public void clearSleepingPosition() {
        this.dataTracker.set(SLEEPING_POSITION, Optional.empty());
    }

    public boolean isSleeping() {
        return this.getSleepingPosition().isPresent();
    }

    public void sleep(BlockPos pos) {
        BlockState lv;
        if (this.hasVehicle()) {
            this.stopRiding();
        }
        if ((lv = this.getEntityWorld().getBlockState(pos)).getBlock() instanceof BedBlock) {
            this.getEntityWorld().setBlockState(pos, (BlockState)lv.with(BedBlock.OCCUPIED, true), Block.NOTIFY_ALL);
        }
        this.setPose(EntityPose.SLEEPING);
        this.setPositionInBed(pos);
        this.setSleepingPosition(pos);
        this.setVelocity(Vec3d.ZERO);
        this.velocityDirty = true;
    }

    private void setPositionInBed(BlockPos pos) {
        this.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 0.6875, (double)pos.getZ() + 0.5);
    }

    private boolean isSleepingInBed() {
        return this.getSleepingPosition().map(pos -> this.getEntityWorld().getBlockState((BlockPos)pos).getBlock() instanceof BedBlock).orElse(false);
    }

    public void wakeUp() {
        this.getSleepingPosition().filter(this.getEntityWorld()::isChunkLoaded).ifPresent(pos -> {
            BlockState lv = this.getEntityWorld().getBlockState((BlockPos)pos);
            if (lv.getBlock() instanceof BedBlock) {
                Direction lv2 = (Direction)lv.get(BedBlock.FACING);
                this.getEntityWorld().setBlockState((BlockPos)pos, (BlockState)lv.with(BedBlock.OCCUPIED, false), Block.NOTIFY_ALL);
                Vec3d lv3 = BedBlock.findWakeUpPosition(this.getType(), (CollisionView)this.getEntityWorld(), pos, lv2, this.getYaw()).orElseGet(() -> {
                    BlockPos lv = pos.up();
                    return new Vec3d((double)lv.getX() + 0.5, (double)lv.getY() + 0.1, (double)lv.getZ() + 0.5);
                });
                Vec3d lv4 = Vec3d.ofBottomCenter(pos).subtract(lv3).normalize();
                float f = (float)MathHelper.wrapDegrees(MathHelper.atan2(lv4.z, lv4.x) * 57.2957763671875 - 90.0);
                this.setPosition(lv3.x, lv3.y, lv3.z);
                this.setYaw(f);
                this.setPitch(0.0f);
            }
        });
        Vec3d lv = this.getEntityPos();
        this.setPose(EntityPose.STANDING);
        this.setPosition(lv.x, lv.y, lv.z);
        this.clearSleepingPosition();
    }

    @Nullable
    public Direction getSleepingDirection() {
        BlockPos lv = this.getSleepingPosition().orElse(null);
        return lv != null ? BedBlock.getDirection(this.getEntityWorld(), lv) : null;
    }

    @Override
    public boolean isInsideWall() {
        return !this.isSleeping() && super.isInsideWall();
    }

    public ItemStack getProjectileType(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    private static byte getEquipmentBreakStatus(EquipmentSlot slot) {
        return switch (slot) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.MAINHAND -> 47;
            case EquipmentSlot.OFFHAND -> 48;
            case EquipmentSlot.HEAD -> 49;
            case EquipmentSlot.CHEST -> 50;
            case EquipmentSlot.FEET -> 52;
            case EquipmentSlot.LEGS -> 51;
            case EquipmentSlot.BODY -> 65;
            case EquipmentSlot.SADDLE -> 68;
        };
    }

    public void sendEquipmentBreakStatus(Item item, EquipmentSlot slot) {
        this.getEntityWorld().sendEntityStatus(this, LivingEntity.getEquipmentBreakStatus(slot));
        this.onEquipmentRemoved(this.getEquippedStack(slot), slot, this.attributes);
    }

    private void onEquipmentRemoved(ItemStack removedEquipment, EquipmentSlot slot, AttributeContainer container) {
        removedEquipment.applyAttributeModifiers(slot, (attribute, modifier) -> {
            EntityAttributeInstance lv = container.getCustomInstance((RegistryEntry<EntityAttribute>)attribute);
            if (lv != null) {
                lv.removeModifier((EntityAttributeModifier)modifier);
            }
        });
        EnchantmentHelper.removeLocationBasedEffects(removedEquipment, this, slot);
    }

    public final boolean canEquipFromDispenser(ItemStack stack) {
        if (!this.isAlive() || this.isSpectator()) {
            return false;
        }
        EquippableComponent lv = stack.get(DataComponentTypes.EQUIPPABLE);
        if (lv == null || !lv.dispensable()) {
            return false;
        }
        EquipmentSlot lv2 = lv.slot();
        if (!this.canUseSlot(lv2) || !lv.allows(this.getType())) {
            return false;
        }
        return this.getEquippedStack(lv2).isEmpty() && this.canDispenserEquipSlot(lv2);
    }

    protected boolean canDispenserEquipSlot(EquipmentSlot slot) {
        return true;
    }

    public final EquipmentSlot getPreferredEquipmentSlot(ItemStack stack) {
        EquippableComponent lv = stack.get(DataComponentTypes.EQUIPPABLE);
        if (lv != null && this.canUseSlot(lv.slot())) {
            return lv.slot();
        }
        return EquipmentSlot.MAINHAND;
    }

    public final boolean canEquip(ItemStack stack, EquipmentSlot slot) {
        EquippableComponent lv = stack.get(DataComponentTypes.EQUIPPABLE);
        if (lv == null) {
            return slot == EquipmentSlot.MAINHAND && this.canUseSlot(EquipmentSlot.MAINHAND);
        }
        return slot == lv.slot() && this.canUseSlot(lv.slot()) && lv.allows(this.getType());
    }

    private static StackReference getStackReference(LivingEntity entity, EquipmentSlot slot) {
        if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            return StackReference.of(entity, slot);
        }
        return StackReference.of(entity, slot, stack -> stack.isEmpty() || entity.getPreferredEquipmentSlot((ItemStack)stack) == slot);
    }

    @Nullable
    private static EquipmentSlot getEquipmentSlot(int slotId) {
        if (slotId == 100 + EquipmentSlot.HEAD.getEntitySlotId()) {
            return EquipmentSlot.HEAD;
        }
        if (slotId == 100 + EquipmentSlot.CHEST.getEntitySlotId()) {
            return EquipmentSlot.CHEST;
        }
        if (slotId == 100 + EquipmentSlot.LEGS.getEntitySlotId()) {
            return EquipmentSlot.LEGS;
        }
        if (slotId == 100 + EquipmentSlot.FEET.getEntitySlotId()) {
            return EquipmentSlot.FEET;
        }
        if (slotId == 98) {
            return EquipmentSlot.MAINHAND;
        }
        if (slotId == 99) {
            return EquipmentSlot.OFFHAND;
        }
        if (slotId == 105) {
            return EquipmentSlot.BODY;
        }
        if (slotId == 106) {
            return EquipmentSlot.SADDLE;
        }
        return null;
    }

    @Override
    public StackReference getStackReference(int mappedIndex) {
        EquipmentSlot lv = LivingEntity.getEquipmentSlot(mappedIndex);
        if (lv != null) {
            return LivingEntity.getStackReference(this, lv);
        }
        return super.getStackReference(mappedIndex);
    }

    @Override
    public boolean canFreeze() {
        if (this.isSpectator()) {
            return false;
        }
        for (EquipmentSlot lv : AttributeModifierSlot.ARMOR) {
            if (!this.getEquippedStack(lv).isIn(ItemTags.FREEZE_IMMUNE_WEARABLES)) continue;
            return false;
        }
        return super.canFreeze();
    }

    @Override
    public boolean isGlowing() {
        return !this.getEntityWorld().isClient() && this.hasStatusEffect(StatusEffects.GLOWING) || super.isGlowing();
    }

    @Override
    public float getBodyYaw() {
        return this.bodyYaw;
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        float g = packet.getYaw();
        float h = packet.getPitch();
        this.updateTrackedPosition(d, e, f);
        this.bodyYaw = packet.getHeadYaw();
        this.headYaw = packet.getHeadYaw();
        this.lastBodyYaw = this.bodyYaw;
        this.lastHeadYaw = this.headYaw;
        this.setId(packet.getEntityId());
        this.setUuid(packet.getUuid());
        this.updatePositionAndAngles(d, e, f, g, h);
        this.setVelocity(packet.getVelocity());
    }

    public float getWeaponDisableBlockingForSeconds() {
        WeaponComponent lv = this.getWeaponStack().get(DataComponentTypes.WEAPON);
        return lv != null ? lv.disableBlockingForSeconds() : 0.0f;
    }

    @Override
    public float getStepHeight() {
        float f = (float)this.getAttributeValue(EntityAttributes.STEP_HEIGHT);
        return this.getControllingPassenger() instanceof PlayerEntity ? Math.max(f, 1.0f) : f;
    }

    @Override
    public Vec3d getPassengerRidingPos(Entity passenger) {
        return this.getEntityPos().add(this.getPassengerAttachmentPos(passenger, this.getDimensions(this.getPose()), this.getScale() * this.getScaleFactor()));
    }

    protected void lerpHeadYaw(int headTrackingIncrements, double serverHeadYaw) {
        this.headYaw = (float)MathHelper.lerpAngleDegrees(1.0 / (double)headTrackingIncrements, (double)this.headYaw, serverHeadYaw);
    }

    @Override
    public void setOnFireForTicks(int ticks) {
        super.setOnFireForTicks(MathHelper.ceil((double)ticks * this.getAttributeValue(EntityAttributes.BURNING_TIME)));
    }

    public boolean isInCreativeMode() {
        return false;
    }

    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        return this.isAlwaysInvulnerableTo(source) || EnchantmentHelper.isInvulnerableTo(world, this, source);
    }

    public static boolean canGlideWith(ItemStack stack, EquipmentSlot slot) {
        if (!stack.contains(DataComponentTypes.GLIDER)) {
            return false;
        }
        EquippableComponent lv = stack.get(DataComponentTypes.EQUIPPABLE);
        return lv != null && slot == lv.slot() && !stack.willBreakNextUse();
    }

    @VisibleForTesting
    public int getPlayerHitTimer() {
        return this.playerHitTimer;
    }

    @Override
    public boolean hasWaypoint() {
        return this.getAttributeValue(EntityAttributes.WAYPOINT_TRANSMIT_RANGE) > 0.0;
    }

    @Override
    public Optional<ServerWaypoint.WaypointTracker> createTracker(ServerPlayerEntity receiver) {
        if (this.firstUpdate || receiver == this) {
            return Optional.empty();
        }
        if (ServerWaypoint.cannotReceive(this, receiver)) {
            return Optional.empty();
        }
        Waypoint.Config lv = this.waypointConfig.withTeamColorOf(this);
        if (ServerWaypoint.shouldUseAzimuth(this, receiver)) {
            return Optional.of(new ServerWaypoint.AzimuthWaypointTracker(this, lv, receiver));
        }
        if (!ServerWaypoint.canReceive(this.getChunkPos(), receiver)) {
            return Optional.of(new ServerWaypoint.ChunkWaypointTracker(this, lv, receiver));
        }
        return Optional.of(new ServerWaypoint.PositionalWaypointTracker(this, lv, receiver));
    }

    @Override
    public Waypoint.Config getWaypointConfig() {
        return this.waypointConfig;
    }

    public record FallSounds(SoundEvent small, SoundEvent big) {
    }
}

