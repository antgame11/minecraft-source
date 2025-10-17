/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class ProjectileUtil {
    public static final float DEFAULT_MARGIN = 0.3f;

    public static HitResult getCollision(Entity entity, Predicate<Entity> predicate) {
        Vec3d lv = entity.getVelocity();
        World lv2 = entity.getEntityWorld();
        Vec3d lv3 = entity.getEntityPos();
        return ProjectileUtil.getCollision(lv3, entity, predicate, lv, lv2, ProjectileUtil.getToleranceMargin(entity), RaycastContext.ShapeType.COLLIDER);
    }

    public static HitResult getCollision(Entity entity, Predicate<Entity> predicate, RaycastContext.ShapeType raycastShapeType) {
        Vec3d lv = entity.getVelocity();
        World lv2 = entity.getEntityWorld();
        Vec3d lv3 = entity.getEntityPos();
        return ProjectileUtil.getCollision(lv3, entity, predicate, lv, lv2, ProjectileUtil.getToleranceMargin(entity), raycastShapeType);
    }

    public static HitResult getCollision(Entity entity, Predicate<Entity> predicate, double range) {
        Vec3d lv = entity.getRotationVec(0.0f).multiply(range);
        World lv2 = entity.getEntityWorld();
        Vec3d lv3 = entity.getEyePos();
        return ProjectileUtil.getCollision(lv3, entity, predicate, lv, lv2, 0.0f, RaycastContext.ShapeType.COLLIDER);
    }

    private static HitResult getCollision(Vec3d pos, Entity entity, Predicate<Entity> predicate, Vec3d velocity, World world, float margin, RaycastContext.ShapeType raycastShapeType) {
        EntityHitResult lv3;
        Vec3d lv = pos.add(velocity);
        HitResult lv2 = world.getCollisionsIncludingWorldBorder(new RaycastContext(pos, lv, raycastShapeType, RaycastContext.FluidHandling.NONE, entity));
        if (((HitResult)lv2).getType() != HitResult.Type.MISS) {
            lv = lv2.getPos();
        }
        if ((lv3 = ProjectileUtil.getEntityCollision(world, entity, pos, lv, entity.getBoundingBox().stretch(velocity).expand(1.0), predicate, margin)) != null) {
            lv2 = lv3;
        }
        return lv2;
    }

    @Nullable
    public static EntityHitResult raycast(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double maxDistance) {
        World lv = entity.getEntityWorld();
        double e = maxDistance;
        Entity lv2 = null;
        Vec3d lv3 = null;
        for (Entity lv4 : lv.getOtherEntities(entity, box, predicate)) {
            Vec3d lv6;
            double f;
            Box lv5 = lv4.getBoundingBox().expand(lv4.getTargetingMargin());
            Optional<Vec3d> optional = lv5.raycast(min, max);
            if (lv5.contains(min)) {
                if (!(e >= 0.0)) continue;
                lv2 = lv4;
                lv3 = optional.orElse(min);
                e = 0.0;
                continue;
            }
            if (!optional.isPresent() || !((f = min.squaredDistanceTo(lv6 = optional.get())) < e) && e != 0.0) continue;
            if (lv4.getRootVehicle() == entity.getRootVehicle()) {
                if (e != 0.0) continue;
                lv2 = lv4;
                lv3 = lv6;
                continue;
            }
            lv2 = lv4;
            lv3 = lv6;
            e = f;
        }
        if (lv2 == null) {
            return null;
        }
        return new EntityHitResult(lv2, lv3);
    }

    @Nullable
    public static EntityHitResult getEntityCollision(World world, ProjectileEntity projectile, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate) {
        return ProjectileUtil.getEntityCollision(world, projectile, min, max, box, predicate, ProjectileUtil.getToleranceMargin(projectile));
    }

    public static float getToleranceMargin(Entity entity) {
        return Math.max(0.0f, Math.min(0.3f, (float)(entity.age - 2) / 20.0f));
    }

    @Nullable
    public static EntityHitResult getEntityCollision(World world, Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, float margin) {
        double d = Double.MAX_VALUE;
        Optional<Object> optional = Optional.empty();
        Entity lv = null;
        for (Entity lv2 : world.getOtherEntities(entity, box, predicate)) {
            double e;
            Box lv3 = lv2.getBoundingBox().expand(margin);
            Optional<Vec3d> optional2 = lv3.raycast(min, max);
            if (!optional2.isPresent() || !((e = min.squaredDistanceTo(optional2.get())) < d)) continue;
            lv = lv2;
            d = e;
            optional = optional2;
        }
        if (lv == null) {
            return null;
        }
        return new EntityHitResult(lv, (Vec3d)optional.get());
    }

    public static void setRotationFromVelocity(Entity entity, float tickProgress) {
        Vec3d lv = entity.getVelocity();
        if (lv.lengthSquared() == 0.0) {
            return;
        }
        double d = lv.horizontalLength();
        entity.setYaw((float)(MathHelper.atan2(lv.z, lv.x) * 57.2957763671875) + 90.0f);
        entity.setPitch((float)(MathHelper.atan2(d, lv.y) * 57.2957763671875) - 90.0f);
        while (entity.getPitch() - entity.lastPitch < -180.0f) {
            entity.lastPitch -= 360.0f;
        }
        while (entity.getPitch() - entity.lastPitch >= 180.0f) {
            entity.lastPitch += 360.0f;
        }
        while (entity.getYaw() - entity.lastYaw < -180.0f) {
            entity.lastYaw -= 360.0f;
        }
        while (entity.getYaw() - entity.lastYaw >= 180.0f) {
            entity.lastYaw += 360.0f;
        }
        entity.setPitch(MathHelper.lerp(tickProgress, entity.lastPitch, entity.getPitch()));
        entity.setYaw(MathHelper.lerp(tickProgress, entity.lastYaw, entity.getYaw()));
    }

    public static Hand getHandPossiblyHolding(LivingEntity entity, Item item) {
        return entity.getMainHandStack().isOf(item) ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    public static PersistentProjectileEntity createArrowProjectile(LivingEntity entity, ItemStack stack, float damageModifier, @Nullable ItemStack bow) {
        ArrowItem lv = (ArrowItem)(stack.getItem() instanceof ArrowItem ? stack.getItem() : Items.ARROW);
        PersistentProjectileEntity lv2 = lv.createArrow(entity.getEntityWorld(), stack, entity, bow);
        lv2.applyDamageModifier(damageModifier);
        return lv2;
    }
}

