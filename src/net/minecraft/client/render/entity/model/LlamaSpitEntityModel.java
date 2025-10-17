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
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;

@Environment(value=EnvType.CLIENT)
public class LlamaSpitEntityModel
extends EntityModel<EntityRenderState> {
    private static final String MAIN = "main";

    public LlamaSpitEntityModel(ModelPart arg) {
        super(arg);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        int i = 2;
        lv2.addChild(MAIN, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).cuboid(0.0f, -4.0f, 0.0f, 2.0f, 2.0f, 2.0f).cuboid(0.0f, 0.0f, -4.0f, 2.0f, 2.0f, 2.0f).cuboid(0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).cuboid(2.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).cuboid(0.0f, 2.0f, 0.0f, 2.0f, 2.0f, 2.0f).cuboid(0.0f, 0.0f, 2.0f, 2.0f, 2.0f, 2.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 32);
    }
}

