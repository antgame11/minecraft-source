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
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class LeavesParticle
extends BillboardParticle {
    private static final float SPEED_SCALE = 0.0025f;
    private static final int field_43373 = 300;
    private static final int field_43366 = 300;
    private float angularVelocity;
    private final float angularAcceleration;
    private final float field_55127;
    private final boolean field_55128;
    private final boolean field_55129;
    private final double field_55130;
    private final double field_55131;
    private final double field_55132;

    protected LeavesParticle(ClientWorld world, double x, double y, double z, Sprite sprite, float gravity, float h, boolean bl, boolean bl2, float size, float initialYVelocity) {
        super(world, x, y, z, sprite);
        float k;
        this.angularVelocity = (float)Math.toRadians(this.random.nextBoolean() ? -30.0 : 30.0);
        this.angularAcceleration = (float)Math.toRadians(this.random.nextBoolean() ? -5.0 : 5.0);
        this.field_55127 = h;
        this.field_55128 = bl;
        this.field_55129 = bl2;
        this.maxAge = 300;
        this.gravityStrength = gravity * 1.2f * 0.0025f;
        this.scale = k = size * (this.random.nextBoolean() ? 0.05f : 0.075f);
        this.setBoundingBoxSpacing(k, k);
        this.velocityMultiplier = 1.0f;
        this.velocityY = -initialYVelocity;
        float l = this.random.nextFloat();
        this.field_55130 = Math.cos(Math.toRadians(l * 60.0f)) * (double)this.field_55127;
        this.field_55131 = Math.sin(Math.toRadians(l * 60.0f)) * (double)this.field_55127;
        this.field_55132 = Math.toRadians(1000.0f + l * 3000.0f);
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
        if (this.maxAge-- <= 0) {
            this.markDead();
        }
        if (this.dead) {
            return;
        }
        float f = 300 - this.maxAge;
        float g = Math.min(f / 300.0f, 1.0f);
        double d = 0.0;
        double e = 0.0;
        if (this.field_55129) {
            d += this.field_55130 * Math.pow(g, 1.25);
            e += this.field_55131 * Math.pow(g, 1.25);
        }
        if (this.field_55128) {
            d += (double)g * Math.cos((double)g * this.field_55132) * (double)this.field_55127;
            e += (double)g * Math.sin((double)g * this.field_55132) * (double)this.field_55127;
        }
        this.velocityX += d * (double)0.0025f;
        this.velocityZ += e * (double)0.0025f;
        this.velocityY -= (double)this.gravityStrength;
        this.angularVelocity += this.angularAcceleration / 20.0f;
        this.lastZRotation = this.zRotation;
        this.zRotation += this.angularVelocity / 20.0f;
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        if (this.onGround || this.maxAge < 299 && (this.velocityX == 0.0 || this.velocityZ == 0.0)) {
            this.markDead();
        }
        if (this.dead) {
            return;
        }
        this.velocityX *= (double)this.velocityMultiplier;
        this.velocityY *= (double)this.velocityMultiplier;
        this.velocityZ *= (double)this.velocityMultiplier;
    }

    @Environment(value=EnvType.CLIENT)
    public static class TintedLeavesFactory
    implements ParticleFactory<TintedParticleEffect> {
        private final SpriteProvider spriteProvider;

        public TintedLeavesFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(TintedParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            LeavesParticle lv = new LeavesParticle(arg2, d, e, f, this.spriteProvider.getSprite(arg3), 0.07f, 10.0f, true, false, 2.0f, 0.021f);
            lv.setColor(arg.getRed(), arg.getGreen(), arg.getBlue());
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class PaleOakLeavesFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public PaleOakLeavesFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            return new LeavesParticle(arg2, d, e, f, this.spriteProvider.getSprite(arg3), 0.07f, 10.0f, true, false, 2.0f, 0.021f);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CherryLeavesFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public CherryLeavesFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            return new LeavesParticle(arg2, d, e, f, this.spriteProvider.getSprite(arg3), 0.25f, 2.0f, false, true, 1.0f, 0.0f);
        }
    }
}

