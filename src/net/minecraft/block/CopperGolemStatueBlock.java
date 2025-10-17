/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.IntFunction;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class CopperGolemStatueBlock
extends BlockWithEntity
implements Waterloggable {
    public static final MapCodec<CopperGolemStatueBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Oxidizable.OxidationLevel.CODEC.fieldOf("weathering_state")).forGetter(CopperGolemStatueBlock::getOxidationLevel), CopperGolemStatueBlock.createSettingsCodec()).apply((Applicative<CopperGolemStatueBlock, ?>)instance, CopperGolemStatueBlock::new));
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<Pose> POSE = Properties.COPPER_GOLEM_POSE;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.createColumnShape(10.0, 0.0, 14.0);
    private final Oxidizable.OxidationLevel oxidationLevel;

    public MapCodec<? extends CopperGolemStatueBlock> getCodec() {
        return CODEC;
    }

    public CopperGolemStatueBlock(Oxidizable.OxidationLevel oxidationLevel, AbstractBlock.Settings settings) {
        super(settings);
        this.oxidationLevel = oxidationLevel;
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, Direction.NORTH)).with(POSE, Pose.STANDING)).with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, POSE, WATERLOGGED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())).with(WATERLOGGED, lv.getFluid() == Fluids.WATER);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCullingShape(BlockState state) {
        return VoxelShapes.empty();
    }

    public Oxidizable.OxidationLevel getOxidationLevel() {
        return this.oxidationLevel;
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isIn(ItemTags.AXES)) {
            return ActionResult.PASS;
        }
        this.changePose(world, state, pos, player);
        return ActionResult.SUCCESS;
    }

    void changePose(World world, BlockState state, BlockPos pos, PlayerEntity player) {
        world.playSound(null, pos, SoundEvents.ENTITY_COPPER_GOLEM_BECOME_STATUE, SoundCategory.BLOCKS);
        world.setBlockState(pos, (BlockState)state.with(POSE, state.get(POSE).getNext()), Block.NOTIFY_ALL);
        world.emitGameEvent((Entity)player, GameEvent.BLOCK_CHANGE, pos);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return type == NavigationType.WATER && state.getFluidState().isIn(FluidTags.WATER);
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CopperGolemStatueBlockEntity(pos, state);
    }

    @Override
    public boolean keepBlockEntityWhenReplacedWith(BlockState state) {
        return state.isIn(BlockTags.COPPER_GOLEM_STATUES);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return state.get(POSE).ordinal() + 1;
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CopperGolemStatueBlockEntity) {
            CopperGolemStatueBlockEntity lv = (CopperGolemStatueBlockEntity)blockEntity;
            return lv.withComponents(this.asItem().getDefaultStack(), state.get(POSE));
        }
        return super.getPickStack(world, pos, state, includeData);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        world.updateComparators(pos, state.getBlock());
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    public static enum Pose implements StringIdentifiable
    {
        STANDING("standing"),
        SITTING("sitting"),
        RUNNING("running"),
        STAR("star");

        public static final IntFunction<Pose> INDEX_MAPPER;
        public static final Codec<Pose> CODEC;
        private final String id;

        private Pose(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        public Pose getNext() {
            return INDEX_MAPPER.apply(this.ordinal() + 1);
        }

        static {
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(Enum::ordinal, Pose.values(), ValueLists.OutOfBoundsHandling.ZERO);
            CODEC = StringIdentifiable.createCodec(Pose::values);
        }
    }
}

