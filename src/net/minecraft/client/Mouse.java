/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client;

import com.mojang.logging.LogUtils;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.input.Scroller;
import net.minecraft.client.input.SystemKeycodes;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Smoother;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWDropCallback;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Mouse {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final long field_61505 = 250L;
    private final MinecraftClient client;
    private boolean leftButtonClicked;
    private boolean middleButtonClicked;
    private boolean rightButtonClicked;
    private double x;
    private double y;
    protected long lastMouseButtonClickTimeMs;
    protected int lastMouseButton;
    private int controlLeftClicks;
    @Nullable
    private MouseInput activeButton = null;
    private boolean hasResolutionChanged = true;
    private int touchHoldTime;
    private double glfwTime;
    private final Smoother cursorXSmoother = new Smoother();
    private final Smoother cursorYSmoother = new Smoother();
    private double cursorDeltaX;
    private double cursorDeltaY;
    private final Scroller scroller;
    private double lastTickTime = Double.MIN_VALUE;
    private boolean cursorLocked;

    public Mouse(MinecraftClient client) {
        this.client = client;
        this.scroller = new Scroller();
    }

    private void onMouseButton(long window, MouseInput input, int action) {
        MouseInput lv2;
        boolean bl;
        block25: {
            Window lv = this.client.getWindow();
            if (window != lv.getHandle()) {
                return;
            }
            this.client.getInactivityFpsLimiter().onInput();
            if (this.client.currentScreen != null) {
                this.client.setNavigationType(GuiNavigationType.MOUSE);
            }
            bl = action == InputUtil.GLFW_RELEASE;
            lv2 = this.modifyMouseInput(input, bl);
            if (bl) {
                if (this.client.options.getTouchscreen().getValue().booleanValue() && this.touchHoldTime++ > 0) {
                    return;
                }
                this.activeButton = lv2;
                this.glfwTime = GlfwUtil.getTime();
            } else if (this.activeButton != null) {
                if (this.client.options.getTouchscreen().getValue().booleanValue() && --this.touchHoldTime > 0) {
                    return;
                }
                this.activeButton = null;
            }
            if (this.client.getOverlay() == null) {
                if (this.client.currentScreen == null) {
                    if (!this.cursorLocked && bl) {
                        this.lockCursor();
                    }
                } else {
                    double d = this.getScaledX(lv);
                    double e = this.getScaledY(lv);
                    Screen lv3 = this.client.currentScreen;
                    Click lv4 = new Click(d, e, lv2);
                    if (bl) {
                        lv3.applyMousePressScrollNarratorDelay();
                        try {
                            boolean bl2;
                            long m = Util.getMeasuringTimeMs();
                            boolean bl3 = bl2 = m - this.lastMouseButtonClickTimeMs < 250L && this.lastMouseButton == lv4.button();
                            if (lv3.mouseClicked(lv4, bl2)) {
                                this.lastMouseButtonClickTimeMs = m;
                                this.lastMouseButton = lv2.button();
                                return;
                            }
                            break block25;
                        } catch (Throwable throwable) {
                            CrashReport lv5 = CrashReport.create(throwable, "mouseClicked event handler");
                            lv3.addCrashReportSection(lv5);
                            CrashReportSection lv6 = lv5.addElement("Mouse");
                            this.addCrashReportSection(lv6, lv);
                            lv6.add("Button", lv4.button());
                            throw new CrashException(lv5);
                        }
                    }
                    try {
                        if (lv3.mouseReleased(lv4)) {
                            return;
                        }
                    } catch (Throwable throwable) {
                        CrashReport lv5 = CrashReport.create(throwable, "mouseReleased event handler");
                        lv3.addCrashReportSection(lv5);
                        CrashReportSection lv6 = lv5.addElement("Mouse");
                        this.addCrashReportSection(lv6, lv);
                        lv6.add("Button", lv4.button());
                        throw new CrashException(lv5);
                    }
                }
            }
        }
        if (this.client.currentScreen == null && this.client.getOverlay() == null) {
            if (lv2.button() == 0) {
                this.leftButtonClicked = bl;
            } else if (lv2.button() == InputUtil.GLFW_MOUSE_BUTTON_MIDDLE) {
                this.middleButtonClicked = bl;
            } else if (lv2.button() == InputUtil.GLFW_MOUSE_BUTTON_RIGHT) {
                this.rightButtonClicked = bl;
            }
            InputUtil.Key lv7 = InputUtil.Type.MOUSE.createFromCode(lv2.button());
            KeyBinding.setKeyPressed(lv7, bl);
            if (bl) {
                KeyBinding.onKeyPressed(lv7);
            }
        }
    }

    private MouseInput modifyMouseInput(MouseInput input, boolean pressed) {
        if (SystemKeycodes.USE_LONG_LEFT_PRESS && input.button() == 0) {
            if (pressed) {
                if ((input.modifiers() & 2) == 2) {
                    ++this.controlLeftClicks;
                    return new MouseInput(1, input.modifiers());
                }
            } else if (this.controlLeftClicks > 0) {
                --this.controlLeftClicks;
                return new MouseInput(1, input.modifiers());
            }
        }
        return input;
    }

    public void addCrashReportSection(CrashReportSection section, Window window) {
        section.add("Mouse location", () -> String.format(Locale.ROOT, "Scaled: (%f, %f). Absolute: (%f, %f)", Mouse.scaleX(window, this.x), Mouse.scaleY(window, this.y), this.x, this.y));
        section.add("Screen size", () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f", window.getScaledWidth(), window.getScaledHeight(), window.getFramebufferWidth(), window.getFramebufferHeight(), window.getScaleFactor()));
    }

    private void onMouseScroll(long window, double horizontal, double vertical) {
        if (window == this.client.getWindow().getHandle()) {
            this.client.getInactivityFpsLimiter().onInput();
            boolean bl = this.client.options.getDiscreteMouseScroll().getValue();
            double f = this.client.options.getMouseWheelSensitivity().getValue();
            double g = (bl ? Math.signum(horizontal) : horizontal) * f;
            double h = (bl ? Math.signum(vertical) : vertical) * f;
            if (this.client.getOverlay() == null) {
                if (this.client.currentScreen != null) {
                    double i = this.getScaledX(this.client.getWindow());
                    double j = this.getScaledY(this.client.getWindow());
                    this.client.currentScreen.mouseScrolled(i, j, g, h);
                    this.client.currentScreen.applyMousePressScrollNarratorDelay();
                } else if (this.client.player != null) {
                    int k;
                    Vector2i vector2i = this.scroller.update(g, h);
                    if (vector2i.x == 0 && vector2i.y == 0) {
                        return;
                    }
                    int n = k = vector2i.y == 0 ? -vector2i.x : vector2i.y;
                    if (this.client.player.isSpectator()) {
                        if (this.client.inGameHud.getSpectatorHud().isOpen()) {
                            this.client.inGameHud.getSpectatorHud().cycleSlot(-k);
                        } else {
                            float m = MathHelper.clamp(this.client.player.getAbilities().getFlySpeed() + (float)vector2i.y * 0.005f, 0.0f, 0.2f);
                            this.client.player.getAbilities().setFlySpeed(m);
                        }
                    } else {
                        PlayerInventory lv = this.client.player.getInventory();
                        lv.setSelectedSlot(Scroller.scrollCycling(k, lv.getSelectedSlot(), PlayerInventory.getHotbarSize()));
                    }
                }
            }
        }
    }

    private void onFilesDropped(long window, List<Path> paths, int invalidFilesCount) {
        this.client.getInactivityFpsLimiter().onInput();
        if (this.client.currentScreen != null) {
            this.client.currentScreen.onFilesDropped(paths);
        }
        if (invalidFilesCount > 0) {
            SystemToast.addFileDropFailure(this.client, invalidFilesCount);
        }
    }

    public void setup(Window window2) {
        InputUtil.setMouseCallbacks(window2, (window, x, y) -> this.client.execute(() -> this.onCursorPos(window, x, y)), (window, button, action, modifiers) -> {
            MouseInput lv = new MouseInput(button, modifiers);
            this.client.execute(() -> this.onMouseButton(window, lv, action));
        }, (window, offsetX, offsetY) -> this.client.execute(() -> this.onMouseScroll(window, offsetX, offsetY)), (window, count, names) -> {
            int k;
            ArrayList<Path> list = new ArrayList<Path>(count);
            int j = 0;
            for (k = 0; k < count; ++k) {
                String string = GLFWDropCallback.getName(names, k);
                try {
                    list.add(Paths.get(string, new String[0]));
                    continue;
                } catch (InvalidPathException invalidPathException) {
                    ++j;
                    LOGGER.error("Failed to parse path '{}'", (Object)string, (Object)invalidPathException);
                }
            }
            if (!list.isEmpty()) {
                k = j;
                this.client.execute(() -> this.onFilesDropped(window, list, k));
            }
        });
    }

    private void onCursorPos(long window, double x, double y) {
        if (window != this.client.getWindow().getHandle()) {
            return;
        }
        if (this.hasResolutionChanged) {
            this.x = x;
            this.y = y;
            this.hasResolutionChanged = false;
            return;
        }
        if (this.client.isWindowFocused()) {
            this.cursorDeltaX += x - this.x;
            this.cursorDeltaY += y - this.y;
        }
        this.x = x;
        this.y = y;
    }

    public void tick() {
        double d = GlfwUtil.getTime();
        double e = d - this.lastTickTime;
        this.lastTickTime = d;
        if (this.client.isWindowFocused()) {
            boolean bl;
            Screen lv = this.client.currentScreen;
            boolean bl2 = bl = this.cursorDeltaX != 0.0 || this.cursorDeltaY != 0.0;
            if (bl) {
                this.client.getInactivityFpsLimiter().onInput();
            }
            if (lv != null && this.client.getOverlay() == null && bl) {
                Window lv2 = this.client.getWindow();
                double f = this.getScaledX(lv2);
                double g = this.getScaledY(lv2);
                try {
                    lv.mouseMoved(f, g);
                } catch (Throwable throwable) {
                    CrashReport lv3 = CrashReport.create(throwable, "mouseMoved event handler");
                    lv.addCrashReportSection(lv3);
                    CrashReportSection lv4 = lv3.addElement("Mouse");
                    this.addCrashReportSection(lv4, lv2);
                    throw new CrashException(lv3);
                }
                if (this.activeButton != null && this.glfwTime > 0.0) {
                    double h = Mouse.scaleX(lv2, this.cursorDeltaX);
                    double i = Mouse.scaleY(lv2, this.cursorDeltaY);
                    try {
                        lv.mouseDragged(new Click(f, g, this.activeButton), h, i);
                    } catch (Throwable throwable2) {
                        CrashReport lv5 = CrashReport.create(throwable2, "mouseDragged event handler");
                        lv.addCrashReportSection(lv5);
                        CrashReportSection lv6 = lv5.addElement("Mouse");
                        this.addCrashReportSection(lv6, lv2);
                        throw new CrashException(lv5);
                    }
                }
                lv.applyMouseMoveNarratorDelay();
            }
            if (this.isCursorLocked() && this.client.player != null) {
                this.updateMouse(e);
            }
        }
        this.cursorDeltaX = 0.0;
        this.cursorDeltaY = 0.0;
    }

    public static double scaleX(Window window, double x) {
        return x * (double)window.getScaledWidth() / (double)window.getWidth();
    }

    public double getScaledX(Window window) {
        return Mouse.scaleX(window, this.x);
    }

    public static double scaleY(Window window, double y) {
        return y * (double)window.getScaledHeight() / (double)window.getHeight();
    }

    public double getScaledY(Window window) {
        return Mouse.scaleY(window, this.y);
    }

    private void updateMouse(double timeDelta) {
        double k;
        double j;
        double e = this.client.options.getMouseSensitivity().getValue() * (double)0.6f + (double)0.2f;
        double f = e * e * e;
        double g = f * 8.0;
        if (this.client.options.smoothCameraEnabled) {
            double h = this.cursorXSmoother.smooth(this.cursorDeltaX * g, timeDelta * g);
            double i = this.cursorYSmoother.smooth(this.cursorDeltaY * g, timeDelta * g);
            j = h;
            k = i;
        } else if (this.client.options.getPerspective().isFirstPerson() && this.client.player.isUsingSpyglass()) {
            this.cursorXSmoother.clear();
            this.cursorYSmoother.clear();
            j = this.cursorDeltaX * f;
            k = this.cursorDeltaY * f;
        } else {
            this.cursorXSmoother.clear();
            this.cursorYSmoother.clear();
            j = this.cursorDeltaX * g;
            k = this.cursorDeltaY * g;
        }
        this.client.getTutorialManager().onUpdateMouse(j, k);
        if (this.client.player != null) {
            this.client.player.changeLookDirection(this.client.options.getInvertMouseX().getValue() != false ? -j : j, this.client.options.getInvertMouseY().getValue() != false ? -k : k);
        }
    }

    public boolean wasLeftButtonClicked() {
        return this.leftButtonClicked;
    }

    public boolean wasMiddleButtonClicked() {
        return this.middleButtonClicked;
    }

    public boolean wasRightButtonClicked() {
        return this.rightButtonClicked;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public void onResolutionChanged() {
        this.hasResolutionChanged = true;
    }

    public boolean isCursorLocked() {
        return this.cursorLocked;
    }

    public void lockCursor() {
        if (!this.client.isWindowFocused()) {
            return;
        }
        if (this.cursorLocked) {
            return;
        }
        if (SystemKeycodes.UPDATE_PRESSED_STATE_ON_MOUSE_GRAB) {
            KeyBinding.updatePressedStates();
        }
        this.cursorLocked = true;
        this.x = this.client.getWindow().getWidth() / 2;
        this.y = this.client.getWindow().getHeight() / 2;
        InputUtil.setCursorParameters(this.client.getWindow(), InputUtil.GLFW_CURSOR_DISABLED, this.x, this.y);
        this.client.setScreen(null);
        this.client.attackCooldown = 10000;
        this.hasResolutionChanged = true;
    }

    public void unlockCursor() {
        if (!this.cursorLocked) {
            return;
        }
        this.cursorLocked = false;
        this.x = this.client.getWindow().getWidth() / 2;
        this.y = this.client.getWindow().getHeight() / 2;
        InputUtil.setCursorParameters(this.client.getWindow(), InputUtil.GLFW_CURSOR_NORMAL, this.x, this.y);
    }

    public void setResolutionChanged() {
        this.hasResolutionChanged = true;
    }

    public void drawScaledPos(TextRenderer textRenderer, DrawContext context) {
        Window lv = this.client.getWindow();
        double d = this.getScaledX(lv);
        double e = this.getScaledY(lv) - 8.0;
        String string = String.format(Locale.ROOT, "%.0f,%.0f", d, e);
        context.drawTextWithShadow(textRenderer, string, (int)d, (int)e, Colors.WHITE);
    }
}

