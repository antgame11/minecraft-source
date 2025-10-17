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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.EmissiveFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WardenEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.WardenEntityRenderState;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class WardenEntityRenderer
extends MobEntityRenderer<WardenEntity, WardenEntityRenderState, WardenEntityModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/warden/warden.png");
    private static final Identifier BIOLUMINESCENT_LAYER_TEXTURE = Identifier.ofVanilla("textures/entity/warden/warden_bioluminescent_layer.png");
    private static final Identifier HEART_TEXTURE = Identifier.ofVanilla("textures/entity/warden/warden_heart.png");
    private static final Identifier PULSATING_SPOTS_1_TEXTURE = Identifier.ofVanilla("textures/entity/warden/warden_pulsating_spots_1.png");
    private static final Identifier PULSATING_SPOTS_2_TEXTURE = Identifier.ofVanilla("textures/entity/warden/warden_pulsating_spots_2.png");

    public WardenEntityRenderer(EntityRendererFactory.Context arg2) {
        super(arg2, new WardenEntityModel(arg2.getPart(EntityModelLayers.WARDEN)), 0.9f);
        WardenEntityModel lv = new WardenEntityModel(arg2.getPart(EntityModelLayers.WARDEN_BIOLUMINESCENT));
        WardenEntityModel lv2 = new WardenEntityModel(arg2.getPart(EntityModelLayers.WARDEN_PULSATING_SPOTS));
        WardenEntityModel lv3 = new WardenEntityModel(arg2.getPart(EntityModelLayers.WARDEN_TENDRILS));
        WardenEntityModel lv4 = new WardenEntityModel(arg2.getPart(EntityModelLayers.WARDEN_HEART));
        this.addFeature(new EmissiveFeatureRenderer<WardenEntityRenderState, WardenEntityModel>(this, arg -> BIOLUMINESCENT_LAYER_TEXTURE, (state, tickProgress) -> 1.0f, lv, RenderLayer::getEntityTranslucentEmissive, false));
        this.addFeature(new EmissiveFeatureRenderer<WardenEntityRenderState, WardenEntityModel>(this, arg -> PULSATING_SPOTS_1_TEXTURE, (state, tickProgress) -> Math.max(0.0f, MathHelper.cos(tickProgress * 0.045f) * 0.25f), lv2, RenderLayer::getEntityTranslucentEmissive, false));
        this.addFeature(new EmissiveFeatureRenderer<WardenEntityRenderState, WardenEntityModel>(this, arg -> PULSATING_SPOTS_2_TEXTURE, (state, tickProgress) -> Math.max(0.0f, MathHelper.cos(tickProgress * 0.045f + (float)Math.PI) * 0.25f), lv2, RenderLayer::getEntityTranslucentEmissive, false));
        this.addFeature(new EmissiveFeatureRenderer<WardenEntityRenderState, WardenEntityModel>(this, arg -> TEXTURE, (state, tickProgress) -> state.tendrilAlpha, lv3, RenderLayer::getEntityTranslucentEmissive, false));
        this.addFeature(new EmissiveFeatureRenderer<WardenEntityRenderState, WardenEntityModel>(this, arg -> HEART_TEXTURE, (state, tickProgress) -> state.heartAlpha, lv4, RenderLayer::getEntityTranslucentEmissive, false));
    }

    @Override
    public Identifier getTexture(WardenEntityRenderState arg) {
        return TEXTURE;
    }

    @Override
    public WardenEntityRenderState createRenderState() {
        return new WardenEntityRenderState();
    }

    @Override
    public void updateRenderState(WardenEntity arg, WardenEntityRenderState arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        arg2.tendrilAlpha = arg.getTendrilAlpha(f);
        arg2.heartAlpha = arg.getHeartAlpha(f);
        arg2.roaringAnimationState.copyFrom(arg.roaringAnimationState);
        arg2.sniffingAnimationState.copyFrom(arg.sniffingAnimationState);
        arg2.emergingAnimationState.copyFrom(arg.emergingAnimationState);
        arg2.diggingAnimationState.copyFrom(arg.diggingAnimationState);
        arg2.attackingAnimationState.copyFrom(arg.attackingAnimationState);
        arg2.chargingSonicBoomAnimationState.copyFrom(arg.chargingSonicBoomAnimationState);
    }

    @Override
    public /* synthetic */ Identifier getTexture(LivingEntityRenderState state) {
        return this.getTexture((WardenEntityRenderState)state);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

