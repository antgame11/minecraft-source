/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component;

import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import org.jetbrains.annotations.Nullable;

public interface ComponentsAccess {
    @Nullable
    public <T> T get(ComponentType<? extends T> var1);

    default public <T> T getOrDefault(ComponentType<? extends T> type, T fallback) {
        T object2 = this.get(type);
        return object2 != null ? object2 : fallback;
    }

    @Nullable
    default public <T> Component<T> getTyped(ComponentType<T> type) {
        T object = this.get(type);
        return object != null ? new Component<T>(type, object) : null;
    }
}

