/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile.thrown;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class LingeringPotionEntity
extends PotionEntity {
    public LingeringPotionEntity(EntityType<? extends LingeringPotionEntity> arg, World arg2) {
        super((EntityType<? extends PotionEntity>)arg, arg2);
    }

    public LingeringPotionEntity(World world, LivingEntity owner, ItemStack stack) {
        super(EntityType.LINGERING_POTION, world, owner, stack);
    }

    public LingeringPotionEntity(World world, double x, double y, double z, ItemStack stack) {
        super(EntityType.LINGERING_POTION, world, x, y, z, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.LINGERING_POTION;
    }

    @Override
    public void spawnAreaEffectCloud(ServerWorld world, ItemStack stack, HitResult hitResult) {
        AreaEffectCloudEntity lv = new AreaEffectCloudEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ());
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            LivingEntity lv2 = (LivingEntity)entity;
            lv.setOwner(lv2);
        }
        lv.setRadius(3.0f);
        lv.setRadiusOnUse(-0.5f);
        lv.setDuration(600);
        lv.setWaitTime(10);
        lv.setRadiusGrowth(-lv.getRadius() / (float)lv.getDuration());
        lv.copyComponentsFrom(stack);
        world.spawnEntity(lv);
    }
}

