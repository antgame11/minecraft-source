/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public record GlyphGuiElementRenderState(Matrix3x2f pose, TextDrawable renderable, @Nullable ScreenRect scissorArea) implements SimpleGuiElementRenderState
{
    @Override
    public void setupVertices(VertexConsumer vertices) {
        this.renderable.render(new Matrix4f().mul(this.pose), vertices, 0xF000F0, true);
    }

    @Override
    public RenderPipeline pipeline() {
        return this.renderable.getPipeline();
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.of(this.renderable.textureView());
    }

    @Override
    @Nullable
    public ScreenRect bounds() {
        return null;
    }

    @Override
    @Nullable
    public ScreenRect scissorArea() {
        return this.scissorArea;
    }
}

