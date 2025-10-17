/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.entity;

import java.util.UUID;

public interface UniquelyIdentifiable {
    public UUID getUuid();

    public boolean isRemoved();
}

