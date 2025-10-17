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
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

@Environment(value=EnvType.CLIENT)
public abstract class PressableWidget
extends ClickableWidget {
    protected static final int field_43050 = 2;
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/button"), Identifier.ofVanilla("widget/button_disabled"), Identifier.ofVanilla("widget/button_highlighted"));

    public PressableWidget(int i, int j, int k, int l, Text arg) {
        super(i, j, k, l, arg);
    }

    public abstract void onPress(AbstractInput var1);

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        MinecraftClient lv = MinecraftClient.getInstance();
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURES.get(this.active, this.isSelected()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ColorHelper.getWhite(this.alpha));
        int k = ColorHelper.withAlpha(this.alpha, this.active ? Colors.WHITE : Colors.LIGHT_GRAY);
        this.drawMessage(context, lv.textRenderer, k);
        if (this.isHovered()) {
            context.setCursor(this.isInteractable() ? StandardCursors.POINTING_HAND : StandardCursors.NOT_ALLOWED);
        }
    }

    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
        this.drawScrollableText(context, textRenderer, 2, color);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        this.onPress(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!this.isInteractable()) {
            return false;
        }
        if (input.isEnterOrSpace()) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onPress(input);
            return true;
        }
        return false;
    }
}

