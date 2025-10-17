/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class ProjectileEntity
extends Entity
implements Ownable {
    private static final boolean DEFAULT_LEFT_OWNER = false;
    private static final boolean DEFAULT_SHOT = false;
    @Nullable
    protected LazyEntityReference<Entity> owner;
    private boolean leftOwner = false;
    private boolean checkedForLeftOwner;
    private boolean shot = false;
    @Nullable
    private Entity lastDeflectedEntity;

    ProjectileEntity(EntityType<? extends ProjectileEntity> arg, World arg2) {
        super(arg, arg2);
    }

    protected void setOwner(@Nullable LazyEntityReference<Entity> owner) {
        this.owner = owner;
    }

    public void setOwner(@Nullable Entity owner) {
        this.setOwner(LazyEntityReference.of(owner));
    }

    @Override
    @Nullable
    public Entity getOwner() {
        return LazyEntityReference.getEntity(this.owner, this.getEntityWorld());
    }

    public Entity getEffectCause() {
        return MoreObjects.firstNonNull(this.getOwner(), this);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        LazyEntityReference.writeData(this.owner, view, "Owner");
        if (this.leftOwner) {
            view.putBoolean("LeftOwner", true);
        }
        view.putBoolean("HasBeenShot", this.shot);
    }

    protected boolean isOwner(Entity entity) {
        return this.owner != null && this.owner.uuidEquals(entity);
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setOwner(LazyEntityReference.fromData(view, "Owner"));
        this.leftOwner = view.getBoolean("LeftOwner", false);
        this.shot = view.getBoolean("HasBeenShot", false);
    }

    @Override
    public void copyFrom(Entity original) {
        super.copyFrom(original);
        if (original instanceof ProjectileEntity) {
            ProjectileEntity lv = (ProjectileEntity)original;
            this.owner = lv.owner;
        }
    }

    @Override
    public void tick() {
        if (!this.shot) {
            this.emitGameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.shot = true;
        }
        this.tickLeftOwner();
        super.tick();
        this.checkedForLeftOwner = false;
    }

    protected void tickLeftOwner() {
        if (!this.leftOwner && !this.checkedForLeftOwner) {
            this.leftOwner = this.hasLeftOwner();
            this.checkedForLeftOwner = true;
        }
    }

    private boolean hasLeftOwner() {
        Entity lv = this.getOwner();
        if (lv != null) {
            Box lv2 = this.getBoundingBox().stretch(this.getVelocity()).expand(1.0);
            return lv.getRootVehicle().streamSelfAndPassengers().filter(EntityPredicates.CAN_HIT).noneMatch(entity -> lv2.intersects(entity.getBoundingBox()));
        }
        return true;
    }

    public Vec3d calculateVelocity(double x, double y, double z, float power, float uncertainty) {
        return new Vec3d(x, y, z).normalize().add(this.random.nextTriangular(0.0, 0.0172275 * (double)uncertainty), this.random.nextTriangular(0.0, 0.0172275 * (double)uncertainty), this.random.nextTriangular(0.0, 0.0172275 * (double)uncertainty)).multiply(power);
    }

    public void setVelocity(double x, double y, double z, float power, float uncertainty) {
        Vec3d lv = this.calculateVelocity(x, y, z, power, uncertainty);
        this.setVelocity(lv);
        this.velocityDirty = true;
        double i = lv.horizontalLength();
        this.setYaw((float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875));
        this.setPitch((float)(MathHelper.atan2(lv.y, i) * 57.2957763671875));
        this.lastYaw = this.getYaw();
        this.lastPitch = this.getPitch();
    }

    public void setVelocity(Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
        float k = -MathHelper.sin(yaw * ((float)Math.PI / 180)) * MathHelper.cos(pitch * ((float)Math.PI / 180));
        float l = -MathHelper.sin((pitch + roll) * ((float)Math.PI / 180));
        float m = MathHelper.cos(yaw * ((float)Math.PI / 180)) * MathHelper.cos(pitch * ((float)Math.PI / 180));
        this.setVelocity(k, l, m, speed, divergence);
        Vec3d lv = shooter.getMovement();
        this.setVelocity(this.getVelocity().add(lv.x, shooter.isOnGround() ? 0.0 : lv.y, lv.z));
    }

    @Override
    public void onBubbleColumnSurfaceCollision(boolean drag, BlockPos pos) {
        double d = drag ? -0.03 : 0.1;
        this.setVelocity(this.getVelocity().add(0.0, d, 0.0));
        ProjectileEntity.spawnBubbleColumnParticles(this.getEntityWorld(), pos);
    }

    @Override
    public void onBubbleColumnCollision(boolean drag) {
        double d = drag ? -0.03 : 0.06;
        this.setVelocity(this.getVelocity().add(0.0, d, 0.0));
        this.onLanding();
    }

    public static <T extends ProjectileEntity> T spawnWithVelocity(ProjectileCreator<T> creator, ServerWorld world, ItemStack projectileStack, LivingEntity shooter, float roll, float power, float divergence) {
        return (T)ProjectileEntity.spawn(creator.create(world, shooter, projectileStack), world, projectileStack, entity -> entity.setVelocity(shooter, shooter.getPitch(), shooter.getYaw(), roll, power, divergence));
    }

    public static <T extends ProjectileEntity> T spawnWithVelocity(ProjectileCreator<T> creator, ServerWorld world, ItemStack projectileStack, LivingEntity shooter, double velocityX, double velocityY, double velocityZ, float power, float divergence) {
        return (T)ProjectileEntity.spawn(creator.create(world, shooter, projectileStack), world, projectileStack, entity -> entity.setVelocity(velocityX, velocityY, velocityZ, power, divergence));
    }

    public static <T extends ProjectileEntity> T spawnWithVelocity(T projectile, ServerWorld world, ItemStack projectileStack, double velocityX, double velocityY, double velocityZ, float power, float divergence) {
        return (T)ProjectileEntity.spawn(projectile, world, projectileStack, entity -> projectile.setVelocity(velocityX, velocityY, velocityZ, power, divergence));
    }

    public static <T extends ProjectileEntity> T spawn(T projectile, ServerWorld world, ItemStack projectileStack) {
        return (T)ProjectileEntity.spawn(projectile, world, projectileStack, entity -> {});
    }

    public static <T extends ProjectileEntity> T spawn(T projectile, ServerWorld world, ItemStack projectileStack, Consumer<T> beforeSpawn) {
        beforeSpawn.accept(projectile);
        world.spawnEntity(projectile);
        projectile.triggerProjectileSpawned(world, projectileStack);
        return projectile;
    }

    public void triggerProjectileSpawned(ServerWorld world, ItemStack projectileStack) {
        PersistentProjectileEntity lv;
        ItemStack lv2;
        EnchantmentHelper.onProjectileSpawned(world, projectileStack, this, item -> {});
        ProjectileEntity projectileEntity = this;
        if (projectileEntity instanceof PersistentProjectileEntity && (lv2 = (lv = (PersistentProjectileEntity)projectileEntity).getWeaponStack()) != null && !lv2.isEmpty() && !projectileStack.getItem().equals(lv2.getItem())) {
            EnchantmentHelper.onProjectileSpawned(world, lv2, this, lv::onBroken);
        }
    }

    protected ProjectileDeflection hitOrDeflect(HitResult hitResult) {
        ProjectileDeflection lv5;
        BlockHitResult lv4;
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult lv = (EntityHitResult)hitResult;
            Entity lv2 = lv.getEntity();
            ProjectileDeflection lv3 = lv2.getProjectileDeflection(this);
            if (lv3 != ProjectileDeflection.NONE) {
                if (lv2 != this.lastDeflectedEntity && this.deflect(lv3, lv2, this.owner, false)) {
                    this.lastDeflectedEntity = lv2;
                }
                return lv3;
            }
        } else if (this.deflectsAgainstWorldBorder() && hitResult instanceof BlockHitResult && (lv4 = (BlockHitResult)hitResult).isAgainstWorldBorder() && this.deflect(lv5 = ProjectileDeflection.SIMPLE, null, this.owner, false)) {
            this.setVelocity(this.getVelocity().multiply(0.2));
            return lv5;
        }
        this.onCollision(hitResult);
        return ProjectileDeflection.NONE;
    }

    protected boolean deflectsAgainstWorldBorder() {
        return false;
    }

    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity deflector, @Nullable LazyEntityReference<Entity> arg3, boolean fromAttack) {
        deflection.deflect(this, deflector, this.random);
        if (!this.getEntityWorld().isClient()) {
            this.setOwner(arg3);
            this.onDeflected(fromAttack);
        }
        return true;
    }

    protected void onDeflected(boolean bl) {
    }

    protected void onBroken(Item item) {
    }

    protected void onCollision(HitResult hitResult) {
        HitResult.Type lv = hitResult.getType();
        if (lv == HitResult.Type.ENTITY) {
            EntityHitResult lv2 = (EntityHitResult)hitResult;
            Entity lv3 = lv2.getEntity();
            if (lv3.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE) && lv3 instanceof ProjectileEntity) {
                ProjectileEntity lv4 = (ProjectileEntity)lv3;
                lv4.deflect(ProjectileDeflection.REDIRECTED, this.getOwner(), this.owner, true);
            }
            this.onEntityHit(lv2);
            this.getEntityWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), GameEvent.Emitter.of(this, null));
        } else if (lv == HitResult.Type.BLOCK) {
            BlockHitResult lv5 = (BlockHitResult)hitResult;
            this.onBlockHit(lv5);
            BlockPos lv6 = lv5.getBlockPos();
            this.getEntityWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, lv6, GameEvent.Emitter.of(this, this.getEntityWorld().getBlockState(lv6)));
        }
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
    }

    protected void onBlockHit(BlockHitResult blockHitResult) {
        BlockState lv = this.getEntityWorld().getBlockState(blockHitResult.getBlockPos());
        lv.onProjectileHit(this.getEntityWorld(), lv, blockHitResult, this);
    }

    protected boolean canHit(Entity entity) {
        if (!entity.canBeHitByProjectile()) {
            return false;
        }
        Entity lv = this.getOwner();
        return lv == null || this.leftOwner || !lv.isConnectedThroughVehicle(entity);
    }

    protected void updateRotation() {
        Vec3d lv = this.getVelocity();
        double d = lv.horizontalLength();
        this.setPitch(ProjectileEntity.updateRotation(this.lastPitch, (float)(MathHelper.atan2(lv.y, d) * 57.2957763671875)));
        this.setYaw(ProjectileEntity.updateRotation(this.lastYaw, (float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875)));
    }

    protected static float updateRotation(float lastRot, float newRot) {
        while (newRot - lastRot < -180.0f) {
            lastRot -= 360.0f;
        }
        while (newRot - lastRot >= 180.0f) {
            lastRot += 360.0f;
        }
        return MathHelper.lerp(0.2f, lastRot, newRot);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        Entity lv = this.getOwner();
        return new EntitySpawnS2CPacket((Entity)this, entityTrackerEntry, lv == null ? 0 : lv.getId());
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        Entity lv = this.getEntityWorld().getEntityById(packet.getEntityData());
        if (lv != null) {
            this.setOwner(lv);
        }
    }

    @Override
    public boolean canModifyAt(ServerWorld world, BlockPos pos) {
        Entity lv = this.getOwner();
        if (lv instanceof PlayerEntity) {
            return lv.canModifyAt(world, pos);
        }
        return lv == null || world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
    }

    public boolean canBreakBlocks(ServerWorld world) {
        return this.getType().isIn(EntityTypeTags.IMPACT_PROJECTILES) && world.getGameRules().getBoolean(GameRules.PROJECTILES_CAN_BREAK_BLOCKS);
    }

    @Override
    public boolean canHit() {
        return this.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    @Override
    public float getTargetingMargin() {
        return this.canHit() ? 1.0f : 0.0f;
    }

    public DoubleDoubleImmutablePair getKnockback(LivingEntity target, DamageSource source) {
        double d = this.getVelocity().x;
        double e = this.getVelocity().z;
        return DoubleDoubleImmutablePair.of(d, e);
    }

    @Override
    public int getDefaultPortalCooldown() {
        return 2;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (!this.isAlwaysInvulnerableTo(source)) {
            this.scheduleVelocityUpdate();
        }
        return false;
    }

    @FunctionalInterface
    public static interface ProjectileCreator<T extends ProjectileEntity> {
        public T create(ServerWorld var1, LivingEntity var2, ItemStack var3);
    }
}

