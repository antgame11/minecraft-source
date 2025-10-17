/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.server.GameProfileResolver;

@Environment(value=EnvType.CLIENT)
public class ClientPlayerProfileResolver
implements GameProfileResolver {
    private final MinecraftClient client;
    private final GameProfileResolver profileResolver;

    public ClientPlayerProfileResolver(MinecraftClient client, GameProfileResolver profileResolver) {
        this.client = client;
        this.profileResolver = profileResolver;
    }

    @Override
    public Optional<GameProfile> getProfileByName(String name) {
        PlayerListEntry lv2;
        ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
        if (lv != null && (lv2 = lv.getCaseInsensitivePlayerInfo(name)) != null) {
            return Optional.of(lv2.getProfile());
        }
        return this.profileResolver.getProfileByName(name);
    }

    @Override
    public Optional<GameProfile> getProfileById(UUID id) {
        PlayerListEntry lv2;
        ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
        if (lv != null && (lv2 = lv.getPlayerListEntry(id)) != null) {
            return Optional.of(lv2.getProfile());
        }
        return this.profileResolver.getProfileById(id);
    }
}

