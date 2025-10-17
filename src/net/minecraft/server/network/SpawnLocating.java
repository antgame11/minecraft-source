/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.CollisionView;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class SpawnLocating {
    private static final EntityDimensions PLAYER_DIMENSIONS = EntityType.PLAYER.getDimensions();
    private static final int MAX_SPAWN_AREA = 1024;
    private final ServerWorld world;
    private final BlockPos spawnPos;
    private final int spawnRadius;
    private final int spawnArea;
    private final int shiftAmount;
    private final int offset;
    private int attempt;
    private final CompletableFuture<Vec3d> future = new CompletableFuture();

    private SpawnLocating(ServerWorld world, BlockPos spawnPos, int spawnRadius) {
        this.world = world;
        this.spawnPos = spawnPos;
        this.spawnRadius = spawnRadius;
        long l = (long)spawnRadius * 2L + 1L;
        this.spawnArea = (int)Math.min(1024L, l * l);
        this.shiftAmount = SpawnLocating.calculateShiftAmount(this.spawnArea);
        this.offset = Random.create().nextInt(this.spawnArea);
    }

    public static CompletableFuture<Vec3d> locateSpawnPos(ServerWorld world, BlockPos spawnPos) {
        if (!world.getDimension().hasSkyLight() || world.getServer().getSaveProperties().getGameMode() == GameMode.ADVENTURE) {
            return CompletableFuture.completedFuture(SpawnLocating.findPosInColumn(world, spawnPos));
        }
        int i = Math.max(0, world.getGameRules().getInt(GameRules.SPAWN_RADIUS));
        int j = MathHelper.floor(world.getWorldBorder().getDistanceInsideBorder(spawnPos.getX(), spawnPos.getZ()));
        if (j < i) {
            i = j;
        }
        if (j <= 1) {
            i = 1;
        }
        SpawnLocating lv = new SpawnLocating(world, spawnPos, i);
        lv.scheduleNextSearch();
        return lv.future;
    }

    private void scheduleNextSearch() {
        int i;
        if ((i = this.attempt++) < this.spawnArea) {
            int j = (this.offset + this.shiftAmount * i) % this.spawnArea;
            int k = j % (this.spawnRadius * 2 + 1);
            int l = j / (this.spawnRadius * 2 + 1);
            int m = this.spawnPos.getX() + k - this.spawnRadius;
            int n = this.spawnPos.getZ() + l - this.spawnRadius;
            this.scheduleSearch(m, n, i, () -> {
                BlockPos lv = SpawnLocating.findOverworldSpawn(this.world, m, n);
                if (lv != null && SpawnLocating.isSpaceEmpty(this.world, lv)) {
                    return Optional.of(Vec3d.ofBottomCenter(lv));
                }
                return Optional.empty();
            });
        } else {
            this.scheduleSearch(this.spawnPos.getX(), this.spawnPos.getZ(), i, () -> Optional.of(SpawnLocating.findPosInColumn(this.world, this.spawnPos)));
        }
    }

    private static Vec3d findPosInColumn(CollisionView world, BlockPos pos) {
        BlockPos.Mutable lv = pos.mutableCopy();
        while (!SpawnLocating.isSpaceEmpty(world, lv) && lv.getY() < world.getTopYInclusive()) {
            lv.move(Direction.UP);
        }
        lv.move(Direction.DOWN);
        while (SpawnLocating.isSpaceEmpty(world, lv) && lv.getY() > world.getBottomY()) {
            lv.move(Direction.DOWN);
        }
        lv.move(Direction.UP);
        return Vec3d.ofBottomCenter(lv);
    }

    private static boolean isSpaceEmpty(CollisionView world, BlockPos pos) {
        return world.isSpaceEmpty(null, PLAYER_DIMENSIONS.getBoxAt(pos.toBottomCenterPos()), true);
    }

    private static int calculateShiftAmount(int spawnArea) {
        return spawnArea <= 16 ? spawnArea - 1 : 17;
    }

    private void scheduleSearch(int x, int z, int index, Supplier<Optional<Vec3d>> spawnFinder) {
        if (this.future.isDone()) {
            return;
        }
        int l = ChunkSectionPos.getSectionCoord(x);
        int m = ChunkSectionPos.getSectionCoord(z);
        this.world.getChunkManager().addChunkLoadingTicket(ChunkTicketType.SPAWN_SEARCH, new ChunkPos(l, m), 0).whenCompleteAsync((object, throwable) -> {
            if (throwable == null) {
                try {
                    Optional optional = (Optional)spawnFinder.get();
                    if (optional.isPresent()) {
                        this.future.complete((Vec3d)optional.get());
                    } else {
                        this.scheduleNextSearch();
                    }
                } catch (Exception exception) {
                    throwable = exception;
                }
            }
            if (throwable != null) {
                CrashReport lv = CrashReport.create(throwable, "Searching for spawn");
                CrashReportSection lv2 = lv.addElement("Spawn Lookup");
                lv2.add("Origin", this.spawnPos::toString);
                lv2.add("Radius", () -> Integer.toString(this.spawnRadius));
                lv2.add("Candidate", () -> "[" + x + "," + z + "]");
                lv2.add("Progress", () -> index + " out of " + this.spawnArea);
                this.future.completeExceptionally(new CrashException(lv));
            }
        }, (Executor)this.world.getServer());
    }

    @Nullable
    protected static BlockPos findOverworldSpawn(ServerWorld world, int x, int z) {
        int k;
        boolean bl = world.getDimension().hasCeiling();
        WorldChunk lv = world.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
        int n = k = bl ? world.getChunkManager().getChunkGenerator().getSpawnHeight(world) : lv.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, x & 0xF, z & 0xF);
        if (k < world.getBottomY()) {
            return null;
        }
        int l = lv.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x & 0xF, z & 0xF);
        if (l <= k && l > lv.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR, x & 0xF, z & 0xF)) {
            return null;
        }
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        for (int m = k + 1; m >= world.getBottomY(); --m) {
            lv2.set(x, m, z);
            BlockState lv3 = world.getBlockState(lv2);
            if (!lv3.getFluidState().isEmpty()) break;
            if (!Block.isFaceFullSquare(lv3.getCollisionShape(world, lv2), Direction.UP)) continue;
            return ((BlockPos)lv2.up()).toImmutable();
        }
        return null;
    }

    @Nullable
    public static BlockPos findServerSpawnPoint(ServerWorld world, ChunkPos chunkPos) {
        if (SharedConstants.isOutsideGenerationArea(chunkPos)) {
            return null;
        }
        for (int i = chunkPos.getStartX(); i <= chunkPos.getEndX(); ++i) {
            for (int j = chunkPos.getStartZ(); j <= chunkPos.getEndZ(); ++j) {
                BlockPos lv = SpawnLocating.findOverworldSpawn(world, i, j);
                if (lv == null) continue;
                return lv;
            }
        }
        return null;
    }
}

