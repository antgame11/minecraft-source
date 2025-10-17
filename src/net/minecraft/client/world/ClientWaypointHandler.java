/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.world;

import com.mojang.datafixers.util.Either;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.world.waypoint.TrackedWaypoint;
import net.minecraft.world.waypoint.TrackedWaypointHandler;
import net.minecraft.world.waypoint.Waypoint;

@Environment(value=EnvType.CLIENT)
public class ClientWaypointHandler
implements TrackedWaypointHandler {
    private final Map<Either<UUID, String>, TrackedWaypoint> waypoints = new ConcurrentHashMap<Either<UUID, String>, TrackedWaypoint>();

    @Override
    public void onTrack(TrackedWaypoint arg) {
        this.waypoints.put(arg.getSource(), arg);
    }

    @Override
    public void onUpdate(TrackedWaypoint arg) {
        this.waypoints.get(arg.getSource()).handleUpdate(arg);
    }

    @Override
    public void onUntrack(TrackedWaypoint arg) {
        this.waypoints.remove(arg.getSource());
    }

    public boolean hasWaypoint() {
        return !this.waypoints.isEmpty();
    }

    public void forEachWaypoint(Entity receiver, Consumer<TrackedWaypoint> action) {
        this.waypoints.values().stream().sorted(Comparator.comparingDouble(waypoint -> waypoint.squaredDistanceTo(receiver)).reversed()).forEachOrdered(action);
    }

    @Override
    public /* synthetic */ void onUntrack(Waypoint waypoint) {
        this.onUntrack((TrackedWaypoint)waypoint);
    }

    @Override
    public /* synthetic */ void onTrack(Waypoint waypoint) {
        this.onTrack((TrackedWaypoint)waypoint);
    }
}

