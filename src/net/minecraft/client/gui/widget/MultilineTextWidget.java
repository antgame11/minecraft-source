/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import java.util.OptionalInt;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AbstractTextWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.CachedMapper;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MultilineTextWidget
extends AbstractTextWidget {
    private OptionalInt maxWidth = OptionalInt.empty();
    private OptionalInt maxRows = OptionalInt.empty();
    private final CachedMapper<CacheKey, MultilineText> cacheKeyToText = Util.cachedMapper(cacheKey -> {
        if (cacheKey.maxRows.isPresent()) {
            return MultilineText.create(textRenderer, cacheKey.maxWidth, cacheKey.maxRows.getAsInt(), cacheKey.message);
        }
        return MultilineText.create(textRenderer, cacheKey.message, cacheKey.maxWidth);
    });
    private boolean centered = false;
    private boolean allowHoverEvents = false;
    @Nullable
    private Consumer<Style> onClick = null;

    public MultilineTextWidget(Text message, TextRenderer textRenderer) {
        this(0, 0, message, textRenderer);
    }

    public MultilineTextWidget(int x, int y, Text message, TextRenderer textRenderer) {
        super(x, y, 0, 0, message, textRenderer);
        this.active = false;
    }

    @Override
    public MultilineTextWidget setTextColor(int i) {
        super.setTextColor(i);
        return this;
    }

    public MultilineTextWidget setMaxWidth(int maxWidth) {
        this.maxWidth = OptionalInt.of(maxWidth);
        return this;
    }

    public MultilineTextWidget setMaxRows(int maxRows) {
        this.maxRows = OptionalInt.of(maxRows);
        return this;
    }

    public MultilineTextWidget setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }

    public MultilineTextWidget setStyleConfig(boolean allowHoverEvents, @Nullable Consumer<Style> onClick) {
        this.allowHoverEvents = allowHoverEvents;
        this.onClick = onClick;
        return this;
    }

    @Override
    public int getWidth() {
        return this.cacheKeyToText.map(this.getCacheKey()).getMaxWidth();
    }

    @Override
    public int getHeight() {
        return this.cacheKeyToText.map(this.getCacheKey()).getLineCount() * this.getTextRenderer().fontHeight;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        MultilineText lv = this.cacheKeyToText.map(this.getCacheKey());
        int k = this.getX();
        int l = this.getY();
        int m = this.getTextRenderer().fontHeight;
        int n = this.getTextColor();
        if (this.centered) {
            int o = k + this.getWidth() / 2;
            lv.draw(context, MultilineText.Alignment.CENTER, o, l, m, true, n);
        } else {
            lv.draw(context, MultilineText.Alignment.LEFT, k, l, m, true, n);
        }
        if (this.isHovered() && this.allowHoverEvents) {
            Style lv2 = this.getStyleAt(mouseX, mouseY);
            context.drawHoverEvent(this.getTextRenderer(), lv2, mouseX, mouseY);
        }
    }

    @Nullable
    private Style getStyleAt(double mouseX, double mouseY) {
        MultilineText lv = this.cacheKeyToText.map(this.getCacheKey());
        int i = this.getX();
        int j = this.getY();
        int k = this.getTextRenderer().fontHeight;
        if (this.centered) {
            int l = i + this.getWidth() / 2;
            return lv.getStyleAt(MultilineText.Alignment.CENTER, l, j, k, mouseX, mouseY);
        }
        return lv.getStyleAt(MultilineText.Alignment.LEFT, i, j, k, mouseX, mouseY);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        Style lv;
        if (this.onClick != null && (lv = this.getStyleAt(click.x(), click.y())) != null) {
            this.onClick.accept(lv);
            return;
        }
        super.onClick(click, doubled);
    }

    private CacheKey getCacheKey() {
        return new CacheKey(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE), this.maxRows);
    }

    @Override
    public /* synthetic */ AbstractTextWidget setTextColor(int textColor) {
        return this.setTextColor(textColor);
    }

    @Environment(value=EnvType.CLIENT)
    record CacheKey(Text message, int maxWidth, OptionalInt maxRows) {
    }
}

