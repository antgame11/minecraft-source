/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.SignBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractSignBlockEntityRenderer
implements BlockEntityRenderer<SignBlockEntity, SignBlockEntityRenderState> {
    private static final int GLOWING_BLACK_TEXT_COLOR = -988212;
    private static final int MAX_COLORED_TEXT_OUTLINE_RENDER_DISTANCE = MathHelper.square(16);
    private final TextRenderer textRenderer;
    private final SpriteHolder spriteHolder;

    public AbstractSignBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.textRenderer = context.textRenderer();
        this.spriteHolder = context.spriteHolder();
    }

    protected abstract Model.SinglePartModel getModel(BlockState var1, WoodType var2);

    protected abstract SpriteIdentifier getTextureId(WoodType var1);

    protected abstract float getSignScale();

    protected abstract float getTextScale();

    protected abstract Vec3d getTextOffset();

    protected abstract void applyTransforms(MatrixStack var1, float var2, BlockState var3);

    @Override
    public void render(SignBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        BlockState lv = arg.blockState;
        AbstractSignBlock lv2 = (AbstractSignBlock)lv.getBlock();
        Model.SinglePartModel lv3 = this.getModel(lv, lv2.getWoodType());
        this.render(arg, arg2, lv, lv2, lv2.getWoodType(), lv3, arg.crumblingOverlay, arg3);
    }

    private void render(SignBlockEntityRenderState renderState, MatrixStack matrices, BlockState blockState, AbstractSignBlock block, WoodType woodType, Model.SinglePartModel model, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay, OrderedRenderCommandQueue queue) {
        matrices.push();
        this.applyTransforms(matrices, -block.getRotationDegrees(blockState), blockState);
        this.renderSign(matrices, renderState.lightmapCoordinates, woodType, model, crumblingOverlay, queue);
        this.renderText(renderState, matrices, queue, true);
        this.renderText(renderState, matrices, queue, false);
        matrices.pop();
    }

    protected void renderSign(MatrixStack matrices, int lightmapCoords, WoodType woodType, Model.SinglePartModel model, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay, OrderedRenderCommandQueue queue) {
        matrices.push();
        float f = this.getSignScale();
        matrices.scale(f, -f, -f);
        SpriteIdentifier lv = this.getTextureId(woodType);
        RenderLayer lv2 = lv.getRenderLayer(model::getLayer);
        queue.submitModel(model, Unit.INSTANCE, matrices, lv2, lightmapCoords, OverlayTexture.DEFAULT_UV, -1, this.spriteHolder.getSprite(lv), 0, crumblingOverlay);
        matrices.pop();
    }

    private void renderText(SignBlockEntityRenderState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, boolean front) {
        int l;
        boolean bl2;
        int k;
        SignText lv;
        SignText signText = lv = front ? renderState.frontText : renderState.backText;
        if (lv == null) {
            return;
        }
        matrices.push();
        this.applyTextTransforms(matrices, front, this.getTextOffset());
        int i = AbstractSignBlockEntityRenderer.getTextColor(lv);
        int j = 4 * renderState.textLineHeight / 2;
        OrderedText[] lvs = lv.getOrderedMessages(renderState.filterText, textx -> {
            List<OrderedText> list = this.textRenderer.wrapLines((StringVisitable)textx, arg.maxTextWidth);
            return list.isEmpty() ? OrderedText.EMPTY : list.get(0);
        });
        if (lv.isGlowing()) {
            k = lv.getColor().getSignColor();
            bl2 = k == DyeColor.BLACK.getSignColor() || renderState.renderTextOutline;
            l = 0xF000F0;
        } else {
            k = i;
            bl2 = false;
            l = renderState.lightmapCoordinates;
        }
        for (int m = 0; m < 4; ++m) {
            OrderedText lv2 = lvs[m];
            float f = -this.textRenderer.getWidth(lv2) / 2;
            queue.submitText(matrices, f, m * renderState.textLineHeight - j, lv2, false, TextRenderer.TextLayerType.POLYGON_OFFSET, l, k, 0, bl2 ? i : 0);
        }
        matrices.pop();
    }

    private void applyTextTransforms(MatrixStack matrices, boolean front, Vec3d textOffset) {
        if (!front) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        }
        float f = 0.015625f * this.getTextScale();
        matrices.translate(textOffset);
        matrices.scale(f, -f, f);
    }

    private static boolean shouldRenderTextOutline(BlockPos pos) {
        MinecraftClient lv = MinecraftClient.getInstance();
        ClientPlayerEntity lv2 = lv.player;
        if (lv2 != null && lv.options.getPerspective().isFirstPerson() && lv2.isUsingSpyglass()) {
            return true;
        }
        Entity lv3 = lv.getCameraEntity();
        return lv3 != null && lv3.squaredDistanceTo(Vec3d.ofCenter(pos)) < (double)MAX_COLORED_TEXT_OUTLINE_RENDER_DISTANCE;
    }

    public static int getTextColor(SignText text) {
        int i = text.getColor().getSignColor();
        if (i == DyeColor.BLACK.getSignColor() && text.isGlowing()) {
            return -988212;
        }
        return ColorHelper.scaleRgb(i, 0.4f);
    }

    @Override
    public SignBlockEntityRenderState createRenderState() {
        return new SignBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(SignBlockEntity arg, SignBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        arg2.maxTextWidth = arg.getMaxTextWidth();
        arg2.textLineHeight = arg.getTextLineHeight();
        arg2.frontText = arg.getFrontText();
        arg2.backText = arg.getBackText();
        arg2.filterText = MinecraftClient.getInstance().shouldFilterText();
        arg2.renderTextOutline = AbstractSignBlockEntityRenderer.shouldRenderTextOutline(arg.getPos());
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

