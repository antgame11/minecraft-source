/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ProjectileItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FireworkRocketItem
extends Item
implements ProjectileItem {
    public static final byte[] FLIGHT_VALUES = new byte[]{1, 2, 3};
    public static final double OFFSET_POS_MULTIPLIER = 0.15;

    public FireworkRocketItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World lv = context.getWorld();
        PlayerEntity lv2 = context.getPlayer();
        if (lv2 != null && lv2.isGliding()) {
            return ActionResult.PASS;
        }
        if (lv instanceof ServerWorld) {
            ServerWorld lv3 = (ServerWorld)lv;
            ItemStack lv4 = context.getStack();
            Vec3d lv5 = context.getHitPos();
            Direction lv6 = context.getSide();
            ProjectileEntity.spawn(new FireworkRocketEntity(lv, context.getPlayer(), lv5.x + (double)lv6.getOffsetX() * 0.15, lv5.y + (double)lv6.getOffsetY() * 0.15, lv5.z + (double)lv6.getOffsetZ() * 0.15, lv4), lv3, lv4);
            lv4.decrement(1);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user.isGliding()) {
            ItemStack lv = user.getStackInHand(hand);
            if (world instanceof ServerWorld) {
                ServerWorld lv2 = (ServerWorld)world;
                if (user.detachAllHeldLeashes(null)) {
                    world.playSoundFromEntity(null, user, SoundEvents.ITEM_LEAD_BREAK, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                }
                ProjectileEntity.spawn(new FireworkRocketEntity(world, lv, user), lv2, lv);
                lv.decrementUnlessCreative(1, user);
                user.incrementStat(Stats.USED.getOrCreateStat(this));
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        return new FireworkRocketEntity(world, stack.copyWithCount(1), pos.getX(), pos.getY(), pos.getZ(), true);
    }

    @Override
    public ProjectileItem.Settings getProjectileSettings() {
        return ProjectileItem.Settings.builder().positionFunction(FireworkRocketItem::position).uncertainty(1.0f).power(0.5f).overrideDispenseEvent(1004).build();
    }

    private static Vec3d position(BlockPointer pointer, Direction facing) {
        return pointer.centerPos().add((double)facing.getOffsetX() * 0.5000099999997474, (double)facing.getOffsetY() * 0.5000099999997474, (double)facing.getOffsetZ() * 0.5000099999997474);
    }
}

