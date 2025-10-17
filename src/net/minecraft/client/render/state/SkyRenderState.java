/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.DimensionEffects;

@Environment(value=EnvType.CLIENT)
public class SkyRenderState {
    public DimensionEffects.SkyType skyType = DimensionEffects.SkyType.NONE;
    public boolean isSunTransition;
    public boolean shouldRenderSkyDark;
    public float solarAngle;
    public float time;
    public float rainGradient;
    public float starBrightness;
    public int sunriseAndSunsetColor;
    public int moonPhase;
    public int skyColor;
    public float endFlashIntensity;
    public float endFlashPitch;
    public float endFlashYaw;

    public void clear() {
        this.skyType = DimensionEffects.SkyType.NONE;
    }
}

