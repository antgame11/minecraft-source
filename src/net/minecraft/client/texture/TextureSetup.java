/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record TextureSetup(@Nullable GpuTextureView texure0, @Nullable GpuTextureView texure1, @Nullable GpuTextureView texure2) {
    private static final TextureSetup EMPTY = new TextureSetup(null, null, null);
    private static int shuffleSeed;

    public static TextureSetup withoutGlTexture(GpuTextureView texture) {
        return new TextureSetup(texture, null, null);
    }

    public static TextureSetup of(GpuTextureView texture) {
        return new TextureSetup(texture, null, MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().getGlTextureView());
    }

    public static TextureSetup of(GpuTextureView texture0, GpuTextureView texture1) {
        return new TextureSetup(texture0, texture1, null);
    }

    public static TextureSetup empty() {
        return EMPTY;
    }

    public int getSortKey() {
        return SharedConstants.SHUFFLE_UI_RENDERING_ORDER ? this.hashCode() * (shuffleSeed + 1) : this.hashCode();
    }

    public static void shuffleRenderingOrder() {
        shuffleSeed = Math.round(100000.0f * (float)Math.random());
    }

    @Nullable
    public GpuTextureView texure0() {
        return this.texure0;
    }

    @Nullable
    public GpuTextureView texure1() {
        return this.texure1;
    }

    @Nullable
    public GpuTextureView texure2() {
        return this.texure2;
    }
}

