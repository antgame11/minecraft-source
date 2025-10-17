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
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.SnifferAnimations;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelTransformer;
import net.minecraft.client.render.entity.state.SnifferEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class SnifferEntityModel
extends EntityModel<SnifferEntityRenderState> {
    public static final ModelTransformer BABY_TRANSFORMER = ModelTransformer.scaling(0.5f);
    private static final float LIMB_ANGLE_SCALE = 9.0f;
    private static final float LIMB_DISTANCE_SCALE = 100.0f;
    private final ModelPart head;
    private final Animation searchingAnimation;
    private final Animation walkingAnimation;
    private final Animation diggingAnimation;
    private final Animation sniffingAnimation;
    private final Animation risingAnimation;
    private final Animation feelingHappyAnimation;
    private final Animation scentingAnimation;
    private final Animation babyGrowthAnimation;

    public SnifferEntityModel(ModelPart arg) {
        super(arg);
        this.head = arg.getChild(EntityModelPartNames.BONE).getChild(EntityModelPartNames.BODY).getChild(EntityModelPartNames.HEAD);
        this.searchingAnimation = SnifferAnimations.SEARCHING.createAnimation(arg);
        this.walkingAnimation = SnifferAnimations.WALKING.createAnimation(arg);
        this.diggingAnimation = SnifferAnimations.DIGGING.createAnimation(arg);
        this.sniffingAnimation = SnifferAnimations.SNIFFING.createAnimation(arg);
        this.risingAnimation = SnifferAnimations.RISING.createAnimation(arg);
        this.feelingHappyAnimation = SnifferAnimations.FEELING_HAPPY.createAnimation(arg);
        this.scentingAnimation = SnifferAnimations.SCENTING.createAnimation(arg);
        this.babyGrowthAnimation = SnifferAnimations.BABY_GROWTH.createAnimation(arg);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BONE, ModelPartBuilder.create(), ModelTransform.origin(0.0f, 5.0f, 0.0f));
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(62, 68).cuboid(-12.5f, -14.0f, -20.0f, 25.0f, 29.0f, 40.0f, new Dilation(0.0f)).uv(62, 0).cuboid(-12.5f, -14.0f, -20.0f, 25.0f, 24.0f, 40.0f, new Dilation(0.5f)).uv(87, 68).cuboid(-12.5f, 12.0f, -20.0f, 25.0f, 0.0f, 40.0f, new Dilation(0.0f)), ModelTransform.origin(0.0f, 0.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(32, 87).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.origin(-7.5f, 10.0f, -15.0f));
        lv3.addChild(EntityModelPartNames.RIGHT_MID_LEG, ModelPartBuilder.create().uv(32, 105).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.origin(-7.5f, 10.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(32, 123).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.origin(-7.5f, 10.0f, 15.0f));
        lv3.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(0, 87).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.origin(7.5f, 10.0f, -15.0f));
        lv3.addChild(EntityModelPartNames.LEFT_MID_LEG, ModelPartBuilder.create().uv(0, 105).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.origin(7.5f, 10.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(0, 123).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.origin(7.5f, 10.0f, 15.0f));
        ModelPartData lv5 = lv4.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(8, 15).cuboid(-6.5f, -7.5f, -11.5f, 13.0f, 18.0f, 11.0f, new Dilation(0.0f)).uv(8, 4).cuboid(-6.5f, 7.5f, -11.5f, 13.0f, 0.0f, 11.0f, new Dilation(0.0f)), ModelTransform.origin(0.0f, 6.5f, -19.48f));
        lv5.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(2, 0).cuboid(0.0f, 0.0f, -3.0f, 1.0f, 19.0f, 7.0f, new Dilation(0.0f)), ModelTransform.origin(6.51f, -7.5f, -4.51f));
        lv5.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(48, 0).cuboid(-1.0f, 0.0f, -3.0f, 1.0f, 19.0f, 7.0f, new Dilation(0.0f)), ModelTransform.origin(-6.51f, -7.5f, -4.51f));
        lv5.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(10, 45).cuboid(-6.5f, -2.0f, -9.0f, 13.0f, 2.0f, 9.0f, new Dilation(0.0f)), ModelTransform.origin(0.0f, -4.5f, -11.5f));
        lv5.addChild("lower_beak", ModelPartBuilder.create().uv(10, 57).cuboid(-6.5f, -7.0f, -8.0f, 13.0f, 12.0f, 9.0f, new Dilation(0.0f)), ModelTransform.origin(0.0f, 2.5f, -12.5f));
        return TexturedModelData.of(lv, 192, 192);
    }

    @Override
    public void setAngles(SnifferEntityRenderState arg) {
        super.setAngles(arg);
        this.head.pitch = arg.pitch * ((float)Math.PI / 180);
        this.head.yaw = arg.relativeHeadYaw * ((float)Math.PI / 180);
        if (arg.searching) {
            this.searchingAnimation.applyWalking(arg.limbSwingAnimationProgress, arg.limbSwingAmplitude, 9.0f, 100.0f);
        } else {
            this.walkingAnimation.applyWalking(arg.limbSwingAnimationProgress, arg.limbSwingAmplitude, 9.0f, 100.0f);
        }
        this.diggingAnimation.apply(arg.diggingAnimationState, arg.age);
        this.sniffingAnimation.apply(arg.sniffingAnimationState, arg.age);
        this.risingAnimation.apply(arg.risingAnimationState, arg.age);
        this.feelingHappyAnimation.apply(arg.feelingHappyAnimationState, arg.age);
        this.scentingAnimation.apply(arg.scentingAnimationState, arg.age);
        if (arg.baby) {
            this.babyGrowthAnimation.applyStatic();
        }
    }
}

