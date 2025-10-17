/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileActionType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.DataFixer;
import com.mojang.jtracy.DiscontinuousFrame;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.lang.runtime.SwitchBootstraps;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientWatchdog;
import net.minecraft.client.Keyboard;
import net.minecraft.client.Mouse;
import net.minecraft.client.QuickPlay;
import net.minecraft.client.QuickPlayLogger;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FreeTypeUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlTimer;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudProfile;
import net.minecraft.client.gui.hud.debug.chart.PieChart;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.OutOfMemoryScreen;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.input.SystemKeycodes;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientMannequinEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerProfileResolver;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.InactivityFpsLimiter;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleSpriteManager;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsPeriodicCheckers;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.resource.DefaultClientResourcePackProvider;
import net.minecraft.client.resource.DryFoliageColormapResourceSupplier;
import net.minecraft.client.resource.FoliageColormapResourceSupplier;
import net.minecraft.client.resource.GrassColormapResourceSupplier;
import net.minecraft.client.resource.PeriodicNotificationManager;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.resource.VideoWarningManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.resource.server.ServerResourcePackLoader;
import net.minecraft.client.resource.waypoint.WaypointStyleAssetManager;
import net.minecraft.client.session.Bans;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.client.session.telemetry.GameLoadTimeEvent;
import net.minecraft.client.session.telemetry.TelemetryEventProperty;
import net.minecraft.client.session.telemetry.TelemetryManager;
import net.minecraft.client.sound.MusicInstance;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.MapTextureManager;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.PlayerSkinTextureDownloader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.client.util.ClientSamplerSource;
import net.minecraft.client.util.CommandHistoryManager;
import net.minecraft.client.util.GlException;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.WindowProvider;
import net.minecraft.client.util.tracy.TracyFrameCapturer;
import net.minecraft.client.world.ClientChunkLoadProgress;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.datafixer.Schemas;
import net.minecraft.dialog.Dialogs;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketApplyBatcher;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientTickEndC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.DialogTags;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.MusicType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.KeybindTranslations;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ModStatus;
import net.minecraft.util.Nullables;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.TickDurationMonitor;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.Unit;
import net.minecraft.util.Urls;
import net.minecraft.util.Util;
import net.minecraft.util.ZipCompressor;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashMemoryReserve;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.crash.ReportType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.path.PathUtil;
import net.minecraft.util.path.SymlinkFinder;
import net.minecraft.util.profiler.DebugRecorder;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.DummyRecorder;
import net.minecraft.util.profiler.EmptyProfileResult;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.profiler.RecordDumper;
import net.minecraft.util.profiler.Recorder;
import net.minecraft.util.profiler.ScopedProfiler;
import net.minecraft.util.profiler.TickTimeTracker;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkLoadProgress;
import net.minecraft.world.chunk.LoggingChunkLoadProgress;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.tick.TickManager;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class MinecraftClient
extends ReentrantThreadExecutor<Runnable>
implements WindowEventHandler {
    static MinecraftClient instance;
    private static final Logger LOGGER;
    private static final int field_32145 = 10;
    public static final Identifier DEFAULT_FONT_ID;
    public static final Identifier UNICODE_FONT_ID;
    public static final Identifier ALT_TEXT_RENDERER_ID;
    private static final Identifier REGIONAL_COMPLIANCIES_ID;
    private static final CompletableFuture<Unit> COMPLETED_UNIT_FUTURE;
    private static final Text SOCIAL_INTERACTIONS_NOT_AVAILABLE;
    private static final Text SAVING_LEVEL_TEXT;
    public static final String GL_ERROR_DIALOGUE = "Please make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).";
    private final long UNIVERSE = Double.doubleToLongBits(Math.PI);
    private final Path resourcePackDir;
    private final CompletableFuture<com.mojang.authlib.yggdrasil.ProfileResult> gameProfileFuture;
    private final TextureManager textureManager;
    private final ShaderLoader shaderLoader;
    private final DataFixer dataFixer;
    private final WindowProvider windowProvider;
    private final Window window;
    private final RenderTickCounter.Dynamic renderTickCounter = new RenderTickCounter.Dynamic(20.0f, 0L, this::getTargetMillisPerTick);
    private final BufferBuilderStorage bufferBuilders;
    public final WorldRenderer worldRenderer;
    private final EntityRenderManager entityRenderManager;
    private final ItemModelManager itemModelManager;
    private final ItemRenderer itemRenderer;
    private final MapRenderer mapRenderer;
    public final ParticleManager particleManager;
    private final ParticleSpriteManager particleSpriteManager;
    private final Session session;
    public final TextRenderer textRenderer;
    public final TextRenderer advanceValidatingTextRenderer;
    public final GameRenderer gameRenderer;
    public final InGameHud inGameHud;
    public final GameOptions options;
    public final DebugHudProfile debugHudEntryList;
    private final HotbarStorage creativeHotbarStorage;
    public final Mouse mouse;
    public final Keyboard keyboard;
    private GuiNavigationType navigationType = GuiNavigationType.NONE;
    public final File runDirectory;
    private final String gameVersion;
    private final String versionType;
    private final Proxy networkProxy;
    private final boolean offlineDeveloperMode;
    private final LevelStorage levelStorage;
    private final boolean isDemo;
    private final boolean multiplayerEnabled;
    private final boolean onlineChatEnabled;
    private final ReloadableResourceManagerImpl resourceManager;
    private final DefaultResourcePack defaultResourcePack;
    private final ServerResourcePackLoader serverResourcePackLoader;
    private final ResourcePackManager resourcePackManager;
    private final LanguageManager languageManager;
    private final BlockColors blockColors;
    private final Framebuffer framebuffer;
    @Nullable
    private final TracyFrameCapturer tracyFrameCapturer;
    private final SoundManager soundManager;
    private final MusicTracker musicTracker;
    private final FontManager fontManager;
    private final SplashTextResourceSupplier splashTextLoader;
    private final VideoWarningManager videoWarningManager;
    private final PeriodicNotificationManager regionalComplianciesManager = new PeriodicNotificationManager(REGIONAL_COMPLIANCIES_ID, MinecraftClient::isCountrySetTo);
    private final UserApiService userApiService;
    private final CompletableFuture<UserApiService.UserProperties> userPropertiesFuture;
    private final PlayerSkinProvider skinProvider;
    private final AtlasManager atlasManager;
    private final BakedModelManager bakedModelManager;
    private final BlockRenderManager blockRenderManager;
    private final MapTextureManager mapTextureManager;
    private final WaypointStyleAssetManager waypointStyleAssetManager;
    private final ToastManager toastManager;
    private final TutorialManager tutorialManager;
    private final SocialInteractionsManager socialInteractionsManager;
    private final BlockEntityRenderManager blockEntityRenderManager;
    private final TelemetryManager telemetryManager;
    private final ProfileKeys profileKeys;
    private final RealmsPeriodicCheckers realmsPeriodicCheckers;
    private final QuickPlayLogger quickPlayLogger;
    private final ApiServices apiServices;
    private final PlayerSkinCache playerSkinCache;
    @Nullable
    public ClientPlayerInteractionManager interactionManager;
    @Nullable
    public ClientWorld world;
    @Nullable
    public ClientPlayerEntity player;
    @Nullable
    private IntegratedServer server;
    @Nullable
    private ClientConnection integratedServerConnection;
    private boolean integratedServerRunning;
    @Nullable
    private Entity cameraEntity;
    @Nullable
    public Entity targetedEntity;
    @Nullable
    public HitResult crosshairTarget;
    private int itemUseCooldown;
    protected int attackCooldown;
    private volatile boolean paused;
    private long lastMetricsSampleTime = Util.getMeasuringTimeNano();
    private long nextDebugInfoUpdateTime;
    private int fpsCounter;
    public boolean skipGameRender;
    @Nullable
    public Screen currentScreen;
    @Nullable
    private Overlay overlay;
    private boolean disconnecting;
    Thread thread;
    private volatile boolean running;
    @Nullable
    private Supplier<CrashReport> crashReportSupplier;
    private static int currentFps;
    private long renderTime;
    private final InactivityFpsLimiter inactivityFpsLimiter;
    public boolean wireFrame;
    public boolean chunkCullingEnabled = true;
    private boolean windowFocused;
    @Nullable
    private CompletableFuture<Void> resourceReloadFuture;
    @Nullable
    private TutorialToast socialInteractionsToast;
    private int trackingTick;
    private final TickTimeTracker tickTimeTracker;
    private Recorder recorder = DummyRecorder.INSTANCE;
    private final ResourceReloadLogger resourceReloadLogger = new ResourceReloadLogger();
    private long metricsSampleDuration;
    private double gpuUtilizationPercentage;
    @Nullable
    private GlTimer.Query currentGlTimerQuery;
    private final NarratorManager narratorManager;
    private final MessageHandler messageHandler;
    private AbuseReportContext abuseReportContext;
    private final CommandHistoryManager commandHistoryManager;
    private final SymlinkFinder symlinkFinder;
    private boolean finishedLoading;
    private final long startTime;
    private long uptimeInTicks;
    private final PacketApplyBatcher packetApplyBatcher;

    public MinecraftClient(final RunArgs args) {
        super("Client");
        instance = this;
        this.startTime = System.currentTimeMillis();
        this.runDirectory = args.directories.runDir;
        File file = args.directories.assetDir;
        this.resourcePackDir = args.directories.resourcePackDir.toPath();
        this.gameVersion = args.game.version;
        this.versionType = args.game.versionType;
        Path path = this.runDirectory.toPath();
        this.symlinkFinder = LevelStorage.createSymlinkFinder(path.resolve("allowed_symlinks.txt"));
        DefaultClientResourcePackProvider lv = new DefaultClientResourcePackProvider(args.directories.getAssetDir(), this.symlinkFinder);
        this.serverResourcePackLoader = new ServerResourcePackLoader(this, path.resolve("downloads"), args.network);
        FileResourcePackProvider lv2 = new FileResourcePackProvider(this.resourcePackDir, ResourceType.CLIENT_RESOURCES, ResourcePackSource.NONE, this.symlinkFinder);
        this.resourcePackManager = new ResourcePackManager(lv, this.serverResourcePackLoader.getPassthroughPackProvider(), lv2);
        this.defaultResourcePack = lv.getResourcePack();
        this.networkProxy = args.network.netProxy;
        this.offlineDeveloperMode = args.game.offlineDeveloperMode;
        YggdrasilAuthenticationService yggdrasilAuthenticationService = this.offlineDeveloperMode ? YggdrasilAuthenticationService.createOffline(this.networkProxy) : new YggdrasilAuthenticationService(this.networkProxy);
        this.apiServices = ApiServices.create(yggdrasilAuthenticationService, this.runDirectory);
        this.session = args.network.session;
        this.gameProfileFuture = this.offlineDeveloperMode ? CompletableFuture.completedFuture(null) : CompletableFuture.supplyAsync(() -> this.apiServices.sessionService().fetchProfile(this.session.getUuidOrNull(), true), Util.getDownloadWorkerExecutor());
        this.userApiService = this.createUserApiService(yggdrasilAuthenticationService, args);
        this.userPropertiesFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return this.userApiService.fetchProperties();
            } catch (AuthenticationException authenticationException) {
                LOGGER.error("Failed to fetch user properties", authenticationException);
                return UserApiService.OFFLINE_PROPERTIES;
            }
        }, Util.getDownloadWorkerExecutor());
        LOGGER.info("Setting user: {}", (Object)this.session.getUsername());
        LOGGER.debug("(Session ID is {})", (Object)this.session.getSessionId());
        this.isDemo = args.game.demo;
        this.multiplayerEnabled = !args.game.multiplayerDisabled;
        this.onlineChatEnabled = !args.game.onlineChatDisabled;
        this.server = null;
        KeybindTranslations.setFactory(KeyBinding::getLocalizedName);
        this.dataFixer = Schemas.getFixer();
        this.thread = Thread.currentThread();
        this.options = new GameOptions(this, this.runDirectory);
        this.debugHudEntryList = new DebugHudProfile(this.runDirectory);
        this.toastManager = new ToastManager(this, this.options);
        boolean bl = this.options.startedCleanly;
        this.options.startedCleanly = false;
        this.options.write();
        this.running = true;
        this.tutorialManager = new TutorialManager(this, this.options);
        this.creativeHotbarStorage = new HotbarStorage(path, this.dataFixer);
        LOGGER.info("Backend library: {}", (Object)RenderSystem.getBackendDescription());
        WindowSettings lv3 = args.windowSettings;
        if (this.options.overrideHeight > 0 && this.options.overrideWidth > 0) {
            lv3 = args.windowSettings.withDimensions(this.options.overrideWidth, this.options.overrideHeight);
        }
        if (!bl) {
            lv3 = lv3.withFullscreen(false);
            this.options.fullscreenResolution = null;
            LOGGER.warn("Detected unexpected shutdown during last game startup: resetting fullscreen mode");
        }
        Util.nanoTimeSupplier = RenderSystem.initBackendSystem();
        this.windowProvider = new WindowProvider(this);
        this.window = this.windowProvider.createWindow(lv3, this.options.fullscreenResolution, this.getWindowTitle());
        this.onWindowFocusChanged(true);
        this.window.setCloseCallback(new Runnable(){
            private boolean closed;

            @Override
            public void run() {
                if (!this.closed) {
                    this.closed = true;
                    ClientWatchdog.shutdownClient(args.directories.runDir, MinecraftClient.this.thread.threadId());
                }
            }
        });
        GameLoadTimeEvent.INSTANCE.stopTimer(TelemetryEventProperty.LOAD_TIME_PRE_WINDOW_MS);
        try {
            this.window.setIcon(this.defaultResourcePack, SharedConstants.getGameVersion().stable() ? Icons.RELEASE : Icons.SNAPSHOT);
        } catch (IOException iOException) {
            LOGGER.error("Couldn't set icon", iOException);
        }
        this.mouse = new Mouse(this);
        this.mouse.setup(this.window);
        this.keyboard = new Keyboard(this);
        this.keyboard.setup(this.window);
        RenderSystem.initRenderer(this.window.getHandle(), this.options.glDebugVerbosity, SharedConstants.SYNCHRONOUS_GL_LOGS, (id, type) -> this.getShaderLoader().getSource((Identifier)id, (ShaderType)((Object)type)), args.game.renderDebugLabels);
        LOGGER.info("Using optional rendering extensions: {}", (Object)String.join((CharSequence)", ", RenderSystem.getDevice().getEnabledExtensions()));
        this.framebuffer = new WindowFramebuffer(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
        this.resourceManager = new ReloadableResourceManagerImpl(ResourceType.CLIENT_RESOURCES);
        this.resourcePackManager.scanPacks();
        this.options.addResourcePackProfilesToManager(this.resourcePackManager);
        this.languageManager = new LanguageManager(this.options.language, translationStorage -> {
            if (this.player != null) {
                this.player.networkHandler.refreshSearchManager();
            }
        });
        this.resourceManager.registerReloader(this.languageManager);
        this.textureManager = new TextureManager(this.resourceManager);
        this.resourceManager.registerReloader(this.textureManager);
        this.shaderLoader = new ShaderLoader(this.textureManager, this::onShaderResourceReloadFailure);
        this.resourceManager.registerReloader(this.shaderLoader);
        PlayerSkinTextureDownloader lv4 = new PlayerSkinTextureDownloader(this.networkProxy, this.textureManager, this);
        this.skinProvider = new PlayerSkinProvider(file.toPath().resolve("skins"), this.apiServices, lv4, this);
        this.levelStorage = new LevelStorage(path.resolve("saves"), path.resolve("backups"), this.symlinkFinder, this.dataFixer);
        this.commandHistoryManager = new CommandHistoryManager(path);
        this.musicTracker = new MusicTracker(this);
        this.soundManager = new SoundManager(this.options, this.musicTracker);
        this.resourceManager.registerReloader(this.soundManager);
        this.splashTextLoader = new SplashTextResourceSupplier(this.session);
        this.resourceManager.registerReloader(this.splashTextLoader);
        this.atlasManager = new AtlasManager(this.textureManager, this.options.getMipmapLevels().getValue());
        this.resourceManager.registerReloader(this.atlasManager);
        ClientPlayerProfileResolver lv5 = new ClientPlayerProfileResolver(this, this.apiServices.profileResolver());
        this.playerSkinCache = new PlayerSkinCache(this.textureManager, this.skinProvider, lv5);
        ClientMannequinEntity.setFactory(this.playerSkinCache);
        this.fontManager = new FontManager(this.textureManager, this.atlasManager, this.playerSkinCache);
        this.textRenderer = this.fontManager.createTextRenderer();
        this.advanceValidatingTextRenderer = this.fontManager.createAdvanceValidatingTextRenderer();
        this.resourceManager.registerReloader(this.fontManager);
        this.onFontOptionsChanged();
        this.resourceManager.registerReloader(new GrassColormapResourceSupplier());
        this.resourceManager.registerReloader(new FoliageColormapResourceSupplier());
        this.resourceManager.registerReloader(new DryFoliageColormapResourceSupplier());
        this.window.setPhase("Startup");
        RenderSystem.setupDefaultState();
        this.window.setPhase("Post startup");
        this.blockColors = BlockColors.create();
        this.bakedModelManager = new BakedModelManager(this.blockColors, this.atlasManager, this.playerSkinCache);
        this.resourceManager.registerReloader(this.bakedModelManager);
        EquipmentModelLoader lv6 = new EquipmentModelLoader();
        this.resourceManager.registerReloader(lv6);
        this.itemModelManager = new ItemModelManager(this.bakedModelManager);
        this.itemRenderer = new ItemRenderer();
        this.mapTextureManager = new MapTextureManager(this.textureManager);
        this.mapRenderer = new MapRenderer(this.atlasManager, this.mapTextureManager);
        try {
            int i = Runtime.getRuntime().availableProcessors();
            Tessellator.initialize();
            this.bufferBuilders = new BufferBuilderStorage(i);
        } catch (OutOfMemoryError outOfMemoryError) {
            TinyFileDialogs.tinyfd_messageBox("Minecraft", "Oh no! The game was unable to allocate memory off-heap while trying to start. You may try to free some memory by closing other applications on your computer, check that your system meets the minimum requirements, and try again. If the problem persists, please visit: " + String.valueOf(Urls.MINECRAFT_SUPPORT), "ok", "error", true);
            throw new GlException("Unable to allocate render buffers", outOfMemoryError);
        }
        this.socialInteractionsManager = new SocialInteractionsManager(this, this.userApiService);
        this.blockRenderManager = new BlockRenderManager(this.bakedModelManager.getBlockModels(), this.atlasManager, this.bakedModelManager.getBlockEntityModelsSupplier(), this.blockColors);
        this.resourceManager.registerReloader(this.blockRenderManager);
        this.entityRenderManager = new EntityRenderManager(this, this.textureManager, this.itemModelManager, this.itemRenderer, this.mapRenderer, this.blockRenderManager, this.atlasManager, this.textRenderer, this.options, this.bakedModelManager.getEntityModelsSupplier(), lv6, this.playerSkinCache);
        this.resourceManager.registerReloader(this.entityRenderManager);
        this.blockEntityRenderManager = new BlockEntityRenderManager(this.textRenderer, this.bakedModelManager.getEntityModelsSupplier(), this.blockRenderManager, this.itemModelManager, this.itemRenderer, this.entityRenderManager, this.atlasManager, this.playerSkinCache);
        this.resourceManager.registerReloader(this.blockEntityRenderManager);
        this.particleSpriteManager = new ParticleSpriteManager();
        this.resourceManager.registerReloader(this.particleSpriteManager);
        this.particleManager = new ParticleManager(this.world, this.particleSpriteManager);
        this.particleSpriteManager.setOnPreparedTask(this.particleManager::clearParticles);
        this.waypointStyleAssetManager = new WaypointStyleAssetManager();
        this.resourceManager.registerReloader(this.waypointStyleAssetManager);
        this.gameRenderer = new GameRenderer(this, this.entityRenderManager.getHeldItemRenderer(), this.bufferBuilders, this.blockRenderManager);
        this.worldRenderer = new WorldRenderer(this, this.entityRenderManager, this.blockEntityRenderManager, this.bufferBuilders, this.gameRenderer.getEntityRenderStates(), this.gameRenderer.getEntityRenderDispatcher());
        this.resourceManager.registerReloader(this.worldRenderer);
        this.resourceManager.registerReloader(this.worldRenderer.getCloudRenderer());
        this.videoWarningManager = new VideoWarningManager();
        this.resourceManager.registerReloader(this.videoWarningManager);
        this.resourceManager.registerReloader(this.regionalComplianciesManager);
        this.inGameHud = new InGameHud(this);
        RealmsClient lv7 = RealmsClient.createRealmsClient(this);
        this.realmsPeriodicCheckers = new RealmsPeriodicCheckers(lv7);
        RenderSystem.setErrorCallback(this::handleGlErrorByDisableVsync);
        if (this.framebuffer.textureWidth != this.window.getFramebufferWidth() || this.framebuffer.textureHeight != this.window.getFramebufferHeight()) {
            StringBuilder stringBuilder = new StringBuilder("Recovering from unsupported resolution (" + this.window.getFramebufferWidth() + "x" + this.window.getFramebufferHeight() + ").\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).");
            try {
                GpuDevice gpuDevice = RenderSystem.getDevice();
                List<String> list = gpuDevice.getLastDebugMessages();
                if (!list.isEmpty()) {
                    stringBuilder.append("\n\nReported GL debug messages:\n").append(String.join((CharSequence)"\n", list));
                }
            } catch (Throwable gpuDevice) {
                // empty catch block
            }
            this.window.setWindowedSize(this.framebuffer.textureWidth, this.framebuffer.textureHeight);
            TinyFileDialogs.tinyfd_messageBox("Minecraft", stringBuilder.toString(), "ok", "error", false);
        } else if (this.options.getFullscreen().getValue().booleanValue() && !this.window.isFullscreen()) {
            if (bl) {
                this.window.toggleFullscreen();
                this.options.getFullscreen().setValue(this.window.isFullscreen());
            } else {
                this.options.getFullscreen().setValue(false);
            }
        }
        this.window.setVsync(this.options.getEnableVsync().getValue());
        this.window.setRawMouseMotion(this.options.getRawMouseInput().getValue());
        this.window.setAllowCursorChanges(this.options.getAllowCursorChanges().getValue());
        this.window.logOnGlError();
        this.onResolutionChanged();
        this.gameRenderer.preloadPrograms(this.defaultResourcePack.getFactory());
        this.telemetryManager = new TelemetryManager(this, this.userApiService, this.session);
        this.profileKeys = this.offlineDeveloperMode ? ProfileKeys.MISSING : ProfileKeys.create(this.userApiService, this.session, path);
        this.narratorManager = new NarratorManager(this);
        this.narratorManager.checkNarratorLibrary(this.options.getNarrator().getValue() != NarratorMode.OFF);
        this.messageHandler = new MessageHandler(this);
        this.messageHandler.setChatDelay(this.options.getChatDelay().getValue());
        this.abuseReportContext = AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), this.userApiService);
        TitleScreen.registerTextures(this.textureManager);
        SplashOverlay.init(this.textureManager);
        this.gameRenderer.getRotatingPanoramaRenderer().registerTextures(this.textureManager);
        this.setScreen(new MessageScreen(Text.translatable("gui.loadingMinecraft")));
        List<ResourcePack> list2 = this.resourcePackManager.createResourcePacks();
        this.resourceReloadLogger.reload(ResourceReloadLogger.ReloadReason.INITIAL, list2);
        ResourceReload lv8 = this.resourceManager.reload(Util.getMainWorkerExecutor().named("resourceLoad"), this, COMPLETED_UNIT_FUTURE, list2);
        GameLoadTimeEvent.INSTANCE.startTimer(TelemetryEventProperty.LOAD_TIME_LOADING_OVERLAY_MS);
        LoadingContext lv9 = new LoadingContext(lv7, args.quickPlay);
        this.setOverlay(new SplashOverlay(this, lv8, error -> Util.ifPresentOrElse(error, throwable -> this.handleResourceReloadException((Throwable)throwable, lv9), () -> {
            if (SharedConstants.isDevelopment) {
                this.checkGameData();
            }
            this.resourceReloadLogger.finish();
            this.onFinishedLoading(lv9);
        }), false));
        this.quickPlayLogger = QuickPlayLogger.create(args.quickPlay.logPath());
        this.inactivityFpsLimiter = new InactivityFpsLimiter(this.options, this);
        this.tickTimeTracker = new TickTimeTracker(Util.nanoTimeSupplier, () -> this.trackingTick, this.inactivityFpsLimiter::shouldDisableProfilerTimeout);
        this.tracyFrameCapturer = TracyClient.isAvailable() && args.game.tracyEnabled ? new TracyFrameCapturer() : null;
        this.packetApplyBatcher = new PacketApplyBatcher(this.thread);
    }

    public boolean isShiftPressed() {
        Window lv = this.getWindow();
        return InputUtil.isKeyPressed(lv, InputUtil.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(lv, InputUtil.GLFW_KEY_RIGHT_SHIFT);
    }

    public boolean isCtrlPressed() {
        Window lv = this.getWindow();
        return InputUtil.isKeyPressed(lv, SystemKeycodes.LEFT_CTRL) || InputUtil.isKeyPressed(lv, SystemKeycodes.RIGHT_CTRL);
    }

    public boolean isAltPressed() {
        Window lv = this.getWindow();
        return InputUtil.isKeyPressed(lv, InputUtil.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(lv, InputUtil.GLFW_KEY_RIGHT_ALT);
    }

    private void onFinishedLoading(@Nullable LoadingContext loadingContext) {
        if (!this.finishedLoading) {
            this.finishedLoading = true;
            this.collectLoadTimes(loadingContext);
        }
    }

    private void collectLoadTimes(@Nullable LoadingContext loadingContext) {
        Runnable runnable = this.onInitFinished(loadingContext);
        GameLoadTimeEvent.INSTANCE.stopTimer(TelemetryEventProperty.LOAD_TIME_LOADING_OVERLAY_MS);
        GameLoadTimeEvent.INSTANCE.stopTimer(TelemetryEventProperty.LOAD_TIME_TOTAL_TIME_MS);
        GameLoadTimeEvent.INSTANCE.send(this.telemetryManager.getSender());
        runnable.run();
        this.options.startedCleanly = true;
        this.options.write();
    }

    public boolean isFinishedLoading() {
        return this.finishedLoading;
    }

    private Runnable onInitFinished(@Nullable LoadingContext loadingContext) {
        ArrayList<Function<Runnable, Screen>> list = new ArrayList<Function<Runnable, Screen>>();
        boolean bl = this.createInitScreens(list);
        Runnable runnable = () -> {
            if (loadingContext != null && arg.quickPlayData.isEnabled()) {
                QuickPlay.startQuickPlay(this, arg.quickPlayData.variant(), loadingContext.realmsClient());
            } else {
                this.setScreen(new TitleScreen(true, new LogoDrawer(bl)));
            }
        };
        for (Function<Runnable, Screen> function : Lists.reverse(list)) {
            Screen lv = function.apply(runnable);
            runnable = () -> this.setScreen(lv);
        }
        return runnable;
    }

    private boolean createInitScreens(List<Function<Runnable, Screen>> list) {
        com.mojang.authlib.yggdrasil.ProfileResult profileResult;
        BanDetails banDetails;
        boolean bl = false;
        if (this.options.onboardAccessibility || SharedConstants.FORCE_ONBOARDING_SCREEN) {
            list.add(onClose -> new AccessibilityOnboardingScreen(this.options, (Runnable)onClose));
            bl = true;
        }
        if ((banDetails = this.getMultiplayerBanDetails()) != null) {
            list.add(onClose -> Bans.createBanScreen(confirmed -> {
                if (confirmed) {
                    Util.getOperatingSystem().open(Urls.JAVA_MODERATION);
                }
                onClose.run();
            }, banDetails));
        }
        if ((profileResult = this.gameProfileFuture.join()) != null) {
            GameProfile gameProfile = profileResult.profile();
            Set<ProfileActionType> set = profileResult.actions();
            if (set.contains((Object)ProfileActionType.FORCED_NAME_CHANGE)) {
                list.add(onClose -> Bans.createUsernameBanScreen(gameProfile.name(), onClose));
            }
            if (set.contains((Object)ProfileActionType.USING_BANNED_SKIN)) {
                list.add(Bans::createSkinBanScreen);
            }
        }
        return bl;
    }

    private static boolean isCountrySetTo(Object country) {
        try {
            return Locale.getDefault().getISO3Country().equals(country);
        } catch (MissingResourceException missingResourceException) {
            return false;
        }
    }

    public void updateWindowTitle() {
        this.window.setTitle(this.getWindowTitle());
    }

    private String getWindowTitle() {
        StringBuilder stringBuilder = new StringBuilder("Minecraft");
        if (MinecraftClient.getModStatus().isModded()) {
            stringBuilder.append("*");
        }
        stringBuilder.append(" ");
        stringBuilder.append(SharedConstants.getGameVersion().name());
        ClientPlayNetworkHandler lv = this.getNetworkHandler();
        if (lv != null && lv.getConnection().isOpen()) {
            stringBuilder.append(" - ");
            ServerInfo lv2 = this.getCurrentServerEntry();
            if (this.server != null && !this.server.isRemote()) {
                stringBuilder.append(I18n.translate("title.singleplayer", new Object[0]));
            } else if (lv2 != null && lv2.isRealm()) {
                stringBuilder.append(I18n.translate("title.multiplayer.realms", new Object[0]));
            } else if (this.server != null || lv2 != null && lv2.isLocal()) {
                stringBuilder.append(I18n.translate("title.multiplayer.lan", new Object[0]));
            } else {
                stringBuilder.append(I18n.translate("title.multiplayer.other", new Object[0]));
            }
        }
        return stringBuilder.toString();
    }

    private UserApiService createUserApiService(YggdrasilAuthenticationService authService, RunArgs runArgs) {
        if (runArgs.game.offlineDeveloperMode) {
            return UserApiService.OFFLINE;
        }
        return authService.createUserApiService(runArgs.network.session.getAccessToken());
    }

    public boolean isOfflineDeveloperMode() {
        return this.offlineDeveloperMode;
    }

    public static ModStatus getModStatus() {
        return ModStatus.check("vanilla", ClientBrandRetriever::getClientModName, "Client", MinecraftClient.class);
    }

    private void handleResourceReloadException(Throwable throwable, @Nullable LoadingContext loadingContext) {
        if (this.resourcePackManager.getEnabledIds().size() > 1) {
            this.onResourceReloadFailure(throwable, null, loadingContext);
        } else {
            Util.throwUnchecked(throwable);
        }
    }

    public void onResourceReloadFailure(Throwable exception, @Nullable Text resourceName, @Nullable LoadingContext loadingContext) {
        LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", exception);
        this.resourceReloadLogger.recover(exception);
        this.serverResourcePackLoader.onReloadFailure();
        this.resourcePackManager.setEnabledProfiles(Collections.emptyList());
        this.options.resourcePacks.clear();
        this.options.incompatibleResourcePacks.clear();
        this.options.write();
        this.reloadResources(true, loadingContext).thenRunAsync(() -> this.showResourceReloadFailureToast(resourceName), this);
    }

    private void onForcedResourceReloadFailure() {
        this.setOverlay(null);
        if (this.world != null) {
            this.world.disconnect(ClientWorld.QUITTING_MULTIPLAYER_TEXT);
            this.disconnectWithProgressScreen();
        }
        this.setScreen(new TitleScreen());
        this.showResourceReloadFailureToast(null);
    }

    private void showResourceReloadFailureToast(@Nullable Text description) {
        ToastManager lv = this.getToastManager();
        SystemToast.show(lv, SystemToast.Type.PACK_LOAD_FAILURE, Text.translatable("resourcePack.load_fail"), description);
    }

    public void onShaderResourceReloadFailure(Exception exception) {
        if (!this.resourcePackManager.hasOptionalProfilesEnabled()) {
            if (this.resourcePackManager.getEnabledIds().size() <= 1) {
                LOGGER.error(LogUtils.FATAL_MARKER, exception.getMessage(), exception);
                this.printCrashReport(new CrashReport(exception.getMessage(), exception));
            } else {
                this.send(this::onForcedResourceReloadFailure);
            }
            return;
        }
        this.onResourceReloadFailure(exception, Text.translatable("resourcePack.runtime_failure"), null);
    }

    public void run() {
        this.thread = Thread.currentThread();
        if (Runtime.getRuntime().availableProcessors() > 4) {
            this.thread.setPriority(10);
        }
        DiscontinuousFrame discontinuousFrame = TracyClient.createDiscontinuousFrame("Client Tick");
        try {
            boolean bl = false;
            while (this.running) {
                this.printCrashReport();
                try {
                    TickDurationMonitor lv = TickDurationMonitor.create("Renderer");
                    boolean bl2 = this.getDebugHud().shouldShowRenderingChart();
                    try (Profilers.Scoped lv2 = Profilers.using(this.startMonitor(bl2, lv));){
                        this.recorder.startTick();
                        discontinuousFrame.start();
                        this.render(!bl);
                        discontinuousFrame.end();
                        this.recorder.endTick();
                    }
                    this.endMonitor(bl2, lv);
                } catch (OutOfMemoryError outOfMemoryError) {
                    if (bl) {
                        throw outOfMemoryError;
                    }
                    this.cleanUpAfterCrash();
                    this.setScreen(new OutOfMemoryScreen());
                    System.gc();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", outOfMemoryError);
                    bl = true;
                }
            }
        } catch (CrashException lv3) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", lv3);
            this.printCrashReport(lv3.getReport());
        } catch (Throwable throwable) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", throwable);
            this.printCrashReport(new CrashReport("Unexpected error", throwable));
        }
    }

    void onFontOptionsChanged() {
        this.fontManager.setActiveFilters(this.options);
    }

    private void handleGlErrorByDisableVsync(int error, long description) {
        this.options.getEnableVsync().setValue(false);
        this.options.write();
    }

    public Framebuffer getFramebuffer() {
        return this.framebuffer;
    }

    public String getGameVersion() {
        return this.gameVersion;
    }

    public String getVersionType() {
        return this.versionType;
    }

    public void setCrashReportSupplierAndAddDetails(CrashReport crashReport) {
        this.crashReportSupplier = () -> this.addDetailsToCrashReport(crashReport);
    }

    public void setCrashReportSupplier(CrashReport crashReport) {
        this.crashReportSupplier = () -> crashReport;
    }

    private void printCrashReport() {
        if (this.crashReportSupplier != null) {
            MinecraftClient.printCrashReport(this, this.runDirectory, this.crashReportSupplier.get());
        }
    }

    public void printCrashReport(CrashReport crashReport) {
        CrashMemoryReserve.releaseMemory();
        CrashReport lv = this.addDetailsToCrashReport(crashReport);
        this.cleanUpAfterCrash();
        MinecraftClient.printCrashReport(this, this.runDirectory, lv);
    }

    public static int saveCrashReport(File runDir, CrashReport crashReport) {
        Path path = runDir.toPath().resolve("crash-reports");
        Path path2 = path.resolve("crash-" + Util.getFormattedCurrentTime() + "-client.txt");
        Bootstrap.println(crashReport.asString(ReportType.MINECRAFT_CRASH_REPORT));
        if (crashReport.getFile() != null) {
            Bootstrap.println("#@!@# Game crashed! Crash report saved to: #@!@# " + String.valueOf(crashReport.getFile().toAbsolutePath()));
            return -1;
        }
        if (crashReport.writeToFile(path2, ReportType.MINECRAFT_CRASH_REPORT)) {
            Bootstrap.println("#@!@# Game crashed! Crash report saved to: #@!@# " + String.valueOf(path2.toAbsolutePath()));
            return -1;
        }
        Bootstrap.println("#@?@# Game crashed! Crash report could not be saved. #@?@#");
        return -2;
    }

    public static void printCrashReport(@Nullable MinecraftClient client, File runDirectory, CrashReport crashReport) {
        int i = MinecraftClient.saveCrashReport(runDirectory, crashReport);
        if (client != null) {
            client.soundManager.stopAbruptly();
        }
        System.exit(i);
    }

    public boolean forcesUnicodeFont() {
        return this.options.getForceUnicodeFont().getValue();
    }

    public CompletableFuture<Void> reloadResources() {
        return this.reloadResources(false, null);
    }

    private CompletableFuture<Void> reloadResources(boolean force, @Nullable LoadingContext loadingContext) {
        if (this.resourceReloadFuture != null) {
            return this.resourceReloadFuture;
        }
        CompletableFuture<Void> completableFuture = new CompletableFuture<Void>();
        if (!force && this.overlay instanceof SplashOverlay) {
            this.resourceReloadFuture = completableFuture;
            return completableFuture;
        }
        this.resourcePackManager.scanPacks();
        List<ResourcePack> list = this.resourcePackManager.createResourcePacks();
        if (!force) {
            this.resourceReloadLogger.reload(ResourceReloadLogger.ReloadReason.MANUAL, list);
        }
        this.setOverlay(new SplashOverlay(this, this.resourceManager.reload(Util.getMainWorkerExecutor().named("resourceLoad"), this, COMPLETED_UNIT_FUTURE, list), error -> Util.ifPresentOrElse(error, throwable -> {
            if (force) {
                this.serverResourcePackLoader.onForcedReloadFailure();
                this.onForcedResourceReloadFailure();
            } else {
                this.handleResourceReloadException((Throwable)throwable, loadingContext);
            }
        }, () -> {
            this.worldRenderer.reload();
            this.resourceReloadLogger.finish();
            this.serverResourcePackLoader.onReloadSuccess();
            completableFuture.complete(null);
            this.onFinishedLoading(loadingContext);
        }), !force));
        return completableFuture;
    }

    private void checkGameData() {
        boolean bl = false;
        BlockModels lv = this.getBlockRenderManager().getModels();
        BlockStateModel lv2 = lv.getModelManager().getMissingModel();
        for (Block lv3 : Registries.BLOCK) {
            for (BlockState lv4 : lv3.getStateManager().getStates()) {
                BlockStateModel lv5;
                if (lv4.getRenderType() != BlockRenderType.MODEL || (lv5 = lv.getModel(lv4)) != lv2) continue;
                LOGGER.debug("Missing model for: {}", (Object)lv4);
                bl = true;
            }
        }
        Sprite lv6 = lv2.particleSprite();
        for (Block lv7 : Registries.BLOCK) {
            for (BlockState lv8 : lv7.getStateManager().getStates()) {
                Sprite lv9 = lv.getModelParticleSprite(lv8);
                if (lv8.isAir() || lv9 != lv6) continue;
                LOGGER.debug("Missing particle icon for: {}", (Object)lv8);
            }
        }
        Registries.ITEM.streamEntries().forEach(item -> {
            Item lv = (Item)item.value();
            String string = lv.getTranslationKey();
            String string2 = Text.translatable(string).getString();
            if (string2.toLowerCase(Locale.ROOT).equals(lv.getTranslationKey())) {
                LOGGER.debug("Missing translation for: {} {} {}", item.registryKey().getValue(), string, lv);
            }
        });
        bl |= HandledScreens.isMissingScreens();
        if (bl |= EntityRendererFactories.isMissingRendererFactories()) {
            throw new IllegalStateException("Your game data is foobar, fix the errors above!");
        }
    }

    public LevelStorage getLevelStorage() {
        return this.levelStorage;
    }

    public void openChatScreen(ChatHud.ChatMethod method) {
        ChatRestriction lv = this.getChatRestriction();
        if (!lv.allowsChat(this.isInSingleplayer())) {
            if (this.inGameHud.shouldShowChatDisabledScreen()) {
                this.inGameHud.setCanShowChatDisabledScreen(false);
                this.setScreen(new ConfirmLinkScreen(confirmed -> {
                    if (confirmed) {
                        Util.getOperatingSystem().open(Urls.JAVA_ACCOUNT_SETTINGS);
                    }
                    this.setScreen(null);
                }, ChatRestriction.MORE_INFO_TEXT, Urls.JAVA_ACCOUNT_SETTINGS, true));
            } else {
                Text lv2 = lv.getDescription();
                this.inGameHud.setOverlayMessage(lv2, false);
                this.narratorManager.narrateSystemImmediately(lv2);
                this.inGameHud.setCanShowChatDisabledScreen(lv == ChatRestriction.DISABLED_BY_PROFILE);
            }
        } else {
            this.inGameHud.getChatHud().setClientScreen(method, ChatScreen::new);
        }
    }

    public void setScreen(@Nullable Screen screen) {
        if (SharedConstants.isDevelopment && Thread.currentThread() != this.thread) {
            LOGGER.error("setScreen called from non-game thread");
        }
        if (this.currentScreen != null) {
            this.currentScreen.removed();
        } else {
            this.setNavigationType(GuiNavigationType.NONE);
        }
        if (screen == null) {
            if (this.disconnecting) {
                throw new IllegalStateException("Trying to return to in-game GUI during disconnection");
            }
            if (this.world == null) {
                screen = new TitleScreen();
            } else if (this.player.isDead()) {
                if (this.player.showsDeathScreen()) {
                    screen = new DeathScreen(null, this.world.getLevelProperties().isHardcore());
                } else {
                    this.player.requestRespawn();
                }
            } else {
                screen = this.inGameHud.getChatHud().removeScreen();
            }
        }
        this.currentScreen = screen;
        if (this.currentScreen != null) {
            this.currentScreen.onDisplayed();
        }
        if (screen != null) {
            this.mouse.unlockCursor();
            KeyBinding.unpressAll();
            screen.init(this, this.window.getScaledWidth(), this.window.getScaledHeight());
            this.skipGameRender = false;
        } else {
            if (this.world != null) {
                KeyBinding.restoreToggleStates();
            }
            this.soundManager.resumeAll();
            this.mouse.lockCursor();
        }
        this.updateWindowTitle();
    }

    public void setOverlay(@Nullable Overlay overlay) {
        this.overlay = overlay;
    }

    public void stop() {
        try {
            LOGGER.info("Stopping!");
            try {
                this.narratorManager.destroy();
            } catch (Throwable throwable) {
                // empty catch block
            }
            try {
                if (this.world != null) {
                    this.world.disconnect(ClientWorld.QUITTING_MULTIPLAYER_TEXT);
                }
                this.disconnectWithProgressScreen();
            } catch (Throwable throwable) {
                // empty catch block
            }
            if (this.currentScreen != null) {
                this.currentScreen.removed();
            }
            this.close();
        } finally {
            Util.nanoTimeSupplier = System::nanoTime;
            if (this.crashReportSupplier == null) {
                System.exit(0);
            }
        }
    }

    @Override
    public void close() {
        if (this.currentGlTimerQuery != null) {
            this.currentGlTimerQuery.close();
        }
        try {
            this.telemetryManager.close();
            this.regionalComplianciesManager.close();
            this.atlasManager.close();
            this.fontManager.close();
            this.gameRenderer.close();
            this.shaderLoader.close();
            this.worldRenderer.close();
            this.soundManager.close();
            this.mapTextureManager.close();
            this.textureManager.close();
            this.resourceManager.close();
            if (this.tracyFrameCapturer != null) {
                this.tracyFrameCapturer.close();
            }
            FreeTypeUtil.release();
            Util.shutdownExecutors();
            RenderSystem.getDevice().close();
        } catch (Throwable throwable) {
            LOGGER.error("Shutdown failure!", throwable);
            throw throwable;
        } finally {
            this.windowProvider.close();
            this.window.close();
        }
    }

    private void render(boolean tick) {
        boolean bl2;
        this.window.setPhase("Pre render");
        if (this.window.shouldClose()) {
            this.scheduleStop();
        }
        if (this.resourceReloadFuture != null && !(this.overlay instanceof SplashOverlay)) {
            CompletableFuture<Void> completableFuture = this.resourceReloadFuture;
            this.resourceReloadFuture = null;
            this.reloadResources().thenRun(() -> completableFuture.complete(null));
        }
        int i = this.renderTickCounter.beginRenderTick(Util.getMeasuringTimeMs(), tick);
        Profiler lv = Profilers.get();
        if (tick) {
            lv.push("scheduledPacketProcessing");
            this.packetApplyBatcher.apply();
            lv.swap("scheduledExecutables");
            this.runTasks();
            lv.swap("tick");
            for (int j = 0; j < Math.min(10, i); ++j) {
                lv.visit("clientTick");
                this.tick();
            }
            lv.pop();
        }
        this.window.setPhase("Render");
        lv.push("gpuAsync");
        RenderSystem.executePendingTasks();
        lv.swap("sound");
        this.soundManager.updateListenerPosition(this.gameRenderer.getCamera());
        lv.swap("toasts");
        this.toastManager.update();
        lv.swap("mouse");
        this.mouse.tick();
        lv.swap("render");
        long l = Util.getMeasuringTimeNano();
        if (this.debugHudEntryList.isEntryVisible(DebugHudEntries.GPU_UTILIZATION) || this.recorder.isActive()) {
            boolean bl = bl2 = (this.currentGlTimerQuery == null || this.currentGlTimerQuery.isResultAvailable()) && !GlTimer.getInstance().isRunning();
            if (bl2) {
                GlTimer.getInstance().beginProfile();
            }
        } else {
            bl2 = false;
            this.gpuUtilizationPercentage = 0.0;
        }
        Framebuffer lv2 = this.getFramebuffer();
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(lv2.getColorAttachment(), 0, lv2.getDepthAttachment(), 1.0);
        lv.push("gameRenderer");
        if (!this.skipGameRender) {
            this.gameRenderer.render(this.renderTickCounter, tick);
        }
        lv.swap("blit");
        if (!this.window.hasZeroWidthOrHeight()) {
            lv2.blitToScreen();
        }
        this.renderTime = Util.getMeasuringTimeNano() - l;
        if (bl2) {
            this.currentGlTimerQuery = GlTimer.getInstance().endProfile();
        }
        lv.swap("updateDisplay");
        if (this.tracyFrameCapturer != null) {
            this.tracyFrameCapturer.upload();
            this.tracyFrameCapturer.capture(lv2);
        }
        this.window.swapBuffers(this.tracyFrameCapturer);
        int k = this.inactivityFpsLimiter.update();
        if (k < 260) {
            RenderSystem.limitDisplayFPS(k);
        }
        lv.pop();
        lv.swap("yield");
        Thread.yield();
        lv.pop();
        this.window.setPhase("Post render");
        ++this.fpsCounter;
        boolean bl3 = this.paused;
        boolean bl = this.paused = this.isIntegratedServerRunning() && (this.currentScreen != null && this.currentScreen.shouldPause() || this.overlay != null && this.overlay.pausesGame()) && !this.server.isRemote();
        if (!bl3 && this.paused) {
            this.soundManager.pauseAllExcept(SoundCategory.MUSIC, SoundCategory.UI);
        }
        this.renderTickCounter.tick(this.paused);
        this.renderTickCounter.setTickFrozen(!this.shouldTick());
        long m = Util.getMeasuringTimeNano();
        long n = m - this.lastMetricsSampleTime;
        if (bl2) {
            this.metricsSampleDuration = n;
        }
        this.getDebugHud().pushToFrameLog(n);
        this.lastMetricsSampleTime = m;
        lv.push("fpsUpdate");
        if (this.currentGlTimerQuery != null && this.currentGlTimerQuery.isResultAvailable()) {
            this.gpuUtilizationPercentage = (double)this.currentGlTimerQuery.queryResult() * 100.0 / (double)this.metricsSampleDuration;
        }
        while (Util.getMeasuringTimeMs() >= this.nextDebugInfoUpdateTime + 1000L) {
            currentFps = this.fpsCounter;
            this.nextDebugInfoUpdateTime += 1000L;
            this.fpsCounter = 0;
        }
        lv.pop();
    }

    private Profiler startMonitor(boolean active, @Nullable TickDurationMonitor monitor) {
        Profiler lv;
        if (!active) {
            this.tickTimeTracker.disable();
            if (!this.recorder.isActive() && monitor == null) {
                return DummyProfiler.INSTANCE;
            }
        }
        if (active) {
            if (!this.tickTimeTracker.isActive()) {
                this.trackingTick = 0;
                this.tickTimeTracker.enable();
            }
            ++this.trackingTick;
            lv = this.tickTimeTracker.getProfiler();
        } else {
            lv = DummyProfiler.INSTANCE;
        }
        if (this.recorder.isActive()) {
            lv = Profiler.union(lv, this.recorder.getProfiler());
        }
        return TickDurationMonitor.tickProfiler(lv, monitor);
    }

    private void endMonitor(boolean active, @Nullable TickDurationMonitor monitor) {
        if (monitor != null) {
            monitor.endTick();
        }
        PieChart lv = this.getDebugHud().getPieChart();
        if (active) {
            lv.setProfileResult(this.tickTimeTracker.getResult());
        } else {
            lv.setProfileResult(null);
        }
    }

    @Override
    public void onResolutionChanged() {
        int i = this.window.calculateScaleFactor(this.options.getGuiScale().getValue(), this.forcesUnicodeFont());
        this.window.setScaleFactor(i);
        if (this.currentScreen != null) {
            this.currentScreen.resize(this, this.window.getScaledWidth(), this.window.getScaledHeight());
        }
        Framebuffer lv = this.getFramebuffer();
        lv.resize(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
        this.gameRenderer.onResized(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
        this.mouse.onResolutionChanged();
    }

    @Override
    public void onCursorEnterChanged() {
        this.mouse.setResolutionChanged();
    }

    public int getCurrentFps() {
        return currentFps;
    }

    public long getRenderTime() {
        return this.renderTime;
    }

    private void cleanUpAfterCrash() {
        CrashMemoryReserve.releaseMemory();
        try {
            if (this.integratedServerRunning && this.server != null) {
                this.server.stop(true);
            }
            this.disconnectWithSavingScreen();
        } catch (Throwable throwable) {
            // empty catch block
        }
        System.gc();
    }

    public boolean toggleDebugProfiler(Consumer<Text> chatMessageSender) {
        Consumer<Path> consumer5;
        if (this.recorder.isActive()) {
            this.stopRecorder();
            return false;
        }
        Consumer<ProfileResult> consumer2 = result -> {
            if (result == EmptyProfileResult.INSTANCE) {
                return;
            }
            int i = result.getTickSpan();
            double d = (double)result.getTimeSpan() / (double)TimeHelper.SECOND_IN_NANOS;
            this.execute(() -> chatMessageSender.accept(Text.translatable("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d), i, String.format(Locale.ROOT, "%.2f", (double)i / d))));
        };
        Consumer<Path> consumer3 = path -> {
            MutableText lv = Text.literal(path.toString()).formatted(Formatting.UNDERLINE).styled(style -> style.withClickEvent(new ClickEvent.OpenFile(path.getParent())));
            this.execute(() -> chatMessageSender.accept(Text.translatable("debug.profiling.stop", lv)));
        };
        SystemDetails lv = MinecraftClient.addSystemDetailsToCrashReport(new SystemDetails(), this, this.languageManager, this.gameVersion, this.options);
        Consumer<List> consumer4 = files -> {
            Path path = this.saveProfilingResult(lv, (List<Path>)files);
            consumer3.accept(path);
        };
        if (this.server == null) {
            consumer5 = path -> consumer4.accept(ImmutableList.of(path));
        } else {
            this.server.addSystemDetails(lv);
            CompletableFuture completableFuture = new CompletableFuture();
            CompletableFuture completableFuture2 = new CompletableFuture();
            CompletableFuture.allOf(completableFuture, completableFuture2).thenRunAsync(() -> consumer4.accept(ImmutableList.of((Path)completableFuture.join(), (Path)completableFuture2.join())), Util.getIoWorkerExecutor());
            this.server.setupRecorder(result -> {}, completableFuture2::complete);
            consumer5 = completableFuture::complete;
        }
        this.recorder = DebugRecorder.of(new ClientSamplerSource(Util.nanoTimeSupplier, this.worldRenderer), Util.nanoTimeSupplier, Util.getIoWorkerExecutor(), new RecordDumper("client"), result -> {
            this.recorder = DummyRecorder.INSTANCE;
            consumer2.accept((ProfileResult)result);
        }, consumer5);
        return true;
    }

    private void stopRecorder() {
        this.recorder.stop();
        if (this.server != null) {
            this.server.stopRecorder();
        }
    }

    private void forceStopRecorder() {
        this.recorder.forceStop();
        if (this.server != null) {
            this.server.forceStopRecorder();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Path saveProfilingResult(SystemDetails details, List<Path> files) {
        Path path;
        ServerInfo lv;
        String string = this.isInSingleplayer() ? this.getServer().getSaveProperties().getLevelName() : ((lv = this.getCurrentServerEntry()) != null ? lv.name : "unknown");
        try {
            String string2 = String.format(Locale.ROOT, "%s-%s-%s", Util.getFormattedCurrentTime(), string, SharedConstants.getGameVersion().id());
            String string3 = PathUtil.getNextUniqueName(RecordDumper.DEBUG_PROFILING_DIRECTORY, string2, ".zip");
            path = RecordDumper.DEBUG_PROFILING_DIRECTORY.resolve(string3);
        } catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
        try (ZipCompressor lv2 = new ZipCompressor(path);){
            lv2.write(Paths.get("system.txt", new String[0]), details.collect());
            lv2.write(Paths.get("client", new String[0]).resolve(this.options.getOptionsFile().getName()), this.options.collectProfiledOptions());
            files.forEach(lv2::copyAll);
        } finally {
            for (Path path2 : files) {
                try {
                    FileUtils.forceDelete(path2.toFile());
                } catch (IOException iOException2) {
                    LOGGER.warn("Failed to delete temporary profiling result {}", (Object)path2, (Object)iOException2);
                }
            }
        }
        return path;
    }

    public void scheduleStop() {
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void openGameMenu(boolean pauseOnly) {
        boolean bl2;
        if (this.currentScreen != null) {
            return;
        }
        boolean bl = bl2 = this.isIntegratedServerRunning() && !this.server.isRemote();
        if (bl2) {
            this.setScreen(new GameMenuScreen(!pauseOnly));
        } else {
            this.setScreen(new GameMenuScreen(true));
        }
    }

    private void handleBlockBreaking(boolean breaking) {
        if (!breaking) {
            this.attackCooldown = 0;
        }
        if (this.attackCooldown > 0 || this.player.isUsingItem()) {
            return;
        }
        if (breaking && this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            Direction lv3;
            BlockHitResult lv = (BlockHitResult)this.crosshairTarget;
            BlockPos lv2 = lv.getBlockPos();
            if (!this.world.getBlockState(lv2).isAir() && this.interactionManager.updateBlockBreakingProgress(lv2, lv3 = lv.getSide())) {
                this.world.spawnBlockBreakingParticle(lv2, lv3);
                this.player.swingHand(Hand.MAIN_HAND);
            }
            return;
        }
        this.interactionManager.cancelBlockBreaking();
    }

    private boolean doAttack() {
        if (this.attackCooldown > 0) {
            return false;
        }
        if (this.crosshairTarget == null) {
            LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
            if (this.interactionManager.hasLimitedAttackSpeed()) {
                this.attackCooldown = 10;
            }
            return false;
        }
        if (this.player.isRiding()) {
            return false;
        }
        ItemStack lv = this.player.getStackInHand(Hand.MAIN_HAND);
        if (!lv.isItemEnabled(this.world.getEnabledFeatures())) {
            return false;
        }
        boolean bl = false;
        switch (this.crosshairTarget.getType()) {
            case ENTITY: {
                this.interactionManager.attackEntity(this.player, ((EntityHitResult)this.crosshairTarget).getEntity());
                break;
            }
            case BLOCK: {
                BlockHitResult lv2 = (BlockHitResult)this.crosshairTarget;
                BlockPos lv3 = lv2.getBlockPos();
                if (!this.world.getBlockState(lv3).isAir()) {
                    this.interactionManager.attackBlock(lv3, lv2.getSide());
                    if (!this.world.getBlockState(lv3).isAir()) break;
                    bl = true;
                    break;
                }
            }
            case MISS: {
                if (this.interactionManager.hasLimitedAttackSpeed()) {
                    this.attackCooldown = 10;
                }
                this.player.resetLastAttackedTicks();
            }
        }
        this.player.swingHand(Hand.MAIN_HAND);
        return bl;
    }

    private void doItemUse() {
        if (this.interactionManager.isBreakingBlock()) {
            return;
        }
        this.itemUseCooldown = 4;
        if (this.player.isRiding()) {
            return;
        }
        if (this.crosshairTarget == null) {
            LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
        }
        for (Hand lv : Hand.values()) {
            ActionResult lv10;
            ItemStack lv2 = this.player.getStackInHand(lv);
            if (!lv2.isItemEnabled(this.world.getEnabledFeatures())) {
                return;
            }
            if (this.crosshairTarget != null) {
                switch (this.crosshairTarget.getType()) {
                    case ENTITY: {
                        EntityHitResult lv3 = (EntityHitResult)this.crosshairTarget;
                        Entity lv4 = lv3.getEntity();
                        if (!this.world.getWorldBorder().contains(lv4.getBlockPos())) {
                            return;
                        }
                        ActionResult lv5 = this.interactionManager.interactEntityAtLocation(this.player, lv4, lv3, lv);
                        if (!lv5.isAccepted()) {
                            lv5 = this.interactionManager.interactEntity(this.player, lv4, lv);
                        }
                        if (!(lv5 instanceof ActionResult.Success)) break;
                        ActionResult.Success lv6 = (ActionResult.Success)lv5;
                        if (lv6.swingSource() == ActionResult.SwingSource.CLIENT) {
                            this.player.swingHand(lv);
                        }
                        return;
                    }
                    case BLOCK: {
                        BlockHitResult lv7 = (BlockHitResult)this.crosshairTarget;
                        int i = lv2.getCount();
                        ActionResult lv8 = this.interactionManager.interactBlock(this.player, lv, lv7);
                        if (lv8 instanceof ActionResult.Success) {
                            ActionResult.Success lv9 = (ActionResult.Success)lv8;
                            if (lv9.swingSource() == ActionResult.SwingSource.CLIENT) {
                                this.player.swingHand(lv);
                                if (!lv2.isEmpty() && (lv2.getCount() != i || this.player.isInCreativeMode())) {
                                    this.gameRenderer.firstPersonRenderer.resetEquipProgress(lv);
                                }
                            }
                            return;
                        }
                        if (!(lv8 instanceof ActionResult.Fail)) break;
                        return;
                    }
                }
            }
            if (lv2.isEmpty() || !((lv10 = this.interactionManager.interactItem(this.player, lv)) instanceof ActionResult.Success)) continue;
            ActionResult.Success lv11 = (ActionResult.Success)lv10;
            if (lv11.swingSource() == ActionResult.SwingSource.CLIENT) {
                this.player.swingHand(lv);
            }
            this.gameRenderer.firstPersonRenderer.resetEquipProgress(lv);
            return;
        }
    }

    public MusicTracker getMusicTracker() {
        return this.musicTracker;
    }

    public void tick() {
        CrashReport lv3;
        ++this.uptimeInTicks;
        if (this.world != null && !this.paused) {
            this.world.getTickManager().step();
        }
        if (this.itemUseCooldown > 0) {
            --this.itemUseCooldown;
        }
        Profiler lv = Profilers.get();
        lv.push("gui");
        this.messageHandler.processDelayedMessages();
        this.inGameHud.tick(this.paused);
        lv.pop();
        this.gameRenderer.updateCrosshairTarget(1.0f);
        this.tutorialManager.tick(this.world, this.crosshairTarget);
        lv.push("gameMode");
        if (!this.paused && this.world != null) {
            this.interactionManager.tick();
        }
        lv.swap("textures");
        if (this.shouldTick()) {
            this.textureManager.tick();
        }
        if (this.currentScreen == null && this.player != null) {
            if (this.player.isDead() && !(this.currentScreen instanceof DeathScreen)) {
                this.setScreen(null);
            } else if (this.player.isSleeping() && this.world != null) {
                this.inGameHud.getChatHud().setClientScreen(ChatHud.ChatMethod.MESSAGE, SleepingChatScreen::new);
            }
        } else {
            Screen screen = this.currentScreen;
            if (screen instanceof SleepingChatScreen) {
                SleepingChatScreen lv2 = (SleepingChatScreen)screen;
                if (!this.player.isSleeping()) {
                    lv2.closeChatIfEmpty();
                }
            }
        }
        if (this.currentScreen != null) {
            this.attackCooldown = 10000;
        }
        if (this.currentScreen != null) {
            try {
                this.currentScreen.tick();
            } catch (Throwable throwable) {
                lv3 = CrashReport.create(throwable, "Ticking screen");
                this.currentScreen.addCrashReportSection(lv3);
                throw new CrashException(lv3);
            }
        }
        if (this.overlay != null) {
            this.overlay.tick();
        }
        if (!this.getDebugHud().shouldShowDebugHud()) {
            this.inGameHud.resetDebugHudChunk();
        }
        if (this.overlay == null && this.currentScreen == null) {
            lv.swap("Keybindings");
            this.handleInputEvents();
            if (this.attackCooldown > 0) {
                --this.attackCooldown;
            }
        }
        if (this.world != null) {
            if (!this.paused) {
                lv.swap("gameRenderer");
                this.gameRenderer.tick();
                lv.swap("entities");
                this.world.tickEntities();
                lv.swap("blockEntities");
                this.world.tickBlockEntities();
            }
        } else if (this.gameRenderer.getPostProcessorId() != null) {
            this.gameRenderer.clearPostProcessor();
        }
        this.musicTracker.tick();
        this.soundManager.tick(this.paused);
        if (this.world != null) {
            ClientPlayNetworkHandler lv7;
            if (!this.paused) {
                lv.swap("level");
                if (!this.options.joinedFirstServer && this.isConnectedToServer()) {
                    MutableText lv4 = Text.translatable("tutorial.socialInteractions.title");
                    MutableText lv5 = Text.translatable("tutorial.socialInteractions.description", TutorialManager.keyToText("socialInteractions"));
                    this.socialInteractionsToast = new TutorialToast(this.textRenderer, TutorialToast.Type.SOCIAL_INTERACTIONS, lv4, lv5, true, 8000);
                    this.toastManager.add(this.socialInteractionsToast);
                    this.options.joinedFirstServer = true;
                    this.options.write();
                }
                this.tutorialManager.tick();
                try {
                    this.world.tick(() -> true);
                } catch (Throwable throwable) {
                    lv3 = CrashReport.create(throwable, "Exception in world tick");
                    if (this.world == null) {
                        CrashReportSection lv6 = lv3.addElement("Affected level");
                        lv6.add("Problem", "Level is null!");
                    } else {
                        this.world.addDetailsToCrashReport(lv3);
                    }
                    throw new CrashException(lv3);
                }
            }
            lv.swap("animateTick");
            if (!this.paused && this.shouldTick()) {
                this.world.doRandomBlockDisplayTicks(this.player.getBlockX(), this.player.getBlockY(), this.player.getBlockZ());
            }
            lv.swap("particles");
            if (!this.paused && this.shouldTick()) {
                this.particleManager.tick();
            }
            if ((lv7 = this.getNetworkHandler()) != null && !this.paused) {
                lv7.sendPacket(ClientTickEndC2SPacket.INSTANCE);
            }
        } else if (this.integratedServerConnection != null) {
            lv.swap("pendingConnection");
            this.integratedServerConnection.tick();
        }
        lv.swap("keyboard");
        this.keyboard.pollDebugCrash();
        lv.pop();
    }

    private boolean shouldTick() {
        return this.world == null || this.world.getTickManager().shouldTick();
    }

    private boolean isConnectedToServer() {
        return !this.integratedServerRunning || this.server != null && this.server.isRemote();
    }

    private void handleInputEvents() {
        while (this.options.togglePerspectiveKey.wasPressed()) {
            Perspective lv = this.options.getPerspective();
            this.options.setPerspective(this.options.getPerspective().next());
            if (lv.isFirstPerson() != this.options.getPerspective().isFirstPerson()) {
                this.gameRenderer.onCameraEntitySet(this.options.getPerspective().isFirstPerson() ? this.getCameraEntity() : null);
            }
            this.worldRenderer.scheduleTerrainUpdate();
        }
        while (this.options.smoothCameraKey.wasPressed()) {
            this.options.smoothCameraEnabled = !this.options.smoothCameraEnabled;
        }
        for (int i = 0; i < 9; ++i) {
            boolean bl = this.options.saveToolbarActivatorKey.isPressed();
            boolean bl2 = this.options.loadToolbarActivatorKey.isPressed();
            if (!this.options.hotbarKeys[i].wasPressed()) continue;
            if (this.player.isSpectator()) {
                this.inGameHud.getSpectatorHud().selectSlot(i);
                continue;
            }
            if (this.player.isInCreativeMode() && this.currentScreen == null && (bl2 || bl)) {
                CreativeInventoryScreen.onHotbarKeyPress(this, i, bl2, bl);
                continue;
            }
            this.player.getInventory().setSelectedSlot(i);
        }
        while (this.options.socialInteractionsKey.wasPressed()) {
            if (!this.isConnectedToServer() && !SharedConstants.SOCIAL_INTERACTIONS) {
                this.player.sendMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
                this.narratorManager.narrateSystemImmediately(SOCIAL_INTERACTIONS_NOT_AVAILABLE);
                continue;
            }
            if (this.socialInteractionsToast != null) {
                this.socialInteractionsToast.hide();
                this.socialInteractionsToast = null;
            }
            this.setScreen(new SocialInteractionsScreen());
        }
        while (this.options.inventoryKey.wasPressed()) {
            if (this.interactionManager.hasRidingInventory()) {
                this.player.openRidingInventory();
                continue;
            }
            this.tutorialManager.onInventoryOpened();
            this.setScreen(new InventoryScreen(this.player));
        }
        while (this.options.advancementsKey.wasPressed()) {
            this.setScreen(new AdvancementsScreen(this.player.networkHandler.getAdvancementHandler()));
        }
        while (this.options.quickActionsKey.wasPressed()) {
            this.getQuickActionsDialog().ifPresent(dialog -> this.player.networkHandler.showDialog((RegistryEntry<Dialog>)dialog, this.currentScreen));
        }
        while (this.options.swapHandsKey.wasPressed()) {
            if (this.player.isSpectator()) continue;
            this.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
        }
        while (this.options.dropKey.wasPressed()) {
            if (this.player.isSpectator() || !this.player.dropSelectedItem(this.isCtrlPressed())) continue;
            this.player.swingHand(Hand.MAIN_HAND);
        }
        while (this.options.chatKey.wasPressed()) {
            this.openChatScreen(ChatHud.ChatMethod.MESSAGE);
        }
        if (this.currentScreen == null && this.overlay == null && this.options.commandKey.wasPressed()) {
            this.openChatScreen(ChatHud.ChatMethod.COMMAND);
        }
        boolean bl3 = false;
        if (this.player.isUsingItem()) {
            if (!this.options.useKey.isPressed()) {
                this.interactionManager.stopUsingItem(this.player);
            }
            while (this.options.attackKey.wasPressed()) {
            }
            while (this.options.useKey.wasPressed()) {
            }
            while (this.options.pickItemKey.wasPressed()) {
            }
        } else {
            while (this.options.attackKey.wasPressed()) {
                bl3 |= this.doAttack();
            }
            while (this.options.useKey.wasPressed()) {
                this.doItemUse();
            }
            while (this.options.pickItemKey.wasPressed()) {
                this.doItemPick();
            }
            if (this.player.isSpectator()) {
                while (this.options.spectatorHotbarKey.wasPressed()) {
                    this.inGameHud.getSpectatorHud().useSelectedCommand();
                }
            }
        }
        if (this.options.useKey.isPressed() && this.itemUseCooldown == 0 && !this.player.isUsingItem()) {
            this.doItemUse();
        }
        this.handleBlockBreaking(this.currentScreen == null && !bl3 && this.options.attackKey.isPressed() && this.mouse.isCursorLocked());
    }

    private Optional<RegistryEntry<Dialog>> getQuickActionsDialog() {
        RegistryWrapper.Impl lv = this.player.networkHandler.getRegistryManager().getOrThrow(RegistryKeys.DIALOG);
        return lv.getOptional(DialogTags.QUICK_ACTIONS).flatMap(quickActionsDialogs -> {
            if (quickActionsDialogs.size() == 0) {
                return Optional.empty();
            }
            if (quickActionsDialogs.size() == 1) {
                return Optional.of(quickActionsDialogs.get(0));
            }
            return ((Registry)lv).getOptional(Dialogs.QUICK_ACTIONS);
        });
    }

    public TelemetryManager getTelemetryManager() {
        return this.telemetryManager;
    }

    public double getGpuUtilizationPercentage() {
        return this.gpuUtilizationPercentage;
    }

    public ProfileKeys getProfileKeys() {
        return this.profileKeys;
    }

    public IntegratedServerLoader createIntegratedServerLoader() {
        return new IntegratedServerLoader(this, this.levelStorage);
    }

    public void startIntegratedServer(LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, boolean newWorld) {
        this.disconnectWithProgressScreen();
        Instant instant = Instant.now();
        ClientChunkLoadProgress lv = new ClientChunkLoadProgress(newWorld ? 500L : 0L);
        LevelLoadingScreen lv2 = new LevelLoadingScreen(lv, LevelLoadingScreen.WorldEntryReason.OTHER);
        this.setScreen(lv2);
        int i = Math.max(5, 3) + ChunkLevels.FULL_GENERATION_REQUIRED_LEVEL + 1;
        try {
            session.backupLevelDataFile(saveLoader.combinedDynamicRegistries().getCombinedRegistryManager(), saveLoader.saveProperties());
            ChunkLoadProgress lv3 = ChunkLoadProgress.compose(lv, LoggingChunkLoadProgress.withPlayer());
            this.server = MinecraftServer.startServer(thread -> new IntegratedServer((Thread)thread, this, session, dataPackManager, saveLoader, this.apiServices, lv3));
            lv.setChunkLoadMap(this.server.createChunkLoadMap(i));
            this.integratedServerRunning = true;
            this.ensureAbuseReportContext(ReporterEnvironment.ofIntegratedServer());
            this.quickPlayLogger.setWorld(QuickPlayLogger.WorldType.SINGLEPLAYER, session.getDirectoryName(), saveLoader.saveProperties().getLevelName());
        } catch (Throwable throwable) {
            CrashReport lv4 = CrashReport.create(throwable, "Starting integrated server");
            CrashReportSection lv5 = lv4.addElement("Starting integrated server");
            lv5.add("Level ID", session.getDirectoryName());
            lv5.add("Level Name", () -> saveLoader.saveProperties().getLevelName());
            throw new CrashException(lv4);
        }
        Profiler lv6 = Profilers.get();
        lv6.push("waitForServer");
        long l = TimeUnit.SECONDS.toNanos(1L) / 60L;
        while (!this.server.isLoading() || this.overlay != null) {
            long m = Util.getMeasuringTimeNano() + l;
            lv2.tick();
            if (this.overlay != null) {
                this.overlay.tick();
            }
            this.render(false);
            this.runTasks();
            this.runTasks(() -> Util.getMeasuringTimeNano() > m);
            this.printCrashReport();
        }
        lv6.pop();
        Duration duration = Duration.between(instant, Instant.now());
        SocketAddress socketAddress = this.server.getNetworkIo().bindLocal();
        ClientConnection lv7 = ClientConnection.connectLocal(socketAddress);
        lv7.connect(socketAddress.toString(), 0, new ClientLoginNetworkHandler(lv7, this, null, null, newWorld, duration, status -> {}, lv, null));
        lv7.send(new LoginHelloC2SPacket(this.getSession().getUsername(), this.getSession().getUuidOrNull()));
        this.integratedServerConnection = lv7;
    }

    public void joinWorld(ClientWorld world) {
        this.world = world;
        this.setWorld(world);
    }

    public void disconnect(Text reasonText) {
        boolean bl = this.isInSingleplayer();
        ServerInfo lv = this.getCurrentServerEntry();
        if (this.world != null) {
            this.world.disconnect(reasonText);
        }
        if (bl) {
            this.disconnectWithSavingScreen();
        } else {
            this.disconnectWithProgressScreen();
        }
        TitleScreen lv2 = new TitleScreen();
        if (bl) {
            this.setScreen(lv2);
        } else if (lv != null && lv.isRealm()) {
            this.setScreen(new RealmsMainScreen(lv2));
        } else {
            this.setScreen(new MultiplayerScreen(lv2));
        }
    }

    public void disconnectWithSavingScreen() {
        this.disconnect(new MessageScreen(SAVING_LEVEL_TEXT), false);
    }

    public void disconnectWithProgressScreen() {
        this.disconnect(new ProgressScreen(true), false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void disconnect(Screen disconnectionScreen, boolean transferring) {
        ClientPlayNetworkHandler lv = this.getNetworkHandler();
        if (lv != null) {
            this.cancelTasks();
            lv.unloadWorld();
            if (!transferring) {
                this.onDisconnected();
            }
        }
        this.socialInteractionsManager.unloadBlockList();
        if (this.recorder.isActive()) {
            this.forceStopRecorder();
        }
        IntegratedServer lv2 = this.server;
        this.server = null;
        this.gameRenderer.reset();
        this.interactionManager = null;
        this.narratorManager.clear();
        this.disconnecting = true;
        try {
            if (this.world != null) {
                this.inGameHud.clear();
            }
            if (lv2 != null) {
                this.setScreen(new MessageScreen(SAVING_LEVEL_TEXT));
                Profiler lv3 = Profilers.get();
                lv3.push("waitForServer");
                while (!lv2.isStopping()) {
                    this.render(false);
                }
                lv3.pop();
            }
            this.setScreenAndRender(disconnectionScreen);
            this.integratedServerRunning = false;
            this.world = null;
            this.setWorld(null);
            this.player = null;
        } finally {
            this.disconnecting = false;
        }
    }

    public void onDisconnected() {
        this.serverResourcePackLoader.clear();
        this.runTasks();
    }

    public void enterReconfiguration(Screen reconfigurationScreen) {
        ClientPlayNetworkHandler lv = this.getNetworkHandler();
        if (lv != null) {
            lv.clearWorld();
        }
        if (this.recorder.isActive()) {
            this.forceStopRecorder();
        }
        this.gameRenderer.reset();
        this.interactionManager = null;
        this.narratorManager.clear();
        this.disconnecting = true;
        try {
            this.setScreenAndRender(reconfigurationScreen);
            this.inGameHud.clear();
            this.world = null;
            this.setWorld(null);
            this.player = null;
        } finally {
            this.disconnecting = false;
        }
    }

    public void setScreenAndRender(Screen screen) {
        try (ScopedProfiler lv = Profilers.get().scoped("forcedTick");){
            this.setScreen(screen);
            this.render(false);
        }
    }

    private void setWorld(@Nullable ClientWorld world) {
        this.soundManager.stopAll();
        this.setCameraEntity(null);
        this.integratedServerConnection = null;
        this.worldRenderer.setWorld(world);
        this.particleManager.setWorld(world);
        this.gameRenderer.setWorld(world);
        this.updateWindowTitle();
    }

    private UserApiService.UserProperties getUserProperties() {
        return this.userPropertiesFuture.join();
    }

    public boolean isOptionalTelemetryEnabled() {
        return this.isOptionalTelemetryEnabledByApi() && this.options.getTelemetryOptInExtra().getValue() != false;
    }

    public boolean isOptionalTelemetryEnabledByApi() {
        return this.isTelemetryEnabledByApi() && this.getUserProperties().flag(UserApiService.UserFlag.OPTIONAL_TELEMETRY_AVAILABLE);
    }

    public boolean isTelemetryEnabledByApi() {
        if (SharedConstants.isDevelopment && !SharedConstants.FORCE_TELEMETRY) {
            return false;
        }
        return this.getUserProperties().flag(UserApiService.UserFlag.TELEMETRY_ENABLED);
    }

    public boolean isMultiplayerEnabled() {
        return this.multiplayerEnabled && this.getUserProperties().flag(UserApiService.UserFlag.SERVERS_ALLOWED) && this.getMultiplayerBanDetails() == null && !this.isUsernameBanned();
    }

    public boolean isRealmsEnabled() {
        return this.getUserProperties().flag(UserApiService.UserFlag.REALMS_ALLOWED) && this.getMultiplayerBanDetails() == null;
    }

    @Nullable
    public BanDetails getMultiplayerBanDetails() {
        return this.getUserProperties().bannedScopes().get("MULTIPLAYER");
    }

    public boolean isUsernameBanned() {
        com.mojang.authlib.yggdrasil.ProfileResult profileResult = this.gameProfileFuture.getNow(null);
        return profileResult != null && profileResult.actions().contains((Object)ProfileActionType.FORCED_NAME_CHANGE);
    }

    public boolean shouldBlockMessages(UUID sender) {
        if (!this.getChatRestriction().allowsChat(false)) {
            return (this.player == null || !sender.equals(this.player.getUuid())) && !sender.equals(Util.NIL_UUID);
        }
        return this.socialInteractionsManager.isPlayerMuted(sender);
    }

    public ChatRestriction getChatRestriction() {
        if (this.options.getChatVisibility().getValue() == ChatVisibility.HIDDEN) {
            return ChatRestriction.DISABLED_BY_OPTIONS;
        }
        if (!this.onlineChatEnabled) {
            return ChatRestriction.DISABLED_BY_LAUNCHER;
        }
        if (!this.getUserProperties().flag(UserApiService.UserFlag.CHAT_ALLOWED)) {
            return ChatRestriction.DISABLED_BY_PROFILE;
        }
        return ChatRestriction.ENABLED;
    }

    public final boolean isDemo() {
        return this.isDemo;
    }

    public final boolean canSwitchGameMode() {
        return this.player != null && this.interactionManager != null;
    }

    @Nullable
    public ClientPlayNetworkHandler getNetworkHandler() {
        return this.player == null ? null : this.player.networkHandler;
    }

    public static boolean isHudEnabled() {
        return !MinecraftClient.instance.options.hudHidden;
    }

    public static boolean isFancyGraphicsOrBetter() {
        return MinecraftClient.instance.options.getGraphicsMode().getValue().getId() >= GraphicsMode.FANCY.getId();
    }

    public static boolean isFabulousGraphicsOrBetter() {
        return !MinecraftClient.instance.gameRenderer.isRenderingPanorama() && MinecraftClient.instance.options.getGraphicsMode().getValue().getId() >= GraphicsMode.FABULOUS.getId();
    }

    public static boolean isAmbientOcclusionEnabled() {
        return MinecraftClient.instance.options.getAo().getValue();
    }

    private void doItemPick() {
        if (this.crosshairTarget == null || this.crosshairTarget.getType() == HitResult.Type.MISS) {
            return;
        }
        boolean bl = this.isCtrlPressed();
        HitResult hitResult = this.crosshairTarget;
        Objects.requireNonNull(hitResult);
        HitResult hitResult2 = hitResult;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{BlockHitResult.class, EntityHitResult.class}, (Object)hitResult2, n)) {
            case 0: {
                BlockHitResult lv = (BlockHitResult)hitResult2;
                this.interactionManager.pickItemFromBlock(lv.getBlockPos(), bl);
                break;
            }
            case 1: {
                EntityHitResult lv2 = (EntityHitResult)hitResult2;
                this.interactionManager.pickItemFromEntity(lv2.getEntity(), bl);
                break;
            }
        }
    }

    public CrashReport addDetailsToCrashReport(CrashReport report) {
        SystemDetails lv = report.getSystemDetailsSection();
        try {
            MinecraftClient.addSystemDetailsToCrashReport(lv, this, this.languageManager, this.gameVersion, this.options);
            this.addUptimesToCrashReport(report.addElement("Uptime"));
            if (this.world != null) {
                this.world.addDetailsToCrashReport(report);
            }
            if (this.server != null) {
                this.server.addSystemDetails(lv);
            }
            this.resourceReloadLogger.addReloadSection(report);
        } catch (Throwable throwable) {
            LOGGER.error("Failed to collect details", throwable);
        }
        return report;
    }

    public static void addSystemDetailsToCrashReport(@Nullable MinecraftClient client, @Nullable LanguageManager languageManager, String version, @Nullable GameOptions options, CrashReport report) {
        SystemDetails lv = report.getSystemDetailsSection();
        MinecraftClient.addSystemDetailsToCrashReport(lv, client, languageManager, version, options);
    }

    private static String formatSeconds(double seconds) {
        return String.format(Locale.ROOT, "%.3fs", seconds);
    }

    private void addUptimesToCrashReport(CrashReportSection section) {
        section.add("JVM uptime", () -> MinecraftClient.formatSeconds((double)ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0));
        section.add("Wall uptime", () -> MinecraftClient.formatSeconds((double)(System.currentTimeMillis() - this.startTime) / 1000.0));
        section.add("High-res time", () -> MinecraftClient.formatSeconds((double)Util.getMeasuringTimeMs() / 1000.0));
        section.add("Client ticks", () -> String.format(Locale.ROOT, "%d ticks / %.3fs", this.uptimeInTicks, (double)this.uptimeInTicks / 20.0));
    }

    private static SystemDetails addSystemDetailsToCrashReport(SystemDetails systemDetails, @Nullable MinecraftClient client, @Nullable LanguageManager languageManager, String version, @Nullable GameOptions options) {
        systemDetails.addSection("Launched Version", () -> version);
        String string2 = MinecraftClient.getLauncherBrand();
        if (string2 != null) {
            systemDetails.addSection("Launcher name", string2);
        }
        systemDetails.addSection("Backend library", RenderSystem::getBackendDescription);
        systemDetails.addSection("Backend API", RenderSystem::getApiDescription);
        systemDetails.addSection("Window size", () -> client != null ? arg.window.getFramebufferWidth() + "x" + arg.window.getFramebufferHeight() : "<not initialized>");
        systemDetails.addSection("GFLW Platform", Window::getGlfwPlatform);
        systemDetails.addSection("Render Extensions", () -> String.join((CharSequence)", ", RenderSystem.getDevice().getEnabledExtensions()));
        systemDetails.addSection("GL debug messages", () -> {
            GpuDevice gpuDevice = RenderSystem.tryGetDevice();
            if (gpuDevice == null) {
                return "<no renderer available>";
            }
            if (gpuDevice.isDebuggingEnabled()) {
                return String.join((CharSequence)"\n", gpuDevice.getLastDebugMessages());
            }
            return "<debugging unavailable>";
        });
        systemDetails.addSection("Is Modded", () -> MinecraftClient.getModStatus().getMessage());
        systemDetails.addSection("Universe", () -> client != null ? Long.toHexString(arg.UNIVERSE) : "404");
        systemDetails.addSection("Type", "Client (map_client.txt)");
        if (options != null) {
            String string3;
            if (client != null && (string3 = client.getVideoWarningManager().getWarningsAsString()) != null) {
                systemDetails.addSection("GPU Warnings", string3);
            }
            systemDetails.addSection("Graphics mode", options.getGraphicsMode().getValue().toString());
            systemDetails.addSection("Render Distance", options.getClampedViewDistance() + "/" + String.valueOf(options.getViewDistance().getValue()) + " chunks");
        }
        if (client != null) {
            systemDetails.addSection("Resource Packs", () -> ResourcePackManager.listPacks(client.getResourcePackManager().getEnabledProfiles()));
        }
        if (languageManager != null) {
            systemDetails.addSection("Current Language", () -> languageManager.getLanguage());
        }
        systemDetails.addSection("Locale", String.valueOf(Locale.getDefault()));
        systemDetails.addSection("System encoding", () -> System.getProperty("sun.jnu.encoding", "<not set>"));
        systemDetails.addSection("File encoding", () -> System.getProperty("file.encoding", "<not set>"));
        systemDetails.addSection("CPU", GLX::_getCpuInfo);
        return systemDetails;
    }

    public static MinecraftClient getInstance() {
        return instance;
    }

    public CompletableFuture<Void> reloadResourcesConcurrently() {
        return this.submit(this::reloadResources).thenCompose(future -> future);
    }

    public void ensureAbuseReportContext(ReporterEnvironment environment) {
        if (!this.abuseReportContext.environmentEquals(environment)) {
            this.abuseReportContext = AbuseReportContext.create(environment, this.userApiService);
        }
    }

    @Nullable
    public ServerInfo getCurrentServerEntry() {
        return Nullables.map(this.getNetworkHandler(), ClientPlayNetworkHandler::getServerInfo);
    }

    public boolean isInSingleplayer() {
        return this.integratedServerRunning;
    }

    public boolean isIntegratedServerRunning() {
        return this.integratedServerRunning && this.server != null;
    }

    @Nullable
    public IntegratedServer getServer() {
        return this.server;
    }

    public boolean isConnectedToLocalServer() {
        IntegratedServer lv = this.getServer();
        return lv != null && !lv.isRemote();
    }

    public boolean uuidEquals(UUID uuid) {
        return uuid.equals(this.getSession().getUuidOrNull());
    }

    public Session getSession() {
        return this.session;
    }

    public GameProfile getGameProfile() {
        com.mojang.authlib.yggdrasil.ProfileResult profileResult = this.gameProfileFuture.join();
        if (profileResult != null) {
            return profileResult.profile();
        }
        return new GameProfile(this.session.getUuidOrNull(), this.session.getUsername());
    }

    public Proxy getNetworkProxy() {
        return this.networkProxy;
    }

    public TextureManager getTextureManager() {
        return this.textureManager;
    }

    public ShaderLoader getShaderLoader() {
        return this.shaderLoader;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public ResourcePackManager getResourcePackManager() {
        return this.resourcePackManager;
    }

    public DefaultResourcePack getDefaultResourcePack() {
        return this.defaultResourcePack;
    }

    public ServerResourcePackLoader getServerResourcePackProvider() {
        return this.serverResourcePackLoader;
    }

    public Path getResourcePackDir() {
        return this.resourcePackDir;
    }

    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public VideoWarningManager getVideoWarningManager() {
        return this.videoWarningManager;
    }

    public SoundManager getSoundManager() {
        return this.soundManager;
    }

    public MusicInstance getMusicInstance() {
        MusicSound lv = Nullables.map(this.currentScreen, Screen::getMusic);
        if (lv != null) {
            return new MusicInstance(lv);
        }
        if (this.player != null) {
            World lv2 = this.player.getEntityWorld();
            if (lv2.getRegistryKey() == World.END) {
                if (this.inGameHud.getBossBarHud().shouldPlayDragonMusic()) {
                    return new MusicInstance(MusicType.DRAGON);
                }
                return new MusicInstance(MusicType.END);
            }
            RegistryEntry<Biome> lv3 = lv2.getBiome(this.player.getBlockPos());
            Biome lv4 = lv3.value();
            float f = lv4.getMusicVolume();
            Optional<Pool<MusicSound>> optional = lv4.getMusic();
            if (optional.isPresent()) {
                Optional<MusicSound> optional2 = optional.get().getOrEmpty(lv2.random);
                return new MusicInstance(optional2.orElse(null), f);
            }
            if (this.musicTracker.isPlayingType(MusicType.UNDERWATER) || this.player.isSubmergedInWater() && lv3.isIn(BiomeTags.PLAYS_UNDERWATER_MUSIC)) {
                return new MusicInstance(MusicType.UNDERWATER, f);
            }
            if (lv2.getRegistryKey() != World.NETHER && this.player.getAbilities().creativeMode && this.player.getAbilities().allowFlying) {
                return new MusicInstance(MusicType.CREATIVE, f);
            }
            return new MusicInstance(MusicType.GAME, f);
        }
        return new MusicInstance(MusicType.MENU);
    }

    public ApiServices getApiServices() {
        return this.apiServices;
    }

    public PlayerSkinProvider getSkinProvider() {
        return this.skinProvider;
    }

    @Nullable
    public Entity getCameraEntity() {
        return this.cameraEntity;
    }

    public void setCameraEntity(@Nullable Entity entity) {
        this.cameraEntity = entity;
        this.gameRenderer.onCameraEntitySet(entity);
    }

    public boolean hasOutline(Entity entity) {
        return entity.isGlowing() || this.player != null && this.player.isSpectator() && this.options.spectatorOutlinesKey.isPressed() && entity.getType() == EntityType.PLAYER;
    }

    @Override
    protected Thread getThread() {
        return this.thread;
    }

    @Override
    public Runnable createTask(Runnable runnable) {
        return runnable;
    }

    @Override
    protected boolean canExecute(Runnable task) {
        return true;
    }

    public BlockRenderManager getBlockRenderManager() {
        return this.blockRenderManager;
    }

    public EntityRenderManager getEntityRenderDispatcher() {
        return this.entityRenderManager;
    }

    public BlockEntityRenderManager getBlockEntityRenderDispatcher() {
        return this.blockEntityRenderManager;
    }

    public ItemRenderer getItemRenderer() {
        return this.itemRenderer;
    }

    public MapRenderer getMapRenderer() {
        return this.mapRenderer;
    }

    public DataFixer getDataFixer() {
        return this.dataFixer;
    }

    public RenderTickCounter getRenderTickCounter() {
        return this.renderTickCounter;
    }

    public BlockColors getBlockColors() {
        return this.blockColors;
    }

    public boolean hasReducedDebugInfo() {
        return this.player != null && this.player.hasReducedDebugInfo() || this.options.getReducedDebugInfo().getValue() != false;
    }

    public ToastManager getToastManager() {
        return this.toastManager;
    }

    public TutorialManager getTutorialManager() {
        return this.tutorialManager;
    }

    public boolean isWindowFocused() {
        return this.windowFocused;
    }

    public HotbarStorage getCreativeHotbarStorage() {
        return this.creativeHotbarStorage;
    }

    public BakedModelManager getBakedModelManager() {
        return this.bakedModelManager;
    }

    public AtlasManager getAtlasManager() {
        return this.atlasManager;
    }

    public MapTextureManager getMapTextureManager() {
        return this.mapTextureManager;
    }

    public WaypointStyleAssetManager getWaypointStyleAssetManager() {
        return this.waypointStyleAssetManager;
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        this.windowFocused = focused;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Text takePanorama(File directory) {
        int i = 4;
        int j = 4096;
        int k = 4096;
        int l = this.window.getFramebufferWidth();
        int m = this.window.getFramebufferHeight();
        Framebuffer lv = this.getFramebuffer();
        float f = this.player.getPitch();
        float g = this.player.getYaw();
        float h = this.player.lastPitch;
        float n = this.player.lastYaw;
        this.gameRenderer.setBlockOutlineEnabled(false);
        try {
            this.gameRenderer.setRenderingPanorama(true);
            this.window.setFramebufferWidth(4096);
            this.window.setFramebufferHeight(4096);
            lv.resize(4096, 4096);
            for (int o = 0; o < 6; ++o) {
                switch (o) {
                    case 0: {
                        this.player.setYaw(g);
                        this.player.setPitch(0.0f);
                        break;
                    }
                    case 1: {
                        this.player.setYaw((g + 90.0f) % 360.0f);
                        this.player.setPitch(0.0f);
                        break;
                    }
                    case 2: {
                        this.player.setYaw((g + 180.0f) % 360.0f);
                        this.player.setPitch(0.0f);
                        break;
                    }
                    case 3: {
                        this.player.setYaw((g - 90.0f) % 360.0f);
                        this.player.setPitch(0.0f);
                        break;
                    }
                    case 4: {
                        this.player.setYaw(g);
                        this.player.setPitch(-90.0f);
                        break;
                    }
                    default: {
                        this.player.setYaw(g);
                        this.player.setPitch(90.0f);
                    }
                }
                this.player.lastYaw = this.player.getYaw();
                this.player.lastPitch = this.player.getPitch();
                this.gameRenderer.renderWorld(RenderTickCounter.ONE);
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                ScreenshotRecorder.saveScreenshot(directory, "panorama_" + o + ".png", lv, 4, message -> {});
            }
            MutableText lv2 = Text.literal(directory.getName()).formatted(Formatting.UNDERLINE).styled(style -> style.withClickEvent(new ClickEvent.OpenFile(directory.getAbsoluteFile())));
            MutableText mutableText = Text.translatable("screenshot.success", lv2);
            return mutableText;
        } catch (Exception exception) {
            LOGGER.error("Couldn't save image", exception);
            MutableText mutableText = Text.translatable("screenshot.failure", exception.getMessage());
            return mutableText;
        } finally {
            this.player.setPitch(f);
            this.player.setYaw(g);
            this.player.lastPitch = h;
            this.player.lastYaw = n;
            this.gameRenderer.setBlockOutlineEnabled(true);
            this.window.setFramebufferWidth(l);
            this.window.setFramebufferHeight(m);
            lv.resize(l, m);
            this.gameRenderer.setRenderingPanorama(false);
        }
    }

    public SplashTextResourceSupplier getSplashTextLoader() {
        return this.splashTextLoader;
    }

    @Nullable
    public Overlay getOverlay() {
        return this.overlay;
    }

    public SocialInteractionsManager getSocialInteractionsManager() {
        return this.socialInteractionsManager;
    }

    public Window getWindow() {
        return this.window;
    }

    public InactivityFpsLimiter getInactivityFpsLimiter() {
        return this.inactivityFpsLimiter;
    }

    public DebugHud getDebugHud() {
        return this.inGameHud.getDebugHud();
    }

    public BufferBuilderStorage getBufferBuilders() {
        return this.bufferBuilders;
    }

    public void setMipmapLevels(int mipmapLevels) {
        this.atlasManager.setMipmapLevels(mipmapLevels);
    }

    public LoadedEntityModels getLoadedEntityModels() {
        return this.bakedModelManager.getEntityModelsSupplier().get();
    }

    public boolean shouldFilterText() {
        return this.getUserProperties().flag(UserApiService.UserFlag.PROFANITY_FILTER_ENABLED);
    }

    public void loadBlockList() {
        this.socialInteractionsManager.loadBlockList();
        this.getProfileKeys().fetchKeyPair();
    }

    public GuiNavigationType getNavigationType() {
        return this.navigationType;
    }

    public void setNavigationType(GuiNavigationType navigationType) {
        this.navigationType = navigationType;
    }

    public NarratorManager getNarratorManager() {
        return this.narratorManager;
    }

    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }

    public AbuseReportContext getAbuseReportContext() {
        return this.abuseReportContext;
    }

    public RealmsPeriodicCheckers getRealmsPeriodicCheckers() {
        return this.realmsPeriodicCheckers;
    }

    public QuickPlayLogger getQuickPlayLogger() {
        return this.quickPlayLogger;
    }

    public CommandHistoryManager getCommandHistoryManager() {
        return this.commandHistoryManager;
    }

    public SymlinkFinder getSymlinkFinder() {
        return this.symlinkFinder;
    }

    public PlayerSkinCache getPlayerSkinCache() {
        return this.playerSkinCache;
    }

    private float getTargetMillisPerTick(float millis) {
        TickManager lv;
        if (this.world != null && (lv = this.world.getTickManager()).shouldTick()) {
            return Math.max(millis, lv.getMillisPerTick());
        }
        return millis;
    }

    public ItemModelManager getItemModelManager() {
        return this.itemModelManager;
    }

    public boolean canCurrentScreenInterruptOtherScreen() {
        return (this.currentScreen == null || this.currentScreen.canInterruptOtherScreen()) && !this.disconnecting;
    }

    @Nullable
    public static String getLauncherBrand() {
        return System.getProperty("minecraft.launcher.brand");
    }

    public PacketApplyBatcher getPacketApplyBatcher() {
        return this.packetApplyBatcher;
    }

    static {
        LOGGER = LogUtils.getLogger();
        DEFAULT_FONT_ID = Identifier.ofVanilla("default");
        UNICODE_FONT_ID = Identifier.ofVanilla("uniform");
        ALT_TEXT_RENDERER_ID = Identifier.ofVanilla("alt");
        REGIONAL_COMPLIANCIES_ID = Identifier.ofVanilla("regional_compliancies.json");
        COMPLETED_UNIT_FUTURE = CompletableFuture.completedFuture(Unit.INSTANCE);
        SOCIAL_INTERACTIONS_NOT_AVAILABLE = Text.translatable("multiplayer.socialInteractions.not_available");
        SAVING_LEVEL_TEXT = Text.translatable("menu.savingLevel");
    }

    @Environment(value=EnvType.CLIENT)
    record LoadingContext(RealmsClient realmsClient, RunArgs.QuickPlay quickPlayData) {
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ChatRestriction {
        ENABLED(ScreenTexts.EMPTY){

            @Override
            public boolean allowsChat(boolean singlePlayer) {
                return true;
            }
        }
        ,
        DISABLED_BY_OPTIONS(Text.translatable("chat.disabled.options").formatted(Formatting.RED)){

            @Override
            public boolean allowsChat(boolean singlePlayer) {
                return false;
            }
        }
        ,
        DISABLED_BY_LAUNCHER(Text.translatable("chat.disabled.launcher").formatted(Formatting.RED)){

            @Override
            public boolean allowsChat(boolean singlePlayer) {
                return singlePlayer;
            }
        }
        ,
        DISABLED_BY_PROFILE(Text.translatable("chat.disabled.profile", Text.keybind(MinecraftClient.instance.options.chatKey.getId())).formatted(Formatting.RED)){

            @Override
            public boolean allowsChat(boolean singlePlayer) {
                return singlePlayer;
            }
        };

        static final Text MORE_INFO_TEXT;
        private final Text description;

        ChatRestriction(Text description) {
            this.description = description;
        }

        public Text getDescription() {
            return this.description;
        }

        public abstract boolean allowsChat(boolean var1);

        static {
            MORE_INFO_TEXT = Text.translatable("chat.disabled.profile.moreInfo");
        }
    }
}

