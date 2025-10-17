/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MultipartBlockStateModel
implements BlockStateModel {
    private final MultipartBakedModel bakedModels;
    private final BlockState state;
    @Nullable
    private List<BlockStateModel> models;

    MultipartBlockStateModel(MultipartBakedModel bakedModels, BlockState state) {
        this.bakedModels = bakedModels;
        this.state = state;
    }

    @Override
    public Sprite particleSprite() {
        return this.bakedModels.particleSprite;
    }

    @Override
    public void addParts(Random random, List<BlockModelPart> parts) {
        if (this.models == null) {
            this.models = this.bakedModels.build(this.state);
        }
        long l = random.nextLong();
        for (BlockStateModel lv : this.models) {
            random.setSeed(l);
            lv.addParts(random, parts);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class MultipartBakedModel {
        private final List<Selector<BlockStateModel>> selectors;
        final Sprite particleSprite;
        private final Map<BitSet, List<BlockStateModel>> map = new ConcurrentHashMap<BitSet, List<BlockStateModel>>();

        private static BlockStateModel getFirst(List<Selector<BlockStateModel>> selectors) {
            if (selectors.isEmpty()) {
                throw new IllegalArgumentException("Model must have at least one selector");
            }
            return selectors.getFirst().model();
        }

        public MultipartBakedModel(List<Selector<BlockStateModel>> selectors) {
            this.selectors = selectors;
            BlockStateModel lv = MultipartBakedModel.getFirst(selectors);
            this.particleSprite = lv.particleSprite();
        }

        public List<BlockStateModel> build(BlockState state) {
            BitSet bitSet2 = new BitSet();
            for (int i = 0; i < this.selectors.size(); ++i) {
                if (!this.selectors.get((int)i).condition.test(state)) continue;
                bitSet2.set(i);
            }
            return this.map.computeIfAbsent(bitSet2, bitSet -> {
                ImmutableList.Builder builder = ImmutableList.builder();
                for (int i = 0; i < this.selectors.size(); ++i) {
                    if (!bitSet.get(i)) continue;
                    builder.add((BlockStateModel)this.selectors.get((int)i).model);
                }
                return builder.build();
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class MultipartUnbaked
    implements BlockStateModel.UnbakedGrouped {
        final List<Selector<BlockStateModel.Unbaked>> selectors;
        private final Baker.ResolvableCacheKey<MultipartBakedModel> bakerCache = new Baker.ResolvableCacheKey<MultipartBakedModel>(){

            @Override
            public MultipartBakedModel compute(Baker arg) {
                ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize(selectors.size());
                for (Selector<BlockStateModel.Unbaked> lv : selectors) {
                    builder.add(lv.build(((BlockStateModel.Unbaked)lv.model).bake(arg)));
                }
                return new MultipartBakedModel((List<Selector<BlockStateModel>>)((Object)builder.build()));
            }

            @Override
            public /* synthetic */ Object compute(Baker arg) {
                return this.compute(arg);
            }
        };

        public MultipartUnbaked(List<Selector<BlockStateModel.Unbaked>> selectors) {
            this.selectors = selectors;
        }

        @Override
        public Object getEqualityGroup(BlockState state) {
            IntArrayList intList = new IntArrayList();
            for (int i = 0; i < this.selectors.size(); ++i) {
                if (!this.selectors.get((int)i).condition.test(state)) continue;
                intList.add(i);
            }
            @Environment(value=EnvType.CLIENT)
            record EqualityGroup(MultipartUnbaked model, IntList selectors) {
            }
            return new EqualityGroup(this, intList);
        }

        @Override
        public void resolve(ResolvableModel.Resolver resolver) {
            this.selectors.forEach(selector -> ((BlockStateModel.Unbaked)selector.model).resolve(resolver));
        }

        @Override
        public BlockStateModel bake(BlockState state, Baker baker) {
            MultipartBakedModel lv = baker.compute(this.bakerCache);
            return new MultipartBlockStateModel(lv, state);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Selector<T>(Predicate<BlockState> condition, T model) {
        public <S> Selector<S> build(S object) {
            return new Selector<S>(this.condition, object);
        }
    }
}

