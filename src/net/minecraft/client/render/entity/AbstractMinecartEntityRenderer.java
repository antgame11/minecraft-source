/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.MinecartEntityModel;
import net.minecraft.client.render.entity.state.MinecartEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.DefaultMinecartController;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.minecraft.entity.vehicle.MinecartController;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractMinecartEntityRenderer<T extends AbstractMinecartEntity, S extends MinecartEntityRenderState>
extends EntityRenderer<T, S> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/minecart.png");
    private static final float field_56953 = 0.75f;
    protected final MinecartEntityModel model;

    public AbstractMinecartEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer) {
        super(ctx);
        this.shadowRadius = 0.7f;
        this.model = new MinecartEntityModel(ctx.getPart(layer));
    }

    @Override
    public void render(S arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        BlockState lv;
        super.render(arg, arg2, arg3, arg4);
        arg2.push();
        long l = ((MinecartEntityRenderState)arg).hash;
        float f = (((float)(l >> 16 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        float g = (((float)(l >> 20 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        float h = (((float)(l >> 24 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        arg2.translate(f, g, h);
        if (((MinecartEntityRenderState)arg).usesExperimentalController) {
            AbstractMinecartEntityRenderer.transformExperimentalControllerMinecart(arg, arg2);
        } else {
            AbstractMinecartEntityRenderer.transformDefaultControllerMinecart(arg, arg2);
        }
        float i = ((MinecartEntityRenderState)arg).damageWobbleTicks;
        if (i > 0.0f) {
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(i) * i * ((MinecartEntityRenderState)arg).damageWobbleStrength / 10.0f * (float)((MinecartEntityRenderState)arg).damageWobbleSide));
        }
        if ((lv = ((MinecartEntityRenderState)arg).containedBlock).getRenderType() != BlockRenderType.INVISIBLE) {
            arg2.push();
            arg2.scale(0.75f, 0.75f, 0.75f);
            arg2.translate(-0.5f, (float)(((MinecartEntityRenderState)arg).blockOffset - 8) / 16.0f, 0.5f);
            arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
            this.renderBlock(arg, lv, arg2, arg3, ((MinecartEntityRenderState)arg).light);
            arg2.pop();
        }
        arg2.scale(-1.0f, -1.0f, 1.0f);
        arg3.submitModel(this.model, arg, arg2, this.model.getLayer(TEXTURE), ((MinecartEntityRenderState)arg).light, OverlayTexture.DEFAULT_UV, ((MinecartEntityRenderState)arg).outlineColor, null);
        arg2.pop();
    }

    private static <S extends MinecartEntityRenderState> void transformExperimentalControllerMinecart(S state, MatrixStack matrices) {
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.lerpedYaw));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-state.lerpedPitch));
        matrices.translate(0.0f, 0.375f, 0.0f);
    }

    private static <S extends MinecartEntityRenderState> void transformDefaultControllerMinecart(S state, MatrixStack matrices) {
        double d = state.x;
        double e = state.y;
        double f = state.z;
        float g = state.lerpedPitch;
        float h = state.lerpedYaw;
        if (state.presentPos != null && state.futurePos != null && state.pastPos != null) {
            Vec3d lv = state.futurePos;
            Vec3d lv2 = state.pastPos;
            matrices.translate(state.presentPos.x - d, (lv.y + lv2.y) / 2.0 - e, state.presentPos.z - f);
            Vec3d lv3 = lv2.add(-lv.x, -lv.y, -lv.z);
            if (lv3.length() != 0.0) {
                lv3 = lv3.normalize();
                h = (float)(Math.atan2(lv3.z, lv3.x) * 180.0 / Math.PI);
                g = (float)(Math.atan(lv3.y) * 73.0);
            }
        }
        matrices.translate(0.0f, 0.375f, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - h));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-g));
    }

    @Override
    public void updateRenderState(T arg, S arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        MinecartController minecartController = ((AbstractMinecartEntity)arg).getController();
        if (minecartController instanceof ExperimentalMinecartController) {
            ExperimentalMinecartController lv = (ExperimentalMinecartController)minecartController;
            AbstractMinecartEntityRenderer.updateFromExperimentalController(arg, lv, arg2, f);
            ((MinecartEntityRenderState)arg2).usesExperimentalController = true;
        } else {
            minecartController = ((AbstractMinecartEntity)arg).getController();
            if (minecartController instanceof DefaultMinecartController) {
                DefaultMinecartController lv2 = (DefaultMinecartController)minecartController;
                AbstractMinecartEntityRenderer.updateFromDefaultController(arg, lv2, arg2, f);
                ((MinecartEntityRenderState)arg2).usesExperimentalController = false;
            }
        }
        long l = (long)((Entity)arg).getId() * 493286711L;
        ((MinecartEntityRenderState)arg2).hash = l * l * 4392167121L + l * 98761L;
        ((MinecartEntityRenderState)arg2).damageWobbleTicks = (float)((VehicleEntity)arg).getDamageWobbleTicks() - f;
        ((MinecartEntityRenderState)arg2).damageWobbleSide = ((VehicleEntity)arg).getDamageWobbleSide();
        ((MinecartEntityRenderState)arg2).damageWobbleStrength = Math.max(((VehicleEntity)arg).getDamageWobbleStrength() - f, 0.0f);
        ((MinecartEntityRenderState)arg2).blockOffset = ((AbstractMinecartEntity)arg).getBlockOffset();
        ((MinecartEntityRenderState)arg2).containedBlock = ((AbstractMinecartEntity)arg).getContainedBlock();
    }

    private static <T extends AbstractMinecartEntity, S extends MinecartEntityRenderState> void updateFromExperimentalController(T minecart, ExperimentalMinecartController controller, S state, float tickProgress) {
        if (controller.hasCurrentLerpSteps()) {
            state.lerpedPos = controller.getLerpedPosition(tickProgress);
            state.lerpedPitch = controller.getLerpedPitch(tickProgress);
            state.lerpedYaw = controller.getLerpedYaw(tickProgress);
        } else {
            state.lerpedPos = null;
            state.lerpedPitch = minecart.getPitch();
            state.lerpedYaw = minecart.getYaw();
        }
    }

    private static <T extends AbstractMinecartEntity, S extends MinecartEntityRenderState> void updateFromDefaultController(T minecart, DefaultMinecartController controller, S state, float tickProgress) {
        float g = 0.3f;
        state.lerpedPitch = minecart.getLerpedPitch(tickProgress);
        state.lerpedYaw = minecart.getLerpedYaw(tickProgress);
        double d = state.x;
        double e = state.y;
        double h = state.z;
        Vec3d lv = controller.snapPositionToRail(d, e, h);
        if (lv != null) {
            state.presentPos = lv;
            Vec3d lv2 = controller.simulateMovement(d, e, h, 0.3f);
            Vec3d lv3 = controller.simulateMovement(d, e, h, -0.3f);
            state.futurePos = Objects.requireNonNullElse(lv2, lv);
            state.pastPos = Objects.requireNonNullElse(lv3, lv);
        } else {
            state.presentPos = null;
            state.futurePos = null;
            state.pastPos = null;
        }
    }

    protected void renderBlock(S state, BlockState blockState, MatrixStack matrices, OrderedRenderCommandQueue arg4, int light) {
        arg4.submitBlock(matrices, blockState, light, OverlayTexture.DEFAULT_UV, ((MinecartEntityRenderState)state).outlineColor);
    }

    @Override
    protected Box getBoundingBox(T arg) {
        Box lv = super.getBoundingBox(arg);
        if (!((AbstractMinecartEntity)arg).getContainedBlock().isAir()) {
            return lv.stretch(0.0, (float)((AbstractMinecartEntity)arg).getBlockOffset() * 0.75f / 16.0f, 0.0);
        }
        return lv;
    }

    @Override
    public Vec3d getPositionOffset(S arg) {
        Vec3d lv = super.getPositionOffset(arg);
        if (((MinecartEntityRenderState)arg).usesExperimentalController && ((MinecartEntityRenderState)arg).lerpedPos != null) {
            return lv.add(((MinecartEntityRenderState)arg).lerpedPos.x - ((MinecartEntityRenderState)arg).x, ((MinecartEntityRenderState)arg).lerpedPos.y - ((MinecartEntityRenderState)arg).y, ((MinecartEntityRenderState)arg).lerpedPos.z - ((MinecartEntityRenderState)arg).z);
        }
        return lv;
    }
}

