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
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.ClientPlayerLikeEntity;
import net.minecraft.client.network.ClientPlayerLikeState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.Deadmau5FeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.PlayerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.feature.ShoulderParrotFeatureRenderer;
import net.minecraft.client.render.entity.feature.StuckArrowsFeatureRenderer;
import net.minecraft.client.render.entity.feature.StuckStingersFeatureRenderer;
import net.minecraft.client.render.entity.feature.TridentRiptideFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EquipmentModelData;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class PlayerEntityRenderer<AvatarlikeEntity extends PlayerLikeEntity>
extends LivingEntityRenderer<AvatarlikeEntity, PlayerEntityRenderState, PlayerEntityModel> {
    public PlayerEntityRenderer(EntityRendererFactory.Context ctx, boolean slim) {
        super(ctx, new PlayerEntityModel(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER), slim), 0.5f);
        this.addFeature(new ArmorFeatureRenderer<PlayerEntityRenderState, PlayerEntityModel, PlayerEntityModel>(this, EquipmentModelData.mapToEntityModel((EquipmentModelData<EntityModelLayer>)(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER_EQUIPMENT), ctx.getEntityModels(), arg -> new PlayerEntityModel((ModelPart)arg, slim)), ctx.getEquipmentRenderer()));
        this.addFeature(new PlayerHeldItemFeatureRenderer<PlayerEntityRenderState, PlayerEntityModel>(this));
        this.addFeature(new StuckArrowsFeatureRenderer(this, ctx));
        this.addFeature(new Deadmau5FeatureRenderer(this, ctx.getEntityModels()));
        this.addFeature(new CapeFeatureRenderer(this, ctx.getEntityModels(), ctx.getEquipmentModelLoader()));
        this.addFeature(new HeadFeatureRenderer<PlayerEntityRenderState, PlayerEntityModel>(this, ctx.getEntityModels(), ctx.getPlayerSkinCache()));
        this.addFeature(new ElytraFeatureRenderer<PlayerEntityRenderState, PlayerEntityModel>(this, ctx.getEntityModels(), ctx.getEquipmentRenderer()));
        this.addFeature(new ShoulderParrotFeatureRenderer(this, ctx.getEntityModels()));
        this.addFeature(new TridentRiptideFeatureRenderer(this, ctx.getEntityModels()));
        this.addFeature(new StuckStingersFeatureRenderer(this, ctx));
    }

    @Override
    protected boolean shouldRenderFeatures(PlayerEntityRenderState arg) {
        return !arg.spectator;
    }

    @Override
    public Vec3d getPositionOffset(PlayerEntityRenderState arg) {
        Vec3d lv = super.getPositionOffset(arg);
        if (arg.isInSneakingPose) {
            return lv.add(0.0, (double)(arg.baseScale * -2.0f) / 16.0, 0.0);
        }
        return lv;
    }

    private static BipedEntityModel.ArmPose getArmPose(PlayerLikeEntity player, Arm arm) {
        ItemStack lv = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack lv2 = player.getStackInHand(Hand.OFF_HAND);
        BipedEntityModel.ArmPose lv3 = PlayerEntityRenderer.getArmPose(player, lv, Hand.MAIN_HAND);
        BipedEntityModel.ArmPose lv4 = PlayerEntityRenderer.getArmPose(player, lv2, Hand.OFF_HAND);
        if (lv3.isTwoHanded()) {
            BipedEntityModel.ArmPose armPose = lv4 = lv2.isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;
        }
        if (player.getMainArm() == arm) {
            return lv3;
        }
        return lv4;
    }

    private static BipedEntityModel.ArmPose getArmPose(PlayerLikeEntity player, ItemStack stack, Hand hand) {
        if (stack.isEmpty()) {
            return BipedEntityModel.ArmPose.EMPTY;
        }
        if (!player.handSwinging && stack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(stack)) {
            return BipedEntityModel.ArmPose.CROSSBOW_HOLD;
        }
        if (player.getActiveHand() == hand && player.getItemUseTimeLeft() > 0) {
            UseAction lv = stack.getUseAction();
            if (lv == UseAction.BLOCK) {
                return BipedEntityModel.ArmPose.BLOCK;
            }
            if (lv == UseAction.BOW) {
                return BipedEntityModel.ArmPose.BOW_AND_ARROW;
            }
            if (lv == UseAction.SPEAR) {
                return BipedEntityModel.ArmPose.THROW_SPEAR;
            }
            if (lv == UseAction.CROSSBOW) {
                return BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
            }
            if (lv == UseAction.SPYGLASS) {
                return BipedEntityModel.ArmPose.SPYGLASS;
            }
            if (lv == UseAction.TOOT_HORN) {
                return BipedEntityModel.ArmPose.TOOT_HORN;
            }
            if (lv == UseAction.BRUSH) {
                return BipedEntityModel.ArmPose.BRUSH;
            }
        }
        return BipedEntityModel.ArmPose.ITEM;
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState arg) {
        return arg.skinTextures.body().texturePath();
    }

    @Override
    protected void scale(PlayerEntityRenderState arg, MatrixStack arg2) {
        float f = 0.9375f;
        arg2.scale(0.9375f, 0.9375f, 0.9375f);
    }

    @Override
    protected void renderLabelIfPresent(PlayerEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        int i;
        arg2.push();
        int n = i = arg.extraEars ? -10 : 0;
        if (arg.playerName != null) {
            arg3.submitLabel(arg2, arg.nameLabelPos, i, arg.playerName, !arg.sneaking, arg.light, arg.squaredDistanceToCamera, arg4);
            Objects.requireNonNull(this.getTextRenderer());
            arg2.translate(0.0f, 9.0f * 1.15f * 0.025f, 0.0f);
        }
        if (arg.displayName != null) {
            arg3.submitLabel(arg2, arg.nameLabelPos, i, arg.displayName, !arg.sneaking, arg.light, arg.squaredDistanceToCamera, arg4);
        }
        arg2.pop();
    }

    @Override
    public PlayerEntityRenderState createRenderState() {
        return new PlayerEntityRenderState();
    }

    @Override
    public void updateRenderState(AvatarlikeEntity arg, PlayerEntityRenderState arg2, float f) {
        ItemStack lv;
        super.updateRenderState(arg, arg2, f);
        BipedEntityRenderer.updateBipedRenderState(arg, arg2, f, this.itemModelResolver);
        arg2.leftArmPose = PlayerEntityRenderer.getArmPose(arg, Arm.LEFT);
        arg2.rightArmPose = PlayerEntityRenderer.getArmPose(arg, Arm.RIGHT);
        arg2.skinTextures = ((ClientPlayerLikeEntity)arg).getSkin();
        arg2.stuckArrowCount = ((LivingEntity)arg).getStuckArrowCount();
        arg2.stingerCount = ((LivingEntity)arg).getStingerCount();
        arg2.spectator = ((Entity)arg).isSpectator();
        arg2.hatVisible = ((PlayerLikeEntity)arg).isModelPartVisible(PlayerModelPart.HAT);
        arg2.jacketVisible = ((PlayerLikeEntity)arg).isModelPartVisible(PlayerModelPart.JACKET);
        arg2.leftPantsLegVisible = ((PlayerLikeEntity)arg).isModelPartVisible(PlayerModelPart.LEFT_PANTS_LEG);
        arg2.rightPantsLegVisible = ((PlayerLikeEntity)arg).isModelPartVisible(PlayerModelPart.RIGHT_PANTS_LEG);
        arg2.leftSleeveVisible = ((PlayerLikeEntity)arg).isModelPartVisible(PlayerModelPart.LEFT_SLEEVE);
        arg2.rightSleeveVisible = ((PlayerLikeEntity)arg).isModelPartVisible(PlayerModelPart.RIGHT_SLEEVE);
        arg2.capeVisible = ((PlayerLikeEntity)arg).isModelPartVisible(PlayerModelPart.CAPE);
        this.updateGliding(arg, arg2, f);
        this.updateCape(arg, arg2, f);
        arg2.playerName = arg2.squaredDistanceToCamera < 100.0 ? ((ClientPlayerLikeEntity)arg).getMannequinName() : null;
        arg2.leftShoulderParrotVariant = ((ClientPlayerLikeEntity)arg).getShoulderParrotVariant(true);
        arg2.rightShoulderParrotVariant = ((ClientPlayerLikeEntity)arg).getShoulderParrotVariant(false);
        arg2.id = ((Entity)arg).getId();
        arg2.extraEars = ((ClientPlayerLikeEntity)arg).hasExtraEars();
        arg2.spyglassState.clear();
        if (arg2.isUsingItem && (lv = ((LivingEntity)arg).getStackInHand(arg2.activeHand)).isOf(Items.SPYGLASS)) {
            this.itemModelResolver.updateForLivingEntity(arg2.spyglassState, lv, ItemDisplayContext.HEAD, (LivingEntity)arg);
        }
    }

    @Override
    protected boolean hasLabel(AvatarlikeEntity arg, double d) {
        return super.hasLabel(arg, d) && (((LivingEntity)arg).shouldRenderName() || ((Entity)arg).hasCustomName() && arg == this.dispatcher.targetedEntity);
    }

    private void updateGliding(AvatarlikeEntity player, PlayerEntityRenderState state, float tickProgress) {
        state.glidingTicks = (float)((LivingEntity)player).getGlidingTicks() + tickProgress;
        Vec3d lv = ((Entity)player).getRotationVec(tickProgress);
        Vec3d lv2 = ((ClientPlayerLikeEntity)player).getState().getVelocity().lerp(((Entity)player).getVelocity(), tickProgress);
        if (lv2.horizontalLengthSquared() > (double)1.0E-5f && lv.horizontalLengthSquared() > (double)1.0E-5f) {
            state.applyFlyingRotation = true;
            double d = lv2.getHorizontal().normalize().dotProduct(lv.getHorizontal().normalize());
            double e = lv2.x * lv.z - lv2.z * lv.x;
            state.flyingRotation = (float)(Math.signum(e) * Math.acos(Math.min(1.0, Math.abs(d))));
        } else {
            state.applyFlyingRotation = false;
            state.flyingRotation = 0.0f;
        }
    }

    private void updateCape(AvatarlikeEntity player, PlayerEntityRenderState state, float tickProgress) {
        ClientPlayerLikeState lv = ((ClientPlayerLikeEntity)player).getState();
        double d = lv.lerpX(tickProgress) - MathHelper.lerp((double)tickProgress, ((PlayerLikeEntity)player).lastX, ((Entity)player).getX());
        double e = lv.lerpY(tickProgress) - MathHelper.lerp((double)tickProgress, ((PlayerLikeEntity)player).lastY, ((Entity)player).getY());
        double g = lv.lerpZ(tickProgress) - MathHelper.lerp((double)tickProgress, ((PlayerLikeEntity)player).lastZ, ((Entity)player).getZ());
        float h = MathHelper.lerpAngleDegrees(tickProgress, ((PlayerLikeEntity)player).lastBodyYaw, ((PlayerLikeEntity)player).bodyYaw);
        double i = MathHelper.sin(h * ((float)Math.PI / 180));
        double j = -MathHelper.cos(h * ((float)Math.PI / 180));
        state.field_53536 = (float)e * 10.0f;
        state.field_53536 = MathHelper.clamp(state.field_53536, -6.0f, 32.0f);
        state.field_53537 = (float)(d * i + g * j) * 100.0f;
        state.field_53537 *= 1.0f - state.getGlidingProgress();
        state.field_53537 = MathHelper.clamp(state.field_53537, 0.0f, 150.0f);
        state.field_53538 = (float)(d * j - g * i) * 100.0f;
        state.field_53538 = MathHelper.clamp(state.field_53538, -20.0f, 20.0f);
        float k = lv.lerpMovement(tickProgress);
        float l = lv.getLerpedDistanceMoved(tickProgress);
        state.field_53536 += MathHelper.sin(l * 6.0f) * 32.0f * k;
    }

    public void renderRightArm(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, Identifier skinTexture, boolean sleeveVisible) {
        this.renderArm(matrices, queue, light, skinTexture, ((PlayerEntityModel)this.model).rightArm, sleeveVisible);
    }

    public void renderLeftArm(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, Identifier skinTexture, boolean sleeveVisible) {
        this.renderArm(matrices, queue, light, skinTexture, ((PlayerEntityModel)this.model).leftArm, sleeveVisible);
    }

    private void renderArm(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, Identifier skinTexture, ModelPart arm, boolean sleeveVisible) {
        PlayerEntityModel lv = (PlayerEntityModel)this.getModel();
        arm.resetTransform();
        arm.visible = true;
        lv.leftSleeve.visible = sleeveVisible;
        lv.rightSleeve.visible = sleeveVisible;
        lv.leftArm.roll = -0.1f;
        lv.rightArm.roll = 0.1f;
        queue.submitModelPart(arm, matrices, RenderLayer.getEntityTranslucent(skinTexture), light, OverlayTexture.DEFAULT_UV, null);
    }

    @Override
    protected void setupTransforms(PlayerEntityRenderState arg, MatrixStack arg2, float f, float g) {
        float h = arg.leaningPitch;
        float i = arg.pitch;
        if (arg.isGliding) {
            super.setupTransforms(arg, arg2, f, g);
            float j = arg.getGlidingProgress();
            if (!arg.usingRiptide) {
                arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(j * (-90.0f - i)));
            }
            if (arg.applyFlyingRotation) {
                arg2.multiply(RotationAxis.POSITIVE_Y.rotation(arg.flyingRotation));
            }
        } else if (h > 0.0f) {
            super.setupTransforms(arg, arg2, f, g);
            float j = arg.touchingWater ? -90.0f - i : -90.0f;
            float k = MathHelper.lerp(h, 0.0f, j);
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k));
            if (arg.isSwimming) {
                arg2.translate(0.0f, -1.0f, 0.3f);
            }
        } else {
            super.setupTransforms(arg, arg2, f, g);
        }
    }

    @Override
    public boolean shouldFlipUpsideDown(AvatarlikeEntity arg) {
        if (((PlayerLikeEntity)arg).isModelPartVisible(PlayerModelPart.CAPE)) {
            if (arg instanceof PlayerEntity) {
                PlayerEntity lv = (PlayerEntity)arg;
                return PlayerEntityRenderer.shouldFlipUpsideDown(lv);
            }
            return super.shouldFlipUpsideDown(arg);
        }
        return false;
    }

    public static boolean shouldFlipUpsideDown(PlayerEntity player) {
        return PlayerEntityRenderer.shouldFlipUpsideDown(player.getGameProfile().name());
    }

    @Override
    public /* synthetic */ boolean shouldFlipUpsideDown(LivingEntity entity) {
        return this.shouldFlipUpsideDown((AvatarlikeEntity)((PlayerLikeEntity)entity));
    }

    @Override
    public /* synthetic */ Identifier getTexture(LivingEntityRenderState state) {
        return this.getTexture((PlayerEntityRenderState)state);
    }

    @Override
    protected /* synthetic */ boolean shouldRenderFeatures(LivingEntityRenderState state) {
        return this.shouldRenderFeatures((PlayerEntityRenderState)state);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ void renderLabelIfPresent(EntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState arg4) {
        this.renderLabelIfPresent((PlayerEntityRenderState)state, matrices, queue, arg4);
    }

    @Override
    public /* synthetic */ Vec3d getPositionOffset(EntityRenderState state) {
        return this.getPositionOffset((PlayerEntityRenderState)state);
    }
}

