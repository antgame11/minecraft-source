/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class ChunkUpdateState
extends PersistentState {
    private final LongSet all;
    private final LongSet remaining;
    private static final Codec<LongSet> LONG_SET_CODEC = Codec.LONG_STREAM.xmap(LongOpenHashSet::toSet, LongCollection::longStream);
    public static final Codec<ChunkUpdateState> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)LONG_SET_CODEC.fieldOf("All")).forGetter(state -> state.all), ((MapCodec)LONG_SET_CODEC.fieldOf("Remaining")).forGetter(state -> state.remaining)).apply((Applicative<ChunkUpdateState, ?>)instance, ChunkUpdateState::new));

    public static PersistentStateType<ChunkUpdateState> createStateType(String id) {
        return new PersistentStateType<ChunkUpdateState>(id, ChunkUpdateState::new, CODEC, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES);
    }

    private ChunkUpdateState(LongSet all, LongSet remaining) {
        this.all = all;
        this.remaining = remaining;
    }

    public ChunkUpdateState() {
        this(new LongOpenHashSet(), new LongOpenHashSet());
    }

    public void add(long pos) {
        this.all.add(pos);
        this.remaining.add(pos);
        this.markDirty();
    }

    public boolean contains(long pos) {
        return this.all.contains(pos);
    }

    public boolean isRemaining(long pos) {
        return this.remaining.contains(pos);
    }

    public void markResolved(long pos) {
        if (this.remaining.remove(pos)) {
            this.markDirty();
        }
    }

    public LongSet getAll() {
        return this.all;
    }
}

