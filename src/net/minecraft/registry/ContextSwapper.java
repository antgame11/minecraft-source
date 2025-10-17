/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.registry.RegistryWrapper;

public interface ContextSwapper {
    public <T> DataResult<T> swapContext(Codec<T> var1, T var2, RegistryWrapper.WrapperLookup var3);
}

