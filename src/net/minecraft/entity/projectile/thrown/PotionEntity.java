/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile.thrown;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public abstract class PotionEntity
extends ThrownItemEntity {
    public static final double field_30667 = 4.0;
    protected static final double WATER_POTION_EXPLOSION_SQUARED_RADIUS = 16.0;
    public static final Predicate<LivingEntity> AFFECTED_BY_WATER = entity -> entity.hurtByWater() || entity.isOnFire();

    public PotionEntity(EntityType<? extends PotionEntity> arg, World arg2) {
        super((EntityType<? extends ThrownItemEntity>)arg, arg2);
    }

    public PotionEntity(EntityType<? extends PotionEntity> type, World world, LivingEntity owner, ItemStack stack) {
        super(type, owner, world, stack);
    }

    public PotionEntity(EntityType<? extends PotionEntity> type, World world, double x, double y, double z, ItemStack stack) {
        super(type, x, y, z, world, stack);
    }

    @Override
    protected double getGravity() {
        return 0.05;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (this.getEntityWorld().isClient()) {
            return;
        }
        ItemStack lv = this.getStack();
        Direction lv2 = blockHitResult.getSide();
        BlockPos lv3 = blockHitResult.getBlockPos();
        BlockPos lv4 = lv3.offset(lv2);
        PotionContentsComponent lv5 = lv.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        if (lv5.matches(Potions.WATER)) {
            this.extinguishFire(lv4);
            this.extinguishFire(lv4.offset(lv2.getOpposite()));
            for (Direction lv6 : Direction.Type.HORIZONTAL) {
                this.extinguishFire(lv4.offset(lv6));
            }
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld lv = (ServerWorld)world;
        ItemStack lv2 = this.getStack();
        PotionContentsComponent lv3 = lv2.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        if (lv3.matches(Potions.WATER)) {
            this.explodeWaterPotion(lv);
        } else if (lv3.hasEffects()) {
            this.spawnAreaEffectCloud(lv, lv2, hitResult);
        }
        int i = lv3.potion().isPresent() && lv3.potion().get().value().hasInstantEffect() ? WorldEvents.INSTANT_SPLASH_POTION_SPLASHED : WorldEvents.SPLASH_POTION_SPLASHED;
        lv.syncWorldEvent(i, this.getBlockPos(), lv3.getColor());
        this.discard();
    }

    private void explodeWaterPotion(ServerWorld world) {
        Box lv = this.getBoundingBox().expand(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.getEntityWorld().getEntitiesByClass(LivingEntity.class, lv, AFFECTED_BY_WATER);
        for (LivingEntity lv2 : list) {
            double d = this.squaredDistanceTo(lv2);
            if (!(d < 16.0)) continue;
            if (lv2.hurtByWater()) {
                lv2.damage(world, this.getDamageSources().indirectMagic(this, this.getOwner()), 1.0f);
            }
            if (!lv2.isOnFire() || !lv2.isAlive()) continue;
            lv2.extinguishWithSound();
        }
        List<AxolotlEntity> list2 = this.getEntityWorld().getNonSpectatingEntities(AxolotlEntity.class, lv);
        for (AxolotlEntity lv3 : list2) {
            lv3.hydrateFromPotion();
        }
    }

    protected abstract void spawnAreaEffectCloud(ServerWorld var1, ItemStack var2, HitResult var3);

    private void extinguishFire(BlockPos pos) {
        BlockState lv = this.getEntityWorld().getBlockState(pos);
        if (lv.isIn(BlockTags.FIRE)) {
            this.getEntityWorld().breakBlock(pos, false, this);
        } else if (AbstractCandleBlock.isLitCandle(lv)) {
            AbstractCandleBlock.extinguish(null, lv, this.getEntityWorld(), pos);
        } else if (CampfireBlock.isLitCampfire(lv)) {
            this.getEntityWorld().syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISHED, pos, 0);
            CampfireBlock.extinguish(this.getOwner(), this.getEntityWorld(), pos, lv);
            this.getEntityWorld().setBlockState(pos, (BlockState)lv.with(CampfireBlock.LIT, false));
        }
    }

    @Override
    public DoubleDoubleImmutablePair getKnockback(LivingEntity target, DamageSource source) {
        double d = target.getEntityPos().x - this.getEntityPos().x;
        double e = target.getEntityPos().z - this.getEntityPos().z;
        return DoubleDoubleImmutablePair.of(d, e);
    }
}

