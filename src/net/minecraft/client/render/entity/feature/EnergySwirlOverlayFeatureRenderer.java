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
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public abstract class EnergySwirlOverlayFeatureRenderer<S extends EntityRenderState, M extends EntityModel<S>>
extends FeatureRenderer<S, M> {
    public EnergySwirlOverlayFeatureRenderer(FeatureRendererContext<S, M> arg) {
        super(arg);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, S state, float limbAngle, float limbDistance) {
        if (!this.shouldRender(state)) {
            return;
        }
        float h = ((EntityRenderState)state).age;
        M lv = this.getEnergySwirlModel();
        queue.getBatchingQueue(1).submitModel(lv, state, matrices, RenderLayer.getEnergySwirl(this.getEnergySwirlTexture(), this.getEnergySwirlX(h) % 1.0f, h * 0.01f % 1.0f), light, OverlayTexture.DEFAULT_UV, -8355712, (Sprite)null, ((EntityRenderState)state).outlineColor, (ModelCommandRenderer.CrumblingOverlayCommand)null);
    }

    protected abstract boolean shouldRender(S var1);

    protected abstract float getEnergySwirlX(float var1);

    protected abstract Identifier getEnergySwirlTexture();

    protected abstract M getEnergySwirlModel();
}

