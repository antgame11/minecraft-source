/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class ColorLerper {
    public static final DyeColor[] RAINBOW_COLORS = new DyeColor[]{DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.LIGHT_BLUE, DyeColor.BLUE, DyeColor.CYAN, DyeColor.GREEN, DyeColor.LIME, DyeColor.YELLOW, DyeColor.ORANGE, DyeColor.PINK, DyeColor.RED, DyeColor.MAGENTA};

    public static int lerpColor(Type type, float step) {
        int i = MathHelper.floor(step);
        int j = i / type.colorDuration;
        int k = type.colors.length;
        int l = j % k;
        int m = (j + 1) % k;
        float g = ((float)(i % type.colorDuration) + MathHelper.fractionalPart(step)) / (float)type.colorDuration;
        int n = type.getArgb(type.colors[l]);
        int o = type.getArgb(type.colors[m]);
        return ColorHelper.lerp(g, n, o);
    }

    static int getArgb(DyeColor color, float multiplier) {
        if (color == DyeColor.WHITE) {
            return -1644826;
        }
        int i = color.getEntityColor();
        return ColorHelper.getArgb(255, MathHelper.floor((float)ColorHelper.getRed(i) * multiplier), MathHelper.floor((float)ColorHelper.getGreen(i) * multiplier), MathHelper.floor((float)ColorHelper.getBlue(i) * multiplier));
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        SHEEP(25, DyeColor.values(), 0.75f),
        MUSIC_NOTE(30, RAINBOW_COLORS, 1.25f);

        final int colorDuration;
        private final Map<DyeColor, Integer> colorToArgb;
        final DyeColor[] colors;

        private Type(int colorDuration, DyeColor[] colors, float multiplier) {
            this.colorDuration = colorDuration;
            this.colorToArgb = Maps.newHashMap(Arrays.stream(colors).collect(Collectors.toMap(color -> color, color -> ColorLerper.getArgb(color, multiplier))));
            this.colors = colors;
        }

        public final int getArgb(DyeColor color) {
            return this.colorToArgb.get(color);
        }
    }
}

