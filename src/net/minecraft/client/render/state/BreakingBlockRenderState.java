/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class BreakingBlockRenderState
extends MovingBlockRenderState {
    public int breakProgress;

    public BreakingBlockRenderState(ClientWorld world, BlockPos entityBlockPos, int breakProgress) {
        this.world = world;
        this.entityBlockPos = entityBlockPos;
        this.blockState = world.getBlockState(entityBlockPos);
        this.breakProgress = breakProgress;
        this.biome = world.getBiome(entityBlockPos);
    }
}

