/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ShulkerBulletEntity
extends ProjectileEntity {
    private static final double field_30666 = 0.15;
    @Nullable
    private LazyEntityReference<Entity> target;
    @Nullable
    private Direction direction;
    private int stepCount;
    private double targetX;
    private double targetY;
    private double targetZ;

    public ShulkerBulletEntity(EntityType<? extends ShulkerBulletEntity> arg, World arg2) {
        super((EntityType<? extends ProjectileEntity>)arg, arg2);
        this.noClip = true;
    }

    public ShulkerBulletEntity(World world, LivingEntity owner, Entity target, Direction.Axis axis) {
        this((EntityType<? extends ShulkerBulletEntity>)EntityType.SHULKER_BULLET, world);
        this.setOwner(owner);
        Vec3d lv = owner.getBoundingBox().getCenter();
        this.refreshPositionAndAngles(lv.x, lv.y, lv.z, this.getYaw(), this.getPitch());
        this.target = LazyEntityReference.of(target);
        this.direction = Direction.UP;
        this.changeTargetDirection(axis, target);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (this.target != null) {
            view.put("Target", Uuids.INT_STREAM_CODEC, this.target.getUuid());
        }
        view.putNullable("Dir", Direction.INDEX_CODEC, this.direction);
        view.putInt("Steps", this.stepCount);
        view.putDouble("TXD", this.targetX);
        view.putDouble("TYD", this.targetY);
        view.putDouble("TZD", this.targetZ);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.stepCount = view.getInt("Steps", 0);
        this.targetX = view.getDouble("TXD", 0.0);
        this.targetY = view.getDouble("TYD", 0.0);
        this.targetZ = view.getDouble("TZD", 0.0);
        this.direction = view.read("Dir", Direction.INDEX_CODEC).orElse(null);
        this.target = LazyEntityReference.fromData(view, "Target");
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Nullable
    private Direction getDirection() {
        return this.direction;
    }

    private void setDirection(@Nullable Direction direction) {
        this.direction = direction;
    }

    private void changeTargetDirection(@Nullable Direction.Axis axis, @Nullable Entity target) {
        BlockPos lv;
        double d = 0.5;
        if (target == null) {
            lv = this.getBlockPos().down();
        } else {
            d = (double)target.getHeight() * 0.5;
            lv = BlockPos.ofFloored(target.getX(), target.getY() + d, target.getZ());
        }
        double e = (double)lv.getX() + 0.5;
        double f = (double)lv.getY() + d;
        double g = (double)lv.getZ() + 0.5;
        Direction lv2 = null;
        if (!lv.isWithinDistance(this.getEntityPos(), 2.0)) {
            BlockPos lv3 = this.getBlockPos();
            ArrayList<Direction> list = Lists.newArrayList();
            if (axis != Direction.Axis.X) {
                if (lv3.getX() < lv.getX() && this.getEntityWorld().isAir(lv3.east())) {
                    list.add(Direction.EAST);
                } else if (lv3.getX() > lv.getX() && this.getEntityWorld().isAir(lv3.west())) {
                    list.add(Direction.WEST);
                }
            }
            if (axis != Direction.Axis.Y) {
                if (lv3.getY() < lv.getY() && this.getEntityWorld().isAir(lv3.up())) {
                    list.add(Direction.UP);
                } else if (lv3.getY() > lv.getY() && this.getEntityWorld().isAir(lv3.down())) {
                    list.add(Direction.DOWN);
                }
            }
            if (axis != Direction.Axis.Z) {
                if (lv3.getZ() < lv.getZ() && this.getEntityWorld().isAir(lv3.south())) {
                    list.add(Direction.SOUTH);
                } else if (lv3.getZ() > lv.getZ() && this.getEntityWorld().isAir(lv3.north())) {
                    list.add(Direction.NORTH);
                }
            }
            lv2 = Direction.random(this.random);
            if (list.isEmpty()) {
                for (int i = 5; !this.getEntityWorld().isAir(lv3.offset(lv2)) && i > 0; --i) {
                    lv2 = Direction.random(this.random);
                }
            } else {
                lv2 = (Direction)list.get(this.random.nextInt(list.size()));
            }
            e = this.getX() + (double)lv2.getOffsetX();
            f = this.getY() + (double)lv2.getOffsetY();
            g = this.getZ() + (double)lv2.getOffsetZ();
        }
        this.setDirection(lv2);
        double h = e - this.getX();
        double j = f - this.getY();
        double k = g - this.getZ();
        double l = Math.sqrt(h * h + j * j + k * k);
        if (l == 0.0) {
            this.targetX = 0.0;
            this.targetY = 0.0;
            this.targetZ = 0.0;
        } else {
            this.targetX = h / l * 0.15;
            this.targetY = j / l * 0.15;
            this.targetZ = k / l * 0.15;
        }
        this.velocityDirty = true;
        this.stepCount = 10 + this.random.nextInt(5) * 10;
    }

    @Override
    public void checkDespawn() {
        if (this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        }
    }

    @Override
    protected double getGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        Vec3d lv3;
        super.tick();
        Entity lv = !this.getEntityWorld().isClient() ? LazyEntityReference.getEntity(this.target, this.getEntityWorld()) : null;
        HitResult lv2 = null;
        if (!this.getEntityWorld().isClient()) {
            if (lv == null) {
                this.target = null;
            }
            if (!(lv == null || !lv.isAlive() || lv instanceof PlayerEntity && lv.isSpectator())) {
                this.targetX = MathHelper.clamp(this.targetX * 1.025, -1.0, 1.0);
                this.targetY = MathHelper.clamp(this.targetY * 1.025, -1.0, 1.0);
                this.targetZ = MathHelper.clamp(this.targetZ * 1.025, -1.0, 1.0);
                lv3 = this.getVelocity();
                this.setVelocity(lv3.add((this.targetX - lv3.x) * 0.2, (this.targetY - lv3.y) * 0.2, (this.targetZ - lv3.z) * 0.2));
            } else {
                this.applyGravity();
            }
            lv2 = ProjectileUtil.getCollision(this, this::canHit);
        }
        lv3 = this.getVelocity();
        this.setPosition(this.getEntityPos().add(lv3));
        this.tickBlockCollision();
        if (this.portalManager != null && this.portalManager.isInPortal()) {
            this.tickPortalTeleportation();
        }
        if (lv2 != null && this.isAlive() && lv2.getType() != HitResult.Type.MISS) {
            this.hitOrDeflect(lv2);
        }
        ProjectileUtil.setRotationFromVelocity(this, 0.5f);
        if (this.getEntityWorld().isClient()) {
            this.getEntityWorld().addParticleClient(ParticleTypes.END_ROD, this.getX() - lv3.x, this.getY() - lv3.y + 0.15, this.getZ() - lv3.z, 0.0, 0.0, 0.0);
        } else if (lv != null) {
            if (this.stepCount > 0) {
                --this.stepCount;
                if (this.stepCount == 0) {
                    this.changeTargetDirection(this.direction == null ? null : this.direction.getAxis(), lv);
                }
            }
            if (this.direction != null) {
                BlockPos lv4 = this.getBlockPos();
                Direction.Axis lv5 = this.direction.getAxis();
                if (this.getEntityWorld().isTopSolid(lv4.offset(this.direction), this)) {
                    this.changeTargetDirection(lv5, lv);
                } else {
                    BlockPos lv6 = lv.getBlockPos();
                    if (lv5 == Direction.Axis.X && lv4.getX() == lv6.getX() || lv5 == Direction.Axis.Z && lv4.getZ() == lv6.getZ() || lv5 == Direction.Axis.Y && lv4.getY() == lv6.getY()) {
                        this.changeTargetDirection(lv5, lv);
                    }
                }
            }
        }
    }

    @Override
    protected boolean shouldTickBlockCollision() {
        return !this.isRemoved();
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && !entity.noClip;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 16384.0;
    }

    @Override
    public float getBrightnessAtEyes() {
        return 1.0f;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity lv = entityHitResult.getEntity();
        Entity lv2 = this.getOwner();
        LivingEntity lv3 = lv2 instanceof LivingEntity ? (LivingEntity)lv2 : null;
        DamageSource lv4 = this.getDamageSources().mobProjectile(this, lv3);
        boolean bl = lv.sidedDamage(lv4, 4.0f);
        if (bl) {
            World world = this.getEntityWorld();
            if (world instanceof ServerWorld) {
                ServerWorld lv5 = (ServerWorld)world;
                EnchantmentHelper.onTargetDamaged(lv5, lv, lv4);
            }
            if (lv instanceof LivingEntity) {
                LivingEntity lv6 = (LivingEntity)lv;
                lv6.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 200), MoreObjects.firstNonNull(lv2, this));
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        ((ServerWorld)this.getEntityWorld()).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
        this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HIT, 1.0f, 1.0f);
    }

    private void destroy() {
        this.discard();
        this.getEntityWorld().emitGameEvent(GameEvent.ENTITY_DAMAGE, this.getEntityPos(), GameEvent.Emitter.of(this));
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        this.destroy();
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean clientDamage(DamageSource source) {
        return true;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HURT, 1.0f, 1.0f);
        world.spawnParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2, 0.2, 0.2, 0.0);
        this.destroy();
        return true;
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.setVelocity(packet.getVelocity());
    }
}

