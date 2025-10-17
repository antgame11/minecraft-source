/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.Validate;

public class BannerItem
extends VerticallyAttachableBlockItem {
    public BannerItem(Block bannerBlock, Block wallBannerBlock, Item.Settings settings) {
        super(bannerBlock, wallBannerBlock, Direction.DOWN, settings);
        Validate.isInstanceOf(AbstractBannerBlock.class, bannerBlock);
        Validate.isInstanceOf(AbstractBannerBlock.class, wallBannerBlock);
    }

    public DyeColor getColor() {
        return ((AbstractBannerBlock)this.getBlock()).getColor();
    }
}

