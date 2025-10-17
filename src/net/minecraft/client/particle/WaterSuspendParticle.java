/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleGroup;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class WaterSuspendParticle
extends BillboardParticle {
    WaterSuspendParticle(ClientWorld arg, double d, double e, double f, Sprite arg2) {
        super(arg, d, e - 0.125, f, arg2);
        this.setBoundingBoxSpacing(0.01f, 0.01f);
        this.scale *= this.random.nextFloat() * 0.6f + 0.2f;
        this.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.collidesWithWorld = false;
        this.velocityMultiplier = 1.0f;
        this.gravityStrength = 0.0f;
    }

    WaterSuspendParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i, Sprite arg2) {
        super(arg, d, e - 0.125, f, g, h, i, arg2);
        this.setBoundingBoxSpacing(0.01f, 0.01f);
        this.scale *= this.random.nextFloat() * 0.6f + 0.6f;
        this.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.collidesWithWorld = false;
        this.velocityMultiplier = 1.0f;
        this.gravityStrength = 0.0f;
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_OPAQUE;
    }

    @Environment(value=EnvType.CLIENT)
    public static class WarpedSporeFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public WarpedSporeFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            double j = (double)arg3.nextFloat() * -1.9 * (double)arg3.nextFloat() * 0.1;
            WaterSuspendParticle lv = new WaterSuspendParticle(arg2, d, e, f, 0.0, j, 0.0, this.spriteProvider.getSprite(arg3));
            lv.setColor(0.1f, 0.1f, 0.3f);
            lv.setBoundingBoxSpacing(0.001f, 0.001f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CrimsonSporeFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public CrimsonSporeFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            double j = arg3.nextGaussian() * (double)1.0E-6f;
            double k = arg3.nextGaussian() * (double)1.0E-4f;
            double l = arg3.nextGaussian() * (double)1.0E-6f;
            WaterSuspendParticle lv = new WaterSuspendParticle(arg2, d, e, f, j, k, l, this.spriteProvider.getSprite(arg3));
            lv.setColor(0.9f, 0.4f, 0.5f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SporeBlossomAirFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public SporeBlossomAirFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            WaterSuspendParticle lv = new WaterSuspendParticle(this, arg2, d, e, f, 0.0, -0.8f, 0.0, this.spriteProvider.getSprite(arg3)){

                @Override
                public Optional<ParticleGroup> getGroup() {
                    return Optional.of(ParticleGroup.SPORE_BLOSSOM_AIR);
                }
            };
            lv.maxAge = MathHelper.nextBetween(arg3, 500, 1000);
            lv.gravityStrength = 0.01f;
            lv.setColor(0.32f, 0.5f, 0.22f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class UnderwaterFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public UnderwaterFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            WaterSuspendParticle lv = new WaterSuspendParticle(arg2, d, e, f, this.spriteProvider.getSprite(arg3));
            lv.setColor(0.4f, 0.4f, 0.7f);
            return lv;
        }
    }
}

