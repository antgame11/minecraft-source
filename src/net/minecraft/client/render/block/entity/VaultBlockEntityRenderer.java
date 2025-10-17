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
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultClientData;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.VaultBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class VaultBlockEntityRenderer
implements BlockEntityRenderer<VaultBlockEntity, VaultBlockEntityRenderState> {
    private final ItemModelManager itemModelManager;
    private final Random random = Random.create();

    public VaultBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemModelManager = context.itemModelManager();
    }

    @Override
    public VaultBlockEntityRenderState createRenderState() {
        return new VaultBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(VaultBlockEntity arg, VaultBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        ItemStack lv = arg.getSharedData().getDisplayItem();
        if (!VaultBlockEntity.Client.hasDisplayItem(arg.getSharedData()) || lv.isEmpty() || arg.getWorld() == null) {
            return;
        }
        arg2.displayItemStackState = new ItemStackEntityRenderState();
        this.itemModelManager.clearAndUpdate(arg2.displayItemStackState.itemRenderState, lv, ItemDisplayContext.GROUND, arg.getWorld(), null, 0);
        arg2.displayItemStackState.renderedAmount = ItemStackEntityRenderState.getRenderedAmount(lv.getCount());
        arg2.displayItemStackState.seed = ItemStackEntityRenderState.getSeed(lv);
        VaultClientData lv2 = arg.getClientData();
        arg2.displayRotationDegrees = MathHelper.lerpAngleDegrees(f, lv2.getLastDisplayRotation(), lv2.getDisplayRotation());
    }

    @Override
    public void render(VaultBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        if (arg.displayItemStackState == null) {
            return;
        }
        arg2.push();
        arg2.translate(0.5f, 0.4f, 0.5f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arg.displayRotationDegrees));
        ItemEntityRenderer.renderStack(arg2, arg3, arg.lightmapCoordinates, arg.displayItemStackState, this.random);
        arg2.pop();
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

