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
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

@Environment(value=EnvType.CLIENT)
public class SkullEntityModel
extends SkullBlockEntityModel {
    protected final ModelPart head;

    public SkullEntityModel(ModelPart arg) {
        super(arg);
        this.head = arg.getChild(EntityModelPartNames.HEAD);
    }

    public static ModelData getModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.NONE);
        return lv;
    }

    public static TexturedModelData getHeadTexturedModelData() {
        ModelData lv = SkullEntityModel.getModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.getChild(EntityModelPartNames.HEAD).addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new Dilation(0.25f)), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getSkullTexturedModelData() {
        ModelData lv = SkullEntityModel.getModelData();
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public void setAngles(SkullBlockEntityModel.SkullModelState arg) {
        super.setAngles(arg);
        this.head.yaw = arg.yaw * ((float)Math.PI / 180);
        this.head.pitch = arg.pitch * ((float)Math.PI / 180);
    }
}

