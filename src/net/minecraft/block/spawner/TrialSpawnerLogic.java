/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.spawner;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TrialSpawnerBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.enums.TrialSpawnerState;
import net.minecraft.block.spawner.EntityDetector;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.TrialSpawnerConfig;
import net.minecraft.block.spawner.TrialSpawnerData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.slf4j.Logger;

public final class TrialSpawnerLogic {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int field_47358 = 40;
    private static final int DEFAULT_COOLDOWN_LENGTH = 36000;
    private static final int DEFAULT_ENTITY_DETECTION_RANGE = 14;
    private static final int MAX_ENTITY_DISTANCE = 47;
    private static final int MAX_ENTITY_DISTANCE_SQUARED = MathHelper.square(47);
    private static final float SOUND_RATE_PER_TICK = 0.02f;
    private final TrialSpawnerData data = new TrialSpawnerData();
    private FullConfig fullConfig;
    private final TrialSpawner trialSpawner;
    private EntityDetector entityDetector;
    private final EntityDetector.Selector entitySelector;
    private boolean forceActivate;
    private boolean ominous;

    public TrialSpawnerLogic(FullConfig fullConfig, TrialSpawner trialSpawner, EntityDetector entityDetector, EntityDetector.Selector entitySelector) {
        this.fullConfig = fullConfig;
        this.trialSpawner = trialSpawner;
        this.entityDetector = entityDetector;
        this.entitySelector = entitySelector;
    }

    public TrialSpawnerConfig getConfig() {
        return this.ominous ? this.fullConfig.ominous().value() : this.fullConfig.normal.value();
    }

    public TrialSpawnerConfig getNormalConfig() {
        return this.fullConfig.normal.value();
    }

    public TrialSpawnerConfig getOminousConfig() {
        return this.fullConfig.ominous.value();
    }

    public void readData(ReadView view) {
        view.read(TrialSpawnerData.Packed.CODEC).ifPresent(this.data::unpack);
        this.fullConfig = view.read(FullConfig.CODEC).orElse(FullConfig.DEFAULT);
    }

    public void writeData(WriteView view) {
        view.put(TrialSpawnerData.Packed.CODEC, this.data.pack());
        view.put(FullConfig.CODEC, this.fullConfig);
    }

    public void setOminous(ServerWorld world, BlockPos pos) {
        world.setBlockState(pos, (BlockState)world.getBlockState(pos).with(TrialSpawnerBlock.OMINOUS, true), Block.NOTIFY_ALL);
        world.syncWorldEvent(WorldEvents.TRIAL_SPAWNER_TURNS_OMINOUS, pos, 1);
        this.ominous = true;
        this.data.resetAndClearMobs(this, world);
    }

    public void setNotOminous(ServerWorld world, BlockPos pos) {
        world.setBlockState(pos, (BlockState)world.getBlockState(pos).with(TrialSpawnerBlock.OMINOUS, false), Block.NOTIFY_ALL);
        this.ominous = false;
    }

    public boolean isOminous() {
        return this.ominous;
    }

    public int getCooldownLength() {
        return this.fullConfig.targetCooldownLength;
    }

    public int getDetectionRadius() {
        return this.fullConfig.requiredPlayerRange;
    }

    public TrialSpawnerState getSpawnerState() {
        return this.trialSpawner.getSpawnerState();
    }

    public TrialSpawnerData getData() {
        return this.data;
    }

    public void setSpawnerState(World world, TrialSpawnerState spawnerState) {
        this.trialSpawner.setSpawnerState(world, spawnerState);
    }

    public void updateListeners() {
        this.trialSpawner.updateListeners();
    }

    public EntityDetector getEntityDetector() {
        return this.entityDetector;
    }

    public EntityDetector.Selector getEntitySelector() {
        return this.entitySelector;
    }

