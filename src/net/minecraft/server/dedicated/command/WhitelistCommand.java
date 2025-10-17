/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class WhitelistCommand {
    private static final SimpleCommandExceptionType ALREADY_ON_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.whitelist.alreadyOn"));
    private static final SimpleCommandExceptionType ALREADY_OFF_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.whitelist.alreadyOff"));
    private static final SimpleCommandExceptionType ADD_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.whitelist.add.failed"));
    private static final SimpleCommandExceptionType REMOVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.whitelist.remove.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("whitelist").requires(CommandManager.requirePermissionLevel(3))).then(CommandManager.literal("on").executes(context -> WhitelistCommand.executeOn((ServerCommandSource)context.getSource())))).then(CommandManager.literal("off").executes(context -> WhitelistCommand.executeOff((ServerCommandSource)context.getSource())))).then(CommandManager.literal("list").executes(context -> WhitelistCommand.executeList((ServerCommandSource)context.getSource())))).then(CommandManager.literal("add").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
            PlayerManager lv = ((ServerCommandSource)context.getSource()).getServer().getPlayerManager();
            return CommandSource.suggestMatching(lv.getPlayerList().stream().map(PlayerEntity::getPlayerConfigEntry).filter(arg2 -> !lv.getWhitelist().isAllowed((PlayerConfigEntry)arg2)).map(PlayerConfigEntry::name), builder);
        }).executes(context -> WhitelistCommand.executeAdd((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))))).then(CommandManager.literal("remove").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getWhitelistedNames(), builder)).executes(context -> WhitelistCommand.executeRemove((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))))).then(CommandManager.literal("reload").executes(context -> WhitelistCommand.executeReload((ServerCommandSource)context.getSource()))));
    }

    private static int executeReload(ServerCommandSource source) {
        source.getServer().getPlayerManager().reloadWhitelist();
        source.sendFeedback(() -> Text.translatable("commands.whitelist.reloaded"), true);
        source.getServer().kickNonWhitelistedPlayers();
        return 1;
    }

    private static int executeAdd(ServerCommandSource source, Collection<PlayerConfigEntry> targets) throws CommandSyntaxException {
        Whitelist lv = source.getServer().getPlayerManager().getWhitelist();
        int i = 0;
        for (PlayerConfigEntry lv2 : targets) {
            if (lv.isAllowed(lv2)) continue;
            WhitelistEntry lv3 = new WhitelistEntry(lv2);
            lv.add(lv3);
            source.sendFeedback(() -> Text.translatable("commands.whitelist.add.success", Text.literal(lv2.name())), true);
            ++i;
        }
        if (i == 0) {
            throw ADD_FAILED_EXCEPTION.create();
        }
        return i;
    }

    private static int executeRemove(ServerCommandSource source, Collection<PlayerConfigEntry> targets) throws CommandSyntaxException {
        Whitelist lv = source.getServer().getPlayerManager().getWhitelist();
        int i = 0;
        for (PlayerConfigEntry lv2 : targets) {
            if (!lv.isAllowed(lv2)) continue;
            WhitelistEntry lv3 = new WhitelistEntry(lv2);
            lv.remove(lv3);
            source.sendFeedback(() -> Text.translatable("commands.whitelist.remove.success", Text.literal(lv2.name())), true);
            ++i;
        }
        if (i == 0) {
            throw REMOVE_FAILED_EXCEPTION.create();
        }
        source.getServer().kickNonWhitelistedPlayers();
        return i;
    }

    private static int executeOn(ServerCommandSource source) throws CommandSyntaxException {
        if (source.getServer().getUseAllowlist()) {
            throw ALREADY_ON_EXCEPTION.create();
        }
        source.getServer().setUseAllowlist(true);
        source.sendFeedback(() -> Text.translatable("commands.whitelist.enabled"), true);
        source.getServer().kickNonWhitelistedPlayers();
        return 1;
    }

    private static int executeOff(ServerCommandSource source) throws CommandSyntaxException {
        if (!source.getServer().getUseAllowlist()) {
            throw ALREADY_OFF_EXCEPTION.create();
        }
        source.getServer().setUseAllowlist(false);
        source.sendFeedback(() -> Text.translatable("commands.whitelist.disabled"), true);
        return 1;
    }

    private static int executeList(ServerCommandSource source) {
        String[] strings = source.getServer().getPlayerManager().getWhitelistedNames();
        if (strings.length == 0) {
            source.sendFeedback(() -> Text.translatable("commands.whitelist.none"), false);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.whitelist.list", strings.length, String.join((CharSequence)", ", strings)), false);
        }
        return strings.length;
    }
}

