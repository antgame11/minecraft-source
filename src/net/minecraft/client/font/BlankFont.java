/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.BuiltinEmptyGlyph;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphMetrics;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlankFont
implements Font {
    private static final Glyph MISSING = new Glyph(){

        @Override
        public GlyphMetrics getMetrics() {
            return BuiltinEmptyGlyph.MISSING;
        }

        @Override
        public BakedGlyph bake(Glyph.AbstractGlyphBaker baker) {
            return baker.getBlankGlyph();
        }
    };

    @Override
    @Nullable
    public Glyph getGlyph(int codePoint) {
        return MISSING;
    }

    @Override
    public IntSet getProvidedGlyphs() {
        return IntSets.EMPTY_SET;
    }
}

