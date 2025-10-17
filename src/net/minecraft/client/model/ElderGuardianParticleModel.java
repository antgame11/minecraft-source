/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Unit;

@Environment(value=EnvType.CLIENT)
public class ElderGuardianParticleModel
extends Model<Unit> {
    public ElderGuardianParticleModel(ModelPart part) {
        super(part, RenderLayer::getEntityCutoutNoCull);
    }
}

