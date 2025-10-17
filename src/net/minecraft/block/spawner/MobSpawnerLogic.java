/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.spawner;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class MobSpawnerLogic {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String SPAWN_DATA_KEY = "SpawnData";
    private static final int field_30951 = 1;
    private static final int field_57757 = 20;
    private static final int DEFAULT_MIN_SPAWN_DELAY = 200;
    private static final int DEFAULT_MAX_SPAWN_DELAY = 800;
    private static final int DEFAULT_SPAWN_COUNT = 4;
    private static final int DEFAULT_MAX_NEARBY_ENTITIES = 6;
    private static final int DEFAULT_REQUIRED_PLAYER_RANGE = 16;
    private static final int DEFAULT_SPAWN_RANGE = 4;
    private int spawnDelay = 20;
    private Pool<MobSpawnerEntry> spawnPotentials = Pool.empty();
    @Nullable
    private MobSpawnerEntry spawnEntry;
    private double rotation;
    private double lastRotation;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    @Nullable
    private Entity renderedEntity;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    public void setEntityId(EntityType<?> type, @Nullable World world, Random random, BlockPos pos) {
        this.getSpawnEntry(world, random, pos).getNbt().putString("id", Registries.ENTITY_TYPE.getId(type).toString());
    }

    private boolean isPlayerInRange(World world, BlockPos pos) {
        return world.isPlayerInRange((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, this.requiredPlayerRange);
    }

    public void clientTick(World world, BlockPos pos) {
        if (!this.isPlayerInRange(world, pos)) {
            this.lastRotation = this.rotation;
        } else if (this.renderedEntity != null) {
            Random lv = world.getRandom();
            double d = (double)pos.getX() + lv.nextDouble();
            double e = (double)pos.getY() + lv.nextDouble();
            double f = (double)pos.getZ() + lv.nextDouble();
            world.addParticleClient(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
            world.addParticleClient(ParticleTypes.FLAME, d, e, f, 0.0, 0.0, 0.0);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }
            this.lastRotation = this.rotation;
            this.rotation = (this.rotation + (double)(1000.0f / ((float)this.spawnDelay + 200.0f))) % 360.0;
        }
    }

    public void serverTick(ServerWorld world, BlockPos pos) {
        if (!this.isPlayerInRange(world, pos) || !world.getServer().areSpawnerBlocksEnabled()) {
            return;
        }
        if (this.spawnDelay == -1) {
            this.updateSpawns(world, pos);
        }
        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }
        boolean bl = false;
        Random lv = world.getRandom();
        MobSpawnerEntry lv2 = this.getSpawnEntry(world, lv, pos);
        for (int i = 0; i < this.spawnCount; ++i) {
            try (ErrorReporter.Logging lv3 = new ErrorReporter.Logging(this::toString, LOGGER);){
                ReadView lv4 = NbtReadView.create(lv3, world.getRegistryManager(), lv2.getNbt());
                Optional<EntityType<?>> optional = EntityType.fromData(lv4);
                if (optional.isEmpty()) {
                    this.updateSpawns(world, pos);
                    return;
                }
                Vec3d lv5 = lv4.read("Pos", Vec3d.CODEC).orElseGet(() -> new Vec3d((double)pos.getX() + (lv.nextDouble() - lv.nextDouble()) * (double)this.spawnRange + 0.5, pos.getY() + lv.nextInt(3) - 1, (double)pos.getZ() + (lv.nextDouble() - lv.nextDouble()) * (double)this.spawnRange + 0.5));
                if (!world.isSpaceEmpty(optional.get().getSpawnBox(lv5.x, lv5.y, lv5.z))) continue;
                BlockPos lv6 = BlockPos.ofFloored(lv5);
                if (lv2.getCustomSpawnRules().isPresent()) {
                    MobSpawnerEntry.CustomSpawnRules lv7;
                    if (!optional.get().getSpawnGroup().isPeaceful() && world.getDifficulty() == Difficulty.PEACEFUL || !(lv7 = lv2.getCustomSpawnRules().get()).canSpawn(lv6, world)) continue;
                } else if (!SpawnRestriction.canSpawn(optional.get(), world, SpawnReason.SPAWNER, lv6, world.getRandom())) continue;
                Entity lv8 = EntityType.loadEntityWithPassengers(lv4, (World)world, SpawnReason.SPAWNER, arg2 -> {
                    arg2.refreshPositionAndAngles(arg.x, arg.y, arg.z, arg2.getYaw(), arg2.getPitch());
                    return arg2;
                });
                if (lv8 == null) {
                    this.updateSpawns(world, pos);
                    return;
                }
                int j = world.getEntitiesByType(TypeFilter.equals(lv8.getClass()), new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).expand(this.spawnRange), EntityPredicates.EXCEPT_SPECTATOR).size();
                if (j >= this.maxNearbyEntities) {
                    this.updateSpawns(world, pos);
                    return;
                }
                lv8.refreshPositionAndAngles(lv8.getX(), lv8.getY(), lv8.getZ(), lv.nextFloat() * 360.0f, 0.0f);
                if (lv8 instanceof MobEntity) {
                    boolean bl2;
                    MobEntity lv9 = (MobEntity)lv8;
                    if (lv2.getCustomSpawnRules().isEmpty() && !lv9.canSpawn(world, SpawnReason.SPAWNER) || !lv9.canSpawn(world)) continue;
                    boolean bl3 = bl2 = lv2.getNbt().getSize() == 1 && lv2.getNbt().getString("id").isPresent();
                    if (bl2) {
                        ((MobEntity)lv8).initialize(world, world.getLocalDifficulty(lv8.getBlockPos()), SpawnReason.SPAWNER, null);
                    }
                    lv2.getEquipment().ifPresent(lv9::setEquipmentFromTable);
                }
                if (!world.spawnNewEntityAndPassengers(lv8)) {
                    this.updateSpawns(world, pos);
                    return;
                }
                world.syncWorldEvent(WorldEvents.SPAWNER_SPAWNS_MOB, pos, 0);
                world.emitGameEvent(lv8, GameEvent.ENTITY_PLACE, lv6);
                if (lv8 instanceof MobEntity) {
                    ((MobEntity)lv8).playSpawnEffects();
                }
                bl = true;
                continue;
            }
        }
        if (bl) {
            this.updateSpawns(world, pos);
        }
    }

    private void updateSpawns(World world, BlockPos pos) {
        Random lv = world.random;
        this.spawnDelay = this.maxSpawnDelay <= this.minSpawnDelay ? this.minSpawnDelay : this.minSpawnDelay + lv.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        this.spawnPotentials.getOrEmpty(lv).ifPresent(spawnPotential -> this.setSpawnEntry(world, pos, (MobSpawnerEntry)spawnPotential));
        this.sendStatus(world, pos, 1);
    }

    public void readData(@Nullable World world, BlockPos pos, ReadView view) {
        this.spawnDelay = view.getShort("Delay", (short)20);
        view.read(SPAWN_DATA_KEY, MobSpawnerEntry.CODEC).ifPresent(arg3 -> this.setSpawnEntry(world, pos, (MobSpawnerEntry)arg3));
        this.spawnPotentials = view.read("SpawnPotentials", MobSpawnerEntry.DATA_POOL_CODEC).orElseGet(() -> Pool.of(this.spawnEntry != null ? this.spawnEntry : new MobSpawnerEntry()));
        this.minSpawnDelay = view.getInt("MinSpawnDelay", 200);
        this.maxSpawnDelay = view.getInt("MaxSpawnDelay", 800);
        this.spawnCount = view.getInt("SpawnCount", 4);
        this.maxNearbyEntities = view.getInt("MaxNearbyEntities", 6);
        this.requiredPlayerRange = view.getInt("RequiredPlayerRange", 16);
        this.spawnRange = view.getInt("SpawnRange", 4);
        this.renderedEntity = null;
    }

    public void writeData(WriteView view) {
        view.putShort("Delay", (short)this.spawnDelay);
        view.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
        view.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
        view.putShort("SpawnCount", (short)this.spawnCount);
        view.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
        view.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
        view.putShort("SpawnRange", (short)this.spawnRange);
        view.putNullable(SPAWN_DATA_KEY, MobSpawnerEntry.CODEC, this.spawnEntry);
        view.put("SpawnPotentials", MobSpawnerEntry.DATA_POOL_CODEC, this.spawnPotentials);
    }

    @Nullable
    public Entity getRenderedEntity(World world, BlockPos pos) {
        if (this.renderedEntity == null) {
            NbtCompound lv = this.getSpawnEntry(world, world.getRandom(), pos).getNbt();
            if (lv.getString("id").isEmpty()) {
                return null;
            }
            this.renderedEntity = EntityType.loadEntityWithPassengers(lv, world, SpawnReason.SPAWNER, Function.identity());
            if (lv.getSize() != 1 || this.renderedEntity instanceof MobEntity) {
                // empty if block
            }
        }
        return this.renderedEntity;
    }

    public boolean handleStatus(World world, int status) {
        if (status == 1) {
            if (world.isClient()) {
                this.spawnDelay = this.minSpawnDelay;
            }
            return true;
        }
        return false;
    }

    protected void setSpawnEntry(@Nullable World world, BlockPos pos, MobSpawnerEntry spawnEntry) {
        this.spawnEntry = spawnEntry;
    }

    private MobSpawnerEntry getSpawnEntry(@Nullable World world, Random random, BlockPos pos) {
        if (this.spawnEntry != null) {
            return this.spawnEntry;
        }
        this.setSpawnEntry(world, pos, this.spawnPotentials.getOrEmpty(random).orElseGet(MobSpawnerEntry::new));
        return this.spawnEntry;
    }

    public abstract void sendStatus(World var1, BlockPos var2, int var3);

    public double getRotation() {
        return this.rotation;
    }

    public double getLastRotation() {
        return this.lastRotation;
    }
}

