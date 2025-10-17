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
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class BlockLeakParticle
extends BillboardParticle {
    private final Fluid fluid;
    protected boolean obsidianTear;

    BlockLeakParticle(ClientWorld world, double x, double y, double z, Fluid fluid, Sprite sprite) {
        super(world, x, y, z, sprite);
        this.setBoundingBoxSpacing(0.01f, 0.01f);
        this.gravityStrength = 0.06f;
        this.fluid = fluid;
    }

    protected Fluid getFluid() {
        return this.fluid;
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_OPAQUE;
    }

    @Override
    public int getBrightness(float tint) {
        if (this.obsidianTear) {
            return 240;
        }
        return super.getBrightness(tint);
    }

    @Override
    public void tick() {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.updateAge();
        if (this.dead) {
            return;
        }
        this.velocityY -= (double)this.gravityStrength;
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        this.updateVelocity();
        if (this.dead) {
            return;
        }
        this.velocityX *= (double)0.98f;
        this.velocityY *= (double)0.98f;
        this.velocityZ *= (double)0.98f;
        if (this.fluid == Fluids.EMPTY) {
            return;
        }
        BlockPos lv = BlockPos.ofFloored(this.x, this.y, this.z);
        FluidState lv2 = this.world.getFluidState(lv);
        if (lv2.getFluid() == this.fluid && this.y < (double)((float)lv.getY() + lv2.getHeight(this.world, lv))) {
            this.markDead();
        }
    }

    protected void updateAge() {
        if (this.maxAge-- <= 0) {
            this.markDead();
        }
    }

    protected void updateVelocity() {
    }

    @Environment(value=EnvType.CLIENT)
    public static class LandingObsidianTearFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public LandingObsidianTearFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            Landing lv = new Landing(arg2, d, e, f, Fluids.EMPTY, this.spriteProvider.getSprite(arg3));
            lv.obsidianTear = true;
            lv.maxAge = (int)(28.0 / (Math.random() * 0.8 + 0.2));
            lv.setColor(0.51171875f, 0.03125f, 0.890625f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FallingObsidianTearFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public FallingObsidianTearFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            ContinuousFalling lv = new ContinuousFalling(arg2, d, e, f, Fluids.EMPTY, ParticleTypes.LANDING_OBSIDIAN_TEAR, this.spriteProvider.getSprite(arg3));
            lv.obsidianTear = true;
            lv.gravityStrength = 0.01f;
            lv.setColor(0.51171875f, 0.03125f, 0.890625f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DrippingObsidianTearFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public DrippingObsidianTearFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            Dripping lv = new Dripping(arg2, d, e, f, Fluids.EMPTY, ParticleTypes.FALLING_OBSIDIAN_TEAR, this.spriteProvider.getSprite(arg3));
            lv.obsidianTear = true;
            lv.gravityStrength *= 0.01f;
            lv.maxAge = 100;
            lv.setColor(0.51171875f, 0.03125f, 0.890625f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FallingSporeBlossomFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public FallingSporeBlossomFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            int j = (int)(64.0f / MathHelper.nextBetween(arg2.getRandom(), 0.1f, 0.9f));
            Falling lv = new Falling(arg2, d, e, f, Fluids.EMPTY, j, this.spriteProvider.getSprite(arg3));
            lv.gravityStrength = 0.005f;
            lv.setColor(0.32f, 0.5f, 0.22f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FallingNectarFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public FallingNectarFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            Falling lv = new Falling(arg2, d, e, f, Fluids.EMPTY, this.spriteProvider.getSprite(arg3));
            lv.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
            lv.gravityStrength = 0.007f;
            lv.setColor(0.92f, 0.782f, 0.72f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FallingDripstoneLavaFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public FallingDripstoneLavaFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            DripstoneLavaDrip lv = new DripstoneLavaDrip(arg2, d, e, f, (Fluid)Fluids.LAVA, ParticleTypes.LANDING_LAVA, this.spriteProvider.getSprite(arg3));
            lv.setColor(1.0f, 0.2857143f, 0.083333336f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DrippingDripstoneLavaFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public DrippingDripstoneLavaFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            DrippingLava lv = new DrippingLava(arg2, d, e, f, Fluids.LAVA, ParticleTypes.FALLING_DRIPSTONE_LAVA, this.spriteProvider.getSprite(arg3));
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FallingDripstoneWaterFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public FallingDripstoneWaterFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            DripstoneLavaDrip lv = new DripstoneLavaDrip(arg2, d, e, f, (Fluid)Fluids.WATER, ParticleTypes.SPLASH, this.spriteProvider.getSprite(arg3));
            lv.setColor(0.2f, 0.3f, 1.0f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DrippingDripstoneWaterFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public DrippingDripstoneWaterFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            Dripping lv = new Dripping(arg2, d, e, f, Fluids.WATER, ParticleTypes.FALLING_DRIPSTONE_WATER, this.spriteProvider.getSprite(arg3));
            lv.setColor(0.2f, 0.3f, 1.0f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class LandingHoneyFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public LandingHoneyFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            Landing lv = new Landing(arg2, d, e, f, Fluids.EMPTY, this.spriteProvider.getSprite(arg3));
            lv.maxAge = (int)(128.0 / (Math.random() * 0.8 + 0.2));
            lv.setColor(0.522f, 0.408f, 0.082f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FallingHoneyFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public FallingHoneyFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            FallingHoney lv = new FallingHoney(arg2, d, e, f, Fluids.EMPTY, ParticleTypes.LANDING_HONEY, this.spriteProvider.getSprite(arg3));
            lv.gravityStrength = 0.01f;
            lv.setColor(0.582f, 0.448f, 0.082f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DrippingHoneyFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public DrippingHoneyFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            Dripping lv = new Dripping(arg2, d, e, f, Fluids.EMPTY, ParticleTypes.FALLING_HONEY, this.spriteProvider.getSprite(arg3));
            lv.gravityStrength *= 0.01f;
            lv.maxAge = 100;
            lv.setColor(0.622f, 0.508f, 0.082f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class LandingLavaFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public LandingLavaFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            Landing lv = new Landing(arg2, d, e, f, Fluids.LAVA, this.spriteProvider.getSprite(arg3));
            lv.setColor(1.0f, 0.2857143f, 0.083333336f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FallingLavaFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public FallingLavaFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            ContinuousFalling lv = new ContinuousFalling(arg2, d, e, f, (Fluid)Fluids.LAVA, ParticleTypes.LANDING_LAVA, this.spriteProvider.getSprite(arg3));
            lv.setColor(1.0f, 0.2857143f, 0.083333336f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DrippingLavaFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public DrippingLavaFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            DrippingLava lv = new DrippingLava(arg2, d, e, f, Fluids.LAVA, ParticleTypes.FALLING_LAVA, this.spriteProvider.getSprite(arg3));
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FallingWaterFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public FallingWaterFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            ContinuousFalling lv = new ContinuousFalling(arg2, d, e, f, (Fluid)Fluids.WATER, ParticleTypes.SPLASH, this.spriteProvider.getSprite(arg3));
            lv.setColor(0.2f, 0.3f, 1.0f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DrippingWaterFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public DrippingWaterFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            Dripping lv = new Dripping(arg2, d, e, f, Fluids.WATER, ParticleTypes.FALLING_WATER, this.spriteProvider.getSprite(arg3));
            lv.setColor(0.2f, 0.3f, 1.0f);
            return lv;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Landing
    extends BlockLeakParticle {
        Landing(ClientWorld arg, double d, double e, double f, Fluid arg2, Sprite arg3) {
            super(arg, d, e, f, arg2, arg3);
            this.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Falling
    extends BlockLeakParticle {
        Falling(ClientWorld arg, double d, double e, double f, Fluid arg2, Sprite arg3) {
            this(arg, d, e, f, arg2, (int)(64.0 / (Math.random() * 0.8 + 0.2)), arg3);
        }

        Falling(ClientWorld world, double x, double y, double z, Fluid fluid, int maxAge, Sprite sprite) {
            super(world, x, y, z, fluid, sprite);
            this.maxAge = maxAge;
        }

        @Override
        protected void updateVelocity() {
            if (this.onGround) {
                this.markDead();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DripstoneLavaDrip
    extends ContinuousFalling {
        DripstoneLavaDrip(ClientWorld arg, double d, double e, double f, Fluid arg2, ParticleEffect arg3, Sprite arg4) {
            super(arg, d, e, f, arg2, arg3, arg4);
        }

        @Override
        protected void updateVelocity() {
            if (this.onGround) {
                this.markDead();
                this.world.addParticleClient(this.nextParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                SoundEvent lv = this.getFluid() == Fluids.LAVA ? SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA : SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER;
                float f = MathHelper.nextBetween(this.random, 0.3f, 1.0f);
                this.world.playSoundClient(this.x, this.y, this.z, lv, SoundCategory.BLOCKS, f, 1.0f, false);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class FallingHoney
    extends ContinuousFalling {
        FallingHoney(ClientWorld arg, double d, double e, double f, Fluid arg2, ParticleEffect arg3, Sprite arg4) {
            super(arg, d, e, f, arg2, arg3, arg4);
        }

        @Override
        protected void updateVelocity() {
            if (this.onGround) {
                this.markDead();
                this.world.addParticleClient(this.nextParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                float f = MathHelper.nextBetween(this.random, 0.3f, 1.0f);
                this.world.playSoundClient(this.x, this.y, this.z, SoundEvents.BLOCK_BEEHIVE_DRIP, SoundCategory.BLOCKS, f, 1.0f, false);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ContinuousFalling
    extends Falling {
        protected final ParticleEffect nextParticle;

        ContinuousFalling(ClientWorld world, double x, double y, double z, Fluid fluid, ParticleEffect nextParticle, Sprite sprite) {
            super(world, x, y, z, fluid, sprite);
            this.nextParticle = nextParticle;
        }

        @Override
        protected void updateVelocity() {
            if (this.onGround) {
                this.markDead();
                this.world.addParticleClient(this.nextParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DrippingLava
    extends Dripping {
        DrippingLava(ClientWorld arg, double d, double e, double f, Fluid arg2, ParticleEffect arg3, Sprite arg4) {
            super(arg, d, e, f, arg2, arg3, arg4);
        }

        @Override
        protected void updateAge() {
            this.red = 1.0f;
            this.green = 16.0f / (float)(40 - this.maxAge + 16);
            this.blue = 4.0f / (float)(40 - this.maxAge + 8);
            super.updateAge();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Dripping
    extends BlockLeakParticle {
        private final ParticleEffect nextParticle;

        Dripping(ClientWorld world, double x, double y, double z, Fluid fluid, ParticleEffect nextParticle, Sprite sprite) {
            super(world, x, y, z, fluid, sprite);
            this.nextParticle = nextParticle;
            this.gravityStrength *= 0.02f;
            this.maxAge = 40;
        }

        @Override
        protected void updateAge() {
            if (this.maxAge-- <= 0) {
                this.markDead();
                this.world.addParticleClient(this.nextParticle, this.x, this.y, this.z, this.velocityX, this.velocityY, this.velocityZ);
            }
        }

        @Override
        protected void updateVelocity() {
            this.velocityX *= 0.02;
            this.velocityY *= 0.02;
            this.velocityZ *= 0.02;
        }
    }
}

