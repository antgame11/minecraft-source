/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CopperChestBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class CarvedPumpkinBlock
extends HorizontalFacingBlock {
    public static final MapCodec<CarvedPumpkinBlock> CODEC = CarvedPumpkinBlock.createCodec(CarvedPumpkinBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    @Nullable
    private BlockPattern snowGolemDispenserPattern;
    @Nullable
    private BlockPattern snowGolemPattern;
    @Nullable
    private BlockPattern ironGolemDispenserPattern;
    @Nullable
    private BlockPattern ironGolemPattern;
    @Nullable
    private BlockPattern copperGolemDispenserPattern;
    @Nullable
    private BlockPattern copperGolemPattern;
    private static final Predicate<BlockState> IS_GOLEM_HEAD_PREDICATE = state -> state != null && (state.isOf(Blocks.CARVED_PUMPKIN) || state.isOf(Blocks.JACK_O_LANTERN));

    public MapCodec<? extends CarvedPumpkinBlock> getCodec() {
        return CODEC;
    }

    protected CarvedPumpkinBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH));
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        this.trySpawnEntity(world, pos);
    }

    public boolean canDispense(WorldView world, BlockPos pos) {
        return this.getSnowGolemDispenserPattern().searchAround(world, pos) != null || this.getIronGolemDispenserPattern().searchAround(world, pos) != null || this.getCopperGolemDispenserPattern().searchAround(world, pos) != null;
    }

    private void trySpawnEntity(World world, BlockPos pos) {
        CopperGolemEntity lv6;
        IronGolemEntity lv4;
        SnowGolemEntity lv2;
        BlockPattern.Result lv = this.getSnowGolemPattern().searchAround(world, pos);
        if (lv != null && (lv2 = EntityType.SNOW_GOLEM.create(world, SpawnReason.TRIGGERED)) != null) {
            CarvedPumpkinBlock.spawnEntity(world, lv, lv2, lv.translate(0, 2, 0).getBlockPos());
            return;
        }
        BlockPattern.Result lv3 = this.getIronGolemPattern().searchAround(world, pos);
        if (lv3 != null && (lv4 = EntityType.IRON_GOLEM.create(world, SpawnReason.TRIGGERED)) != null) {
            lv4.setPlayerCreated(true);
            CarvedPumpkinBlock.spawnEntity(world, lv3, lv4, lv3.translate(1, 2, 0).getBlockPos());
            return;
        }
        BlockPattern.Result lv5 = this.getCopperGolemPattern().searchAround(world, pos);
        if (lv5 != null && (lv6 = EntityType.COPPER_GOLEM.create(world, SpawnReason.TRIGGERED)) != null) {
            CarvedPumpkinBlock.spawnEntity(world, lv5, lv6, lv5.translate(0, 0, 0).getBlockPos());
            this.replaceCopperBlockWithChest(world, lv5);
            lv6.onSpawn(this.getOxidationLevel(lv5));
        }
    }

    private Oxidizable.OxidationLevel getOxidationLevel(BlockPattern.Result patternResult) {
        BlockState lv = patternResult.translate(0, 1, 0).getBlockState();
        Block lv2 = lv.getBlock();
        if (lv2 instanceof Oxidizable) {
            Oxidizable lv3 = (Oxidizable)((Object)lv2);
            return (Oxidizable.OxidationLevel)lv3.getDegradationLevel();
        }
        return (Oxidizable.OxidationLevel)Optional.ofNullable((Block)HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get(lv.getBlock())).filter(block -> block instanceof Oxidizable).map(block -> (Oxidizable)((Object)block)).orElse((Oxidizable)((Object)Blocks.COPPER_BLOCK)).getDegradationLevel();
    }

    private static void spawnEntity(World world, BlockPattern.Result patternResult, Entity entity, BlockPos pos) {
        CarvedPumpkinBlock.breakPatternBlocks(world, patternResult);
        entity.refreshPositionAndAngles((double)pos.getX() + 0.5, (double)pos.getY() + 0.05, (double)pos.getZ() + 0.5, 0.0f, 0.0f);
        world.spawnEntity(entity);
        for (ServerPlayerEntity lv : world.getNonSpectatingEntities(ServerPlayerEntity.class, entity.getBoundingBox().expand(5.0))) {
            Criteria.SUMMONED_ENTITY.trigger(lv, entity);
        }
        CarvedPumpkinBlock.updatePatternBlocks(world, patternResult);
    }

    public static void breakPatternBlocks(World world, BlockPattern.Result patternResult) {
        for (int i = 0; i < patternResult.getWidth(); ++i) {
            for (int j = 0; j < patternResult.getHeight(); ++j) {
                CachedBlockPosition lv = patternResult.translate(i, j, 0);
                world.setBlockState(lv.getBlockPos(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, lv.getBlockPos(), Block.getRawIdFromState(lv.getBlockState()));
            }
        }
    }

    public static void updatePatternBlocks(World world, BlockPattern.Result patternResult) {
        for (int i = 0; i < patternResult.getWidth(); ++i) {
            for (int j = 0; j < patternResult.getHeight(); ++j) {
                CachedBlockPosition lv = patternResult.translate(i, j, 0);
                world.updateNeighbors(lv.getBlockPos(), Blocks.AIR);
            }
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private BlockPattern getSnowGolemDispenserPattern() {
        if (this.snowGolemDispenserPattern == null) {
            this.snowGolemDispenserPattern = BlockPatternBuilder.start().aisle(" ", "#", "#").where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK))).build();
        }
        return this.snowGolemDispenserPattern;
    }

    private BlockPattern getSnowGolemPattern() {
        if (this.snowGolemPattern == null) {
            this.snowGolemPattern = BlockPatternBuilder.start().aisle("^", "#", "#").where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE)).where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK))).build();
        }
        return this.snowGolemPattern;
    }

    private BlockPattern getIronGolemDispenserPattern() {
        if (this.ironGolemDispenserPattern == null) {
            this.ironGolemDispenserPattern = BlockPatternBuilder.start().aisle("~ ~", "###", "~#~").where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', pos -> pos.getBlockState().isAir()).build();
        }
        return this.ironGolemDispenserPattern;
    }

    private BlockPattern getIronGolemPattern() {
        if (this.ironGolemPattern == null) {
            this.ironGolemPattern = BlockPatternBuilder.start().aisle("~^~", "###", "~#~").where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE)).where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', pos -> pos.getBlockState().isAir()).build();
        }
        return this.ironGolemPattern;
    }

    private BlockPattern getCopperGolemDispenserPattern() {
        if (this.copperGolemDispenserPattern == null) {
            this.copperGolemDispenserPattern = BlockPatternBuilder.start().aisle(" ", "#").where('#', CachedBlockPosition.matchesBlockState(state -> state.isIn(BlockTags.COPPER))).build();
        }
        return this.copperGolemDispenserPattern;
    }

    private BlockPattern getCopperGolemPattern() {
        if (this.copperGolemPattern == null) {
            this.copperGolemPattern = BlockPatternBuilder.start().aisle("^", "#").where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE)).where('#', CachedBlockPosition.matchesBlockState(state -> state.isIn(BlockTags.COPPER))).build();
        }
        return this.copperGolemPattern;
    }

    public void replaceCopperBlockWithChest(World world, BlockPattern.Result patternResult) {
        CachedBlockPosition lv = patternResult.translate(0, 1, 0);
        CachedBlockPosition lv2 = patternResult.translate(0, 0, 0);
        Direction lv3 = lv2.getBlockState().get(FACING);
        BlockState lv4 = CopperChestBlock.fromCopperBlock(lv.getBlockState().getBlock(), lv3, world, lv.getBlockPos());
        world.setBlockState(lv.getBlockPos(), lv4, Block.NOTIFY_LISTENERS);
    }
}

