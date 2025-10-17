/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import com.mojang.text2speech.Narrator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.AccessibilityOnboardingButtons;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AccessibilityOnboardingScreen
extends Screen {
    private static final Text TITLE_TEXT = Text.translatable("accessibility.onboarding.screen.title");
    private static final Text NARRATOR_PROMPT = Text.translatable("accessibility.onboarding.screen.narrator");
    private static final int field_41838 = 4;
    private static final int field_41839 = 16;
    private static final float field_60459 = 1000.0f;
    private final LogoDrawer logoDrawer;
    private final GameOptions gameOptions;
    private final boolean isNarratorUsable;
    private boolean narratorPrompted;
    private float narratorPromptTimer;
    private final Runnable onClose;
    @Nullable
    private NarratedMultilineTextWidget textWidget;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, this.yMargin(), 33);
    private float fadeTime;
    private boolean fading = true;
    private float closeTime;

    public AccessibilityOnboardingScreen(GameOptions gameOptions, Runnable onClose) {
        super(TITLE_TEXT);
        this.gameOptions = gameOptions;
        this.onClose = onClose;
        this.logoDrawer = new LogoDrawer(true);
        this.isNarratorUsable = MinecraftClient.getInstance().getNarratorManager().isActive();
    }

    @Override
    public void init() {
        DirectionalLayoutWidget lv = this.layout.addBody(DirectionalLayoutWidget.vertical());
        lv.getMainPositioner().alignHorizontalCenter().margin(4);
        this.textWidget = lv.add(new NarratedMultilineTextWidget(this.width, this.title, this.textRenderer), positioner -> positioner.margin(8));
        ClickableWidget clickableWidget = this.gameOptions.getNarrator().createWidget(this.gameOptions);
        if (clickableWidget instanceof CyclingButtonWidget) {
            CyclingButtonWidget lv2;
            this.narratorToggleButton = lv2 = (CyclingButtonWidget)clickableWidget;
            this.narratorToggleButton.active = this.isNarratorUsable;
            lv.add(this.narratorToggleButton);
        }
        lv.add(AccessibilityOnboardingButtons.createAccessibilityButton(150, button -> this.setScreen(new AccessibilityOptionsScreen(this, this.client.options)), false));
        lv.add(AccessibilityOnboardingButtons.createLanguageButton(150, button -> this.setScreen(new LanguageOptionsScreen((Screen)this, this.client.options, this.client.getLanguageManager())), false));
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.CONTINUE, button -> this.close()).build());
        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        if (this.textWidget != null) {
            this.textWidget.initMaxWidth(this.width);
        }
        this.layout.refreshPositions();
    }

    @Override
    protected void setInitialFocus() {
        if (this.isNarratorUsable && this.narratorToggleButton != null) {
            this.setInitialFocus(this.narratorToggleButton);
        } else {
            super.setInitialFocus();
        }
    }

    private int yMargin() {
        return 90;
    }

    @Override
    public void close() {
        if (this.closeTime == 0.0f) {
            this.closeTime = Util.getMeasuringTimeMs();
        }
    }

    private void setScreen(Screen screen) {
        this.saveAndRun(false, () -> this.client.setScreen(screen));
    }

    private void saveAndRun(boolean dontShowAgain, Runnable callback) {
        if (dontShowAgain) {
            this.gameOptions.setAccessibilityOnboarded();
        }
        Narrator.getNarrator().clear();
        callback.run();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        float h;
        float g;
        super.render(context, mouseX, mouseY, deltaTicks);
        this.tickNarratorPrompt();
        if (this.fadeTime == 0.0f && this.fading) {
            this.fadeTime = Util.getMeasuringTimeMs();
        }
        if (this.fadeTime > 0.0f) {
            g = ((float)Util.getMeasuringTimeMs() - this.fadeTime) / 2000.0f;
            h = 1.0f;
            if (g >= 1.0f) {
                this.fading = false;
                this.fadeTime = 0.0f;
            } else {
                g = MathHelper.clamp(g, 0.0f, 1.0f);
                h = MathHelper.clampedMap(g, 0.5f, 1.0f, 0.0f, 1.0f);
            }
            this.setWidgetAlpha(h);
        }
        if (this.closeTime > 0.0f) {
            g = 1.0f - ((float)Util.getMeasuringTimeMs() - this.closeTime) / 1000.0f;
            h = 0.0f;
            if (g <= 0.0f) {
                this.closeTime = 0.0f;
                this.saveAndRun(true, this.onClose);
            } else {
                g = MathHelper.clamp(g, 0.0f, 1.0f);
                h = MathHelper.clampedMap(g, 0.5f, 1.0f, 0.0f, 1.0f);
            }
            this.setWidgetAlpha(h);
        }
        this.logoDrawer.draw(context, this.width, 1.0f);
    }

    @Override
    protected boolean allowRotatingPanorama() {
        return false;
    }

    private void tickNarratorPrompt() {
        if (!this.narratorPrompted && this.isNarratorUsable) {
            if (this.narratorPromptTimer < 40.0f) {
                this.narratorPromptTimer += 1.0f;
            } else if (this.client.isWindowFocused()) {
                Narrator.getNarrator().say(NARRATOR_PROMPT.getString(), true, this.client.options.getSoundVolume(SoundCategory.VOICE));
                this.narratorPrompted = true;
            }
        }
    }
}

