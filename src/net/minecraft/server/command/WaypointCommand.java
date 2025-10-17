/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.HexColorArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.WaypointArgument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.waypoint.ServerWaypoint;
import net.minecraft.world.waypoint.Waypoint;
import net.minecraft.world.waypoint.WaypointStyle;
import net.minecraft.world.waypoint.WaypointStyles;

public class WaypointCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("waypoint").requires(CommandManager.requirePermissionLevel(2))).then(CommandManager.literal("list").executes(context -> WaypointCommand.executeList((ServerCommandSource)context.getSource())))).then(CommandManager.literal("modify").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("waypoint", EntityArgumentType.entity()).then((ArgumentBuilder<ServerCommandSource, ?>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("color").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("color", ColorArgumentType.color()).executes(context -> WaypointCommand.executeColor((ServerCommandSource)context.getSource(), WaypointArgument.getWaypoint(context, "waypoint"), ColorArgumentType.getColor(context, "color"))))).then(CommandManager.literal("hex").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("color", HexColorArgumentType.hexColor()).executes(context -> WaypointCommand.executeColor((ServerCommandSource)context.getSource(), WaypointArgument.getWaypoint(context, "waypoint"), HexColorArgumentType.getArgbColor(context, "color")))))).then(CommandManager.literal("reset").executes(context -> WaypointCommand.executeReset((ServerCommandSource)context.getSource(), WaypointArgument.getWaypoint(context, "waypoint")))))).then(((LiteralArgumentBuilder)CommandManager.literal("style").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("reset").executes(context -> WaypointCommand.executeStyle((ServerCommandSource)context.getSource(), WaypointArgument.getWaypoint(context, "waypoint"), WaypointStyles.DEFAULT)))).then(CommandManager.literal("set").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("style", IdentifierArgumentType.identifier()).executes(context -> WaypointCommand.executeStyle((ServerCommandSource)context.getSource(), WaypointArgument.getWaypoint(context, "waypoint"), RegistryKey.of(WaypointStyles.REGISTRY, IdentifierArgumentType.getIdentifier(context, "style"))))))))));
    }

    private static int executeStyle(ServerCommandSource source, ServerWaypoint waypoint, RegistryKey<WaypointStyle> style) {
        WaypointCommand.updateWaypointConfig(source, waypoint, config -> {
            config.style = style;
        });
        source.sendFeedback(() -> Text.translatable("commands.waypoint.modify.style"), false);
        return 0;
    }

    private static int executeColor(ServerCommandSource source, ServerWaypoint waypoint, Formatting color) {
        WaypointCommand.updateWaypointConfig(source, waypoint, config -> {
            config.color = Optional.of(color.getColorValue());
        });
        source.sendFeedback(() -> Text.translatable("commands.waypoint.modify.color", Text.literal(color.getName()).formatted(color)), false);
        return 0;
    }

    private static int executeColor(ServerCommandSource source, ServerWaypoint waypoint, Integer color) {
        WaypointCommand.updateWaypointConfig(source, waypoint, config -> {
            config.color = Optional.of(color);
        });
        source.sendFeedback(() -> Text.translatable("commands.waypoint.modify.color", Text.literal(String.format("%06X", ColorHelper.withAlpha(0, (int)color))).withColor(color)), false);
        return 0;
    }

    private static int executeReset(ServerCommandSource source, ServerWaypoint waypoint) {
        WaypointCommand.updateWaypointConfig(source, waypoint, config -> {
            config.color = Optional.empty();
        });
        source.sendFeedback(() -> Text.translatable("commands.waypoint.modify.color.reset"), false);
        return 0;
    }

    private static int executeList(ServerCommandSource source) {
        ServerWorld lv = source.getWorld();
        Set<ServerWaypoint> set = lv.getWaypointHandler().getWaypoints();
        String string = lv.getRegistryKey().getValue().toString();
        if (set.isEmpty()) {
            source.sendFeedback(() -> Text.translatable("commands.waypoint.list.empty", string), false);
            return 0;
        }
        Text lv2 = Texts.join(set.stream().map(waypoint -> {
            if (waypoint instanceof LivingEntity) {
                LivingEntity lv = (LivingEntity)waypoint;
                BlockPos lv2 = lv.getBlockPos();
                return lv.getStyledDisplayName().copy().styled(style -> style.withClickEvent(new ClickEvent.SuggestCommand("/execute in " + string + " run tp @s " + lv2.getX() + " " + lv2.getY() + " " + lv2.getZ())).withHoverEvent(new HoverEvent.ShowText(Text.translatable("chat.coordinates.tooltip"))).withColor(arg2.getWaypointConfig().color.orElse(-1)));
            }
            return Text.literal(waypoint.toString());
        }).toList(), Function.identity());
        source.sendFeedback(() -> Text.translatable("commands.waypoint.list.success", set.size(), string, lv2), false);
        return set.size();
    }

    private static void updateWaypointConfig(ServerCommandSource source, ServerWaypoint waypoint, Consumer<Waypoint.Config> configConsumer) {
        ServerWorld lv = source.getWorld();
        lv.getWaypointHandler().onUntrack(waypoint);
        configConsumer.accept(waypoint.getWaypointConfig());
        lv.getWaypointHandler().onTrack(waypoint);
    }
}

