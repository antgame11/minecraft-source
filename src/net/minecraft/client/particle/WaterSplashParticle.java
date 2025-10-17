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
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.RainSplashParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class WaterSplashParticle
extends RainSplashParticle {
    WaterSplashParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i, Sprite arg2) {
        super(arg, d, e, f, arg2);
        this.gravityStrength = 0.04f;
        if (h == 0.0 && (g != 0.0 || i != 0.0)) {
            this.velocityX = g;
            this.velocityY = 0.1;
            this.velocityZ = i;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SplashFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public SplashFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            return new WaterSplashParticle(arg2, d, e, f, g, h, i, this.spriteProvider.getSprite(arg3));
        }
    }
}

