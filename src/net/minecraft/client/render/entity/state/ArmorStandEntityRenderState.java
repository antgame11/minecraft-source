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
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.EulerAngle;

@Environment(value=EnvType.CLIENT)
public class ArmorStandEntityRenderState
extends BipedEntityRenderState {
    public float yaw;
    public float timeSinceLastHit;
    public boolean marker;
    public boolean small;
    public boolean showArms;
    public boolean showBasePlate = true;
    public EulerAngle headRotation = ArmorStandEntity.DEFAULT_HEAD_ROTATION;
    public EulerAngle bodyRotation = ArmorStandEntity.DEFAULT_BODY_ROTATION;
    public EulerAngle leftArmRotation = ArmorStandEntity.DEFAULT_LEFT_ARM_ROTATION;
    public EulerAngle rightArmRotation = ArmorStandEntity.DEFAULT_RIGHT_ARM_ROTATION;
    public EulerAngle leftLegRotation = ArmorStandEntity.DEFAULT_LEFT_LEG_ROTATION;
    public EulerAngle rightLegRotation = ArmorStandEntity.DEFAULT_RIGHT_LEG_ROTATION;
}

