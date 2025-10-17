/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;

@Environment(value=EnvType.CLIENT)
public enum BlockRenderLayer {
    SOLID(RenderPipelines.SOLID, 0x400000, true, false),
    CUTOUT_MIPPED(RenderPipelines.CUTOUT_MIPPED, 0x400000, true, false),
    CUTOUT(RenderPipelines.CUTOUT, 786432, false, false),
    TRANSLUCENT(RenderPipelines.TRANSLUCENT, 786432, true, true),
    TRIPWIRE(RenderPipelines.TRIPWIRE, 1536, true, true);

    private final RenderPipeline pipeline;
    private final int size;
    private final boolean mipmap;
    private final boolean translucent;
    private final String name;

    private BlockRenderLayer(RenderPipeline pipeline, int size, boolean mipmap, boolean translucent) {
        this.pipeline = pipeline;
        this.size = size;
        this.mipmap = mipmap;
        this.translucent = translucent;
        this.name = this.toString().toLowerCase(Locale.ROOT);
    }

    public RenderPipeline getPipeline() {
        return this.pipeline;
    }

    public int getBufferSize() {
        return this.size;
    }

    public String getName() {
        return this.name;
    }

    public boolean isTranslucent() {
        return this.translucent;
    }

    public GpuTextureView getTextureView() {
        TextureManager lv = MinecraftClient.getInstance().getTextureManager();
        AbstractTexture lv2 = lv.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        lv2.setUseMipmaps(this.mipmap);
        return lv2.getGlTextureView();
    }
}

