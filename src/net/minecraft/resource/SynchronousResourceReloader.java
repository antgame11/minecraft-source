/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Unit;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

public interface SynchronousResourceReloader
extends ResourceReloader {
    @Override
    default public CompletableFuture<Void> reload(ResourceReloader.Store store, Executor prepareExecutor, ResourceReloader.Synchronizer reloadSynchronizer, Executor applyExecutor) {
        ResourceManager lv = store.getResourceManager();
        return reloadSynchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> {
            Profiler lv = Profilers.get();
            lv.push("listener");
            this.reload(lv);
            lv.pop();
        }, applyExecutor);
    }

    public void reload(ResourceManager var1);
}

