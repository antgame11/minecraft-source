/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public sealed interface NbtPrimitive
extends NbtElement
permits AbstractNbtNumber, NbtString {
    @Override
    default public NbtElement copy() {
        return this;
    }
}

