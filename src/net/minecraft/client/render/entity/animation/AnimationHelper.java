/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class AnimationHelper {
    public static Vector3f createTranslationalVector(float x, float y, float z) {
        return new Vector3f(x, -y, z);
    }

    public static Vector3f createRotationalVector(float x, float y, float z) {
        return new Vector3f(x * ((float)Math.PI / 180), y * ((float)Math.PI / 180), z * ((float)Math.PI / 180));
    }

    public static Vector3f createScalingVector(double x, double y, double z) {
        return new Vector3f((float)(x - 1.0), (float)(y - 1.0), (float)(z - 1.0));
    }
}

