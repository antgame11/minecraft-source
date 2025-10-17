/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.dispatch;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.dedicated.management.RpcKickReason;
import net.minecraft.server.dedicated.management.RpcPlayer;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.dispatch.PlayersRpcDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ServerRpcDispatcher {
    public static RpcStatus status(ManagementHandlerDispatcher dispatcher) {
        if (!dispatcher.getServerHandler().isLoading()) {
            return RpcStatus.EMPTY;
        }
        return new RpcStatus(true, PlayersRpcDispatcher.get(dispatcher), ServerMetadata.Version.create());
    }

    public static boolean save(ManagementHandlerDispatcher dispatcher, boolean flush, ManagementConnectionId remote) {
        return dispatcher.getServerHandler().save(true, flush, true, remote);
    }

    public static boolean stop(ManagementHandlerDispatcher dispatcher, ManagementConnectionId remote) {
        dispatcher.submit(() -> dispatcher.getServerHandler().stop(false, remote));
        return true;
    }

    public static boolean systemMessage(ManagementHandlerDispatcher dispatcher, RpcSystemMessage message, ManagementConnectionId remote) {
        Text lv = message.message().toText().orElse(null);
        if (lv == null) {
            return false;
        }
        if (message.receivingPlayers().isPresent()) {
            if (message.receivingPlayers().get().isEmpty()) {
                return false;
            }
            for (RpcPlayer lv2 : message.receivingPlayers().get()) {
                ServerPlayerEntity lv3;
                if (lv2.id().isPresent()) {
                    lv3 = dispatcher.getPlayerListHandler().getPlayer(lv2.id().get());
                } else {
                    if (!lv2.name().isPresent()) continue;
                    lv3 = dispatcher.getPlayerListHandler().getPlayer(lv2.name().get());
                }
                if (lv3 == null) continue;
                lv3.sendMessageToClient(lv, message.overlay());
            }
        } else {
            dispatcher.getServerHandler().broadcastMessage(lv, message.overlay(), remote);
        }
        return true;
    }

    public record RpcStatus(boolean started, List<RpcPlayer> players, ServerMetadata.Version version) {
        public static final Codec<RpcStatus> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("started")).forGetter(RpcStatus::started), RpcPlayer.CODEC.codec().listOf().lenientOptionalFieldOf("players", List.of()).forGetter(RpcStatus::players), ((MapCodec)ServerMetadata.Version.CODEC.fieldOf("version")).forGetter(RpcStatus::version)).apply((Applicative<RpcStatus, ?>)instance, RpcStatus::new));
        public static final RpcStatus EMPTY = new RpcStatus(false, List.of(), ServerMetadata.Version.create());
    }

    public record RpcSystemMessage(RpcKickReason message, boolean overlay, Optional<List<RpcPlayer>> receivingPlayers) {
        public static final Codec<RpcSystemMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)RpcKickReason.CODEC.fieldOf("message")).forGetter(RpcSystemMessage::message), ((MapCodec)Codec.BOOL.fieldOf("overlay")).forGetter(RpcSystemMessage::overlay), RpcPlayer.CODEC.codec().listOf().lenientOptionalFieldOf("receivingPlayers").forGetter(RpcSystemMessage::receivingPlayers)).apply((Applicative<RpcSystemMessage, ?>)instance, RpcSystemMessage::new));
    }
}

