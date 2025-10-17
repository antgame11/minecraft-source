/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.MultipartModelCondition;
import net.minecraft.client.render.model.json.SimpleMultipartModelSelector;
import net.minecraft.state.property.Property;

@Environment(value=EnvType.CLIENT)
public class MultipartModelConditionBuilder {
    private final ImmutableMap.Builder<String, SimpleMultipartModelSelector.Terms> values = ImmutableMap.builder();

    private <T extends Comparable<T>> void putTerms(Property<T> property, SimpleMultipartModelSelector.Terms terms) {
        this.values.put(property.getName(), terms);
    }

    public final <T extends Comparable<T>> MultipartModelConditionBuilder put(Property<T> property, T value) {
        this.putTerms(property, new SimpleMultipartModelSelector.Terms(List.of(new SimpleMultipartModelSelector.Term(property.name(value), false))));
        return this;
    }

    @SafeVarargs
    public final <T extends Comparable<T>> MultipartModelConditionBuilder put(Property<T> property, T value, T ... values) {
        List<SimpleMultipartModelSelector.Term> list = Stream.concat(Stream.of(value), Stream.of(values)).map(property::name).sorted().distinct().map(valuex -> new SimpleMultipartModelSelector.Term((String)valuex, false)).toList();
        this.putTerms(property, new SimpleMultipartModelSelector.Terms(list));
        return this;
    }

    public final <T extends Comparable<T>> MultipartModelConditionBuilder replace(Property<T> property, T value) {
        this.putTerms(property, new SimpleMultipartModelSelector.Terms(List.of(new SimpleMultipartModelSelector.Term(property.name(value), true))));
        return this;
    }

    public MultipartModelCondition build() {
        return new SimpleMultipartModelSelector(this.values.buildOrThrow());
    }
}

