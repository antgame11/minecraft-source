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
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class HeldItemFeatureRenderer<S extends ArmedEntityRenderState, M extends EntityModel<S>>
extends FeatureRenderer<S, M> {
    public HeldItemFeatureRenderer(FeatureRendererContext<S, M> arg) {
        super(arg);
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, S arg3, float f, float g) {
        this.renderItem(arg3, ((ArmedEntityRenderState)arg3).rightHandItemState, Arm.RIGHT, arg, arg2, i);
        this.renderItem(arg3, ((ArmedEntityRenderState)arg3).leftHandItemState, Arm.LEFT, arg, arg2, i);
    }

    protected void renderItem(S entityState, ItemRenderState arg2, Arm arm, MatrixStack matrices, OrderedRenderCommandQueue arg5, int light) {
        if (arg2.isEmpty()) {
            return;
        }
        matrices.push();
        ((ModelWithArms)this.getContextModel()).setArmAngle(entityState, arm, matrices);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        boolean bl = arm == Arm.LEFT;
        matrices.translate((float)(bl ? -1 : 1) / 16.0f, 0.125f, -0.625f);
        arg2.render(matrices, arg5, light, OverlayTexture.DEFAULT_UV, ((ArmedEntityRenderState)entityState).outlineColor);
        matrices.pop();
    }
}

