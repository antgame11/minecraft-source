/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.GlTexture;

@Environment(value=EnvType.CLIENT)
public class GlTextureView
extends GpuTextureView {
    private boolean closed;

    protected GlTextureView(GlTexture texture, int baseMipLevel, int mipLevels) {
        super(texture, baseMipLevel, mipLevels);
        texture.incrementRefCount();
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.texture().decrementRefCount();
        }
    }

    @Override
    public GlTexture texture() {
        return (GlTexture)super.texture();
    }

    @Override
    public /* synthetic */ GpuTexture texture() {
        return this.texture();
    }
}

