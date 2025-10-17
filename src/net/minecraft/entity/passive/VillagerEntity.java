/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import net.minecraft.SharedConstants;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.InteractionObserver;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LargeEntitySpawnHelper;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.GolemLastSeenSensor;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerGossipType;
import net.minecraft.village.VillagerGossips;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class VillagerEntity
extends MerchantEntity
implements InteractionObserver,
VillagerDataContainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TrackedData<VillagerData> VILLAGER_DATA = DataTracker.registerData(VillagerEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);
    public static final int field_30602 = 12;
    public static final Map<Item, Integer> ITEM_FOOD_VALUES = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1);
    private static final int field_30604 = 2;
    private static final int field_30605 = 10;
    private static final int field_30606 = 1200;
    private static final int field_30607 = 24000;
    private static final int field_30609 = 10;
    private static final int field_30610 = 5;
    private static final long field_30611 = 24000L;
    @VisibleForTesting
    public static final float field_30603 = 0.5f;
    private static final int field_57709 = 0;
    private static final byte field_57710 = 0;
    private static final int field_57711 = 0;
    private static final int field_57712 = 0;
    private static final int field_57713 = 0;
    private static final boolean DEFAULT_NATURAL = false;
    private int levelUpTimer;
    private boolean levelingUp;
    @Nullable
    private PlayerEntity lastCustomer;
    private boolean field_30612;
    private int foodLevel = 0;
    private final VillagerGossips gossip = new VillagerGossips();
    private long gossipStartTime;
    private long lastGossipDecayTime = 0L;
    private int experience = 0;
    private long lastRestockTime = 0L;
    private int restocksToday = 0;
    private long lastRestockCheckTime;
    private boolean natural = false;
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.HOME, MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT, MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, new MemoryModuleType[]{MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.BREED_TARGET, MemoryModuleType.PATH, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_BED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleType.HIDING_PLACE, MemoryModuleType.HEARD_BELL_TIME, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.LAST_SLEPT, MemoryModuleType.LAST_WOKEN, MemoryModuleType.LAST_WORKED_AT_POI, MemoryModuleType.GOLEM_DETECTED_RECENTLY});
    private static final ImmutableList<SensorType<? extends Sensor<? super VillagerEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_BED, SensorType.HURT_BY, SensorType.VILLAGER_HOSTILES, SensorType.VILLAGER_BABIES, SensorType.SECONDARY_POIS, SensorType.GOLEM_DETECTED);
    public static final Map<MemoryModuleType<GlobalPos>, BiPredicate<VillagerEntity, RegistryEntry<PointOfInterestType>>> POINTS_OF_INTEREST = ImmutableMap.of(MemoryModuleType.HOME, (villager, arg2) -> arg2.matchesKey(PointOfInterestTypes.HOME), MemoryModuleType.JOB_SITE, (villager, arg2) -> villager.getVillagerData().profession().value().heldWorkstation().test((RegistryEntry<PointOfInterestType>)arg2), MemoryModuleType.POTENTIAL_JOB_SITE, (villager, arg2) -> VillagerProfession.IS_ACQUIRABLE_JOB_SITE.test((RegistryEntry<PointOfInterestType>)arg2), MemoryModuleType.MEETING_POINT, (villager, arg2) -> arg2.matchesKey(PointOfInterestTypes.MEETING));

    public VillagerEntity(EntityType<? extends VillagerEntity> arg, World arg2) {
        this(arg, arg2, VillagerType.PLAINS);
    }

    public VillagerEntity(EntityType<? extends VillagerEntity> entityType, World world, RegistryKey<VillagerType> type) {
        this(entityType, world, world.getRegistryManager().getEntryOrThrow(type));
    }

    public VillagerEntity(EntityType<? extends VillagerEntity> entityType, World world, RegistryEntry<VillagerType> type) {
        super((EntityType<? extends MerchantEntity>)entityType, world);
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanSwim(true);
        this.getNavigation().setMaxFollowRange(48.0f);
        this.setCanPickUpLoot(true);
        this.setVillagerData(this.getVillagerData().withType(type).withProfession(world.getRegistryManager(), VillagerProfession.NONE));
    }

    public Brain<VillagerEntity> getBrain() {
        return super.getBrain();
    }

    protected Brain.Profile<VillagerEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        Brain<VillagerEntity> lv = this.createBrainProfile().deserialize(dynamic);
        this.initBrain(lv);
        return lv;
    }

    public void reinitializeBrain(ServerWorld world) {
        Brain<VillagerEntity> lv = this.getBrain();
        lv.stopAllTasks(world, this);
        this.brain = lv.copy();
        this.initBrain(this.getBrain());
    }

    private void initBrain(Brain<VillagerEntity> brain) {
        RegistryEntry<VillagerProfession> lv = this.getVillagerData().profession();
        if (this.isBaby()) {
            brain.setSchedule(Schedule.VILLAGER_BABY);
            brain.setTaskList(Activity.PLAY, VillagerTaskListProvider.createPlayTasks(0.5f));
        } else {
            brain.setSchedule(Schedule.VILLAGER_DEFAULT);
            brain.setTaskList(Activity.WORK, VillagerTaskListProvider.createWorkTasks(lv, 0.5f), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleState.VALUE_PRESENT)));
        }
        brain.setTaskList(Activity.CORE, VillagerTaskListProvider.createCoreTasks(lv, 0.5f));
        brain.setTaskList(Activity.MEET, VillagerTaskListProvider.createMeetTasks(lv, 0.5f), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleState.VALUE_PRESENT)));
        brain.setTaskList(Activity.REST, VillagerTaskListProvider.createRestTasks(lv, 0.5f));
        brain.setTaskList(Activity.IDLE, VillagerTaskListProvider.createIdleTasks(lv, 0.5f));
        brain.setTaskList(Activity.PANIC, VillagerTaskListProvider.createPanicTasks(lv, 0.5f));
        brain.setTaskList(Activity.PRE_RAID, VillagerTaskListProvider.createPreRaidTasks(lv, 0.5f));
        brain.setTaskList(Activity.RAID, VillagerTaskListProvider.createRaidTasks(lv, 0.5f));
        brain.setTaskList(Activity.HIDE, VillagerTaskListProvider.createHideTasks(lv, 0.5f));
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.doExclusively(Activity.IDLE);
        brain.refreshActivities(this.getEntityWorld().getTimeOfDay(), this.getEntityWorld().getTime());
    }

    @Override
    protected void onGrowUp() {
        super.onGrowUp();
        if (this.getEntityWorld() instanceof ServerWorld) {
            this.reinitializeBrain((ServerWorld)this.getEntityWorld());
        }
    }

    public static DefaultAttributeContainer.Builder createVillagerAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.MOVEMENT_SPEED, 0.5);
    }

    public boolean isNatural() {
        return this.natural;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Raid lv2;
        Profiler lv = Profilers.get();
        lv.push("villagerBrain");
        this.getBrain().tick(world, this);
        lv.pop();
        if (this.natural) {
            this.natural = false;
        }
        if (!this.hasCustomer() && this.levelUpTimer > 0) {
            --this.levelUpTimer;
            if (this.levelUpTimer <= 0) {
                if (this.levelingUp) {
                    this.levelUp();
                    this.levelingUp = false;
                }
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0));
            }
        }
        if (this.lastCustomer != null) {
            world.handleInteraction(EntityInteraction.TRADE, this.lastCustomer, this);
            world.sendEntityStatus(this, EntityStatuses.ADD_VILLAGER_HAPPY_PARTICLES);
            this.lastCustomer = null;
        }
        if (!this.isAiDisabled() && this.random.nextInt(100) == 0 && (lv2 = world.getRaidAt(this.getBlockPos())) != null && lv2.isActive() && !lv2.isFinished()) {
            world.sendEntityStatus(this, EntityStatuses.ADD_SPLASH_PARTICLES);
        }
        if (this.getVillagerData().profession().matchesKey(VillagerProfession.NONE) && this.hasCustomer()) {
            this.resetCustomer();
        }
        super.mobTick(world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getHeadRollingTimeLeft() > 0) {
            this.setHeadRollingTimeLeft(this.getHeadRollingTimeLeft() - 1);
        }
        this.decayGossip();
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (!lv.isOf(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.hasCustomer() && !this.isSleeping()) {
            if (this.isBaby()) {
                this.sayNo();
                return ActionResult.SUCCESS;
            }
            if (!this.getEntityWorld().isClient()) {
                boolean bl = this.getOffers().isEmpty();
                if (hand == Hand.MAIN_HAND) {
                    if (bl) {
                        this.sayNo();
                    }
                    player.incrementStat(Stats.TALKED_TO_VILLAGER);
                }
                if (bl) {
                    return ActionResult.CONSUME;
                }
                this.beginTradeWith(player);
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    private void sayNo() {
        this.setHeadRollingTimeLeft(40);
        if (!this.getEntityWorld().isClient()) {
            this.playSound(SoundEvents.ENTITY_VILLAGER_NO);
        }
    }

    private void beginTradeWith(PlayerEntity customer) {
        this.prepareOffersFor(customer);
        this.setCustomer(customer);
        this.sendOffers(customer, this.getDisplayName(), this.getVillagerData().level());
    }

    @Override
    public void setCustomer(@Nullable PlayerEntity customer) {
        boolean bl = this.getCustomer() != null && customer == null;
        super.setCustomer(customer);
        if (bl) {
            this.resetCustomer();
        }
    }

    @Override
    protected void resetCustomer() {
        super.resetCustomer();
        this.clearSpecialPrices();
    }

    private void clearSpecialPrices() {
        if (this.getEntityWorld().isClient()) {
            return;
        }
        for (TradeOffer lv : this.getOffers()) {
            lv.clearSpecialPrice();
        }
    }

    @Override
    public boolean canRefreshTrades() {
        return true;
    }

    public void restock() {
        this.updateDemandBonus();
        for (TradeOffer lv : this.getOffers()) {
            lv.resetUses();
        }
        this.sendOffersToCustomer();
        this.lastRestockTime = this.getEntityWorld().getTime();
        ++this.restocksToday;
    }

    private void sendOffersToCustomer() {
        TradeOfferList lv = this.getOffers();
        PlayerEntity lv2 = this.getCustomer();
        if (lv2 != null && !lv.isEmpty()) {
            lv2.sendTradeOffers(lv2.currentScreenHandler.syncId, lv, this.getVillagerData().level(), this.getExperience(), this.isLeveledMerchant(), this.canRefreshTrades());
        }
    }

    private boolean needsRestock() {
        for (TradeOffer lv : this.getOffers()) {
            if (!lv.hasBeenUsed()) continue;
            return true;
        }
        return false;
    }

    private boolean canRestock() {
        return this.restocksToday == 0 || this.restocksToday < 2 && this.getEntityWorld().getTime() > this.lastRestockTime + 2400L;
    }

    public boolean shouldRestock() {
        long l = this.lastRestockTime + 12000L;
        long m = this.getEntityWorld().getTime();
        boolean bl = m > l;
        long n = this.getEntityWorld().getTimeOfDay();
        if (this.lastRestockCheckTime > 0L) {
            long p = n / 24000L;
            long o = this.lastRestockCheckTime / 24000L;
            bl |= p > o;
        }
        this.lastRestockCheckTime = n;
        if (bl) {
            this.lastRestockTime = m;
            this.clearDailyRestockCount();
        }
        return this.canRestock() && this.needsRestock();
    }

    private void restockAndUpdateDemandBonus() {
        int i = 2 - this.restocksToday;
        if (i > 0) {
            for (TradeOffer lv : this.getOffers()) {
                lv.resetUses();
            }
        }
        for (int j = 0; j < i; ++j) {
            this.updateDemandBonus();
        }
        this.sendOffersToCustomer();
    }

    private void updateDemandBonus() {
        for (TradeOffer lv : this.getOffers()) {
            lv.updateDemandBonus();
        }
    }

    private void prepareOffersFor(PlayerEntity player) {
        int i = this.getReputation(player);
        if (i != 0) {
            for (TradeOffer lv : this.getOffers()) {
                lv.increaseSpecialPrice(-MathHelper.floor((float)i * lv.getPriceMultiplier()));
            }
        }
        if (player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            StatusEffectInstance lv2 = player.getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
            int j = lv2.getAmplifier();
            for (TradeOffer lv3 : this.getOffers()) {
                double d = 0.3 + 0.0625 * (double)j;
                int k = (int)Math.floor(d * (double)lv3.getOriginalFirstBuyItem().getCount());
                lv3.increaseSpecialPrice(-Math.max(k, 1));
            }
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VILLAGER_DATA, VillagerEntity.createVillagerData());
    }

    public static VillagerData createVillagerData() {
        return new VillagerData(Registries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS), Registries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE), 1);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("VillagerData", VillagerData.CODEC, this.getVillagerData());
        view.putByte("FoodLevel", (byte)this.foodLevel);
        view.put("Gossips", VillagerGossips.CODEC, this.gossip);
        view.putInt("Xp", this.experience);
        view.putLong("LastRestock", this.lastRestockTime);
        view.putLong("LastGossipDecay", this.lastGossipDecayTime);
        view.putInt("RestocksToday", this.restocksToday);
        if (this.natural) {
            view.putBoolean("AssignProfessionWhenSpawned", true);
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.dataTracker.set(VILLAGER_DATA, view.read("VillagerData", VillagerData.CODEC).orElseGet(VillagerEntity::createVillagerData));
        this.foodLevel = view.getByte("FoodLevel", (byte)0);
        this.gossip.clear();
        view.read("Gossips", VillagerGossips.CODEC).ifPresent(this.gossip::add);
        this.experience = view.getInt("Xp", 0);
        this.lastRestockTime = view.getLong("LastRestock", 0L);
        this.lastGossipDecayTime = view.getLong("LastGossipDecay", 0L);
        if (this.getEntityWorld() instanceof ServerWorld) {
            this.reinitializeBrain((ServerWorld)this.getEntityWorld());
        }
        this.restocksToday = view.getInt("RestocksToday", 0);
        this.natural = view.getBoolean("AssignProfessionWhenSpawned", false);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.isSleeping()) {
            return null;
        }
        if (this.hasCustomer()) {
            return SoundEvents.ENTITY_VILLAGER_TRADE;
        }
        return SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VILLAGER_DEATH;
    }

    public void playWorkSound() {
        this.playSound(this.getVillagerData().profession().value().workSound());
    }

    @Override
    public void setVillagerData(VillagerData villagerData) {
        VillagerData lv = this.getVillagerData();
        if (!lv.profession().equals(villagerData.profession())) {
            this.offers = null;
        }
        this.dataTracker.set(VILLAGER_DATA, villagerData);
    }

    @Override
    public VillagerData getVillagerData() {
        return this.dataTracker.get(VILLAGER_DATA);
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        int i = 3 + this.random.nextInt(4);
        this.experience += offer.getMerchantExperience();
        this.lastCustomer = this.getCustomer();
        if (this.canLevelUp()) {
            this.levelUpTimer = 40;
            this.levelingUp = true;
            i += 5;
        }
        if (offer.shouldRewardPlayerExperience()) {
            this.getEntityWorld().spawnEntity(new ExperienceOrbEntity(this.getEntityWorld(), this.getX(), this.getY() + 0.5, this.getZ(), i));
        }
    }

    @Override
    public void setAttacker(@Nullable LivingEntity attacker) {
        if (attacker != null && this.getEntityWorld() instanceof ServerWorld) {
            ((ServerWorld)this.getEntityWorld()).handleInteraction(EntityInteraction.VILLAGER_HURT, attacker, this);
            if (this.isAlive() && attacker instanceof PlayerEntity) {
                this.getEntityWorld().sendEntityStatus(this, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
            }
        }
        super.setAttacker(attacker);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        LOGGER.info("Villager {} died, message: '{}'", (Object)this, (Object)damageSource.getDeathMessage(this).getString());
        Entity lv = damageSource.getAttacker();
        if (lv != null) {
            this.notifyDeath(lv);
        }
        this.releaseAllTickets();
        super.onDeath(damageSource);
    }

    private void releaseAllTickets() {
        this.releaseTicketFor(MemoryModuleType.HOME);
        this.releaseTicketFor(MemoryModuleType.JOB_SITE);
        this.releaseTicketFor(MemoryModuleType.POTENTIAL_JOB_SITE);
        this.releaseTicketFor(MemoryModuleType.MEETING_POINT);
    }

    private void notifyDeath(Entity killer) {
        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld lv = (ServerWorld)world;
        Optional<LivingTargetCache> optional = this.brain.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS);
        if (optional.isEmpty()) {
            return;
        }
        optional.get().iterate(InteractionObserver.class::isInstance).forEach(observer -> lv.handleInteraction(EntityInteraction.VILLAGER_KILLED, killer, (InteractionObserver)((Object)observer)));
    }

    public void releaseTicketFor(MemoryModuleType<GlobalPos> pos) {
        if (!(this.getEntityWorld() instanceof ServerWorld)) {
            return;
        }
        MinecraftServer minecraftServer = ((ServerWorld)this.getEntityWorld()).getServer();
        this.brain.getOptionalRegisteredMemory(pos).ifPresent(posx -> {
            ServerWorld lv = minecraftServer.getWorld(posx.dimension());
            if (lv == null) {
                return;
            }
            PointOfInterestStorage lv2 = lv.getPointOfInterestStorage();
            Optional<RegistryEntry<PointOfInterestType>> optional = lv2.getType(posx.pos());
            BiPredicate<VillagerEntity, RegistryEntry<PointOfInterestType>> biPredicate = POINTS_OF_INTEREST.get(pos);
            if (optional.isPresent() && biPredicate.test(this, optional.get())) {
                lv2.releaseTicket(posx.pos());
                lv.getSubscriptionTracker().onPoiUpdated(posx.pos());
            }
        });
    }

    @Override
    public boolean isReadyToBreed() {
        return this.foodLevel + this.getAvailableFood() >= 12 && !this.isSleeping() && this.getBreedingAge() == 0;
    }

    private boolean canEatFood() {
        return this.foodLevel < 12;
    }

    private void consumeAvailableFood() {
        if (!this.canEatFood() || this.getAvailableFood() == 0) {
            return;
        }
        for (int i = 0; i < this.getInventory().size(); ++i) {
            int j;
            Integer integer;
            ItemStack lv = this.getInventory().getStack(i);
            if (lv.isEmpty() || (integer = ITEM_FOOD_VALUES.get(lv.getItem())) == null) continue;
            for (int k = j = lv.getCount(); k > 0; --k) {
                this.foodLevel += integer.intValue();
                this.getInventory().removeStack(i, 1);
                if (this.canEatFood()) continue;
                return;
            }
        }
    }

    public int getReputation(PlayerEntity player) {
        return this.gossip.getReputationFor(player.getUuid(), gossipType -> true);
    }

    private void depleteFood(int amount) {
        this.foodLevel -= amount;
    }

    public void eatForBreeding() {
        this.consumeAvailableFood();
        this.depleteFood(12);
    }

    public void setOffers(TradeOfferList offers) {
        this.offers = offers;
    }

    private boolean canLevelUp() {
        int i = this.getVillagerData().level();
        return VillagerData.canLevelUp(i) && this.experience >= VillagerData.getUpperLevelExperience(i);
    }

    private void levelUp() {
        this.setVillagerData(this.getVillagerData().withLevel(this.getVillagerData().level() + 1));
        this.fillRecipes();
    }

    @Override
    protected Text getDefaultName() {
        return this.getVillagerData().profession().value().id();
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.ADD_VILLAGER_HEART_PARTICLES) {
            this.produceParticles(ParticleTypes.HEART);
        } else if (status == EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES) {
            this.produceParticles(ParticleTypes.ANGRY_VILLAGER);
        } else if (status == EntityStatuses.ADD_VILLAGER_HAPPY_PARTICLES) {
            this.produceParticles(ParticleTypes.HAPPY_VILLAGER);
        } else if (status == EntityStatuses.ADD_SPLASH_PARTICLES) {
            this.produceParticles(ParticleTypes.SPLASH);
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        if (spawnReason == SpawnReason.BREEDING) {
            this.setVillagerData(this.getVillagerData().withProfession(world.getRegistryManager(), VillagerProfession.NONE));
        }
        if (spawnReason == SpawnReason.COMMAND || spawnReason == SpawnReason.SPAWN_ITEM_USE || SpawnReason.isAnySpawner(spawnReason) || spawnReason == SpawnReason.DISPENSER) {
            this.setVillagerData(this.getVillagerData().withType(world.getRegistryManager(), VillagerType.forBiome(world.getBiome(this.getBlockPos()))));
        }
        if (spawnReason == SpawnReason.STRUCTURE) {
            this.natural = true;
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    @Nullable
    public VillagerEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        double d = this.random.nextDouble();
        RegistryEntry<VillagerType> lv = d < 0.5 ? arg.getRegistryManager().getEntryOrThrow(VillagerType.forBiome(arg.getBiome(this.getBlockPos()))) : (d < 0.75 ? this.getVillagerData().type() : ((VillagerEntity)arg2).getVillagerData().type());
        VillagerEntity lv2 = new VillagerEntity(EntityType.VILLAGER, (World)arg, lv);
        lv2.initialize(arg, arg.getLocalDifficulty(lv2.getBlockPos()), SpawnReason.BREEDING, null);
        return lv2;
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        if (world.getDifficulty() != Difficulty.PEACEFUL) {
            LOGGER.info("Villager {} was struck by lightning {}.", (Object)this, (Object)lightning);
            WitchEntity lv = this.convertTo(EntityType.WITCH, EntityConversionContext.create(this, false, false), witch -> {
                witch.initialize(world, world.getLocalDifficulty(witch.getBlockPos()), SpawnReason.CONVERSION, null);
                witch.setPersistent();
                this.releaseAllTickets();
            });
            if (lv == null) {
                super.onStruckByLightning(world, lightning);
            }
        } else {
            super.onStruckByLightning(world, lightning);
        }
    }

    @Override
    protected void loot(ServerWorld world, ItemEntity itemEntity) {
        InventoryOwner.pickUpItem(world, this, this, itemEntity);
    }

    @Override
    public boolean canGather(ServerWorld world, ItemStack stack) {
        Item lv = stack.getItem();
        return (stack.isIn(ItemTags.VILLAGER_PICKS_UP) || this.getVillagerData().profession().value().gatherableItems().contains(lv)) && this.getInventory().canInsert(stack);
    }

    public boolean canShareFoodForBreeding() {
        return this.getAvailableFood() >= 24;
    }

    public boolean needsFoodForBreeding() {
        return this.getAvailableFood() < 12;
    }

    private int getAvailableFood() {
        SimpleInventory lv = this.getInventory();
        return ITEM_FOOD_VALUES.entrySet().stream().mapToInt(item -> lv.count((Item)item.getKey()) * (Integer)item.getValue()).sum();
    }

    public boolean hasSeedToPlant() {
        return this.getInventory().containsAny(stack -> stack.isIn(ItemTags.VILLAGER_PLANTABLE_SEEDS));
    }

    @Override
    protected void fillRecipes() {
        Int2ObjectMap<TradeOffers.Factory[]> int2ObjectMap;
        VillagerData lv = this.getVillagerData();
        RegistryKey lv2 = lv.profession().getKey().orElse(null);
        if (lv2 == null) {
            return;
        }
        Int2ObjectMap<TradeOffers.Factory[]> int2ObjectMap2 = this.getEntityWorld().getEnabledFeatures().contains(FeatureFlags.TRADE_REBALANCE) ? ((int2ObjectMap = TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE.get(lv2)) != null ? int2ObjectMap : TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(lv2)) : TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(lv2);
        if (int2ObjectMap2 == null || int2ObjectMap2.isEmpty()) {
            return;
        }
        TradeOffers.Factory[] lvs = (TradeOffers.Factory[])int2ObjectMap2.get(lv.level());
        if (lvs == null) {
            return;
        }
        TradeOfferList lv3 = this.getOffers();
        this.fillRecipesFromPool(lv3, lvs, 2);
        if (SharedConstants.UNLOCK_ALL_TRADES && lv.level() < int2ObjectMap2.size()) {
            this.levelUp();
        }
    }

    public void talkWithVillager(ServerWorld world, VillagerEntity villager, long time) {
        if (time >= this.gossipStartTime && time < this.gossipStartTime + 1200L || time >= villager.gossipStartTime && time < villager.gossipStartTime + 1200L) {
            return;
        }
        this.gossip.shareGossipFrom(villager.gossip, this.random, 10);
        this.gossipStartTime = time;
        villager.gossipStartTime = time;
        this.summonGolem(world, time, 5);
    }

    private void decayGossip() {
        long l = this.getEntityWorld().getTime();
        if (this.lastGossipDecayTime == 0L) {
            this.lastGossipDecayTime = l;
            return;
        }
        if (l < this.lastGossipDecayTime + 24000L) {
            return;
        }
        this.gossip.decay();
        this.lastGossipDecayTime = l;
    }

    public void summonGolem(ServerWorld world, long time, int requiredCount) {
        if (!this.canSummonGolem(time)) {
            return;
        }
        Box lv = this.getBoundingBox().expand(10.0, 10.0, 10.0);
        List<VillagerEntity> list = world.getNonSpectatingEntities(VillagerEntity.class, lv);
        List<VillagerEntity> list2 = list.stream().filter(villager -> villager.canSummonGolem(time)).limit(5L).toList();
        if (list2.size() < requiredCount) {
            return;
        }
        if (LargeEntitySpawnHelper.trySpawnAt(EntityType.IRON_GOLEM, SpawnReason.MOB_SUMMONED, world, this.getBlockPos(), 10, 8, 6, LargeEntitySpawnHelper.Requirements.IRON_GOLEM, false).isEmpty()) {
            return;
        }
        list.forEach(GolemLastSeenSensor::rememberIronGolem);
    }

    public boolean canSummonGolem(long time) {
        if (!this.hasRecentlySlept(this.getEntityWorld().getTime())) {
            return false;
        }
        return !this.brain.hasMemoryModule(MemoryModuleType.GOLEM_DETECTED_RECENTLY);
    }

    @Override
    public void onInteractionWith(EntityInteraction interaction, Entity entity) {
        if (interaction == EntityInteraction.ZOMBIE_VILLAGER_CURED) {
            this.gossip.startGossip(entity.getUuid(), VillagerGossipType.MAJOR_POSITIVE, 20);
            this.gossip.startGossip(entity.getUuid(), VillagerGossipType.MINOR_POSITIVE, 25);
        } else if (interaction == EntityInteraction.TRADE) {
            this.gossip.startGossip(entity.getUuid(), VillagerGossipType.TRADING, 2);
        } else if (interaction == EntityInteraction.VILLAGER_HURT) {
            this.gossip.startGossip(entity.getUuid(), VillagerGossipType.MINOR_NEGATIVE, 25);
        } else if (interaction == EntityInteraction.VILLAGER_KILLED) {
            this.gossip.startGossip(entity.getUuid(), VillagerGossipType.MAJOR_NEGATIVE, 25);
        }
    }

    @Override
    public int getExperience() {
        return this.experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    private void clearDailyRestockCount() {
        this.restockAndUpdateDemandBonus();
        this.restocksToday = 0;
    }

    public VillagerGossips getGossip() {
        return this.gossip;
    }

    public void readGossipData(VillagerGossips gossips) {
        this.gossip.add(gossips);
    }

    @Override
    public void sleep(BlockPos pos) {
        super.sleep(pos);
        this.brain.remember(MemoryModuleType.LAST_SLEPT, this.getEntityWorld().getTime());
        this.brain.forget(MemoryModuleType.WALK_TARGET);
        this.brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }

    @Override
    public void wakeUp() {
        super.wakeUp();
        this.brain.remember(MemoryModuleType.LAST_WOKEN, this.getEntityWorld().getTime());
    }

    private boolean hasRecentlySlept(long worldTime) {
        Optional<Long> optional = this.brain.getOptionalRegisteredMemory(MemoryModuleType.LAST_SLEPT);
        return optional.filter(lastSlept -> worldTime - lastSlept < 24000L).isPresent();
    }

    @Override
    @Nullable
    public <T> T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.VILLAGER_VARIANT) {
            return VillagerEntity.castComponentValue(type, this.getVillagerData().type());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.VILLAGER_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.VILLAGER_VARIANT) {
            RegistryEntry<VillagerType> lv = VillagerEntity.castComponentValue(DataComponentTypes.VILLAGER_VARIANT, value);
            this.setVillagerData(this.getVillagerData().withType(lv));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    @Override
    @Nullable
    public /* synthetic */ PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }
}

