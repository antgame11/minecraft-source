/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class DeOpCommand {
    private static final SimpleCommandExceptionType ALREADY_DEOPPED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.deop.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("deop").requires(CommandManager.requirePermissionLevel(3))).then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getOpNames(), builder)).executes(context -> DeOpCommand.deop((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))));
    }

    private static int deop(ServerCommandSource source, Collection<PlayerConfigEntry> targets) throws CommandSyntaxException {
        PlayerManager lv = source.getServer().getPlayerManager();
        int i = 0;
        for (PlayerConfigEntry lv2 : targets) {
            if (!lv.isOperator(lv2)) continue;
            lv.removeFromOperators(lv2);
            ++i;
            source.sendFeedback(() -> Text.translatable("commands.deop.success", ((PlayerConfigEntry)targets.iterator().next()).name()), true);
        }
        if (i == 0) {
            throw ALREADY_DEOPPED_EXCEPTION.create();
        }
        source.getServer().kickNonWhitelistedPlayers();
        return i;
    }
}

