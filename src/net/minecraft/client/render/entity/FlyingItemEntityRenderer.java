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
import net.minecraft.client.render.entity.state.FlyingItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class FlyingItemEntityRenderer<T extends Entity>
extends EntityRenderer<T, FlyingItemEntityRenderState> {
    private final ItemModelManager itemModelManager;
    private final float scale;
    private final boolean lit;

    public FlyingItemEntityRenderer(EntityRendererFactory.Context ctx, float scale, boolean lit) {
        super(ctx);
        this.itemModelManager = ctx.getItemModelManager();
        this.scale = scale;
        this.lit = lit;
    }

    public FlyingItemEntityRenderer(EntityRendererFactory.Context arg) {
        this(arg, 1.0f, false);
    }

    @Override
    protected int getBlockLight(T entity, BlockPos pos) {
        return this.lit ? 15 : super.getBlockLight(entity, pos);
    }

    @Override
    public void render(FlyingItemEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        arg2.push();
        arg2.scale(this.scale, this.scale, this.scale);
        arg2.multiply(arg4.orientation);
        arg.itemRenderState.render(arg2, arg3, arg.light, OverlayTexture.DEFAULT_UV, arg.outlineColor);
        arg2.pop();
        super.render(arg, arg2, arg3, arg4);
    }

    @Override
    public FlyingItemEntityRenderState createRenderState() {
        return new FlyingItemEntityRenderState();
    }

    @Override
    public void updateRenderState(T arg, FlyingItemEntityRenderState arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        this.itemModelManager.updateForNonLivingEntity(arg2.itemRenderState, ((FlyingItemEntity)arg).getStack(), ItemDisplayContext.GROUND, (Entity)arg);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

