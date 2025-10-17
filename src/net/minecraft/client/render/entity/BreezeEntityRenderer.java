/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.BreezeEyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.BreezeWindFeatureRenderer;
import net.minecraft.client.render.entity.model.BreezeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BreezeEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class BreezeEntityRenderer
extends MobEntityRenderer<BreezeEntity, BreezeEntityRenderState, BreezeEntityModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/breeze/breeze.png");

    public BreezeEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new BreezeEntityModel(arg.getPart(EntityModelLayers.BREEZE)), 0.5f);
        this.addFeature(new BreezeWindFeatureRenderer(this, arg.getEntityModels()));
        this.addFeature(new BreezeEyesFeatureRenderer(this, arg.getEntityModels()));
    }

    @Override
    public Identifier getTexture(BreezeEntityRenderState arg) {
        return TEXTURE;
    }

    @Override
    public BreezeEntityRenderState createRenderState() {
        return new BreezeEntityRenderState();
    }

    @Override
    public void updateRenderState(BreezeEntity arg, BreezeEntityRenderState arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        arg2.idleAnimationState.copyFrom(arg.idleAnimationState);
        arg2.shootingAnimationState.copyFrom(arg.shootingAnimationState);
        arg2.slidingAnimationState.copyFrom(arg.slidingAnimationState);
        arg2.slidingBackAnimationState.copyFrom(arg.slidingBackAnimationState);
        arg2.inhalingAnimationState.copyFrom(arg.inhalingAnimationState);
        arg2.longJumpingAnimationState.copyFrom(arg.longJumpingAnimationState);
    }

    @Override
    public /* synthetic */ Identifier getTexture(LivingEntityRenderState state) {
        return this.getTexture((BreezeEntityRenderState)state);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

