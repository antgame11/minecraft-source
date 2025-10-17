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
import net.minecraft.client.render.entity.state.LivingEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class CowEntityModel
extends QuadrupedEntityModel<LivingEntityRenderState> {
    public static final ModelTransformer BABY_TRANSFORMER = new BabyModelTransformer(false, 8.0f, 6.0f, Set.of("head"));
    private static final int field_56493 = 12;

    public CowEntityModel(ModelPart arg) {
        super(arg);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = CowEntityModel.getModelData();
        return TexturedModelData.of(lv, 64, 64);
    }

    static ModelData getModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, -6.0f, 8.0f, 8.0f, 6.0f).uv(1, 33).cuboid(-3.0f, 1.0f, -7.0f, 6.0f, 3.0f, 1.0f).uv(22, 0).cuboid(EntityModelPartNames.RIGHT_HORN, -5.0f, -5.0f, -5.0f, 1.0f, 3.0f, 1.0f).uv(22, 0).cuboid(EntityModelPartNames.LEFT_HORN, 4.0f, -5.0f, -5.0f, 1.0f, 3.0f, 1.0f), ModelTransform.origin(0.0f, 4.0f, -8.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(18, 4).cuboid(-6.0f, -10.0f, -7.0f, 12.0f, 18.0f, 10.0f).uv(52, 0).cuboid(-2.0f, 2.0f, -8.0f, 4.0f, 6.0f, 1.0f), ModelTransform.of(0.0f, 5.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        ModelPartBuilder lv3 = ModelPartBuilder.create().mirrored().uv(0, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f);
        ModelPartBuilder lv4 = ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f);
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv4, ModelTransform.origin(-4.0f, 12.0f, 7.0f));
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv3, ModelTransform.origin(4.0f, 12.0f, 7.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv4, ModelTransform.origin(-4.0f, 12.0f, -5.0f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv3, ModelTransform.origin(4.0f, 12.0f, -5.0f));
        return lv;
    }

    public ModelPart getHead() {
        return this.head;
    }
}

