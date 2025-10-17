/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.dispatch;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.dedicated.management.RpcKickReason;
import net.minecraft.server.dedicated.management.RpcPlayer;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class PlayersRpcDispatcher {
    private static final Text DEFAULT_KICK_REASON = Text.translatable("multiplayer.disconnect.kicked");

    public static List<RpcPlayer> get(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPlayerListHandler().getPlayerList().stream().map(RpcPlayer::of).toList();
    }

    public static List<RpcPlayer> kick(ManagementHandlerDispatcher dispatcher, List<RpcEntry> list, ManagementConnectionId remote) {
        ArrayList<RpcPlayer> list2 = new ArrayList<RpcPlayer>();
        for (RpcEntry lv : list) {
            ServerPlayerEntity lv2 = PlayersRpcDispatcher.getPlayer(dispatcher, lv.player());
            if (lv2 == null) continue;
            dispatcher.getPlayerListHandler().removePlayer(lv2, remote);
            lv2.networkHandler.disconnect(lv.message.flatMap(RpcKickReason::toText).orElse(DEFAULT_KICK_REASON));
            list2.add(lv.player());
        }
        return list2;
    }

    @Nullable
    private static ServerPlayerEntity getPlayer(ManagementHandlerDispatcher dispatcher, RpcPlayer player) {
        if (player.id().isPresent()) {
            return dispatcher.getPlayerListHandler().getPlayer(player.id().get());
        }
        if (player.name().isPresent()) {
            return dispatcher.getPlayerListHandler().getPlayer(player.name().get());
        }
        return null;
    }

    public record RpcEntry(RpcPlayer player, Optional<RpcKickReason> message) {
        public static final MapCodec<RpcEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RpcPlayer.CODEC.codec().fieldOf("player")).forGetter(RpcEntry::player), RpcKickReason.CODEC.optionalFieldOf("message").forGetter(RpcEntry::message)).apply((Applicative<RpcEntry, ?>)instance, RpcEntry::new));
    }
}

