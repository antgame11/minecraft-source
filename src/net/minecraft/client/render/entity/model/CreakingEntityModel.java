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
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.CreakingAnimations;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.CreakingEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class CreakingEntityModel
extends EntityModel<CreakingEntityRenderState> {
    private final ModelPart head;
    private final Animation walkingAnimation;
    private final Animation attackingAnimation;
    private final Animation invulnerableAnimation;
    private final Animation crumblingAnimation;

    public CreakingEntityModel(ModelPart arg) {
        super(arg);
        ModelPart lv = arg.getChild(EntityModelPartNames.ROOT);
        ModelPart lv2 = lv.getChild(EntityModelPartNames.UPPER_BODY);
        this.head = lv2.getChild(EntityModelPartNames.HEAD);
        this.walkingAnimation = CreakingAnimations.WALKING.createAnimation(lv);
        this.attackingAnimation = CreakingAnimations.ATTACKING.createAnimation(lv);
        this.invulnerableAnimation = CreakingAnimations.INVULNERABLE.createAnimation(lv);
        this.crumblingAnimation = CreakingAnimations.CRUMBLING.createAnimation(lv);
    }

    private static ModelData getModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.origin(0.0f, 24.0f, 0.0f));
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.UPPER_BODY, ModelPartBuilder.create(), ModelTransform.origin(-1.0f, -19.0f, 0.0f));
        lv4.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -10.0f, -3.0f, 6.0f, 10.0f, 6.0f).uv(28, 31).cuboid(-3.0f, -13.0f, -3.0f, 6.0f, 3.0f, 6.0f).uv(12, 40).cuboid(3.0f, -13.0f, 0.0f, 9.0f, 14.0f, 0.0f).uv(34, 12).cuboid(-12.0f, -14.0f, 0.0f, 9.0f, 14.0f, 0.0f), ModelTransform.origin(-3.0f, -11.0f, 0.0f));
        lv4.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 16).cuboid(0.0f, -3.0f, -3.0f, 6.0f, 13.0f, 5.0f).uv(24, 0).cuboid(-6.0f, -4.0f, -3.0f, 6.0f, 7.0f, 5.0f), ModelTransform.origin(0.0f, -7.0f, 1.0f));
        lv4.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(22, 13).cuboid(-2.0f, -1.5f, -1.5f, 3.0f, 21.0f, 3.0f).uv(46, 0).cuboid(-2.0f, 19.5f, -1.5f, 3.0f, 4.0f, 3.0f), ModelTransform.origin(-7.0f, -9.5f, 1.5f));
        lv4.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(30, 40).cuboid(0.0f, -1.0f, -1.5f, 3.0f, 16.0f, 3.0f).uv(52, 12).cuboid(0.0f, -5.0f, -1.5f, 3.0f, 4.0f, 3.0f).uv(52, 19).cuboid(0.0f, 15.0f, -1.5f, 3.0f, 4.0f, 3.0f), ModelTransform.origin(6.0f, -9.0f, 0.5f));
        lv3.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(42, 40).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 16.0f, 3.0f).uv(45, 55).cuboid(-1.5f, 15.7f, -4.5f, 5.0f, 0.0f, 9.0f), ModelTransform.origin(1.5f, -16.0f, 0.5f));
        lv3.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 34).cuboid(-3.0f, -1.5f, -1.5f, 3.0f, 19.0f, 3.0f).uv(45, 46).cuboid(-5.0f, 17.2f, -4.5f, 5.0f, 0.0f, 9.0f).uv(12, 34).cuboid(-3.0f, -4.5f, -1.5f, 3.0f, 3.0f, 3.0f), ModelTransform.origin(-1.0f, -17.5f, 0.5f));
        return lv;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = CreakingEntityModel.getModelData();
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getEyesTexturedModelData() {
        ModelData lv = CreakingEntityModel.getModelData();
        lv.getRoot().resetChildrenExceptExact(Set.of("head"));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public void setAngles(CreakingEntityRenderState arg) {
        super.setAngles(arg);
        this.head.pitch = arg.pitch * ((float)Math.PI / 180);
        this.head.yaw = arg.relativeHeadYaw * ((float)Math.PI / 180);
        if (arg.unrooted) {
            this.walkingAnimation.applyWalking(arg.limbSwingAnimationProgress, arg.limbSwingAmplitude, 1.0f, 1.0f);
        }
        this.attackingAnimation.apply(arg.attackAnimationState, arg.age);
        this.invulnerableAnimation.apply(arg.invulnerableAnimationState, arg.age);
        this.crumblingAnimation.apply(arg.crumblingAnimationState, arg.age);
    }
}

