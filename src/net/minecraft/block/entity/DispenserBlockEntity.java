/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class DispenserBlockEntity
extends LootableContainerBlockEntity {
    public static final int INVENTORY_SIZE = 9;
    private static final Text CONTAINER_NAME_TEXT = Text.translatable("container.dispenser");
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    protected DispenserBlockEntity(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    public DispenserBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntityType.DISPENSER, pos, state);
    }

    @Override
    public int size() {
        return 9;
    }

    public int chooseNonEmptySlot(Random random) {
        this.generateLoot(null);
        int i = -1;
        int j = 1;
        for (int k = 0; k < this.inventory.size(); ++k) {
            if (this.inventory.get(k).isEmpty() || random.nextInt(j++) != 0) continue;
            i = k;
        }
        return i;
    }

    public ItemStack addToFirstFreeSlot(ItemStack stack) {
        int i = this.getMaxCount(stack);
        for (int j = 0; j < this.inventory.size(); ++j) {
            ItemStack lv = this.inventory.get(j);
            if (!lv.isEmpty() && !ItemStack.areItemsAndComponentsEqual(stack, lv)) continue;
            int k = Math.min(stack.getCount(), i - lv.getCount());
            if (k > 0) {
                if (lv.isEmpty()) {
                    this.setStack(j, stack.split(k));
                } else {
                    stack.decrement(k);
                    lv.increment(k);
                }
            }
            if (stack.isEmpty()) break;
        }
        return stack;
    }

    @Override
    protected Text getContainerName() {
        return CONTAINER_NAME_TEXT;
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.readLootTable(view)) {
            Inventories.readData(view, this.inventory);
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (!this.writeLootTable(view)) {
            Inventories.writeData(view, this.inventory);
        }
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new Generic3x3ContainerScreenHandler(syncId, playerInventory, this);
    }
}

