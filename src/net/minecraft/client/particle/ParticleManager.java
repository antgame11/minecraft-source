/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.BillboardParticleRenderer;
import net.minecraft.client.particle.ElderGuardianParticleRenderer;
import net.minecraft.client.particle.EmitterParticle;
import net.minecraft.client.particle.ItemPickupParticleRenderer;
import net.minecraft.client.particle.NoRenderParticleRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleSpriteManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.SubmittableBatch;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profilers;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ParticleManager {
    private static final List<ParticleTextureSheet> PARTICLE_TEXTURE_SHEETS = List.of(ParticleTextureSheet.SINGLE_QUADS, ParticleTextureSheet.ITEM_PICKUP, ParticleTextureSheet.ELDER_GUARDIANS);
    protected ClientWorld world;
    private final Map<ParticleTextureSheet, ParticleRenderer<?>> particles = Maps.newIdentityHashMap();
    private final Queue<EmitterParticle> newEmitterParticles = Queues.newArrayDeque();
    private final Queue<Particle> newParticles = Queues.newArrayDeque();
    private final Object2IntOpenHashMap<ParticleGroup> groupCounts = new Object2IntOpenHashMap();
    private final ParticleSpriteManager spriteManager;
    private final Random random = Random.create();

    public ParticleManager(ClientWorld world, ParticleSpriteManager spriteManager) {
        this.world = world;
        this.spriteManager = spriteManager;
    }

    public void addEmitter(Entity entity, ParticleEffect parameters) {
        this.newEmitterParticles.add(new EmitterParticle(this.world, entity, parameters));
    }

    public void addEmitter(Entity entity, ParticleEffect parameters, int maxAge) {
        this.newEmitterParticles.add(new EmitterParticle(this.world, entity, parameters, maxAge));
    }

    @Nullable
    public Particle addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        Particle lv = this.createParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
        if (lv != null) {
            this.addParticle(lv);
            return lv;
        }
        return null;
    }

    @Nullable
    private <T extends ParticleEffect> Particle createParticle(T parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        ParticleFactory lv = (ParticleFactory)this.spriteManager.getParticleFactories().get(Registries.PARTICLE_TYPE.getRawId(parameters.getType()));
        if (lv == null) {
            return null;
        }
        return lv.createParticle(parameters, this.world, x, y, z, velocityX, velocityY, velocityZ, this.random);
    }

    public void addParticle(Particle particle) {
        Optional<ParticleGroup> optional = particle.getGroup();
        if (optional.isPresent()) {
            if (this.canAdd(optional.get())) {
                this.newParticles.add(particle);
                this.addTo(optional.get(), 1);
            }
        } else {
            this.newParticles.add(particle);
        }
    }

    public void tick() {
        this.particles.forEach((textureSheet, particle) -> {
            Profilers.get().push(textureSheet.name());
            particle.tick();
            Profilers.get().pop();
        });
        if (!this.newEmitterParticles.isEmpty()) {
            ArrayList<EmitterParticle> list = Lists.newArrayList();
            for (EmitterParticle lv : this.newEmitterParticles) {
                lv.tick();
                if (lv.isAlive()) continue;
                list.add(lv);
            }
            this.newEmitterParticles.removeAll(list);
        }
        if (!this.newParticles.isEmpty()) {
            Particle lv2;
            while ((lv2 = this.newParticles.poll()) != null) {
                this.particles.computeIfAbsent(lv2.textureSheet(), this::createParticleRenderer).add(lv2);
            }
        }
    }

    private ParticleRenderer<?> createParticleRenderer(ParticleTextureSheet textureSheet) {
        if (textureSheet == ParticleTextureSheet.ITEM_PICKUP) {
            return new ItemPickupParticleRenderer(this);
        }
        if (textureSheet == ParticleTextureSheet.ELDER_GUARDIANS) {
            return new ElderGuardianParticleRenderer(this);
        }
        if (textureSheet == ParticleTextureSheet.NO_RENDER) {
            return new NoRenderParticleRenderer(this);
        }
        return new BillboardParticleRenderer(this, textureSheet);
    }

    protected void addTo(ParticleGroup group, int count) {
        this.groupCounts.addTo(group, count);
    }

    public void addToBatch(SubmittableBatch batch, Frustum frustum, Camera camera, float tickProgress) {
        for (ParticleTextureSheet lv : PARTICLE_TEXTURE_SHEETS) {
            ParticleRenderer<?> lv2 = this.particles.get(lv);
            if (lv2 == null || lv2.isEmpty()) continue;
            batch.add(lv2.render(frustum, camera, tickProgress));
        }
    }

    public void setWorld(@Nullable ClientWorld world) {
        this.world = world;
        this.clearParticles();
        this.newEmitterParticles.clear();
    }

    public String getDebugString() {
        return String.valueOf(this.particles.values().stream().mapToInt(ParticleRenderer::size).sum());
    }

    private boolean canAdd(ParticleGroup group) {
        return this.groupCounts.getInt(group) < group.maxCount();
    }

    public void clearParticles() {
        this.particles.clear();
        this.newParticles.clear();
        this.newEmitterParticles.clear();
        this.groupCounts.clear();
    }
}

