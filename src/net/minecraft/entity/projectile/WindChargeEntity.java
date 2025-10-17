/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.AdvancedExplosionBehavior;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public class WindChargeEntity
extends AbstractWindChargeEntity {
    private static final ExplosionBehavior EXPLOSION_BEHAVIOR = new AdvancedExplosionBehavior(true, false, Optional.of(Float.valueOf(1.22f)), Registries.BLOCK.getOptional(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity()));
    private static final float EXPLOSION_POWER = 1.2f;
    private static final float MAX_RENDER_DISTANCE_WHEN_NEWLY_SPAWNED = MathHelper.square(3.5f);
    private int deflectCooldown = 5;

    public WindChargeEntity(EntityType<? extends AbstractWindChargeEntity> arg, World arg2) {
        super(arg, arg2);
    }

    public WindChargeEntity(PlayerEntity player, World world, double x, double y, double z) {
        super(EntityType.WIND_CHARGE, world, player, x, y, z);
    }

    public WindChargeEntity(World world, double x, double y, double z, Vec3d velocity) {
        super((EntityType<? extends AbstractWindChargeEntity>)EntityType.WIND_CHARGE, x, y, z, velocity, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.deflectCooldown > 0) {
            --this.deflectCooldown;
        }
    }

    @Override
    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity deflector, @Nullable LazyEntityReference<Entity> arg3, boolean fromAttack) {
        if (this.deflectCooldown > 0) {
            return false;
        }
        return super.deflect(deflection, deflector, arg3, fromAttack);
    }

    @Override
    protected void createExplosion(Vec3d pos) {
        this.getEntityWorld().createExplosion(this, null, EXPLOSION_BEHAVIOR, pos.getX(), pos.getY(), pos.getZ(), 1.2f, false, World.ExplosionSourceType.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, Pool.empty(), SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST);
    }

    @Override
    public boolean shouldRender(double distance) {
        if (this.age < 2 && distance < (double)MAX_RENDER_DISTANCE_WHEN_NEWLY_SPAWNED) {
            return false;
        }
        return super.shouldRender(distance);
    }
}

