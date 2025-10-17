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
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;

@Environment(value=EnvType.CLIENT)
public abstract class EyesFeatureRenderer<S extends EntityRenderState, M extends EntityModel<S>>
extends FeatureRenderer<S, M> {
    public EyesFeatureRenderer(FeatureRendererContext<S, M> arg) {
        super(arg);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, S state, float limbAngle, float limbDistance) {
        queue.getBatchingQueue(1).submitModel(this.getContextModel(), state, matrices, this.getEyesTexture(), light, OverlayTexture.DEFAULT_UV, -1, (Sprite)null, ((EntityRenderState)state).outlineColor, (ModelCommandRenderer.CrumblingOverlayCommand)null);
    }

    public abstract RenderLayer getEyesTexture();
}

