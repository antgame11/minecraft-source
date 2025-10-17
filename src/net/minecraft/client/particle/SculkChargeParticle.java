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
import net.minecraft.particle.SculkChargeParticleEffect;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class SculkChargeParticle
extends BillboardParticle {
    private final SpriteProvider spriteProvider;

    SculkChargeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.getFirst());
        this.velocityMultiplier = 0.96f;
        this.spriteProvider = spriteProvider;
        this.scale(1.5f);
        this.collidesWithWorld = false;
        this.updateSprite(spriteProvider);
    }

    @Override
    public int getBrightness(float tint) {
        return 240;
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateSprite(this.spriteProvider);
    }

    @Environment(value=EnvType.CLIENT)
    public record Factory(SpriteProvider spriteProvider) implements ParticleFactory<SculkChargeParticleEffect>
    {
        @Override
        public Particle createParticle(SculkChargeParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            SculkChargeParticle lv = new SculkChargeParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
            lv.setAlpha(1.0f);
            lv.setVelocity(g, h, i);
            lv.lastZRotation = arg.roll();
            lv.zRotation = arg.roll();
            lv.setMaxAge(arg3.nextInt(12) + 8);
            return lv;
        }
    }
}

