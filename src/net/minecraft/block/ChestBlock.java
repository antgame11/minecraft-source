/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class ChestBlock
extends AbstractChestBlock<ChestBlockEntity>
implements Waterloggable {
    public static final MapCodec<ChestBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Registries.SOUND_EVENT.getCodec().fieldOf("open_sound")).forGetter(ChestBlock::getOpenSound), ((MapCodec)Registries.SOUND_EVENT.getCodec().fieldOf("close_sound")).forGetter(ChestBlock::getCloseSound), ChestBlock.createSettingsCodec()).apply((Applicative<ChestBlock, ?>)instance, (openSound, closeSound, settings) -> new ChestBlock(() -> BlockEntityType.CHEST, (SoundEvent)openSound, (SoundEvent)closeSound, (AbstractBlock.Settings)settings)));
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<ChestType> CHEST_TYPE = Properties.CHEST_TYPE;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final int field_31057 = 1;
    private static final VoxelShape SINGLE_SHAPE = Block.createColumnShape(14.0, 0.0, 14.0);
    private static final Map<Direction, VoxelShape> DOUBLE_SHAPES_BY_DIRECTION = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidZShape(14.0, 0.0, 14.0, 0.0, 15.0));
    private final SoundEvent openSound;
    private final SoundEvent closeSound;
    private static final DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<Inventory>> INVENTORY_RETRIEVER = new DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<Inventory>>(){

        @Override
        public Optional<Inventory> getFromBoth(ChestBlockEntity arg, ChestBlockEntity arg2) {
            return Optional.of(new DoubleInventory(arg, arg2));
        }

        @Override
        public Optional<Inventory> getFrom(ChestBlockEntity arg) {
            return Optional.of(arg);
        }

        @Override
        public Optional<Inventory> getFallback() {
            return Optional.empty();
        }

        @Override
        public /* synthetic */ Object getFallback() {
            return this.getFallback();
        }
    };
    private static final DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<NamedScreenHandlerFactory>> NAME_RETRIEVER = new DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<NamedScreenHandlerFactory>>(){

        @Override
        public Optional<NamedScreenHandlerFactory> getFromBoth(final ChestBlockEntity arg, final ChestBlockEntity arg2) {
            final DoubleInventory lv = new DoubleInventory(arg, arg2);
            return Optional.of(new NamedScreenHandlerFactory(){

                @Override
                @Nullable
                public ScreenHandler createMenu(int i, PlayerInventory arg3, PlayerEntity arg22) {
                    if (arg.checkUnlocked(arg22) && arg2.checkUnlocked(arg22)) {
                        arg.generateLoot(arg3.player);
                        arg2.generateLoot(arg3.player);
                        return GenericContainerScreenHandler.createGeneric9x6(i, arg3, lv);
                    }
                    return null;
                }

                @Override
                public Text getDisplayName() {
                    if (arg.hasCustomName()) {
                        return arg.getDisplayName();
                    }
                    if (arg2.hasCustomName()) {
                        return arg2.getDisplayName();
                    }
                    return Text.translatable("container.chestDouble");
                }
            });
        }

        @Override
        public Optional<NamedScreenHandlerFactory> getFrom(ChestBlockEntity arg) {
            return Optional.of(arg);
        }

        @Override
        public Optional<NamedScreenHandlerFactory> getFallback() {
            return Optional.empty();
        }

        @Override
        public /* synthetic */ Object getFallback() {
            return this.getFallback();
        }
    };

    @Override
    public MapCodec<? extends ChestBlock> getCodec() {
        return CODEC;
    }

    protected ChestBlock(Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityTypeSupplier, SoundEvent openSound, SoundEvent closeSound, AbstractBlock.Settings settings) {
        super(settings, blockEntityTypeSupplier);
        this.openSound = openSound;
        this.closeSound = closeSound;
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(CHEST_TYPE, ChestType.SINGLE)).with(WATERLOGGED, false));
    }

    public static DoubleBlockProperties.Type getDoubleBlockType(BlockState state) {
        ChestType lv = state.get(CHEST_TYPE);
        if (lv == ChestType.SINGLE) {
            return DoubleBlockProperties.Type.SINGLE;
        }
        if (lv == ChestType.RIGHT) {
            return DoubleBlockProperties.Type.FIRST;
        }
        return DoubleBlockProperties.Type.SECOND;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (this.canMergeWith(neighborState) && direction.getAxis().isHorizontal()) {
            ChestType lv = neighborState.get(CHEST_TYPE);
            if (state.get(CHEST_TYPE) == ChestType.SINGLE && lv != ChestType.SINGLE && state.get(FACING) == neighborState.get(FACING) && ChestBlock.getFacing(neighborState) == direction.getOpposite()) {
                return (BlockState)state.with(CHEST_TYPE, lv.getOpposite());
            }
        } else if (ChestBlock.getFacing(state) == direction) {
            return (BlockState)state.with(CHEST_TYPE, ChestType.SINGLE);
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    public boolean canMergeWith(BlockState state) {
        return state.isOf(this);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(CHEST_TYPE)) {
            default -> throw new MatchException(null, null);
            case ChestType.SINGLE -> SINGLE_SHAPE;
            case ChestType.LEFT, ChestType.RIGHT -> DOUBLE_SHAPES_BY_DIRECTION.get(ChestBlock.getFacing(state));
        };
    }

    public static Direction getFacing(BlockState state) {
        Direction lv = state.get(FACING);
        return state.get(CHEST_TYPE) == ChestType.LEFT ? lv.rotateYClockwise() : lv.rotateYCounterclockwise();
    }

    public static BlockPos getPosInFrontOf(BlockPos pos, BlockState state) {
        Direction lv = ChestBlock.getFacing(state);
        return pos.offset(lv);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction lv5;
        ChestType lv = ChestType.SINGLE;
        Direction lv2 = ctx.getHorizontalPlayerFacing().getOpposite();
        FluidState lv3 = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = ctx.shouldCancelInteraction();
        Direction lv4 = ctx.getSide();
        if (lv4.getAxis().isHorizontal() && bl && (lv5 = this.getNeighborChestDirection(ctx.getWorld(), ctx.getBlockPos(), lv4.getOpposite())) != null && lv5.getAxis() != lv4.getAxis()) {
            lv2 = lv5;
            ChestType chestType = lv = lv2.rotateYCounterclockwise() == lv4.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
        }
        if (lv == ChestType.SINGLE && !bl) {
            lv = this.getChestType(ctx.getWorld(), ctx.getBlockPos(), lv2);
        }
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, lv2)).with(CHEST_TYPE, lv)).with(WATERLOGGED, lv3.getFluid() == Fluids.WATER);
    }

    protected ChestType getChestType(World world, BlockPos pos, Direction facing) {
        if (facing == this.getNeighborChestDirection(world, pos, facing.rotateYClockwise())) {
            return ChestType.LEFT;
        }
        if (facing == this.getNeighborChestDirection(world, pos, facing.rotateYCounterclockwise())) {
            return ChestType.RIGHT;
        }
        return ChestType.SINGLE;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Nullable
    private Direction getNeighborChestDirection(World world, BlockPos pos, Direction neighborOffset) {
        BlockState lv = world.getBlockState(pos.offset(neighborOffset));
        return this.canMergeWith(lv) && lv.get(CHEST_TYPE) == ChestType.SINGLE ? lv.get(FACING) : null;
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            NamedScreenHandlerFactory lv2 = this.createScreenHandlerFactory(state, world, pos);
            if (lv2 != null) {
                player.openHandledScreen(lv2);
                player.incrementStat(this.getOpenStat());
                PiglinBrain.onGuardedBlockInteracted(lv, player, true);
            }
        }
        return ActionResult.SUCCESS;
    }

    protected Stat<Identifier> getOpenStat() {
        return Stats.CUSTOM.getOrCreateStat(Stats.OPEN_CHEST);
    }

    public BlockEntityType<? extends ChestBlockEntity> getExpectedEntityType() {
        return (BlockEntityType)this.entityTypeRetriever.get();
    }

    @Nullable
    public static Inventory getInventory(ChestBlock block, BlockState state, World world, BlockPos pos, boolean ignoreBlocked) {
        return block.getBlockEntitySource(state, world, pos, ignoreBlocked).apply(INVENTORY_RETRIEVER).orElse(null);
    }

    @Override
    public DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> getBlockEntitySource(BlockState state, World world, BlockPos pos, boolean ignoreBlocked) {
        BiPredicate<WorldAccess, BlockPos> biPredicate = ignoreBlocked ? (worldx, posx) -> false : ChestBlock::isChestBlocked;
        return DoubleBlockProperties.toPropertySource((BlockEntityType)this.entityTypeRetriever.get(), ChestBlock::getDoubleBlockType, ChestBlock::getFacing, FACING, state, world, pos, biPredicate);
    }

    @Override
    @Nullable
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return this.getBlockEntitySource(state, world, pos, false).apply(NAME_RETRIEVER).orElse(null);
    }

    public static DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Float2FloatFunction> getAnimationProgressRetriever(final LidOpenable progress) {
        return new DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Float2FloatFunction>(){

            @Override
            public Float2FloatFunction getFromBoth(ChestBlockEntity arg, ChestBlockEntity arg2) {
                return tickProgress -> Math.max(arg.getAnimationProgress(tickProgress), arg2.getAnimationProgress(tickProgress));
            }

            @Override
            public Float2FloatFunction getFrom(ChestBlockEntity arg) {
                return arg::getAnimationProgress;
            }

            @Override
            public Float2FloatFunction getFallback() {
                return progress::getAnimationProgress;
            }

            @Override
            public /* synthetic */ Object getFallback() {
                return this.getFallback();
            }
        };
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChestBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient() ? ChestBlock.validateTicker(type, this.getExpectedEntityType(), ChestBlockEntity::clientTick) : null;
    }

    public static boolean isChestBlocked(WorldAccess world, BlockPos pos) {
        return ChestBlock.hasBlockOnTop(world, pos) || ChestBlock.hasCatOnTop(world, pos);
    }

    private static boolean hasBlockOnTop(BlockView world, BlockPos pos) {
        BlockPos lv = pos.up();
        return world.getBlockState(lv).isSolidBlock(world, lv);
    }

    private static boolean hasCatOnTop(WorldAccess world, BlockPos pos) {
        List<CatEntity> list = world.getNonSpectatingEntities(CatEntity.class, new Box(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1));
        if (!list.isEmpty()) {
            for (CatEntity lv : list) {
                if (!lv.isInSittingPose()) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return ScreenHandler.calculateComparatorOutput(ChestBlock.getInventory(this, state, world, pos, false));
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
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, CHEST_TYPE, WATERLOGGED);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof ChestBlockEntity) {
            ((ChestBlockEntity)lv).onScheduledTick();
        }
    }

    public SoundEvent getOpenSound() {
        return this.openSound;
    }

    public SoundEvent getCloseSound() {
        return this.closeSound;
    }
}

