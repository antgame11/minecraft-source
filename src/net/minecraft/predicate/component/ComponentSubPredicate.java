/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.component;

import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.predicate.component.ComponentPredicate;

public interface ComponentSubPredicate<T>
extends ComponentPredicate {
    @Override
    default public boolean test(ComponentsAccess components) {
        T object = components.get(this.getComponentType());
        return object != null && this.test(object);
    }

    public ComponentType<T> getComponentType();

    public boolean test(T var1);
}

