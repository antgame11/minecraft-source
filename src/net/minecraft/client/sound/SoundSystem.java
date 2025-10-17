/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.sound;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundExecutor;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.SoundListener;
import net.minecraft.client.sound.SoundListenerTransform;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.Source;
import net.minecraft.client.sound.StaticSound;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Environment(value=EnvType.CLIENT)
public class SoundSystem {
    private static final Marker MARKER = MarkerFactory.getMarker("SOUNDS");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float MIN_PITCH = 0.5f;
    private static final float MAX_PITCH = 2.0f;
    private static final float MIN_VOLUME = 0.0f;
    private static final float MAX_VOLUME = 1.0f;
    private static final int field_33025 = 20;
    private static final Set<Identifier> UNKNOWN_SOUNDS = Sets.newHashSet();
    private static final long MIN_TIME_INTERVAL_TO_RELOAD_SOUNDS = 1000L;
    public static final String FOR_THE_DEBUG = "FOR THE DEBUG!";
    public static final String OPENAL_SOFT_ON = "OpenAL Soft on ";
    public static final int OPENAL_SOFT_ON_LENGTH = "OpenAL Soft on ".length();
    private final MusicTracker musicTracker;
    private final SoundManager soundManager;
    private final GameOptions options;
    private boolean started;
    private final SoundEngine soundEngine = new SoundEngine();
    private final SoundListener listener = this.soundEngine.getListener();
    private final SoundLoader soundLoader;
    private final SoundExecutor taskQueue = new SoundExecutor();
    private final Channel channel = new Channel(this.soundEngine, this.taskQueue);
    private int ticks;
    private long lastSoundDeviceCheckTime;
    private final AtomicReference<DeviceChangeStatus> deviceChangeStatus = new AtomicReference<DeviceChangeStatus>(DeviceChangeStatus.NO_CHANGE);
    private final Map<SoundInstance, Channel.SourceManager> sources = Maps.newHashMap();
    private final Multimap<SoundCategory, SoundInstance> sounds = HashMultimap.create();
    private final List<TickableSoundInstance> tickingSounds = Lists.newArrayList();
    private final Map<SoundInstance, Integer> soundStartTicks = Maps.newHashMap();
    private final Map<SoundInstance, Integer> soundEndTicks = Maps.newHashMap();
    private final List<SoundInstanceListener> listeners = Lists.newArrayList();
    private final List<TickableSoundInstance> soundsToPlayNextTick = Lists.newArrayList();
    private final List<Sound> preloadedSounds = Lists.newArrayList();

    public SoundSystem(MusicTracker musicTracker, SoundManager soundManager, GameOptions options, ResourceFactory resourceFactory) {
        this.musicTracker = musicTracker;
        this.soundManager = soundManager;
        this.options = options;
        this.soundLoader = new SoundLoader(resourceFactory);
    }

    public void reloadSounds() {
        UNKNOWN_SOUNDS.clear();
        for (SoundEvent lv : Registries.SOUND_EVENT) {
            Identifier lv2;
            if (lv == SoundEvents.INTENTIONALLY_EMPTY || this.soundManager.get(lv2 = lv.id()) != null) continue;
            LOGGER.warn("Missing sound for event: {}", (Object)Registries.SOUND_EVENT.getId(lv));
            UNKNOWN_SOUNDS.add(lv2);
        }
        this.stop();
        this.start();
    }

    private synchronized void start() {
        if (this.started) {
            return;
        }
        try {
            String string = this.options.getSoundDevice().getValue();
            this.soundEngine.init("".equals(string) ? null : string, this.options.getDirectionalAudio().getValue());
            this.listener.init();
            this.soundLoader.loadStatic(this.preloadedSounds).thenRun(this.preloadedSounds::clear);
            this.started = true;
            LOGGER.info(MARKER, "Sound engine started");
        } catch (RuntimeException runtimeException) {
            LOGGER.error(MARKER, "Error starting SoundSystem. Turning off sounds & music", runtimeException);
        }
    }

    public void updateSoundVolume(SoundCategory category) {
        if (!this.started) {
            return;
        }
        if ((category == SoundCategory.MASTER || category == SoundCategory.MUSIC) && this.options.getSoundVolume(SoundCategory.MUSIC) > 0.0f) {
            this.musicTracker.tryShowToast();
        }
        this.sources.forEach((source2, sourceManager) -> {
            float f = this.getAdjustedVolume((SoundInstance)source2);
            sourceManager.run(source -> source.setVolume(f));
        });
    }

    public void stop() {
        if (this.started) {
            this.stopAll();
            this.soundLoader.close();
            this.soundEngine.close();
            this.started = false;
        }
    }

