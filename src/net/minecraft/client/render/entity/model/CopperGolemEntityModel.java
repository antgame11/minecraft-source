/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.CopperGolemAnimations;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.state.CopperGolemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CopperGolemState;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class CopperGolemEntityModel
extends EntityModel<CopperGolemEntityRenderState>
implements ModelWithArms<CopperGolemEntityRenderState>,
ModelWithHead {
    private static final float field_61668 = 2.0f;
    private static final float field_61669 = 2.5f;
    private static final float field_61670 = 0.015f;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final Animation walkingWithoutItemAnimation;
    private final Animation walkingWithItemAnimation;
    private final Animation spinHeadAnimation;
    private final Animation gettingItemAnimation;
    private final Animation gettingNoItemAnimation;
    private final Animation droppingItemAnimation;
    private final Animation droppingNoItemAnimation;

    public CopperGolemEntityModel(ModelPart arg) {
        super(arg);
        this.body = arg.getChild(EntityModelPartNames.BODY);
        this.head = this.body.getChild(EntityModelPartNames.HEAD);
        this.rightArm = this.body.getChild(EntityModelPartNames.RIGHT_ARM);
        this.leftArm = this.body.getChild(EntityModelPartNames.LEFT_ARM);
        this.walkingWithoutItemAnimation = CopperGolemAnimations.WALKING_WITHOUT_ITEM.createAnimation(arg);
        this.walkingWithItemAnimation = CopperGolemAnimations.WALKING_WITH_ITEM.createAnimation(arg);
        this.spinHeadAnimation = CopperGolemAnimations.SPIN_HEAD.createAnimation(arg);
        this.gettingItemAnimation = CopperGolemAnimations.GETTING_ITEM.createAnimation(arg);
        this.gettingNoItemAnimation = CopperGolemAnimations.GETTING_NO_ITEM.createAnimation(arg);
        this.droppingItemAnimation = CopperGolemAnimations.DROPPING_ITEM.createAnimation(arg);
        this.droppingNoItemAnimation = CopperGolemAnimations.DROPPING_NO_ITEM.createAnimation(arg);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData().transform(transform -> transform.moveOrigin(0.0f, 24.0f, 0.0f));
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 15).cuboid(-4.0f, -6.0f, -3.0f, 8.0f, 6.0f, 6.0f, Dilation.NONE), ModelTransform.origin(0.0f, -5.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -5.0f, -5.0f, 8.0f, 5.0f, 10.0f, new Dilation(0.015f)).uv(56, 0).cuboid(-1.0f, -2.0f, -6.0f, 2.0f, 3.0f, 2.0f, Dilation.NONE).uv(37, 8).cuboid(-1.0f, -9.0f, -1.0f, 2.0f, 4.0f, 2.0f, new Dilation(-0.015f)).uv(37, 0).cuboid(-2.0f, -13.0f, -2.0f, 4.0f, 4.0f, 4.0f, new Dilation(-0.015f)), ModelTransform.origin(0.0f, -6.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(36, 16).cuboid(-3.0f, -1.0f, -2.0f, 3.0f, 10.0f, 4.0f, Dilation.NONE), ModelTransform.origin(-4.0f, -6.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(50, 16).cuboid(0.0f, -1.0f, -2.0f, 3.0f, 10.0f, 4.0f, Dilation.NONE), ModelTransform.origin(4.0f, -6.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 27).cuboid(-4.0f, 0.0f, -2.0f, 4.0f, 5.0f, 4.0f, Dilation.NONE), ModelTransform.origin(0.0f, -5.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(16, 27).cuboid(0.0f, 0.0f, -2.0f, 4.0f, 5.0f, 4.0f, Dilation.NONE), ModelTransform.origin(0.0f, -5.0f, 0.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getRunningTexturedModelData() {
        ModelData lv = new ModelData().transform(transform -> transform.moveOrigin(0.0f, 0.0f, 0.0f));
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.origin(-1.064f, -5.0f, 0.0f));
        lv3.addChild("body_r1", ModelPartBuilder.create().uv(0, 15).cuboid(-4.02f, -6.116f, -3.5f, 8.0f, 6.0f, 6.0f, new Dilation(0.0f)), ModelTransform.of(1.1f, 0.1f, 0.7f, 0.1204f, -0.0064f, -0.0779f));
        lv3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -5.1f, -5.0f, 8.0f, 5.0f, 10.0f, new Dilation(0.0f)).uv(56, 0).cuboid(-1.02f, -2.1f, -6.0f, 2.0f, 3.0f, 2.0f, new Dilation(0.0f)).uv(37, 8).cuboid(-1.02f, -9.1f, -1.0f, 2.0f, 4.0f, 2.0f, new Dilation(-0.015f)).uv(37, 0).cuboid(-2.0f, -13.1f, -2.0f, 4.0f, 4.0f, 4.0f, new Dilation(-0.015f)), ModelTransform.origin(0.7f, -5.6f, -1.8f));
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create(), ModelTransform.origin(-4.0f, -6.0f, 0.0f));
        lv4.addChild("right_arm_r1", ModelPartBuilder.create().uv(36, 16).cuboid(-3.052f, -1.11f, -2.036f, 3.0f, 10.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(0.7f, -0.248f, -1.62f, 1.0036f, 0.0f, 0.0f));
        ModelPartData lv5 = lv3.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create(), ModelTransform.origin(4.0f, -6.0f, 0.0f));
        lv5.addChild("left_arm_r1", ModelPartBuilder.create().uv(50, 16).cuboid(0.032f, -1.1f, -2.0f, 3.0f, 10.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(0.732f, 0.0f, 0.0f, -0.8715f, -0.0535f, -0.0449f));
        ModelPartData lv6 = lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create(), ModelTransform.origin(-3.064f, -5.0f, 0.0f));
        lv6.addChild("right_leg_r1", ModelPartBuilder.create().uv(0, 27).cuboid(-1.856f, -0.1f, -1.09f, 4.0f, 5.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(1.048f, 0.0f, -0.9f, -0.8727f, 0.0f, 0.0f));
        ModelPartData lv7 = lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create(), ModelTransform.origin(0.936f, -5.0f, 0.0f));
        lv7.addChild("left_leg_r1", ModelPartBuilder.create().uv(16, 27).cuboid(-2.088f, -0.1f, -2.0f, 4.0f, 5.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(1.0f, 0.0f, 0.0f, 0.7854f, 0.0f, 0.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getSittingTexturedModelData() {
        ModelData lv = new ModelData().transform(transform -> transform.moveOrigin(0.0f, 0.0f, 0.0f));
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(3, 19).cuboid(-3.0f, -4.0f, -4.525f, 6.0f, 1.0f, 6.0f, new Dilation(0.0f)).uv(0, 15).cuboid(-4.0f, -3.0f, -3.525f, 8.0f, 6.0f, 6.0f, new Dilation(0.0f)), ModelTransform.origin(0.0f, -3.0f, 2.325f));
        lv3.addChild("body_r1", ModelPartBuilder.create().uv(3, 18).cuboid(-4.0f, -3.0f, -2.2f, 8.0f, 6.0f, 3.0f, new Dilation(0.0f)), ModelTransform.of(0.0f, -1.0f, -4.325f, 0.0f, 0.0f, -3.1416f));
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(37, 8).cuboid(-1.0f, -7.0f, -3.3f, 2.0f, 4.0f, 2.0f, new Dilation(-0.015f)).uv(37, 0).cuboid(-2.0f, -11.0f, -4.3f, 4.0f, 4.0f, 4.0f, new Dilation(-0.015f)).uv(0, 0).cuboid(-4.0f, -3.0f, -7.325f, 8.0f, 5.0f, 10.0f, new Dilation(0.0f)).uv(56, 0).cuboid(-1.0f, 0.0f, -8.325f, 2.0f, 3.0f, 2.0f, new Dilation(0.0f)), ModelTransform.origin(0.0f, -6.0f, -0.2f));
        ModelPartData lv5 = lv3.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create(), ModelTransform.of(-4.0f, -5.6f, -1.8f, 0.4363f, 0.0f, 0.0f));
        lv5.addChild("right_arm_r1", ModelPartBuilder.create().uv(36, 16).cuboid(-3.075f, -0.9733f, -1.9966f, 3.0f, 10.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(0.0f, 0.0893f, 0.1198f, -1.0472f, 0.0f, 0.0f));
        ModelPartData lv6 = lv3.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create(), ModelTransform.of(4.0f, -5.6f, -1.7f, 0.4363f, 0.0f, 0.0f));
        lv6.addChild("left_arm_r1", ModelPartBuilder.create().uv(50, 16).cuboid(0.075f, -1.0443f, -1.8997f, 3.0f, 10.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(0.0f, -0.0015f, -0.0808f, -1.0472f, 0.0f, 0.0f));
        ModelPartData lv7 = lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create(), ModelTransform.origin(-2.1f, -2.1f, -2.075f));
        lv7.addChild("right_leg_r1", ModelPartBuilder.create().uv(0, 27).cuboid(-2.0f, 0.975f, 0.0f, 4.0f, 5.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(0.05f, -1.9f, 1.075f, -1.5708f, 0.0f, 0.0f));
        ModelPartData lv8 = lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create(), ModelTransform.origin(2.0f, -2.0f, -2.075f));
        lv8.addChild("left_leg_r1", ModelPartBuilder.create().uv(16, 27).cuboid(-2.0f, 0.975f, 0.0f, 4.0f, 5.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(0.05f, -2.0f, 1.075f, -1.5708f, 0.0f, 0.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getStarTexturedModelData() {
        ModelData lv = new ModelData().transform(transform -> transform.moveOrigin(0.0f, 0.0f, 0.0f));
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 15).cuboid(-4.0f, -6.0f, -3.0f, 8.0f, 6.0f, 6.0f, new Dilation(0.0f)), ModelTransform.origin(0.0f, -5.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -5.0f, -5.0f, 8.0f, 5.0f, 10.0f, new Dilation(0.0f)).uv(56, 0).cuboid(-1.0f, -2.0f, -6.0f, 2.0f, 3.0f, 2.0f, new Dilation(0.0f)).uv(37, 8).cuboid(-1.0f, -9.0f, -1.0f, 2.0f, 4.0f, 2.0f, new Dilation(-0.015f)).uv(37, 0).cuboid(-2.0f, -13.0f, -2.0f, 4.0f, 4.0f, 4.0f, new Dilation(-0.015f)), ModelTransform.origin(0.0f, -6.0f, 0.0f));
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create(), ModelTransform.origin(-4.0f, -6.0f, 0.0f));
        lv4.addChild("right_arm_r1", ModelPartBuilder.create().uv(36, 16).cuboid(-1.5f, -5.0f, -2.0f, 3.0f, 10.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.9199f));
        lv4.addChild("rightItem", ModelPartBuilder.create(), ModelTransform.origin(-1.0f, 7.4f, -1.0f));
        ModelPartData lv5 = lv3.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create(), ModelTransform.origin(4.0f, -6.0f, 0.0f));
        lv5.addChild("left_arm_r1", ModelPartBuilder.create().uv(50, 16).cuboid(-1.5f, -5.0f, -2.0f, 3.0f, 10.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(-1.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.9199f));
        ModelPartData lv6 = lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create(), ModelTransform.origin(-3.0f, -5.0f, 0.0f));
        lv6.addChild("right_leg_r1", ModelPartBuilder.create().uv(0, 27).cuboid(-2.0f, -2.5f, -2.0f, 4.0f, 5.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(0.35f, 2.0f, 0.01f, 0.0f, 0.0f, 0.2618f));
        ModelPartData lv7 = lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create(), ModelTransform.origin(1.0f, -5.0f, 0.0f));
        lv7.addChild("left_leg_r1", ModelPartBuilder.create().uv(16, 27).cuboid(-2.0f, -2.5f, -2.0f, 4.0f, 5.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(1.65f, 2.0f, 0.0f, 0.0f, 0.0f, -0.2618f));
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getEyesTexturedModelData() {
        return CopperGolemEntityModel.getTexturedModelData().transform(transform -> {
            transform.getRoot().resetChildrenExcept(Set.of("eyes"));
            return transform;
        });
    }

    @Override
    public void setAngles(CopperGolemEntityRenderState arg) {
        super.setAngles(arg);
        this.head.pitch = arg.pitch * ((float)Math.PI / 180);
        this.head.yaw = arg.relativeHeadYaw * ((float)Math.PI / 180);
        if (arg.rightHandItemState.isEmpty() && arg.leftHandItemState.isEmpty()) {
            this.walkingWithoutItemAnimation.applyWalking(arg.limbSwingAnimationProgress, arg.limbSwingAmplitude, 2.0f, 2.5f);
        } else {
            this.walkingWithItemAnimation.applyWalking(arg.limbSwingAnimationProgress, arg.limbSwingAmplitude, 2.0f, 2.5f);
            this.clampArmRotations();
        }
        this.spinHeadAnimation.apply(arg.spinHeadAnimationState, arg.age);
        this.gettingItemAnimation.apply(arg.gettingItemAnimationState, arg.age);
        this.gettingNoItemAnimation.apply(arg.gettingNoItemAnimationState, arg.age);
        this.droppingItemAnimation.apply(arg.droppingItemAnimationState, arg.age);
        this.droppingNoItemAnimation.apply(arg.droppingNoItemAnimationState, arg.age);
    }

    @Override
    public void setArmAngle(CopperGolemEntityRenderState arg, Arm arg2, MatrixStack arg3) {
        this.root.applyTransform(arg3);
        this.body.applyTransform(arg3);
        ModelPart lv = arg2 == Arm.RIGHT ? this.rightArm : this.leftArm;
        lv.applyTransform(arg3);
        if (arg.copperGolemState.equals(CopperGolemState.IDLE)) {
            arg3.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arg2 == Arm.RIGHT ? -90.0f : 90.0f));
            arg3.translate(0.0f, 0.0f, 0.125f);
        } else {
            arg3.scale(0.55f, 0.55f, 0.55f);
            arg3.translate(-0.125f, 0.3125f, -0.1875f);
        }
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void applyTransform(MatrixStack matrices) {
        this.body.applyTransform(matrices);
        this.head.applyTransform(matrices);
        matrices.translate(0.0f, 0.125f, 0.0f);
        matrices.scale(1.0625f, 1.0625f, 1.0625f);
    }

    public void transformMatricesForBlock(MatrixStack matrices) {
        this.root.applyTransform(matrices);
        this.body.applyTransform(matrices);
        this.head.applyTransform(matrices);
        matrices.translate(0.0, -2.25, 0.0);
    }

    private void clampArmRotations() {
        this.rightArm.pitch = Math.min(this.rightArm.pitch, -0.87266463f);
        this.leftArm.pitch = Math.min(this.leftArm.pitch, -0.87266463f);
        this.rightArm.yaw = Math.min(this.rightArm.yaw, -0.1134464f);
        this.leftArm.yaw = Math.max(this.leftArm.yaw, 0.1134464f);
        this.rightArm.roll = Math.min(this.rightArm.roll, -0.064577185f);
        this.leftArm.roll = Math.max(this.leftArm.roll, 0.064577185f);
    }
}

