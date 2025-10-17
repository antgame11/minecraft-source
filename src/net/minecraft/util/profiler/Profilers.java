/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiler;

import com.mojang.jtracy.TracyClient;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.TracyProfiler;

public final class Profilers {
    private static final ThreadLocal<TracyProfiler> TRACY_PROFILER = ThreadLocal.withInitial(TracyProfiler::new);
    private static final ThreadLocal<Profiler> BUILTIN_PROFILER = new ThreadLocal();
    private static final AtomicInteger ACTIVE_BUILTIN_PROFILER_COUNT = new AtomicInteger();

    private Profilers() {
    }

    public static Scoped using(Profiler profiler) {
        Profilers.activate(profiler);
        return Profilers::deactivate;
    }

    private static void activate(Profiler profiler) {
        if (BUILTIN_PROFILER.get() != null) {
            throw new IllegalStateException("Profiler is already active");
        }
        Profiler lv = Profilers.union(profiler);
        BUILTIN_PROFILER.set(lv);
        ACTIVE_BUILTIN_PROFILER_COUNT.incrementAndGet();
        lv.startTick();
    }

    private static void deactivate() {
        Profiler lv = BUILTIN_PROFILER.get();
        if (lv == null) {
            throw new IllegalStateException("Profiler was not active");
        }
        BUILTIN_PROFILER.remove();
        ACTIVE_BUILTIN_PROFILER_COUNT.decrementAndGet();
        lv.endTick();
    }

    private static Profiler union(Profiler builtinProfiler) {
        return Profiler.union(Profilers.getDefault(), builtinProfiler);
    }

    public static Profiler get() {
        if (ACTIVE_BUILTIN_PROFILER_COUNT.get() == 0) {
            return Profilers.getDefault();
        }
        return Objects.requireNonNullElseGet(BUILTIN_PROFILER.get(), Profilers::getDefault);
    }

    private static Profiler getDefault() {
        if (TracyClient.isAvailable()) {
            return TRACY_PROFILER.get();
        }
        return DummyProfiler.INSTANCE;
    }

    public static interface Scoped
    extends AutoCloseable {
        @Override
        public void close();
    }
}

