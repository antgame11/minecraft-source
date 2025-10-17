/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkUpdateState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FeatureUpdater {
    private static final Map<String, String> OLD_TO_NEW = Util.make(Maps.newHashMap(), map -> {
        map.put("Village", "Village");
        map.put("Mineshaft", "Mineshaft");
        map.put("Mansion", "Mansion");
        map.put("Igloo", "Temple");
        map.put("Desert_Pyramid", "Temple");
        map.put("Jungle_Pyramid", "Temple");
        map.put("Swamp_Hut", "Temple");
        map.put("Stronghold", "Stronghold");
        map.put("Monument", "Monument");
        map.put("Fortress", "Fortress");
        map.put("EndCity", "EndCity");
    });
    private static final Map<String, String> ANCIENT_TO_OLD = Util.make(Maps.newHashMap(), map -> {
        map.put("Iglu", "Igloo");
        map.put("TeDP", "Desert_Pyramid");
        map.put("TeJP", "Jungle_Pyramid");
        map.put("TeSH", "Swamp_Hut");
    });
    private static final Set<String> NEW_STRUCTURE_NAMES = Set.of("pillager_outpost", "mineshaft", "mansion", "jungle_pyramid", "desert_pyramid", "igloo", "ruined_portal", "shipwreck", "swamp_hut", "stronghold", "monument", "ocean_ruin", "fortress", "endcity", "buried_treasure", "village", "nether_fossil", "bastion_remnant");
    private final boolean needsUpdate;
    private final Map<String, Long2ObjectMap<NbtCompound>> featureIdToChunkNbt = Maps.newHashMap();
    private final Map<String, ChunkUpdateState> updateStates = Maps.newHashMap();
    private final List<String> oldNames;
    private final List<String> newNames;

    public FeatureUpdater(@Nullable PersistentStateManager persistentStateManager, List<String> oldNames, List<String> newNames) {
        this.oldNames = oldNames;
        this.newNames = newNames;
        this.init(persistentStateManager);
        boolean bl = false;
        for (String string : this.newNames) {
            bl |= this.featureIdToChunkNbt.get(string) != null;
        }
        this.needsUpdate = bl;
    }

    public void markResolved(long chunkPos) {
        for (String string : this.oldNames) {
            ChunkUpdateState lv = this.updateStates.get(string);
            if (lv == null || !lv.isRemaining(chunkPos)) continue;
            lv.markResolved(chunkPos);
        }
    }

    public NbtCompound getUpdatedReferences(NbtCompound nbt) {
        NbtCompound lv = nbt.getCompoundOrEmpty("Level");
        ChunkPos lv2 = new ChunkPos(lv.getInt("xPos", 0), lv.getInt("zPos", 0));
        if (this.needsUpdate(lv2.x, lv2.z)) {
            nbt = this.getUpdatedStarts(nbt, lv2);
        }
        NbtCompound lv3 = lv.getCompoundOrEmpty("Structures");
        NbtCompound lv4 = lv3.getCompoundOrEmpty("References");
        for (String string : this.newNames) {
            boolean bl = NEW_STRUCTURE_NAMES.contains(string.toLowerCase(Locale.ROOT));
            if (lv4.getLongArray(string).isPresent() || !bl) continue;
            int i = 8;
            LongArrayList longList = new LongArrayList();
            for (int j = lv2.x - 8; j <= lv2.x + 8; ++j) {
                for (int k = lv2.z - 8; k <= lv2.z + 8; ++k) {
                    if (!this.needsUpdate(j, k, string)) continue;
                    longList.add(ChunkPos.toLong(j, k));
                }
            }
            lv4.putLongArray(string, longList.toLongArray());
        }
        lv3.put("References", lv4);
        lv.put("Structures", lv3);
        nbt.put("Level", lv);
        return nbt;
    }

    private boolean needsUpdate(int chunkX, int chunkZ, String id) {
        if (!this.needsUpdate) {
            return false;
        }
        return this.featureIdToChunkNbt.get(id) != null && this.updateStates.get(OLD_TO_NEW.get(id)).contains(ChunkPos.toLong(chunkX, chunkZ));
    }

    private boolean needsUpdate(int chunkX, int chunkZ) {
        if (!this.needsUpdate) {
            return false;
        }
        for (String string : this.newNames) {
            if (this.featureIdToChunkNbt.get(string) == null || !this.updateStates.get(OLD_TO_NEW.get(string)).isRemaining(ChunkPos.toLong(chunkX, chunkZ))) continue;
            return true;
        }
        return false;
    }

    private NbtCompound getUpdatedStarts(NbtCompound nbt, ChunkPos pos) {
        NbtCompound lv = nbt.getCompoundOrEmpty("Level");
        NbtCompound lv2 = lv.getCompoundOrEmpty("Structures");
        NbtCompound lv3 = lv2.getCompoundOrEmpty("Starts");
        for (String string : this.newNames) {
            NbtCompound lv4;
            Long2ObjectMap<NbtCompound> long2ObjectMap = this.featureIdToChunkNbt.get(string);
            if (long2ObjectMap == null) continue;
            long l = pos.toLong();
            if (!this.updateStates.get(OLD_TO_NEW.get(string)).isRemaining(l) || (lv4 = (NbtCompound)long2ObjectMap.get(l)) == null) continue;
            lv3.put(string, lv4);
        }
        lv2.put("Starts", lv3);
        lv.put("Structures", lv2);
        nbt.put("Level", lv);
        return nbt;
    }

    private void init(@Nullable PersistentStateManager persistentStateManager) {
        if (persistentStateManager == null) {
            return;
        }
        for (String string : this.oldNames) {
            NbtCompound lv = new NbtCompound();
            try {
                lv = persistentStateManager.readNbt(string, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES, 1493).getCompoundOrEmpty("data").getCompoundOrEmpty("Features");
                if (lv.isEmpty()) {
                    continue;
                }
            } catch (IOException iOException) {
                // empty catch block
            }
            lv.forEach((key, nbt) -> {
                if (!(nbt instanceof NbtCompound)) {
                    return;
                }
                NbtCompound lv = (NbtCompound)nbt;
                long l = ChunkPos.toLong(lv.getInt("ChunkX", 0), lv.getInt("ChunkZ", 0));
                NbtList lv2 = lv.getListOrEmpty("Children");
                if (!lv2.isEmpty()) {
                    Optional<String> optional = lv2.getCompound(0).flatMap(child -> child.getString("id"));
                    optional.map(ANCIENT_TO_OLD::get).ifPresent(id -> lv.putString("id", (String)id));
                }
                lv.getString("id").ifPresent(id -> this.featureIdToChunkNbt.computeIfAbsent((String)id, featureId -> new Long2ObjectOpenHashMap()).put(l, lv));
            });
            String string2 = string + "_index";
            ChunkUpdateState lv2 = persistentStateManager.getOrCreate(ChunkUpdateState.createStateType(string2));
            if (lv2.getAll().isEmpty()) {
                ChunkUpdateState lv3 = new ChunkUpdateState();
                this.updateStates.put(string, lv3);
                lv.forEach((key, nbt) -> {
                    if (nbt instanceof NbtCompound) {
                        NbtCompound lv = (NbtCompound)nbt;
                        lv3.add(ChunkPos.toLong(lv.getInt("ChunkX", 0), lv.getInt("ChunkZ", 0)));
                    }
                });
                continue;
            }
            this.updateStates.put(string, lv2);
        }
    }

    public static FeatureUpdater create(RegistryKey<World> world, @Nullable PersistentStateManager persistentStateManager) {
        if (world == World.OVERWORLD) {
            return new FeatureUpdater(persistentStateManager, ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"), ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument"));
        }
        if (world == World.NETHER) {
            ImmutableList<String> list = ImmutableList.of("Fortress");
            return new FeatureUpdater(persistentStateManager, list, list);
        }
        if (world == World.END) {
            ImmutableList<String> list = ImmutableList.of("EndCity");
            return new FeatureUpdater(persistentStateManager, list, list);
        }
        throw new RuntimeException(String.format(Locale.ROOT, "Unknown dimension type : %s", world));
    }
}

