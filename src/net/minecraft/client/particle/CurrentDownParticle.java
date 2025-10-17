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
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class CurrentDownParticle
extends BillboardParticle {
    private float accelerationAngle;

    CurrentDownParticle(ClientWorld arg, double d, double e, double f, Sprite arg2) {
        super(arg, d, e, f, arg2);
        this.maxAge = (int)(Math.random() * 60.0) + 30;
        this.collidesWithWorld = false;
        this.velocityX = 0.0;
        this.velocityY = -0.05;
        this.velocityZ = 0.0;
        this.setBoundingBoxSpacing(0.02f, 0.02f);
        this.scale *= this.random.nextFloat() * 0.6f + 0.2f;
        this.gravityStrength = 0.002f;
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_OPAQUE;
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
        float f = 0.6f;
        this.velocityX += (double)(0.6f * MathHelper.cos(this.accelerationAngle));
        this.velocityZ += (double)(0.6f * MathHelper.sin(this.accelerationAngle));
        this.velocityX *= 0.07;
        this.velocityZ *= 0.07;
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        if (!this.world.getFluidState(BlockPos.ofFloored(this.x, this.y, this.z)).isIn(FluidTags.WATER) || this.onGround) {
            this.markDead();
        }
        this.accelerationAngle += 0.08f;
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
            return new CurrentDownParticle(arg2, d, e, f, this.spriteProvider.getSprite(arg3));
        }
    }
}

