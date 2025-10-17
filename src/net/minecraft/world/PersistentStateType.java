/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.world.PersistentState;

public record PersistentStateType<T extends PersistentState>(String id, Function<PersistentState.Context, T> constructor, Function<PersistentState.Context, Codec<T>> codec, DataFixTypes dataFixType) {
    public PersistentStateType(String id, Supplier<T> constructor, Codec<T> codec, DataFixTypes dataFixType) {
        this(id, (PersistentState.Context context) -> (PersistentState)constructor.get(), (PersistentState.Context context) -> codec, dataFixType);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PersistentStateType)) return false;
        PersistentStateType lv = (PersistentStateType)o;
        if (!this.id.equals(lv.id)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "SavedDataType[" + this.id + "]";
    }
}

