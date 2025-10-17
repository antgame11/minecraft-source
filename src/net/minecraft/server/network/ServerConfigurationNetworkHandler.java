/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.c2s.config.AcceptCodeOfConductC2SPacket;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.c2s.config.SelectKnownPacksC2SPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerLinksS2CPacket;
import net.minecraft.network.packet.s2c.config.FeaturesS2CPacket;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.JoinWorldTask;
import net.minecraft.server.network.PrepareSpawnTask;
import net.minecraft.server.network.SendCodeOfConductTask;
import net.minecraft.server.network.SendResourcePackTask;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.server.network.SynchronizeRegistriesTask;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerConfigurationNetworkHandler
extends ServerCommonNetworkHandler
implements ServerConfigurationPacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text INVALID_PLAYER_DATA_TEXT = Text.translatable("multiplayer.disconnect.invalid_player_data");
    private static final Text CONFIGURATION_ERROR_TEXT = Text.translatable("multiplayer.disconnect.configuration_error");
    private final GameProfile profile;
    private final Queue<ServerPlayerConfigurationTask> tasks = new ConcurrentLinkedQueue<ServerPlayerConfigurationTask>();
    @Nullable
    private ServerPlayerConfigurationTask currentTask;
    private SyncedClientOptions syncedOptions;
    @Nullable
    private SynchronizeRegistriesTask synchronizedRegistriesTask;
    @Nullable
    private PrepareSpawnTask prepareSpawnTask;

    public ServerConfigurationNetworkHandler(MinecraftServer minecraftServer, ClientConnection arg, ConnectedClientData arg2) {
        super(minecraftServer, arg, arg2);
        this.profile = arg2.gameProfile();
        this.syncedOptions = arg2.syncedOptions();
    }

    @Override
    protected GameProfile getProfile() {
        return this.profile;
    }

    @Override
    public void onDisconnected(DisconnectionInfo info) {
        LOGGER.info("{} ({}) lost connection: {}", this.profile.name(), this.profile.id(), info.reason().getString());
        if (this.prepareSpawnTask != null) {
            this.prepareSpawnTask.onDisconnected();
            this.prepareSpawnTask = null;
        }
        super.onDisconnected(info);
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen();
    }

    public void sendConfigurations() {
        this.sendPacket(new CustomPayloadS2CPacket(new BrandCustomPayload(this.server.getServerModName())));
        ServerLinks lv = this.server.getServerLinks();
        if (!lv.isEmpty()) {
            this.sendPacket(new ServerLinksS2CPacket(lv.getLinks()));
        }
        CombinedDynamicRegistries<ServerDynamicRegistryType> lv2 = this.server.getCombinedDynamicRegistries();
        List<VersionedIdentifier> list = this.server.getResourceManager().streamResourcePacks().flatMap(pack -> pack.getInfo().knownPackInfo().stream()).toList();
        this.sendPacket(new FeaturesS2CPacket(FeatureFlags.FEATURE_MANAGER.toId(this.server.getSaveProperties().getEnabledFeatures())));
        this.synchronizedRegistriesTask = new SynchronizeRegistriesTask(list, lv2);
        this.tasks.add(this.synchronizedRegistriesTask);
        this.queueSendResourcePackTask();
        this.endConfiguration();
    }

    public void endConfiguration() {
        this.prepareSpawnTask = new PrepareSpawnTask(this.server, new PlayerConfigEntry(this.profile));
        this.tasks.add(this.prepareSpawnTask);
        this.tasks.add(new JoinWorldTask());
        this.pollTask();
    }

    private void queueSendResourcePackTask() {
        Map<String, String> map = this.server.getCodeOfConductLanguages();
        if (!map.isEmpty()) {
            this.tasks.add(new SendCodeOfConductTask(() -> {
                String string = (String)map.get(this.syncedOptions.language().toLowerCase(Locale.ROOT));
                if (string == null) {
                    string = (String)map.get("en_us");
                }
                if (string == null) {
                    string = (String)map.values().iterator().next();
                }
                return string;
            }));
        }
        this.server.getResourcePackProperties().ifPresent(properties -> this.tasks.add(new SendResourcePackTask((MinecraftServer.ServerResourcePackProperties)properties)));
    }

    @Override
    public void onClientOptions(ClientOptionsC2SPacket packet) {
        this.syncedOptions = packet.options();
    }

    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {
        super.onResourcePackStatus(packet);
        if (packet.status().hasFinished()) {
            this.onTaskFinished(SendResourcePackTask.KEY);
        }
    }

    @Override
    public void onSelectKnownPacks(SelectKnownPacksC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.server.getPacketApplyBatcher());
        if (this.synchronizedRegistriesTask == null) {
            throw new IllegalStateException("Unexpected response from client: received pack selection, but no negotiation ongoing");
        }
        this.synchronizedRegistriesTask.onSelectKnownPacks(packet.knownPacks(), this::sendPacket);
        this.onTaskFinished(SynchronizeRegistriesTask.KEY);
    }

    @Override
    public void onAcceptCodeOfConduct(AcceptCodeOfConductC2SPacket packet) {
        this.onTaskFinished(SendCodeOfConductTask.KEY);
    }

    @Override
    public void onReady(ReadyC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.server.getPacketApplyBatcher());
        this.onTaskFinished(JoinWorldTask.KEY);
        this.connection.transitionOutbound(PlayStateFactories.S2C.bind(RegistryByteBuf.makeFactory(this.server.getRegistryManager())));
        try {
            PlayerManager lv = this.server.getPlayerManager();
            if (lv.getPlayer(this.profile.id()) != null) {
                this.disconnect(PlayerManager.DUPLICATE_LOGIN_TEXT);
                return;
            }
            Text lv2 = lv.checkCanJoin(this.connection.getAddress(), new PlayerConfigEntry(this.profile));
            if (lv2 != null) {
                this.disconnect(lv2);
                return;
            }
            Objects.requireNonNull(this.prepareSpawnTask).onReady(this.connection, this.createClientData(this.syncedOptions));
        } catch (Exception exception) {
            LOGGER.error("Couldn't place player in world", exception);
            this.disconnect(INVALID_PLAYER_DATA_TEXT);
        }
    }

    @Override
    public void tick() {
        this.baseTick();
        ServerPlayerConfigurationTask lv = this.currentTask;
        if (lv != null) {
            try {
                if (lv.hasFinished()) {
                    this.onTaskFinished(lv.getKey());
                }
            } catch (Exception exception) {
                LOGGER.error("Failed to tick configuration task {}", (Object)lv.getKey(), (Object)exception);
                this.disconnect(CONFIGURATION_ERROR_TEXT);
            }
        }
        if (this.prepareSpawnTask != null) {
            this.prepareSpawnTask.tick();
        }
    }

    private void pollTask() {
        if (this.currentTask != null) {
            throw new IllegalStateException("Task " + this.currentTask.getKey().id() + " has not finished yet");
        }
        if (!this.isConnectionOpen()) {
            return;
        }
        ServerPlayerConfigurationTask lv = this.tasks.poll();
        if (lv != null) {
            this.currentTask = lv;
            try {
                lv.sendPacket(this::sendPacket);
            } catch (Exception exception) {
                LOGGER.error("Failed to start configuration task {}", (Object)lv.getKey(), (Object)exception);
                this.disconnect(CONFIGURATION_ERROR_TEXT);
            }
        }
    }

    private void onTaskFinished(ServerPlayerConfigurationTask.Key key) {
        ServerPlayerConfigurationTask.Key lv;
        ServerPlayerConfigurationTask.Key key2 = lv = this.currentTask != null ? this.currentTask.getKey() : null;
        if (!key.equals(lv)) {
            throw new IllegalStateException("Unexpected request for task finish, current task: " + String.valueOf(lv) + ", requested: " + String.valueOf(key));
        }
        this.currentTask = null;
        this.pollTask();
    }
}

