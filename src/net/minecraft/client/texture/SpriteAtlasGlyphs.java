/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.GlyphMetrics;
import net.minecraft.client.font.GlyphProvider;
import net.minecraft.client.font.SpriteGlyph;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderLayerSet;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.FixedGlyphProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class SpriteAtlasGlyphs {
    private static final float field_62130 = 8.0f;
    private static final float field_62131 = 8.0f;
    static final GlyphMetrics EMPTY_SPRITE_METRICS = GlyphMetrics.empty(8.0f);
    final SpriteAtlasTexture atlasTexture;
    final TextRenderLayerSet renderLayerSet;
    private final GlyphProvider missingGlyphProvider;
    private final Map<Identifier, GlyphProvider> cachedGlyphs = new HashMap<Identifier, GlyphProvider>();
    private final Function<Identifier, GlyphProvider> computeSprite;

    public SpriteAtlasGlyphs(SpriteAtlasTexture atlasTexture) {
        this.atlasTexture = atlasTexture;
        this.renderLayerSet = TextRenderLayerSet.of(atlasTexture.getId());
        Sprite lv = atlasTexture.getMissingSprite();
        this.missingGlyphProvider = this.createFixedGlyphProvider(lv);
        this.computeSprite = id -> {
            Sprite lv = atlasTexture.getSprite((Identifier)id);
            if (lv == lv) {
                return this.missingGlyphProvider;
            }
            return this.createFixedGlyphProvider(lv);
        };
    }

    public GlyphProvider getGlyphProvider(Identifier id) {
        return this.cachedGlyphs.computeIfAbsent(id, this.computeSprite);
    }

    private GlyphProvider createFixedGlyphProvider(final Sprite sprite) {
        return new FixedGlyphProvider(new BakedGlyph(){

            @Override
            public GlyphMetrics getMetrics() {
                return EMPTY_SPRITE_METRICS;
            }

            @Override
            public TextDrawable create(float x, float y, int color, int shadowColor, Style style, float boldOffset, float shadowOffset) {
                return new Glyph(SpriteAtlasGlyphs.this.renderLayerSet, SpriteAtlasGlyphs.this.atlasTexture.getGlTextureView(), sprite, x, y, color, shadowColor, shadowOffset);
            }
        });
    }

    @Environment(value=EnvType.CLIENT)
    record Glyph(TextRenderLayerSet renderTypes, GpuTextureView textureView, Sprite sprite, float x, float y, int color, int shadowColor, float shadowOffset) implements SpriteGlyph
    {
        @Override
        public void draw(Matrix4f matrix, VertexConsumer vertexConsumer, int light, float x, float y, float z, int color) {
            float k = x + this.getEffectiveMinX();
            float l = x + this.getEffectiveMaxX();
            float m = y + this.getEffectiveMinY();
            float n = y + this.getEffectiveMaxY();
            vertexConsumer.vertex(matrix, k, m, z).texture(this.sprite.getMinU(), this.sprite.getMinV()).color(color).light(light);
            vertexConsumer.vertex(matrix, k, n, z).texture(this.sprite.getMinU(), this.sprite.getMaxV()).color(color).light(light);
            vertexConsumer.vertex(matrix, l, n, z).texture(this.sprite.getMaxU(), this.sprite.getMaxV()).color(color).light(light);
            vertexConsumer.vertex(matrix, l, m, z).texture(this.sprite.getMaxU(), this.sprite.getMinV()).color(color).light(light);
        }

        @Override
        public RenderLayer getRenderLayer(TextRenderer.TextLayerType type) {
            return this.renderTypes.getRenderLayer(type);
        }

        @Override
        public RenderPipeline getPipeline() {
            return this.renderTypes.guiPipeline();
        }

        @Override
        public float getEffectiveMinX() {
            return 0.0f;
        }

        @Override
        public float getEffectiveMaxX() {
            return 8.0f;
        }

        @Override
        public float getEffectiveMinY() {
            return -1.0f;
        }

        @Override
        public float getEffectiveMaxY() {
            return 7.0f;
        }
    }
}

