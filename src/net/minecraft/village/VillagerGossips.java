/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.village;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.Uuids;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.VillagerGossipType;

public class VillagerGossips {
    public static final Codec<VillagerGossips> CODEC = GossipEntry.CODEC.listOf().xmap(VillagerGossips::new, gossips -> gossips.entries().toList());
    public static final int field_30236 = 2;
    private final Map<UUID, Reputation> entityReputation = new HashMap<UUID, Reputation>();

    public VillagerGossips() {
    }

    private VillagerGossips(List<GossipEntry> gossips) {
        gossips.forEach(gossip -> this.getReputationFor((UUID)gossip.target).associatedGossip.put(gossip.type, gossip.value));
    }

    @Debug
    public Map<UUID, Object2IntMap<VillagerGossipType>> getEntityReputationAssociatedGossips() {
        HashMap<UUID, Object2IntMap<VillagerGossipType>> map = Maps.newHashMap();
        this.entityReputation.keySet().forEach(uuid -> {
            Reputation lv = this.entityReputation.get(uuid);
            map.put((UUID)uuid, lv.associatedGossip);
        });
        return map;
    }

    public void decay() {
        Iterator<Reputation> iterator = this.entityReputation.values().iterator();
        while (iterator.hasNext()) {
            Reputation lv = iterator.next();
            lv.decay();
            if (!lv.isObsolete()) continue;
            iterator.remove();
        }
    }

    private Stream<GossipEntry> entries() {
        return this.entityReputation.entrySet().stream().flatMap(entry -> ((Reputation)entry.getValue()).entriesFor((UUID)entry.getKey()));
    }

    private Collection<GossipEntry> pickGossips(Random random, int count) {
        List<GossipEntry> list = this.entries().toList();
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        int[] is = new int[list.size()];
        int j = 0;
        for (int k = 0; k < list.size(); ++k) {
            GossipEntry lv = list.get(k);
            is[k] = (j += Math.abs(lv.getValue())) - 1;
        }
        Set<GossipEntry> set = Sets.newIdentityHashSet();
        for (int l = 0; l < count; ++l) {
            int m = random.nextInt(j);
            int n = Arrays.binarySearch(is, m);
            set.add(list.get(n < 0 ? -n - 1 : n));
        }
        return set;
    }

    private Reputation getReputationFor(UUID target) {
        return this.entityReputation.computeIfAbsent(target, uuid -> new Reputation());
    }

    public void shareGossipFrom(VillagerGossips from, Random random, int count) {
        Collection<GossipEntry> collection = from.pickGossips(random, count);
        collection.forEach(gossip -> {
            int i = gossip.value - gossip.type.shareDecrement;
            if (i >= 2) {
                this.getReputationFor((UUID)gossip.target).associatedGossip.mergeInt(gossip.type, i, VillagerGossips::max);
            }
        });
    }

    public int getReputationFor(UUID target, Predicate<VillagerGossipType> gossipTypeFilter) {
        Reputation lv = this.entityReputation.get(target);
        return lv != null ? lv.getValueFor(gossipTypeFilter) : 0;
    }

    public long getReputationCount(VillagerGossipType type, DoublePredicate predicate) {
        return this.entityReputation.values().stream().filter(reputation -> predicate.test(reputation.associatedGossip.getOrDefault((Object)type, 0) * arg.multiplier)).count();
    }

    public void startGossip(UUID target, VillagerGossipType type, int value) {
        Reputation lv = this.getReputationFor(target);
        lv.associatedGossip.mergeInt(type, value, (left, right) -> this.mergeReputation(type, left, right));
        lv.clamp(type);
        if (lv.isObsolete()) {
            this.entityReputation.remove(target);
        }
    }

    public void removeGossip(UUID target, VillagerGossipType type, int value) {
        this.startGossip(target, type, -value);
    }

    public void remove(UUID target, VillagerGossipType type) {
        Reputation lv = this.entityReputation.get(target);
        if (lv != null) {
            lv.remove(type);
            if (lv.isObsolete()) {
                this.entityReputation.remove(target);
            }
        }
    }

    public void remove(VillagerGossipType type) {
        Iterator<Reputation> iterator = this.entityReputation.values().iterator();
        while (iterator.hasNext()) {
            Reputation lv = iterator.next();
            lv.remove(type);
            if (!lv.isObsolete()) continue;
            iterator.remove();
        }
    }

    public void clear() {
        this.entityReputation.clear();
    }

    public void add(VillagerGossips gossips) {
        gossips.entityReputation.forEach((target, reputation) -> this.getReputationFor((UUID)target).associatedGossip.putAll(reputation.associatedGossip));
    }

    private static int max(int left, int right) {
        return Math.max(left, right);
    }

    private int mergeReputation(VillagerGossipType type, int left, int right) {
        int k = left + right;
        return k > type.maxValue ? Math.max(type.maxValue, left) : k;
    }

    public VillagerGossips copy() {
        VillagerGossips lv = new VillagerGossips();
        lv.add(this);
        return lv;
    }

    static class Reputation {
        final Object2IntMap<VillagerGossipType> associatedGossip = new Object2IntOpenHashMap<VillagerGossipType>();

        Reputation() {
        }

        public int getValueFor(Predicate<VillagerGossipType> gossipTypeFilter) {
            return this.associatedGossip.object2IntEntrySet().stream().filter(entry -> gossipTypeFilter.test((VillagerGossipType)entry.getKey())).mapToInt(entry -> entry.getIntValue() * ((VillagerGossipType)entry.getKey()).multiplier).sum();
        }

        public Stream<GossipEntry> entriesFor(UUID target) {
            return this.associatedGossip.object2IntEntrySet().stream().map(entry -> new GossipEntry(target, (VillagerGossipType)entry.getKey(), entry.getIntValue()));
        }

        public void decay() {
            Iterator objectIterator = this.associatedGossip.object2IntEntrySet().iterator();
            while (objectIterator.hasNext()) {
                Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
                int i = entry.getIntValue() - ((VillagerGossipType)entry.getKey()).decay;
                if (i < 2) {
                    objectIterator.remove();
                    continue;
                }
                entry.setValue(i);
            }
        }

        public boolean isObsolete() {
            return this.associatedGossip.isEmpty();
        }

        public void clamp(VillagerGossipType gossipType) {
            int i = this.associatedGossip.getInt(gossipType);
            if (i > gossipType.maxValue) {
                this.associatedGossip.put(gossipType, gossipType.maxValue);
            }
            if (i < 2) {
                this.remove(gossipType);
            }
        }

        public void remove(VillagerGossipType gossipType) {
            this.associatedGossip.removeInt(gossipType);
        }
    }

    record GossipEntry(UUID target, VillagerGossipType type, int value) {
        public static final Codec<GossipEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Uuids.INT_STREAM_CODEC.fieldOf("Target")).forGetter(GossipEntry::target), ((MapCodec)VillagerGossipType.CODEC.fieldOf("Type")).forGetter(GossipEntry::type), ((MapCodec)Codecs.POSITIVE_INT.fieldOf("Value")).forGetter(GossipEntry::value)).apply((Applicative<GossipEntry, ?>)instance, GossipEntry::new));

        public int getValue() {
            return this.value * this.type.multiplier;
        }
    }
}

