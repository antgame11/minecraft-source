/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class IdCountsState
extends PersistentState {
    private static final int field_56476 = -1;
    public static final Codec<IdCountsState> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.INT.optionalFieldOf("map", -1).forGetter(state -> state.map)).apply((Applicative<IdCountsState, ?>)instance, IdCountsState::new));
    public static final PersistentStateType<IdCountsState> STATE_TYPE = new PersistentStateType<IdCountsState>("idcounts", IdCountsState::new, CODEC, DataFixTypes.SAVED_DATA_MAP_INDEX);
    private int map;

    public IdCountsState() {
        this(-1);
    }

    public IdCountsState(int map) {
        this.map = map;
    }

    public MapIdComponent createNextMapId() {
        MapIdComponent lv = new MapIdComponent(++this.map);
        this.markDirty();
        return lv;
    }
}

