/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.main;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import com.mojang.util.UndashedUuid;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBootstrap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.telemetry.GameLoadTimeEvent;
import net.minecraft.client.session.telemetry.TelemetryEventProperty;
import net.minecraft.client.util.GlException;
import net.minecraft.client.util.tracy.TracyLoader;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Nullables;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.WinNativeModuleUtil;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.util.profiling.jfr.InstanceType;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Main {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @DontObfuscate
    public static void main(String[] args) {
        RunArgs lv3;
        Logger logger;
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        optionParser.accepts("demo");
        optionParser.accepts("disableMultiplayer");
        optionParser.accepts("disableChat");
        optionParser.accepts("fullscreen");
        optionParser.accepts("checkGlErrors");
        OptionSpecBuilder optionSpec = optionParser.accepts("renderDebugLabels");
        OptionSpecBuilder optionSpec2 = optionParser.accepts("jfrProfile");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("tracy");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("tracyNoImages");
        ArgumentAcceptingOptionSpec<String> optionSpec5 = optionParser.accepts("quickPlayPath").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec6 = optionParser.accepts("quickPlaySingleplayer").withOptionalArg();
        ArgumentAcceptingOptionSpec<String> optionSpec7 = optionParser.accepts("quickPlayMultiplayer").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec8 = optionParser.accepts("quickPlayRealms").withRequiredArg();
        ArgumentAcceptingOptionSpec<File> optionSpec9 = optionParser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."), (File[])new File[0]);
        ArgumentAcceptingOptionSpec<File> optionSpec10 = optionParser.accepts("assetsDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec<File> optionSpec11 = optionParser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec<String> optionSpec12 = optionParser.accepts("proxyHost").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> optionSpec13 = optionParser.accepts("proxyPort").withRequiredArg().defaultsTo("8080", (String[])new String[0]).ofType(Integer.class);
        ArgumentAcceptingOptionSpec<String> optionSpec14 = optionParser.accepts("proxyUser").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec15 = optionParser.accepts("proxyPass").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec16 = optionParser.accepts("username").withRequiredArg().defaultsTo("Player" + System.currentTimeMillis() % 1000L, (String[])new String[0]);
        OptionSpecBuilder optionSpec17 = optionParser.accepts("offlineDeveloperMode");
        ArgumentAcceptingOptionSpec<String> optionSpec18 = optionParser.accepts("uuid").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec19 = optionParser.accepts("xuid").withOptionalArg().defaultsTo("", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec20 = optionParser.accepts("clientId").withOptionalArg().defaultsTo("", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec21 = optionParser.accepts("accessToken").withRequiredArg().required();
        ArgumentAcceptingOptionSpec<String> optionSpec22 = optionParser.accepts("version").withRequiredArg().required();
        ArgumentAcceptingOptionSpec<Integer> optionSpec23 = optionParser.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<Integer> optionSpec24 = optionParser.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<Integer> optionSpec25 = optionParser.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec<Integer> optionSpec26 = optionParser.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec<String> optionSpec27 = optionParser.accepts("assetIndex").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec28 = optionParser.accepts("versionType").withRequiredArg().defaultsTo("release", (String[])new String[0]);
        NonOptionArgumentSpec<String> optionSpec29 = optionParser.nonOptions();
        OptionSet optionSet = optionParser.parse(args);
        File file = Main.getOption(optionSet, optionSpec9);
        String string = Main.getOption(optionSet, optionSpec22);
        String string2 = "Pre-bootstrap";
        try {
            if (optionSet.has(optionSpec2)) {
                FlightProfiler.INSTANCE.start(InstanceType.CLIENT);
            }
            if (optionSet.has(optionSpec3)) {
                TracyLoader.load();
            }
            Stopwatch stopwatch = Stopwatch.createStarted(Ticker.systemTicker());
            Stopwatch stopwatch2 = Stopwatch.createStarted(Ticker.systemTicker());
            GameLoadTimeEvent.INSTANCE.addTimer(TelemetryEventProperty.LOAD_TIME_TOTAL_TIME_MS, stopwatch);
            GameLoadTimeEvent.INSTANCE.addTimer(TelemetryEventProperty.LOAD_TIME_PRE_WINDOW_MS, stopwatch2);
            SharedConstants.createGameVersion();
            TracyClient.reportAppInfo("Minecraft Java Edition " + SharedConstants.getGameVersion().name());
            CompletableFuture<?> completableFuture = Schemas.optimize(DataFixTypes.REQUIRED_TYPES);
            CrashReport.initCrashReport();
            logger = LogUtils.getLogger();
            string2 = "Bootstrap";
            Bootstrap.initialize();
            ClientBootstrap.initialize();
            GameLoadTimeEvent.INSTANCE.setBootstrapTime(Bootstrap.LOAD_TIME.get());
            Bootstrap.logMissing();
            string2 = "Argument parsing";
            List<String> list = optionSet.valuesOf(optionSpec29);
            if (!list.isEmpty()) {
                logger.info("Completely ignored arguments: {}", (Object)list);
            }
            String string3 = Main.getOption(optionSet, optionSpec12);
            Proxy proxy = Proxy.NO_PROXY;
            if (string3 != null) {
                try {
                    proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(string3, (int)Main.getOption(optionSet, optionSpec13)));
                } catch (Exception exception) {
                    // empty catch block
                }
            }
            final String string4 = Main.getOption(optionSet, optionSpec14);
            final String string5 = Main.getOption(optionSet, optionSpec15);
            if (!proxy.equals(Proxy.NO_PROXY) && Main.isNotNullOrEmpty(string4) && Main.isNotNullOrEmpty(string5)) {
                Authenticator.setDefault(new Authenticator(){

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(string4, string5.toCharArray());
                    }
                });
            }
            int i = Main.getOption(optionSet, optionSpec23);
            int j = Main.getOption(optionSet, optionSpec24);
            OptionalInt optionalInt = Main.toOptional(Main.getOption(optionSet, optionSpec25));
            OptionalInt optionalInt2 = Main.toOptional(Main.getOption(optionSet, optionSpec26));
            boolean bl = optionSet.has("fullscreen");
            boolean bl2 = optionSet.has("demo");
            boolean bl3 = optionSet.has("disableMultiplayer");
            boolean bl4 = optionSet.has("disableChat");
            boolean bl5 = !optionSet.has(optionSpec4);
            boolean bl6 = optionSet.has(optionSpec);
            String string6 = Main.getOption(optionSet, optionSpec28);
            File file2 = optionSet.has(optionSpec10) ? Main.getOption(optionSet, optionSpec10) : new File(file, "assets/");
            File file3 = optionSet.has(optionSpec11) ? Main.getOption(optionSet, optionSpec11) : new File(file, "resourcepacks/");
            UUID uUID = Main.isUuidSetAndValid(optionSpec18, optionSet, logger) ? UndashedUuid.fromStringLenient((String)optionSpec18.value(optionSet)) : Uuids.getOfflinePlayerUuid((String)optionSpec16.value(optionSet));
            String string7 = optionSet.has(optionSpec27) ? (String)optionSpec27.value(optionSet) : null;
            String string8 = optionSet.valueOf(optionSpec19);
            String string9 = optionSet.valueOf(optionSpec20);
            String string10 = Main.getOption(optionSet, optionSpec5);
            RunArgs.QuickPlayVariant lv = Main.getQuickPlayVariant(optionSet, optionSpec6, optionSpec7, optionSpec8);
            Session lv2 = new Session((String)optionSpec16.value(optionSet), uUID, (String)optionSpec21.value(optionSet), Main.toOptional(string8), Main.toOptional(string9));
            lv3 = new RunArgs(new RunArgs.Network(lv2, proxy), new WindowSettings(i, j, optionalInt, optionalInt2, bl), new RunArgs.Directories(file, file3, file2, string7), new RunArgs.Game(bl2, string, string6, bl3, bl4, bl5, bl6, optionSet.has(optionSpec17)), new RunArgs.QuickPlay(string10, lv));
            Util.startTimerHack();
            completableFuture.join();
        } catch (Throwable throwable) {
            CrashReport lv4 = CrashReport.create(throwable, string2);
            CrashReportSection lv5 = lv4.addElement("Initialization");
            WinNativeModuleUtil.addDetailTo(lv5);
            MinecraftClient.addSystemDetailsToCrashReport(null, null, string, null, lv4);
            MinecraftClient.printCrashReport(null, file, lv4);
            return;
        }
        Thread thread = new Thread("Client Shutdown Thread"){

            @Override
            public void run() {
                MinecraftClient lv = MinecraftClient.getInstance();
                if (lv == null) {
                    return;
                }
                IntegratedServer lv2 = lv.getServer();
                if (lv2 != null) {
                    lv2.stop(true);
                }
            }
        };
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(logger));
        Runtime.getRuntime().addShutdownHook(thread);
        MinecraftClient lv6 = null;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            lv6 = new MinecraftClient(lv3);
        } catch (GlException lv7) {
            Util.shutdownExecutors();
            logger.warn("Failed to create window: ", lv7);
            return;
        } catch (Throwable throwable2) {
            CrashReport lv8 = CrashReport.create(throwable2, "Initializing game");
            CrashReportSection lv9 = lv8.addElement("Initialization");
            WinNativeModuleUtil.addDetailTo(lv9);
            MinecraftClient.addSystemDetailsToCrashReport(lv6, null, lv3.game.version, null, lv8);
            MinecraftClient.printCrashReport(lv6, lv3.directories.runDir, lv8);
            return;
        }
        MinecraftClient lv10 = lv6;
        lv10.run();
        try {
            lv10.scheduleStop();
        } finally {
            lv10.stop();
        }
    }

    private static RunArgs.QuickPlayVariant getQuickPlayVariant(OptionSet optionSet, OptionSpec<String> worldIdOption, OptionSpec<String> serverAddressOption, OptionSpec<String> realmIdOption) {
        long l = Stream.of(worldIdOption, serverAddressOption, realmIdOption).filter(optionSet::has).count();
        if (l == 0L) {
            return RunArgs.QuickPlayVariant.DEFAULT;
        }
        if (l > 1L) {
            throw new IllegalArgumentException("Only one quick play option can be specified");
        }
        if (optionSet.has(worldIdOption)) {
            String string = Main.unescape(Main.getOption(optionSet, worldIdOption));
            return new RunArgs.SingleplayerQuickPlay(string);
        }
        if (optionSet.has(serverAddressOption)) {
            String string = Main.unescape(Main.getOption(optionSet, serverAddressOption));
            return Nullables.mapOrElse(string, RunArgs.MultiplayerQuickPlay::new, RunArgs.QuickPlayVariant.DEFAULT);
        }
        if (optionSet.has(realmIdOption)) {
            String string = Main.unescape(Main.getOption(optionSet, realmIdOption));
            return Nullables.mapOrElse(string, RunArgs.RealmsQuickPlay::new, RunArgs.QuickPlayVariant.DEFAULT);
        }
        return RunArgs.QuickPlayVariant.DEFAULT;
    }

    @Nullable
    private static String unescape(@Nullable String string) {
        if (string == null) {
            return null;
        }
        return StringEscapeUtils.unescapeJava(string);
    }

    private static Optional<String> toOptional(String string) {
        return string.isEmpty() ? Optional.empty() : Optional.of(string);
    }

    private static OptionalInt toOptional(@Nullable Integer i) {
        return i != null ? OptionalInt.of(i) : OptionalInt.empty();
    }

    @Nullable
    private static <T> T getOption(OptionSet optionSet, OptionSpec<T> optionSpec) {
        try {
            return optionSet.valueOf(optionSpec);
        } catch (Throwable throwable) {
            ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec;
            List list;
            if (optionSpec instanceof ArgumentAcceptingOptionSpec && !(list = (argumentAcceptingOptionSpec = (ArgumentAcceptingOptionSpec)optionSpec).defaultValues()).isEmpty()) {
                return (T)list.get(0);
            }
            throw throwable;
        }
    }

    private static boolean isNotNullOrEmpty(@Nullable String s) {
        return s != null && !s.isEmpty();
    }

    private static boolean isUuidSetAndValid(OptionSpec<String> uuidOption, OptionSet optionSet, Logger logger) {
        return optionSet.has(uuidOption) && Main.isUuidValid(uuidOption, optionSet, logger);
    }

    private static boolean isUuidValid(OptionSpec<String> uuidOption, OptionSet optionSet, Logger logger) {
        try {
            UndashedUuid.fromStringLenient(uuidOption.value(optionSet));
        } catch (IllegalArgumentException illegalArgumentException) {
            logger.warn("Invalid UUID: '{}", (Object)uuidOption.value(optionSet));
            return false;
        }
        return true;
    }

    static {
        System.setProperty("java.awt.headless", "true");
    }
}

