/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

public class GameRuleCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        final LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)CommandManager.literal("gamerule").requires(CommandManager.requirePermissionLevel(2));
        new GameRules(commandRegistryAccess.getEnabledFeatures()).accept(new GameRules.Visitor(){

            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder2 = CommandManager.literal(key.getName());
                literalArgumentBuilder.then(((LiteralArgumentBuilder)literalArgumentBuilder2.executes(context -> GameRuleCommand.executeQuery((ServerCommandSource)context.getSource(), key))).then(type.argument("value").executes(context -> GameRuleCommand.executeSet(context, key))));
            }
        });
        dispatcher.register(literalArgumentBuilder);
    }

    static <T extends GameRules.Rule<T>> int executeSet(CommandContext<ServerCommandSource> context, GameRules.Key<T> key) {
        ServerCommandSource lv = context.getSource();
        Object lv2 = lv.getServer().getGameRules().get(key);
        ((GameRules.Rule)lv2).set(context, "value");
        lv.getServer().onGameRuleUpdated(key.getName(), (GameRules.Rule<?>)lv2);
        lv.sendFeedback(() -> Text.translatable("commands.gamerule.set", key.getName(), lv2.toString()), true);
        return ((GameRules.Rule)lv2).getCommandResult();
    }

    static <T extends GameRules.Rule<T>> int executeQuery(ServerCommandSource source, GameRules.Key<T> key) {
        Object lv = source.getServer().getGameRules().get(key);
        source.sendFeedback(() -> Text.translatable("commands.gamerule.query", key.getName(), lv.toString()), false);
        return ((GameRules.Rule)lv).getCommandResult();
    }
}

