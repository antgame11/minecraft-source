/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.gui.screen.RealmsCreateWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsUploadScreen;
import net.minecraft.client.realms.task.WorldCreationTask;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsSelectFileToUploadScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Text TITLE = Text.translatable("mco.upload.select.world.title");
    private static final Text LOADING_ERROR_TEXT = Text.translatable("selectWorld.unable_to_load");
    @Nullable
    private final WorldCreationTask creationTask;
    private final RealmsCreateWorldScreen parent;
    private final long worldId;
    private final int slotId;
    private final ThreePartsLayoutWidget field_62099;
    @Nullable
    protected TextFieldWidget field_62100;
    @Nullable
    private WorldListWidget worldSelectionList;
    @Nullable
    private ButtonWidget uploadButton;

    public RealmsSelectFileToUploadScreen(@Nullable WorldCreationTask creationTask, long worldId, int slotId, RealmsCreateWorldScreen parent) {
        super(TITLE);
        this.field_62099 = new ThreePartsLayoutWidget(this, 8 + MinecraftClient.getInstance().textRenderer.fontHeight + 8 + 20 + 4, 33);
        this.creationTask = creationTask;
        this.parent = parent;
        this.worldId = worldId;
        this.slotId = slotId;
    }

    @Override
    public void init() {
        DirectionalLayoutWidget lv = this.field_62099.addHeader(DirectionalLayoutWidget.vertical().spacing(4));
        lv.getMainPositioner().alignHorizontalCenter();
        lv.add(new TextWidget(this.title, this.textRenderer));
        this.field_62100 = lv.add(new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 22, 200, 20, this.field_62100, Text.translatable("selectWorld.search")));
        this.field_62100.setChangedListener(string -> {
            if (this.worldSelectionList != null) {
                this.worldSelectionList.setSearch((String)string);
            }
        });
        try {
            this.worldSelectionList = this.field_62099.addBody(new WorldListWidget.Builder(this.client, this).width(this.width).height(this.field_62099.getContentHeight()).search(this.field_62100.getText()).predecessor(this.worldSelectionList).uploadWorld().selectionCallback(this::worldSelected).confirmationCallback(this::upload).toWidget());
        } catch (Exception exception) {
            LOGGER.error("Couldn't load level list", exception);
            this.client.setScreen(new RealmsGenericErrorScreen(LOADING_ERROR_TEXT, Text.of(exception.getMessage()), this.parent));
            return;
        }
        DirectionalLayoutWidget lv2 = this.field_62099.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        lv2.getMainPositioner().alignHorizontalCenter();
        this.uploadButton = lv2.add(ButtonWidget.builder(Text.translatable("mco.upload.button.name"), arg -> this.worldSelectionList.getSelectedAsOptional().ifPresent(this::upload)).build());
        lv2.add(ButtonWidget.builder(ScreenTexts.BACK, arg -> this.close()).build());
        this.worldSelected(null);
        this.field_62099.forEachChild(arg2 -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(arg2);
        });
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        if (this.worldSelectionList != null) {
            this.worldSelectionList.position(this.width, this.field_62099);
        }
        this.field_62099.refreshPositions();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.field_62100);
    }

    private void worldSelected(@Nullable LevelSummary level) {
        if (this.worldSelectionList != null && this.uploadButton != null) {
            this.uploadButton.active = this.worldSelectionList.getSelectedOrNull() != null;
        }
    }

    private void upload(WorldListWidget.WorldEntry arg) {
        this.client.setScreen(new RealmsUploadScreen(this.creationTask, this.worldId, this.slotId, this.parent, arg.getLevel()));
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(this.getTitle(), this.narrateLabels());
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}

