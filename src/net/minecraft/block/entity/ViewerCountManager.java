/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ContainerUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public abstract class ViewerCountManager {
    private static final int SCHEDULE_TICK_DELAY = 5;
    private int viewerCount;
    private double maxBlockInteractionRange;

    protected abstract void onContainerOpen(World var1, BlockPos var2, BlockState var3);

    protected abstract void onContainerClose(World var1, BlockPos var2, BlockState var3);

    protected abstract void onViewerCountUpdate(World var1, BlockPos var2, BlockState var3, int var4, int var5);

    public abstract boolean isPlayerViewing(PlayerEntity var1);

    public void openContainer(LivingEntity user, World world, BlockPos pos, BlockState state, double userInteractionRange) {
        int i;
        if ((i = this.viewerCount++) == 0) {
            this.onContainerOpen(world, pos, state);
            world.emitGameEvent((Entity)user, GameEvent.CONTAINER_OPEN, pos);
            ViewerCountManager.scheduleBlockTick(world, pos, state);
        }
        this.onViewerCountUpdate(world, pos, state, i, this.viewerCount);
        this.maxBlockInteractionRange = Math.max(userInteractionRange, this.maxBlockInteractionRange);
    }

    public void closeContainer(LivingEntity user, World world, BlockPos pos, BlockState state) {
        int i = this.viewerCount--;
        if (this.viewerCount == 0) {
            this.onContainerClose(world, pos, state);
            world.emitGameEvent((Entity)user, GameEvent.CONTAINER_CLOSE, pos);
            this.maxBlockInteractionRange = 0.0;
        }
        this.onViewerCountUpdate(world, pos, state, i, this.viewerCount);
    }

    public List<ContainerUser> getViewingUsers(World world, BlockPos pos) {
        double d = this.maxBlockInteractionRange + 4.0;
        Box lv = new Box(pos).expand(d);
        return world.getOtherEntities(null, lv, entity -> this.hasViewingUsers((Entity)entity, pos)).stream().map(arg -> (ContainerUser)((Object)arg)).collect(Collectors.toList());
    }

    private boolean hasViewingUsers(Entity entity, BlockPos blockPos) {
        ContainerUser lv;
        if (entity instanceof ContainerUser && !(lv = (ContainerUser)((Object)entity)).asLivingEntity().isSpectator()) {
            return lv.isViewingContainerAt(this, blockPos);
        }
        return false;
    }

    public void updateViewerCount(World world, BlockPos pos, BlockState state) {
        List<ContainerUser> list = this.getViewingUsers(world, pos);
        this.maxBlockInteractionRange = 0.0;
        for (ContainerUser lv : list) {
            this.maxBlockInteractionRange = Math.max(lv.getContainerInteractionRange(), this.maxBlockInteractionRange);
        }
        int j = this.viewerCount;
        int i = list.size();
        if (j != i) {
            boolean bl2;
            boolean bl = i != 0;
            boolean bl3 = bl2 = j != 0;
            if (bl && !bl2) {
                this.onContainerOpen(world, pos, state);
                world.emitGameEvent(null, GameEvent.CONTAINER_OPEN, pos);
            } else if (!bl) {
                this.onContainerClose(world, pos, state);
                world.emitGameEvent(null, GameEvent.CONTAINER_CLOSE, pos);
            }
            this.viewerCount = i;
        }
        this.onViewerCountUpdate(world, pos, state, j, i);
        if (i > 0) {
            ViewerCountManager.scheduleBlockTick(world, pos, state);
        }
    }

    public int getViewerCount() {
        return this.viewerCount;
    }

    private static void scheduleBlockTick(World world, BlockPos pos, BlockState state) {
        world.scheduleBlockTick(pos, state.getBlock(), 5);
    }
}

