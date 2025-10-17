/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen.tab;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.WorldTemplate;
import net.minecraft.client.realms.gui.RealmsPopups;
import net.minecraft.client.realms.gui.RealmsWorldSlotButton;
import net.minecraft.client.realms.gui.screen.RealmsBackupScreen;
import net.minecraft.client.realms.gui.screen.RealmsConfigureWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsCreateWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsSelectWorldTemplateScreen;
import net.minecraft.client.realms.gui.screen.RealmsSlotOptionsScreen;
import net.minecraft.client.realms.gui.screen.tab.RealmsUpdatableTab;
import net.minecraft.client.realms.task.SwitchMinigameTask;
import net.minecraft.client.realms.task.SwitchSlotTask;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
class RealmsWorldsTab
extends GridScreenTab
implements RealmsUpdatableTab {
    static final Text TITLE_TEXT = Text.translatable("mco.configure.worlds.title");
    private final RealmsConfigureWorldScreen screen;
    private final MinecraftClient client;
    private RealmsServer server;
    private final ButtonWidget optionsButton;
    private final ButtonWidget backupButton;
    private final ButtonWidget resetButton;
    private final List<RealmsWorldSlotButton> slotButtons = Lists.newArrayList();

    RealmsWorldsTab(RealmsConfigureWorldScreen screen, MinecraftClient client, RealmsServer server) {
        super(TITLE_TEXT);
        this.screen = screen;
        this.client = client;
        this.server = server;
        GridWidget.Adder lv = this.grid.setSpacing(20).createAdder(1);
        GridWidget.Adder lv2 = new GridWidget().setSpacing(16).createAdder(4);
        this.slotButtons.clear();
        for (int i = 1; i < 5; ++i) {
            this.slotButtons.add(lv2.add(this.createSlotButton(i), Positioner.create().alignBottom()));
        }
        lv.add(lv2.getGridWidget());
        GridWidget.Adder lv3 = new GridWidget().setSpacing(8).createAdder(1);
        this.optionsButton = lv3.add(ButtonWidget.builder(Text.translatable("mco.configure.world.buttons.options"), button -> client.setScreen(new RealmsSlotOptionsScreen(screen, arg3.slots.get(arg3.activeSlot).clone(), arg3.worldType, arg3.activeSlot))).dimensions(0, 0, 150, 20).build());
        this.backupButton = lv3.add(ButtonWidget.builder(Text.translatable("mco.configure.world.backup"), button -> client.setScreen(new RealmsBackupScreen(screen, server.clone(), arg3.activeSlot))).dimensions(0, 0, 150, 20).build());
        this.resetButton = lv3.add(ButtonWidget.builder(Text.empty(), button -> this.reset()).dimensions(0, 0, 150, 20).build());
        lv.add(lv3.getGridWidget(), Positioner.create().alignHorizontalCenter());
        this.backupButton.active = true;
        this.update(server);
    }

    private void reset() {
        if (this.isMinigame()) {
            this.client.setScreen(new RealmsSelectWorldTemplateScreen(Text.translatable("mco.template.title.minigame"), this::switchMinigame, RealmsServer.WorldType.MINIGAME, null));
        } else {
            this.client.setScreen(RealmsCreateWorldScreen.resetWorld(this.screen, this.server.clone(), () -> this.client.execute(() -> this.client.setScreen(this.screen.getNewScreen()))));
        }
    }

    private void switchMinigame(@Nullable WorldTemplate template) {
        if (template != null && WorldTemplate.WorldTemplateType.MINIGAME == template.type) {
            this.screen.stateChanged();
            RealmsConfigureWorldScreen lv = this.screen.getNewScreen();
            this.client.setScreen(new RealmsLongRunningMcoTaskScreen(lv, new SwitchMinigameTask(this.server.id, template, lv)));
        } else {
            this.client.setScreen(this.screen);
        }
    }

    private boolean isMinigame() {
        return this.server.isMinigame();
    }

    @Override
    public void onLoaded(RealmsServer server) {
        this.update(server);
    }

    @Override
    public void update(RealmsServer server) {
        this.server = server;
        this.optionsButton.active = !server.expired && !this.isMinigame();
        boolean bl = this.resetButton.active = !server.expired;
        if (this.isMinigame()) {
            this.resetButton.setMessage(Text.translatable("mco.configure.world.buttons.switchminigame"));
        } else {
            boolean bl2;
            boolean bl3 = bl2 = server.slots.containsKey(server.activeSlot) && server.slots.get((Object)Integer.valueOf((int)server.activeSlot)).options.empty;
            if (bl2) {
                this.resetButton.setMessage(Text.translatable("mco.configure.world.buttons.newworld"));
            } else {
                this.resetButton.setMessage(Text.translatable("mco.configure.world.buttons.resetworld"));
            }
        }
        this.backupButton.active = !this.isMinigame();
        for (RealmsWorldSlotButton lv : this.slotButtons) {
            RealmsWorldSlotButton.State lv2 = lv.setServer(server);
            if (lv2.active) {
                lv.setDimensions(80, 80);
                continue;
            }
            lv.setDimensions(50, 50);
        }
    }

    private RealmsWorldSlotButton createSlotButton(int slotIndex) {
        return new RealmsWorldSlotButton(0, 0, 80, 80, slotIndex, this.server, button -> {
            RealmsWorldSlotButton.State lv = ((RealmsWorldSlotButton)button).getState();
            switch (lv.action) {
                case NOTHING: {
                    break;
                }
                case SWITCH_SLOT: {
                    if (lv.minigame) {
                        this.showSwitchMinigameScreen();
                        break;
                    }
                    if (lv.empty) {
                        this.createWorld(slotIndex, this.server);
                        break;
                    }
                    this.switchWorld(slotIndex, this.server);
                    break;
                }
                default: {
                    throw new IllegalStateException("Unknown action " + String.valueOf((Object)lv.action));
                }
            }
        });
    }

    private void showSwitchMinigameScreen() {
        RealmsSelectWorldTemplateScreen lv = new RealmsSelectWorldTemplateScreen(Text.translatable("mco.template.title.minigame"), this::switchMinigame, RealmsServer.WorldType.MINIGAME, null, List.of(Text.translatable("mco.minigame.world.info.line1"), Text.translatable("mco.minigame.world.info.line2")));
        this.client.setScreen(lv);
    }

    private void switchWorld(int slotId, RealmsServer server) {
        this.client.setScreen(RealmsPopups.createInfoPopup(this.screen, Text.translatable("mco.configure.world.slot.switch.question.line1"), popup -> {
            RealmsConfigureWorldScreen lv = this.screen.getNewScreen();
            this.screen.stateChanged();
            this.client.setScreen(new RealmsLongRunningMcoTaskScreen(lv, new SwitchSlotTask(arg.id, slotId, () -> this.client.execute(() -> this.client.setScreen(lv)))));
        }));
    }

    private void createWorld(int slotId, RealmsServer server) {
        this.client.setScreen(RealmsPopups.createInfoPopup(this.screen, Text.translatable("mco.configure.world.slot.switch.question.line1"), popup -> {
            this.screen.stateChanged();
            RealmsCreateWorldScreen lv = RealmsCreateWorldScreen.newWorld(this.screen, slotId, server, () -> this.client.execute(() -> this.client.setScreen(this.screen.getNewScreen())));
            this.client.setScreen(lv);
        }));
    }
}

