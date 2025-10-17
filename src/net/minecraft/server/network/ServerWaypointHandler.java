/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.waypoint.ServerWaypoint;
import net.minecraft.world.waypoint.Waypoint;
import net.minecraft.world.waypoint.WaypointHandler;

public class ServerWaypointHandler
implements WaypointHandler<ServerWaypoint> {
    private final Set<ServerWaypoint> waypoints = new HashSet<ServerWaypoint>();
    private final Set<ServerPlayerEntity> players = new HashSet<ServerPlayerEntity>();
    private final Table<ServerPlayerEntity, ServerWaypoint, ServerWaypoint.WaypointTracker> trackers = HashBasedTable.create();

    @Override
    public void onTrack(ServerWaypoint arg) {
        this.waypoints.add(arg);
        for (ServerPlayerEntity lv : this.players) {
            this.refreshTracking(lv, arg);
        }
    }

    @Override
    public void onUpdate(ServerWaypoint arg) {
        if (!this.waypoints.contains(arg)) {
            return;
        }
        Map<ServerPlayerEntity, ServerWaypoint.WaypointTracker> map = Tables.transpose(this.trackers).row(arg);
        Sets.SetView<ServerPlayerEntity> setView = Sets.difference(this.players, map.keySet());
        for (Map.Entry entry : ImmutableSet.copyOf(map.entrySet())) {
            this.refreshTracking((ServerPlayerEntity)entry.getKey(), arg, (ServerWaypoint.WaypointTracker)entry.getValue());
        }
        for (ServerPlayerEntity serverPlayerEntity : setView) {
            this.refreshTracking(serverPlayerEntity, arg);
        }
    }

    @Override
    public void onUntrack(ServerWaypoint arg) {
        this.trackers.column(arg).forEach((player, tracker) -> tracker.untrack());
        Tables.transpose(this.trackers).row(arg).clear();
        this.waypoints.remove(arg);
    }

    public void addPlayer(ServerPlayerEntity player) {
        this.players.add(player);
        for (ServerWaypoint lv : this.waypoints) {
            this.refreshTracking(player, lv);
        }
        if (player.hasWaypoint()) {
            this.onTrack(player);
        }
    }

    public void updatePlayerPos(ServerPlayerEntity player) {
        Map<ServerWaypoint, ServerWaypoint.WaypointTracker> map = this.trackers.row(player);
        Sets.SetView<ServerWaypoint> setView = Sets.difference(this.waypoints, map.keySet());
        for (Map.Entry entry : ImmutableSet.copyOf(map.entrySet())) {
            this.refreshTracking(player, (ServerWaypoint)entry.getKey(), (ServerWaypoint.WaypointTracker)entry.getValue());
        }
        for (ServerWaypoint serverWaypoint : setView) {
            this.refreshTracking(player, serverWaypoint);
        }
    }

    public void removePlayer(ServerPlayerEntity player) {
        this.trackers.row(player).values().removeIf(tracker -> {
            tracker.untrack();
            return true;
        });
        this.onUntrack(player);
        this.players.remove(player);
    }

    public void clear() {
        this.trackers.values().forEach(ServerWaypoint.WaypointTracker::untrack);
        this.trackers.clear();
    }

    public void refreshTracking(ServerWaypoint waypoint) {
        for (ServerPlayerEntity lv : this.players) {
            this.refreshTracking(lv, waypoint);
        }
    }

    public Set<ServerWaypoint> getWaypoints() {
        return this.waypoints;
    }

    private static boolean isLocatorBarEnabled(ServerPlayerEntity player) {
        return player.getEntityWorld().getServer().getGameRules().getBoolean(GameRules.LOCATOR_BAR);
    }

    private void refreshTracking(ServerPlayerEntity player, ServerWaypoint waypoint) {
        if (player == waypoint) {
            return;
        }
        if (!ServerWaypointHandler.isLocatorBarEnabled(player)) {
            return;
        }
        waypoint.createTracker(player).ifPresentOrElse(tracker -> {
            this.trackers.put(player, waypoint, (ServerWaypoint.WaypointTracker)tracker);
            tracker.track();
        }, () -> {
            ServerWaypoint.WaypointTracker lv = this.trackers.remove(player, waypoint);
            if (lv != null) {
                lv.untrack();
            }
        });
    }

    private void refreshTracking(ServerPlayerEntity player, ServerWaypoint waypoint, ServerWaypoint.WaypointTracker tracker) {
        if (player == waypoint) {
            return;
        }
        if (!ServerWaypointHandler.isLocatorBarEnabled(player)) {
            return;
        }
        if (!tracker.isInvalid()) {
            tracker.update();
            return;
        }
        waypoint.createTracker(player).ifPresentOrElse(newTracker -> {
            newTracker.track();
            this.trackers.put(player, waypoint, (ServerWaypoint.WaypointTracker)newTracker);
        }, () -> {
            tracker.untrack();
            this.trackers.remove(player, waypoint);
        });
    }

    @Override
    public /* synthetic */ void onUntrack(Waypoint waypoint) {
        this.onUntrack((ServerWaypoint)waypoint);
    }

    @Override
    public /* synthetic */ void onTrack(Waypoint waypoint) {
        this.onTrack((ServerWaypoint)waypoint);
    }
}

