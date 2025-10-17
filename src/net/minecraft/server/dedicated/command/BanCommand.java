/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class BanCommand {
    private static final SimpleCommandExceptionType ALREADY_BANNED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.ban.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("ban").requires(CommandManager.requirePermissionLevel(3))).then(((RequiredArgumentBuilder)CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).executes(context -> BanCommand.ban((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets"), null))).then(CommandManager.argument("reason", MessageArgumentType.message()).executes(context -> BanCommand.ban((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets"), MessageArgumentType.getMessage(context, "reason"))))));
    }

    private static int ban(ServerCommandSource source, Collection<PlayerConfigEntry> targets, @Nullable Text reason) throws CommandSyntaxException {
        BannedPlayerList lv = source.getServer().getPlayerManager().getUserBanList();
        int i = 0;
        for (PlayerConfigEntry lv2 : targets) {
            if (lv.contains(lv2)) continue;
            BannedPlayerEntry lv3 = new BannedPlayerEntry(lv2, null, source.getName(), null, reason == null ? null : reason.getString());
            lv.add(lv3);
            ++i;
            source.sendFeedback(() -> Text.translatable("commands.ban.success", Text.literal(lv2.name()), lv3.getReasonText()), true);
            ServerPlayerEntity lv4 = source.getServer().getPlayerManager().getPlayer(lv2.id());
            if (lv4 == null) continue;
            lv4.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.banned"));
        }
        if (i == 0) {
            throw ALREADY_BANNED_EXCEPTION.create();
        }
        return i;
    }
}

