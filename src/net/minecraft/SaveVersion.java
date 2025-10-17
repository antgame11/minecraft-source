/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft;

import net.minecraft.SharedConstants;

public record SaveVersion(int id, String series) {
    public static final String MAIN_SERIES = "main";

    public boolean isNotMainSeries() {
        return !this.series.equals(MAIN_SERIES);
    }

    public boolean isAvailableTo(SaveVersion other) {
        if (SharedConstants.OPEN_INCOMPATIBLE_WORLDS) {
            return true;
        }
        return this.series().equals(other.series());
    }
}

