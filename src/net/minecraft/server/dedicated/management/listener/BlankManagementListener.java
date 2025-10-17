/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.listener;

import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.management.listener.ManagementListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

public class BlankManagementListener
implements ManagementListener {
    @Override
    public void onPlayerJoined(ServerPlayerEntity player) {
    }

    @Override
    public void onPlayerLeft(ServerPlayerEntity player) {
    }

    @Override
    public void onServerStarted() {
    }

    @Override
    public void onServerStopping() {
    }

    @Override
    public void onServerSaving() {
    }

    @Override
    public void onServerSaved() {
    }

    @Override
    public void onOperatorAdded(OperatorEntry operator) {
    }

    @Override
    public void onOperatorRemoved(OperatorEntry operator) {
    }

    @Override
    public void onAllowlistAdded(PlayerConfigEntry player) {
    }

    @Override
    public void onAllowlistRemoved(PlayerConfigEntry player) {
    }

    @Override
    public void onIpBanAdded(BannedIpEntry entry) {
    }

    @Override
    public void onIpBanRemoved(String string) {
    }

    @Override
    public void onBanAdded(BannedPlayerEntry entry) {
    }

    @Override
    public void onBanRemoved(PlayerConfigEntry player) {
    }

    @Override
    public void onGameRuleUpdated(String gameRuleKey, GameRules.Rule<?> gameRule) {
    }

    @Override
    public void onServerStatusHeartbeat() {
    }
}

