/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PotionItem
extends Item {
    public PotionItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack lv = super.getDefaultStack();
        lv.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.WATER));
        return lv;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World lv = context.getWorld();
        BlockPos lv2 = context.getBlockPos();
        PlayerEntity lv3 = context.getPlayer();
        ItemStack lv4 = context.getStack();
        PotionContentsComponent lv5 = lv4.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        BlockState lv6 = lv.getBlockState(lv2);
        if (context.getSide() != Direction.DOWN && lv6.isIn(BlockTags.CONVERTABLE_TO_MUD) && lv5.matches(Potions.WATER)) {
            lv.playSound(null, lv2, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1.0f, 1.0f);
            lv3.setStackInHand(context.getHand(), ItemUsage.exchangeStack(lv4, lv3, new ItemStack(Items.GLASS_BOTTLE)));
            if (!lv.isClient()) {
                ServerWorld lv7 = (ServerWorld)lv;
                for (int i = 0; i < 5; ++i) {
                    lv7.spawnParticles(ParticleTypes.SPLASH, (double)lv2.getX() + lv.random.nextDouble(), lv2.getY() + 1, (double)lv2.getZ() + lv.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0);
                }
            }
            lv.playSound(null, lv2, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            lv.emitGameEvent(null, GameEvent.FLUID_PLACE, lv2);
            lv.setBlockState(lv2, Blocks.MUD.getDefaultState());
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public Text getName(ItemStack stack) {
        PotionContentsComponent lv = stack.get(DataComponentTypes.POTION_CONTENTS);
        return lv != null ? lv.getName(this.translationKey + ".effect.") : super.getName(stack);
    }
}

