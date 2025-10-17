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
import net.minecraft.particle.DragonBreathParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class DragonBreathParticle
extends BillboardParticle {
    private static final int MIN_COLOR = 11993298;
    private static final int MAX_COLOR = 14614777;
    private static final float MIN_RED = 0.7176471f;
    private static final float MIN_GREEN = 0.0f;
    private static final float MIN_BLUE = 0.8235294f;
    private static final float MAX_RED = 0.8745098f;
    private static final float MAX_GREEN = 0.0f;
    private static final float MAX_BLUE = 0.9764706f;
    private boolean reachedGround;
    private final SpriteProvider spriteProvider;

    DragonBreathParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, spriteProvider.getFirst());
        this.velocityMultiplier = 0.96f;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.red = MathHelper.nextFloat(this.random, 0.7176471f, 0.8745098f);
        this.green = MathHelper.nextFloat(this.random, 0.0f, 0.0f);
        this.blue = MathHelper.nextFloat(this.random, 0.8235294f, 0.9764706f);
        this.scale *= 0.75f;
        this.maxAge = (int)(20.0 / ((double)this.random.nextFloat() * 0.8 + 0.2));
        this.reachedGround = false;
        this.collidesWithWorld = false;
        this.spriteProvider = spriteProvider;
        this.updateSprite(spriteProvider);
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
        this.updateSprite(this.spriteProvider);
        if (this.onGround) {
            this.velocityY = 0.0;
            this.reachedGround = true;
        }
        if (this.reachedGround) {
            this.velocityY += 0.002;
        }
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        if (this.y == this.lastY) {
            this.velocityX *= 1.1;
            this.velocityZ *= 1.1;
        }
        this.velocityX *= (double)this.velocityMultiplier;
        this.velocityZ *= (double)this.velocityMultiplier;
        if (this.reachedGround) {
            this.velocityY *= (double)this.velocityMultiplier;
        }
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
    public static class Factory
    implements ParticleFactory<DragonBreathParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DragonBreathParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            DragonBreathParticle lv = new DragonBreathParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
            lv.move(arg.getPower());
            return lv;
        }
    }
}

