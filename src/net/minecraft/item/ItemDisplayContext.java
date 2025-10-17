/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum ItemDisplayContext implements StringIdentifiable
{
    NONE(0, "none"),
    THIRD_PERSON_LEFT_HAND(1, "thirdperson_lefthand"),
    THIRD_PERSON_RIGHT_HAND(2, "thirdperson_righthand"),
    FIRST_PERSON_LEFT_HAND(3, "firstperson_lefthand"),
    FIRST_PERSON_RIGHT_HAND(4, "firstperson_righthand"),
    HEAD(5, "head"),
    GUI(6, "gui"),
    GROUND(7, "ground"),
    FIXED(8, "fixed"),
    ON_SHELF(9, "on_shelf");

    public static final Codec<ItemDisplayContext> CODEC;
    public static final IntFunction<ItemDisplayContext> FROM_INDEX;
    private final byte index;
    private final String name;

    private ItemDisplayContext(int index, String name) {
        this.name = name;
        this.index = (byte)index;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public byte getIndex() {
        return this.index;
    }

    public boolean isFirstPerson() {
        return this == FIRST_PERSON_LEFT_HAND || this == FIRST_PERSON_RIGHT_HAND;
    }

    public boolean isLeftHand() {
        return this == FIRST_PERSON_LEFT_HAND || this == THIRD_PERSON_LEFT_HAND;
    }

    static {
        CODEC = StringIdentifiable.createCodec(ItemDisplayContext::values);
        FROM_INDEX = ValueLists.createIndexToValueFunction(ItemDisplayContext::getIndex, ItemDisplayContext.values(), ValueLists.OutOfBoundsHandling.ZERO);
    }
}

