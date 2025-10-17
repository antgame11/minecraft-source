/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.debug.chart;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.log.MultiValueDebugSampleLog;

@Environment(value=EnvType.CLIENT)
public abstract class DebugChart {
    protected static final int TEXT_COLOR = -2039584;
    protected static final int field_45916 = 60;
    protected static final int field_45917 = 1;
    protected final TextRenderer textRenderer;
    protected final MultiValueDebugSampleLog log;

    protected DebugChart(TextRenderer textRenderer, MultiValueDebugSampleLog log) {
        this.textRenderer = textRenderer;
        this.log = log;
    }

    public int getWidth(int centerX) {
        return Math.min(this.log.getDimension() + 2, centerX);
    }

    public int getHeight() {
        return 60 + this.textRenderer.fontHeight;
    }

    public void render(DrawContext context, int x, int width) {
        int k = context.getScaledWindowHeight();
        context.fill(x, k - 60, x + width, k, -1873784752);
        long l = 0L;
        long m = Integer.MAX_VALUE;
        long n = Integer.MIN_VALUE;
        int o = Math.max(0, this.log.getDimension() - (width - 2));
        int p = this.log.getLength() - o;
        for (int q = 0; q < p; ++q) {
            int r = x + q + 1;
            int s = o + q;
            long t = this.get(s);
            m = Math.min(m, t);
            n = Math.max(n, t);
            l += t;
            this.drawBar(context, k, r, s);
        }
        context.drawHorizontalLine(x, x + width - 1, k - 60, Colors.WHITE);
        context.drawHorizontalLine(x, x + width - 1, k - 1, Colors.WHITE);
        context.drawVerticalLine(x, k - 60, k, Colors.WHITE);
        context.drawVerticalLine(x + width - 1, k - 60, k, Colors.WHITE);
        if (p > 0) {
            String string = this.format(m) + " min";
            String string2 = this.format((double)l / (double)p) + " avg";
            String string3 = this.format(n) + " max";
            context.drawTextWithShadow(this.textRenderer, string, x + 2, k - 60 - this.textRenderer.fontHeight, -2039584);
            context.drawCenteredTextWithShadow(this.textRenderer, string2, x + width / 2, k - 60 - this.textRenderer.fontHeight, -2039584);
            context.drawTextWithShadow(this.textRenderer, string3, x + width - this.textRenderer.getWidth(string3) - 2, k - 60 - this.textRenderer.fontHeight, -2039584);
        }
        this.renderThresholds(context, x, width, k);
    }

    protected void drawBar(DrawContext context, int y, int x, int index) {
        this.drawTotalBar(context, y, x, index);
        this.drawOverlayBar(context, y, x, index);
    }

    protected void drawTotalBar(DrawContext context, int y, int x, int index) {
        long l = this.log.get(index);
        int m = this.getHeight(l);
        int n = this.getColor(l);
        context.fill(x, y - m, x + 1, y, n);
    }

    protected void drawOverlayBar(DrawContext context, int y, int x, int index) {
    }

    protected long get(int index) {
        return this.log.get(index);
    }

    protected void renderThresholds(DrawContext context, int x, int width, int height) {
    }

    protected void drawBorderedText(DrawContext context, String string, int x, int y) {
        context.fill(x, y, x + this.textRenderer.getWidth(string) + 1, y + this.textRenderer.fontHeight, -1873784752);
        context.drawText(this.textRenderer, string, x + 1, y + 1, -2039584, false);
    }

    protected abstract String format(double var1);

    protected abstract int getHeight(double var1);

    protected abstract int getColor(long var1);

    protected int getColor(double value, double min, int minColor, double median, int medianColor, double max, int maxColor) {
        if ((value = MathHelper.clamp(value, min, max)) < median) {
            return ColorHelper.lerp((float)((value - min) / (median - min)), minColor, medianColor);
        }
        return ColorHelper.lerp((float)((value - median) / (max - median)), medianColor, maxColor);
    }
}

