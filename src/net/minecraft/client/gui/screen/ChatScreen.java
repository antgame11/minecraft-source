/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatScreen
extends Screen {
    public static final double SHIFT_SCROLL_AMOUNT = 7.0;
    private static final Text USAGE_TEXT = Text.translatable("chat_screen.usage");
    private static final int MAX_INDICATOR_TOOLTIP_WIDTH = 210;
    private String chatLastMessage = "";
    private int messageHistoryIndex = -1;
    protected TextFieldWidget chatField;
    protected String originalChatText;
    protected boolean draft;
    protected CloseReason closeReason = CloseReason.INTERRUPTED;
    ChatInputSuggestor chatInputSuggestor;

    public ChatScreen(String text, boolean draft) {
        super(Text.translatable("chat_screen.title"));
        this.originalChatText = text;
        this.draft = draft;
    }

    @Override
    protected void init() {
        this.messageHistoryIndex = this.client.inGameHud.getChatHud().getMessageHistory().size();
        this.chatField = new TextFieldWidget(this.client.advanceValidatingTextRenderer, 4, this.height - 12, this.width - 4, 12, (Text)Text.translatable("chat.editBox")){

            @Override
            protected MutableText getNarrationMessage() {
                return super.getNarrationMessage().append(ChatScreen.this.chatInputSuggestor.getNarration());
            }
        };
        this.chatField.setMaxLength(256);
        this.chatField.setDrawsBackground(false);
        this.chatField.setText(this.originalChatText);
        this.chatField.setChangedListener(this::onChatFieldUpdate);
        this.chatField.addFormatter(this::format);
        this.chatField.setFocusUnlocked(false);
        this.addDrawableChild(this.chatField);
        this.chatInputSuggestor = new ChatInputSuggestor(this.client, this, this.chatField, this.textRenderer, false, false, 1, 10, true, -805306368);
        this.chatInputSuggestor.setCanLeave(false);
        this.chatInputSuggestor.setWindowActive(false);
        this.chatInputSuggestor.refresh();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.chatField);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        this.originalChatText = this.chatField.getText();
        this.init(client, width, height);
    }

    @Override
    public void close() {
        this.closeReason = CloseReason.INTENTIONAL;
        super.close();
    }

    @Override
    public void removed() {
        this.client.inGameHud.getChatHud().resetScroll();
        this.originalChatText = this.chatField.getText();
        if (this.shouldNotSaveDraft() || StringUtils.isBlank(this.originalChatText)) {
            this.client.inGameHud.getChatHud().discardDraft();
        } else if (!this.draft) {
            this.client.inGameHud.getChatHud().saveDraft(this.originalChatText);
        }
    }

    protected boolean shouldNotSaveDraft() {
        return this.closeReason != CloseReason.INTERRUPTED && (this.closeReason != CloseReason.INTENTIONAL || this.client.options.getChatDrafts().getValue() == false);
    }

    private void onChatFieldUpdate(String chatText) {
        this.chatInputSuggestor.setWindowActive(true);
        this.chatInputSuggestor.refresh();
        this.draft = false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (this.chatInputSuggestor.keyPressed(input)) {
            return true;
        }
        if (this.draft && input.key() == InputUtil.GLFW_KEY_BACKSPACE) {
            this.chatField.setText("");
            this.draft = false;
            return true;
        }
        if (super.keyPressed(input)) {
            return true;
        }
        if (input.isEnter()) {
            this.sendMessage(this.chatField.getText(), true);
            this.closeReason = CloseReason.DONE;
            this.client.setScreen(null);
            return true;
        }
        switch (input.key()) {
            case 265: {
                this.setChatFromHistory(-1);
                break;
            }
            case 264: {
                this.setChatFromHistory(1);
                break;
            }
            case 266: {
                this.client.inGameHud.getChatHud().scroll(this.client.inGameHud.getChatHud().getVisibleLineCount() - 1);
                break;
            }
            case 267: {
                this.client.inGameHud.getChatHud().scroll(-this.client.inGameHud.getChatHud().getVisibleLineCount() + 1);
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.chatInputSuggestor.mouseScrolled(verticalAmount = MathHelper.clamp(verticalAmount, -1.0, 1.0))) {
            return true;
        }
        if (!this.client.isShiftPressed()) {
            verticalAmount *= 7.0;
        }
        this.client.inGameHud.getChatHud().scroll((int)verticalAmount);
        return true;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.chatInputSuggestor.mouseClicked(click)) {
            return true;
        }
        if (click.button() == 0) {
            ChatHud lv = this.client.inGameHud.getChatHud();
            if (lv.mouseClicked(click.x(), click.y())) {
                return true;
            }
            Style lv2 = this.getTextStyleAt(click.x(), click.y());
            if (lv2 != null && this.handleTextClick(lv2)) {
                this.originalChatText = this.chatField.getText();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void insertText(String text, boolean override) {
        if (override) {
            this.chatField.setText(text);
        } else {
            this.chatField.write(text);
        }
    }

    public void setChatFromHistory(int offset) {
        int j = this.messageHistoryIndex + offset;
        int k = this.client.inGameHud.getChatHud().getMessageHistory().size();
        if ((j = MathHelper.clamp(j, 0, k)) == this.messageHistoryIndex) {
            return;
        }
        if (j == k) {
            this.messageHistoryIndex = k;
            this.chatField.setText(this.chatLastMessage);
            return;
        }
        if (this.messageHistoryIndex == k) {
            this.chatLastMessage = this.chatField.getText();
        }
        this.chatField.setText(this.client.inGameHud.getChatHud().getMessageHistory().get(j));
        this.chatInputSuggestor.setWindowActive(false);
        this.messageHistoryIndex = j;
    }

    @Nullable
    private OrderedText format(String string, int firstCharacterIndex) {
        if (this.draft) {
            return OrderedText.styledForwardsVisitedString(string, Style.EMPTY.withColor(Formatting.GRAY).withItalic(true));
        }
        return null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(2, this.height - 14, this.width - 2, this.height - 2, this.client.options.getTextBackgroundColor(Integer.MIN_VALUE));
        this.client.inGameHud.getChatHud().render(context, this.client.inGameHud.getTicks(), mouseX, mouseY, true);
        super.render(context, mouseX, mouseY, deltaTicks);
        this.chatInputSuggestor.render(context, mouseX, mouseY);
        MessageIndicator lv = this.client.inGameHud.getChatHud().getIndicatorAt(mouseX, mouseY);
        if (lv != null && lv.text() != null) {
            context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines(lv.text(), 210), mouseX, mouseY);
        } else {
            Style lv2 = this.getTextStyleAt(mouseX, mouseY);
            context.drawHoverEvent(this.textRenderer, lv2, mouseX, mouseY);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keepOpenThroughPortal() {
        return true;
    }

    @Override
    protected void addScreenNarrations(NarrationMessageBuilder messageBuilder) {
        messageBuilder.put(NarrationPart.TITLE, this.getTitle());
        messageBuilder.put(NarrationPart.USAGE, USAGE_TEXT);
        String string = this.chatField.getText();
        if (!string.isEmpty()) {
            messageBuilder.nextMessage().put(NarrationPart.TITLE, (Text)Text.translatable("chat_screen.message", string));
        }
    }

    @Nullable
    private Style getTextStyleAt(double x, double y) {
        return this.client.inGameHud.getChatHud().getTextStyleAt(x, y);
    }

    public void sendMessage(String chatText, boolean addToHistory) {
        if ((chatText = this.normalize(chatText)).isEmpty()) {
            return;
        }
        if (addToHistory) {
            this.client.inGameHud.getChatHud().addToMessageHistory(chatText);
        }
        if (chatText.startsWith("/")) {
            this.client.player.networkHandler.sendChatCommand(chatText.substring(1));
        } else {
            this.client.player.networkHandler.sendChatMessage(chatText);
        }
    }

    public String normalize(String chatText) {
        return StringHelper.truncateChat(StringUtils.normalizeSpace(chatText.trim()));
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum CloseReason {
        INTENTIONAL,
        INTERRUPTED,
        DONE;

    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface Factory<T extends ChatScreen> {
        public T create(String var1, boolean var2);
    }
}

