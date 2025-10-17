/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.debug;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockValueDebugS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkValueDebugS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityValueDebugS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.debug.DebugSubscriptionType;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.DebugTrackable;
import net.minecraft.world.debug.data.PoiDebugData;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

public abstract class TrackedSubscription<T> {
    protected final DebugSubscriptionType<T> type;
    private final Set<UUID> subscribingPlayers = new ObjectOpenHashSet<UUID>();

    public TrackedSubscription(DebugSubscriptionType<T> type) {
        this.type = type;
    }

    public final void refreshTracking(ServerWorld world) {
        for (ServerPlayerEntity lv : world.getPlayers()) {
            boolean bl = this.subscribingPlayers.contains(lv.getUuid());
            boolean bl2 = lv.getSubscribedTypes().contains(this.type);
            if (bl2 == bl) continue;
            if (bl2) {
                this.startTracking(lv);
                continue;
            }
            this.subscribingPlayers.remove(lv.getUuid());
        }
        this.subscribingPlayers.removeIf(uuid -> world.getPlayerByUuid((UUID)uuid) == null);
        if (!this.subscribingPlayers.isEmpty()) {
            this.sendUpdate(world);
        }
    }

    private void startTracking(ServerPlayerEntity player) {
        this.subscribingPlayers.add(player.getUuid());
        player.getChunkFilter().forEach(chunkPos -> {
            if (!arg.networkHandler.chunkDataSender.isInNextBatch(chunkPos.toLong())) {
                this.sendInitialIfSubscribed(player, (ChunkPos)chunkPos);
            }
        });
        player.getEntityWorld().getChunkManager().chunkLoadingManager.forEachEntityTrackedBy(player, entity -> this.sendInitialIfSubscribed(player, (Entity)entity));
    }

    protected final void sendToTrackingPlayers(ServerWorld world, ChunkPos chunkPos, Packet<? super ClientPlayPacketListener> packet) {
        ServerChunkLoadingManager lv = world.getChunkManager().chunkLoadingManager;
        for (UUID uUID : this.subscribingPlayers) {
            ServerPlayerEntity lv2;
            PlayerEntity playerEntity = world.getPlayerByUuid(uUID);
            if (!(playerEntity instanceof ServerPlayerEntity) || !lv.isTracked(lv2 = (ServerPlayerEntity)playerEntity, chunkPos.x, chunkPos.z)) continue;
            lv2.networkHandler.sendPacket(packet);
        }
    }

    protected final void sendToTrackingPlayers(ServerWorld world, Entity entity, Packet<? super ClientPlayPacketListener> packet) {
        ServerChunkLoadingManager lv = world.getChunkManager().chunkLoadingManager;
        lv.sendToOtherNearbyPlayersIf(entity, packet, player -> this.subscribingPlayers.contains(player.getUuid()));
    }

    public final void sendInitialIfSubscribed(ServerPlayerEntity player, ChunkPos chunkPos) {
        if (this.subscribingPlayers.contains(player.getUuid())) {
            this.sendInitial(player, chunkPos);
        }
    }

    public final void sendInitialIfSubscribed(ServerPlayerEntity player, Entity entity) {
        if (this.subscribingPlayers.contains(player.getUuid())) {
            this.sendInitial(player, entity);
        }
    }

    protected void clear() {
    }

    protected void sendUpdate(ServerWorld world) {
    }

    protected void sendInitial(ServerPlayerEntity player, ChunkPos chunkPos) {
    }

    protected void sendInitial(ServerPlayerEntity player, Entity entity) {
    }

