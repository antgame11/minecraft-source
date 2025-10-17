/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.sound;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;

public record MusicSound(RegistryEntry<SoundEvent> sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) {
    public static final Codec<MusicSound> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)SoundEvent.ENTRY_CODEC.fieldOf("sound")).forGetter(sound -> sound.sound), ((MapCodec)Codec.INT.fieldOf("min_delay")).forGetter(sound -> sound.minDelay), ((MapCodec)Codec.INT.fieldOf("max_delay")).forGetter(sound -> sound.maxDelay), ((MapCodec)Codec.BOOL.fieldOf("replace_current_music")).forGetter(sound -> sound.replaceCurrentMusic)).apply((Applicative<MusicSound, ?>)instance, MusicSound::new));
}

