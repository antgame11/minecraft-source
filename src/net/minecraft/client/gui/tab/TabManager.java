/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.tab;

import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TabManager {
    private final Consumer<ClickableWidget> tabLoadWidgetConsumer;
    private final Consumer<ClickableWidget> tabUnloadWidgetConsumer;
    private final Consumer<Tab> tabLoadTabConsumer;
    private final Consumer<Tab> tabUnloadTabConsumer;
    @Nullable
    private Tab currentTab;
    @Nullable
    private ScreenRect tabArea;

    public TabManager(Consumer<ClickableWidget> tabLoadWidgetConsumer, Consumer<ClickableWidget> tabUnloadWidgetConsumer) {
        this(tabLoadWidgetConsumer, tabUnloadWidgetConsumer, loadedTab -> {}, unloadedTab -> {});
    }

    public TabManager(Consumer<ClickableWidget> tabLoadWidgetConsumer, Consumer<ClickableWidget> tabUnloadWidgetConsumer, Consumer<Tab> tabLoadTabConsumer, Consumer<Tab> tabUnloadTabConsumer) {
        this.tabLoadWidgetConsumer = tabLoadWidgetConsumer;
        this.tabUnloadWidgetConsumer = tabUnloadWidgetConsumer;
        this.tabLoadTabConsumer = tabLoadTabConsumer;
        this.tabUnloadTabConsumer = tabUnloadTabConsumer;
    }

    public void setTabArea(ScreenRect tabArea) {
        this.tabArea = tabArea;
        Tab lv = this.getCurrentTab();
        if (lv != null) {
            lv.refreshGrid(tabArea);
        }
    }

    public void setCurrentTab(Tab tab, boolean clickSound) {
        if (!Objects.equals(this.currentTab, tab)) {
            if (this.currentTab != null) {
                this.currentTab.forEachChild(this.tabUnloadWidgetConsumer);
            }
            Tab lv = this.currentTab;
            this.currentTab = tab;
            tab.forEachChild(this.tabLoadWidgetConsumer);
            if (this.tabArea != null) {
                tab.refreshGrid(this.tabArea);
            }
            if (clickSound) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
            this.tabUnloadTabConsumer.accept(lv);
            this.tabLoadTabConsumer.accept(this.currentTab);
        }
    }

    @Nullable
    public Tab getCurrentTab() {
        return this.currentTab;
    }
}

