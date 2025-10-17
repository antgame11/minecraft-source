/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class LeadItem
extends Item {
    public LeadItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos lv2;
        World lv = context.getWorld();
        BlockState lv3 = lv.getBlockState(lv2 = context.getBlockPos());
        if (lv3.isIn(BlockTags.FENCES)) {
            PlayerEntity lv4 = context.getPlayer();
            if (!lv.isClient() && lv4 != null) {
                return LeadItem.attachHeldMobsToBlock(lv4, lv, lv2);
            }
        }
        return ActionResult.PASS;
    }

    public static ActionResult attachHeldMobsToBlock(PlayerEntity player, World world, BlockPos pos) {
        LeashKnotEntity lv = null;
        List<Leashable> list = Leashable.collectLeashablesAround(world, Vec3d.ofCenter(pos), entity -> entity.getLeashHolder() == player);
        boolean bl = false;
        for (Leashable lv2 : list) {
            if (lv == null) {
                lv = LeashKnotEntity.getOrCreate(world, pos);
                lv.onPlace();
            }
            if (!lv2.canBeLeashedTo(lv)) continue;
            lv2.attachLeash(lv, true);
            bl = true;
        }
        if (bl) {
            world.emitGameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Emitter.of(player));
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.PASS;
    }
}