    public boolean canActivate(ServerWorld world) {
        if (!world.getServer().getGameRules().getBoolean(GameRules.SPAWNER_BLOCKS_ENABLED)) {
            return false;
        }
        if (this.forceActivate) {
            return true;
        }
        if (world.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
    }

    public Optional<UUID> trySpawnMob(ServerWorld world, BlockPos pos) {
        Random lv = world.getRandom();
        MobSpawnerEntry lv2 = this.data.getSpawnData(this, world.getRandom());
        try (ErrorReporter.Logging lv3 = new ErrorReporter.Logging(() -> "spawner@" + String.valueOf(pos), LOGGER);){
            Object lv9;
            MobSpawnerEntry.CustomSpawnRules lv7;
            ReadView lv4 = NbtReadView.create(lv3, world.getRegistryManager(), lv2.entity());
            Optional<EntityType<?>> optional = EntityType.fromData(lv4);
            if (optional.isEmpty()) {
                Optional<UUID> optional2 = Optional.empty();
                return optional2;
            }
            Vec3d lv5 = lv4.read("Pos", Vec3d.CODEC).orElseGet(() -> {
                TrialSpawnerConfig lv = this.getConfig();
                return new Vec3d((double)pos.getX() + (lv.nextDouble() - lv.nextDouble()) * (double)lv.spawnRange() + 0.5, pos.getY() + lv.nextInt(3) - 1, (double)pos.getZ() + (lv.nextDouble() - lv.nextDouble()) * (double)lv.spawnRange() + 0.5);
            });
            if (!world.isSpaceEmpty(optional.get().getSpawnBox(lv5.x, lv5.y, lv5.z))) {
                Optional<UUID> optional3 = Optional.empty();
                return optional3;
            }
            if (!TrialSpawnerLogic.hasLineOfSight(world, pos.toCenterPos(), lv5)) {
                Optional<UUID> optional4 = Optional.empty();
                return optional4;
            }
            BlockPos lv6 = BlockPos.ofFloored(lv5);
            if (!SpawnRestriction.canSpawn(optional.get(), world, SpawnReason.TRIAL_SPAWNER, lv6, world.getRandom())) {
                Optional<UUID> optional5 = Optional.empty();
                return optional5;
            }
            if (lv2.getCustomSpawnRules().isPresent() && !(lv7 = lv2.getCustomSpawnRules().get()).canSpawn(lv6, world)) {
                Optional<UUID> optional6 = Optional.empty();
                return optional6;
            }
            Entity lv8 = EntityType.loadEntityWithPassengers(lv4, (World)world, SpawnReason.TRIAL_SPAWNER, entity -> {
                entity.refreshPositionAndAngles(arg.x, arg.y, arg.z, lv.nextFloat() * 360.0f, 0.0f);
                return entity;
            });
            if (lv8 == null) {
                Optional<UUID> optional7 = Optional.empty();
                return optional7;
            }
            if (lv8 instanceof MobEntity) {
                boolean bl;
                lv9 = (MobEntity)lv8;
                if (!((MobEntity)lv9).canSpawn(world)) {
                    Optional<UUID> optional8 = Optional.empty();
                    return optional8;
                }
                boolean bl2 = bl = lv2.getNbt().getSize() == 1 && lv2.getNbt().getString("id").isPresent();
                if (bl) {
                    ((MobEntity)lv9).initialize(world, world.getLocalDifficulty(((Entity)lv9).getBlockPos()), SpawnReason.TRIAL_SPAWNER, null);
                }
                ((MobEntity)lv9).setPersistent();
                lv2.getEquipment().ifPresent(((MobEntity)lv9)::setEquipmentFromTable);
            }
            if (!world.spawnNewEntityAndPassengers(lv8)) {
                lv9 = Optional.empty();
                return lv9;
            }
            Type lv10 = this.ominous ? Type.OMINOUS : Type.NORMAL;
            world.syncWorldEvent(WorldEvents.TRIAL_SPAWNER_SPAWNS_MOB, pos, lv10.getIndex());
            world.syncWorldEvent(WorldEvents.TRIAL_SPAWNER_SPAWNS_MOB_AT_SPAWN_POS, lv6, lv10.getIndex());
            world.emitGameEvent(lv8, GameEvent.ENTITY_PLACE, lv6);
            Optional<UUID> optional9 = Optional.of(lv8.getUuid());
            return optional9;
        }
    }

    public void ejectLootTable(ServerWorld world, BlockPos pos, RegistryKey<LootTable> lootTable) {
        LootWorldContext lv2;
        LootTable lv = world.getServer().getReloadableRegistries().getLootTable(lootTable);
        ObjectArrayList<ItemStack> objectArrayList = lv.generateLoot(lv2 = new LootWorldContext.Builder(world).build(LootContextTypes.EMPTY));
        if (!objectArrayList.isEmpty()) {
            for (ItemStack lv3 : objectArrayList) {
                ItemDispenserBehavior.spawnItem(world, lv3, 2, Direction.UP, Vec3d.ofBottomCenter(pos).offset(Direction.UP, 1.2));
            }
            world.syncWorldEvent(WorldEvents.TRIAL_SPAWNER_EJECTS_ITEM, pos, 0);
        }
    }

    public void tickClient(World world, BlockPos pos, boolean ominous) {
        Random lv2;
        TrialSpawnerState lv = this.getSpawnerState();
        lv.emitParticles(world, pos, ominous);
        if (lv.doesDisplayRotate()) {
            double d = Math.max(0L, this.data.nextMobSpawnsAt - world.getTime());
            this.data.lastDisplayEntityRotation = this.data.displayEntityRotation;
            this.data.displayEntityRotation = (this.data.displayEntityRotation + lv.getDisplayRotationSpeed() / (d + 200.0)) % 360.0;
        }
        if (lv.playsSound() && (lv2 = world.getRandom()).nextFloat() <= 0.02f) {
            SoundEvent lv3 = ominous ? SoundEvents.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS : SoundEvents.BLOCK_TRIAL_SPAWNER_AMBIENT;
            world.playSoundAtBlockCenterClient(pos, lv3, SoundCategory.BLOCKS, lv2.nextFloat() * 0.25f + 0.75f, lv2.nextFloat() + 0.5f, false);
        }
    }

    public void tickServer(ServerWorld world, BlockPos pos, boolean ominous) {
        TrialSpawnerState lv2;
        this.ominous = ominous;
        TrialSpawnerState lv = this.getSpawnerState();
        if (this.data.spawnedMobsAlive.removeIf(uuid -> TrialSpawnerLogic.shouldRemoveMobFromData(world, pos, uuid))) {
            this.data.nextMobSpawnsAt = world.getTime() + (long)this.getConfig().ticksBetweenSpawn();
        }
        if ((lv2 = lv.tick(pos, this, world)) != lv) {
            this.setSpawnerState(world, lv2);
        }
    }

    private static boolean shouldRemoveMobFromData(ServerWorld world, BlockPos pos, UUID uuid) {
        Entity lv = world.getEntity(uuid);
        return lv == null || !lv.isAlive() || !lv.getEntityWorld().getRegistryKey().equals(world.getRegistryKey()) || lv.getBlockPos().getSquaredDistance(pos) > (double)MAX_ENTITY_DISTANCE_SQUARED;
    }

    private static boolean hasLineOfSight(World world, Vec3d spawnerPos, Vec3d spawnPos) {
        BlockHitResult lv = world.raycast(new RaycastContext(spawnPos, spawnerPos, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, ShapeContext.absent()));
        return lv.getBlockPos().equals(BlockPos.ofFloored(spawnerPos)) || lv.getType() == HitResult.Type.MISS;
    }

    public static void addMobSpawnParticles(World world, BlockPos pos, Random random, SimpleParticleType particle) {
        for (int i = 0; i < 20; ++i) {
            double d = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double e = (double)pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double f = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            world.addParticleClient(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
            world.addParticleClient(particle, d, e, f, 0.0, 0.0, 0.0);
        }
    }

    public static void addTrialOmenParticles(World world, BlockPos pos, Random random) {
        for (int i = 0; i < 20; ++i) {
            double d = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double e = (double)pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double f = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double g = random.nextGaussian() * 0.02;
            double h = random.nextGaussian() * 0.02;
            double j = random.nextGaussian() * 0.02;
            world.addParticleClient(ParticleTypes.TRIAL_OMEN, d, e, f, g, h, j);
            world.addParticleClient(ParticleTypes.SOUL_FIRE_FLAME, d, e, f, g, h, j);
        }
    }

    public static void addDetectionParticles(World world, BlockPos pos, Random random, int playerCount, ParticleEffect particle) {
        for (int j = 0; j < 30 + Math.min(playerCount, 10) * 5; ++j) {
            double d = (double)(2.0f * random.nextFloat() - 1.0f) * 0.65;
            double e = (double)(2.0f * random.nextFloat() - 1.0f) * 0.65;
            double f = (double)pos.getX() + 0.5 + d;
            double g = (double)pos.getY() + 0.1 + (double)random.nextFloat() * 0.8;
            double h = (double)pos.getZ() + 0.5 + e;
            world.addParticleClient(particle, f, g, h, 0.0, 0.0, 0.0);
        }
    }

    public static void addEjectItemParticles(World world, BlockPos pos, Random random) {
        for (int i = 0; i < 20; ++i) {
            double d = (double)pos.getX() + 0.4 + random.nextDouble() * 0.2;
            double e = (double)pos.getY() + 0.4 + random.nextDouble() * 0.2;
            double f = (double)pos.getZ() + 0.4 + random.nextDouble() * 0.2;
            double g = random.nextGaussian() * 0.02;
            double h = random.nextGaussian() * 0.02;
            double j = random.nextGaussian() * 0.02;
            world.addParticleClient(ParticleTypes.SMALL_FLAME, d, e, f, g, h, j * 0.25);
            world.addParticleClient(ParticleTypes.SMOKE, d, e, f, g, h, j);
        }
    }

    public void setEntityType(EntityType<?> entityType, World world) {
        this.data.reset();
        this.fullConfig = this.fullConfig.withEntityType(entityType);
        this.setSpawnerState(world, TrialSpawnerState.INACTIVE);
    }

    @Deprecated(forRemoval=true)
    @VisibleForTesting
    public void setEntityDetector(EntityDetector detector) {
        this.entityDetector = detector;
    }

    @Deprecated(forRemoval=true)
    @VisibleForTesting
    public void forceActivate() {
        this.forceActivate = true;
    }

    public record FullConfig(RegistryEntry<TrialSpawnerConfig> normal, RegistryEntry<TrialSpawnerConfig> ominous, int targetCooldownLength, int requiredPlayerRange) {
        public static final MapCodec<FullConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(TrialSpawnerConfig.ENTRY_CODEC.optionalFieldOf("normal_config", RegistryEntry.of(TrialSpawnerConfig.DEFAULT)).forGetter(FullConfig::normal), TrialSpawnerConfig.ENTRY_CODEC.optionalFieldOf("ominous_config", RegistryEntry.of(TrialSpawnerConfig.DEFAULT)).forGetter(FullConfig::ominous), Codecs.NON_NEGATIVE_INT.optionalFieldOf("target_cooldown_length", 36000).forGetter(FullConfig::targetCooldownLength), Codec.intRange(1, 128).optionalFieldOf("required_player_range", 14).forGetter(FullConfig::requiredPlayerRange)).apply((Applicative<FullConfig, ?>)instance, FullConfig::new));
        public static final FullConfig DEFAULT = new FullConfig(RegistryEntry.of(TrialSpawnerConfig.DEFAULT), RegistryEntry.of(TrialSpawnerConfig.DEFAULT), 36000, 14);

        public FullConfig withEntityType(EntityType<?> entityType) {
            return new FullConfig(RegistryEntry.of(this.normal.value().withSpawnPotential(entityType)), RegistryEntry.of(this.ominous.value().withSpawnPotential(entityType)), this.targetCooldownLength, this.requiredPlayerRange);
        }
    }

    public static interface TrialSpawner {
        public void setSpawnerState(World var1, TrialSpawnerState var2);

        public TrialSpawnerState getSpawnerState();

        public void updateListeners();
    }

    public static enum Type {
        NORMAL(ParticleTypes.FLAME),
        OMINOUS(ParticleTypes.SOUL_FIRE_FLAME);

        public final SimpleParticleType particle;

        private Type(SimpleParticleType particle) {
            this.particle = particle;
        }

        public static Type fromIndex(int index) {
            Type[] lvs = Type.values();
            if (index > lvs.length || index < 0) {
                return NORMAL;
            }
            return lvs[index];
        }

        public int getIndex() {
            return this.ordinal();
        }
    }
}

