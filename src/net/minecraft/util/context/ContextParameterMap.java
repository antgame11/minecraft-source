/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.context;

import com.google.common.collect.Sets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.context.ContextType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class ContextParameterMap {
    private final Map<ContextParameter<?>, Object> map;

    ContextParameterMap(Map<ContextParameter<?>, Object> map) {
        this.map = map;
    }

    public boolean contains(ContextParameter<?> parameter) {
        return this.map.containsKey(parameter);
    }

    public <T> T getOrThrow(ContextParameter<T> parameter) {
        Object object = this.map.get(parameter);
        if (object == null) {
            throw new NoSuchElementException(parameter.getId().toString());
        }
        return (T)object;
    }

    @Nullable
    public <T> T getNullable(ContextParameter<T> parameter) {
        return (T)this.map.get(parameter);
    }

    @Nullable
    @Contract(value="_,!null->!null; _,_->_")
    public <T> T getOrDefault(ContextParameter<T> parameter, @Nullable T defaultValue) {
        return (T)this.map.getOrDefault(parameter, defaultValue);
    }

    public static class Builder {
        private final Map<ContextParameter<?>, Object> map = new IdentityHashMap();

        public <T> Builder add(ContextParameter<T> parameter, T value) {
            this.map.put(parameter, value);
            return this;
        }

        public <T> Builder addNullable(ContextParameter<T> parameter, @Nullable T value) {
            if (value == null) {
                this.map.remove(parameter);
            } else {
                this.map.put(parameter, value);
            }
            return this;
        }

        public <T> T getOrThrow(ContextParameter<T> parameter) {
            Object object = this.map.get(parameter);
            if (object == null) {
                throw new NoSuchElementException(parameter.getId().toString());
            }
            return (T)object;
        }

        @Nullable
        public <T> T getNullable(ContextParameter<T> parameter) {
            return (T)this.map.get(parameter);
        }

        public ContextParameterMap build(ContextType type) {
            Sets.SetView<ContextParameter<?>> set = Sets.difference(this.map.keySet(), type.getAllowed());
            if (!set.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + String.valueOf(set));
            }
            Sets.SetView<ContextParameter<?>> set2 = Sets.difference(type.getRequired(), this.map.keySet());
            if (!set2.isEmpty()) {
                throw new IllegalArgumentException("Missing required parameters: " + String.valueOf(set2));
            }
            return new ContextParameterMap(this.map);
        }
    }
}