    public void stopAbruptly() {
        if (this.started) {
            this.soundEngine.close();
        }
    }

    public void stop(SoundInstance sound) {
        Channel.SourceManager lv;
        if (this.started && (lv = this.sources.get(sound)) != null) {
            lv.run(Source::stop);
        }
    }

    public void setVolume(SoundInstance sound, float volume) {
        Channel.SourceManager lv;
        if (this.started && (lv = this.sources.get(sound)) != null) {
            lv.run(source -> source.setVolume(volume * this.getAdjustedVolume(sound)));
        }
    }

    public void stopAll() {
        if (this.started) {
            this.taskQueue.stop();
            this.sources.clear();
            this.channel.close();
            this.soundStartTicks.clear();
            this.tickingSounds.clear();
            this.sounds.clear();
            this.soundEndTicks.clear();
            this.soundsToPlayNextTick.clear();
            this.taskQueue.restart();
        }
    }

    public void registerListener(SoundInstanceListener listener) {
        this.listeners.add(listener);
    }

    public void unregisterListener(SoundInstanceListener listener) {
        this.listeners.remove(listener);
    }

    private boolean shouldReloadSounds() {
        boolean bl;
        if (this.soundEngine.isDeviceUnavailable()) {
            LOGGER.info("Audio device was lost!");
            return true;
        }
        long l = Util.getMeasuringTimeMs();
        boolean bl2 = bl = l - this.lastSoundDeviceCheckTime >= 1000L;
        if (bl) {
            this.lastSoundDeviceCheckTime = l;
            if (this.deviceChangeStatus.compareAndSet(DeviceChangeStatus.NO_CHANGE, DeviceChangeStatus.ONGOING)) {
                String string = this.options.getSoundDevice().getValue();
                Util.getIoWorkerExecutor().execute(() -> {
                    if ("".equals(string)) {
                        if (this.soundEngine.updateDeviceSpecifier()) {
                            LOGGER.info("System default audio device has changed!");
                            this.deviceChangeStatus.compareAndSet(DeviceChangeStatus.ONGOING, DeviceChangeStatus.CHANGE_DETECTED);
                        }
                    } else if (!this.soundEngine.getCurrentDeviceName().equals(string) && this.soundEngine.getSoundDevices().contains(string)) {
                        LOGGER.info("Preferred audio device has become available!");
                        this.deviceChangeStatus.compareAndSet(DeviceChangeStatus.ONGOING, DeviceChangeStatus.CHANGE_DETECTED);
                    }
                    this.deviceChangeStatus.compareAndSet(DeviceChangeStatus.ONGOING, DeviceChangeStatus.NO_CHANGE);
                });
            }
        }
        return this.deviceChangeStatus.compareAndSet(DeviceChangeStatus.CHANGE_DETECTED, DeviceChangeStatus.NO_CHANGE);
    }

    public void tick(boolean paused) {
        if (this.shouldReloadSounds()) {
            this.reloadSounds();
        }
        if (!paused) {
            this.tick();
        } else {
            this.tickPaused();
        }
        this.channel.tick();
    }

