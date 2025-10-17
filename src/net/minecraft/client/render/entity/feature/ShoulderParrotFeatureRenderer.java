/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ParrotEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.ParrotEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.ParrotEntity;

@Environment(value=EnvType.CLIENT)
public class ShoulderParrotFeatureRenderer
extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final ParrotEntityModel model;

    public ShoulderParrotFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context, LoadedEntityModels loader) {
        super(context);
        this.model = new ParrotEntityModel(loader.getModelPart(EntityModelLayers.PARROT));
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, PlayerEntityRenderState arg3, float f, float g) {
        ParrotEntity.Variant lv2;
        ParrotEntity.Variant lv = arg3.leftShoulderParrotVariant;
        if (lv != null) {
            this.render(arg, arg2, i, arg3, lv, f, g, true);
        }
        if ((lv2 = arg3.rightShoulderParrotVariant) != null) {
            this.render(arg, arg2, i, arg3, lv2, f, g, false);
        }
    }

    private void render(MatrixStack matrices, OrderedRenderCommandQueue arg2, int light, PlayerEntityRenderState state, ParrotEntity.Variant parrotVariant, float headYaw, float headPitch, boolean left) {
        matrices.push();
        matrices.translate(left ? 0.4f : -0.4f, state.isInSneakingPose ? -1.3f : -1.5f, 0.0f);
        ParrotEntityRenderState lv = new ParrotEntityRenderState();
        lv.parrotPose = ParrotEntityModel.Pose.ON_SHOULDER;
        lv.age = state.age;
        lv.limbSwingAnimationProgress = state.limbSwingAnimationProgress;
        lv.limbSwingAmplitude = state.limbSwingAmplitude;
        lv.relativeHeadYaw = headYaw;
        lv.pitch = headPitch;
        arg2.submitModel(this.model, lv, matrices, this.model.getLayer(ParrotEntityRenderer.getTexture(parrotVariant)), light, OverlayTexture.DEFAULT_UV, state.outlineColor, null);
        matrices.pop();
    }
}

