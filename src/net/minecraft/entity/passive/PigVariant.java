/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.ModelAndTexture;
import net.minecraft.util.StringIdentifiable;

public record PigVariant(ModelAndTexture<Model> modelAndTexture, SpawnConditionSelectors spawnConditions) implements VariantSelectorProvider<SpawnContext, SpawnCondition>
{
    public static final Codec<PigVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(ModelAndTexture.createMapCodec(Model.CODEC, Model.NORMAL).forGetter(PigVariant::modelAndTexture), ((MapCodec)SpawnConditionSelectors.CODEC.fieldOf("spawn_conditions")).forGetter(PigVariant::spawnConditions)).apply((Applicative<PigVariant, ?>)instance, PigVariant::new));
    public static final Codec<PigVariant> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(ModelAndTexture.createMapCodec(Model.CODEC, Model.NORMAL).forGetter(PigVariant::modelAndTexture)).apply((Applicative<PigVariant, ?>)instance, PigVariant::new));
    public static final Codec<RegistryEntry<PigVariant>> ENTRY_CODEC = RegistryFixedCodec.of(RegistryKeys.PIG_VARIANT);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<PigVariant>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.PIG_VARIANT);

    private PigVariant(ModelAndTexture<Model> modelAndTexture) {
        this(modelAndTexture, SpawnConditionSelectors.EMPTY);
    }

    @Override
    public List<VariantSelectorProvider.Selector<SpawnContext, SpawnCondition>> getSelectors() {
        return this.spawnConditions.selectors();
    }

    public static enum Model implements StringIdentifiable
    {
        NORMAL("normal"),
        COLD("cold");

        public static final Codec<Model> CODEC;
        private final String id;

        private Model(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Model::values);
        }
    }
}

