/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import java.util.Objects;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public abstract class PersistentState {
    private boolean dirty;

    public void markDirty() {
        this.setDirty(true);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public record Context(@Nullable ServerWorld world, long worldSeed) {
        public Context(ServerWorld world) {
            this(world, world.getSeed());
        }

        public ServerWorld getWorldOrThrow() {
            return Objects.requireNonNull(this.world);
        }

        @Nullable
        public ServerWorld world() {
            return this.world;
        }
    }
}

