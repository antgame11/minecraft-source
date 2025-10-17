/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.SlimeEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class MagmaCubeEntityModel
extends EntityModel<SlimeEntityRenderState> {
    private static final int SLICES_COUNT = 8;
    private final ModelPart[] slices = new ModelPart[8];

    public MagmaCubeEntityModel(ModelPart arg) {
        super(arg);
        Arrays.setAll(this.slices, i -> arg.getChild(MagmaCubeEntityModel.getSliceName(i)));
    }

    private static String getSliceName(int index) {
        return "cube" + index;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        for (int i = 0; i < 8; ++i) {
            int j = 0;
            int k = 0;
            if (i > 0 && i < 4) {
                k += 9 * i;
            } else if (i > 3) {
                j = 32;
                k += 9 * i - 36;
            }
            lv2.addChild(MagmaCubeEntityModel.getSliceName(i), ModelPartBuilder.create().uv(j, k).cuboid(-4.0f, 16 + i, -4.0f, 8.0f, 1.0f, 8.0f), ModelTransform.NONE);
        }
        lv2.addChild("inside_cube", ModelPartBuilder.create().uv(24, 40).cuboid(-2.0f, 18.0f, -2.0f, 4.0f, 4.0f, 4.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public void setAngles(SlimeEntityRenderState arg) {
        super.setAngles(arg);
        float f = Math.max(0.0f, arg.stretch);
        for (int i = 0; i < this.slices.length; ++i) {
            this.slices[i].originY = (float)(-(4 - i)) * f * 1.7f;
        }
    }
}

