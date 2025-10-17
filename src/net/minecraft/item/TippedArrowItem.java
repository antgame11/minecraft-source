/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;

public class TippedArrowItem
extends ArrowItem {
    public TippedArrowItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack lv = super.getDefaultStack();
        lv.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.POISON));
        return lv;
    }

    @Override
    public Text getName(ItemStack stack) {
        PotionContentsComponent lv = stack.get(DataComponentTypes.POTION_CONTENTS);
        return lv != null ? lv.getName(this.translationKey + ".effect.") : super.getName(stack);
    }
}

