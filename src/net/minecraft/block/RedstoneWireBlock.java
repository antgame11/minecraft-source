/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.DefaultRedstoneController;
import net.minecraft.world.ExperimentalRedstoneController;
import net.minecraft.world.RedstoneController;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.OrientationHelper;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class RedstoneWireBlock
extends Block {
    public static final MapCodec<RedstoneWireBlock> CODEC = RedstoneWireBlock.createCodec(RedstoneWireBlock::new);
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_NORTH = Properties.NORTH_WIRE_CONNECTION;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_EAST = Properties.EAST_WIRE_CONNECTION;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_SOUTH = Properties.SOUTH_WIRE_CONNECTION;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_WEST = Properties.WEST_WIRE_CONNECTION;
    public static final IntProperty POWER = Properties.POWER;
    public static final Map<Direction, EnumProperty<WireConnection>> DIRECTION_TO_WIRE_CONNECTION_PROPERTY = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(Direction.NORTH, WIRE_CONNECTION_NORTH, Direction.EAST, WIRE_CONNECTION_EAST, Direction.SOUTH, WIRE_CONNECTION_SOUTH, Direction.WEST, WIRE_CONNECTION_WEST)));
    private static final int[] COLORS = Util.make(new int[16], colors -> {
        for (int i = 0; i <= 15; ++i) {
            float f;
            float g = f * 0.6f + ((f = (float)i / 15.0f) > 0.0f ? 0.4f : 0.3f);
            float h = MathHelper.clamp(f * f * 0.7f - 0.5f, 0.0f, 1.0f);
            float j = MathHelper.clamp(f * f * 0.6f - 0.7f, 0.0f, 1.0f);
            colors[i] = ColorHelper.fromFloats(1.0f, g, h, j);
        }
    });
    private static final float field_31221 = 0.2f;
    private final Function<BlockState, VoxelShape> shapeFunction;
    private final BlockState dotState;
    private final RedstoneController redstoneController = new DefaultRedstoneController(this);
    private boolean wiresGivePower = true;

    public MapCodec<RedstoneWireBlock> getCodec() {
        return CODEC;
    }

    public RedstoneWireBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(WIRE_CONNECTION_NORTH, WireConnection.NONE)).with(WIRE_CONNECTION_EAST, WireConnection.NONE)).with(WIRE_CONNECTION_SOUTH, WireConnection.NONE)).with(WIRE_CONNECTION_WEST, WireConnection.NONE)).with(POWER, 0));
        this.shapeFunction = this.createShapeFunction();
        this.dotState = (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(WIRE_CONNECTION_NORTH, WireConnection.SIDE)).with(WIRE_CONNECTION_EAST, WireConnection.SIDE)).with(WIRE_CONNECTION_SOUTH, WireConnection.SIDE)).with(WIRE_CONNECTION_WEST, WireConnection.SIDE);
    }

    private Function<BlockState, VoxelShape> createShapeFunction() {
        boolean i = true;
        int j = 10;
        VoxelShape lv = Block.createColumnShape(10.0, 0.0, 1.0);
        Map<Direction, VoxelShape> map = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidZShape(10.0, 0.0, 1.0, 0.0, 8.0));
        Map<Direction, VoxelShape> map2 = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidZShape(10.0, 16.0, 0.0, 1.0));
        return this.createShapeFunction(state -> {
            VoxelShape lv = lv;
            for (Map.Entry<Direction, EnumProperty<WireConnection>> entry : DIRECTION_TO_WIRE_CONNECTION_PROPERTY.entrySet()) {
                lv = switch ((WireConnection)state.get(entry.getValue())) {
                    default -> throw new MatchException(null, null);
                    case WireConnection.UP -> VoxelShapes.union(lv, (VoxelShape)map.get(entry.getKey()), (VoxelShape)map2.get(entry.getKey()));
                    case WireConnection.SIDE -> VoxelShapes.union(lv, (VoxelShape)map.get(entry.getKey()));
                    case WireConnection.NONE -> lv;
                };
            }
            return lv;
        }, POWER);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getPlacementState(ctx.getWorld(), this.dotState, ctx.getBlockPos());
    }

    private BlockState getPlacementState(BlockView world, BlockState state, BlockPos pos) {
        boolean bl7;
        boolean bl = RedstoneWireBlock.isNotConnected(state);
        state = this.getDefaultWireState(world, (BlockState)this.getDefaultState().with(POWER, state.get(POWER)), pos);
        if (bl && RedstoneWireBlock.isNotConnected(state)) {
            return state;
        }
        boolean bl2 = state.get(WIRE_CONNECTION_NORTH).isConnected();
        boolean bl3 = state.get(WIRE_CONNECTION_SOUTH).isConnected();
        boolean bl4 = state.get(WIRE_CONNECTION_EAST).isConnected();
        boolean bl5 = state.get(WIRE_CONNECTION_WEST).isConnected();
        boolean bl6 = !bl2 && !bl3;
        boolean bl8 = bl7 = !bl4 && !bl5;
        if (!bl5 && bl6) {
            state = (BlockState)state.with(WIRE_CONNECTION_WEST, WireConnection.SIDE);
        }
        if (!bl4 && bl6) {
            state = (BlockState)state.with(WIRE_CONNECTION_EAST, WireConnection.SIDE);
        }
        if (!bl2 && bl7) {
            state = (BlockState)state.with(WIRE_CONNECTION_NORTH, WireConnection.SIDE);
        }
        if (!bl3 && bl7) {
            state = (BlockState)state.with(WIRE_CONNECTION_SOUTH, WireConnection.SIDE);
        }
        return state;
    }

    private BlockState getDefaultWireState(BlockView world, BlockState state, BlockPos pos) {
        boolean bl = !world.getBlockState(pos.up()).isSolidBlock(world, pos);
        for (Direction lv : Direction.Type.HORIZONTAL) {
            if (((WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(lv))).isConnected()) continue;
            WireConnection lv2 = this.getRenderConnectionType(world, pos, lv, bl);
            state = (BlockState)state.with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(lv), lv2);
        }
        return state;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction == Direction.DOWN) {
            if (!this.canRunOnTop(world, neighborPos, neighborState)) {
                return Blocks.AIR.getDefaultState();
            }
            return state;
        }
        if (direction == Direction.UP) {
            return this.getPlacementState(world, state, pos);
        }
        WireConnection lv = this.getRenderConnectionType(world, pos, direction);
        if (lv.isConnected() == ((WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction))).isConnected() && !RedstoneWireBlock.isFullyConnected(state)) {
            return (BlockState)state.with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), lv);
        }
        return this.getPlacementState(world, (BlockState)((BlockState)this.dotState.with(POWER, state.get(POWER))).with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), lv), pos);
    }

    private static boolean isFullyConnected(BlockState state) {
        return state.get(WIRE_CONNECTION_NORTH).isConnected() && state.get(WIRE_CONNECTION_SOUTH).isConnected() && state.get(WIRE_CONNECTION_EAST).isConnected() && state.get(WIRE_CONNECTION_WEST).isConnected();
    }

    private static boolean isNotConnected(BlockState state) {
        return !state.get(WIRE_CONNECTION_NORTH).isConnected() && !state.get(WIRE_CONNECTION_SOUTH).isConnected() && !state.get(WIRE_CONNECTION_EAST).isConnected() && !state.get(WIRE_CONNECTION_WEST).isConnected();
    }

    @Override
    protected void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (Direction lv2 : Direction.Type.HORIZONTAL) {
            WireConnection lv3 = (WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(lv2));
            if (lv3 == WireConnection.NONE || world.getBlockState(lv.set((Vec3i)pos, lv2)).isOf(this)) continue;
            lv.move(Direction.DOWN);
            BlockState lv4 = world.getBlockState(lv);
            if (lv4.isOf(this)) {
                Vec3i lv5 = lv.offset(lv2.getOpposite());
                world.replaceWithStateForNeighborUpdate(lv2.getOpposite(), lv, (BlockPos)lv5, world.getBlockState((BlockPos)lv5), flags, maxUpdateDepth);
            }
            lv.set((Vec3i)pos, lv2).move(Direction.UP);
            BlockState lv6 = world.getBlockState(lv);
            if (!lv6.isOf(this)) continue;
            Vec3i lv7 = lv.offset(lv2.getOpposite());
            world.replaceWithStateForNeighborUpdate(lv2.getOpposite(), lv, (BlockPos)lv7, world.getBlockState((BlockPos)lv7), flags, maxUpdateDepth);
        }
    }

    private WireConnection getRenderConnectionType(BlockView world, BlockPos pos, Direction direction) {
        return this.getRenderConnectionType(world, pos, direction, !world.getBlockState(pos.up()).isSolidBlock(world, pos));
    }

    private WireConnection getRenderConnectionType(BlockView world, BlockPos pos, Direction direction, boolean bl) {
        BlockPos lv = pos.offset(direction);
        BlockState lv2 = world.getBlockState(lv);
        if (bl) {
            boolean bl2;
            boolean bl3 = bl2 = lv2.getBlock() instanceof TrapdoorBlock || this.canRunOnTop(world, lv, lv2);
            if (bl2 && RedstoneWireBlock.connectsTo(world.getBlockState(lv.up()))) {
                if (lv2.isSideSolidFullSquare(world, lv, direction.getOpposite())) {
                    return WireConnection.UP;
                }
                return WireConnection.SIDE;
            }
        }
        if (RedstoneWireBlock.connectsTo(lv2, direction) || !lv2.isSolidBlock(world, lv) && RedstoneWireBlock.connectsTo(world.getBlockState(lv.down()))) {
            return WireConnection.SIDE;
        }
        return WireConnection.NONE;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        return this.canRunOnTop(world, lv, lv2);
    }

    private boolean canRunOnTop(BlockView world, BlockPos pos, BlockState floor) {
        return floor.isSideSolidFullSquare(world, pos, Direction.UP) || floor.isOf(Blocks.HOPPER);
    }

    private void update(World world, BlockPos pos, BlockState state, @Nullable WireOrientation orientation, boolean blockAdded) {
        if (RedstoneWireBlock.areRedstoneExperimentsEnabled(world)) {
            new ExperimentalRedstoneController(this).update(world, pos, state, orientation, blockAdded);
        } else {
            this.redstoneController.update(world, pos, state, orientation, blockAdded);
        }
    }

    public int getStrongPower(World world, BlockPos pos) {
        this.wiresGivePower = false;
        int i = world.getReceivedRedstonePower(pos);
        this.wiresGivePower = true;
        return i;
    }

    private void updateNeighbors(World world, BlockPos pos) {
        if (!world.getBlockState(pos).isOf(this)) {
            return;
        }
        world.updateNeighbors(pos, this);
        for (Direction lv : Direction.values()) {
            world.updateNeighbors(pos.offset(lv), this);
        }
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock()) || world.isClient()) {
            return;
        }
        this.update(world, pos, state, null, true);
        for (Direction lv : Direction.Type.VERTICAL) {
            world.updateNeighbors(pos.offset(lv), this);
        }
        this.updateOffsetNeighbors(world, pos);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (moved) {
            return;
        }
        for (Direction lv : Direction.values()) {
            world.updateNeighbors(pos.offset(lv), this);
        }
        this.update(world, pos, state, null, false);
        this.updateOffsetNeighbors(world, pos);
    }

    private void updateOffsetNeighbors(World world, BlockPos pos) {
        for (Direction lv : Direction.Type.HORIZONTAL) {
            this.updateNeighbors(world, pos.offset(lv));
        }
        for (Direction lv : Direction.Type.HORIZONTAL) {
            BlockPos lv2 = pos.offset(lv);
            if (world.getBlockState(lv2).isSolidBlock(world, lv2)) {
                this.updateNeighbors(world, lv2.up());
                continue;
            }
            this.updateNeighbors(world, lv2.down());
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        if (world.isClient()) {
            return;
        }
        if (sourceBlock == this && RedstoneWireBlock.areRedstoneExperimentsEnabled(world)) {
            return;
        }
        if (state.canPlaceAt(world, pos)) {
            this.update(world, pos, state, wireOrientation, false);
        } else {
            RedstoneWireBlock.dropStacks(state, world, pos);
            world.removeBlock(pos, false);
        }
    }

    private static boolean areRedstoneExperimentsEnabled(World world) {
        return world.getEnabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS);
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!this.wiresGivePower) {
            return 0;
        }
        return state.getWeakRedstonePower(world, pos, direction);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!this.wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        int i = state.get(POWER);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((WireConnection)this.getPlacementState(world, state, pos).get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    protected static boolean connectsTo(BlockState state) {
        return RedstoneWireBlock.connectsTo(state, null);
    }

    protected static boolean connectsTo(BlockState state, @Nullable Direction dir) {
        if (state.isOf(Blocks.REDSTONE_WIRE)) {
            return true;
        }
        if (state.isOf(Blocks.REPEATER)) {
            Direction lv = (Direction)state.get(RepeaterBlock.FACING);
            return lv == dir || lv.getOpposite() == dir;
        }
        if (state.isOf(Blocks.OBSERVER)) {
            return dir == state.get(ObserverBlock.FACING);
        }
        return state.emitsRedstonePower() && dir != null;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return this.wiresGivePower;
    }

    public static int getWireColor(int powerLevel) {
        return COLORS[powerLevel];
    }

    private static void addPoweredParticles(World world, Random random, BlockPos pos, int color, Direction perpendicular, Direction direction, float minOffset, float maxOffset) {
        float h = maxOffset - minOffset;
        if (random.nextFloat() >= 0.2f * h) {
            return;
        }
        float j = 0.4375f;
        float k = minOffset + h * random.nextFloat();
        double d = 0.5 + (double)(0.4375f * (float)perpendicular.getOffsetX()) + (double)(k * (float)direction.getOffsetX());
        double e = 0.5 + (double)(0.4375f * (float)perpendicular.getOffsetY()) + (double)(k * (float)direction.getOffsetY());
        double l = 0.5 + (double)(0.4375f * (float)perpendicular.getOffsetZ()) + (double)(k * (float)direction.getOffsetZ());
        world.addParticleClient(new DustParticleEffect(color, 1.0f), (double)pos.getX() + d, (double)pos.getY() + e, (double)pos.getZ() + l, 0.0, 0.0, 0.0);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        int i = state.get(POWER);
        if (i == 0) {
            return;
        }
        block4: for (Direction lv : Direction.Type.HORIZONTAL) {
            WireConnection lv2 = (WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(lv));
            switch (lv2) {
                case UP: {
                    RedstoneWireBlock.addPoweredParticles(world, random, pos, COLORS[i], lv, Direction.UP, -0.5f, 0.5f);
                }
                case SIDE: {
                    RedstoneWireBlock.addPoweredParticles(world, random, pos, COLORS[i], Direction.DOWN, lv, 0.0f, 0.5f);
                    continue block4;
                }
            }
            RedstoneWireBlock.addPoweredParticles(world, random, pos, COLORS[i], Direction.DOWN, lv, 0.0f, 0.3f);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_SOUTH))).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_WEST))).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_NORTH))).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_EAST));
            }
            case COUNTERCLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_EAST))).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_SOUTH))).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_WEST))).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_NORTH));
            }
            case CLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_WEST))).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_NORTH))).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_EAST))).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_SOUTH));
            }
        }
        return state;
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT: {
                return (BlockState)((BlockState)state.with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_SOUTH))).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_NORTH));
            }
            case FRONT_BACK: {
                return (BlockState)((BlockState)state.with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_WEST))).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_EAST));
            }
        }
        return super.mirror(state, mirror);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WIRE_CONNECTION_NORTH, WIRE_CONNECTION_EAST, WIRE_CONNECTION_SOUTH, WIRE_CONNECTION_WEST, POWER);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        }
        if (RedstoneWireBlock.isFullyConnected(state) || RedstoneWireBlock.isNotConnected(state)) {
            BlockState lv = RedstoneWireBlock.isFullyConnected(state) ? this.getDefaultState() : this.dotState;
            lv = (BlockState)lv.with(POWER, state.get(POWER));
            if ((lv = this.getPlacementState(world, lv, pos)) != state) {
                world.setBlockState(pos, lv, Block.NOTIFY_ALL);
                this.updateForNewState(world, pos, state, lv);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    private void updateForNewState(World world, BlockPos pos, BlockState oldState, BlockState newState) {
        WireOrientation lv = OrientationHelper.getEmissionOrientation(world, null, Direction.UP);
        for (Direction lv2 : Direction.Type.HORIZONTAL) {
            BlockPos lv3 = pos.offset(lv2);
            if (((WireConnection)oldState.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(lv2))).isConnected() == ((WireConnection)newState.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(lv2))).isConnected() || !world.getBlockState(lv3).isSolidBlock(world, lv3)) continue;
            world.updateNeighborsExcept(lv3, newState.getBlock(), lv2.getOpposite(), OrientationHelper.withFrontNullable(lv, lv2));
        }
    }
}

