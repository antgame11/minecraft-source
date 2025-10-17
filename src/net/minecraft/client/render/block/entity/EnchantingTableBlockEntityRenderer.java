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
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.EnchantingTableBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EnchantingTableBlockEntityRenderer
implements BlockEntityRenderer<EnchantingTableBlockEntity, EnchantingTableBlockEntityRenderState> {
    public static final SpriteIdentifier BOOK_TEXTURE = TexturedRenderLayers.ENTITY_SPRITE_MAPPER.mapVanilla("enchanting_table_book");
    private final SpriteHolder spriteHolder;
    private final BookModel book;

    public EnchantingTableBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.spriteHolder = ctx.spriteHolder();
        this.book = new BookModel(ctx.getLayerModelPart(EntityModelLayers.BOOK));
    }

    @Override
    public EnchantingTableBlockEntityRenderState createRenderState() {
        return new EnchantingTableBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(EnchantingTableBlockEntity arg, EnchantingTableBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        float g;
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        arg2.pageAngle = MathHelper.lerp(f, arg.pageAngle, arg.nextPageAngle);
        arg2.pageTurningSpeed = MathHelper.lerp(f, arg.pageTurningSpeed, arg.nextPageTurningSpeed);
        arg2.ticks = (float)arg.ticks + f;
        for (g = arg.bookRotation - arg.lastBookRotation; g >= (float)Math.PI; g -= (float)Math.PI * 2) {
        }
        while (g < (float)(-Math.PI)) {
            g += (float)Math.PI * 2;
        }
        arg2.bookRotationDegrees = arg.lastBookRotation + g * f;
    }

    @Override
    public void render(EnchantingTableBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        arg2.push();
        arg2.translate(0.5f, 0.75f, 0.5f);
        arg2.translate(0.0f, 0.1f + MathHelper.sin(arg.ticks * 0.1f) * 0.01f, 0.0f);
        float f = arg.bookRotationDegrees;
        arg2.multiply(RotationAxis.POSITIVE_Y.rotation(-f));
        arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(80.0f));
        float g = MathHelper.fractionalPart(arg.pageAngle + 0.25f) * 1.6f - 0.3f;
        float h = MathHelper.fractionalPart(arg.pageAngle + 0.75f) * 1.6f - 0.3f;
        BookModel.BookModelState lv = new BookModel.BookModelState(arg.ticks, MathHelper.clamp(g, 0.0f, 1.0f), MathHelper.clamp(h, 0.0f, 1.0f), arg.pageTurningSpeed);
        arg3.submitModel(this.book, lv, arg2, BOOK_TEXTURE.getRenderLayer(RenderLayer::getEntitySolid), arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, -1, this.spriteHolder.getSprite(BOOK_TEXTURE), 0, arg.crumblingOverlay);
        arg2.pop();
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

