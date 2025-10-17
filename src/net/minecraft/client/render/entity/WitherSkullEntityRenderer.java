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
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.WitherSkullEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class WitherSkullEntityRenderer
extends EntityRenderer<WitherSkullEntity, WitherSkullEntityRenderState> {
    private static final Identifier INVULNERABLE_TEXTURE = Identifier.ofVanilla("textures/entity/wither/wither_invulnerable.png");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/wither/wither.png");
    private final SkullEntityModel model;

    public WitherSkullEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.model = new SkullEntityModel(arg.getPart(EntityModelLayers.WITHER_SKULL));
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 35).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    protected int getBlockLight(WitherSkullEntity arg, BlockPos arg2) {
        return 15;
    }

    @Override
    public void render(WitherSkullEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        arg2.push();
        arg2.scale(-1.0f, -1.0f, 1.0f);
        arg3.submitModel(this.model, arg.skullState, arg2, this.model.getLayer(this.getTexture(arg)), arg.light, OverlayTexture.DEFAULT_UV, arg.outlineColor, null);
        arg2.pop();
        super.render(arg, arg2, arg3, arg4);
    }

    private Identifier getTexture(WitherSkullEntityRenderState state) {
        return state.charged ? INVULNERABLE_TEXTURE : TEXTURE;
    }

    @Override
    public WitherSkullEntityRenderState createRenderState() {
        return new WitherSkullEntityRenderState();
    }

    @Override
    public void updateRenderState(WitherSkullEntity arg, WitherSkullEntityRenderState arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        arg2.charged = arg.isCharged();
        arg2.skullState.poweredTicks = 0.0f;
        arg2.skullState.yaw = arg.getLerpedYaw(f);
        arg2.skullState.pitch = arg.getLerpedPitch(f);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

