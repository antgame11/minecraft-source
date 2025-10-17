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
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class SystemKeycodes {
    private static final boolean IS_MAC_OS_IMPL;
    public static final boolean IS_MAC_OS;
    public static final int LEFT_CTRL;
    public static final int RIGHT_CTRL;
    public static final int CTRL_MOD;
    public static final boolean USE_LONG_LEFT_PRESS;
    public static final boolean UPDATE_PRESSED_STATE_ON_MOUSE_GRAB;

    static {
        IS_MAC_OS = IS_MAC_OS_IMPL = Util.getOperatingSystem() == Util.OperatingSystem.OSX;
        LEFT_CTRL = IS_MAC_OS ? 343 : 341;
        RIGHT_CTRL = IS_MAC_OS ? 347 : 345;
        CTRL_MOD = IS_MAC_OS ? 8 : 2;
        USE_LONG_LEFT_PRESS = IS_MAC_OS_IMPL;
        UPDATE_PRESSED_STATE_ON_MOUSE_GRAB = !IS_MAC_OS_IMPL;
    }
}

