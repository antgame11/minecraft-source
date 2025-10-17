/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.world;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.screen.world.SymlinkWarningScreen;
import net.minecraft.client.gui.screen.world.WorldIcon;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.nbt.NbtCrashException;
import net.minecraft.nbt.NbtException;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.path.SymlinkEntry;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WorldListWidget
extends AlwaysSelectedEntryListWidget<Entry> {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
    static final Identifier ERROR_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("world_list/error_highlighted");
    static final Identifier ERROR_TEXTURE = Identifier.ofVanilla("world_list/error");
    static final Identifier MARKED_JOIN_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("world_list/marked_join_highlighted");
    static final Identifier MARKED_JOIN_TEXTURE = Identifier.ofVanilla("world_list/marked_join");
    static final Identifier WARNING_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("world_list/warning_highlighted");
    static final Identifier WARNING_TEXTURE = Identifier.ofVanilla("world_list/warning");
    static final Identifier JOIN_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("world_list/join_highlighted");
    static final Identifier JOIN_TEXTURE = Identifier.ofVanilla("world_list/join");
    static final Logger LOGGER = LogUtils.getLogger();
    static final Text FROM_NEWER_VERSION_FIRST_LINE = Text.translatable("selectWorld.tooltip.fromNewerVersion1").formatted(Formatting.RED);
    static final Text FROM_NEWER_VERSION_SECOND_LINE = Text.translatable("selectWorld.tooltip.fromNewerVersion2").formatted(Formatting.RED);
    static final Text SNAPSHOT_FIRST_LINE = Text.translatable("selectWorld.tooltip.snapshot1").formatted(Formatting.GOLD);
    static final Text SNAPSHOT_SECOND_LINE = Text.translatable("selectWorld.tooltip.snapshot2").formatted(Formatting.GOLD);
    static final Text LOCKED_TEXT = Text.translatable("selectWorld.locked").formatted(Formatting.RED);
    static final Text CONVERSION_TOOLTIP = Text.translatable("selectWorld.conversion.tooltip").formatted(Formatting.RED);
    static final Text INCOMPATIBLE_TOOLTIP = Text.translatable("selectWorld.incompatible.tooltip").formatted(Formatting.RED);
    static final Text EXPERIMENTAL_TEXT = Text.translatable("selectWorld.experimental");
    private final Screen parent;
    private CompletableFuture<List<LevelSummary>> levelsFuture;
    @Nullable
    private List<LevelSummary> levels;
    private final LoadingEntry loadingEntry;
    final WorldListType worldListType;
    private String search;
    private boolean failedToGetLevels;
    @Nullable
    private final Consumer<LevelSummary> selectionCallback;
    @Nullable
    final Consumer<WorldEntry> confirmationCallback;

    WorldListWidget(Screen parent, MinecraftClient client, int width, int height, String search, @Nullable WorldListWidget predecessor, @Nullable Consumer<LevelSummary> selectionCallback, @Nullable Consumer<WorldEntry> confirmationCallback, WorldListType worldListType) {
        super(client, width, height, 0, 36);
        this.parent = parent;
        this.loadingEntry = new LoadingEntry(client);
        this.search = search;
        this.selectionCallback = selectionCallback;
        this.confirmationCallback = confirmationCallback;
        this.worldListType = worldListType;
        this.levelsFuture = predecessor != null ? predecessor.levelsFuture : this.loadLevels();
        this.addEntry(this.loadingEntry);
        this.show(this.tryGet());
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(Entry::close);
        super.clearEntries();
    }

    @Nullable
    private List<LevelSummary> tryGet() {
        try {
            List<LevelSummary> list = this.levelsFuture.getNow(null);
            if (this.worldListType == WorldListType.UPLOAD_WORLD) {
                if (list != null && !this.failedToGetLevels) {
                    this.failedToGetLevels = true;
                    list = list.stream().filter(LevelSummary::isImmediatelyLoadable).toList();
                } else {
                    return null;
                }
            }
            return list;
        } catch (CancellationException | CompletionException runtimeException) {
            return null;
        }
    }

    public void load() {
        this.levelsFuture = this.loadLevels();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        List<LevelSummary> list = this.tryGet();
        if (list != this.levels) {
            this.show(list);
        }
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
    }

    private void show(@Nullable List<LevelSummary> summaries) {
        if (summaries == null) {
            return;
        }
        if (summaries.isEmpty()) {
            switch (this.worldListType.ordinal()) {
                case 0: {
                    CreateWorldScreen.show(this.client, () -> this.client.setScreen(null));
                    break;
                }
                case 1: {
                    this.clearEntries();
                    this.addEntry(new EmptyListEntry(Text.translatable("mco.upload.select.world.none"), this.parent.getTextRenderer()));
                }
            }
        } else {
            this.showSummaries(this.search, summaries);
            this.levels = summaries;
        }
    }

    public void setSearch(String search) {
        if (this.levels != null && !search.equals(this.search)) {
            this.showSummaries(search, this.levels);
        }
        this.search = search;
    }

    private CompletableFuture<List<LevelSummary>> loadLevels() {
        LevelStorage.LevelList lv;
        try {
            lv = this.client.getLevelStorage().getLevelList();
        } catch (LevelStorageException lv2) {
            LOGGER.error("Couldn't load level list", lv2);
            this.showUnableToLoadScreen(lv2.getMessageText());
            return CompletableFuture.completedFuture(List.of());
        }
        return this.client.getLevelStorage().loadSummaries(lv).exceptionally(throwable -> {
            this.client.setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Couldn't load level list"));
            return List.of();
        });
    }

    private void showSummaries(String search, List<LevelSummary> summaries) {
        ArrayList<WorldEntry> list2 = new ArrayList<WorldEntry>();
        Optional<WorldEntry> optional = this.getSelectedAsOptional();
        WorldEntry lv = null;
        for (LevelSummary lv2 : summaries.stream().filter(summary -> this.shouldShow(search.toLowerCase(Locale.ROOT), (LevelSummary)summary)).toList()) {
            WorldEntry lv3 = new WorldEntry(this, lv2);
            if (optional.isPresent() && optional.get().getLevel().getName().equals(lv3.getLevel().getName())) {
                lv = lv3;
            }
            list2.add(lv3);
        }
        this.removeEntries(this.children().stream().filter(child -> !list2.contains(child)).toList());
        list2.forEach(entry -> {
            if (!this.children().contains(entry)) {
                this.addEntry(entry);
            }
        });
        this.setSelected(lv);
        this.narrateScreenIfNarrationEnabled();
    }

    private boolean shouldShow(String search, LevelSummary summary) {
        return summary.getDisplayName().toLowerCase(Locale.ROOT).contains(search) || summary.getName().toLowerCase(Locale.ROOT).contains(search);
    }

    private void narrateScreenIfNarrationEnabled() {
        this.refreshScroll();
        this.parent.narrateScreenIfNarrationEnabled(true);
    }

    private void showUnableToLoadScreen(Text message) {
        this.client.setScreen(new FatalErrorScreen(Text.translatable("selectWorld.unable_to_load"), message));
    }

    @Override
    public int getRowWidth() {
        return 270;
    }

    @Override
    public void setSelected(@Nullable Entry arg) {
        super.setSelected(arg);
        if (this.selectionCallback != null) {
            LevelSummary levelSummary;
            if (arg instanceof WorldEntry) {
                WorldEntry lv = (WorldEntry)arg;
                levelSummary = lv.level;
            } else {
                levelSummary = null;
            }
            this.selectionCallback.accept(levelSummary);
        }
    }

    public Optional<WorldEntry> getSelectedAsOptional() {
        Entry lv = (Entry)this.getSelectedOrNull();
        if (lv instanceof WorldEntry) {
            WorldEntry lv2 = (WorldEntry)lv;
            return Optional.of(lv2);
        }
        return Optional.empty();
    }

    public void refresh() {
        this.load();
        this.client.setScreen(this.parent);
    }

    public Screen getParent() {
        return this.parent;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        if (this.children().contains(this.loadingEntry)) {
            this.loadingEntry.appendNarrations(builder);
            return;
        }
        super.appendClickableNarrations(builder);
    }

    @Environment(value=EnvType.CLIENT)
    public static class LoadingEntry
    extends Entry {
        private static final Text LOADING_LIST_TEXT = Text.translatable("selectWorld.loading_list");
        private final MinecraftClient client;

        public LoadingEntry(MinecraftClient client) {
            this.client = client;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int k = (this.client.currentScreen.width - this.client.textRenderer.getWidth(LOADING_LIST_TEXT)) / 2;
            int l = this.getContentY() + (this.getContentHeight() - this.client.textRenderer.fontHeight) / 2;
            context.drawTextWithShadow(this.client.textRenderer, LOADING_LIST_TEXT, k, l, Colors.WHITE);
            String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
            int m = (this.client.currentScreen.width - this.client.textRenderer.getWidth(string)) / 2;
            int n = l + this.client.textRenderer.fontHeight;
            context.drawTextWithShadow(this.client.textRenderer, string, m, n, Colors.GRAY);
        }

        @Override
        public Text getNarration() {
            return LOADING_LIST_TEXT;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum WorldListType {
        SINGLEPLAYER,
        UPLOAD_WORLD;

    }

    @Environment(value=EnvType.CLIENT)
    public static final class EmptyListEntry
    extends Entry {
        private final TextWidget widget;

        public EmptyListEntry(Text text, TextRenderer textRenderer) {
            this.widget = new TextWidget(text, textRenderer);
        }

        @Override
        public Text getNarration() {
            return this.widget.getMessage();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            this.widget.setPosition(this.getContentMiddleX() - this.widget.getWidth() / 2, this.getContentMiddleY() - this.widget.getHeight() / 2);
            this.widget.render(context, mouseX, mouseY, deltaTicks);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class WorldEntry
    extends Entry {
        private static final int field_32435 = 32;
        private static final int field_32436 = 32;
        private final WorldListWidget parent;
        private final MinecraftClient client;
        private final Screen screen;
        final LevelSummary level;
        private final WorldIcon icon;
        private final TextWidget displayNameWidget;
        private final TextWidget nameWidget;
        private final TextWidget detailsWidget;
        @Nullable
        private Path iconPath;

        public WorldEntry(WorldListWidget parent, LevelSummary summary) {
            this.parent = parent;
            this.client = parent.client;
            this.screen = parent.getParent();
            this.level = summary;
            this.icon = WorldIcon.forWorld(this.client.getTextureManager(), summary.getName());
            this.iconPath = summary.getIconPath();
            int i = parent.getRowWidth() - this.getTextX() - 2;
            MutableText lv = Text.literal(summary.getDisplayName());
            this.displayNameWidget = new TextWidget(lv, this.client.textRenderer);
            this.displayNameWidget.setMaxWidth(i);
            if (this.client.textRenderer.getWidth(lv) > i) {
                this.displayNameWidget.setTooltip(Tooltip.of(lv));
            }
            Object string = summary.getName();
            long l = summary.getLastPlayed();
            if (l != -1L) {
                string = (String)string + " (" + DATE_FORMAT.format(Instant.ofEpochMilli(l)) + ")";
            }
            MutableText lv2 = Text.literal((String)string);
            this.nameWidget = new TextWidget(lv2, this.client.textRenderer).setTextColor(-8355712);
            this.nameWidget.setMaxWidth(i);
            if (this.client.textRenderer.getWidth((String)string) > i) {
                this.nameWidget.setTooltip(Tooltip.of(lv2));
            }
            Text lv3 = summary.getDetails();
            this.detailsWidget = new TextWidget(lv3, this.client.textRenderer).setTextColor(-8355712);
            this.detailsWidget.setMaxWidth(i);
            if (this.client.textRenderer.getWidth(lv3) > i) {
                this.detailsWidget.setTooltip(Tooltip.of(lv3));
            }
            this.validateIconPath();
            this.loadIcon();
        }

        private void validateIconPath() {
            if (this.iconPath == null) {
                return;
            }
            try {
                BasicFileAttributes basicFileAttributes = Files.readAttributes(this.iconPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (basicFileAttributes.isSymbolicLink()) {
                    List<SymlinkEntry> list = this.client.getSymlinkFinder().validate(this.iconPath);
                    if (!list.isEmpty()) {
                        LOGGER.warn("{}", (Object)SymlinkValidationException.getMessage(this.iconPath, list));
                        this.iconPath = null;
                    } else {
                        basicFileAttributes = Files.readAttributes(this.iconPath, BasicFileAttributes.class, new LinkOption[0]);
                    }
                }
                if (!basicFileAttributes.isRegularFile()) {
                    this.iconPath = null;
                }
            } catch (NoSuchFileException noSuchFileException) {
                this.iconPath = null;
            } catch (IOException iOException) {
                LOGGER.error("could not validate symlink", iOException);
                this.iconPath = null;
            }
        }

        @Override
        public Text getNarration() {
            MutableText lv = Text.translatable("narrator.select.world_info", this.level.getDisplayName(), Text.of(new Date(this.level.getLastPlayed())), this.level.getDetails());
            if (this.level.isLocked()) {
                lv = ScreenTexts.joinSentences(lv, LOCKED_TEXT);
            }
            if (this.level.isExperimental()) {
                lv = ScreenTexts.joinSentences(lv, EXPERIMENTAL_TEXT);
            }
            return Text.translatable("narrator.select", lv);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int k = this.getTextX();
            this.displayNameWidget.setPosition(k, this.getContentY() + 1);
            this.displayNameWidget.render(context, mouseX, mouseY, deltaTicks);
            this.nameWidget.setPosition(k, this.getContentY() + this.client.textRenderer.fontHeight + 3);
            this.nameWidget.render(context, mouseX, mouseY, deltaTicks);
            this.detailsWidget.setPosition(k, this.getContentY() + this.client.textRenderer.fontHeight + this.client.textRenderer.fontHeight + 3);
            this.detailsWidget.render(context, mouseX, mouseY, deltaTicks);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, this.icon.getTextureId(), this.getContentX(), this.getContentY(), 0.0f, 0.0f, 32, 32, 32, 32);
            if (this.parent.worldListType == WorldListType.SINGLEPLAYER && (this.client.options.getTouchscreen().getValue().booleanValue() || hovered)) {
                Identifier lv4;
                context.fill(this.getContentX(), this.getContentY(), this.getContentX() + 32, this.getContentY() + 32, -1601138544);
                int l = mouseX - this.getContentX();
                boolean bl2 = l < 32;
                Identifier lv = bl2 ? JOIN_HIGHLIGHTED_TEXTURE : JOIN_TEXTURE;
                Identifier lv2 = bl2 ? WARNING_HIGHLIGHTED_TEXTURE : WARNING_TEXTURE;
                Identifier lv3 = bl2 ? ERROR_HIGHLIGHTED_TEXTURE : ERROR_TEXTURE;
                Identifier identifier = lv4 = bl2 ? MARKED_JOIN_HIGHLIGHTED_TEXTURE : MARKED_JOIN_TEXTURE;
                if (this.level instanceof LevelSummary.SymlinkLevelSummary || this.level instanceof LevelSummary.RecoveryWarning) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv3, this.getContentX(), this.getContentY(), 32, 32);
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv4, this.getContentX(), this.getContentY(), 32, 32);
                    return;
                }
                if (this.level.isLocked()) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv3, this.getContentX(), this.getContentY(), 32, 32);
                    if (bl2) {
                        context.drawTooltip(this.client.textRenderer.wrapLines(LOCKED_TEXT, 175), mouseX, mouseY);
                    }
                } else if (this.level.requiresConversion()) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv3, this.getContentX(), this.getContentY(), 32, 32);
                    if (bl2) {
                        context.drawTooltip(this.client.textRenderer.wrapLines(CONVERSION_TOOLTIP, 175), mouseX, mouseY);
                    }
                } else if (!this.level.isVersionAvailable()) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv3, this.getContentX(), this.getContentY(), 32, 32);
                    if (bl2) {
                        context.drawTooltip(this.client.textRenderer.wrapLines(INCOMPATIBLE_TOOLTIP, 175), mouseX, mouseY);
                    }
                } else if (this.level.shouldPromptBackup()) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv4, this.getContentX(), this.getContentY(), 32, 32);
                    if (this.level.wouldBeDowngraded()) {
                        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv3, this.getContentX(), this.getContentY(), 32, 32);
                        if (bl2) {
                            context.drawTooltip(ImmutableList.of(FROM_NEWER_VERSION_FIRST_LINE.asOrderedText(), FROM_NEWER_VERSION_SECOND_LINE.asOrderedText()), mouseX, mouseY);
                        }
                    } else if (!SharedConstants.getGameVersion().stable()) {
                        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv2, this.getContentX(), this.getContentY(), 32, 32);
                        if (bl2) {
                            context.drawTooltip(ImmutableList.of(SNAPSHOT_FIRST_LINE.asOrderedText(), SNAPSHOT_SECOND_LINE.asOrderedText()), mouseX, mouseY);
                        }
                    }
                } else {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lv, this.getContentX(), this.getContentY(), 32, 32);
                }
            }
        }

        private int getTextX() {
            return this.getContentX() + 32 + 3;
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            if (this.allowConfirmationByKeyboard() && (doubled || click.x() - (double)this.parent.getRowLeft() <= 32.0 && this.parent.worldListType == WorldListType.SINGLEPLAYER)) {
                this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                Consumer<WorldEntry> consumer = this.parent.confirmationCallback;
                if (consumer != null) {
                    consumer.accept(this);
                    return true;
                }
            }
            return super.mouseClicked(click, doubled);
        }

        @Override
        public boolean keyPressed(KeyInput input) {
            if (input.isEnterOrSpace() && this.allowConfirmationByKeyboard()) {
                this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                Consumer<WorldEntry> consumer = this.parent.confirmationCallback;
                if (consumer != null) {
                    consumer.accept(this);
                    return true;
                }
            }
            return super.keyPressed(input);
        }

        public boolean allowConfirmationByKeyboard() {
            return this.level.isSelectable() || this.parent.worldListType == WorldListType.UPLOAD_WORLD;
        }

        public void play() {
            if (!this.level.isSelectable()) {
                return;
            }
            if (this.level instanceof LevelSummary.SymlinkLevelSummary) {
                this.client.setScreen(SymlinkWarningScreen.world(() -> this.client.setScreen(this.screen)));
                return;
            }
            this.client.createIntegratedServerLoader().start(this.level.getName(), this.parent::refresh);
        }

        public void deleteIfConfirmed() {
            this.client.setScreen(new ConfirmScreen(confirmed -> {
                if (confirmed) {
                    this.client.setScreen(new ProgressScreen(true));
                    this.delete();
                }
                this.parent.refresh();
            }, Text.translatable("selectWorld.deleteQuestion"), Text.translatable("selectWorld.deleteWarning", this.level.getDisplayName()), Text.translatable("selectWorld.deleteButton"), ScreenTexts.CANCEL));
        }

        public void delete() {
            LevelStorage lv = this.client.getLevelStorage();
            String string = this.level.getName();
            try (LevelStorage.Session lv2 = lv.createSessionWithoutSymlinkCheck(string);){
                lv2.deleteSessionLock();
            } catch (IOException iOException) {
                SystemToast.addWorldDeleteFailureToast(this.client, string);
                LOGGER.error("Failed to delete world {}", (Object)string, (Object)iOException);
            }
        }

        public void edit() {
            EditWorldScreen lv3;
            LevelStorage.Session lv;
            this.openReadingWorldScreen();
            String string = this.level.getName();
            try {
                lv = this.client.getLevelStorage().createSession(string);
            } catch (IOException iOException) {
                SystemToast.addWorldAccessFailureToast(this.client, string);
                LOGGER.error("Failed to access level {}", (Object)string, (Object)iOException);
                this.parent.load();
                return;
            } catch (SymlinkValidationException lv2) {
                LOGGER.warn("{}", (Object)lv2.getMessage());
                this.client.setScreen(SymlinkWarningScreen.world(() -> this.client.setScreen(this.screen)));
                return;
            }
            try {
                lv3 = EditWorldScreen.create(this.client, lv, edited -> {
                    lv.tryClose();
                    this.parent.refresh();
                });
            } catch (IOException | NbtCrashException | NbtException exception) {
                lv.tryClose();
                SystemToast.addWorldAccessFailureToast(this.client, string);
                LOGGER.error("Failed to load world data {}", (Object)string, (Object)exception);
                this.parent.load();
                return;
            }
            this.client.setScreen(lv3);
        }

        public void recreate() {
            this.openReadingWorldScreen();
            try (LevelStorage.Session lv = this.client.getLevelStorage().createSession(this.level.getName());){
                Pair<LevelInfo, GeneratorOptionsHolder> pair = this.client.createIntegratedServerLoader().loadForRecreation(lv);
                LevelInfo lv2 = pair.getFirst();
                GeneratorOptionsHolder lv3 = pair.getSecond();
                Path path = CreateWorldScreen.copyDataPack(lv.getDirectory(WorldSavePath.DATAPACKS), this.client);
                lv3.initializeIndexedFeaturesLists();
                if (lv3.generatorOptions().isLegacyCustomizedType()) {
                    this.client.setScreen(new ConfirmScreen(confirmed -> this.client.setScreen(confirmed ? CreateWorldScreen.create(this.client, this.parent::refresh, lv2, lv3, path) : this.screen), Text.translatable("selectWorld.recreate.customized.title"), Text.translatable("selectWorld.recreate.customized.text"), ScreenTexts.PROCEED, ScreenTexts.CANCEL));
                } else {
                    this.client.setScreen(CreateWorldScreen.create(this.client, this.parent::refresh, lv2, lv3, path));
                }
            } catch (SymlinkValidationException lv4) {
                LOGGER.warn("{}", (Object)lv4.getMessage());
                this.client.setScreen(SymlinkWarningScreen.world(() -> this.client.setScreen(this.screen)));
            } catch (Exception exception) {
                LOGGER.error("Unable to recreate world", exception);
                this.client.setScreen(new NoticeScreen(() -> this.client.setScreen(this.screen), Text.translatable("selectWorld.recreate.error.title"), Text.translatable("selectWorld.recreate.error.text")));
            }
        }

        private void openReadingWorldScreen() {
            this.client.setScreenAndRender(new MessageScreen(Text.translatable("selectWorld.data_read")));
        }

        private void loadIcon() {
            boolean bl;
            boolean bl2 = bl = this.iconPath != null && Files.isRegularFile(this.iconPath, new LinkOption[0]);
            if (bl) {
                try (InputStream inputStream = Files.newInputStream(this.iconPath, new OpenOption[0]);){
                    this.icon.load(NativeImage.read(inputStream));
                } catch (Throwable throwable) {
                    LOGGER.error("Invalid icon for world {}", (Object)this.level.getName(), (Object)throwable);
                    this.iconPath = null;
                }
            } else {
                this.icon.destroy();
            }
        }

        @Override
        public void close() {
            if (!this.icon.isClosed()) {
                this.icon.close();
            }
        }

        public String getLevelDisplayName() {
            return this.level.getDisplayName();
        }

        @Override
        public LevelSummary getLevel() {
            return this.level;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry
    extends AlwaysSelectedEntryListWidget.Entry<Entry>
    implements AutoCloseable {
        @Override
        public void close() {
        }

        @Nullable
        public LevelSummary getLevel() {
            return null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final MinecraftClient client;
        private final Screen parent;
        private int width;
        private int height;
        private String search = "";
        private WorldListType worldListType = WorldListType.SINGLEPLAYER;
        @Nullable
        private WorldListWidget predecessor = null;
        @Nullable
        private Consumer<LevelSummary> selectionCallback = null;
        @Nullable
        private Consumer<WorldEntry> confirmationCallback = null;

        public Builder(MinecraftClient client, Screen parent) {
            this.client = client;
            this.parent = parent;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder search(String search) {
            this.search = search;
            return this;
        }

        public Builder predecessor(@Nullable WorldListWidget predecessor) {
            this.predecessor = predecessor;
            return this;
        }

        public Builder selectionCallback(Consumer<LevelSummary> selectionCallback) {
            this.selectionCallback = selectionCallback;
            return this;
        }

        public Builder confirmationCallback(Consumer<WorldEntry> confirmationCallback) {
            this.confirmationCallback = confirmationCallback;
            return this;
        }

        public Builder uploadWorld() {
            this.worldListType = WorldListType.UPLOAD_WORLD;
            return this;
        }

        public WorldListWidget toWidget() {
            return new WorldListWidget(this.parent, this.client, this.width, this.height, this.search, this.predecessor, this.selectionCallback, this.confirmationCallback, this.worldListType);
        }
    }
}

