/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.command;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.EntityHitboxAndView;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public interface RenderCommandQueue {
    public void submitDebugHitbox(MatrixStack var1, EntityRenderState var2, EntityHitboxAndView var3);

    public void submitShadowPieces(MatrixStack var1, float var2, List<EntityRenderState.ShadowPiece> var3);

    public void submitLabel(MatrixStack var1, @Nullable Vec3d var2, int var3, Text var4, boolean var5, int var6, double var7, CameraRenderState var9);

    public void submitText(MatrixStack var1, float var2, float var3, OrderedText var4, boolean var5, TextRenderer.TextLayerType var6, int var7, int var8, int var9, int var10);

    public void submitFire(MatrixStack var1, EntityRenderState var2, Quaternionf var3);

    public void submitLeash(MatrixStack var1, EntityRenderState.LeashData var2);

    public <S> void submitModel(Model<? super S> var1, S var2, MatrixStack var3, RenderLayer var4, int var5, int var6, int var7, @Nullable Sprite var8, int var9, @Nullable ModelCommandRenderer.CrumblingOverlayCommand var10);

    default public <S> void submitModel(Model<? super S> model, S state, MatrixStack matrices, RenderLayer renderLayer, int light, int overlay, int outlineColor, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        this.submitModel(model, state, matrices, renderLayer, light, overlay, -1, null, outlineColor, crumblingOverlay);
    }

    default public void submitModelPart(ModelPart part, MatrixStack matrices, RenderLayer renderLayer, int light, int overlay, @Nullable Sprite sprite) {
        this.submitModelPart(part, matrices, renderLayer, light, overlay, sprite, false, false, -1, null, 0);
    }

    default public void submitModelPart(ModelPart part, MatrixStack matrices, RenderLayer renderLayer, int light, int overlay, @Nullable Sprite sprite, int tintedColor, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        this.submitModelPart(part, matrices, renderLayer, light, overlay, sprite, false, false, tintedColor, crumblingOverlay, 0);
    }

    default public void submitModelPart(ModelPart part, MatrixStack matrices, RenderLayer renderLayer, int light, int overlay, @Nullable Sprite sprite, boolean sheeted, boolean hasGlint) {
        this.submitModelPart(part, matrices, renderLayer, light, overlay, sprite, sheeted, hasGlint, -1, null, 0);
    }

    public void submitModelPart(ModelPart var1, MatrixStack var2, RenderLayer var3, int var4, int var5, @Nullable Sprite var6, boolean var7, boolean var8, int var9, @Nullable ModelCommandRenderer.CrumblingOverlayCommand var10, int var11);

    public void submitBlock(MatrixStack var1, BlockState var2, int var3, int var4, int var5);

    public void submitMovingBlock(MatrixStack var1, MovingBlockRenderState var2);

    public void submitBlockStateModel(MatrixStack var1, RenderLayer var2, BlockStateModel var3, float var4, float var5, float var6, int var7, int var8, int var9);

    public void submitItem(MatrixStack var1, ItemDisplayContext var2, int var3, int var4, int var5, int[] var6, List<BakedQuad> var7, RenderLayer var8, ItemRenderState.Glint var9);

    public void submitCustom(MatrixStack var1, RenderLayer var2, OrderedRenderCommandQueue.Custom var3);

    public void submitCustom(OrderedRenderCommandQueue.LayeredCustom var1);
}

