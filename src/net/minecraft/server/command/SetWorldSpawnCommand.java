/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.WorldProperties;

public class SetWorldSpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("setworldspawn").requires(CommandManager.requirePermissionLevel(2))).executes(context -> SetWorldSpawnCommand.execute((ServerCommandSource)context.getSource(), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition()), DefaultPosArgument.DEFAULT_ROTATION))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> SetWorldSpawnCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getValidBlockPos(context, "pos"), DefaultPosArgument.DEFAULT_ROTATION))).then(CommandManager.argument("rotation", RotationArgumentType.rotation()).executes(context -> SetWorldSpawnCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getValidBlockPos(context, "pos"), RotationArgumentType.getRotation(context, "rotation"))))));
    }

    private static int execute(ServerCommandSource source, BlockPos pos, PosArgument rotation) {
        ServerWorld lv = source.getWorld();
        Vec2f lv2 = rotation.getRotation(source);
        float f = lv2.y;
        float g = lv2.x;
        lv.setSpawnPoint(WorldProperties.SpawnPoint.create(lv.getRegistryKey(), pos, f, g));
        source.sendFeedback(() -> Text.translatable("commands.setworldspawn.success", pos.getX(), pos.getY(), pos.getZ(), Float.valueOf(f), Float.valueOf(g), lv.getRegistryKey().getValue().toString()), true);
        return 1;
    }
}

