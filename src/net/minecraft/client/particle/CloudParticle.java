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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class CloudParticle
extends BillboardParticle {
    private final SpriteProvider spriteProvider;

    CloudParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.0, 0.0, 0.0, spriteProvider.getFirst());
        float k;
        this.velocityMultiplier = 0.96f;
        this.spriteProvider = spriteProvider;
        float j = 2.5f;
        this.velocityX *= (double)0.1f;
        this.velocityY *= (double)0.1f;
        this.velocityZ *= (double)0.1f;
        this.velocityX += velocityX;
        this.velocityY += velocityY;
        this.velocityZ += velocityZ;
        this.red = k = 1.0f - (float)(Math.random() * (double)0.3f);
        this.green = k;
        this.blue = k;
        this.scale *= 1.875f;
        int l = (int)(8.0 / (Math.random() * 0.8 + 0.3));
        this.maxAge = (int)Math.max((float)l * 2.5f, 1.0f);
        this.collidesWithWorld = false;
        this.updateSprite(spriteProvider);
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    @Override
    public float getSize(float tickProgress) {
        return this.scale * MathHelper.clamp(((float)this.age + tickProgress) / (float)this.maxAge * 32.0f, 0.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.dead) {
            double d;
            this.updateSprite(this.spriteProvider);
            PlayerEntity lv = this.world.getClosestPlayer(this.x, this.y, this.z, 2.0, false);
            if (lv != null && this.y > (d = lv.getY())) {
                this.y += (d - this.y) * 0.2;
                this.velocityY += (lv.getVelocity().y - this.velocityY) * 0.2;
                this.setPos(this.x, this.y, this.z);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SneezeFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public SneezeFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            CloudParticle lv = new CloudParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
            lv.setColor(0.22f, 1.0f, 0.53f);
            lv.setAlpha(0.4f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CloudFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public CloudFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            return new CloudParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}

