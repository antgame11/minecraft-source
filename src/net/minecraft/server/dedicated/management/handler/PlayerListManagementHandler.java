/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public interface PlayerListManagementHandler {
    public List<ServerPlayerEntity> getPlayerList();

    @Nullable
    public ServerPlayerEntity getPlayer(UUID var1);

    default public CompletableFuture<Optional<PlayerConfigEntry>> getPlayerAsync(Optional<UUID> uuid, Optional<String> name) {
        if (uuid.isPresent()) {
            Optional<PlayerConfigEntry> optional3 = this.getByUuid(uuid.get());
            if (optional3.isPresent()) {
                return CompletableFuture.completedFuture(optional3);
            }
            return CompletableFuture.supplyAsync(() -> this.fetchPlayer((UUID)uuid.get()), Util.getDownloadWorkerExecutor());
        }
        if (name.isPresent()) {
            return CompletableFuture.supplyAsync(() -> this.findByName((String)name.get()), Util.getDownloadWorkerExecutor());
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    public Optional<PlayerConfigEntry> findByName(String var1);

    public Optional<PlayerConfigEntry> fetchPlayer(UUID var1);

    public Optional<PlayerConfigEntry> getByUuid(UUID var1);

    public Optional<ServerPlayerEntity> getPlayer(Optional<UUID> var1, Optional<String> var2);

    public List<ServerPlayerEntity> getPlayersByIpAddress(String var1);

    @Nullable
    public ServerPlayerEntity getPlayer(String var1);

    public void removePlayer(ServerPlayerEntity var1, ManagementConnectionId var2);
}

