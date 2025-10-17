/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.AccessibilityOnboardingButtons;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TitleScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text NARRATOR_SCREEN_TITLE = Text.translatable("narrator.screen.title");
    private static final Text COPYRIGHT = Text.translatable("title.credits");
    private static final String DEMO_WORLD_NAME = "Demo_World";
    @Nullable
    private SplashTextRenderer splashText;
    @Nullable
    private RealmsNotificationsScreen realmsNotificationGui;
    private boolean doBackgroundFade;
    private long backgroundFadeStart;
    private final LogoDrawer logoDrawer;

    public TitleScreen() {
        this(false);
    }

    public TitleScreen(boolean doBackgroundFade) {
        this(doBackgroundFade, null);
    }

    public TitleScreen(boolean doBackgroundFade, @Nullable LogoDrawer logoDrawer) {
        super(NARRATOR_SCREEN_TITLE);
        this.doBackgroundFade = doBackgroundFade;
        this.logoDrawer = Objects.requireNonNullElseGet(logoDrawer, () -> new LogoDrawer(false));
    }

    private boolean isRealmsNotificationsGuiDisplayed() {
        return this.realmsNotificationGui != null;
    }

    @Override
    public void tick() {
        if (this.isRealmsNotificationsGuiDisplayed()) {
            this.realmsNotificationGui.tick();
        }
    }

    public static void registerTextures(TextureManager textureManager) {
        textureManager.registerTexture(LogoDrawer.LOGO_TEXTURE);
        textureManager.registerTexture(LogoDrawer.EDITION_TEXTURE);
        textureManager.registerTexture(RotatingCubeMapRenderer.OVERLAY_TEXTURE);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        if (this.splashText == null) {
            this.splashText = this.client.getSplashTextLoader().get();
        }
        int i = this.textRenderer.getWidth(COPYRIGHT);
        int j = this.width - i - 2;
        int k = 24;
        int l = this.height / 4 + 48;
        l = this.client.isDemo() ? this.addDemoWidgets(l, 24) : this.addNormalWidgets(l, 24);
        l = this.addDevelopmentWidgets(l, 24);
        TextIconButtonWidget lv = this.addDrawableChild(AccessibilityOnboardingButtons.createLanguageButton(20, button -> this.client.setScreen(new LanguageOptionsScreen((Screen)this, this.client.options, this.client.getLanguageManager())), true));
        lv.setPosition(this.width / 2 - 124, l += 36);
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.options"), button -> this.client.setScreen(new OptionsScreen(this, this.client.options))).dimensions(this.width / 2 - 100, l, 98, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.quit"), button -> this.client.scheduleStop()).dimensions(this.width / 2 + 2, l, 98, 20).build());
        TextIconButtonWidget lv2 = this.addDrawableChild(AccessibilityOnboardingButtons.createAccessibilityButton(20, button -> this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options)), true));
        lv2.setPosition(this.width / 2 + 104, l);
        this.addDrawableChild(new PressableTextWidget(j, this.height - 10, i, 10, COPYRIGHT, button -> this.client.setScreen(new CreditsAndAttributionScreen(this)), this.textRenderer));
        if (this.realmsNotificationGui == null) {
            this.realmsNotificationGui = new RealmsNotificationsScreen();
        }
        if (this.isRealmsNotificationsGuiDisplayed()) {
            this.realmsNotificationGui.init(this.client, this.width, this.height);
        }
    }

    private int addDevelopmentWidgets(int y, int spacingY) {
        if (SharedConstants.isDevelopment) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Create Test World"), button -> CreateWorldScreen.showTestWorld(this.client, () -> this.client.setScreen(this))).dimensions(this.width / 2 - 100, y += spacingY, 200, 20).build());
        }
        return y;
    }

    private int addNormalWidgets(int y, int spacingY) {
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.singleplayer"), button -> this.client.setScreen(new SelectWorldScreen(this))).dimensions(this.width / 2 - 100, y, 200, 20).build());
        Text lv = this.getMultiplayerDisabledText();
        boolean bl = lv == null;
        Tooltip lv2 = lv != null ? Tooltip.of(lv) : null;
        y += spacingY;
        this.addDrawableChild(ButtonWidget.builder((Text)Text.translatable((String)"menu.multiplayer"), (ButtonWidget.PressAction)(ButtonWidget.PressAction)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/widget/ButtonWidget;)V, onMultiplayerButtonPressed(net.minecraft.client.gui.widget.ButtonWidget ), (Lnet/minecraft/client/gui/widget/ButtonWidget;)V)((TitleScreen)this)).dimensions((int)(this.width / 2 - 100), (int)v0, (int)200, (int)20).tooltip((Tooltip)lv2).build()).active = bl;
        this.addDrawableChild(ButtonWidget.builder((Text)Text.translatable((String)"menu.online"), (ButtonWidget.PressAction)(ButtonWidget.PressAction)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/widget/ButtonWidget;)V, method_55814(net.minecraft.client.gui.widget.ButtonWidget ), (Lnet/minecraft/client/gui/widget/ButtonWidget;)V)((TitleScreen)this)).dimensions((int)(this.width / 2 - 100), (int)v1, (int)200, (int)20).tooltip((Tooltip)lv2).build()).active = bl;
        return y += spacingY;
    }

    @Nullable
    private Text getMultiplayerDisabledText() {
        if (this.client.isMultiplayerEnabled()) {
            return null;
        }
        if (this.client.isUsernameBanned()) {
            return Text.translatable("title.multiplayer.disabled.banned.name");
        }
        BanDetails banDetails = this.client.getMultiplayerBanDetails();
        if (banDetails != null) {
            if (banDetails.expires() != null) {
                return Text.translatable("title.multiplayer.disabled.banned.temporary");
            }
            return Text.translatable("title.multiplayer.disabled.banned.permanent");
        }
        return Text.translatable("title.multiplayer.disabled");
    }

    private int addDemoWidgets(int y, int spacingY) {
        boolean bl = this.canReadDemoWorldData();
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.playdemo"), button -> {
            if (bl) {
                this.client.createIntegratedServerLoader().start(DEMO_WORLD_NAME, () -> this.client.setScreen(this));
            } else {
                this.client.createIntegratedServerLoader().createAndStart(DEMO_WORLD_NAME, MinecraftServer.DEMO_LEVEL_INFO, GeneratorOptions.DEMO_OPTIONS, WorldPresets::createDemoOptions, this);
            }
        }).dimensions(this.width / 2 - 100, y, 200, 20).build());
        ButtonWidget lv = this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.resetdemo"), button -> {
            LevelStorage lv = this.client.getLevelStorage();
            try (LevelStorage.Session lv2 = lv.createSessionWithoutSymlinkCheck(DEMO_WORLD_NAME);){
                if (lv2.levelDatExists()) {
                    this.client.setScreen(new ConfirmScreen(this::onDemoDeletionConfirmed, Text.translatable("selectWorld.deleteQuestion"), Text.translatable("selectWorld.deleteWarning", MinecraftServer.DEMO_LEVEL_INFO.getLevelName()), Text.translatable("selectWorld.deleteButton"), ScreenTexts.CANCEL));
                }
            } catch (IOException iOException) {
                SystemToast.addWorldAccessFailureToast(this.client, DEMO_WORLD_NAME);
                LOGGER.warn("Failed to access demo world", iOException);
            }
        }).dimensions(this.width / 2 - 100, y += spacingY, 200, 20).build());
        lv.active = bl;
        return y;
    }

    private boolean canReadDemoWorldData() {
        boolean bl;
        block8: {
            LevelStorage.Session lv = this.client.getLevelStorage().createSessionWithoutSymlinkCheck(DEMO_WORLD_NAME);
            try {
                bl = lv.levelDatExists();
                if (lv == null) break block8;
            } catch (Throwable throwable) {
                try {
                    if (lv != null) {
                        try {
                            lv.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (IOException iOException) {
                    SystemToast.addWorldAccessFailureToast(this.client, DEMO_WORLD_NAME);
                    LOGGER.warn("Failed to read demo world data", iOException);
                    return false;
                }
            }
            lv.close();
        }
        return bl;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.backgroundFadeStart == 0L && this.doBackgroundFade) {
            this.backgroundFadeStart = Util.getMeasuringTimeMs();
        }
        float g = 1.0f;
        if (this.doBackgroundFade) {
            float h = (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 2000.0f;
            if (h > 1.0f) {
                this.doBackgroundFade = false;
            } else {
                h = MathHelper.clamp(h, 0.0f, 1.0f);
                g = MathHelper.clampedMap(h, 0.5f, 1.0f, 0.0f, 1.0f);
            }
            this.setWidgetAlpha(g);
        }
        this.renderPanoramaBackground(context, deltaTicks);
        super.render(context, mouseX, mouseY, deltaTicks);
        this.logoDrawer.draw(context, this.width, this.logoDrawer.shouldIgnoreAlpha() ? 1.0f : g);
        if (this.splashText != null && !this.client.options.getHideSplashTexts().getValue().booleanValue()) {
            this.splashText.render(context, this.width, this.textRenderer, g);
        }
        String string = "Minecraft " + SharedConstants.getGameVersion().name();
        string = this.client.isDemo() ? string + " Demo" : string + (String)("release".equalsIgnoreCase(this.client.getVersionType()) ? "" : "/" + this.client.getVersionType());
        if (MinecraftClient.getModStatus().isModded()) {
            string = string + I18n.translate("menu.modded", new Object[0]);
        }
        context.drawTextWithShadow(this.textRenderer, string, 2, this.height - 10, ColorHelper.withAlpha(g, Colors.WHITE));
        if (this.isRealmsNotificationsGuiDisplayed() && g >= 1.0f) {
            this.realmsNotificationGui.render(context, mouseX, mouseY, deltaTicks);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) {
            return true;
        }
        return this.isRealmsNotificationsGuiDisplayed() && this.realmsNotificationGui.mouseClicked(click, doubled);
    }

    @Override
    public void removed() {
        if (this.realmsNotificationGui != null) {
            this.realmsNotificationGui.removed();
        }
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        if (this.realmsNotificationGui != null) {
            this.realmsNotificationGui.onDisplayed();
        }
    }

    private void onDemoDeletionConfirmed(boolean delete) {
        if (delete) {
            try (LevelStorage.Session lv = this.client.getLevelStorage().createSessionWithoutSymlinkCheck(DEMO_WORLD_NAME);){
                lv.deleteSessionLock();
            } catch (IOException iOException) {
                SystemToast.addWorldDeleteFailureToast(this.client, DEMO_WORLD_NAME);
                LOGGER.warn("Failed to delete demo world", iOException);
            }
        }
        this.client.setScreen(this);
    }

    @Override
    public boolean canInterruptOtherScreen() {
        return true;
    }

    private /* synthetic */ void method_55814(ButtonWidget button) {
        this.client.setScreen(new RealmsMainScreen(this));
    }

    private /* synthetic */ void onMultiplayerButtonPressed(ButtonWidget button) {
        Screen lv = this.client.options.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this);
        this.client.setScreen(lv);
    }
}

