/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.decoration;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class ArmorStandEntity
extends LivingEntity {
    public static final int field_30443 = 5;
    private static final boolean field_30445 = true;
    public static final EulerAngle DEFAULT_HEAD_ROTATION = new EulerAngle(0.0f, 0.0f, 0.0f);
    public static final EulerAngle DEFAULT_BODY_ROTATION = new EulerAngle(0.0f, 0.0f, 0.0f);
    public static final EulerAngle DEFAULT_LEFT_ARM_ROTATION = new EulerAngle(-10.0f, 0.0f, -10.0f);
    public static final EulerAngle DEFAULT_RIGHT_ARM_ROTATION = new EulerAngle(-15.0f, 0.0f, 10.0f);
    public static final EulerAngle DEFAULT_LEFT_LEG_ROTATION = new EulerAngle(-1.0f, 0.0f, -1.0f);
    public static final EulerAngle DEFAULT_RIGHT_LEG_ROTATION = new EulerAngle(1.0f, 0.0f, 1.0f);
    private static final EntityDimensions MARKER_DIMENSIONS = EntityDimensions.fixed(0.0f, 0.0f);
    private static final EntityDimensions SMALL_DIMENSIONS = EntityType.ARMOR_STAND.getDimensions().scaled(0.5f).withEyeHeight(0.9875f);
    private static final double field_30447 = 0.1;
    private static final double field_30448 = 0.9;
    private static final double field_30449 = 0.4;
    private static final double field_30450 = 1.6;
    public static final int field_30446 = 8;
    public static final int field_30451 = 16;
    public static final int SMALL_FLAG = 1;
    public static final int SHOW_ARMS_FLAG = 4;
    public static final int HIDE_BASE_PLATE_FLAG = 8;
    public static final int MARKER_FLAG = 16;
    public static final TrackedData<Byte> ARMOR_STAND_FLAGS = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.BYTE);
    public static final TrackedData<EulerAngle> TRACKER_HEAD_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_BODY_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_LEFT_ARM_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_RIGHT_ARM_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_LEFT_LEG_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_RIGHT_LEG_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    private static final Predicate<Entity> RIDEABLE_MINECART_PREDICATE = entity -> {
        AbstractMinecartEntity lv;
        return entity instanceof AbstractMinecartEntity && (lv = (AbstractMinecartEntity)entity).isRideable();
    };
    private static final boolean field_57644 = false;
    private static final int DEFAULT_DISABLED_SLOTS = 0;
    private static final boolean field_57646 = false;
    private static final boolean field_57647 = false;
    private static final boolean field_57648 = false;
    private static final boolean field_57649 = false;
    private boolean invisible = false;
    public long lastHitTime;
    private int disabledSlots = 0;

    public ArmorStandEntity(EntityType<? extends ArmorStandEntity> arg, World arg2) {
        super((EntityType<? extends LivingEntity>)arg, arg2);
    }

    public ArmorStandEntity(World world, double x, double y, double z) {
        this((EntityType<? extends ArmorStandEntity>)EntityType.ARMOR_STAND, world);
        this.setPosition(x, y, z);
    }

    public static DefaultAttributeContainer.Builder createArmorStandAttributes() {
        return ArmorStandEntity.createLivingAttributes().add(EntityAttributes.STEP_HEIGHT, 0.0);
    }

    @Override
    public void calculateDimensions() {
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        super.calculateDimensions();
        this.setPosition(d, e, f);
    }

    private boolean canClip() {
        return !this.isMarker() && !this.hasNoGravity();
    }

    @Override
    public boolean canActVoluntarily() {
        return super.canActVoluntarily() && this.canClip();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ARMOR_STAND_FLAGS, (byte)0);
        builder.add(TRACKER_HEAD_ROTATION, DEFAULT_HEAD_ROTATION);
        builder.add(TRACKER_BODY_ROTATION, DEFAULT_BODY_ROTATION);
        builder.add(TRACKER_LEFT_ARM_ROTATION, DEFAULT_LEFT_ARM_ROTATION);
        builder.add(TRACKER_RIGHT_ARM_ROTATION, DEFAULT_RIGHT_ARM_ROTATION);
        builder.add(TRACKER_LEFT_LEG_ROTATION, DEFAULT_LEFT_LEG_ROTATION);
        builder.add(TRACKER_RIGHT_LEG_ROTATION, DEFAULT_RIGHT_LEG_ROTATION);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return slot != EquipmentSlot.BODY && slot != EquipmentSlot.SADDLE && !this.isSlotDisabled(slot);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("Invisible", this.isInvisible());
        view.putBoolean("Small", this.isSmall());
        view.putBoolean("ShowArms", this.shouldShowArms());
        view.putInt("DisabledSlots", this.disabledSlots);
        view.putBoolean("NoBasePlate", !this.shouldShowBasePlate());
        if (this.isMarker()) {
            view.putBoolean("Marker", this.isMarker());
        }
        view.put("Pose", PackedRotation.CODEC, this.packRotation());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setInvisible(view.getBoolean("Invisible", false));
        this.setSmall(view.getBoolean("Small", false));
        this.setShowArms(view.getBoolean("ShowArms", false));
        this.disabledSlots = view.getInt("DisabledSlots", 0);
        this.setHideBasePlate(view.getBoolean("NoBasePlate", false));
        this.setMarker(view.getBoolean("Marker", false));
        this.noClip = !this.canClip();
        view.read("Pose", PackedRotation.CODEC).ifPresent(this::unpackRotation);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    @Override
    protected void tickCramming() {
        List<Entity> list = this.getEntityWorld().getOtherEntities(this, this.getBoundingBox(), RIDEABLE_MINECART_PREDICATE);
        for (Entity lv : list) {
            if (!(this.squaredDistanceTo(lv) <= 0.2)) continue;
            lv.pushAwayFrom(this);
        }
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (this.isMarker() || lv.isOf(Items.NAME_TAG)) {
            return ActionResult.PASS;
        }
        if (player.isSpectator()) {
            return ActionResult.SUCCESS;
        }
        if (player.getEntityWorld().isClient()) {
            return ActionResult.SUCCESS_SERVER;
        }
        EquipmentSlot lv2 = this.getPreferredEquipmentSlot(lv);
        if (lv.isEmpty()) {
            EquipmentSlot lv4;
            EquipmentSlot lv3 = this.getSlotFromPosition(hitPos);
            EquipmentSlot equipmentSlot = lv4 = this.isSlotDisabled(lv3) ? lv2 : lv3;
            if (this.hasStackEquipped(lv4) && this.equip(player, lv4, lv, hand)) {
                return ActionResult.SUCCESS_SERVER;
            }
        } else {
            if (this.isSlotDisabled(lv2)) {
                return ActionResult.FAIL;
            }
            if (lv2.getType() == EquipmentSlot.Type.HAND && !this.shouldShowArms()) {
                return ActionResult.FAIL;
            }
            if (this.equip(player, lv2, lv, hand)) {
                return ActionResult.SUCCESS_SERVER;
            }
        }
        return ActionResult.PASS;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private EquipmentSlot getSlotFromPosition(Vec3d hitPos) {
        EquipmentSlot lv = EquipmentSlot.MAINHAND;
        boolean bl = this.isSmall();
        double d = hitPos.y / (double)(this.getScale() * this.getScaleFactor());
        EquipmentSlot lv2 = EquipmentSlot.FEET;
        if (d >= 0.1) {
            double d2 = bl ? 0.8 : 0.45;
            if (d < 0.1 + d2 && this.hasStackEquipped(lv2)) {
                return EquipmentSlot.FEET;
            }
        }
        double d3 = bl ? 0.3 : 0.0;
        if (d >= 0.9 + d3) {
            double d4 = bl ? 1.0 : 0.7;
            if (d < 0.9 + d4 && this.hasStackEquipped(EquipmentSlot.CHEST)) {
                return EquipmentSlot.CHEST;
            }
        }
        if (d >= 0.4) {
            double d5 = bl ? 1.0 : 0.8;
            if (d < 0.4 + d5 && this.hasStackEquipped(EquipmentSlot.LEGS)) {
                return EquipmentSlot.LEGS;
            }
        }
        if (d >= 1.6 && this.hasStackEquipped(EquipmentSlot.HEAD)) {
            return EquipmentSlot.HEAD;
        }
        if (this.hasStackEquipped(EquipmentSlot.MAINHAND)) return lv;
        if (!this.hasStackEquipped(EquipmentSlot.OFFHAND)) return lv;
        return EquipmentSlot.OFFHAND;
    }

    private boolean isSlotDisabled(EquipmentSlot slot) {
        return (this.disabledSlots & 1 << slot.getOffsetIndex(0)) != 0 || slot.getType() == EquipmentSlot.Type.HAND && !this.shouldShowArms();
    }

    private boolean equip(PlayerEntity player, EquipmentSlot slot, ItemStack stack, Hand hand) {
        ItemStack lv = this.getEquippedStack(slot);
        if (!lv.isEmpty() && (this.disabledSlots & 1 << slot.getOffsetIndex(8)) != 0) {
            return false;
        }
        if (lv.isEmpty() && (this.disabledSlots & 1 << slot.getOffsetIndex(16)) != 0) {
            return false;
        }
        if (player.isInCreativeMode() && lv.isEmpty() && !stack.isEmpty()) {
            this.equipStack(slot, stack.copyWithCount(1));
            return true;
        }
        if (!stack.isEmpty() && stack.getCount() > 1) {
            if (!lv.isEmpty()) {
                return false;
            }
            this.equipStack(slot, stack.split(1));
            return true;
        }
        this.equipStack(slot, stack);
        player.setStackInHand(hand, lv);
        return true;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isRemoved()) {
            return false;
        }
        if (!world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && source.getAttacker() instanceof MobEntity) {
            return false;
        }
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.kill(world);
            return false;
        }
        if (this.isInvulnerableTo(world, source) || this.invisible || this.isMarker()) {
            return false;
        }
        if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
            this.onBreak(world, source);
            this.kill(world);
            return false;
        }
        if (source.isIn(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
            if (this.isOnFire()) {
                this.updateHealth(world, source, 0.15f);
            } else {
                this.setOnFireFor(5.0f);
            }
            return false;
        }
        if (source.isIn(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5f) {
            this.updateHealth(world, source, 4.0f);
            return false;
        }
        boolean bl = source.isIn(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
        boolean bl2 = source.isIn(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
        if (!bl && !bl2) {
            return false;
        }
        Entity entity = source.getAttacker();
        if (entity instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)entity;
            if (!lv.getAbilities().allowModifyWorld) {
                return false;
            }
        }
        if (source.isSourceCreativePlayer()) {
            this.playBreakSound();
            this.spawnBreakParticles();
            this.kill(world);
            return true;
        }
        long l = world.getTime();
        if (l - this.lastHitTime <= 5L || bl2) {
            this.breakAndDropItem(world, source);
            this.spawnBreakParticles();
            this.kill(world);
        } else {
            world.sendEntityStatus(this, EntityStatuses.HIT_ARMOR_STAND);
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
            this.lastHitTime = l;
        }
        return true;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.HIT_ARMOR_STAND) {
            if (this.getEntityWorld().isClient()) {
                this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_HIT, this.getSoundCategory(), 0.3f, 1.0f, false);
                this.lastHitTime = this.getEntityWorld().getTime();
            }
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(e) || e == 0.0) {
            e = 4.0;
        }
        return distance < (e *= 64.0) * e;
    }

    private void spawnBreakParticles() {
        if (this.getEntityWorld() instanceof ServerWorld) {
            ((ServerWorld)this.getEntityWorld()).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.getDefaultState()), this.getX(), this.getBodyY(0.6666666666666666), this.getZ(), 10, this.getWidth() / 4.0f, this.getHeight() / 4.0f, this.getWidth() / 4.0f, 0.05);
        }
    }

    private void updateHealth(ServerWorld world, DamageSource damageSource, float amount) {
        float g = this.getHealth();
        if ((g -= amount) <= 0.5f) {
            this.onBreak(world, damageSource);
            this.kill(world);
        } else {
            this.setHealth(g);
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getAttacker());
        }
    }

    private void breakAndDropItem(ServerWorld world, DamageSource damageSource) {
        ItemStack lv = new ItemStack(Items.ARMOR_STAND);
        lv.set(DataComponentTypes.CUSTOM_NAME, this.getCustomName());
        Block.dropStack(this.getEntityWorld(), this.getBlockPos(), lv);
        this.onBreak(world, damageSource);
    }

    private void onBreak(ServerWorld world, DamageSource damageSource) {
        this.playBreakSound();
        this.drop(world, damageSource);
        for (EquipmentSlot lv : EquipmentSlot.VALUES) {
            ItemStack lv2 = this.equipment.put(lv, ItemStack.EMPTY);
            if (lv2.isEmpty()) continue;
            Block.dropStack(this.getEntityWorld(), this.getBlockPos().up(), lv2);
        }
    }

    private void playBreakSound() {
        this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_BREAK, this.getSoundCategory(), 1.0f, 1.0f);
    }

    @Override
    protected void turnHead(float bodyRotation) {
        this.lastBodyYaw = this.lastYaw;
        this.bodyYaw = this.getYaw();
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (!this.canClip()) {
            return;
        }
        super.travel(movementInput);
    }

    @Override
    public void setBodyYaw(float bodyYaw) {
        this.lastBodyYaw = this.lastYaw = bodyYaw;
        this.lastHeadYaw = this.headYaw = bodyYaw;
    }

    @Override
    public void setHeadYaw(float headYaw) {
        this.lastBodyYaw = this.lastYaw = headYaw;
        this.lastHeadYaw = this.headYaw = headYaw;
    }

    @Override
    protected void updatePotionVisibility() {
        this.setInvisible(this.invisible);
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
        super.setInvisible(invisible);
    }

    @Override
    public boolean isBaby() {
        return this.isSmall();
    }

    @Override
    public void kill(ServerWorld world) {
        this.remove(Entity.RemovalReason.KILLED);
        this.emitGameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        if (explosion.preservesDecorativeEntities()) {
            return this.isInvisible();
        }
        return true;
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        if (this.isMarker()) {
            return PistonBehavior.IGNORE;
        }
        return super.getPistonBehavior();
    }

    @Override
    public boolean canAvoidTraps() {
        return this.isMarker();
    }

    private void setSmall(boolean small) {
        this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(ARMOR_STAND_FLAGS), SMALL_FLAG, small));
    }

    public boolean isSmall() {
        return (this.dataTracker.get(ARMOR_STAND_FLAGS) & 1) != 0;
    }

    public void setShowArms(boolean showArms) {
        this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(ARMOR_STAND_FLAGS), SHOW_ARMS_FLAG, showArms));
    }

    public boolean shouldShowArms() {
        return (this.dataTracker.get(ARMOR_STAND_FLAGS) & 4) != 0;
    }

    public void setHideBasePlate(boolean hideBasePlate) {
        this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(ARMOR_STAND_FLAGS), HIDE_BASE_PLATE_FLAG, hideBasePlate));
    }

    public boolean shouldShowBasePlate() {
        return (this.dataTracker.get(ARMOR_STAND_FLAGS) & 8) == 0;
    }

    private void setMarker(boolean marker) {
        this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(ARMOR_STAND_FLAGS), MARKER_FLAG, marker));
    }

    public boolean isMarker() {
        return (this.dataTracker.get(ARMOR_STAND_FLAGS) & 0x10) != 0;
    }

    private byte setBitField(byte value, int bitField, boolean set) {
        value = set ? (byte)(value | bitField) : (byte)(value & ~bitField);
        return value;
    }

    public void setHeadRotation(EulerAngle angle) {
        this.dataTracker.set(TRACKER_HEAD_ROTATION, angle);
    }

    public void setBodyRotation(EulerAngle angle) {
        this.dataTracker.set(TRACKER_BODY_ROTATION, angle);
    }

    public void setLeftArmRotation(EulerAngle angle) {
        this.dataTracker.set(TRACKER_LEFT_ARM_ROTATION, angle);
    }

    public void setRightArmRotation(EulerAngle angle) {
        this.dataTracker.set(TRACKER_RIGHT_ARM_ROTATION, angle);
    }

    public void setLeftLegRotation(EulerAngle angle) {
        this.dataTracker.set(TRACKER_LEFT_LEG_ROTATION, angle);
    }

    public void setRightLegRotation(EulerAngle angle) {
        this.dataTracker.set(TRACKER_RIGHT_LEG_ROTATION, angle);
    }

    public EulerAngle getHeadRotation() {
        return this.dataTracker.get(TRACKER_HEAD_ROTATION);
    }

    public EulerAngle getBodyRotation() {
        return this.dataTracker.get(TRACKER_BODY_ROTATION);
    }

    public EulerAngle getLeftArmRotation() {
        return this.dataTracker.get(TRACKER_LEFT_ARM_ROTATION);
    }

    public EulerAngle getRightArmRotation() {
        return this.dataTracker.get(TRACKER_RIGHT_ARM_ROTATION);
    }

    public EulerAngle getLeftLegRotation() {
        return this.dataTracker.get(TRACKER_LEFT_LEG_ROTATION);
    }

    public EulerAngle getRightLegRotation() {
        return this.dataTracker.get(TRACKER_RIGHT_LEG_ROTATION);
    }

    @Override
    public boolean canHit() {
        return super.canHit() && !this.isMarker();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean handleAttack(Entity attacker) {
        if (!(attacker instanceof PlayerEntity)) return false;
        PlayerEntity lv = (PlayerEntity)attacker;
        if (this.getEntityWorld().canEntityModifyAt(lv, this.getBlockPos())) return false;
        return true;
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public LivingEntity.FallSounds getFallSounds() {
        return new LivingEntity.FallSounds(SoundEvents.ENTITY_ARMOR_STAND_FALL, SoundEvents.ENTITY_ARMOR_STAND_FALL);
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ARMOR_STAND_HIT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ARMOR_STAND_BREAK;
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
    }

    @Override
    public boolean isAffectedBySplashPotions() {
        return false;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (ARMOR_STAND_FLAGS.equals(data)) {
            this.calculateDimensions();
            this.intersectionChecked = !this.isMarker();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    public boolean isMobOrPlayer() {
        return false;
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.getDimensions(this.isMarker());
    }

    private EntityDimensions getDimensions(boolean marker) {
        if (marker) {
            return MARKER_DIMENSIONS;
        }
        return this.isBaby() ? SMALL_DIMENSIONS : this.getType().getDimensions();
    }

    @Override
    public Vec3d getClientCameraPosVec(float tickProgress) {
        if (this.isMarker()) {
            Box lv = this.getDimensions(false).getBoxAt(this.getEntityPos());
            BlockPos lv2 = this.getBlockPos();
            int i = Integer.MIN_VALUE;
            for (BlockPos lv3 : BlockPos.iterate(BlockPos.ofFloored(lv.minX, lv.minY, lv.minZ), BlockPos.ofFloored(lv.maxX, lv.maxY, lv.maxZ))) {
                int j = Math.max(this.getEntityWorld().getLightLevel(LightType.BLOCK, lv3), this.getEntityWorld().getLightLevel(LightType.SKY, lv3));
                if (j == 15) {
                    return Vec3d.ofCenter(lv3);
                }
                if (j <= i) continue;
                i = j;
                lv2 = lv3.toImmutable();
            }
            return Vec3d.ofCenter(lv2);
        }
        return super.getClientCameraPosVec(tickProgress);
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(Items.ARMOR_STAND);
    }

    @Override
    public boolean isPartOfGame() {
        return !this.isInvisible() && !this.isMarker();
    }

    public void unpackRotation(PackedRotation packedRotation) {
        this.setHeadRotation(packedRotation.head());
        this.setBodyRotation(packedRotation.body());
        this.setLeftArmRotation(packedRotation.leftArm());
        this.setRightArmRotation(packedRotation.rightArm());
        this.setLeftLegRotation(packedRotation.leftLeg());
        this.setRightLegRotation(packedRotation.rightLeg());
    }

    public PackedRotation packRotation() {
        return new PackedRotation(this.getHeadRotation(), this.getBodyRotation(), this.getLeftArmRotation(), this.getRightArmRotation(), this.getLeftLegRotation(), this.getRightLegRotation());
    }

    public record PackedRotation(EulerAngle head, EulerAngle body, EulerAngle leftArm, EulerAngle rightArm, EulerAngle leftLeg, EulerAngle rightLeg) {
        public static final PackedRotation DEFAULT = new PackedRotation(DEFAULT_HEAD_ROTATION, DEFAULT_BODY_ROTATION, DEFAULT_LEFT_ARM_ROTATION, DEFAULT_RIGHT_ARM_ROTATION, DEFAULT_LEFT_LEG_ROTATION, DEFAULT_RIGHT_LEG_ROTATION);
        public static final Codec<PackedRotation> CODEC = RecordCodecBuilder.create(instance -> instance.group(EulerAngle.CODEC.optionalFieldOf("Head", DEFAULT_HEAD_ROTATION).forGetter(PackedRotation::head), EulerAngle.CODEC.optionalFieldOf("Body", DEFAULT_BODY_ROTATION).forGetter(PackedRotation::body), EulerAngle.CODEC.optionalFieldOf("LeftArm", DEFAULT_LEFT_ARM_ROTATION).forGetter(PackedRotation::leftArm), EulerAngle.CODEC.optionalFieldOf("RightArm", DEFAULT_RIGHT_ARM_ROTATION).forGetter(PackedRotation::rightArm), EulerAngle.CODEC.optionalFieldOf("LeftLeg", DEFAULT_LEFT_LEG_ROTATION).forGetter(PackedRotation::leftLeg), EulerAngle.CODEC.optionalFieldOf("RightLeg", DEFAULT_RIGHT_LEG_ROTATION).forGetter(PackedRotation::rightLeg)).apply((Applicative<PackedRotation, ?>)instance, PackedRotation::new));
    }
}

