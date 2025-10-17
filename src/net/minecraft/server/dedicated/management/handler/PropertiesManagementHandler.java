/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.handler;

import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public interface PropertiesManagementHandler {
    public boolean getAutosave();

    public boolean setAutosave(boolean var1, ManagementConnectionId var2);

    public Difficulty getDifficulty();

    public Difficulty setDifficulty(Difficulty var1, ManagementConnectionId var2);

    public boolean getEnforceAllowlist();

    public boolean setEnforceAllowlist(boolean var1, ManagementConnectionId var2);

    public boolean getUseAllowlist();

    public boolean setUseAllowlist(boolean var1, ManagementConnectionId var2);

    public int getMaxPlayers();

    public int setMaxPlayers(int var1, ManagementConnectionId var2);

    public int getPauseWhenEmptySeconds();

    public int setPauseWhenEmptySeconds(int var1, ManagementConnectionId var2);

    public int getPlayerIdleTimeout();

    public int setPlayerIdleTimeout(int var1, ManagementConnectionId var2);

    public boolean getAllowFlight();

    public boolean setAllowFlight(boolean var1, ManagementConnectionId var2);

    public int getSpawnProtectionRadius();

    public int setSpawnProtectionRadius(int var1, ManagementConnectionId var2);

    public String getMotd();

    public String setMotd(String var1, ManagementConnectionId var2);

    public boolean getForceGameMode();

    public boolean setForceGameMode(boolean var1, ManagementConnectionId var2);

    public GameMode getGameMode();

    public GameMode setGameMode(GameMode var1, ManagementConnectionId var2);

    public int getViewDistance();

    public int setViewDistance(int var1, ManagementConnectionId var2);

    public int getSimulationDistance();

    public int setSimulationDistance(int var1, ManagementConnectionId var2);

    public boolean getAcceptTransfers();

    public boolean setAcceptTransfers(boolean var1, ManagementConnectionId var2);

    public int getStatusHeartbeatInterval();

    public int setStatusHeartbeatInterval(int var1, ManagementConnectionId var2);

    public int getOperatorUserPermissionLevel();

    public int setOperatorUserPermissionLevel(int var1, ManagementConnectionId var2);

    public boolean getHideOnlinePlayers();

    public boolean setHideOnlinePlayers(boolean var1, ManagementConnectionId var2);

    public boolean getStatusReplies();

    public boolean setStatusReplies(boolean var1, ManagementConnectionId var2);

    public int getEntityBroadcastRange();

    public int setEntityBroadcastRange(int var1, ManagementConnectionId var2);
}

