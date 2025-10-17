/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class FireballEntity
extends AbstractFireballEntity {
    private static final byte DEFAULT_EXPLOSION_POWER = 1;
    private int explosionPower = 1;

    public FireballEntity(EntityType<? extends FireballEntity> arg, World arg2) {
        super((EntityType<? extends AbstractFireballEntity>)arg, arg2);
    }

    public FireballEntity(World world, LivingEntity owner, Vec3d velocity, int explosionPower) {
        super((EntityType<? extends AbstractFireballEntity>)EntityType.FIREBALL, owner, velocity, world);
        this.explosionPower = explosionPower;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            boolean bl = lv.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
            this.getEntityWorld().createExplosion((Entity)this, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, bl, World.ExplosionSourceType.MOB);
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld lv = (ServerWorld)world;
        Entity lv2 = entityHitResult.getEntity();
        Entity lv3 = this.getOwner();
        DamageSource lv4 = this.getDamageSources().fireball(this, lv3);
        lv2.damage(lv, lv4, 6.0f);
        EnchantmentHelper.onTargetDamaged(lv, lv2, lv4);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putByte("ExplosionPower", (byte)this.explosionPower);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.explosionPower = view.getByte("ExplosionPower", (byte)1);
    }
}

