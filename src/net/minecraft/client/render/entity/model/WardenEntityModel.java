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
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.WardenAnimations;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.WardenEntityRenderState;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class WardenEntityModel
extends EntityModel<WardenEntityRenderState> {
    private static final float field_38324 = 13.0f;
    private static final float field_38325 = 1.0f;
    protected final ModelPart bone;
    protected final ModelPart body;
    protected final ModelPart head;
    protected final ModelPart rightTendril;
    protected final ModelPart leftTendril;
    protected final ModelPart leftLeg;
    protected final ModelPart leftArm;
    protected final ModelPart leftRibcage;
    protected final ModelPart rightArm;
    protected final ModelPart rightLeg;
    protected final ModelPart rightRibcage;
    private final Animation attackingAnimation;
    private final Animation chargingSonicBoomAnimation;
    private final Animation diggingAnimation;
    private final Animation emergingAnimation;
    private final Animation roaringAnimation;
    private final Animation sniffingAnimation;

    public WardenEntityModel(ModelPart arg) {
        super(arg, RenderLayer::getEntityCutoutNoCull);
        this.bone = arg.getChild(EntityModelPartNames.BONE);
        this.body = this.bone.getChild(EntityModelPartNames.BODY);
        this.head = this.body.getChild(EntityModelPartNames.HEAD);
        this.rightLeg = this.bone.getChild(EntityModelPartNames.RIGHT_LEG);
        this.leftLeg = this.bone.getChild(EntityModelPartNames.LEFT_LEG);
        this.rightArm = this.body.getChild(EntityModelPartNames.RIGHT_ARM);
        this.leftArm = this.body.getChild(EntityModelPartNames.LEFT_ARM);
        this.rightTendril = this.head.getChild(EntityModelPartNames.RIGHT_TENDRIL);
        this.leftTendril = this.head.getChild(EntityModelPartNames.LEFT_TENDRIL);
        this.rightRibcage = this.body.getChild(EntityModelPartNames.RIGHT_RIBCAGE);
        this.leftRibcage = this.body.getChild(EntityModelPartNames.LEFT_RIBCAGE);
        this.attackingAnimation = WardenAnimations.ATTACKING.createAnimation(arg);
        this.chargingSonicBoomAnimation = WardenAnimations.CHARGING_SONIC_BOOM.createAnimation(arg);
        this.diggingAnimation = WardenAnimations.DIGGING.createAnimation(arg);
        this.emergingAnimation = WardenAnimations.EMERGING.createAnimation(arg);
        this.roaringAnimation = WardenAnimations.ROARING.createAnimation(arg);
        this.sniffingAnimation = WardenAnimations.SNIFFING.createAnimation(arg);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BONE, ModelPartBuilder.create(), ModelTransform.origin(0.0f, 24.0f, 0.0f));
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-9.0f, -13.0f, -4.0f, 18.0f, 21.0f, 11.0f), ModelTransform.origin(0.0f, -21.0f, 0.0f));
        lv4.addChild(EntityModelPartNames.RIGHT_RIBCAGE, ModelPartBuilder.create().uv(90, 11).cuboid(-2.0f, -11.0f, -0.1f, 9.0f, 21.0f, 0.0f), ModelTransform.origin(-7.0f, -2.0f, -4.0f));
        lv4.addChild(EntityModelPartNames.LEFT_RIBCAGE, ModelPartBuilder.create().uv(90, 11).mirrored().cuboid(-7.0f, -11.0f, -0.1f, 9.0f, 21.0f, 0.0f).mirrored(false), ModelTransform.origin(7.0f, -2.0f, -4.0f));
        ModelPartData lv5 = lv4.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 32).cuboid(-8.0f, -16.0f, -5.0f, 16.0f, 16.0f, 10.0f), ModelTransform.origin(0.0f, -13.0f, 0.0f));
        lv5.addChild(EntityModelPartNames.RIGHT_TENDRIL, ModelPartBuilder.create().uv(52, 32).cuboid(-16.0f, -13.0f, 0.0f, 16.0f, 16.0f, 0.0f), ModelTransform.origin(-8.0f, -12.0f, 0.0f));
        lv5.addChild(EntityModelPartNames.LEFT_TENDRIL, ModelPartBuilder.create().uv(58, 0).cuboid(0.0f, -13.0f, 0.0f, 16.0f, 16.0f, 0.0f), ModelTransform.origin(8.0f, -12.0f, 0.0f));
        lv4.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(44, 50).cuboid(-4.0f, 0.0f, -4.0f, 8.0f, 28.0f, 8.0f), ModelTransform.origin(-13.0f, -13.0f, 1.0f));
        lv4.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(0, 58).cuboid(-4.0f, 0.0f, -4.0f, 8.0f, 28.0f, 8.0f), ModelTransform.origin(13.0f, -13.0f, 1.0f));
        lv3.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(76, 48).cuboid(-3.1f, 0.0f, -3.0f, 6.0f, 13.0f, 6.0f), ModelTransform.origin(-5.9f, -13.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(76, 76).cuboid(-2.9f, 0.0f, -3.0f, 6.0f, 13.0f, 6.0f), ModelTransform.origin(5.9f, -13.0f, 0.0f));
        return TexturedModelData.of(lv, 128, 128);
    }

    public static TexturedModelData getTendrilsTexturedModelData() {
        return WardenEntityModel.getTexturedModelData().transform(modelData -> {
            modelData.getRoot().resetChildrenExceptExact(Set.of("left_tendril", "right_tendril"));
            return modelData;
        });
    }

    public static TexturedModelData getHeartTexturedModelData() {
        return WardenEntityModel.getTexturedModelData().transform(modelData -> {
            modelData.getRoot().resetChildrenExceptExact(Set.of("body"));
            return modelData;
        });
    }

    public static TexturedModelData getBioluminescentTexturedModelData() {
        return WardenEntityModel.getTexturedModelData().transform(modelData -> {
            modelData.getRoot().resetChildrenExceptExact(Set.of("head", "left_arm", "right_arm", "left_leg", "right_leg"));
            return modelData;
        });
    }

    public static TexturedModelData getPulsatingSpotsTexturedModelData() {
        return WardenEntityModel.getTexturedModelData().transform(modelData -> {
            modelData.getRoot().resetChildrenExceptExact(Set.of("body", "head", "left_arm", "right_arm", "left_leg", "right_leg"));
            return modelData;
        });
    }

    @Override
    public void setAngles(WardenEntityRenderState arg) {
        super.setAngles(arg);
        this.setHeadAngle(arg.relativeHeadYaw, arg.pitch);
        this.setLimbAngles(arg.limbSwingAnimationProgress, arg.limbSwingAmplitude);
        this.setHeadAndBodyAngles(arg.age);
        this.setTendrilPitches(arg, arg.age);
        this.attackingAnimation.apply(arg.attackingAnimationState, arg.age);
        this.chargingSonicBoomAnimation.apply(arg.chargingSonicBoomAnimationState, arg.age);
        this.diggingAnimation.apply(arg.diggingAnimationState, arg.age);
        this.emergingAnimation.apply(arg.emergingAnimationState, arg.age);
        this.roaringAnimation.apply(arg.roaringAnimationState, arg.age);
        this.sniffingAnimation.apply(arg.sniffingAnimationState, arg.age);
    }

    private void setHeadAngle(float yaw, float pitch) {
        this.head.pitch = pitch * ((float)Math.PI / 180);
        this.head.yaw = yaw * ((float)Math.PI / 180);
    }

    private void setHeadAndBodyAngles(float animationProgress) {
        float g = animationProgress * 0.1f;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        this.head.roll += 0.06f * h;
        this.head.pitch += 0.06f * i;
        this.body.roll += 0.025f * i;
        this.body.pitch += 0.025f * h;
    }

    private void setLimbAngles(float angle, float distance) {
        float h = Math.min(0.5f, 3.0f * distance);
        float i = angle * 0.8662f;
        float j = MathHelper.cos(i);
        float k = MathHelper.sin(i);
        float l = Math.min(0.35f, h);
        this.head.roll += 0.3f * k * h;
        this.head.pitch += 1.2f * MathHelper.cos(i + 1.5707964f) * l;
        this.body.roll = 0.1f * k * h;
        this.body.pitch = 1.0f * j * l;
        this.leftLeg.pitch = 1.0f * j * h;
        this.rightLeg.pitch = 1.0f * MathHelper.cos(i + (float)Math.PI) * h;
        this.leftArm.pitch = -(0.8f * j * h);
        this.leftArm.roll = 0.0f;
        this.rightArm.pitch = -(0.8f * k * h);
        this.rightArm.roll = 0.0f;
        this.setArmPivots();
    }

    private void setArmPivots() {
        this.leftArm.yaw = 0.0f;
        this.leftArm.originZ = 1.0f;
        this.leftArm.originX = 13.0f;
        this.leftArm.originY = -13.0f;
        this.rightArm.yaw = 0.0f;
        this.rightArm.originZ = 1.0f;
        this.rightArm.originX = -13.0f;
        this.rightArm.originY = -13.0f;
    }

    private void setTendrilPitches(WardenEntityRenderState state, float animationProgress) {
        float g;
        this.leftTendril.pitch = g = state.tendrilAlpha * (float)(Math.cos((double)animationProgress * 2.25) * Math.PI * (double)0.1f);
        this.rightTendril.pitch = -g;
    }
}

