/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class WaterAnimalEntity
extends PassiveEntity {
    protected WaterAnimalEntity(EntityType<? extends WaterAnimalEntity> arg, World arg2) {
        super((EntityType<? extends PassiveEntity>)arg, arg2);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.doesNotIntersectEntities(this);
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 120;
    }

    @Override
    public int getExperienceToDrop(ServerWorld world) {
        return 1 + this.random.nextInt(3);
    }

    protected void tickBreathing(int air) {
        if (this.isAlive() && !this.isTouchingWater()) {
            this.setAir(air - 1);
            if (this.shouldDrown()) {
                this.setAir(0);
                this.serverDamage(this.getDamageSources().drown(), 2.0f);
            }
        } else {
            this.setAir(300);
        }
    }

    @Override
    public void baseTick() {
        int i = this.getAir();
        super.baseTick();
        this.tickBreathing(i);
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    public static boolean canSpawn(EntityType<? extends WaterAnimalEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        int i = world.getSeaLevel();
        int j = i - 13;
        return pos.getY() >= j && pos.getY() <= i && world.getFluidState(pos.down()).isIn(FluidTags.WATER) && world.getBlockState(pos.up()).isOf(Blocks.WATER);
    }
}

