/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsPeriodicCheckers;
import net.minecraft.client.realms.dto.PendingInvite;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsPendingInvitesScreen
extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Text NO_PENDING_TEXT = Text.translatable("mco.invites.nopending");
    private final Screen parent;
    private final CompletableFuture<List<PendingInvite>> pendingInvites = CompletableFuture.supplyAsync(() -> {
        try {
            return RealmsClient.create().pendingInvites().pendingInvites;
        } catch (RealmsServiceException lv) {
            LOGGER.error("Couldn't list invites", lv);
            return List.of();
        }
    }, Util.getIoWorkerExecutor());
    final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    @Nullable
    PendingInvitationSelectionList pendingInvitationSelectionList;

    public RealmsPendingInvitesScreen(Screen parent, Text title) {
        super(title);
        this.parent = parent;
    }

    @Override
    public void init() {
        RealmsMainScreen.resetPendingInvitesCount();
        this.layout.addHeader(this.title, this.textRenderer);
        this.pendingInvitationSelectionList = this.layout.addBody(new PendingInvitationSelectionList(this, this.client));
        this.pendingInvites.thenAcceptAsync(pendingInvites -> {
            List<PendingInvitationSelectionListEntry> list2 = pendingInvites.stream().map(invite -> new PendingInvitationSelectionListEntry((PendingInvite)invite)).toList();
            this.pendingInvitationSelectionList.replaceEntries(list2);
            if (list2.isEmpty()) {
                this.client.getNarratorManager().narrateSystemMessage(NO_PENDING_TEXT);
            }
        }, this.executor);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(200).build());
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        if (this.pendingInvitationSelectionList != null) {
            this.pendingInvitationSelectionList.position(this.width, this.layout);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        if (this.pendingInvites.isDone() && this.pendingInvitationSelectionList.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, NO_PENDING_TEXT, this.width / 2, this.height / 2 - 20, Colors.WHITE);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class PendingInvitationSelectionList
    extends ElementListWidget<PendingInvitationSelectionListEntry> {
        public static final int field_62098 = 36;

        public PendingInvitationSelectionList(RealmsPendingInvitesScreen arg, MinecraftClient client) {
            super(client, arg.width, arg.layout.getContentHeight(), arg.layout.getHeaderHeight(), 36);
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        public boolean isEmpty() {
            return this.getEntryCount() == 0;
        }

        public void remove(PendingInvitationSelectionListEntry invitation) {
            this.removeEntry(invitation);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class PendingInvitationSelectionListEntry
    extends ElementListWidget.Entry<PendingInvitationSelectionListEntry> {
        private static final Text ACCEPT_TEXT = Text.translatable("mco.invites.button.accept");
        private static final Text REJECT_TEXT = Text.translatable("mco.invites.button.reject");
        private static final ButtonTextures ACCEPT_TEXTURE = new ButtonTextures(Identifier.ofVanilla("pending_invite/accept"), Identifier.ofVanilla("pending_invite/accept_highlighted"));
        private static final ButtonTextures REJECT_TEXTURE = new ButtonTextures(Identifier.ofVanilla("pending_invite/reject"), Identifier.ofVanilla("pending_invite/reject_highlighted"));
        private static final int field_62090 = 18;
        private static final int field_62091 = 21;
        private static final int field_32123 = 38;
        private final PendingInvite pendingInvite;
        private final List<ClickableWidget> buttons = new ArrayList<ClickableWidget>();
        private final TextIconButtonWidget acceptButton;
        private final TextIconButtonWidget rejectButton;
        private final TextWidget worldNameText;
        private final TextWidget worldOwnerNameText;
        private final TextWidget dateText;

        PendingInvitationSelectionListEntry(PendingInvite pendingInvite) {
            this.pendingInvite = pendingInvite;
            int i = RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.getRowWidth() - 32 - 32 - 42;
            this.worldNameText = new TextWidget(Text.literal(pendingInvite.worldName), RealmsPendingInvitesScreen.this.textRenderer).setMaxWidth(i).setTextColor(-1);
            this.worldOwnerNameText = new TextWidget(Text.literal(pendingInvite.worldOwnerName), RealmsPendingInvitesScreen.this.textRenderer).setMaxWidth(i).setTextColor(-6250336);
            this.dateText = new TextWidget(RealmsUtil.convertToAgePresentation(pendingInvite.date), RealmsPendingInvitesScreen.this.textRenderer).setMaxWidth(i).setTextColor(-6250336);
            ButtonWidget.NarrationSupplier lv = this.getNarration(pendingInvite);
            this.acceptButton = TextIconButtonWidget.builder(ACCEPT_TEXT, button -> this.handle(true), false).texture(ACCEPT_TEXTURE, 18, 18).dimension(21, 21).narration(lv).useTextAsTooltip().build();
            this.rejectButton = TextIconButtonWidget.builder(REJECT_TEXT, button -> this.handle(false), false).texture(REJECT_TEXTURE, 18, 18).dimension(21, 21).narration(lv).useTextAsTooltip().build();
            this.buttons.addAll(List.of(this.acceptButton, this.rejectButton));
        }

        private ButtonWidget.NarrationSupplier getNarration(PendingInvite invite) {
            return textSupplier -> {
                MutableText lv = ScreenTexts.joinSentences((Text)textSupplier.get(), Text.literal(arg.worldName), Text.literal(arg.worldOwnerName), RealmsUtil.convertToAgePresentation(arg.date));
                return Text.translatable("narrator.select", lv);
            };
        }

        @Override
        public List<? extends Element> children() {
            return this.buttons;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return this.buttons;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int k = this.getContentX();
            int l = this.getContentY();
            int m = k + 38;
            RealmsUtil.drawPlayerHead(context, k, l, 32, this.pendingInvite.worldOwnerUuid);
            this.worldNameText.setPosition(m, l + 1);
            this.worldNameText.renderWidget(context, mouseX, mouseY, k);
            this.worldOwnerNameText.setPosition(m, l + 12);
            this.worldOwnerNameText.renderWidget(context, mouseX, mouseY, k);
            this.dateText.setPosition(m, l + 24);
            this.dateText.renderWidget(context, mouseX, mouseY, k);
            int n = l + this.getContentHeight() / 2 - 10;
            this.acceptButton.setPosition(k + this.getContentWidth() - 16 - 42, n);
            this.acceptButton.render(context, mouseX, mouseY, deltaTicks);
            this.rejectButton.setPosition(k + this.getContentWidth() - 8 - 21, n);
            this.rejectButton.render(context, mouseX, mouseY, deltaTicks);
        }

        private void handle(boolean accepted) {
            String string = this.pendingInvite.invitationId;
            CompletableFuture.supplyAsync(() -> {
                try {
                    RealmsClient lv = RealmsClient.create();
                    if (accepted) {
                        lv.acceptInvitation(string);
                    } else {
                        lv.rejectInvitation(string);
                    }
                    return true;
                } catch (RealmsServiceException lv2) {
                    LOGGER.error("Couldn't handle invite", lv2);
                    return false;
                }
            }, Util.getIoWorkerExecutor()).thenAcceptAsync(processed -> {
                if (processed.booleanValue()) {
                    RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.remove(this);
                    RealmsPeriodicCheckers lv = RealmsPendingInvitesScreen.this.client.getRealmsPeriodicCheckers();
                    if (accepted) {
                        lv.serverList.reset();
                    }
                    lv.pendingInvitesCount.reset();
                }
            }, RealmsPendingInvitesScreen.this.executor);
        }
    }
}

