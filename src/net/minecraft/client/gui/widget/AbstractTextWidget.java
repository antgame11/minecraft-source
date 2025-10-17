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
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractTextWidget
extends ClickableWidget {
    private final TextRenderer textRenderer;
    private int textColor = -1;

    public AbstractTextWidget(int x, int y, int width, int height, Text message, TextRenderer textRenderer) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    public AbstractTextWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    protected final TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    protected final int getTextColor() {
        return ColorHelper.withAlpha(this.alpha, this.textColor);
    }

    @Override
    public void setMessage(Text message) {
        super.setMessage(message);
        this.setWidth(this.getTextRenderer().getWidth(message.asOrderedText()));
    }
}

