/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;

public record RpcPlayer(Optional<UUID> id, Optional<String> name) {
    public static final MapCodec<RpcPlayer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Uuids.STRING_CODEC.optionalFieldOf("id").forGetter(RpcPlayer::id), Codec.STRING.optionalFieldOf("name").forGetter(RpcPlayer::name)).apply((Applicative<RpcPlayer, ?>)instance, RpcPlayer::new));

    public static RpcPlayer of(GameProfile profile) {
        return new RpcPlayer(Optional.of(profile.id()), Optional.of(profile.name()));
    }

    public static RpcPlayer of(PlayerConfigEntry player) {
        return new RpcPlayer(Optional.of(player.id()), Optional.of(player.name()));
    }

    public static RpcPlayer of(ServerPlayerEntity player) {
        GameProfile gameProfile = player.getGameProfile();
        return RpcPlayer.of(gameProfile);
    }
}

