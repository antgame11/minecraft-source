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
import net.minecraft.client.render.entity.model.BabyModelTransformer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelTransformer;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.client.render.entity.state.TurtleEntityRenderState;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class TurtleEntityModel
extends QuadrupedEntityModel<TurtleEntityRenderState> {
    private static final String EGG_BELLY = "egg_belly";
    public static final ModelTransformer BABY_TRANSFORMER = new BabyModelTransformer(true, 120.0f, 0.0f, 9.0f, 6.0f, 120.0f, Set.of("head"));
    private final ModelPart plastron;

    public TurtleEntityModel(ModelPart arg) {
        super(arg);
        this.plastron = arg.getChild(EGG_BELLY);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(3, 0).cuboid(-3.0f, -1.0f, -3.0f, 6.0f, 5.0f, 6.0f), ModelTransform.origin(0.0f, 19.0f, -10.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(7, 37).cuboid("shell", -9.5f, 3.0f, -10.0f, 19.0f, 20.0f, 6.0f).uv(31, 1).cuboid("belly", -5.5f, 3.0f, -13.0f, 11.0f, 18.0f, 3.0f), ModelTransform.of(0.0f, 11.0f, -10.0f, 1.5707964f, 0.0f, 0.0f));
        lv2.addChild(EGG_BELLY, ModelPartBuilder.create().uv(70, 33).cuboid(-4.5f, 3.0f, -14.0f, 9.0f, 18.0f, 1.0f), ModelTransform.of(0.0f, 11.0f, -10.0f, 1.5707964f, 0.0f, 0.0f));
        boolean i = true;
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(1, 23).cuboid(-2.0f, 0.0f, 0.0f, 4.0f, 1.0f, 10.0f), ModelTransform.origin(-3.5f, 22.0f, 11.0f));
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(1, 12).cuboid(-2.0f, 0.0f, 0.0f, 4.0f, 1.0f, 10.0f), ModelTransform.origin(3.5f, 22.0f, 11.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(27, 30).cuboid(-13.0f, 0.0f, -2.0f, 13.0f, 1.0f, 5.0f), ModelTransform.origin(-5.0f, 21.0f, -4.0f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(27, 24).cuboid(0.0f, 0.0f, -2.0f, 13.0f, 1.0f, 5.0f), ModelTransform.origin(5.0f, 21.0f, -4.0f));
        return TexturedModelData.of(lv, 128, 64);
    }

    @Override
    public void setAngles(TurtleEntityRenderState arg) {
        super.setAngles(arg);
        float f = arg.limbSwingAnimationProgress;
        float g = arg.limbSwingAmplitude;
        if (arg.onLand) {
            float h = arg.diggingSand ? 4.0f : 1.0f;
            float i = arg.diggingSand ? 2.0f : 1.0f;
            float j = f * 5.0f;
            float k = MathHelper.cos(h * j);
            float l = MathHelper.cos(j);
            this.rightFrontLeg.yaw = -k * 8.0f * g * i;
            this.leftFrontLeg.yaw = k * 8.0f * g * i;
            this.rightHindLeg.yaw = -l * 3.0f * g;
            this.leftHindLeg.yaw = l * 3.0f * g;
        } else {
            float i;
            float h = 0.5f * g;
            this.rightHindLeg.pitch = i = MathHelper.cos(f * 0.6662f * 0.6f) * h;
            this.leftHindLeg.pitch = -i;
            this.rightFrontLeg.roll = -i;
            this.leftFrontLeg.roll = i;
        }
        this.plastron.visible = arg.hasEgg;
        if (this.plastron.visible) {
            this.root.originY -= 1.0f;
        }
    }
}

