/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.Collection;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;

public interface BanManagementHandler {
    public void addPlayer(BannedPlayerEntry var1, ManagementConnectionId var2);

    public void removePlayer(PlayerConfigEntry var1, ManagementConnectionId var2);

    public Collection<BannedPlayerEntry> getUserBanList();

    public Collection<BannedIpEntry> getIpBanList();

    public void addIpAddress(BannedIpEntry var1, ManagementConnectionId var2);

    public void clearIpBanList(ManagementConnectionId var1);

    public void removeIpAddress(String var1, ManagementConnectionId var2);

    public void clearBanList(ManagementConnectionId var1);
}

