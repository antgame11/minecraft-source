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
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class EmotionParticle
extends BillboardParticle {
    EmotionParticle(ClientWorld arg, double d, double e, double f, Sprite arg2) {
        super(arg, d, e, f, 0.0, 0.0, 0.0, arg2);
        this.ascending = true;
        this.velocityMultiplier = 0.86f;
        this.velocityX *= (double)0.01f;
        this.velocityY *= (double)0.01f;
        this.velocityZ *= (double)0.01f;
        this.velocityY += 0.1;
        this.scale *= 1.5f;
        this.maxAge = 16;
        this.collidesWithWorld = false;
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_OPAQUE;
    }

    @Override
    public float getSize(float tickProgress) {
        return this.scale * MathHelper.clamp(((float)this.age + tickProgress) / (float)this.maxAge * 32.0f, 0.0f, 1.0f);
    }

    @Environment(value=EnvType.CLIENT)
    public static class AngryVillagerFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public AngryVillagerFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            EmotionParticle lv = new EmotionParticle(arg2, d, e + 0.5, f, this.spriteProvider.getSprite(arg3));
            lv.setColor(1.0f, 1.0f, 1.0f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class HeartFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public HeartFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            EmotionParticle lv = new EmotionParticle(arg2, d, e, f, this.spriteProvider.getSprite(arg3));
            return lv;
        }
    }
}

