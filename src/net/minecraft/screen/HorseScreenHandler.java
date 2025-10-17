/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.ArmorSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class HorseScreenHandler
extends ScreenHandler {
    private static final Identifier EMPTY_SADDLE_SLOT_TEXTURE = Identifier.ofVanilla("container/slot/saddle");
    private static final Identifier EMPTY_LLAMA_ARMOR_SLOT_TEXTURE = Identifier.ofVanilla("container/slot/llama_armor");
    private static final Identifier EMPTY_HORSE_ARMOR_SLOT_TEXTURE = Identifier.ofVanilla("container/slot/horse_armor");
    private final Inventory inventory;
    private final AbstractHorseEntity entity;
    private static final int field_55978 = 0;
    private static final int field_48835 = 1;
    private static final int field_48836 = 2;

    public HorseScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, final AbstractHorseEntity entity, int slotColumnCount) {
        super(null, syncId);
        this.inventory = inventory;
        this.entity = entity;
        inventory.onOpen(playerInventory.player);
        Inventory lv = entity.createEquipmentInventory(EquipmentSlot.SADDLE);
        this.addSlot(new ArmorSlot(this, lv, entity, EquipmentSlot.SADDLE, 0, 8, 18, EMPTY_SADDLE_SLOT_TEXTURE){

            @Override
            public boolean isEnabled() {
                return entity.canUseSlot(EquipmentSlot.SADDLE) && entity.getType().isIn(EntityTypeTags.CAN_EQUIP_SADDLE);
            }
        });
        final boolean bl = entity instanceof LlamaEntity;
        Identifier lv2 = bl ? EMPTY_LLAMA_ARMOR_SLOT_TEXTURE : EMPTY_HORSE_ARMOR_SLOT_TEXTURE;
        Inventory lv3 = entity.createEquipmentInventory(EquipmentSlot.BODY);
        this.addSlot(new ArmorSlot(this, lv3, entity, EquipmentSlot.BODY, 0, 8, 36, lv2){

            @Override
            public boolean isEnabled() {
                return entity.canUseSlot(EquipmentSlot.BODY) && (entity.getType().isIn(EntityTypeTags.CAN_WEAR_HORSE_ARMOR) || bl);
            }
        });
        if (slotColumnCount > 0) {
            for (int k = 0; k < 3; ++k) {
                for (int l = 0; l < slotColumnCount; ++l) {
                    this.addSlot(new Slot(inventory, l + k * slotColumnCount, 80 + l * 18, 18 + k * 18));
                }
            }
        }
        this.addPlayerSlots(playerInventory, 8, 84);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return !this.entity.areInventoriesDifferent(this.inventory) && this.inventory.canPlayerUse(player) && this.entity.isAlive() && player.canInteractWithEntity(this.entity, 4.0);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            int j = 2 + this.inventory.size();
            if (slot < j) {
                if (!this.insertItem(lv3, j, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).canInsert(lv3) && !this.getSlot(1).hasStack()) {
                if (!this.insertItem(lv3, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).canInsert(lv3) && !this.getSlot(0).hasStack()) {
                if (!this.insertItem(lv3, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.inventory.size() == 0 || !this.insertItem(lv3, 2, j, false)) {
                int k;
                int l = k = j + 27;
                int m = l + 9;
                if (slot >= l && slot < m ? !this.insertItem(lv3, j, k, false) : (slot >= j && slot < k ? !this.insertItem(lv3, l, m, false) : !this.insertItem(lv3, l, k, false))) {
                    return ItemStack.EMPTY;
                }
                return ItemStack.EMPTY;
            }
            if (lv3.isEmpty()) {
                lv2.setStack(ItemStack.EMPTY);
            } else {
                lv2.markDirty();
            }
        }
        return lv;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }
}

