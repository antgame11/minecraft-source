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

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface ModelTransformer {
    public static final ModelTransformer NO_OP = data -> data;

    public static ModelTransformer scaling(float scale) {
        float g = 24.016f * (1.0f - scale);
        return data -> data.transform(transform -> transform.scaled(scale).moveOrigin(0.0f, g, 0.0f));
    }

    public ModelData apply(ModelData var1);
}

