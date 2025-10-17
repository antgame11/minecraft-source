/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.conversion;

import java.util.Set;
import java.util.UUID;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;

public enum EntityConversionType {
    SINGLE(true){

        @Override
        void setUpNewEntity(MobEntity oldEntity, MobEntity newEntity, EntityConversionContext context) {
            Entity lv3;
            Entity lv = oldEntity.getFirstPassenger();
            newEntity.copyPositionAndRotation(oldEntity);
            newEntity.setVelocity(oldEntity.getVelocity());
            if (lv != null) {
                lv.stopRiding();
                lv.ridingCooldown = 0;
                for (Entity entity : newEntity.getPassengerList()) {
                    entity.stopRiding();
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
                lv.startRiding(newEntity);
            }
            if ((lv3 = oldEntity.getVehicle()) != null) {
                oldEntity.stopRiding();
                newEntity.startRiding(lv3, false, false);
            }
            if (context.keepEquipment()) {
                for (EquipmentSlot lv4 : EquipmentSlot.VALUES) {
                    ItemStack lv5 = oldEntity.getEquippedStack(lv4);
                    if (lv5.isEmpty()) continue;
                    newEntity.equipStack(lv4, lv5.copyAndEmpty());
                    newEntity.setEquipmentDropChance(lv4, oldEntity.getEquipmentDropChances().get(lv4));
                }
            }
            newEntity.fallDistance = oldEntity.fallDistance;
            newEntity.setFlag(Entity.GLIDING_FLAG_INDEX, oldEntity.isGliding());
            newEntity.playerHitTimer = oldEntity.playerHitTimer;
            newEntity.hurtTime = oldEntity.hurtTime;
            newEntity.bodyYaw = oldEntity.bodyYaw;
            newEntity.setOnGround(oldEntity.isOnGround());
            oldEntity.getSleepingPosition().ifPresent(newEntity::setSleepingPosition);
            Entity entity = oldEntity.getLeashHolder();
            if (entity != null) {
                newEntity.attachLeash(entity, true);
            }
            this.copyData(oldEntity, newEntity, context);
        }
    }
    ,
    SPLIT_ON_DEATH(false){

        @Override
        void setUpNewEntity(MobEntity oldEntity, MobEntity newEntity, EntityConversionContext context) {
            Entity lv2;
            Entity lv = oldEntity.getFirstPassenger();
            if (lv != null) {
                lv.stopRiding();
            }
            if ((lv2 = oldEntity.getLeashHolder()) != null) {
                oldEntity.detachLeash();
            }
            this.copyData(oldEntity, newEntity, context);
        }
    };

    private static final Set<ComponentType<?>> CUSTOM_COMPONENTS;
    private final boolean discardOldEntity;

    EntityConversionType(boolean discardOldEntity) {
        this.discardOldEntity = discardOldEntity;
    }

    public boolean shouldDiscardOldEntity() {
        return this.discardOldEntity;
    }

    abstract void setUpNewEntity(MobEntity var1, MobEntity var2, EntityConversionContext var3);

    void copyData(MobEntity oldEntity, MobEntity newEntity, EntityConversionContext context) {
        ZombieEntity lv8;
        newEntity.setAbsorptionAmount(oldEntity.getAbsorptionAmount());
        for (StatusEffectInstance lv : oldEntity.getStatusEffects()) {
            newEntity.addStatusEffect(new StatusEffectInstance(lv));
        }
        if (oldEntity.isBaby()) {
            newEntity.setBaby(true);
        }
        if (oldEntity instanceof PassiveEntity) {
            PassiveEntity lv2 = (PassiveEntity)oldEntity;
            if (newEntity instanceof PassiveEntity) {
                PassiveEntity lv3 = (PassiveEntity)newEntity;
                lv3.setBreedingAge(lv2.getBreedingAge());
                lv3.forcedAge = lv2.forcedAge;
                lv3.happyTicksRemaining = lv2.happyTicksRemaining;
            }
        }
        Brain<UUID> lv4 = oldEntity.getBrain();
        Brain<?> lv5 = newEntity.getBrain();
        if (lv4.isMemoryInState(MemoryModuleType.ANGRY_AT, MemoryModuleState.REGISTERED) && lv4.hasMemoryModule(MemoryModuleType.ANGRY_AT)) {
            lv5.remember(MemoryModuleType.ANGRY_AT, lv4.getOptionalRegisteredMemory(MemoryModuleType.ANGRY_AT));
        }
        if (context.preserveCanPickUpLoot()) {
            newEntity.setCanPickUpLoot(oldEntity.canPickUpLoot());
        }
        newEntity.setLeftHanded(oldEntity.isLeftHanded());
        newEntity.setAiDisabled(oldEntity.isAiDisabled());
        if (oldEntity.isPersistent()) {
            newEntity.setPersistent();
        }
        newEntity.setCustomNameVisible(oldEntity.isCustomNameVisible());
        newEntity.setOnFire(oldEntity.isOnFire());
        newEntity.setInvulnerable(oldEntity.isInvulnerable());
        newEntity.setNoGravity(oldEntity.hasNoGravity());
        newEntity.setPortalCooldown(oldEntity.getPortalCooldown());
        newEntity.setSilent(oldEntity.isSilent());
        oldEntity.getCommandTags().forEach(newEntity::addCommandTag);
        for (ComponentType<?> lv6 : CUSTOM_COMPONENTS) {
            EntityConversionType.copyComponent(oldEntity, newEntity, lv6);
        }
        if (context.team() != null) {
            Scoreboard lv7 = newEntity.getEntityWorld().getScoreboard();
            lv7.addScoreHolderToTeam(newEntity.getUuidAsString(), context.team());
            if (oldEntity.getScoreboardTeam() != null && oldEntity.getScoreboardTeam() == context.team()) {
                lv7.removeScoreHolderFromTeam(oldEntity.getUuidAsString(), oldEntity.getScoreboardTeam());
            }
        }
        if (oldEntity instanceof ZombieEntity && (lv8 = (ZombieEntity)oldEntity).canBreakDoors() && newEntity instanceof ZombieEntity) {
            ZombieEntity lv9 = (ZombieEntity)newEntity;
            lv9.setCanBreakDoors(true);
        }
    }

    private static <T> void copyComponent(MobEntity oldEntity, MobEntity newEntity, ComponentType<T> type) {
        T object = oldEntity.get(type);
        if (object != null) {
            newEntity.setComponent(type, object);
        }
    }

    static {
        CUSTOM_COMPONENTS = Set.of(DataComponentTypes.CUSTOM_NAME, DataComponentTypes.CUSTOM_DATA);
    }
}

