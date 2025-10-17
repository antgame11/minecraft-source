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
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Direction;

@Environment(value=EnvType.CLIENT)
public class CopperGolemStatueModel
extends Model<Direction> {
    public CopperGolemStatueModel(ModelPart root) {
        super(root, RenderLayer::getEntityCutoutNoCull);
    }

    @Override
    public void setAngles(Direction arg) {
        this.root.originY = 0.0f;
        this.root.yaw = arg.getOpposite().getPositiveHorizontalDegrees() * ((float)Math.PI / 180);
        this.root.roll = (float)Math.PI;
    }
}

