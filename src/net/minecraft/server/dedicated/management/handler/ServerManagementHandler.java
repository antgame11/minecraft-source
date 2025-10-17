/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.Collection;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface ServerManagementHandler {
    public boolean isLoading();

    public boolean save(boolean var1, boolean var2, boolean var3, ManagementConnectionId var4);

    public void stop(boolean var1, ManagementConnectionId var2);

    public void broadcastMessage(Text var1, ManagementConnectionId var2);

    public void sendMessageTo(Text var1, boolean var2, Collection<ServerPlayerEntity> var3, ManagementConnectionId var4);

    public void broadcastMessage(Text var1, boolean var2, ManagementConnectionId var3);
}

