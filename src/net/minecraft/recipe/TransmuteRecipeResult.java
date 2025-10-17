/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Codecs;

public record TransmuteRecipeResult(RegistryEntry<Item> itemEntry, int count, ComponentChanges components) {
    private static final Codec<TransmuteRecipeResult> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Item.ENTRY_CODEC.fieldOf("id")).forGetter(TransmuteRecipeResult::itemEntry), Codecs.rangedInt(1, 99).optionalFieldOf("count", 1).forGetter(TransmuteRecipeResult::count), ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(TransmuteRecipeResult::components)).apply((Applicative<TransmuteRecipeResult, ?>)instance, TransmuteRecipeResult::new));
    public static final Codec<TransmuteRecipeResult> CODEC = Codec.withAlternative(BASE_CODEC, Item.ENTRY_CODEC, itemEntry -> new TransmuteRecipeResult((Item)itemEntry.value())).validate(TransmuteRecipeResult::validate);
    public static final PacketCodec<RegistryByteBuf, TransmuteRecipeResult> PACKET_CODEC = PacketCodec.tuple(Item.ENTRY_PACKET_CODEC, TransmuteRecipeResult::itemEntry, PacketCodecs.VAR_INT, TransmuteRecipeResult::count, ComponentChanges.PACKET_CODEC, TransmuteRecipeResult::components, TransmuteRecipeResult::new);

    public TransmuteRecipeResult(Item item) {
        this(item.getRegistryEntry(), 1, ComponentChanges.EMPTY);
    }

    private static DataResult<TransmuteRecipeResult> validate(TransmuteRecipeResult result) {
        return ItemStack.validate(new ItemStack(result.itemEntry, result.count, result.components)).map(stack -> result);
    }

    public ItemStack apply(ItemStack stack) {
        ItemStack lv = stack.copyComponentsToNewStack(this.itemEntry.value(), this.count);
        lv.applyUnvalidatedChanges(this.components);
        return lv;
    }

    public boolean isEqualToResult(ItemStack stack) {
        ItemStack lv = this.apply(stack);
        return lv.getCount() == 1 && ItemStack.areItemsAndComponentsEqual(stack, lv);
    }

    public SlotDisplay createSlotDisplay() {
        return new SlotDisplay.StackSlotDisplay(new ItemStack(this.itemEntry, this.count, this.components));
    }
}

