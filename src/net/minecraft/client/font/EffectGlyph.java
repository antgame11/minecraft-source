/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextDrawable;

@Environment(value=EnvType.CLIENT)
public interface EffectGlyph {
    public TextDrawable create(float var1, float var2, float var3, float var4, float var5, int var6, int var7, float var8);
}

