/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.passive.ParrotEntity;

@Environment(value=EnvType.CLIENT)
public class ParrotEntityRenderState
extends LivingEntityRenderState {
    public ParrotEntity.Variant variant = ParrotEntity.Variant.RED_BLUE;
    public float flapAngle;
    public ParrotEntityModel.Pose parrotPose = ParrotEntityModel.Pose.FLYING;
}

