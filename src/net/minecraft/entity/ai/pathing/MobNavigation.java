/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.pathing;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public class MobNavigation
extends EntityNavigation {
    private boolean avoidSunlight;
    private boolean skipRetarget;

    public MobNavigation(MobEntity arg, World arg2) {
        super(arg, arg2);
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new LandPathNodeMaker();
        return new PathNodeNavigator(this.nodeMaker, range);
    }

    @Override
    protected boolean isAtValidPosition() {
        return this.entity.isOnGround() || this.entity.isInFluid() || this.entity.hasVehicle();
    }

    @Override
    protected Vec3d getPos() {
        return new Vec3d(this.entity.getX(), this.getPathfindingY(), this.entity.getZ());
    }

    @Override
    public Path findPathTo(BlockPos target, int distance) {
        WorldChunk lv = this.world.getChunkManager().getWorldChunk(ChunkSectionPos.getSectionCoord(target.getX()), ChunkSectionPos.getSectionCoord(target.getZ()));
        if (lv == null) {
            return null;
        }
        if (!this.skipRetarget) {
            target = this.retargetToSolidBlock(lv, target, distance);
        }
        return super.findPathTo(target, distance);
    }

    final BlockPos retargetToSolidBlock(WorldChunk chunk, BlockPos pos, int distance) {
        BlockPos.Mutable lv;
        if (chunk.getBlockState(pos).isAir()) {
            lv = pos.mutableCopy().move(Direction.DOWN);
            while (lv.getY() >= this.world.getBottomY() && chunk.getBlockState(lv).isAir()) {
                lv.move(Direction.DOWN);
            }
            if (lv.getY() >= this.world.getBottomY()) {
                return lv.up();
            }
            lv.setY(pos.getY() + 1);
            while (lv.getY() <= this.world.getTopYInclusive() && chunk.getBlockState(lv).isAir()) {
                lv.move(Direction.UP);
            }
            pos = lv;
        }
        if (chunk.getBlockState(pos).isSolid()) {
            lv = pos.mutableCopy().move(Direction.UP);
            while (lv.getY() <= this.world.getTopYInclusive() && chunk.getBlockState(lv).isSolid()) {
                lv.move(Direction.UP);
            }
            return lv.toImmutable();
        }
        return pos;
    }

    @Override
    public Path findPathTo(Entity entity, int distance) {
        return this.findPathTo(entity.getBlockPos(), distance);
    }

    private int getPathfindingY() {
        if (!this.entity.isTouchingWater() || !this.canSwim()) {
            return MathHelper.floor(this.entity.getY() + 0.5);
        }
        int i = this.entity.getBlockY();
        BlockState lv = this.world.getBlockState(BlockPos.ofFloored(this.entity.getX(), i, this.entity.getZ()));
        int j = 0;
        while (lv.isOf(Blocks.WATER)) {
            lv = this.world.getBlockState(BlockPos.ofFloored(this.entity.getX(), ++i, this.entity.getZ()));
            if (++j <= 16) continue;
            return this.entity.getBlockY();
        }
        return i;
    }

    @Override
    protected void adjustPath() {
        super.adjustPath();
        if (this.avoidSunlight) {
            if (this.world.isSkyVisible(BlockPos.ofFloored(this.entity.getX(), this.entity.getY() + 0.5, this.entity.getZ()))) {
                return;
            }
            for (int i = 0; i < this.currentPath.getLength(); ++i) {
                PathNode lv = this.currentPath.getNode(i);
                if (!this.world.isSkyVisible(new BlockPos(lv.x, lv.y, lv.z))) continue;
                this.currentPath.setLength(i);
                return;
            }
        }
    }

    @Override
    public boolean canControlOpeningDoors() {
        return true;
    }

    protected boolean canWalkOnPath(PathNodeType pathType) {
        if (pathType == PathNodeType.WATER) {
            return false;
        }
        if (pathType == PathNodeType.LAVA) {
            return false;
        }
        return pathType != PathNodeType.OPEN;
    }

    public void setAvoidSunlight(boolean avoidSunlight) {
        this.avoidSunlight = avoidSunlight;
    }

    public void setCanWalkOverFences(boolean canWalkOverFences) {
        this.nodeMaker.setCanWalkOverFences(canWalkOverFences);
    }

    public void setSkipRetarget(boolean skipRetarget) {
        this.skipRetarget = skipRetarget;
    }
}

