/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import com.mojang.blaze3d.textures.GpuTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface UploadableGlyph {
    public int getWidth();

    public int getHeight();

    public void upload(int var1, int var2, GpuTexture var3);

    public boolean hasColor();

    public float getOversample();

    default public float getXMin() {
        return this.getBearingX();
    }

    default public float getXMax() {
        return this.getXMin() + (float)this.getWidth() / this.getOversample();
    }

    default public float getYMin() {
        return 7.0f - this.getAscent();
    }

    default public float getYMax() {
        return this.getYMin() + (float)this.getHeight() / this.getOversample();
    }

    default public float getBearingX() {
        return 0.0f;
    }

    default public float getAscent() {
        return 7.0f;
    }
}

