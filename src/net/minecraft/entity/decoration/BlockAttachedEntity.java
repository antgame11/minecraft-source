/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.decoration;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BlockAttachedEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int attachCheckTimer;
    protected BlockPos attachedBlockPos;

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> arg, World arg2) {
        super(arg, arg2);
    }

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> type, World world, BlockPos attachedBlockPos) {
        this(type, world);
        this.attachedBlockPos = attachedBlockPos;
    }

    protected abstract void updateAttachmentPosition();

    @Override
    public void tick() {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            this.attemptTickInVoid();
            if (this.attachCheckTimer++ == 100) {
                this.attachCheckTimer = 0;
                if (!this.isRemoved() && !this.canStayAttached()) {
                    this.discard();
                    this.onBreak(lv, null);
                }
            }
        }
    }

    public abstract boolean canStayAttached();

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        if (attacker instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)attacker;
            if (!this.getEntityWorld().canEntityModifyAt(lv, this.attachedBlockPos)) {
                return true;
            }
            return this.sidedDamage(this.getDamageSources().playerAttack(lv), 0.0f);
        }
        return false;
    }

    @Override
    public boolean clientDamage(DamageSource source) {
        return !this.isAlwaysInvulnerableTo(source);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isAlwaysInvulnerableTo(source)) {
            return false;
        }
        if (!world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && source.getAttacker() instanceof MobEntity) {
            return false;
        }
        if (!this.isRemoved()) {
            this.kill(world);
            this.scheduleVelocityUpdate();
            this.onBreak(world, source.getAttacker());
        }
        return true;
    }

    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        Entity lv = explosion.getEntity();
        if (lv != null && lv.isTouchingWater()) {
            return true;
        }
        if (explosion.preservesDecorativeEntities()) {
            return super.isImmuneToExplosion(explosion);
        }
        return true;
    }

    @Override
    public void move(MovementType type, Vec3d movement) {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            if (!this.isRemoved() && movement.lengthSquared() > 0.0) {
                this.kill(lv);
                this.onBreak(lv, null);
            }
        }
    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            if (!this.isRemoved() && deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 0.0) {
                this.kill(lv);
                this.onBreak(lv, null);
            }
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.put("block_pos", BlockPos.CODEC, this.getAttachedBlockPos());
    }

    @Override
    protected void readCustomData(ReadView view) {
        BlockPos lv = view.read("block_pos", BlockPos.CODEC).orElse(null);
        if (lv == null || !lv.isWithinDistance(this.getBlockPos(), 16.0)) {
            LOGGER.error("Block-attached entity at invalid position: {}", (Object)lv);
            return;
        }
        this.attachedBlockPos = lv;
    }

    public abstract void onBreak(ServerWorld var1, @Nullable Entity var2);

    @Override
    protected boolean shouldSetPositionOnLoad() {
        return false;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.attachedBlockPos = BlockPos.ofFloored(x, y, z);
        this.updateAttachmentPosition();
        this.velocityDirty = true;
    }

    public BlockPos getAttachedBlockPos() {
        return this.attachedBlockPos;
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
    }

    @Override
    public void calculateDimensions() {
    }
}

