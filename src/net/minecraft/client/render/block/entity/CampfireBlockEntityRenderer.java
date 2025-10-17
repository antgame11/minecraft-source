/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.CampfireBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CampfireBlockEntityRenderer
implements BlockEntityRenderer<CampfireBlockEntity, CampfireBlockEntityRenderState> {
    private static final float SCALE = 0.375f;
    private final ItemModelManager itemModelManager;

    public CampfireBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemModelManager = ctx.itemModelManager();
    }

    @Override
    public CampfireBlockEntityRenderState createRenderState() {
        return new CampfireBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(CampfireBlockEntity arg, CampfireBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        arg2.facing = arg.getCachedState().get(CampfireBlock.FACING);
        int i = (int)arg.getPos().asLong();
        arg2.cookedItemStates = new ArrayList<ItemRenderState>();
        for (int j = 0; j < arg.getItemsBeingCooked().size(); ++j) {
            ItemRenderState lv = new ItemRenderState();
            this.itemModelManager.clearAndUpdate(lv, arg.getItemsBeingCooked().get(j), ItemDisplayContext.FIXED, arg.getWorld(), null, i + j);
            arg2.cookedItemStates.add(lv);
        }
    }

    @Override
    public void render(CampfireBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        Direction lv = arg.facing;
        List<ItemRenderState> list = arg.cookedItemStates;
        for (int i = 0; i < list.size(); ++i) {
            ItemRenderState lv2 = list.get(i);
            if (lv2.isEmpty()) continue;
            arg2.push();
            arg2.translate(0.5f, 0.44921875f, 0.5f);
            Direction lv3 = Direction.fromHorizontalQuarterTurns((i + lv.getHorizontalQuarterTurns()) % 4);
            float f = -lv3.getPositiveHorizontalDegrees();
            arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f));
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            arg2.translate(-0.3125f, -0.3125f, 0.0f);
            arg2.scale(0.375f, 0.375f, 0.375f);
            lv2.render(arg2, arg3, arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
            arg2.pop();
        }
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

