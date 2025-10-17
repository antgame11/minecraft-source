/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.world;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.path.PathUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SelectWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final GeneratorOptions DEBUG_GENERATOR_OPTIONS = new GeneratorOptions("test1".hashCode(), true, false);
    protected final Screen parent;
    private final ThreePartsLayoutWidget layout;
    @Nullable
    private ButtonWidget deleteButton;
    @Nullable
    private ButtonWidget selectButton;
    @Nullable
    private ButtonWidget editButton;
    @Nullable
    private ButtonWidget recreateButton;
    @Nullable
    protected TextFieldWidget searchBox;
    @Nullable
    private WorldListWidget levelList;

    public SelectWorldScreen(Screen parent) {
        super(Text.translatable("selectWorld.title"));
        this.layout = new ThreePartsLayoutWidget(this, 8 + MinecraftClient.getInstance().textRenderer.fontHeight + 8 + 20 + 4, 60);
        this.parent = parent;
    }

    @Override
    protected void init() {
        DirectionalLayoutWidget lv = this.layout.addHeader(DirectionalLayoutWidget.vertical().spacing(4));
        lv.getMainPositioner().alignHorizontalCenter();
        lv.add(new TextWidget(this.title, this.textRenderer));
        DirectionalLayoutWidget lv2 = lv.add(DirectionalLayoutWidget.horizontal().spacing(4));
        if (SharedConstants.WORLD_RECREATE) {
            lv2.add(this.createDebugRecreateButton());
        }
        this.searchBox = lv2.add(new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 22, 200, 20, this.searchBox, Text.translatable("selectWorld.search")));
        this.searchBox.setChangedListener(search -> {
            if (this.levelList != null) {
                this.levelList.setSearch((String)search);
            }
        });
        Consumer<WorldListWidget.WorldEntry> consumer = WorldListWidget.WorldEntry::play;
        this.levelList = this.layout.addBody(new WorldListWidget.Builder(this.client, this).width(this.width).height(this.layout.getContentHeight()).search(this.searchBox.getText()).predecessor(this.levelList).selectionCallback(this::worldSelected).confirmationCallback(consumer).toWidget());
        this.addButtons(consumer, this.levelList);
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.refreshWidgetPositions();
        this.worldSelected(null);
    }

    private void addButtons(Consumer<WorldListWidget.WorldEntry> playAction, WorldListWidget levelList) {
        GridWidget lv = this.layout.addFooter(new GridWidget().setColumnSpacing(8).setRowSpacing(4));
        lv.getMainPositioner().alignHorizontalCenter();
        GridWidget.Adder lv2 = lv.createAdder(4);
        this.selectButton = lv2.add(ButtonWidget.builder(LevelSummary.SELECT_WORLD_TEXT, button -> levelList.getSelectedAsOptional().ifPresent(playAction)).build(), 2);
        lv2.add(ButtonWidget.builder(Text.translatable("selectWorld.create"), button -> CreateWorldScreen.show(this.client, levelList::refresh)).build(), 2);
        this.editButton = lv2.add(ButtonWidget.builder(Text.translatable("selectWorld.edit"), button -> levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::edit)).width(71).build());
        this.deleteButton = lv2.add(ButtonWidget.builder(Text.translatable("selectWorld.delete"), button -> levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::deleteIfConfirmed)).width(71).build());
        this.recreateButton = lv2.add(ButtonWidget.builder(Text.translatable("selectWorld.recreate"), button -> levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::recreate)).width(71).build());
        lv2.add(ButtonWidget.builder(ScreenTexts.BACK, button -> this.client.setScreen(this.parent)).width(71).build());
    }

    private ButtonWidget createDebugRecreateButton() {
        return ButtonWidget.builder(Text.literal("DEBUG recreate"), button -> {
            try {
                WorldListWidget.WorldEntry lv2;
                WorldListWidget.Entry lv;
                String string = "DEBUG world";
                if (this.levelList != null && !this.levelList.children().isEmpty() && (lv = (WorldListWidget.Entry)this.levelList.children().getFirst()) instanceof WorldListWidget.WorldEntry && (lv2 = (WorldListWidget.WorldEntry)lv).getLevelDisplayName().equals("DEBUG world")) {
                    lv2.delete();
                }
                LevelInfo lv3 = new LevelInfo("DEBUG world", GameMode.SPECTATOR, false, Difficulty.NORMAL, true, new GameRules(DataConfiguration.SAFE_MODE.enabledFeatures()), DataConfiguration.SAFE_MODE);
                String string2 = PathUtil.getNextUniqueName(this.client.getLevelStorage().getSavesDirectory(), "DEBUG world", "");
                this.client.createIntegratedServerLoader().createAndStart(string2, lv3, DEBUG_GENERATOR_OPTIONS, WorldPresets::createDemoOptions, this);
            } catch (IOException iOException) {
                LOGGER.error("Failed to recreate the debug world", iOException);
            }
        }).width(72).build();
    }

    @Override
    protected void refreshWidgetPositions() {
        if (this.levelList != null) {
            this.levelList.position(this.width, this.layout);
        }
        this.layout.refreshPositions();
    }

    @Override
    protected void setInitialFocus() {
        if (this.searchBox != null) {
            this.setInitialFocus(this.searchBox);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    public void worldSelected(@Nullable LevelSummary levelSummary) {
        if (this.selectButton == null || this.editButton == null || this.recreateButton == null || this.deleteButton == null) {
            return;
        }
        if (levelSummary == null) {
            this.selectButton.setMessage(LevelSummary.SELECT_WORLD_TEXT);
            this.selectButton.active = false;
            this.editButton.active = false;
            this.recreateButton.active = false;
            this.deleteButton.active = false;
        } else {
            this.selectButton.setMessage(levelSummary.getSelectWorldText());
            this.selectButton.active = levelSummary.isSelectable();
            this.editButton.active = levelSummary.isEditable();
            this.recreateButton.active = levelSummary.isRecreatable();
            this.deleteButton.active = levelSummary.isDeletable();
        }
    }

    @Override
    public void removed() {
        if (this.levelList != null) {
            this.levelList.children().forEach(WorldListWidget.Entry::close);
        }
    }
}

