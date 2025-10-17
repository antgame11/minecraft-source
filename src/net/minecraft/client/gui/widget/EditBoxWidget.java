/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ScrollableTextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class EditBoxWidget
extends ScrollableTextFieldWidget {
    private static final int CURSOR_PADDING = 1;
    private static final int CURSOR_COLOR = -3092272;
    private static final String UNDERSCORE = "_";
    private static final int FOCUSED_BOX_TEXT_COLOR = -2039584;
    private static final int UNFOCUSED_BOX_TEXT_COLOR = -857677600;
    private static final int CURSOR_BLINK_INTERVAL = 300;
    private final TextRenderer textRenderer;
    private final Text placeholder;
    private final EditBox editBox;
    private final int textColor;
    private final boolean textShadow;
    private final int cursorColor;
    private long lastSwitchFocusTime = Util.getMeasuringTimeMs();

    EditBoxWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder, Text message, int textColor, boolean textShadow, int cursorColor, boolean hasBackground, boolean hasOverlay) {
        super(x, y, width, height, message, hasBackground, hasOverlay);
        this.textRenderer = textRenderer;
        this.textShadow = textShadow;
        this.textColor = textColor;
        this.cursorColor = cursorColor;
        this.placeholder = placeholder;
        this.editBox = new EditBox(textRenderer, width - this.getPadding());
        this.editBox.setCursorChangeListener(this::onCursorChange);
    }

    public void setMaxLength(int maxLength) {
        this.editBox.setMaxLength(maxLength);
    }

    public void setMaxLines(int maxLines) {
        this.editBox.setMaxLines(maxLines);
    }

    public void setChangeListener(Consumer<String> changeListener) {
        this.editBox.setChangeListener(changeListener);
    }

    public void setText(String text) {
        this.setText(text, false);
    }

    public void setText(String text, boolean allowOverflow) {
        this.editBox.setText(text, allowOverflow);
    }

    public String getText() {
        return this.editBox.getText();
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, (Text)Text.translatable("gui.narrate.editBox", this.getMessage(), this.getText()));
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        if (doubled) {
            this.editBox.selectWord();
        } else {
            this.editBox.setSelecting(click.hasShift());
            this.moveCursor(click.x(), click.y());
        }
    }

    @Override
    protected void onDrag(Click click, double offsetX, double offsetY) {
        this.editBox.setSelecting(true);
        this.moveCursor(click.x(), click.y());
        this.editBox.setSelecting(click.hasShift());
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        return this.editBox.handleSpecialKey(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!(this.visible && this.isFocused() && input.isValidChar())) {
            return false;
        }
        this.editBox.replaceSelection(input.asString());
        return true;
    }

    @Override
    protected void renderContents(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        String string = this.editBox.getText();
        if (string.isEmpty() && !this.isFocused()) {
            context.drawWrappedTextWithShadow(this.textRenderer, this.placeholder, this.getTextX(), this.getTextY(), this.width - this.getPadding(), -857677600);
            return;
        }
        int k = this.editBox.getCursor();
        boolean bl = this.isFocused() && (Util.getMeasuringTimeMs() - this.lastSwitchFocusTime) / 300L % 2L == 0L;
        boolean bl2 = k < string.length();
        int l = 0;
        int m = 0;
        int n = this.getTextY();
        boolean bl3 = false;
        for (EditBox.Substring lv : this.editBox.getLines()) {
            boolean bl4 = this.isVisible(n, n + this.textRenderer.fontHeight);
            int o = this.getTextX();
            if (bl && bl2 && k >= lv.beginIndex() && k <= lv.endIndex()) {
                if (bl4) {
                    string2 = string.substring(lv.beginIndex(), k);
                    context.drawText(this.textRenderer, string2, o, n, this.textColor, this.textShadow);
                    l = o + this.textRenderer.getWidth(string2);
                    if (!bl3) {
                        context.fill(l, n - 1, l + 1, n + 1 + this.textRenderer.fontHeight, this.cursorColor);
                        bl3 = true;
                    }
                    context.drawText(this.textRenderer, string.substring(k, lv.endIndex()), l, n, this.textColor, this.textShadow);
                }
            } else {
                if (bl4) {
                    string2 = string.substring(lv.beginIndex(), lv.endIndex());
                    context.drawText(this.textRenderer, string2, o, n, this.textColor, this.textShadow);
                    l = o + this.textRenderer.getWidth(string2) - 1;
                }
                m = n;
            }
            n += this.textRenderer.fontHeight;
        }
        if (bl && !bl2 && this.isVisible(m, m + this.textRenderer.fontHeight)) {
            context.drawText(this.textRenderer, UNDERSCORE, l + 1, m, this.cursorColor, this.textShadow);
        }
        if (this.editBox.hasSelection()) {
            EditBox.Substring lv2 = this.editBox.getSelection();
            int p = this.getTextX();
            n = this.getTextY();
            for (EditBox.Substring lv3 : this.editBox.getLines()) {
                if (lv2.beginIndex() > lv3.endIndex()) {
                    n += this.textRenderer.fontHeight;
                    continue;
                }
                if (lv3.beginIndex() > lv2.endIndex()) break;
                if (this.isVisible(n, n + this.textRenderer.fontHeight)) {
                    int q = this.textRenderer.getWidth(string.substring(lv3.beginIndex(), Math.max(lv2.beginIndex(), lv3.beginIndex())));
                    int r = lv2.endIndex() > lv3.endIndex() ? this.width - this.getTextMargin() : this.textRenderer.getWidth(string.substring(lv3.beginIndex(), lv2.endIndex()));
                    context.drawSelection(p + q, n, p + r, n + this.textRenderer.fontHeight);
                }
                n += this.textRenderer.fontHeight;
            }
        }
        if (this.isHovered()) {
            context.setCursor(StandardCursors.IBEAM);
        }
    }

    @Override
    protected void renderOverlay(DrawContext context) {
        super.renderOverlay(context);
        if (this.editBox.hasMaxLength()) {
            int i = this.editBox.getMaxLength();
            MutableText lv = Text.translatable("gui.multiLineEditBox.character_limit", this.editBox.getText().length(), i);
            context.drawTextWithShadow(this.textRenderer, lv, this.getX() + this.width - this.textRenderer.getWidth(lv), this.getY() + this.height + 4, Colors.LIGHT_GRAY);
        }
    }

    @Override
    public int getContentsHeight() {
        return this.textRenderer.fontHeight * this.editBox.getLineCount();
    }

    @Override
    protected double getDeltaYPerScroll() {
        return (double)this.textRenderer.fontHeight / 2.0;
    }

    private void onCursorChange() {
        double d = this.getScrollY();
        EditBox.Substring lv = this.editBox.getLine((int)(d / (double)this.textRenderer.fontHeight));
        if (this.editBox.getCursor() <= lv.beginIndex()) {
            d = this.editBox.getCurrentLineIndex() * this.textRenderer.fontHeight;
        } else {
            EditBox.Substring lv2 = this.editBox.getLine((int)((d + (double)this.height) / (double)this.textRenderer.fontHeight) - 1);
            if (this.editBox.getCursor() > lv2.endIndex()) {
                d = this.editBox.getCurrentLineIndex() * this.textRenderer.fontHeight - this.height + this.textRenderer.fontHeight + this.getPadding();
            }
        }
        this.setScrollY(d);
    }

    private void moveCursor(double mouseX, double mouseY) {
        double f = mouseX - (double)this.getX() - (double)this.getTextMargin();
        double g = mouseY - (double)this.getY() - (double)this.getTextMargin() + this.getScrollY();
        this.editBox.moveCursor(f, g);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) {
            this.lastSwitchFocusTime = Util.getMeasuringTimeMs();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private int x;
        private int y;
        private Text placeholder = ScreenTexts.EMPTY;
        private int textColor = -2039584;
        private boolean textShadow = true;
        private int cursorColor = -3092272;
        private boolean hasBackground = true;
        private boolean hasOverlay = true;

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder placeholder(Text placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder textColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder textShadow(boolean textShadow) {
            this.textShadow = textShadow;
            return this;
        }

        public Builder cursorColor(int cursorColor) {
            this.cursorColor = cursorColor;
            return this;
        }

        public Builder hasBackground(boolean hasBackground) {
            this.hasBackground = hasBackground;
            return this;
        }

        public Builder hasOverlay(boolean hasOverlay) {
            this.hasOverlay = hasOverlay;
            return this;
        }

        public EditBoxWidget build(TextRenderer textRenderer, int width, int height, Text message) {
            return new EditBoxWidget(textRenderer, this.x, this.y, width, height, this.placeholder, message, this.textColor, this.textShadow, this.cursorColor, this.hasBackground, this.hasOverlay);
        }
    }
}

