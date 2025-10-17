/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Nullable;

public class StateManager<O, S extends State<O, S>> {
    static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private final O owner;
    private final ImmutableSortedMap<String, Property<?>> properties;
    private final ImmutableList<S> states;

    protected StateManager(Function<O, S> defaultStateGetter, O owner, Factory<O, S> factory, Map<String, Property<?>> propertiesMap) {
        this.owner = owner;
        this.properties = ImmutableSortedMap.copyOf(propertiesMap);
        Supplier<State> supplier = () -> (State)defaultStateGetter.apply(owner);
        MapCodec<State> mapCodec = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));
        for (Map.Entry entry : this.properties.entrySet()) {
            mapCodec = StateManager.addFieldToMapCodec(mapCodec, supplier, (String)entry.getKey(), (Property)entry.getValue());
        }
        MapCodec<State> mapCodec2 = mapCodec;
        LinkedHashMap map2 = Maps.newLinkedHashMap();
        ArrayList<State> list = Lists.newArrayList();
        Stream<List<List<Object>>> stream = Stream.of(Collections.emptyList());
        for (Property lv : this.properties.values()) {
            stream = stream.flatMap(entries -> lv.getValues().stream().map(value -> {
                ArrayList<Pair<Property, Comparable>> list2 = Lists.newArrayList(entries);
                list2.add(Pair.of(lv, value));
                return list2;
            }));
        }
        stream.forEach(entries -> {
            Reference2ObjectArrayMap reference2ObjectArrayMap = new Reference2ObjectArrayMap(entries.size());
            for (Pair pair : entries) {
                reference2ObjectArrayMap.put((Property)pair.getFirst(), (Comparable)pair.getSecond());
            }
            State lv = (State)factory.create(owner, reference2ObjectArrayMap, mapCodec2);
            map2.put(reference2ObjectArrayMap, lv);
            list.add(lv);
        });
        for (State lv2 : list) {
            lv2.createWithMap(map2);
        }
        this.states = ImmutableList.copyOf(list);
    }

    private static <S extends State<?, S>, T extends Comparable<T>> MapCodec<S> addFieldToMapCodec(MapCodec<S> mapCodec, Supplier<S> defaultStateGetter, String key, Property<T> property) {
        return Codec.mapPair(mapCodec, ((MapCodec)property.getValueCodec().fieldOf(key)).orElseGet(value -> {}, () -> property.createValue((State)defaultStateGetter.get()))).xmap(pair -> (State)((State)pair.getFirst()).with(property, ((Property.Value)pair.getSecond()).value()), state -> Pair.of(state, property.createValue((State<?, ?>)state)));
    }

    public ImmutableList<S> getStates() {
        return this.states;
    }

    public S getDefaultState() {
        return (S)((State)this.states.get(0));
    }

    public O getOwner() {
        return this.owner;
    }

    public Collection<Property<?>> getProperties() {
        return this.properties.values();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("block", this.owner).add("properties", this.properties.values().stream().map(Property::getName).collect(Collectors.toList())).toString();
    }

    @Nullable
    public Property<?> getProperty(String name) {
        return this.properties.get(name);
    }

    public static interface Factory<O, S> {
        public S create(O var1, Reference2ObjectArrayMap<Property<?>, Comparable<?>> var2, MapCodec<S> var3);
    }

    public static class Builder<O, S extends State<O, S>> {
        private final O owner;
        private final Map<String, Property<?>> namedProperties = Maps.newHashMap();

        public Builder(O owner) {
            this.owner = owner;
        }

        public Builder<O, S> add(Property<?> ... properties) {
            for (Property<?> lv : properties) {
                this.validate(lv);
                this.namedProperties.put(lv.getName(), lv);
            }
            return this;
        }

        private <T extends Comparable<T>> void validate(Property<T> property) {
            String string = property.getName();
            if (!VALID_NAME_PATTERN.matcher(string).matches()) {
                throw new IllegalArgumentException(String.valueOf(this.owner) + " has invalidly named property: " + string);
            }
            List<T> collection = property.getValues();
            if (collection.size() <= 1) {
                throw new IllegalArgumentException(String.valueOf(this.owner) + " attempted use property " + string + " with <= 1 possible values");
            }
            for (Comparable comparable : collection) {
                String string2 = property.name(comparable);
                if (VALID_NAME_PATTERN.matcher(string2).matches()) continue;
                throw new IllegalArgumentException(String.valueOf(this.owner) + " has property: " + string + " with invalidly named value: " + string2);
            }
            if (this.namedProperties.containsKey(string)) {
                throw new IllegalArgumentException(String.valueOf(this.owner) + " has duplicate property: " + string);
            }
        }

        public StateManager<O, S> build(Function<O, S> defaultStateGetter, Factory<O, S> factory) {
            return new StateManager<O, S>(defaultStateGetter, this.owner, factory, this.namedProperties);
        }
    }
}

