/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.debug;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.debug.DebugSubscriptionType;
import org.jetbrains.annotations.Nullable;

public interface DebugTrackable {
    public void registerTracking(ServerWorld var1, Tracker var2);

    public static interface DebugDataSupplier<T> {
        @Nullable
        public T get();
    }

    public static interface Tracker {
        public <T> void track(DebugSubscriptionType<T> var1, DebugDataSupplier<T> var2);
    }
}

