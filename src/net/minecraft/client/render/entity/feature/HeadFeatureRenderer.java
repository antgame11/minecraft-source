/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class HeadFeatureRenderer<S extends LivingEntityRenderState, M extends EntityModel<S>>
extends FeatureRenderer<S, M> {
    private static final float field_53209 = 0.625f;
    private static final float field_53210 = 1.1875f;
    private final HeadTransformation headTransformation;
    private final Function<SkullBlock.SkullType, SkullBlockEntityModel> headModels;
    private final PlayerSkinCache skinCache;

    public HeadFeatureRenderer(FeatureRendererContext<S, M> context, LoadedEntityModels models, PlayerSkinCache skinCache) {
        this(context, models, skinCache, HeadTransformation.DEFAULT);
    }

    public HeadFeatureRenderer(FeatureRendererContext<S, M> context, LoadedEntityModels models, PlayerSkinCache skinCache, HeadTransformation headTransformation) {
        super(context);
        this.headTransformation = headTransformation;
        this.headModels = Util.memoize(type -> SkullBlockEntityRenderer.getModels(models, type));
        this.skinCache = skinCache;
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, S arg3, float f, float g) {
        if (((LivingEntityRenderState)arg3).headItemRenderState.isEmpty() && ((LivingEntityRenderState)arg3).wearingSkullType == null) {
            return;
        }
        arg.push();
        arg.scale(this.headTransformation.horizontalScale(), 1.0f, this.headTransformation.horizontalScale());
        Object lv = this.getContextModel();
        ((Model)lv).getRootPart().applyTransform(arg);
        ((ModelWithHead)lv).applyTransform(arg);
        if (((LivingEntityRenderState)arg3).wearingSkullType != null) {
            arg.translate(0.0f, this.headTransformation.skullYOffset(), 0.0f);
            arg.scale(1.1875f, -1.1875f, -1.1875f);
            arg.translate(-0.5, 0.0, -0.5);
            SkullBlock.SkullType lv2 = ((LivingEntityRenderState)arg3).wearingSkullType;
            SkullBlockEntityModel lv3 = this.headModels.apply(lv2);
            RenderLayer lv4 = this.getRenderLayer((LivingEntityRenderState)arg3, lv2);
            SkullBlockEntityRenderer.render(null, 180.0f, ((LivingEntityRenderState)arg3).headItemAnimationProgress, arg, arg2, i, lv3, lv4, ((LivingEntityRenderState)arg3).outlineColor, null);
        } else {
            HeadFeatureRenderer.translate(arg, this.headTransformation);
            ((LivingEntityRenderState)arg3).headItemRenderState.render(arg, arg2, i, OverlayTexture.DEFAULT_UV, ((LivingEntityRenderState)arg3).outlineColor);
        }
        arg.pop();
    }

    private RenderLayer getRenderLayer(LivingEntityRenderState state, SkullBlock.SkullType skullType) {
        ProfileComponent lv;
        if (skullType == SkullBlock.Type.PLAYER && (lv = state.wearingSkullProfile) != null) {
            return this.skinCache.get(lv).getRenderLayer();
        }
        return SkullBlockEntityRenderer.getCutoutRenderLayer(skullType, null);
    }

    public static void translate(MatrixStack matrices, HeadTransformation transformation) {
        matrices.translate(0.0f, -0.25f + transformation.yOffset(), 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.scale(0.625f, -0.625f, -0.625f);
    }

    @Environment(value=EnvType.CLIENT)
    public record HeadTransformation(float yOffset, float skullYOffset, float horizontalScale) {
        public static final HeadTransformation DEFAULT = new HeadTransformation(0.0f, 0.0f, 1.0f);
    }
}

