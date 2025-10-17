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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AbstractTextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

@Environment(value=EnvType.CLIENT)
public class TextWidget
extends AbstractTextWidget {
    private int maxWidth = 0;
    private int cachedWidth = 0;
    private boolean cachedWidthDirty = true;
    private TextOverflow textOverflow = TextOverflow.CLAMPED;

    public TextWidget(Text message, TextRenderer textRenderer) {
        this(0, 0, textRenderer.getWidth(message.asOrderedText()), textRenderer.fontHeight, message, textRenderer);
    }

    public TextWidget(int width, int height, Text message, TextRenderer textRenderer) {
        this(0, 0, width, height, message, textRenderer);
    }

    public TextWidget(int x, int y, int width, int height, Text message, TextRenderer textRenderer) {
        super(x, y, width, height, message, textRenderer);
        this.active = false;
    }

    @Override
    public TextWidget setTextColor(int textColor) {
        super.setTextColor(textColor);
        return this;
    }

    @Override
    public void setMessage(Text message) {
        super.setMessage(message);
        this.cachedWidthDirty = true;
    }

    public TextWidget setMaxWidth(int width) {
        return this.setMaxWidth(width, TextOverflow.CLAMPED);
    }

    public TextWidget setMaxWidth(int width, TextOverflow textOverflow) {
        this.maxWidth = width;
        this.textOverflow = textOverflow;
        return this;
    }

    @Override
    public int getWidth() {
        if (this.maxWidth > 0) {
            if (this.cachedWidthDirty) {
                this.cachedWidth = Math.min(this.maxWidth, this.getTextRenderer().getWidth(this.getMessage().asOrderedText()));
                this.cachedWidthDirty = false;
            }
            return this.cachedWidth;
        }
        return super.getWidth();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        boolean bl;
        Text lv = this.getMessage();
        TextRenderer lv2 = this.getTextRenderer();
        int k = this.maxWidth > 0 ? this.maxWidth : this.getWidth();
        int l = lv2.getWidth(lv);
        int m = this.getX();
        int n = this.getY() + (this.getHeight() - lv2.fontHeight) / 2;
        boolean bl2 = bl = l > k;
        if (bl) {
            switch (this.textOverflow.ordinal()) {
                case 0: {
                    context.drawTextWithShadow(lv2, this.trim(lv, k), m, n, this.getTextColor());
                    break;
                }
                case 1: {
                    this.drawScrollableText(context, lv2, 2, this.getTextColor());
                }
            }
        } else {
            context.drawTextWithShadow(lv2, lv.asOrderedText(), m, n, this.getTextColor());
        }
    }

    private OrderedText trim(Text text, int width) {
        TextRenderer lv = this.getTextRenderer();
        StringVisitable lv2 = lv.trimToWidth(text, width - lv.getWidth(ScreenTexts.ELLIPSIS));
        return Language.getInstance().reorder(StringVisitable.concat(lv2, ScreenTexts.ELLIPSIS));
    }

    @Override
    public /* synthetic */ AbstractTextWidget setTextColor(int textColor) {
        return this.setTextColor(textColor);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum TextOverflow {
        CLAMPED,
        SCROLLING;

    }
}

