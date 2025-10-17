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
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.ChickenEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

@Environment(value=EnvType.CLIENT)
public class ColdChickenEntityModel
extends ChickenEntityModel {
    public ColdChickenEntityModel(ModelPart arg) {
        super(arg);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = ColdChickenEntityModel.getModelData();
        lv.getRoot().addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 9).cuboid(-3.0f, -4.0f, -3.0f, 6.0f, 8.0f, 6.0f).uv(38, 9).cuboid(0.0f, 3.0f, -1.0f, 0.0f, 3.0f, 5.0f), ModelTransform.of(0.0f, 16.0f, 0.0f, 1.5707964f, 0.0f, 0.0f));
        lv.getRoot().addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-2.0f, -6.0f, -2.0f, 4.0f, 6.0f, 3.0f).uv(44, 0).cuboid(-3.0f, -7.0f, -2.015f, 6.0f, 3.0f, 4.0f), ModelTransform.origin(0.0f, 15.0f, -4.0f));
        return TexturedModelData.of(lv, 64, 32);
    }
}

