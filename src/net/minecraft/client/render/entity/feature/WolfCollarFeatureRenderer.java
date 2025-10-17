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
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.render.entity.state.WolfEntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class WolfCollarFeatureRenderer
extends FeatureRenderer<WolfEntityRenderState, WolfEntityModel> {
    private static final Identifier SKIN = Identifier.ofVanilla("textures/entity/wolf/wolf_collar.png");

    public WolfCollarFeatureRenderer(FeatureRendererContext<WolfEntityRenderState, WolfEntityModel> arg) {
        super(arg);
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, WolfEntityRenderState arg3, float f, float g) {
        DyeColor lv = arg3.collarColor;
        if (lv == null || arg3.invisible) {
            return;
        }
        int j = lv.getEntityColor();
        arg2.getBatchingQueue(1).submitModel(this.getContextModel(), arg3, arg, RenderLayer.getEntityCutoutNoCull(SKIN), i, OverlayTexture.DEFAULT_UV, j, (Sprite)null, arg3.outlineColor, (ModelCommandRenderer.CrumblingOverlayCommand)null);
    }
}

