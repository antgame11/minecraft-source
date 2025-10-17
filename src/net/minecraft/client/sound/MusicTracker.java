/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.sound;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TranslatableOption;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MusicTracker {
    private static final int DEFAULT_TIME_UNTIL_NEXT_SONG = 100;
    private final Random random = Random.create();
    private final MinecraftClient client;
    @Nullable
    private SoundInstance current;
    private MusicFrequency musicFrequency;
    private float volume = 1.0f;
    private int timeUntilNextSong = 100;
    private boolean shownToast = false;

    public MusicTracker(MinecraftClient client) {
        this.client = client;
        this.musicFrequency = client.options.getMusicFrequency().getValue();
    }

    public void tick() {
        boolean bl;
        MusicInstance lv = this.client.getMusicInstance();
        float f = lv.volume();
        if (this.current != null && this.volume != f && !(bl = this.canFadeTowardsVolume(f))) {
            return;
        }
        MusicSound lv2 = lv.music();
        if (lv2 == null) {
            this.timeUntilNextSong = Math.max(this.timeUntilNextSong, 100);
            return;
        }
        if (this.current != null) {
            if (lv.shouldReplace(this.current)) {
                this.client.getSoundManager().stop(this.current);
                this.timeUntilNextSong = MathHelper.nextInt(this.random, 0, lv2.minDelay() / 2);
            }
            if (!this.client.getSoundManager().isPlaying(this.current)) {
                this.current = null;
                this.timeUntilNextSong = Math.min(this.timeUntilNextSong, this.musicFrequency.getDelayBeforePlaying(lv2, this.random));
            }
        }
        this.timeUntilNextSong = Math.min(this.timeUntilNextSong, this.musicFrequency.getDelayBeforePlaying(lv2, this.random));
        if (this.current == null && this.timeUntilNextSong-- <= 0) {
            this.play(lv);
        }
    }

    public void play(MusicInstance instance) {
        SoundEvent lv = instance.music().sound().value();
        this.current = PositionedSoundInstance.music(lv, instance.volume());
        switch (this.client.getSoundManager().play(this.current)) {
            case STARTED: {
                this.client.getToastManager().onMusicTrackStart();
                this.shownToast = true;
                break;
            }
            case STARTED_SILENTLY: {
                this.shownToast = false;
            }
        }
        this.timeUntilNextSong = Integer.MAX_VALUE;
        this.volume = instance.volume();
    }

    public void tryShowToast() {
        if (!this.shownToast) {
            this.client.getToastManager().onMusicTrackStart();
            this.shownToast = true;
        }
    }

    public void stop(MusicSound type) {
        if (this.isPlayingType(type)) {
            this.stop();
        }
    }

    public void stop() {
        if (this.current != null) {
            this.client.getSoundManager().stop(this.current);
            this.current = null;
            this.client.getToastManager().onMusicTrackStop();
        }
        this.timeUntilNextSong += 100;
    }

    private boolean canFadeTowardsVolume(float volume) {
        if (this.current == null) {
            return false;
        }
        if (this.volume == volume) {
            return true;
        }
        if (this.volume < volume) {
            this.volume += MathHelper.clamp(this.volume, 5.0E-4f, 0.005f);
            if (this.volume > volume) {
                this.volume = volume;
            }
        } else {
            this.volume = 0.03f * volume + 0.97f * this.volume;
            if (Math.abs(this.volume - volume) < 1.0E-4f || this.volume < volume) {
                this.volume = volume;
            }
        }
        this.volume = MathHelper.clamp(this.volume, 0.0f, 1.0f);
        if (this.volume <= 1.0E-4f) {
            this.stop();
            return false;
        }
        this.client.getSoundManager().setVolume(this.current, this.volume);
        return true;
    }

    public boolean isPlayingType(MusicSound type) {
        if (this.current == null) {
            return false;
        }
        return type.sound().value().id().equals(this.current.getId());
    }

    @Nullable
    public String getCurrentMusicTranslationKey() {
        Sound lv;
        if (this.current != null && (lv = this.current.getSound()) != null) {
            return lv.getIdentifier().toShortTranslationKey();
        }
        return null;
    }

    public void setMusicFrequency(MusicFrequency musicFrequency) {
        this.musicFrequency = musicFrequency;
        this.timeUntilNextSong = this.musicFrequency.getDelayBeforePlaying(this.client.getMusicInstance().music(), this.random);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum MusicFrequency implements TranslatableOption,
    StringIdentifiable
    {
        DEFAULT(20),
        FREQUENT(10),
        CONSTANT(0);

        public static final Codec<MusicFrequency> CODEC;
        private static final String TRANSLATION_KEY_PREFIX = "options.music_frequency.";
        private final int index;
        private final int delayBetweenTracks;
        private final String translationKey;

        private MusicFrequency(int index) {
            this.index = index;
            this.delayBetweenTracks = index * 1200;
            this.translationKey = TRANSLATION_KEY_PREFIX + this.name().toLowerCase();
        }

        int getDelayBeforePlaying(@Nullable MusicSound music, Random random) {
            if (music == null) {
                return this.delayBetweenTracks;
            }
            if (this == CONSTANT) {
                return 100;
            }
            int i = Math.min(music.minDelay(), this.delayBetweenTracks);
            int j = Math.min(music.maxDelay(), this.delayBetweenTracks);
            return MathHelper.nextInt(random, i, j);
        }

        @Override
        public int getId() {
            return this.index;
        }

        @Override
        public String getTranslationKey() {
            return this.translationKey;
        }

        @Override
        public String asString() {
            return this.name();
        }

        static {
            CODEC = StringIdentifiable.createCodec(MusicFrequency::values);
        }
    }
}

