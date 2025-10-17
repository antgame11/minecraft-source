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
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.BrushableBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BrushableBlockEntityRenderer
implements BlockEntityRenderer<BrushableBlockEntity, BrushableBlockEntityRenderState> {
    private final ItemModelManager itemModelManager;

    public BrushableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemModelManager = context.itemModelManager();
    }

    @Override
    public BrushableBlockEntityRenderState createRenderState() {
        return new BrushableBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(BrushableBlockEntity arg, BrushableBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        arg2.face = arg.getHitDirection();
        arg2.dusted = arg.getCachedState().get(Properties.DUSTED);
        if (arg.getWorld() != null && arg.getHitDirection() != null) {
            arg2.lightmapCoordinates = WorldRenderer.getLightmapCoordinates(WorldRenderer.BrightnessGetter.DEFAULT, arg.getWorld(), arg.getCachedState(), arg.getPos().offset(arg.getHitDirection()));
        }
        this.itemModelManager.clearAndUpdate(arg2.itemRenderState, arg.getItem(), ItemDisplayContext.FIXED, arg.getWorld(), null, 0);
    }

    @Override
    public void render(BrushableBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        if (arg.dusted <= 0 || arg.face == null || arg.itemRenderState.isEmpty()) {
            return;
        }
        arg2.push();
        arg2.translate(0.0f, 0.5f, 0.0f);
        float[] fs = this.getTranslation(arg.face, arg.dusted);
        arg2.translate(fs[0], fs[1], fs[2]);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(75.0f));
        boolean bl = arg.face == Direction.EAST || arg.face == Direction.WEST;
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((bl ? 90 : 0) + 11));
        arg2.scale(0.5f, 0.5f, 0.5f);
        arg.itemRenderState.render(arg2, arg3, arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
        arg2.pop();
    }

    private float[] getTranslation(Direction direction, int dustedLevel) {
        float[] fs = new float[]{0.5f, 0.0f, 0.5f};
        float f = (float)dustedLevel / 10.0f * 0.75f;
        switch (direction) {
            case EAST: {
                fs[0] = 0.73f + f;
                break;
            }
            case WEST: {
                fs[0] = 0.25f - f;
                break;
            }
            case UP: {
                fs[1] = 0.25f + f;
                break;
            }
            case DOWN: {
                fs[1] = -0.23f - f;
                break;
            }
            case NORTH: {
                fs[2] = 0.25f - f;
                break;
            }
            case SOUTH: {
                fs[2] = 0.73f + f;
            }
        }
        return fs;
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

