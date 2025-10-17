/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecorator;

public class FallenTreeFeatureConfig
implements FeatureConfig {
    public static final Codec<FallenTreeFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("trunk_provider")).forGetter(featureConfig -> featureConfig.trunkProvider), ((MapCodec)IntProvider.createValidatingCodec(0, 16).fieldOf("log_length")).forGetter(featureConfig -> featureConfig.logLength), ((MapCodec)TreeDecorator.TYPE_CODEC.listOf().fieldOf("stump_decorators")).forGetter(featureConfig -> featureConfig.stumpDecorators), ((MapCodec)TreeDecorator.TYPE_CODEC.listOf().fieldOf("log_decorators")).forGetter(featureConfig -> featureConfig.logDecorators)).apply((Applicative<FallenTreeFeatureConfig, ?>)instance, FallenTreeFeatureConfig::new));
    public final BlockStateProvider trunkProvider;
    public final IntProvider logLength;
    public final List<TreeDecorator> stumpDecorators;
    public final List<TreeDecorator> logDecorators;

    protected FallenTreeFeatureConfig(BlockStateProvider trunkProvider, IntProvider logLength, List<TreeDecorator> stumpDecorators, List<TreeDecorator> logDecorators) {
        this.trunkProvider = trunkProvider;
        this.logLength = logLength;
        this.stumpDecorators = stumpDecorators;
        this.logDecorators = logDecorators;
    }

    public static class Builder {
        private final BlockStateProvider trunkProvider;
        private final IntProvider logLength;
        private List<TreeDecorator> stumpDecorators = new ArrayList<TreeDecorator>();
        private List<TreeDecorator> logDecorators = new ArrayList<TreeDecorator>();

        public Builder(BlockStateProvider trunkProvider, IntProvider logLength) {
            this.trunkProvider = trunkProvider;
            this.logLength = logLength;
        }

        public Builder stumpDecorators(List<TreeDecorator> stumpDecorators) {
            this.stumpDecorators = stumpDecorators;
            return this;
        }

        public Builder logDecorators(List<TreeDecorator> logDecorators) {
            this.logDecorators = logDecorators;
            return this;
        }

        public FallenTreeFeatureConfig build() {
            return new FallenTreeFeatureConfig(this.trunkProvider, this.logLength, this.stumpDecorators, this.logDecorators);
        }
    }
}

