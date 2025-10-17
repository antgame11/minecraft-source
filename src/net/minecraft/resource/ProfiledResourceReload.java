/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SimpleResourceReload;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import org.slf4j.Logger;

public class ProfiledResourceReload
extends SimpleResourceReload<Summary> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Stopwatch reloadTimer = Stopwatch.createUnstarted();

    public static ResourceReload start(ResourceManager manager, List<ResourceReloader> reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage) {
        ProfiledResourceReload lv = new ProfiledResourceReload(reloaders);
        lv.start(prepareExecutor, applyExecutor, manager, reloaders, (store, reloadSynchronizer, reloader, prepare, apply) -> {
            AtomicLong atomicLong = new AtomicLong();
            AtomicLong atomicLong2 = new AtomicLong();
            AtomicLong atomicLong3 = new AtomicLong();
            AtomicLong atomicLong4 = new AtomicLong();
            CompletableFuture<Void> completableFuture = reloader.reload(store, ProfiledResourceReload.getProfiledExecutor(prepare, atomicLong, atomicLong2, reloader.getName()), reloadSynchronizer, ProfiledResourceReload.getProfiledExecutor(apply, atomicLong3, atomicLong4, reloader.getName()));
            return completableFuture.thenApplyAsync(v -> {
                LOGGER.debug("Finished reloading {}", (Object)reloader.getName());
                return new Summary(reloader.getName(), atomicLong, atomicLong2, atomicLong3, atomicLong4);
            }, applyExecutor);
        }, initialStage);
        return lv;
    }

    private ProfiledResourceReload(List<ResourceReloader> waitingReloaders) {
        super(waitingReloaders);
        this.reloadTimer.start();
    }

    @Override
    protected CompletableFuture<List<Summary>> startAsync(Executor prepareExecutor, Executor applyExecutor, ResourceManager manager, List<ResourceReloader> reloaders, SimpleResourceReload.Factory<Summary> factory, CompletableFuture<?> initialStage) {
        return super.startAsync(prepareExecutor, applyExecutor, manager, reloaders, factory, initialStage).thenApplyAsync(this::finish, applyExecutor);
    }

    private static Executor getProfiledExecutor(Executor executor, AtomicLong output, AtomicLong counter, String name) {
        return runnable -> executor.execute(() -> {
            Profiler lv = Profilers.get();
            lv.push(name);
            long l = Util.getMeasuringTimeNano();
            runnable.run();
            output.addAndGet(Util.getMeasuringTimeNano() - l);
            counter.incrementAndGet();
            lv.pop();
        });
    }

    private List<Summary> finish(List<Summary> summaries) {
        this.reloadTimer.stop();
        long l = 0L;
        LOGGER.info("Resource reload finished after {} ms", (Object)this.reloadTimer.elapsed(TimeUnit.MILLISECONDS));
        for (Summary lv : summaries) {
            long m = TimeUnit.NANOSECONDS.toMillis(lv.prepareTimeMs.get());
            long n = lv.preparationCount.get();
            long o = TimeUnit.NANOSECONDS.toMillis(lv.applyTimeMs.get());
            long p = lv.reloadCount.get();
            long q = m + o;
            long r = n + p;
            String string = lv.name;
            LOGGER.info("{} took approximately {} tasks/{} ms ({} tasks/{} ms preparing, {} tasks/{} ms applying)", string, r, q, n, m, p, o);
            l += o;
        }
        LOGGER.info("Total blocking time: {} ms", (Object)l);
        return summaries;
    }

    public record Summary(String name, AtomicLong prepareTimeMs, AtomicLong preparationCount, AtomicLong applyTimeMs, AtomicLong reloadCount) {
    }
}

