/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class HappyGhastRidingSoundInstance
extends MovingSoundInstance {
    private static final float field_59985 = 0.0f;
    private static final float field_59986 = 1.0f;
    private final PlayerEntity player;
    private final HappyGhastEntity happyGhast;

    public HappyGhastRidingSoundInstance(PlayerEntity player, HappyGhastEntity happyGhast) {
        super(SoundEvents.ENTITY_HAPPY_GHAST_RIDING, happyGhast.getSoundCategory(), SoundInstance.createRandom());
        this.player = player;
        this.happyGhast = happyGhast;
        this.attenuationType = SoundInstance.AttenuationType.NONE;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.0f;
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    @Override
    public void tick() {
        if (this.happyGhast.isRemoved() || !this.player.hasVehicle() || this.player.getVehicle() != this.happyGhast) {
            this.setDone();
            return;
        }
        float f = (float)this.happyGhast.getVelocity().length();
        this.volume = f >= 0.01f ? 5.0f * MathHelper.clampedLerp(0.0f, 1.0f, f) : 0.0f;
    }
}

