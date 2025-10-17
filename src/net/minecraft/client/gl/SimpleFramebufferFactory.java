/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.util.ClosableFactory;

@Environment(value=EnvType.CLIENT)
public record SimpleFramebufferFactory(int width, int height, boolean useDepth, int clearColor) implements ClosableFactory<Framebuffer>
{
    @Override
    public Framebuffer create() {
        return new SimpleFramebuffer(null, this.width, this.height, this.useDepth);
    }

    @Override
    public void prepare(Framebuffer arg) {
        if (this.useDepth) {
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(arg.getColorAttachment(), this.clearColor, arg.getDepthAttachment(), 1.0);
        } else {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(arg.getColorAttachment(), this.clearColor);
        }
    }

    @Override
    public void close(Framebuffer arg) {
        arg.delete();
    }

    @Override
    public boolean equals(ClosableFactory<?> factory) {
        if (factory instanceof SimpleFramebufferFactory) {
            SimpleFramebufferFactory lv = (SimpleFramebufferFactory)factory;
            return this.width == lv.width && this.height == lv.height && this.useDepth == lv.useDepth;
        }
        return false;
    }

    @Override
    public /* synthetic */ void close(Object value) {
        this.close((Framebuffer)value);
    }

    @Override
    public /* synthetic */ void prepare(Object value) {
        this.prepare((Framebuffer)value);
    }

    @Override
    public /* synthetic */ Object create() {
        return this.create();
    }
}

