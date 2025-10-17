/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.Sherds;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
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
import org.jetbrains.annotations.Nullable;

public class DecoratedPotBlock
extends BlockWithEntity
implements Waterloggable {
    public static final MapCodec<DecoratedPotBlock> CODEC = DecoratedPotBlock.createCodec(DecoratedPotBlock::new);
    public static final Identifier SHERDS_DYNAMIC_DROP_ID = Identifier.ofVanilla("sherds");
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty CRACKED = Properties.CRACKED;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.createColumnShape(14.0, 0.0, 16.0);

    public MapCodec<DecoratedPotBlock> getCodec() {
        return CODEC;
    }

    protected DecoratedPotBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, false)).with(CRACKED, false));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing())).with(WATERLOGGED, lv.getFluid() == Fluids.WATER)).with(CRACKED, false);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof DecoratedPotBlockEntity)) {
            return ActionResult.PASS;
        }
        DecoratedPotBlockEntity lv = (DecoratedPotBlockEntity)blockEntity;
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        ItemStack lv2 = lv.getStack();
        if (!stack.isEmpty() && (lv2.isEmpty() || ItemStack.areItemsAndComponentsEqual(lv2, stack) && lv2.getCount() < lv2.getMaxCount())) {
            float f;
            lv.wobble(DecoratedPotBlockEntity.WobbleType.POSITIVE);
            player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            ItemStack lv3 = stack.splitUnlessCreative(1, player);
            if (lv.isEmpty()) {
                lv.setStack(lv3);
                f = (float)lv3.getCount() / (float)lv3.getMaxCount();
            } else {
                lv2.increment(1);
                f = (float)lv2.getCount() / (float)lv2.getMaxCount();
            }
            world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_INSERT, SoundCategory.BLOCKS, 1.0f, 0.7f + 0.5f * f);
            if (world instanceof ServerWorld) {
                ServerWorld lv4 = (ServerWorld)world;
                lv4.spawnParticles(ParticleTypes.DUST_PLUME, (double)pos.getX() + 0.5, (double)pos.getY() + 1.2, (double)pos.getZ() + 0.5, 7, 0.0, 0.0, 0.0, 0.0);
            }
            lv.markDirty();
            world.emitGameEvent((Entity)player, GameEvent.BLOCK_CHANGE, pos);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof DecoratedPotBlockEntity)) {
            return ActionResult.PASS;
        }
        DecoratedPotBlockEntity lv = (DecoratedPotBlockEntity)blockEntity;
        world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_INSERT_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f);
        lv.wobble(DecoratedPotBlockEntity.WobbleType.NEGATIVE);
        world.emitGameEvent((Entity)player, GameEvent.BLOCK_CHANGE, pos);
        return ActionResult.SUCCESS;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, CRACKED);
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DecoratedPotBlockEntity(pos, state);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        BlockEntity lv = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (lv instanceof DecoratedPotBlockEntity) {
            DecoratedPotBlockEntity lv2 = (DecoratedPotBlockEntity)lv;
            builder.addDynamicDrop(SHERDS_DYNAMIC_DROP_ID, lootConsumer -> {
                for (Item lv : lv2.getSherds().toList()) {
                    lootConsumer.accept(lv.getDefaultStack());
                }
            });
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        ItemStack lv = player.getMainHandStack();
        BlockState lv2 = state;
        if (lv.isIn(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasAnyEnchantmentsIn(lv, EnchantmentTags.PREVENTS_DECORATED_POT_SHATTERING)) {
            lv2 = (BlockState)state.with(CRACKED, true);
            world.setBlockState(pos, lv2, Block.SKIP_REDRAW_AND_BLOCK_ENTITY_REPLACED_CALLBACK);
        }
        return super.onBreak(world, pos, lv2, player);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected BlockSoundGroup getSoundGroup(BlockState state) {
        if (state.get(CRACKED).booleanValue()) {
            return BlockSoundGroup.DECORATED_POT_SHATTER;
        }
        return BlockSoundGroup.DECORATED_POT;
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        ServerWorld lv2;
        BlockPos lv = hit.getBlockPos();
        if (world instanceof ServerWorld && projectile.canModifyAt(lv2 = (ServerWorld)world, lv) && projectile.canBreakBlocks(lv2)) {
            world.setBlockState(lv, (BlockState)state.with(CRACKED, true), Block.SKIP_REDRAW_AND_BLOCK_ENTITY_REPLACED_CALLBACK);
            world.breakBlock(lv, true, projectile);
        }
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof DecoratedPotBlockEntity) {
            DecoratedPotBlockEntity lv = (DecoratedPotBlockEntity)blockEntity;
            Sherds lv2 = lv.getSherds();
            return DecoratedPotBlockEntity.getStackWith(lv2);
        }
        return super.getPickStack(world, pos, state, includeData);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}

