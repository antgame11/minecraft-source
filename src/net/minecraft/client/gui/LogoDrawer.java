/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class LogoDrawer {
    public static final Identifier LOGO_TEXTURE = Identifier.ofVanilla("textures/gui/title/minecraft.png");
    public static final Identifier MINCERAFT_TEXTURE = Identifier.ofVanilla("textures/gui/title/minceraft.png");
    public static final Identifier EDITION_TEXTURE = Identifier.ofVanilla("textures/gui/title/edition.png");
    public static final int LOGO_REGION_WIDTH = 256;
    public static final int LOGO_REGION_HEIGHT = 44;
    private static final int LOGO_TEXTURE_WIDTH = 256;
    private static final int LOGO_TEXTURE_HEIGHT = 64;
    private static final int EDITION_REGION_WIDTH = 128;
    private static final int EDITION_REGION_HEIGHT = 14;
    private static final int EDITION_TEXTURE_WIDTH = 128;
    private static final int EDITION_TEXTURE_HEIGHT = 16;
    public static final int LOGO_BASE_Y = 30;
    private static final int LOGO_AND_EDITION_OVERLAP = 7;
    private final boolean minceraft = (double)Random.create().nextFloat() < 1.0E-4;
    private final boolean ignoreAlpha;

    public LogoDrawer(boolean ignoreAlpha) {
        this.ignoreAlpha = ignoreAlpha;
    }

    public void draw(DrawContext context, int screenWidth, float alpha) {
        this.draw(context, screenWidth, alpha, 30);
    }

    public void draw(DrawContext context, int screenWidth, float alpha, int y) {
        int k = screenWidth / 2 - 128;
        float g = this.ignoreAlpha ? 1.0f : alpha;
        int l = ColorHelper.getWhite(g);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, this.minceraft ? MINCERAFT_TEXTURE : LOGO_TEXTURE, k, y, 0.0f, 0.0f, 256, 44, 256, 64, l);
        int m = screenWidth / 2 - 64;
        int n = y + 44 - 7;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, EDITION_TEXTURE, m, n, 0.0f, 0.0f, 128, 14, 128, 16, l);
    }

    public boolean shouldIgnoreAlpha() {
        return this.ignoreAlpha;
    }
}

