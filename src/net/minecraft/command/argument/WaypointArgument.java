/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.waypoint.ServerWaypoint;

public class WaypointArgument {
    public static final SimpleCommandExceptionType INVALID_WAYPOINT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.waypoint.invalid"));

    public static ServerWaypoint getWaypoint(CommandContext<ServerCommandSource> context, String argument) throws CommandSyntaxException {
        Entity lv = context.getArgument(argument, EntitySelector.class).getEntity(context.getSource());
        if (lv instanceof ServerWaypoint) {
            ServerWaypoint lv2 = (ServerWaypoint)((Object)lv);
            return lv2;
        }
        throw INVALID_WAYPOINT_EXCEPTION.create();
    }
}

