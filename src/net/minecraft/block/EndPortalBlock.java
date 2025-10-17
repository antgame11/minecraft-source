/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Portal;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.EndPlatformFeature;
import org.jetbrains.annotations.Nullable;

public class EndPortalBlock
extends BlockWithEntity
implements Portal {
    public static final MapCodec<EndPortalBlock> CODEC = EndPortalBlock.createCodec(EndPortalBlock::new);
    private static final VoxelShape SHAPE = Block.createColumnShape(16.0, 6.0, 12.0);

    public MapCodec<EndPortalBlock> getCodec() {
        return CODEC;
    }

    protected EndPortalBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EndPortalBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getInsideCollisionShape(BlockState state, BlockView world, BlockPos pos, Entity entity) {
        return state.getOutlineShape(world, pos);
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (!entity.canUsePortals(false)) return;
        if (!world.isClient() && world.getRegistryKey() == World.END && entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            if (!lv.seenCredits) {
                lv.detachForDimensionChange();
                return;
            }
        }
        entity.tryUsePortal(this, pos);
    }

    @Override
    @Nullable
    public TeleportTarget createTeleportTarget(ServerWorld world, Entity entity, BlockPos pos) {
        Set<PositionFlag> set;
        float g;
        float f;
        WorldProperties.SpawnPoint lv = world.getSpawnPoint();
        RegistryKey<World> lv2 = world.getRegistryKey();
        boolean bl = lv2 == World.END;
        RegistryKey<World> lv3 = bl ? lv.getDimension() : World.END;
        BlockPos lv4 = bl ? lv.getPos() : ServerWorld.END_SPAWN_POS;
        ServerWorld lv5 = world.getServer().getWorld(lv3);
        if (lv5 == null) {
            return null;
        }
        Vec3d lv6 = lv4.toBottomCenterPos();
        if (!bl) {
            EndPlatformFeature.generate(lv5, BlockPos.ofFloored(lv6).down(), true);
            f = Direction.WEST.getPositiveHorizontalDegrees();
            g = 0.0f;
            set = PositionFlag.combine(PositionFlag.DELTA, Set.of(PositionFlag.X_ROT));
            if (entity instanceof ServerPlayerEntity) {
                lv6 = lv6.subtract(0.0, 1.0, 0.0);
            }
        } else {
            f = lv.yaw();
            g = lv.pitch();
            set = PositionFlag.combine(PositionFlag.DELTA, PositionFlag.ROT);
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv7 = (ServerPlayerEntity)entity;
                return lv7.getRespawnTarget(false, TeleportTarget.NO_OP);
            }
            lv6 = entity.getWorldSpawnPos(lv5, lv4).toBottomCenterPos();
        }
        return new TeleportTarget(lv5, lv6, Vec3d.ZERO, f, g, set, TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(TeleportTarget.ADD_PORTAL_CHUNK_TICKET));
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        double d = (double)pos.getX() + random.nextDouble();
        double e = (double)pos.getY() + 0.8;
        double f = (double)pos.getZ() + random.nextDouble();
        world.addParticleClient(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canBucketPlace(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }
}

