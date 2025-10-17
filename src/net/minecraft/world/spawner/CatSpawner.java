/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.spawner;

import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.spawner.SpecialSpawner;

public class CatSpawner
implements SpecialSpawner {
    private static final int SPAWN_INTERVAL = 1200;
    private int cooldown;

    @Override
    public void spawn(ServerWorld world, boolean spawnMonsters) {
        --this.cooldown;
        if (this.cooldown > 0) {
            return;
        }
        this.cooldown = 1200;
        ServerPlayerEntity lv = world.getRandomAlivePlayer();
        if (lv == null) {
            return;
        }
        Random lv2 = world.random;
        int i = (8 + lv2.nextInt(24)) * (lv2.nextBoolean() ? -1 : 1);
        int j = (8 + lv2.nextInt(24)) * (lv2.nextBoolean() ? -1 : 1);
        BlockPos lv3 = lv.getBlockPos().add(i, 0, j);
        int k = 10;
        if (!world.isRegionLoaded(lv3.getX() - 10, lv3.getZ() - 10, lv3.getX() + 10, lv3.getZ() + 10)) {
            return;
        }
        if (SpawnRestriction.isSpawnPosAllowed(EntityType.CAT, world, lv3)) {
            if (world.isNearOccupiedPointOfInterest(lv3, 2)) {
                this.spawnInHouse(world, lv3);
            } else if (world.getStructureAccessor().getStructureContaining(lv3, StructureTags.CATS_SPAWN_IN).hasChildren()) {
                this.spawnInStructure(world, lv3);
            }
        }
    }

    private void spawnInHouse(ServerWorld world, BlockPos pos) {
        List<CatEntity> list;
        int i = 48;
        if (world.getPointOfInterestStorage().count(entry -> entry.matchesKey(PointOfInterestTypes.HOME), pos, 48, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED) > 4L && (list = world.getNonSpectatingEntities(CatEntity.class, new Box(pos).expand(48.0, 8.0, 48.0))).size() < 5) {
            this.spawn(pos, world, false);
        }
    }

    private void spawnInStructure(ServerWorld world, BlockPos pos) {
        int i = 16;
        List<CatEntity> list = world.getNonSpectatingEntities(CatEntity.class, new Box(pos).expand(16.0, 8.0, 16.0));
        if (list.isEmpty()) {
            this.spawn(pos, world, true);
        }
    }

    private void spawn(BlockPos pos, ServerWorld world, boolean persistent) {
        CatEntity lv = EntityType.CAT.create(world, SpawnReason.NATURAL);
        if (lv == null) {
            return;
        }
        lv.initialize(world, world.getLocalDifficulty(pos), SpawnReason.NATURAL, null);
        if (persistent) {
            lv.setPersistent();
        }
        lv.refreshPositionAndAngles(pos, 0.0f, 0.0f);
        world.spawnEntityAndPassengers(lv);
    }
}

