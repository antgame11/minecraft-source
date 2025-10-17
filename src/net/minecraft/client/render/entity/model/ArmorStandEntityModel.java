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
import net.minecraft.client.render.entity.model.ArmorStandArmorEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

@Environment(value=EnvType.CLIENT)
public class ArmorStandEntityModel
extends ArmorStandArmorEntityModel {
    private static final String RIGHT_BODY_STICK = "right_body_stick";
    private static final String LEFT_BODY_STICK = "left_body_stick";
    private static final String SHOULDER_STICK = "shoulder_stick";
    private static final String BASE_PLATE = "base_plate";
    private final ModelPart rightBodyStick;
    private final ModelPart leftBodyStick;
    private final ModelPart shoulderStick;
    private final ModelPart basePlate;

    public ArmorStandEntityModel(ModelPart arg) {
        super(arg);
        this.rightBodyStick = arg.getChild(RIGHT_BODY_STICK);
        this.leftBodyStick = arg.getChild(LEFT_BODY_STICK);
        this.shoulderStick = arg.getChild(SHOULDER_STICK);
        this.basePlate = arg.getChild(BASE_PLATE);
        this.hat.visible = false;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -7.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.origin(0.0f, 1.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 26).cuboid(-6.0f, 0.0f, -1.5f, 12.0f, 3.0f, 3.0f), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(24, 0).cuboid(-2.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.origin(-5.0f, 2.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(32, 16).mirrored().cuboid(0.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.origin(5.0f, 2.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(8, 0).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 11.0f, 2.0f), ModelTransform.origin(-1.9f, 12.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 11.0f, 2.0f), ModelTransform.origin(1.9f, 12.0f, 0.0f));
        lv2.addChild(RIGHT_BODY_STICK, ModelPartBuilder.create().uv(16, 0).cuboid(-3.0f, 3.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.NONE);
        lv2.addChild(LEFT_BODY_STICK, ModelPartBuilder.create().uv(48, 16).cuboid(1.0f, 3.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.NONE);
        lv2.addChild(SHOULDER_STICK, ModelPartBuilder.create().uv(0, 48).cuboid(-4.0f, 10.0f, -1.0f, 8.0f, 2.0f, 2.0f), ModelTransform.NONE);
        lv2.addChild(BASE_PLATE, ModelPartBuilder.create().uv(0, 32).cuboid(-6.0f, 11.0f, -6.0f, 12.0f, 1.0f, 12.0f), ModelTransform.origin(0.0f, 12.0f, 0.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public void setAngles(ArmorStandEntityRenderState arg) {
        super.setAngles(arg);
        this.basePlate.yaw = (float)Math.PI / 180 * -arg.yaw;
        this.leftArm.visible = arg.showArms;
        this.rightArm.visible = arg.showArms;
        this.basePlate.visible = arg.showBasePlate;
        this.rightBodyStick.pitch = (float)Math.PI / 180 * arg.bodyRotation.pitch();
        this.rightBodyStick.yaw = (float)Math.PI / 180 * arg.bodyRotation.yaw();
        this.rightBodyStick.roll = (float)Math.PI / 180 * arg.bodyRotation.roll();
        this.leftBodyStick.pitch = (float)Math.PI / 180 * arg.bodyRotation.pitch();
        this.leftBodyStick.yaw = (float)Math.PI / 180 * arg.bodyRotation.yaw();
        this.leftBodyStick.roll = (float)Math.PI / 180 * arg.bodyRotation.roll();
        this.shoulderStick.pitch = (float)Math.PI / 180 * arg.bodyRotation.pitch();
        this.shoulderStick.yaw = (float)Math.PI / 180 * arg.bodyRotation.yaw();
        this.shoulderStick.roll = (float)Math.PI / 180 * arg.bodyRotation.roll();
    }

    @Override
    public void setArmAngle(ArmorStandEntityRenderState arg, Arm arg2, MatrixStack arg3) {
        ModelPart lv = this.getArm(arg2);
        boolean bl = lv.visible;
        lv.visible = true;
        super.setArmAngle(arg, arg2, arg3);
        lv.visible = bl;
    }
}

