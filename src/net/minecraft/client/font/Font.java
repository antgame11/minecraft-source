/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.FontFilterType;
import net.minecraft.client.font.Glyph;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface Font
extends AutoCloseable {
    public static final float DEFAULT_ASCENT = 7.0f;

    @Override
    default public void close() {
    }

    @Nullable
    default public Glyph getGlyph(int codePoint) {
        return null;
    }

    public IntSet getProvidedGlyphs();

    @Environment(value=EnvType.CLIENT)
    public record FontFilterPair(Font provider, FontFilterType.FilterMap filter) implements AutoCloseable
    {
        @Override
        public void close() {
            this.provider.close();
        }
    }
}

