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
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class LargePufferfishEntityModel
extends EntityModel<EntityRenderState> {
    private final ModelPart leftBlueFin;
    private final ModelPart rightBlueFin;

    public LargePufferfishEntityModel(ModelPart arg) {
        super(arg);
        this.leftBlueFin = arg.getChild(EntityModelPartNames.LEFT_BLUE_FIN);
        this.rightBlueFin = arg.getChild(EntityModelPartNames.RIGHT_BLUE_FIN);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        int i = 22;
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.origin(0.0f, 22.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_BLUE_FIN, ModelPartBuilder.create().uv(24, 0).cuboid(-2.0f, 0.0f, -1.0f, 2.0f, 1.0f, 2.0f), ModelTransform.origin(-4.0f, 15.0f, -2.0f));
        lv2.addChild(EntityModelPartNames.LEFT_BLUE_FIN, ModelPartBuilder.create().uv(24, 3).cuboid(0.0f, 0.0f, -1.0f, 2.0f, 1.0f, 2.0f), ModelTransform.origin(4.0f, 15.0f, -2.0f));
        lv2.addChild("top_front_fin", ModelPartBuilder.create().uv(15, 17).cuboid(-4.0f, -1.0f, 0.0f, 8.0f, 1.0f, 0.0f), ModelTransform.of(0.0f, 14.0f, -4.0f, 0.7853982f, 0.0f, 0.0f));
        lv2.addChild("top_middle_fin", ModelPartBuilder.create().uv(14, 16).cuboid(-4.0f, -1.0f, 0.0f, 8.0f, 1.0f, 1.0f), ModelTransform.origin(0.0f, 14.0f, 0.0f));
        lv2.addChild("top_back_fin", ModelPartBuilder.create().uv(23, 18).cuboid(-4.0f, -1.0f, 0.0f, 8.0f, 1.0f, 0.0f), ModelTransform.of(0.0f, 14.0f, 4.0f, -0.7853982f, 0.0f, 0.0f));
        lv2.addChild("right_front_fin", ModelPartBuilder.create().uv(5, 17).cuboid(-1.0f, -8.0f, 0.0f, 1.0f, 8.0f, 0.0f), ModelTransform.of(-4.0f, 22.0f, -4.0f, 0.0f, -0.7853982f, 0.0f));
        lv2.addChild("left_front_fin", ModelPartBuilder.create().uv(1, 17).cuboid(0.0f, -8.0f, 0.0f, 1.0f, 8.0f, 0.0f), ModelTransform.of(4.0f, 22.0f, -4.0f, 0.0f, 0.7853982f, 0.0f));
        lv2.addChild("bottom_front_fin", ModelPartBuilder.create().uv(15, 20).cuboid(-4.0f, 0.0f, 0.0f, 8.0f, 1.0f, 0.0f), ModelTransform.of(0.0f, 22.0f, -4.0f, -0.7853982f, 0.0f, 0.0f));
        lv2.addChild("bottom_middle_fin", ModelPartBuilder.create().uv(15, 20).cuboid(-4.0f, 0.0f, 0.0f, 8.0f, 1.0f, 0.0f), ModelTransform.origin(0.0f, 22.0f, 0.0f));
        lv2.addChild("bottom_back_fin", ModelPartBuilder.create().uv(15, 20).cuboid(-4.0f, 0.0f, 0.0f, 8.0f, 1.0f, 0.0f), ModelTransform.of(0.0f, 22.0f, 4.0f, 0.7853982f, 0.0f, 0.0f));
        lv2.addChild("right_back_fin", ModelPartBuilder.create().uv(9, 17).cuboid(-1.0f, -8.0f, 0.0f, 1.0f, 8.0f, 0.0f), ModelTransform.of(-4.0f, 22.0f, 4.0f, 0.0f, 0.7853982f, 0.0f));
        lv2.addChild("left_back_fin", ModelPartBuilder.create().uv(9, 17).cuboid(0.0f, -8.0f, 0.0f, 1.0f, 8.0f, 0.0f), ModelTransform.of(4.0f, 22.0f, 4.0f, 0.0f, -0.7853982f, 0.0f));
        return TexturedModelData.of(lv, 32, 32);
    }

    @Override
    public void setAngles(EntityRenderState arg) {
        super.setAngles(arg);
        this.rightBlueFin.roll = -0.2f + 0.4f * MathHelper.sin(arg.age * 0.2f);
        this.leftBlueFin.roll = 0.2f - 0.4f * MathHelper.sin(arg.age * 0.2f);
    }
}

