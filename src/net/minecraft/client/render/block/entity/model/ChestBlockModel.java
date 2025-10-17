/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

@Environment(value=EnvType.CLIENT)
public class ChestBlockModel
extends Model<Float> {
    private static final String BOTTOM = "bottom";
    private static final String LID = "lid";
    private static final String LOCK = "lock";
    private final ModelPart lid;
    private final ModelPart lock;

    public ChestBlockModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);
        this.lid = root.getChild(LID);
        this.lock = root.getChild(LOCK);
    }

    public static TexturedModelData getSingleTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.BOTTOM, ModelPartBuilder.create().uv(0, 19).cuboid(1.0f, 0.0f, 1.0f, 14.0f, 10.0f, 14.0f), ModelTransform.NONE);
        lv2.addChild(LID, ModelPartBuilder.create().uv(0, 0).cuboid(1.0f, 0.0f, 0.0f, 14.0f, 5.0f, 14.0f), ModelTransform.origin(0.0f, 9.0f, 1.0f));
        lv2.addChild(LOCK, ModelPartBuilder.create().uv(0, 0).cuboid(7.0f, -2.0f, 14.0f, 2.0f, 4.0f, 1.0f), ModelTransform.origin(0.0f, 9.0f, 1.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getDoubleChestRightTexturedBlockData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.BOTTOM, ModelPartBuilder.create().uv(0, 19).cuboid(1.0f, 0.0f, 1.0f, 15.0f, 10.0f, 14.0f), ModelTransform.NONE);
        lv2.addChild(LID, ModelPartBuilder.create().uv(0, 0).cuboid(1.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f), ModelTransform.origin(0.0f, 9.0f, 1.0f));
        lv2.addChild(LOCK, ModelPartBuilder.create().uv(0, 0).cuboid(15.0f, -2.0f, 14.0f, 1.0f, 4.0f, 1.0f), ModelTransform.origin(0.0f, 9.0f, 1.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getDoubleChestLeftTexturedBlockData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.BOTTOM, ModelPartBuilder.create().uv(0, 19).cuboid(0.0f, 0.0f, 1.0f, 15.0f, 10.0f, 14.0f), ModelTransform.NONE);
        lv2.addChild(LID, ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f), ModelTransform.origin(0.0f, 9.0f, 1.0f));
        lv2.addChild(LOCK, ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, -2.0f, 14.0f, 1.0f, 4.0f, 1.0f), ModelTransform.origin(0.0f, 9.0f, 1.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public void setAngles(Float float_) {
        super.setAngles(float_);
        this.lock.pitch = this.lid.pitch = -(float_.floatValue() * 1.5707964f);
    }
}

