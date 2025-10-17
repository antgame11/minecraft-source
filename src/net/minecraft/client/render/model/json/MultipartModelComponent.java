/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.json.MultipartModelCondition;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;

@Environment(value=EnvType.CLIENT)
public record MultipartModelComponent(Optional<MultipartModelCondition> selector, BlockStateModel.Unbaked model) {
    public static final Codec<MultipartModelComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(MultipartModelCondition.CODEC.optionalFieldOf("when").forGetter(MultipartModelComponent::selector), ((MapCodec)BlockStateModel.Unbaked.CODEC.fieldOf("apply")).forGetter(MultipartModelComponent::model)).apply((Applicative<MultipartModelComponent, ?>)instance, MultipartModelComponent::new));

    public <O, S extends State<O, S>> Predicate<S> init(StateManager<O, S> value) {
        return this.selector.map(arg2 -> arg2.instantiate(value)).orElse(arg -> true);
    }
}