    public static class TrackedVillageSections
    extends TrackedSubscription<Unit> {
        public TrackedVillageSections() {
            super(DebugSubscriptionTypes.VILLAGE_SECTIONS);
        }

        @Override
        protected void sendInitial(ServerPlayerEntity player, ChunkPos chunkPos) {
            ServerWorld lv = player.getEntityWorld();
            PointOfInterestStorage lv2 = lv.getPointOfInterestStorage();
            lv2.getInChunk(type -> true, chunkPos, PointOfInterestStorage.OccupationStatus.ANY).forEach(poi -> {
                ChunkSectionPos lv = ChunkSectionPos.from(poi.getPos());
                TrackedVillageSections.forEachSurrounding(lv, lv, (sectionPos, nearOccupiedPoi) -> {
                    BlockPos lv = sectionPos.getCenterPos();
                    arg.networkHandler.sendPacket(new BlockValueDebugS2CPacket(lv, this.type.optionalValueFor(nearOccupiedPoi != false ? Unit.INSTANCE : null)));
                });
            });
        }

        public void onPoiAdded(ServerWorld world, PointOfInterest poi) {
            this.handlePoiUpdate(world, poi.getPos());
        }

        public void onPoiRemoved(ServerWorld world, BlockPos pos) {
            this.handlePoiUpdate(world, pos);
        }

        private void handlePoiUpdate(ServerWorld world, BlockPos pos) {
            TrackedVillageSections.forEachSurrounding(world, ChunkSectionPos.from(pos), (sectionPos, nearOccupiedPoi) -> {
                BlockPos lv = sectionPos.getCenterPos();
                if (nearOccupiedPoi.booleanValue()) {
                    this.sendToTrackingPlayers(world, new ChunkPos(lv), (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(lv, this.type.optionalValueFor(Unit.INSTANCE)));
                } else {
                    this.sendToTrackingPlayers(world, new ChunkPos(lv), (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(lv, this.type.optionalValueFor()));
                }
            });
        }

        private static void forEachSurrounding(ServerWorld world, ChunkSectionPos sectionPos, BiConsumer<ChunkSectionPos, Boolean> action) {
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = -1; k <= 1; ++k) {
                        ChunkSectionPos lv = sectionPos.add(j, k, i);
                        if (world.isNearOccupiedPointOfInterest(lv.getCenterPos())) {
                            action.accept(lv, true);
                            continue;
                        }
                        action.accept(lv, false);
                    }
                }
            }
        }
    }

    public static class TrackedPoi
    extends TrackedSubscription<PoiDebugData> {
        public TrackedPoi() {
            super(DebugSubscriptionTypes.POIS);
        }

        @Override
        protected void sendInitial(ServerPlayerEntity player, ChunkPos chunkPos) {
            ServerWorld lv = player.getEntityWorld();
            PointOfInterestStorage lv2 = lv.getPointOfInterestStorage();
            lv2.getInChunk(type -> true, chunkPos, PointOfInterestStorage.OccupationStatus.ANY).forEach(poi -> arg.networkHandler.sendPacket(new BlockValueDebugS2CPacket(poi.getPos(), this.type.optionalValueFor(new PoiDebugData((PointOfInterest)poi)))));
        }

        public void onPoiAdded(ServerWorld world, PointOfInterest poi) {
            this.sendToTrackingPlayers(world, new ChunkPos(poi.getPos()), (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(poi.getPos(), this.type.optionalValueFor(new PoiDebugData(poi))));
        }

        public void onPoiRemoved(ServerWorld world, BlockPos pos) {
            this.sendToTrackingPlayers(world, new ChunkPos(pos), (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(pos, this.type.optionalValueFor()));
        }

        public void onPoiUpdated(ServerWorld world, BlockPos pos) {
            this.sendToTrackingPlayers(world, new ChunkPos(pos), (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(pos, this.type.optionalValueFor(world.getPointOfInterestStorage().getDebugData(pos))));
        }
    }

    static class UpdateQuerier<T> {
        private final DebugTrackable.DebugDataSupplier<T> dataSupplier;
        @Nullable
        T lastData;

        UpdateQuerier(DebugTrackable.DebugDataSupplier<T> dataSupplier) {
            this.dataSupplier = dataSupplier;
        }

        @Nullable
        public DebugSubscriptionType.OptionalValue<T> queryUpdate(DebugSubscriptionType<T> type) {
            T object = this.dataSupplier.get();
            if (!Objects.equals(object, this.lastData)) {
                this.lastData = object;
                return type.optionalValueFor(object);
            }
            return null;
        }
    }

    public static class UpdateTrackedSubscription<T>
    extends TrackedSubscription<T> {
        private final Map<ChunkPos, UpdateQuerier<T>> trackedChunks = new HashMap<ChunkPos, UpdateQuerier<T>>();
        private final Map<BlockPos, UpdateQuerier<T>> trackedBlockEntities = new HashMap<BlockPos, UpdateQuerier<T>>();
        private final Map<UUID, UpdateQuerier<T>> trackedEntities = new HashMap<UUID, UpdateQuerier<T>>();

        public UpdateTrackedSubscription(DebugSubscriptionType<T> arg) {
            super(arg);
        }

        @Override
        protected void clear() {
            this.trackedChunks.clear();
            this.trackedBlockEntities.clear();
            this.trackedEntities.clear();
        }

        @Override
        protected void sendUpdate(ServerWorld world) {
            DebugSubscriptionType.OptionalValue<T> lv;
            for (Map.Entry<ChunkPos, UpdateQuerier<T>> entry : this.trackedChunks.entrySet()) {
                lv = entry.getValue().queryUpdate(this.type);
                if (lv == null) continue;
                ChunkPos lv2 = entry.getKey();
                this.sendToTrackingPlayers(world, lv2, (Packet<ClientPlayPacketListener>)new ChunkValueDebugS2CPacket(lv2, lv));
            }
            for (Map.Entry<Object, UpdateQuerier<T>> entry : this.trackedBlockEntities.entrySet()) {
                lv = entry.getValue().queryUpdate(this.type);
                if (lv == null) continue;
                BlockPos lv3 = (BlockPos)entry.getKey();
                ChunkPos lv4 = new ChunkPos(lv3);
                this.sendToTrackingPlayers(world, lv4, (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(lv3, lv));
            }
            for (Map.Entry<Object, UpdateQuerier<T>> entry : this.trackedEntities.entrySet()) {
                lv = entry.getValue().queryUpdate(this.type);
                if (lv == null) continue;
                Entity lv5 = Objects.requireNonNull(world.getEntity((UUID)entry.getKey()));
                this.sendToTrackingPlayers(world, lv5, (Packet<ClientPlayPacketListener>)new EntityValueDebugS2CPacket(lv5.getId(), lv));
            }
        }

        public void trackChunk(ChunkPos chunkPos, DebugTrackable.DebugDataSupplier<T> dataSupplier) {
            this.trackedChunks.put(chunkPos, new UpdateQuerier<T>(dataSupplier));
        }

        public void trackBlockEntity(BlockPos chunkPos, DebugTrackable.DebugDataSupplier<T> dataSupplier) {
            this.trackedBlockEntities.put(chunkPos, new UpdateQuerier<T>(dataSupplier));
        }

        public void trackEntity(UUID uuid, DebugTrackable.DebugDataSupplier<T> dataSupplier) {
            this.trackedEntities.put(uuid, new UpdateQuerier<T>(dataSupplier));
        }

        public void untrackChunk(ChunkPos chunkPos) {
            this.trackedChunks.remove(chunkPos);
            this.trackedBlockEntities.keySet().removeIf(chunkPos::contains);
        }

        public void untrackBlockEntity(ServerWorld world, BlockPos pos) {
            UpdateQuerier<T> lv = this.trackedBlockEntities.remove(pos);
            if (lv != null) {
                ChunkPos lv2 = new ChunkPos(pos);
                this.sendToTrackingPlayers(world, lv2, (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(pos, this.type.optionalValueFor()));
            }
        }

        public void untrackEntity(Entity entity) {
            this.trackedEntities.remove(entity.getUuid());
        }

        @Override
        protected void sendInitial(ServerPlayerEntity player, ChunkPos chunkPos) {
            UpdateQuerier<T> lv = this.trackedChunks.get(chunkPos);
            if (lv != null && lv.lastData != null) {
                player.networkHandler.sendPacket(new ChunkValueDebugS2CPacket(chunkPos, this.type.optionalValueFor(lv.lastData)));
            }
            for (Map.Entry<BlockPos, UpdateQuerier<T>> entry : this.trackedBlockEntities.entrySet()) {
                BlockPos lv2;
                Object object = entry.getValue().lastData;
                if (object == null || !chunkPos.contains(lv2 = entry.getKey())) continue;
                player.networkHandler.sendPacket(new BlockValueDebugS2CPacket(lv2, this.type.optionalValueFor(object)));
            }
        }

        @Override
        protected void sendInitial(ServerPlayerEntity player, Entity entity) {
            UpdateQuerier<T> lv = this.trackedEntities.get(entity.getUuid());
            if (lv != null && lv.lastData != null) {
                player.networkHandler.sendPacket(new EntityValueDebugS2CPacket(entity.getId(), this.type.optionalValueFor(lv.lastData)));
            }
        }
    }
}

