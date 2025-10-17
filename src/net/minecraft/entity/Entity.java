/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.Portal;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityBlockIntersectionType;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.PositionInterpolator;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.data.DataTracked;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Hand;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nameable;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.BlockView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.DebugTrackable;
import net.minecraft.world.dimension.NetherPortal;
import net.minecraft.world.dimension.PortalManager;
import net.minecraft.world.entity.EntityChangeListener;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.waypoint.ServerWaypoint;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class Entity
implements DataTracked,
DebugTrackable,
Nameable,
HeldItemContext,
EntityLike,
ScoreHolder,
ComponentsAccess {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ID_KEY = "id";
    public static final String UUID_KEY = "UUID";
    public static final String PASSENGERS_KEY = "Passengers";
    public static final String CUSTOM_DATA_KEY = "data";
    public static final String POS_KEY = "Pos";
    public static final String MOTION_KEY = "Motion";
    public static final String ROTATION_KEY = "Rotation";
    public static final String PORTAL_COOLDOWN_KEY = "PortalCooldown";
    public static final String NO_GRAVITY_KEY = "NoGravity";
    public static final String AIR_KEY = "Air";
    public static final String ON_GROUND_KEY = "OnGround";
    public static final String FALL_DISTANCE_KEY = "fall_distance";
    public static final String FIRE_KEY = "Fire";
    public static final String SILENT_KEY = "Silent";
    public static final String GLOWING_KEY = "Glowing";
    public static final String INVULNERABLE_KEY = "Invulnerable";
    public static final String CUSTOM_NAME_KEY = "CustomName";
    private static final AtomicInteger CURRENT_ID = new AtomicInteger();
    public static final int field_49791 = 0;
    public static final int MAX_RIDING_COOLDOWN = 60;
    public static final int DEFAULT_PORTAL_COOLDOWN = 300;
    public static final int MAX_COMMAND_TAGS = 1024;
    private static final Codec<List<String>> TAG_LIST_CODEC = Codec.STRING.sizeLimitedListOf(1024);
    public static final float field_44870 = 0.2f;
    public static final double field_44871 = 0.500001;
    public static final double field_44872 = 0.999999;
    public static final int DEFAULT_MIN_FREEZE_DAMAGE_TICKS = 140;
    public static final int FREEZING_DAMAGE_INTERVAL = 40;
    public static final int field_49073 = 3;
    private static final Box NULL_BOX = new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    private static final double SPEED_IN_WATER = 0.014;
    private static final double SPEED_IN_LAVA_IN_NETHER = 0.007;
    private static final double SPEED_IN_LAVA = 0.0023333333333333335;
    private static final int field_61895 = 16;
    private static final double field_61894 = 8.0;
    private static double renderDistanceMultiplier = 1.0;
    private final EntityType<?> type;
    private boolean alwaysSyncAbsolute;
    private int id = CURRENT_ID.incrementAndGet();
    public boolean intersectionChecked;
    private ImmutableList<Entity> passengerList = ImmutableList.of();
    protected int ridingCooldown;
    @Nullable
    private Entity vehicle;
    private World world;
    public double lastX;
    public double lastY;
    public double lastZ;
    private Vec3d pos;
    private BlockPos blockPos;
    private ChunkPos chunkPos;
    private Vec3d velocity = Vec3d.ZERO;
    private float yaw;
    private float pitch;
    public float lastYaw;
    public float lastPitch;
    private Box boundingBox = NULL_BOX;
    private boolean onGround;
    public boolean horizontalCollision;
    public boolean verticalCollision;
    public boolean groundCollision;
    public boolean collidedSoftly;
    public boolean velocityModified;
    protected Vec3d movementMultiplier = Vec3d.ZERO;
    @Nullable
    private RemovalReason removalReason;
    public static final float DEFAULT_FRICTION = 0.6f;
    public static final float MIN_RISING_BUBBLE_COLUMN_SPEED = 1.8f;
    public float distanceTraveled;
    public float speed;
    public double fallDistance;
    private float nextStepSoundDistance = 1.0f;
    public double lastRenderX;
    public double lastRenderY;
    public double lastRenderZ;
    public boolean noClip;
    protected final Random random = Random.create();
    public int age;
    private int fireTicks;
    protected boolean touchingWater;
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight = new Object2DoubleArrayMap<TagKey<Fluid>>(2);
    protected boolean submergedInWater;
    private final Set<TagKey<Fluid>> submergedFluidTag = new HashSet<TagKey<Fluid>>();
    public int timeUntilRegen;
    protected boolean firstUpdate = true;
    protected final DataTracker dataTracker;
    protected static final TrackedData<Byte> FLAGS = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final int ON_FIRE_FLAG_INDEX = 0;
    private static final int SNEAKING_FLAG_INDEX = 1;
    private static final int SPRINTING_FLAG_INDEX = 3;
    private static final int SWIMMING_FLAG_INDEX = 4;
    private static final int INVISIBLE_FLAG_INDEX = 5;
    protected static final int GLOWING_FLAG_INDEX = 6;
    protected static final int GLIDING_FLAG_INDEX = 7;
    private static final TrackedData<Integer> AIR = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Optional<Text>> CUSTOM_NAME = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT);
    private static final TrackedData<Boolean> NAME_VISIBLE = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> SILENT = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> NO_GRAVITY = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected static final TrackedData<EntityPose> POSE = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.ENTITY_POSE);
    private static final TrackedData<Integer> FROZEN_TICKS = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.INTEGER);
    private EntityChangeListener changeListener = EntityChangeListener.NONE;
    private final TrackedPosition trackedPosition = new TrackedPosition();
    public boolean velocityDirty;
    @Nullable
    public PortalManager portalManager;
    private int portalCooldown;
    private boolean invulnerable;
    protected UUID uuid = MathHelper.randomUuid(this.random);
    protected String uuidString = this.uuid.toString();
    private boolean glowing;
    private final Set<String> commandTags = Sets.newHashSet();
    private final double[] pistonMovementDelta = new double[]{0.0, 0.0, 0.0};
    private long pistonMovementTick;
    private EntityDimensions dimensions;
    private float standingEyeHeight;
    public boolean inPowderSnow;
    public boolean wasInPowderSnow;
    public Optional<BlockPos> supportingBlockPos = Optional.empty();
    private boolean forceUpdateSupportingBlockPos = false;
    private float lastChimeIntensity;
    private int lastChimeAge;
    private boolean hasVisualFire;
    @Nullable
    private BlockState stateAtPos = null;
    public static final int MAX_QUEUED_COLLISION_CHECKS = 100;
    private final ArrayDeque<QueuedCollisionCheck> queuedCollisionChecks = new ArrayDeque(100);
    private final List<QueuedCollisionCheck> currentlyCheckedCollisions = new ObjectArrayList<QueuedCollisionCheck>();
    private final LongSet collidedBlockPositions = new LongOpenHashSet();
    private final EntityCollisionHandler.Impl collisionHandler = new EntityCollisionHandler.Impl();
    private NbtComponent customData = NbtComponent.DEFAULT;

    public Entity(EntityType<?> type, World world) {
        this.type = type;
        this.world = world;
        this.dimensions = type.getDimensions();
        this.pos = Vec3d.ZERO;
        this.blockPos = BlockPos.ORIGIN;
        this.chunkPos = ChunkPos.ORIGIN;
        DataTracker.Builder lv = new DataTracker.Builder(this);
        lv.add(FLAGS, (byte)0);
        lv.add(AIR, this.getMaxAir());
        lv.add(NAME_VISIBLE, false);
        lv.add(CUSTOM_NAME, Optional.empty());
        lv.add(SILENT, false);
        lv.add(NO_GRAVITY, false);
        lv.add(POSE, EntityPose.STANDING);
        lv.add(FROZEN_TICKS, 0);
        this.initDataTracker(lv);
        this.dataTracker = lv.build();
        this.setPosition(0.0, 0.0, 0.0);
        this.standingEyeHeight = this.dimensions.eyeHeight();
    }

    public boolean collidesWithStateAtPos(BlockPos pos, BlockState state) {
        VoxelShape lv = state.getCollisionShape(this.getEntityWorld(), pos, ShapeContext.of(this)).offset(pos);
        return VoxelShapes.matchesAnywhere(lv, VoxelShapes.cuboid(this.getBoundingBox()), BooleanBiFunction.AND);
    }

    public int getTeamColorValue() {
        Team lv = this.getScoreboardTeam();
        if (lv != null && ((AbstractTeam)lv).getColor().getColorValue() != null) {
            return ((AbstractTeam)lv).getColor().getColorValue();
        }
        return 0xFFFFFF;
    }

    public boolean isSpectator() {
        return false;
    }

    public boolean isInteractable() {
        return this.isAlive() && !this.isRemoved() && !this.isSpectator();
    }

    public final void detach() {
        if (this.hasPassengers()) {
            this.removeAllPassengers();
        }
        if (this.hasVehicle()) {
            this.stopRiding();
        }
    }

    public void updateTrackedPosition(double x, double y, double z) {
        this.trackedPosition.setPos(new Vec3d(x, y, z));
    }

    public TrackedPosition getTrackedPosition() {
        return this.trackedPosition;
    }

    public EntityType<?> getType() {
        return this.type;
    }

    public boolean shouldAlwaysSyncAbsolute() {
        return this.alwaysSyncAbsolute;
    }

    public void setAlwaysSyncAbsolute(boolean alwaysSyncAbsolute) {
        this.alwaysSyncAbsolute = alwaysSyncAbsolute;
    }

    @Override
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<String> getCommandTags() {
        return this.commandTags;
    }

    public boolean addCommandTag(String tag) {
        if (this.commandTags.size() >= 1024) {
            return false;
        }
        return this.commandTags.add(tag);
    }

    public boolean removeCommandTag(String tag) {
        return this.commandTags.remove(tag);
    }

    public void kill(ServerWorld world) {
        this.remove(RemovalReason.KILLED);
        this.emitGameEvent(GameEvent.ENTITY_DIE);
    }

    public final void discard() {
        this.remove(RemovalReason.DISCARDED);
    }

    protected abstract void initDataTracker(DataTracker.Builder var1);

    public DataTracker getDataTracker() {
        return this.dataTracker;
    }

    public boolean equals(Object o) {
        if (o instanceof Entity) {
            return ((Entity)o).id == this.id;
        }
        return false;
    }

    public int hashCode() {
        return this.id;
    }

    public void remove(RemovalReason reason) {
        this.setRemoved(reason);
    }

    public void onRemoved() {
    }

    public void onRemove(RemovalReason reason) {
    }

    public void setPose(EntityPose pose) {
        this.dataTracker.set(POSE, pose);
    }

    public EntityPose getPose() {
        return this.dataTracker.get(POSE);
    }

    public boolean isInPose(EntityPose pose) {
        return this.getPose() == pose;
    }

    public boolean isInRange(Entity entity, double radius) {
        return this.getEntityPos().isInRange(entity.getEntityPos(), radius);
    }

    public boolean isInRange(Entity entity, double horizontalRadius, double verticalRadius) {
        double f = entity.getX() - this.getX();
        double g = entity.getY() - this.getY();
        double h = entity.getZ() - this.getZ();
        return MathHelper.squaredHypot(f, h) < MathHelper.square(horizontalRadius) && MathHelper.square(g) < MathHelper.square(verticalRadius);
    }

    protected void setRotation(float yaw, float pitch) {
        this.setYaw(yaw % 360.0f);
        this.setPitch(pitch % 360.0f);
    }

    public final void setPosition(Vec3d pos) {
        this.setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    public void setPosition(double x, double y, double z) {
        this.setPos(x, y, z);
        this.setBoundingBox(this.calculateBoundingBox());
    }

    protected final Box calculateBoundingBox() {
        return this.calculateDefaultBoundingBox(this.pos);
    }

    protected Box calculateDefaultBoundingBox(Vec3d pos) {
        return this.dimensions.getBoxAt(pos);
    }

    protected void refreshPosition() {
        this.setPosition(this.pos.x, this.pos.y, this.pos.z);
    }

    public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
        float f = (float)cursorDeltaY * 0.15f;
        float g = (float)cursorDeltaX * 0.15f;
        this.setPitch(this.getPitch() + f);
        this.setYaw(this.getYaw() + g);
        this.setPitch(MathHelper.clamp(this.getPitch(), -90.0f, 90.0f));
        this.lastPitch += f;
        this.lastYaw += g;
        this.lastPitch = MathHelper.clamp(this.lastPitch, -90.0f, 90.0f);
        if (this.vehicle != null) {
            this.vehicle.onPassengerLookAround(this);
        }
    }

    public void tick() {
        this.baseTick();
    }

    public void baseTick() {
        ServerWorld lv2;
        Profiler lv = Profilers.get();
        lv.push("entityBaseTick");
        this.stateAtPos = null;
        if (this.hasVehicle() && this.getVehicle().isRemoved()) {
            this.stopRiding();
        }
        if (this.ridingCooldown > 0) {
            --this.ridingCooldown;
        }
        this.tickPortalTeleportation();
        if (this.shouldSpawnSprintingParticles()) {
            this.spawnSprintingParticles();
        }
        this.wasInPowderSnow = this.inPowderSnow;
        this.inPowderSnow = false;
        this.updateWaterState();
        this.updateSubmergedInWaterState();
        this.updateSwimming();
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            lv2 = (ServerWorld)world;
            if (this.fireTicks > 0) {
                if (this.isFireImmune()) {
                    this.extinguish();
                } else {
                    if (this.fireTicks % 20 == 0 && !this.isInLava()) {
                        this.damage(lv2, this.getDamageSources().onFire(), 1.0f);
                    }
                    this.setFireTicks(this.fireTicks - 1);
                }
            }
        } else {
            this.extinguish();
        }
        if (this.isInLava()) {
            this.fallDistance *= 0.5;
        }
        this.attemptTickInVoid();
        if (!this.getEntityWorld().isClient()) {
            this.setOnFire(this.fireTicks > 0);
        }
        this.firstUpdate = false;
        world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            lv2 = (ServerWorld)world;
            if (this instanceof Leashable) {
                Leashable.tickLeash(lv2, (Entity)((Object)((Leashable)((Object)this))));
            }
        }
        lv.pop();
    }

    public void setOnFire(boolean onFire) {
        this.setFlag(ON_FIRE_FLAG_INDEX, onFire || this.hasVisualFire);
    }

    public void attemptTickInVoid() {
        if (this.getY() < (double)(this.getEntityWorld().getBottomY() - 64)) {
            this.tickInVoid();
        }
    }

    public void resetPortalCooldown() {
        this.portalCooldown = this.getDefaultPortalCooldown();
    }

    public void setPortalCooldown(int portalCooldown) {
        this.portalCooldown = portalCooldown;
    }

    public int getPortalCooldown() {
        return this.portalCooldown;
    }

    public boolean hasPortalCooldown() {
        return this.portalCooldown > 0;
    }

    protected void tickPortalCooldown() {
        if (this.hasPortalCooldown()) {
            --this.portalCooldown;
        }
    }

    public void igniteByLava() {
        if (this.isFireImmune()) {
            return;
        }
        this.setOnFireFor(15.0f);
    }

    public void setOnFireFromLava() {
        ServerWorld lv;
        if (this.isFireImmune()) {
            return;
        }
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld && this.damage(lv = (ServerWorld)world, this.getDamageSources().lava(), 4.0f) && this.shouldPlayBurnSoundInLava() && !this.isSilent()) {
            lv.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_GENERIC_BURN, this.getSoundCategory(), 0.4f, 2.0f + this.random.nextFloat() * 0.4f);
        }
    }

    protected boolean shouldPlayBurnSoundInLava() {
        return true;
    }

    public final void setOnFireFor(float seconds) {
        this.setOnFireForTicks(MathHelper.floor(seconds * 20.0f));
    }

    public void setOnFireForTicks(int ticks) {
        if (this.fireTicks < ticks) {
            this.setFireTicks(ticks);
        }
        this.defrost();
    }

    public void setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
    }

    public int getFireTicks() {
        return this.fireTicks;
    }

    public void extinguish() {
        this.setFireTicks(Math.min(0, this.getFireTicks()));
    }

    protected void tickInVoid() {
        this.discard();
    }

    public boolean doesNotCollide(double offsetX, double offsetY, double offsetZ) {
        return this.doesNotCollide(this.getBoundingBox().offset(offsetX, offsetY, offsetZ));
    }

    private boolean doesNotCollide(Box box) {
        return this.getEntityWorld().isSpaceEmpty(this, box) && !this.getEntityWorld().containsFluid(box);
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
        this.updateSupportingBlockPos(onGround, null);
    }

    public void setMovement(boolean onGround, Vec3d movement) {
        this.setMovement(onGround, this.horizontalCollision, movement);
    }

    public void setMovement(boolean onGround, boolean horizontalCollision, Vec3d movement) {
        this.onGround = onGround;
        this.horizontalCollision = horizontalCollision;
        this.updateSupportingBlockPos(onGround, movement);
    }

    public boolean isSupportedBy(BlockPos pos) {
        return this.supportingBlockPos.isPresent() && this.supportingBlockPos.get().equals(pos);
    }

    protected void updateSupportingBlockPos(boolean onGround, @Nullable Vec3d movement) {
        if (onGround) {
            Box lv = this.getBoundingBox();
            Box lv2 = new Box(lv.minX, lv.minY - 1.0E-6, lv.minZ, lv.maxX, lv.minY, lv.maxZ);
            Optional<BlockPos> optional = this.world.findSupportingBlockPos(this, lv2);
            if (optional.isPresent() || this.forceUpdateSupportingBlockPos) {
                this.supportingBlockPos = optional;
            } else if (movement != null) {
                Box lv3 = lv2.offset(-movement.x, 0.0, -movement.z);
                optional = this.world.findSupportingBlockPos(this, lv3);
                this.supportingBlockPos = optional;
            }
            this.forceUpdateSupportingBlockPos = optional.isEmpty();
        } else {
            this.forceUpdateSupportingBlockPos = false;
            if (this.supportingBlockPos.isPresent()) {
                this.supportingBlockPos = Optional.empty();
            }
        }
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void move(MovementType type, Vec3d movement) {
        MoveEffect lv11;
        Vec3d lv2;
        double d;
        if (this.noClip) {
            this.setPosition(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
            this.horizontalCollision = false;
            this.verticalCollision = false;
            this.groundCollision = false;
            this.collidedSoftly = false;
            return;
        }
        if (type == MovementType.PISTON && (movement = this.adjustMovementForPiston(movement)).equals(Vec3d.ZERO)) {
            return;
        }
        Profiler lv = Profilers.get();
        lv.push("move");
        if (this.movementMultiplier.lengthSquared() > 1.0E-7) {
            if (type != MovementType.PISTON) {
                movement = movement.multiply(this.movementMultiplier);
            }
            this.movementMultiplier = Vec3d.ZERO;
            this.setVelocity(Vec3d.ZERO);
        }
        if ((d = (lv2 = this.adjustMovementForCollisions(movement = this.adjustMovementForSneaking(movement, type))).lengthSquared()) > 1.0E-7 || movement.lengthSquared() - d < 1.0E-7) {
            if (this.fallDistance != 0.0 && d >= 1.0) {
                double e = Math.min(lv2.length(), 8.0);
                Vec3d lv3 = this.getEntityPos().add(lv2.normalize().multiply(e));
                BlockHitResult lv4 = this.getEntityWorld().raycast(new RaycastContext(this.getEntityPos(), lv3, RaycastContext.ShapeType.FALLDAMAGE_RESETTING, RaycastContext.FluidHandling.WATER, this));
                if (lv4.getType() != HitResult.Type.MISS) {
                    this.onLanding();
                }
            }
            Vec3d lv5 = this.getEntityPos();
            Vec3d lv6 = lv5.add(lv2);
            this.addQueuedCollisionChecks(new QueuedCollisionCheck(lv5, lv6, movement));
            this.setPosition(lv6);
        }
        lv.pop();
        lv.push("rest");
        boolean bl = !MathHelper.approximatelyEquals(movement.x, lv2.x);
        boolean bl2 = !MathHelper.approximatelyEquals(movement.z, lv2.z);
        boolean bl3 = this.horizontalCollision = bl || bl2;
        if (Math.abs(movement.y) > 0.0 || this.isLogicalSideForUpdatingMovement()) {
            this.verticalCollision = movement.y != lv2.y;
            this.groundCollision = this.verticalCollision && movement.y < 0.0;
            this.setMovement(this.groundCollision, this.horizontalCollision, lv2);
        }
        this.collidedSoftly = this.horizontalCollision ? this.hasCollidedSoftly(lv2) : false;
        BlockPos lv7 = this.getLandingPos();
        BlockState lv8 = this.getEntityWorld().getBlockState(lv7);
        if (this.isLogicalSideForUpdatingMovement()) {
            this.fall(lv2.y, this.isOnGround(), lv8, lv7);
        }
        if (this.isRemoved()) {
            lv.pop();
            return;
        }
        if (this.horizontalCollision) {
            Vec3d lv9 = this.getVelocity();
            this.setVelocity(bl ? 0.0 : lv9.x, lv9.y, bl2 ? 0.0 : lv9.z);
        }
        if (this.canMoveVoluntarily()) {
            Block lv10 = lv8.getBlock();
            if (movement.y != lv2.y) {
                lv10.onEntityLand(this.getEntityWorld(), this);
            }
        }
        if ((!this.getEntityWorld().isClient() || this.isLogicalSideForUpdatingMovement()) && (lv11 = this.getMoveEffect()).hasAny() && !this.hasVehicle()) {
            this.applyMoveEffect(lv11, lv2, lv7, lv8);
        }
        float f = this.getVelocityMultiplier();
        this.setVelocity(this.getVelocity().multiply(f, 1.0, f));
        lv.pop();
    }

    private void applyMoveEffect(MoveEffect moveEffect, Vec3d movement, BlockPos landingPos, BlockState landingState) {
        float f = 0.6f;
        float g = (float)(movement.length() * (double)0.6f);
        float h = (float)(movement.horizontalLength() * (double)0.6f);
        BlockPos lv = this.getSteppingPos();
        BlockState lv2 = this.getEntityWorld().getBlockState(lv);
        boolean bl = this.canClimb(lv2);
        this.distanceTraveled += bl ? g : h;
        this.speed += g;
        if (this.distanceTraveled > this.nextStepSoundDistance && !lv2.isAir()) {
            boolean bl2 = lv.equals(landingPos);
            boolean bl3 = this.stepOnBlock(landingPos, landingState, moveEffect.playsSounds(), bl2, movement);
            if (!bl2) {
                bl3 |= this.stepOnBlock(lv, lv2, false, moveEffect.emitsGameEvents(), movement);
            }
            if (bl3) {
                this.nextStepSoundDistance = this.calculateNextStepSoundDistance();
            } else if (this.isTouchingWater()) {
                this.nextStepSoundDistance = this.calculateNextStepSoundDistance();
                if (moveEffect.playsSounds()) {
                    this.playSwimSound();
                }
                if (moveEffect.emitsGameEvents()) {
                    this.emitGameEvent(GameEvent.SWIM);
                }
            }
        } else if (lv2.isAir()) {
            this.addAirTravelEffects();
        }
    }

    protected void tickBlockCollision() {
        this.currentlyCheckedCollisions.clear();
        this.currentlyCheckedCollisions.addAll(this.queuedCollisionChecks);
        this.queuedCollisionChecks.clear();
        if (this.currentlyCheckedCollisions.isEmpty()) {
            this.currentlyCheckedCollisions.add(new QueuedCollisionCheck(this.getLastRenderPos(), this.getEntityPos()));
        } else if (this.currentlyCheckedCollisions.getLast().to.squaredDistanceTo(this.getEntityPos()) > 9.999999439624929E-11) {
            this.currentlyCheckedCollisions.add(new QueuedCollisionCheck(this.currentlyCheckedCollisions.getLast().to, this.getEntityPos()));
        }
        this.tickBlockCollisions(this.currentlyCheckedCollisions);
    }

    private void addQueuedCollisionChecks(QueuedCollisionCheck queuedCollisionCheck) {
        if (this.queuedCollisionChecks.size() >= 100) {
            QueuedCollisionCheck lv = this.queuedCollisionChecks.removeFirst();
            QueuedCollisionCheck lv2 = this.queuedCollisionChecks.removeFirst();
            QueuedCollisionCheck lv3 = new QueuedCollisionCheck(lv.from(), lv2.to());
            this.queuedCollisionChecks.addFirst(lv3);
        }
        this.queuedCollisionChecks.add(queuedCollisionCheck);
    }

    public void popQueuedCollisionCheck() {
        if (!this.queuedCollisionChecks.isEmpty()) {
            this.queuedCollisionChecks.removeLast();
        }
    }

    protected void clearQueuedCollisionChecks() {
        this.queuedCollisionChecks.clear();
    }

    public void tickBlockCollision(Vec3d lastRenderPos, Vec3d pos) {
        this.tickBlockCollisions(List.of(new QueuedCollisionCheck(lastRenderPos, pos)));
    }

    private void tickBlockCollisions(List<QueuedCollisionCheck> checks) {
        boolean bl3;
        if (!this.shouldTickBlockCollision()) {
            return;
        }
        if (this.isOnGround()) {
            BlockPos lv = this.getLandingPos();
            BlockState lv2 = this.getEntityWorld().getBlockState(lv);
            lv2.getBlock().onSteppedOn(this.getEntityWorld(), lv, lv2, this);
        }
        boolean bl = this.isOnFire();
        boolean bl2 = this.shouldEscapePowderSnow();
        int i = this.getFireTicks();
        this.checkBlockCollisions(checks, this.collisionHandler);
        this.collisionHandler.runCallbacks(this);
        if (this.isBeingRainedOn()) {
            this.extinguish();
        }
        if (bl && !this.isOnFire() || bl2 && !this.shouldEscapePowderSnow()) {
            this.playExtinguishSound();
        }
        boolean bl4 = bl3 = this.getFireTicks() > i;
        if (!(this.getEntityWorld().isClient() || this.isOnFire() || bl3)) {
            this.setFireTicks(-this.getBurningDuration());
        }
    }

    protected boolean shouldTickBlockCollision() {
        return !this.isRemoved() && !this.noClip;
    }

    private boolean canClimb(BlockState state) {
        return state.isIn(BlockTags.CLIMBABLE) || state.isOf(Blocks.POWDER_SNOW);
    }

    private boolean stepOnBlock(BlockPos pos, BlockState state, boolean playSound, boolean emitEvent, Vec3d movement) {
        if (state.isAir()) {
            return false;
        }
        boolean bl3 = this.canClimb(state);
        if ((this.isOnGround() || bl3 || this.isInSneakingPose() && movement.y == 0.0 || this.isOnRail()) && !this.isSwimming()) {
            if (playSound) {
                this.playStepSounds(pos, state);
            }
            if (emitEvent) {
                this.getEntityWorld().emitGameEvent(GameEvent.STEP, this.getEntityPos(), GameEvent.Emitter.of(this, state));
            }
            return true;
        }
        return false;
    }

    protected boolean hasCollidedSoftly(Vec3d adjustedMovement) {
        return false;
    }

    protected void playExtinguishSound() {
        if (!this.world.isClient()) {
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, this.getSoundCategory(), 0.7f, 1.6f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
        }
    }

    public void extinguishWithSound() {
        if (this.isOnFire()) {
            this.playExtinguishSound();
        }
        this.extinguish();
    }

    protected void addAirTravelEffects() {
        if (this.isFlappingWings()) {
            this.addFlapEffects();
            if (this.getMoveEffect().emitsGameEvents()) {
                this.emitGameEvent(GameEvent.FLAP);
            }
        }
    }

    @Deprecated
    public BlockPos getLandingPos() {
        return this.getPosWithYOffset(0.2f);
    }

    public BlockPos getVelocityAffectingPos() {
        return this.getPosWithYOffset(0.500001f);
    }

    public BlockPos getSteppingPos() {
        return this.getPosWithYOffset(1.0E-5f);
    }

    protected BlockPos getPosWithYOffset(float offset) {
        if (this.supportingBlockPos.isPresent()) {
            BlockPos lv = this.supportingBlockPos.get();
            if (offset > 1.0E-5f) {
                BlockState lv2 = this.getEntityWorld().getBlockState(lv);
                if ((double)offset <= 0.5 && lv2.isIn(BlockTags.FENCES) || lv2.isIn(BlockTags.WALLS) || lv2.getBlock() instanceof FenceGateBlock) {
                    return lv;
                }
                return lv.withY(MathHelper.floor(this.pos.y - (double)offset));
            }
            return lv;
        }
        int i = MathHelper.floor(this.pos.x);
        int j = MathHelper.floor(this.pos.y - (double)offset);
        int k = MathHelper.floor(this.pos.z);
        return new BlockPos(i, j, k);
    }

    protected float getJumpVelocityMultiplier() {
        float f = this.getEntityWorld().getBlockState(this.getBlockPos()).getBlock().getJumpVelocityMultiplier();
        float g = this.getEntityWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getJumpVelocityMultiplier();
        return (double)f == 1.0 ? g : f;
    }

    protected float getVelocityMultiplier() {
        BlockState lv = this.getEntityWorld().getBlockState(this.getBlockPos());
        float f = lv.getBlock().getVelocityMultiplier();
        if (lv.isOf(Blocks.WATER) || lv.isOf(Blocks.BUBBLE_COLUMN)) {
            return f;
        }
        return (double)f == 1.0 ? this.getEntityWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getVelocityMultiplier() : f;
    }

    protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
        return movement;
    }

    protected Vec3d adjustMovementForPiston(Vec3d movement) {
        if (movement.lengthSquared() <= 1.0E-7) {
            return movement;
        }
        long l = this.getEntityWorld().getTime();
        if (l != this.pistonMovementTick) {
            Arrays.fill(this.pistonMovementDelta, 0.0);
            this.pistonMovementTick = l;
        }
        if (movement.x != 0.0) {
            double d = this.calculatePistonMovementFactor(Direction.Axis.X, movement.x);
            return Math.abs(d) <= (double)1.0E-5f ? Vec3d.ZERO : new Vec3d(d, 0.0, 0.0);
        }
        if (movement.y != 0.0) {
            double d = this.calculatePistonMovementFactor(Direction.Axis.Y, movement.y);
            return Math.abs(d) <= (double)1.0E-5f ? Vec3d.ZERO : new Vec3d(0.0, d, 0.0);
        }
        if (movement.z != 0.0) {
            double d = this.calculatePistonMovementFactor(Direction.Axis.Z, movement.z);
            return Math.abs(d) <= (double)1.0E-5f ? Vec3d.ZERO : new Vec3d(0.0, 0.0, d);
        }
        return Vec3d.ZERO;
    }

    private double calculatePistonMovementFactor(Direction.Axis axis, double offsetFactor) {
        int i = axis.ordinal();
        double e = MathHelper.clamp(offsetFactor + this.pistonMovementDelta[i], -0.51, 0.51);
        offsetFactor = e - this.pistonMovementDelta[i];
        this.pistonMovementDelta[i] = e;
        return offsetFactor;
    }

    public double calcDistanceFromBottomCollision(double checkedDistance) {
        Box lv = this.getBoundingBox();
        Box lv2 = lv.withMinY(lv.minY - checkedDistance).withMaxY(lv.minY);
        List<VoxelShape> list = Entity.findCollisions(this, this.world, lv2);
        if (list.isEmpty()) {
            return checkedDistance;
        }
        return -VoxelShapes.calculateMaxOffset(Direction.Axis.Y, lv, list, -checkedDistance);
    }

    private Vec3d adjustMovementForCollisions(Vec3d movement) {
        boolean bl4;
        Box lv = this.getBoundingBox();
        List<VoxelShape> list = this.getEntityWorld().getEntityCollisions(this, lv.stretch(movement));
        Vec3d lv2 = movement.lengthSquared() == 0.0 ? movement : Entity.adjustMovementForCollisions(this, movement, lv, this.getEntityWorld(), list);
        boolean bl = movement.x != lv2.x;
        boolean bl2 = movement.y != lv2.y;
        boolean bl3 = movement.z != lv2.z;
        boolean bl5 = bl4 = bl2 && movement.y < 0.0;
        if (this.getStepHeight() > 0.0f && (bl4 || this.isOnGround()) && (bl || bl3)) {
            float[] fs;
            Box lv3 = bl4 ? lv.offset(0.0, lv2.y, 0.0) : lv;
            Box lv4 = lv3.stretch(movement.x, this.getStepHeight(), movement.z);
            if (!bl4) {
                lv4 = lv4.stretch(0.0, -1.0E-5f, 0.0);
            }
            List<VoxelShape> list2 = Entity.findCollisionsForMovement(this, this.world, list, lv4);
            float f = (float)lv2.y;
            for (float g : fs = Entity.collectStepHeights(lv3, list2, this.getStepHeight(), f)) {
                Vec3d lv5 = Entity.adjustMovementForCollisions(new Vec3d(movement.x, g, movement.z), lv3, list2);
                if (!(lv5.horizontalLengthSquared() > lv2.horizontalLengthSquared())) continue;
                double d = lv.minY - lv3.minY;
                return lv5.subtract(0.0, d, 0.0);
            }
        }
        return lv2;
    }

    private static float[] collectStepHeights(Box collisionBox, List<VoxelShape> collisions, float f, float stepHeight) {
        FloatArraySet floatSet = new FloatArraySet(4);
        block0: for (VoxelShape lv : collisions) {
            DoubleList doubleList = lv.getPointPositions(Direction.Axis.Y);
            DoubleListIterator doubleListIterator = doubleList.iterator();
            while (doubleListIterator.hasNext()) {
                double d = (Double)doubleListIterator.next();
                float h = (float)(d - collisionBox.minY);
                if (h < 0.0f || h == stepHeight) continue;
                if (h > f) continue block0;
                floatSet.add(h);
            }
        }
        float[] fs = floatSet.toFloatArray();
        FloatArrays.unstableSort(fs);
        return fs;
    }

    public static Vec3d adjustMovementForCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> collisions) {
        List<VoxelShape> list2 = Entity.findCollisionsForMovement(entity, world, collisions, entityBoundingBox.stretch(movement));
        return Entity.adjustMovementForCollisions(movement, entityBoundingBox, list2);
    }

    public static List<VoxelShape> findCollisions(@Nullable Entity entity, World world, Box box) {
        List<VoxelShape> list = world.getEntityCollisions(entity, box);
        return Entity.findCollisionsForMovement(entity, world, list, box);
    }

    private static List<VoxelShape> findCollisionsForMovement(@Nullable Entity entity, World world, List<VoxelShape> regularCollisions, Box movingEntityBoundingBox) {
        boolean bl;
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize(regularCollisions.size() + 1);
        if (!regularCollisions.isEmpty()) {
            builder.addAll(regularCollisions);
        }
        WorldBorder lv = world.getWorldBorder();
        boolean bl2 = bl = entity != null && lv.canCollide(entity, movingEntityBoundingBox);
        if (bl) {
            builder.add(lv.asVoxelShape());
        }
        builder.addAll(world.getBlockCollisions(entity, movingEntityBoundingBox));
        return builder.build();
    }

    private static Vec3d adjustMovementForCollisions(Vec3d movement, Box entityBoundingBox, List<VoxelShape> collisions) {
        if (collisions.isEmpty()) {
            return movement;
        }
        Vec3d lv = Vec3d.ZERO;
        for (Direction.Axis lv2 : Direction.method_73163(movement)) {
            double d = movement.getComponentAlongAxis(lv2);
            if (d == 0.0) continue;
            double e = VoxelShapes.calculateMaxOffset(lv2, entityBoundingBox.offset(lv), collisions, d);
            lv = lv.withAxis(lv2, e);
        }
        return lv;
    }

    protected float calculateNextStepSoundDistance() {
        return (int)this.distanceTraveled + 1;
    }

    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_GENERIC_SWIM;
    }

    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_GENERIC_SPLASH;
    }

    protected SoundEvent getHighSpeedSplashSound() {
        return SoundEvents.ENTITY_GENERIC_SPLASH;
    }

    private void checkBlockCollisions(List<QueuedCollisionCheck> queuedCollisionChecks, EntityCollisionHandler.Impl collisionHandler) {
        if (!this.shouldTickBlockCollision()) {
            return;
        }
        LongSet longSet = this.collidedBlockPositions;
        for (QueuedCollisionCheck lv : queuedCollisionChecks) {
            Vec3d lv2 = lv.from;
            Vec3d lv3 = lv.to().subtract(lv.from());
            int i = 16;
            if (lv.axisDependentOriginalMovement().isPresent() && lv3.lengthSquared() > 0.0) {
                for (Direction.Axis lv4 : Direction.method_73163(lv.axisDependentOriginalMovement().get())) {
                    double d = lv3.getComponentAlongAxis(lv4);
                    if (d == 0.0) continue;
                    Vec3d lv5 = lv2.offset(lv4.getPositiveDirection(), d);
                    i -= this.checkBlockCollision(lv2, lv5, collisionHandler, longSet, i);
                    lv2 = lv5;
                }
            } else {
                i -= this.checkBlockCollision(lv.from(), lv.to(), collisionHandler, longSet, 16);
            }
            if (i > 0) continue;
            this.checkBlockCollision(lv.to(), lv.to(), collisionHandler, longSet, 1);
        }
        longSet.clear();
    }

    private int checkBlockCollision(Vec3d from, Vec3d to, EntityCollisionHandler.Impl collisionHandler, LongSet collidedBlockPositions, int i) {
        ServerWorld lv2;
        Box lv = this.calculateDefaultBoundingBox(to).contract(1.0E-5f);
        boolean bl = from.squaredDistanceTo(to) > MathHelper.square(0.9999900000002526);
        World world = this.world;
        boolean bl2 = world instanceof ServerWorld && (lv2 = (ServerWorld)world).getServer().getSubscriberTracker().hasSubscriber(DebugSubscriptionTypes.ENTITY_BLOCK_INTERSECTIONS);
        AtomicInteger atomicInteger = new AtomicInteger();
        BlockView.collectCollisionsBetween(from, to, lv, (arg5, j) -> {
            if (!this.isAlive()) {
                return false;
            }
            if (j >= i) {
                return false;
            }
            atomicInteger.set(j);
            BlockState lv = this.getEntityWorld().getBlockState(arg5);
            if (lv.isAir()) {
                if (bl2) {
                    this.afterCollisionCheck((ServerWorld)this.getEntityWorld(), arg5.toImmutable(), false, false);
                }
                return true;
            }
            VoxelShape lv2 = lv.getInsideCollisionShape(this.getEntityWorld(), arg5, this);
            boolean bl3 = lv2 == VoxelShapes.fullCube() || this.collides(from, to, lv2.offset(new Vec3d(arg5)).getBoundingBoxes());
            boolean bl4 = this.collidesWithFluid(lv.getFluidState(), arg5, from, to);
            if (!bl3 && !bl4 || !collidedBlockPositions.add(arg5.asLong())) {
                return true;
            }
            if (bl3) {
                try {
                    boolean bl5 = bl || lv.contains(arg5);
                    collisionHandler.updateIfNecessary(j);
                    lv.onEntityCollision(this.getEntityWorld(), arg5, this, collisionHandler, bl5);
                    this.onBlockCollision(lv);
                } catch (Throwable throwable) {
                    CrashReport lv3 = CrashReport.create(throwable, "Colliding entity with block");
                    CrashReportSection lv4 = lv3.addElement("Block being collided with");
                    CrashReportSection.addBlockInfo(lv4, this.getEntityWorld(), arg5, lv);
                    CrashReportSection lv5 = lv3.addElement("Entity being checked for collision");
                    this.populateCrashReport(lv5);
                    throw new CrashException(lv3);
                }
            }
            if (bl4) {
                collisionHandler.updateIfNecessary(j);
                lv.getFluidState().onEntityCollision(this.getEntityWorld(), arg5, this, collisionHandler);
            }
            if (bl2) {
                this.afterCollisionCheck((ServerWorld)this.getEntityWorld(), arg5.toImmutable(), bl3, bl4);
            }
            return true;
        });
        return atomicInteger.get() + 1;
    }

    private void afterCollisionCheck(ServerWorld world, BlockPos pos, boolean blockCollision, boolean fluidCollision) {
        EntityBlockIntersectionType lv = fluidCollision ? EntityBlockIntersectionType.IN_FLUID : (blockCollision ? EntityBlockIntersectionType.IN_BLOCK : EntityBlockIntersectionType.IN_AIR);
        world.getSubscriptionTracker().sendBlockDebugData(pos, DebugSubscriptionTypes.ENTITY_BLOCK_INTERSECTIONS, lv);
    }

    public boolean collidesWithFluid(FluidState state, BlockPos fluidPos, Vec3d oldPos, Vec3d newPos) {
        Box lv = state.getCollisionBox(this.getEntityWorld(), fluidPos);
        return lv != null && this.collides(oldPos, newPos, List.of(lv));
    }

    public boolean collides(Vec3d oldPos, Vec3d newPos, List<Box> boxes) {
        Box lv = this.calculateDefaultBoundingBox(oldPos);
        Vec3d lv2 = newPos.subtract(oldPos);
        return lv.collides(lv2, boxes);
    }

    protected void onBlockCollision(BlockState state) {
    }

    public BlockPos getWorldSpawnPos(ServerWorld world, BlockPos basePos) {
        BlockPos lv = world.getSpawnPoint().getPos();
        Vec3d lv2 = lv.toCenterPos();
        int i = world.getWorldChunk(lv).sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, lv.getX(), lv.getZ()) + 1;
        return BlockPos.ofFloored(lv2.x, i, lv2.z);
    }

    public void emitGameEvent(RegistryEntry<GameEvent> event, @Nullable Entity entity) {
        this.getEntityWorld().emitGameEvent(entity, event, this.pos);
    }

    public void emitGameEvent(RegistryEntry<GameEvent> event) {
        this.emitGameEvent(event, this);
    }

    private void playStepSounds(BlockPos pos, BlockState state) {
        this.playStepSound(pos, state);
        if (this.shouldPlayAmethystChimeSound(state)) {
            this.playAmethystChimeSound();
        }
    }

    protected void playSwimSound() {
        Entity lv = Objects.requireNonNullElse(this.getControllingPassenger(), this);
        float f = lv == this ? 0.35f : 0.4f;
        Vec3d lv2 = lv.getVelocity();
        float g = Math.min(1.0f, (float)Math.sqrt(lv2.x * lv2.x * (double)0.2f + lv2.y * lv2.y + lv2.z * lv2.z * (double)0.2f) * f);
        this.playSwimSound(g);
    }

    protected BlockPos getStepSoundPos(BlockPos pos) {
        BlockPos lv = pos.up();
        BlockState lv2 = this.getEntityWorld().getBlockState(lv);
        if (lv2.isIn(BlockTags.INSIDE_STEP_SOUND_BLOCKS) || lv2.isIn(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
            return lv;
        }
        return pos;
    }

    protected void playCombinationStepSounds(BlockState primaryState, BlockState secondaryState) {
        BlockSoundGroup lv = primaryState.getSoundGroup();
        this.playSound(lv.getStepSound(), lv.getVolume() * 0.15f, lv.getPitch());
        this.playSecondaryStepSound(secondaryState);
    }

    protected void playSecondaryStepSound(BlockState state) {
        BlockSoundGroup lv = state.getSoundGroup();
        this.playSound(lv.getStepSound(), lv.getVolume() * 0.05f, lv.getPitch() * 0.8f);
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        BlockSoundGroup lv = state.getSoundGroup();
        this.playSound(lv.getStepSound(), lv.getVolume() * 0.15f, lv.getPitch());
    }

    private boolean shouldPlayAmethystChimeSound(BlockState state) {
        return state.isIn(BlockTags.CRYSTAL_SOUND_BLOCKS) && this.age >= this.lastChimeAge + 20;
    }

    private void playAmethystChimeSound() {
        this.lastChimeIntensity *= (float)Math.pow(0.997, this.age - this.lastChimeAge);
        this.lastChimeIntensity = Math.min(1.0f, this.lastChimeIntensity + 0.07f);
        float f = 0.5f + this.lastChimeIntensity * this.random.nextFloat() * 1.2f;
        float g = 0.1f + this.lastChimeIntensity * 1.2f;
        this.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, g, f);
        this.lastChimeAge = this.age;
    }

    protected void playSwimSound(float volume) {
        this.playSound(this.getSwimSound(), volume, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
    }

    protected void addFlapEffects() {
    }

    protected boolean isFlappingWings() {
        return false;
    }

    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (!this.isSilent()) {
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch);
        }
    }

    public void playSoundIfNotSilent(SoundEvent event) {
        if (!this.isSilent()) {
            this.playSound(event, 1.0f, 1.0f);
        }
    }

    public boolean isSilent() {
        return this.dataTracker.get(SILENT);
    }

    public void setSilent(boolean silent) {
        this.dataTracker.set(SILENT, silent);
    }

    public boolean hasNoGravity() {
        return this.dataTracker.get(NO_GRAVITY);
    }

    public void setNoGravity(boolean noGravity) {
        this.dataTracker.set(NO_GRAVITY, noGravity);
    }

    protected double getGravity() {
        return 0.0;
    }

    public final double getFinalGravity() {
        return this.hasNoGravity() ? 0.0 : this.getGravity();
    }

    protected void applyGravity() {
        double d = this.getFinalGravity();
        if (d != 0.0) {
            this.setVelocity(this.getVelocity().add(0.0, -d, 0.0));
        }
    }

    protected MoveEffect getMoveEffect() {
        return MoveEffect.ALL;
    }

    public boolean occludeVibrationSignals() {
        return false;
    }

    public final void handleFall(double xDifference, double yDifference, double zDifference, boolean onGround) {
        if (this.isRegionUnloaded()) {
            return;
        }
        this.updateSupportingBlockPos(onGround, new Vec3d(xDifference, yDifference, zDifference));
        BlockPos lv = this.getLandingPos();
        BlockState lv2 = this.getEntityWorld().getBlockState(lv);
        this.fall(yDifference, onGround, lv2, lv);
    }

    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        if (!this.isTouchingWater() && heightDifference < 0.0) {
            this.fallDistance -= (double)((float)heightDifference);
        }
        if (onGround) {
            if (this.fallDistance > 0.0) {
                state.getBlock().onLandedUpon(this.getEntityWorld(), state, landedPosition, this, this.fallDistance);
                this.getEntityWorld().emitGameEvent(GameEvent.HIT_GROUND, this.pos, GameEvent.Emitter.of(this, this.supportingBlockPos.map(pos -> this.getEntityWorld().getBlockState((BlockPos)pos)).orElse(state)));
            }
            this.onLanding();
        }
    }

    public boolean isFireImmune() {
        return this.getType().isFireImmune();
    }

    public boolean handleFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        if (this.type.isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return false;
        }
        this.handleFallDamageForPassengers(fallDistance, damagePerDistance, damageSource);
        return false;
    }

    protected void handleFallDamageForPassengers(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        if (this.hasPassengers()) {
            for (Entity lv : this.getPassengerList()) {
                lv.handleFallDamage(fallDistance, damagePerDistance, damageSource);
            }
        }
    }

    public boolean isTouchingWater() {
        return this.touchingWater;
    }

    boolean isBeingRainedOn() {
        BlockPos lv = this.getBlockPos();
        return this.getEntityWorld().hasRain(lv) || this.getEntityWorld().hasRain(BlockPos.ofFloored(lv.getX(), this.getBoundingBox().maxY, lv.getZ()));
    }

    public boolean isTouchingWaterOrRain() {
        return this.isTouchingWater() || this.isBeingRainedOn();
    }

    public boolean isInFluid() {
        return this.isTouchingWater() || this.isInLava();
    }

    public boolean isSubmergedInWater() {
        return this.submergedInWater && this.isTouchingWater();
    }

    public boolean isPartlyTouchingWater() {
        return this.isTouchingWater() && !this.isSubmergedInWater();
    }

    public boolean isAtCloudHeight() {
        Optional<Integer> optional = this.world.getDimension().cloudHeight();
        if (optional.isEmpty()) {
            return false;
        }
        int i = optional.get();
        if (this.getY() + (double)this.getHeight() < (double)i) {
            return false;
        }
        int j = i + 4;
        return this.getY() <= (double)j;
    }

    public void updateSwimming() {
        if (this.isSwimming()) {
            this.setSwimming(this.isSprinting() && this.isTouchingWater() && !this.hasVehicle());
        } else {
            this.setSwimming(this.isSprinting() && this.isSubmergedInWater() && !this.hasVehicle() && this.getEntityWorld().getFluidState(this.blockPos).isIn(FluidTags.WATER));
        }
    }

    protected boolean updateWaterState() {
        this.fluidHeight.clear();
        this.checkWaterState();
        double d = this.getEntityWorld().getDimension().ultrawarm() ? 0.007 : 0.0023333333333333335;
        boolean bl = this.updateMovementInFluid(FluidTags.LAVA, d);
        return this.isTouchingWater() || bl;
    }

    void checkWaterState() {
        AbstractBoatEntity lv;
        Entity entity = this.getVehicle();
        if (entity instanceof AbstractBoatEntity && !(lv = (AbstractBoatEntity)entity).isSubmergedInWater()) {
            this.touchingWater = false;
        } else if (this.updateMovementInFluid(FluidTags.WATER, 0.014)) {
            if (!this.touchingWater && !this.firstUpdate) {
                this.onSwimmingStart();
            }
            this.onLanding();
            this.touchingWater = true;
        } else {
            this.touchingWater = false;
        }
    }

    private void updateSubmergedInWaterState() {
        AbstractBoatEntity lv2;
        this.submergedInWater = this.isSubmergedIn(FluidTags.WATER);
        this.submergedFluidTag.clear();
        double d = this.getEyeY();
        Entity lv = this.getVehicle();
        if (lv instanceof AbstractBoatEntity && !(lv2 = (AbstractBoatEntity)lv).isSubmergedInWater() && lv2.getBoundingBox().maxY >= d && lv2.getBoundingBox().minY <= d) {
            return;
        }
        BlockPos lv3 = BlockPos.ofFloored(this.getX(), d, this.getZ());
        FluidState lv4 = this.getEntityWorld().getFluidState(lv3);
        double e = (float)lv3.getY() + lv4.getHeight(this.getEntityWorld(), lv3);
        if (e > d) {
            lv4.streamTags().forEach(this.submergedFluidTag::add);
        }
    }

    protected void onSwimmingStart() {
        double e;
        double d;
        Entity lv = Objects.requireNonNullElse(this.getControllingPassenger(), this);
        float f = lv == this ? 0.2f : 0.9f;
        Vec3d lv2 = lv.getVelocity();
        float g = Math.min(1.0f, (float)Math.sqrt(lv2.x * lv2.x * (double)0.2f + lv2.y * lv2.y + lv2.z * lv2.z * (double)0.2f) * f);
        if (g < 0.25f) {
            this.playSound(this.getSplashSound(), g, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
        } else {
            this.playSound(this.getHighSpeedSplashSound(), g, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
        }
        float h = MathHelper.floor(this.getY());
        int i = 0;
        while ((float)i < 1.0f + this.dimensions.width() * 20.0f) {
            d = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            e = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            this.getEntityWorld().addParticleClient(ParticleTypes.BUBBLE, this.getX() + d, h + 1.0f, this.getZ() + e, lv2.x, lv2.y - this.random.nextDouble() * (double)0.2f, lv2.z);
            ++i;
        }
        i = 0;
        while ((float)i < 1.0f + this.dimensions.width() * 20.0f) {
            d = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            e = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
            this.getEntityWorld().addParticleClient(ParticleTypes.SPLASH, this.getX() + d, h + 1.0f, this.getZ() + e, lv2.x, lv2.y, lv2.z);
            ++i;
        }
        this.emitGameEvent(GameEvent.SPLASH);
    }

    @Deprecated
    protected BlockState getLandingBlockState() {
        return this.getEntityWorld().getBlockState(this.getLandingPos());
    }

    public BlockState getSteppingBlockState() {
        return this.getEntityWorld().getBlockState(this.getSteppingPos());
    }

    public boolean shouldSpawnSprintingParticles() {
        return this.isSprinting() && !this.isTouchingWater() && !this.isSpectator() && !this.isInSneakingPose() && !this.isInLava() && this.isAlive();
    }

    protected void spawnSprintingParticles() {
        BlockPos lv = this.getLandingPos();
        BlockState lv2 = this.getEntityWorld().getBlockState(lv);
        if (lv2.getRenderType() != BlockRenderType.INVISIBLE) {
            Vec3d lv3 = this.getVelocity();
            BlockPos lv4 = this.getBlockPos();
            double d = this.getX() + (this.random.nextDouble() - 0.5) * (double)this.dimensions.width();
            double e = this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.dimensions.width();
            if (lv4.getX() != lv.getX()) {
                d = MathHelper.clamp(d, (double)lv.getX(), (double)lv.getX() + 1.0);
            }
            if (lv4.getZ() != lv.getZ()) {
                e = MathHelper.clamp(e, (double)lv.getZ(), (double)lv.getZ() + 1.0);
            }
            this.getEntityWorld().addParticleClient(new BlockStateParticleEffect(ParticleTypes.BLOCK, lv2), d, this.getY() + 0.1, e, lv3.x * -4.0, 1.5, lv3.z * -4.0);
        }
    }

    public boolean isSubmergedIn(TagKey<Fluid> fluidTag) {
        return this.submergedFluidTag.contains(fluidTag);
    }

    public boolean isInLava() {
        return !this.firstUpdate && this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0;
    }

    public void updateVelocity(float speed, Vec3d movementInput) {
        Vec3d lv = Entity.movementInputToVelocity(movementInput, speed, this.getYaw());
        this.setVelocity(this.getVelocity().add(lv));
    }

    protected static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        }
        Vec3d lv = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
        float h = MathHelper.sin(yaw * ((float)Math.PI / 180));
        float i = MathHelper.cos(yaw * ((float)Math.PI / 180));
        return new Vec3d(lv.x * (double)i - lv.z * (double)h, lv.y, lv.z * (double)i + lv.x * (double)h);
    }

    @Deprecated
    public float getBrightnessAtEyes() {
        if (this.getEntityWorld().isPosLoaded(this.getBlockX(), this.getBlockZ())) {
            return this.getEntityWorld().getBrightness(BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ()));
        }
        return 0.0f;
    }

    public void updatePositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        this.updatePosition(x, y, z);
        this.setAngles(yaw, pitch);
    }

    public void setAngles(float yaw, float pitch) {
        this.setYaw(yaw % 360.0f);
        this.setPitch(MathHelper.clamp(pitch, -90.0f, 90.0f) % 360.0f);
        this.lastYaw = this.getYaw();
        this.lastPitch = this.getPitch();
    }

    public void updatePosition(double x, double y, double z) {
        double g = MathHelper.clamp(x, -3.0E7, 3.0E7);
        double h = MathHelper.clamp(z, -3.0E7, 3.0E7);
        this.lastX = g;
        this.lastY = y;
        this.lastZ = h;
        this.setPosition(g, y, h);
    }

    public void refreshPositionAfterTeleport(Vec3d pos) {
        this.refreshPositionAfterTeleport(pos.x, pos.y, pos.z);
    }

    public void refreshPositionAfterTeleport(double x, double y, double z) {
        this.refreshPositionAndAngles(x, y, z, this.getYaw(), this.getPitch());
    }

    public void refreshPositionAndAngles(BlockPos pos, float yaw, float pitch) {
        this.refreshPositionAndAngles(pos.toBottomCenterPos(), yaw, pitch);
    }

    public void refreshPositionAndAngles(Vec3d pos, float yaw, float pitch) {
        this.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
    }

    public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        this.setPos(x, y, z);
        this.setYaw(yaw);
        this.setPitch(pitch);
        this.resetPosition();
        this.refreshPosition();
    }

    public final void resetPosition() {
        this.updateLastPosition();
        this.updateLastAngles();
    }

    public final void setLastPositionAndAngles(Vec3d pos, float yaw, float pitch) {
        this.setLastPosition(pos);
        this.setLastAngles(yaw, pitch);
    }

    protected void updateLastPosition() {
        this.setLastPosition(this.pos);
    }

    public void updateLastAngles() {
        this.setLastAngles(this.getYaw(), this.getPitch());
    }

    private void setLastPosition(Vec3d pos) {
        this.lastX = this.lastRenderX = pos.x;
        this.lastY = this.lastRenderY = pos.y;
        this.lastZ = this.lastRenderZ = pos.z;
    }

    private void setLastAngles(float lastYaw, float lastPitch) {
        this.lastYaw = lastYaw;
        this.lastPitch = lastPitch;
    }

    public final Vec3d getLastRenderPos() {
        return new Vec3d(this.lastRenderX, this.lastRenderY, this.lastRenderZ);
    }

    public float distanceTo(Entity entity) {
        float f = (float)(this.getX() - entity.getX());
        float g = (float)(this.getY() - entity.getY());
        float h = (float)(this.getZ() - entity.getZ());
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

    public double squaredDistanceTo(double x, double y, double z) {
        double g = this.getX() - x;
        double h = this.getY() - y;
        double i = this.getZ() - z;
        return g * g + h * h + i * i;
    }

    public double squaredDistanceTo(Entity entity) {
        return this.squaredDistanceTo(entity.getEntityPos());
    }

    public double squaredDistanceTo(Vec3d vector) {
        double d = this.getX() - vector.x;
        double e = this.getY() - vector.y;
        double f = this.getZ() - vector.z;
        return d * d + e * e + f * f;
    }

    public void onPlayerCollision(PlayerEntity player) {
    }

    public void pushAwayFrom(Entity entity) {
        double e;
        if (this.isConnectedThroughVehicle(entity)) {
            return;
        }
        if (entity.noClip || this.noClip) {
            return;
        }
        double d = entity.getX() - this.getX();
        double f = MathHelper.absMax(d, e = entity.getZ() - this.getZ());
        if (f >= (double)0.01f) {
            f = Math.sqrt(f);
            d /= f;
            e /= f;
            double g = 1.0 / f;
            if (g > 1.0) {
                g = 1.0;
            }
            d *= g;
            e *= g;
            d *= (double)0.05f;
            e *= (double)0.05f;
            if (!this.hasPassengers() && this.isPushable()) {
                this.addVelocity(-d, 0.0, -e);
            }
            if (!entity.hasPassengers() && entity.isPushable()) {
                entity.addVelocity(d, 0.0, e);
            }
        }
    }

    public void addVelocity(Vec3d velocity) {
        this.addVelocity(velocity.x, velocity.y, velocity.z);
    }

    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        this.setVelocity(this.getVelocity().add(deltaX, deltaY, deltaZ));
        this.velocityDirty = true;
    }

    protected void scheduleVelocityUpdate() {
        this.velocityModified = true;
    }

    @Deprecated
    public final void serverDamage(DamageSource source, float amount) {
        World world = this.world;
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            this.damage(lv, source, amount);
        }
    }

    @Deprecated
    public final boolean sidedDamage(DamageSource source, float amount) {
        World world = this.world;
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            return this.damage(lv, source, amount);
        }
        return this.clientDamage(source);
    }

    public abstract boolean damage(ServerWorld var1, DamageSource var2, float var3);

    public boolean clientDamage(DamageSource source) {
        return false;
    }

    public final Vec3d getRotationVec(float tickProgress) {
        return this.getRotationVector(this.getPitch(tickProgress), this.getYaw(tickProgress));
    }

    public Direction getFacing() {
        return Direction.getFacing(this.getRotationVec(1.0f));
    }

    public float getPitch(float tickProgress) {
        return this.getLerpedPitch(tickProgress);
    }

    public float getYaw(float tickProgress) {
        return this.getLerpedYaw(tickProgress);
    }

    public float getLerpedPitch(float tickProgress) {
        if (tickProgress == 1.0f) {
            return this.getPitch();
        }
        return MathHelper.lerp(tickProgress, this.lastPitch, this.getPitch());
    }

    public float getLerpedYaw(float tickProgress) {
        if (tickProgress == 1.0f) {
            return this.getYaw();
        }
        return MathHelper.lerpAngleDegrees(tickProgress, this.lastYaw, this.getYaw());
    }

    public final Vec3d getRotationVector(float pitch, float yaw) {
        float h = pitch * ((float)Math.PI / 180);
        float i = -yaw * ((float)Math.PI / 180);
        float j = MathHelper.cos(i);
        float k = MathHelper.sin(i);
        float l = MathHelper.cos(h);
        float m = MathHelper.sin(h);
        return new Vec3d(k * l, -m, j * l);
    }

    public final Vec3d getOppositeRotationVector(float tickProgress) {
        return this.getOppositeRotationVector(this.getPitch(tickProgress), this.getYaw(tickProgress));
    }

    protected final Vec3d getOppositeRotationVector(float pitch, float yaw) {
        return this.getRotationVector(pitch - 90.0f, yaw);
    }

    public final Vec3d getEyePos() {
        return new Vec3d(this.getX(), this.getEyeY(), this.getZ());
    }

    public final Vec3d getCameraPosVec(float tickProgress) {
        double d = MathHelper.lerp((double)tickProgress, this.lastX, this.getX());
        double e = MathHelper.lerp((double)tickProgress, this.lastY, this.getY()) + (double)this.getStandingEyeHeight();
        double g = MathHelper.lerp((double)tickProgress, this.lastZ, this.getZ());
        return new Vec3d(d, e, g);
    }

    public Vec3d getClientCameraPosVec(float tickProgress) {
        return this.getCameraPosVec(tickProgress);
    }

    public final Vec3d getLerpedPos(float deltaTicks) {
        double d = MathHelper.lerp((double)deltaTicks, this.lastX, this.getX());
        double e = MathHelper.lerp((double)deltaTicks, this.lastY, this.getY());
        double g = MathHelper.lerp((double)deltaTicks, this.lastZ, this.getZ());
        return new Vec3d(d, e, g);
    }

    public HitResult raycast(double maxDistance, float tickProgress, boolean includeFluids) {
        Vec3d lv = this.getCameraPosVec(tickProgress);
        Vec3d lv2 = this.getRotationVec(tickProgress);
        Vec3d lv3 = lv.add(lv2.x * maxDistance, lv2.y * maxDistance, lv2.z * maxDistance);
        return this.getEntityWorld().raycast(new RaycastContext(lv, lv3, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, this));
    }

    public boolean canBeHitByProjectile() {
        return this.isAlive() && this.canHit();
    }

    public boolean canHit() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    public void updateKilledAdvancementCriterion(Entity entityKilled, DamageSource damageSource) {
        if (entityKilled instanceof ServerPlayerEntity) {
            Criteria.ENTITY_KILLED_PLAYER.trigger((ServerPlayerEntity)entityKilled, this, damageSource);
        }
    }

    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        double g = this.getX() - cameraX;
        double h = this.getY() - cameraY;
        double i = this.getZ() - cameraZ;
        double j = g * g + h * h + i * i;
        return this.shouldRender(j);
    }

    public boolean shouldRender(double distance) {
        double e = this.getBoundingBox().getAverageSideLength();
        if (Double.isNaN(e)) {
            e = 1.0;
        }
        return distance < (e *= 64.0 * renderDistanceMultiplier) * e;
    }

    public boolean saveSelfData(WriteView view) {
        if (this.removalReason != null && !this.removalReason.shouldSave()) {
            return false;
        }
        String string = this.getSavedEntityId();
        if (string == null) {
            return false;
        }
        view.putString(ID_KEY, string);
        this.writeData(view);
        return true;
    }

    public boolean saveData(WriteView view) {
        if (this.hasVehicle()) {
            return false;
        }
        return this.saveSelfData(view);
    }

    public void writeData(WriteView view) {
        try {
            int i;
            if (this.vehicle != null) {
                view.put(POS_KEY, Vec3d.CODEC, new Vec3d(this.vehicle.getX(), this.getY(), this.vehicle.getZ()));
            } else {
                view.put(POS_KEY, Vec3d.CODEC, this.getEntityPos());
            }
            view.put(MOTION_KEY, Vec3d.CODEC, this.getVelocity());
            view.put(ROTATION_KEY, Vec2f.CODEC, new Vec2f(this.getYaw(), this.getPitch()));
            view.putDouble(FALL_DISTANCE_KEY, this.fallDistance);
            view.putShort(FIRE_KEY, (short)this.fireTicks);
            view.putShort(AIR_KEY, (short)this.getAir());
            view.putBoolean(ON_GROUND_KEY, this.isOnGround());
            view.putBoolean(INVULNERABLE_KEY, this.invulnerable);
            view.putInt(PORTAL_COOLDOWN_KEY, this.portalCooldown);
            view.put(UUID_KEY, Uuids.INT_STREAM_CODEC, this.getUuid());
            view.putNullable(CUSTOM_NAME_KEY, TextCodecs.CODEC, this.getCustomName());
            if (this.isCustomNameVisible()) {
                view.putBoolean("CustomNameVisible", this.isCustomNameVisible());
            }
            if (this.isSilent()) {
                view.putBoolean(SILENT_KEY, this.isSilent());
            }
            if (this.hasNoGravity()) {
                view.putBoolean(NO_GRAVITY_KEY, this.hasNoGravity());
            }
            if (this.glowing) {
                view.putBoolean(GLOWING_KEY, true);
            }
            if ((i = this.getFrozenTicks()) > 0) {
                view.putInt("TicksFrozen", this.getFrozenTicks());
            }
            if (this.hasVisualFire) {
                view.putBoolean("HasVisualFire", this.hasVisualFire);
            }
            if (!this.commandTags.isEmpty()) {
                view.put("Tags", TAG_LIST_CODEC, List.copyOf(this.commandTags));
            }
            if (!this.customData.isEmpty()) {
                view.put(CUSTOM_DATA_KEY, NbtComponent.CODEC, this.customData);
            }
            this.writeCustomData(view);
            if (this.hasPassengers()) {
                WriteView.ListView lv = view.getList(PASSENGERS_KEY);
                for (Entity lv2 : this.getPassengerList()) {
                    WriteView lv3;
                    if (lv2.saveSelfData(lv3 = lv.add())) continue;
                    lv.removeLast();
                }
                if (lv.isEmpty()) {
                    view.remove(PASSENGERS_KEY);
                }
            }
        } catch (Throwable throwable) {
            CrashReport lv4 = CrashReport.create(throwable, "Saving entity NBT");
            CrashReportSection lv5 = lv4.addElement("Entity being saved");
            this.populateCrashReport(lv5);
            throw new CrashException(lv4);
        }
    }

    public void readData(ReadView view) {
        try {
            Vec3d lv = view.read(POS_KEY, Vec3d.CODEC).orElse(Vec3d.ZERO);
            Vec3d lv2 = view.read(MOTION_KEY, Vec3d.CODEC).orElse(Vec3d.ZERO);
            Vec2f lv3 = view.read(ROTATION_KEY, Vec2f.CODEC).orElse(Vec2f.ZERO);
            this.setVelocity(Math.abs(lv2.x) > 10.0 ? 0.0 : lv2.x, Math.abs(lv2.y) > 10.0 ? 0.0 : lv2.y, Math.abs(lv2.z) > 10.0 ? 0.0 : lv2.z);
            this.velocityDirty = true;
            double d = 3.0000512E7;
            this.setPos(MathHelper.clamp(lv.x, -3.0000512E7, 3.0000512E7), MathHelper.clamp(lv.y, -2.0E7, 2.0E7), MathHelper.clamp(lv.z, -3.0000512E7, 3.0000512E7));
            this.setYaw(lv3.x);
            this.setPitch(lv3.y);
            this.resetPosition();
            this.setHeadYaw(this.getYaw());
            this.setBodyYaw(this.getYaw());
            this.fallDistance = view.getDouble(FALL_DISTANCE_KEY, 0.0);
            this.fireTicks = view.getShort(FIRE_KEY, (short)0);
            this.setAir(view.getInt(AIR_KEY, this.getMaxAir()));
            this.onGround = view.getBoolean(ON_GROUND_KEY, false);
            this.invulnerable = view.getBoolean(INVULNERABLE_KEY, false);
            this.portalCooldown = view.getInt(PORTAL_COOLDOWN_KEY, 0);
            view.read(UUID_KEY, Uuids.INT_STREAM_CODEC).ifPresent(uuid -> {
                this.uuid = uuid;
                this.uuidString = this.uuid.toString();
            });
            if (!(Double.isFinite(this.getX()) && Double.isFinite(this.getY()) && Double.isFinite(this.getZ()))) {
                throw new IllegalStateException("Entity has invalid position");
            }
            if (!Double.isFinite(this.getYaw()) || !Double.isFinite(this.getPitch())) {
                throw new IllegalStateException("Entity has invalid rotation");
            }
            this.refreshPosition();
            this.setRotation(this.getYaw(), this.getPitch());
            this.setCustomName(view.read(CUSTOM_NAME_KEY, TextCodecs.CODEC).orElse(null));
            this.setCustomNameVisible(view.getBoolean("CustomNameVisible", false));
            this.setSilent(view.getBoolean(SILENT_KEY, false));
            this.setNoGravity(view.getBoolean(NO_GRAVITY_KEY, false));
            this.setGlowing(view.getBoolean(GLOWING_KEY, false));
            this.setFrozenTicks(view.getInt("TicksFrozen", 0));
            this.hasVisualFire = view.getBoolean("HasVisualFire", false);
            this.customData = view.read(CUSTOM_DATA_KEY, NbtComponent.CODEC).orElse(NbtComponent.DEFAULT);
            this.commandTags.clear();
            view.read("Tags", TAG_LIST_CODEC).ifPresent(this.commandTags::addAll);
            this.readCustomData(view);
            if (this.shouldSetPositionOnLoad()) {
                this.refreshPosition();
            }
        } catch (Throwable throwable) {
            CrashReport lv4 = CrashReport.create(throwable, "Loading entity NBT");
            CrashReportSection lv5 = lv4.addElement("Entity being loaded");
            this.populateCrashReport(lv5);
            throw new CrashException(lv4);
        }
    }

    protected boolean shouldSetPositionOnLoad() {
        return true;
    }

    @Nullable
    protected final String getSavedEntityId() {
        EntityType<?> lv = this.getType();
        Identifier lv2 = EntityType.getId(lv);
        return !lv.isSaveable() ? null : lv2.toString();
    }

    protected abstract void readCustomData(ReadView var1);

    protected abstract void writeCustomData(WriteView var1);

    @Nullable
    public ItemEntity dropItem(ServerWorld world, ItemConvertible item) {
        return this.dropStack(world, new ItemStack(item), 0.0f);
    }

    @Nullable
    public ItemEntity dropStack(ServerWorld world, ItemStack stack) {
        return this.dropStack(world, stack, 0.0f);
    }

    @Nullable
    public ItemEntity dropStack(ServerWorld world, ItemStack stack, Vec3d offset) {
        if (stack.isEmpty()) {
            return null;
        }
        ItemEntity lv = new ItemEntity(world, this.getX() + offset.x, this.getY() + offset.y, this.getZ() + offset.z, stack);
        lv.setToDefaultPickupDelay();
        world.spawnEntity(lv);
        return lv;
    }

    @Nullable
    public ItemEntity dropStack(ServerWorld world, ItemStack stack, float yOffset) {
        return this.dropStack(world, stack, new Vec3d(0.0, yOffset, 0.0));
    }

    public boolean isAlive() {
        return !this.isRemoved();
    }

    public boolean isInsideWall() {
        if (this.noClip) {
            return false;
        }
        float f = this.dimensions.width() * 0.8f;
        Box lv = Box.of(this.getEyePos(), f, 1.0E-6, f);
        return BlockPos.stream(lv).anyMatch(pos -> {
            BlockState lv = this.getEntityWorld().getBlockState((BlockPos)pos);
            return !lv.isAir() && lv.shouldSuffocate(this.getEntityWorld(), (BlockPos)pos) && VoxelShapes.matchesAnywhere(lv.getCollisionShape(this.getEntityWorld(), (BlockPos)pos).offset((Vec3i)pos), VoxelShapes.cuboid(lv), BooleanBiFunction.AND);
        });
    }

    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack lv4;
        Object list;
        LivingEntity lv2;
        Entity entity;
        Leashable lv;
        Entity entity2;
        if (!this.getEntityWorld().isClient() && player.shouldCancelInteraction() && (entity2 = this) instanceof Leashable && (lv = (Leashable)((Object)entity2)).canBeLeashed() && this.isAlive() && (!((entity = this) instanceof LivingEntity) || !(lv2 = (LivingEntity)entity).isBaby()) && !(list = Leashable.collectLeashablesAround(this, leashable -> leashable.getLeashHolder() == player)).isEmpty()) {
            boolean bl = false;
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                Leashable lv3 = (Leashable)iterator.next();
                if (!lv3.canBeLeashedTo(this)) continue;
                lv3.attachLeash(this, true);
                bl = true;
            }
            if (bl) {
                this.getEntityWorld().emitGameEvent(GameEvent.ENTITY_ACTION, this.getBlockPos(), GameEvent.Emitter.of(player));
                this.playSoundIfNotSilent(SoundEvents.ITEM_LEAD_TIED);
                return ActionResult.SUCCESS_SERVER.noIncrementStat();
            }
        }
        if ((lv4 = player.getStackInHand(hand)).isOf(Items.SHEARS) && this.snipAllHeldLeashes(player)) {
            lv4.damage(1, (LivingEntity)player, hand);
            return ActionResult.SUCCESS;
        }
        list = this;
        if (list instanceof MobEntity) {
            MobEntity lv5 = (MobEntity)list;
            if (lv4.isOf(Items.SHEARS) && lv5.canRemoveSaddle(player) && !player.shouldCancelInteraction() && this.shearEquipment(player, hand, lv4, lv5)) {
                return ActionResult.SUCCESS;
            }
        }
        if (this.isAlive() && (list = this) instanceof Leashable) {
            Leashable lv6 = (Leashable)list;
            if (lv6.getLeashHolder() == player) {
                if (!this.getEntityWorld().isClient()) {
                    if (player.isInCreativeMode()) {
                        lv6.detachLeashWithoutDrop();
                    } else {
                        lv6.detachLeash();
                    }
                    this.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
                    this.playSoundIfNotSilent(SoundEvents.ITEM_LEAD_UNTIED);
                }
                return ActionResult.SUCCESS.noIncrementStat();
            }
            ItemStack lv7 = player.getStackInHand(hand);
            if (lv7.isOf(Items.LEAD) && !(lv6.getLeashHolder() instanceof PlayerEntity)) {
                if (!this.getEntityWorld().isClient() && lv6.canBeLeashedTo(player)) {
                    if (lv6.isLeashed()) {
                        lv6.detachLeash();
                    }
                    lv6.attachLeash(player, true);
                    this.playSoundIfNotSilent(SoundEvents.ITEM_LEAD_TIED);
                    lv7.decrement(1);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    public boolean snipAllHeldLeashes(@Nullable PlayerEntity player) {
        World world;
        boolean bl = this.detachAllHeldLeashes(player);
        if (bl && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            lv.playSound(null, this.getBlockPos(), SoundEvents.ITEM_SHEARS_SNIP, player != null ? player.getSoundCategory() : this.getSoundCategory());
        }
        return bl;
    }

    public boolean detachAllHeldLeashes(@Nullable PlayerEntity player) {
        Leashable lv;
        List<Leashable> list = Leashable.collectLeashablesHeldBy(this);
        boolean bl = !list.isEmpty();
        Entity entity = this;
        if (entity instanceof Leashable && (lv = (Leashable)((Object)entity)).isLeashed()) {
            lv.detachLeash();
            bl = true;
        }
        for (Leashable lv2 : list) {
            lv2.detachLeash();
        }
        if (bl) {
            this.emitGameEvent(GameEvent.SHEAR, player);
            return true;
        }
        return false;
    }

    private boolean shearEquipment(PlayerEntity player, Hand hand, ItemStack shears, MobEntity entity) {
        for (EquipmentSlot lv : EquipmentSlot.VALUES) {
            ItemStack lv2 = entity.getEquippedStack(lv);
            EquippableComponent lv3 = lv2.get(DataComponentTypes.EQUIPPABLE);
            if (lv3 == null || !lv3.canBeSheared() || EnchantmentHelper.hasAnyEnchantmentsWith(lv2, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE) && !player.isCreative()) continue;
            shears.damage(1, (LivingEntity)player, hand.getEquipmentSlot());
            Vec3d lv4 = this.dimensions.attachments().getPointOrDefault(EntityAttachmentType.PASSENGER);
            entity.equipLootStack(lv, ItemStack.EMPTY);
            this.emitGameEvent(GameEvent.SHEAR, player);
            this.playSoundIfNotSilent(lv3.shearingSound().value());
            World world = this.getEntityWorld();
            if (world instanceof ServerWorld) {
                ServerWorld lv5 = (ServerWorld)world;
                this.dropStack(lv5, lv2, lv4);
                Criteria.PLAYER_SHEARED_EQUIPMENT.trigger((ServerPlayerEntity)player, lv2, entity);
            }
            return true;
        }
        return false;
    }

    public boolean collidesWith(Entity other) {
        return other.isCollidable(this) && !this.isConnectedThroughVehicle(other);
    }

    public boolean isCollidable(@Nullable Entity entity) {
        return false;
    }

    public void tickRiding() {
        this.setVelocity(Vec3d.ZERO);
        this.tick();
        if (!this.hasVehicle()) {
            return;
        }
        this.getVehicle().updatePassengerPosition(this);
    }

    public final void updatePassengerPosition(Entity passenger) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        this.updatePassengerPosition(passenger, Entity::setPosition);
    }

    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        Vec3d lv = this.getPassengerRidingPos(passenger);
        Vec3d lv2 = passenger.getVehicleAttachmentPos(this);
        positionUpdater.accept(passenger, lv.x - lv2.x, lv.y - lv2.y, lv.z - lv2.z);
    }

    public void onPassengerLookAround(Entity passenger) {
    }

    public Vec3d getVehicleAttachmentPos(Entity vehicle) {
        return this.getAttachments().getPoint(EntityAttachmentType.VEHICLE, 0, this.yaw);
    }

    public Vec3d getPassengerRidingPos(Entity passenger) {
        return this.getEntityPos().add(this.getPassengerAttachmentPos(passenger, this.dimensions, 1.0f));
    }

    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        return Entity.getPassengerAttachmentPos(this, passenger, dimensions.attachments());
    }

    protected static Vec3d getPassengerAttachmentPos(Entity vehicle, Entity passenger, EntityAttachments attachments) {
        int i = vehicle.getPassengerList().indexOf(passenger);
        return attachments.getPointOrDefault(EntityAttachmentType.PASSENGER, i, vehicle.yaw);
    }

    public final boolean startRiding(Entity entity) {
        return this.startRiding(entity, false, true);
    }

    public boolean isLiving() {
        return this instanceof LivingEntity;
    }

    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
        if (entity == this.vehicle) {
            return false;
        }
        if (!entity.couldAcceptPassenger()) {
            return false;
        }
        if (!this.getEntityWorld().isClient() && !entity.type.isSaveable()) {
            return false;
        }
        Entity lv = entity;
        while (lv.vehicle != null) {
            if (lv.vehicle == this) {
                return false;
            }
            lv = lv.vehicle;
        }
        if (!(force || this.canStartRiding(entity) && entity.canAddPassenger(this))) {
            return false;
        }
        if (this.hasVehicle()) {
            this.stopRiding();
        }
        this.setPose(EntityPose.STANDING);
        this.vehicle = entity;
        this.vehicle.addPassenger(this);
        if (emitEvent) {
            this.getEntityWorld().emitGameEvent(this, GameEvent.ENTITY_MOUNT, this.vehicle.pos);
            entity.streamIntoPassengers().filter(passenger -> passenger instanceof ServerPlayerEntity).forEach(player -> Criteria.STARTED_RIDING.trigger((ServerPlayerEntity)player));
        }
        return true;
    }

    protected boolean canStartRiding(Entity entity) {
        return !this.isSneaking() && this.ridingCooldown <= 0;
    }

    public void removeAllPassengers() {
        for (int i = this.passengerList.size() - 1; i >= 0; --i) {
            ((Entity)this.passengerList.get(i)).stopRiding();
        }
    }

    public void dismountVehicle() {
        if (this.vehicle != null) {
            Entity lv = this.vehicle;
            this.vehicle = null;
            lv.removePassenger(this);
            RemovalReason lv2 = this.getRemovalReason();
            if (lv2 == null || lv2.shouldDestroy()) {
                this.getEntityWorld().emitGameEvent(this, GameEvent.ENTITY_DISMOUNT, lv.pos);
            }
        }
    }

    public void stopRiding() {
        this.dismountVehicle();
    }

    protected void addPassenger(Entity passenger) {
        if (passenger.getVehicle() != this) {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        }
        if (this.passengerList.isEmpty()) {
            this.passengerList = ImmutableList.of(passenger);
        } else {
            ArrayList<Entity> list = Lists.newArrayList(this.passengerList);
            if (!this.getEntityWorld().isClient() && passenger instanceof PlayerEntity && !(this.getFirstPassenger() instanceof PlayerEntity)) {
                list.add(0, passenger);
            } else {
                list.add(passenger);
            }
            this.passengerList = ImmutableList.copyOf(list);
        }
    }

    protected void removePassenger(Entity passenger) {
        if (passenger.getVehicle() == this) {
            throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
        }
        this.passengerList = this.passengerList.size() == 1 && this.passengerList.get(0) == passenger ? ImmutableList.of() : this.passengerList.stream().filter(entity -> entity != passenger).collect(ImmutableList.toImmutableList());
        passenger.ridingCooldown = 60;
    }

    protected boolean canAddPassenger(Entity passenger) {
        return this.passengerList.isEmpty();
    }

    protected boolean couldAcceptPassenger() {
        return true;
    }

    public final boolean isInterpolating() {
        return this.getInterpolator() != null && this.getInterpolator().isInterpolating();
    }

    public final void updateTrackedPositionAndAngles(Vec3d pos, float f, float g) {
        this.updateTrackedPositionAndAngles(Optional.of(pos), Optional.of(Float.valueOf(f)), Optional.of(Float.valueOf(g)));
    }

    public final void updateTrackedAngles(float f, float g) {
        this.updateTrackedPositionAndAngles(Optional.empty(), Optional.of(Float.valueOf(f)), Optional.of(Float.valueOf(g)));
    }

    public final void updateTrackedPosition(Vec3d arg) {
        this.updateTrackedPositionAndAngles(Optional.of(arg), Optional.empty(), Optional.empty());
    }

    public final void updateTrackedPositionAndAngles(Optional<Vec3d> optional, Optional<Float> optional2, Optional<Float> optional3) {
        PositionInterpolator lv = this.getInterpolator();
        if (lv != null) {
            lv.refreshPositionAndAngles(optional.orElse(lv.getLerpedPos()), optional2.orElse(Float.valueOf(lv.getLerpedYaw())).floatValue(), optional3.orElse(Float.valueOf(lv.getLerpedPitch())).floatValue());
        } else {
            optional.ifPresent(this::setPosition);
            optional2.ifPresent(float_ -> this.setYaw(float_.floatValue() % 360.0f));
            optional3.ifPresent(float_ -> this.setPitch(float_.floatValue() % 360.0f));
        }
    }

    @Nullable
    public PositionInterpolator getInterpolator() {
        return null;
    }

    public void updateTrackedHeadRotation(float yaw, int interpolationSteps) {
        this.setHeadYaw(yaw);
    }

    public float getTargetingMargin() {
        return 0.0f;
    }

    public Vec3d getRotationVector() {
        return this.getRotationVector(this.getPitch(), this.getYaw());
    }

    public Vec3d getHandPosOffset(Item item) {
        Entity entity = this;
        if (entity instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)entity;
            boolean bl = lv.getOffHandStack().isOf(item) && !lv.getMainHandStack().isOf(item);
            Arm lv2 = bl ? lv.getMainArm().getOpposite() : lv.getMainArm();
            return this.getRotationVector(0.0f, this.getYaw() + (float)(lv2 == Arm.RIGHT ? 80 : -80)).multiply(0.5);
        }
        return Vec3d.ZERO;
    }

    public Vec2f getRotationClient() {
        return new Vec2f(this.getPitch(), this.getYaw());
    }

    public Vec3d getRotationVecClient() {
        return Vec3d.fromPolar(this.getRotationClient());
    }

    public void tryUsePortal(Portal portal, BlockPos pos) {
        if (this.hasPortalCooldown()) {
            this.resetPortalCooldown();
            return;
        }
        if (this.portalManager == null || !this.portalManager.portalMatches(portal)) {
            this.portalManager = new PortalManager(portal, pos.toImmutable());
        } else if (!this.portalManager.isInPortal()) {
            this.portalManager.setPortalPos(pos.toImmutable());
            this.portalManager.setInPortal(true);
        }
    }

    protected void tickPortalTeleportation() {
        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld lv = (ServerWorld)world;
        this.tickPortalCooldown();
        if (this.portalManager == null) {
            return;
        }
        if (this.portalManager.tick(lv, this, this.canUsePortals(false))) {
            Profiler lv2 = Profilers.get();
            lv2.push("portal");
            this.resetPortalCooldown();
            TeleportTarget lv3 = this.portalManager.createTeleportTarget(lv, this);
            if (lv3 != null) {
                ServerWorld lv4 = lv3.world();
                if (lv.getServer().isEnterableWithPortal(lv4) && (lv4.getRegistryKey() == lv.getRegistryKey() || this.canTeleportBetween(lv, lv4))) {
                    this.teleportTo(lv3);
                }
            }
            lv2.pop();
        } else if (this.portalManager.hasExpired()) {
            this.portalManager = null;
        }
    }

    public int getDefaultPortalCooldown() {
        Entity lv = this.getFirstPassenger();
        return lv instanceof ServerPlayerEntity ? lv.getDefaultPortalCooldown() : 300;
    }

    public void setVelocityClient(Vec3d clientVelocity) {
        this.setVelocity(clientVelocity);
    }

    public void onDamaged(DamageSource damageSource) {
    }

    public void handleStatus(byte status) {
        switch (status) {
            case 53: {
                HoneyBlock.addRegularParticles(this);
            }
        }
    }

    public void animateDamage(float yaw) {
    }

    public boolean isOnFire() {
        boolean bl = this.getEntityWorld() != null && this.getEntityWorld().isClient();
        return !this.isFireImmune() && (this.fireTicks > 0 || bl && this.getFlag(ON_FIRE_FLAG_INDEX));
    }

    public boolean hasVehicle() {
        return this.getVehicle() != null;
    }

    public boolean hasPassengers() {
        return !this.passengerList.isEmpty();
    }

    public boolean shouldDismountUnderwater() {
        return this.getType().isIn(EntityTypeTags.DISMOUNTS_UNDERWATER);
    }

    public boolean shouldControlVehicles() {
        return !this.getType().isIn(EntityTypeTags.NON_CONTROLLING_RIDER);
    }

    public void setSneaking(boolean sneaking) {
        this.setFlag(SNEAKING_FLAG_INDEX, sneaking);
    }

    public boolean isSneaking() {
        return this.getFlag(SNEAKING_FLAG_INDEX);
    }

    public boolean bypassesSteppingEffects() {
        return this.isSneaking();
    }

    public boolean bypassesLandingEffects() {
        return this.isSneaking();
    }

    public boolean isSneaky() {
        return this.isSneaking();
    }

    public boolean isDescending() {
        return this.isSneaking();
    }

    public boolean isInSneakingPose() {
        return this.isInPose(EntityPose.CROUCHING);
    }

    public boolean isSprinting() {
        return this.getFlag(SPRINTING_FLAG_INDEX);
    }

    public void setSprinting(boolean sprinting) {
        this.setFlag(SPRINTING_FLAG_INDEX, sprinting);
    }

    public boolean isSwimming() {
        return this.getFlag(SWIMMING_FLAG_INDEX);
    }

    public boolean isInSwimmingPose() {
        return this.isInPose(EntityPose.SWIMMING);
    }

    public boolean isCrawling() {
        return this.isInSwimmingPose() && !this.isTouchingWater();
    }

    public void setSwimming(boolean swimming) {
        this.setFlag(SWIMMING_FLAG_INDEX, swimming);
    }

    public final boolean isGlowingLocal() {
        return this.glowing;
    }

    public final void setGlowing(boolean glowing) {
        this.glowing = glowing;
        this.setFlag(GLOWING_FLAG_INDEX, this.isGlowing());
    }

    public boolean isGlowing() {
        if (this.getEntityWorld().isClient()) {
            return this.getFlag(GLOWING_FLAG_INDEX);
        }
        return this.glowing;
    }

    public boolean isInvisible() {
        return this.getFlag(INVISIBLE_FLAG_INDEX);
    }

    public boolean isInvisibleTo(PlayerEntity player) {
        if (player.isSpectator()) {
            return false;
        }
        Team lv = this.getScoreboardTeam();
        if (lv != null && player != null && player.getScoreboardTeam() == lv && ((AbstractTeam)lv).shouldShowFriendlyInvisibles()) {
            return false;
        }
        return this.isInvisible();
    }

    public boolean isOnRail() {
        return false;
    }

    public void updateEventHandler(BiConsumer<EntityGameEventHandler<?>, ServerWorld> callback) {
    }

    @Nullable
    public Team getScoreboardTeam() {
        return this.getEntityWorld().getScoreboard().getScoreHolderTeam(this.getNameForScoreboard());
    }

    public final boolean isTeammate(@Nullable Entity other) {
        if (other == null) {
            return false;
        }
        return this == other || this.isInSameTeam(other) || other.isInSameTeam(this);
    }

    protected boolean isInSameTeam(Entity other) {
        return this.isTeamPlayer(other.getScoreboardTeam());
    }

    public boolean isTeamPlayer(@Nullable AbstractTeam team) {
        if (this.getScoreboardTeam() != null) {
            return this.getScoreboardTeam().isEqual(team);
        }
        return false;
    }

    public void setInvisible(boolean invisible) {
        this.setFlag(INVISIBLE_FLAG_INDEX, invisible);
    }

    protected boolean getFlag(int index) {
        return (this.dataTracker.get(FLAGS) & 1 << index) != 0;
    }

    protected void setFlag(int index, boolean value) {
        byte b = this.dataTracker.get(FLAGS);
        if (value) {
            this.dataTracker.set(FLAGS, (byte)(b | 1 << index));
        } else {
            this.dataTracker.set(FLAGS, (byte)(b & ~(1 << index)));
        }
    }

    public int getMaxAir() {
        return 300;
    }

    public int getAir() {
        return this.dataTracker.get(AIR);
    }

    public void setAir(int air) {
        this.dataTracker.set(AIR, air);
    }

    public void defrost() {
        this.setFrozenTicks(0);
    }

    public int getFrozenTicks() {
        return this.dataTracker.get(FROZEN_TICKS);
    }

    public void setFrozenTicks(int frozenTicks) {
        this.dataTracker.set(FROZEN_TICKS, frozenTicks);
    }

    public float getFreezingScale() {
        int i = this.getMinFreezeDamageTicks();
        return (float)Math.min(this.getFrozenTicks(), i) / (float)i;
    }

    public boolean isFrozen() {
        return this.getFrozenTicks() >= this.getMinFreezeDamageTicks();
    }

    public int getMinFreezeDamageTicks() {
        return 140;
    }

    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        this.setFireTicks(this.fireTicks + 1);
        if (this.fireTicks == 0) {
            this.setOnFireFor(8.0f);
        }
        this.damage(world, this.getDamageSources().lightningBolt(), 5.0f);
    }

    public void onBubbleColumnSurfaceCollision(boolean drag, BlockPos pos) {
        Entity.applyBubbleColumnSurfaceEffects(this, drag, pos);
    }

    protected static void applyBubbleColumnSurfaceEffects(Entity entity, boolean drag, BlockPos pos) {
        Vec3d lv = entity.getVelocity();
        double d = drag ? Math.max(-0.9, lv.y - 0.03) : Math.min(1.8, lv.y + 0.1);
        entity.setVelocity(lv.x, d, lv.z);
        Entity.spawnBubbleColumnParticles(entity.world, pos);
    }

    protected static void spawnBubbleColumnParticles(World world, BlockPos pos) {
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            for (int i = 0; i < 2; ++i) {
                lv.spawnParticles(ParticleTypes.SPLASH, (double)pos.getX() + world.random.nextDouble(), pos.getY() + 1, (double)pos.getZ() + world.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0);
                lv.spawnParticles(ParticleTypes.BUBBLE, (double)pos.getX() + world.random.nextDouble(), pos.getY() + 1, (double)pos.getZ() + world.random.nextDouble(), 1, 0.0, 0.01, 0.0, 0.2);
            }
        }
    }

    public void onBubbleColumnCollision(boolean drag) {
        Entity.applyBubbleColumnEffects(this, drag);
    }

    protected static void applyBubbleColumnEffects(Entity entity, boolean drag) {
        Vec3d lv = entity.getVelocity();
        double d = drag ? Math.max(-0.3, lv.y - 0.03) : Math.min(0.7, lv.y + 0.06);
        entity.setVelocity(lv.x, d, lv.z);
        entity.onLanding();
    }

    public boolean onKilledOther(ServerWorld world, LivingEntity other, DamageSource damageSource) {
        return true;
    }

    public void limitFallDistance() {
        if (this.getVelocity().getY() > -0.5 && this.fallDistance > 1.0) {
            this.fallDistance = 1.0;
        }
    }

    public void onLanding() {
        this.fallDistance = 0.0;
    }

    protected void pushOutOfBlocks(double x, double y, double z) {
        BlockPos lv = BlockPos.ofFloored(x, y, z);
        Vec3d lv2 = new Vec3d(x - (double)lv.getX(), y - (double)lv.getY(), z - (double)lv.getZ());
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        Direction lv4 = Direction.UP;
        double g = Double.MAX_VALUE;
        for (Direction lv5 : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP}) {
            double i;
            lv3.set((Vec3i)lv, lv5);
            if (this.getEntityWorld().getBlockState(lv3).isFullCube(this.getEntityWorld(), lv3)) continue;
            double h = lv2.getComponentAlongAxis(lv5.getAxis());
            double d = i = lv5.getDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - h : h;
            if (!(i < g)) continue;
            g = i;
            lv4 = lv5;
        }
        float j = this.random.nextFloat() * 0.2f + 0.1f;
        float k = lv4.getDirection().offset();
        Vec3d lv6 = this.getVelocity().multiply(0.75);
        if (lv4.getAxis() == Direction.Axis.X) {
            this.setVelocity(k * j, lv6.y, lv6.z);
        } else if (lv4.getAxis() == Direction.Axis.Y) {
            this.setVelocity(lv6.x, k * j, lv6.z);
        } else if (lv4.getAxis() == Direction.Axis.Z) {
            this.setVelocity(lv6.x, lv6.y, k * j);
        }
    }

    public void slowMovement(BlockState state, Vec3d multiplier) {
        this.onLanding();
        this.movementMultiplier = multiplier;
    }

    private static Text removeClickEvents(Text textComponent) {
        MutableText lv = textComponent.copyContentOnly().setStyle(textComponent.getStyle().withClickEvent(null));
        for (Text lv2 : textComponent.getSiblings()) {
            lv.append(Entity.removeClickEvents(lv2));
        }
        return lv;
    }

    @Override
    public Text getName() {
        Text lv = this.getCustomName();
        if (lv != null) {
            return Entity.removeClickEvents(lv);
        }
        return this.getDefaultName();
    }

    protected Text getDefaultName() {
        return this.type.getName();
    }

    public boolean isPartOf(Entity entity) {
        return this == entity;
    }

    public float getHeadYaw() {
        return 0.0f;
    }

    public void setHeadYaw(float headYaw) {
    }

    public void setBodyYaw(float bodyYaw) {
    }

    public boolean isAttackable() {
        return true;
    }

    public boolean handleAttack(Entity attacker) {
        return false;
    }

    public String toString() {
        String string;
        String string2 = string = this.getEntityWorld() == null ? "~NULL~" : this.getEntityWorld().toString();
        if (this.removalReason != null) {
            return String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f, removed=%s]", new Object[]{this.getClass().getSimpleName(), this.getStringifiedName(), this.id, string, this.getX(), this.getY(), this.getZ(), this.removalReason});
        }
        return String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getStringifiedName(), this.id, string, this.getX(), this.getY(), this.getZ());
    }

    protected final boolean isAlwaysInvulnerableTo(DamageSource damageSource) {
        return this.isRemoved() || this.invulnerable && !damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damageSource.isSourceCreativePlayer() || damageSource.isIn(DamageTypeTags.IS_FIRE) && this.isFireImmune() || damageSource.isIn(DamageTypeTags.IS_FALL) && this.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE);
    }

    public boolean isInvulnerable() {
        return this.invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public void copyPositionAndRotation(Entity entity) {
        this.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
    }

    public void copyFrom(Entity original) {
        try (ErrorReporter.Logging lv = new ErrorReporter.Logging(this.getErrorReporterContext(), LOGGER);){
            NbtWriteView lv2 = NbtWriteView.create(lv, original.getRegistryManager());
            original.writeData(lv2);
            this.readData(NbtReadView.create(lv, this.getRegistryManager(), lv2.getNbt()));
        }
        this.portalCooldown = original.portalCooldown;
        this.portalManager = original.portalManager;
    }

    @Nullable
    public Entity teleportTo(TeleportTarget teleportTarget) {
        boolean bl;
        ServerWorld lv;
        block6: {
            block5: {
                World world = this.getEntityWorld();
                if (!(world instanceof ServerWorld)) break block5;
                lv = (ServerWorld)world;
                if (!this.isRemoved()) break block6;
            }
            return null;
        }
        ServerWorld lv2 = teleportTarget.world();
        boolean bl2 = bl = lv2.getRegistryKey() != lv.getRegistryKey();
        if (!teleportTarget.asPassenger()) {
            this.stopRiding();
        }
        if (bl) {
            return this.teleportCrossDimension(lv, lv2, teleportTarget);
        }
        return this.teleportSameDimension(lv, teleportTarget);
    }

    private Entity teleportSameDimension(ServerWorld world, TeleportTarget teleportTarget) {
        for (Entity lv : this.getPassengerList()) {
            lv.teleportTo(this.getPassengerTeleportTarget(teleportTarget, lv));
        }
        Profiler lv2 = Profilers.get();
        lv2.push("teleportSameDimension");
        this.setPosition(EntityPosition.fromTeleportTarget(teleportTarget), teleportTarget.relatives());
        if (!teleportTarget.asPassenger()) {
            this.sendTeleportPacket(teleportTarget);
        }
        teleportTarget.postTeleportTransition().onTransition(this);
        lv2.pop();
        return this;
    }

    @Nullable
    private Entity teleportCrossDimension(ServerWorld from, ServerWorld to, TeleportTarget teleportTarget) {
        Entity lv2;
        List<Entity> list = this.getPassengerList();
        ArrayList<Entity> list2 = new ArrayList<Entity>(list.size());
        this.removeAllPassengers();
        for (Entity lv2 : list) {
            Entity lv22 = lv2.teleportTo(this.getPassengerTeleportTarget(teleportTarget, lv2));
            if (lv22 == null) continue;
            list2.add(lv22);
        }
        Profiler lv3 = Profilers.get();
        lv3.push("teleportCrossDimension");
        lv2 = this.getType().create(to, SpawnReason.DIMENSION_TRAVEL);
        if (lv2 == null) {
            lv3.pop();
            return null;
        }
        lv2.copyFrom(this);
        this.removeFromDimension();
        lv2.setPosition(EntityPosition.fromEntity(this), EntityPosition.fromTeleportTarget(teleportTarget), teleportTarget.relatives());
        to.onDimensionChanged(lv2);
        for (Entity lv4 : list2) {
            lv4.startRiding(lv2, true, false);
        }
        to.resetIdleTimeout();
        teleportTarget.postTeleportTransition().onTransition(lv2);
        this.teleportSpectatingPlayers(teleportTarget, from);
        lv3.pop();
        return lv2;
    }

    protected void teleportSpectatingPlayers(TeleportTarget teleportTarget, ServerWorld from) {
        List<ServerPlayerEntity> list = List.copyOf(from.getPlayers());
        for (ServerPlayerEntity lv : list) {
            if (lv.getCameraEntity() != this) continue;
            lv.teleportTo(teleportTarget);
            lv.setCameraEntity(null);
        }
    }

    private TeleportTarget getPassengerTeleportTarget(TeleportTarget teleportTarget, Entity passenger) {
        float f = teleportTarget.yaw() + (teleportTarget.relatives().contains((Object)PositionFlag.Y_ROT) ? 0.0f : passenger.getYaw() - this.getYaw());
        float g = teleportTarget.pitch() + (teleportTarget.relatives().contains((Object)PositionFlag.X_ROT) ? 0.0f : passenger.getPitch() - this.getPitch());
        Vec3d lv = passenger.getEntityPos().subtract(this.getEntityPos());
        Vec3d lv2 = teleportTarget.position().add(teleportTarget.relatives().contains((Object)PositionFlag.X) ? 0.0 : lv.getX(), teleportTarget.relatives().contains((Object)PositionFlag.Y) ? 0.0 : lv.getY(), teleportTarget.relatives().contains((Object)PositionFlag.Z) ? 0.0 : lv.getZ());
        return teleportTarget.withPosition(lv2).withRotation(f, g).asPassenger();
    }

    private void sendTeleportPacket(TeleportTarget teleportTarget) {
        LivingEntity lv = this.getControllingPassenger();
        for (Entity lv2 : this.getPassengersDeep()) {
            if (!(lv2 instanceof ServerPlayerEntity)) continue;
            ServerPlayerEntity lv3 = (ServerPlayerEntity)lv2;
            if (lv != null && lv3.getId() == lv.getId()) {
                lv3.networkHandler.sendPacket(EntityPositionS2CPacket.create(this.getId(), EntityPosition.fromTeleportTarget(teleportTarget), teleportTarget.relatives(), this.onGround));
                continue;
            }
            lv3.networkHandler.sendPacket(EntityPositionS2CPacket.create(this.getId(), EntityPosition.fromEntity(this), Set.of(), this.onGround));
        }
    }

    public void setPosition(EntityPosition pos, Set<PositionFlag> flags) {
        this.setPosition(EntityPosition.fromEntity(this), pos, flags);
    }

    public void setPosition(EntityPosition currentPos, EntityPosition newPos, Set<PositionFlag> flags) {
        EntityPosition lv = EntityPosition.apply(currentPos, newPos, flags);
        this.setPos(lv.position().x, lv.position().y, lv.position().z);
        this.setYaw(lv.yaw());
        this.setHeadYaw(lv.yaw());
        this.setPitch(lv.pitch());
        this.refreshPosition();
        this.resetPosition();
        this.setVelocity(lv.deltaMovement());
        this.clearQueuedCollisionChecks();
    }

    public void rotate(float yaw, boolean relativeYaw, float pitch, boolean relativePitch) {
        Set<PositionFlag> set = PositionFlag.ofRot(relativeYaw, relativePitch);
        EntityPosition lv = EntityPosition.fromEntity(this);
        EntityPosition lv2 = lv.withRotation(yaw, pitch);
        EntityPosition lv3 = EntityPosition.apply(lv, lv2, set);
        this.setYaw(lv3.yaw());
        this.setHeadYaw(lv3.yaw());
        this.setPitch(lv3.pitch());
        this.updateLastAngles();
    }

    public void addPortalChunkTicketAt(BlockPos pos) {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            lv.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(pos), 3);
        }
    }

    protected void removeFromDimension() {
        Object object;
        this.setRemoved(RemovalReason.CHANGED_DIMENSION);
        Entity entity = this;
        if (entity instanceof Leashable) {
            Leashable lv = (Leashable)((Object)entity);
            lv.detachLeashWithoutDrop();
        }
        if ((object = this) instanceof ServerWaypoint) {
            ServerWaypoint lv2 = (ServerWaypoint)object;
            object = this.world;
            if (object instanceof ServerWorld) {
                ServerWorld lv3 = (ServerWorld)object;
                lv3.getWaypointHandler().onUntrack(lv2);
            }
        }
    }

    public Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        return NetherPortal.entityPosInPortal(portalRect, portalAxis, this.getEntityPos(), this.getDimensions(this.getPose()));
    }

    public boolean canUsePortals(boolean allowVehicles) {
        return (allowVehicles || !this.hasVehicle()) && this.isAlive();
    }

    public boolean canTeleportBetween(World from, World to) {
        if (from.getRegistryKey() == World.END && to.getRegistryKey() == World.OVERWORLD) {
            for (Entity lv : this.getPassengerList()) {
                if (!(lv instanceof ServerPlayerEntity)) continue;
                ServerPlayerEntity lv2 = (ServerPlayerEntity)lv;
                if (lv2.seenCredits) continue;
                return false;
            }
        }
        return true;
    }

    public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
        return max;
    }

    public boolean canExplosionDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float explosionPower) {
        return true;
    }

    public int getSafeFallDistance() {
        return 3;
    }

    public boolean canAvoidTraps() {
        return false;
    }

    public void populateCrashReport(CrashReportSection section) {
        section.add("Entity Type", () -> String.valueOf(EntityType.getId(this.getType())) + " (" + this.getClass().getCanonicalName() + ")");
        section.add("Entity ID", this.id);
        section.add("Entity Name", () -> this.getStringifiedName());
        section.add("Entity's Exact location", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", this.getX(), this.getY(), this.getZ()));
        section.add("Entity's Block location", CrashReportSection.createPositionString((HeightLimitView)this.getEntityWorld(), MathHelper.floor(this.getX()), MathHelper.floor(this.getY()), MathHelper.floor(this.getZ())));
        Vec3d lv = this.getVelocity();
        section.add("Entity's Momentum", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", lv.x, lv.y, lv.z));
        section.add("Entity's Passengers", () -> this.getPassengerList().toString());
        section.add("Entity's Vehicle", () -> String.valueOf(this.getVehicle()));
    }

    public boolean doesRenderOnFire() {
        return this.isOnFire() && !this.isSpectator();
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
        this.uuidString = this.uuid.toString();
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    public String getUuidAsString() {
        return this.uuidString;
    }

    @Override
    public String getNameForScoreboard() {
        return this.uuidString;
    }

    public boolean isPushedByFluids() {
        return true;
    }

    public static double getRenderDistanceMultiplier() {
        return renderDistanceMultiplier;
    }

    public static void setRenderDistanceMultiplier(double value) {
        renderDistanceMultiplier = value;
    }

    @Override
    public Text getDisplayName() {
        return Team.decorateName(this.getScoreboardTeam(), this.getName()).styled(style -> style.withHoverEvent(this.getHoverEvent()).withInsertion(this.getUuidAsString()));
    }

    public void setCustomName(@Nullable Text name) {
        this.dataTracker.set(CUSTOM_NAME, Optional.ofNullable(name));
    }

    @Override
    @Nullable
    public Text getCustomName() {
        return this.dataTracker.get(CUSTOM_NAME).orElse(null);
    }

    @Override
    public boolean hasCustomName() {
        return this.dataTracker.get(CUSTOM_NAME).isPresent();
    }

    public void setCustomNameVisible(boolean visible) {
        this.dataTracker.set(NAME_VISIBLE, visible);
    }

    public boolean isCustomNameVisible() {
        return this.dataTracker.get(NAME_VISIBLE);
    }

    public boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch, boolean resetCamera) {
        Entity lv = this.teleportTo(new TeleportTarget(world, new Vec3d(destX, destY, destZ), Vec3d.ZERO, yaw, pitch, flags, TeleportTarget.NO_OP));
        return lv != null;
    }

    public void requestTeleportAndDismount(double destX, double destY, double destZ) {
        this.requestTeleport(destX, destY, destZ);
    }

    public void requestTeleport(double destX, double destY, double destZ) {
        if (!(this.getEntityWorld() instanceof ServerWorld)) {
            return;
        }
        this.refreshPositionAndAngles(destX, destY, destZ, this.getYaw(), this.getPitch());
        this.teleportPassengers();
    }

    private void teleportPassengers() {
        this.streamSelfAndPassengers().forEach(entity -> {
            for (Entity lv : entity.passengerList) {
                entity.updatePassengerPosition(lv, Entity::refreshPositionAfterTeleport);
            }
        });
    }

    public void requestTeleportOffset(double offsetX, double offsetY, double offsetZ) {
        this.requestTeleport(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ);
    }

    public boolean shouldRenderName() {
        return this.isCustomNameVisible();
    }

    @Override
    public void onDataTrackerUpdate(List<DataTracker.SerializedEntry<?>> entries) {
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (POSE.equals(data)) {
            this.calculateDimensions();
        }
    }

    @Deprecated
    protected void reinitDimensions() {
        EntityDimensions lv2;
        EntityPose lv = this.getPose();
        this.dimensions = lv2 = this.getDimensions(lv);
        this.standingEyeHeight = lv2.eyeHeight();
    }

    public void calculateDimensions() {
        boolean bl;
        EntityDimensions lv3;
        EntityDimensions lv = this.dimensions;
        EntityPose lv2 = this.getPose();
        this.dimensions = lv3 = this.getDimensions(lv2);
        this.standingEyeHeight = lv3.eyeHeight();
        this.refreshPosition();
        boolean bl2 = bl = lv3.width() <= 4.0f && lv3.height() <= 4.0f;
        if (!(this.world.isClient() || this.firstUpdate || this.noClip || !bl || !(lv3.width() > lv.width()) && !(lv3.height() > lv.height()) || this instanceof PlayerEntity)) {
            this.recalculateDimensions(lv);
        }
    }

    public boolean recalculateDimensions(EntityDimensions previous) {
        VoxelShape lv4;
        Optional<Vec3d> optional2;
        double e;
        double d;
        EntityDimensions lv = this.getDimensions(this.getPose());
        Vec3d lv2 = this.getEntityPos().add(0.0, (double)previous.height() / 2.0, 0.0);
        VoxelShape lv3 = VoxelShapes.cuboid(Box.of(lv2, d = (double)Math.max(0.0f, lv.width() - previous.width()) + 1.0E-6, e = (double)Math.max(0.0f, lv.height() - previous.height()) + 1.0E-6, d));
        Optional<Vec3d> optional = this.world.findClosestCollision(this, lv3, lv2, lv.width(), lv.height(), lv.width());
        if (optional.isPresent()) {
            this.setPosition(optional.get().add(0.0, (double)(-lv.height()) / 2.0, 0.0));
            return true;
        }
        if (lv.width() > previous.width() && lv.height() > previous.height() && (optional2 = this.world.findClosestCollision(this, lv4 = VoxelShapes.cuboid(Box.of(lv2, d, 1.0E-6, d)), lv2, lv.width(), previous.height(), lv.width())).isPresent()) {
            this.setPosition(optional2.get().add(0.0, (double)(-previous.height()) / 2.0 + 1.0E-6, 0.0));
            return true;
        }
        return false;
    }

    public Direction getHorizontalFacing() {
        return Direction.fromHorizontalDegrees(this.getYaw());
    }

    public Direction getMovementDirection() {
        return this.getHorizontalFacing();
    }

    protected HoverEvent getHoverEvent() {
        return new HoverEvent.ShowEntity(new HoverEvent.EntityContent(this.getType(), this.getUuid(), this.getName()));
    }

    public boolean canBeSpectated(ServerPlayerEntity spectator) {
        return true;
    }

    @Override
    public final Box getBoundingBox() {
        return this.boundingBox;
    }

    public final void setBoundingBox(Box boundingBox) {
        this.boundingBox = boundingBox;
    }

    public final float getEyeHeight(EntityPose pose) {
        return this.getDimensions(pose).eyeHeight();
    }

    public final float getStandingEyeHeight() {
        return this.standingEyeHeight;
    }

    public StackReference getStackReference(int mappedIndex) {
        return StackReference.EMPTY;
    }

    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        return ActionResult.PASS;
    }

    public boolean isImmuneToExplosion(Explosion explosion) {
        return false;
    }

    public void onStartedTrackingBy(ServerPlayerEntity player) {
    }

    public void onStoppedTrackingBy(ServerPlayerEntity player) {
    }

    public float applyRotation(BlockRotation rotation) {
        float f = MathHelper.wrapDegrees(this.getYaw());
        return switch (rotation) {
            case BlockRotation.CLOCKWISE_180 -> f + 180.0f;
            case BlockRotation.COUNTERCLOCKWISE_90 -> f + 270.0f;
            case BlockRotation.CLOCKWISE_90 -> f + 90.0f;
            default -> f;
        };
    }

    public float applyMirror(BlockMirror mirror) {
        float f = MathHelper.wrapDegrees(this.getYaw());
        return switch (mirror) {
            case BlockMirror.FRONT_BACK -> -f;
            case BlockMirror.LEFT_RIGHT -> 180.0f - f;
            default -> f;
        };
    }

    public ProjectileDeflection getProjectileDeflection(ProjectileEntity projectile) {
        return this.getType().isIn(EntityTypeTags.DEFLECTS_PROJECTILES) ? ProjectileDeflection.SIMPLE : ProjectileDeflection.NONE;
    }

    @Nullable
    public LivingEntity getControllingPassenger() {
        return null;
    }

    public final boolean hasControllingPassenger() {
        return this.getControllingPassenger() != null;
    }

    public final List<Entity> getPassengerList() {
        return this.passengerList;
    }

    @Nullable
    public Entity getFirstPassenger() {
        return this.passengerList.isEmpty() ? null : (Entity)this.passengerList.get(0);
    }

    public boolean hasPassenger(Entity passenger) {
        return this.passengerList.contains(passenger);
    }

    public boolean hasPassenger(Predicate<Entity> predicate) {
        for (Entity lv : this.passengerList) {
            if (!predicate.test(lv)) continue;
            return true;
        }
        return false;
    }

    private Stream<Entity> streamIntoPassengers() {
        return this.passengerList.stream().flatMap(Entity::streamSelfAndPassengers);
    }

    public Stream<Entity> streamSelfAndPassengers() {
        return Stream.concat(Stream.of(this), this.streamIntoPassengers());
    }

    public Stream<Entity> streamPassengersAndSelf() {
        return Stream.concat(this.passengerList.stream().flatMap(Entity::streamPassengersAndSelf), Stream.of(this));
    }

    public Iterable<Entity> getPassengersDeep() {
        return () -> this.streamIntoPassengers().iterator();
    }

    public int getPlayerPassengers() {
        return (int)this.streamIntoPassengers().filter(passenger -> passenger instanceof PlayerEntity).count();
    }

    public boolean hasPlayerRider() {
        return this.getPlayerPassengers() == 1;
    }

    public Entity getRootVehicle() {
        Entity lv = this;
        while (lv.hasVehicle()) {
            lv = lv.getVehicle();
        }
        return lv;
    }

    public boolean isConnectedThroughVehicle(Entity entity) {
        return this.getRootVehicle() == entity.getRootVehicle();
    }

    public boolean hasPassengerDeep(Entity passenger) {
        if (!passenger.hasVehicle()) {
            return false;
        }
        Entity lv = passenger.getVehicle();
        if (lv == this) {
            return true;
        }
        return this.hasPassengerDeep(lv);
    }

    public final boolean isLogicalSideForUpdatingMovement() {
        if (this.world.isClient()) {
            return this.isControlledByMainPlayer();
        }
        return !this.isControlledByPlayer();
    }

    protected boolean isControlledByMainPlayer() {
        LivingEntity lv = this.getControllingPassenger();
        return lv != null && lv.isControlledByMainPlayer();
    }

    public boolean isControlledByPlayer() {
        LivingEntity lv = this.getControllingPassenger();
        return lv != null && lv.isControlledByPlayer();
    }

    public boolean canMoveVoluntarily() {
        return this.isLogicalSideForUpdatingMovement();
    }

    public boolean canActVoluntarily() {
        return this.isLogicalSideForUpdatingMovement();
    }

    protected static Vec3d getPassengerDismountOffset(double vehicleWidth, double passengerWidth, float passengerYaw) {
        double g = (vehicleWidth + passengerWidth + (double)1.0E-5f) / 2.0;
        float h = -MathHelper.sin(passengerYaw * ((float)Math.PI / 180));
        float i = MathHelper.cos(passengerYaw * ((float)Math.PI / 180));
        float j = Math.max(Math.abs(h), Math.abs(i));
        return new Vec3d((double)h * g / (double)j, 0.0, (double)i * g / (double)j);
    }

    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        return new Vec3d(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    @Nullable
    public Entity getVehicle() {
        return this.vehicle;
    }

    @Nullable
    public Entity getControllingVehicle() {
        return this.vehicle != null && this.vehicle.getControllingPassenger() == this ? this.vehicle : null;
    }

    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.NORMAL;
    }

    public SoundCategory getSoundCategory() {
        return SoundCategory.NEUTRAL;
    }

    protected int getBurningDuration() {
        return 0;
    }

    public ServerCommandSource getCommandSource(ServerWorld world) {
        return new ServerCommandSource(CommandOutput.DUMMY, this.getEntityPos(), this.getRotationClient(), world, 0, this.getStringifiedName(), this.getDisplayName(), world.getServer(), this);
    }

    public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        Vec3d lv = anchorPoint.positionAt(this);
        double d = target.x - lv.x;
        double e = target.y - lv.y;
        double f = target.z - lv.z;
        double g = Math.sqrt(d * d + f * f);
        this.setPitch(MathHelper.wrapDegrees((float)(-(MathHelper.atan2(e, g) * 57.2957763671875))));
        this.setYaw(MathHelper.wrapDegrees((float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0f));
        this.setHeadYaw(this.getYaw());
        this.lastPitch = this.getPitch();
        this.lastYaw = this.getYaw();
    }

    public float lerpYaw(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastYaw, this.yaw);
    }

    public boolean updateMovementInFluid(TagKey<Fluid> tag, double speed) {
        if (this.isRegionUnloaded()) {
            return false;
        }
        Box lv = this.getBoundingBox().contract(0.001);
        int i = MathHelper.floor(lv.minX);
        int j = MathHelper.ceil(lv.maxX);
        int k = MathHelper.floor(lv.minY);
        int l = MathHelper.ceil(lv.maxY);
        int m = MathHelper.floor(lv.minZ);
        int n = MathHelper.ceil(lv.maxZ);
        double e = 0.0;
        boolean bl = this.isPushedByFluids();
        boolean bl2 = false;
        Vec3d lv2 = Vec3d.ZERO;
        int o = 0;
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        for (int p = i; p < j; ++p) {
            for (int q = k; q < l; ++q) {
                for (int r = m; r < n; ++r) {
                    double f;
                    lv3.set(p, q, r);
                    FluidState lv4 = this.getEntityWorld().getFluidState(lv3);
                    if (!lv4.isIn(tag) || !((f = (double)((float)q + lv4.getHeight(this.getEntityWorld(), lv3))) >= lv.minY)) continue;
                    bl2 = true;
                    e = Math.max(f - lv.minY, e);
                    if (!bl) continue;
                    Vec3d lv5 = lv4.getVelocity(this.getEntityWorld(), lv3);
                    if (e < 0.4) {
                        lv5 = lv5.multiply(e);
                    }
                    lv2 = lv2.add(lv5);
                    ++o;
                }
            }
        }
        if (lv2.length() > 0.0) {
            if (o > 0) {
                lv2 = lv2.multiply(1.0 / (double)o);
            }
            if (!(this instanceof PlayerEntity)) {
                lv2 = lv2.normalize();
            }
            Vec3d lv6 = this.getVelocity();
            lv2 = lv2.multiply(speed);
            double g = 0.003;
            if (Math.abs(lv6.x) < 0.003 && Math.abs(lv6.z) < 0.003 && lv2.length() < 0.0045000000000000005) {
                lv2 = lv2.normalize().multiply(0.0045000000000000005);
            }
            this.setVelocity(this.getVelocity().add(lv2));
        }
        this.fluidHeight.put(tag, e);
        return bl2;
    }

    public boolean isRegionUnloaded() {
        Box lv = this.getBoundingBox().expand(1.0);
        int i = MathHelper.floor(lv.minX);
        int j = MathHelper.ceil(lv.maxX);
        int k = MathHelper.floor(lv.minZ);
        int l = MathHelper.ceil(lv.maxZ);
        return !this.getEntityWorld().isRegionLoaded(i, k, j, l);
    }

    public double getFluidHeight(TagKey<Fluid> fluid) {
        return this.fluidHeight.getDouble(fluid);
    }

    public double getSwimHeight() {
        return (double)this.getStandingEyeHeight() < 0.4 ? 0.0 : 0.4;
    }

    public final float getWidth() {
        return this.dimensions.width();
    }

    public final float getHeight() {
        return this.dimensions.height();
    }

    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket(this, entityTrackerEntry);
    }

    public EntityDimensions getDimensions(EntityPose pose) {
        return this.type.getDimensions();
    }

    public final EntityAttachments getAttachments() {
        return this.dimensions.attachments();
    }

    @Override
    public Vec3d getEntityPos() {
        return this.pos;
    }

    public Vec3d getSyncedPos() {
        return this.getEntityPos();
    }

    @Override
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public BlockState getBlockStateAtPos() {
        if (this.stateAtPos == null) {
            this.stateAtPos = this.getEntityWorld().getBlockState(this.getBlockPos());
        }
        return this.stateAtPos;
    }

    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    public Vec3d getVelocity() {
        return this.velocity;
    }

    public void setVelocity(Vec3d velocity) {
        this.velocity = velocity;
    }

    public void addVelocityInternal(Vec3d velocity) {
        this.setVelocity(this.getVelocity().add(velocity));
    }

    public void setVelocity(double x, double y, double z) {
        this.setVelocity(new Vec3d(x, y, z));
    }

    public final int getBlockX() {
        return this.blockPos.getX();
    }

    public final double getX() {
        return this.pos.x;
    }

    public double getBodyX(double widthScale) {
        return this.pos.x + (double)this.getWidth() * widthScale;
    }

    public double getParticleX(double widthScale) {
        return this.getBodyX((2.0 * this.random.nextDouble() - 1.0) * widthScale);
    }

    public final int getBlockY() {
        return this.blockPos.getY();
    }

    public final double getY() {
        return this.pos.y;
    }

    public double getBodyY(double heightScale) {
        return this.pos.y + (double)this.getHeight() * heightScale;
    }

    public double getRandomBodyY() {
        return this.getBodyY(this.random.nextDouble());
    }

    public double getEyeY() {
        return this.pos.y + (double)this.standingEyeHeight;
    }

    public final int getBlockZ() {
        return this.blockPos.getZ();
    }

    public final double getZ() {
        return this.pos.z;
    }

    public double getBodyZ(double widthScale) {
        return this.pos.z + (double)this.getWidth() * widthScale;
    }

    public double getParticleZ(double widthScale) {
        return this.getBodyZ((2.0 * this.random.nextDouble() - 1.0) * widthScale);
    }

    public final void setPos(double x, double y, double z) {
        if (this.pos.x != x || this.pos.y != y || this.pos.z != z) {
            World world;
            this.pos = new Vec3d(x, y, z);
            int i = MathHelper.floor(x);
            int j = MathHelper.floor(y);
            int k = MathHelper.floor(z);
            if (i != this.blockPos.getX() || j != this.blockPos.getY() || k != this.blockPos.getZ()) {
                this.blockPos = new BlockPos(i, j, k);
                this.stateAtPos = null;
                if (ChunkSectionPos.getSectionCoord(i) != this.chunkPos.x || ChunkSectionPos.getSectionCoord(k) != this.chunkPos.z) {
                    this.chunkPos = new ChunkPos(this.blockPos);
                }
            }
            this.changeListener.updateEntityPosition();
            if (!this.firstUpdate && (world = this.world) instanceof ServerWorld) {
                ServerWorld lv = (ServerWorld)world;
                if (!this.isRemoved()) {
                    ServerPlayerEntity lv3;
                    ServerWaypoint lv2;
                    Entity entity = this;
                    if (entity instanceof ServerWaypoint && (lv2 = (ServerWaypoint)((Object)entity)).hasWaypoint()) {
                        lv.getWaypointHandler().onUpdate(lv2);
                    }
                    if ((entity = this) instanceof ServerPlayerEntity && (lv3 = (ServerPlayerEntity)entity).canReceiveWaypoints() && lv3.networkHandler != null) {
                        lv.getWaypointHandler().updatePlayerPos(lv3);
                    }
                }
            }
        }
    }

    public void checkDespawn() {
    }

    public Vec3d[] getHeldQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.0, 0.5, 0.5, 0.0);
    }

    public boolean hasQuadLeashAttachmentPoints() {
        return false;
    }

    public void tickHeldLeash(Leashable leashedEntity) {
    }

    public void onHeldLeashUpdate(Leashable heldLeashable) {
    }

    public Vec3d getLeashPos(float tickProgress) {
        return this.getLerpedPos(tickProgress).add(0.0, (double)this.standingEyeHeight * 0.7, 0.0);
    }

    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        int i = packet.getEntityId();
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        this.updateTrackedPosition(d, e, f);
        this.refreshPositionAndAngles(d, e, f, packet.getYaw(), packet.getPitch());
        this.setId(i);
        this.setUuid(packet.getUuid());
        this.setVelocity(packet.getVelocity());
    }

    @Nullable
    public ItemStack getPickBlockStack() {
        return null;
    }

    public void setInPowderSnow(boolean inPowderSnow) {
        this.inPowderSnow = inPowderSnow;
    }

    public boolean canFreeze() {
        return !this.getType().isIn(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES);
    }

    public boolean shouldEscapePowderSnow() {
        return this.getFrozenTicks() > 0;
    }

    public float getYaw() {
        return this.yaw;
    }

    @Override
    public float getBodyYaw() {
        return this.getYaw();
    }

    public void setYaw(float yaw) {
        if (!Float.isFinite(yaw)) {
            Util.logErrorOrPause("Invalid entity rotation: " + yaw + ", discarding.");
            return;
        }
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        if (!Float.isFinite(pitch)) {
            Util.logErrorOrPause("Invalid entity rotation: " + pitch + ", discarding.");
            return;
        }
        this.pitch = Math.clamp(pitch % 360.0f, -90.0f, 90.0f);
    }

    public boolean canSprintAsVehicle() {
        return false;
    }

    public float getStepHeight() {
        return 0.0f;
    }

    public void onExplodedBy(@Nullable Entity entity) {
    }

    @Override
    public final boolean isRemoved() {
        return this.removalReason != null;
    }

    @Nullable
    public RemovalReason getRemovalReason() {
        return this.removalReason;
    }

    @Override
    public final void setRemoved(RemovalReason reason) {
        if (this.removalReason == null) {
            this.removalReason = reason;
        }
        if (this.removalReason.shouldDestroy()) {
            this.stopRiding();
        }
        this.getPassengerList().forEach(Entity::stopRiding);
        this.changeListener.remove(reason);
        this.onRemove(reason);
    }

    protected void unsetRemoved() {
        this.removalReason = null;
    }

    @Override
    public void setChangeListener(EntityChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    @Override
    public boolean shouldSave() {
        if (this.removalReason != null && !this.removalReason.shouldSave()) {
            return false;
        }
        if (this.hasVehicle()) {
            return false;
        }
        return !this.hasPassengers() || !this.hasPlayerRider();
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    public boolean canModifyAt(ServerWorld world, BlockPos pos) {
        return true;
    }

    public boolean isFlyingVehicle() {
        return false;
    }

    @Override
    public World getEntityWorld() {
        return this.world;
    }

    protected void setWorld(World world) {
        this.world = world;
    }

    public DamageSources getDamageSources() {
        return this.getEntityWorld().getDamageSources();
    }

    public DynamicRegistryManager getRegistryManager() {
        return this.getEntityWorld().getRegistryManager();
    }

    protected void lerpPosAndRotation(int step, double x, double y, double z, double yaw, double pitch) {
        double j = 1.0 / (double)step;
        double k = MathHelper.lerp(j, this.getX(), x);
        double l = MathHelper.lerp(j, this.getY(), y);
        double m = MathHelper.lerp(j, this.getZ(), z);
        float n = (float)MathHelper.lerpAngleDegrees(j, (double)this.getYaw(), yaw);
        float o = (float)MathHelper.lerp(j, (double)this.getPitch(), pitch);
        this.setPosition(k, l, m);
        this.setRotation(n, o);
    }

    public Random getRandom() {
        return this.random;
    }

    public Vec3d getMovement() {
        LivingEntity livingEntity = this.getControllingPassenger();
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)livingEntity;
            if (this.isAlive()) {
                return lv.getMovement();
            }
        }
        return this.getVelocity();
    }

    @Nullable
    public ItemStack getWeaponStack() {
        return null;
    }

    public Optional<RegistryKey<LootTable>> getLootTableKey() {
        return this.type.getLootTableKey();
    }

    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.CUSTOM_NAME);
        this.copyComponentFrom(from, DataComponentTypes.CUSTOM_DATA);
    }

    public final void copyComponentsFrom(ItemStack stack) {
        this.copyComponentsFrom(stack.getComponents());
    }

    @Override
    @Nullable
    public <T> T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.CUSTOM_NAME) {
            return Entity.castComponentValue(type, this.getCustomName());
        }
        if (type == DataComponentTypes.CUSTOM_DATA) {
            return Entity.castComponentValue(type, this.customData);
        }
        return null;
    }

    @Nullable
    @Contract(value="_,!null->!null;_,_->_")
    protected static <T> T castComponentValue(ComponentType<T> type, @Nullable Object value) {
        return (T)value;
    }

    public <T> void setComponent(ComponentType<T> type, T value) {
        this.setApplicableComponent(type, value);
    }

    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.CUSTOM_NAME) {
            this.setCustomName(Entity.castComponentValue(DataComponentTypes.CUSTOM_NAME, value));
            return true;
        }
        if (type == DataComponentTypes.CUSTOM_DATA) {
            this.customData = Entity.castComponentValue(DataComponentTypes.CUSTOM_DATA, value);
            return true;
        }
        return false;
    }

    protected <T> boolean copyComponentFrom(ComponentsAccess from, ComponentType<T> type) {
        T object = from.get(type);
        if (object != null) {
            return this.setApplicableComponent(type, object);
        }
        return false;
    }

    public ErrorReporter.Context getErrorReporterContext() {
        return new ErrorReporterContext(this);
    }

    @Override
    public void registerTracking(ServerWorld world, DebugTrackable.Tracker tracker) {
    }

    public static enum RemovalReason {
        KILLED(true, false),
        DISCARDED(true, false),
        UNLOADED_TO_CHUNK(false, true),
        UNLOADED_WITH_PLAYER(false, false),
        CHANGED_DIMENSION(false, false);

        private final boolean destroy;
        private final boolean save;

        private RemovalReason(boolean destroy, boolean save) {
            this.destroy = destroy;
            this.save = save;
        }

        public boolean shouldDestroy() {
            return this.destroy;
        }

        public boolean shouldSave() {
            return this.save;
        }
    }

    record QueuedCollisionCheck(Vec3d from, Vec3d to, Optional<Vec3d> axisDependentOriginalMovement) {
        public QueuedCollisionCheck(Vec3d arg, Vec3d arg2, Vec3d arg3) {
            this(arg, arg2, Optional.of(arg3));
        }

        public QueuedCollisionCheck(Vec3d arg, Vec3d arg2) {
            this(arg, arg2, Optional.empty());
        }
    }

    public static enum MoveEffect {
        NONE(false, false),
        SOUNDS(true, false),
        EVENTS(false, true),
        ALL(true, true);

        final boolean sounds;
        final boolean events;

        private MoveEffect(boolean sounds, boolean events) {
            this.sounds = sounds;
            this.events = events;
        }

        public boolean hasAny() {
            return this.events || this.sounds;
        }

        public boolean emitsGameEvents() {
            return this.events;
        }

        public boolean playsSounds() {
            return this.sounds;
        }
    }

    @FunctionalInterface
    public static interface PositionUpdater {
        public void accept(Entity var1, double var2, double var4, double var6);
    }

    record ErrorReporterContext(Entity entity) implements ErrorReporter.Context
    {
        @Override
        public String getName() {
            return this.entity.toString();
        }
    }
}

