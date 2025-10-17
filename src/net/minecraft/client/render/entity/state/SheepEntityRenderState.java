/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.ColorLerper;
import net.minecraft.util.DyeColor;

@Environment(value=EnvType.CLIENT)
public class SheepEntityRenderState
extends LivingEntityRenderState {
    public float neckAngle;
    public float headAngle;
    public boolean sheared;
    public DyeColor color = DyeColor.WHITE;
    public boolean rainbow;

    public int getRgbColor() {
        if (this.rainbow) {
            return ColorLerper.lerpColor(ColorLerper.Type.SHEEP, this.age);
        }
        return ColorLerper.Type.SHEEP.getArgb(this.color);
    }
}

