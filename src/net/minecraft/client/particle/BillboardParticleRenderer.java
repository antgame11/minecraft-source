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
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Submittable;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

@Environment(value=EnvType.CLIENT)
public class BillboardParticleRenderer
extends ParticleRenderer<BillboardParticle> {
    private final ParticleTextureSheet textureSheet;
    final BillboardParticleSubmittable submittable = new BillboardParticleSubmittable();

    public BillboardParticleRenderer(ParticleManager manager, ParticleTextureSheet textureSheet) {
        super(manager);
        this.textureSheet = textureSheet;
    }

    @Override
    public Submittable render(Frustum frustum, Camera camera, float tickProgress) {
        for (BillboardParticle lv : this.particles) {
            if (!frustum.intersectPoint(lv.x, lv.y, lv.z)) continue;
            try {
                lv.render(this.submittable, camera, tickProgress);
            } catch (Throwable throwable) {
                CrashReport lv2 = CrashReport.create(throwable, "Rendering Particle");
                CrashReportSection lv3 = lv2.addElement("Particle being rendered");
                lv3.add("Particle", lv::toString);
                lv3.add("Particle Type", this.textureSheet::toString);
                throw new CrashException(lv2);
            }
        }
        return this.submittable;
    }
}