    private void tick() {
        ++this.ticks;
        this.soundsToPlayNextTick.stream().filter(SoundInstance::canPlay).forEach(this::play);
        this.soundsToPlayNextTick.clear();
        for (TickableSoundInstance lv : this.tickingSounds) {
            if (!lv.canPlay()) {
                this.stop(lv);
            }
            lv.tick();
            if (lv.isDone()) {
                this.stop(lv);
                continue;
            }
            float f = this.getAdjustedVolume(lv);
            float g = this.getAdjustedPitch(lv);
            Vec3d lv2 = new Vec3d(lv.getX(), lv.getY(), lv.getZ());
            Channel.SourceManager lv3 = this.sources.get(lv);
            if (lv3 == null) continue;
            lv3.run(source -> {
                source.setVolume(f);
                source.setPitch(g);
                source.setPosition(lv2);
            });
        }
        Iterator<Map.Entry<SoundInstance, Channel.SourceManager>> iterator = this.sources.entrySet().iterator();
        while (iterator.hasNext()) {
            int i;
            Map.Entry<SoundInstance, Channel.SourceManager> entry = iterator.next();
            Channel.SourceManager lv4 = entry.getValue();
            SoundInstance lv5 = entry.getKey();
            if (!lv4.isStopped() || (i = this.soundEndTicks.get(lv5).intValue()) > this.ticks) continue;
            if (SoundSystem.shouldDelayRepeat(lv5)) {
                this.soundStartTicks.put(lv5, this.ticks + lv5.getRepeatDelay());
            }
            iterator.remove();
            LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", (Object)lv4);
            this.soundEndTicks.remove(lv5);
            try {
                this.sounds.remove((Object)lv5.getCategory(), lv5);
            } catch (RuntimeException runtimeException) {
                // empty catch block
            }
            if (!(lv5 instanceof TickableSoundInstance)) continue;
            this.tickingSounds.remove(lv5);
        }
        Iterator<Map.Entry<SoundInstance, Integer>> iterator2 = this.soundStartTicks.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<SoundInstance, Integer> entry2 = iterator2.next();
            if (this.ticks < entry2.getValue()) continue;
            SoundInstance lv5 = entry2.getKey();
            if (lv5 instanceof TickableSoundInstance) {
                ((TickableSoundInstance)lv5).tick();
            }
            this.play(lv5);
            iterator2.remove();
        }
    }

    private void tickPaused() {
        Iterator<Map.Entry<SoundInstance, Channel.SourceManager>> iterator = this.sources.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SoundInstance, Channel.SourceManager> entry = iterator.next();
            Channel.SourceManager lv = entry.getValue();
            SoundInstance lv2 = entry.getKey();
            if (lv2.getCategory() != SoundCategory.MUSIC || !lv.isStopped()) continue;
            iterator.remove();
            LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", (Object)lv);
            this.soundEndTicks.remove(lv2);
            this.sounds.remove((Object)lv2.getCategory(), lv2);
        }
    }

    private static boolean hasRepeatDelay(SoundInstance sound) {
        return sound.getRepeatDelay() > 0;
    }

    private static boolean shouldDelayRepeat(SoundInstance sound) {
        return sound.isRepeatable() && SoundSystem.hasRepeatDelay(sound);
    }

    private static boolean shouldRepeatInstantly(SoundInstance sound) {
        return sound.isRepeatable() && !SoundSystem.hasRepeatDelay(sound);
    }

    public boolean isPlaying(SoundInstance sound) {
        if (!this.started) {
            return false;
        }
        if (this.soundEndTicks.containsKey(sound) && this.soundEndTicks.get(sound) <= this.ticks) {
            return true;
        }
        return this.sources.containsKey(sound);
    }

    public PlayResult play(SoundInstance sound2) {
        Sound lv3;
        if (!this.started) {
            return PlayResult.NOT_STARTED;
        }
        if (!sound2.canPlay()) {
            return PlayResult.NOT_STARTED;
        }
        WeightedSoundSet lv = sound2.getSoundSet(this.soundManager);
        Identifier lv2 = sound2.getId();
        if (lv == null) {
            if (UNKNOWN_SOUNDS.add(lv2)) {
                LOGGER.warn(MARKER, "Unable to play unknown soundEvent: {}", (Object)lv2);
            }
            if (!SharedConstants.SUBTITLES) {
                return PlayResult.NOT_STARTED;
            }
            lv = new WeightedSoundSet(lv2, FOR_THE_DEBUG);
        }
        if ((lv3 = sound2.getSound()) == SoundManager.INTENTIONALLY_EMPTY_SOUND) {
            return PlayResult.NOT_STARTED;
        }
        if (lv3 == SoundManager.MISSING_SOUND) {
            if (UNKNOWN_SOUNDS.add(lv2)) {
                LOGGER.warn(MARKER, "Unable to play empty soundEvent: {}", (Object)lv2);
            }
            return PlayResult.NOT_STARTED;
        }
        float f = sound2.getVolume();
        float g = Math.max(f, 1.0f) * (float)lv3.getAttenuation();
        SoundCategory lv4 = sound2.getCategory();
        float h = this.getAdjustedVolume(f, lv4);
        float i = this.getAdjustedPitch(sound2);
        SoundInstance.AttenuationType lv5 = sound2.getAttenuationType();
        boolean bl = sound2.isRelative();
        if (!this.listeners.isEmpty()) {
            float j = bl || lv5 == SoundInstance.AttenuationType.NONE ? Float.POSITIVE_INFINITY : g;
            for (SoundInstanceListener lv6 : this.listeners) {
                lv6.onSoundPlayed(sound2, lv, j);
            }
        }
        boolean bl2 = false;
        if (h == 0.0f) {
            if (sound2.shouldAlwaysPlay() || lv4 == SoundCategory.MUSIC) {
                bl2 = true;
            } else {
                LOGGER.debug(MARKER, "Skipped playing sound {}, volume was zero.", (Object)lv3.getIdentifier());
                return PlayResult.NOT_STARTED;
            }
        }
        Vec3d lv7 = new Vec3d(sound2.getX(), sound2.getY(), sound2.getZ());
        boolean bl3 = SoundSystem.shouldRepeatInstantly(sound2);
        boolean bl4 = lv3.isStreamed();
        CompletableFuture<Channel.SourceManager> completableFuture = this.channel.createSource(lv3.isStreamed() ? SoundEngine.RunMode.STREAMING : SoundEngine.RunMode.STATIC);
        Channel.SourceManager lv8 = completableFuture.join();
        if (lv8 == null) {
            if (SharedConstants.isDevelopment) {
                LOGGER.warn("Failed to create new sound handle");
            }
            return PlayResult.NOT_STARTED;
        }
        LOGGER.debug(MARKER, "Playing sound {} for event {}", (Object)lv3.getIdentifier(), (Object)lv2);
        this.soundEndTicks.put(sound2, this.ticks + 20);
        this.sources.put(sound2, lv8);
        this.sounds.put(lv4, sound2);
        lv8.run(source -> {
            source.setPitch(i);
            source.setVolume(h);
            if (lv5 == SoundInstance.AttenuationType.LINEAR) {
                source.setAttenuation(g);
            } else {
                source.disableAttenuation();
            }
            source.setLooping(bl3 && !bl4);
            source.setPosition(lv7);
            source.setRelative(bl);
        });
        if (!bl4) {
            this.soundLoader.loadStatic(lv3.getLocation()).thenAccept(sound -> lv8.run(source -> {
                source.setBuffer((StaticSound)sound);
                source.play();
            }));
        } else {
            this.soundLoader.loadStreamed(lv3.getLocation(), bl3).thenAccept(stream -> lv8.run(source -> {
                source.setStream((AudioStream)stream);
                source.play();
            }));
        }
        if (sound2 instanceof TickableSoundInstance) {
            this.tickingSounds.add((TickableSoundInstance)sound2);
        }
        if (bl2) {
            return PlayResult.STARTED_SILENTLY;
        }
        return PlayResult.STARTED;
    }

    public void playNextTick(TickableSoundInstance sound) {
        this.soundsToPlayNextTick.add(sound);
    }

    public void addPreloadedSound(Sound sound) {
        this.preloadedSounds.add(sound);
    }

    private float getAdjustedPitch(SoundInstance sound) {
        return MathHelper.clamp(sound.getPitch(), 0.5f, 2.0f);
    }

    private float getAdjustedVolume(SoundInstance sound) {
        return this.getAdjustedVolume(sound.getVolume(), sound.getCategory());
    }

    private float getAdjustedVolume(float volume, SoundCategory category) {
        return MathHelper.clamp(volume, 0.0f, 1.0f) * MathHelper.clamp(this.options.getSoundVolume(category), 0.0f, 1.0f);
    }

    public void pauseAllExcept(SoundCategory ... categories) {
        if (!this.started) {
            return;
        }
        for (Map.Entry<SoundInstance, Channel.SourceManager> entry : this.sources.entrySet()) {
            if (List.of(categories).contains((Object)entry.getKey().getCategory())) continue;
            entry.getValue().run(Source::pause);
        }
    }

    public void resumeAll() {
        if (this.started) {
            this.channel.execute(sources -> sources.forEach(Source::resume));
        }
    }

    public void play(SoundInstance sound, int delay) {
        this.soundStartTicks.put(sound, this.ticks + delay);
    }

    public void updateListenerPosition(Camera camera) {
        if (!this.started || !camera.isReady()) {
            return;
        }
        SoundListenerTransform lv = new SoundListenerTransform(camera.getPos(), new Vec3d(camera.getHorizontalPlane()), new Vec3d(camera.getVerticalPlane()));
        this.taskQueue.execute(() -> this.listener.setTransform(lv));
    }

    public void stopSounds(@Nullable Identifier id, @Nullable SoundCategory category) {
        if (category != null) {
            for (SoundInstance lv : this.sounds.get(category)) {
                if (id != null && !lv.getId().equals(id)) continue;
                this.stop(lv);
            }
        } else if (id == null) {
            this.stopAll();
        } else {
            for (SoundInstance lv : this.sources.keySet()) {
                if (!lv.getId().equals(id)) continue;
                this.stop(lv);
            }
        }
    }

    public String getDebugString() {
        return this.soundEngine.getDebugString();
    }

    public List<String> getSoundDevices() {
        return this.soundEngine.getSoundDevices();
    }

    public SoundListenerTransform getListenerTransform() {
        return this.listener.getTransform();
    }

    @Environment(value=EnvType.CLIENT)
    static enum DeviceChangeStatus {
        ONGOING,
        CHANGE_DETECTED,
        NO_CHANGE;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum PlayResult {
        STARTED,
        STARTED_SILENTLY,
        NOT_STARTED;

    }
}

