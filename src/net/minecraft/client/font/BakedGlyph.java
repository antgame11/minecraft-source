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
import net.minecraft.client.font.GlyphMetrics;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface BakedGlyph {
    public GlyphMetrics getMetrics();

    @Nullable
    public TextDrawable create(float var1, float var2, int var3, int var4, Style var5, float var6, float var7);
}

