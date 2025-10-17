/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.event.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.data.GameEventListenerDebugData;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.event.listener.GameEventListener;

public class SimpleGameEventDispatcher
implements GameEventDispatcher {
    private final List<GameEventListener> listeners = Lists.newArrayList();
    private final Set<GameEventListener> toRemove = Sets.newHashSet();
    private final List<GameEventListener> toAdd = Lists.newArrayList();
    private boolean dispatching;
    private final ServerWorld world;
    private final int ySectionCoord;
    private final DisposalCallback disposalCallback;

    public SimpleGameEventDispatcher(ServerWorld world, int ySectionCoord, DisposalCallback disposalCallback) {
        this.world = world;
        this.ySectionCoord = ySectionCoord;
        this.disposalCallback = disposalCallback;
    }

    @Override
    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    @Override
    public void addListener(GameEventListener listener) {
        if (this.dispatching) {
            this.toAdd.add(listener);
        } else {
            this.listeners.add(listener);
        }
        SimpleGameEventDispatcher.sendDebugData(this.world, listener);
    }

    private static void sendDebugData(ServerWorld world, GameEventListener listener) {
        EntityPositionSource lv4;
        Entity lv5;
        if (!world.getSubscriptionTracker().isSubscribed(DebugSubscriptionTypes.GAME_EVENT_LISTENERS)) {
            return;
        }
        GameEventListenerDebugData lv = new GameEventListenerDebugData(listener.getRange());
        PositionSource lv2 = listener.getPositionSource();
        if (lv2 instanceof BlockPositionSource) {
            BlockPositionSource lv3 = (BlockPositionSource)lv2;
            world.getSubscriptionTracker().sendBlockDebugData(lv3.pos(), DebugSubscriptionTypes.GAME_EVENT_LISTENERS, lv);
        } else if (lv2 instanceof EntityPositionSource && (lv5 = world.getEntity((lv4 = (EntityPositionSource)lv2).getUuid())) != null) {
            world.getSubscriptionTracker().sendEntityDebugData(lv5, DebugSubscriptionTypes.GAME_EVENT_LISTENERS, lv);
        }
    }

    @Override
    public void removeListener(GameEventListener listener) {
        if (this.dispatching) {
            this.toRemove.add(listener);
        } else {
            this.listeners.remove(listener);
        }
        if (this.listeners.isEmpty()) {
            this.disposalCallback.apply(this.ySectionCoord);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean dispatch(RegistryEntry<GameEvent> event, Vec3d pos, GameEvent.Emitter emitter, GameEventDispatcher.DispatchCallback callback) {
        this.dispatching = true;
        boolean bl = false;
        try {
            Iterator<GameEventListener> iterator = this.listeners.iterator();
            while (iterator.hasNext()) {
                GameEventListener lv = iterator.next();
                if (this.toRemove.remove(lv)) {
                    iterator.remove();
                    continue;
                }
                Optional<Vec3d> optional = SimpleGameEventDispatcher.dispatchTo(this.world, pos, lv);
                if (!optional.isPresent()) continue;
                callback.visit(lv, optional.get());
                bl = true;
            }
        } finally {
            this.dispatching = false;
        }
        if (!this.toAdd.isEmpty()) {
            this.listeners.addAll(this.toAdd);
            this.toAdd.clear();
        }
        if (!this.toRemove.isEmpty()) {
            this.listeners.removeAll(this.toRemove);
            this.toRemove.clear();
        }
        return bl;
    }

    private static Optional<Vec3d> dispatchTo(ServerWorld world, Vec3d listenerPos, GameEventListener listener) {
        int i;
        Optional<Vec3d> optional = listener.getPositionSource().getPos(world);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        double d = BlockPos.ofFloored(optional.get()).getSquaredDistance(BlockPos.ofFloored(listenerPos));
        if (d > (double)(i = listener.getRange() * listener.getRange())) {
            return Optional.empty();
        }
        return optional;
    }

    @FunctionalInterface
    public static interface DisposalCallback {
        public void apply(int var1);
    }
}

