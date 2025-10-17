/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.WorldTemplate;
import net.minecraft.client.realms.dto.WorldTemplatePaginatedList;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.util.RealmsTextureManager;
import net.minecraft.client.realms.util.TextRenderingUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Urls;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsSelectWorldTemplateScreen
extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    static final Identifier SLOT_FRAME_TEXTURE = Identifier.ofVanilla("widget/slot_frame");
    private static final Text SELECT_TEXT = Text.translatable("mco.template.button.select");
    private static final Text TRAILER_TEXT = Text.translatable("mco.template.button.trailer");
    private static final Text PUBLISHER_TEXT = Text.translatable("mco.template.button.publisher");
    private static final int field_45974 = 100;
    final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    final Consumer<WorldTemplate> callback;
    WorldTemplateObjectSelectionList templateList;
    private final RealmsServer.WorldType worldType;
    private final List<Text> field_62460;
    private ButtonWidget selectButton;
    private ButtonWidget trailerButton;
    private ButtonWidget publisherButton;
    @Nullable
    WorldTemplate selectedTemplate = null;
    @Nullable
    String currentLink;
    @Nullable
    List<TextRenderingUtils.Line> noTemplatesMessage;

    public RealmsSelectWorldTemplateScreen(Text title, Consumer<WorldTemplate> callback, RealmsServer.WorldType worldType, @Nullable WorldTemplatePaginatedList arg3) {
        this(title, callback, worldType, arg3, List.of());
    }

    public RealmsSelectWorldTemplateScreen(Text title, Consumer<WorldTemplate> callback, RealmsServer.WorldType worldType, @Nullable WorldTemplatePaginatedList templateList, List<Text> list) {
        super(title);
        this.callback = callback;
        this.worldType = worldType;
        if (templateList == null) {
            this.templateList = new WorldTemplateObjectSelectionList();
            this.setPagination(new WorldTemplatePaginatedList(10));
        } else {
            this.templateList = new WorldTemplateObjectSelectionList(Lists.newArrayList(templateList.templates));
            this.setPagination(templateList);
        }
        this.field_62460 = list;
    }

    @Override
    public void init() {
        this.layout.setHeaderHeight(33 + this.field_62460.size() * (this.getTextRenderer().fontHeight + 4));
        DirectionalLayoutWidget lv = this.layout.addHeader(DirectionalLayoutWidget.vertical().spacing(4));
        lv.getMainPositioner().alignHorizontalCenter();
        lv.add(new TextWidget(this.title, this.textRenderer));
        this.field_62460.forEach(arg2 -> lv.add(new TextWidget((Text)arg2, this.textRenderer).setTextColor(-4539718)));
        this.templateList = this.layout.addBody(new WorldTemplateObjectSelectionList(this.templateList.getValues()));
        DirectionalLayoutWidget lv2 = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        lv2.getMainPositioner().alignHorizontalCenter();
        this.trailerButton = lv2.add(ButtonWidget.builder(TRAILER_TEXT, button -> this.onTrailer()).width(100).build());
        this.selectButton = lv2.add(ButtonWidget.builder(SELECT_TEXT, button -> this.selectTemplate()).width(100).build());
        lv2.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).width(100).build());
        this.publisherButton = lv2.add(ButtonWidget.builder(PUBLISHER_TEXT, button -> this.onPublish()).width(100).build());
        this.updateButtonStates();
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        this.templateList.position(this.width, this.layout);
        this.layout.refreshPositions();
    }

    @Override
    public Text getNarratedTitle() {
        ArrayList<Text> list = Lists.newArrayListWithCapacity(2);
        list.add(this.title);
        list.addAll(this.field_62460);
        return ScreenTexts.joinLines(list);
    }

    void updateButtonStates() {
        this.publisherButton.visible = this.selectedTemplate != null && !this.selectedTemplate.link.isEmpty();
        this.trailerButton.visible = this.selectedTemplate != null && !this.selectedTemplate.trailer.isEmpty();
        this.selectButton.active = this.selectedTemplate != null;
    }

    @Override
    public void close() {
        this.callback.accept(null);
    }

    private void selectTemplate() {
        if (this.selectedTemplate != null) {
            this.callback.accept(this.selectedTemplate);
        }
    }

    private void onTrailer() {
        if (this.selectedTemplate != null && !this.selectedTemplate.trailer.isBlank()) {
            ConfirmLinkScreen.open((Screen)this, this.selectedTemplate.trailer);
        }
    }

    private void onPublish() {
        if (this.selectedTemplate != null && !this.selectedTemplate.link.isBlank()) {
            ConfirmLinkScreen.open((Screen)this, this.selectedTemplate.link);
        }
    }

    private void setPagination(final WorldTemplatePaginatedList templateList) {
        new Thread("realms-template-fetcher"){

            @Override
            public void run() {
                WorldTemplatePaginatedList lv = templateList;
                RealmsClient lv2 = RealmsClient.create();
                while (lv != null) {
                    Either<WorldTemplatePaginatedList, Exception> either = RealmsSelectWorldTemplateScreen.this.fetchWorldTemplates(lv, lv2);
                    lv = RealmsSelectWorldTemplateScreen.this.client.submit(() -> {
                        if (either.right().isPresent()) {
                            LOGGER.error("Couldn't fetch templates", (Throwable)either.right().get());
                            if (RealmsSelectWorldTemplateScreen.this.templateList.isEmpty()) {
                                RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(I18n.translate("mco.template.select.failure", new Object[0]), new TextRenderingUtils.LineSegment[0]);
                            }
                            return null;
                        }
                        WorldTemplatePaginatedList lv = (WorldTemplatePaginatedList)either.left().get();
                        for (WorldTemplate lv2 : lv.templates) {
                            RealmsSelectWorldTemplateScreen.this.templateList.addEntry(lv2);
                        }
                        if (lv.templates.isEmpty()) {
                            if (RealmsSelectWorldTemplateScreen.this.templateList.isEmpty()) {
                                String string = I18n.translate("mco.template.select.none", "%link");
                                TextRenderingUtils.LineSegment lv3 = TextRenderingUtils.LineSegment.link(I18n.translate("mco.template.select.none.linkTitle", new Object[0]), Urls.REALMS_CONTENT_CREATOR.toString());
                                RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(string, lv3);
                            }
                            return null;
                        }
                        return lv;
                    }).join();
                }
            }
        }.start();
    }

    Either<WorldTemplatePaginatedList, Exception> fetchWorldTemplates(WorldTemplatePaginatedList templateList, RealmsClient realms) {
        try {
            return Either.left(realms.fetchWorldTemplates(templateList.page + 1, templateList.size, this.worldType));
        } catch (RealmsServiceException lv) {
            return Either.right(lv);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.currentLink = null;
        if (this.noTemplatesMessage != null) {
            this.renderMessages(context, mouseX, mouseY, this.noTemplatesMessage);
        }
    }

    private void renderMessages(DrawContext context, int x, int y, List<TextRenderingUtils.Line> messages) {
        for (int k = 0; k < messages.size(); ++k) {
            TextRenderingUtils.Line lv = messages.get(k);
            int l = RealmsSelectWorldTemplateScreen.row(4 + k);
            int m = lv.segments.stream().mapToInt(segment -> this.textRenderer.getWidth(segment.renderedText())).sum();
            int n = this.width / 2 - m / 2;
            for (TextRenderingUtils.LineSegment lv2 : lv.segments) {
                int o = lv2.isLink() ? RealmsScreen.BLUE : Colors.WHITE;
                String string = lv2.renderedText();
                context.drawTextWithShadow(this.textRenderer, string, n, l, o);
                int p = n + this.textRenderer.getWidth(string);
                if (lv2.isLink() && x > n && x < p && y > l - 3 && y < l + 8) {
                    context.drawTooltip(Text.literal(lv2.getLinkUrl()), x, y);
                    this.currentLink = lv2.getLinkUrl();
                }
                n = p;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class WorldTemplateObjectSelectionList
    extends AlwaysSelectedEntryListWidget<WorldTemplateObjectSelectionListEntry> {
        public WorldTemplateObjectSelectionList() {
            this(Collections.emptyList());
        }

        public WorldTemplateObjectSelectionList(Iterable<WorldTemplate> templates) {
            super(MinecraftClient.getInstance(), RealmsSelectWorldTemplateScreen.this.width, RealmsSelectWorldTemplateScreen.this.layout.getContentHeight(), RealmsSelectWorldTemplateScreen.this.layout.getHeaderHeight(), 46);
            templates.forEach(this::addEntry);
        }

        public void addEntry(WorldTemplate template) {
            this.addEntry(new WorldTemplateObjectSelectionListEntry(template));
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
                ConfirmLinkScreen.open((Screen)RealmsSelectWorldTemplateScreen.this, RealmsSelectWorldTemplateScreen.this.currentLink);
                return true;
            }
            return super.mouseClicked(click, doubled);
        }

        @Override
        public void setSelected(@Nullable WorldTemplateObjectSelectionListEntry arg) {
            super.setSelected(arg);
            RealmsSelectWorldTemplateScreen.this.selectedTemplate = arg == null ? null : arg.mTemplate;
            RealmsSelectWorldTemplateScreen.this.updateButtonStates();
        }

        @Override
        public int getRowWidth() {
            return 300;
        }

        public boolean isEmpty() {
            return this.getEntryCount() == 0;
        }

        public List<WorldTemplate> getValues() {
            return this.children().stream().map(child -> child.mTemplate).collect(Collectors.toList());
        }
    }

    @Environment(value=EnvType.CLIENT)
    class WorldTemplateObjectSelectionListEntry
    extends AlwaysSelectedEntryListWidget.Entry<WorldTemplateObjectSelectionListEntry> {
        private static final ButtonTextures LINK_TEXTURES = new ButtonTextures(Identifier.ofVanilla("icon/link"), Identifier.ofVanilla("icon/link_highlighted"));
        private static final ButtonTextures VIDEO_LINK_TEXTURES = new ButtonTextures(Identifier.ofVanilla("icon/video_link"), Identifier.ofVanilla("icon/video_link_highlighted"));
        private static final Text INFO_TOOLTIP_TEXT = Text.translatable("mco.template.info.tooltip");
        private static final Text TRAILER_TOOLTIP_TEXT = Text.translatable("mco.template.trailer.tooltip");
        public final WorldTemplate mTemplate;
        @Nullable
        private TexturedButtonWidget infoButton;
        @Nullable
        private TexturedButtonWidget trailerButton;

        public WorldTemplateObjectSelectionListEntry(WorldTemplate template) {
            this.mTemplate = template;
            if (!template.link.isBlank()) {
                this.infoButton = new TexturedButtonWidget(15, 15, LINK_TEXTURES, ConfirmLinkScreen.opening((Screen)RealmsSelectWorldTemplateScreen.this, template.link), INFO_TOOLTIP_TEXT);
                this.infoButton.setTooltip(Tooltip.of(INFO_TOOLTIP_TEXT));
            }
            if (!template.trailer.isBlank()) {
                this.trailerButton = new TexturedButtonWidget(15, 15, VIDEO_LINK_TEXTURES, ConfirmLinkScreen.opening((Screen)RealmsSelectWorldTemplateScreen.this, template.trailer), TRAILER_TOOLTIP_TEXT);
                this.trailerButton.setTooltip(Tooltip.of(TRAILER_TOOLTIP_TEXT));
            }
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.mTemplate;
            RealmsSelectWorldTemplateScreen.this.updateButtonStates();
            if (doubled && this.isFocused()) {
                RealmsSelectWorldTemplateScreen.this.callback.accept(this.mTemplate);
            }
            if (this.infoButton != null) {
                this.infoButton.mouseClicked(click, doubled);
            }
            if (this.trailerButton != null) {
                this.trailerButton.mouseClicked(click, doubled);
            }
            return super.mouseClicked(click, doubled);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, RealmsTextureManager.getTextureId(this.mTemplate.id, this.mTemplate.image), this.getContentX() + 1, this.getContentY() + 1 + 1, 0.0f, 0.0f, 38, 38, 38, 38);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_FRAME_TEXTURE, this.getContentX(), this.getContentY() + 1, 40, 40);
            int k = 5;
            int l = RealmsSelectWorldTemplateScreen.this.textRenderer.getWidth(this.mTemplate.version);
            if (this.infoButton != null) {
                this.infoButton.setPosition(this.getContentRightEnd() - l - this.infoButton.getWidth() - 10, this.getContentY());
                this.infoButton.render(context, mouseX, mouseY, deltaTicks);
            }
            if (this.trailerButton != null) {
                this.trailerButton.setPosition(this.getContentRightEnd() - l - this.trailerButton.getWidth() * 2 - 15, this.getContentY());
                this.trailerButton.render(context, mouseX, mouseY, deltaTicks);
            }
            int m = this.getContentX() + 45 + 20;
            int n = this.getContentY() + 5;
            context.drawTextWithShadow(RealmsSelectWorldTemplateScreen.this.textRenderer, this.mTemplate.name, m, n, Colors.WHITE);
            context.drawTextWithShadow(RealmsSelectWorldTemplateScreen.this.textRenderer, this.mTemplate.version, this.getContentRightEnd() - l - 5, n, Colors.LIGHT_GRAY);
            context.drawTextWithShadow(RealmsSelectWorldTemplateScreen.this.textRenderer, this.mTemplate.author, m, n + ((RealmsSelectWorldTemplateScreen)RealmsSelectWorldTemplateScreen.this).textRenderer.fontHeight + 5, Colors.LIGHT_GRAY);
            if (!this.mTemplate.recommendedPlayers.isBlank()) {
                context.drawTextWithShadow(RealmsSelectWorldTemplateScreen.this.textRenderer, this.mTemplate.recommendedPlayers, m, this.getContentBottomEnd() - ((RealmsSelectWorldTemplateScreen)RealmsSelectWorldTemplateScreen.this).textRenderer.fontHeight / 2 - 5, Colors.GRAY);
            }
        }

        @Override
        public Text getNarration() {
            Text lv = ScreenTexts.joinLines(Text.literal(this.mTemplate.name), Text.translatable("mco.template.select.narrate.authors", this.mTemplate.author), Text.literal(this.mTemplate.recommendedPlayers), Text.translatable("mco.template.select.narrate.version", this.mTemplate.version));
            return Text.translatable("narrator.select", lv);
        }
    }
}

