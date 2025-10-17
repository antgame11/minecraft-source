/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.util;

import com.mojang.logging.LogUtils;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text NOW_TEXT = Text.translatable("mco.util.time.now");
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_DAY = 86400;

    public static Text convertToAgePresentation(long milliseconds) {
        if (milliseconds < 0L) {
            return NOW_TEXT;
        }
        long m = milliseconds / 1000L;
        if (m < 60L) {
            return Text.translatable("mco.time.secondsAgo", m);
        }
        if (m < 3600L) {
            long n = m / 60L;
            return Text.translatable("mco.time.minutesAgo", n);
        }
        if (m < 86400L) {
            long n = m / 3600L;
            return Text.translatable("mco.time.hoursAgo", n);
        }
        long n = m / 86400L;
        return Text.translatable("mco.time.daysAgo", n);
    }

    public static Text convertToAgePresentation(Date date) {
        return RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - date.getTime());
    }

    public static void drawPlayerHead(DrawContext context, int x, int y, int size, UUID playerUuid) {
        PlayerSkinCache.Entry lv = MinecraftClient.getInstance().getPlayerSkinCache().get(ProfileComponent.ofDynamic(playerUuid));
        PlayerSkinDrawer.draw(context, lv.getTextures(), x, y, size);
    }

    public static <T> CompletableFuture<T> runAsync(RealmsSupplier<T> supplier, @Nullable Consumer<RealmsServiceException> errorCallback) {
        return CompletableFuture.supplyAsync(() -> {
            RealmsClient lv = RealmsClient.create();
            try {
                return supplier.apply(lv);
            } catch (Throwable throwable) {
                if (throwable instanceof RealmsServiceException) {
                    RealmsServiceException lv2 = (RealmsServiceException)throwable;
                    if (errorCallback != null) {
                        errorCallback.accept(lv2);
                    }
                } else {
                    LOGGER.error("Unhandled exception", throwable);
                }
                throw new RuntimeException(throwable);
            }
        }, Util.getDownloadWorkerExecutor());
    }

    public static CompletableFuture<Void> runAsync(RealmsRunnable runnable, @Nullable Consumer<RealmsServiceException> errorCallback) {
        return RealmsUtil.runAsync(runnable, errorCallback);
    }

    public static Consumer<RealmsServiceException> openingScreen(Function<RealmsServiceException, Screen> screenCreator) {
        MinecraftClient lv = MinecraftClient.getInstance();
        return error -> lv.execute(() -> lv.setScreen((Screen)screenCreator.apply((RealmsServiceException)error)));
    }

    public static Consumer<RealmsServiceException> openingScreenAndLogging(Function<RealmsServiceException, Screen> screenCreator, String errorPrefix) {
        return RealmsUtil.openingScreen(screenCreator).andThen(error -> LOGGER.error(errorPrefix, (Throwable)error));
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface RealmsSupplier<T> {
        public T apply(RealmsClient var1) throws RealmsServiceException;
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface RealmsRunnable
    extends RealmsSupplier<Void> {
        public void accept(RealmsClient var1) throws RealmsServiceException;

        @Override
        default public Void apply(RealmsClient arg) throws RealmsServiceException {
            this.accept(arg);
            return null;
        }
    }
}

