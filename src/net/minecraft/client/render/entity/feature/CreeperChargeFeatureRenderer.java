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
import net.minecraft.client.render.entity.feature.EnergySwirlOverlayFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class CreeperChargeFeatureRenderer
extends EnergySwirlOverlayFeatureRenderer<CreeperEntityRenderState, CreeperEntityModel> {
    private static final Identifier SKIN = Identifier.ofVanilla("textures/entity/creeper/creeper_armor.png");
    private final CreeperEntityModel model;

    public CreeperChargeFeatureRenderer(FeatureRendererContext<CreeperEntityRenderState, CreeperEntityModel> context, LoadedEntityModels loader) {
        super(context);
        this.model = new CreeperEntityModel(loader.getModelPart(EntityModelLayers.CREEPER_ARMOR));
    }

    @Override
    protected boolean shouldRender(CreeperEntityRenderState arg) {
        return arg.charged;
    }

    @Override
    protected float getEnergySwirlX(float partialAge) {
        return partialAge * 0.01f;
    }

    @Override
    protected Identifier getEnergySwirlTexture() {
        return SKIN;
    }

    @Override
    protected CreeperEntityModel getEnergySwirlModel() {
        return this.model;
    }

    @Override
    protected /* synthetic */ EntityModel getEnergySwirlModel() {
        return this.getEnergySwirlModel();
    }
}

