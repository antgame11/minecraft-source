/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class TurtleEntity
extends AnimalEntity {
    private static final TrackedData<Boolean> HAS_EGG = DataTracker.registerData(TurtleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> DIGGING_SAND = DataTracker.registerData(TurtleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final float BABY_SCALE = 0.3f;
    private static final EntityDimensions BABY_BASE_DIMENSIONS = EntityType.TURTLE.getDimensions().withAttachments(EntityAttachments.builder().add(EntityAttachmentType.PASSENGER, 0.0f, EntityType.TURTLE.getHeight(), -0.25f)).scaled(0.3f);
    private static final boolean DEFAULT_HAS_EGG = false;
    int sandDiggingCounter;
    public static final TargetPredicate.EntityPredicate BABY_TURTLE_ON_LAND_FILTER = (entity, world) -> entity.isBaby() && !entity.isTouchingWater();
    BlockPos homePos = BlockPos.ORIGIN;
    @Nullable
    BlockPos travelPos;
    boolean landBound;

    public TurtleEntity(EntityType<? extends TurtleEntity> arg, World arg2) {
        super((EntityType<? extends AnimalEntity>)arg, arg2);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        this.setPathfindingPenalty(PathNodeType.DOOR_IRON_CLOSED, -1.0f);
        this.setPathfindingPenalty(PathNodeType.DOOR_WOOD_CLOSED, -1.0f);
        this.setPathfindingPenalty(PathNodeType.DOOR_OPEN, -1.0f);
        this.moveControl = new TurtleMoveControl(this);
    }

    public void setHomePos(BlockPos pos) {
        this.homePos = pos;
    }

    public boolean hasEgg() {
        return this.dataTracker.get(HAS_EGG);
    }

    void setHasEgg(boolean hasEgg) {
        this.dataTracker.set(HAS_EGG, hasEgg);
    }

    public boolean isDiggingSand() {
        return this.dataTracker.get(DIGGING_SAND);
    }

    void setDiggingSand(boolean diggingSand) {
        this.sandDiggingCounter = diggingSand ? 1 : 0;
        this.dataTracker.set(DIGGING_SAND, diggingSand);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(HAS_EGG, false);
        builder.add(DIGGING_SAND, false);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("home_pos", BlockPos.CODEC, this.homePos);
        view.putBoolean("has_egg", this.hasEgg());
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setHomePos(view.read("home_pos", BlockPos.CODEC).orElse(this.getBlockPos()));
        super.readCustomData(view);
        this.setHasEgg(view.getBoolean("has_egg", false));
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.setHomePos(this.getBlockPos());
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public static boolean canSpawn(EntityType<TurtleEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return pos.getY() < world.getSeaLevel() + 4 && TurtleEggBlock.isSandBelow(world, pos) && TurtleEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new TurtleEscapeDangerGoal(this, 1.2));
        this.goalSelector.add(1, new MateGoal(this, 1.0));
        this.goalSelector.add(1, new LayEggGoal(this, 1.0));
        this.goalSelector.add(2, new TemptGoal(this, 1.1, stack -> stack.isIn(ItemTags.TURTLE_FOOD), false));
        this.goalSelector.add(3, new WanderInWaterGoal(this, 1.0));
        this.goalSelector.add(4, new GoHomeGoal(this, 1.0));
        this.goalSelector.add(7, new TravelGoal(this, 1.0));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(9, new WanderOnLandGoal(this, 1.0, 100));
    }

    public static DefaultAttributeContainer.Builder createTurtleAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 30.0).add(EntityAttributes.MOVEMENT_SPEED, 0.25).add(EntityAttributes.STEP_HEIGHT, 1.0);
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 200;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (!this.isTouchingWater() && this.isOnGround() && !this.isBaby()) {
            return SoundEvents.ENTITY_TURTLE_AMBIENT_LAND;
        }
        return super.getAmbientSound();
    }

    @Override
    protected void playSwimSound(float volume) {
        super.playSwimSound(volume * 1.5f);
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_TURTLE_SWIM;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.isBaby()) {
            return SoundEvents.ENTITY_TURTLE_HURT_BABY;
        }
        return SoundEvents.ENTITY_TURTLE_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        if (this.isBaby()) {
            return SoundEvents.ENTITY_TURTLE_DEATH_BABY;
        }
        return SoundEvents.ENTITY_TURTLE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        SoundEvent lv = this.isBaby() ? SoundEvents.ENTITY_TURTLE_SHAMBLE_BABY : SoundEvents.ENTITY_TURTLE_SHAMBLE;
        this.playSound(lv, 0.15f, 1.0f);
    }

    @Override
    public boolean canEat() {
        return super.canEat() && !this.hasEgg();
    }

    @Override
    protected float calculateNextStepSoundDistance() {
        return this.distanceTraveled + 0.15f;
    }

    @Override
    public float getScaleFactor() {
        return this.isBaby() ? 0.3f : 1.0f;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new TurtleSwimNavigation(this, world);
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return EntityType.TURTLE.create(world, SpawnReason.BREEDING);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.TURTLE_FOOD);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (!this.landBound && world.getFluidState(pos).isIn(FluidTags.WATER)) {
            return 10.0f;
        }
        if (TurtleEggBlock.isSandBelow(world, pos)) {
            return 10.0f;
        }
        return world.getPhototaxisFavor(pos);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.isAlive() && this.isDiggingSand() && this.sandDiggingCounter >= 1 && this.sandDiggingCounter % 5 == 0) {
            BlockPos lv = this.getBlockPos();
            if (TurtleEggBlock.isSandBelow(this.getEntityWorld(), lv)) {
                this.getEntityWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, lv, Block.getRawIdFromState(this.getEntityWorld().getBlockState(lv.down())));
                this.emitGameEvent(GameEvent.ENTITY_ACTION);
            }
        }
    }

    @Override
    protected void onGrowUp() {
        ServerWorld lv;
        World world;
        super.onGrowUp();
        if (!this.isBaby() && (world = this.getEntityWorld()) instanceof ServerWorld && (lv = (ServerWorld)world).getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            this.forEachGiftedItem(lv, LootTables.TURTLE_GROW_GAMEPLAY, this::dropStack);
        }
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isTouchingWater()) {
            this.updateVelocity(0.1f, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9));
            if (!(this.getTarget() != null || this.landBound && this.homePos.isWithinDistance(this.getEntityPos(), 20.0))) {
                this.setVelocity(this.getVelocity().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(movementInput);
        }
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        this.damage(world, this.getDamageSources().lightningBolt(), Float.MAX_VALUE);
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.isBaby() ? BABY_BASE_DIMENSIONS : super.getBaseDimensions(pose);
    }

    static class TurtleMoveControl
    extends MoveControl {
        private final TurtleEntity turtle;

        TurtleMoveControl(TurtleEntity turtle) {
            super(turtle);
            this.turtle = turtle;
        }

        private void updateVelocity() {
            if (this.turtle.isTouchingWater()) {
                this.turtle.setVelocity(this.turtle.getVelocity().add(0.0, 0.005, 0.0));
                if (!this.turtle.homePos.isWithinDistance(this.turtle.getEntityPos(), 16.0)) {
                    this.turtle.setMovementSpeed(Math.max(this.turtle.getMovementSpeed() / 2.0f, 0.08f));
                }
                if (this.turtle.isBaby()) {
                    this.turtle.setMovementSpeed(Math.max(this.turtle.getMovementSpeed() / 3.0f, 0.06f));
                }
            } else if (this.turtle.isOnGround()) {
                this.turtle.setMovementSpeed(Math.max(this.turtle.getMovementSpeed() / 2.0f, 0.06f));
            }
        }

        @Override
        public void tick() {
            double f;
            double e;
            this.updateVelocity();
            if (this.state != MoveControl.State.MOVE_TO || this.turtle.getNavigation().isIdle()) {
                this.turtle.setMovementSpeed(0.0f);
                return;
            }
            double d = this.targetX - this.turtle.getX();
            double g = Math.sqrt(d * d + (e = this.targetY - this.turtle.getY()) * e + (f = this.targetZ - this.turtle.getZ()) * f);
            if (g < (double)1.0E-5f) {
                this.entity.setMovementSpeed(0.0f);
                return;
            }
            e /= g;
            float h = (float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0f;
            this.turtle.setYaw(this.wrapDegrees(this.turtle.getYaw(), h, 90.0f));
            this.turtle.bodyYaw = this.turtle.getYaw();
            float i = (float)(this.speed * this.turtle.getAttributeValue(EntityAttributes.MOVEMENT_SPEED));
            this.turtle.setMovementSpeed(MathHelper.lerp(0.125f, this.turtle.getMovementSpeed(), i));
            this.turtle.setVelocity(this.turtle.getVelocity().add(0.0, (double)this.turtle.getMovementSpeed() * e * 0.1, 0.0));
        }
    }

    static class TurtleEscapeDangerGoal
    extends EscapeDangerGoal {
        TurtleEscapeDangerGoal(TurtleEntity turtle, double speed) {
            super(turtle, speed);
        }

        @Override
        public boolean canStart() {
            if (!this.isInDanger()) {
                return false;
            }
            BlockPos lv = this.locateClosestWater(this.mob.getEntityWorld(), this.mob, 7);
            if (lv != null) {
                this.targetX = lv.getX();
                this.targetY = lv.getY();
                this.targetZ = lv.getZ();
                return true;
            }
            return this.findTarget();
        }
    }

    static class MateGoal
    extends AnimalMateGoal {
        private final TurtleEntity turtle;

        MateGoal(TurtleEntity turtle, double speed) {
            super(turtle, speed);
            this.turtle = turtle;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !this.turtle.hasEgg();
        }

        @Override
        protected void breed() {
            ServerPlayerEntity lv = this.animal.getLovingPlayer();
            if (lv == null && this.mate.getLovingPlayer() != null) {
                lv = this.mate.getLovingPlayer();
            }
            if (lv != null) {
                lv.incrementStat(Stats.ANIMALS_BRED);
                Criteria.BRED_ANIMALS.trigger(lv, this.animal, this.mate, null);
            }
            this.turtle.setHasEgg(true);
            this.animal.setBreedingAge(6000);
            this.mate.setBreedingAge(6000);
            this.animal.resetLoveTicks();
            this.mate.resetLoveTicks();
            Random lv2 = this.animal.getRandom();
            if (MateGoal.castToServerWorld(this.world).getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
                this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.animal.getX(), this.animal.getY(), this.animal.getZ(), lv2.nextInt(7) + 1));
            }
        }
    }

    static class LayEggGoal
    extends MoveToTargetPosGoal {
        private final TurtleEntity turtle;

        LayEggGoal(TurtleEntity turtle, double speed) {
            super(turtle, speed, 16);
            this.turtle = turtle;
        }

        @Override
        public boolean canStart() {
            if (this.turtle.hasEgg() && this.turtle.homePos.isWithinDistance(this.turtle.getEntityPos(), 9.0)) {
                return super.canStart();
            }
            return false;
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && this.turtle.hasEgg() && this.turtle.homePos.isWithinDistance(this.turtle.getEntityPos(), 9.0);
        }

        @Override
        public void tick() {
            super.tick();
            BlockPos lv = this.turtle.getBlockPos();
            if (!this.turtle.isTouchingWater() && this.hasReached()) {
                if (this.turtle.sandDiggingCounter < 1) {
                    this.turtle.setDiggingSand(true);
                } else if (this.turtle.sandDiggingCounter > this.getTickCount(200)) {
                    World lv2 = this.turtle.getEntityWorld();
                    lv2.playSound(null, lv, SoundEvents.ENTITY_TURTLE_LAY_EGG, SoundCategory.BLOCKS, 0.3f, 0.9f + lv2.random.nextFloat() * 0.2f);
                    BlockPos lv3 = this.targetPos.up();
                    BlockState lv4 = (BlockState)Blocks.TURTLE_EGG.getDefaultState().with(TurtleEggBlock.EGGS, this.turtle.random.nextInt(4) + 1);
                    lv2.setBlockState(lv3, lv4, Block.NOTIFY_ALL);
                    lv2.emitGameEvent(GameEvent.BLOCK_PLACE, lv3, GameEvent.Emitter.of(this.turtle, lv4));
                    this.turtle.setHasEgg(false);
                    this.turtle.setDiggingSand(false);
                    this.turtle.setLoveTicks(600);
                }
                if (this.turtle.isDiggingSand()) {
                    ++this.turtle.sandDiggingCounter;
                }
            }
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            if (!world.isAir(pos.up())) {
                return false;
            }
            return TurtleEggBlock.isSand(world, pos);
        }
    }

    static class WanderInWaterGoal
    extends MoveToTargetPosGoal {
        private static final int field_30385 = 1200;
        private final TurtleEntity turtle;

        WanderInWaterGoal(TurtleEntity turtle, double speed) {
            super(turtle, turtle.isBaby() ? 2.0 : speed, 24);
            this.turtle = turtle;
            this.lowestY = -1;
        }

        @Override
        public boolean shouldContinue() {
            return !this.turtle.isTouchingWater() && this.tryingTime <= 1200 && this.isTargetPos(this.turtle.getEntityWorld(), this.targetPos);
        }

        @Override
        public boolean canStart() {
            if (this.turtle.isBaby() && !this.turtle.isTouchingWater()) {
                return super.canStart();
            }
            if (!(this.turtle.landBound || this.turtle.isTouchingWater() || this.turtle.hasEgg())) {
                return super.canStart();
            }
            return false;
        }

        @Override
        public boolean shouldResetPath() {
            return this.tryingTime % 160 == 0;
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            return world.getBlockState(pos).isOf(Blocks.WATER);
        }
    }

    static class GoHomeGoal
    extends Goal {
        private final TurtleEntity turtle;
        private final double speed;
        private boolean noPath;
        private int homeReachingTryTicks;
        private static final int MAX_TRY_TICKS = 600;

        GoHomeGoal(TurtleEntity turtle, double speed) {
            this.turtle = turtle;
            this.speed = speed;
        }

        @Override
        public boolean canStart() {
            if (this.turtle.isBaby()) {
                return false;
            }
            if (this.turtle.hasEgg()) {
                return true;
            }
            if (this.turtle.getRandom().nextInt(GoHomeGoal.toGoalTicks(700)) != 0) {
                return false;
            }
            return !this.turtle.homePos.isWithinDistance(this.turtle.getEntityPos(), 64.0);
        }

        @Override
        public void start() {
            this.turtle.landBound = true;
            this.noPath = false;
            this.homeReachingTryTicks = 0;
        }

        @Override
        public void stop() {
            this.turtle.landBound = false;
        }

        @Override
        public boolean shouldContinue() {
            return !this.turtle.homePos.isWithinDistance(this.turtle.getEntityPos(), 7.0) && !this.noPath && this.homeReachingTryTicks <= this.getTickCount(600);
        }

        @Override
        public void tick() {
            BlockPos lv = this.turtle.homePos;
            boolean bl = lv.isWithinDistance(this.turtle.getEntityPos(), 16.0);
            if (bl) {
                ++this.homeReachingTryTicks;
            }
            if (this.turtle.getNavigation().isIdle()) {
                Vec3d lv2 = Vec3d.ofBottomCenter(lv);
                Vec3d lv3 = NoPenaltyTargeting.findTo(this.turtle, 16, 3, lv2, 0.3141592741012573);
                if (lv3 == null) {
                    lv3 = NoPenaltyTargeting.findTo(this.turtle, 8, 7, lv2, 1.5707963705062866);
                }
                if (lv3 != null && !bl && !this.turtle.getEntityWorld().getBlockState(BlockPos.ofFloored(lv3)).isOf(Blocks.WATER)) {
                    lv3 = NoPenaltyTargeting.findTo(this.turtle, 16, 5, lv2, 1.5707963705062866);
                }
                if (lv3 == null) {
                    this.noPath = true;
                    return;
                }
                this.turtle.getNavigation().startMovingTo(lv3.x, lv3.y, lv3.z, this.speed);
            }
        }
    }

    static class TravelGoal
    extends Goal {
        private final TurtleEntity turtle;
        private final double speed;
        private boolean noPath;

        TravelGoal(TurtleEntity turtle, double speed) {
            this.turtle = turtle;
            this.speed = speed;
        }

        @Override
        public boolean canStart() {
            return !this.turtle.landBound && !this.turtle.hasEgg() && this.turtle.isTouchingWater();
        }

        @Override
        public void start() {
            int i = 512;
            int j = 4;
            Random lv = this.turtle.random;
            int k = lv.nextInt(1025) - 512;
            int l = lv.nextInt(9) - 4;
            int m = lv.nextInt(1025) - 512;
            if ((double)l + this.turtle.getY() > (double)(this.turtle.getEntityWorld().getSeaLevel() - 1)) {
                l = 0;
            }
            this.turtle.travelPos = BlockPos.ofFloored((double)k + this.turtle.getX(), (double)l + this.turtle.getY(), (double)m + this.turtle.getZ());
            this.noPath = false;
        }

        @Override
        public void tick() {
            if (this.turtle.travelPos == null) {
                this.noPath = true;
                return;
            }
            if (this.turtle.getNavigation().isIdle()) {
                Vec3d lv = Vec3d.ofBottomCenter(this.turtle.travelPos);
                Vec3d lv2 = NoPenaltyTargeting.findTo(this.turtle, 16, 3, lv, 0.3141592741012573);
                if (lv2 == null) {
                    lv2 = NoPenaltyTargeting.findTo(this.turtle, 8, 7, lv, 1.5707963705062866);
                }
                if (lv2 != null) {
                    int i = MathHelper.floor(lv2.x);
                    int j = MathHelper.floor(lv2.z);
                    int k = 34;
                    if (!this.turtle.getEntityWorld().isRegionLoaded(i - 34, j - 34, i + 34, j + 34)) {
                        lv2 = null;
                    }
                }
                if (lv2 == null) {
                    this.noPath = true;
                    return;
                }
                this.turtle.getNavigation().startMovingTo(lv2.x, lv2.y, lv2.z, this.speed);
            }
        }

        @Override
        public boolean shouldContinue() {
            return !this.turtle.getNavigation().isIdle() && !this.noPath && !this.turtle.landBound && !this.turtle.isInLove() && !this.turtle.hasEgg();
        }

        @Override
        public void stop() {
            this.turtle.travelPos = null;
            super.stop();
        }
    }

    static class WanderOnLandGoal
    extends WanderAroundGoal {
        private final TurtleEntity turtle;

        WanderOnLandGoal(TurtleEntity turtle, double speed, int chance) {
            super(turtle, speed, chance);
            this.turtle = turtle;
        }

        @Override
        public boolean canStart() {
            if (!(this.mob.isTouchingWater() || this.turtle.landBound || this.turtle.hasEgg())) {
                return super.canStart();
            }
            return false;
        }
    }

    static class TurtleSwimNavigation
    extends AmphibiousSwimNavigation {
        TurtleSwimNavigation(TurtleEntity owner, World world) {
            super(owner, world);
        }

        @Override
        public boolean isValidPosition(BlockPos pos) {
            MobEntity mobEntity = this.entity;
            if (mobEntity instanceof TurtleEntity) {
                TurtleEntity lv = (TurtleEntity)mobEntity;
                if (lv.travelPos != null) {
                    return this.world.getBlockState(pos).isOf(Blocks.WATER);
                }
            }
            return !this.world.getBlockState(pos.down()).isAir();
        }
    }
}

