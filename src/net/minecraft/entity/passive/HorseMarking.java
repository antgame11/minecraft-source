/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import java.util.function.IntFunction;
import net.minecraft.util.function.ValueLists;

public enum HorseMarking {
    NONE(0),
    WHITE(1),
    WHITE_FIELD(2),
    WHITE_DOTS(3),
    BLACK_DOTS(4);

    private static final IntFunction<HorseMarking> INDEX_MAPPER;
    private final int index;

    private HorseMarking(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public static HorseMarking byIndex(int index) {
        return INDEX_MAPPER.apply(index);
    }

    static {
        INDEX_MAPPER = ValueLists.createIndexToValueFunction(HorseMarking::getIndex, HorseMarking.values(), ValueLists.OutOfBoundsHandling.WRAP);
    }
}

