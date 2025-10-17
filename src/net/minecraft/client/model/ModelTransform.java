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

@Environment(value=EnvType.CLIENT)
public record ModelTransform(float x, float y, float z, float pitch, float yaw, float roll, float xScale, float yScale, float zScale) {
    public static final ModelTransform NONE = ModelTransform.of(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

    public static ModelTransform origin(float x, float y, float z) {
        return ModelTransform.of(x, y, z, 0.0f, 0.0f, 0.0f);
    }

    public static ModelTransform rotation(float pitch, float yaw, float roll) {
        return ModelTransform.of(0.0f, 0.0f, 0.0f, pitch, yaw, roll);
    }

    public static ModelTransform of(float originX, float originY, float originZ, float pitch, float yaw, float roll) {
        return new ModelTransform(originX, originY, originZ, pitch, yaw, roll, 1.0f, 1.0f, 1.0f);
    }

    public ModelTransform moveOrigin(float x, float y, float z) {
        return new ModelTransform(this.x + x, this.y + y, this.z + z, this.pitch, this.yaw, this.roll, this.xScale, this.yScale, this.zScale);
    }

    public ModelTransform withScale(float scale) {
        return new ModelTransform(this.x, this.y, this.z, this.pitch, this.yaw, this.roll, scale, scale, scale);
    }

    public ModelTransform scaled(float scale) {
        if (scale == 1.0f) {
            return this;
        }
        return this.scaled(scale, scale, scale);
    }

    public ModelTransform scaled(float xScale, float yScale, float zScale) {
        return new ModelTransform(this.x * xScale, this.y * yScale, this.z * zScale, this.pitch, this.yaw, this.roll, this.xScale * xScale, this.yScale * yScale, this.zScale * zScale);
    }
}

