/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.spawner;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.block.enums.TrialSpawnerState;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.TrialSpawnerConfig;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class TrialSpawnerData {
    private static final String SPAWN_DATA_KEY = "spawn_data";
    private static final String NEXT_MOB_SPAWNS_AT_KEY = "next_mob_spawns_at";
    private static final int field_50190 = 20;
    private static final int field_50191 = 18000;
    final Set<UUID> players = new HashSet<UUID>();
    final Set<UUID> spawnedMobsAlive = new HashSet<UUID>();
    long cooldownEnd;
    long nextMobSpawnsAt;
    int totalSpawnedMobs;
    Optional<MobSpawnerEntry> spawnData = Optional.empty();
    Optional<RegistryKey<LootTable>> rewardLootTable = Optional.empty();
    @Nullable
    private Entity displayEntity;
    @Nullable
    private Pool<ItemStack> itemsToDropWhenOminous;
    double displayEntityRotation;
    double lastDisplayEntityRotation;

    public Packed pack() {
        return new Packed(Set.copyOf(this.players), Set.copyOf(this.spawnedMobsAlive), this.cooldownEnd, this.nextMobSpawnsAt, this.totalSpawnedMobs, this.spawnData, this.rewardLootTable);
    }

    public void unpack(Packed packed) {
        this.players.clear();
        this.players.addAll(packed.detectedPlayers);
        this.spawnedMobsAlive.clear();
        this.spawnedMobsAlive.addAll(packed.currentMobs);
        this.cooldownEnd = packed.cooldownEndsAt;
        this.nextMobSpawnsAt = packed.nextMobSpawnsAt;
        this.totalSpawnedMobs = packed.totalMobsSpawned;
        this.spawnData = packed.nextSpawnData;
        this.rewardLootTable = packed.ejectingLootTable;
    }

    public void reset() {
        this.spawnedMobsAlive.clear();
        this.spawnData = Optional.empty();
        this.deactivate();
    }

    public void deactivate() {
        this.players.clear();
        this.totalSpawnedMobs = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEnd = 0L;
    }

    public boolean hasSpawnData(TrialSpawnerLogic logic, Random random) {
        boolean bl = this.getSpawnData(logic, random).getNbt().getString("id").isPresent();
        return bl || !logic.getConfig().spawnPotentials().isEmpty();
    }

    public boolean hasSpawnedAllMobs(TrialSpawnerConfig config, int additionalPlayers) {
        return this.totalSpawnedMobs >= config.getTotalMobs(additionalPlayers);
    }

    public boolean areMobsDead() {
        return this.spawnedMobsAlive.isEmpty();
    }

    public boolean canSpawnMore(ServerWorld world, TrialSpawnerConfig config, int additionalPlayers) {
        return world.getTime() >= this.nextMobSpawnsAt && this.spawnedMobsAlive.size() < config.getSimultaneousMobs(additionalPlayers);
    }

    public int getAdditionalPlayers(BlockPos pos) {
        if (this.players.isEmpty()) {
            Util.logErrorOrPause("Trial Spawner at " + String.valueOf(pos) + " has no detected players");
        }
        return Math.max(0, this.players.size() - 1);
    }

    public void updatePlayers(ServerWorld world, BlockPos pos, TrialSpawnerLogic logic) {
        List<UUID> list2;
        boolean bl2;
        boolean bl;
        boolean bl3 = bl = (pos.asLong() + world.getTime()) % 20L != 0L;
        if (bl) {
            return;
        }
        if (logic.getSpawnerState().equals(TrialSpawnerState.COOLDOWN) && logic.isOminous()) {
            return;
        }
        List<UUID> list = logic.getEntityDetector().detect(world, logic.getEntitySelector(), pos, logic.getDetectionRadius(), true);
        if (logic.isOminous() || list.isEmpty()) {
            bl2 = false;
        } else {
            Optional<Pair<PlayerEntity, RegistryEntry<StatusEffect>>> optional = TrialSpawnerData.findPlayerWithOmen(world, list);
            optional.ifPresent(pair -> {
                PlayerEntity lv = (PlayerEntity)pair.getFirst();
                if (pair.getSecond() == StatusEffects.BAD_OMEN) {
                    TrialSpawnerData.applyTrialOmen(lv);
                }
                world.syncWorldEvent(WorldEvents.TRIAL_SPAWNER_TURNS_OMINOUS, BlockPos.ofFloored(lv.getEyePos()), 0);
                logic.setOminous(world, pos);
            });
            bl2 = optional.isPresent();
        }
        if (logic.getSpawnerState().equals(TrialSpawnerState.COOLDOWN) && !bl2) {
            return;
        }
        boolean bl32 = logic.getData().players.isEmpty();
        List<UUID> list3 = list2 = bl32 ? list : logic.getEntityDetector().detect(world, logic.getEntitySelector(), pos, logic.getDetectionRadius(), false);
        if (this.players.addAll(list2)) {
            this.nextMobSpawnsAt = Math.max(world.getTime() + 40L, this.nextMobSpawnsAt);
            if (!bl2) {
                int i = logic.isOminous() ? WorldEvents.OMINOUS_TRIAL_SPAWNER_DETECTS_PLAYER : WorldEvents.TRIAL_SPAWNER_DETECTS_PLAYER;
                world.syncWorldEvent(i, pos, this.players.size());
            }
        }
    }

    private static Optional<Pair<PlayerEntity, RegistryEntry<StatusEffect>>> findPlayerWithOmen(ServerWorld world, List<UUID> players) {
        PlayerEntity lv = null;
        for (UUID uUID : players) {
            PlayerEntity lv2 = world.getPlayerByUuid(uUID);
            if (lv2 == null) continue;
            RegistryEntry<StatusEffect> lv3 = StatusEffects.TRIAL_OMEN;
            if (lv2.hasStatusEffect(lv3)) {
                return Optional.of(Pair.of(lv2, lv3));
            }
            if (!lv2.hasStatusEffect(StatusEffects.BAD_OMEN)) continue;
            lv = lv2;
        }
        return Optional.ofNullable(lv).map(player -> Pair.of(player, StatusEffects.BAD_OMEN));
    }

    public void resetAndClearMobs(TrialSpawnerLogic logic, ServerWorld world) {
        this.spawnedMobsAlive.stream().map(world::getEntity).forEach(entity -> {
            if (entity == null) {
                return;
            }
            world.syncWorldEvent(WorldEvents.TRIAL_SPAWNER_SPAWNS_MOB_AT_SPAWN_POS, entity.getBlockPos(), TrialSpawnerLogic.Type.NORMAL.getIndex());
            if (entity instanceof MobEntity) {
                MobEntity lv = (MobEntity)entity;
                lv.dropAllForeignEquipment(world);
            }
            entity.remove(Entity.RemovalReason.DISCARDED);
        });
        if (!logic.getOminousConfig().spawnPotentials().isEmpty()) {
            this.spawnData = Optional.empty();
        }
        this.totalSpawnedMobs = 0;
        this.spawnedMobsAlive.clear();
        this.nextMobSpawnsAt = world.getTime() + (long)logic.getOminousConfig().ticksBetweenSpawn();
        logic.updateListeners();
        this.cooldownEnd = world.getTime() + logic.getOminousConfig().getCooldownLength();
    }

    private static void applyTrialOmen(PlayerEntity player) {
        StatusEffectInstance lv = player.getStatusEffect(StatusEffects.BAD_OMEN);
        if (lv == null) {
            return;
        }
        int i = lv.getAmplifier() + 1;
        int j = 18000 * i;
        player.removeStatusEffect(StatusEffects.BAD_OMEN);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.TRIAL_OMEN, j, 0));
    }

    public boolean isCooldownPast(ServerWorld world, float f, int i) {
        long l = this.cooldownEnd - (long)i;
        return (float)world.getTime() >= (float)l + f;
    }

    public boolean isCooldownAtRepeating(ServerWorld world, float f, int i) {
        long l = this.cooldownEnd - (long)i;
        return (float)(world.getTime() - l) % f == 0.0f;
    }

    public boolean isCooldownOver(ServerWorld world) {
        return world.getTime() >= this.cooldownEnd;
    }

    protected MobSpawnerEntry getSpawnData(TrialSpawnerLogic logic, Random random) {
        if (this.spawnData.isPresent()) {
            return this.spawnData.get();
        }
        Pool<MobSpawnerEntry> lv = logic.getConfig().spawnPotentials();
        Optional<MobSpawnerEntry> optional = lv.isEmpty() ? this.spawnData : lv.getOrEmpty(random);
        this.spawnData = Optional.of(optional.orElseGet(MobSpawnerEntry::new));
        logic.updateListeners();
        return this.spawnData.get();
    }

    @Nullable
    public Entity setDisplayEntity(TrialSpawnerLogic logic, World world, TrialSpawnerState state) {
        NbtCompound lv;
        if (!state.doesDisplayRotate()) {
            return null;
        }
        if (this.displayEntity == null && (lv = this.getSpawnData(logic, world.getRandom()).getNbt()).getString("id").isPresent()) {
            this.displayEntity = EntityType.loadEntityWithPassengers(lv, world, SpawnReason.TRIAL_SPAWNER, Function.identity());
        }
        return this.displayEntity;
    }

    public NbtCompound getSpawnDataNbt(TrialSpawnerState state) {
        NbtCompound lv = new NbtCompound();
        if (state == TrialSpawnerState.ACTIVE) {
            lv.putLong(NEXT_MOB_SPAWNS_AT_KEY, this.nextMobSpawnsAt);
        }
        this.spawnData.ifPresent(spawnData -> lv.put(SPAWN_DATA_KEY, MobSpawnerEntry.CODEC, spawnData));
        return lv;
    }

    public double getDisplayEntityRotation() {
        return this.displayEntityRotation;
    }

    public double getLastDisplayEntityRotation() {
        return this.lastDisplayEntityRotation;
    }

    Pool<ItemStack> getItemsToDropWhenOminous(ServerWorld world, TrialSpawnerConfig config, BlockPos pos) {
        long l;
        LootWorldContext lv2;
        if (this.itemsToDropWhenOminous != null) {
            return this.itemsToDropWhenOminous;
        }
        LootTable lv = world.getServer().getReloadableRegistries().getLootTable(config.itemsToDropWhenOminous());
        ObjectArrayList<ItemStack> objectArrayList = lv.generateLoot(lv2 = new LootWorldContext.Builder(world).build(LootContextTypes.EMPTY), l = TrialSpawnerData.getLootSeed(world, pos));
        if (objectArrayList.isEmpty()) {
            return Pool.empty();
        }
        Pool.Builder<ItemStack> lv3 = Pool.builder();
        for (ItemStack lv4 : objectArrayList) {
            lv3.add(lv4.copyWithCount(1), lv4.getCount());
        }
        this.itemsToDropWhenOminous = lv3.build();
        return this.itemsToDropWhenOminous;
    }

    private static long getLootSeed(ServerWorld world, BlockPos pos) {
        BlockPos lv = new BlockPos(MathHelper.floor((float)pos.getX() / 30.0f), MathHelper.floor((float)pos.getY() / 20.0f), MathHelper.floor((float)pos.getZ() / 30.0f));
        return world.getSeed() + lv.asLong();
    }

    public record Packed(Set<UUID> detectedPlayers, Set<UUID> currentMobs, long cooldownEndsAt, long nextMobSpawnsAt, int totalMobsSpawned, Optional<MobSpawnerEntry> nextSpawnData, Optional<RegistryKey<LootTable>> ejectingLootTable) {
        public static final MapCodec<Packed> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Uuids.SET_CODEC.lenientOptionalFieldOf("registered_players", Set.of()).forGetter(Packed::detectedPlayers), Uuids.SET_CODEC.lenientOptionalFieldOf("current_mobs", Set.of()).forGetter(Packed::currentMobs), Codec.LONG.lenientOptionalFieldOf("cooldown_ends_at", 0L).forGetter(Packed::cooldownEndsAt), Codec.LONG.lenientOptionalFieldOf(TrialSpawnerData.NEXT_MOB_SPAWNS_AT_KEY, 0L).forGetter(Packed::nextMobSpawnsAt), Codec.intRange(0, Integer.MAX_VALUE).lenientOptionalFieldOf("total_mobs_spawned", 0).forGetter(Packed::totalMobsSpawned), MobSpawnerEntry.CODEC.lenientOptionalFieldOf(TrialSpawnerData.SPAWN_DATA_KEY).forGetter(Packed::nextSpawnData), LootTable.TABLE_KEY.lenientOptionalFieldOf("ejecting_loot_table").forGetter(Packed::ejectingLootTable)).apply((Applicative<Packed, ?>)instance, Packed::new));
    }
}

