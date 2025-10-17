/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityQueriable;
import net.minecraft.world.entity.UniquelyIdentifiable;
import org.jetbrains.annotations.Nullable;

public final class LazyEntityReference<StoredEntityType extends UniquelyIdentifiable> {
    private static final Codec<? extends LazyEntityReference<?>> CODEC = Uuids.INT_STREAM_CODEC.xmap(LazyEntityReference::new, LazyEntityReference::getUuid);
    private static final PacketCodec<ByteBuf, ? extends LazyEntityReference<?>> PACKET_CODEC = Uuids.PACKET_CODEC.xmap(LazyEntityReference::new, LazyEntityReference::getUuid);
    private Either<UUID, StoredEntityType> value;

    public static <Type extends UniquelyIdentifiable> Codec<LazyEntityReference<Type>> createCodec() {
        return CODEC;
    }

    public static <Type extends UniquelyIdentifiable> PacketCodec<ByteBuf, LazyEntityReference<Type>> createPacketCodec() {
        return PACKET_CODEC;
    }

    private LazyEntityReference(StoredEntityType value) {
        this.value = Either.right(value);
    }

    private LazyEntityReference(UUID value) {
        this.value = Either.left(value);
    }

    @Nullable
    public static <T extends UniquelyIdentifiable> LazyEntityReference<T> of(@Nullable T object) {
        return object != null ? new LazyEntityReference<T>(object) : null;
    }

    public static <T extends UniquelyIdentifiable> LazyEntityReference<T> ofUUID(UUID uuid) {
        return new LazyEntityReference(uuid);
    }

    public UUID getUuid() {
        return this.value.map(uuid -> uuid, UniquelyIdentifiable::getUuid);
    }

    @Nullable
    public StoredEntityType resolve(EntityQueriable<? extends UniquelyIdentifiable> world, Class<StoredEntityType> type) {
        StoredEntityType lv2;
        Optional<UUID> optional2;
        Optional<StoredEntityType> optional = this.value.right();
        if (optional.isPresent()) {
            UniquelyIdentifiable lv = (UniquelyIdentifiable)optional.get();
            if (lv.isRemoved()) {
                this.value = Either.left(lv.getUuid());
            } else {
                return (StoredEntityType)lv;
            }
        }
        if ((optional2 = this.value.left()).isPresent() && (lv2 = this.cast(world.lookup(optional2.get()), type)) != null && !lv2.isRemoved()) {
            this.value = Either.right(lv2);
            return lv2;
        }
        return null;
    }

    @Nullable
    public StoredEntityType getEntityByClass(World world, Class<StoredEntityType> clazz) {
        if (PlayerEntity.class.isAssignableFrom(clazz)) {
            return this.resolve(world::getPlayerAnyDimension, clazz);
        }
        return this.resolve(world::getEntityAnyDimension, clazz);
    }

    @Nullable
    private StoredEntityType cast(@Nullable UniquelyIdentifiable entity, Class<StoredEntityType> clazz) {
        if (entity != null && clazz.isAssignableFrom(entity.getClass())) {
            return (StoredEntityType)((UniquelyIdentifiable)clazz.cast(entity));
        }
        return null;
    }

    public boolean uuidEquals(StoredEntityType o) {
        return this.getUuid().equals(o.getUuid());
    }

    public void writeData(WriteView view, String key) {
        view.put(key, Uuids.INT_STREAM_CODEC, this.getUuid());
    }

    public static void writeData(@Nullable LazyEntityReference<?> entityRef, WriteView view, String key) {
        if (entityRef != null) {
            entityRef.writeData(view, key);
        }
    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifiable> StoredEntityType resolve(@Nullable LazyEntityReference<StoredEntityType> entity, World world, Class<StoredEntityType> type) {
        return entity != null ? (StoredEntityType)entity.getEntityByClass(world, type) : null;
    }

    @Nullable
    public static Entity getEntity(@Nullable LazyEntityReference<Entity> entityReference, World world) {
        return LazyEntityReference.resolve(entityReference, world, Entity.class);
    }

    @Nullable
    public static LivingEntity getLivingEntity(@Nullable LazyEntityReference<LivingEntity> livingReference, World world) {
        return LazyEntityReference.resolve(livingReference, world, LivingEntity.class);
    }

    @Nullable
    public static PlayerEntity getPlayerEntity(@Nullable LazyEntityReference<PlayerEntity> playerReference, World world) {
        return LazyEntityReference.resolve(playerReference, world, PlayerEntity.class);
    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifiable> LazyEntityReference<StoredEntityType> fromData(ReadView view, String key) {
        return view.read(key, LazyEntityReference.createCodec()).orElse(null);
    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifiable> LazyEntityReference<StoredEntityType> fromDataOrPlayerName(ReadView view, String key, World world) {
        Optional<UUID> optional = view.read(key, Uuids.INT_STREAM_CODEC);
        if (optional.isPresent()) {
            return LazyEntityReference.ofUUID(optional.get());
        }
        return view.getOptionalString(key).map(name -> ServerConfigHandler.getPlayerUuidByName(world.getServer(), name)).map(LazyEntityReference::new).orElse(null);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof LazyEntityReference)) return false;
        LazyEntityReference lv = (LazyEntityReference)object;
        if (!this.getUuid().equals(lv.getUuid())) return false;
        return true;
    }

    public int hashCode() {
        return this.getUuid().hashCode();
    }
}

