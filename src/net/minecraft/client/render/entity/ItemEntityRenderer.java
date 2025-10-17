/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class ItemEntityRenderer
extends EntityRenderer<ItemEntity, ItemEntityRenderState> {
    private static final float field_56954 = 0.0625f;
    private static final float field_32924 = 0.15f;
    private static final float field_56955 = 0.0625f;
    private final ItemModelManager itemModelManager;
    private final Random random = Random.create();

    public ItemEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.itemModelManager = arg.getItemModelManager();
        this.shadowRadius = 0.15f;
        this.shadowOpacity = 0.75f;
    }

    @Override
    public ItemEntityRenderState createRenderState() {
        return new ItemEntityRenderState();
    }

    @Override
    public void updateRenderState(ItemEntity arg, ItemEntityRenderState arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        arg2.uniqueOffset = arg.uniqueOffset;
        arg2.update(arg, arg.getStack(), this.itemModelManager);
    }

    @Override
    public void render(ItemEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        if (arg.itemRenderState.isEmpty()) {
            return;
        }
        arg2.push();
        Box lv = arg.itemRenderState.getModelBoundingBox();
        float f = -((float)lv.minY) + 0.0625f;
        float g = MathHelper.sin(arg.age / 10.0f + arg.uniqueOffset) * 0.1f + 0.1f;
        arg2.translate(0.0f, g + f, 0.0f);
        float h = ItemEntity.getRotation(arg.age, arg.uniqueOffset);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotation(h));
        ItemEntityRenderer.render(arg2, arg3, arg.light, arg, this.random, lv);
        arg2.pop();
        super.render(arg, arg2, arg3, arg4);
    }

    public static void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, ItemStackEntityRenderState state, Random random) {
        ItemEntityRenderer.render(matrices, queue, light, state, random, state.itemRenderState.getModelBoundingBox());
    }

    public static void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, ItemStackEntityRenderState state, Random random, Box boundingBox) {
        int j = state.renderedAmount;
        if (j == 0) {
            return;
        }
        random.setSeed(state.seed);
        ItemRenderState lv = state.itemRenderState;
        float f = (float)boundingBox.getLengthZ();
        if (f > 0.0625f) {
            lv.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor);
            for (int k = 1; k < j; ++k) {
                matrices.push();
                float g = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                float h = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                float l = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                matrices.translate(g, h, l);
                lv.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor);
                matrices.pop();
            }
        } else {
            float m = f * 1.5f;
            matrices.translate(0.0f, 0.0f, -(m * (float)(j - 1) / 2.0f));
            lv.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor);
            matrices.translate(0.0f, 0.0f, m);
            for (int n = 1; n < j; ++n) {
                matrices.push();
                float h = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                float l = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                matrices.translate(h, l, 0.0f);
                lv.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor);
                matrices.pop();
                matrices.translate(0.0f, 0.0f, m);
            }
        }
    }

    public static void renderStack(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, ItemStackEntityRenderState state, Random random) {
        Box lv = state.itemRenderState.getModelBoundingBox();
        int j = state.renderedAmount;
        if (j == 0) {
            return;
        }
        random.setSeed(state.seed);
        ItemRenderState lv2 = state.itemRenderState;
        float f = (float)lv.getLengthZ();
        if (f > 0.0625f) {
            lv2.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor);
            for (int k = 1; k < j; ++k) {
                matrices.push();
                float g = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                float h = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                float l = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                matrices.translate(g, h, l);
                lv2.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor);
                matrices.pop();
            }
        } else {
            float m = f * 1.5f;
            matrices.translate(0.0f, 0.0f, -(m * (float)(j - 1) / 2.0f));
            lv2.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor);
            matrices.translate(0.0f, 0.0f, m);
            for (int n = 1; n < j; ++n) {
                matrices.push();
                float h = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                float l = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                matrices.translate(h, l, 0.0f);
                lv2.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor);
                matrices.pop();
                matrices.translate(0.0f, 0.0f, m);
            }
        }
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

