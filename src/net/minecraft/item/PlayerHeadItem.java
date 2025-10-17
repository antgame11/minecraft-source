/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

public class PlayerHeadItem
extends VerticallyAttachableBlockItem {
    public PlayerHeadItem(Block block, Block wallBlock, Item.Settings settings) {
        super(block, wallBlock, Direction.DOWN, settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        ProfileComponent lv = stack.get(DataComponentTypes.PROFILE);
        if (lv != null && lv.getName().isPresent()) {
            return Text.translatable(this.translationKey + ".named", lv.getName().get());
        }
        return super.getName(stack);
    }
}

