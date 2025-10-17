/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TextFieldWidget
extends ClickableWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/text_field"), Identifier.ofVanilla("widget/text_field_highlighted"));
    public static final int field_32194 = -1;
    public static final int field_32195 = 1;
    private static final int field_32197 = 1;
    private static final String HORIZONTAL_CURSOR = "_";
    public static final int DEFAULT_EDITABLE_COLOR = -2039584;
    public static final Style PLACEHOLDER_STYLE = Style.EMPTY.withColor(Formatting.DARK_GRAY);
    public static final Style SEARCH_STYLE = Style.EMPTY.withFormatting(Formatting.GRAY, Formatting.ITALIC);
    private static final int field_45354 = 300;
    private final TextRenderer textRenderer;
    private String text = "";
    private int maxLength = 32;
    private boolean drawsBackground = true;
    private boolean focusUnlocked = true;
    private boolean editable = true;
    private boolean centered = false;
    private boolean textShadow = true;
    private int firstCharacterIndex;
    private int selectionStart;
    private int selectionEnd;
    private int editableColor = -2039584;
    private int uneditableColor = -9408400;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> changedListener;
    private Predicate<String> textPredicate = Objects::nonNull;
    private final List<Formatter> formatters = new ArrayList<Formatter>();
    @Nullable
    private Text placeholder;
    private long lastSwitchFocusTime = Util.getMeasuringTimeMs();
    private int textX;
    private int textY;

    public TextFieldWidget(TextRenderer textRenderer, int width, int height, Text text) {
        this(textRenderer, 0, 0, width, height, text);
    }

    public TextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        this(textRenderer, x, y, width, height, null, text);
    }

    public TextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
        super(x, y, width, height, text);
        this.textRenderer = textRenderer;
        if (copyFrom != null) {
            this.setText(copyFrom.getText());
        }
        this.updateTextPosition();
    }

    public void setChangedListener(Consumer<String> changedListener) {
        this.changedListener = changedListener;
    }

    public void addFormatter(Formatter formatter) {
        this.formatters.add(formatter);
    }

    @Override
    protected MutableText getNarrationMessage() {
        Text lv = this.getMessage();
        return Text.translatable("gui.narrate.editBox", lv, this.text);
    }

    public void setText(String text) {
        if (!this.textPredicate.test(text)) {
            return;
        }
        this.text = text.length() > this.maxLength ? text.substring(0, this.maxLength) : text;
        this.setCursorToEnd(false);
        this.setSelectionEnd(this.selectionStart);
        this.onChanged(text);
    }

    public String getText() {
        return this.text;
    }

    public String getSelectedText() {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        return this.text.substring(i, j);
    }

    @Override
    public void setX(int i) {
        super.setX(i);
        this.updateTextPosition();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.updateTextPosition();
    }

    public void setTextPredicate(Predicate<String> textPredicate) {
        this.textPredicate = textPredicate;
    }

    public void write(String text) {
        String string3;
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        int k = this.maxLength - this.text.length() - (i - j);
        if (k <= 0) {
            return;
        }
        String string2 = StringHelper.stripInvalidChars(text);
        int l = string2.length();
        if (k < l) {
            if (Character.isHighSurrogate(string2.charAt(k - 1))) {
                --k;
            }
            string2 = string2.substring(0, k);
            l = k;
        }
        if (!this.textPredicate.test(string3 = new StringBuilder(this.text).replace(i, j, string2).toString())) {
            return;
        }
        this.text = string3;
        this.setSelectionStart(i + l);
        this.setSelectionEnd(this.selectionStart);
        this.onChanged(this.text);
    }

    private void onChanged(String newText) {
        if (this.changedListener != null) {
            this.changedListener.accept(newText);
        }
        this.updateTextPosition();
    }

    private void erase(int offset, boolean words) {
        if (words) {
            this.eraseWords(offset);
        } else {
            this.eraseCharacters(offset);
        }
    }

    public void eraseWords(int wordOffset) {
        if (this.text.isEmpty()) {
            return;
        }
        if (this.selectionEnd != this.selectionStart) {
            this.write("");
            return;
        }
        this.eraseCharactersTo(this.getWordSkipPosition(wordOffset));
    }

    public void eraseCharacters(int characterOffset) {
        this.eraseCharactersTo(this.getCursorPosWithOffset(characterOffset));
    }

    public void eraseCharactersTo(int position) {
        int k;
        if (this.text.isEmpty()) {
            return;
        }
        if (this.selectionEnd != this.selectionStart) {
            this.write("");
            return;
        }
        int j = Math.min(position, this.selectionStart);
        if (j == (k = Math.max(position, this.selectionStart))) {
            return;
        }
        String string = new StringBuilder(this.text).delete(j, k).toString();
        if (!this.textPredicate.test(string)) {
            return;
        }
        this.text = string;
        this.setCursor(j, false);
    }

    public int getWordSkipPosition(int wordOffset) {
        return this.getWordSkipPosition(wordOffset, this.getCursor());
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition) {
        return this.getWordSkipPosition(wordOffset, cursorPosition, true);
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
        int k = cursorPosition;
        boolean bl2 = wordOffset < 0;
        int l = Math.abs(wordOffset);
        for (int m = 0; m < l; ++m) {
            if (bl2) {
                while (skipOverSpaces && k > 0 && this.text.charAt(k - 1) == ' ') {
                    --k;
                }
                while (k > 0 && this.text.charAt(k - 1) != ' ') {
                    --k;
                }
                continue;
            }
            int n = this.text.length();
            if ((k = this.text.indexOf(32, k)) == -1) {
                k = n;
                continue;
            }
            while (skipOverSpaces && k < n && this.text.charAt(k) == ' ') {
                ++k;
            }
        }
        return k;
    }

    public void moveCursor(int offset, boolean shiftKeyPressed) {
        this.setCursor(this.getCursorPosWithOffset(offset), shiftKeyPressed);
    }

    private int getCursorPosWithOffset(int offset) {
        return Util.moveCursor(this.text, this.selectionStart, offset);
    }

    public void setCursor(int cursor, boolean select) {
        this.setSelectionStart(cursor);
        if (!select) {
            this.setSelectionEnd(this.selectionStart);
        }
        this.onChanged(this.text);
    }

    public void setSelectionStart(int cursor) {
        this.selectionStart = MathHelper.clamp(cursor, 0, this.text.length());
        this.updateFirstCharacterIndex(this.selectionStart);
    }

    public void setCursorToStart(boolean shiftKeyPressed) {
        this.setCursor(0, shiftKeyPressed);
    }

    public void setCursorToEnd(boolean shiftKeyPressed) {
        this.setCursor(this.text.length(), shiftKeyPressed);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!this.isInteractable() || !this.isFocused()) {
            return false;
        }
        switch (input.key()) {
            case 263: {
                if (input.hasCtrl()) {
                    this.setCursor(this.getWordSkipPosition(-1), input.hasShift());
                } else {
                    this.moveCursor(-1, input.hasShift());
                }
                return true;
            }
            case 262: {
                if (input.hasCtrl()) {
                    this.setCursor(this.getWordSkipPosition(1), input.hasShift());
                } else {
                    this.moveCursor(1, input.hasShift());
                }
                return true;
            }
            case 259: {
                if (this.editable) {
                    this.erase(-1, input.hasCtrl());
                }
                return true;
            }
            case 261: {
                if (this.editable) {
                    this.erase(1, input.hasCtrl());
                }
                return true;
            }
            case 268: {
                this.setCursorToStart(input.hasShift());
                return true;
            }
            case 269: {
                this.setCursorToEnd(input.hasShift());
                return true;
            }
        }
        if (input.isSelectAll()) {
            this.setCursorToEnd(false);
            this.setSelectionEnd(0);
            return true;
        }
        if (input.isCopy()) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
            return true;
        }
        if (input.isPaste()) {
            if (this.isEditable()) {
                this.write(MinecraftClient.getInstance().keyboard.getClipboard());
            }
            return true;
        }
        if (input.isCut()) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
            if (this.isEditable()) {
                this.write("");
            }
            return true;
        }
        return false;
    }

    public boolean isActive() {
        return this.isInteractable() && this.isFocused() && this.isEditable();
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!this.isActive()) {
            return false;
        }
        if (input.isValidChar()) {
            if (this.editable) {
                this.write(input.asString());
            }
            return true;
        }
        return false;
    }

    private int calculateCursorPos(Click click) {
        int i = Math.min(MathHelper.floor(click.x()) - this.textX, this.getInnerWidth());
        String string = this.text.substring(this.firstCharacterIndex);
        return this.firstCharacterIndex + this.textRenderer.trimToWidth(string, i).length();
    }

    private void selectWord(Click click) {
        int i = this.calculateCursorPos(click);
        int j = this.getWordSkipPosition(-1, i);
        int k = this.getWordSkipPosition(1, i);
        this.setCursor(j, false);
        this.setCursor(k, true);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        if (doubled) {
            this.selectWord(click);
        } else {
            this.setCursor(this.calculateCursorPos(click), click.hasShift());
        }
    }

    @Override
    protected void onDrag(Click click, double offsetX, double offsetY) {
        this.setCursor(this.calculateCursorPos(click), true);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (!this.isVisible()) {
            return;
        }
        if (this.drawsBackground()) {
            Identifier lv = TEXTURES.get(this.isInteractable(), this.isFocused());
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
        int k = this.editable ? this.editableColor : this.uneditableColor;
        int l = this.selectionStart - this.firstCharacterIndex;
        String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
        boolean bl = l >= 0 && l <= string.length();
        boolean bl2 = this.isFocused() && (Util.getMeasuringTimeMs() - this.lastSwitchFocusTime) / 300L % 2L == 0L && bl;
        int m = this.textX;
        int n = MathHelper.clamp(this.selectionEnd - this.firstCharacterIndex, 0, string.length());
        if (!string.isEmpty()) {
            String string2 = bl ? string.substring(0, l) : string;
            OrderedText lv2 = this.format(string2, this.firstCharacterIndex);
            context.drawText(this.textRenderer, lv2, m, this.textY, k, this.textShadow);
            m += this.textRenderer.getWidth(lv2) + 1;
        }
        boolean bl3 = this.selectionStart < this.text.length() || this.text.length() >= this.getMaxLength();
        int o = m;
        if (!bl) {
            o = l > 0 ? this.textX + this.width : this.textX;
        } else if (bl3) {
            --o;
            --m;
        }
        if (!string.isEmpty() && bl && l < string.length()) {
            context.drawText(this.textRenderer, this.format(string.substring(l), this.selectionStart), m, this.textY, k, this.textShadow);
        }
        if (this.placeholder != null && string.isEmpty() && !this.isFocused()) {
            context.drawTextWithShadow(this.textRenderer, this.placeholder, m, this.textY, k);
        }
        if (!bl3 && this.suggestion != null) {
            context.drawText(this.textRenderer, this.suggestion, o - 1, this.textY, Colors.GRAY, this.textShadow);
        }
        if (n != l) {
            int p = this.textX + this.textRenderer.getWidth(string.substring(0, n));
            context.drawSelection(Math.min(o, this.getX() + this.width), this.textY - 1, Math.min(p - 1, this.getX() + this.width), this.textY + 1 + this.textRenderer.fontHeight);
        }
        if (bl2) {
            if (bl3) {
                context.fill(o, this.textY - 1, o + 1, this.textY + 1 + this.textRenderer.fontHeight, k);
            } else {
                context.drawText(this.textRenderer, HORIZONTAL_CURSOR, o, this.textY, k, this.textShadow);
            }
        }
        if (this.isHovered()) {
            context.setCursor(this.isEditable() ? StandardCursors.IBEAM : StandardCursors.NOT_ALLOWED);
        }
    }

    private OrderedText format(String string, int firstCharacterIndex) {
        for (Formatter lv : this.formatters) {
            OrderedText lv2 = lv.format(string, firstCharacterIndex);
            if (lv2 == null) continue;
            return lv2;
        }
        return OrderedText.styledForwardsVisitedString(string, Style.EMPTY);
    }

    private void updateTextPosition() {
        if (this.textRenderer == null) {
            return;
        }
        String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
        this.textX = this.getX() + (this.isCentered() ? (this.getWidth() - this.textRenderer.getWidth(string)) / 2 : (this.drawsBackground ? 4 : 0));
        this.textY = this.drawsBackground ? this.getY() + (this.height - 8) / 2 : this.getY();
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (this.text.length() > maxLength) {
            this.text = this.text.substring(0, maxLength);
            this.onChanged(this.text);
        }
    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursor() {
        return this.selectionStart;
    }

    public boolean drawsBackground() {
        return this.drawsBackground;
    }

    public void setDrawsBackground(boolean drawsBackground) {
        this.drawsBackground = drawsBackground;
        this.updateTextPosition();
    }

    public void setEditableColor(int editableColor) {
        this.editableColor = editableColor;
    }

    public void setUneditableColor(int uneditableColor) {
        this.uneditableColor = uneditableColor;
    }

    @Override
    public void setFocused(boolean focused) {
        if (!this.focusUnlocked && !focused) {
            return;
        }
        super.setFocused(focused);
        if (focused) {
            this.lastSwitchFocusTime = Util.getMeasuringTimeMs();
        }
    }

    private boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    private boolean isCentered() {
        return this.centered;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
        this.updateTextPosition();
    }

    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }

    public int getInnerWidth() {
        return this.drawsBackground() ? this.width - 8 : this.width;
    }

    public void setSelectionEnd(int index) {
        this.selectionEnd = MathHelper.clamp(index, 0, this.text.length());
        this.updateFirstCharacterIndex(this.selectionEnd);
    }

    private void updateFirstCharacterIndex(int cursor) {
        if (this.textRenderer == null) {
            return;
        }
        this.firstCharacterIndex = Math.min(this.firstCharacterIndex, this.text.length());
        int j = this.getInnerWidth();
        String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), j);
        int k = string.length() + this.firstCharacterIndex;
        if (cursor == this.firstCharacterIndex) {
            this.firstCharacterIndex -= this.textRenderer.trimToWidth(this.text, j, true).length();
        }
        if (cursor > k) {
            this.firstCharacterIndex += cursor - k;
        } else if (cursor <= this.firstCharacterIndex) {
            this.firstCharacterIndex -= this.firstCharacterIndex - cursor;
        }
        this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, this.text.length());
    }

    public void setFocusUnlocked(boolean focusUnlocked) {
        this.focusUnlocked = focusUnlocked;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    public int getCharacterX(int index) {
        if (index > this.text.length()) {
            return this.getX();
        }
        return this.getX() + this.textRenderer.getWidth(this.text.substring(0, index));
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, (Text)this.getNarrationMessage());
    }

    public void setPlaceholder(Text placeholder) {
        boolean bl = placeholder.getStyle().equals(Style.EMPTY);
        this.placeholder = bl ? placeholder.copy().fillStyle(PLACEHOLDER_STYLE) : placeholder;
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface Formatter {
        @Nullable
        public OrderedText format(String var1, int var2);
    }
}

