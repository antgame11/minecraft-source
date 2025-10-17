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
import net.minecraft.client.render.entity.model.ArrowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.ProjectileEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public abstract class ProjectileEntityRenderer<T extends PersistentProjectileEntity, S extends ProjectileEntityRenderState>
extends EntityRenderer<T, S> {
    private final ArrowEntityModel model;

    public ProjectileEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.model = new ArrowEntityModel(arg.getPart(EntityModelLayers.ARROW));
    }

    @Override
    public void render(S arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        arg2.push();
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((ProjectileEntityRenderState)arg).yaw - 90.0f));
        arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(((ProjectileEntityRenderState)arg).pitch));
        arg3.submitModel(this.model, arg, arg2, RenderLayer.getEntityCutout(this.getTexture(arg)), ((ProjectileEntityRenderState)arg).light, OverlayTexture.DEFAULT_UV, ((ProjectileEntityRenderState)arg).outlineColor, null);
        arg2.pop();
        super.render(arg, arg2, arg3, arg4);
    }

    protected abstract Identifier getTexture(S var1);

    @Override
    public void updateRenderState(T arg, S arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        ((ProjectileEntityRenderState)arg2).pitch = ((Entity)arg).getLerpedPitch(f);
        ((ProjectileEntityRenderState)arg2).yaw = ((Entity)arg).getLerpedYaw(f);
        ((ProjectileEntityRenderState)arg2).shake = (float)((PersistentProjectileEntity)arg).shake - f;
    }
}

