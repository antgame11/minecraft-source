/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.BakedGlyphImpl;
import net.minecraft.client.font.GlyphBaker;
import net.minecraft.client.font.GlyphMetrics;
import net.minecraft.client.font.UploadableGlyph;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public enum BuiltinEmptyGlyph implements GlyphMetrics
{
    WHITE(() -> BuiltinEmptyGlyph.createRectImage(5, 8, (x, y) -> -1)),
    MISSING(() -> {
        int i = 5;
        int j = 8;
        return BuiltinEmptyGlyph.createRectImage(5, 8, (x, y) -> {
            boolean bl = x == 0 || x + 1 == 5 || y == 0 || y + 1 == 8;
            return bl ? -1 : 0;
        });
    });

    final NativeImage image;

    private static NativeImage createRectImage(int width, int height, ColorSupplier colorSupplier) {
        NativeImage lv = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        for (int k = 0; k < height; ++k) {
            for (int l = 0; l < width; ++l) {
                lv.setColorArgb(l, k, colorSupplier.getColor(l, k));
            }
        }
        lv.untrack();
        return lv;
    }

    private BuiltinEmptyGlyph(Supplier<NativeImage> imageSupplier) {
        this.image = imageSupplier.get();
    }

    @Override
    public float getAdvance() {
        return this.image.getWidth() + 1;
    }

    @Nullable
    public BakedGlyphImpl bake(GlyphBaker glyphBaker) {
        return glyphBaker.bake(this, new UploadableGlyph(){

            @Override
            public int getWidth() {
                return BuiltinEmptyGlyph.this.image.getWidth();
            }

            @Override
            public int getHeight() {
                return BuiltinEmptyGlyph.this.image.getHeight();
            }

            @Override
            public float getOversample() {
                return 1.0f;
            }

            @Override
            public void upload(int x, int y, GpuTexture texture) {
                RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, BuiltinEmptyGlyph.this.image, 0, 0, x, y, BuiltinEmptyGlyph.this.image.getWidth(), BuiltinEmptyGlyph.this.image.getHeight(), 0, 0);
            }

            @Override
            public boolean hasColor() {
                return true;
            }
        });
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    static interface ColorSupplier {
        public int getColor(int var1, int var2);
    }
}

