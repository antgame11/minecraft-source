/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.PlayerSkinGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4fStack;

@Environment(value=EnvType.CLIENT)
public class PlayerSkinGuiElementRenderer
extends SpecialGuiElementRenderer<PlayerSkinGuiElementRenderState> {
    public PlayerSkinGuiElementRenderer(VertexConsumerProvider.Immediate arg) {
        super(arg);
    }

    @Override
    public Class<PlayerSkinGuiElementRenderState> getElementClass() {
        return PlayerSkinGuiElementRenderState.class;
    }

    @Override
    protected void render(PlayerSkinGuiElementRenderState arg, MatrixStack arg2) {
        MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.PLAYER_SKIN);
        int i = MinecraftClient.getInstance().getWindow().getScaleFactor();
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        float f = arg.scale() * (float)i;
        matrix4fStack.rotateAround(RotationAxis.POSITIVE_X.rotationDegrees(arg.xRotation()), 0.0f, f * -arg.yPivot(), 0.0f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-arg.yRotation()));
        arg2.translate(0.0f, -1.6010001f, 0.0f);
        RenderLayer lv = arg.playerModel().getLayer(arg.texture());
        arg.playerModel().render(arg2, this.vertexConsumers.getBuffer(lv), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
        this.vertexConsumers.draw();
        matrix4fStack.popMatrix();
    }

    @Override
    protected String getName() {
        return "player skin";
    }
}

