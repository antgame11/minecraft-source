/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.ChestType;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class CopperChestBlock
extends ChestBlock {
    public static final MapCodec<CopperChestBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Oxidizable.OxidationLevel.CODEC.fieldOf("weathering_state")).forGetter(CopperChestBlock::getOxidationLevel), ((MapCodec)Registries.SOUND_EVENT.getCodec().fieldOf("open_sound")).forGetter(ChestBlock::getOpenSound), ((MapCodec)Registries.SOUND_EVENT.getCodec().fieldOf("close_sound")).forGetter(ChestBlock::getCloseSound), CopperChestBlock.createSettingsCodec()).apply((Applicative<CopperChestBlock, ?>)instance, CopperChestBlock::new));
    private static final Map<Block, Supplier<Block>> FROM_COPPER_BLOCK = Map.of(Blocks.COPPER_BLOCK, () -> Blocks.COPPER_CHEST, Blocks.EXPOSED_COPPER, () -> Blocks.EXPOSED_COPPER_CHEST, Blocks.WEATHERED_COPPER, () -> Blocks.WEATHERED_COPPER_CHEST, Blocks.OXIDIZED_COPPER, () -> Blocks.OXIDIZED_COPPER_CHEST, Blocks.WAXED_COPPER_BLOCK, () -> Blocks.COPPER_CHEST, Blocks.WAXED_EXPOSED_COPPER, () -> Blocks.EXPOSED_COPPER_CHEST, Blocks.WAXED_WEATHERED_COPPER, () -> Blocks.WEATHERED_COPPER_CHEST, Blocks.WAXED_OXIDIZED_COPPER, () -> Blocks.OXIDIZED_COPPER_CHEST);
    private final Oxidizable.OxidationLevel oxidationLevel;

    @Override
    public MapCodec<? extends CopperChestBlock> getCodec() {
        return CODEC;
    }

    public CopperChestBlock(Oxidizable.OxidationLevel oxidationLevel, SoundEvent openSound, SoundEvent closeSound, AbstractBlock.Settings settings) {
        super(() -> BlockEntityType.CHEST, openSound, closeSound, settings);
        this.oxidationLevel = oxidationLevel;
    }

    @Override
    public boolean canMergeWith(BlockState state) {
        return state.isIn(BlockTags.COPPER_CHESTS) && state.contains(ChestBlock.CHEST_TYPE);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState lv = super.getPlacementState(ctx);
        return CopperChestBlock.getNewState(lv, ctx.getWorld(), ctx.getBlockPos());
    }

    private static BlockState getNewState(BlockState state, World world, BlockPos pos) {
        Block block;
        BlockState lv = world.getBlockState(pos.offset(CopperChestBlock.getFacing(state)));
        if (!state.get(ChestBlock.CHEST_TYPE).equals(ChestType.SINGLE) && (block = state.getBlock()) instanceof CopperChestBlock) {
            CopperChestBlock lv2 = (CopperChestBlock)block;
            block = lv.getBlock();
            if (block instanceof CopperChestBlock) {
                CopperChestBlock lv3 = (CopperChestBlock)block;
                BlockState lv4 = state;
                BlockState lv5 = lv;
                if (lv2.isWaxed() != lv3.isWaxed()) {
                    lv4 = CopperChestBlock.getUnwaxed(lv2, state).orElse(lv4);
                    lv5 = CopperChestBlock.getUnwaxed(lv3, lv).orElse(lv5);
                }
                Block lv6 = lv2.oxidationLevel.ordinal() <= lv3.oxidationLevel.ordinal() ? lv4.getBlock() : lv5.getBlock();
                return lv6.getStateWithProperties(lv4);
            }
        }
        return state;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        ChestType lv2;
        BlockState lv = super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        if (this.canMergeWith(neighborState) && !(lv2 = lv.get(ChestBlock.CHEST_TYPE)).equals(ChestType.SINGLE) && CopperChestBlock.getFacing(lv) == direction) {
            return neighborState.getBlock().getStateWithProperties(lv);
        }
        return lv;
    }

    private static Optional<BlockState> getUnwaxed(CopperChestBlock block, BlockState state) {
        if (!block.isWaxed()) {
            return Optional.of(state);
        }
        return Optional.ofNullable((Block)HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get(state.getBlock())).map(waxedState -> waxedState.getStateWithProperties(state));
    }

    public Oxidizable.OxidationLevel getOxidationLevel() {
        return this.oxidationLevel;
    }

    public static BlockState fromCopperBlock(Block block, Direction facing, World world, BlockPos pos) {
        CopperChestBlock lv = (CopperChestBlock)FROM_COPPER_BLOCK.getOrDefault(block, Blocks.COPPER_CHEST::asBlock).get();
        ChestType lv2 = lv.getChestType(world, pos, facing);
        BlockState lv3 = (BlockState)((BlockState)lv.getDefaultState().with(FACING, facing)).with(CHEST_TYPE, lv2);
        return CopperChestBlock.getNewState(lv3, world, pos);
    }

    public boolean isWaxed() {
        return true;
    }

    @Override
    public boolean keepBlockEntityWhenReplacedWith(BlockState state) {
        return state.isIn(BlockTags.COPPER_CHESTS);
    }
}

