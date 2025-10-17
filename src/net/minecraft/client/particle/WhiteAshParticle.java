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
import net.minecraft.client.particle.AscendingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class WhiteAshParticle
extends AscendingParticle {
    private static final int COLOR = 12235202;

    protected WhiteAshParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.1f, -0.1f, 0.1f, velocityX, velocityY, velocityZ, scaleMultiplier, spriteProvider, 0.0f, 20, 0.0125f, false);
        this.red = (float)ColorHelper.getRed(12235202) / 255.0f;
        this.green = (float)ColorHelper.getGreen(12235202) / 255.0f;
        this.blue = (float)ColorHelper.getBlue(12235202) / 255.0f;
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
            double j = (double)arg3.nextFloat() * -1.9 * (double)arg3.nextFloat() * 0.1;
            double k = (double)arg3.nextFloat() * -0.5 * (double)arg3.nextFloat() * 0.1 * 5.0;
            double l = (double)arg3.nextFloat() * -1.9 * (double)arg3.nextFloat() * 0.1;
            return new WhiteAshParticle(arg2, d, e, f, j, k, l, 1.0f, this.spriteProvider);
        }
    }
}

