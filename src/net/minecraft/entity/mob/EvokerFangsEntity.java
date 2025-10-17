/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import java.util.List;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EvokerFangsEntity
extends Entity
implements Ownable {
    public static final int field_30662 = 20;
    public static final int field_30663 = 2;
    public static final int field_30664 = 14;
    private static final int DEFAULT_WARMUP = 0;
    private int warmup = 0;
    private boolean startedAttack;
    private int ticksLeft = 22;
    private boolean playingAnimation;
    @Nullable
    private LazyEntityReference<LivingEntity> owner;

    public EvokerFangsEntity(EntityType<? extends EvokerFangsEntity> arg, World arg2) {
        super(arg, arg2);
    }

    public EvokerFangsEntity(World world, double x, double y, double z, float yaw, int warmup, LivingEntity owner) {
        this((EntityType<? extends EvokerFangsEntity>)EntityType.EVOKER_FANGS, world);
        this.warmup = warmup;
        this.setOwner(owner);
        this.setYaw(yaw * 57.295776f);
        this.setPosition(x, y, z);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = LazyEntityReference.of(owner);
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        return LazyEntityReference.getLivingEntity(this.owner, this.getEntityWorld());
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.warmup = view.getInt("Warmup", 0);
        this.owner = LazyEntityReference.fromData(view, "Owner");
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putInt("Warmup", this.warmup);
        LazyEntityReference.writeData(this.owner, view, "Owner");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getEntityWorld().isClient()) {
            if (this.playingAnimation) {
                --this.ticksLeft;
                if (this.ticksLeft == 14) {
                    for (int i = 0; i < 12; ++i) {
                        double d = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getWidth() * 0.5;
                        double e = this.getY() + 0.05 + this.random.nextDouble();
                        double f = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getWidth() * 0.5;
                        double g = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        double h = 0.3 + this.random.nextDouble() * 0.3;
                        double j = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        this.getEntityWorld().addParticleClient(ParticleTypes.CRIT, d, e + 1.0, f, g, h, j);
                    }
                }
            }
        } else if (--this.warmup < 0) {
            if (this.warmup == -8) {
                List<LivingEntity> list = this.getEntityWorld().getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(0.2, 0.0, 0.2));
                for (LivingEntity lv : list) {
                    this.damage(lv);
                }
            }
            if (!this.startedAttack) {
                this.getEntityWorld().sendEntityStatus(this, EntityStatuses.PLAY_ATTACK_SOUND);
                this.startedAttack = true;
            }
            if (--this.ticksLeft < 0) {
                this.discard();
            }
        }
    }

    private void damage(LivingEntity target) {
        LivingEntity lv = this.getOwner();
        if (!target.isAlive() || target.isInvulnerable() || target == lv) {
            return;
        }
        if (lv == null) {
            target.serverDamage(this.getDamageSources().magic(), 6.0f);
        } else {
            ServerWorld lv3;
            if (lv.isTeammate(target)) {
                return;
            }
            DamageSource lv2 = this.getDamageSources().indirectMagic(this, lv);
            World world = this.getEntityWorld();
            if (world instanceof ServerWorld && target.damage(lv3 = (ServerWorld)world, lv2, 6.0f)) {
                EnchantmentHelper.onTargetDamaged(lv3, target, lv2);
            }
        }
    }

    @Override
    public void handleStatus(byte status) {
        super.handleStatus(status);
        if (status == EntityStatuses.PLAY_ATTACK_SOUND) {
            this.playingAnimation = true;
            if (!this.isSilent()) {
                this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_EVOKER_FANGS_ATTACK, this.getSoundCategory(), 1.0f, this.random.nextFloat() * 0.2f + 0.85f, false);
            }
        }
    }

    public float getAnimationProgress(float tickProgress) {
        if (!this.playingAnimation) {
            return 0.0f;
        }
        int i = this.ticksLeft - 2;
        if (i <= 0) {
            return 1.0f;
        }
        return 1.0f - ((float)i - tickProgress) / 20.0f;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    @Nullable
    public /* synthetic */ Entity getOwner() {
        return this.getOwner();
    }
}

