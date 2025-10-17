/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.inventory;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;

public class Inventories {
    public static final String ITEMS_NBT_KEY = "Items";

    public static ItemStack splitStack(List<ItemStack> stacks, int slot, int amount) {
        if (slot < 0 || slot >= stacks.size() || stacks.get(slot).isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot).split(amount);
    }

    public static ItemStack removeStack(List<ItemStack> stacks, int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            return ItemStack.EMPTY;
        }
        return stacks.set(slot, ItemStack.EMPTY);
    }

    public static void writeData(WriteView view, DefaultedList<ItemStack> stacks) {
        Inventories.writeData(view, stacks, true);
    }

    public static void writeData(WriteView view, DefaultedList<ItemStack> stacks, boolean setIfEmpty) {
        WriteView.ListAppender<StackWithSlot> lv = view.getListAppender(ITEMS_NBT_KEY, StackWithSlot.CODEC);
        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack lv2 = stacks.get(i);
            if (lv2.isEmpty()) continue;
            lv.add(new StackWithSlot(i, lv2));
        }
        if (lv.isEmpty() && !setIfEmpty) {
            view.remove(ITEMS_NBT_KEY);
        }
    }

    public static void readData(ReadView view, DefaultedList<ItemStack> stacks) {
        for (StackWithSlot lv : view.getTypedListView(ITEMS_NBT_KEY, StackWithSlot.CODEC)) {
            if (!lv.isValidSlot(stacks.size())) continue;
            stacks.set(lv.slot(), lv.stack());
        }
    }

    public static int remove(Inventory inventory, Predicate<ItemStack> shouldRemove, int maxCount, boolean dryRun) {
        int j = 0;
        for (int k = 0; k < inventory.size(); ++k) {
            ItemStack lv = inventory.getStack(k);
            int l = Inventories.remove(lv, shouldRemove, maxCount - j, dryRun);
            if (l > 0 && !dryRun && lv.isEmpty()) {
                inventory.setStack(k, ItemStack.EMPTY);
            }
            j += l;
        }
        return j;
    }

    public static int remove(ItemStack stack, Predicate<ItemStack> shouldRemove, int maxCount, boolean dryRun) {
        if (stack.isEmpty() || !shouldRemove.test(stack)) {
            return 0;
        }
        if (dryRun) {
            return stack.getCount();
        }
        int j = maxCount < 0 ? stack.getCount() : Math.min(maxCount, stack.getCount());
        stack.decrement(j);
        return j;
    }
}

