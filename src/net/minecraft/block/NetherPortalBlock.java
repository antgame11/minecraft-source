/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Portal;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.NetherPortal;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NetherPortalBlock
extends Block
implements Portal {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<NetherPortalBlock> CODEC = NetherPortalBlock.createCodec(NetherPortalBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
    private static final Map<Direction.Axis, VoxelShape> SHAPES_BY_AXIS = VoxelShapes.createHorizontalAxisShapeMap(Block.createColumnShape(4.0, 16.0, 0.0, 16.0));

    public MapCodec<NetherPortalBlock> getCodec() {
        return CODEC;
    }

    public NetherPortalBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AXIS, Direction.Axis.X));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES_BY_AXIS.get(state.get(AXIS));
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getDimension().natural() && world.method_74962() && random.nextInt(2000) < world.getDifficulty().getId() && world.shouldTickBlockAt(pos)) {
            ZombifiedPiglinEntity lv;
            while (world.getBlockState(pos).isOf(this)) {
                pos = pos.down();
            }
            if (world.getBlockState(pos).allowsSpawning(world, pos, EntityType.ZOMBIFIED_PIGLIN) && (lv = EntityType.ZOMBIFIED_PIGLIN.spawn(world, pos.up(), SpawnReason.STRUCTURE)) != null) {
                lv.resetPortalCooldown();
                Entity lv2 = lv.getVehicle();
                if (lv2 != null) {
                    lv2.resetPortalCooldown();
                }
            }
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        boolean bl;
        Direction.Axis lv = direction.getAxis();
        Direction.Axis lv2 = state.get(AXIS);
        boolean bl2 = bl = lv2 != lv && lv.isHorizontal();
        if (bl || neighborState.isOf(this) || NetherPortal.getOnAxis(world, pos, lv2).wasAlreadyValid()) {
            return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (entity.canUsePortals(false)) {
            entity.tryUsePortal(this, pos);
        }
    }

    @Override
    public int getPortalDelay(ServerWorld world, Entity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)entity;
            return Math.max(0, world.getGameRules().getInt(lv.getAbilities().invulnerable ? GameRules.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY : GameRules.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY));
        }
        return 0;
    }

    @Override
    @Nullable
    public TeleportTarget createTeleportTarget(ServerWorld world, Entity entity, BlockPos pos) {
        RegistryKey<World> lv = world.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER;
        ServerWorld lv2 = world.getServer().getWorld(lv);
        if (lv2 == null) {
            return null;
        }
        boolean bl = lv2.getRegistryKey() == World.NETHER;
        WorldBorder lv3 = lv2.getWorldBorder();
        double d = DimensionType.getCoordinateScaleFactor(world.getDimension(), lv2.getDimension());
        BlockPos lv4 = lv3.clampFloored(entity.getX() * d, entity.getY(), entity.getZ() * d);
        return this.getOrCreateExitPortalTarget(lv2, entity, pos, lv4, bl, lv3);
    }

    @Nullable
    private TeleportTarget getOrCreateExitPortalTarget(ServerWorld world, Entity entity2, BlockPos pos, BlockPos scaledPos, boolean inNether, WorldBorder worldBorder) {
        TeleportTarget.PostDimensionTransition lv4;
        BlockLocating.Rectangle lv3;
        Optional<BlockPos> optional = world.getPortalForcer().getPortalPos(scaledPos, inNether, worldBorder);
        if (optional.isPresent()) {
            BlockPos lv = optional.get();
            BlockState lv2 = world.getBlockState(lv);
            lv3 = BlockLocating.getLargestRectangle(lv, lv2.get(Properties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, posx -> world.getBlockState((BlockPos)posx) == lv2);
            lv4 = TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(entity -> entity.addPortalChunkTicketAt(lv));
        } else {
            Direction.Axis lv5 = entity2.getEntityWorld().getBlockState(pos).getOrEmpty(AXIS).orElse(Direction.Axis.X);
            Optional<BlockLocating.Rectangle> optional2 = world.getPortalForcer().createPortal(scaledPos, lv5);
            if (optional2.isEmpty()) {
                LOGGER.error("Unable to create a portal, likely target out of worldborder");
                return null;
            }
            lv3 = optional2.get();
            lv4 = TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(TeleportTarget.ADD_PORTAL_CHUNK_TICKET);
        }
        return NetherPortalBlock.getExitPortalTarget(entity2, pos, lv3, world, lv4);
    }

    private static TeleportTarget getExitPortalTarget(Entity entity, BlockPos pos, BlockLocating.Rectangle exitPortalRectangle, ServerWorld world, TeleportTarget.PostDimensionTransition postDimensionTransition) {
        Vec3d lv4;
        Direction.Axis lv2;
        BlockState lv = entity.getEntityWorld().getBlockState(pos);
        if (lv.contains(Properties.HORIZONTAL_AXIS)) {
            lv2 = lv.get(Properties.HORIZONTAL_AXIS);
            BlockLocating.Rectangle lv3 = BlockLocating.getLargestRectangle(pos, lv2, 21, Direction.Axis.Y, 21, posx -> entity.getEntityWorld().getBlockState((BlockPos)posx) == lv);
            lv4 = entity.positionInPortal(lv2, lv3);
        } else {
            lv2 = Direction.Axis.X;
            lv4 = new Vec3d(0.5, 0.0, 0.0);
        }
        return NetherPortalBlock.getExitPortalTarget(world, exitPortalRectangle, lv2, lv4, entity, postDimensionTransition);
    }

    private static TeleportTarget getExitPortalTarget(ServerWorld world, BlockLocating.Rectangle exitPortalRectangle, Direction.Axis axis, Vec3d positionInPortal, Entity entity, TeleportTarget.PostDimensionTransition postDimensionTransition) {
        BlockPos lv = exitPortalRectangle.lowerLeft;
        BlockState lv2 = world.getBlockState(lv);
        Direction.Axis lv3 = lv2.getOrEmpty(Properties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
        double d = exitPortalRectangle.width;
        double e = exitPortalRectangle.height;
        EntityDimensions lv4 = entity.getDimensions(entity.getPose());
        int i = axis == lv3 ? 0 : 90;
        double f = (double)lv4.width() / 2.0 + (d - (double)lv4.width()) * positionInPortal.getX();
        double g = (e - (double)lv4.height()) * positionInPortal.getY();
        double h = 0.5 + positionInPortal.getZ();
        boolean bl = lv3 == Direction.Axis.X;
        Vec3d lv5 = new Vec3d((double)lv.getX() + (bl ? f : h), (double)lv.getY() + g, (double)lv.getZ() + (bl ? h : f));
        Vec3d lv6 = NetherPortal.findOpenPosition(lv5, world, entity, lv4);
        return new TeleportTarget(world, lv6, Vec3d.ZERO, i, 0.0f, PositionFlag.combine(PositionFlag.DELTA, PositionFlag.ROT), postDimensionTransition);
    }

    @Override
    public Portal.Effect getPortalEffect() {
        return Portal.Effect.CONFUSION;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(100) == 0) {
            world.playSoundClient((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5f, random.nextFloat() * 0.4f + 0.8f, false);
        }
        for (int i = 0; i < 4; ++i) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() + random.nextDouble();
            double f = (double)pos.getZ() + random.nextDouble();
            double g = ((double)random.nextFloat() - 0.5) * 0.5;
            double h = ((double)random.nextFloat() - 0.5) * 0.5;
            double j = ((double)random.nextFloat() - 0.5) * 0.5;
            int k = random.nextInt(2) * 2 - 1;
            if (world.getBlockState(pos.west()).isOf(this) || world.getBlockState(pos.east()).isOf(this)) {
                f = (double)pos.getZ() + 0.5 + 0.25 * (double)k;
                j = random.nextFloat() * 2.0f * (float)k;
            } else {
                d = (double)pos.getX() + 0.5 + 0.25 * (double)k;
                g = random.nextFloat() * 2.0f * (float)k;
            }
            world.addParticleClient(ParticleTypes.PORTAL, d, e, f, g, h, j);
        }
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                switch (state.get(AXIS)) {
                    case X: {
                        return (BlockState)state.with(AXIS, Direction.Axis.Z);
                    }
                    case Z: {
                        return (BlockState)state.with(AXIS, Direction.Axis.X);
                    }
                }
                return state;
            }
        }
        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
}

