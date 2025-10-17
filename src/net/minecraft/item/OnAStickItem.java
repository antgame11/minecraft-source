/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class OnAStickItem<T extends Entity>
extends Item {
    private final EntityType<T> target;
    private final int damagePerUse;

    public OnAStickItem(EntityType<T> target, int damagePerUse, Item.Settings settings) {
        super(settings);
        this.target = target;
        this.damagePerUse = damagePerUse;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        if (world.isClient()) {
            return ActionResult.PASS;
        }
        Entity lv2 = user.getControllingVehicle();
        if (user.hasVehicle() && lv2 instanceof ItemSteerable) {
            ItemSteerable lv3 = (ItemSteerable)((Object)lv2);
            if (lv2.getType() == this.target && lv3.consumeOnAStickItem()) {
                EquipmentSlot lv4 = hand.getEquipmentSlot();
                ItemStack lv5 = lv.damage(this.damagePerUse, Items.FISHING_ROD, user, lv4);
                return ActionResult.SUCCESS_SERVER.withNewHandStack(lv5);
            }
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return ActionResult.PASS;
    }
}

