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
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

@Environment(value=EnvType.CLIENT)
public class Deadmau5EarsEntityModel
extends PlayerEntityModel {
    public Deadmau5EarsEntityModel(ModelPart arg) {
        super(arg, false);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = PlayerEntityModel.getTexturedModelData(Dilation.NONE, false);
        ModelPartData lv2 = lv.getRoot().resetChildrenParts();
        ModelPartData lv3 = lv2.getChild(EntityModelPartNames.HEAD);
        ModelPartBuilder lv4 = ModelPartBuilder.create().uv(24, 0).cuboid(-3.0f, -6.0f, -1.0f, 6.0f, 6.0f, 1.0f, new Dilation(1.0f));
        lv3.addChild(EntityModelPartNames.LEFT_EAR, lv4, ModelTransform.origin(-6.0f, -6.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.RIGHT_EAR, lv4, ModelTransform.origin(6.0f, -6.0f, 0.0f));
        return TexturedModelData.of(lv, 64, 64);
    }
}

