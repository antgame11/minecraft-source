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
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class PlayerCapeModel
extends PlayerEntityModel {
    private static final String CAPE = "cape";
    private final ModelPart cape;

    public PlayerCapeModel(ModelPart arg) {
        super(arg, false);
        this.cape = this.body.getChild(CAPE);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = PlayerEntityModel.getTexturedModelData(Dilation.NONE, false);
        ModelPartData lv2 = lv.getRoot().resetChildrenParts();
        ModelPartData lv3 = lv2.getChild(EntityModelPartNames.BODY);
        lv3.addChild(CAPE, ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, 0.0f, -1.0f, 10.0f, 16.0f, 1.0f, Dilation.NONE, 1.0f, 0.5f), ModelTransform.of(0.0f, 0.0f, 2.0f, 0.0f, (float)Math.PI, 0.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public void setAngles(PlayerEntityRenderState arg) {
        super.setAngles(arg);
        this.cape.rotate(new Quaternionf().rotateY((float)(-Math.PI)).rotateX((6.0f + arg.field_53537 / 2.0f + arg.field_53536) * ((float)Math.PI / 180)).rotateZ(arg.field_53538 / 2.0f * ((float)Math.PI / 180)).rotateY((180.0f - arg.field_53538 / 2.0f) * ((float)Math.PI / 180)));
    }
}

