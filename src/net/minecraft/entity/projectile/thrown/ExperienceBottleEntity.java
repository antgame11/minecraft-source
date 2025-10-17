/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile.thrown;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class ExperienceBottleEntity
extends ThrownItemEntity {
    public ExperienceBottleEntity(EntityType<? extends ExperienceBottleEntity> arg, World arg2) {
        super((EntityType<? extends ThrownItemEntity>)arg, arg2);
    }

    public ExperienceBottleEntity(World world, LivingEntity owner, ItemStack stack) {
        super(EntityType.EXPERIENCE_BOTTLE, owner, world, stack);
    }

    public ExperienceBottleEntity(World world, double x, double y, double z, ItemStack stack) {
        super(EntityType.EXPERIENCE_BOTTLE, x, y, z, world, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EXPERIENCE_BOTTLE;
    }

    @Override
    protected double getGravity() {
        return 0.07;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            lv.syncWorldEvent(WorldEvents.SPLASH_POTION_SPLASHED, this.getBlockPos(), -13083194);
            int i = 3 + lv.random.nextInt(5) + lv.random.nextInt(5);
            if (hitResult instanceof BlockHitResult) {
                BlockHitResult lv2 = (BlockHitResult)hitResult;
                Vec3d lv3 = lv2.getSide().getDoubleVector();
                ExperienceOrbEntity.spawn(lv, hitResult.getPos(), lv3, i);
            } else {
                ExperienceOrbEntity.spawn(lv, hitResult.getPos(), this.getVelocity().multiply(-1.0), i);
            }
            this.discard();
        }
    }
}

