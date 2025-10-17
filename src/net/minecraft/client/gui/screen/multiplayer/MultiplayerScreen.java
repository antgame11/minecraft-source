/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.multiplayer;

import com.mojang.logging.LogUtils;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.network.LanServerQueryManager;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class MultiplayerScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_41850 = 100;
    private static final int field_41851 = 74;
    private final ThreePartsLayoutWidget field_62178 = new ThreePartsLayoutWidget(this, 33, 60);
    private final MultiplayerServerListPinger serverListPinger = new MultiplayerServerListPinger();
    private final Screen parent;
    protected MultiplayerServerListWidget serverListWidget;
    private ServerList serverList;
    private ButtonWidget buttonEdit;
    private ButtonWidget buttonJoin;
    private ButtonWidget buttonDelete;
    private ServerInfo selectedEntry;
    private LanServerQueryManager.LanServerEntryList lanServers;
    @Nullable
    private LanServerQueryManager.LanServerDetector lanServerDetector;

    public MultiplayerScreen(Screen parent) {
        super(Text.translatable("multiplayer.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.field_62178.addHeader(this.title, this.textRenderer);
        this.serverList = new ServerList(this.client);
        this.serverList.loadFile();
        this.lanServers = new LanServerQueryManager.LanServerEntryList();
        try {
            this.lanServerDetector = new LanServerQueryManager.LanServerDetector(this.lanServers);
            this.lanServerDetector.start();
        } catch (Exception exception) {
            LOGGER.warn("Unable to start LAN server detection: {}", (Object)exception.getMessage());
        }
        this.serverListWidget = this.field_62178.addBody(new MultiplayerServerListWidget(this, this.client, this.width, this.field_62178.getContentHeight(), this.field_62178.getHeaderHeight(), 36));
        this.serverListWidget.setServers(this.serverList);
        DirectionalLayoutWidget lv = this.field_62178.addFooter(DirectionalLayoutWidget.vertical().spacing(4));
        lv.getMainPositioner().alignHorizontalCenter();
        DirectionalLayoutWidget lv2 = lv.add(DirectionalLayoutWidget.horizontal().spacing(4));
        DirectionalLayoutWidget lv3 = lv.add(DirectionalLayoutWidget.horizontal().spacing(4));
        this.buttonJoin = lv2.add(ButtonWidget.builder(Text.translatable("selectServer.select"), button -> {
            MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
            if (lv != null) {
                lv.connect();
            }
        }).width(100).build());
        lv2.add(ButtonWidget.builder(Text.translatable("selectServer.direct"), button -> {
            this.selectedEntry = new ServerInfo(I18n.translate("selectServer.defaultName", new Object[0]), "", ServerInfo.ServerType.OTHER);
            this.client.setScreen(new DirectConnectScreen(this, this::directConnect, this.selectedEntry));
        }).width(100).build());
        lv2.add(ButtonWidget.builder(Text.translatable("selectServer.add"), button -> {
            this.selectedEntry = new ServerInfo("", "", ServerInfo.ServerType.OTHER);
            this.client.setScreen(new AddServerScreen(this, Text.translatable("manageServer.add.title"), this::addEntry, this.selectedEntry));
        }).width(100).build());
        this.buttonEdit = lv3.add(ButtonWidget.builder(Text.translatable("selectServer.edit"), button -> {
            MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
            if (lv instanceof MultiplayerServerListWidget.ServerEntry) {
                ServerInfo lv2 = ((MultiplayerServerListWidget.ServerEntry)lv).getServer();
                this.selectedEntry = new ServerInfo(lv2.name, lv2.address, ServerInfo.ServerType.OTHER);
                this.selectedEntry.copyWithSettingsFrom(lv2);
                this.client.setScreen(new AddServerScreen(this, Text.translatable("manageServer.edit.title"), this::editEntry, this.selectedEntry));
            }
        }).width(74).build());
        this.buttonDelete = lv3.add(ButtonWidget.builder(Text.translatable("selectServer.delete"), button -> {
            String string;
            MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
            if (lv instanceof MultiplayerServerListWidget.ServerEntry && (string = ((MultiplayerServerListWidget.ServerEntry)lv).getServer().name) != null) {
                MutableText lv2 = Text.translatable("selectServer.deleteQuestion");
                MutableText lv3 = Text.translatable("selectServer.deleteWarning", string);
                MutableText lv4 = Text.translatable("selectServer.deleteButton");
                Text lv5 = ScreenTexts.CANCEL;
                this.client.setScreen(new ConfirmScreen(this::removeEntry, lv2, lv3, lv4, lv5));
            }
        }).width(74).build());
        lv3.add(ButtonWidget.builder(Text.translatable("selectServer.refresh"), button -> this.refresh()).width(74).build());
        lv3.add(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).width(74).build());
        this.field_62178.forEachChild(arg2 -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(arg2);
        });
        this.refreshWidgetPositions();
        this.updateButtonActivationStates();
    }

    @Override
    protected void refreshWidgetPositions() {
        this.field_62178.refreshPositions();
        if (this.serverListWidget != null) {
            this.serverListWidget.position(this.width, this.field_62178);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void tick() {
        super.tick();
        List<LanServerInfo> list = this.lanServers.getEntriesIfUpdated();
        if (list != null) {
            this.serverListWidget.setLanServers(list);
        }
        this.serverListPinger.tick();
    }

    @Override
    public void removed() {
        if (this.lanServerDetector != null) {
            this.lanServerDetector.interrupt();
            this.lanServerDetector = null;
        }
        this.serverListPinger.cancel();
        this.serverListWidget.onRemoved();
    }

    private void refresh() {
        this.client.setScreen(new MultiplayerScreen(this.parent));
    }

    private void removeEntry(boolean confirmedAction) {
        MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
        if (confirmedAction && lv instanceof MultiplayerServerListWidget.ServerEntry) {
            this.serverList.remove(((MultiplayerServerListWidget.ServerEntry)lv).getServer());
            this.serverList.saveFile();
            this.serverListWidget.setSelected((MultiplayerServerListWidget.Entry)null);
            this.serverListWidget.setServers(this.serverList);
        }
        this.client.setScreen(this);
    }

    private void editEntry(boolean confirmedAction) {
        MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
        if (confirmedAction && lv instanceof MultiplayerServerListWidget.ServerEntry) {
            ServerInfo lv2 = ((MultiplayerServerListWidget.ServerEntry)lv).getServer();
            lv2.name = this.selectedEntry.name;
            lv2.address = this.selectedEntry.address;
            lv2.copyWithSettingsFrom(this.selectedEntry);
            this.serverList.saveFile();
            this.serverListWidget.setServers(this.serverList);
        }
        this.client.setScreen(this);
    }

    private void addEntry(boolean confirmedAction) {
        if (confirmedAction) {
            ServerInfo lv = this.serverList.tryUnhide(this.selectedEntry.address);
            if (lv != null) {
                lv.copyFrom(this.selectedEntry);
                this.serverList.saveFile();
            } else {
                this.serverList.add(this.selectedEntry, false);
                this.serverList.saveFile();
            }
            this.serverListWidget.setSelected((MultiplayerServerListWidget.Entry)null);
            this.serverListWidget.setServers(this.serverList);
        }
        this.client.setScreen(this);
    }

    private void directConnect(boolean confirmedAction) {
        if (confirmedAction) {
            ServerInfo lv = this.serverList.get(this.selectedEntry.address);
            if (lv == null) {
                this.serverList.add(this.selectedEntry, true);
                this.serverList.saveFile();
                this.connect(this.selectedEntry);
            } else {
                this.connect(lv);
            }
        } else {
            this.client.setScreen(this);
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (super.keyPressed(input)) {
            return true;
        }
        if (input.key() == InputUtil.GLFW_KEY_F5) {
            this.refresh();
            return true;
        }
        return false;
    }

    public void connect(ServerInfo entry) {
        ConnectScreen.connect(this, this.client, ServerAddress.parse(entry.address), entry, false, null);
    }

    protected void updateButtonActivationStates() {
        this.buttonJoin.active = false;
        this.buttonEdit.active = false;
        this.buttonDelete.active = false;
        MultiplayerServerListWidget.Entry lv = (MultiplayerServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
        if (lv != null && !(lv instanceof MultiplayerServerListWidget.ScanningEntry)) {
            this.buttonJoin.active = true;
            if (lv instanceof MultiplayerServerListWidget.ServerEntry) {
                this.buttonEdit.active = true;
                this.buttonDelete.active = true;
            }
        }
    }

    public MultiplayerServerListPinger getServerListPinger() {
        return this.serverListPinger;
    }

    public ServerList getServerList() {
        return this.serverList;
    }
}

