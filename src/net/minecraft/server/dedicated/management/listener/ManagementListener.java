/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.listener;

import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

public interface ManagementListener {
    public void onPlayerJoined(ServerPlayerEntity var1);

    public void onPlayerLeft(ServerPlayerEntity var1);

    public void onServerStarted();

    public void onServerStopping();

    public void onServerSaving();

    public void onServerSaved();

    public void onOperatorAdded(OperatorEntry var1);

    public void onOperatorRemoved(OperatorEntry var1);

    public void onAllowlistAdded(PlayerConfigEntry var1);

    public void onAllowlistRemoved(PlayerConfigEntry var1);

    public void onIpBanAdded(BannedIpEntry var1);

    public void onIpBanRemoved(String var1);

    public void onBanAdded(BannedPlayerEntry var1);

    public void onBanRemoved(PlayerConfigEntry var1);

    public void onGameRuleUpdated(String var1, GameRules.Rule<?> var2);

    public void onServerStatusHeartbeat();
}

