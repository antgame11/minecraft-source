/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.inventory;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.dynamic.Codecs;

public record StackWithSlot(int slot, ItemStack stack) {
    public static final Codec<StackWithSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.UNSIGNED_BYTE.fieldOf("Slot")).orElse(0).forGetter(StackWithSlot::slot), ItemStack.MAP_CODEC.forGetter(StackWithSlot::stack)).apply((Applicative<StackWithSlot, ?>)instance, StackWithSlot::new));

    public boolean isValidSlot(int slots) {
        return this.slot >= 0 && this.slot < slots;
    }
}

