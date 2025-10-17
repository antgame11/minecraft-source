/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.LecternBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LecternBlockEntityRenderer
implements BlockEntityRenderer<LecternBlockEntity, LecternBlockEntityRenderState> {
    private final SpriteHolder spriteHolder;
    private final BookModel book;
    private final BookModel.BookModelState bookModelState = new BookModel.BookModelState(0.0f, 0.1f, 0.9f, 1.2f);

    public LecternBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.spriteHolder = ctx.spriteHolder();
        this.book = new BookModel(ctx.getLayerModelPart(EntityModelLayers.BOOK));
    }

    @Override
    public LecternBlockEntityRenderState createRenderState() {
        return new LecternBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(LecternBlockEntity arg, LecternBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        arg2.hasBook = arg.getCachedState().get(LecternBlock.HAS_BOOK);
        arg2.bookRotationDegrees = arg.getCachedState().get(LecternBlock.FACING).rotateYClockwise().getPositiveHorizontalDegrees();
    }

    @Override
    public void render(LecternBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        if (!arg.hasBook) {
            return;
        }
        arg2.push();
        arg2.translate(0.5f, 1.0625f, 0.5f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-arg.bookRotationDegrees));
        arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(67.5f));
        arg2.translate(0.0f, -0.125f, 0.0f);
        arg3.submitModel(this.book, this.bookModelState, arg2, EnchantingTableBlockEntityRenderer.BOOK_TEXTURE.getRenderLayer(RenderLayer::getEntitySolid), arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, -1, this.spriteHolder.getSprite(EnchantingTableBlockEntityRenderer.BOOK_TEXTURE), 0, arg.crumblingOverlay);
        arg2.pop();
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

