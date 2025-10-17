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
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public abstract class FeatureRenderer<S extends EntityRenderState, M extends EntityModel<? super S>> {
    private final FeatureRendererContext<S, M> context;

    public FeatureRenderer(FeatureRendererContext<S, M> context) {
        this.context = context;
    }

    protected static <S extends LivingEntityRenderState> void render(Model<? super S> arg, Identifier texture, MatrixStack matrices, OrderedRenderCommandQueue arg4, int light, S state, int color, int k) {
        if (!state.invisible) {
            FeatureRenderer.renderModel(arg, texture, matrices, arg4, light, state, color, k);
        }
    }

    protected static <S extends LivingEntityRenderState> void renderModel(Model<? super S> arg, Identifier arg2, MatrixStack arg3, OrderedRenderCommandQueue arg4, int light, S state, int color, int k) {
        arg4.getBatchingQueue(k).submitModel(arg, state, arg3, RenderLayer.getEntityCutoutNoCull(arg2), light, LivingEntityRenderer.getOverlay(state, 0.0f), color, null, state.outlineColor, null);
    }

    public M getContextModel() {
        return this.context.getModel();
    }

    public abstract void render(MatrixStack var1, OrderedRenderCommandQueue var2, int var3, S var4, float var5, float var6);
}

