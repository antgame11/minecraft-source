/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class SaveOffCommand {
    private static final SimpleCommandExceptionType ALREADY_OFF_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.save.alreadyOff"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("save-off").requires(CommandManager.requirePermissionLevel(4))).executes(context -> {
            ServerCommandSource lv = (ServerCommandSource)context.getSource();
            boolean bl = lv.getServer().setAutosave(false);
            if (!bl) {
                throw ALREADY_OFF_EXCEPTION.create();
            }
            lv.sendFeedback(() -> Text.translatable("commands.save.disabled"), true);
            return 1;
        }));
    }
}

