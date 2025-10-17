/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public class GameModeCommand {
    public static final int REQUIRED_PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("gamemode").requires(CommandManager.requirePermissionLevel(2))).then(((RequiredArgumentBuilder)CommandManager.argument("gamemode", GameModeArgumentType.gameMode()).executes(context -> GameModeCommand.execute(context, Collections.singleton(((ServerCommandSource)context.getSource()).getPlayerOrThrow()), GameModeArgumentType.getGameMode(context, "gamemode")))).then(CommandManager.argument("target", EntityArgumentType.players()).executes(context -> GameModeCommand.execute(context, EntityArgumentType.getPlayers(context, "target"), GameModeArgumentType.getGameMode(context, "gamemode"))))));
    }

    private static void sendFeedback(ServerCommandSource source, ServerPlayerEntity player, GameMode gameMode) {
        MutableText lv = Text.translatable("gameMode." + gameMode.getId());
        if (source.getEntity() == player) {
            source.sendFeedback(() -> Text.translatable("commands.gamemode.success.self", lv), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendMessage(Text.translatable("gameMode.changed", lv));
            }
            source.sendFeedback(() -> Text.translatable("commands.gamemode.success.other", player.getDisplayName(), lv), true);
        }
    }

    private static int execute(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets, GameMode gameMode) {
        int i = 0;
        for (ServerPlayerEntity lv : targets) {
            if (!GameModeCommand.execute(context.getSource(), lv, gameMode)) continue;
            ++i;
        }
        return i;
    }

    public static void execute(ServerPlayerEntity target, GameMode gameMode) {
        GameModeCommand.execute(target.getCommandSource(), target, gameMode);
    }

    private static boolean execute(ServerCommandSource source, ServerPlayerEntity target, GameMode gameMode) {
        if (target.changeGameMode(gameMode)) {
            GameModeCommand.sendFeedback(source, target, gameMode);
            return true;
        }
        return false;
    }
}

