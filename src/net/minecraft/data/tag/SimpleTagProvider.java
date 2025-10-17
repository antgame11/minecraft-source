/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.tag;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.data.tag.TagProvider;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

public abstract class SimpleTagProvider<T>
extends TagProvider<T> {
    protected SimpleTagProvider(DataOutput arg, RegistryKey<? extends Registry<T>> arg2, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(arg, arg2, completableFuture);
    }

    protected ProvidedTagBuilder<RegistryKey<T>, T> builder(TagKey<T> tag) {
        TagBuilder lv = this.getTagBuilder(tag);
        return ProvidedTagBuilder.of(lv);
    }
}

