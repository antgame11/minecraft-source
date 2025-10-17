/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.village.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnLocation;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Rarity;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import org.jetbrains.annotations.Nullable;

public class Raid {
    public static final SpawnLocation RAVAGER_SPAWN_LOCATION = SpawnRestriction.getLocation(EntityType.RAVAGER);
    public static final MapCodec<Raid> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("started")).forGetter(raid -> raid.started), ((MapCodec)Codec.BOOL.fieldOf("active")).forGetter(raid -> raid.active), ((MapCodec)Codec.LONG.fieldOf("ticks_active")).forGetter(raid -> raid.ticksActive), ((MapCodec)Codec.INT.fieldOf("raid_omen_level")).forGetter(raid -> raid.raidOmenLevel), ((MapCodec)Codec.INT.fieldOf("groups_spawned")).forGetter(raid -> raid.wavesSpawned), ((MapCodec)Codec.INT.fieldOf("cooldown_ticks")).forGetter(raid -> raid.preRaidTicks), ((MapCodec)Codec.INT.fieldOf("post_raid_ticks")).forGetter(raid -> raid.postRaidTicks), ((MapCodec)Codec.FLOAT.fieldOf("total_health")).forGetter(raid -> Float.valueOf(raid.totalHealth)), ((MapCodec)Codec.INT.fieldOf("group_count")).forGetter(raid -> raid.waveCount), ((MapCodec)Status.CODEC.fieldOf("status")).forGetter(raid -> raid.status), ((MapCodec)BlockPos.CODEC.fieldOf("center")).forGetter(raid -> raid.center), ((MapCodec)Uuids.SET_CODEC.fieldOf("heroes_of_the_village")).forGetter(raid -> raid.heroesOfTheVillage)).apply((Applicative<Raid, ?>)instance, Raid::new));
    private static final int field_53977 = 7;
    private static final int field_30676 = 2;
    private static final int field_30680 = 32;
    private static final int field_30681 = 48000;
    private static final int field_30682 = 5;
    private static final Text OMINOUS_BANNER_TRANSLATION_KEY = Text.translatable("block.minecraft.ominous_banner");
    private static final String RAIDERS_REMAINING_TRANSLATION_KEY = "event.minecraft.raid.raiders_remaining";
    public static final int field_30669 = 16;
    private static final int field_30685 = 40;
    private static final int DEFAULT_PRE_RAID_TICKS = 300;
    public static final int MAX_DESPAWN_COUNTER = 2400;
    public static final int field_30671 = 600;
    private static final int field_30687 = 30;
    public static final int field_30672 = 24000;
    public static final int field_30673 = 5;
    private static final int field_30688 = 2;
    private static final Text EVENT_TEXT = Text.translatable("event.minecraft.raid");
    private static final Text VICTORY_TITLE = Text.translatable("event.minecraft.raid.victory.full");
    private static final Text DEFEAT_TITLE = Text.translatable("event.minecraft.raid.defeat.full");
    private static final int MAX_ACTIVE_TICKS = 48000;
    private static final int field_53978 = 96;
    public static final int field_30674 = 9216;
    public static final int SQUARED_MAX_RAIDER_DISTANCE = 12544;
    private final Map<Integer, RaiderEntity> waveToCaptain = Maps.newHashMap();
    private final Map<Integer, Set<RaiderEntity>> waveToRaiders = Maps.newHashMap();
    private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
    private long ticksActive;
    private BlockPos center;
    private boolean started;
    private float totalHealth;
    private int raidOmenLevel;
    private boolean active;
    private int wavesSpawned;
    private final ServerBossBar bar = new ServerBossBar(EVENT_TEXT, BossBar.Color.RED, BossBar.Style.NOTCHED_10);
    private int postRaidTicks;
    private int preRaidTicks;
    private final Random random = Random.create();
    private final int waveCount;
    private Status status;
    private int finishCooldown;
    private Optional<BlockPos> preCalculatedRaidersSpawnLocation = Optional.empty();

    public Raid(BlockPos center, Difficulty difficulty) {
        this.active = true;
        this.preRaidTicks = 300;
        this.bar.setPercent(0.0f);
        this.center = center;
        this.waveCount = this.getMaxWaves(difficulty);
        this.status = Status.ONGOING;
    }

    private Raid(boolean started, boolean active, long ticksActive, int raidOmenLevel, int wavesSpawned, int preRaidTicks, int postRaidTicks, float totalHealth, int waveCount, Status status, BlockPos center, Set<UUID> heroesOfTheVillage) {
        this.started = started;
        this.active = active;
        this.ticksActive = ticksActive;
        this.raidOmenLevel = raidOmenLevel;
        this.wavesSpawned = wavesSpawned;
        this.preRaidTicks = preRaidTicks;
        this.postRaidTicks = postRaidTicks;
        this.totalHealth = totalHealth;
        this.center = center;
        this.waveCount = waveCount;
        this.status = status;
        this.heroesOfTheVillage.addAll(heroesOfTheVillage);
    }

    public boolean isFinished() {
        return this.hasWon() || this.hasLost();
    }

    public boolean isPreRaid() {
        return this.hasSpawned() && this.getRaiderCount() == 0 && this.preRaidTicks > 0;
    }

    public boolean hasSpawned() {
        return this.wavesSpawned > 0;
    }

    public boolean hasStopped() {
        return this.status == Status.STOPPED;
    }

    public boolean hasWon() {
        return this.status == Status.VICTORY;
    }

    public boolean hasLost() {
        return this.status == Status.LOSS;
    }

    public float getTotalHealth() {
        return this.totalHealth;
    }

    public Set<RaiderEntity> getAllRaiders() {
        HashSet<RaiderEntity> set = Sets.newHashSet();
        for (Set<RaiderEntity> set2 : this.waveToRaiders.values()) {
            set.addAll(set2);
        }
        return set;
    }

    public boolean hasStarted() {
        return this.started;
    }

    public int getGroupsSpawned() {
        return this.wavesSpawned;
    }

    private Predicate<ServerPlayerEntity> isInRaidDistance() {
        return player -> {
            BlockPos lv = player.getBlockPos();
            return player.isAlive() && player.getEntityWorld().getRaidAt(lv) == this;
        };
    }

    private void updateBarToPlayers(ServerWorld world) {
        HashSet<ServerPlayerEntity> set = Sets.newHashSet(this.bar.getPlayers());
        List<ServerPlayerEntity> list = world.getPlayers(this.isInRaidDistance());
        for (ServerPlayerEntity lv : list) {
            if (set.contains(lv)) continue;
            this.bar.addPlayer(lv);
        }
        for (ServerPlayerEntity lv : set) {
            if (list.contains(lv)) continue;
            this.bar.removePlayer(lv);
        }
    }

    public int getMaxAcceptableBadOmenLevel() {
        return 5;
    }

    public int getBadOmenLevel() {
        return this.raidOmenLevel;
    }

    public void setBadOmenLevel(int badOmenLevel) {
        this.raidOmenLevel = badOmenLevel;
    }

    public boolean start(ServerPlayerEntity player) {
        StatusEffectInstance lv = player.getStatusEffect(StatusEffects.RAID_OMEN);
        if (lv == null) {
            return false;
        }
        this.raidOmenLevel += lv.getAmplifier() + 1;
        this.raidOmenLevel = MathHelper.clamp(this.raidOmenLevel, 0, this.getMaxAcceptableBadOmenLevel());
        if (!this.hasSpawned()) {
            player.incrementStat(Stats.RAID_TRIGGER);
            Criteria.VOLUNTARY_EXILE.trigger(player);
        }
        return true;
    }

    public void invalidate() {
        this.active = false;
        this.bar.clearPlayers();
        this.status = Status.STOPPED;
    }

    public void tick(ServerWorld world) {
        if (this.hasStopped()) {
            return;
        }
        if (this.status == Status.ONGOING) {
            boolean bl2;
            boolean bl = this.active;
            this.active = world.isChunkLoaded(this.center);
            if (world.getDifficulty() == Difficulty.PEACEFUL) {
                this.invalidate();
                return;
            }
            if (bl != this.active) {
                this.bar.setVisible(this.active);
            }
            if (!this.active) {
                return;
            }
            if (!world.isNearOccupiedPointOfInterest(this.center)) {
                this.moveRaidCenter(world);
            }
            if (!world.isNearOccupiedPointOfInterest(this.center)) {
                if (this.wavesSpawned > 0) {
                    this.status = Status.LOSS;
                } else {
                    this.invalidate();
                }
            }
            ++this.ticksActive;
            if (this.ticksActive >= 48000L) {
                this.invalidate();
                return;
            }
            int i = this.getRaiderCount();
            if (i == 0 && this.shouldSpawnMoreGroups()) {
                if (this.preRaidTicks > 0) {
                    boolean bl3;
                    bl2 = this.preCalculatedRaidersSpawnLocation.isPresent();
                    boolean bl4 = bl3 = !bl2 && this.preRaidTicks % 5 == 0;
                    if (bl2 && !world.shouldTickEntityAt(this.preCalculatedRaidersSpawnLocation.get())) {
                        bl3 = true;
                    }
                    if (bl3) {
                        this.preCalculatedRaidersSpawnLocation = this.getRaidersSpawnLocation(world);
                    }
                    if (this.preRaidTicks == 300 || this.preRaidTicks % 20 == 0) {
                        this.updateBarToPlayers(world);
                    }
                    --this.preRaidTicks;
                    this.bar.setPercent(MathHelper.clamp((float)(300 - this.preRaidTicks) / 300.0f, 0.0f, 1.0f));
                } else if (this.preRaidTicks == 0 && this.wavesSpawned > 0) {
                    this.preRaidTicks = 300;
                    this.bar.setName(EVENT_TEXT);
                    return;
                }
            }
            if (this.ticksActive % 20L == 0L) {
                this.updateBarToPlayers(world);
                this.removeObsoleteRaiders(world);
                if (i > 0) {
                    if (i <= 2) {
                        this.bar.setName(EVENT_TEXT.copy().append(" - ").append(Text.translatable(RAIDERS_REMAINING_TRANSLATION_KEY, i)));
                    } else {
                        this.bar.setName(EVENT_TEXT);
                    }
                } else {
                    this.bar.setName(EVENT_TEXT);
                }
            }
            if (SharedConstants.RAIDS) {
                this.bar.setName(EVENT_TEXT.copy().append(" wave: ").append("" + this.wavesSpawned).append(ScreenTexts.SPACE).append("Raiders alive: ").append("" + this.getRaiderCount()).append(ScreenTexts.SPACE).append("" + this.getCurrentRaiderHealth()).append(" / ").append("" + this.totalHealth).append(" Is bonus? ").append("" + (this.hasExtraWave() && this.hasSpawnedExtraWave())).append(" Status: ").append(this.status.asString()));
            }
            bl2 = false;
            int j = 0;
            while (this.canSpawnRaiders()) {
                BlockPos lv = this.preCalculatedRaidersSpawnLocation.orElseGet(() -> this.findRandomRaidersSpawnLocation(world, 20));
                if (lv != null) {
                    this.started = true;
                    this.spawnNextWave(world, lv);
                    if (!bl2) {
                        this.playRaidHorn(world, lv);
                        bl2 = true;
                    }
                } else {
                    ++j;
                }
                if (j <= 5) continue;
                this.invalidate();
                break;
            }
            if (this.hasStarted() && !this.shouldSpawnMoreGroups() && i == 0) {
                if (this.postRaidTicks < 40) {
                    ++this.postRaidTicks;
                } else {
                    this.status = Status.VICTORY;
                    for (UUID uUID : this.heroesOfTheVillage) {
                        Entity lv2 = world.getEntity(uUID);
                        if (!(lv2 instanceof LivingEntity)) continue;
                        LivingEntity lv3 = (LivingEntity)lv2;
                        if (lv2.isSpectator()) continue;
                        lv3.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 48000, this.raidOmenLevel - 1, false, false, true));
                        if (!(lv3 instanceof ServerPlayerEntity)) continue;
                        ServerPlayerEntity lv4 = (ServerPlayerEntity)lv3;
                        lv4.incrementStat(Stats.RAID_WIN);
                        Criteria.HERO_OF_THE_VILLAGE.trigger(lv4);
                    }
                }
            }
            this.markDirty(world);
        } else if (this.isFinished()) {
            ++this.finishCooldown;
            if (this.finishCooldown >= 600) {
                this.invalidate();
                return;
            }
            if (this.finishCooldown % 20 == 0) {
                this.updateBarToPlayers(world);
                this.bar.setVisible(true);
                if (this.hasWon()) {
                    this.bar.setPercent(0.0f);
                    this.bar.setName(VICTORY_TITLE);
                } else {
                    this.bar.setName(DEFEAT_TITLE);
                }
            }
        }
    }

    private void moveRaidCenter(ServerWorld world) {
        Stream<ChunkSectionPos> stream = ChunkSectionPos.stream(ChunkSectionPos.from(this.center), 2);
        stream.filter(world::isNearOccupiedPointOfInterest).map(ChunkSectionPos::getCenterPos).min(Comparator.comparingDouble(pos -> pos.getSquaredDistance(this.center))).ifPresent(this::setCenter);
    }

    private Optional<BlockPos> getRaidersSpawnLocation(ServerWorld world) {
        BlockPos lv = this.findRandomRaidersSpawnLocation(world, 8);
        if (lv != null) {
            return Optional.of(lv);
        }
        return Optional.empty();
    }

    private boolean shouldSpawnMoreGroups() {
        if (this.hasExtraWave()) {
            return !this.hasSpawnedExtraWave();
        }
        return !this.hasSpawnedFinalWave();
    }

    private boolean hasSpawnedFinalWave() {
        return this.getGroupsSpawned() == this.waveCount;
    }

    private boolean hasExtraWave() {
        return this.raidOmenLevel > 1;
    }

    private boolean hasSpawnedExtraWave() {
        return this.getGroupsSpawned() > this.waveCount;
    }

    private boolean isSpawningExtraWave() {
        return this.hasSpawnedFinalWave() && this.getRaiderCount() == 0 && this.hasExtraWave();
    }

    private void removeObsoleteRaiders(ServerWorld world) {
        Iterator<Set<RaiderEntity>> iterator = this.waveToRaiders.values().iterator();
        HashSet<RaiderEntity> set = Sets.newHashSet();
        while (iterator.hasNext()) {
            Set<RaiderEntity> set2 = iterator.next();
            for (RaiderEntity lv : set2) {
                BlockPos lv2 = lv.getBlockPos();
                if (lv.isRemoved() || lv.getEntityWorld().getRegistryKey() != world.getRegistryKey() || this.center.getSquaredDistance(lv2) >= 12544.0) {
                    set.add(lv);
                    continue;
                }
                if (lv.age <= 600) continue;
                if (world.getEntity(lv.getUuid()) == null) {
                    set.add(lv);
                }
                if (!world.isNearOccupiedPointOfInterest(lv2) && lv.getDespawnCounter() > 2400) {
                    lv.setOutOfRaidCounter(lv.getOutOfRaidCounter() + 1);
                }
                if (lv.getOutOfRaidCounter() < 30) continue;
                set.add(lv);
            }
        }
        for (RaiderEntity lv3 : set) {
            this.removeFromWave(world, lv3, true);
            if (!lv3.isPatrolLeader()) continue;
            this.removeLeader(lv3.getWave());
        }
    }

    private void playRaidHorn(ServerWorld world, BlockPos pos) {
        float f = 13.0f;
        int i = 64;
        Collection<ServerPlayerEntity> collection = this.bar.getPlayers();
        long l = this.random.nextLong();
        for (ServerPlayerEntity lv : world.getPlayers()) {
            Vec3d lv2 = lv.getEntityPos();
            Vec3d lv3 = Vec3d.ofCenter(pos);
            double d = Math.sqrt((lv3.x - lv2.x) * (lv3.x - lv2.x) + (lv3.z - lv2.z) * (lv3.z - lv2.z));
            double e = lv2.x + 13.0 / d * (lv3.x - lv2.x);
            double g = lv2.z + 13.0 / d * (lv3.z - lv2.z);
            if (!(d <= 64.0) && !collection.contains(lv)) continue;
            lv.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.EVENT_RAID_HORN, SoundCategory.NEUTRAL, e, lv.getY(), g, 64.0f, 1.0f, l));
        }
    }

    private void spawnNextWave(ServerWorld world, BlockPos pos) {
        boolean bl = false;
        int i = this.wavesSpawned + 1;
        this.totalHealth = 0.0f;
        LocalDifficulty lv = world.getLocalDifficulty(pos);
        boolean bl2 = this.isSpawningExtraWave();
        for (Member lv2 : Member.VALUES) {
            RaiderEntity lv3;
            int j = this.getCount(lv2, i, bl2) + this.getBonusCount(lv2, this.random, i, lv, bl2);
            int k = 0;
            for (int l = 0; l < j && (lv3 = lv2.type.create(world, SpawnReason.EVENT)) != null; ++l) {
                if (!bl && lv3.canLead()) {
                    lv3.setPatrolLeader(true);
                    this.setWaveCaptain(i, lv3);
                    bl = true;
                }
                this.addRaider(world, i, lv3, pos, false);
                if (lv2.type != EntityType.RAVAGER) continue;
                RaiderEntity lv4 = null;
                if (i == this.getMaxWaves(Difficulty.NORMAL)) {
                    lv4 = EntityType.PILLAGER.create(world, SpawnReason.EVENT);
                } else if (i >= this.getMaxWaves(Difficulty.HARD)) {
                    lv4 = k == 0 ? (RaiderEntity)EntityType.EVOKER.create(world, SpawnReason.EVENT) : (RaiderEntity)EntityType.VINDICATOR.create(world, SpawnReason.EVENT);
                }
                ++k;
                if (lv4 == null) continue;
                this.addRaider(world, i, lv4, pos, false);
                lv4.refreshPositionAndAngles(pos, 0.0f, 0.0f);
                lv4.startRiding(lv3, false, false);
            }
        }
        this.preCalculatedRaidersSpawnLocation = Optional.empty();
        ++this.wavesSpawned;
        this.updateBar();
        this.markDirty(world);
    }

    public void addRaider(ServerWorld world, int wave, RaiderEntity raider, @Nullable BlockPos pos, boolean existing) {
        boolean bl2 = this.addToWave(world, wave, raider);
        if (bl2) {
            raider.setRaid(this);
            raider.setWave(wave);
            raider.setAbleToJoinRaid(true);
            raider.setOutOfRaidCounter(0);
            if (!existing && pos != null) {
                raider.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
                raider.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
                raider.addBonusForWave(world, wave, false);
                raider.setOnGround(true);
                world.spawnEntityAndPassengers(raider);
            }
        }
    }

    public void updateBar() {
        this.bar.setPercent(MathHelper.clamp(this.getCurrentRaiderHealth() / this.totalHealth, 0.0f, 1.0f));
    }

    public float getCurrentRaiderHealth() {
        float f = 0.0f;
        for (Set<RaiderEntity> set : this.waveToRaiders.values()) {
            for (RaiderEntity lv : set) {
                f += lv.getHealth();
            }
        }
        return f;
    }

    private boolean canSpawnRaiders() {
        return this.preRaidTicks == 0 && (this.wavesSpawned < this.waveCount || this.isSpawningExtraWave()) && this.getRaiderCount() == 0;
    }

    public int getRaiderCount() {
        return this.waveToRaiders.values().stream().mapToInt(Set::size).sum();
    }

    public void removeFromWave(ServerWorld world, RaiderEntity raider, boolean countHealth) {
        boolean bl2;
        Set<RaiderEntity> set = this.waveToRaiders.get(raider.getWave());
        if (set != null && (bl2 = set.remove(raider))) {
            if (countHealth) {
                this.totalHealth -= raider.getHealth();
            }
            raider.setRaid(null);
            this.updateBar();
            this.markDirty(world);
        }
    }

    private void markDirty(ServerWorld world) {
        world.getRaidManager().markDirty();
    }

    public static ItemStack createOminousBanner(RegistryEntryLookup<BannerPattern> bannerPatternLookup) {
        ItemStack lv = new ItemStack(Items.WHITE_BANNER);
        BannerPatternsComponent lv2 = new BannerPatternsComponent.Builder().add(bannerPatternLookup, BannerPatterns.RHOMBUS, DyeColor.CYAN).add(bannerPatternLookup, BannerPatterns.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.STRIPE_CENTER, DyeColor.GRAY).add(bannerPatternLookup, BannerPatterns.BORDER, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.STRIPE_MIDDLE, DyeColor.BLACK).add(bannerPatternLookup, BannerPatterns.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.CIRCLE, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.BORDER, DyeColor.BLACK).build();
        lv.set(DataComponentTypes.BANNER_PATTERNS, lv2);
        lv.set(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT.with(DataComponentTypes.BANNER_PATTERNS, true));
        lv.set(DataComponentTypes.ITEM_NAME, OMINOUS_BANNER_TRANSLATION_KEY);
        lv.set(DataComponentTypes.RARITY, Rarity.UNCOMMON);
        return lv;
    }

    @Nullable
    public RaiderEntity getCaptain(int wave) {
        return this.waveToCaptain.get(wave);
    }

    @Nullable
    private BlockPos findRandomRaidersSpawnLocation(ServerWorld world, int proximity) {
        int j = this.preRaidTicks / 20;
        float f = 0.22f * (float)j - 0.24f;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        float g = world.random.nextFloat() * ((float)Math.PI * 2);
        for (int k = 0; k < proximity; ++k) {
            int m;
            float h = g + (float)Math.PI * (float)k / 8.0f;
            int l = this.center.getX() + MathHelper.floor(MathHelper.cos(h) * 32.0f * f) + world.random.nextInt(3) * MathHelper.floor(f);
            int n = world.getTopY(Heightmap.Type.WORLD_SURFACE, l, m = this.center.getZ() + MathHelper.floor(MathHelper.sin(h) * 32.0f * f) + world.random.nextInt(3) * MathHelper.floor(f));
            if (MathHelper.abs(n - this.center.getY()) > 96) continue;
            lv.set(l, n, m);
            if (world.isNearOccupiedPointOfInterest(lv) && j > 7) continue;
            int o = 10;
            if (!world.isRegionLoaded(lv.getX() - 10, lv.getZ() - 10, lv.getX() + 10, lv.getZ() + 10) || !world.shouldTickEntityAt(lv) || !RAVAGER_SPAWN_LOCATION.isSpawnPositionOk(world, lv, EntityType.RAVAGER) && (!world.getBlockState((BlockPos)lv.down()).isOf(Blocks.SNOW) || !world.getBlockState(lv).isAir())) continue;
            return lv;
        }
        return null;
    }

    private boolean addToWave(ServerWorld world, int wave, RaiderEntity raider) {
        return this.addToWave(world, wave, raider, true);
    }

    public boolean addToWave(ServerWorld world, int wave, RaiderEntity raider, boolean countHealth) {
        this.waveToRaiders.computeIfAbsent(wave, wavex -> Sets.newHashSet());
        Set<RaiderEntity> set = this.waveToRaiders.get(wave);
        RaiderEntity lv = null;
        for (RaiderEntity lv2 : set) {
            if (!lv2.getUuid().equals(raider.getUuid())) continue;
            lv = lv2;
            break;
        }
        if (lv != null) {
            set.remove(lv);
            set.add(raider);
        }
        set.add(raider);
        if (countHealth) {
            this.totalHealth += raider.getHealth();
        }
        this.updateBar();
        this.markDirty(world);
        return true;
    }

    public void setWaveCaptain(int wave, RaiderEntity entity) {
        this.waveToCaptain.put(wave, entity);
        entity.equipStack(EquipmentSlot.HEAD, Raid.createOminousBanner(entity.getRegistryManager().getOrThrow(RegistryKeys.BANNER_PATTERN)));
        entity.setEquipmentDropChance(EquipmentSlot.HEAD, 2.0f);
    }

    public void removeLeader(int wave) {
        this.waveToCaptain.remove(wave);
    }

    public BlockPos getCenter() {
        return this.center;
    }

    private void setCenter(BlockPos center) {
        this.center = center;
    }

    private int getCount(Member member, int wave, boolean extra) {
        return extra ? member.countInWave[this.waveCount] : member.countInWave[wave];
    }

    private int getBonusCount(Member member, Random random, int wave, LocalDifficulty localDifficulty, boolean extra) {
        int j;
        Difficulty lv = localDifficulty.getGlobalDifficulty();
        boolean bl2 = lv == Difficulty.EASY;
        boolean bl3 = lv == Difficulty.NORMAL;
        switch (member.ordinal()) {
            case 3: {
                if (!bl2 && wave > 2 && wave != 4) {
                    j = 1;
                    break;
                }
                return 0;
            }
            case 0: 
            case 2: {
                if (bl2) {
                    j = random.nextInt(2);
                    break;
                }
                if (bl3) {
                    j = 1;
                    break;
                }
                j = 2;
                break;
            }
            case 4: {
                j = !bl2 && extra ? 1 : 0;
                break;
            }
            default: {
                return 0;
            }
        }
        return j > 0 ? random.nextInt(j + 1) : 0;
    }

    public boolean isActive() {
        return this.active;
    }

    public int getMaxWaves(Difficulty difficulty) {
        return switch (difficulty) {
            default -> throw new MatchException(null, null);
            case Difficulty.PEACEFUL -> 0;
            case Difficulty.EASY -> 3;
            case Difficulty.NORMAL -> 5;
            case Difficulty.HARD -> 7;
        };
    }

    public float getEnchantmentChance() {
        int i = this.getBadOmenLevel();
        if (i == 2) {
            return 0.1f;
        }
        if (i == 3) {
            return 0.25f;
        }
        if (i == 4) {
            return 0.5f;
        }
        if (i == 5) {
            return 0.75f;
        }
        return 0.0f;
    }

    public void addHero(Entity entity) {
        this.heroesOfTheVillage.add(entity.getUuid());
    }

    static enum Status implements StringIdentifiable
    {
        ONGOING("ongoing"),
        VICTORY("victory"),
        LOSS("loss"),
        STOPPED("stopped");

        public static final Codec<Status> CODEC;
        private final String id;

        private Status(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Status::values);
        }
    }

    static enum Member {
        VINDICATOR(EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}),
        EVOKER(EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}),
        PILLAGER(EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}),
        WITCH(EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}),
        RAVAGER(EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

        static final Member[] VALUES;
        final EntityType<? extends RaiderEntity> type;
        final int[] countInWave;

        private Member(EntityType<? extends RaiderEntity> type, int[] countInWave) {
            this.type = type;
            this.countInWave = countInWave;
        }

        static {
            VALUES = Member.values();
        }
    }
}

