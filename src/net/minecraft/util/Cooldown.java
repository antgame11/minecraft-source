/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

public class Cooldown {
    private final int increment;
    private final int threshold;
    private int current;

    public Cooldown(int increment, int threshold) {
        this.increment = increment;
        this.threshold = threshold;
    }

    public void increment() {
        this.current += this.increment;
    }

    public void tick() {
        if (this.current > 0) {
            --this.current;
        }
    }

    public boolean canUse() {
        return this.current < this.threshold;
    }
}

