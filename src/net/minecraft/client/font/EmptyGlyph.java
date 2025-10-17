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
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphMetrics;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EmptyGlyph
implements Glyph {
    final GlyphMetrics glyph;

    public EmptyGlyph(float advance) {
        this.glyph = GlyphMetrics.empty(advance);
    }

    @Override
    public GlyphMetrics getMetrics() {
        return this.glyph;
    }

    @Override
    public BakedGlyph bake(Glyph.AbstractGlyphBaker baker) {
        return new BakedGlyph(){

            @Override
            public GlyphMetrics getMetrics() {
                return EmptyGlyph.this.glyph;
            }

            @Override
            @Nullable
            public TextDrawable create(float x, float y, int color, int shadowColor, Style style, float boldOffset, float shadowOffset) {
                return null;
            }
        };
    }
}

