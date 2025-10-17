/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ConfirmScreen
extends Screen {
    private final Text message;
    protected DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical().spacing(8);
    protected Text yesText;
    protected Text noText;
    @Nullable
    protected ButtonWidget yesButton;
    @Nullable
    protected ButtonWidget noButton;
    private int buttonEnableTimer;
    protected final BooleanConsumer callback;

    public ConfirmScreen(BooleanConsumer callback, Text title, Text message) {
        this(callback, title, message, ScreenTexts.YES, ScreenTexts.NO);
    }

    public ConfirmScreen(BooleanConsumer callback, Text title, Text message, Text yesText, Text noText) {
        super(title);
        this.callback = callback;
        this.message = message;
        this.yesText = yesText;
        this.noText = noText;
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(super.getNarratedTitle(), this.message);
    }

    @Override
    protected void init() {
        super.init();
        this.layout.getMainPositioner().alignHorizontalCenter();
        this.layout.add(new TextWidget(this.title, this.textRenderer));
        this.layout.add(new MultilineTextWidget(this.message, this.textRenderer).setMaxWidth(this.width - 50).setMaxRows(15).setCentered(true));
        this.initExtras();
        DirectionalLayoutWidget lv = this.layout.add(DirectionalLayoutWidget.horizontal().spacing(4));
        lv.getMainPositioner().marginTop(16);
        this.addButtons(lv);
        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(this.layout, this.getNavigationFocus());
    }

    protected void initExtras() {
    }

    protected void addButtons(DirectionalLayoutWidget layout) {
        this.yesButton = layout.add(ButtonWidget.builder(this.yesText, button -> this.callback.accept(true)).build());
        this.noButton = layout.add(ButtonWidget.builder(this.noText, button -> this.callback.accept(false)).build());
    }

    public void disableButtons(int ticks) {
        this.buttonEnableTimer = ticks;
        this.yesButton.active = false;
        this.noButton.active = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (--this.buttonEnableTimer == 0) {
            this.yesButton.active = true;
            this.noButton.active = true;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (this.buttonEnableTimer <= 0 && input.key() == InputUtil.GLFW_KEY_ESCAPE) {
            this.callback.accept(false);
            return true;
        }
        return super.keyPressed(input);
    }
}

