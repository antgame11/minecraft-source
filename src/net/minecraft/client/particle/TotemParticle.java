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
import net.minecraft.client.particle.AnimatedParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class TotemParticle
extends AnimatedParticle {
    TotemParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, spriteProvider, 1.25f);
        this.velocityMultiplier = 0.6f;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.scale *= 0.75f;
        this.maxAge = 60 + this.random.nextInt(12);
        this.updateSprite(spriteProvider);
        if (this.random.nextInt(4) == 0) {
            this.setColor(0.6f + this.random.nextFloat() * 0.2f, 0.6f + this.random.nextFloat() * 0.3f, this.random.nextFloat() * 0.2f);
        } else {
            this.setColor(0.1f + this.random.nextFloat() * 0.2f, 0.4f + this.random.nextFloat() * 0.3f, this.random.nextFloat() * 0.2f);
        }
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
            return new TotemParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}

