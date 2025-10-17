/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CreakingHeartBlock;
import net.minecraft.block.EyeblossomBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;

public class FlowerPotBlock
extends Block {
    public static final MapCodec<FlowerPotBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Registries.BLOCK.getCodec().fieldOf("potted")).forGetter(block -> block.content), FlowerPotBlock.createSettingsCodec()).apply((Applicative<FlowerPotBlock, ?>)instance, FlowerPotBlock::new));
    private static final Map<Block, Block> CONTENT_TO_POTTED = Maps.newHashMap();
    private static final VoxelShape SHAPE = Block.createColumnShape(6.0, 0.0, 6.0);
    private final Block content;

    public MapCodec<FlowerPotBlock> getCodec() {
        return CODEC;
    }

    public FlowerPotBlock(Block content, AbstractBlock.Settings settings) {
        super(settings);
        this.content = content;
        CONTENT_TO_POTTED.put(content, this);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        Block block;
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            BlockItem lv = (BlockItem)item;
            block = CONTENT_TO_POTTED.getOrDefault(lv.getBlock(), Blocks.AIR);
        } else {
            block = Blocks.AIR;
        }
        BlockState lv2 = block.getDefaultState();
        if (lv2.isAir()) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!this.isEmpty()) {
            return ActionResult.CONSUME;
        }
        world.setBlockState(pos, lv2, Block.NOTIFY_ALL);
        world.emitGameEvent((Entity)player, GameEvent.BLOCK_CHANGE, pos);
        player.incrementStat(Stats.POT_FLOWER);
        stack.decrementUnlessCreative(1, player);
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (this.isEmpty()) {
            return ActionResult.CONSUME;
        }
        ItemStack lv = new ItemStack(this.content);
        if (!player.giveItemStack(lv)) {
            player.dropItem(lv, false);
        }
        world.setBlockState(pos, Blocks.FLOWER_POT.getDefaultState(), Block.NOTIFY_ALL);
        world.emitGameEvent((Entity)player, GameEvent.BLOCK_CHANGE, pos);
        return ActionResult.SUCCESS;
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        if (this.isEmpty()) {
            return super.getPickStack(world, pos, state, includeData);
        }
        return new ItemStack(this.content);
    }

    private boolean isEmpty() {
        return this.content == Blocks.AIR;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    public Block getContent() {
        return this.content;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return state.isOf(Blocks.POTTED_OPEN_EYEBLOSSOM) || state.isOf(Blocks.POTTED_CLOSED_EYEBLOSSOM);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean bl2;
        boolean bl;
        if (this.hasRandomTicks(state) && world.getDimension().natural() && (bl = this.content == Blocks.OPEN_EYEBLOSSOM) != (bl2 = CreakingHeartBlock.isNightAndNatural(world))) {
            world.setBlockState(pos, this.getToggledState(state), Block.NOTIFY_ALL);
            EyeblossomBlock.EyeblossomState lv = EyeblossomBlock.EyeblossomState.of(bl).getOpposite();
            lv.spawnTrailParticle(world, pos, random);
            world.playSound(null, pos, lv.getLongSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        super.randomTick(state, world, pos, random);
    }

    public BlockState getToggledState(BlockState state) {
        if (state.isOf(Blocks.POTTED_OPEN_EYEBLOSSOM)) {
            return Blocks.POTTED_CLOSED_EYEBLOSSOM.getDefaultState();
        }
        if (state.isOf(Blocks.POTTED_CLOSED_EYEBLOSSOM)) {
            return Blocks.POTTED_OPEN_EYEBLOSSOM.getDefaultState();
        }
        return state;
    }
}

