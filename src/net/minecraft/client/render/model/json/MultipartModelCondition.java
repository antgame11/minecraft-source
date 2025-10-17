/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.MultipartModelCombinedCondition;
import net.minecraft.client.render.model.json.SimpleMultipartModelSelector;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.util.StringIdentifiable;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface MultipartModelCondition {
    public static final Codec<MultipartModelCondition> CODEC = Codec.recursive("condition", group -> {
        Codec<MultipartModelCombinedCondition> codec2 = Codec.simpleMap(MultipartModelCombinedCondition.LogicalOperator.CODEC, group.listOf(), StringIdentifiable.toKeyable(MultipartModelCombinedCondition.LogicalOperator.values())).codec().comapFlatMap(map -> {
            if (map.size() != 1) {
                return DataResult.error(() -> "Invalid map size for combiner condition, expected exactly one element");
            }
            Map.Entry entry = map.entrySet().iterator().next();
            return DataResult.success(new MultipartModelCombinedCondition((MultipartModelCombinedCondition.LogicalOperator)entry.getKey(), (List)entry.getValue()));
        }, arg -> Map.of(arg.operation(), arg.terms()));
        return Codec.either(codec2, SimpleMultipartModelSelector.CODEC).flatComapMap(either -> (MultipartModelCondition)((Object)either.map(arg -> arg, arg -> arg)), arg -> {
            MultipartModelCondition multipartModelCondition = arg;
            Objects.requireNonNull(multipartModelCondition);
            MultipartModelCondition lv = multipartModelCondition;
            int i = 0;
            DataResult dataResult = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{MultipartModelCombinedCondition.class, SimpleMultipartModelSelector.class}, (Object)lv, i)) {
                case 0 -> {
                    MultipartModelCombinedCondition lv2 = (MultipartModelCombinedCondition)lv;
                    yield DataResult.success(Either.left(lv2));
                }
                case 1 -> {
                    SimpleMultipartModelSelector lv3 = (SimpleMultipartModelSelector)lv;
                    yield DataResult.success(Either.right(lv3));
                }
                default -> DataResult.error(() -> "Unrecognized condition");
            };
            return dataResult;
        });
    });

    public <O, S extends State<O, S>> Predicate<S> instantiate(StateManager<O, S> var1);
}

