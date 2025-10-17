/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.biome;

public interface BiomeColors {
    public static int getColor(double temperature, double downfall, int[] colormap, int fallback) {
        int k = (int)((1.0 - (downfall *= temperature)) * 255.0);
        int j = (int)((1.0 - temperature) * 255.0);
        int l = k << 8 | j;
        if (l >= colormap.length) {
            return fallback;
        }
        return colormap[l];
    }
}

