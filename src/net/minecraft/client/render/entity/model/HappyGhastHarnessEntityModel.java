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
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.HappyGhastEntityModel;
import net.minecraft.client.render.entity.model.ModelTransformer;
import net.minecraft.client.render.entity.state.HappyGhastEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class HappyGhastHarnessEntityModel
extends EntityModel<HappyGhastEntityRenderState> {
    private static final float field_59950 = 14.0f;
    private final ModelPart goggles;

    public HappyGhastHarnessEntityModel(ModelPart arg) {
        super(arg);
        this.goggles = arg.getChild(EntityModelPartNames.GOGGLES);
    }

    public static TexturedModelData getTexturedModelData(boolean baby) {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HARNESS, ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, -16.0f, -8.0f, 16.0f, 16.0f, 16.0f), ModelTransform.origin(0.0f, 24.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.GOGGLES, ModelPartBuilder.create().uv(0, 32).cuboid(-8.0f, -2.5f, -2.5f, 16.0f, 5.0f, 5.0f, new Dilation(0.15f)), ModelTransform.origin(0.0f, 14.0f, -5.5f));
        return TexturedModelData.of(lv, 64, 64).transform(ModelTransformer.scaling(4.0f)).transform(baby ? HappyGhastEntityModel.BABY_TRANSFORMER : ModelTransformer.NO_OP);
    }

    @Override
    public void setAngles(HappyGhastEntityRenderState arg) {
        super.setAngles(arg);
        if (arg.hasPassengers) {
            this.goggles.pitch = 0.0f;
            this.goggles.originY = 14.0f;
        } else {
            this.goggles.pitch = -0.7854f;
            this.goggles.originY = 9.0f;
        }
    }
}

