/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.SystemKeycodes;
import net.minecraft.client.util.InputUtil;

@Environment(value=EnvType.CLIENT)
public interface AbstractInput {
    public static final int field_62593 = -1;

    public int getKeycode();

    public int modifiers();

    default public boolean isEnterOrSpace() {
        return this.getKeycode() == InputUtil.GLFW_KEY_ENTER || this.getKeycode() == InputUtil.GLFW_KEY_SPACE || this.getKeycode() == InputUtil.GLFW_KEY_KP_ENTER;
    }

    default public boolean isEnter() {
        return this.getKeycode() == InputUtil.GLFW_KEY_ENTER || this.getKeycode() == InputUtil.GLFW_KEY_KP_ENTER;
    }

    default public boolean isEscape() {
        return this.getKeycode() == InputUtil.GLFW_KEY_ESCAPE;
    }

    default public boolean isLeft() {
        return this.getKeycode() == InputUtil.GLFW_KEY_LEFT;
    }

    default public boolean isRight() {
        return this.getKeycode() == InputUtil.GLFW_KEY_RIGHT;
    }

    default public boolean isUp() {
        return this.getKeycode() == InputUtil.GLFW_KEY_UP;
    }

    default public boolean isDown() {
        return this.getKeycode() == InputUtil.GLFW_KEY_DOWN;
    }

    default public boolean isTab() {
        return this.getKeycode() == InputUtil.GLFW_KEY_TAB;
    }

    default public int asNumber() {
        int i = this.getKeycode() - InputUtil.GLFW_KEY_0;
        if (i >= 0 && i <= 9) {
            return i;
        }
        return -1;
    }

    default public boolean hasAlt() {
        return (this.modifiers() & 4) != 0;
    }

    default public boolean hasShift() {
        return (this.modifiers() & 1) != 0;
    }

    default public boolean hasCtrl() {
        return (this.modifiers() & SystemKeycodes.CTRL_MOD) != 0;
    }

    default public boolean isSelectAll() {
        return this.getKeycode() == InputUtil.GLFW_KEY_A && this.hasCtrl() && !this.hasShift() && !this.hasAlt();
    }

    default public boolean isCopy() {
        return this.getKeycode() == InputUtil.GLFW_KEY_C && this.hasCtrl() && !this.hasShift() && !this.hasAlt();
    }

    default public boolean isPaste() {
        return this.getKeycode() == InputUtil.GLFW_KEY_V && this.hasCtrl() && !this.hasShift() && !this.hasAlt();
    }

    default public boolean isCut() {
        return this.getKeycode() == InputUtil.GLFW_KEY_X && this.hasCtrl() && !this.hasShift() && !this.hasAlt();
    }
}

