/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.waypoint;

import net.minecraft.entity.Entity;

@FunctionalInterface
public interface EntityTickProgress {
    public float getTickProgress(Entity var1);
}

