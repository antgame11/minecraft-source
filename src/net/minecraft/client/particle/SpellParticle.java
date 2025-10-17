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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.EffectParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class SpellParticle
extends BillboardParticle {
    private static final Random RANDOM = Random.create();
    private final SpriteProvider spriteProvider;
    private float defaultAlpha = 1.0f;

    SpellParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.5 - RANDOM.nextDouble(), velocityY, 0.5 - RANDOM.nextDouble(), spriteProvider.getFirst());
        this.velocityMultiplier = 0.96f;
        this.gravityStrength = -0.1f;
        this.ascending = true;
        this.spriteProvider = spriteProvider;
        this.velocityY *= (double)0.2f;
        if (velocityX == 0.0 && velocityZ == 0.0) {
            this.velocityX *= (double)0.1f;
            this.velocityZ *= (double)0.1f;
        }
        this.scale *= 0.75f;
        this.maxAge = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.collidesWithWorld = false;
        this.updateSprite(spriteProvider);
        if (this.isInvisible()) {
            this.setAlpha(0.0f);
        }
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateSprite(this.spriteProvider);
        this.alpha = this.isInvisible() ? 0.0f : MathHelper.lerp(0.05f, this.alpha, this.defaultAlpha);
    }

    @Override
    protected void setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.defaultAlpha = alpha;
    }

    private boolean isInvisible() {
        MinecraftClient lv = MinecraftClient.getInstance();
        ClientPlayerEntity lv2 = lv.player;
        return lv2 != null && lv2.getEyePos().squaredDistanceTo(this.x, this.y, this.z) <= 9.0 && lv.options.getPerspective().isFirstPerson() && lv2.isUsingSpyglass();
    }

    @Environment(value=EnvType.CLIENT)
    public static class InstantFactory
    implements ParticleFactory<EffectParticleEffect> {
        private final SpriteProvider spriteProvider;

        public InstantFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(EffectParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            SpellParticle lv = new SpellParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
            lv.setColor(arg.getRed(), arg.getGreen(), arg.getBlue());
            lv.move(arg.getPower());
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WitchFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public WitchFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            SpellParticle lv = new SpellParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
            float j = arg3.nextFloat() * 0.5f + 0.35f;
            lv.setColor(1.0f * j, 0.0f * j, 1.0f * j);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class EntityFactory
    implements ParticleFactory<TintedParticleEffect> {
        private final SpriteProvider spriteProvider;

        public EntityFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(TintedParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            SpellParticle lv = new SpellParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
            lv.setColor(arg.getRed(), arg.getGreen(), arg.getBlue());
            lv.setAlpha(arg.getAlpha());
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DefaultFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public DefaultFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            return new SpellParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}

