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
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public abstract class ScrollableTextFieldWidget
extends ScrollableWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/text_field"), Identifier.ofVanilla("widget/text_field_highlighted"));
    private static final int field_55261 = 4;
    public static final int field_60867 = 8;
    private boolean hasBackground = true;
    private boolean hasOverlay = true;

    public ScrollableTextFieldWidget(int i, int j, int k, int l, Text arg) {
        super(i, j, k, l, arg);
    }

    public ScrollableTextFieldWidget(int x, int y, int width, int height, Text message, boolean hasBackground, boolean hasOverlay) {
        this(x, y, width, height, message);
        this.hasBackground = hasBackground;
        this.hasOverlay = hasOverlay;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        boolean bl2 = this.checkScrollbarDragged(click);
        return super.mouseClicked(click, doubled) || bl2;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        boolean bl = input.isUp();
        boolean bl2 = input.isDown();
        if (bl || bl2) {
            double d = this.getScrollY();
            this.setScrollY(this.getScrollY() + (double)(bl ? -1 : 1) * this.getDeltaYPerScroll());
            if (d != this.getScrollY()) {
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (!this.visible) {
            return;
        }
        if (this.hasBackground) {
            this.drawBox(context);
        }
        context.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0.0f, (float)(-this.getScrollY()));
        this.renderContents(context, mouseX, mouseY, deltaTicks);
        context.getMatrices().popMatrix();
        context.disableScissor();
        this.drawScrollbar(context, mouseX, mouseY);
        if (this.hasOverlay) {
            this.renderOverlay(context);
        }
    }

    protected void renderOverlay(DrawContext context) {
    }

    protected int getTextMargin() {
        return 4;
    }

    protected int getPadding() {
        return this.getTextMargin() * 2;
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return this.active && this.visible && d >= (double)this.getX() && e >= (double)this.getY() && d < (double)(this.getRight() + 6) && e < (double)this.getBottom();
    }

    @Override
    protected int getScrollbarX() {
        return this.getRight();
    }

    @Override
    protected int getContentsHeightWithPadding() {
        return this.getContentsHeight() + this.getPadding();
    }

    protected void drawBox(DrawContext context) {
        this.draw(context, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    protected void draw(DrawContext context, int x, int y, int width, int height) {
        Identifier lv = TEXTURES.get(this.isInteractable(), this.isFocused());
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv, x, y, width, height);
    }

    protected boolean isVisible(int textTop, int textBottom) {
        return (double)textBottom - this.getScrollY() >= (double)this.getY() && (double)textTop - this.getScrollY() <= (double)(this.getY() + this.height);
    }

    protected abstract int getContentsHeight();

    protected abstract void renderContents(DrawContext var1, int var2, int var3, float var4);

    protected int getTextX() {
        return this.getX() + this.getTextMargin();
    }

    protected int getTextY() {
        return this.getY() + this.getTextMargin();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }
}

