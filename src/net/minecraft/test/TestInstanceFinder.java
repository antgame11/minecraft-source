/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import java.util.stream.Stream;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.TestInstance;

@FunctionalInterface
public interface TestInstanceFinder {
    public Stream<RegistryEntry.Reference<TestInstance>> findTests();
}

