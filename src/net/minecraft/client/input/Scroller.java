/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector2i;

@Environment(value=EnvType.CLIENT)
public class Scroller {
    private double cumulHorizontal;
    private double cumulVertical;

    public Vector2i update(double horizontal, double vertical) {
        if (this.cumulHorizontal != 0.0 && Math.signum(horizontal) != Math.signum(this.cumulHorizontal)) {
            this.cumulHorizontal = 0.0;
        }
        if (this.cumulVertical != 0.0 && Math.signum(vertical) != Math.signum(this.cumulVertical)) {
            this.cumulVertical = 0.0;
        }
        this.cumulHorizontal += horizontal;
        this.cumulVertical += vertical;
        int i = (int)this.cumulHorizontal;
        int j = (int)this.cumulVertical;
        if (i == 0 && j == 0) {
            return new Vector2i(0, 0);
        }
        this.cumulHorizontal -= (double)i;
        this.cumulVertical -= (double)j;
        return new Vector2i(i, j);
    }

    public static int scrollCycling(double amount, int selectedIndex, int total) {
        int k = (int)Math.signum(amount);
        selectedIndex -= k;
        for (selectedIndex = Math.max(-1, selectedIndex); selectedIndex < 0; selectedIndex += total) {
        }
        while (selectedIndex >= total) {
            selectedIndex -= total;
        }
        return selectedIndex;
    }
}

