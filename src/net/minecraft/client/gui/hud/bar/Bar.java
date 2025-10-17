/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.bar;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

@Environment(value=EnvType.CLIENT)
public interface Bar {
    public static final int WIDTH = 182;
    public static final int HEIGHT = 5;
    public static final int VERTICAL_OFFSET = 24;
    public static final Bar EMPTY = new Bar(){

        @Override
        public void renderBar(DrawContext context, RenderTickCounter tickCounter) {
        }

        @Override
        public void renderAddons(DrawContext context, RenderTickCounter tickCounter) {
        }
    };

    default public int getCenterX(Window window) {
        return (window.getScaledWidth() - 182) / 2;
    }

    default public int getCenterY(Window window) {
        return window.getScaledHeight() - 24 - 5;
    }

    public void renderBar(DrawContext var1, RenderTickCounter var2);

    public void renderAddons(DrawContext var1, RenderTickCounter var2);

    public static void drawExperienceLevel(DrawContext context, TextRenderer textRenderer, int level) {
        MutableText lv = Text.translatable("gui.experience.level", level);
        int j = (context.getScaledWindowWidth() - textRenderer.getWidth(lv)) / 2;
        int k = context.getScaledWindowHeight() - 24 - textRenderer.fontHeight - 2;
        context.drawText(textRenderer, lv, j + 1, k, Colors.BLACK, false);
        context.drawText(textRenderer, lv, j - 1, k, Colors.BLACK, false);
        context.drawText(textRenderer, lv, j, k + 1, Colors.BLACK, false);
        context.drawText(textRenderer, lv, j, k - 1, Colors.BLACK, false);
        context.drawText(textRenderer, lv, j, k, -8323296, false);
    }
}

