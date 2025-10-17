/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.cursor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.cursor.Cursor;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class StandardCursors {
    public static final Cursor ARROW = Cursor.createStandard(GLFW.GLFW_ARROW_CURSOR, "arrow", Cursor.DEFAULT);
    public static final Cursor IBEAM = Cursor.createStandard(GLFW.GLFW_IBEAM_CURSOR, "ibeam", Cursor.DEFAULT);
    public static final Cursor CROSSHAIR = Cursor.createStandard(GLFW.GLFW_CROSSHAIR_CURSOR, "crosshair", Cursor.DEFAULT);
    public static final Cursor POINTING_HAND = Cursor.createStandard(GLFW.GLFW_POINTING_HAND_CURSOR, "pointing_hand", Cursor.DEFAULT);
    public static final Cursor RESIZE_NS = Cursor.createStandard(GLFW.GLFW_RESIZE_NS_CURSOR, "resize_ns", Cursor.DEFAULT);
    public static final Cursor RESIZE_EW = Cursor.createStandard(GLFW.GLFW_RESIZE_EW_CURSOR, "resize_ew", Cursor.DEFAULT);
    public static final Cursor RESIZE_ALL = Cursor.createStandard(GLFW.GLFW_RESIZE_ALL_CURSOR, "resize_all", Cursor.DEFAULT);
    public static final Cursor NOT_ALLOWED = Cursor.createStandard(GLFW.GLFW_NOT_ALLOWED_CURSOR, "not_allowed", Cursor.DEFAULT);
}

