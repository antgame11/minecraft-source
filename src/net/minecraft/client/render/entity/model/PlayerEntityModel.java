/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.EquipmentModelData;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class PlayerEntityModel
extends BipedEntityModel<PlayerEntityRenderState> {
    protected static final String LEFT_SLEEVE = "left_sleeve";
    protected static final String RIGHT_SLEEVE = "right_sleeve";
    protected static final String LEFT_PANTS = "left_pants";
    protected static final String RIGHT_PANTS = "right_pants";
    private final List<ModelPart> parts;
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final boolean thinArms;

    public PlayerEntityModel(ModelPart root, boolean thinArms) {
        super(root, RenderLayer::getEntityTranslucent);
        this.thinArms = thinArms;
        this.leftSleeve = this.leftArm.getChild(LEFT_SLEEVE);
        this.rightSleeve = this.rightArm.getChild(RIGHT_SLEEVE);
        this.leftPants = this.leftLeg.getChild(LEFT_PANTS);
        this.rightPants = this.rightLeg.getChild(RIGHT_PANTS);
        this.jacket = this.body.getChild(EntityModelPartNames.JACKET);
        this.parts = List.of(this.head, this.body, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg);
    }

    public static ModelData getTexturedModelData(Dilation dilation, boolean slim) {
        ModelPartData lv4;
        ModelPartData lv3;
        ModelData lv = BipedEntityModel.getModelData(dilation, 0.0f);
        ModelPartData lv2 = lv.getRoot();
        float f = 0.25f;
        if (slim) {
            lv3 = lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(32, 48).cuboid(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, dilation), ModelTransform.origin(5.0f, 2.0f, 0.0f));
            lv4 = lv2.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(40, 16).cuboid(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, dilation), ModelTransform.origin(-5.0f, 2.0f, 0.0f));
            lv3.addChild(LEFT_SLEEVE, ModelPartBuilder.create().uv(48, 48).cuboid(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, dilation.add(0.25f)), ModelTransform.NONE);
            lv4.addChild(RIGHT_SLEEVE, ModelPartBuilder.create().uv(40, 32).cuboid(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, dilation.add(0.25f)), ModelTransform.NONE);
        } else {
            lv3 = lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(32, 48).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation), ModelTransform.origin(5.0f, 2.0f, 0.0f));
            lv4 = lv2.getChild(EntityModelPartNames.RIGHT_ARM);
            lv3.addChild(LEFT_SLEEVE, ModelPartBuilder.create().uv(48, 48).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.25f)), ModelTransform.NONE);
            lv4.addChild(RIGHT_SLEEVE, ModelPartBuilder.create().uv(40, 32).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.25f)), ModelTransform.NONE);
        }
        lv3 = lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(16, 48).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation), ModelTransform.origin(1.9f, 12.0f, 0.0f));
        lv4 = lv2.getChild(EntityModelPartNames.RIGHT_LEG);
        lv3.addChild(LEFT_PANTS, ModelPartBuilder.create().uv(0, 48).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.25f)), ModelTransform.NONE);
        lv4.addChild(RIGHT_PANTS, ModelPartBuilder.create().uv(0, 32).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.25f)), ModelTransform.NONE);
        ModelPartData lv5 = lv2.getChild(EntityModelPartNames.BODY);
        lv5.addChild(EntityModelPartNames.JACKET, ModelPartBuilder.create().uv(16, 32).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, dilation.add(0.25f)), ModelTransform.NONE);
        return lv;
    }

    public static EquipmentModelData<ModelData> createEquipmentModelData(Dilation hatDilation, Dilation armorDilation) {
        return BipedEntityModel.createEquipmentModelData(hatDilation, armorDilation).map(modelData -> {
            ModelPartData lv = modelData.getRoot();
            ModelPartData lv2 = lv.getChild(EntityModelPartNames.LEFT_ARM);
            ModelPartData lv3 = lv.getChild(EntityModelPartNames.RIGHT_ARM);
            lv2.addChild(LEFT_SLEEVE, ModelPartBuilder.create(), ModelTransform.NONE);
            lv3.addChild(RIGHT_SLEEVE, ModelPartBuilder.create(), ModelTransform.NONE);
            ModelPartData lv4 = lv.getChild(EntityModelPartNames.LEFT_LEG);
            ModelPartData lv5 = lv.getChild(EntityModelPartNames.RIGHT_LEG);
            lv4.addChild(LEFT_PANTS, ModelPartBuilder.create(), ModelTransform.NONE);
            lv5.addChild(RIGHT_PANTS, ModelPartBuilder.create(), ModelTransform.NONE);
            ModelPartData lv6 = lv.getChild(EntityModelPartNames.BODY);
            lv6.addChild(EntityModelPartNames.JACKET, ModelPartBuilder.create(), ModelTransform.NONE);
            return modelData;
        });
    }

    @Override
    public void setAngles(PlayerEntityRenderState arg) {
        boolean bl;
        this.body.visible = bl = !arg.spectator;
        this.rightArm.visible = bl;
        this.leftArm.visible = bl;
        this.rightLeg.visible = bl;
        this.leftLeg.visible = bl;
        this.hat.visible = arg.hatVisible;
        this.jacket.visible = arg.jacketVisible;
        this.leftPants.visible = arg.leftPantsLegVisible;
        this.rightPants.visible = arg.rightPantsLegVisible;
        this.leftSleeve.visible = arg.leftSleeveVisible;
        this.rightSleeve.visible = arg.rightSleeveVisible;
        super.setAngles(arg);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.leftSleeve.visible = visible;
        this.rightSleeve.visible = visible;
        this.leftPants.visible = visible;
        this.rightPants.visible = visible;
        this.jacket.visible = visible;
    }

    @Override
    public void setArmAngle(PlayerEntityRenderState arg, Arm arg2, MatrixStack arg3) {
        this.getRootPart().applyTransform(arg3);
        ModelPart lv = this.getArm(arg2);
        if (this.thinArms) {
            float f = 0.5f * (float)(arg2 == Arm.RIGHT ? 1 : -1);
            lv.originX += f;
            lv.applyTransform(arg3);
            lv.originX -= f;
        } else {
            lv.applyTransform(arg3);
        }
    }

    public ModelPart getRandomPart(Random random) {
        return Util.getRandom(this.parts, random);
    }
}

