/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.sound;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public record SoundEvent(Identifier id, Optional<Float> fixedRange) {
    public static final Codec<SoundEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("sound_id")).forGetter(SoundEvent::id), Codec.FLOAT.lenientOptionalFieldOf("range").forGetter(SoundEvent::fixedRange)).apply((Applicative<SoundEvent, ?>)instance, SoundEvent::of));
    public static final Codec<RegistryEntry<SoundEvent>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.SOUND_EVENT, CODEC);
    public static final PacketCodec<ByteBuf, SoundEvent> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, SoundEvent::id, PacketCodecs.FLOAT.collect(PacketCodecs::optional), SoundEvent::fixedRange, SoundEvent::of);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<SoundEvent>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.SOUND_EVENT, PACKET_CODEC);

    private static SoundEvent of(Identifier id, Optional<Float> fixedRange) {
        return fixedRange.map(fixedRangex -> SoundEvent.of(id, fixedRangex.floatValue())).orElseGet(() -> SoundEvent.of(id));
    }

    public static SoundEvent of(Identifier id) {
        return new SoundEvent(id, Optional.empty());
    }

    public static SoundEvent of(Identifier id, float fixedRange) {
        return new SoundEvent(id, Optional.of(Float.valueOf(fixedRange)));
    }

    public float getDistanceToTravel(float volume) {
        return this.fixedRange.orElse(Float.valueOf(volume > 1.0f ? 16.0f * volume : 16.0f)).floatValue();
    }
}

