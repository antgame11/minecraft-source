/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.WorldTemplate;
import net.minecraft.client.realms.dto.WorldTemplatePaginatedList;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsSelectFileToUploadScreen;
import net.minecraft.client.realms.gui.screen.RealmsSelectWorldTemplateScreen;
import net.minecraft.client.realms.gui.screen.RealmsWorldCreating;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.client.realms.task.ResettingWorldTemplateTask;
import net.minecraft.client.realms.task.SwitchSlotTask;
import net.minecraft.client.realms.task.WorldCreationTask;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsCreateWorldScreen
extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Text CREATE_REALM_TITLE = Text.translatable("mco.selectServer.create");
    private static final Text CREATE_REALM_SUBTITLE = Text.translatable("mco.selectServer.create.subtitle");
    private static final Text CREATE_WORLD_TITLE = Text.translatable("mco.configure.world.switch.slot");
    private static final Text CREATE_WORLD_SUBTITLE = Text.translatable("mco.configure.world.switch.slot.subtitle");
    private static final Text NEW_WORLD_BUTTON_TEXT = Text.translatable("mco.reset.world.generate");
    private static final Text RESET_WORLD_TITLE = Text.translatable("mco.reset.world.title");
    private static final Text RESET_WORLD_SUBTITLE = Text.translatable("mco.reset.world.warning");
    public static final Text CREATING_TEXT = Text.translatable("mco.create.world.reset.title");
    private static final Text RESETTING_TEXT = Text.translatable("mco.reset.world.resetting.screen.title");
    private static final Text TEMPLATE_TEXT = Text.translatable("mco.reset.world.template");
    private static final Text ADVENTURE_TEXT = Text.translatable("mco.reset.world.adventure");
    private static final Text EXPERIENCE_TEXT = Text.translatable("mco.reset.world.experience");
    private static final Text INSPIRATION_TEXT = Text.translatable("mco.reset.world.inspiration");
    private final Screen parent;
    private final RealmsServer serverData;
    private final Text subtitle;
    private final int subtitleColor;
    private final Text taskTitle;
    private static final Identifier UPLOAD_TEXTURE = Identifier.ofVanilla("textures/gui/realms/upload.png");
    private static final Identifier ADVENTURE_TEXTURE = Identifier.ofVanilla("textures/gui/realms/adventure.png");
    private static final Identifier SURVIVAL_SPAWN_TEXTURE = Identifier.ofVanilla("textures/gui/realms/survival_spawn.png");
    private static final Identifier NEW_WORLD_TEXTURE = Identifier.ofVanilla("textures/gui/realms/new_world.png");
    private static final Identifier EXPERIENCE_TEXTURE = Identifier.ofVanilla("textures/gui/realms/experience.png");
    private static final Identifier INSPIRATION_TEXTURE = Identifier.ofVanilla("textures/gui/realms/inspiration.png");
    WorldTemplatePaginatedList normalWorldTemplates;
    WorldTemplatePaginatedList adventureWorldTemplates;
    WorldTemplatePaginatedList experienceWorldTemplates;
    WorldTemplatePaginatedList inspirationWorldTemplates;
    public final int slot;
    @Nullable
    private final WorldCreationTask creationTask;
    private final Runnable callback;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    private RealmsCreateWorldScreen(Screen parent, RealmsServer serverData, int slot, Text title, Text subtitle, int subtitleColor, Text taskTitle, Runnable callback) {
        this(parent, serverData, slot, title, subtitle, subtitleColor, taskTitle, null, callback);
    }

    public RealmsCreateWorldScreen(Screen parent, RealmsServer serverData, int slot, Text title, Text subtitle, int subtitleColor, Text taskTitle, @Nullable WorldCreationTask creationTask, Runnable callback) {
        super(title);
        this.parent = parent;
        this.serverData = serverData;
        this.slot = slot;
        this.subtitle = subtitle;
        this.subtitleColor = subtitleColor;
        this.taskTitle = taskTitle;
        this.creationTask = creationTask;
        this.callback = callback;
    }

    public static RealmsCreateWorldScreen newRealm(Screen parent, RealmsServer serverData, WorldCreationTask creationTask, Runnable callback) {
        return new RealmsCreateWorldScreen(parent, serverData, serverData.activeSlot, CREATE_REALM_TITLE, CREATE_REALM_SUBTITLE, -6250336, CREATING_TEXT, creationTask, callback);
    }

    public static RealmsCreateWorldScreen newWorld(Screen parent, int slot, RealmsServer serverData, Runnable callback) {
        return new RealmsCreateWorldScreen(parent, serverData, slot, CREATE_WORLD_TITLE, CREATE_WORLD_SUBTITLE, -6250336, CREATING_TEXT, callback);
    }

    public static RealmsCreateWorldScreen resetWorld(Screen parent, RealmsServer serverData, Runnable callback) {
        return new RealmsCreateWorldScreen(parent, serverData, serverData.activeSlot, RESET_WORLD_TITLE, RESET_WORLD_SUBTITLE, -65536, RESETTING_TEXT, callback);
    }

    @Override
    public void init() {
        DirectionalLayoutWidget lv = this.layout.addHeader(DirectionalLayoutWidget.vertical());
        lv.getMainPositioner().margin(this.textRenderer.fontHeight / 3);
        lv.add(new TextWidget(this.title, this.textRenderer), Positioner::alignHorizontalCenter);
        lv.add(new TextWidget(this.subtitle, this.textRenderer).setTextColor(this.subtitleColor), Positioner::alignHorizontalCenter);
        new Thread("Realms-reset-world-fetcher"){

            @Override
            public void run() {
                RealmsClient lv = RealmsClient.create();
                try {
                    WorldTemplatePaginatedList lv2 = lv.fetchWorldTemplates(1, 10, RealmsServer.WorldType.NORMAL);
                    WorldTemplatePaginatedList lv3 = lv.fetchWorldTemplates(1, 10, RealmsServer.WorldType.ADVENTUREMAP);
                    WorldTemplatePaginatedList lv4 = lv.fetchWorldTemplates(1, 10, RealmsServer.WorldType.EXPERIENCE);
                    WorldTemplatePaginatedList lv5 = lv.fetchWorldTemplates(1, 10, RealmsServer.WorldType.INSPIRATION);
                    RealmsCreateWorldScreen.this.client.execute(() -> {
                        RealmsCreateWorldScreen.this.normalWorldTemplates = lv2;
                        RealmsCreateWorldScreen.this.adventureWorldTemplates = lv3;
                        RealmsCreateWorldScreen.this.experienceWorldTemplates = lv4;
                        RealmsCreateWorldScreen.this.inspirationWorldTemplates = lv5;
                    });
                } catch (RealmsServiceException lv6) {
                    LOGGER.error("Couldn't fetch templates in reset world", lv6);
                }
            }
        }.start();
        GridWidget lv2 = this.layout.addBody(new GridWidget());
        GridWidget.Adder lv3 = lv2.createAdder(3);
        lv3.getMainPositioner().marginX(16);
        lv3.add(new FrameButton(this.client.textRenderer, NEW_WORLD_BUTTON_TEXT, NEW_WORLD_TEXTURE, button -> RealmsWorldCreating.showCreateWorldScreen(this.client, this.parent, this, this.slot, this.serverData, this.creationTask)));
        lv3.add(new FrameButton(this.client.textRenderer, RealmsSelectFileToUploadScreen.TITLE, UPLOAD_TEXTURE, button -> this.client.setScreen(new RealmsSelectFileToUploadScreen(this.creationTask, this.serverData.id, this.slot, this))));
        lv3.add(new FrameButton(this.client.textRenderer, TEMPLATE_TEXT, SURVIVAL_SPAWN_TEXTURE, button -> this.client.setScreen(new RealmsSelectWorldTemplateScreen(TEMPLATE_TEXT, this::onSelectWorldTemplate, RealmsServer.WorldType.NORMAL, this.normalWorldTemplates))));
        lv3.add(EmptyWidget.ofHeight(16), 3);
        lv3.add(new FrameButton(this.client.textRenderer, ADVENTURE_TEXT, ADVENTURE_TEXTURE, button -> this.client.setScreen(new RealmsSelectWorldTemplateScreen(ADVENTURE_TEXT, this::onSelectWorldTemplate, RealmsServer.WorldType.ADVENTUREMAP, this.adventureWorldTemplates))));
        lv3.add(new FrameButton(this.client.textRenderer, EXPERIENCE_TEXT, EXPERIENCE_TEXTURE, button -> this.client.setScreen(new RealmsSelectWorldTemplateScreen(EXPERIENCE_TEXT, this::onSelectWorldTemplate, RealmsServer.WorldType.EXPERIENCE, this.experienceWorldTemplates))));
        lv3.add(new FrameButton(this.client.textRenderer, INSPIRATION_TEXT, INSPIRATION_TEXTURE, button -> this.client.setScreen(new RealmsSelectWorldTemplateScreen(INSPIRATION_TEXT, this::onSelectWorldTemplate, RealmsServer.WorldType.INSPIRATION, this.inspirationWorldTemplates))));
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).build());
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(this.getTitle(), this.subtitle);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private void onSelectWorldTemplate(@Nullable WorldTemplate template) {
        this.client.setScreen(this);
        if (template != null) {
            this.runTasks(new ResettingWorldTemplateTask(template, this.serverData.id, this.taskTitle, this.callback));
        }
        RealmsMainScreen.resetServerList();
    }

    private void runTasks(LongRunningTask task) {
        ArrayList<LongRunningTask> list = new ArrayList<LongRunningTask>();
        if (this.creationTask != null) {
            list.add(this.creationTask);
        }
        if (this.slot != this.serverData.activeSlot) {
            list.add(new SwitchSlotTask(this.serverData.id, this.slot, () -> {}));
        }
        list.add(task);
        this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent, list.toArray(new LongRunningTask[0])));
    }

    @Environment(value=EnvType.CLIENT)
    class FrameButton
    extends ButtonWidget {
        private static final Identifier TEXTURE = Identifier.ofVanilla("widget/slot_frame");
        private static final int SIZE = 60;
        private static final int TEXTURE_MARGIN = 2;
        private static final int TEXTURE_SIZE = 56;
        private final Identifier image;

        FrameButton(TextRenderer textRenderer, Text message, Identifier image, ButtonWidget.PressAction onPress) {
            super(0, 0, 60, 60 + textRenderer.fontHeight, message, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.image = image;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            boolean bl = this.isSelected();
            int k = Colors.WHITE;
            if (bl) {
                k = ColorHelper.fromFloats(1.0f, 0.56f, 0.56f, 0.56f);
            }
            int l = this.getX();
            int m = this.getY();
            context.drawTexture(RenderPipelines.GUI_TEXTURED, this.image, l + 2, m + 2, 0.0f, 0.0f, 56, 56, 56, 56, 56, 56, k);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, l, m, 60, 60, k);
            int n = bl ? Colors.LIGHT_GRAY : Colors.WHITE;
            context.drawCenteredTextWithShadow(RealmsCreateWorldScreen.this.textRenderer, this.getMessage(), l + 28, m - 14, n);
        }
    }
}

