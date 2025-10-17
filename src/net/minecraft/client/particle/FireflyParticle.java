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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class FireflyParticle
extends BillboardParticle {
    private static final float field_56803 = 0.3f;
    private static final float field_56804 = 0.1f;
    private static final float field_56801 = 0.5f;
    private static final float field_56802 = 0.3f;
    private static final int MIN_MAX_AGE = 200;
    private static final int MAX_MAX_AGE = 300;

    FireflyParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i, Sprite arg2) {
        super(arg, d, e, f, g, h, i, arg2);
        this.ascending = true;
        this.velocityMultiplier = 0.96f;
        this.scale *= 0.75f;
        this.velocityY *= (double)0.8f;
        this.velocityX *= (double)0.8f;
        this.velocityZ *= (double)0.8f;
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    @Override
    public int getBrightness(float tint) {
        return (int)(255.0f * FireflyParticle.method_67878(this.method_67879((float)this.age + tint), 0.1f, 0.3f));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.world.getBlockState(BlockPos.ofFloored(this.x, this.y, this.z)).isAir()) {
            this.markDead();
            return;
        }
        this.setAlpha(FireflyParticle.method_67878(this.method_67879(this.age), 0.3f, 0.5f));
        if (Math.random() > 0.95 || this.age == 1) {
            this.setVelocity((double)-0.05f + (double)0.1f * Math.random(), (double)-0.05f + (double)0.1f * Math.random(), (double)-0.05f + (double)0.1f * Math.random());
        }
    }

    private float method_67879(float f) {
        return MathHelper.clamp(f / (float)this.maxAge, 0.0f, 1.0f);
    }

    private static float method_67878(float f, float g, float h) {
        if (f >= 1.0f - g) {
            return (1.0f - f) / g;
        }
        if (f <= h) {
            return f / h;
        }
        return 1.0f;
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
            FireflyParticle lv = new FireflyParticle(arg2, d, e, f, 0.5 - arg3.nextDouble(), arg3.nextBoolean() ? h : -h, 0.5 - arg3.nextDouble(), this.spriteProvider.getSprite(arg3));
            lv.setMaxAge(arg3.nextBetween(200, 300));
            lv.scale(1.5f);
            lv.setAlpha(0.0f);
            return lv;
        }
    }
}

