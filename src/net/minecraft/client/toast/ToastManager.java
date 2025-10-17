/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.toast;

import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.NowPlayingToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ToastManager {
    private static final int SPACES = 5;
    private static final int field_52786 = -1;
    final MinecraftClient client;
    private final List<Entry<?>> visibleEntries = new ArrayList();
    private final BitSet occupiedSpaces = new BitSet(5);
    private final Deque<Toast> toastQueue = Queues.newArrayDeque();
    private final Set<SoundEvent> queuedToastSounds = new HashSet<SoundEvent>();
    @Nullable
    private Entry<NowPlayingToast> nowPlayingToast;

    public ToastManager(MinecraftClient client, GameOptions gameOptions) {
        this.client = client;
        if (gameOptions.getShowNowPlayingToast().getValue().booleanValue()) {
            this.showNowPlayingToast();
        }
    }

    public void update() {
        MutableBoolean mutableBoolean = new MutableBoolean(false);
        this.visibleEntries.removeIf(entry -> {
            Toast.Visibility lv = entry.visibility;
            entry.update();
            if (entry.visibility != lv && mutableBoolean.isFalse()) {
                mutableBoolean.setTrue();
                entry.visibility.playSound(this.client.getSoundManager());
            }
            if (entry.isFinishedRendering()) {
                this.occupiedSpaces.clear(entry.topIndex, entry.topIndex + entry.requiredSpaceCount);
                return true;
            }
            return false;
        });
        if (!this.toastQueue.isEmpty() && this.getEmptySpaceCount() > 0) {
            this.toastQueue.removeIf(toast -> {
                int i = toast.getRequiredSpaceCount();
                int j = this.getTopIndex(i);
                if (j == -1) {
                    return false;
                }
                this.visibleEntries.add(new Entry(this, toast, j, i));
                this.occupiedSpaces.set(j, j + i);
                SoundEvent lv = toast.getSoundEvent();
                if (lv != null && this.queuedToastSounds.add(lv)) {
                    this.client.getSoundManager().play(PositionedSoundInstance.master(lv, 1.0f, 1.0f));
                }
                return true;
            });
        }
        this.queuedToastSounds.clear();
        if (this.nowPlayingToast != null) {
            this.nowPlayingToast.update();
        }
    }

    public void draw(DrawContext context) {
        if (this.client.options.hudHidden) {
            return;
        }
        int i = context.getScaledWindowWidth();
        if (!this.visibleEntries.isEmpty()) {
            context.createNewRootLayer();
        }
        for (Entry<?> lv : this.visibleEntries) {
            lv.draw(context, i);
        }
        if (this.client.options.getShowNowPlayingToast().getValue().booleanValue() && this.nowPlayingToast != null && (this.client.currentScreen == null || !(this.client.currentScreen instanceof GameMenuScreen))) {
            this.nowPlayingToast.draw(context, i);
        }
    }

    private int getTopIndex(int requiredSpaces) {
        if (this.getEmptySpaceCount() >= requiredSpaces) {
            int j = 0;
            for (int k = 0; k < 5; ++k) {
                if (this.occupiedSpaces.get(k)) {
                    j = 0;
                    continue;
                }
                if (++j != requiredSpaces) continue;
                return k + 1 - j;
            }
        }
        return -1;
    }

    private int getEmptySpaceCount() {
        return 5 - this.occupiedSpaces.cardinality();
    }

    @Nullable
    public <T extends Toast> T getToast(Class<? extends T> toastClass, Object type) {
        for (Entry<?> lv : this.visibleEntries) {
            if (lv == null || !toastClass.isAssignableFrom(lv.getInstance().getClass()) || !lv.getInstance().getType().equals(type)) continue;
            return (T)lv.getInstance();
        }
        for (Toast lv2 : this.toastQueue) {
            if (!toastClass.isAssignableFrom(lv2.getClass()) || !lv2.getType().equals(type)) continue;
            return (T)lv2;
        }
        return null;
    }

    public void clear() {
        this.occupiedSpaces.clear();
        this.visibleEntries.clear();
        this.toastQueue.clear();
    }

    public void add(Toast toast) {
        this.toastQueue.add(toast);
    }

    public void onMusicTrackStart() {
        if (this.nowPlayingToast != null) {
            this.nowPlayingToast.init();
            this.nowPlayingToast.getInstance().show(this.client.options);
        }
    }

    public void onMusicTrackStop() {
        if (this.nowPlayingToast != null) {
            this.nowPlayingToast.getInstance().setVisibility(Toast.Visibility.HIDE);
        }
    }

    public void showNowPlayingToast() {
        this.nowPlayingToast = new Entry(this, (Toast)new NowPlayingToast(), 0, 0);
    }

    public void hideNowPlayingToast() {
        this.nowPlayingToast = null;
    }

    public MinecraftClient getClient() {
        return this.client;
    }

    public double getNotificationDisplayTimeMultiplier() {
        return this.client.options.getNotificationDisplayTime().getValue();
    }

    @Environment(value=EnvType.CLIENT)
    class Entry<T extends Toast> {
        private static final long DISAPPEAR_TIME = 600L;
        private final T instance;
        final int topIndex;
        final int requiredSpaceCount;
        private long startTime;
        private long fullyVisibleTime;
        Toast.Visibility visibility;
        private long showTime;
        private float visibleWidthPortion;
        protected boolean finishedRendering;
        final /* synthetic */ ToastManager field_2245;

        /*
         * WARNING - Possible parameter corruption
         */
        Entry(T instance, int topIndex, int requiredSpaceCount) {
            this.field_2245 = (ToastManager)arg;
            this.instance = instance;
            this.topIndex = topIndex;
            this.requiredSpaceCount = requiredSpaceCount;
            this.init();
        }

        public T getInstance() {
            return this.instance;
        }

        public void init() {
            this.startTime = -1L;
            this.fullyVisibleTime = -1L;
            this.visibility = Toast.Visibility.HIDE;
            this.showTime = 0L;
            this.visibleWidthPortion = 0.0f;
            this.finishedRendering = false;
        }

        public boolean isFinishedRendering() {
            return this.finishedRendering;
        }

        private void updateVisibleWidthPortion(long time) {
            float f = MathHelper.clamp((float)(time - this.startTime) / 600.0f, 0.0f, 1.0f);
            f *= f;
            this.visibleWidthPortion = this.visibility == Toast.Visibility.HIDE ? 1.0f - f : f;
        }

        public void update() {
            long l = Util.getMeasuringTimeMs();
            if (this.startTime == -1L) {
                this.startTime = l;
                this.visibility = Toast.Visibility.SHOW;
            }
            if (this.visibility == Toast.Visibility.SHOW && l - this.startTime <= 600L) {
                this.fullyVisibleTime = l;
            }
            this.showTime = l - this.fullyVisibleTime;
            this.updateVisibleWidthPortion(l);
            this.instance.update(this.field_2245, this.showTime);
            Toast.Visibility lv = this.instance.getVisibility();
            if (lv != this.visibility) {
                this.startTime = l - (long)((int)((1.0f - this.visibleWidthPortion) * 600.0f));
                this.visibility = lv;
            }
            boolean bl = this.finishedRendering;
            boolean bl2 = this.finishedRendering = this.visibility == Toast.Visibility.HIDE && l - this.startTime > 600L;
            if (this.finishedRendering && !bl) {
                this.instance.onFinishedRendering();
            }
        }

        public void draw(DrawContext context, int scaledWindowWidth) {
            if (this.finishedRendering) {
                return;
            }
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(this.instance.getXPos(scaledWindowWidth, this.visibleWidthPortion), this.instance.getYPos(this.topIndex));
            this.instance.draw(context, this.field_2245.client.textRenderer, this.showTime);
            context.getMatrices().popMatrix();
        }
    }
}

