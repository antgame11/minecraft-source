/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.EffectGlyph;
import net.minecraft.client.font.GlyphMetrics;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderLayerSet;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Style;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class BakedGlyphImpl
implements BakedGlyph,
EffectGlyph {
    public static final float Z_OFFSET = 0.001f;
    private final GlyphMetrics glyph;
    final TextRenderLayerSet textRenderLayers;
    final GpuTextureView textureView;
    private final float minU;
    private final float maxU;
    private final float minV;
    private final float maxV;
    private final float minX;
    private final float maxX;
    private final float minY;
    private final float maxY;

    public BakedGlyphImpl(GlyphMetrics glyph, TextRenderLayerSet textRenderLayers, GpuTextureView textureView, float minU, float maxU, float minV, float maxV, float minX, float maxX, float minY, float maxY) {
        this.glyph = glyph;
        this.textRenderLayers = textRenderLayers;
        this.textureView = textureView;
        this.minU = minU;
        this.maxU = maxU;
        this.minV = minV;
        this.maxV = maxV;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    float getEffectiveMinX(DrawnGlyph glyph) {
        return glyph.x + this.minX + (glyph.style.isItalic() ? Math.min(this.getItalicOffsetAtMinY(), this.getItalicOffsetAtMaxY()) : 0.0f) - BakedGlyphImpl.getXExpansion(glyph.style.isBold());
    }

    float getEffectiveMinY(DrawnGlyph glyph) {
        return glyph.y + this.minY - BakedGlyphImpl.getXExpansion(glyph.style.isBold());
    }

    float getEffectiveMaxX(DrawnGlyph glyph) {
        return glyph.x + this.maxX + (glyph.hasShadow() ? glyph.shadowOffset : 0.0f) + (glyph.style.isItalic() ? Math.max(this.getItalicOffsetAtMinY(), this.getItalicOffsetAtMaxY()) : 0.0f) + BakedGlyphImpl.getXExpansion(glyph.style.isBold());
    }

    float getEffectiveMaxY(DrawnGlyph glyph) {
        return glyph.y + this.maxY + (glyph.hasShadow() ? glyph.shadowOffset : 0.0f) + BakedGlyphImpl.getXExpansion(glyph.style.isBold());
    }

    void draw(DrawnGlyph glyph, Matrix4f matrix, VertexConsumer vertexConsumer, int light, boolean fixedZ) {
        float l;
        float h;
        Style lv = glyph.style();
        boolean bl2 = lv.isItalic();
        float f = glyph.x();
        float g = glyph.y();
        int j = glyph.color();
        boolean bl3 = lv.isBold();
        float f2 = h = fixedZ ? 0.0f : 0.001f;
        if (glyph.hasShadow()) {
            int k = glyph.shadowColor();
            this.draw(bl2, f + glyph.shadowOffset(), g + glyph.shadowOffset(), 0.0f, matrix, vertexConsumer, k, bl3, light);
            if (bl3) {
                this.draw(bl2, f + glyph.boldOffset() + glyph.shadowOffset(), g + glyph.shadowOffset(), h, matrix, vertexConsumer, k, true, light);
            }
            l = fixedZ ? 0.0f : 0.03f;
        } else {
            l = 0.0f;
        }
        this.draw(bl2, f, g, l, matrix, vertexConsumer, j, bl3, light);
        if (bl3) {
            this.draw(bl2, f + glyph.boldOffset(), g, l + h, matrix, vertexConsumer, j, true, light);
        }
    }

    private void draw(boolean italic, float x, float y, float z, Matrix4f matrix, VertexConsumer vertexConsumer, int color, boolean bold, int light) {
        float k = x + this.minX;
        float l = x + this.maxX;
        float m = y + this.minY;
        float n = y + this.maxY;
        float o = italic ? this.getItalicOffsetAtMinY() : 0.0f;
        float p = italic ? this.getItalicOffsetAtMaxY() : 0.0f;
        float q = BakedGlyphImpl.getXExpansion(bold);
        vertexConsumer.vertex(matrix, k + o - q, m - q, z).color(color).texture(this.minU, this.minV).light(light);
        vertexConsumer.vertex(matrix, k + p - q, n + q, z).color(color).texture(this.minU, this.maxV).light(light);
        vertexConsumer.vertex(matrix, l + p + q, n + q, z).color(color).texture(this.maxU, this.maxV).light(light);
        vertexConsumer.vertex(matrix, l + o + q, m - q, z).color(color).texture(this.maxU, this.minV).light(light);
    }

    private static float getXExpansion(boolean bold) {
        return bold ? 0.1f : 0.0f;
    }

    private float getItalicOffsetAtMaxY() {
        return 1.0f - 0.25f * this.maxY;
    }

    private float getItalicOffsetAtMinY() {
        return 1.0f - 0.25f * this.minY;
    }

    void drawRectangle(Rectangle rectangle, Matrix4f matrix, VertexConsumer vertexConsumer, int light, boolean fixedZ) {
        float f;
        float f2 = f = fixedZ ? 0.0f : rectangle.zIndex;
        if (rectangle.hasShadow()) {
            this.drawRectangle(rectangle, rectangle.shadowOffset(), f, rectangle.shadowColor(), vertexConsumer, light, matrix);
            f += fixedZ ? 0.0f : 0.03f;
        }
        this.drawRectangle(rectangle, 0.0f, f, rectangle.color, vertexConsumer, light, matrix);
    }

    private void drawRectangle(Rectangle rectangle, float shadowOffset, float zOffset, int color, VertexConsumer vertexConsumer, int light, Matrix4f matrix) {
        vertexConsumer.vertex(matrix, rectangle.minX + shadowOffset, rectangle.maxY + shadowOffset, zOffset).color(color).texture(this.minU, this.minV).light(light);
        vertexConsumer.vertex(matrix, rectangle.maxX + shadowOffset, rectangle.maxY + shadowOffset, zOffset).color(color).texture(this.minU, this.maxV).light(light);
        vertexConsumer.vertex(matrix, rectangle.maxX + shadowOffset, rectangle.minY + shadowOffset, zOffset).color(color).texture(this.maxU, this.maxV).light(light);
        vertexConsumer.vertex(matrix, rectangle.minX + shadowOffset, rectangle.minY + shadowOffset, zOffset).color(color).texture(this.maxU, this.minV).light(light);
    }

    @Override
    public GlyphMetrics getMetrics() {
        return this.glyph;
    }

    @Override
    public TextDrawable create(float x, float y, int color, int shadowColor, Style style, float boldOffset, float shadowOffset) {
        return new DrawnGlyph(x, y, color, shadowColor, this, style, boldOffset, shadowOffset);
    }

    @Override
    public TextDrawable create(float minX, float minY, float maxX, float maxY, float depth, int color, int shadowColor, float shadowOffset) {
        return new Rectangle(this, minX, minY, maxX, maxY, depth, color, shadowColor, shadowOffset);
    }

    @Environment(value=EnvType.CLIENT)
    record DrawnGlyph(float x, float y, int color, int shadowColor, BakedGlyphImpl glyph, Style style, float boldOffset, float shadowOffset) implements TextDrawable
    {
        @Override
        public float getEffectiveMinX() {
            return this.glyph.getEffectiveMinX(this);
        }

        @Override
        public float getEffectiveMinY() {
            return this.glyph.getEffectiveMinY(this);
        }

        @Override
        public float getEffectiveMaxX() {
            return this.glyph.getEffectiveMaxX(this);
        }

        @Override
        public float getEffectiveMaxY() {
            return this.glyph.getEffectiveMaxY(this);
        }

        boolean hasShadow() {
            return this.shadowColor() != 0;
        }

        @Override
        public void render(Matrix4f matrix4f, VertexConsumer consumer, int light, boolean noDepth) {
            this.glyph.draw(this, matrix4f, consumer, light, noDepth);
        }

        @Override
        public RenderLayer getRenderLayer(TextRenderer.TextLayerType type) {
            return this.glyph.textRenderLayers.getRenderLayer(type);
        }

        @Override
        public GpuTextureView textureView() {
            return this.glyph.textureView;
        }

        @Override
        public RenderPipeline getPipeline() {
            return this.glyph.textRenderLayers.guiPipeline();
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Rectangle(BakedGlyphImpl glyph, float minX, float minY, float maxX, float maxY, float zIndex, int color, int shadowColor, float shadowOffset) implements TextDrawable
    {
        @Override
        public float getEffectiveMinX() {
            return this.minX;
        }

        @Override
        public float getEffectiveMinY() {
            return this.minY;
        }

        @Override
        public float getEffectiveMaxX() {
            return this.maxX + (this.hasShadow() ? this.shadowOffset : 0.0f);
        }

        @Override
        public float getEffectiveMaxY() {
            return this.maxY + (this.hasShadow() ? this.shadowOffset : 0.0f);
        }

        boolean hasShadow() {
            return this.shadowColor() != 0;
        }

        @Override
        public void render(Matrix4f matrix4f, VertexConsumer consumer, int light, boolean noDepth) {
            this.glyph.drawRectangle(this, matrix4f, consumer, light, false);
        }

        @Override
        public RenderLayer getRenderLayer(TextRenderer.TextLayerType type) {
            return this.glyph.textRenderLayers.getRenderLayer(type);
        }

        @Override
        public GpuTextureView textureView() {
            return this.glyph.textureView;
        }

        @Override
        public RenderPipeline getPipeline() {
            return this.glyph.textRenderLayers.guiPipeline();
        }
    }
}

