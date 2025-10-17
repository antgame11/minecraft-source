/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management;

import com.mojang.serialization.Codec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.dedicated.management.OutgoingRpcMethod;
import net.minecraft.server.dedicated.management.RpcPlayer;
import net.minecraft.server.dedicated.management.RpcRequestParameter;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.IpBansRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.OperatorsRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.PlayerBansRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.ServerRpcDispatcher;
import net.minecraft.server.dedicated.management.schema.RpcSchema;

public class OutgoingRpcMethods {
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Simple> SERVER_STARTED = OutgoingRpcMethod.createSimpleBuilder().description("Server started").buildAndRegisterVanilla("server/started");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Simple> SERVER_STOPPING = OutgoingRpcMethod.createSimpleBuilder().description("Server shutting down").buildAndRegisterVanilla("server/stopping");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Simple> SERVER_SAVING = OutgoingRpcMethod.createSimpleBuilder().description("Server save started").buildAndRegisterVanilla("server/saving");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Simple> SERVER_SAVED = OutgoingRpcMethod.createSimpleBuilder().description("Server save completed").buildAndRegisterVanilla("server/saved");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<RpcPlayer>> PLAYER_JOINED = OutgoingRpcMethod.createNotificationBuilder(RpcPlayer.CODEC.codec()).requestParameter(new RpcRequestParameter("player", RpcSchema.PLAYER.ref())).description("Player joined").buildAndRegisterVanilla("players/joined");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<RpcPlayer>> PLAYER_LEFT = OutgoingRpcMethod.createNotificationBuilder(RpcPlayer.CODEC.codec()).requestParameter(new RpcRequestParameter("player", RpcSchema.PLAYER.ref())).description("Player left").buildAndRegisterVanilla("players/left");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<OperatorsRpcDispatcher.RpcEntry>> OPERATOR_ADDED = OutgoingRpcMethod.createNotificationBuilder(OperatorsRpcDispatcher.RpcEntry.CODEC.codec()).requestParameter(new RpcRequestParameter("player", RpcSchema.OPERATOR.ref())).description("Player was oped").buildAndRegisterVanilla("operators/added");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<OperatorsRpcDispatcher.RpcEntry>> OPERATOR_REMOVED = OutgoingRpcMethod.createNotificationBuilder(OperatorsRpcDispatcher.RpcEntry.CODEC.codec()).requestParameter(new RpcRequestParameter("player", RpcSchema.OPERATOR.ref())).description("Player was deoped").buildAndRegisterVanilla("operators/removed");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<RpcPlayer>> ALLOWLIST_ADDED = OutgoingRpcMethod.createNotificationBuilder(RpcPlayer.CODEC.codec()).requestParameter(new RpcRequestParameter("player", RpcSchema.PLAYER.ref())).description("Player was added to allowlist").buildAndRegisterVanilla("allowlist/added");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<RpcPlayer>> ALLOWLIST_REMOVED = OutgoingRpcMethod.createNotificationBuilder(RpcPlayer.CODEC.codec()).requestParameter(new RpcRequestParameter("player", RpcSchema.PLAYER.ref())).description("Player was removed from allowlist").buildAndRegisterVanilla("allowlist/removed");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<IpBansRpcDispatcher.IpBanData>> IP_BAN_ADDED = OutgoingRpcMethod.createNotificationBuilder(IpBansRpcDispatcher.IpBanData.CODEC.codec()).requestParameter(new RpcRequestParameter("player", RpcSchema.IP_BAN.ref())).description("Ip was added to ip ban list").buildAndRegisterVanilla("ip_bans/added");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<String>> IP_BAN_REMOVED = OutgoingRpcMethod.createNotificationBuilder(Codec.STRING).requestParameter(new RpcRequestParameter("player", RpcSchema.STRING)).description("Ip was removed from ip ban list").buildAndRegisterVanilla("ip_bans/removed");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<PlayerBansRpcDispatcher.RpcEntry>> BAN_ADDED = OutgoingRpcMethod.createNotificationBuilder(PlayerBansRpcDispatcher.RpcEntry.CODEC.codec()).requestParameter(new RpcRequestParameter("player", RpcSchema.USER_BAN.ref())).description("Player was added to ban list").buildAndRegisterVanilla("bans/added");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<RpcPlayer>> BAN_REMOVED = OutgoingRpcMethod.createNotificationBuilder(RpcPlayer.CODEC.codec()).requestParameter(new RpcRequestParameter("player", RpcSchema.PLAYER.ref())).description("Player was removed from ban list").buildAndRegisterVanilla("bans/removed");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<GameRuleRpcDispatcher.TypedRule>> GAMERULE_UPDATED = OutgoingRpcMethod.createNotificationBuilder(GameRuleRpcDispatcher.TypedRule.CODEC.codec()).requestParameter(new RpcRequestParameter("gamerule", RpcSchema.TYPED_GAME_RULE.ref())).description("Gamerule was changed").buildAndRegisterVanilla("gamerules/updated");
    public static final RegistryEntry.Reference<OutgoingRpcMethod.Notification<ServerRpcDispatcher.RpcStatus>> SERVER_STATUS_HEARTBEAT = OutgoingRpcMethod.createNotificationBuilder(ServerRpcDispatcher.RpcStatus.CODEC).requestParameter(new RpcRequestParameter("status", RpcSchema.SERVER_STATE.ref())).description("Server status heartbeat").buildAndRegisterVanilla("server/status");
}

