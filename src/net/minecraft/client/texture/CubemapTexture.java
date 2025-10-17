/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ReloadableTexture;
import net.minecraft.client.texture.TextureContents;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class CubemapTexture
extends ReloadableTexture {
    private static final String[] TEXTURE_SUFFIXES = new String[]{"_1.png", "_3.png", "_5.png", "_4.png", "_0.png", "_2.png"};

    public CubemapTexture(Identifier arg) {
        super(arg);
    }

    @Override
    public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
        Identifier lv = this.getId();
        try (TextureContents lv2 = TextureContents.load(resourceManager, lv.withSuffixedPath(TEXTURE_SUFFIXES[0]));){
            int i = lv2.image().getWidth();
            int j = lv2.image().getHeight();
            NativeImage lv3 = new NativeImage(i, j * 6, false);
            lv2.image().copyRect(lv3, 0, 0, 0, 0, i, j, false, true);
            for (int k = 1; k < 6; ++k) {
                try (TextureContents lv4 = TextureContents.load(resourceManager, lv.withSuffixedPath(TEXTURE_SUFFIXES[k]));){
                    if (lv4.image().getWidth() != i || lv4.image().getHeight() != j) {
                        throw new IOException("Image dimensions of cubemap '" + String.valueOf(lv) + "' sides do not match: part 0 is " + i + "x" + j + ", but part " + k + " is " + lv4.image().getWidth() + "x" + lv4.image().getHeight());
                    }
                    lv4.image().copyRect(lv3, 0, 0, 0, k * j, i, j, false, true);
                    continue;
                }
            }
            TextureContents textureContents = new TextureContents(lv3, new TextureResourceMetadata(true, false));
            return textureContents;
        }
    }

    @Override
    protected void load(NativeImage image, boolean blur, boolean clamp) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        int i = image.getWidth();
        int j = image.getHeight() / 6;
        this.close();
        this.glTexture = gpuDevice.createTexture(this.getId()::toString, 21, TextureFormat.RGBA8, i, j, 6, 1);
        this.glTextureView = gpuDevice.createTextureView(this.glTexture);
        this.setFilter(blur, false);
        this.setClamp(clamp);
        for (int k = 0; k < 6; ++k) {
            gpuDevice.createCommandEncoder().writeToTexture(this.glTexture, image, 0, k, 0, 0, i, j, 0, j * k);
        }
    }
}

