/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import com.google.common.collect.EvictingQueue;
import java.util.Iterator;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Submittable;
import net.minecraft.particle.ParticleGroup;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

@Environment(value=EnvType.CLIENT)
public abstract class ParticleRenderer<P extends Particle> {
    private static final int QUEUE_SIZE = 16384;
    protected final ParticleManager particleManager;
    protected final Queue<P> particles = EvictingQueue.create(16384);

    public ParticleRenderer(ParticleManager particleManager) {
        this.particleManager = particleManager;
    }

    public boolean isEmpty() {
        return this.particles.isEmpty();
    }

    public void tick() {
        if (!this.particles.isEmpty()) {
            Iterator iterator = this.particles.iterator();
            while (iterator.hasNext()) {
                Particle lv = (Particle)iterator.next();
                this.tickParticle(lv);
                if (lv.isAlive()) continue;
                lv.getGroup().ifPresent(group -> this.particleManager.addTo((ParticleGroup)group, -1));
                iterator.remove();
            }
        }
    }

    private void tickParticle(Particle particle) {
        try {
            particle.tick();
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Ticking Particle");
            CrashReportSection lv2 = lv.addElement("Particle being ticked");
            lv2.add("Particle", particle::toString);
            lv2.add("Particle Type", particle.textureSheet()::toString);
            throw new CrashException(lv);
        }
    }

    public void add(Particle particle) {
        this.particles.add(particle);
    }

    public int size() {
        return this.particles.size();
    }

    public abstract Submittable render(Frustum var1, Camera var2, float var3);

    public Queue<P> getParticles() {
        return this.particles;
    }
}

