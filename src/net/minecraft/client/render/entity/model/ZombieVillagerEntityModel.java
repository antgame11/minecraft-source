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
import net.minecraft.client.render.entity.model.ArmPosing;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.EquipmentModelData;
import net.minecraft.client.render.entity.model.ModelWithHat;
import net.minecraft.client.render.entity.state.ZombieVillagerRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

@Environment(value=EnvType.CLIENT)
public class ZombieVillagerEntityModel<S extends ZombieVillagerRenderState>
extends BipedEntityModel<S>
implements ModelWithHat<S> {
    public ZombieVillagerEntityModel(ModelPart arg) {
        super(arg);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, new ModelPartBuilder().uv(0, 0).cuboid(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f).uv(24, 0).cuboid(-1.0f, -3.0f, -6.0f, 2.0f, 4.0f, 2.0f), ModelTransform.NONE);
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f, new Dilation(0.5f)), ModelTransform.NONE);
        lv4.addChild(EntityModelPartNames.HAT_RIM, ModelPartBuilder.create().uv(30, 47).cuboid(-8.0f, -8.0f, -6.0f, 16.0f, 16.0f, 1.0f), ModelTransform.rotation(-1.5707964f, 0.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 20).cuboid(-4.0f, 0.0f, -3.0f, 8.0f, 12.0f, 6.0f).uv(0, 38).cuboid(-4.0f, 0.0f, -3.0f, 8.0f, 20.0f, 6.0f, new Dilation(0.05f)), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(44, 22).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.origin(-5.0f, 2.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(44, 22).mirrored().cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.origin(5.0f, 2.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 22).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.origin(-2.0f, 12.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 22).mirrored().cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.origin(2.0f, 12.0f, 0.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getTexturedModelDataWithoutHead() {
        return ZombieVillagerEntityModel.getTexturedModelData().transform(data -> {
            data.getRoot().resetChildrenParts(EntityModelPartNames.HEAD).resetChildrenParts();
            return data;
        });
    }

    public static EquipmentModelData<TexturedModelData> getEquipmentModelData(Dilation hatDilation, Dilation armorDilation) {
        return ZombieVillagerEntityModel.createEquipmentModelData(ZombieVillagerEntityModel::getArmorTexturedModelData, hatDilation, armorDilation).map(modelData -> TexturedModelData.of(modelData, 64, 32));
    }

    private static ModelData getArmorTexturedModelData(Dilation dilation) {
        ModelData lv = BipedEntityModel.getModelData(dilation, 0.0f);
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -10.0f, -4.0f, 8.0f, 8.0f, 8.0f, dilation), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 16).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, dilation.add(0.1f)), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.1f)), ModelTransform.origin(-2.0f, 12.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.1f)), ModelTransform.origin(2.0f, 12.0f, 0.0f));
        lv3.getChild(EntityModelPartNames.HAT).addChild(EntityModelPartNames.HAT_RIM, ModelPartBuilder.create(), ModelTransform.NONE);
        return lv;
    }

    @Override
    public void setAngles(S arg) {
        super.setAngles(arg);
        float f = ((ZombieVillagerRenderState)arg).handSwingProgress;
        ArmPosing.zombieArms(this.leftArm, this.rightArm, ((ZombieVillagerRenderState)arg).attacking, f, ((ZombieVillagerRenderState)arg).age);
    }

    @Override
    public void rotateArms(ZombieVillagerRenderState arg, MatrixStack arg2) {
        this.setArmAngle(arg, Arm.RIGHT, arg2);
    }
}

