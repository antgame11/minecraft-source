/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.village.raid;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

public class RaidManager
extends PersistentState {
    private static final String RAIDS = "raids";
    public static final Codec<RaidManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(RaidWithId.CODEC.listOf().optionalFieldOf(RAIDS, List.of()).forGetter(raidManager -> raidManager.raids.int2ObjectEntrySet().stream().map(RaidWithId::fromMapEntry).toList()), ((MapCodec)Codec.INT.fieldOf("next_id")).forGetter(raidManager -> raidManager.nextAvailableId), ((MapCodec)Codec.INT.fieldOf("tick")).forGetter(raidManager -> raidManager.currentTime)).apply((Applicative<RaidManager, ?>)instance, RaidManager::new));
    public static final PersistentStateType<RaidManager> STATE_TYPE = new PersistentStateType<RaidManager>("raids", RaidManager::new, CODEC, DataFixTypes.SAVED_DATA_RAIDS);
    public static final PersistentStateType<RaidManager> END_STATE_TYPE = new PersistentStateType<RaidManager>("raids_end", RaidManager::new, CODEC, DataFixTypes.SAVED_DATA_RAIDS);
    private final Int2ObjectMap<Raid> raids = new Int2ObjectOpenHashMap<Raid>();
    private int nextAvailableId = 1;
    private int currentTime;

    public static PersistentStateType<RaidManager> getPersistentStateType(RegistryEntry<DimensionType> dimensionType) {
        if (dimensionType.matchesKey(DimensionTypes.THE_END)) {
            return END_STATE_TYPE;
        }
        return STATE_TYPE;
    }

    public RaidManager() {
        this.markDirty();
    }

    private RaidManager(List<RaidWithId> raids, int nextAvailableId, int currentTime) {
        for (RaidWithId lv : raids) {
            this.raids.put(lv.id, lv.raid);
        }
        this.nextAvailableId = nextAvailableId;
        this.currentTime = currentTime;
    }

    @Nullable
    public Raid getRaid(int id) {
        return (Raid)this.raids.get(id);
    }

    public OptionalInt getRaidId(Raid raid) {
        for (Int2ObjectMap.Entry entry : this.raids.int2ObjectEntrySet()) {
            if (entry.getValue() != raid) continue;
            return OptionalInt.of(entry.getIntKey());
        }
        return OptionalInt.empty();
    }

    public void tick(ServerWorld world) {
        ++this.currentTime;
        Iterator iterator = this.raids.values().iterator();
        while (iterator.hasNext()) {
            Raid lv = (Raid)iterator.next();
            if (world.getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
                lv.invalidate();
            }
            if (lv.hasStopped()) {
                iterator.remove();
                this.markDirty();
                continue;
            }
            lv.tick(world);
        }
        if (this.currentTime % 200 == 0) {
            this.markDirty();
        }
    }

    public static boolean isValidRaiderFor(RaiderEntity raider) {
        return raider.isAlive() && raider.canJoinRaid() && raider.getDespawnCounter() <= 2400;
    }

    @Nullable
    public Raid startRaid(ServerPlayerEntity player, BlockPos pos) {
        BlockPos lv6;
        if (player.isSpectator()) {
            return null;
        }
        ServerWorld lv = player.getEntityWorld();
        if (lv.getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
            return null;
        }
        DimensionType lv2 = lv.getDimension();
        if (!lv2.hasRaids()) {
            return null;
        }
        List<PointOfInterest> list = lv.getPointOfInterestStorage().getInCircle(poiType -> poiType.isIn(PointOfInterestTypeTags.VILLAGE), pos, 64, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED).toList();
        int i = 0;
        Vec3d lv3 = Vec3d.ZERO;
        for (PointOfInterest lv4 : list) {
            BlockPos lv5 = lv4.getPos();
            lv3 = lv3.add(lv5.getX(), lv5.getY(), lv5.getZ());
            ++i;
        }
        if (i > 0) {
            lv3 = lv3.multiply(1.0 / (double)i);
            lv6 = BlockPos.ofFloored(lv3);
        } else {
            lv6 = pos;
        }
        Raid lv7 = this.getOrCreateRaid(lv, lv6);
        if (!lv7.hasStarted() && !this.raids.containsValue(lv7)) {
            this.raids.put(this.nextId(), lv7);
        }
        if (!lv7.hasStarted() || lv7.getBadOmenLevel() < lv7.getMaxAcceptableBadOmenLevel()) {
            lv7.start(player);
        }
        this.markDirty();
        return lv7;
    }

    private Raid getOrCreateRaid(ServerWorld world, BlockPos pos) {
        Raid lv = world.getRaidAt(pos);
        return lv != null ? lv : new Raid(pos, world.getDifficulty());
    }

    public static RaidManager fromNbt(NbtCompound nbt) {
        return CODEC.parse(NbtOps.INSTANCE, nbt).resultOrPartial().orElseGet(RaidManager::new);
    }

    private int nextId() {
        return ++this.nextAvailableId;
    }

    @Nullable
    public Raid getRaidAt(BlockPos pos, int searchDistance) {
        Raid lv = null;
        double d = searchDistance;
        for (Raid lv2 : this.raids.values()) {
            double e = lv2.getCenter().getSquaredDistance(pos);
            if (!lv2.isActive() || !(e < d)) continue;
            lv = lv2;
            d = e;
        }
        return lv;
    }

    @Debug
    public List<BlockPos> getRaidCenters(ChunkPos chunkPos) {
        return this.raids.values().stream().map(Raid::getCenter).filter(chunkPos::contains).toList();
    }

    record RaidWithId(int id, Raid raid) {
        public static final Codec<RaidWithId> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("id")).forGetter(RaidWithId::id), Raid.CODEC.forGetter(RaidWithId::raid)).apply((Applicative<RaidWithId, ?>)instance, RaidWithId::new));

        public static RaidWithId fromMapEntry(Int2ObjectMap.Entry<Raid> entry) {
            return new RaidWithId(entry.getIntKey(), (Raid)entry.getValue());
        }
    }
}

