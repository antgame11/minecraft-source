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
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PiglinEntityModel;

@Environment(value=EnvType.CLIENT)
public class PiglinHeadEntityModel
extends SkullBlockEntityModel {
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;

    public PiglinHeadEntityModel(ModelPart root) {
        super(root);
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.leftEar = this.head.getChild(EntityModelPartNames.LEFT_EAR);
        this.rightEar = this.head.getChild(EntityModelPartNames.RIGHT_EAR);
    }

    public static ModelData getModelData() {
        ModelData lv = new ModelData();
        PiglinEntityModel.getModelPartData(Dilation.NONE, lv);
        return lv;
    }

    @Override
    public void setAngles(SkullBlockEntityModel.SkullModelState arg) {
        super.setAngles(arg);
        this.head.yaw = arg.yaw * ((float)Math.PI / 180);
        this.head.pitch = arg.pitch * ((float)Math.PI / 180);
        float f = 1.2f;
        this.leftEar.roll = (float)(-(Math.cos(arg.poweredTicks * (float)Math.PI * 0.2f * 1.2f) + 2.5)) * 0.2f;
        this.rightEar.roll = (float)(Math.cos(arg.poweredTicks * (float)Math.PI * 0.2f) + 2.5) * 0.2f;
    }
}

