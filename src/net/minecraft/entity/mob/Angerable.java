/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface Angerable {
    public static final String ANGER_TIME_KEY = "AngerTime";
    public static final String ANGRY_AT_KEY = "AngryAt";

    public int getAngerTime();

    public void setAngerTime(int var1);

    @Nullable
    public UUID getAngryAt();

    public void setAngryAt(@Nullable UUID var1);

    public void chooseRandomAngerTime();

    default public void writeAngerToData(WriteView view) {
        view.putInt(ANGER_TIME_KEY, this.getAngerTime());
        view.putNullable(ANGRY_AT_KEY, Uuids.INT_STREAM_CODEC, this.getAngryAt());
    }

    default public void readAngerFromData(World world, ReadView view) {
        Entity lv2;
        this.setAngerTime(view.getInt(ANGER_TIME_KEY, 0));
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld lv = (ServerWorld)world;
        UUID uUID = view.read(ANGRY_AT_KEY, Uuids.INT_STREAM_CODEC).orElse(null);
        this.setAngryAt(uUID);
        Entity entity = lv2 = uUID != null ? lv.getEntity(uUID) : null;
        if (lv2 instanceof LivingEntity) {
            LivingEntity lv3 = (LivingEntity)lv2;
            this.setTarget(lv3);
        }
    }

    default public void tickAngerLogic(ServerWorld world, boolean angerPersistent) {
        LivingEntity lv = this.getTarget();
        UUID uUID = this.getAngryAt();
        if ((lv == null || lv.isDead()) && uUID != null && world.getEntity(uUID) instanceof MobEntity) {
            this.stopAnger();
            return;
        }
        if (lv != null && !Objects.equals(uUID, lv.getUuid())) {
            this.setAngryAt(lv.getUuid());
            this.chooseRandomAngerTime();
        }
        if (!(this.getAngerTime() <= 0 || lv != null && lv.getType() == EntityType.PLAYER && angerPersistent)) {
            this.setAngerTime(this.getAngerTime() - 1);
            if (this.getAngerTime() == 0) {
                this.stopAnger();
            }
        }
    }

    default public boolean shouldAngerAt(LivingEntity entity, ServerWorld world) {
        if (!this.canTarget(entity)) {
            return false;
        }
        if (entity.getType() == EntityType.PLAYER && this.isUniversallyAngry(world)) {
            return true;
        }
        return entity.getUuid().equals(this.getAngryAt());
    }

    default public boolean isUniversallyAngry(ServerWorld world) {
        return world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER) && this.hasAngerTime() && this.getAngryAt() == null;
    }

    default public boolean hasAngerTime() {
        return this.getAngerTime() > 0;
    }

    default public void forgive(ServerWorld world, PlayerEntity player) {
        if (!world.getGameRules().getBoolean(GameRules.FORGIVE_DEAD_PLAYERS)) {
            return;
        }
        if (!player.getUuid().equals(this.getAngryAt())) {
            return;
        }
        this.stopAnger();
    }

    default public void universallyAnger() {
        this.stopAnger();
        this.chooseRandomAngerTime();
    }

    default public void stopAnger() {
        this.setAttacker(null);
        this.setAngryAt(null);
        this.setTarget(null);
        this.setAngerTime(0);
    }

    @Nullable
    public LivingEntity getAttacker();

    public void setAttacker(@Nullable LivingEntity var1);

    public void setTarget(@Nullable LivingEntity var1);

    public boolean canTarget(LivingEntity var1);

    @Nullable
    public LivingEntity getTarget();
}

