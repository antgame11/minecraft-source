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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.SlimeEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.render.entity.state.SlimeEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

@Environment(value=EnvType.CLIENT)
public class SlimeOverlayFeatureRenderer
extends FeatureRenderer<SlimeEntityRenderState, SlimeEntityModel> {
    private final SlimeEntityModel model;

    public SlimeOverlayFeatureRenderer(FeatureRendererContext<SlimeEntityRenderState, SlimeEntityModel> context, LoadedEntityModels loader) {
        super(context);
        this.model = new SlimeEntityModel(loader.getModelPart(EntityModelLayers.SLIME_OUTER));
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, SlimeEntityRenderState arg3, float f, float g) {
        boolean bl;
        boolean bl2 = bl = arg3.hasOutline() && arg3.invisible;
        if (arg3.invisible && !bl) {
            return;
        }
        int j = LivingEntityRenderer.getOverlay(arg3, 0.0f);
        if (bl) {
            arg2.getBatchingQueue(1).submitModel(this.model, arg3, arg, RenderLayer.getOutline(SlimeEntityRenderer.TEXTURE), i, j, -1, null, arg3.outlineColor, null);
        } else {
            arg2.getBatchingQueue(1).submitModel(this.model, arg3, arg, RenderLayer.getEntityTranslucent(SlimeEntityRenderer.TEXTURE), i, j, -1, null, arg3.outlineColor, null);
        }
    }
}

