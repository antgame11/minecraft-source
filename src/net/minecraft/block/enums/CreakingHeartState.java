/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum CreakingHeartState implements StringIdentifiable
{
    UPROOTED("uprooted"),
    DORMANT("dormant"),
    AWAKE("awake");

    private final String id;

    private CreakingHeartState(String id) {
        this.id = id;
    }

    public String toString() {
        return this.id;
    }

    @Override
    public String asString() {
        return this.id;
    }
}

