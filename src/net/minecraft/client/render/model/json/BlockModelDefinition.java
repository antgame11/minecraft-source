/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.MultipartBlockStateModel;
import net.minecraft.client.render.model.json.BlockPropertiesPredicate;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.state.StateManager;
import net.minecraft.util.dynamic.Codecs;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record BlockModelDefinition(Optional<Variants> simpleModels, Optional<Multipart> multipartModel) {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<BlockModelDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(Variants.CODEC.optionalFieldOf("variants").forGetter(BlockModelDefinition::simpleModels), Multipart.CODEC.optionalFieldOf("multipart").forGetter(BlockModelDefinition::multipartModel)).apply((Applicative<BlockModelDefinition, ?>)instance, BlockModelDefinition::new)).validate(modelDefinition -> {
        if (modelDefinition.simpleModels().isEmpty() && modelDefinition.multipartModel().isEmpty()) {
            return DataResult.error(() -> "Neither 'variants' nor 'multipart' found");
        }
        return DataResult.success(modelDefinition);
    });

    public Map<BlockState, BlockStateModel.UnbakedGrouped> load(StateManager<Block, BlockState> stateManager, Supplier<String> idSupplier) {
        IdentityHashMap<BlockState, BlockStateModel.UnbakedGrouped> map = new IdentityHashMap<BlockState, BlockStateModel.UnbakedGrouped>();
        this.simpleModels.ifPresent(simpleModels -> simpleModels.load(stateManager, idSupplier, (state, model) -> {
            BlockStateModel.UnbakedGrouped lv = map.put((BlockState)state, (BlockStateModel.UnbakedGrouped)model);
            if (lv != null) {
                throw new IllegalArgumentException("Overlapping definition on state: " + String.valueOf(state));
            }
        }));
        this.multipartModel.ifPresent(multipartModel -> {
            ImmutableList list = stateManager.getStates();
            MultipartBlockStateModel.MultipartUnbaked lv = multipartModel.toModel(stateManager);
            for (BlockState lv2 : list) {
                map.putIfAbsent(lv2, lv);
            }
        });
        return map;
    }

    @Environment(value=EnvType.CLIENT)
    public record Multipart(List<MultipartModelComponent> selectors) {
        public static final Codec<Multipart> CODEC = Codecs.nonEmptyList(MultipartModelComponent.CODEC.listOf()).xmap(Multipart::new, Multipart::selectors);

        public MultipartBlockStateModel.MultipartUnbaked toModel(StateManager<Block, BlockState> stateManager) {
            ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize(this.selectors.size());
            for (MultipartModelComponent lv : this.selectors) {
                builder.add(new MultipartBlockStateModel.Selector<BlockStateModel.Unbaked>(lv.init(stateManager), lv.model()));
            }
            return new MultipartBlockStateModel.MultipartUnbaked((List<MultipartBlockStateModel.Selector<BlockStateModel.Unbaked>>)((Object)builder.build()));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Variants(Map<String, BlockStateModel.Unbaked> models) {
        public static final Codec<Variants> CODEC = Codecs.nonEmptyMap(Codec.unboundedMap(Codec.STRING, BlockStateModel.Unbaked.CODEC)).xmap(Variants::new, Variants::models);

        public void load(StateManager<Block, BlockState> stateManager, Supplier<String> idSupplier, BiConsumer<BlockState, BlockStateModel.UnbakedGrouped> callback) {
            this.models.forEach((predicate, model) -> {
                try {
                    Predicate predicate2 = BlockPropertiesPredicate.parse(stateManager, predicate);
                    BlockStateModel.UnbakedGrouped lv = model.cached();
                    for (BlockState lv2 : stateManager.getStates()) {
                        if (!predicate2.test(lv2)) continue;
                        callback.accept(lv2, lv);
                    }
                } catch (Exception exception) {
                    LOGGER.warn("Exception loading blockstate definition: '{}' for variant: '{}': {}", idSupplier.get(), predicate, exception.getMessage());
                }
            });
        }
    }
}

