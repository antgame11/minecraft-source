/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.TestInstance;

public class RuntimeTestInstances {
    private static final Set<RegistryEntry.Reference<TestInstance>> INSTANCES = Sets.newHashSet();

    public static Stream<RegistryEntry.Reference<TestInstance>> stream() {
        return INSTANCES.stream();
    }

    public static void add(RegistryEntry.Reference<TestInstance> instance) {
        INSTANCES.add(instance);
    }

    public static void clear() {
        INSTANCES.clear();
    }
}

