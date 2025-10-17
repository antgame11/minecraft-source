/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class BlazeEntityModel
extends EntityModel<LivingEntityRenderState> {
    private final ModelPart[] rods;
    private final ModelPart head;

    public BlazeEntityModel(ModelPart arg) {
        super(arg);
        this.head = arg.getChild(EntityModelPartNames.HEAD);
        this.rods = new ModelPart[12];
        Arrays.setAll(this.rods, i -> arg.getChild(BlazeEntityModel.getRodName(i)));
    }

    private static String getRodName(int index) {
        return "part" + index;
    }

    public static TexturedModelData getTexturedModelData() {
        float j;
        float h;
        float g;
        int i;
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.NONE);
        float f = 0.0f;
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(0, 16).cuboid(0.0f, 0.0f, 0.0f, 2.0f, 8.0f, 2.0f);
        for (i = 0; i < 4; ++i) {
            g = MathHelper.cos(f) * 9.0f;
            h = -2.0f + MathHelper.cos((float)(i * 2) * 0.25f);
            j = MathHelper.sin(f) * 9.0f;
            lv2.addChild(BlazeEntityModel.getRodName(i), lv3, ModelTransform.origin(g, h, j));
            f += 1.5707964f;
        }
        f = 0.7853982f;
        for (i = 4; i < 8; ++i) {
            g = MathHelper.cos(f) * 7.0f;
            h = 2.0f + MathHelper.cos((float)(i * 2) * 0.25f);
            j = MathHelper.sin(f) * 7.0f;
            lv2.addChild(BlazeEntityModel.getRodName(i), lv3, ModelTransform.origin(g, h, j));
            f += 1.5707964f;
        }
        f = 0.47123894f;
        for (i = 8; i < 12; ++i) {
            g = MathHelper.cos(f) * 5.0f;
            h = 11.0f + MathHelper.cos((float)i * 1.5f * 0.5f);
            j = MathHelper.sin(f) * 5.0f;
            lv2.addChild(BlazeEntityModel.getRodName(i), lv3, ModelTransform.origin(g, h, j));
            f += 1.5707964f;
        }
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public void setAngles(LivingEntityRenderState arg) {
        int i;
        super.setAngles(arg);
        float f = arg.age * (float)Math.PI * -0.1f;
        for (i = 0; i < 4; ++i) {
            this.rods[i].originY = -2.0f + MathHelper.cos(((float)(i * 2) + arg.age) * 0.25f);
            this.rods[i].originX = MathHelper.cos(f) * 9.0f;
            this.rods[i].originZ = MathHelper.sin(f) * 9.0f;
            f += 1.5707964f;
        }
        f = 0.7853982f + arg.age * (float)Math.PI * 0.03f;
        for (i = 4; i < 8; ++i) {
            this.rods[i].originY = 2.0f + MathHelper.cos(((float)(i * 2) + arg.age) * 0.25f);
            this.rods[i].originX = MathHelper.cos(f) * 7.0f;
            this.rods[i].originZ = MathHelper.sin(f) * 7.0f;
            f += 1.5707964f;
        }
        f = 0.47123894f + arg.age * (float)Math.PI * -0.05f;
        for (i = 8; i < 12; ++i) {
            this.rods[i].originY = 11.0f + MathHelper.cos(((float)i * 1.5f + arg.age) * 0.5f);
            this.rods[i].originX = MathHelper.cos(f) * 5.0f;
            this.rods[i].originZ = MathHelper.sin(f) * 5.0f;
            f += 1.5707964f;
        }
        this.head.yaw = arg.relativeHeadYaw * ((float)Math.PI / 180);
        this.head.pitch = arg.pitch * ((float)Math.PI / 180);
    }
}

