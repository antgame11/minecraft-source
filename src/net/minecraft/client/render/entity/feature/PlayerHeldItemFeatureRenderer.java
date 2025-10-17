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
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class PlayerHeldItemFeatureRenderer<S extends PlayerEntityRenderState, M extends EntityModel<S> & ModelWithHead>
extends HeldItemFeatureRenderer<S, M> {
    private static final float HEAD_YAW = -0.5235988f;
    private static final float HEAD_ROLL = 1.5707964f;

    public PlayerHeldItemFeatureRenderer(FeatureRendererContext<S, M> arg) {
        super(arg);
    }

    @Override
    protected void renderItem(S arg, ItemRenderState arg2, Arm arg3, MatrixStack arg4, OrderedRenderCommandQueue arg5, int i) {
        Hand lv;
        if (arg2.isEmpty()) {
            return;
        }
        Hand hand = lv = arg3 == ((PlayerEntityRenderState)arg).mainArm ? Hand.MAIN_HAND : Hand.OFF_HAND;
        if (((PlayerEntityRenderState)arg).isUsingItem && ((PlayerEntityRenderState)arg).activeHand == lv && ((PlayerEntityRenderState)arg).handSwingProgress < 1.0E-5f && !((PlayerEntityRenderState)arg).spyglassState.isEmpty()) {
            this.renderSpyglass(arg, arg3, arg4, arg5, i);
        } else {
            super.renderItem(arg, arg2, arg3, arg4, arg5, i);
        }
    }

    private void renderSpyglass(S arg, Arm arm, MatrixStack matrices, OrderedRenderCommandQueue arg4, int light) {
        matrices.push();
        ((Model)this.getContextModel()).getRootPart().applyTransform(matrices);
        ModelPart lv = ((ModelWithHead)this.getContextModel()).getHead();
        float f = lv.pitch;
        lv.pitch = MathHelper.clamp(lv.pitch, -0.5235988f, 1.5707964f);
        lv.applyTransform(matrices);
        lv.pitch = f;
        HeadFeatureRenderer.translate(matrices, HeadFeatureRenderer.HeadTransformation.DEFAULT);
        boolean bl = arm == Arm.LEFT;
        matrices.translate((bl ? -2.5f : 2.5f) / 16.0f, -0.0625f, 0.0f);
        ((PlayerEntityRenderState)arg).spyglassState.render(matrices, arg4, light, OverlayTexture.DEFAULT_UV, ((PlayerEntityRenderState)arg).outlineColor);
        matrices.pop();
    }
}

