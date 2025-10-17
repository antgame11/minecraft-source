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
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.state.MooshroomEntityRenderState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class MooshroomMushroomFeatureRenderer
extends FeatureRenderer<MooshroomEntityRenderState, CowEntityModel> {
    private final BlockRenderManager blockRenderManager;

    public MooshroomMushroomFeatureRenderer(FeatureRendererContext<MooshroomEntityRenderState, CowEntityModel> context, BlockRenderManager blockRenderManager) {
        super(context);
        this.blockRenderManager = blockRenderManager;
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, MooshroomEntityRenderState arg3, float f, float g) {
        boolean bl;
        if (arg3.baby) {
            return;
        }
        boolean bl2 = bl = arg3.hasOutline() && arg3.invisible;
        if (arg3.invisible && !bl) {
            return;
        }
        BlockState lv = arg3.type.getMushroomState();
        int j = LivingEntityRenderer.getOverlay(arg3, 0.0f);
        BlockStateModel lv2 = this.blockRenderManager.getModel(lv);
        arg.push();
        arg.translate(0.2f, -0.35f, 0.5f);
        arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-48.0f));
        arg.scale(-1.0f, -1.0f, 1.0f);
        arg.translate(-0.5f, -0.5f, -0.5f);
        this.renderMushroom(arg, arg2, i, bl, arg3.outlineColor, lv, j, lv2);
        arg.pop();
        arg.push();
        arg.translate(0.2f, -0.35f, 0.5f);
        arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(42.0f));
        arg.translate(0.1f, 0.0f, -0.6f);
        arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-48.0f));
        arg.scale(-1.0f, -1.0f, 1.0f);
        arg.translate(-0.5f, -0.5f, -0.5f);
        this.renderMushroom(arg, arg2, i, bl, arg3.outlineColor, lv, j, lv2);
        arg.pop();
        arg.push();
        ((CowEntityModel)this.getContextModel()).getHead().applyTransform(arg);
        arg.translate(0.0f, -0.7f, -0.2f);
        arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-78.0f));
        arg.scale(-1.0f, -1.0f, 1.0f);
        arg.translate(-0.5f, -0.5f, -0.5f);
        this.renderMushroom(arg, arg2, i, bl, arg3.outlineColor, lv, j, lv2);
        arg.pop();
    }

    private void renderMushroom(MatrixStack matrices, OrderedRenderCommandQueue arg2, int light, boolean renderAsModel, int j, BlockState arg3, int k, BlockStateModel arg4) {
        if (renderAsModel) {
            arg2.submitBlockStateModel(matrices, RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE), arg4, 0.0f, 0.0f, 0.0f, light, k, j);
        } else {
            arg2.submitBlock(matrices, arg3, light, k, j);
        }
    }
}

