/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class AxeItem
extends Item {
    protected static final Map<Block, Block> STRIPPED_BLOCKS = new ImmutableMap.Builder<Block, Block>().put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD).put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG).put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD).put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG).put(Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD).put(Blocks.PALE_OAK_LOG, Blocks.STRIPPED_PALE_OAK_LOG).put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD).put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG).put(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD).put(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG).put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD).put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG).put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD).put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG).put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD).put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG).put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM).put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE).put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM).put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE).put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD).put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG).put(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK).build();

    public AxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Settings settings) {
        super(settings.axe(material, attackDamage, attackSpeed));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World lv = context.getWorld();
        BlockPos lv2 = context.getBlockPos();
        PlayerEntity lv3 = context.getPlayer();
        if (AxeItem.shouldCancelStripAttempt(context)) {
            return ActionResult.PASS;
        }
        Optional<BlockState> optional = this.tryStrip(lv, lv2, lv3, lv.getBlockState(lv2));
        if (optional.isEmpty()) {
            return ActionResult.PASS;
        }
        ItemStack lv4 = context.getStack();
        if (lv3 instanceof ServerPlayerEntity) {
            Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)lv3, lv2, lv4);
        }
        lv.setBlockState(lv2, optional.get(), Block.NOTIFY_ALL_AND_REDRAW);
        lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv2, GameEvent.Emitter.of(lv3, optional.get()));
        if (lv3 != null) {
            lv4.damage(1, (LivingEntity)lv3, context.getHand().getEquipmentSlot());
        }
        return ActionResult.SUCCESS;
    }

    private static boolean shouldCancelStripAttempt(ItemUsageContext context) {
        PlayerEntity lv = context.getPlayer();
        return context.getHand().equals((Object)Hand.MAIN_HAND) && lv.getOffHandStack().contains(DataComponentTypes.BLOCKS_ATTACKS) && !lv.shouldCancelInteraction();
    }

    private Optional<BlockState> tryStrip(World world, BlockPos pos, @Nullable PlayerEntity player, BlockState state) {
        Optional<BlockState> optional = this.getStrippedState(state);
        if (optional.isPresent()) {
            world.playSound((Entity)player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0f, 1.0f);
            return optional;
        }
        Optional<BlockState> optional2 = Oxidizable.getDecreasedOxidationState(state);
        if (optional2.isPresent()) {
            AxeItem.strip(world, pos, player, state, SoundEvents.ITEM_AXE_SCRAPE, 3005);
            return optional2;
        }
        Optional<BlockState> optional3 = Optional.ofNullable((Block)HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get(state.getBlock())).map(block -> block.getStateWithProperties(state));
        if (optional3.isPresent()) {
            AxeItem.strip(world, pos, player, state, SoundEvents.ITEM_AXE_WAX_OFF, 3004);
            return optional3;
        }
        return Optional.empty();
    }

    private static void strip(World world, BlockPos pos, @Nullable PlayerEntity player, BlockState state, SoundEvent sound, int worldEvent) {
        world.playSound((Entity)player, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.syncWorldEvent(player, worldEvent, pos, 0);
        if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
            BlockPos lv = ChestBlock.getPosInFrontOf(pos, state);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, lv, GameEvent.Emitter.of(player, world.getBlockState(lv)));
            world.syncWorldEvent(player, worldEvent, lv, 0);
        }
    }

    private Optional<BlockState> getStrippedState(BlockState state) {
        return Optional.ofNullable(STRIPPED_BLOCKS.get(state.getBlock())).map(block -> (BlockState)block.getDefaultState().with(PillarBlock.AXIS, state.get(PillarBlock.AXIS)));
    }
}

