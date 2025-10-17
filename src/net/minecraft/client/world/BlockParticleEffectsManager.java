/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.world;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockParticleEffect;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class BlockParticleEffectsManager {
    private static final int field_62022 = 512;
    private final List<Entry> pool = new ArrayList<Entry>();

    public void scheduleBlockParticles(Vec3d center, float radius, int blockCount, Pool<BlockParticleEffect> particles) {
        if (!particles.isEmpty()) {
            this.pool.add(new Entry(center, radius, blockCount, particles));
        }
    }

    public void tick(ClientWorld world) {
        if (MinecraftClient.getInstance().options.getParticles().getValue() != ParticlesMode.ALL) {
            this.pool.clear();
            return;
        }
        int i = Weighting.getWeightSum(this.pool, Entry::blockCount);
        int j = Math.min(i, 512);
        for (int k = 0; k < j; ++k) {
            Weighting.getRandom(world.getRandom(), this.pool, i, Entry::blockCount).ifPresent(entry -> this.addEffect(world, (Entry)entry));
        }
        this.pool.clear();
    }

    private void addEffect(ClientWorld world, Entry entry) {
        float f;
        Vec3d lv3;
        Vec3d lv4;
        Random lv = world.getRandom();
        Vec3d lv2 = entry.center();
        Vec3d lv5 = lv2.add(lv4 = (lv3 = new Vec3d(lv.nextFloat() * 2.0f - 1.0f, lv.nextFloat() * 2.0f - 1.0f, lv.nextFloat() * 2.0f - 1.0f).normalize()).multiply(f = (float)Math.cbrt(lv.nextFloat()) * entry.radius()));
        if (!world.getBlockState(BlockPos.ofFloored(lv5)).isAir()) {
            return;
        }
        float g = 0.5f / (f / entry.radius() + 0.1f) * lv.nextFloat() * lv.nextFloat() + 0.3f;
        BlockParticleEffect lv6 = entry.blockParticles.get(lv);
        Vec3d lv7 = lv2.add(lv4.multiply(lv6.scaling()));
        Vec3d lv8 = lv3.multiply(g * lv6.speed());
        world.addParticleClient(lv6.particle(), lv7.getX(), lv7.getY(), lv7.getZ(), lv8.getX(), lv8.getY(), lv8.getZ());
    }

    @Environment(value=EnvType.CLIENT)
    record Entry(Vec3d center, float radius, int blockCount, Pool<BlockParticleEffect> blockParticles) {
    }
}

