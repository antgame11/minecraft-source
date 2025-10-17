/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.particle.DragonBreathParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class DragonFireballEntity
extends ExplosiveProjectileEntity {
    public static final float DAMAGE_RANGE = 4.0f;

    public DragonFireballEntity(EntityType<? extends DragonFireballEntity> arg, World arg2) {
        super((EntityType<? extends ExplosiveProjectileEntity>)arg, arg2);
    }

    public DragonFireballEntity(World world, LivingEntity owner, Vec3d velocity) {
        super(EntityType.DRAGON_FIREBALL, owner, velocity, world);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (hitResult.getType() == HitResult.Type.ENTITY && this.isOwner(((EntityHitResult)hitResult).getEntity())) {
            return;
        }
        if (!this.getEntityWorld().isClient()) {
            List<LivingEntity> list = this.getEntityWorld().getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(4.0, 2.0, 4.0));
            AreaEffectCloudEntity lv = new AreaEffectCloudEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ());
            Entity lv2 = this.getOwner();
            if (lv2 instanceof LivingEntity) {
                lv.setOwner((LivingEntity)lv2);
            }
            lv.setParticleType(DragonBreathParticleEffect.of(ParticleTypes.DRAGON_BREATH, 1.0f));
            lv.setRadius(3.0f);
            lv.setDuration(600);
            lv.setRadiusGrowth((7.0f - lv.getRadius()) / (float)lv.getDuration());
            lv.setPotionDurationScale(0.25f);
            lv.addEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1));
            if (!list.isEmpty()) {
                for (LivingEntity lv3 : list) {
                    double d = this.squaredDistanceTo(lv3);
                    if (!(d < 16.0)) continue;
                    lv.setPosition(lv3.getX(), lv3.getY(), lv3.getZ());
                    break;
                }
            }
            this.getEntityWorld().syncWorldEvent(WorldEvents.DRAGON_BREATH_CLOUD_SPAWNS, this.getBlockPos(), this.isSilent() ? -1 : 1);
            this.getEntityWorld().spawnEntity(lv);
            this.discard();
        }
    }

    @Override
    protected ParticleEffect getParticleType() {
        return DragonBreathParticleEffect.of(ParticleTypes.DRAGON_BREATH, 1.0f);
    }

    @Override
    protected boolean isBurning() {
        return false;
    }
}

