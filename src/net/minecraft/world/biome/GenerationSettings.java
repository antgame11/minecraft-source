/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Util;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;

public class GenerationSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final GenerationSettings INSTANCE = new GenerationSettings(RegistryEntryList.of(new RegistryEntry[0]), List.of());
    public static final MapCodec<GenerationSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ConfiguredCarver.LIST_CODEC.promotePartial((Consumer)Util.addPrefix("Carver: ", LOGGER::error)).fieldOf("carvers")).forGetter(generationSettings -> generationSettings.carvers), ((MapCodec)PlacedFeature.LISTS_CODEC.promotePartial((Consumer)Util.addPrefix("Features: ", LOGGER::error)).fieldOf("features")).forGetter(generationSettings -> generationSettings.features)).apply((Applicative<GenerationSettings, ?>)instance, GenerationSettings::new));
    private final RegistryEntryList<ConfiguredCarver<?>> carvers;
    private final List<RegistryEntryList<PlacedFeature>> features;
    private final Supplier<List<ConfiguredFeature<?, ?>>> flowerFeatures;
    private final Supplier<Set<PlacedFeature>> allowedFeatures;

    GenerationSettings(RegistryEntryList<ConfiguredCarver<?>> carvers, List<RegistryEntryList<PlacedFeature>> features) {
        this.carvers = carvers;
        this.features = features;
        this.flowerFeatures = Suppliers.memoize(() -> features.stream().flatMap(RegistryEntryList::stream).map(RegistryEntry::value).flatMap(PlacedFeature::getDecoratedFeatures).filter(feature -> feature.feature() == Feature.FLOWER).collect(ImmutableList.toImmutableList()));
        this.allowedFeatures = Suppliers.memoize(() -> features.stream().flatMap(RegistryEntryList::stream).map(RegistryEntry::value).collect(Collectors.toSet()));
    }

    public Iterable<RegistryEntry<ConfiguredCarver<?>>> getCarversForStep() {
        return this.carvers;
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures.get();
    }

    public List<RegistryEntryList<PlacedFeature>> getFeatures() {
        return this.features;
    }

    public boolean isFeatureAllowed(PlacedFeature feature) {
        return this.allowedFeatures.get().contains(feature);
    }

    public static class LookupBackedBuilder
    extends Builder {
        private final RegistryEntryLookup<PlacedFeature> placedFeatureLookup;
        private final RegistryEntryLookup<ConfiguredCarver<?>> configuredCarverLookup;

        public LookupBackedBuilder(RegistryEntryLookup<PlacedFeature> placedFeatureLookup, RegistryEntryLookup<ConfiguredCarver<?>> configuredCarverLookup) {
            this.placedFeatureLookup = placedFeatureLookup;
            this.configuredCarverLookup = configuredCarverLookup;
        }

        public LookupBackedBuilder feature(GenerationStep.Feature featureStep, RegistryKey<PlacedFeature> featureKey) {
            this.addFeature(featureStep.ordinal(), this.placedFeatureLookup.getOrThrow(featureKey));
            return this;
        }

        public LookupBackedBuilder carver(RegistryKey<ConfiguredCarver<?>> carverKey) {
            this.carver(this.configuredCarverLookup.getOrThrow(carverKey));
            return this;
        }
    }

    public static class Builder {
        private final List<RegistryEntry<ConfiguredCarver<?>>> carverStepsToCarvers = new ArrayList();
        private final List<List<RegistryEntry<PlacedFeature>>> indexedFeaturesList = new ArrayList<List<RegistryEntry<PlacedFeature>>>();

        public Builder feature(GenerationStep.Feature featureStep, RegistryEntry<PlacedFeature> featureEntry) {
            return this.addFeature(featureStep.ordinal(), featureEntry);
        }

        public Builder addFeature(int ordinal, RegistryEntry<PlacedFeature> featureEntry) {
            this.fillFeaturesList(ordinal);
            this.indexedFeaturesList.get(ordinal).add(featureEntry);
            return this;
        }

        public Builder carver(RegistryEntry<ConfiguredCarver<?>> carverEntry) {
            this.carverStepsToCarvers.add(carverEntry);
            return this;
        }

        private void fillFeaturesList(int size) {
            while (this.indexedFeaturesList.size() <= size) {
                this.indexedFeaturesList.add(Lists.newArrayList());
            }
        }

        public GenerationSettings build() {
            return new GenerationSettings(RegistryEntryList.of(this.carverStepsToCarvers), this.indexedFeaturesList.stream().map(RegistryEntryList::of).collect(ImmutableList.toImmutableList()));
        }
    }
}

