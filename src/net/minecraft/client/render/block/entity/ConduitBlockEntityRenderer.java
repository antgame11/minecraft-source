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
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.ConduitBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.SpriteMapper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ConduitBlockEntityRenderer
implements BlockEntityRenderer<ConduitBlockEntity, ConduitBlockEntityRenderState> {
    public static final SpriteMapper SPRITE_MAPPER = new SpriteMapper(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, "entity/conduit");
    public static final SpriteIdentifier BASE_TEXTURE = SPRITE_MAPPER.mapVanilla("base");
    public static final SpriteIdentifier CAGE_TEXTURE = SPRITE_MAPPER.mapVanilla("cage");
    public static final SpriteIdentifier WIND_TEXTURE = SPRITE_MAPPER.mapVanilla("wind");
    public static final SpriteIdentifier WIND_VERTICAL_TEXTURE = SPRITE_MAPPER.mapVanilla("wind_vertical");
    public static final SpriteIdentifier OPEN_EYE_TEXTURE = SPRITE_MAPPER.mapVanilla("open_eye");
    public static final SpriteIdentifier CLOSED_EYE_TEXTURE = SPRITE_MAPPER.mapVanilla("closed_eye");
    private final SpriteHolder materials;
    private final ModelPart conduitEye;
    private final ModelPart conduitWind;
    private final ModelPart conduitShell;
    private final ModelPart conduit;

    public ConduitBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.materials = ctx.spriteHolder();
        this.conduitEye = ctx.getLayerModelPart(EntityModelLayers.CONDUIT_EYE);
        this.conduitWind = ctx.getLayerModelPart(EntityModelLayers.CONDUIT_WIND);
        this.conduitShell = ctx.getLayerModelPart(EntityModelLayers.CONDUIT_SHELL);
        this.conduit = ctx.getLayerModelPart(EntityModelLayers.CONDUIT);
    }

    public static TexturedModelData getEyeTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild("eye", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, 0.0f, 8.0f, 8.0f, 0.0f, new Dilation(0.01f)), ModelTransform.NONE);
        return TexturedModelData.of(lv, 16, 16);
    }

    public static TexturedModelData getWindTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild("wind", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 32);
    }

    public static TexturedModelData getShellTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild("shell", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 32, 16);
    }

    public static TexturedModelData getPlainTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild("shell", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 32, 16);
    }

    @Override
    public ConduitBlockEntityRenderState createRenderState() {
        return new ConduitBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(ConduitBlockEntity arg, ConduitBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        arg2.active = arg.isActive();
        arg2.rotation = arg.getRotation(arg.isActive() ? f : 0.0f);
        arg2.ticks = (float)arg.ticks + f;
        arg2.rotationPhase = arg.ticks / 66 % 3;
        arg2.eyeOpen = arg.isEyeOpen();
    }

    @Override
    public void render(ConduitBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        if (!arg.active) {
            arg2.push();
            arg2.translate(0.5f, 0.5f, 0.5f);
            arg2.multiply(new Quaternionf().rotationY(arg.rotation * ((float)Math.PI / 180)));
            arg3.submitModelPart(this.conduitShell, arg2, BASE_TEXTURE.getRenderLayer(RenderLayer::getEntitySolid), arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, this.materials.getSprite(BASE_TEXTURE), -1, arg.crumblingOverlay);
            arg2.pop();
            return;
        }
        float f = arg.rotation * 57.295776f;
        float g = MathHelper.sin(arg.ticks * 0.1f) / 2.0f + 0.5f;
        g = g * g + g;
        arg2.push();
        arg2.translate(0.5f, 0.3f + g * 0.2f, 0.5f);
        Vector3f vector3f = new Vector3f(0.5f, 1.0f, 0.5f).normalize();
        arg2.multiply(new Quaternionf().rotationAxis(f * ((float)Math.PI / 180), vector3f));
        arg3.submitModelPart(this.conduit, arg2, CAGE_TEXTURE.getRenderLayer(RenderLayer::getEntityCutoutNoCull), arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, this.materials.getSprite(CAGE_TEXTURE), -1, arg.crumblingOverlay);
        arg2.pop();
        arg2.push();
        arg2.translate(0.5f, 0.5f, 0.5f);
        if (arg.rotationPhase == 1) {
            arg2.multiply(new Quaternionf().rotationX(1.5707964f));
        } else if (arg.rotationPhase == 2) {
            arg2.multiply(new Quaternionf().rotationZ(1.5707964f));
        }
        SpriteIdentifier lv = arg.rotationPhase == 1 ? WIND_VERTICAL_TEXTURE : WIND_TEXTURE;
        RenderLayer lv2 = lv.getRenderLayer(RenderLayer::getEntityCutoutNoCull);
        Sprite lv3 = this.materials.getSprite(lv);
        arg3.submitModelPart(this.conduitWind, arg2, lv2, arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, lv3);
        arg2.pop();
        arg2.push();
        arg2.translate(0.5f, 0.5f, 0.5f);
        arg2.scale(0.875f, 0.875f, 0.875f);
        arg2.multiply(new Quaternionf().rotationXYZ((float)Math.PI, 0.0f, (float)Math.PI));
        arg3.submitModelPart(this.conduitWind, arg2, lv2, arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, lv3);
        arg2.pop();
        arg2.push();
        arg2.translate(0.5f, 0.3f + g * 0.2f, 0.5f);
        arg2.scale(0.5f, 0.5f, 0.5f);
        arg2.multiply(arg4.orientation);
        arg2.multiply(new Quaternionf().rotationZ((float)Math.PI).rotateY((float)Math.PI));
        float h = 1.3333334f;
        arg2.scale(1.3333334f, 1.3333334f, 1.3333334f);
        SpriteIdentifier lv4 = arg.eyeOpen ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE;
        arg3.submitModelPart(this.conduitEye, arg2, lv4.getRenderLayer(RenderLayer::getEntityCutoutNoCull), arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, this.materials.getSprite(lv4));
        arg2.pop();
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

