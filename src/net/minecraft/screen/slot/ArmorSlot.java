/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen.slot;

import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

class ArmorSlot
extends Slot {
    private final LivingEntity entity;
    private final EquipmentSlot equipmentSlot;
    @Nullable
    private final Identifier backgroundSprite;

    public ArmorSlot(Inventory inventory, LivingEntity entity, EquipmentSlot equipmentSlot, int index, int x, int y, @Nullable Identifier backgroundSprite) {
        super(inventory, index, x, y);
        this.entity = entity;
        this.equipmentSlot = equipmentSlot;
        this.backgroundSprite = backgroundSprite;
    }

    @Override
    public void setStack(ItemStack stack, ItemStack previousStack) {
        this.entity.onEquipStack(this.equipmentSlot, previousStack, stack);
        super.setStack(stack, previousStack);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return this.entity.canEquip(stack, this.equipmentSlot);
    }

    @Override
    public boolean isEnabled() {
        return this.entity.canUseSlot(this.equipmentSlot);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        ItemStack lv = this.getStack();
        if (!lv.isEmpty() && !playerEntity.isCreative() && EnchantmentHelper.hasAnyEnchantmentsWith(lv, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        return super.canTakeItems(playerEntity);
    }

    @Override
    @Nullable
    public Identifier getBackgroundSprite() {
        return this.backgroundSprite;
    }
}

