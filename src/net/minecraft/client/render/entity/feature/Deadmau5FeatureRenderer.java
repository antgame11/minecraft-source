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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.Deadmau5EarsEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

@Environment(value=EnvType.CLIENT)
public class Deadmau5FeatureRenderer
extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final BipedEntityModel<PlayerEntityRenderState> model;

    public Deadmau5FeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context, LoadedEntityModels entityModels) {
        super(context);
        this.model = new Deadmau5EarsEntityModel(entityModels.getModelPart(EntityModelLayers.PLAYER_EARS));
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, PlayerEntityRenderState arg3, float f, float g) {
        if (!arg3.extraEars || arg3.invisible) {
            return;
        }
        int j = LivingEntityRenderer.getOverlay(arg3, 0.0f);
        arg2.submitModel(this.model, arg3, arg, RenderLayer.getEntitySolid(arg3.skinTextures.body().texturePath()), i, j, arg3.outlineColor, null);
    }
}

