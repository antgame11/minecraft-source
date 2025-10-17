/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

public interface InventoryOwner {
    public static final String INVENTORY_KEY = "Inventory";

    public SimpleInventory getInventory();

    public static void pickUpItem(ServerWorld world, MobEntity entity, InventoryOwner inventoryOwner, ItemEntity item) {
        ItemStack lv = item.getStack();
        if (entity.canGather(world, lv)) {
            SimpleInventory lv2 = inventoryOwner.getInventory();
            boolean bl = lv2.canInsert(lv);
            if (!bl) {
                return;
            }
            entity.triggerItemPickedUpByEntityCriteria(item);
            int i = lv.getCount();
            ItemStack lv3 = lv2.addStack(lv);
            entity.sendPickup(item, i - lv3.getCount());
            if (lv3.isEmpty()) {
                item.discard();
            } else {
                lv.setCount(lv3.getCount());
            }
        }
    }

    default public void readInventory(ReadView view) {
        view.getOptionalTypedListView(INVENTORY_KEY, ItemStack.CODEC).ifPresent(list -> this.getInventory().readDataList((ReadView.TypedListReadView<ItemStack>)list));
    }

    default public void writeInventory(WriteView view) {
        this.getInventory().toDataList(view.getListAppender(INVENTORY_KEY, ItemStack.CODEC));
    }
}

