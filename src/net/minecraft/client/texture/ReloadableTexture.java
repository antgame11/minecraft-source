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
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureContents;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public abstract class ReloadableTexture
extends AbstractTexture {
    private final Identifier textureId;

    public ReloadableTexture(Identifier textureId) {
        this.textureId = textureId;
    }

    public Identifier getId() {
        return this.textureId;
    }

    public void reload(TextureContents contents) {
        boolean bl = contents.clamp();
        boolean bl2 = contents.blur();
        try (NativeImage lv = contents.image();){
            this.load(lv, bl2, bl);
        }
    }

    protected void load(NativeImage image, boolean blur, boolean clamp) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.close();
        this.glTexture = gpuDevice.createTexture(this.textureId::toString, 5, TextureFormat.RGBA8, image.getWidth(), image.getHeight(), 1, 1);
        this.glTextureView = gpuDevice.createTextureView(this.glTexture);
        this.setFilter(blur, false);
        this.setClamp(clamp);
        gpuDevice.createCommandEncoder().writeToTexture(this.glTexture, image);
    }

    public abstract TextureContents loadContents(ResourceManager var1) throws IOException;
}

