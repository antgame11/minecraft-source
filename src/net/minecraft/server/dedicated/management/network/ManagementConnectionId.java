/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.network;

public record ManagementConnectionId(Integer connectionId) {
    public static ManagementConnectionId of(Integer connectionId) {
        return new ManagementConnectionId(connectionId);
    }
}

