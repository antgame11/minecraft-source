/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EmptyMapItem
extends Item {
    public EmptyMapItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        if (!(world instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }
        ServerWorld lv2 = (ServerWorld)world;
        lv.decrementUnlessCreative(1, user);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        lv2.playSoundFromEntity(null, user, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, user.getSoundCategory(), 1.0f, 1.0f);
        ItemStack lv3 = FilledMapItem.createMap(lv2, user.getBlockX(), user.getBlockZ(), (byte)0, true, false);
        if (lv.isEmpty()) {
            return ActionResult.SUCCESS.withNewHandStack(lv3);
        }
        if (!user.getInventory().insertStack(lv3.copy())) {
            user.dropItem(lv3, false);
        }
        return ActionResult.SUCCESS;
    }
}

