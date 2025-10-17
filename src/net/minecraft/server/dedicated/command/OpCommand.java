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

public class OpCommand {
    private static final SimpleCommandExceptionType ALREADY_OPPED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.op.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("op").requires(CommandManager.requirePermissionLevel(3))).then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
            PlayerManager lv = ((ServerCommandSource)context.getSource()).getServer().getPlayerManager();
            return CommandSource.suggestMatching(lv.getPlayerList().stream().filter(player -> !lv.isOperator(player.getPlayerConfigEntry())).map(player -> player.getGameProfile().name()), builder);
        }).executes(context -> OpCommand.op((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))));
    }

    private static int op(ServerCommandSource source, Collection<PlayerConfigEntry> targets) throws CommandSyntaxException {
        PlayerManager lv = source.getServer().getPlayerManager();
        int i = 0;
        for (PlayerConfigEntry lv2 : targets) {
            if (lv.isOperator(lv2)) continue;
            lv.addToOperators(lv2);
            ++i;
            source.sendFeedback(() -> Text.translatable("commands.op.success", lv2.name()), true);
        }
        if (i == 0) {
            throw ALREADY_OPPED_EXCEPTION.create();
        }
        return i;
    }
}

