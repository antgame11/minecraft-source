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
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.world.ClientWorld;

@Environment(value=EnvType.CLIENT)
public class NoRenderParticle
extends Particle {
    protected NoRenderParticle(ClientWorld arg, double d, double e, double f) {
        super(arg, d, e, f);
    }

    protected NoRenderParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
        super(arg, d, e, f, g, h, i);
    }

    @Override
    public ParticleTextureSheet textureSheet() {
        return ParticleTextureSheet.NO_RENDER;
    }
}

