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
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.Atlases;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class CrackParticle
extends BillboardParticle {
    private final float sampleU;
    private final float sampleV;

    CrackParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i, Sprite arg2) {
        this(arg, d, e, f, arg2);
        this.velocityX *= (double)0.1f;
        this.velocityY *= (double)0.1f;
        this.velocityZ *= (double)0.1f;
        this.velocityX += g;
        this.velocityY += h;
        this.velocityZ += i;
    }

    @Override
    public BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.BLOCK_ATLAS_TRANSLUCENT;
    }

    protected CrackParticle(ClientWorld arg, double d, double e, double f, Sprite arg2) {
        super(arg, d, e, f, 0.0, 0.0, 0.0, arg2);
        this.gravityStrength = 1.0f;
        this.scale /= 2.0f;
        this.sampleU = this.random.nextFloat() * 3.0f;
        this.sampleV = this.random.nextFloat() * 3.0f;
    }

    @Override
    protected float getMinU() {
        return this.sprite.getFrameU((this.sampleU + 1.0f) / 4.0f);
    }

    @Override
    protected float getMaxU() {
        return this.sprite.getFrameU(this.sampleU / 4.0f);
    }

    @Override
    protected float getMinV() {
        return this.sprite.getFrameV(this.sampleV / 4.0f);
    }

    @Override
    protected float getMaxV() {
        return this.sprite.getFrameV((this.sampleV + 1.0f) / 4.0f);
    }

    @Environment(value=EnvType.CLIENT)
    public static class SnowballFactory
    extends Factory<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            return new CrackParticle(arg2, d, e, f, this.getSprite(new ItemStack(Items.SNOWBALL), arg2, arg3));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CobwebFactory
    extends Factory<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            return new CrackParticle(arg2, d, e, f, this.getSprite(new ItemStack(Items.COBWEB), arg2, arg3));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SlimeballFactory
    extends Factory<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            return new CrackParticle(arg2, d, e, f, this.getSprite(new ItemStack(Items.SLIME_BALL), arg2, arg3));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ItemFactory
    extends Factory<ItemStackParticleEffect> {
        @Override
        public Particle createParticle(ItemStackParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i, Random arg3) {
            return new CrackParticle(arg2, d, e, f, g, h, i, this.getSprite(arg.getItemStack(), arg2, arg3));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Factory<T extends ParticleEffect>
    implements ParticleFactory<T> {
        private final ItemRenderState itemRenderState = new ItemRenderState();

        protected Sprite getSprite(ItemStack stack, ClientWorld world, Random random) {
            MinecraftClient.getInstance().getItemModelManager().clearAndUpdate(this.itemRenderState, stack, ItemDisplayContext.GROUND, world, null, 0);
            Sprite lv = this.itemRenderState.getParticleSprite(random);
            return lv != null ? lv : MinecraftClient.getInstance().getAtlasManager().getAtlasTexture(Atlases.BLOCKS).getMissingSprite();
        }
    }
}

