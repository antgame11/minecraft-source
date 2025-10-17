/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;

public class SpawnPointCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spawnpoint").requires(CommandManager.requirePermissionLevel(2))).executes(context -> SpawnPointCommand.execute((ServerCommandSource)context.getSource(), Collections.singleton(((ServerCommandSource)context.getSource()).getPlayerOrThrow()), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition()), DefaultPosArgument.DEFAULT_ROTATION))).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes(context -> SpawnPointCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition()), DefaultPosArgument.DEFAULT_ROTATION))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> SpawnPointCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), BlockPosArgumentType.getValidBlockPos(context, "pos"), DefaultPosArgument.DEFAULT_ROTATION))).then(CommandManager.argument("rotation", RotationArgumentType.rotation()).executes(context -> SpawnPointCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), BlockPosArgumentType.getValidBlockPos(context, "pos"), RotationArgumentType.getRotation(context, "rotation")))))));
    }

    private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, BlockPos pos, PosArgument rotation) {
        RegistryKey<World> lv = source.getWorld().getRegistryKey();
        Vec2f lv2 = rotation.getRotation(source);
        float f = lv2.y;
        float g = lv2.x;
        for (ServerPlayerEntity lv3 : targets) {
            lv3.setSpawnPoint(new ServerPlayerEntity.Respawn(WorldProperties.SpawnPoint.create(lv, pos, f, g), true), false);
        }
        String string = lv.getValue().toString();
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.spawnpoint.success.single", pos.getX(), pos.getY(), pos.getZ(), Float.valueOf(f), Float.valueOf(g), string, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.spawnpoint.success.multiple", pos.getX(), pos.getY(), pos.getZ(), Float.valueOf(f), Float.valueOf(g), string, targets.size()), true);
        }
        return targets.size();
    }
}

