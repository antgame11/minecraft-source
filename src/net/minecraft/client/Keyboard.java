/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.DebugOptionsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.GameModeSwitcherScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.Window;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.c2s.play.ChangeGameModeC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.command.VersionCommand;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WinNativeModuleUtil;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.util.FeatureDebugLogger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Keyboard {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int DEBUG_CRASH_TIME = 10000;
    private final MinecraftClient client;
    private final Clipboard clipboard = new Clipboard();
    private long debugCrashStartTime = -1L;
    private long debugCrashLastLogTime = -1L;
    private long debugCrashElapsedTime = -1L;
    private boolean switchF3State;

    public Keyboard(MinecraftClient client) {
        this.client = client;
    }

    private boolean processDebugKeys(KeyInput input) {
        switch (input.key()) {
            case 69: {
                if (this.client.player == null) {
                    return false;
                }
                boolean bl = this.client.debugHudEntryList.toggleVisibility(DebugHudEntries.CHUNK_SECTION_PATHS);
                this.debugFormattedLog("SectionPath: {0}", bl ? "shown" : "hidden");
                return true;
            }
            case 76: {
                this.client.chunkCullingEnabled = !this.client.chunkCullingEnabled;
                this.debugFormattedLog("SmartCull: {0}", this.client.chunkCullingEnabled ? "enabled" : "disabled");
                return true;
            }
            case 79: {
                if (this.client.player == null) {
                    return false;
                }
                boolean bl2 = this.client.debugHudEntryList.toggleVisibility(DebugHudEntries.CHUNK_SECTION_OCTREE);
                this.debugFormattedLog("Frustum culling Octree: {0}", bl2 ? "enabled" : "disabled");
                return true;
            }
            case 70: {
                boolean bl3 = FogRenderer.toggleFog();
                this.debugFormattedLog("Fog: {0}", bl3 ? "enabled" : "disabled");
                return true;
            }
            case 85: {
                if (input.hasShift()) {
                    this.client.worldRenderer.killFrustum();
                    this.debugFormattedLog("Killed frustum", new Object[0]);
                } else {
                    this.client.worldRenderer.captureFrustum();
                    this.debugFormattedLog("Captured frustum", new Object[0]);
                }
                return true;
            }
            case 86: {
                if (this.client.player == null) {
                    return false;
                }
                boolean bl4 = this.client.debugHudEntryList.toggleVisibility(DebugHudEntries.CHUNK_SECTION_VISIBILITY);
                this.debugFormattedLog("SectionVisibility: {0}", bl4 ? "enabled" : "disabled");
                return true;
            }
            case 87: {
                this.client.wireFrame = !this.client.wireFrame;
                this.debugFormattedLog("WireFrame: {0}", this.client.wireFrame ? "enabled" : "disabled");
                return true;
            }
        }
        return false;
    }

    private void sendMessage(Text message) {
        this.client.inGameHud.getChatHud().addMessage(message);
        this.client.getNarratorManager().narrateSystemMessage(message);
    }

    private static Text getDebugMessage(Formatting formatting, Text message) {
        return Text.empty().append(Text.translatable("debug.prefix").formatted(formatting, Formatting.BOLD)).append(ScreenTexts.SPACE).append(message);
    }

    private void debugError(Text message) {
        this.sendMessage(Keyboard.getDebugMessage(Formatting.RED, message));
    }

    private void debugLog(Text text) {
        this.sendMessage(Keyboard.getDebugMessage(Formatting.YELLOW, text));
    }

    private void debugLog(String key) {
        this.debugLog(Text.translatable(key));
    }

    private void debugFormattedLog(String pattern, Object ... args) {
        this.debugLog(Text.literal(MessageFormat.format(pattern, args)));
    }

    private boolean processF3(KeyInput arg) {
        if (this.debugCrashStartTime > 0L && this.debugCrashStartTime < Util.getMeasuringTimeMs() - 100L) {
            return true;
        }
        if (SharedConstants.HOTKEYS && this.processDebugKeys(arg)) {
            return true;
        }
        if (SharedConstants.FEATURE_COUNT) {
            switch (arg.key()) {
                case 82: {
                    FeatureDebugLogger.clear();
                    return true;
                }
                case 76: {
                    FeatureDebugLogger.dump();
                    return true;
                }
            }
        }
        switch (arg.key()) {
            case 65: {
                this.client.worldRenderer.reload();
                this.debugLog("debug.reload_chunks.message");
                return true;
            }
            case 66: {
                if (this.client.player == null || this.client.player.hasReducedDebugInfo()) {
                    return false;
                }
                boolean bl = this.client.debugHudEntryList.toggleVisibility(DebugHudEntries.ENTITY_HITBOXES);
                this.debugLog(bl ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
                return true;
            }
            case 68: {
                if (this.client.inGameHud != null) {
                    this.client.inGameHud.getChatHud().clear(false);
                }
                return true;
            }
            case 71: {
                if (this.client.player == null || this.client.player.hasReducedDebugInfo()) {
                    return false;
                }
                boolean bl2 = this.client.debugHudEntryList.toggleVisibility(DebugHudEntries.CHUNK_BORDERS);
                this.debugLog(bl2 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
                return true;
            }
            case 72: {
                this.client.options.advancedItemTooltips = !this.client.options.advancedItemTooltips;
                this.debugLog(this.client.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
                this.client.options.write();
                return true;
            }
            case 73: {
                if (this.client.player != null && !this.client.player.hasReducedDebugInfo()) {
                    this.copyLookAt(this.client.player.hasPermissionLevel(2), !arg.hasShift());
                }
                return true;
            }
            case 78: {
                if (this.client.player == null || !this.client.player.hasPermissionLevel(2)) {
                    this.debugLog("debug.creative_spectator.error");
                } else if (!this.client.player.isSpectator()) {
                    this.client.player.networkHandler.sendPacket(new ChangeGameModeC2SPacket(GameMode.SPECTATOR));
                } else {
                    GameMode lv = MoreObjects.firstNonNull(this.client.interactionManager.getPreviousGameMode(), GameMode.CREATIVE);
                    this.client.player.networkHandler.sendPacket(new ChangeGameModeC2SPacket(lv));
                }
                return true;
            }
            case 293: {
                if (!this.client.canSwitchGameMode() || !this.client.player.hasPermissionLevel(2)) {
                    this.debugLog("debug.gamemodes.error");
                } else if (!(this.client.currentScreen instanceof CreditsScreen)) {
                    this.client.setScreen(new GameModeSwitcherScreen());
                }
                return true;
            }
            case 295: {
                if (this.client.currentScreen instanceof DebugOptionsScreen) {
                    this.client.currentScreen.close();
                } else if (this.client.canCurrentScreenInterruptOtherScreen()) {
                    if (this.client.currentScreen != null) {
                        this.client.currentScreen.close();
                    }
                    this.client.setScreen(new DebugOptionsScreen());
                }
                return true;
            }
            case 80: {
                this.client.options.pauseOnLostFocus = !this.client.options.pauseOnLostFocus;
                this.client.options.write();
                this.debugLog(this.client.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
                return true;
            }
            case 81: {
                this.debugLog("debug.help.message");
                this.sendMessage(Text.translatable("debug.reload_chunks.help"));
                this.sendMessage(Text.translatable("debug.show_hitboxes.help"));
                this.sendMessage(Text.translatable("debug.copy_location.help"));
                this.sendMessage(Text.translatable("debug.clear_chat.help"));
                this.sendMessage(Text.translatable("debug.chunk_boundaries.help"));
                this.sendMessage(Text.translatable("debug.advanced_tooltips.help"));
                this.sendMessage(Text.translatable("debug.inspect.help"));
                this.sendMessage(Text.translatable("debug.profiling.help"));
                this.sendMessage(Text.translatable("debug.creative_spectator.help"));
                this.sendMessage(Text.translatable("debug.pause_focus.help"));
                this.sendMessage(Text.translatable("debug.help.help"));
                this.sendMessage(Text.translatable("debug.dump_dynamic_textures.help"));
                this.sendMessage(Text.translatable("debug.reload_resourcepacks.help"));
                this.sendMessage(Text.translatable("debug.version.help"));
                this.sendMessage(Text.translatable("debug.pause.help"));
                this.sendMessage(Text.translatable("debug.gamemodes.help"));
                this.sendMessage(Text.translatable("debug.options.help"));
                return true;
            }
            case 83: {
                Path path = this.client.runDirectory.toPath().toAbsolutePath();
                Path path2 = TextureUtil.getDebugTexturePath(path);
                this.client.getTextureManager().dumpDynamicTextures(path2);
                MutableText lv2 = Text.literal(path.relativize(path2).toString()).formatted(Formatting.UNDERLINE).styled(style -> style.withClickEvent(new ClickEvent.OpenFile(path2)));
                this.debugLog(Text.translatable("debug.dump_dynamic_textures", lv2));
                return true;
            }
            case 84: {
                this.debugLog("debug.reload_resourcepacks.message");
                this.client.reloadResources();
                return true;
            }
            case 76: {
                if (this.client.toggleDebugProfiler(this::debugLog)) {
                    this.debugLog(Text.translatable("debug.profiling.start", 10));
                }
                return true;
            }
            case 67: {
                if (this.client.player == null || this.client.player.hasReducedDebugInfo()) {
                    return false;
                }
                ClientPlayNetworkHandler lv3 = this.client.player.networkHandler;
                if (lv3 == null) {
                    return false;
                }
                this.debugLog("debug.copy_location.message");
                this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.client.player.getEntityWorld().getRegistryKey().getValue(), this.client.player.getX(), this.client.player.getY(), this.client.player.getZ(), Float.valueOf(this.client.player.getYaw()), Float.valueOf(this.client.player.getPitch())));
                return true;
            }
            case 86: {
                this.debugLog("debug.version.header");
                VersionCommand.acceptInfo(this::sendMessage);
                return true;
            }
            case 49: {
                this.client.getDebugHud().toggleRenderingChart();
                return true;
            }
            case 50: {
                this.client.getDebugHud().toggleRenderingAndTickCharts();
                return true;
            }
            case 51: {
                this.client.getDebugHud().togglePacketSizeAndPingCharts();
                return true;
            }
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void copyLookAt(boolean hasQueryPermission, boolean queryServer) {
        HitResult lv = this.client.crosshairTarget;
        if (lv == null) {
            return;
        }
        switch (lv.getType()) {
            case BLOCK: {
                BlockPos lv2 = ((BlockHitResult)lv).getBlockPos();
                World lv3 = this.client.player.getEntityWorld();
                BlockState lv4 = lv3.getBlockState(lv2);
                if (!hasQueryPermission) {
                    this.copyBlock(lv4, lv2, null);
                    this.debugLog("debug.inspect.client.block");
                    return;
                }
                if (queryServer) {
                    this.client.player.networkHandler.getDataQueryHandler().queryBlockNbt(lv2, nbt -> {
                        this.copyBlock(lv4, lv2, (NbtCompound)nbt);
                        this.debugLog("debug.inspect.server.block");
                    });
                    return;
                }
                BlockEntity lv5 = lv3.getBlockEntity(lv2);
                NbtCompound lv6 = lv5 != null ? lv5.createNbt(lv3.getRegistryManager()) : null;
                this.copyBlock(lv4, lv2, lv6);
                this.debugLog("debug.inspect.client.block");
                return;
            }
            case ENTITY: {
                Entity lv7 = ((EntityHitResult)lv).getEntity();
                Identifier lv8 = Registries.ENTITY_TYPE.getId(lv7.getType());
                if (!hasQueryPermission) {
                    this.copyEntity(lv8, lv7.getEntityPos(), null);
                    this.debugLog("debug.inspect.client.entity");
                    return;
                }
                if (queryServer) {
                    this.client.player.networkHandler.getDataQueryHandler().queryEntityNbt(lv7.getId(), nbt -> {
                        this.copyEntity(lv8, lv7.getEntityPos(), (NbtCompound)nbt);
                        this.debugLog("debug.inspect.server.entity");
                    });
                    return;
                }
                try (ErrorReporter.Logging lv9 = new ErrorReporter.Logging(lv7.getErrorReporterContext(), LOGGER);){
                    NbtWriteView lv10 = NbtWriteView.create(lv9, lv7.getRegistryManager());
                    lv7.writeData(lv10);
                    this.copyEntity(lv8, lv7.getEntityPos(), lv10.getNbt());
                }
                this.debugLog("debug.inspect.client.entity");
                return;
            }
        }
    }

    private void copyBlock(BlockState state, BlockPos pos, @Nullable NbtCompound nbt) {
        StringBuilder stringBuilder = new StringBuilder(BlockArgumentParser.stringifyBlockState(state));
        if (nbt != null) {
            stringBuilder.append(nbt);
        }
        String string = String.format(Locale.ROOT, "/setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), stringBuilder);
        this.setClipboard(string);
    }

    private void copyEntity(Identifier id, Vec3d pos, @Nullable NbtCompound nbt) {
        String string2;
        if (nbt != null) {
            nbt.remove("UUID");
            nbt.remove("Pos");
            String string = NbtHelper.toPrettyPrintedText(nbt).getString();
            string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", id, pos.x, pos.y, pos.z, string);
        } else {
            string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", id, pos.x, pos.y, pos.z);
        }
        this.setClipboard(string2);
    }

    private void onKey(long window, int action, KeyInput input) {
        int j;
        GameMenuScreen lv7;
        Screen screen;
        boolean bl4;
        Screen lv2;
        Window lv = this.client.getWindow();
        if (window != lv.getHandle()) {
            return;
        }
        this.client.getInactivityFpsLimiter().onInput();
        boolean bl = InputUtil.isKeyPressed(lv, InputUtil.GLFW_KEY_F3);
        if (this.debugCrashStartTime > 0L) {
            if (!InputUtil.isKeyPressed(lv, InputUtil.GLFW_KEY_C) || !bl) {
                this.debugCrashStartTime = -1L;
            }
        } else if (InputUtil.isKeyPressed(lv, InputUtil.GLFW_KEY_C) && bl) {
            this.switchF3State = true;
            this.debugCrashStartTime = Util.getMeasuringTimeMs();
            this.debugCrashLastLogTime = Util.getMeasuringTimeMs();
            this.debugCrashElapsedTime = 0L;
        }
        if ((lv2 = this.client.currentScreen) != null) {
            switch (input.key()) {
                case 262: 
                case 263: 
                case 264: 
                case 265: {
                    this.client.setNavigationType(GuiNavigationType.KEYBOARD_ARROW);
                    break;
                }
                case 258: {
                    this.client.setNavigationType(GuiNavigationType.KEYBOARD_TAB);
                }
            }
        }
        if (!(action != InputUtil.GLFW_RELEASE || this.client.currentScreen instanceof KeybindsScreen && ((KeybindsScreen)lv2).lastKeyCodeUpdateTime > Util.getMeasuringTimeMs() - 20L)) {
            if (this.client.options.fullscreenKey.matchesKey(input)) {
                lv.toggleFullscreen();
                boolean bl2 = lv.isFullscreen();
                this.client.options.getFullscreen().setValue(bl2);
                this.client.options.write();
                Screen screen2 = this.client.currentScreen;
                if (screen2 instanceof VideoOptionsScreen) {
                    VideoOptionsScreen lv3 = (VideoOptionsScreen)screen2;
                    lv3.updateFullscreenButtonValue(bl2);
                }
                return;
            }
            if (this.client.options.screenshotKey.matchesKey(input)) {
                if (input.hasCtrl() && SharedConstants.PANORAMA_SCREENSHOT) {
                    this.sendMessage(this.client.takePanorama(this.client.runDirectory));
                } else {
                    ScreenshotRecorder.saveScreenshot(this.client.runDirectory, this.client.getFramebuffer(), message -> this.client.execute(() -> this.sendMessage((Text)message)));
                }
                return;
            }
        }
        if (action != 0) {
            boolean bl2;
            boolean bl3 = bl2 = lv2 == null || !(lv2.getFocused() instanceof TextFieldWidget) || !((TextFieldWidget)lv2.getFocused()).isActive();
            if (bl2) {
                if (input.hasCtrl() && input.key() == InputUtil.GLFW_KEY_B && this.client.getNarratorManager().isActive() && this.client.options.getNarratorHotkey().getValue().booleanValue()) {
                    boolean bl32 = this.client.options.getNarrator().getValue() == NarratorMode.OFF;
                    this.client.options.getNarrator().setValue(NarratorMode.byId(this.client.options.getNarrator().getValue().getId() + 1));
                    this.client.options.write();
                    if (lv2 != null) {
                        lv2.refreshNarrator(bl32);
                    }
                }
                ClientPlayerEntity bl32 = this.client.player;
            }
        }
        if (lv2 != null) {
            try {
                if (action == InputUtil.GLFW_RELEASE || action == InputUtil.GLFW_REPEAT) {
                    lv2.applyKeyPressNarratorDelay();
                    if (lv2.keyPressed(input)) {
                        if (this.client.currentScreen == null) {
                            InputUtil.Key lv4 = InputUtil.fromKeyCode(input);
                            KeyBinding.setKeyPressed(lv4, false);
                        }
                        return;
                    }
                } else if (action == 0 && lv2.keyReleased(input)) {
                    return;
                }
            } catch (Throwable throwable) {
                CrashReport lv5 = CrashReport.create(throwable, "keyPressed event handler");
                lv2.addCrashReportSection(lv5);
                CrashReportSection lv6 = lv5.addElement("Key");
                lv6.add("Key", input.key());
                lv6.add("Scancode", input.scancode());
                lv6.add("Mods", input.modifiers());
                throw new CrashException(lv5);
            }
        }
        InputUtil.Key lv4 = InputUtil.fromKeyCode(input);
        boolean bl3 = this.client.currentScreen == null;
        boolean bl5 = bl4 = bl3 || (screen = this.client.currentScreen) instanceof GameMenuScreen && !(lv7 = (GameMenuScreen)screen).shouldShowMenu() || this.client.currentScreen instanceof GameModeSwitcherScreen;
        if (action == 0) {
            KeyBinding.setKeyPressed(lv4, false);
            if (input.key() == InputUtil.GLFW_KEY_F3) {
                if (this.switchF3State) {
                    this.switchF3State = false;
                } else {
                    this.client.debugHudEntryList.toggleF3Enabled();
                }
            }
            return;
        }
        boolean bl52 = false;
        if (bl4 && input.isEscape()) {
            this.client.openGameMenu(bl);
            bl52 = bl;
        } else if (bl) {
            bl52 = this.processF3(input);
        } else if (bl4 && input.key() == InputUtil.GLFW_KEY_F1) {
            this.client.options.hudHidden = !this.client.options.hudHidden;
        } else if (bl4 && input.key() == InputUtil.GLFW_KEY_F4) {
            this.client.gameRenderer.togglePostProcessorEnabled();
        }
        this.switchF3State |= bl52;
        if (this.client.getDebugHud().shouldShowRenderingChart() && !bl && (j = input.asNumber()) != -1) {
            this.client.getDebugHud().getPieChart().select(j);
        }
        if (bl3) {
            if (bl52) {
                KeyBinding.setKeyPressed(lv4, false);
            } else {
                KeyBinding.setKeyPressed(lv4, true);
                KeyBinding.onKeyPressed(lv4);
            }
        }
    }

    private void onChar(long window, CharInput input) {
        if (window != this.client.getWindow().getHandle()) {
            return;
        }
        Screen lv = this.client.currentScreen;
        if (lv == null || this.client.getOverlay() != null) {
            return;
        }
        try {
            lv.charTyped(input);
        } catch (Throwable throwable) {
            CrashReport lv2 = CrashReport.create(throwable, "charTyped event handler");
            lv.addCrashReportSection(lv2);
            CrashReportSection lv3 = lv2.addElement("Key");
            lv3.add("Codepoint", input.codepoint());
            lv3.add("Mods", input.modifiers());
            throw new CrashException(lv2);
        }
    }

    public void setup(Window window2) {
        InputUtil.setKeyboardCallbacks(window2, (handle, key, scancode, action, modifiers) -> {
            KeyInput lv = new KeyInput(key, scancode, modifiers);
            this.client.execute(() -> this.onKey(handle, action, lv));
        }, (window, codePoint, modifiers) -> {
            CharInput lv = new CharInput(codePoint, modifiers);
            this.client.execute(() -> this.onChar(window, lv));
        });
    }

    public String getClipboard() {
        return this.clipboard.get(this.client.getWindow(), (error, description) -> {
            if (error != GLFW.GLFW_FORMAT_UNAVAILABLE) {
                this.client.getWindow().logGlError(error, description);
            }
        });
    }

    public void setClipboard(String clipboard) {
        if (!clipboard.isEmpty()) {
            this.clipboard.set(this.client.getWindow(), clipboard);
        }
    }

    public void pollDebugCrash() {
        if (this.debugCrashStartTime > 0L) {
            long l = Util.getMeasuringTimeMs();
            long m = 10000L - (l - this.debugCrashStartTime);
            long n = l - this.debugCrashLastLogTime;
            if (m < 0L) {
                if (this.client.isCtrlPressed()) {
                    GlfwUtil.makeJvmCrash();
                }
                String string = "Manually triggered debug crash";
                CrashReport lv = new CrashReport("Manually triggered debug crash", new Throwable("Manually triggered debug crash"));
                CrashReportSection lv2 = lv.addElement("Manual crash details");
                WinNativeModuleUtil.addDetailTo(lv2);
                throw new CrashException(lv);
            }
            if (n >= 1000L) {
                if (this.debugCrashElapsedTime == 0L) {
                    this.debugLog("debug.crash.message");
                } else {
                    this.debugError(Text.translatable("debug.crash.warning", MathHelper.ceil((float)m / 1000.0f)));
                }
                this.debugCrashLastLogTime = l;
                ++this.debugCrashElapsedTime;
            }
        }
    }
}

