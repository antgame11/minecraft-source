/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.UseRemainderComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.provider.EnchantmentProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentDropChances;
import net.minecraft.entity.EquipmentHolder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentTable;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobVisibilityCache;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.DebugTrackable;
import net.minecraft.world.debug.data.BrainDebugData;
import net.minecraft.world.debug.data.EntityPathDebugData;
import net.minecraft.world.debug.data.GoalSelectorDebugData;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class MobEntity
extends LivingEntity
implements EquipmentHolder,
Leashable,
Targeter {
    private static final TrackedData<Byte> MOB_FLAGS = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int AI_DISABLED_FLAG = 1;
    private static final int LEFT_HANDED_FLAG = 2;
    private static final int ATTACKING_FLAG = 4;
    protected static final int MINIMUM_DROPPED_EXPERIENCE_PER_EQUIPMENT = 1;
    private static final Vec3i ITEM_PICK_UP_RANGE_EXPANDER = new Vec3i(1, 0, 1);
    private static final List<EquipmentSlot> EQUIPMENT_INIT_ORDER = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    public static final float BASE_SPAWN_EQUIPMENT_CHANCE = 0.15f;
    public static final float ARMOR_UPGRADE_CHANCE = 0.1087f;
    public static final float ARMOR_UPGRADE_ROLLS = 3.0f;
    public static final float DEFAULT_CAN_PICKUP_LOOT_CHANCE = 0.55f;
    public static final float BASE_ENCHANTED_ARMOR_CHANCE = 0.5f;
    public static final float BASE_ENCHANTED_MAIN_HAND_EQUIPMENT_CHANCE = 0.25f;
    public static final int field_35039 = 2;
    private static final double ATTACK_RANGE = Math.sqrt(2.04f) - (double)0.6f;
    private static final boolean DEFAULT_CAN_PICK_UP_LOOT = false;
    private static final boolean DEFAULT_PERSISTENCE_REQUIRED = false;
    private static final boolean DEFAULT_LEFT_HANDED = false;
    private static final boolean DEFAULT_NO_AI = false;
    protected static final Identifier RANDOM_SPAWN_BONUS_MODIFIER_ID = Identifier.ofVanilla("random_spawn_bonus");
    public static final String DROP_CHANCES_KEY = "drop_chances";
    public static final String LEFT_HANDED_KEY = "LeftHanded";
    public static final String CAN_PICK_UP_LOOT_KEY = "CanPickUpLoot";
    public static final String NO_AI_KEY = "NoAI";
    public int ambientSoundChance;
    protected int experiencePoints;
    protected LookControl lookControl;
    protected MoveControl moveControl;
    protected JumpControl jumpControl;
    private final BodyControl bodyControl;
    protected EntityNavigation navigation;
    protected final GoalSelector goalSelector;
    protected final GoalSelector targetSelector;
    @Nullable
    private LivingEntity target;
    private final MobVisibilityCache visibilityCache;
    private EquipmentDropChances equipmentDropChances = EquipmentDropChances.DEFAULT;
    private boolean canPickUpLoot = false;
    private boolean persistent = false;
    private final Map<PathNodeType, Float> pathfindingPenalties = Maps.newEnumMap(PathNodeType.class);
    private Optional<RegistryKey<LootTable>> lootTable = Optional.empty();
    private long lootTableSeed;
    @Nullable
    private Leashable.LeashData leashData;
    private BlockPos positionTarget = BlockPos.ORIGIN;
    private int positionTargetRange = -1;

    protected MobEntity(EntityType<? extends MobEntity> arg, World arg2) {
        super((EntityType<? extends LivingEntity>)arg, arg2);
        this.goalSelector = new GoalSelector();
        this.targetSelector = new GoalSelector();
        this.lookControl = new LookControl(this);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);
        this.bodyControl = this.createBodyControl();
        this.navigation = this.createNavigation(arg2);
        this.visibilityCache = new MobVisibilityCache(this);
        if (arg2 instanceof ServerWorld) {
            this.initGoals();
        }
    }

    protected void initGoals() {
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes().add(EntityAttributes.FOLLOW_RANGE, 16.0);
    }

    protected EntityNavigation createNavigation(World world) {
        return new MobNavigation(this, world);
    }

    protected boolean movesIndependently() {
        return false;
    }

    public float getPathfindingPenalty(PathNodeType nodeType) {
        MobEntity lv;
        Entity entity = this.getControllingVehicle();
        MobEntity lv2 = entity instanceof MobEntity && (lv = (MobEntity)entity).movesIndependently() ? lv : this;
        Float float_ = lv2.pathfindingPenalties.get((Object)nodeType);
        return float_ == null ? nodeType.getDefaultPenalty() : float_.floatValue();
    }

    public void setPathfindingPenalty(PathNodeType nodeType, float penalty) {
        this.pathfindingPenalties.put(nodeType, Float.valueOf(penalty));
    }

    public void onStartPathfinding() {
    }

    public void onFinishPathfinding() {
    }

    protected BodyControl createBodyControl() {
        return new BodyControl(this);
    }

    public LookControl getLookControl() {
        return this.lookControl;
    }

    public MoveControl getMoveControl() {
        Entity entity = this.getControllingVehicle();
        if (entity instanceof MobEntity) {
            MobEntity lv = (MobEntity)entity;
            return lv.getMoveControl();
        }
        return this.moveControl;
    }

    public JumpControl getJumpControl() {
        return this.jumpControl;
    }

    public EntityNavigation getNavigation() {
        Entity entity = this.getControllingVehicle();
        if (entity instanceof MobEntity) {
            MobEntity lv = (MobEntity)entity;
            return lv.getNavigation();
        }
        return this.navigation;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity lv = this.getFirstPassenger();
        if (this.isAiDisabled()) return null;
        if (!(lv instanceof MobEntity)) return null;
        MobEntity lv2 = (MobEntity)lv;
        if (!lv.shouldControlVehicles()) return null;
        MobEntity mobEntity = lv2;
        return mobEntity;
    }

    public MobVisibilityCache getVisibilityCache() {
        return this.visibilityCache;
    }

    @Override
    @Nullable
    public LivingEntity getTarget() {
        return this.target;
    }

    @Nullable
    protected final LivingEntity getTargetInBrain() {
        return this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.target = target;
    }

    @Override
    public boolean canTarget(EntityType<?> type) {
        return type != EntityType.GHAST;
    }

    public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
        return false;
    }

    public void onEatingGrass() {
        this.emitGameEvent(GameEvent.EAT);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MOB_FLAGS, (byte)0);
    }

    public int getMinAmbientSoundDelay() {
        return 80;
    }

    public void playAmbientSound() {
        this.playSound(this.getAmbientSound());
    }

    @Override
    public void baseTick() {
        super.baseTick();
        Profiler lv = Profilers.get();
        lv.push("mobBaseTick");
        if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundChance++) {
            this.resetSoundDelay();
            this.playAmbientSound();
        }
        lv.pop();
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        this.resetSoundDelay();
        super.playHurtSound(damageSource);
    }

    private void resetSoundDelay() {
        this.ambientSoundChance = -this.getMinAmbientSoundDelay();
    }

    @Override
    protected int getExperienceToDrop(ServerWorld world) {
        if (this.experiencePoints > 0) {
            int i = this.experiencePoints;
            for (EquipmentSlot lv : EquipmentSlot.VALUES) {
                ItemStack lv2;
                if (!lv.increasesDroppedExperience() || (lv2 = this.getEquippedStack(lv)).isEmpty() || !(this.equipmentDropChances.get(lv) <= 1.0f)) continue;
                i += 1 + this.random.nextInt(3);
            }
            return i;
        }
        return this.experiencePoints;
    }

    public void playSpawnEffects() {
        if (this.getEntityWorld().isClient()) {
            this.addDeathParticles();
        } else {
            this.getEntityWorld().sendEntityStatus(this, EntityStatuses.PLAY_SPAWN_EFFECTS);
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.PLAY_SPAWN_EFFECTS) {
            this.playSpawnEffects();
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getEntityWorld().isClient() && this.age % 5 == 0) {
            this.updateGoalControls();
        }
    }

    protected void updateGoalControls() {
        boolean bl = !(this.getControllingPassenger() instanceof MobEntity);
        boolean bl2 = !(this.getVehicle() instanceof AbstractBoatEntity);
        this.goalSelector.setControlEnabled(Goal.Control.MOVE, bl);
        this.goalSelector.setControlEnabled(Goal.Control.JUMP, bl && bl2);
        this.goalSelector.setControlEnabled(Goal.Control.LOOK, bl);
    }

    @Override
    protected void turnHead(float bodyRotation) {
        this.bodyControl.tick();
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean(CAN_PICK_UP_LOOT_KEY, this.canPickUpLoot());
        view.putBoolean("PersistenceRequired", this.persistent);
        if (!this.equipmentDropChances.equals(EquipmentDropChances.DEFAULT)) {
            view.put(DROP_CHANCES_KEY, EquipmentDropChances.CODEC, this.equipmentDropChances);
        }
        this.writeLeashData(view, this.leashData);
        if (this.hasPositionTarget()) {
            view.putInt("home_radius", this.positionTargetRange);
            view.put("home_pos", BlockPos.CODEC, this.positionTarget);
        }
        view.putBoolean(LEFT_HANDED_KEY, this.isLeftHanded());
        this.lootTable.ifPresent(lootTableKey -> view.put("DeathLootTable", LootTable.TABLE_KEY, lootTableKey));
        if (this.lootTableSeed != 0L) {
            view.putLong("DeathLootTableSeed", this.lootTableSeed);
        }
        if (this.isAiDisabled()) {
            view.putBoolean(NO_AI_KEY, this.isAiDisabled());
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setCanPickUpLoot(view.getBoolean(CAN_PICK_UP_LOOT_KEY, false));
        this.persistent = view.getBoolean("PersistenceRequired", false);
        this.equipmentDropChances = view.read(DROP_CHANCES_KEY, EquipmentDropChances.CODEC).orElse(EquipmentDropChances.DEFAULT);
        this.readLeashData(view);
        this.positionTargetRange = view.getInt("home_radius", -1);
        if (this.positionTargetRange >= 0) {
            this.positionTarget = view.read("home_pos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        }
        this.setLeftHanded(view.getBoolean(LEFT_HANDED_KEY, false));
        this.lootTable = view.read("DeathLootTable", LootTable.TABLE_KEY);
        this.lootTableSeed = view.getLong("DeathLootTableSeed", 0L);
        this.setAiDisabled(view.getBoolean(NO_AI_KEY, false));
    }

    @Override
    protected void dropLoot(ServerWorld world, DamageSource damageSource, boolean causedByPlayer) {
        super.dropLoot(world, damageSource, causedByPlayer);
        this.lootTable = Optional.empty();
    }

    @Override
    public final Optional<RegistryKey<LootTable>> getLootTableKey() {
        if (this.lootTable.isPresent()) {
            return this.lootTable;
        }
        return super.getLootTableKey();
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setForwardSpeed(float forwardSpeed) {
        this.forwardSpeed = forwardSpeed;
    }

    public void setUpwardSpeed(float upwardSpeed) {
        this.upwardSpeed = upwardSpeed;
    }

    public void setSidewaysSpeed(float sidewaysSpeed) {
        this.sidewaysSpeed = sidewaysSpeed;
    }

    @Override
    public void setMovementSpeed(float movementSpeed) {
        super.setMovementSpeed(movementSpeed);
        this.setForwardSpeed(movementSpeed);
    }

    public void stopMovement() {
        this.getNavigation().stop();
        this.setSidewaysSpeed(0.0f);
        this.setUpwardSpeed(0.0f);
        this.setMovementSpeed(0.0f);
        this.setVelocity(0.0, 0.0, 0.0);
        this.resetLeashMomentum();
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        Profiler lv = Profilers.get();
        lv.push("looting");
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv2 = (ServerWorld)world;
            if (this.canPickUpLoot() && this.isAlive() && !this.dead && lv2.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                Vec3i lv3 = this.getItemPickUpRangeExpander();
                List<ItemEntity> list = this.getEntityWorld().getNonSpectatingEntities(ItemEntity.class, this.getBoundingBox().expand(lv3.getX(), lv3.getY(), lv3.getZ()));
                for (ItemEntity lv4 : list) {
                    if (lv4.isRemoved() || lv4.getStack().isEmpty() || lv4.cannotPickup() || !this.canGather(lv2, lv4.getStack())) continue;
                    this.loot(lv2, lv4);
                }
            }
        }
        lv.pop();
    }

    protected Vec3i getItemPickUpRangeExpander() {
        return ITEM_PICK_UP_RANGE_EXPANDER;
    }

    protected void loot(ServerWorld world, ItemEntity itemEntity) {
        ItemStack lv = itemEntity.getStack();
        ItemStack lv2 = this.tryEquip(world, lv.copy());
        if (!lv2.isEmpty()) {
            this.triggerItemPickedUpByEntityCriteria(itemEntity);
            this.sendPickup(itemEntity, lv2.getCount());
            lv.decrement(lv2.getCount());
            if (lv.isEmpty()) {
                itemEntity.discard();
            }
        }
    }

    public ItemStack tryEquip(ServerWorld world, ItemStack stack) {
        EquipmentSlot lv = this.getPreferredEquipmentSlot(stack);
        if (!this.canEquip(stack, lv)) {
            return ItemStack.EMPTY;
        }
        ItemStack lv2 = this.getEquippedStack(lv);
        boolean bl = this.prefersNewEquipment(stack, lv2, lv);
        if (lv.isArmorSlot() && !bl) {
            lv = EquipmentSlot.MAINHAND;
            lv2 = this.getEquippedStack(lv);
            bl = lv2.isEmpty();
        }
        if (bl && this.canPickupItem(stack)) {
            double d = this.equipmentDropChances.get(lv);
            if (!lv2.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1f, 0.0f) < d) {
                this.dropStack(world, lv2);
            }
            ItemStack lv3 = lv.split(stack);
            this.equipLootStack(lv, lv3);
            return lv3;
        }
        return ItemStack.EMPTY;
    }

    protected void equipLootStack(EquipmentSlot slot, ItemStack stack) {
        this.equipStack(slot, stack);
        this.setDropGuaranteed(slot);
        this.persistent = true;
    }

    protected boolean canRemoveSaddle(PlayerEntity player) {
        return !this.hasPassengers();
    }

    public void setDropGuaranteed(EquipmentSlot slot) {
        this.equipmentDropChances = this.equipmentDropChances.withGuaranteed(slot);
    }

    protected boolean prefersNewEquipment(ItemStack newStack, ItemStack currentStack, EquipmentSlot slot) {
        if (currentStack.isEmpty()) {
            return true;
        }
        if (slot.isArmorSlot()) {
            return this.prefersNewArmor(newStack, currentStack, slot);
        }
        if (slot == EquipmentSlot.MAINHAND) {
            return this.prefersNewWeapon(newStack, currentStack, slot);
        }
        return false;
    }

    private boolean prefersNewArmor(ItemStack newStack, ItemStack currentStack, EquipmentSlot slot) {
        if (EnchantmentHelper.hasAnyEnchantmentsWith(currentStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        double d = this.getAttributeValueWithStack(newStack, EntityAttributes.ARMOR, slot);
        double e = this.getAttributeValueWithStack(currentStack, EntityAttributes.ARMOR, slot);
        double f = this.getAttributeValueWithStack(newStack, EntityAttributes.ARMOR_TOUGHNESS, slot);
        double g = this.getAttributeValueWithStack(currentStack, EntityAttributes.ARMOR_TOUGHNESS, slot);
        if (d != e) {
            return d > e;
        }
        if (f != g) {
            return f > g;
        }
        return this.prefersNewDamageableItem(newStack, currentStack);
    }

    private boolean prefersNewWeapon(ItemStack newStack, ItemStack currentStack, EquipmentSlot slot) {
        double e;
        double d;
        TagKey<Item> lv = this.getPreferredWeapons();
        if (lv != null) {
            if (currentStack.isIn(lv) && !newStack.isIn(lv)) {
                return false;
            }
            if (!currentStack.isIn(lv) && newStack.isIn(lv)) {
                return true;
            }
        }
        if ((d = this.getAttributeValueWithStack(newStack, EntityAttributes.ATTACK_DAMAGE, slot)) != (e = this.getAttributeValueWithStack(currentStack, EntityAttributes.ATTACK_DAMAGE, slot))) {
            return d > e;
        }
        return this.prefersNewDamageableItem(newStack, currentStack);
    }

    private double getAttributeValueWithStack(ItemStack stack, RegistryEntry<EntityAttribute> attribute, EquipmentSlot slot) {
        double d = this.getAttributes().hasAttribute(attribute) ? this.getAttributeBaseValue(attribute) : 0.0;
        AttributeModifiersComponent lv = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        return lv.applyOperations(d, slot);
    }

    public boolean prefersNewDamageableItem(ItemStack newStack, ItemStack oldStack) {
        int j;
        Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> set = oldStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getEnchantmentEntries();
        Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> set2 = newStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getEnchantmentEntries();
        if (set2.size() != set.size()) {
            return set2.size() > set.size();
        }
        int i = newStack.getDamage();
        if (i != (j = oldStack.getDamage())) {
            return i < j;
        }
        return newStack.contains(DataComponentTypes.CUSTOM_NAME) && !oldStack.contains(DataComponentTypes.CUSTOM_NAME);
    }

    public boolean canPickupItem(ItemStack stack) {
        return true;
    }

    public boolean canGather(ServerWorld world, ItemStack stack) {
        return this.canPickupItem(stack);
    }

    @Nullable
    public TagKey<Item> getPreferredWeapons() {
        return null;
    }

    public boolean canImmediatelyDespawn(double distanceSquared) {
        return true;
    }

    public boolean cannotDespawn() {
        return this.hasVehicle();
    }

    @Override
    public void checkDespawn() {
        if (this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL && !this.getType().isAllowedInPeaceful()) {
            this.discard();
            return;
        }
        if (this.isPersistent() || this.cannotDespawn()) {
            this.despawnCounter = 0;
            return;
        }
        PlayerEntity lv = this.getEntityWorld().getClosestPlayer(this, -1.0);
        if (lv != null) {
            int i;
            int j;
            double d = lv.squaredDistanceTo(this);
            if (d > (double)(j = (i = this.getType().getSpawnGroup().getImmediateDespawnRange()) * i) && this.canImmediatelyDespawn(d)) {
                this.discard();
            }
            int k = this.getType().getSpawnGroup().getDespawnStartRange();
            int l = k * k;
            if (this.despawnCounter > 600 && this.random.nextInt(800) == 0 && d > (double)l && this.canImmediatelyDespawn(d)) {
                this.discard();
            } else if (d < (double)l) {
                this.despawnCounter = 0;
            }
        }
    }

    @Override
    protected final void tickNewAi() {
        ++this.despawnCounter;
        Profiler lv = Profilers.get();
        lv.push("sensing");
        this.visibilityCache.clear();
        lv.pop();
        int i = this.age + this.getId();
        if (i % 2 == 0 || this.age <= 1) {
            lv.push("targetSelector");
            this.targetSelector.tick();
            lv.pop();
            lv.push("goalSelector");
            this.goalSelector.tick();
            lv.pop();
        } else {
            lv.push("targetSelector");
            this.targetSelector.tickGoals(false);
            lv.pop();
            lv.push("goalSelector");
            this.goalSelector.tickGoals(false);
            lv.pop();
        }
        lv.push("navigation");
        this.navigation.tick();
        lv.pop();
        lv.push("mob tick");
        this.mobTick((ServerWorld)this.getEntityWorld());
        lv.pop();
        lv.push("controls");
        lv.push("move");
        this.moveControl.tick();
        lv.swap("look");
        this.lookControl.tick();
        lv.swap("jump");
        this.jumpControl.tick();
        lv.pop();
        lv.pop();
    }

    protected void mobTick(ServerWorld world) {
    }

    public int getMaxLookPitchChange() {
        return 40;
    }

    public int getMaxHeadRotation() {
        return 75;
    }

    protected void clampHeadYaw() {
        float f = this.getMaxHeadRotation();
        float g = this.getHeadYaw();
        float h = MathHelper.wrapDegrees(this.bodyYaw - g);
        float i = MathHelper.clamp(MathHelper.wrapDegrees(this.bodyYaw - g), -f, f);
        float j = g + h - i;
        this.setHeadYaw(j);
    }

    public int getMaxLookYawChange() {
        return 10;
    }

    public void lookAtEntity(Entity targetEntity, float maxYawChange, float maxPitchChange) {
        double h;
        double d = targetEntity.getX() - this.getX();
        double e = targetEntity.getZ() - this.getZ();
        if (targetEntity instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)targetEntity;
            h = lv.getEyeY() - this.getEyeY();
        } else {
            h = (targetEntity.getBoundingBox().minY + targetEntity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }
        double i = Math.sqrt(d * d + e * e);
        float j = (float)(MathHelper.atan2(e, d) * 57.2957763671875) - 90.0f;
        float k = (float)(-(MathHelper.atan2(h, i) * 57.2957763671875));
        this.setPitch(this.changeAngle(this.getPitch(), k, maxPitchChange));
        this.setYaw(this.changeAngle(this.getYaw(), j, maxYawChange));
    }

    private float changeAngle(float from, float to, float max) {
        float i = MathHelper.wrapDegrees(to - from);
        if (i > max) {
            i = max;
        }
        if (i < -max) {
            i = -max;
        }
        return from + i;
    }

    public static boolean canMobSpawn(EntityType<? extends MobEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        BlockPos lv = pos.down();
        return SpawnReason.isAnySpawner(spawnReason) || world.getBlockState(lv).allowsSpawning(world, lv, type);
    }

    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return true;
    }

    public boolean canSpawn(WorldView world) {
        return !world.containsFluid(this.getBoundingBox()) && world.doesNotIntersectEntities(this);
    }

    public int getLimitPerChunk() {
        return 4;
    }

    public boolean spawnsTooManyForEachTry(int count) {
        return false;
    }

    @Override
    public int getSafeFallDistance() {
        if (this.getTarget() == null) {
            return this.getSafeFallDistance(0.0f);
        }
        int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33f);
        if ((i -= (3 - this.getEntityWorld().getDifficulty().getId()) * 4) < 0) {
            i = 0;
        }
        return this.getSafeFallDistance(i);
    }

    public ItemStack getBodyArmor() {
        return this.getEquippedStack(EquipmentSlot.BODY);
    }

    public boolean hasSaddleEquipped() {
        return this.isWearing(EquipmentSlot.SADDLE);
    }

    public boolean isWearingBodyArmor() {
        return this.isWearing(EquipmentSlot.BODY);
    }

    private boolean isWearing(EquipmentSlot slot) {
        return this.hasStackEquipped(slot) && this.canEquip(this.getEquippedStack(slot), slot);
    }

    public void equipBodyArmor(ItemStack stack) {
        this.equipLootStack(EquipmentSlot.BODY, stack);
    }

    public Inventory createEquipmentInventory(final EquipmentSlot slot) {
        return new SingleStackInventory(){

            @Override
            public ItemStack getStack() {
                return MobEntity.this.getEquippedStack(slot);
            }

            @Override
            public void setStack(ItemStack stack) {
                MobEntity.this.equipStack(slot, stack);
                if (!stack.isEmpty()) {
                    MobEntity.this.setDropGuaranteed(slot);
                    MobEntity.this.setPersistent();
                }
            }

            @Override
            public void markDirty() {
            }

            @Override
            public boolean canPlayerUse(PlayerEntity player) {
                return player.getVehicle() == MobEntity.this || player.canInteractWithEntity(MobEntity.this, 4.0);
            }
        };
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        super.dropEquipment(world, source, causedByPlayer);
        for (EquipmentSlot lv : EquipmentSlot.VALUES) {
            ItemStack lv2 = this.getEquippedStack(lv);
            float f = this.equipmentDropChances.get(lv);
            if (f == 0.0f) continue;
            boolean bl2 = this.equipmentDropChances.dropsExactly(lv);
            Object object = source.getAttacker();
            if (object instanceof LivingEntity) {
                LivingEntity lv3 = (LivingEntity)object;
                object = this.getEntityWorld();
                if (object instanceof ServerWorld) {
                    ServerWorld lv4 = (ServerWorld)object;
                    f = EnchantmentHelper.getEquipmentDropChance(lv4, lv3, source, f);
                }
            }
            if (lv2.isEmpty() || EnchantmentHelper.hasAnyEnchantmentsWith(lv2, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP) || !causedByPlayer && !bl2 || !(this.random.nextFloat() < f)) continue;
            if (!bl2 && lv2.isDamageable()) {
                lv2.setDamage(lv2.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(lv2.getMaxDamage() - 3, 1))));
            }
            this.dropStack(world, lv2);
            this.equipStack(lv, ItemStack.EMPTY);
        }
    }

    public EquipmentDropChances getEquipmentDropChances() {
        return this.equipmentDropChances;
    }

    public void dropAllForeignEquipment(ServerWorld world) {
        this.dropForeignEquipment(world, stack -> true);
    }

    public Set<EquipmentSlot> dropForeignEquipment(ServerWorld world, Predicate<ItemStack> dropPredicate) {
        HashSet<EquipmentSlot> set = new HashSet<EquipmentSlot>();
        for (EquipmentSlot lv : EquipmentSlot.VALUES) {
            ItemStack lv2 = this.getEquippedStack(lv);
            if (lv2.isEmpty()) continue;
            if (!dropPredicate.test(lv2)) {
                set.add(lv);
                continue;
            }
            if (!this.equipmentDropChances.dropsExactly(lv)) continue;
            this.equipStack(lv, ItemStack.EMPTY);
            this.dropStack(world, lv2);
        }
        return set;
    }

    private LootWorldContext createEquipmentLootParameters(ServerWorld world) {
        return new LootWorldContext.Builder(world).add(LootContextParameters.ORIGIN, this.getEntityPos()).add(LootContextParameters.THIS_ENTITY, this).build(LootContextTypes.EQUIPMENT);
    }

    public void setEquipmentFromTable(EquipmentTable equipmentTable) {
        this.setEquipmentFromTable(equipmentTable.lootTable(), equipmentTable.slotDropChances());
    }

    public void setEquipmentFromTable(RegistryKey<LootTable> lootTable, Map<EquipmentSlot, Float> slotDropChances) {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            this.setEquipmentFromTable(lootTable, this.createEquipmentLootParameters(lv), slotDropChances);
        }
    }

    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (random.nextFloat() < 0.15f * localDifficulty.getClampedLocalDifficulty()) {
            int i = random.nextInt(3);
            int j = 1;
            while ((float)j <= 3.0f) {
                if (random.nextFloat() < 0.1087f) {
                    ++i;
                }
                ++j;
            }
            float f = this.getEntityWorld().getDifficulty() == Difficulty.HARD ? 0.1f : 0.25f;
            boolean bl = true;
            for (EquipmentSlot lv : EQUIPMENT_INIT_ORDER) {
                Item lv3;
                ItemStack lv2 = this.getEquippedStack(lv);
                if (!bl && random.nextFloat() < f) break;
                bl = false;
                if (!lv2.isEmpty() || (lv3 = MobEntity.getEquipmentForSlot(lv, i)) == null) continue;
                this.equipStack(lv, new ItemStack(lv3));
            }
        }
    }

    @Nullable
    public static Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int equipmentLevel) {
        switch (equipmentSlot) {
            case HEAD: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_HELMET;
                }
                if (equipmentLevel == 1) {
                    return Items.COPPER_HELMET;
                }
                if (equipmentLevel == 2) {
                    return Items.GOLDEN_HELMET;
                }
                if (equipmentLevel == 3) {
                    return Items.CHAINMAIL_HELMET;
                }
                if (equipmentLevel == 4) {
                    return Items.IRON_HELMET;
                }
                if (equipmentLevel == 5) {
                    return Items.DIAMOND_HELMET;
                }
            }
            case CHEST: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_CHESTPLATE;
                }
                if (equipmentLevel == 1) {
                    return Items.COPPER_CHESTPLATE;
                }
                if (equipmentLevel == 2) {
                    return Items.GOLDEN_CHESTPLATE;
                }
                if (equipmentLevel == 3) {
                    return Items.CHAINMAIL_CHESTPLATE;
                }
                if (equipmentLevel == 4) {
                    return Items.IRON_CHESTPLATE;
                }
                if (equipmentLevel == 5) {
                    return Items.DIAMOND_CHESTPLATE;
                }
            }
            case LEGS: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_LEGGINGS;
                }
                if (equipmentLevel == 1) {
                    return Items.COPPER_LEGGINGS;
                }
                if (equipmentLevel == 2) {
                    return Items.GOLDEN_LEGGINGS;
                }
                if (equipmentLevel == 3) {
                    return Items.CHAINMAIL_LEGGINGS;
                }
                if (equipmentLevel == 4) {
                    return Items.IRON_LEGGINGS;
                }
                if (equipmentLevel == 5) {
                    return Items.DIAMOND_LEGGINGS;
                }
            }
            case FEET: {
                if (equipmentLevel == 0) {
                    return Items.LEATHER_BOOTS;
                }
                if (equipmentLevel == 1) {
                    return Items.COPPER_BOOTS;
                }
                if (equipmentLevel == 2) {
                    return Items.GOLDEN_BOOTS;
                }
                if (equipmentLevel == 3) {
                    return Items.CHAINMAIL_BOOTS;
                }
                if (equipmentLevel == 4) {
                    return Items.IRON_BOOTS;
                }
                if (equipmentLevel != 5) break;
                return Items.DIAMOND_BOOTS;
            }
        }
        return null;
    }

    protected void updateEnchantments(ServerWorldAccess world, Random random, LocalDifficulty localDifficulty) {
        this.enchantMainHandItem(world, random, localDifficulty);
        for (EquipmentSlot lv : EquipmentSlot.VALUES) {
            if (lv.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            this.enchantEquipment(world, random, lv, localDifficulty);
        }
    }

    protected void enchantMainHandItem(ServerWorldAccess world, Random random, LocalDifficulty localDifficulty) {
        this.enchantEquipment(world, EquipmentSlot.MAINHAND, random, 0.25f, localDifficulty);
    }

    protected void enchantEquipment(ServerWorldAccess world, Random random, EquipmentSlot slot, LocalDifficulty localDifficulty) {
        this.enchantEquipment(world, slot, random, 0.5f, localDifficulty);
    }

    private void enchantEquipment(ServerWorldAccess world, EquipmentSlot slot, Random random, float power, LocalDifficulty localDifficulty) {
        ItemStack lv = this.getEquippedStack(slot);
        if (!lv.isEmpty() && random.nextFloat() < power * localDifficulty.getClampedLocalDifficulty()) {
            EnchantmentHelper.applyEnchantmentProvider(lv, world.getRegistryManager(), EnchantmentProviders.MOB_SPAWN_EQUIPMENT, localDifficulty, random);
            this.equipStack(slot, lv);
        }
    }

    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random lv = world.getRandom();
        EntityAttributeInstance lv2 = Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.FOLLOW_RANGE));
        if (!lv2.hasModifier(RANDOM_SPAWN_BONUS_MODIFIER_ID)) {
            lv2.addPersistentModifier(new EntityAttributeModifier(RANDOM_SPAWN_BONUS_MODIFIER_ID, lv.nextTriangular(0.0, 0.11485000000000001), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        this.setLeftHanded(lv.nextFloat() < 0.05f);
        return entityData;
    }

    public void setPersistent() {
        this.persistent = true;
    }

    @Override
    public void setEquipmentDropChance(EquipmentSlot slot, float dropChance) {
        this.equipmentDropChances = this.equipmentDropChances.withChance(slot, dropChance);
    }

    @Override
    public boolean canPickUpLoot() {
        return this.canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
    }

    @Override
    protected boolean canDispenserEquipSlot(EquipmentSlot slot) {
        return this.canPickUpLoot();
    }

    public boolean isPersistent() {
        return this.persistent;
    }

    @Override
    public final ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.isAlive()) {
            return ActionResult.PASS;
        }
        ActionResult lv = this.interactWithItem(player, hand);
        if (lv.isAccepted()) {
            this.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
            return lv;
        }
        ActionResult lv2 = super.interact(player, hand);
        if (lv2 != ActionResult.PASS) {
            return lv2;
        }
        lv = this.interactMob(player, hand);
        if (lv.isAccepted()) {
            this.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
            return lv;
        }
        return ActionResult.PASS;
    }

    private ActionResult interactWithItem(PlayerEntity player, Hand hand) {
        ActionResult lv2;
        ItemStack lv = player.getStackInHand(hand);
        if (lv.isOf(Items.NAME_TAG) && (lv2 = lv.useOnEntity(player, this, hand)).isAccepted()) {
            return lv2;
        }
        if (lv.getItem() instanceof SpawnEggItem) {
            if (this.getEntityWorld() instanceof ServerWorld) {
                SpawnEggItem lv3 = (SpawnEggItem)lv.getItem();
                Optional<MobEntity> optional = lv3.spawnBaby(player, this, this.getType(), (ServerWorld)this.getEntityWorld(), this.getEntityPos(), lv);
                optional.ifPresent(entity -> this.onPlayerSpawnedChild(player, (MobEntity)entity));
                if (optional.isEmpty()) {
                    return ActionResult.PASS;
                }
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.PASS;
    }

    protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
    }

    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        return ActionResult.PASS;
    }

    protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
        int i = stack.getCount();
        UseRemainderComponent lv = stack.get(DataComponentTypes.USE_REMAINDER);
        stack.decrementUnlessCreative(1, player);
        if (lv != null) {
            ItemStack lv2 = lv.convert(stack, i, player.isInCreativeMode(), player::giveOrDropStack);
            player.setStackInHand(hand, lv2);
        }
    }

    public boolean isInPositionTargetRange() {
        return this.isInPositionTargetRange(this.getBlockPos());
    }

    public boolean isInPositionTargetRange(BlockPos pos) {
        if (this.positionTargetRange == -1) {
            return true;
        }
        return this.positionTarget.getSquaredDistance(pos) < (double)(this.positionTargetRange * this.positionTargetRange);
    }

    public boolean isInPositionTargetRange(Vec3d pos) {
        if (this.positionTargetRange == -1) {
            return true;
        }
        return this.positionTarget.getSquaredDistance(pos) < (double)(this.positionTargetRange * this.positionTargetRange);
    }

    public void setPositionTarget(BlockPos target, int range) {
        this.positionTarget = target;
        this.positionTargetRange = range;
    }

    public BlockPos getPositionTarget() {
        return this.positionTarget;
    }

    public int getPositionTargetRange() {
        return this.positionTargetRange;
    }

    public void clearPositionTarget() {
        this.positionTargetRange = -1;
    }

    public boolean hasPositionTarget() {
        return this.positionTargetRange != -1;
    }

    @Nullable
    public <T extends MobEntity> T convertTo(EntityType<T> entityType, EntityConversionContext context, SpawnReason reason, EntityConversionContext.Finalizer<T> finalizer) {
        if (this.isRemoved()) {
            return null;
        }
        MobEntity lv = (MobEntity)entityType.create(this.getEntityWorld(), reason);
        if (lv == null) {
            return null;
        }
        context.type().setUpNewEntity(this, lv, context);
        finalizer.finalizeConversion(lv);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv2 = (ServerWorld)world;
            lv2.spawnEntity(lv);
        }
        if (context.type().shouldDiscardOldEntity()) {
            this.discard();
        }
        return (T)lv;
    }

    @Nullable
    public <T extends MobEntity> T convertTo(EntityType<T> entityType, EntityConversionContext context, EntityConversionContext.Finalizer<T> finalizer) {
        return this.convertTo(entityType, context, SpawnReason.CONVERSION, finalizer);
    }

    @Override
    @Nullable
    public Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    private void resetLeashMomentum() {
        if (this.leashData != null) {
            this.leashData.momentum = 0.0;
        }
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData leashData) {
        this.leashData = leashData;
    }

    @Override
    public void onLeashRemoved() {
        if (this.getLeashData() == null) {
            this.clearPositionTarget();
        }
    }

    @Override
    public void snapLongLeash() {
        Leashable.super.snapLongLeash();
        this.goalSelector.disableControl(Goal.Control.MOVE);
    }

    @Override
    public boolean canBeLeashed() {
        return !(this instanceof Monster);
    }

    @Override
    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
        boolean bl3 = super.startRiding(entity, force, emitEvent);
        if (bl3 && this.isLeashed()) {
            this.detachLeash();
        }
        return bl3;
    }

    @Override
    public boolean canActVoluntarily() {
        return super.canActVoluntarily() && !this.isAiDisabled();
    }

    public void setAiDisabled(boolean aiDisabled) {
        byte b = this.dataTracker.get(MOB_FLAGS);
        this.dataTracker.set(MOB_FLAGS, aiDisabled ? (byte)(b | 1) : (byte)(b & 0xFFFFFFFE));
    }

    public void setLeftHanded(boolean leftHanded) {
        byte b = this.dataTracker.get(MOB_FLAGS);
        this.dataTracker.set(MOB_FLAGS, leftHanded ? (byte)(b | 2) : (byte)(b & 0xFFFFFFFD));
    }

    public void setAttacking(boolean attacking) {
        byte b = this.dataTracker.get(MOB_FLAGS);
        this.dataTracker.set(MOB_FLAGS, attacking ? (byte)(b | 4) : (byte)(b & 0xFFFFFFFB));
    }

    public boolean isAiDisabled() {
        return (this.dataTracker.get(MOB_FLAGS) & 1) != 0;
    }

    public boolean isLeftHanded() {
        return (this.dataTracker.get(MOB_FLAGS) & 2) != 0;
    }

    public boolean isAttacking() {
        return (this.dataTracker.get(MOB_FLAGS) & 4) != 0;
    }

    public void setBaby(boolean baby) {
    }

    @Override
    public Arm getMainArm() {
        return this.isLeftHanded() ? Arm.LEFT : Arm.RIGHT;
    }

    public boolean isInAttackRange(LivingEntity entity) {
        return this.getAttackBox().intersects(entity.getHitbox());
    }

    protected Box getAttackBox() {
        Box lv4;
        Entity lv = this.getVehicle();
        if (lv != null) {
            Box lv2 = lv.getBoundingBox();
            Box lv3 = this.getBoundingBox();
            lv4 = new Box(Math.min(lv3.minX, lv2.minX), lv3.minY, Math.min(lv3.minZ, lv2.minZ), Math.max(lv3.maxX, lv2.maxX), lv3.maxY, Math.max(lv3.maxZ, lv2.maxZ));
        } else {
            lv4 = this.getBoundingBox();
        }
        return lv4.expand(ATTACK_RANGE, 0.0, ATTACK_RANGE);
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean bl;
        float f = (float)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        ItemStack lv = this.getWeaponStack();
        DamageSource lv2 = Optional.ofNullable(lv.getItem().getDamageSource(this)).orElse(this.getDamageSources().mobAttack(this));
        f = EnchantmentHelper.getDamage(world, lv, target, lv2, f);
        if (bl = target.damage(world, lv2, f += lv.getItem().getBonusAttackDamage(target, f, lv2))) {
            LivingEntity lv3;
            float g = this.getAttackKnockbackAgainst(target, lv2);
            if (g > 0.0f && target instanceof LivingEntity) {
                lv3 = (LivingEntity)target;
                lv3.takeKnockback(g * 0.5f, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            }
            if (target instanceof LivingEntity) {
                lv3 = (LivingEntity)target;
                lv.postHit(lv3, this);
            }
            EnchantmentHelper.onTargetDamaged(world, target, lv2);
            this.onAttacking(target);
            this.playAttackSound();
        }
        return bl;
    }

    protected void playAttackSound() {
    }

    protected boolean isAffectedByDaylight() {
        if (this.getEntityWorld().isDay() && !this.getEntityWorld().isClient()) {
            boolean bl;
            float f = this.getBrightnessAtEyes();
            BlockPos lv = BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ());
            boolean bl2 = bl = this.isTouchingWaterOrRain() || this.inPowderSnow || this.wasInPowderSnow;
            if (f > 0.5f && this.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f && !bl && this.getEntityWorld().isSkyVisible(lv)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void swimUpward(TagKey<Fluid> fluid) {
        if (this.getNavigation().canSwim()) {
            super.swimUpward(fluid);
        } else {
            this.setVelocity(this.getVelocity().add(0.0, 0.3, 0.0));
        }
    }

    @VisibleForTesting
    public void clearGoalsAndTasks() {
        this.clearGoals(goal -> true);
        this.getBrain().clear();
    }

    public void clearGoals(Predicate<Goal> predicate) {
        this.goalSelector.clear(predicate);
    }

    @Override
    protected void removeFromDimension() {
        super.removeFromDimension();
        for (EquipmentSlot lv : EquipmentSlot.VALUES) {
            ItemStack lv2 = this.getEquippedStack(lv);
            if (lv2.isEmpty()) continue;
            lv2.setCount(0);
        }
    }

    @Override
    @Nullable
    public ItemStack getPickBlockStack() {
        SpawnEggItem lv = SpawnEggItem.forEntity(this.getType());
        if (lv == null) {
            return null;
        }
        return new ItemStack(lv);
    }

    @Override
    protected void updateAttribute(RegistryEntry<EntityAttribute> attribute) {
        super.updateAttribute(attribute);
        if (attribute.matches(EntityAttributes.FOLLOW_RANGE) || attribute.matches(EntityAttributes.TEMPT_RANGE)) {
            this.getNavigation().updateRange();
        }
    }

    @Override
    public void registerTracking(ServerWorld world, DebugTrackable.Tracker tracker) {
        tracker.track(DebugSubscriptionTypes.ENTITY_PATHS, () -> {
            Path lv = this.getNavigation().getCurrentPath();
            if (lv != null && lv.getDebugNodeInfos() != null) {
                return new EntityPathDebugData(lv.copy(), this.getNavigation().getNodeReachProximity());
            }
            return null;
        });
        tracker.track(DebugSubscriptionTypes.GOAL_SELECTORS, () -> {
            Set<PrioritizedGoal> set = this.goalSelector.getGoals();
            ArrayList<GoalSelectorDebugData.Goal> list = new ArrayList<GoalSelectorDebugData.Goal>(set.size());
            set.forEach(goal -> list.add(new GoalSelectorDebugData.Goal(goal.getPriority(), goal.isRunning(), goal.getGoal().getClass().getSimpleName())));
            return new GoalSelectorDebugData(list);
        });
        if (!this.brain.isEmpty()) {
            tracker.track(DebugSubscriptionTypes.BRAINS, () -> BrainDebugData.fromEntity(world, this));
        }
    }
}

