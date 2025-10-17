/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class BlockMarkerParticle
extends BillboardParticle {
    BlockMarkerParticle(ClientWorld world, double x, double y, double z, BlockState state) {
        super(world, x, y, z, MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(state));
        this.gravityStrength = 0.0f;
        this.maxAge = 80;
        this.collidesWithWorld = false;
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.BLOCK_ATLAS_TRANSLUCENT;
    }

    @Override
    public float getSize(float tickProgress) {
        return 0.5f;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Factory
    implements ParticleFactory<BlockStateParticleEffect> {
        @Override
        public Particle createParticle(BlockStateParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            return new BlockMarkerParticle(arg2, d, e, f, arg.getBlockState());
        }
    }
}

