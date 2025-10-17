/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkTicketManager
extends PersistentState {
    private static final int DEFAULT_TICKETS_MAP_SIZE = 4;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Codec<Pair<ChunkPos, ChunkTicket>> TICKET_POS_CODEC = Codec.mapPair(ChunkPos.CODEC.fieldOf("chunk_pos"), ChunkTicket.CODEC).codec();
    public static final Codec<ChunkTicketManager> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<O> instance) -> instance.group(TICKET_POS_CODEC.listOf().optionalFieldOf("tickets", List.of()).forGetter(ChunkTicketManager::getTickets)).apply((Applicative<ChunkTicketManager, ?>)instance, ChunkTicketManager::create));
    public static final PersistentStateType<ChunkTicketManager> STATE_TYPE = new PersistentStateType<ChunkTicketManager>("chunks", ChunkTicketManager::new, CODEC, DataFixTypes.SAVED_DATA_FORCED_CHUNKS);
    private final Long2ObjectOpenHashMap<List<ChunkTicket>> tickets;
    private final Long2ObjectOpenHashMap<List<ChunkTicket>> savedTickets;
    private LongSet forcedChunks = new LongOpenHashSet();
    @Nullable
    private LevelUpdater loadingLevelUpdater;
    @Nullable
    private LevelUpdater simulationLevelUpdater;

    private ChunkTicketManager(Long2ObjectOpenHashMap<List<ChunkTicket>> tickets, Long2ObjectOpenHashMap<List<ChunkTicket>> savedTickets) {
        this.tickets = tickets;
        this.savedTickets = savedTickets;
        this.recomputeForcedChunks();
    }

    public ChunkTicketManager() {
        this(new Long2ObjectOpenHashMap<List<ChunkTicket>>(4), new Long2ObjectOpenHashMap<List<ChunkTicket>>());
    }

    private static ChunkTicketManager create(List<Pair<ChunkPos, ChunkTicket>> tickets) {
        Long2ObjectOpenHashMap<List<ChunkTicket>> long2ObjectOpenHashMap = new Long2ObjectOpenHashMap<List<ChunkTicket>>();
        for (Pair<ChunkPos, ChunkTicket> pair : tickets) {
            ChunkPos lv = pair.getFirst();
            List list2 = long2ObjectOpenHashMap.computeIfAbsent(lv.toLong(), l -> new ObjectArrayList(4));
            list2.add(pair.getSecond());
        }
        return new ChunkTicketManager(new Long2ObjectOpenHashMap<List<ChunkTicket>>(4), long2ObjectOpenHashMap);
    }

    private List<Pair<ChunkPos, ChunkTicket>> getTickets() {
        ArrayList<Pair<ChunkPos, ChunkTicket>> list = new ArrayList<Pair<ChunkPos, ChunkTicket>>();
        this.forEachTicket((pos, ticket) -> {
            if (ticket.getType().shouldSerialize()) {
                list.add(new Pair<ChunkPos, ChunkTicket>((ChunkPos)pos, (ChunkTicket)ticket));
            }
        });
        return list;
    }

    private void forEachTicket(BiConsumer<ChunkPos, ChunkTicket> ticketConsumer) {
        ChunkTicketManager.forEachTicket(ticketConsumer, this.tickets);
        ChunkTicketManager.forEachTicket(ticketConsumer, this.savedTickets);
    }

    private static void forEachTicket(BiConsumer<ChunkPos, ChunkTicket> ticketConsumer, Long2ObjectOpenHashMap<List<ChunkTicket>> tickets) {
        for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(tickets)) {
            ChunkPos lv = new ChunkPos(entry.getLongKey());
            for (ChunkTicket lv2 : (List)entry.getValue()) {
                ticketConsumer.accept(lv, lv2);
            }
        }
    }

    public void promoteToRealTickets() {
        for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(this.savedTickets)) {
            for (ChunkTicket lv : (List)entry.getValue()) {
                this.addTicket(entry.getLongKey(), lv);
            }
        }
        this.savedTickets.clear();
    }

    public void setLoadingLevelUpdater(@Nullable LevelUpdater loadingLevelUpdater) {
        this.loadingLevelUpdater = loadingLevelUpdater;
    }

    public void setSimulationLevelUpdater(@Nullable LevelUpdater simulationLevelUpdater) {
        this.simulationLevelUpdater = simulationLevelUpdater;
    }

    public boolean hasTickets() {
        return !this.tickets.isEmpty();
    }

    public boolean shouldResetIdleTimeout() {
        for (List list : this.tickets.values()) {
            for (ChunkTicket lv : list) {
                if (!lv.getType().resetsIdleTimeout()) continue;
                return true;
            }
        }
        return false;
    }

    public List<ChunkTicket> getTickets(long pos) {
        return this.tickets.getOrDefault(pos, List.of());
    }

    private List<ChunkTicket> getTicketsMutable(long pos) {
        return this.tickets.computeIfAbsent(pos, chunkPos -> new ObjectArrayList(4));
    }

    public void addTicket(ChunkTicketType type, ChunkPos pos, int radius) {
        ChunkTicket lv = new ChunkTicket(type, ChunkLevels.getLevelFromType(ChunkLevelType.FULL) - radius);
        this.addTicket(pos.toLong(), lv);
    }

    public void addTicket(ChunkTicket ticket, ChunkPos pos) {
        this.addTicket(pos.toLong(), ticket);
    }

    public boolean addTicket(long pos, ChunkTicket ticket) {
        List<ChunkTicket> list = this.getTicketsMutable(pos);
        for (ChunkTicket lv : list) {
            if (!ChunkTicketManager.ticketsEqual(ticket, lv)) continue;
            lv.refreshExpiry();
            this.markDirty();
            return false;
        }
        int i = ChunkTicketManager.getLevel(list, true);
        int j = ChunkTicketManager.getLevel(list, false);
        list.add(ticket);
        if (SharedConstants.VERBOSE_SERVER_EVENTS) {
            LOGGER.debug("ATI {} {}", (Object)new ChunkPos(pos), (Object)ticket);
        }
        if (ticket.getType().isForSimulation() && ticket.getLevel() < i && this.simulationLevelUpdater != null) {
            this.simulationLevelUpdater.update(pos, ticket.getLevel(), true);
        }
        if (ticket.getType().isForLoading() && ticket.getLevel() < j && this.loadingLevelUpdater != null) {
            this.loadingLevelUpdater.update(pos, ticket.getLevel(), true);
        }
        if (ticket.getType().equals(ChunkTicketType.FORCED)) {
            this.forcedChunks.add(pos);
        }
        this.markDirty();
        return true;
    }

    private static boolean ticketsEqual(ChunkTicket a, ChunkTicket b) {
        return b.getType() == a.getType() && b.getLevel() == a.getLevel();
    }

    public int getLevel(long pos, boolean forSimulation) {
        return ChunkTicketManager.getLevel(this.getTickets(pos), forSimulation);
    }

    private static int getLevel(List<ChunkTicket> tickets, boolean forSimulation) {
        ChunkTicket lv = ChunkTicketManager.getActiveTicket(tickets, forSimulation);
        return lv == null ? ChunkLevels.INACCESSIBLE + 1 : lv.getLevel();
    }

    @Nullable
    private static ChunkTicket getActiveTicket(@Nullable List<ChunkTicket> tickets, boolean forSimulation) {
        if (tickets == null) {
            return null;
        }
        ChunkTicket lv = null;
        for (ChunkTicket lv2 : tickets) {
            if (lv != null && lv2.getLevel() >= lv.getLevel()) continue;
            if (forSimulation && lv2.getType().isForSimulation()) {
                lv = lv2;
                continue;
            }
            if (forSimulation || !lv2.getType().isForLoading()) continue;
            lv = lv2;
        }
        return lv;
    }

    public void removeTicket(ChunkTicketType type, ChunkPos pos, int radius) {
        ChunkTicket lv = new ChunkTicket(type, ChunkLevels.getLevelFromType(ChunkLevelType.FULL) - radius);
        this.removeTicket(pos.toLong(), lv);
    }

    public void removeTicket(ChunkTicket ticket, ChunkPos pos) {
        this.removeTicket(pos.toLong(), ticket);
    }

    public boolean removeTicket(long pos, ChunkTicket ticket) {
        List<ChunkTicket> list = this.tickets.get(pos);
        if (list == null) {
            return false;
        }
        boolean bl = false;
        Iterator<ChunkTicket> iterator = list.iterator();
        while (iterator.hasNext()) {
            ChunkTicket lv = iterator.next();
            if (!ChunkTicketManager.ticketsEqual(ticket, lv)) continue;
            iterator.remove();
            if (SharedConstants.VERBOSE_SERVER_EVENTS) {
                LOGGER.debug("RTI {} {}", (Object)new ChunkPos(pos), (Object)lv);
            }
            bl = true;
            break;
        }
        if (!bl) {
            return false;
        }
        if (list.isEmpty()) {
            this.tickets.remove(pos);
        }
        if (ticket.getType().isForSimulation() && this.simulationLevelUpdater != null) {
            this.simulationLevelUpdater.update(pos, ChunkTicketManager.getLevel(list, true), false);
        }
        if (ticket.getType().isForLoading() && this.loadingLevelUpdater != null) {
            this.loadingLevelUpdater.update(pos, ChunkTicketManager.getLevel(list, false), false);
        }
        if (ticket.getType().equals(ChunkTicketType.FORCED)) {
            this.recomputeForcedChunks();
        }
        this.markDirty();
        return true;
    }

    private void recomputeForcedChunks() {
        this.forcedChunks = this.getAllChunksMatching(ticket -> ticket.getType().equals(ChunkTicketType.FORCED));
    }

    public String getDebugString(long pos, boolean forSimulation) {
        List<ChunkTicket> list = this.getTickets(pos);
        ChunkTicket lv = ChunkTicketManager.getActiveTicket(list, forSimulation);
        return lv == null ? "no_ticket" : lv.toString();
    }

    public void tick(ServerChunkLoadingManager chunkLoadingManager) {
        this.removeTicketsIf((ticket, pos) -> {
            if (this.canTicketExpire(chunkLoadingManager, ticket, pos)) {
                ticket.tick();
                return ticket.isExpired();
            }
            return false;
        }, null);
        this.markDirty();
    }

    private boolean canTicketExpire(ServerChunkLoadingManager chunkLoadingManager, ChunkTicket ticket, long pos) {
        if (!ticket.getType().canExpire()) {
            return false;
        }
        if (ticket.getType().canExpireBeforeLoad()) {
            return true;
        }
        ChunkHolder lv = chunkLoadingManager.getCurrentChunkHolder(pos);
        return lv == null || lv.isSavable();
    }

    public void shutdown() {
        this.removeTicketsIf((ticket, pos) -> ticket.getType() != ChunkTicketType.UNKNOWN, this.savedTickets);
    }

    public void removeTicketsIf(TicketPredicate predicate, @Nullable Long2ObjectOpenHashMap<List<ChunkTicket>> transferTo) {
        ObjectIterator objectIterator = this.tickets.long2ObjectEntrySet().fastIterator();
        boolean bl = false;
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            Iterator iterator = ((List)entry.getValue()).iterator();
            long l = entry.getLongKey();
            boolean bl2 = false;
            boolean bl3 = false;
            while (iterator.hasNext()) {
                ChunkTicket lv = (ChunkTicket)iterator.next();
                if (!predicate.test(lv, l)) continue;
                if (transferTo != null) {
                    List list = transferTo.computeIfAbsent(l, pos -> new ObjectArrayList(((List)entry.getValue()).size()));
                    list.add(lv);
                }
                iterator.remove();
                if (lv.getType().isForLoading()) {
                    bl3 = true;
                }
                if (lv.getType().isForSimulation()) {
                    bl2 = true;
                }
                if (!lv.getType().equals(ChunkTicketType.FORCED)) continue;
                bl = true;
            }
            if (!bl3 && !bl2) continue;
            if (bl3 && this.loadingLevelUpdater != null) {
                this.loadingLevelUpdater.update(l, ChunkTicketManager.getLevel((List)entry.getValue(), false), false);
            }
            if (bl2 && this.simulationLevelUpdater != null) {
                this.simulationLevelUpdater.update(l, ChunkTicketManager.getLevel((List)entry.getValue(), true), false);
            }
            this.markDirty();
            if (!((List)entry.getValue()).isEmpty()) continue;
            objectIterator.remove();
        }
        if (bl) {
            this.recomputeForcedChunks();
        }
    }

    public void updateLevel(int level, ChunkTicketType type) {
        ArrayList<Pair<ChunkTicket, Long>> list = new ArrayList<Pair<ChunkTicket, Long>>();
        for (Long2ObjectMap.Entry entry : this.tickets.long2ObjectEntrySet()) {
            for (ChunkTicket lv : (List)entry.getValue()) {
                if (lv.getType() != type) continue;
                list.add(Pair.of(lv, entry.getLongKey()));
            }
        }
        for (Pair pair : list) {
            ChunkTicket lv;
            Long long_ = (Long)pair.getSecond();
            lv = (ChunkTicket)pair.getFirst();
            this.removeTicket(long_, lv);
            ChunkTicketType lv2 = lv.getType();
            this.addTicket(long_, new ChunkTicket(lv2, level));
        }
    }

    public boolean setChunkForced(ChunkPos pos, boolean forced) {
        ChunkTicket lv = new ChunkTicket(ChunkTicketType.FORCED, ServerChunkLoadingManager.FORCED_CHUNK_LEVEL);
        if (forced) {
            return this.addTicket(pos.toLong(), lv);
        }
        return this.removeTicket(pos.toLong(), lv);
    }

    public LongSet getForcedChunks() {
        return this.forcedChunks;
    }

    private LongSet getAllChunksMatching(Predicate<ChunkTicket> predicate) {
        LongOpenHashSet longOpenHashSet = new LongOpenHashSet();
        block0: for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(this.tickets)) {
            for (ChunkTicket lv : (List)entry.getValue()) {
                if (!predicate.test(lv)) continue;
                longOpenHashSet.add(entry.getLongKey());
                continue block0;
            }
        }
        return longOpenHashSet;
    }

    @FunctionalInterface
    public static interface LevelUpdater {
        public void update(long var1, int var3, boolean var4);
    }

    public static interface TicketPredicate {
        public boolean test(ChunkTicket var1, long var2);
    }
}

