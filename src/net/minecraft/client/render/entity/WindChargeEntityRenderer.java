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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WindChargeEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class WindChargeEntityRenderer
extends EntityRenderer<AbstractWindChargeEntity, EntityRenderState> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/projectiles/wind_charge.png");
    private final WindChargeEntityModel model;

    public WindChargeEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.model = new WindChargeEntityModel(arg.getPart(EntityModelLayers.WIND_CHARGE));
    }

    @Override
    public void render(EntityRenderState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitModel(this.model, renderState, matrices, RenderLayer.getBreezeWind(TEXTURE, this.getXOffset(renderState.age) % 1.0f, 0.0f), renderState.light, OverlayTexture.DEFAULT_UV, renderState.outlineColor, null);
        super.render(renderState, matrices, queue, cameraState);
    }

    protected float getXOffset(float tickProgress) {
        return tickProgress * 0.03f;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}

