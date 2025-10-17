/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile.thrown;

import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class SplashPotionEntity
extends PotionEntity {
    public SplashPotionEntity(EntityType<? extends SplashPotionEntity> arg, World arg2) {
        super((EntityType<? extends PotionEntity>)arg, arg2);
    }

    public SplashPotionEntity(World world, LivingEntity owner, ItemStack stack) {
        super(EntityType.SPLASH_POTION, world, owner, stack);
    }

    public SplashPotionEntity(World world, double x, double y, double z, ItemStack stack) {
        super(EntityType.SPLASH_POTION, world, x, y, z, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    public void spawnAreaEffectCloud(ServerWorld world, ItemStack stack, HitResult hitResult) {
        PotionContentsComponent lv = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        float f = stack.getOrDefault(DataComponentTypes.POTION_DURATION_SCALE, Float.valueOf(1.0f)).floatValue();
        Iterable<StatusEffectInstance> iterable = lv.getEffects();
        Box lv2 = this.getBoundingBox().offset(hitResult.getPos().subtract(this.getEntityPos()));
        Box lv3 = lv2.expand(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.getEntityWorld().getNonSpectatingEntities(LivingEntity.class, lv3);
        float g = ProjectileUtil.getToleranceMargin(this);
        if (!list.isEmpty()) {
            Entity lv4 = this.getEffectCause();
            for (LivingEntity lv5 : list) {
                double d;
                if (!lv5.isAffectedBySplashPotions() || !((d = lv2.squaredMagnitude(lv5.getBoundingBox().expand(g))) < 16.0)) continue;
                double e = 1.0 - Math.sqrt(d) / 4.0;
                for (StatusEffectInstance lv6 : iterable) {
                    RegistryEntry<StatusEffect> lv7 = lv6.getEffectType();
                    if (lv7.value().isInstant()) {
                        lv7.value().applyInstantEffect(world, this, this.getOwner(), lv5, lv6.getAmplifier(), e);
                        continue;
                    }
                    int i = lv6.mapDuration(baseDuration -> (int)(e * (double)baseDuration * (double)f + 0.5));
                    StatusEffectInstance lv8 = new StatusEffectInstance(lv7, i, lv6.getAmplifier(), lv6.isAmbient(), lv6.shouldShowParticles());
                    if (lv8.isDurationBelow(20)) continue;
                    lv5.addStatusEffect(lv8, lv4);
                }
            }
        }
    }
}

