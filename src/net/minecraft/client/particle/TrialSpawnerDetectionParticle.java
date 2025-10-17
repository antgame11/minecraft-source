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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class TrialSpawnerDetectionParticle
extends BillboardParticle {
    private final SpriteProvider spriteProvider;
    private static final int field_47460 = 8;

    protected TrialSpawnerDetectionParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scale, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.0, 0.0, 0.0, spriteProvider.getFirst());
        this.spriteProvider = spriteProvider;
        this.velocityMultiplier = 0.96f;
        this.gravityStrength = -0.1f;
        this.ascending = true;
        this.velocityX *= 0.0;
        this.velocityY *= 0.9;
        this.velocityZ *= 0.0;
        this.velocityX += velocityX;
        this.velocityY += velocityY;
        this.velocityZ += velocityZ;
        this.scale *= 0.75f * scale;
        this.maxAge = (int)(8.0f / MathHelper.nextBetween(this.random, 0.5f, 1.0f) * scale);
        this.maxAge = Math.max(this.maxAge, 1);
        this.updateSprite(spriteProvider);
        this.collidesWithWorld = true;
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_OPAQUE;
    }

    @Override
    public int getBrightness(float tint) {
        return 240;
    }

    @Override
    public BillboardParticle.Rotator getRotator() {
        return BillboardParticle.Rotator.Y_AND_W_ONLY;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateSprite(this.spriteProvider);
    }

    @Override
    public float getSize(float tickProgress) {
        return this.scale * MathHelper.clamp(((float)this.age + tickProgress) / (float)this.maxAge * 32.0f, 0.0f, 1.0f);
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
            return new TrialSpawnerDetectionParticle(arg2, d, e, f, g, h, i, 1.5f, this.spriteProvider);
        }
    }
}

