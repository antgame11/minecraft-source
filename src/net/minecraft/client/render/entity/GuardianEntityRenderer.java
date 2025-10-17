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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.GuardianEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.GuardianEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GuardianEntityRenderer
extends MobEntityRenderer<GuardianEntity, GuardianEntityRenderState, GuardianEntityModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/guardian.png");
    private static final Identifier EXPLOSION_BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/guardian_beam.png");
    private static final RenderLayer LAYER = RenderLayer.getEntityCutoutNoCull(EXPLOSION_BEAM_TEXTURE);

    public GuardianEntityRenderer(EntityRendererFactory.Context arg) {
        this(arg, 0.5f, EntityModelLayers.GUARDIAN);
    }

    protected GuardianEntityRenderer(EntityRendererFactory.Context ctx, float shadowRadius, EntityModelLayer layer) {
        super(ctx, new GuardianEntityModel(ctx.getPart(layer)), shadowRadius);
    }

    @Override
    public boolean shouldRender(GuardianEntity arg, Frustum arg2, double d, double e, double f) {
        LivingEntity lv;
        if (super.shouldRender(arg, arg2, d, e, f)) {
            return true;
        }
        if (arg.hasBeamTarget() && (lv = arg.getBeamTarget()) != null) {
            Vec3d lv2 = this.fromLerpedPosition(lv, (double)lv.getHeight() * 0.5, 1.0f);
            Vec3d lv3 = this.fromLerpedPosition(arg, arg.getStandingEyeHeight(), 1.0f);
            return arg2.isVisible(new Box(lv3.x, lv3.y, lv3.z, lv2.x, lv2.y, lv2.z));
        }
        return false;
    }

    private Vec3d fromLerpedPosition(LivingEntity entity, double yOffset, float delta) {
        double e = MathHelper.lerp((double)delta, entity.lastRenderX, entity.getX());
        double g = MathHelper.lerp((double)delta, entity.lastRenderY, entity.getY()) + yOffset;
        double h = MathHelper.lerp((double)delta, entity.lastRenderZ, entity.getZ());
        return new Vec3d(e, g, h);
    }

    @Override
    public void render(GuardianEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        super.render(arg, arg2, arg3, arg4);
        Vec3d lv = arg.beamTargetPos;
        if (lv != null) {
            float f = arg.beamTicks * 0.5f % 1.0f;
            arg2.push();
            arg2.translate(0.0f, arg.standingEyeHeight, 0.0f);
            GuardianEntityRenderer.renderBeam(arg2, arg3, lv.subtract(arg.cameraPosVec), arg.beamTicks, arg.beamProgress, f);
            arg2.pop();
        }
    }

    private static void renderBeam(MatrixStack arg3, OrderedRenderCommandQueue arg22, Vec3d arg32, float beamTicks, float g, float h) {
        float i = (float)(arg32.length() + 1.0);
        arg32 = arg32.normalize();
        float j = (float)Math.acos(arg32.y);
        float k = 1.5707964f - (float)Math.atan2(arg32.z, arg32.x);
        arg3.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(k * 57.295776f));
        arg3.multiply(RotationAxis.POSITIVE_X.rotationDegrees(j * 57.295776f));
        float l = beamTicks * 0.05f * -1.5f;
        float m = g * g;
        int n = 64 + (int)(m * 191.0f);
        int o = 32 + (int)(m * 191.0f);
        int p = 128 - (int)(m * 64.0f);
        float q = 0.2f;
        float r = 0.282f;
        float s = MathHelper.cos(l + 2.3561945f) * 0.282f;
        float t = MathHelper.sin(l + 2.3561945f) * 0.282f;
        float u = MathHelper.cos(l + 0.7853982f) * 0.282f;
        float v = MathHelper.sin(l + 0.7853982f) * 0.282f;
        float w = MathHelper.cos(l + 3.926991f) * 0.282f;
        float x = MathHelper.sin(l + 3.926991f) * 0.282f;
        float y = MathHelper.cos(l + 5.4977875f) * 0.282f;
        float z = MathHelper.sin(l + 5.4977875f) * 0.282f;
        float aa = MathHelper.cos(l + (float)Math.PI) * 0.2f;
        float ab = MathHelper.sin(l + (float)Math.PI) * 0.2f;
        float ac = MathHelper.cos(l + 0.0f) * 0.2f;
        float ad = MathHelper.sin(l + 0.0f) * 0.2f;
        float ae = MathHelper.cos(l + 1.5707964f) * 0.2f;
        float af = MathHelper.sin(l + 1.5707964f) * 0.2f;
        float ag = MathHelper.cos(l + 4.712389f) * 0.2f;
        float ah = MathHelper.sin(l + 4.712389f) * 0.2f;
        float ai = i;
        float aj = 0.0f;
        float ak = 0.4999f;
        float al = -1.0f + h;
        float am = al + i * 2.5f;
        arg22.submitCustom(arg3, LAYER, (arg, arg2) -> {
            GuardianEntityRenderer.vertex(arg2, arg, aa, ai, ab, n, o, p, 0.4999f, am);
            GuardianEntityRenderer.vertex(arg2, arg, aa, 0.0f, ab, n, o, p, 0.4999f, al);
            GuardianEntityRenderer.vertex(arg2, arg, ac, 0.0f, ad, n, o, p, 0.0f, al);
            GuardianEntityRenderer.vertex(arg2, arg, ac, ai, ad, n, o, p, 0.0f, am);
            GuardianEntityRenderer.vertex(arg2, arg, ae, ai, af, n, o, p, 0.4999f, am);
            GuardianEntityRenderer.vertex(arg2, arg, ae, 0.0f, af, n, o, p, 0.4999f, al);
            GuardianEntityRenderer.vertex(arg2, arg, ag, 0.0f, ah, n, o, p, 0.0f, al);
            GuardianEntityRenderer.vertex(arg2, arg, ag, ai, ah, n, o, p, 0.0f, am);
            float ac = MathHelper.floor(beamTicks) % 2 == 0 ? 0.5f : 0.0f;
            GuardianEntityRenderer.vertex(arg2, arg, s, ai, t, n, o, p, 0.5f, ac + 0.5f);
            GuardianEntityRenderer.vertex(arg2, arg, u, ai, v, n, o, p, 1.0f, ac + 0.5f);
            GuardianEntityRenderer.vertex(arg2, arg, y, ai, z, n, o, p, 1.0f, ac);
            GuardianEntityRenderer.vertex(arg2, arg, w, ai, x, n, o, p, 0.5f, ac);
        });
    }

    private static void vertex(VertexConsumer vertexConsumer, MatrixStack.Entry matrix, float x, float y, float z, int red, int green, int blue, float u, float v) {
        vertexConsumer.vertex(matrix, x, y, z).color(red, green, blue, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(matrix, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public Identifier getTexture(GuardianEntityRenderState arg) {
        return TEXTURE;
    }

    @Override
    public GuardianEntityRenderState createRenderState() {
        return new GuardianEntityRenderState();
    }

    @Override
    public void updateRenderState(GuardianEntity arg, GuardianEntityRenderState arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        arg2.spikesExtension = arg.getSpikesExtension(f);
        arg2.tailAngle = arg.getTailAngle(f);
        arg2.cameraPosVec = arg.getCameraPosVec(f);
        Entity lv = GuardianEntityRenderer.getBeamTarget(arg);
        if (lv != null) {
            arg2.rotationVec = arg.getRotationVec(f);
            arg2.lookAtPos = lv.getCameraPosVec(f);
        } else {
            arg2.rotationVec = null;
            arg2.lookAtPos = null;
        }
        LivingEntity lv2 = arg.getBeamTarget();
        if (lv2 != null) {
            arg2.beamProgress = arg.getBeamProgress(f);
            arg2.beamTicks = arg.getBeamTicks() + f;
            arg2.beamTargetPos = this.fromLerpedPosition(lv2, (double)lv2.getHeight() * 0.5, f);
        } else {
            arg2.beamTargetPos = null;
        }
    }

    @Nullable
    private static Entity getBeamTarget(GuardianEntity guardian) {
        Entity lv = MinecraftClient.getInstance().getCameraEntity();
        if (guardian.hasBeamTarget()) {
            return guardian.getBeamTarget();
        }
        return lv;
    }

    @Override
    public /* synthetic */ Identifier getTexture(LivingEntityRenderState state) {
        return this.getTexture((GuardianEntityRenderState)state);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

