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
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.FelineEntityModel;
import net.minecraft.client.render.entity.model.ModelTransformer;
import net.minecraft.client.render.entity.state.CatEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class CatEntityModel
extends FelineEntityModel<CatEntityRenderState> {
    public static final ModelTransformer CAT_TRANSFORMER = ModelTransformer.scaling(0.8f);

    public CatEntityModel(ModelPart arg) {
        super(arg);
    }
}

