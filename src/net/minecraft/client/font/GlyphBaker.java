/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.BakedGlyphImpl;
import net.minecraft.client.font.GlyphAtlasTexture;
import net.minecraft.client.font.GlyphMetrics;
import net.minecraft.client.font.TextRenderLayerSet;
import net.minecraft.client.font.UploadableGlyph;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GlyphBaker
implements AutoCloseable {
    private final TextureManager textureManager;
    private final Identifier fontId;
    private final List<GlyphAtlasTexture> glyphAtlases = new ArrayList<GlyphAtlasTexture>();

    public GlyphBaker(TextureManager textureManager, Identifier fontId) {
        this.textureManager = textureManager;
        this.fontId = fontId;
    }

    public void clear() {
        int i = this.glyphAtlases.size();
        this.glyphAtlases.clear();
        for (int j = 0; j < i; ++j) {
            this.textureManager.destroyTexture(this.getAtlasId(j));
        }
    }

    @Override
    public void close() {
        this.clear();
    }

    @Nullable
    public BakedGlyphImpl bake(GlyphMetrics metrics, UploadableGlyph glyph) {
        for (GlyphAtlasTexture lv : this.glyphAtlases) {
            BakedGlyphImpl lv2 = lv.bake(metrics, glyph);
            if (lv2 == null) continue;
            return lv2;
        }
        int i = this.glyphAtlases.size();
        Identifier lv3 = this.getAtlasId(i);
        boolean bl = glyph.hasColor();
        TextRenderLayerSet lv4 = bl ? TextRenderLayerSet.of(lv3) : TextRenderLayerSet.ofIntensity(lv3);
        GlyphAtlasTexture lv5 = new GlyphAtlasTexture(lv3::toString, lv4, bl);
        this.glyphAtlases.add(lv5);
        this.textureManager.registerTexture(lv3, lv5);
        return lv5.bake(metrics, glyph);
    }

    private Identifier getAtlasId(int atlasIndex) {
        return this.fontId.withSuffixedPath("/" + atlasIndex);
    }
}

