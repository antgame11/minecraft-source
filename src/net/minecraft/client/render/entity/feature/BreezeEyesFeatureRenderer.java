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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BreezeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.state.BreezeEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class BreezeEyesFeatureRenderer
extends FeatureRenderer<BreezeEntityRenderState, BreezeEntityModel> {
    private static final RenderLayer TEXTURE = RenderLayer.getEntityTranslucentEmissiveNoOutline(Identifier.ofVanilla("textures/entity/breeze/breeze_eyes.png"));
    private final BreezeEntityModel field_61803;

    public BreezeEyesFeatureRenderer(FeatureRendererContext<BreezeEntityRenderState, BreezeEntityModel> arg, LoadedEntityModels arg2) {
        super(arg);
        this.field_61803 = new BreezeEntityModel(arg2.getModelPart(EntityModelLayers.BREEZE_EYES));
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, BreezeEntityRenderState arg3, float f, float g) {
        arg2.getBatchingQueue(1).submitModel(this.field_61803, arg3, arg, TEXTURE, i, OverlayTexture.DEFAULT_UV, -1, null, arg3.outlineColor, null);
    }
}

