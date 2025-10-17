/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class EndLightFlashManager {
    public static final int field_62214 = 30;
    private static final int INTERVAL = 600;
    private static final int MAX_START_TIME = 200;
    private static final int MIN_DURATION = 100;
    private static final int field_62035 = 380;
    private long currentWindow;
    private int startTime;
    private int duration;
    private float nextSkyFactor;
    private float lastSkyFactor;
    private float pitch;
    private float yaw;

    public void tick(long time) {
        this.update(time);
        this.lastSkyFactor = this.nextSkyFactor;
        this.nextSkyFactor = this.calcSkyFactor(time);
    }

    private void update(long time) {
        long m = time / 600L;
        if (m != this.currentWindow) {
            Random lv = Random.create(m);
            lv.nextFloat();
            this.startTime = MathHelper.nextBetween(lv, 0, 200);
            this.duration = MathHelper.nextBetween(lv, 100, Math.min(380, 600 - this.startTime));
            this.pitch = MathHelper.nextBetween(lv, -60.0f, 10.0f);
            this.yaw = MathHelper.nextBetween(lv, -180.0f, 180.0f);
            this.currentWindow = m;
        }
    }

    private float calcSkyFactor(long time) {
        long m = time % 600L;
        if (m < (long)this.startTime || m > (long)(this.startTime + this.duration)) {
            return 0.0f;
        }
        return MathHelper.sin((float)(m - (long)this.startTime) * (float)Math.PI / (float)this.duration);
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getSkyFactor(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastSkyFactor, this.nextSkyFactor);
    }

    public boolean shouldFlash() {
        return this.nextSkyFactor > 0.0f && this.lastSkyFactor <= 0.0f;
    }
}

