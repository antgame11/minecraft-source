/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.GameProfileResolver;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;

public abstract sealed class ProfileComponent
implements TooltipAppender {
    private static final Codec<ProfileComponent> COMPONENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.mapEither(Codecs.INT_STREAM_UUID_GAME_PROFILE_CODEC, Data.CODEC).forGetter(ProfileComponent::get), SkinTextures.SkinOverride.CODEC.forGetter(ProfileComponent::getOverride)).apply((Applicative<ProfileComponent, ?>)instance, ProfileComponent::ofDispatched));
    public static final Codec<ProfileComponent> CODEC = Codec.withAlternative(COMPONENT_CODEC, Codecs.PLAYER_NAME, ProfileComponent::ofDynamic);
    public static final PacketCodec<ByteBuf, ProfileComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.either(PacketCodecs.GAME_PROFILE, Data.PACKET_CODEC), ProfileComponent::get, SkinTextures.SkinOverride.PACKET_CODEC, ProfileComponent::getOverride, ProfileComponent::ofDispatched);
    protected final GameProfile profile;
    protected final SkinTextures.SkinOverride override;

    private static ProfileComponent ofDispatched(Either<GameProfile, Data> profileOrData, SkinTextures.SkinOverride override) {
        return profileOrData.map(profile -> new Static(Either.left(profile), override), data -> {
            if (!data.properties.isEmpty() || data.id.isPresent() == data.name.isPresent()) {
                return new Static(Either.right(data), override);
            }
            return data.name.map(name -> new Dynamic(Either.left(name), override)).orElseGet(() -> new Dynamic(Either.right(arg.id.get()), override));
        });
    }

    public static ProfileComponent ofStatic(GameProfile profile) {
        return new Static(Either.left(profile), SkinTextures.SkinOverride.EMPTY);
    }

    public static ProfileComponent ofDynamic(String name) {
        return new Dynamic(Either.left(name), SkinTextures.SkinOverride.EMPTY);
    }

    public static ProfileComponent ofDynamic(UUID id) {
        return new Dynamic(Either.right(id), SkinTextures.SkinOverride.EMPTY);
    }

    protected abstract Either<GameProfile, Data> get();

    protected ProfileComponent(GameProfile profile, SkinTextures.SkinOverride override) {
        this.profile = profile;
        this.override = override;
    }

    public abstract CompletableFuture<GameProfile> resolve(GameProfileResolver var1);

    public GameProfile getGameProfile() {
        return this.profile;
    }

    public SkinTextures.SkinOverride getOverride() {
        return this.override;
    }

    static GameProfile createGameProfile(Optional<String> name, Optional<UUID> id, PropertyMap properties) {
        String string = name.orElse("");
        UUID uUID = id.orElseGet(() -> name.map(Uuids::getOfflinePlayerUuid).orElse(Util.NIL_UUID));
        return new GameProfile(uUID, string, properties);
    }

    public abstract Optional<String> getName();

    public static final class Static
    extends ProfileComponent {
        public static final Static EMPTY = new Static(Either.right(Data.EMPTY), SkinTextures.SkinOverride.EMPTY);
        private final Either<GameProfile, Data> profileOrData;

        Static(Either<GameProfile, Data> profileOrData, SkinTextures.SkinOverride override) {
            super(profileOrData.map(profile -> profile, Data::createGameProfile), override);
            this.profileOrData = profileOrData;
        }

        @Override
        public CompletableFuture<GameProfile> resolve(GameProfileResolver resolver) {
            return CompletableFuture.completedFuture(this.profile);
        }

        @Override
        protected Either<GameProfile, Data> get() {
            return this.profileOrData;
        }

        @Override
        public Optional<String> getName() {
            return this.profileOrData.map(profile -> Optional.of(profile.name()), data -> data.name);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Static)) return false;
            Static lv = (Static)o;
            if (!this.profileOrData.equals(lv.profileOrData)) return false;
            if (!this.override.equals(lv.override)) return false;
            return true;
        }

        public int hashCode() {
            int i = 31 + this.profileOrData.hashCode();
            i = 31 * i + this.override.hashCode();
            return i;
        }

        @Override
        public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        }
    }

    public static final class Dynamic
    extends ProfileComponent {
        private static final Text TEXT = Text.translatable("component.profile.dynamic").formatted(Formatting.GRAY);
        private final Either<String, UUID> nameOrId;

        Dynamic(Either<String, UUID> nameOrId, SkinTextures.SkinOverride override) {
            super(ProfileComponent.createGameProfile(nameOrId.left(), nameOrId.right(), PropertyMap.EMPTY), override);
            this.nameOrId = nameOrId;
        }

        @Override
        public Optional<String> getName() {
            return this.nameOrId.left();
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Dynamic)) return false;
            Dynamic lv = (Dynamic)o;
            if (!this.nameOrId.equals(lv.nameOrId)) return false;
            if (!this.override.equals(lv.override)) return false;
            return true;
        }

        public int hashCode() {
            int i = 31 + this.nameOrId.hashCode();
            i = 31 * i + this.override.hashCode();
            return i;
        }

        @Override
        protected Either<GameProfile, Data> get() {
            return Either.right(new Data(this.nameOrId.left(), this.nameOrId.right(), PropertyMap.EMPTY));
        }

        @Override
        public CompletableFuture<GameProfile> resolve(GameProfileResolver resolver) {
            return CompletableFuture.supplyAsync(() -> resolver.getProfile(this.nameOrId).orElse(this.profile), Util.getDownloadWorkerExecutor());
        }

        @Override
        public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
            textConsumer.accept(TEXT);
        }
    }

    protected record Data(Optional<String> name, Optional<UUID> id, PropertyMap properties) {
        public static final Data EMPTY = new Data(Optional.empty(), Optional.empty(), PropertyMap.EMPTY);
        static final MapCodec<Data> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codecs.PLAYER_NAME.optionalFieldOf("name").forGetter(Data::name), Uuids.INT_STREAM_CODEC.optionalFieldOf("id").forGetter(Data::id), Codecs.GAME_PROFILE_PROPERTY_MAP.optionalFieldOf("properties", PropertyMap.EMPTY).forGetter(Data::properties)).apply((Applicative<Data, ?>)instance, Data::new));
        public static final PacketCodec<ByteBuf, Data> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.PLAYER_NAME.collect(PacketCodecs::optional), Data::name, Uuids.PACKET_CODEC.collect(PacketCodecs::optional), Data::id, PacketCodecs.PROPERTY_MAP, Data::properties, Data::new);

        private GameProfile createGameProfile() {
            return ProfileComponent.createGameProfile(this.name, this.id, this.properties);
        }
    }
}

