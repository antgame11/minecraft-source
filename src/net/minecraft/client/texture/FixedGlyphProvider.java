/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.GlyphProvider;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public record FixedGlyphProvider(BakedGlyph glyph) implements GlyphProvider
{
    @Override
    public BakedGlyph get(int codePoint) {
        return this.glyph;
    }

    @Override
    public BakedGlyph getObfuscated(Random random, int width) {
        return this.glyph;
    }
}

