/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CheckboxWidget
extends PressableWidget {
    private static final Identifier SELECTED_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("widget/checkbox_selected_highlighted");
    private static final Identifier SELECTED_TEXTURE = Identifier.ofVanilla("widget/checkbox_selected");
    private static final Identifier HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("widget/checkbox_highlighted");
    private static final Identifier TEXTURE = Identifier.ofVanilla("widget/checkbox");
    private static final int TEXT_COLOR = -2039584;
    private static final int field_47105 = 4;
    private static final int field_47106 = 8;
    private boolean checked;
    private final Callback callback;
    private final MultilineTextWidget textWidget;

    CheckboxWidget(int x, int y, int maxWidth, Text message, TextRenderer textRenderer, boolean checked, Callback callback) {
        super(x, y, 0, 0, message);
        this.width = this.calculateWidth(maxWidth, message, textRenderer);
        this.textWidget = new MultilineTextWidget(message, textRenderer).setMaxWidth(this.width).setTextColor(-2039584);
        this.height = this.calculateHeight(textRenderer);
        this.checked = checked;
        this.callback = callback;
    }

    private int calculateWidth(int max, Text text, TextRenderer textRenderer) {
        return Math.min(CheckboxWidget.calculateWidth(text, textRenderer), max);
    }

    private int calculateHeight(TextRenderer textRenderer) {
        return Math.max(CheckboxWidget.getCheckboxSize(textRenderer), this.textWidget.getHeight());
    }

    static int calculateWidth(Text text, TextRenderer textRenderer) {
        return CheckboxWidget.getCheckboxSize(textRenderer) + 4 + textRenderer.getWidth(text);
    }

    public static Builder builder(Text text, TextRenderer textRenderer) {
        return new Builder(text, textRenderer);
    }

    public static int getCheckboxSize(TextRenderer textRenderer) {
        return textRenderer.fontHeight + 8;
    }

    @Override
    public void onPress(AbstractInput input) {
        this.checked = !this.checked;
        this.callback.onValueChange(this, this.checked);
    }

    public boolean isChecked() {
        return this.checked;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, (Text)this.getNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.checkbox.usage.focused"));
            } else {
                builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        MinecraftClient lv = MinecraftClient.getInstance();
        TextRenderer lv2 = lv.textRenderer;
        Identifier lv3 = this.checked ? (this.isFocused() ? SELECTED_HIGHLIGHTED_TEXTURE : SELECTED_TEXTURE) : (this.isFocused() ? HIGHLIGHTED_TEXTURE : TEXTURE);
        int k = CheckboxWidget.getCheckboxSize(lv2);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv3, this.getX(), this.getY(), k, k, ColorHelper.getWhite(this.alpha));
        int l = this.getX() + k + 4;
        int m = this.getY() + k / 2 - this.textWidget.getHeight() / 2;
        this.textWidget.setPosition(l, m);
        this.textWidget.renderWidget(context, mouseX, mouseY, deltaTicks);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Callback {
        public static final Callback EMPTY = (checkbox, checked) -> {};

        public void onValueChange(CheckboxWidget var1, boolean var2);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Text message;
        private final TextRenderer textRenderer;
        private int maxWidth;
        private int x = 0;
        private int y = 0;
        private Callback callback = Callback.EMPTY;
        private boolean checked = false;
        @Nullable
        private SimpleOption<Boolean> option = null;
        @Nullable
        private Tooltip tooltip = null;

        Builder(Text message, TextRenderer textRenderer) {
            this.message = message;
            this.textRenderer = textRenderer;
            this.maxWidth = CheckboxWidget.calculateWidth(message, textRenderer);
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder callback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public Builder checked(boolean checked) {
            this.checked = checked;
            this.option = null;
            return this;
        }

        public Builder option(SimpleOption<Boolean> option) {
            this.option = option;
            this.checked = option.getValue();
            return this;
        }

        public Builder tooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public CheckboxWidget build() {
            Callback lv = this.option == null ? this.callback : (checkbox, checked) -> {
                this.option.setValue(checked);
                this.callback.onValueChange(checkbox, checked);
            };
            CheckboxWidget lv2 = new CheckboxWidget(this.x, this.y, this.maxWidth, this.message, this.textRenderer, this.checked, lv);
            lv2.setTooltip(this.tooltip);
            return lv2;
        }
    }
}

