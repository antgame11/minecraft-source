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
import net.minecraft.client.render.entity.ArrowEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.entity.model.ArrowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.state.ProjectileEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class StuckArrowsFeatureRenderer<M extends PlayerEntityModel>
extends StuckObjectsFeatureRenderer<M, ProjectileEntityRenderState> {
    public StuckArrowsFeatureRenderer(LivingEntityRenderer<?, PlayerEntityRenderState, M> entityRenderer, EntityRendererFactory.Context context) {
        super(entityRenderer, new ArrowEntityModel(context.getPart(EntityModelLayers.ARROW)), new ProjectileEntityRenderState(), ArrowEntityRenderer.TEXTURE, StuckObjectsFeatureRenderer.RenderPosition.IN_CUBE);
    }

    @Override
    protected int getObjectCount(PlayerEntityRenderState playerRenderState) {
        return playerRenderState.stuckArrowCount;
    }
}

