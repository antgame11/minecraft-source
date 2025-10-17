/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.GlyphMetrics;
import net.minecraft.client.font.GlyphProvider;
import net.minecraft.client.font.SpriteGlyph;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.FixedGlyphProvider;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class PlayerHeadGlyphs {
    private static final float WIDTH = 8.0f;
    private static final float HEIGHT = 8.0f;
    static final GlyphMetrics EMPTY_SPRITE_METRICS = GlyphMetrics.empty(8.0f);
    final PlayerSkinCache playerSkinCache;
    private final LoadingCache<StyleSpriteSource.Player, GlyphProvider> fetchingCache = CacheBuilder.newBuilder().expireAfterAccess(PlayerSkinCache.TIME_TO_LIVE).build(new CacheLoader<StyleSpriteSource.Player, GlyphProvider>(){

        @Override
        public GlyphProvider load(StyleSpriteSource.Player arg) {
            final Supplier<PlayerSkinCache.Entry> supplier = PlayerHeadGlyphs.this.playerSkinCache.getSupplier(arg.profile());
            final boolean bl = arg.hat();
            return new FixedGlyphProvider(new BakedGlyph(){

                @Override
                public GlyphMetrics getMetrics() {
                    return EMPTY_SPRITE_METRICS;
                }

                @Override
                public TextDrawable create(float x, float y, int color, int shadowColor, Style style, float boldOffset, float shadowOffset) {
                    return new Glyph(supplier, bl, x, y, color, shadowColor, shadowOffset);
                }
            });
        }

        @Override
        public /* synthetic */ Object load(Object source) throws Exception {
            return this.load((StyleSpriteSource.Player)source);
        }
    });

    public PlayerHeadGlyphs(PlayerSkinCache playerSkinCache) {
        this.playerSkinCache = playerSkinCache;
    }

    public GlyphProvider get(StyleSpriteSource.Player source) {
        return this.fetchingCache.getUnchecked(source);
    }

    @Environment(value=EnvType.CLIENT)
    record Glyph(Supplier<PlayerSkinCache.Entry> skin, boolean hat, float x, float y, int color, int shadowColor, float shadowOffset) implements SpriteGlyph
    {
        @Override
        public void draw(Matrix4f matrix, VertexConsumer vertexConsumer, int light, float x, float y, float z, int color) {
            float k = x + this.getEffectiveMinX();
            float l = x + this.getEffectiveMaxX();
            float m = y + this.getEffectiveMinY();
            float n = y + this.getEffectiveMaxY();
            Glyph.drawInternal(matrix, vertexConsumer, light, k, l, m, n, z, color, 8.0f, 8.0f, 8, 8, 64, 64);
            if (this.hat) {
                Glyph.drawInternal(matrix, vertexConsumer, light, k, l, m, n, z, color, 40.0f, 8.0f, 8, 8, 64, 64);
            }
        }

        private static void drawInternal(Matrix4f matrix, VertexConsumer vertexConsumer, int light, float xMin, float xMax, float yMin, float yMax, float z, int color, float regionTop, float regionLeft, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
            float s = (regionTop + 0.0f) / (float)textureWidth;
            float t = (regionTop + (float)regionWidth) / (float)textureWidth;
            float u = (regionLeft + 0.0f) / (float)textureHeight;
            float v = (regionLeft + (float)regionHeight) / (float)textureHeight;
            vertexConsumer.vertex(matrix, xMin, yMin, z).texture(s, u).color(color).light(light);
            vertexConsumer.vertex(matrix, xMin, yMax, z).texture(s, v).color(color).light(light);
            vertexConsumer.vertex(matrix, xMax, yMax, z).texture(t, v).color(color).light(light);
            vertexConsumer.vertex(matrix, xMax, yMin, z).texture(t, u).color(color).light(light);
        }

        @Override
        public RenderLayer getRenderLayer(TextRenderer.TextLayerType type) {
            return this.skin.get().getTextRenderLayers().getRenderLayer(type);
        }

        @Override
        public RenderPipeline getPipeline() {
            return this.skin.get().getTextRenderLayers().guiPipeline();
        }

        @Override
        public GpuTextureView textureView() {
            return this.skin.get().getTextureView();
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

