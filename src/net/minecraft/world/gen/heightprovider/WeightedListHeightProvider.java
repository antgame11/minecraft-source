/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.heightprovider;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.heightprovider.HeightProviderType;

public class WeightedListHeightProvider
extends HeightProvider {
    public static final MapCodec<WeightedListHeightProvider> WEIGHTED_LIST_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Pool.createNonEmptyCodec(HeightProvider.CODEC).fieldOf("distribution")).forGetter(arg -> arg.weightedList)).apply((Applicative<WeightedListHeightProvider, ?>)instance, WeightedListHeightProvider::new));
    private final Pool<HeightProvider> weightedList;

    public WeightedListHeightProvider(Pool<HeightProvider> weightedList) {
        this.weightedList = weightedList;
    }

    @Override
    public int get(Random random, HeightContext context) {
        return this.weightedList.get(random).get(random, context);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.WEIGHTED_LIST;
    }
}

