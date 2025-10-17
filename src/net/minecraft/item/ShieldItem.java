/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public class ShieldItem
extends Item {
    public ShieldItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public Text getName(ItemStack stack) {
        DyeColor lv = stack.get(DataComponentTypes.BASE_COLOR);
        if (lv != null) {
            return Text.translatable(this.translationKey + "." + lv.getId());
        }
        return super.getName(stack);
    }
}

