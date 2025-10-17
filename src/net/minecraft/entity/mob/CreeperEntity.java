/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import java.util.Collection;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.CreeperIgniteGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class CreeperEntity
extends HostileEntity {
    private static final TrackedData<Integer> FUSE_SPEED = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CHARGED = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IGNITED = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final boolean DEFAULT_CHARGED = false;
    private static final boolean DEFAULT_IGNITED = false;
    private static final short DEFAULT_FUSE = 30;
    private static final byte DEFAULT_EXPLOSION_RADIUS = 3;
    private int lastFuseTime;
    private int currentFuseTime;
    private int fuseTime = 30;
    private int explosionRadius = 3;
    private boolean headsDropped;

    public CreeperEntity(EntityType<? extends CreeperEntity> arg, World arg2) {
        super((EntityType<? extends HostileEntity>)arg, arg2);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new CreeperIgniteGoal(this));
        this.goalSelector.add(3, new FleeEntityGoal<OcelotEntity>(this, OcelotEntity.class, 6.0f, 1.0, 1.2));
        this.goalSelector.add(3, new FleeEntityGoal<CatEntity>(this, CatEntity.class, 6.0f, 1.0, 1.2));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<PlayerEntity>((MobEntity)this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this, new Class[0]));
    }

    public static DefaultAttributeContainer.Builder createCreeperAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public int getSafeFallDistance() {
        if (this.getTarget() == null) {
            return this.getSafeFallDistance(0.0f);
        }
        return this.getSafeFallDistance(this.getHealth() - 1.0f);
    }

    @Override
    public boolean handleFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        boolean bl = super.handleFallDamage(fallDistance, damagePerDistance, damageSource);
        this.currentFuseTime += (int)(fallDistance * 1.5);
        if (this.currentFuseTime > this.fuseTime - 5) {
            this.currentFuseTime = this.fuseTime - 5;
        }
        return bl;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(FUSE_SPEED, -1);
        builder.add(CHARGED, false);
        builder.add(IGNITED, false);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("powered", this.isCharged());
        view.putShort("Fuse", (short)this.fuseTime);
        view.putByte("ExplosionRadius", (byte)this.explosionRadius);
        view.putBoolean("ignited", this.isIgnited());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.dataTracker.set(CHARGED, view.getBoolean("powered", false));
        this.fuseTime = view.getShort("Fuse", (short)30);
        this.explosionRadius = view.getByte("ExplosionRadius", (byte)3);
        if (view.getBoolean("ignited", false)) {
            this.ignite();
        }
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            int i;
            this.lastFuseTime = this.currentFuseTime;
            if (this.isIgnited()) {
                this.setFuseSpeed(1);
            }
            if ((i = this.getFuseSpeed()) > 0 && this.currentFuseTime == 0) {
                this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0f, 0.5f);
                this.emitGameEvent(GameEvent.PRIME_FUSE);
            }
            this.currentFuseTime += i;
            if (this.currentFuseTime < 0) {
                this.currentFuseTime = 0;
            }
            if (this.currentFuseTime >= this.fuseTime) {
                this.currentFuseTime = this.fuseTime;
                this.explode();
            }
        }
        super.tick();
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof GoatEntity) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CREEPER_DEATH;
    }

    @Override
    public boolean onKilledOther(ServerWorld world, LivingEntity other, DamageSource damageSource) {
        if (this.shouldDropLoot(world) && this.isCharged() && !this.headsDropped) {
            other.generateLoot(world, damageSource, false, LootTables.ROOT_CHARGED_CREEPER, stack -> {
                other.dropStack(world, (ItemStack)stack);
                this.headsDropped = true;
            });
        }
        return super.onKilledOther(world, other, damageSource);
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        return true;
    }

    public boolean isCharged() {
        return this.dataTracker.get(CHARGED);
    }

    public float getLerpedFuseTime(float tickProgress) {
        return MathHelper.lerp(tickProgress, (float)this.lastFuseTime, (float)this.currentFuseTime) / (float)(this.fuseTime - 2);
    }

    public int getFuseSpeed() {
        return this.dataTracker.get(FUSE_SPEED);
    }

    public void setFuseSpeed(int fuseSpeed) {
        this.dataTracker.set(FUSE_SPEED, fuseSpeed);
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        super.onStruckByLightning(world, lightning);
        this.dataTracker.set(CHARGED, true);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (lv.isIn(ItemTags.CREEPER_IGNITERS)) {
            SoundEvent lv2 = lv.isOf(Items.FIRE_CHARGE) ? SoundEvents.ITEM_FIRECHARGE_USE : SoundEvents.ITEM_FLINTANDSTEEL_USE;
            this.getEntityWorld().playSound((Entity)player, this.getX(), this.getY(), this.getZ(), lv2, this.getSoundCategory(), 1.0f, this.random.nextFloat() * 0.4f + 0.8f);
            if (!this.getEntityWorld().isClient()) {
                this.ignite();
                if (!lv.isDamageable()) {
                    lv.decrement(1);
                } else {
                    lv.damage(1, (LivingEntity)player, hand.getEquipmentSlot());
                }
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    private void explode() {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            float f = this.isCharged() ? 2.0f : 1.0f;
            this.dead = true;
            lv.createExplosion(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionRadius * f, World.ExplosionSourceType.MOB);
            this.spawnEffectsCloud();
            this.onRemoval(lv, Entity.RemovalReason.KILLED);
            this.discard();
        }
    }

    private void spawnEffectsCloud() {
        Collection<StatusEffectInstance> collection = this.getStatusEffects();
        if (!collection.isEmpty()) {
            AreaEffectCloudEntity lv = new AreaEffectCloudEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ());
            lv.setRadius(2.5f);
            lv.setRadiusOnUse(-0.5f);
            lv.setWaitTime(10);
            lv.setDuration(300);
            lv.setPotionDurationScale(0.25f);
            lv.setRadiusGrowth(-lv.getRadius() / (float)lv.getDuration());
            for (StatusEffectInstance lv2 : collection) {
                lv.addEffect(new StatusEffectInstance(lv2));
            }
            this.getEntityWorld().spawnEntity(lv);
        }
    }

    public boolean isIgnited() {
        return this.dataTracker.get(IGNITED);
    }

    public void ignite() {
        this.dataTracker.set(IGNITED, true);
    }
}

