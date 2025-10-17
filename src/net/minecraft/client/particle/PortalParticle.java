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
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class PortalParticle
extends BillboardParticle {
    private final double startX;
    private final double startY;
    private final double startZ;

    protected PortalParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i, Sprite arg2) {
        super(arg, d, e, f, arg2);
        this.velocityX = g;
        this.velocityY = h;
        this.velocityZ = i;
        this.x = d;
        this.y = e;
        this.z = f;
        this.startX = this.x;
        this.startY = this.y;
        this.startZ = this.z;
        this.scale = 0.1f * (this.random.nextFloat() * 0.2f + 0.5f);
        float j = this.random.nextFloat() * 0.6f + 0.4f;
        this.red = j * 0.9f;
        this.green = j * 0.3f;
        this.blue = j;
        this.maxAge = (int)(Math.random() * 10.0) + 40;
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_OPAQUE;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
        this.repositionFromBoundingBox();
    }

    @Override
    public float getSize(float tickProgress) {
        float g = ((float)this.age + tickProgress) / (float)this.maxAge;
        g = 1.0f - g;
        g *= g;
        g = 1.0f - g;
        return this.scale * g;
    }

    @Override
    public int getBrightness(float tint) {
        int i = super.getBrightness(tint);
        float g = (float)this.age / (float)this.maxAge;
        g *= g;
        g *= g;
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        if ((k += (int)(g * 15.0f * 16.0f)) > 240) {
            k = 240;
        }
        return j | k << 16;
    }

    @Override
    public void tick() {
        float f;
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }
        float g = f = (float)this.age / (float)this.maxAge;
        f = -f + f * f * 2.0f;
        f = 1.0f - f;
        this.x = this.startX + this.velocityX * (double)f;
        this.y = this.startY + this.velocityY * (double)f + (double)(1.0f - g);
        this.z = this.startZ + this.velocityZ * (double)f;
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
            PortalParticle lv = new PortalParticle(arg2, d, e, f, g, h, i, this.spriteProvider.getSprite(arg3));
            return lv;
        }
    }
}

