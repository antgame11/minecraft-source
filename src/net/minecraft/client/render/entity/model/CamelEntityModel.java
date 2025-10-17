/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.CamelAnimations;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelTransformer;
import net.minecraft.client.render.entity.state.CamelEntityRenderState;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class CamelEntityModel
extends EntityModel<CamelEntityRenderState> {
    private static final float LIMB_ANGLE_SCALE = 2.0f;
    private static final float LIMB_DISTANCE_SCALE = 2.5f;
    public static final ModelTransformer BABY_TRANSFORMER = ModelTransformer.scaling(0.45f);
    protected final ModelPart head;
    private final Animation walkingAnimation;
    private final Animation sittingTransitionAnimation;
    private final Animation sittingAnimation;
    private final Animation standingTransitionAnimation;
    private final Animation idlingAnimation;
    private final Animation dashingAnimation;

    public CamelEntityModel(ModelPart arg) {
        super(arg);
        ModelPart lv = arg.getChild(EntityModelPartNames.BODY);
        this.head = lv.getChild(EntityModelPartNames.HEAD);
        this.walkingAnimation = CamelAnimations.WALKING.createAnimation(arg);
        this.sittingTransitionAnimation = CamelAnimations.SITTING_TRANSITION.createAnimation(arg);
        this.sittingAnimation = CamelAnimations.SITTING.createAnimation(arg);
        this.standingTransitionAnimation = CamelAnimations.STANDING_TRANSITION.createAnimation(arg);
        this.idlingAnimation = CamelAnimations.IDLING.createAnimation(arg);
        this.dashingAnimation = CamelAnimations.DASHING.createAnimation(arg);
    }

    public static TexturedModelData getTexturedModelData() {
        return TexturedModelData.of(CamelEntityModel.getModelData(), 128, 128);
    }

    protected static ModelData getModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 25).cuboid(-7.5f, -12.0f, -23.5f, 15.0f, 12.0f, 27.0f), ModelTransform.origin(0.0f, 4.0f, 9.5f));
        lv3.addChild("hump", ModelPartBuilder.create().uv(74, 0).cuboid(-4.5f, -5.0f, -5.5f, 9.0f, 5.0f, 11.0f), ModelTransform.origin(0.0f, -12.0f, -10.0f));
        lv3.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(122, 0).cuboid(-1.5f, 0.0f, 0.0f, 3.0f, 14.0f, 0.0f), ModelTransform.origin(0.0f, -9.0f, 3.5f));
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(60, 24).cuboid(-3.5f, -7.0f, -15.0f, 7.0f, 8.0f, 19.0f).uv(21, 0).cuboid(-3.5f, -21.0f, -15.0f, 7.0f, 14.0f, 7.0f).uv(50, 0).cuboid(-2.5f, -21.0f, -21.0f, 5.0f, 5.0f, 6.0f), ModelTransform.origin(0.0f, -3.0f, -19.5f));
        lv4.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(45, 0).cuboid(-0.5f, 0.5f, -1.0f, 3.0f, 1.0f, 2.0f), ModelTransform.origin(2.5f, -21.0f, -9.5f));
        lv4.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(67, 0).cuboid(-2.5f, 0.5f, -1.0f, 3.0f, 1.0f, 2.0f), ModelTransform.origin(-2.5f, -21.0f, -9.5f));
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(58, 16).cuboid(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), ModelTransform.origin(4.9f, 1.0f, 9.5f));
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(94, 16).cuboid(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), ModelTransform.origin(-4.9f, 1.0f, 9.5f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(0, 0).cuboid(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), ModelTransform.origin(4.9f, 1.0f, -10.5f));
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(0, 26).cuboid(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), ModelTransform.origin(-4.9f, 1.0f, -10.5f));
        return lv;
    }

    @Override
    public void setAngles(CamelEntityRenderState arg) {
        super.setAngles(arg);
        this.setHeadAngles(arg, arg.relativeHeadYaw, arg.pitch);
        this.walkingAnimation.applyWalking(arg.limbSwingAnimationProgress, arg.limbSwingAmplitude, 2.0f, 2.5f);
        this.sittingTransitionAnimation.apply(arg.sittingTransitionAnimationState, arg.age);
        this.sittingAnimation.apply(arg.sittingAnimationState, arg.age);
        this.standingTransitionAnimation.apply(arg.standingTransitionAnimationState, arg.age);
        this.idlingAnimation.apply(arg.idlingAnimationState, arg.age);
        this.dashingAnimation.apply(arg.dashingAnimationState, arg.age);
    }

    private void setHeadAngles(CamelEntityRenderState state, float headYaw, float headPitch) {
        headYaw = MathHelper.clamp(headYaw, -30.0f, 30.0f);
        headPitch = MathHelper.clamp(headPitch, -25.0f, 45.0f);
        if (state.jumpCooldown > 0.0f) {
            float h = 45.0f * state.jumpCooldown / 55.0f;
            headPitch = MathHelper.clamp(headPitch + h, -25.0f, 70.0f);
        }
        this.head.yaw = headYaw * ((float)Math.PI / 180);
        this.head.pitch = headPitch * ((float)Math.PI / 180);
    }
}

