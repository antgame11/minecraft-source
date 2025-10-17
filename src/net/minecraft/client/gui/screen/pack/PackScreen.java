/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.pack;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.screen.world.SymlinkWarningScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackOpener;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.path.SymlinkEntry;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PackScreen
extends Screen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Text AVAILABLE_TITLE = Text.translatable("pack.available.title");
    private static final Text SELECTED_TITLE = Text.translatable("pack.selected.title");
    private static final Text OPEN_FOLDER = Text.translatable("pack.openFolder");
    private static final Text SEARCH_BOX_PLACEHOLDER = Text.translatable("gui.packSelection.search").fillStyle(TextFieldWidget.SEARCH_STYLE);
    private static final int field_32395 = 200;
    private static final int field_62930 = 4;
    private static final int field_62931 = 15;
    private static final Text DROP_INFO = Text.translatable("pack.dropInfo").formatted(Formatting.GRAY);
    private static final Text FOLDER_INFO = Text.translatable("pack.folderInfo");
    private static final int field_32396 = 20;
    private static final Identifier UNKNOWN_PACK = Identifier.ofVanilla("textures/misc/unknown_pack.png");
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final ResourcePackOrganizer organizer;
    @Nullable
    private DirectoryWatcher directoryWatcher;
    private long refreshTimeout;
    @Nullable
    private PackListWidget availablePackList;
    @Nullable
    private PackListWidget selectedPackList;
    @Nullable
    private TextFieldWidget searchBox;
    private final Path file;
    @Nullable
    private ButtonWidget doneButton;
    private final Map<String, Identifier> iconTextures = Maps.newHashMap();

    public PackScreen(ResourcePackManager resourcePackManager, Consumer<ResourcePackManager> applier, Path file, Text title) {
        super(title);
        this.organizer = new ResourcePackOrganizer(this::updatePackLists, this::getPackIconTexture, resourcePackManager, applier);
        this.file = file;
        this.directoryWatcher = DirectoryWatcher.create(file);
    }

    @Override
    public void close() {
        this.organizer.apply();
        this.closeDirectoryWatcher();
    }

    private void closeDirectoryWatcher() {
        if (this.directoryWatcher != null) {
            try {
                this.directoryWatcher.close();
                this.directoryWatcher = null;
            } catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Override
    protected void init() {
        this.layout.setHeaderHeight(4 + this.textRenderer.fontHeight + 4 + this.textRenderer.fontHeight + 4 + 15 + 4);
        DirectionalLayoutWidget lv = this.layout.addHeader(DirectionalLayoutWidget.vertical().spacing(4));
        lv.getMainPositioner().alignHorizontalCenter();
        lv.add(new TextWidget(this.getTitle(), this.textRenderer));
        lv.add(new TextWidget(DROP_INFO, this.textRenderer));
        this.searchBox = lv.add(new TextFieldWidget(this.textRenderer, 0, 0, 200, 15, Text.empty()));
        this.searchBox.setPlaceholder(SEARCH_BOX_PLACEHOLDER);
        this.searchBox.setChangedListener(this::setSearch);
        this.availablePackList = this.addDrawableChild(new PackListWidget(this.client, this, 200, this.height - 66, AVAILABLE_TITLE));
        this.selectedPackList = this.addDrawableChild(new PackListWidget(this.client, this, 200, this.height - 66, SELECTED_TITLE));
        DirectionalLayoutWidget lv2 = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        lv2.add(ButtonWidget.builder(OPEN_FOLDER, button -> Util.getOperatingSystem().open(this.file)).tooltip(Tooltip.of(FOLDER_INFO)).build());
        this.doneButton = lv2.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).build());
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.refreshWidgetPositions();
        this.setInitialFocus(this.searchBox);
        this.refresh();
    }

    private void setSearch(String search) {
        this.filter(search, this.organizer.getEnabledPacks(), this.selectedPackList);
        this.filter(search, this.organizer.getDisabledPacks(), this.availablePackList);
    }

    private void filter(String search, Stream<ResourcePackOrganizer.Pack> packs, @Nullable PackListWidget listWidget) {
        if (listWidget == null) {
            return;
        }
        String string2 = search.toLowerCase(Locale.ROOT);
        Stream<ResourcePackOrganizer.Pack> stream2 = packs.filter(pack -> search.isBlank() || pack.getName().toLowerCase(Locale.ROOT).contains(string2) || pack.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(string2) || pack.getDescription().getString().toLowerCase(Locale.ROOT).contains(string2));
        listWidget.set(stream2, null);
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        if (this.availablePackList != null) {
            this.availablePackList.position(200, this.layout.getContentHeight(), this.width / 2 - 15 - 200, this.layout.getHeaderHeight());
        }
        if (this.selectedPackList != null) {
            this.selectedPackList.position(200, this.layout.getContentHeight(), this.width / 2 + 15, this.layout.getHeaderHeight());
        }
    }

    @Override
    public void tick() {
        if (this.directoryWatcher != null) {
            try {
                if (this.directoryWatcher.pollForChange()) {
                    this.refreshTimeout = 20L;
                }
            } catch (IOException iOException) {
                LOGGER.warn("Failed to poll for directory {} changes, stopping", (Object)this.file);
                this.closeDirectoryWatcher();
            }
        }
        if (this.refreshTimeout > 0L && --this.refreshTimeout == 0L) {
            this.refresh();
        }
    }

    private void updatePackLists(@Nullable ResourcePackOrganizer.AbstractPack focused) {
        if (this.selectedPackList != null) {
            this.selectedPackList.set(this.organizer.getEnabledPacks(), focused);
        }
        if (this.availablePackList != null) {
            this.availablePackList.set(this.organizer.getDisabledPacks(), focused);
        }
        if (this.searchBox != null) {
            this.setSearch(this.searchBox.getText());
        }
        if (this.doneButton != null) {
            this.doneButton.active = !this.selectedPackList.children().isEmpty();
        }
    }

    public void clearSelection() {
        if (this.selectedPackList != null) {
            this.selectedPackList.setSelected(null);
        }
        if (this.availablePackList != null) {
            this.availablePackList.setSelected(null);
        }
    }

    private void refresh() {
        this.organizer.refresh();
        this.updatePackLists(null);
        this.refreshTimeout = 0L;
        this.iconTextures.clear();
    }

    protected static void copyPacks(MinecraftClient client, List<Path> srcPaths, Path destPath) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        srcPaths.forEach(src -> {
            try (Stream<Path> stream = Files.walk(src, new FileVisitOption[0]);){
                stream.forEach(toCopy -> {
                    try {
                        Util.relativeCopy(src.getParent(), destPath, toCopy);
                    } catch (IOException iOException) {
                        LOGGER.warn("Failed to copy datapack file  from {} to {}", toCopy, destPath, iOException);
                        mutableBoolean.setTrue();
                    }
                });
            } catch (IOException iOException) {
                LOGGER.warn("Failed to copy datapack file from {} to {}", src, (Object)destPath);
                mutableBoolean.setTrue();
            }
        });
        if (mutableBoolean.isTrue()) {
            SystemToast.addPackCopyFailure(client, destPath.toString());
        }
    }

    @Override
    public void onFilesDropped(List<Path> paths) {
        String string = PackScreen.streamFileNames(paths).collect(Collectors.joining(", "));
        this.client.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                ArrayList<Path> list2 = new ArrayList<Path>(paths.size());
                HashSet<Path> set = new HashSet<Path>(paths);
                ResourcePackOpener<Path> lv = new ResourcePackOpener<Path>(this, this.client.getSymlinkFinder()){

                    @Override
                    protected Path openZip(Path path) {
                        return path;
                    }

                    @Override
                    protected Path openDirectory(Path path) {
                        return path;
                    }

                    @Override
                    protected /* synthetic */ Object openDirectory(Path path) throws IOException {
                        return this.openDirectory(path);
                    }

                    @Override
                    protected /* synthetic */ Object openZip(Path path) throws IOException {
                        return this.openZip(path);
                    }
                };
                ArrayList<SymlinkEntry> list3 = new ArrayList<SymlinkEntry>();
                for (Path path : paths) {
                    try {
                        Path path2 = (Path)lv.open(path, list3);
                        if (path2 == null) {
                            LOGGER.warn("Path {} does not seem like pack", (Object)path);
                            continue;
                        }
                        list2.add(path2);
                        set.remove(path2);
                    } catch (IOException iOException) {
                        LOGGER.warn("Failed to check {} for packs", (Object)path, (Object)iOException);
                    }
                }
                if (!list3.isEmpty()) {
                    this.client.setScreen(SymlinkWarningScreen.pack(() -> this.client.setScreen(this)));
                    return;
                }
                if (!list2.isEmpty()) {
                    PackScreen.copyPacks(this.client, list2, this.file);
                    this.refresh();
                }
                if (!set.isEmpty()) {
                    String string = PackScreen.streamFileNames(set).collect(Collectors.joining(", "));
                    this.client.setScreen(new NoticeScreen(() -> this.client.setScreen(this), Text.translatable("pack.dropRejected.title"), Text.translatable("pack.dropRejected.message", string)));
                    return;
                }
            }
            this.client.setScreen(this);
        }, Text.translatable("pack.dropConfirm"), Text.literal(string)));
    }

    private static Stream<String> streamFileNames(Collection<Path> paths) {
        return paths.stream().map(Path::getFileName).map(Path::toString);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private Identifier loadPackIcon(TextureManager textureManager, ResourcePackProfile resourcePackProfile) {
        try (ResourcePack lv = resourcePackProfile.createResourcePack();){
            Identifier identifier;
            block16: {
                InputSupplier<InputStream> lv2 = lv.openRoot("pack.png");
                if (lv2 == null) {
                    Identifier identifier2 = UNKNOWN_PACK;
                    return identifier2;
                }
                String string = resourcePackProfile.getId();
                Identifier lv3 = Identifier.ofVanilla("pack/" + Util.replaceInvalidChars(string, Identifier::isPathCharacterValid) + "/" + String.valueOf(Hashing.sha1().hashUnencodedChars(string)) + "/icon");
                InputStream inputStream = lv2.get();
                try {
                    NativeImage lv4 = NativeImage.read(inputStream);
                    textureManager.registerTexture(lv3, new NativeImageBackedTexture(lv3::toString, lv4));
                    identifier = lv3;
                    if (inputStream == null) break block16;
                } catch (Throwable throwable) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                inputStream.close();
            }
            return identifier;
        } catch (Exception exception) {
            LOGGER.warn("Failed to load icon from pack {}", (Object)resourcePackProfile.getId(), (Object)exception);
            return UNKNOWN_PACK;
        }
    }

    private Identifier getPackIconTexture(ResourcePackProfile resourcePackProfile) {
        return this.iconTextures.computeIfAbsent(resourcePackProfile.getId(), profileName -> this.loadPackIcon(this.client.getTextureManager(), resourcePackProfile));
    }

    @Environment(value=EnvType.CLIENT)
    static class DirectoryWatcher
    implements AutoCloseable {
        private final WatchService watchService;
        private final Path path;

        public DirectoryWatcher(Path path) throws IOException {
            this.path = path;
            this.watchService = path.getFileSystem().newWatchService();
            try {
                this.watchDirectory(path);
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);){
                    for (Path path2 : directoryStream) {
                        if (!Files.isDirectory(path2, LinkOption.NOFOLLOW_LINKS)) continue;
                        this.watchDirectory(path2);
                    }
                }
            } catch (Exception exception) {
                this.watchService.close();
                throw exception;
            }
        }

        @Nullable
        public static DirectoryWatcher create(Path path) {
            try {
                return new DirectoryWatcher(path);
            } catch (IOException iOException) {
                LOGGER.warn("Failed to initialize pack directory {} monitoring", (Object)path, (Object)iOException);
                return null;
            }
        }

        private void watchDirectory(Path path) throws IOException {
            path.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        public boolean pollForChange() throws IOException {
            WatchKey watchKey;
            boolean bl = false;
            while ((watchKey = this.watchService.poll()) != null) {
                List<WatchEvent<?>> list = watchKey.pollEvents();
                for (WatchEvent<?> watchEvent : list) {
                    Path path;
                    bl = true;
                    if (watchKey.watchable() != this.path || watchEvent.kind() != StandardWatchEventKinds.ENTRY_CREATE || !Files.isDirectory(path = this.path.resolve((Path)watchEvent.context()), LinkOption.NOFOLLOW_LINKS)) continue;
                    this.watchDirectory(path);
                }
                watchKey.reset();
            }
            return bl;
        }

        @Override
        public void close() throws IOException {
            this.watchService.close();
        }
    }
}

