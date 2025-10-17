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
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class ReversePortalParticle
extends PortalParticle {
    ReversePortalParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i, Sprite arg2) {
        super(arg, d, e, f, g, h, i, arg2);
        this.scale *= 1.5f;
        this.maxAge = (int)(Math.random() * 2.0) + 60;
    }

    @Override
    public float getSize(float tickProgress) {
        float g = 1.0f - ((float)this.age + tickProgress) / ((float)this.maxAge * 1.5f);
        return this.scale * g;
    }

    @Override
    public void tick() {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }
        float f = (float)this.age / (float)this.maxAge;
        this.x += this.velocityX * (double)f;
        this.y += this.velocityY * (double)f;
        this.z += this.velocityZ * (double)f;
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
            ReversePortalParticle lv = new ReversePortalParticle(arg2, d, e, f, g, h, i, this.spriteProvider.getSprite(arg3));
            return lv;
        }
    }
}

