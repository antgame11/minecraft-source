/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum SideChainPart implements StringIdentifiable
{
    UNCONNECTED("unconnected"),
    RIGHT("right"),
    CENTER("center"),
    LEFT("left");

    private final String id;

    private SideChainPart(String id) {
        this.id = id;
    }

    public String toString() {
        return this.asString();
    }

    @Override
    public String asString() {
        return this.id;
    }

    public boolean isConnected() {
        return this != UNCONNECTED;
    }

    public boolean isCenterOr(SideChainPart sideChainPart) {
        return this == CENTER || this == sideChainPart;
    }

    public boolean isNotCenter() {
        return this != CENTER;
    }

    public SideChainPart connectToRight() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 3 -> LEFT;
            case 1, 2 -> CENTER;
        };
    }

    public SideChainPart connectToLeft() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 1 -> RIGHT;
            case 2, 3 -> CENTER;
        };
    }

    public SideChainPart disconnectFromRight() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 3 -> UNCONNECTED;
            case 1, 2 -> RIGHT;
        };
    }

    public SideChainPart disconnectFromLeft() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 1 -> UNCONNECTED;
            case 2, 3 -> LEFT;
        };
    }
}

