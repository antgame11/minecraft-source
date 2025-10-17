/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.render.model.SimpleBlockStateModel;
import net.minecraft.client.render.model.WeightedBlockStateModel;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public interface BlockStateModel {
    public void addParts(Random var1, List<BlockModelPart> var2);

    default public List<BlockModelPart> getParts(Random random) {
        ObjectArrayList<BlockModelPart> list = new ObjectArrayList<BlockModelPart>();
        this.addParts(random, list);
        return list;
    }

    public Sprite particleSprite();

    @Environment(value=EnvType.CLIENT)
    public static class CachedUnbaked
    implements UnbakedGrouped {
        final Unbaked delegate;
        private final Baker.ResolvableCacheKey<BlockStateModel> cacheKey = new Baker.ResolvableCacheKey<BlockStateModel>(){

            @Override
            public BlockStateModel compute(Baker arg) {
                return delegate.bake(arg);
            }

            @Override
            public /* synthetic */ Object compute(Baker arg) {
                return this.compute(arg);
            }
        };

        public CachedUnbaked(Unbaked delegate) {
            this.delegate = delegate;
        }

        @Override
        public void resolve(ResolvableModel.Resolver resolver) {
            this.delegate.resolve(resolver);
        }

        @Override
        public BlockStateModel bake(BlockState state, Baker baker) {
            return baker.compute(this.cacheKey);
        }

        @Override
        public Object getEqualityGroup(BlockState state) {
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Unbaked
    extends ResolvableModel {
        public static final Codec<Weighted<ModelVariant>> WEIGHTED_VARIANT_CODEC = RecordCodecBuilder.create(instance -> instance.group(ModelVariant.MAP_CODEC.forGetter(Weighted::value), Codecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(Weighted::weight)).apply((Applicative<Weighted, ?>)instance, Weighted::new));
        public static final Codec<WeightedBlockStateModel.Unbaked> WEIGHTED_CODEC = Codecs.nonEmptyList(WEIGHTED_VARIANT_CODEC.listOf()).flatComapMap(list -> new WeightedBlockStateModel.Unbaked(Pool.of(Lists.transform(list, weighted -> weighted.transform(SimpleBlockStateModel.Unbaked::new)))), arg -> {
            List<Weighted<Unbaked>> list = arg.entries().getEntries();
            ArrayList<Weighted<ModelVariant>> list2 = new ArrayList<Weighted<ModelVariant>>(list.size());
            for (Weighted<Unbaked> lv : list) {
                Unbaked object = lv.value();
                if (object instanceof SimpleBlockStateModel.Unbaked) {
                    SimpleBlockStateModel.Unbaked lv2 = (SimpleBlockStateModel.Unbaked)object;
                    list2.add(new Weighted<ModelVariant>(lv2.variant(), lv.weight()));
                    continue;
                }
                return DataResult.error(() -> "Only single variants are supported");
            }
            return DataResult.success(list2);
        });
        public static final Codec<Unbaked> CODEC = Codec.either(WEIGHTED_CODEC, SimpleBlockStateModel.Unbaked.CODEC).flatComapMap(either -> (Unbaked)((Object)either.map(left -> left, right -> right)), variant -> {
            Unbaked unbaked = variant;
            Objects.requireNonNull(unbaked);
            Unbaked lv = unbaked;
            int i = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{SimpleBlockStateModel.Unbaked.class, WeightedBlockStateModel.Unbaked.class}, (Object)lv, i)) {
                case 0 -> {
                    SimpleBlockStateModel.Unbaked lv2 = (SimpleBlockStateModel.Unbaked)lv;
                    yield DataResult.success(Either.right(lv2));
                }
                case 1 -> {
                    WeightedBlockStateModel.Unbaked lv3 = (WeightedBlockStateModel.Unbaked)lv;
                    yield DataResult.success(Either.left(lv3));
                }
                default -> DataResult.error(() -> "Only a single variant or a list of variants are supported");
            };
        });

        public BlockStateModel bake(Baker var1);

        default public UnbakedGrouped cached() {
            return new CachedUnbaked(this);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface UnbakedGrouped
    extends ResolvableModel {
        public BlockStateModel bake(BlockState var1, Baker var2);

        public Object getEqualityGroup(BlockState var1);
    }
}

