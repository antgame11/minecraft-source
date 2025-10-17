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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class SplashTextRenderer {
    public static final SplashTextRenderer MERRY_X_MAS_ = new SplashTextRenderer("Merry X-mas!");
    public static final SplashTextRenderer HAPPY_NEW_YEAR_ = new SplashTextRenderer("Happy new year!");
    public static final SplashTextRenderer OOOOO_O_O_OOOOO__SPOOKY_ = new SplashTextRenderer("OOoooOOOoooo! Spooky!");
    private static final int TEXT_X = 123;
    private static final int TEXT_Y = 69;
    private final String text;

    public SplashTextRenderer(String text) {
        this.text = text;
    }

    public void render(DrawContext context, int screenWidth, TextRenderer textRenderer, float alpha) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float)screenWidth / 2.0f + 123.0f, 69.0f);
        context.getMatrices().rotate(-0.34906584f);
        float g = 1.8f - MathHelper.abs(MathHelper.sin((float)(Util.getMeasuringTimeMs() % 1000L) / 1000.0f * ((float)Math.PI * 2)) * 0.1f);
        g = g * 100.0f / (float)(textRenderer.getWidth(this.text) + 32);
        context.getMatrices().scale(g, g);
        context.drawCenteredTextWithShadow(textRenderer, this.text, 0, -8, ColorHelper.withAlpha(alpha, Colors.YELLOW));
        context.getMatrices().popMatrix();
    }
}

