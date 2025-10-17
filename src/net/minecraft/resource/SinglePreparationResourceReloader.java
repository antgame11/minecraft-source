/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

public abstract class SinglePreparationResourceReloader<T>
implements ResourceReloader {
    @Override
    public final CompletableFuture<Void> reload(ResourceReloader.Store store, Executor prepareExecutor, ResourceReloader.Synchronizer reloadSynchronizer, Executor applyExecutor) {
        ResourceManager lv = store.getResourceManager();
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> this.prepare(lv, Profilers.get()), prepareExecutor).thenCompose(reloadSynchronizer::whenPrepared)).thenAcceptAsync(prepared -> this.apply(prepared, lv, Profilers.get()), applyExecutor);
    }

    protected abstract T prepare(ResourceManager var1, Profiler var2);

    protected abstract void apply(T var1, ResourceManager var2, Profiler var3);
}

