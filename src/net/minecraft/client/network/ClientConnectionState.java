/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.session.telemetry.WorldSession;
import net.minecraft.client.world.ClientChunkLoadProgress;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.ServerLinks;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ClientConnectionState(ClientChunkLoadProgress chunkLoadProgress, GameProfile localGameProfile, WorldSession worldSession, DynamicRegistryManager.Immutable receivedRegistries, FeatureSet enabledFeatures, @Nullable String serverBrand, @Nullable ServerInfo serverInfo, @Nullable Screen postDisconnectScreen, Map<Identifier, byte[]> serverCookies, @Nullable ChatHud.ChatState chatState, Map<String, String> customReportDetails, ServerLinks serverLinks, Map<UUID, PlayerListEntry> seenPlayers, boolean seenInsecureChatWarning) {
    @Nullable
    public String serverBrand() {
        return this.serverBrand;
    }

    @Nullable
    public ServerInfo serverInfo() {
        return this.serverInfo;
    }

    @Nullable
    public Screen postDisconnectScreen() {
        return this.postDisconnectScreen;
    }

    @Nullable
    public ChatHud.ChatState chatState() {
        return this.chatState;
    }
}

