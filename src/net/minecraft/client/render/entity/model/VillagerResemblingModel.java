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
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelTransformer;
import net.minecraft.client.render.entity.model.ModelWithHat;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class VillagerResemblingModel
extends EntityModel<VillagerEntityRenderState>
implements ModelWithHead,
ModelWithHat<VillagerEntityRenderState> {
    public static final ModelTransformer BABY_TRANSFORMER = ModelTransformer.scaling(0.5f);
    private final ModelPart head;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart arms;

    public VillagerResemblingModel(ModelPart arg) {
        super(arg);
        this.head = arg.getChild(EntityModelPartNames.HEAD);
        this.rightLeg = arg.getChild(EntityModelPartNames.RIGHT_LEG);
        this.leftLeg = arg.getChild(EntityModelPartNames.LEFT_LEG);
        this.arms = arg.getChild(EntityModelPartNames.ARMS);
    }

    public static ModelData getModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        float f = 0.5f;
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f), ModelTransform.NONE);
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f, new Dilation(0.51f)), ModelTransform.NONE);
        lv4.addChild(EntityModelPartNames.HAT_RIM, ModelPartBuilder.create().uv(30, 47).cuboid(-8.0f, -8.0f, -6.0f, 16.0f, 16.0f, 1.0f), ModelTransform.rotation(-1.5707964f, 0.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(24, 0).cuboid(-1.0f, -1.0f, -6.0f, 2.0f, 4.0f, 2.0f), ModelTransform.origin(0.0f, -2.0f, 0.0f));
        ModelPartData lv5 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 20).cuboid(-4.0f, 0.0f, -3.0f, 8.0f, 12.0f, 6.0f), ModelTransform.NONE);
        lv5.addChild(EntityModelPartNames.JACKET, ModelPartBuilder.create().uv(0, 38).cuboid(-4.0f, 0.0f, -3.0f, 8.0f, 20.0f, 6.0f, new Dilation(0.5f)), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.ARMS, ModelPartBuilder.create().uv(44, 22).cuboid(-8.0f, -2.0f, -2.0f, 4.0f, 8.0f, 4.0f).uv(44, 22).cuboid(4.0f, -2.0f, -2.0f, 4.0f, 8.0f, 4.0f, true).uv(40, 38).cuboid(-4.0f, 2.0f, -2.0f, 8.0f, 4.0f, 4.0f), ModelTransform.of(0.0f, 3.0f, -1.0f, -0.75f, 0.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 22).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.origin(-2.0f, 12.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 22).mirrored().cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.origin(2.0f, 12.0f, 0.0f));
        return lv;
    }

    public static ModelData getNoHatModelData() {
        ModelData lv = VillagerResemblingModel.getModelData();
        lv.getRoot().resetChildrenParts(EntityModelPartNames.HEAD).resetChildrenParts();
        return lv;
    }

    @Override
    public void setAngles(VillagerEntityRenderState arg) {
        super.setAngles(arg);
        this.head.yaw = arg.relativeHeadYaw * ((float)Math.PI / 180);
        this.head.pitch = arg.pitch * ((float)Math.PI / 180);
        if (arg.headRolling) {
            this.head.roll = 0.3f * MathHelper.sin(0.45f * arg.age);
            this.head.pitch = 0.4f;
        } else {
            this.head.roll = 0.0f;
        }
        this.rightLeg.pitch = MathHelper.cos(arg.limbSwingAnimationProgress * 0.6662f) * 1.4f * arg.limbSwingAmplitude * 0.5f;
        this.leftLeg.pitch = MathHelper.cos(arg.limbSwingAnimationProgress * 0.6662f + (float)Math.PI) * 1.4f * arg.limbSwingAmplitude * 0.5f;
        this.rightLeg.yaw = 0.0f;
        this.leftLeg.yaw = 0.0f;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void rotateArms(VillagerEntityRenderState arg, MatrixStack arg2) {
        this.root.applyTransform(arg2);
        this.arms.applyTransform(arg2);
    }
}

