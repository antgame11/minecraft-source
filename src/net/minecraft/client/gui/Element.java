/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.navigation.Navigable;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface Element
extends Navigable {
    default public void mouseMoved(double mouseX, double mouseY) {
    }

    default public boolean mouseClicked(Click click, boolean doubled) {
        return false;
    }

    default public boolean mouseReleased(Click click) {
        return false;
    }

    default public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        return false;
    }

    default public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    default public boolean keyPressed(KeyInput input) {
        return false;
    }

    default public boolean keyReleased(KeyInput input) {
        return false;
    }

    default public boolean charTyped(CharInput input) {
        return false;
    }

    @Nullable
    default public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return null;
    }

    default public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    public void setFocused(boolean var1);

    public boolean isFocused();

    default public boolean isClickable() {
        return true;
    }

    @Nullable
    default public GuiNavigationPath getFocusedPath() {
        if (this.isFocused()) {
            return GuiNavigationPath.of(this);
        }
        return null;
    }

    default public ScreenRect getNavigationFocus() {
        return ScreenRect.empty();
    }

    default public ScreenRect getBorder(NavigationDirection direction) {
        return this.getNavigationFocus().getBorder(direction);
    }
}

