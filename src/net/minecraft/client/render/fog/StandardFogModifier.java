/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.fog;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.source.BiomeAccess;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public abstract class StandardFogModifier
extends FogModifier {
    @Override
    public int getFogColor(ClientWorld world, Camera camera, int viewDistance, float skyDarkness) {
        float u;
        float t;
        float m;
        float g = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(skyDarkness) * ((float)Math.PI * 2)) * 2.0f + 0.5f, 0.0f, 1.0f);
        BiomeAccess lv = world.getBiomeAccess();
        Vec3d lv2 = camera.getPos().subtract(2.0, 2.0, 2.0).multiply(0.25);
        Vec3d lv3 = world.getDimensionEffects().adjustFogColor(CubicSampler.sampleColor(lv2, (biomeX, biomeY, biomeZ) -> Vec3d.unpackRgb(lv.getBiomeForNoiseGen(biomeX, biomeY, biomeZ).value().getFogColor())), g);
        float h = (float)lv3.getX();
        float j = (float)lv3.getY();
        float k = (float)lv3.getZ();
        if (viewDistance >= 4) {
            float l = MathHelper.sin(world.getSkyAngleRadians(skyDarkness)) > 0.0f ? -1.0f : 1.0f;
            Vector3f vector3f = new Vector3f(l, 0.0f, 0.0f);
            m = camera.getHorizontalPlane().dot(vector3f);
            if (m > 0.0f && world.getDimensionEffects().isSunRisingOrSetting(world.getSkyAngle(skyDarkness))) {
                int n = world.getDimensionEffects().getSkyColor(world.getSkyAngle(skyDarkness));
                h = MathHelper.lerp(m *= ColorHelper.getAlphaFloat(n), h, ColorHelper.getRedFloat(n));
                j = MathHelper.lerp(m, j, ColorHelper.getGreenFloat(n));
                k = MathHelper.lerp(m, k, ColorHelper.getBlueFloat(n));
            }
        }
        int o = world.getSkyColor(camera.getPos(), skyDarkness);
        float p = ColorHelper.getRedFloat(o);
        m = ColorHelper.getGreenFloat(o);
        float q = ColorHelper.getBlueFloat(o);
        float r = 0.25f + 0.75f * (float)viewDistance / 32.0f;
        r = 1.0f - (float)Math.pow(r, 0.25);
        h += (p - h) * r;
        j += (m - j) * r;
        k += (q - k) * r;
        float s = world.getRainGradient(skyDarkness);
        if (s > 0.0f) {
            t = 1.0f - s * 0.5f;
            u = 1.0f - s * 0.4f;
            h *= t;
            j *= t;
            k *= u;
        }
        if ((t = world.getThunderGradient(skyDarkness)) > 0.0f) {
            u = 1.0f - t * 0.5f;
            h *= u;
            j *= u;
            k *= u;
        }
        return ColorHelper.fromFloats(1.0f, h, j, k);
    }
}

