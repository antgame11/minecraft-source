/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.BakedGlyphImpl;
import net.minecraft.client.font.GlyphMetrics;
import net.minecraft.client.font.TextRenderLayerSet;
import net.minecraft.client.font.UploadableGlyph;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.DynamicTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GlyphAtlasTexture
extends AbstractTexture
implements DynamicTexture {
    private static final int SLOT_LENGTH = 256;
    private final TextRenderLayerSet textRenderLayers;
    private final boolean hasColor;
    private final Slot rootSlot;

    public GlyphAtlasTexture(Supplier<String> nameSupplier, TextRenderLayerSet textRenderLayers, boolean hasColor) {
        this.hasColor = hasColor;
        this.rootSlot = new Slot(0, 0, 256, 256);
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.glTexture = gpuDevice.createTexture(nameSupplier, 7, hasColor ? TextureFormat.RGBA8 : TextureFormat.RED8, 256, 256, 1, 1);
        this.glTexture.setTextureFilter(FilterMode.NEAREST, false);
        this.glTextureView = gpuDevice.createTextureView(this.glTexture);
        this.textRenderLayers = textRenderLayers;
    }

    @Nullable
    public BakedGlyphImpl bake(GlyphMetrics metrics, UploadableGlyph glyph) {
        if (glyph.hasColor() != this.hasColor) {
            return null;
        }
        Slot lv = this.rootSlot.findSlotFor(glyph);
        if (lv != null) {
            glyph.upload(lv.x, lv.y, this.getGlTexture());
            float f = 256.0f;
            float g = 256.0f;
            float h = 0.01f;
            return new BakedGlyphImpl(metrics, this.textRenderLayers, this.getGlTextureView(), ((float)lv.x + 0.01f) / 256.0f, ((float)lv.x - 0.01f + (float)glyph.getWidth()) / 256.0f, ((float)lv.y + 0.01f) / 256.0f, ((float)lv.y - 0.01f + (float)glyph.getHeight()) / 256.0f, glyph.getXMin(), glyph.getXMax(), glyph.getYMin(), glyph.getYMax());
        }
        return null;
    }

    @Override
    public void save(Identifier id, Path path) {
        if (this.glTexture == null) {
            return;
        }
        String string = id.toUnderscoreSeparatedString();
        TextureUtil.writeAsPNG(path, string, this.glTexture, 0, color -> (color & 0xFF000000) == 0 ? -16777216 : color);
    }

    @Environment(value=EnvType.CLIENT)
    static class Slot {
        final int x;
        final int y;
        private final int width;
        private final int height;
        @Nullable
        private Slot subSlot1;
        @Nullable
        private Slot subSlot2;
        private boolean occupied;

        Slot(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Nullable
        Slot findSlotFor(UploadableGlyph glyph) {
            if (this.subSlot1 != null && this.subSlot2 != null) {
                Slot lv = this.subSlot1.findSlotFor(glyph);
                if (lv == null) {
                    lv = this.subSlot2.findSlotFor(glyph);
                }
                return lv;
            }
            if (this.occupied) {
                return null;
            }
            int i = glyph.getWidth();
            int j = glyph.getHeight();
            if (i > this.width || j > this.height) {
                return null;
            }
            if (i == this.width && j == this.height) {
                this.occupied = true;
                return this;
            }
            int k = this.width - i;
            int l = this.height - j;
            if (k > l) {
                this.subSlot1 = new Slot(this.x, this.y, i, this.height);
                this.subSlot2 = new Slot(this.x + i + 1, this.y, this.width - i - 1, this.height);
            } else {
                this.subSlot1 = new Slot(this.x, this.y, this.width, j);
                this.subSlot2 = new Slot(this.x, this.y + j + 1, this.width, this.height - j - 1);
            }
            return this.subSlot1.findSlotFor(glyph);
        }
    }
}

