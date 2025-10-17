/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client;

import com.mojang.logging.LogUtils;
import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerList;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.task.RealmsPrepareConnectionTask;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class QuickPlay {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Text ERROR_TITLE = Text.translatable("quickplay.error.title");
    private static final Text ERROR_INVALID_IDENTIFIER = Text.translatable("quickplay.error.invalid_identifier");
    private static final Text ERROR_REALM_CONNECT = Text.translatable("quickplay.error.realm_connect");
    private static final Text ERROR_REALM_PERMISSION = Text.translatable("quickplay.error.realm_permission");
    private static final Text TO_TITLE = Text.translatable("gui.toTitle");
    private static final Text TO_WORLD = Text.translatable("gui.toWorld");
    private static final Text TO_REALMS = Text.translatable("gui.toRealms");

    public static void startQuickPlay(MinecraftClient client, RunArgs.QuickPlayVariant variant, RealmsClient realmsClient) {
        if (!variant.isEnabled()) {
            LOGGER.error("Quick play disabled");
            client.setScreen(new TitleScreen());
            return;
        }
        RunArgs.QuickPlayVariant quickPlayVariant = variant;
        Objects.requireNonNull(quickPlayVariant);
        RunArgs.QuickPlayVariant quickPlayVariant2 = quickPlayVariant;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{RunArgs.MultiplayerQuickPlay.class, RunArgs.RealmsQuickPlay.class, RunArgs.SingleplayerQuickPlay.class, RunArgs.DisabledQuickPlay.class}, (Object)quickPlayVariant2, n)) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                RunArgs.MultiplayerQuickPlay lv = (RunArgs.MultiplayerQuickPlay)quickPlayVariant2;
                QuickPlay.startMultiplayer(client, lv.serverAddress());
                break;
            }
            case 1: {
                RunArgs.RealmsQuickPlay lv2 = (RunArgs.RealmsQuickPlay)quickPlayVariant2;
                QuickPlay.startRealms(client, realmsClient, lv2.realmId());
                break;
            }
            case 2: {
                RunArgs.SingleplayerQuickPlay lv3 = (RunArgs.SingleplayerQuickPlay)quickPlayVariant2;
                String string = lv3.worldId();
                if (StringHelper.isBlank(string)) {
                    string = QuickPlay.getLatestLevelName(client.getLevelStorage());
                }
                QuickPlay.startSingleplayer(client, string);
                break;
            }
            case 3: {
                RunArgs.DisabledQuickPlay lv4 = (RunArgs.DisabledQuickPlay)quickPlayVariant2;
                LOGGER.error("Quick play disabled");
                client.setScreen(new TitleScreen());
            }
        }
    }

    @Nullable
    private static String getLatestLevelName(LevelStorage storage) {
        try {
            List<LevelSummary> list = storage.loadSummaries(storage.getLevelList()).get();
            if (list.isEmpty()) {
                LOGGER.warn("no latest singleplayer world found");
                return null;
            }
            return list.getFirst().getName();
        } catch (InterruptedException | ExecutionException exception) {
            LOGGER.error("failed to load singleplayer world summaries", exception);
            return null;
        }
    }

    private static void startSingleplayer(MinecraftClient client, @Nullable String levelName) {
        if (StringHelper.isBlank(levelName) || !client.getLevelStorage().levelExists(levelName)) {
            SelectWorldScreen lv = new SelectWorldScreen(new TitleScreen());
            client.setScreen(new DisconnectedScreen((Screen)lv, ERROR_TITLE, ERROR_INVALID_IDENTIFIER, TO_WORLD));
            return;
        }
        client.createIntegratedServerLoader().start(levelName, () -> client.setScreen(new TitleScreen()));
    }

    private static void startMultiplayer(MinecraftClient client, String serverAddress) {
        ServerList lv = new ServerList(client);
        lv.loadFile();
        ServerInfo lv2 = lv.get(serverAddress);
        if (lv2 == null) {
            lv2 = new ServerInfo(I18n.translate("selectServer.defaultName", new Object[0]), serverAddress, ServerInfo.ServerType.OTHER);
            lv.add(lv2, true);
            lv.saveFile();
        }
        ServerAddress lv3 = ServerAddress.parse(serverAddress);
        ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), client, lv3, lv2, true, null);
    }

    private static void startRealms(MinecraftClient client, RealmsClient realmsClient, String realmId) {
        RealmsServerList lv;
        long l;
        try {
            l = Long.parseLong(realmId);
            lv = realmsClient.listWorlds();
        } catch (NumberFormatException numberFormatException) {
            RealmsMainScreen lv2 = new RealmsMainScreen(new TitleScreen());
            client.setScreen(new DisconnectedScreen((Screen)lv2, ERROR_TITLE, ERROR_INVALID_IDENTIFIER, TO_REALMS));
            return;
        } catch (RealmsServiceException lv3) {
            TitleScreen lv2 = new TitleScreen();
            client.setScreen(new DisconnectedScreen((Screen)lv2, ERROR_TITLE, ERROR_REALM_CONNECT, TO_TITLE));
            return;
        }
        RealmsServer lv4 = lv.servers.stream().filter(server -> server.id == l).findFirst().orElse(null);
        if (lv4 == null) {
            RealmsMainScreen lv2 = new RealmsMainScreen(new TitleScreen());
            client.setScreen(new DisconnectedScreen((Screen)lv2, ERROR_TITLE, ERROR_REALM_PERMISSION, TO_REALMS));
            return;
        }
        TitleScreen lv5 = new TitleScreen();
        client.setScreen(new RealmsLongRunningMcoTaskScreen(lv5, new RealmsPrepareConnectionTask(lv5, lv4)));
    }
}

