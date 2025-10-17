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
import net.minecraft.client.render.Camera;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class EndLightFlashSoundInstance
extends MovingSoundInstance {
    private final Camera camera;
    private final float lightFlashPitch;
    private final float lightFlashYaw;

    public EndLightFlashSoundInstance(SoundEvent soundEvent, SoundCategory category, Random random, Camera camera, float rotationDegreesX, float rotationDegreesY) {
        super(soundEvent, category, random);
        this.camera = camera;
        this.lightFlashPitch = rotationDegreesX;
        this.lightFlashYaw = rotationDegreesY;
        this.update();
    }

    private void update() {
        Vec3d lv = Vec3d.fromPolar(this.lightFlashPitch, this.lightFlashYaw).multiply(10.0);
        this.x = this.camera.getPos().x + lv.x;
        this.y = this.camera.getPos().y + lv.y;
        this.z = this.camera.getPos().z + lv.z;
        this.attenuationType = SoundInstance.AttenuationType.NONE;
    }

    @Override
    public void tick() {
        this.update();
    }
}

