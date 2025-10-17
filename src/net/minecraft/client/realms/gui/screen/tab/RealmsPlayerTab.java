/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen.tab;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.realms.dto.Ops;
import net.minecraft.client.realms.dto.PlayerInfo;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.gui.screen.RealmsConfigureWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsConfirmScreen;
import net.minecraft.client.realms.gui.screen.RealmsInviteScreen;
import net.minecraft.client.realms.gui.screen.tab.RealmsUpdatableTab;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
class RealmsPlayerTab
extends GridScreenTab
implements RealmsUpdatableTab {
    static final Logger LOGGER = LogUtils.getLogger();
    static final Text TITLE = Text.translatable("mco.configure.world.players.title");
    static final Text QUESTION_TEXT = Text.translatable("mco.question");
    private static final int field_49462 = 8;
    final RealmsConfigureWorldScreen screen;
    final MinecraftClient client;
    final TextRenderer textRenderer;
    RealmsServer serverData;
    final InvitedObjectSelectionList playerList;

    RealmsPlayerTab(RealmsConfigureWorldScreen screen, MinecraftClient client, RealmsServer serverData) {
        super(TITLE);
        this.screen = screen;
        this.client = client;
        this.textRenderer = screen.getTextRenderer();
        this.serverData = serverData;
        GridWidget.Adder lv = this.grid.setSpacing(8).createAdder(1);
        this.playerList = lv.add(new InvitedObjectSelectionList(screen.width, this.getPlayerListHeight()), Positioner.create().alignTop().alignHorizontalCenter());
        lv.add(ButtonWidget.builder(Text.translatable("mco.configure.world.buttons.invite"), button -> client.setScreen(new RealmsInviteScreen(screen, serverData))).build(), Positioner.create().alignBottom().alignHorizontalCenter());
        this.update(serverData);
    }

    public int getPlayerListHeight() {
        return this.screen.getContentHeight() - 20 - 16;
    }

    @Override
    public void refreshGrid(ScreenRect tabArea) {
        this.playerList.position(this.screen.width, this.getPlayerListHeight(), this.screen.layout.getHeaderHeight());
        super.refreshGrid(tabArea);
    }

    @Override
    public void update(RealmsServer server) {
        this.serverData = server;
        this.playerList.refreshEntries(server);
    }

    @Environment(value=EnvType.CLIENT)
    class InvitedObjectSelectionList
    extends ElementListWidget<PlayerTabEntry> {
        private static final int field_49472 = 36;

        public InvitedObjectSelectionList(int width, int height) {
            super(MinecraftClient.getInstance(), width, height, RealmsPlayerTab.this.screen.getHeaderHeight(), 36);
        }

        void refreshEntries(RealmsServer serverData) {
            this.clearEntries();
            this.addEntries(serverData);
        }

        private void addEntries(RealmsServer serverData) {
            HeaderEntry headerEntry = new HeaderEntry(RealmsPlayerTab.this.textRenderer);
            Objects.requireNonNull(RealmsPlayerTab.this.textRenderer);
            this.addEntry(headerEntry, (int)(9.0f * 1.5f));
            for (InvitedObjectSelectionListEntry lv : serverData.players.stream().map(arg -> new InvitedObjectSelectionListEntry((PlayerInfo)arg)).toList()) {
                this.addEntry(lv);
            }
        }

        @Override
        protected void drawMenuListBackground(DrawContext context) {
        }

        @Override
        protected void drawHeaderAndFooterSeparators(DrawContext context) {
        }

        @Override
        public int getRowWidth() {
            return 300;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class HeaderEntry
    extends PlayerTabEntry {
        private final TextRenderer textRenderer;
        private String invitedPlayerCount = "";
        private final NarratedMultilineTextWidget textWidget;

        public HeaderEntry(TextRenderer textRenderer) {
            this.textRenderer = textRenderer;
            this.textWidget = new NarratedMultilineTextWidget(RealmsPlayerTab.this.playerList.getRowWidth(), Text.translatable("mco.configure.world.invited.number", "").formatted(Formatting.UNDERLINE), textRenderer, false, NarratedMultilineTextWidget.BackgroundRendering.ON_FOCUS, 4);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            String string;
            String string2 = string = RealmsPlayerTab.this.serverData.players != null ? Integer.toString(RealmsPlayerTab.this.serverData.players.size()) : "0";
            if (!string.equals(this.invitedPlayerCount)) {
                this.invitedPlayerCount = string;
                MutableText lv = Text.translatable("mco.configure.world.invited.number", string);
                this.textWidget.setMessage(lv.formatted(Formatting.UNDERLINE));
            }
            this.textWidget.setPosition(this.getX() + this.getWidth() / 2 - this.textRenderer.getWidth(this.textWidget.getMessage()) / 2, this.getY() + this.getHeight() / 2 - this.textRenderer.fontHeight / 2);
            this.textWidget.render(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(this.textWidget);
        }

        @Override
        public List<? extends Element> children() {
            return List.of(this.textWidget);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class InvitedObjectSelectionListEntry
    extends PlayerTabEntry {
        protected static final int field_60252 = 32;
        private static final Text NORMAL_TOOLTIP_TEXT = Text.translatable("mco.configure.world.invites.normal.tooltip");
        private static final Text OPS_TOOLTIP_TEXT = Text.translatable("mco.configure.world.invites.ops.tooltip");
        private static final Text REMOVE_TOOLTIP_TEXT = Text.translatable("mco.configure.world.invites.remove.tooltip");
        private static final Identifier MAKE_OPERATOR_TEXTURE = Identifier.ofVanilla("player_list/make_operator");
        private static final Identifier REMOVE_OPERATOR_TEXTURE = Identifier.ofVanilla("player_list/remove_operator");
        private static final Identifier REMOVE_PLAYER_TEXTURE = Identifier.ofVanilla("player_list/remove_player");
        private static final int field_49470 = 8;
        private static final int field_49471 = 7;
        private final PlayerInfo playerInfo;
        private final ButtonWidget uninviteButton;
        private final ButtonWidget opButton;
        private final ButtonWidget deopButton;

        public InvitedObjectSelectionListEntry(PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
            int i = RealmsPlayerTab.this.serverData.players.indexOf(this.playerInfo);
            this.opButton = TextIconButtonWidget.builder(NORMAL_TOOLTIP_TEXT, button -> this.op(i), false).texture(MAKE_OPERATOR_TEXTURE, 8, 7).width(16 + RealmsPlayerTab.this.screen.getTextRenderer().getWidth(NORMAL_TOOLTIP_TEXT)).narration(textSupplier -> ScreenTexts.joinSentences(Text.translatable("mco.invited.player.narration", playerInfo.getName()), (Text)textSupplier.get(), Text.translatable("narration.cycle_button.usage.focused", OPS_TOOLTIP_TEXT))).build();
            this.deopButton = TextIconButtonWidget.builder(OPS_TOOLTIP_TEXT, button -> this.deop(i), false).texture(REMOVE_OPERATOR_TEXTURE, 8, 7).width(16 + RealmsPlayerTab.this.screen.getTextRenderer().getWidth(OPS_TOOLTIP_TEXT)).narration(textSupplier -> ScreenTexts.joinSentences(Text.translatable("mco.invited.player.narration", playerInfo.getName()), (Text)textSupplier.get(), Text.translatable("narration.cycle_button.usage.focused", NORMAL_TOOLTIP_TEXT))).build();
            this.uninviteButton = TextIconButtonWidget.builder(REMOVE_TOOLTIP_TEXT, button -> this.uninvite(i), false).texture(REMOVE_PLAYER_TEXTURE, 8, 7).width(16 + RealmsPlayerTab.this.screen.getTextRenderer().getWidth(REMOVE_TOOLTIP_TEXT)).narration(textSupplier -> ScreenTexts.joinSentences(Text.translatable("mco.invited.player.narration", playerInfo.getName()), (Text)textSupplier.get())).build();
            this.refreshOpButtonsVisibility();
        }

        private void op(int index) {
            UUID uUID = RealmsPlayerTab.this.serverData.players.get(index).getUuid();
            RealmsUtil.runAsync(client -> client.op(RealmsPlayerTab.this.serverData.id, uUID), error -> LOGGER.error("Couldn't op the user", (Throwable)error)).thenAcceptAsync(ops -> {
                this.setOps((Ops)ops);
                this.refreshOpButtonsVisibility();
                this.setFocused(this.deopButton);
            }, (Executor)RealmsPlayerTab.this.client);
        }

        private void deop(int index) {
            UUID uUID = RealmsPlayerTab.this.serverData.players.get(index).getUuid();
            RealmsUtil.runAsync(client -> client.deop(RealmsPlayerTab.this.serverData.id, uUID), error -> LOGGER.error("Couldn't deop the user", (Throwable)error)).thenAcceptAsync(ops -> {
                this.setOps((Ops)ops);
                this.refreshOpButtonsVisibility();
                this.setFocused(this.opButton);
            }, (Executor)RealmsPlayerTab.this.client);
        }

        private void uninvite(int index) {
            if (index >= 0 && index < RealmsPlayerTab.this.serverData.players.size()) {
                PlayerInfo lv = RealmsPlayerTab.this.serverData.players.get(index);
                RealmsConfirmScreen lv2 = new RealmsConfirmScreen(confirmed -> {
                    if (confirmed) {
                        RealmsUtil.runAsync(client -> client.uninvite(RealmsPlayerTab.this.serverData.id, lv.getUuid()), error -> LOGGER.error("Couldn't uninvite user", (Throwable)error));
                        RealmsPlayerTab.this.serverData.players.remove(index);
                        RealmsPlayerTab.this.update(RealmsPlayerTab.this.serverData);
                    }
                    RealmsPlayerTab.this.client.setScreen(RealmsPlayerTab.this.screen);
                }, QUESTION_TEXT, Text.translatable("mco.configure.world.uninvite.player", lv.getName()));
                RealmsPlayerTab.this.client.setScreen(lv2);
            }
        }

        private void setOps(Ops ops) {
            for (PlayerInfo lv : RealmsPlayerTab.this.serverData.players) {
                lv.setOperator(ops.ops.contains(lv.getName()));
            }
        }

        private void refreshOpButtonsVisibility() {
            this.opButton.visible = !this.playerInfo.isOperator();
            this.deopButton.visible = !this.opButton.visible;
        }

        private ButtonWidget getOpButton() {
            if (this.opButton.visible) {
                return this.opButton;
            }
            return this.deopButton;
        }

        @Override
        public List<? extends Element> children() {
            return ImmutableList.of(this.getOpButton(), this.uninviteButton);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.getOpButton(), this.uninviteButton);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int k = !this.playerInfo.isAccepted() ? -6250336 : (this.playerInfo.isOnline() ? -16711936 : Colors.WHITE);
            int l = this.getContentMiddleY() - 16;
            RealmsUtil.drawPlayerHead(context, this.getContentX(), l, 32, this.playerInfo.getUuid());
            int m = this.getContentMiddleY() - RealmsPlayerTab.this.textRenderer.fontHeight / 2;
            context.drawTextWithShadow(RealmsPlayerTab.this.textRenderer, this.playerInfo.getName(), this.getContentX() + 8 + 32, m, k);
            int n = this.getContentMiddleY() - 10;
            int o = this.getContentRightEnd() - this.uninviteButton.getWidth();
            this.uninviteButton.setPosition(o, n);
            this.uninviteButton.render(context, mouseX, mouseY, deltaTicks);
            int p = o - this.getOpButton().getWidth() - 8;
            this.opButton.setPosition(p, n);
            this.opButton.render(context, mouseX, mouseY, deltaTicks);
            this.deopButton.setPosition(p, n);
            this.deopButton.render(context, mouseX, mouseY, deltaTicks);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static abstract class PlayerTabEntry
    extends ElementListWidget.Entry<PlayerTabEntry> {
        PlayerTabEntry() {
        }
    }
}

