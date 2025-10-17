/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile.thrown;

import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnderPearlEntity
extends ThrownItemEntity {
    private long chunkTicketExpiryTicks = 0L;

    public EnderPearlEntity(EntityType<? extends EnderPearlEntity> arg, World arg2) {
        super((EntityType<? extends ThrownItemEntity>)arg, arg2);
    }

    public EnderPearlEntity(World world, LivingEntity owner, ItemStack stack) {
        super(EntityType.ENDER_PEARL, owner, world, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void setOwner(@Nullable LazyEntityReference<Entity> owner) {
        this.removeFromOwner();
        super.setOwner(owner);
        this.addToOwner();
    }

    private void removeFromOwner() {
        Entity entity = this.getOwner();
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            lv.removeEnderPearl(this);
        }
    }

    private void addToOwner() {
        Entity entity = this.getOwner();
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            lv.addEnderPearl(this);
        }
    }

    @Override
    @Nullable
    public Entity getOwner() {
        World world;
        if (this.owner == null || !((world = this.getEntityWorld()) instanceof ServerWorld)) {
            return super.getOwner();
        }
        ServerWorld lv = (ServerWorld)world;
        return this.owner.getEntityByClass(lv, Entity.class);
    }

    @Nullable
    private static Entity getPlayer(ServerWorld world, UUID uuid) {
        Entity lv = world.getEntityAnyDimension(uuid);
        if (lv != null) {
            return lv;
        }
        return world.getServer().getPlayerManager().getPlayer(uuid);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        entityHitResult.getEntity().serverDamage(this.getDamageSources().thrown(this, this.getOwner()), 0.0f);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        ServerWorld lv;
        block14: {
            block13: {
                super.onCollision(hitResult);
                for (int i = 0; i < 32; ++i) {
                    this.getEntityWorld().addParticleClient(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian());
                }
                World world = this.getEntityWorld();
                if (!(world instanceof ServerWorld)) break block13;
                lv = (ServerWorld)world;
                if (!this.isRemoved()) break block14;
            }
            return;
        }
        Entity lv2 = this.getOwner();
        if (lv2 == null || !EnderPearlEntity.canTeleportEntityTo(lv2, lv)) {
            this.discard();
            return;
        }
        Vec3d lv3 = this.getLastRenderPos();
        if (lv2 instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv4 = (ServerPlayerEntity)lv2;
            if (lv4.networkHandler.isConnectionOpen()) {
                ServerPlayerEntity lv6;
                EndermiteEntity lv5;
                if (this.random.nextFloat() < 0.05f && lv.method_74962() && (lv5 = EntityType.ENDERMITE.create(lv, SpawnReason.TRIGGERED)) != null) {
                    lv5.refreshPositionAndAngles(lv2.getX(), lv2.getY(), lv2.getZ(), lv2.getYaw(), lv2.getPitch());
                    lv.spawnEntity(lv5);
                }
                if (this.hasPortalCooldown()) {
                    lv2.resetPortalCooldown();
                }
                if ((lv6 = lv4.teleportTo(new TeleportTarget(lv, lv3, Vec3d.ZERO, 0.0f, 0.0f, PositionFlag.combine(PositionFlag.ROT, PositionFlag.DELTA), TeleportTarget.NO_OP))) != null) {
                    lv6.onLanding();
                    lv6.clearCurrentExplosion();
                    lv6.damage(lv4.getEntityWorld(), this.getDamageSources().enderPearl(), 5.0f);
                }
                this.playTeleportSound(lv, lv3);
            }
        } else {
            Entity lv7 = lv2.teleportTo(new TeleportTarget(lv, lv3, lv2.getVelocity(), lv2.getYaw(), lv2.getPitch(), TeleportTarget.NO_OP));
            if (lv7 != null) {
                lv7.onLanding();
            }
            this.playTeleportSound(lv, lv3);
        }
        this.discard();
    }

    private static boolean canTeleportEntityTo(Entity entity, World world) {
        if (entity.getEntityWorld().getRegistryKey() == world.getRegistryKey()) {
            if (entity instanceof LivingEntity) {
                LivingEntity lv = (LivingEntity)entity;
                return lv.isAlive() && !lv.isSleeping();
            }
            return entity.isAlive();
        }
        return entity.canUsePortals(true);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void tick() {
        var2_1 = this.getEntityWorld();
        if (!(var2_1 instanceof ServerWorld)) {
            super.tick();
            return;
        }
        lv = (ServerWorld)var2_1;
        i = ChunkSectionPos.getSectionCoordFloored(this.getEntityPos().getX());
        j = ChunkSectionPos.getSectionCoordFloored(this.getEntityPos().getZ());
        v0 = lv2 = this.owner != null ? EnderPearlEntity.getPlayer(lv, this.owner.getUuid()) : null;
        if (!(lv2 instanceof ServerPlayerEntity)) ** GOTO lbl-1000
        lv3 = (ServerPlayerEntity)lv2;
        if (!lv2.isAlive() && !lv3.notInAnyWorld && lv3.getEntityWorld().getGameRules().getBoolean(GameRules.ENDER_PEARLS_VANISH_ON_DEATH)) {
            this.discard();
        } else lbl-1000:
        // 2 sources

        {
            super.tick();
        }
        if (!this.isAlive()) {
            return;
        }
        lv4 = BlockPos.ofFloored(this.getEntityPos());
        if ((--this.chunkTicketExpiryTicks <= 0L || i != ChunkSectionPos.getSectionCoord(lv4.getX()) || j != ChunkSectionPos.getSectionCoord(lv4.getZ())) && lv2 instanceof ServerPlayerEntity) {
            lv5 = (ServerPlayerEntity)lv2;
            this.chunkTicketExpiryTicks = lv5.handleThrownEnderPearl(this);
        }
    }

    private void playTeleportSound(World world, Vec3d pos) {
        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_PLAYER_TELEPORT, SoundCategory.PLAYERS);
    }

    @Override
    @Nullable
    public Entity teleportTo(TeleportTarget teleportTarget) {
        Entity lv = super.teleportTo(teleportTarget);
        if (lv != null) {
            lv.addPortalChunkTicketAt(BlockPos.ofFloored(lv.getEntityPos()));
        }
        return lv;
    }

    @Override
    public boolean canTeleportBetween(World from, World to) {
        Entity entity;
        if (from.getRegistryKey() == World.END && to.getRegistryKey() == World.OVERWORLD && (entity = this.getOwner()) instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            return super.canTeleportBetween(from, to) && lv.seenCredits;
        }
        return super.canTeleportBetween(from, to);
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        Entity entity;
        super.onBlockCollision(state);
        if (state.isOf(Blocks.END_GATEWAY) && (entity = this.getOwner()) instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            lv.onBlockCollision(state);
        }
    }

    @Override
    public void onRemove(Entity.RemovalReason reason) {
        if (reason != Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            this.removeFromOwner();
        }
        super.onRemove(reason);
    }

    @Override
    public void onBubbleColumnSurfaceCollision(boolean drag, BlockPos pos) {
        Entity.applyBubbleColumnSurfaceEffects(this, drag, pos);
    }

    @Override
    public void onBubbleColumnCollision(boolean drag) {
        Entity.applyBubbleColumnEffects(this, drag);
    }
}

