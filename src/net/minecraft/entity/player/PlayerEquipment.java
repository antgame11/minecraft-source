/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.player;

import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class PlayerEquipment
extends EntityEquipment {
    private final PlayerEntity player;

    public PlayerEquipment(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public ItemStack put(EquipmentSlot slot, ItemStack arg2) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.player.getInventory().setSelectedStack(arg2);
        }
        return super.put(slot, arg2);
    }

    @Override
    public ItemStack get(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.player.getInventory().getSelectedStack();
        }
        return super.get(slot);
    }

    @Override
    public boolean isEmpty() {
        return this.player.getInventory().getSelectedStack().isEmpty() && super.isEmpty();
    }
}

