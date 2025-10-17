/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

@Environment(value=EnvType.CLIENT)
public abstract class DimensionEffects {
    private static final Object2ObjectMap<Identifier, DimensionEffects> BY_IDENTIFIER = Util.make(new Object2ObjectArrayMap(), map -> {
        Overworld lv = new Overworld();
        map.defaultReturnValue(lv);
        map.put(DimensionTypes.OVERWORLD_ID, lv);
        map.put(DimensionTypes.THE_NETHER_ID, new Nether());
        map.put(DimensionTypes.THE_END_ID, new End());
    });
    private final SkyType skyType;
    private final boolean darkened;
    private final boolean alternateSkyColor;

    public DimensionEffects(SkyType skyType, boolean darkened, boolean alternateSkyColor) {
        this.skyType = skyType;
        this.darkened = darkened;
        this.alternateSkyColor = alternateSkyColor;
    }

    public static DimensionEffects byDimensionType(DimensionType dimensionType) {
        return (DimensionEffects)BY_IDENTIFIER.get(dimensionType.effects());
    }

    public boolean isSunRisingOrSetting(float skyAngle) {
        return false;
    }

    public int getSkyColor(float skyAngle) {
        return 0;
    }

    public abstract Vec3d adjustFogColor(Vec3d var1, float var2);

    public abstract boolean useThickFog(int var1, int var2);

    public SkyType getSkyType() {
        return this.skyType;
    }

    public boolean isDarkened() {
        return this.darkened;
    }

    public boolean hasAlternateSkyColor() {
        return this.alternateSkyColor;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SkyType {
        NONE,
        NORMAL,
        END;

    }

    @Environment(value=EnvType.CLIENT)
    public static class Overworld
    extends DimensionEffects {
        private static final float SUN_RISE_SET_THRESHOLD = 0.4f;

        public Overworld() {
            super(SkyType.NORMAL, false, false);
        }

        @Override
        public boolean isSunRisingOrSetting(float skyAngle) {
            float g = MathHelper.cos(skyAngle * ((float)Math.PI * 2));
            return g >= -0.4f && g <= 0.4f;
        }

        @Override
        public int getSkyColor(float skyAngle) {
            float g = MathHelper.cos(skyAngle * ((float)Math.PI * 2));
            float h = g / 0.4f * 0.5f + 0.5f;
            float i = MathHelper.square(1.0f - (1.0f - MathHelper.sin(h * (float)Math.PI)) * 0.99f);
            return ColorHelper.fromFloats(i, h * 0.3f + 0.7f, h * h * 0.7f + 0.2f, 0.2f);
        }

        @Override
        public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
            return color.multiply(sunHeight * 0.94f + 0.06f, sunHeight * 0.94f + 0.06f, sunHeight * 0.91f + 0.09f);
        }

        @Override
        public boolean useThickFog(int camX, int camY) {
            return false;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Nether
    extends DimensionEffects {
        public Nether() {
            super(SkyType.NONE, true, false);
        }

        @Override
        public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
            return color;
        }

        @Override
        public boolean useThickFog(int camX, int camY) {
            return true;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class End
    extends DimensionEffects {
        public End() {
            super(SkyType.END, false, true);
        }

        @Override
        public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
            return color.multiply(0.15f);
        }

        @Override
        public boolean useThickFog(int camX, int camY) {
            return false;
        }
    }
}

