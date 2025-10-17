/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import it.unimi.dsi.fastutil.HashCommon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ShelfBlock;
import net.minecraft.block.entity.ShelfBlockEntity;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.ShelfBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ShelfBlockEntityRenderer
implements BlockEntityRenderer<ShelfBlockEntity, ShelfBlockEntityRenderState> {
    private static final float ITEM_SCALE = 0.25f;
    private static final float BOTTOM_ALIGNED_OFFSET = -0.25f;
    private final ItemModelManager itemModelManager;

    public ShelfBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemModelManager = context.itemModelManager();
    }

    @Override
    public ShelfBlockEntityRenderState createRenderState() {
        return new ShelfBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(ShelfBlockEntity arg, ShelfBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        arg2.alignItemsToBottom = arg.shouldAlignItemsToBottom();
        DefaultedList<ItemStack> lv = arg.getHeldStacks();
        int i = HashCommon.long2int(arg.getPos().asLong());
        for (int j = 0; j < lv.size(); ++j) {
            ItemStack lv2 = lv.get(j);
            if (lv2.isEmpty()) continue;
            ItemRenderState lv3 = new ItemRenderState();
            this.itemModelManager.clearAndUpdate(lv3, lv2, ItemDisplayContext.ON_SHELF, arg.getEntityWorld(), arg, i + j);
            arg2.itemRenderStates[j] = lv3;
        }
    }

    @Override
    public void render(ShelfBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        Direction lv = arg.blockState.get(ShelfBlock.FACING);
        float f = lv.getAxis().isHorizontal() ? -lv.getPositiveHorizontalDegrees() : 180.0f;
        for (int i = 0; i < arg.itemRenderStates.length; ++i) {
            ItemRenderState lv2 = arg.itemRenderStates[i];
            if (lv2 == null) continue;
            this.renderItem(arg, lv2, arg2, arg3, i, f);
        }
    }

    private void renderItem(ShelfBlockEntityRenderState state, ItemRenderState itemRenderState, MatrixStack matrices, OrderedRenderCommandQueue queue, int overlay, float rotationDegrees) {
        float g = (float)(overlay - 1) * 0.3125f;
        Vec3d lv = new Vec3d(g, state.alignItemsToBottom ? -0.25 : 0.0, -0.25);
        matrices.push();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));
        matrices.translate(lv);
        matrices.scale(0.25f, 0.25f, 0.25f);
        Box lv2 = itemRenderState.getModelBoundingBox();
        double d = -lv2.minY;
        if (!state.alignItemsToBottom) {
            d += -(lv2.maxY - lv2.minY) / 2.0;
        }
        matrices.translate(0.0, d, 0.0);
        itemRenderState.render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
        matrices.pop();
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

