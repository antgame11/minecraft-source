/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SignChangingItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class HoneycombItem
extends Item
implements SignChangingItem {
    public static final Supplier<BiMap<Block, Block>> UNWAXED_TO_WAXED_BLOCKS = Suppliers.memoize(() -> ((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)ImmutableBiMap.builder().put(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK)).put(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER)).put(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER)).put(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER)).put(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER)).put(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER)).put(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER)).put(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER)).put(Blocks.CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER_SLAB)).put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB)).put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB)).put(Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB)).put(Blocks.CUT_COPPER_STAIRS, Blocks.WAXED_CUT_COPPER_STAIRS)).put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS)).put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS)).put(Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS)).put(Blocks.CHISELED_COPPER, Blocks.WAXED_CHISELED_COPPER)).put(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_CHISELED_COPPER)).put(Blocks.WEATHERED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER)).put(Blocks.OXIDIZED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_CHISELED_COPPER)).put(Blocks.COPPER_DOOR, Blocks.WAXED_COPPER_DOOR)).put(Blocks.EXPOSED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR)).put(Blocks.WEATHERED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR)).put(Blocks.OXIDIZED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR)).put(Blocks.COPPER_TRAPDOOR, Blocks.WAXED_COPPER_TRAPDOOR)).put(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR)).put(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR)).put(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR)).putAll(Blocks.COPPER_BARS.getWaxingMap())).put(Blocks.COPPER_GRATE, Blocks.WAXED_COPPER_GRATE)).put(Blocks.EXPOSED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER_GRATE)).put(Blocks.WEATHERED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER_GRATE)).put(Blocks.OXIDIZED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER_GRATE)).put(Blocks.COPPER_BULB, Blocks.WAXED_COPPER_BULB)).put(Blocks.EXPOSED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB)).put(Blocks.WEATHERED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB)).put(Blocks.OXIDIZED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB)).put(Blocks.COPPER_CHEST, Blocks.WAXED_COPPER_CHEST)).put(Blocks.EXPOSED_COPPER_CHEST, Blocks.WAXED_EXPOSED_COPPER_CHEST)).put(Blocks.WEATHERED_COPPER_CHEST, Blocks.WAXED_WEATHERED_COPPER_CHEST)).put(Blocks.OXIDIZED_COPPER_CHEST, Blocks.WAXED_OXIDIZED_COPPER_CHEST)).put(Blocks.COPPER_GOLEM_STATUE, Blocks.WAXED_COPPER_GOLEM_STATUE)).put(Blocks.EXPOSED_COPPER_GOLEM_STATUE, Blocks.WAXED_EXPOSED_COPPER_GOLEM_STATUE)).put(Blocks.WEATHERED_COPPER_GOLEM_STATUE, Blocks.WAXED_WEATHERED_COPPER_GOLEM_STATUE)).put(Blocks.OXIDIZED_COPPER_GOLEM_STATUE, Blocks.WAXED_OXIDIZED_COPPER_GOLEM_STATUE)).put(Blocks.LIGHTNING_ROD, Blocks.WAXED_LIGHTNING_ROD)).put(Blocks.EXPOSED_LIGHTNING_ROD, Blocks.WAXED_EXPOSED_LIGHTNING_ROD)).put(Blocks.WEATHERED_LIGHTNING_ROD, Blocks.WAXED_WEATHERED_LIGHTNING_ROD)).put(Blocks.OXIDIZED_LIGHTNING_ROD, Blocks.WAXED_OXIDIZED_LIGHTNING_ROD)).putAll(Blocks.COPPER_LANTERNS.getWaxingMap())).putAll(Blocks.COPPER_CHAINS.getWaxingMap())).build());
    public static final Supplier<BiMap<Block, Block>> WAXED_TO_UNWAXED_BLOCKS = Suppliers.memoize(() -> UNWAXED_TO_WAXED_BLOCKS.get().inverse());

    public HoneycombItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World lv = context.getWorld();
        BlockPos lv2 = context.getBlockPos();
        BlockState lv3 = lv.getBlockState(lv2);
        return HoneycombItem.getWaxedState(lv3).map(arg5 -> {
            PlayerEntity lv = context.getPlayer();
            ItemStack lv2 = context.getStack();
            if (lv instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv3 = (ServerPlayerEntity)lv;
                Criteria.ITEM_USED_ON_BLOCK.trigger(lv3, lv2, lv2);
            }
            lv2.decrement(1);
            lv.setBlockState(lv2, (BlockState)arg5, Block.NOTIFY_ALL_AND_REDRAW);
            lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv2, GameEvent.Emitter.of(lv, arg5));
            lv.syncWorldEvent(lv, WorldEvents.BLOCK_WAXED, lv2, 0);
            if (lv3.getBlock() instanceof ChestBlock && lv3.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                BlockPos lv4 = ChestBlock.getPosInFrontOf(lv2, lv3);
                lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv4, GameEvent.Emitter.of(lv, lv.getBlockState(lv4)));
                lv.syncWorldEvent(lv, WorldEvents.BLOCK_WAXED, lv4, 0);
            }
            return ActionResult.SUCCESS;
        }).orElse(ActionResult.PASS);
    }

    public static Optional<BlockState> getWaxedState(BlockState state) {
        return Optional.ofNullable((Block)UNWAXED_TO_WAXED_BLOCKS.get().get(state.getBlock())).map(block -> block.getStateWithProperties(state));
    }

    @Override
    public boolean useOnSign(World world, SignBlockEntity signBlockEntity, boolean front, PlayerEntity player) {
        if (signBlockEntity.setWaxed(true)) {
            world.syncWorldEvent(null, WorldEvents.BLOCK_WAXED, signBlockEntity.getPos(), 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean canUseOnSignText(SignText signText, PlayerEntity player) {
        return true;
    }
}

