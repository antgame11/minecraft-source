/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ShearsItem
extends Item {
    public ShearsItem(Item.Settings arg) {
        super(arg);
    }

    public static ToolComponent createToolComponent() {
        RegistryEntryLookup<Block> lv = Registries.createEntryLookup(Registries.BLOCK);
        return new ToolComponent(List.of(ToolComponent.Rule.ofAlwaysDropping(RegistryEntryList.of(Blocks.COBWEB.getRegistryEntry()), 15.0f), ToolComponent.Rule.of(lv.getOrThrow(BlockTags.LEAVES), 15.0f), ToolComponent.Rule.of(lv.getOrThrow(BlockTags.WOOL), 5.0f), ToolComponent.Rule.of(RegistryEntryList.of(Blocks.VINE.getRegistryEntry(), Blocks.GLOW_LICHEN.getRegistryEntry()), 2.0f)), 1.0f, 1, true);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        ToolComponent lv = stack.get(DataComponentTypes.TOOL);
        if (lv == null) {
            return false;
        }
        if (!world.isClient() && !state.isIn(BlockTags.FIRE) && lv.damagePerBlock() > 0) {
            stack.damage(lv.damagePerBlock(), miner, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        AbstractPlantStemBlock lv5;
        BlockPos lv2;
        World lv = context.getWorld();
        BlockState lv3 = lv.getBlockState(lv2 = context.getBlockPos());
        Block lv4 = lv3.getBlock();
        if (lv4 instanceof AbstractPlantStemBlock && !(lv5 = (AbstractPlantStemBlock)lv4).hasMaxAge(lv3)) {
            PlayerEntity lv6 = context.getPlayer();
            ItemStack lv7 = context.getStack();
            if (lv6 instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)lv6, lv2, lv7);
            }
            lv.playSound((Entity)lv6, lv2, SoundEvents.BLOCK_GROWING_PLANT_CROP, SoundCategory.BLOCKS, 1.0f, 1.0f);
            BlockState lv8 = lv5.withMaxAge(lv3);
            lv.setBlockState(lv2, lv8);
            lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv2, GameEvent.Emitter.of(context.getPlayer(), lv8));
            if (lv6 != null) {
                lv7.damage(1, (LivingEntity)lv6, context.getHand().getEquipmentSlot());
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }
}

