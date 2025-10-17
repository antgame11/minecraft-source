/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class DefaultGameModeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("defaultgamemode").requires(CommandManager.requirePermissionLevel(2))).then(CommandManager.argument("gamemode", GameModeArgumentType.gameMode()).executes(commandContext -> DefaultGameModeCommand.execute((ServerCommandSource)commandContext.getSource(), GameModeArgumentType.getGameMode(commandContext, "gamemode")))));
    }

    private static int execute(ServerCommandSource source, GameMode defaultGameMode) {
        MinecraftServer minecraftServer = source.getServer();
        minecraftServer.setDefaultGameMode(defaultGameMode);
        int i = minecraftServer.changeGameModeGlobally(minecraftServer.getForcedGameMode());
        source.sendFeedback(() -> Text.translatable("commands.defaultgamemode.success", defaultGameMode.getTranslatableName()), true);
        return i;
    }
}

