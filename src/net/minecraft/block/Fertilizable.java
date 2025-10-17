/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public interface Fertilizable {
    public boolean isFertilizable(WorldView var1, BlockPos var2, BlockState var3);

    public boolean canGrow(World var1, Random var2, BlockPos var3, BlockState var4);

    public void grow(ServerWorld var1, Random var2, BlockPos var3, BlockState var4);

    public static boolean canSpread(WorldView world, BlockPos pos, BlockState state) {
        return Fertilizable.findPosToSpreadTo(Direction.Type.HORIZONTAL.stream().toList(), world, pos, state).isPresent();
    }

    public static Optional<BlockPos> findPosToSpreadTo(World world, BlockPos pos, BlockState state) {
        return Fertilizable.findPosToSpreadTo(Direction.Type.HORIZONTAL.getShuffled(world.random), world, pos, state);
    }

    private static Optional<BlockPos> findPosToSpreadTo(List<Direction> directions, WorldView world, BlockPos pos, BlockState state) {
        for (Direction lv : directions) {
            BlockPos lv2 = pos.offset(lv);
            if (!world.isAir(lv2) || !state.canPlaceAt(world, lv2)) continue;
            return Optional.of(lv2);
        }
        return Optional.empty();
    }

    default public BlockPos getFertilizeParticlePos(BlockPos pos) {
        return switch (this.getFertilizableType().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> pos.up();
            case 1 -> pos;
        };
    }

    default public FertilizableType getFertilizableType() {
        return FertilizableType.GROWER;
    }

    public static enum FertilizableType {
        NEIGHBOR_SPREADER,
        GROWER;

    }
}

