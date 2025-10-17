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
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public abstract class SliderWidget
extends ClickableWidget {
    private static final Identifier TEXTURE = Identifier.ofVanilla("widget/slider");
    private static final Identifier HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("widget/slider_highlighted");
    private static final Identifier HANDLE_TEXTURE = Identifier.ofVanilla("widget/slider_handle");
    private static final Identifier HANDLE_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("widget/slider_handle_highlighted");
    protected static final int field_43054 = 2;
    public static final int field_60708 = 20;
    private static final int field_41790 = 8;
    private static final int field_41789 = 4;
    protected double value;
    private boolean sliderFocused;
    private boolean field_62464;

    public SliderWidget(int x, int y, int width, int height, Text text, double value) {
        super(x, y, width, height, text);
        this.value = value;
    }

    private Identifier getTexture() {
        if (this.isInteractable() && this.isFocused() && !this.sliderFocused) {
            return HIGHLIGHTED_TEXTURE;
        }
        return TEXTURE;
    }

    private Identifier getHandleTexture() {
        if (this.isInteractable() && (this.hovered || this.sliderFocused)) {
            return HANDLE_HIGHLIGHTED_TEXTURE;
        }
        return HANDLE_TEXTURE;
    }

    @Override
    protected MutableText getNarrationMessage() {
        return Text.translatable("gui.narrate.slider", this.getMessage());
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, (Text)this.getNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                if (this.sliderFocused) {
                    builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.slider.usage.focused"));
                } else {
                    builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.slider.usage.focused.keyboard_cannot_change_value"));
                }
            } else {
                builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.slider.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        MinecraftClient lv = MinecraftClient.getInstance();
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.getTexture(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ColorHelper.getWhite(this.alpha));
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.getHandleTexture(), this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 8, this.getHeight(), ColorHelper.getWhite(this.alpha));
        int k = ColorHelper.withAlpha(this.alpha, this.active ? Colors.WHITE : Colors.LIGHT_GRAY);
        this.drawScrollableText(context, lv.textRenderer, 2, k);
        if (this.isHovered()) {
            context.setCursor(this.field_62464 ? StandardCursors.RESIZE_EW : StandardCursors.POINTING_HAND);
        }
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        this.field_62464 = this.active;
        this.setValueFromMouse(click);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.sliderFocused = false;
            return;
        }
        GuiNavigationType lv = MinecraftClient.getInstance().getNavigationType();
        if (lv == GuiNavigationType.MOUSE || lv == GuiNavigationType.KEYBOARD_TAB) {
            this.sliderFocused = true;
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEnterOrSpace()) {
            this.sliderFocused = !this.sliderFocused;
            return true;
        }
        if (this.sliderFocused) {
            boolean bl = input.isLeft();
            boolean bl2 = input.isRight();
            if (bl || bl2) {
                float f = bl ? -1.0f : 1.0f;
                this.setValue(this.value + (double)(f / (float)(this.width - 8)));
                return true;
            }
        }
        return false;
    }

    private void setValueFromMouse(Click arg) {
        this.setValue((arg.x() - (double)(this.getX() + 4)) / (double)(this.width - 8));
    }

    private void setValue(double value) {
        double e = this.value;
        this.value = MathHelper.clamp(value, 0.0, 1.0);
        if (e != this.value) {
            this.applyValue();
        }
        this.updateMessage();
    }

    @Override
    protected void onDrag(Click click, double offsetX, double offsetY) {
        this.setValueFromMouse(click);
        super.onDrag(click, offsetX, offsetY);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void onRelease(Click click) {
        this.field_62464 = false;
        super.playDownSound(MinecraftClient.getInstance().getSoundManager());
    }

    protected abstract void updateMessage();

    protected abstract void applyValue();
}

