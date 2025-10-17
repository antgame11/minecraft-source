/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.data;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.state.property.Property;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public record PropertiesMap(List<Property.Value<?>> values) {
    public static final PropertiesMap EMPTY = new PropertiesMap(List.of());
    private static final Comparator<Property.Value<?>> COMPARATOR = Comparator.comparing(value -> value.property().getName());

    public PropertiesMap withValue(Property.Value<?> value) {
        return new PropertiesMap(Util.withAppended(this.values, value));
    }

    public PropertiesMap copyOf(PropertiesMap propertiesMap) {
        return new PropertiesMap((List<Property.Value<?>>)((Object)((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builder().addAll(this.values)).addAll(propertiesMap.values)).build()));
    }

    public static PropertiesMap withValues(Property.Value<?> ... values) {
        return new PropertiesMap(List.of(values));
    }

    public String asString() {
        return this.values.stream().sorted(COMPARATOR).map(Property.Value::toString).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return this.asString();
    }
}

