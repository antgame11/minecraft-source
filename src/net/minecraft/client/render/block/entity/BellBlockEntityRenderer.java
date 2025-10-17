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
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.model.BellBlockModel;
import net.minecraft.client.render.block.entity.state.BellBlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BellBlockEntityRenderer
implements BlockEntityRenderer<BellBlockEntity, BellBlockEntityRenderState> {
    public static final SpriteIdentifier BELL_BODY_TEXTURE = TexturedRenderLayers.ENTITY_SPRITE_MAPPER.mapVanilla("bell/bell_body");
    private final SpriteHolder materials;
    private final BellBlockModel bellBody;

    public BellBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.materials = context.spriteHolder();
        this.bellBody = new BellBlockModel(context.getLayerModelPart(EntityModelLayers.BELL));
    }

    @Override
    public BellBlockEntityRenderState createRenderState() {
        return new BellBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(BellBlockEntity arg, BellBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        arg2.ringTicks = (float)arg.ringTicks + f;
        arg2.shakeDirection = arg.ringing ? arg.lastSideHit : null;
    }

    @Override
    public void render(BellBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        BellBlockModel.BellModelState lv = new BellBlockModel.BellModelState(arg.ringTicks, arg.shakeDirection);
        this.bellBody.setAngles(lv);
        RenderLayer lv2 = BELL_BODY_TEXTURE.getRenderLayer(RenderLayer::getEntitySolid);
        arg3.submitModel(this.bellBody, lv, arg2, lv2, arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, -1, this.materials.getSprite(BELL_BODY_TEXTURE), 0, arg.crumblingOverlay);
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

