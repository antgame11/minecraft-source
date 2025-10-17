/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import java.util.Optional;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class AnimalEntity
extends PassiveEntity {
    protected static final int BREEDING_COOLDOWN = 6000;
    private static final int DEFAULT_LOVE_TICKS = 0;
    private int loveTicks = 0;
    @Nullable
    private LazyEntityReference<ServerPlayerEntity> lovingPlayer;

    protected AnimalEntity(EntityType<? extends AnimalEntity> arg, World arg2) {
        super((EntityType<? extends PassiveEntity>)arg, arg2);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 16.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0f);
    }

    public static DefaultAttributeContainer.Builder createAnimalAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.TEMPT_RANGE, 10.0);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        if (this.getBreedingAge() != 0) {
            this.loveTicks = 0;
        }
        super.mobTick(world);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getBreedingAge() != 0) {
            this.loveTicks = 0;
        }
        if (this.loveTicks > 0) {
            --this.loveTicks;
            if (this.loveTicks % 10 == 0) {
                double d = this.random.nextGaussian() * 0.02;
                double e = this.random.nextGaussian() * 0.02;
                double f = this.random.nextGaussian() * 0.02;
                this.getEntityWorld().addParticleClient(ParticleTypes.HEART, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
            }
        }
    }

    @Override
    protected void applyDamage(ServerWorld world, DamageSource source, float amount) {
        this.resetLoveTicks();
        super.applyDamage(world, source, amount);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (world.getBlockState(pos.down()).isOf(Blocks.GRASS_BLOCK)) {
            return 10.0f;
        }
        return world.getPhototaxisFavor(pos);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("InLove", this.loveTicks);
        LazyEntityReference.writeData(this.lovingPlayer, view, "LoveCause");
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.loveTicks = view.getInt("InLove", 0);
        this.lovingPlayer = LazyEntityReference.fromData(view, "LoveCause");
    }

    public static boolean isValidNaturalSpawn(EntityType<? extends AnimalEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        boolean bl = SpawnReason.isTrialSpawner(spawnReason) || AnimalEntity.isLightLevelValidForNaturalSpawn(world, pos);
        return world.getBlockState(pos.down()).isIn(BlockTags.ANIMALS_SPAWNABLE_ON) && bl;
    }

    protected static boolean isLightLevelValidForNaturalSpawn(BlockRenderView world, BlockPos pos) {
        return world.getBaseLightLevel(pos, 0) > 8;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 120;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected int getExperienceToDrop(ServerWorld world) {
        return 1 + this.random.nextInt(3);
    }

    public abstract boolean isBreedingItem(ItemStack var1);

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (this.isBreedingItem(lv)) {
            int i = this.getBreedingAge();
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv2 = (ServerPlayerEntity)player;
                if (i == 0 && this.canEat()) {
                    this.eat(player, hand, lv);
                    this.lovePlayer(lv2);
                    this.playEatSound();
                    return ActionResult.SUCCESS_SERVER;
                }
            }
            if (this.isBaby()) {
                this.eat(player, hand, lv);
                this.growUp(AnimalEntity.toGrowUpAge(-i), true);
                this.playEatSound();
                return ActionResult.SUCCESS;
            }
            if (this.getEntityWorld().isClient()) {
                return ActionResult.CONSUME;
            }
        }
        return super.interactMob(player, hand);
    }

    protected void playEatSound() {
    }

    public boolean canEat() {
        return this.loveTicks <= 0;
    }

    public void lovePlayer(@Nullable PlayerEntity player) {
        this.loveTicks = 600;
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)player;
            this.lovingPlayer = LazyEntityReference.of(lv);
        }
        this.getEntityWorld().sendEntityStatus(this, EntityStatuses.ADD_BREEDING_PARTICLES);
    }

    public void setLoveTicks(int loveTicks) {
        this.loveTicks = loveTicks;
    }

    public int getLoveTicks() {
        return this.loveTicks;
    }

    @Nullable
    public ServerPlayerEntity getLovingPlayer() {
        return LazyEntityReference.resolve(this.lovingPlayer, this.getEntityWorld(), ServerPlayerEntity.class);
    }

    public boolean isInLove() {
        return this.loveTicks > 0;
    }

    public void resetLoveTicks() {
        this.loveTicks = 0;
    }

    public boolean canBreedWith(AnimalEntity other) {
        if (other == this) {
            return false;
        }
        if (other.getClass() != this.getClass()) {
            return false;
        }
        return this.isInLove() && other.isInLove();
    }

    public void breed(ServerWorld world, AnimalEntity other) {
        PassiveEntity lv = this.createChild(world, other);
        if (lv == null) {
            return;
        }
        lv.setBaby(true);
        lv.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), 0.0f, 0.0f);
        this.breed(world, other, lv);
        world.spawnEntityAndPassengers(lv);
    }

    public void breed(ServerWorld world, AnimalEntity other, @Nullable PassiveEntity baby) {
        Optional.ofNullable(this.getLovingPlayer()).or(() -> Optional.ofNullable(other.getLovingPlayer())).ifPresent(player -> {
            player.incrementStat(Stats.ANIMALS_BRED);
            Criteria.BRED_ANIMALS.trigger((ServerPlayerEntity)player, this, other, baby);
        });
        this.setBreedingAge(6000);
        other.setBreedingAge(6000);
        this.resetLoveTicks();
        other.resetLoveTicks();
        world.sendEntityStatus(this, EntityStatuses.ADD_BREEDING_PARTICLES);
        if (world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            world.spawnEntity(new ExperienceOrbEntity(world, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.ADD_BREEDING_PARTICLES) {
            for (int i = 0; i < 7; ++i) {
                double d = this.random.nextGaussian() * 0.02;
                double e = this.random.nextGaussian() * 0.02;
                double f = this.random.nextGaussian() * 0.02;
                this.getEntityWorld().addParticleClient(ParticleTypes.HEART, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
            }
        } else {
            super.handleStatus(status);
        }
    }
}

