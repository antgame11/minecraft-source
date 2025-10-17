/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.tick;

import com.mojang.serialization.Codec;

public enum TickPriority {
    EXTREMELY_HIGH(-3),
    VERY_HIGH(-2),
    HIGH(-1),
    NORMAL(0),
    LOW(1),
    VERY_LOW(2),
    EXTREMELY_LOW(3);

    public static final Codec<TickPriority> CODEC;
    private final int index;

    private TickPriority(int index) {
        this.index = index;
    }

    public static TickPriority byIndex(int index) {
        for (TickPriority lv : TickPriority.values()) {
            if (lv.index != index) continue;
            return lv;
        }
        if (index < TickPriority.EXTREMELY_HIGH.index) {
            return EXTREMELY_HIGH;
        }
        return EXTREMELY_LOW;
    }

    public int getIndex() {
        return this.index;
    }

    static {
        CODEC = Codec.INT.xmap(TickPriority::byIndex, TickPriority::getIndex);
    }
}

