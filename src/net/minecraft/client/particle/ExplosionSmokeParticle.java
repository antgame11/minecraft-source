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
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class ExplosionSmokeParticle
extends BillboardParticle {
    private final SpriteProvider spriteProvider;

    protected ExplosionSmokeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, spriteProvider.getFirst());
        float j;
        this.gravityStrength = -0.1f;
        this.velocityMultiplier = 0.9f;
        this.spriteProvider = spriteProvider;
        this.velocityX = velocityX + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.velocityY = velocityY + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.velocityZ = velocityZ + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.red = j = this.random.nextFloat() * 0.3f + 0.7f;
        this.green = j;
        this.blue = j;
        this.scale = 0.1f * (this.random.nextFloat() * this.random.nextFloat() * 6.0f + 1.0f);
        this.maxAge = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
        this.updateSprite(spriteProvider);
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateSprite(this.spriteProvider);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Factory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            return new ExplosionSmokeParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}

