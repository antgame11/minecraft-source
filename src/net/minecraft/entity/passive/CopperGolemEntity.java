/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Dynamic;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CopperGolemStatueBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.ContainerUser;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CopperGolemBrain;
import net.minecraft.entity.passive.CopperGolemOxidationLevels;
import net.minecraft.entity.passive.CopperGolemState;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class CopperGolemEntity
extends GolemEntity
implements ContainerUser,
Shearable {
    private static final long field_61257 = -2L;
    private static final long field_61258 = -1L;
    private static final int field_61259 = 504000;
    private static final int field_61273 = 552000;
    private static final int field_61274 = 200;
    private static final int field_61275 = 240;
    private static final float field_61260 = 10.0f;
    private static final float field_63113 = 0.0058f;
    private static final int field_63114 = 60;
    private static final int field_63115 = 100;
    private static final TrackedData<Oxidizable.OxidationLevel> OXIDATION_LEVEL = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.OXIDATION_LEVEL);
    private static final TrackedData<CopperGolemState> COPPER_GOLEM_STATE = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.COPPER_GOLEM_STATE);
    @Nullable
    private BlockPos targetContainer;
    @Nullable
    private UUID lastStruckLightning;
    private long nextOxidationAge = -1L;
    private int spinHeadTimer = 0;
    private final AnimationState spinHeadAnimationState = new AnimationState();
    private final AnimationState gettingItemAnimationState = new AnimationState();
    private final AnimationState gettingNoItemAnimationState = new AnimationState();
    private final AnimationState droppingItemAnimationState = new AnimationState();
    private final AnimationState droppingNoItemAnimationState = new AnimationState();
    public static final EquipmentSlot POPPY_SLOT = EquipmentSlot.SADDLE;

    public CopperGolemEntity(EntityType<? extends GolemEntity> arg, World arg2) {
        super(arg, arg2);
        this.getNavigation().setMaxFollowRange(48.0f);
        this.getNavigation().setCanOpenDoors(true);
        this.setPersistent();
        this.setState(CopperGolemState.IDLE);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 16.0f);
        this.setPathfindingPenalty(PathNodeType.DANGER_OTHER, 16.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0f);
        this.getBrain().remember(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, this.getRandom().nextBetweenExclusive(60, 100));
    }

    public static DefaultAttributeContainer.Builder createCopperGolemAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.MOVEMENT_SPEED, 0.2f).add(EntityAttributes.STEP_HEIGHT, 1.0).add(EntityAttributes.MAX_HEALTH, 12.0);
    }

    public CopperGolemState getState() {
        return this.dataTracker.get(COPPER_GOLEM_STATE);
    }

    public void setState(CopperGolemState state) {
        this.dataTracker.set(COPPER_GOLEM_STATE, state);
    }

    public Oxidizable.OxidationLevel getOxidationLevel() {
        return this.dataTracker.get(OXIDATION_LEVEL);
    }

    public void setOxidationLevel(Oxidizable.OxidationLevel oxidationLevel) {
        this.dataTracker.set(OXIDATION_LEVEL, oxidationLevel);
    }

    public void setTargetContainerPos(BlockPos pos) {
        this.targetContainer = pos;
    }

    public void resetTargetContainerPos() {
        this.targetContainer = null;
    }

    public AnimationState getSpinHeadAnimationState() {
        return this.spinHeadAnimationState;
    }

    public AnimationState getGettingItemAnimationState() {
        return this.gettingItemAnimationState;
    }

    public AnimationState getGettingNoItemAnimationState() {
        return this.gettingNoItemAnimationState;
    }

    public AnimationState getDroppingItemAnimationState() {
        return this.droppingItemAnimationState;
    }

    public AnimationState getDroppingNoItemAnimationState() {
        return this.droppingNoItemAnimationState;
    }

    protected Brain.Profile<CopperGolemEntity> createBrainProfile() {
        return CopperGolemBrain.createBrainProfile();
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return CopperGolemBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    public Brain<CopperGolemEntity> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(OXIDATION_LEVEL, Oxidizable.OxidationLevel.UNAFFECTED);
        builder.add(COPPER_GOLEM_STATE, CopperGolemState.IDLE);
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putLong("next_weather_age", this.nextOxidationAge);
        view.put("weather_state", Oxidizable.OxidationLevel.CODEC, this.getOxidationLevel());
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.nextOxidationAge = view.getLong("next_weather_age", -1L);
        this.setOxidationLevel(view.read("weather_state", Oxidizable.OxidationLevel.CODEC).orElse(Oxidizable.OxidationLevel.UNAFFECTED));
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Profiler lv = Profilers.get();
        lv.push("copperGolemBrain");
        this.getBrain().tick(world, this);
        lv.pop();
        lv.push("copperGolemActivityUpdate");
        CopperGolemBrain.updateActivity(this);
        lv.pop();
        super.mobTick(world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getEntityWorld().isClient()) {
            if (!this.isAiDisabled()) {
                this.clientTick();
            }
        } else {
            this.serverTick((ServerWorld)this.getEntityWorld(), this.getEntityWorld().getRandom(), this.getEntityWorld().getTime());
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        Oxidizable.OxidationLevel lv5;
        ItemStack lv2;
        ItemStack lv = player.getStackInHand(hand);
        if (lv.isEmpty() && !(lv2 = this.getMainHandStack()).isEmpty()) {
            TargetUtil.give(this, lv2, player.getEntityPos());
            this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            return ActionResult.SUCCESS;
        }
        World lv3 = this.getEntityWorld();
        if (lv.isOf(Items.SHEARS) && this.isShearable()) {
            if (lv3 instanceof ServerWorld) {
                ServerWorld lv4 = (ServerWorld)lv3;
                this.sheared(lv4, SoundCategory.PLAYERS, lv);
                this.emitGameEvent(GameEvent.SHEAR, player);
                lv.damage(1, (LivingEntity)player, hand);
            }
            return ActionResult.SUCCESS;
        }
        if (lv3.isClient()) {
            return ActionResult.PASS;
        }
        if (lv.isOf(Items.HONEYCOMB) && this.nextOxidationAge != -2L) {
            lv3.syncWorldEvent(this, WorldEvents.BLOCK_WAXED, this.getBlockPos(), 0);
            this.nextOxidationAge = -2L;
            this.eat(player, hand, lv);
            return ActionResult.SUCCESS_SERVER;
        }
        if (lv.isIn(ItemTags.AXES) && this.nextOxidationAge == -2L) {
            lv3.playSoundFromEntity(null, this, SoundEvents.ITEM_AXE_SCRAPE, this.getSoundCategory(), 1.0f, 1.0f);
            lv3.syncWorldEvent(this, WorldEvents.WAX_REMOVED, this.getBlockPos(), 0);
            this.nextOxidationAge = -1L;
            lv.damage(1, (LivingEntity)player, hand.getEquipmentSlot());
            return ActionResult.SUCCESS_SERVER;
        }
        if (lv.isIn(ItemTags.AXES) && (lv5 = this.getOxidationLevel()) != Oxidizable.OxidationLevel.UNAFFECTED) {
            lv3.playSoundFromEntity(null, this, SoundEvents.ITEM_AXE_SCRAPE, this.getSoundCategory(), 1.0f, 1.0f);
            lv3.syncWorldEvent(this, WorldEvents.BLOCK_SCRAPED, this.getBlockPos(), 0);
            this.nextOxidationAge = -1L;
            this.dataTracker.set(OXIDATION_LEVEL, lv5.getDecreased(), true);
            lv.damage(1, (LivingEntity)player, hand.getEquipmentSlot());
            return ActionResult.SUCCESS_SERVER;
        }
        return super.interactMob(player, hand);
    }

    private void serverTick(ServerWorld world, Random random, long timeOfDay) {
        if (this.nextOxidationAge == -2L) {
            return;
        }
        if (this.nextOxidationAge == -1L) {
            this.nextOxidationAge = timeOfDay + (long)random.nextBetween(504000, 552000);
            return;
        }
        Oxidizable.OxidationLevel lv = this.dataTracker.get(OXIDATION_LEVEL);
        boolean bl = lv.equals(Oxidizable.OxidationLevel.OXIDIZED);
        if (timeOfDay >= this.nextOxidationAge && !bl) {
            Oxidizable.OxidationLevel lv2 = lv.getIncreased();
            boolean bl2 = lv2.equals(Oxidizable.OxidationLevel.OXIDIZED);
            this.setOxidationLevel(lv2);
            long l = this.nextOxidationAge = bl2 ? 0L : this.nextOxidationAge + (long)random.nextBetween(504000, 552000);
        }
        if (bl && this.canTurnIntoStatue(world)) {
            this.turnIntoStatue(world);
        }
    }

    private boolean canTurnIntoStatue(World world) {
        return world.getBlockState(this.getBlockPos()).isOf(Blocks.AIR) && world.random.nextFloat() <= 0.0058f;
    }

    private void turnIntoStatue(ServerWorld world) {
        BlockPos lv = this.getBlockPos();
        world.setBlockState(lv, (BlockState)((BlockState)Blocks.OXIDIZED_COPPER_GOLEM_STATUE.getDefaultState().with(CopperGolemStatueBlock.POSE, CopperGolemStatueBlock.Pose.values()[this.random.nextBetweenExclusive(0, CopperGolemStatueBlock.Pose.values().length)])).with(CopperGolemStatueBlock.FACING, Direction.fromHorizontalDegrees(this.getYaw())), Block.NOTIFY_ALL);
        BlockEntity blockEntity = world.getBlockEntity(lv);
        if (blockEntity instanceof CopperGolemStatueBlockEntity) {
            CopperGolemStatueBlockEntity lv2 = (CopperGolemStatueBlockEntity)blockEntity;
            lv2.copyDataFrom(this);
            this.dropAllForeignEquipment(world);
            this.discard();
            this.playSoundIfNotSilent(SoundEvents.ENTITY_COPPER_GOLEM_BECOME_STATUE);
            if (this.isLeashed()) {
                if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                    this.detachLeash();
                } else {
                    this.detachLeashWithoutDrop();
                }
            }
        }
    }

    private void clientTick() {
        switch (this.getState()) {
            case IDLE: {
                this.gettingNoItemAnimationState.stop();
                this.gettingItemAnimationState.stop();
                this.droppingItemAnimationState.stop();
                this.droppingNoItemAnimationState.stop();
                if (this.spinHeadTimer == this.age) {
                    this.spinHeadAnimationState.start(this.age);
                } else if (this.spinHeadTimer == 0) {
                    this.spinHeadTimer = this.age + this.random.nextBetweenExclusive(200, 240);
                }
                if ((float)this.age != (float)this.spinHeadTimer + 10.0f) break;
                this.playSpinHeadSound();
                this.spinHeadTimer = 0;
                break;
            }
            case GETTING_ITEM: {
                this.spinHeadAnimationState.stop();
                this.spinHeadTimer = 0;
                this.gettingNoItemAnimationState.stop();
                this.droppingItemAnimationState.stop();
                this.droppingNoItemAnimationState.stop();
                this.gettingItemAnimationState.startIfNotRunning(this.age);
                break;
            }
            case GETTING_NO_ITEM: {
                this.spinHeadAnimationState.stop();
                this.spinHeadTimer = 0;
                this.gettingItemAnimationState.stop();
                this.droppingNoItemAnimationState.stop();
                this.droppingItemAnimationState.stop();
                this.gettingNoItemAnimationState.startIfNotRunning(this.age);
                break;
            }
            case DROPPING_ITEM: {
                this.spinHeadAnimationState.stop();
                this.spinHeadTimer = 0;
                this.gettingItemAnimationState.stop();
                this.gettingNoItemAnimationState.stop();
                this.droppingNoItemAnimationState.stop();
                this.droppingItemAnimationState.startIfNotRunning(this.age);
                break;
            }
            case DROPPING_NO_ITEM: {
                this.spinHeadAnimationState.stop();
                this.spinHeadTimer = 0;
                this.gettingItemAnimationState.stop();
                this.gettingNoItemAnimationState.stop();
                this.droppingItemAnimationState.stop();
                this.droppingNoItemAnimationState.startIfNotRunning(this.age);
            }
        }
    }

    public void onSpawn(Oxidizable.OxidationLevel oxidationLevel) {
        this.setOxidationLevel(oxidationLevel);
        this.playSpawnSound();
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.playSpawnSound();
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public void playSpawnSound() {
        this.playSoundIfNotSilent(SoundEvents.ENTITY_COPPER_GOLEM_SPAWN);
    }

    private void playSpinHeadSound() {
        if (!this.isSilent()) {
            this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), this.getSpinHeadSound(), this.getSoundCategory(), 1.0f, 1.0f, false);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return CopperGolemOxidationLevels.get(this.getOxidationLevel()).hurtSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CopperGolemOxidationLevels.get(this.getOxidationLevel()).deathSound();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(CopperGolemOxidationLevels.get(this.getOxidationLevel()).stepSound(), 1.0f, 1.0f);
    }

    private SoundEvent getSpinHeadSound() {
        return CopperGolemOxidationLevels.get(this.getOxidationLevel()).spinHeadSound();
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.75f * this.getStandingEyeHeight(), 0.0);
    }

    @Override
    public boolean isViewingContainerAt(ViewerCountManager viewerCountManager, BlockPos pos) {
        if (this.targetContainer == null) {
            return false;
        }
        BlockState lv = this.getEntityWorld().getBlockState(this.targetContainer);
        return this.targetContainer.equals(pos) || lv.getBlock() instanceof ChestBlock && lv.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE && ChestBlock.getPosInFrontOf(this.targetContainer, lv).equals(pos);
    }

    @Override
    public double getContainerInteractionRange() {
        return 3.0;
    }

    @Override
    public void sheared(ServerWorld world, SoundCategory shearedSoundCategory, ItemStack shears) {
        world.playSoundFromEntity(null, this, SoundEvents.ENTITY_COPPER_GOLEM_SHEAR, shearedSoundCategory, 1.0f, 1.0f);
        ItemStack lv = this.getEquippedStack(POPPY_SLOT);
        this.equipStack(POPPY_SLOT, ItemStack.EMPTY);
        this.dropStack(world, lv, 1.5f);
    }

    @Override
    public boolean isShearable() {
        return this.isAlive() && this.getEquippedStack(POPPY_SLOT).isIn(ItemTags.SHEARABLE_FROM_COPPER_GOLEM);
    }

    @Override
    protected void dropInventory(ServerWorld world) {
        super.dropInventory(world);
        this.dropAllForeignEquipment(world);
    }

    @Override
    protected void applyDamage(ServerWorld world, DamageSource source, float amount) {
        super.applyDamage(world, source, amount);
        this.setState(CopperGolemState.IDLE);
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        super.onStruckByLightning(world, lightning);
        UUID uUID = lightning.getUuid();
        if (!uUID.equals(this.lastStruckLightning)) {
            this.lastStruckLightning = uUID;
            Oxidizable.OxidationLevel lv = this.getOxidationLevel();
            if (lv != Oxidizable.OxidationLevel.UNAFFECTED) {
                this.nextOxidationAge = -1L;
                this.dataTracker.set(OXIDATION_LEVEL, lv.getDecreased(), true);
            }
        }
    }
}

