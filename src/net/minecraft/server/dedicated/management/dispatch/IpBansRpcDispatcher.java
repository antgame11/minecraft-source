/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.dispatch;

import com.google.common.net.InetAddresses;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.dedicated.management.RpcPlayer;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

public class IpBansRpcDispatcher {
    private static final String DEFAULT_SOURCE = "Management server";

    public static List<IpBanData> get(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getBanHandler().getIpBanList().stream().map(IpBanInfo::fromBannedIpEntry).map(IpBanData::fromIpBanInfo).toList();
    }

    public static List<IpBanData> add(ManagementHandlerDispatcher dispatcher, List<IncomingRpcIpBanData> entries, ManagementConnectionId remote) {
        entries.stream().map(ipAddress -> IpBansRpcDispatcher.banIpFromRpcEntry(dispatcher, ipAddress, remote)).flatMap(Collection::stream).forEach(ipBannedPlayer -> ipBannedPlayer.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.ip_banned")));
        return IpBansRpcDispatcher.get(dispatcher);
    }

    private static List<ServerPlayerEntity> banIpFromRpcEntry(ManagementHandlerDispatcher dispatcher, IncomingRpcIpBanData entry, ManagementConnectionId remote) {
        Optional<ServerPlayerEntity> optional;
        IpBanInfo lv = entry.toIpBanInfoOrNull();
        if (lv != null) {
            return IpBansRpcDispatcher.banIp(dispatcher, lv, remote);
        }
        if (entry.player().isPresent() && (optional = dispatcher.getPlayerListHandler().getPlayer(entry.player().get().id(), entry.player().get().name())).isPresent()) {
            return IpBansRpcDispatcher.banIp(dispatcher, entry.toIpBanInfoFromPlayer(optional.get()), remote);
        }
        return List.of();
    }

    private static List<ServerPlayerEntity> banIp(ManagementHandlerDispatcher dispatcher, IpBanInfo ipBanInfo, ManagementConnectionId remote) {
        dispatcher.getBanHandler().addIpAddress(ipBanInfo.toBannedIpEntry(), remote);
        return dispatcher.getPlayerListHandler().getPlayersByIpAddress(ipBanInfo.ipAddress());
    }

    public static List<IpBanData> clearIpBans(ManagementHandlerDispatcher dispatcher, ManagementConnectionId remote) {
        dispatcher.getBanHandler().clearIpBanList(remote);
        return IpBansRpcDispatcher.get(dispatcher);
    }

    public static List<IpBanData> remove(ManagementHandlerDispatcher dispatcher, List<String> ipAddresses, ManagementConnectionId remote) {
        ipAddresses.forEach(address -> dispatcher.getBanHandler().removeIpAddress((String)address, remote));
        return IpBansRpcDispatcher.get(dispatcher);
    }

    public static List<IpBanData> set(ManagementHandlerDispatcher dispatcher, List<IpBanData> entries, ManagementConnectionId remote) {
        Set set = entries.stream().filter(ipBanInfo -> InetAddresses.isInetAddress(ipBanInfo.ipAddress())).map(IpBanData::toIpBanInfo).collect(Collectors.toSet());
        Set set2 = dispatcher.getBanHandler().getIpBanList().stream().map(IpBanInfo::fromBannedIpEntry).collect(Collectors.toSet());
        set2.stream().filter(ipBanInfo -> !set.contains(ipBanInfo)).forEach(ipBanInfoToRemove -> dispatcher.getBanHandler().removeIpAddress(ipBanInfoToRemove.ipAddress(), remote));
        set.stream().filter(ipBanInfo -> !set2.contains(ipBanInfo)).forEach(ipBanInfoToAdd -> dispatcher.getBanHandler().addIpAddress(ipBanInfoToAdd.toBannedIpEntry(), remote));
        set.stream().filter(ipBanInfo -> !set2.contains(ipBanInfo)).flatMap(newAddedIpBanInfo -> dispatcher.getPlayerListHandler().getPlayersByIpAddress(newAddedIpBanInfo.ipAddress()).stream()).forEach(ipBanInfo -> ipBanInfo.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.ip_banned")));
        return IpBansRpcDispatcher.get(dispatcher);
    }

    public record IncomingRpcIpBanData(Optional<RpcPlayer> player, Optional<String> ipAddress, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
        public static final MapCodec<IncomingRpcIpBanData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(RpcPlayer.CODEC.codec().optionalFieldOf("player").forGetter(IncomingRpcIpBanData::player), Codec.STRING.optionalFieldOf("ip").forGetter(IncomingRpcIpBanData::ipAddress), Codec.STRING.optionalFieldOf("reason").forGetter(IncomingRpcIpBanData::reason), Codec.STRING.optionalFieldOf("source").forGetter(IncomingRpcIpBanData::source), Codecs.INSTANT.optionalFieldOf("expires").forGetter(IncomingRpcIpBanData::expires)).apply((Applicative<IncomingRpcIpBanData, ?>)instance, IncomingRpcIpBanData::new));

        IpBanInfo toIpBanInfoFromPlayer(ServerPlayerEntity player) {
            return new IpBanInfo(player.getIp(), this.reason().orElse(null), this.source().orElse(IpBansRpcDispatcher.DEFAULT_SOURCE), this.expires());
        }

        @Nullable
        IpBanInfo toIpBanInfoOrNull() {
            if (this.ipAddress().isEmpty() || !InetAddresses.isInetAddress(this.ipAddress().get())) {
                return null;
            }
            return new IpBanInfo(this.ipAddress().get(), this.reason().orElse(null), this.source().orElse(IpBansRpcDispatcher.DEFAULT_SOURCE), this.expires());
        }
    }

    record IpBanInfo(String ipAddress, @Nullable String reason, String source, Optional<Instant> expires) {
        static IpBanInfo fromBannedIpEntry(BannedIpEntry bannedIpEntry) {
            return new IpBanInfo(Objects.requireNonNull((String)bannedIpEntry.getKey()), bannedIpEntry.getReason(), bannedIpEntry.getSource(), Optional.ofNullable(bannedIpEntry.getExpiryDate()).map(Date::toInstant));
        }

        BannedIpEntry toBannedIpEntry() {
            return new BannedIpEntry(this.ipAddress(), null, this.source(), (Date)this.expires().map(Date::from).orElse(null), this.reason());
        }

        @Nullable
        public String reason() {
            return this.reason;
        }
    }

    public record IpBanData(String ipAddress, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
        public static final MapCodec<IpBanData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("ip")).forGetter(IpBanData::ipAddress), Codec.STRING.optionalFieldOf("reason").forGetter(IpBanData::reason), Codec.STRING.optionalFieldOf("source").forGetter(IpBanData::source), Codecs.INSTANT.optionalFieldOf("expires").forGetter(IpBanData::expires)).apply((Applicative<IpBanData, ?>)instance, IpBanData::new));

        private static IpBanData fromIpBanInfo(IpBanInfo ipBanInfo) {
            return new IpBanData(ipBanInfo.ipAddress(), Optional.ofNullable(ipBanInfo.reason()), Optional.of(ipBanInfo.source()), ipBanInfo.expires());
        }

        public static IpBanData fromBannedIpEntry(BannedIpEntry bannedIpEntry) {
            return IpBanData.fromIpBanInfo(IpBanInfo.fromBannedIpEntry(bannedIpEntry));
        }

        private IpBanInfo toIpBanInfo() {
            return new IpBanInfo(this.ipAddress(), this.reason().orElse(null), this.source().orElse(IpBansRpcDispatcher.DEFAULT_SOURCE), this.expires());
        }
    }
}

