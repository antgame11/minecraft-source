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
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PardonCommand {
    private static final SimpleCommandExceptionType ALREADY_UNBANNED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.pardon.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("pardon").requires(CommandManager.requirePermissionLevel(3))).then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getUserBanList().getNames(), builder)).executes(context -> PardonCommand.pardon((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))));
    }

    private static int pardon(ServerCommandSource source, Collection<PlayerConfigEntry> targets) throws CommandSyntaxException {
        BannedPlayerList lv = source.getServer().getPlayerManager().getUserBanList();
        int i = 0;
        for (PlayerConfigEntry lv2 : targets) {
            if (!lv.contains(lv2)) continue;
            lv.remove(lv2);
            ++i;
            source.sendFeedback(() -> Text.translatable("commands.pardon.success", Text.literal(lv2.name())), true);
        }
        if (i == 0) {
            throw ALREADY_UNBANNED_EXCEPTION.create();
        }
        return i;
    }
}

