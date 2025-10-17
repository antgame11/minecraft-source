/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.ForcedUsernameChangeException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserBannedException;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.security.PublicKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientDynamicRegistryType;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientChunkLoadProgress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.login.EnterConfigurationC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.ServerLinks;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientLoginNetworkHandler
implements ClientLoginPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftClient client;
    @Nullable
    private final ServerInfo serverInfo;
    @Nullable
    private final Screen parentScreen;
    private final Consumer<Text> statusConsumer;
    private final ClientConnection connection;
    private final boolean newWorld;
    @Nullable
    private final Duration worldLoadTime;
    @Nullable
    private String minigameName;
    private final ClientChunkLoadProgress field_61722;
    private final Map<Identifier, byte[]> serverCookies;
    private final boolean hasCookies;
    private final Map<UUID, PlayerListEntry> field_62024;
    private final boolean field_62025;
    private final AtomicReference<State> state = new AtomicReference<State>(State.CONNECTING);

    public ClientLoginNetworkHandler(ClientConnection connection, MinecraftClient client, @Nullable ServerInfo serverInfo, @Nullable Screen parentScreen, boolean newWorld, @Nullable Duration worldLoadTime, Consumer<Text> statusConsumer, ClientChunkLoadProgress arg5, @Nullable CookieStorage arg6) {
        this.connection = connection;
        this.client = client;
        this.serverInfo = serverInfo;
        this.parentScreen = parentScreen;
        this.statusConsumer = statusConsumer;
        this.newWorld = newWorld;
        this.worldLoadTime = worldLoadTime;
        this.field_61722 = arg5;
        this.serverCookies = arg6 != null ? new HashMap<Identifier, byte[]>(arg6.cookies()) : new HashMap();
        this.field_62024 = arg6 != null ? arg6.seenPlayers() : Map.of();
        this.field_62025 = arg6 != null ? arg6.seenInsecureChatWarning() : false;
        this.hasCookies = arg6 != null;
    }

    private void switchTo(State state) {
        State lv = this.state.updateAndGet(currentState -> {
            if (!arg.prevStates.contains(currentState)) {
                throw new IllegalStateException("Tried to switch to " + String.valueOf((Object)state) + " from " + String.valueOf(currentState) + ", but expected one of " + String.valueOf(arg.prevStates));
            }
            return state;
        });
        this.statusConsumer.accept(lv.name);
    }

    @Override
    public void onHello(LoginHelloS2CPacket packet) {
        LoginKeyC2SPacket lv;
        Cipher cipher2;
        Cipher cipher;
        String string;
        this.switchTo(State.AUTHORIZING);
        try {
            SecretKey secretKey = NetworkEncryptionUtils.generateSecretKey();
            PublicKey publicKey = packet.getPublicKey();
            string = new BigInteger(NetworkEncryptionUtils.computeServerId(packet.getServerId(), publicKey, secretKey)).toString(16);
            cipher = NetworkEncryptionUtils.cipherFromKey(2, secretKey);
            cipher2 = NetworkEncryptionUtils.cipherFromKey(1, secretKey);
            byte[] bs = packet.getNonce();
            lv = new LoginKeyC2SPacket(secretKey, publicKey, bs);
        } catch (Exception exception) {
            throw new IllegalStateException("Protocol error", exception);
        }
        if (packet.needsAuthentication()) {
            Util.getIoWorkerExecutor().execute(() -> {
                Text lv = this.joinServerSession(string);
                if (lv != null) {
                    if (this.serverInfo != null && this.serverInfo.isLocal()) {
                        LOGGER.warn(lv.getString());
                    } else {
                        this.connection.disconnect(lv);
                        return;
                    }
                }
                this.setupEncryption(lv, cipher, cipher2);
            });
        } else {
            this.setupEncryption(lv, cipher, cipher2);
        }
    }

    private void setupEncryption(LoginKeyC2SPacket keyPacket, Cipher decryptionCipher, Cipher encryptionCipher) {
        this.switchTo(State.ENCRYPTING);
        this.connection.send(keyPacket, PacketCallbacks.always(() -> this.connection.setupEncryption(decryptionCipher, encryptionCipher)));
    }

    @Nullable
    private Text joinServerSession(String serverId) {
        try {
            this.client.getApiServices().sessionService().joinServer(this.client.getSession().getUuidOrNull(), this.client.getSession().getAccessToken(), serverId);
        } catch (AuthenticationUnavailableException authenticationUnavailableException) {
            return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.serversUnavailable"));
        } catch (InvalidCredentialsException invalidCredentialsException) {
            return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.invalidSession"));
        } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
            return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.insufficientPrivileges"));
        } catch (ForcedUsernameChangeException | UserBannedException authenticationException) {
            return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.userBanned"));
        } catch (AuthenticationException authenticationException) {
            return Text.translatable("disconnect.loginFailedInfo", authenticationException.getMessage());
        }
        return null;
    }

    @Override
    public void onSuccess(LoginSuccessS2CPacket packet) {
        this.switchTo(State.JOINING);
        GameProfile gameProfile = packet.profile();
        this.connection.transitionInbound(ConfigurationStates.S2C, new ClientConfigurationNetworkHandler(this.client, this.connection, new ClientConnectionState(this.field_61722, gameProfile, this.client.getTelemetryManager().createWorldSession(this.newWorld, this.worldLoadTime, this.minigameName), ClientDynamicRegistryType.createCombinedDynamicRegistries().getCombinedRegistryManager(), FeatureFlags.DEFAULT_ENABLED_FEATURES, null, this.serverInfo, this.parentScreen, this.serverCookies, null, Map.of(), ServerLinks.EMPTY, this.field_62024, false)));
        this.connection.send(EnterConfigurationC2SPacket.INSTANCE);
        this.connection.transitionOutbound(ConfigurationStates.C2S);
        this.connection.send(new CustomPayloadC2SPacket(new BrandCustomPayload(ClientBrandRetriever.getClientModName())));
        this.connection.send(new ClientOptionsC2SPacket(this.client.options.getSyncedOptions()));
    }

    @Override
    public void onDisconnected(DisconnectionInfo info) {
        Text lv;
        Text text = lv = this.hasCookies ? ScreenTexts.CONNECT_FAILED_TRANSFER : ScreenTexts.CONNECT_FAILED;
        if (this.serverInfo != null && this.serverInfo.isRealm()) {
            this.client.setScreen(new DisconnectedScreen(this.parentScreen, lv, info.reason(), ScreenTexts.BACK));
        } else {
            this.client.setScreen(new DisconnectedScreen(this.parentScreen, lv, info));
        }
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen();
    }

    @Override
    public void onDisconnect(LoginDisconnectS2CPacket packet) {
        this.connection.disconnect(packet.reason());
    }

    @Override
    public void onCompression(LoginCompressionS2CPacket packet) {
        if (!this.connection.isLocal()) {
            this.connection.setCompressionThreshold(packet.getCompressionThreshold(), false);
        }
    }

    @Override
    public void onQueryRequest(LoginQueryRequestS2CPacket packet) {
        this.statusConsumer.accept(Text.translatable("connect.negotiating"));
        this.connection.send(new LoginQueryResponseC2SPacket(packet.queryId(), null));
    }

    public void setMinigameName(@Nullable String minigameName) {
        this.minigameName = minigameName;
    }

    @Override
    public void onCookieRequest(CookieRequestS2CPacket packet) {
        this.connection.send(new CookieResponseC2SPacket(packet.key(), this.serverCookies.get(packet.key())));
    }

    @Override
    public void addCustomCrashReportInfo(CrashReport report, CrashReportSection section) {
        section.add("Server type", () -> this.serverInfo != null ? this.serverInfo.getServerType().toString() : "<unknown>");
        section.add("Login phase", () -> this.state.get().toString());
        section.add("Is Local", () -> String.valueOf(this.connection.isLocal()));
    }

    @Environment(value=EnvType.CLIENT)
    static enum State {
        CONNECTING(Text.translatable("connect.connecting"), Set.of()),
        AUTHORIZING(Text.translatable("connect.authorizing"), Set.of(CONNECTING)),
        ENCRYPTING(Text.translatable("connect.encrypting"), Set.of(AUTHORIZING)),
        JOINING(Text.translatable("connect.joining"), Set.of(ENCRYPTING, CONNECTING));

        final Text name;
        final Set<State> prevStates;

        private State(Text name, Set<State> prevStates) {
            this.name = name;
            this.prevStates = prevStates;
        }
    }
}

