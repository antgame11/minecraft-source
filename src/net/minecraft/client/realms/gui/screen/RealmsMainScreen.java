/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.ProfilesTooltipComponent;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.IconWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.LoadingWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.realms.Ping;
import net.minecraft.client.realms.RealmsAvailability;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsPeriodicCheckers;
import net.minecraft.client.realms.dto.PingResult;
import net.minecraft.client.realms.dto.RealmsNews;
import net.minecraft.client.realms.dto.RealmsNotification;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerPlayerList;
import net.minecraft.client.realms.dto.RegionPingResult;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.RealmsPopups;
import net.minecraft.client.realms.gui.screen.BuyRealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsConfigureWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsCreateRealmScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsPendingInvitesScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.task.RealmsPrepareConnectionTask;
import net.minecraft.client.realms.util.PeriodicRunnerFactory;
import net.minecraft.client.realms.util.RealmsPersistence;
import net.minecraft.client.realms.util.RealmsServerFilterer;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Urls;
import net.minecraft.util.Util;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsMainScreen
extends RealmsScreen {
    static final Identifier INFO_ICON_TEXTURE = Identifier.ofVanilla("icon/info");
    static final Identifier NEW_REALM_ICON_TEXTURE = Identifier.ofVanilla("icon/new_realm");
    static final Identifier EXPIRED_STATUS_TEXTURE = Identifier.ofVanilla("realm_status/expired");
    static final Identifier EXPIRES_SOON_STATUS_TEXTURE = Identifier.ofVanilla("realm_status/expires_soon");
    static final Identifier OPEN_STATUS_TEXTURE = Identifier.ofVanilla("realm_status/open");
    static final Identifier CLOSED_STATUS_TEXTURE = Identifier.ofVanilla("realm_status/closed");
    private static final Identifier INVITE_ICON_TEXTURE = Identifier.ofVanilla("icon/invite");
    private static final Identifier NEWS_ICON_TEXTURE = Identifier.ofVanilla("icon/news");
    public static final Identifier HARDCORE_ICON_TEXTURE = Identifier.ofVanilla("hud/heart/hardcore_full");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier NO_REALMS_TEXTURE = Identifier.ofVanilla("textures/gui/realms/no_realms.png");
    private static final Text MENU_TEXT = Text.translatable("menu.online");
    private static final Text LOADING_TEXT = Text.translatable("mco.selectServer.loading");
    static final Text UNINITIALIZED_TEXT = Text.translatable("mco.selectServer.uninitialized");
    static final Text EXPIRED_LIST_TEXT = Text.translatable("mco.selectServer.expiredList");
    private static final Text EXPIRED_RENEW_TEXT = Text.translatable("mco.selectServer.expiredRenew");
    static final Text EXPIRED_TRIAL_TEXT = Text.translatable("mco.selectServer.expiredTrial");
    private static final Text PLAY_TEXT = Text.translatable("mco.selectServer.play");
    private static final Text LEAVE_TEXT = Text.translatable("mco.selectServer.leave");
    private static final Text CONFIGURE_TEXT = Text.translatable("mco.selectServer.configure");
    static final Text EXPIRED_TEXT = Text.translatable("mco.selectServer.expired");
    static final Text EXPIRES_SOON_TEXT = Text.translatable("mco.selectServer.expires.soon");
    static final Text EXPIRES_IN_A_DAY_TEXT = Text.translatable("mco.selectServer.expires.day");
    static final Text OPEN_TEXT = Text.translatable("mco.selectServer.open");
    static final Text CLOSED_TEXT = Text.translatable("mco.selectServer.closed");
    static final Text UNINITIALIZED_BUTTON_NARRATION = Text.translatable("gui.narrate.button", UNINITIALIZED_TEXT);
    private static final Text NO_REALMS_TEXT = Text.translatable("mco.selectServer.noRealms");
    private static final Text NO_PENDING_TOOLTIP = Text.translatable("mco.invites.nopending");
    private static final Text PENDING_TOOLTIP = Text.translatable("mco.invites.pending");
    private static final Text INCOMPATIBLE_POPUP_TITLE = Text.translatable("mco.compatibility.incompatible.popup.title");
    private static final Text INCOMPATIBLE_RELEASE_TYPE_MESSAGE = Text.translatable("mco.compatibility.incompatible.releaseType.popup.message");
    private static final int field_42862 = 100;
    private static final int field_45209 = 3;
    private static final int field_45210 = 4;
    private static final int field_45211 = 308;
    private static final int field_44513 = 5;
    private static final int field_44514 = 44;
    private static final int field_45212 = 11;
    private static final int field_46670 = 40;
    private static final int field_46671 = 20;
    private static final boolean GAME_ON_SNAPSHOT;
    private static boolean showingSnapshotRealms;
    private final CompletableFuture<RealmsAvailability.Info> availabilityInfo = RealmsAvailability.check();
    @Nullable
    private PeriodicRunnerFactory.RunnersManager periodicRunnersManager;
    private final Set<UUID> seenNotifications = new HashSet<UUID>();
    private static boolean regionsPinged;
    private final RateLimiter rateLimiter;
    private final Screen parent;
    private ButtonWidget playButton;
    private ButtonWidget backButton;
    private ButtonWidget renewButton;
    private ButtonWidget configureButton;
    private ButtonWidget leaveButton;
    RealmSelectionList realmSelectionList;
    RealmsServerFilterer serverFilterer;
    List<RealmsServer> availableSnapshotServers = List.of();
    RealmsServerPlayerList onlinePlayers = new RealmsServerPlayerList();
    private volatile boolean trialAvailable;
    @Nullable
    private volatile String newsLink;
    final List<RealmsNotification> notifications = new ArrayList<RealmsNotification>();
    private ButtonWidget purchaseButton;
    private NotificationButtonWidget inviteButton;
    private NotificationButtonWidget newsButton;
    private LoadStatus loadStatus;
    @Nullable
    private ThreePartsLayoutWidget layout;

    public RealmsMainScreen(Screen parent) {
        super(MENU_TEXT);
        this.parent = parent;
        this.rateLimiter = RateLimiter.create(0.01666666753590107);
    }

    @Override
    public void init() {
        this.serverFilterer = new RealmsServerFilterer(this.client);
        this.realmSelectionList = new RealmSelectionList();
        MutableText lv = Text.translatable("mco.invites.title");
        this.inviteButton = new NotificationButtonWidget(lv, INVITE_ICON_TEXTURE, button -> this.client.setScreen(new RealmsPendingInvitesScreen(this, lv)), null);
        MutableText lv2 = Text.translatable("mco.news");
        this.newsButton = new NotificationButtonWidget(lv2, NEWS_ICON_TEXTURE, button -> {
            String string = this.newsLink;
            if (string == null) {
                return;
            }
            ConfirmLinkScreen.open((Screen)this, string);
            if (this.newsButton.getNotificationCount() != 0) {
                RealmsPersistence.RealmsPersistenceData lv = RealmsPersistence.readFile();
                lv.hasUnreadNews = false;
                RealmsPersistence.writeFile(lv);
                this.newsButton.setNotificationCount(0);
            }
        }, lv2);
        this.playButton = ButtonWidget.builder(PLAY_TEXT, button -> RealmsMainScreen.play(this.getSelectedServer(), this)).width(100).build();
        this.configureButton = ButtonWidget.builder(CONFIGURE_TEXT, button -> this.configureClicked(this.getSelectedServer())).width(100).build();
        this.renewButton = ButtonWidget.builder(EXPIRED_RENEW_TEXT, button -> this.onRenew(this.getSelectedServer())).width(100).build();
        this.leaveButton = ButtonWidget.builder(LEAVE_TEXT, button -> this.leaveClicked(this.getSelectedServer())).width(100).build();
        this.purchaseButton = ButtonWidget.builder(Text.translatable("mco.selectServer.purchase"), button -> this.showBuyRealmsScreen()).size(100, 20).build();
        this.backButton = ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).width(100).build();
        if (RealmsClient.ENVIRONMENT == RealmsClient.Environment.STAGE) {
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Snapshot"), Text.literal("Release")).build(5, 5, 100, 20, Text.literal("Realm"), (button, snapshot) -> {
                showingSnapshotRealms = snapshot;
                this.availableSnapshotServers = List.of();
                this.resetPeriodicCheckers();
            }));
        }
        this.onLoadStatusChange(LoadStatus.LOADING);
        this.refreshButtons();
        this.availabilityInfo.thenAcceptAsync(availabilityInfo -> {
            Screen lv = availabilityInfo.createScreen(this.parent);
            if (lv == null) {
                this.periodicRunnersManager = this.createPeriodicRunnersManager(this.client.getRealmsPeriodicCheckers());
            } else {
                this.client.setScreen(lv);
            }
        }, this.executor);
    }

    public static boolean isSnapshotRealmsEligible() {
        return GAME_ON_SNAPSHOT && showingSnapshotRealms;
    }

    @Override
    protected void refreshWidgetPositions() {
        if (this.layout != null) {
            this.realmSelectionList.position(this.width, this.layout);
            this.layout.refreshPositions();
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private void updateLoadStatus() {
        if (this.serverFilterer.isEmpty() && this.availableSnapshotServers.isEmpty() && this.notifications.isEmpty()) {
            this.onLoadStatusChange(LoadStatus.NO_REALMS);
        } else {
            this.onLoadStatusChange(LoadStatus.LIST);
        }
    }

    private void onLoadStatusChange(LoadStatus loadStatus) {
        if (this.loadStatus == loadStatus) {
            return;
        }
        if (this.layout != null) {
            this.layout.forEachChild(child -> this.remove((Element)child));
        }
        this.layout = this.makeLayoutFor(loadStatus);
        this.loadStatus = loadStatus;
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.refreshWidgetPositions();
    }

    private ThreePartsLayoutWidget makeLayoutFor(LoadStatus loadStatus) {
        ThreePartsLayoutWidget lv = new ThreePartsLayoutWidget(this);
        lv.setHeaderHeight(44);
        lv.addHeader(this.makeHeader());
        LayoutWidget lv2 = this.makeInnerLayout(loadStatus);
        lv2.refreshPositions();
        lv.setFooterHeight(lv2.getHeight() + 22);
        lv.addFooter(lv2);
        switch (loadStatus.ordinal()) {
            case 0: {
                lv.addBody(new LoadingWidget(this.textRenderer, LOADING_TEXT));
                break;
            }
            case 1: {
                lv.addBody(this.makeNoRealmsLayout());
                break;
            }
            case 2: {
                lv.addBody(this.realmSelectionList);
            }
        }
        return lv;
    }

    private LayoutWidget makeHeader() {
        int i = 90;
        DirectionalLayoutWidget lv = DirectionalLayoutWidget.horizontal().spacing(4);
        lv.getMainPositioner().alignVerticalCenter();
        lv.add(this.inviteButton);
        lv.add(this.newsButton);
        DirectionalLayoutWidget lv2 = DirectionalLayoutWidget.horizontal();
        lv2.getMainPositioner().alignVerticalCenter();
        lv2.add(EmptyWidget.ofWidth(90));
        lv2.add(RealmsMainScreen.createRealmsLogoIconWidget(), Positioner::alignHorizontalCenter);
        lv2.add(new SimplePositioningWidget(90, 44)).add(lv, Positioner::alignRight);
        return lv2;
    }

    private LayoutWidget makeInnerLayout(LoadStatus loadStatus) {
        GridWidget lv = new GridWidget().setSpacing(4);
        GridWidget.Adder lv2 = lv.createAdder(3);
        if (loadStatus == LoadStatus.LIST) {
            lv2.add(this.playButton);
            lv2.add(this.configureButton);
            lv2.add(this.renewButton);
            lv2.add(this.leaveButton);
        }
        lv2.add(this.purchaseButton);
        lv2.add(this.backButton);
        return lv;
    }

    private DirectionalLayoutWidget makeNoRealmsLayout() {
        DirectionalLayoutWidget lv = DirectionalLayoutWidget.vertical().spacing(8);
        lv.getMainPositioner().alignHorizontalCenter();
        lv.add(IconWidget.create(130, 64, NO_REALMS_TEXTURE, 130, 64));
        NarratedMultilineTextWidget lv2 = new NarratedMultilineTextWidget(308, NO_REALMS_TEXT, this.textRenderer, false, NarratedMultilineTextWidget.BackgroundRendering.NEVER, 4);
        lv.add(lv2);
        return lv;
    }

    void refreshButtons() {
        RealmsServer lv = this.getSelectedServer();
        boolean bl = lv != null;
        this.purchaseButton.active = this.loadStatus != LoadStatus.LOADING;
        boolean bl2 = this.playButton.active = bl && lv.shouldAllowPlay();
        if (!this.playButton.active && bl && lv.state == RealmsServer.State.CLOSED) {
            this.playButton.setTooltip(Tooltip.of(RealmsServer.REALM_CLOSED_TEXT));
        }
        this.renewButton.active = bl && this.shouldRenewButtonBeActive(lv);
        this.leaveButton.active = bl && this.shouldLeaveButtonBeActive(lv);
        this.configureButton.active = bl && this.shouldConfigureButtonBeActive(lv);
    }

    private boolean shouldRenewButtonBeActive(RealmsServer server) {
        return server.expired && RealmsMainScreen.isSelfOwnedServer(server);
    }

    private boolean shouldConfigureButtonBeActive(RealmsServer server) {
        return RealmsMainScreen.isSelfOwnedServer(server) && server.state != RealmsServer.State.UNINITIALIZED;
    }

    private boolean shouldLeaveButtonBeActive(RealmsServer server) {
        return !RealmsMainScreen.isSelfOwnedServer(server);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.periodicRunnersManager != null) {
            this.periodicRunnersManager.runAll();
        }
    }

    public static void resetPendingInvitesCount() {
        MinecraftClient.getInstance().getRealmsPeriodicCheckers().pendingInvitesCount.reset();
    }

    public static void resetServerList() {
        MinecraftClient.getInstance().getRealmsPeriodicCheckers().serverList.reset();
    }

    private void resetPeriodicCheckers() {
        for (PeriodicRunnerFactory.PeriodicRunner<?> lv : this.client.getRealmsPeriodicCheckers().getCheckers()) {
            lv.reset();
        }
    }

    private PeriodicRunnerFactory.RunnersManager createPeriodicRunnersManager(RealmsPeriodicCheckers periodicCheckers) {
        PeriodicRunnerFactory.RunnersManager lv = periodicCheckers.runnerFactory.create();
        lv.add(periodicCheckers.serverList, availableServers -> {
            this.serverFilterer.filterAndSort(availableServers.serverList());
            this.availableSnapshotServers = availableServers.availableSnapshotServers();
            this.refresh();
            boolean bl = false;
            for (RealmsServer lv : this.serverFilterer) {
                if (!this.isOwnedNotExpired(lv)) continue;
                bl = true;
            }
            if (!regionsPinged && bl) {
                regionsPinged = true;
                this.pingRegions();
            }
        });
        RealmsMainScreen.request(RealmsClient::listNotifications, notifications -> {
            this.notifications.clear();
            this.notifications.addAll((Collection<RealmsNotification>)notifications);
            for (RealmsNotification lv : notifications) {
                RealmsNotification.InfoPopup lv2;
                PopupScreen lv3;
                if (!(lv instanceof RealmsNotification.InfoPopup) || (lv3 = (lv2 = (RealmsNotification.InfoPopup)lv).createScreen(this, this::dismissNotification)) == null) continue;
                this.client.setScreen(lv3);
                this.markAsSeen(List.of(lv));
                break;
            }
            if (!this.notifications.isEmpty() && this.loadStatus != LoadStatus.LOADING) {
                this.refresh();
            }
        });
        lv.add(periodicCheckers.pendingInvitesCount, pendingInvitesCount -> {
            this.inviteButton.setNotificationCount((int)pendingInvitesCount);
            this.inviteButton.setTooltip(pendingInvitesCount == 0 ? Tooltip.of(NO_PENDING_TOOLTIP) : Tooltip.of(PENDING_TOOLTIP));
            if (pendingInvitesCount > 0 && this.rateLimiter.tryAcquire(1)) {
                this.client.getNarratorManager().narrateSystemImmediately(Text.translatable("mco.configure.world.invite.narration", pendingInvitesCount));
            }
        });
        lv.add(periodicCheckers.trialAvailability, trialAvailable -> {
            this.trialAvailable = trialAvailable;
        });
        lv.add(periodicCheckers.onlinePlayers, onlinePlayers -> {
            this.onlinePlayers = onlinePlayers;
        });
        lv.add(periodicCheckers.news, news -> {
            arg.newsUpdater.updateNews((RealmsNews)news);
            this.newsLink = arg.newsUpdater.getNewsLink();
            this.newsButton.setNotificationCount(arg.newsUpdater.hasUnreadNews() ? Integer.MAX_VALUE : 0);
        });
        return lv;
    }

    void markAsSeen(Collection<RealmsNotification> notifications) {
        ArrayList<UUID> list = new ArrayList<UUID>(notifications.size());
        for (RealmsNotification lv : notifications) {
            if (lv.isSeen() || this.seenNotifications.contains(lv.getUuid())) continue;
            list.add(lv.getUuid());
        }
        if (!list.isEmpty()) {
            RealmsMainScreen.request(client -> {
                client.markNotificationsAsSeen(list);
                return null;
            }, result -> this.seenNotifications.addAll(list));
        }
    }

    private static <T> void request(Request<T> request, Consumer<T> resultConsumer) {
        MinecraftClient lv = MinecraftClient.getInstance();
        ((CompletableFuture)CompletableFuture.supplyAsync(() -> {
            try {
                return request.request(RealmsClient.createRealmsClient(lv));
            } catch (RealmsServiceException lv) {
                throw new RuntimeException(lv);
            }
        }).thenAcceptAsync(resultConsumer, (Executor)lv)).exceptionally(throwable -> {
            LOGGER.error("Failed to execute call to Realms Service", (Throwable)throwable);
            return null;
        });
    }

    private void refresh() {
        this.realmSelectionList.refresh(this);
        this.updateLoadStatus();
        this.refreshButtons();
    }

    private void pingRegions() {
        new Thread(() -> {
            List<RegionPingResult> list = Ping.pingAllRegions();
            RealmsClient lv = RealmsClient.create();
            PingResult lv2 = new PingResult();
            lv2.pingResults = list;
            lv2.worldIds = this.getOwnedNonExpiredWorldIds();
            try {
                lv.sendPingResults(lv2);
            } catch (Throwable throwable) {
                LOGGER.warn("Could not send ping result to Realms: ", throwable);
            }
        }).start();
    }

    private List<Long> getOwnedNonExpiredWorldIds() {
        ArrayList<Long> list = Lists.newArrayList();
        for (RealmsServer lv : this.serverFilterer) {
            if (!this.isOwnedNotExpired(lv)) continue;
            list.add(lv.id);
        }
        return list;
    }

    private void onRenew(@Nullable RealmsServer realmsServer) {
        if (realmsServer != null) {
            String string = Urls.getExtendJavaRealmsUrl(realmsServer.remoteSubscriptionId, this.client.getSession().getUuidOrNull(), realmsServer.expiredTrial);
            this.client.setScreen(new ConfirmLinkScreen(bl -> {
                if (bl) {
                    Util.getOperatingSystem().open(string);
                } else {
                    this.client.setScreen(this);
                }
            }, string, true));
        }
    }

    private void configureClicked(@Nullable RealmsServer serverData) {
        if (serverData != null && this.client.uuidEquals(serverData.ownerUUID)) {
            this.client.setScreen(new RealmsConfigureWorldScreen(this, serverData.id));
        }
    }

    private void leaveClicked(@Nullable RealmsServer selectedServer) {
        if (selectedServer != null && !this.client.uuidEquals(selectedServer.ownerUUID)) {
            MutableText lv = Text.translatable("mco.configure.world.leave.question.line1");
            this.client.setScreen(RealmsPopups.createInfoPopup(this, lv, popup -> this.leaveServer(selectedServer)));
        }
    }

    @Nullable
    private RealmsServer getSelectedServer() {
        Object e = this.realmSelectionList.getSelectedOrNull();
        if (e instanceof RealmSelectionListEntry) {
            RealmSelectionListEntry lv = (RealmSelectionListEntry)e;
            return lv.getRealmsServer();
        }
        return null;
    }

    private void leaveServer(final RealmsServer server) {
        new Thread("Realms-leave-server"){

            @Override
            public void run() {
                try {
                    RealmsClient lv = RealmsClient.create();
                    lv.uninviteMyselfFrom(server.id);
                    RealmsMainScreen.this.client.execute(RealmsMainScreen::resetServerList);
                } catch (RealmsServiceException lv2) {
                    LOGGER.error("Couldn't configure world", lv2);
                    RealmsMainScreen.this.client.execute(() -> RealmsMainScreen.this.client.setScreen(new RealmsGenericErrorScreen(lv2, (Screen)RealmsMainScreen.this)));
                }
            }
        }.start();
        this.client.setScreen(this);
    }

    void dismissNotification(UUID notification) {
        RealmsMainScreen.request(client -> {
            client.dismissNotifications(List.of(notification));
            return null;
        }, void_ -> {
            this.notifications.removeIf(notificationId -> notificationId.isDismissable() && notification.equals(notificationId.getUuid()));
            this.refresh();
        });
    }

    public void removeSelection() {
        this.realmSelectionList.setSelected((Entry)null);
        RealmsMainScreen.resetServerList();
    }

    @Override
    public Text getNarratedTitle() {
        return switch (this.loadStatus.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> ScreenTexts.joinSentences(super.getNarratedTitle(), LOADING_TEXT);
            case 1 -> ScreenTexts.joinSentences(super.getNarratedTitle(), NO_REALMS_TEXT);
            case 2 -> super.getNarratedTitle();
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        if (RealmsMainScreen.isSnapshotRealmsEligible()) {
            context.drawTextWithShadow(this.textRenderer, "Minecraft " + SharedConstants.getGameVersion().name(), 2, this.height - 10, Colors.WHITE);
        }
        if (this.trialAvailable && this.purchaseButton.active) {
            BuyRealmsScreen.drawTrialAvailableTexture(context, this.purchaseButton);
        }
        switch (RealmsClient.ENVIRONMENT) {
            case STAGE: {
                this.drawEnvironmentText(context, "STAGE!", -256);
                break;
            }
            case LOCAL: {
                this.drawEnvironmentText(context, "LOCAL!", -8388737);
            }
        }
    }

    private void showBuyRealmsScreen() {
        this.client.setScreen(new BuyRealmsScreen(this, this.trialAvailable));
    }

    public static void play(@Nullable RealmsServer serverData, Screen parent) {
        RealmsMainScreen.play(serverData, parent, false);
    }

    public static void play(@Nullable RealmsServer server, Screen parent, boolean needsPreparation) {
        if (server != null) {
            if (!RealmsMainScreen.isSnapshotRealmsEligible() || needsPreparation || server.isMinigame()) {
                MinecraftClient.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(parent, new RealmsPrepareConnectionTask(parent, server)));
                return;
            }
            switch (server.compatibility) {
                case COMPATIBLE: {
                    MinecraftClient.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(parent, new RealmsPrepareConnectionTask(parent, server)));
                    break;
                }
                case UNVERIFIABLE: {
                    RealmsMainScreen.showCompatibilityScreen(server, parent, Text.translatable("mco.compatibility.unverifiable.title").withColor(Colors.LIGHT_YELLOW), Text.translatable("mco.compatibility.unverifiable.message"), ScreenTexts.CONTINUE);
                    break;
                }
                case NEEDS_DOWNGRADE: {
                    RealmsMainScreen.showCompatibilityScreen(server, parent, Text.translatable("selectWorld.backupQuestion.downgrade").withColor(Colors.LIGHT_RED), Text.translatable("mco.compatibility.downgrade.description", Text.literal(server.activeVersion).withColor(Colors.LIGHT_YELLOW), Text.literal(SharedConstants.getGameVersion().name()).withColor(Colors.LIGHT_YELLOW)), Text.translatable("mco.compatibility.downgrade"));
                    break;
                }
                case NEEDS_UPGRADE: {
                    RealmsMainScreen.showNeedsUpgradeScreen(server, parent);
                    break;
                }
                case INCOMPATIBLE: {
                    MinecraftClient.getInstance().setScreen(new PopupScreen.Builder(parent, INCOMPATIBLE_POPUP_TITLE).message(Text.translatable("mco.compatibility.incompatible.series.popup.message", Text.literal(server.activeVersion).withColor(Colors.LIGHT_YELLOW), Text.literal(SharedConstants.getGameVersion().name()).withColor(Colors.LIGHT_YELLOW))).button(ScreenTexts.BACK, PopupScreen::close).build());
                    break;
                }
                case RELEASE_TYPE_INCOMPATIBLE: {
                    MinecraftClient.getInstance().setScreen(new PopupScreen.Builder(parent, INCOMPATIBLE_POPUP_TITLE).message(INCOMPATIBLE_RELEASE_TYPE_MESSAGE).button(ScreenTexts.BACK, PopupScreen::close).build());
                }
            }
        }
    }

    private static void showCompatibilityScreen(RealmsServer server, Screen parent, Text title, Text description, Text confirmText) {
        MinecraftClient.getInstance().setScreen(new PopupScreen.Builder(parent, title).message(description).button(confirmText, popup -> {
            MinecraftClient.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(parent, new RealmsPrepareConnectionTask(parent, server)));
            RealmsMainScreen.resetServerList();
        }).button(ScreenTexts.CANCEL, PopupScreen::close).build());
    }

    private static void showNeedsUpgradeScreen(RealmsServer serverData, Screen parent) {
        MutableText lv = Text.translatable("mco.compatibility.upgrade.title").withColor(Colors.LIGHT_YELLOW);
        MutableText lv2 = Text.translatable("mco.compatibility.upgrade");
        MutableText lv3 = Text.literal(serverData.activeVersion).withColor(Colors.LIGHT_YELLOW);
        MutableText lv4 = Text.literal(SharedConstants.getGameVersion().name()).withColor(Colors.LIGHT_YELLOW);
        MutableText lv5 = RealmsMainScreen.isSelfOwnedServer(serverData) ? Text.translatable("mco.compatibility.upgrade.description", lv3, lv4) : Text.translatable("mco.compatibility.upgrade.friend.description", lv3, lv4);
        RealmsMainScreen.showCompatibilityScreen(serverData, parent, lv, lv5, lv2);
    }

    public static Text getVersionText(String version, boolean compatible) {
        return RealmsMainScreen.getVersionText(version, compatible ? -8355712 : -2142128);
    }

    public static Text getVersionText(String version, int color) {
        if (StringUtils.isBlank(version)) {
            return ScreenTexts.EMPTY;
        }
        return Text.literal(version).withColor(color);
    }

    public static Text getGameModeText(int id, boolean hardcore) {
        if (hardcore) {
            return Text.translatable("gameMode.hardcore").withColor(Colors.RED);
        }
        return GameMode.byIndex(id).getTranslatableName();
    }

    static boolean isSelfOwnedServer(RealmsServer server) {
        return MinecraftClient.getInstance().uuidEquals(server.ownerUUID);
    }

    private boolean isOwnedNotExpired(RealmsServer serverData) {
        return RealmsMainScreen.isSelfOwnedServer(serverData) && !serverData.expired;
    }

    private void drawEnvironmentText(DrawContext context, String text, int color) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(this.width / 2 - 25, 20.0f);
        context.getMatrices().rotate(-0.34906584f);
        context.getMatrices().scale(1.5f, 1.5f);
        context.drawTextWithShadow(this.textRenderer, text, 0, 0, color);
        context.getMatrices().popMatrix();
    }

    static {
        showingSnapshotRealms = GAME_ON_SNAPSHOT = !SharedConstants.getGameVersion().stable();
    }

    @Environment(value=EnvType.CLIENT)
    class RealmSelectionList
    extends AlwaysSelectedEntryListWidget<Entry> {
        public RealmSelectionList() {
            super(MinecraftClient.getInstance(), RealmsMainScreen.this.width, RealmsMainScreen.this.height, 0, 36);
        }

        @Override
        public void setSelected(@Nullable Entry arg) {
            super.setSelected(arg);
            RealmsMainScreen.this.refreshButtons();
        }

        @Override
        public int getRowWidth() {
            return 300;
        }

        void refresh(RealmsMainScreen mainScreen) {
            Entry lv = (Entry)this.getSelectedOrNull();
            this.clearEntries();
            for (RealmsNotification lv2 : RealmsMainScreen.this.notifications) {
                if (!(lv2 instanceof RealmsNotification.VisitUrl)) continue;
                RealmsNotification.VisitUrl lv3 = (RealmsNotification.VisitUrl)lv2;
                this.addVisitEntries(lv3, mainScreen, lv);
                RealmsMainScreen.this.markAsSeen(List.of(lv2));
                break;
            }
            this.addServerEntries(lv);
        }

        private void addVisitEntries(RealmsNotification.VisitUrl url, RealmsMainScreen mainScreen, @Nullable Entry selectedEntry) {
            VisitUrlNotification lv3;
            Text lv = url.getDefaultMessage();
            int i = RealmsMainScreen.this.textRenderer.getWrappedLinesHeight(lv, VisitUrlNotification.getTextWidth(this.getRowWidth()));
            VisitUrlNotification lv2 = new VisitUrlNotification(mainScreen, i, lv, url);
            this.addEntry(lv2, 38 + i);
            if (selectedEntry instanceof VisitUrlNotification && (lv3 = (VisitUrlNotification)selectedEntry).getMessage().equals(lv)) {
                this.setSelected(lv2);
            }
        }

        private void addServerEntries(@Nullable Entry selectedEntry) {
            for (RealmsServer lv : RealmsMainScreen.this.availableSnapshotServers) {
                this.addEntry(new SnapshotEntry(lv));
            }
            for (RealmsServer lv : RealmsMainScreen.this.serverFilterer) {
                Entry lv2;
                if (RealmsMainScreen.isSnapshotRealmsEligible() && !lv.isPrerelease()) {
                    if (lv.state == RealmsServer.State.UNINITIALIZED) continue;
                    lv2 = new ParentRealmSelectionListEntry(RealmsMainScreen.this, lv);
                } else {
                    lv2 = new RealmSelectionListEntry(lv);
                }
                this.addEntry(lv2);
                if (!(selectedEntry instanceof RealmSelectionListEntry)) continue;
                RealmSelectionListEntry lv3 = (RealmSelectionListEntry)selectedEntry;
                if (lv3.server.id != lv.id) continue;
                this.setSelected(lv2);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class NotificationButtonWidget
    extends TextIconButtonWidget.IconOnly {
        private static final Identifier[] TEXTURES = new Identifier[]{Identifier.ofVanilla("notification/1"), Identifier.ofVanilla("notification/2"), Identifier.ofVanilla("notification/3"), Identifier.ofVanilla("notification/4"), Identifier.ofVanilla("notification/5"), Identifier.ofVanilla("notification/more")};
        private static final int field_45228 = Integer.MAX_VALUE;
        private static final int SIZE = 20;
        private static final int TEXTURE_SIZE = 14;
        private int notificationCount;

        public NotificationButtonWidget(Text message, Identifier texture, ButtonWidget.PressAction onPress, @Nullable Text tooltip) {
            super(20, 20, message, 14, 14, new ButtonTextures(texture), onPress, tooltip, null);
        }

        int getNotificationCount() {
            return this.notificationCount;
        }

        public void setNotificationCount(int notificationCount) {
            this.notificationCount = notificationCount;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            super.renderWidget(context, mouseX, mouseY, deltaTicks);
            if (this.active && this.notificationCount != 0) {
                this.render(context);
            }
        }

        private void render(DrawContext context) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURES[Math.min(this.notificationCount, 6) - 1], this.getX() + this.getWidth() - 5, this.getY() - 3, 8, 8);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum LoadStatus {
        LOADING,
        NO_REALMS,
        LIST;

    }

    @Environment(value=EnvType.CLIENT)
    static interface Request<T> {
        public T request(RealmsClient var1) throws RealmsServiceException;
    }

    @Environment(value=EnvType.CLIENT)
    class RealmSelectionListEntry
    extends Entry {
        private static final Text ONLINE_PLAYERS_TEXT = Text.translatable("mco.onlinePlayers");
        private static final int field_52120 = 9;
        private static final int field_62084 = 3;
        private static final int field_32054 = 36;
        final RealmsServer server;
        private final TooltipState tooltip;

        public RealmSelectionListEntry(RealmsServer server) {
            this.tooltip = new TooltipState();
            this.server = server;
            boolean bl = RealmsMainScreen.isSelfOwnedServer(server);
            if (RealmsMainScreen.isSnapshotRealmsEligible() && bl && server.isPrerelease()) {
                this.tooltip.setTooltip(Tooltip.of(Text.translatable("mco.snapshot.paired", server.parentWorldName)));
            } else if (!bl && server.needsDowngrade()) {
                this.tooltip.setTooltip(Tooltip.of(Text.translatable("mco.snapshot.friendsRealm.downgrade", server.activeVersion)));
            }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            if (this.server.state == RealmsServer.State.UNINITIALIZED) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, NEW_REALM_ICON_TEXTURE, this.getContentX() - 5, this.getContentMiddleY() - 10, 40, 20);
                int k = this.getContentMiddleY() - ((RealmsMainScreen)RealmsMainScreen.this).textRenderer.fontHeight / 2;
                context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, UNINITIALIZED_TEXT, this.getContentX() + 40 - 2, k, RealmsScreen.GREEN);
                return;
            }
            RealmsUtil.drawPlayerHead(context, this.getContentX(), this.getContentY(), 32, this.server.ownerUUID);
            this.drawServerNameAndVersion(context, this.getContentY(), this.getContentX(), this.getContentWidth(), -1, this.server);
            this.drawDescription(context, this.getContentY(), this.getContentX(), this.getContentWidth(), this.server);
            this.drawOwnerOrExpiredText(context, this.getContentY(), this.getContentX(), this.server);
            this.renderStatusIcon(this.server, context, this.getContentRightEnd(), this.getContentY(), mouseX, mouseY);
            boolean bl2 = this.drawPlayers(context, this.getContentY(), this.getContentX(), this.getContentWidth(), this.getContentHeight(), mouseX, mouseY, deltaTicks);
            if (!bl2) {
                this.tooltip.render(context, mouseX, mouseY, hovered, this.isFocused(), new ScreenRect(this.getContentX(), this.getContentY(), this.getContentWidth(), this.getContentHeight()));
            }
        }

        private boolean drawPlayers(DrawContext arg, int top, int left, int width, int height, int mouseX, int mouseY, float tickProgress) {
            List<ProfileComponent> list = RealmsMainScreen.this.onlinePlayers.get(this.server.id);
            int o = list.size();
            if (o > 0) {
                int p = left + width - 21;
                int q = top + height - 9 - 2;
                int r = 9 * o + 3 * (o - 1);
                int s = p - r;
                ArrayList<PlayerSkinCache.Entry> list2 = mouseX >= s && mouseX <= p && mouseY >= q && mouseY <= q + 9 ? new ArrayList<PlayerSkinCache.Entry>(o) : null;
                PlayerSkinCache lv = RealmsMainScreen.this.client.getPlayerSkinCache();
                for (int t = 0; t < list.size(); ++t) {
                    ProfileComponent lv2 = list.get(t);
                    PlayerSkinCache.Entry lv3 = lv.get(lv2);
                    int u = s + 12 * t;
                    PlayerSkinDrawer.draw(arg, lv3.getTextures(), u, q, 9);
                    if (list2 == null) continue;
                    list2.add(lv3);
                }
                if (list2 != null) {
                    arg.drawTooltip(RealmsMainScreen.this.textRenderer, List.of(ONLINE_PLAYERS_TEXT), Optional.of(new ProfilesTooltipComponent.ProfilesData(list2)), mouseX, mouseY);
                    return true;
                }
            }
            return false;
        }

        private void play() {
            RealmsMainScreen.this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            RealmsMainScreen.play(this.server, RealmsMainScreen.this);
        }

        private void createRealm() {
            RealmsMainScreen.this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            RealmsCreateRealmScreen lv = new RealmsCreateRealmScreen(RealmsMainScreen.this, this.server, this.server.isPrerelease());
            RealmsMainScreen.this.client.setScreen(lv);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            if (this.server.state == RealmsServer.State.UNINITIALIZED) {
                this.createRealm();
            } else if (this.server.shouldAllowPlay() && doubled && this.isFocused()) {
                this.play();
            }
            return true;
        }

        @Override
        public boolean keyPressed(KeyInput input) {
            if (input.isEnterOrSpace()) {
                if (this.server.state == RealmsServer.State.UNINITIALIZED) {
                    this.createRealm();
                    return true;
                }
                if (this.server.shouldAllowPlay()) {
                    this.play();
                    return true;
                }
            }
            return super.keyPressed(input);
        }

        @Override
        public Text getNarration() {
            if (this.server.state == RealmsServer.State.UNINITIALIZED) {
                return UNINITIALIZED_BUTTON_NARRATION;
            }
            return Text.translatable("narrator.select", Objects.requireNonNullElse(this.server.name, "unknown server"));
        }

        public RealmsServer getRealmsServer() {
            return this.server;
        }
    }

    @Environment(value=EnvType.CLIENT)
    abstract class Entry
    extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        protected static final int field_46680 = 10;
        private static final int field_46681 = 28;
        protected static final int field_52117 = 7;
        protected static final int field_52118 = 2;

        Entry() {
        }

        protected void renderStatusIcon(RealmsServer server, DrawContext context, int x, int y, int mouseX, int mouseY) {
            int m = x - 10 - 7;
            int n = y + 2;
            if (server.expired) {
                this.drawTextureWithTooltip(context, m, n, mouseX, mouseY, EXPIRED_STATUS_TEXTURE, () -> EXPIRED_TEXT);
            } else if (server.state == RealmsServer.State.CLOSED) {
                this.drawTextureWithTooltip(context, m, n, mouseX, mouseY, CLOSED_STATUS_TEXTURE, () -> CLOSED_TEXT);
            } else if (RealmsMainScreen.isSelfOwnedServer(server) && server.daysLeft < 7) {
                this.drawTextureWithTooltip(context, m, n, mouseX, mouseY, EXPIRES_SOON_STATUS_TEXTURE, () -> {
                    if (arg.daysLeft <= 0) {
                        return EXPIRES_SOON_TEXT;
                    }
                    if (arg.daysLeft == 1) {
                        return EXPIRES_IN_A_DAY_TEXT;
                    }
                    return Text.translatable("mco.selectServer.expires.days", arg.daysLeft);
                });
            } else if (server.state == RealmsServer.State.OPEN) {
                this.drawTextureWithTooltip(context, m, n, mouseX, mouseY, OPEN_STATUS_TEXTURE, () -> OPEN_TEXT);
            }
        }

        private void drawTextureWithTooltip(DrawContext context, int x, int y, int mouseX, int mouseY, Identifier texture, Supplier<Text> tooltip) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 10, 28);
            if (RealmsMainScreen.this.realmSelectionList.isMouseOver(mouseX, mouseY) && mouseX >= x && mouseX <= x + 10 && mouseY >= y && mouseY <= y + 28) {
                context.drawTooltip(tooltip.get(), mouseX, mouseY);
            }
        }

        protected void drawServerNameAndVersion(DrawContext context, int y, int x, int width, int color, RealmsServer server) {
            int m = this.getNameX(x);
            int n = this.getNameY(y);
            Text lv = RealmsMainScreen.getVersionText(server.activeVersion, server.isCompatible());
            int o = this.getVersionRight(x, width, lv);
            this.drawTrimmedText(context, server.getName(), m, n, o, color);
            if (lv != ScreenTexts.EMPTY && !server.isMinigame()) {
                context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, lv, o, n, Colors.GRAY);
            }
        }

        protected void drawDescription(DrawContext context, int y, int x, int width, RealmsServer server) {
            int l = this.getNameX(x);
            int m = this.getNameY(y);
            int n = this.getDescriptionY(m);
            String string = server.getMinigameName();
            boolean bl = server.isMinigame();
            if (bl && string != null) {
                MutableText lv = Text.literal(string).formatted(Formatting.GRAY);
                context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, Text.translatable("mco.selectServer.minigameName", lv).withColor(Colors.LIGHT_YELLOW), l, n, Colors.WHITE);
            } else {
                int o = this.drawGameMode(server, context, x, width, m);
                this.drawTrimmedText(context, server.getDescription(), l, this.getDescriptionY(m), o, -8355712);
            }
        }

        protected void drawOwnerOrExpiredText(DrawContext context, int y, int x, RealmsServer server) {
            int k = this.getNameX(x);
            int l = this.getNameY(y);
            int m = this.getStatusY(l);
            if (!RealmsMainScreen.isSelfOwnedServer(server)) {
                context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, server.owner, k, this.getStatusY(l), Colors.GRAY);
            } else if (server.expired) {
                Text lv = server.expiredTrial ? EXPIRED_TRIAL_TEXT : EXPIRED_LIST_TEXT;
                context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, lv, k, m, Colors.LIGHT_RED);
            }
        }

        protected void drawTrimmedText(DrawContext context, @Nullable String string, int left, int y, int right, int color) {
            if (string == null) {
                return;
            }
            int m = right - left;
            if (RealmsMainScreen.this.textRenderer.getWidth(string) > m) {
                String string2 = RealmsMainScreen.this.textRenderer.trimToWidth(string, m - RealmsMainScreen.this.textRenderer.getWidth("... "));
                context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, string2 + "...", left, y, color);
            } else {
                context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, string, left, y, color);
            }
        }

        protected int getVersionRight(int x, int width, Text version) {
            return x + width - RealmsMainScreen.this.textRenderer.getWidth(version) - 20;
        }

        protected int getGameModeRight(int x, int width, Text gameMode) {
            return x + width - RealmsMainScreen.this.textRenderer.getWidth(gameMode) - 20;
        }

        protected int drawGameMode(RealmsServer server, DrawContext context, int x, int entryWidth, int y) {
            boolean bl = server.hardcore;
            int l = server.gameMode;
            int m = x;
            if (GameMode.isValid(l)) {
                Text lv = RealmsMainScreen.getGameModeText(l, bl);
                m = this.getGameModeRight(x, entryWidth, lv);
                context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, lv, m, this.getDescriptionY(y), Colors.GRAY);
            }
            if (bl) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HARDCORE_ICON_TEXTURE, m -= 10, this.getDescriptionY(y), 8, 8);
            }
            return m;
        }

        protected int getNameY(int y) {
            return y + 1;
        }

        protected int getTextHeight() {
            return 2 + ((RealmsMainScreen)RealmsMainScreen.this).textRenderer.fontHeight;
        }

        protected int getNameX(int x) {
            return x + 36 + 2;
        }

        protected int getDescriptionY(int y) {
            return y + this.getTextHeight();
        }

        protected int getStatusY(int y) {
            return y + this.getTextHeight() * 2;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CrossButton
    extends TexturedButtonWidget {
        private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/cross_button"), Identifier.ofVanilla("widget/cross_button_highlighted"));

        protected CrossButton(ButtonWidget.PressAction onPress, Text tooltip) {
            super(0, 0, 14, 14, TEXTURES, onPress);
            this.setTooltip(Tooltip.of(tooltip));
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ParentRealmSelectionListEntry
    extends Entry {
        private final RealmsServer server;
        private final TooltipState tooltip = new TooltipState();

        public ParentRealmSelectionListEntry(RealmsMainScreen arg, RealmsServer server) {
            this.server = server;
            if (!server.expired) {
                this.tooltip.setTooltip(Tooltip.of(Text.translatable("mco.snapshot.parent.tooltip")));
            }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            this.renderStatusIcon(this.server, context, this.getContentRightEnd(), this.getContentY(), mouseX, mouseY);
            RealmsUtil.drawPlayerHead(context, this.getContentX(), this.getContentY(), 32, this.server.ownerUUID);
            this.drawServerNameAndVersion(context, this.getContentY(), this.getContentX(), this.getContentWidth(), -8355712, this.server);
            this.drawDescription(context, this.getContentY(), this.getContentX(), this.getContentWidth(), this.server);
            this.drawOwnerOrExpiredText(context, this.getContentY(), this.getContentX(), this.server);
            this.tooltip.render(context, mouseX, mouseY, hovered, this.isFocused(), new ScreenRect(this.getContentX(), this.getContentY(), this.getContentWidth(), this.getContentHeight()));
        }

        @Override
        public Text getNarration() {
            return Text.literal(Objects.requireNonNullElse(this.server.name, "unknown server"));
        }
    }

    @Environment(value=EnvType.CLIENT)
    class SnapshotEntry
    extends Entry {
        private static final Text START_TEXT = Text.translatable("mco.snapshot.start");
        private static final int field_46677 = 5;
        private final TooltipState tooltip = new TooltipState();
        private final RealmsServer server;

        public SnapshotEntry(RealmsServer server) {
            this.server = server;
            this.tooltip.setTooltip(Tooltip.of(Text.translatable("mco.snapshot.tooltip")));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, NEW_REALM_ICON_TEXTURE, this.getContentX() - 5, this.getContentMiddleY() - 10, 40, 20);
            int k = this.getContentMiddleY() - ((RealmsMainScreen)RealmsMainScreen.this).textRenderer.fontHeight / 2;
            context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, START_TEXT, this.getContentX() + 40 - 2, k - 5, RealmsScreen.GREEN);
            context.drawTextWithShadow(RealmsMainScreen.this.textRenderer, Text.translatable("mco.snapshot.description", Objects.requireNonNullElse(this.server.name, "unknown server")), this.getContentX() + 40 - 2, k + 5, Colors.GRAY);
            this.tooltip.render(context, mouseX, mouseY, hovered, this.isFocused(), new ScreenRect(this.getContentX(), this.getContentY(), this.getContentWidth(), this.getContentHeight()));
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            this.showPopup();
            return true;
        }

        @Override
        public boolean keyPressed(KeyInput input) {
            if (input.isEnterOrSpace()) {
                this.showPopup();
                return false;
            }
            return super.keyPressed(input);
        }

        private void showPopup() {
            RealmsMainScreen.this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            RealmsMainScreen.this.client.setScreen(new PopupScreen.Builder(RealmsMainScreen.this, Text.translatable("mco.snapshot.createSnapshotPopup.title")).message(Text.translatable("mco.snapshot.createSnapshotPopup.text")).button(Text.translatable("mco.selectServer.create"), screen -> RealmsMainScreen.this.client.setScreen(new RealmsCreateRealmScreen(RealmsMainScreen.this, this.server, true))).button(ScreenTexts.CANCEL, PopupScreen::close).build());
        }

        @Override
        public Text getNarration() {
            return Text.translatable("gui.narrate.button", ScreenTexts.joinSentences(START_TEXT, Text.translatable("mco.snapshot.description", Objects.requireNonNullElse(this.server.name, "unknown server"))));
        }
    }

    @Environment(value=EnvType.CLIENT)
    class VisitUrlNotification
    extends Entry {
        private static final int field_43002 = 40;
        public static final int field_62554 = 7;
        public static final int field_62082 = 38;
        private final Text message;
        private final List<ClickableWidget> gridChildren = new ArrayList<ClickableWidget>();
        @Nullable
        private final CrossButton dismissButton;
        private final MultilineTextWidget textWidget;
        private final GridWidget grid;
        private final SimplePositioningWidget textGrid;
        private final ButtonWidget urlButton;
        private int width = -1;

        public VisitUrlNotification(RealmsMainScreen parent, int lines, Text message, RealmsNotification.VisitUrl url) {
            this.message = message;
            this.grid = new GridWidget();
            this.grid.add(IconWidget.create(20, 20, INFO_ICON_TEXTURE), 0, 0, this.grid.copyPositioner().margin(7, 7, 0, 0));
            this.grid.add(EmptyWidget.ofWidth(40), 0, 0);
            this.textGrid = this.grid.add(new SimplePositioningWidget(0, lines), 0, 1, this.grid.copyPositioner().marginTop(7));
            this.textWidget = this.textGrid.add(new MultilineTextWidget(message, RealmsMainScreen.this.textRenderer).setCentered(true), this.textGrid.copyPositioner().alignHorizontalCenter().alignTop());
            this.grid.add(EmptyWidget.ofWidth(40), 0, 2);
            this.dismissButton = url.isDismissable() ? this.grid.add(new CrossButton(button -> RealmsMainScreen.this.dismissNotification(url.getUuid()), Text.translatable("mco.notification.dismiss")), 0, 2, this.grid.copyPositioner().alignRight().margin(0, 7, 7, 0)) : null;
            this.urlButton = this.grid.add(url.createButton(parent), 1, 1, this.grid.copyPositioner().alignHorizontalCenter().margin(4));
            this.grid.forEachChild(this.gridChildren::add);
        }

        @Override
        public boolean keyPressed(KeyInput input) {
            if (this.dismissButton != null && this.dismissButton.keyPressed(input)) {
                return true;
            }
            if (this.urlButton.keyPressed(input)) {
                return true;
            }
            return super.keyPressed(input);
        }

        private void setWidth() {
            int i = this.getContentWidth();
            if (this.width != i) {
                this.updateWidth(i);
                this.width = i;
            }
        }

        private void updateWidth(int width) {
            int j = VisitUrlNotification.getTextWidth(width);
            this.textGrid.setMinWidth(j);
            this.textWidget.setMaxWidth(j);
            this.grid.refreshPositions();
        }

        public static int getTextWidth(int width) {
            return width - 80;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            this.grid.setPosition(this.getContentX(), this.getContentY());
            this.setWidth();
            this.gridChildren.forEach(child -> child.render(context, mouseX, mouseY, deltaTicks));
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            if (this.dismissButton != null && this.dismissButton.mouseClicked(click, doubled)) {
                return true;
            }
            if (this.urlButton.mouseClicked(click, doubled)) {
                return true;
            }
            return super.mouseClicked(click, doubled);
        }

        public Text getMessage() {
            return this.message;
        }

        @Override
        public Text getNarration() {
            return this.getMessage();
        }
    }
}

