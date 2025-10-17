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
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;

@Environment(value=EnvType.CLIENT)
public class NarratedMultilineTextWidget
extends MultilineTextWidget {
    public static final int DEFAULT_MARGIN = 4;
    private final boolean alwaysShowBorders;
    private final BackgroundRendering backgroundRendering;
    private final int margin;

    public NarratedMultilineTextWidget(int maxWidth, Text message, TextRenderer textRenderer) {
        this(maxWidth, message, textRenderer, 4);
    }

    public NarratedMultilineTextWidget(int maxWidth, Text message, TextRenderer textRenderer, int margin) {
        this(maxWidth, message, textRenderer, true, BackgroundRendering.ALWAYS, margin);
    }

    public NarratedMultilineTextWidget(int maxWidth, Text message, TextRenderer textRenderer, boolean alwaysShowBorders, BackgroundRendering backgroundRendering, int margin) {
        super(message, textRenderer);
        this.setMaxWidth(maxWidth);
        this.setCentered(true);
        this.active = true;
        this.alwaysShowBorders = alwaysShowBorders;
        this.backgroundRendering = backgroundRendering;
        this.margin = margin;
    }

    public void initMaxWidth(int baseWidth) {
        this.setMaxWidth(baseWidth - this.margin * 4);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.getMessage());
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int k = this.getX() - this.margin;
        int l = this.getY() - this.margin;
        int m = this.getWidth() + this.margin * 2;
        int n = this.getHeight() + this.margin * 2;
        int o = ColorHelper.withAlpha(this.alpha, this.alwaysShowBorders ? (this.isFocused() ? Colors.WHITE : Colors.LIGHT_GRAY) : Colors.WHITE);
        switch (this.backgroundRendering.ordinal()) {
            case 0: {
                context.fill(k + 1, l, k + m, l + n, ColorHelper.withAlpha(this.alpha, Colors.BLACK));
                break;
            }
            case 1: {
                if (!this.isFocused()) break;
                context.fill(k + 1, l, k + m, l + n, ColorHelper.withAlpha(this.alpha, Colors.BLACK));
                break;
            }
        }
        if (this.isFocused() || this.alwaysShowBorders) {
            context.drawStrokedRectangle(k, l, m, n, o);
        }
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Environment(value=EnvType.CLIENT)
    public static enum BackgroundRendering {
        ALWAYS,
        ON_FOCUS,
        NEVER;

    }
}

