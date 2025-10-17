/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.fog;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.AtmosphericFogModifier;
import net.minecraft.client.render.fog.BlindnessEffectFogModifier;
import net.minecraft.client.render.fog.DarknessEffectFogModifier;
import net.minecraft.client.render.fog.DimensionOrBossFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.FogModifier;
import net.minecraft.client.render.fog.LavaFogModifier;
import net.minecraft.client.render.fog.PowderSnowFogModifier;
import net.minecraft.client.render.fog.WaterFogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

@Environment(value=EnvType.CLIENT)
public class FogRenderer
implements AutoCloseable {
    public static final int FOG_UBO_SIZE = new Std140SizeCalculator().putVec4().putFloat().putFloat().putFloat().putFloat().putFloat().putFloat().get();
    private static final List<FogModifier> FOG_MODIFIERS = Lists.newArrayList(new LavaFogModifier(), new PowderSnowFogModifier(), new BlindnessEffectFogModifier(), new DarknessEffectFogModifier(), new WaterFogModifier(), new DimensionOrBossFogModifier(), new AtmosphericFogModifier());
    private static boolean fogEnabled = true;
    private final GpuBuffer emptyBuffer;
    private final MappableRingBuffer fogBuffer;

    public FogRenderer() {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.fogBuffer = new MappableRingBuffer(() -> "Fog UBO", 130, FOG_UBO_SIZE);
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(FOG_UBO_SIZE);
            this.applyFog(byteBuffer, 0, new Vector4f(0.0f), Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            this.emptyBuffer = gpuDevice.createBuffer(() -> "Empty fog", GpuBuffer.USAGE_UNIFORM, byteBuffer.flip());
        }
        RenderSystem.setShaderFog(this.getFogBuffer(FogType.NONE));
    }

    @Override
    public void close() {
        this.emptyBuffer.close();
        this.fogBuffer.close();
    }

    public void rotate() {
        this.fogBuffer.rotate();
    }

    public GpuBufferSlice getFogBuffer(FogType fogType) {
        if (!fogEnabled) {
            return this.emptyBuffer.slice(0, FOG_UBO_SIZE);
        }
        return switch (fogType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.emptyBuffer.slice(0, FOG_UBO_SIZE);
            case 1 -> this.fogBuffer.getBlocking().slice(0, FOG_UBO_SIZE);
        };
    }

    private Vector4f getFogColor(Camera camera, float tickProgress, ClientWorld world, int viewDistance, float skyDarkness, boolean thick) {
        LivingEntity lv7;
        float o;
        CameraSubmersionType lv = this.getCameraSubmersionType(camera, thick);
        Entity lv2 = camera.getFocusedEntity();
        FogModifier lv3 = null;
        FogModifier lv4 = null;
        for (FogModifier lv5 : FOG_MODIFIERS) {
            if (lv5.shouldApply(lv, lv2)) {
                if (lv3 == null && lv5.isColorSource()) {
                    lv3 = lv5;
                }
                if (lv4 != null || !lv5.isDarknessModifier()) continue;
                lv4 = lv5;
                continue;
            }
            lv5.onSkipped();
        }
        if (lv3 == null) {
            throw new IllegalStateException("No color source environment found");
        }
        int j = lv3.getFogColor(world, camera, viewDistance, skyDarkness);
        float h = world.getLevelProperties().getVoidDarknessRange();
        float k = MathHelper.clamp((h + (float)world.getBottomY() - (float)camera.getPos().y) / h, 0.0f, 1.0f);
        if (lv4 != null) {
            LivingEntity lv6 = (LivingEntity)lv2;
            k = lv4.applyDarknessModifier(lv6, k, tickProgress);
        }
        float l = ColorHelper.getRedFloat(j);
        float m = ColorHelper.getGreenFloat(j);
        float n = ColorHelper.getBlueFloat(j);
        if (k > 0.0f && lv != CameraSubmersionType.LAVA && lv != CameraSubmersionType.POWDER_SNOW) {
            o = MathHelper.square(1.0f - k);
            l *= o;
            m *= o;
            n *= o;
        }
        if (skyDarkness > 0.0f) {
            l = MathHelper.lerp(skyDarkness, l, l * 0.7f);
            m = MathHelper.lerp(skyDarkness, m, m * 0.6f);
            n = MathHelper.lerp(skyDarkness, n, n * 0.6f);
        }
        o = lv == CameraSubmersionType.WATER ? (lv2 instanceof ClientPlayerEntity ? ((ClientPlayerEntity)lv2).getUnderwaterVisibility() : 1.0f) : (lv2 instanceof LivingEntity && (lv7 = (LivingEntity)lv2).hasStatusEffect(StatusEffects.NIGHT_VISION) && !lv7.hasStatusEffect(StatusEffects.DARKNESS) ? GameRenderer.getNightVisionStrength(lv7, tickProgress) : 0.0f);
        if (l != 0.0f && m != 0.0f && n != 0.0f) {
            float p = 1.0f / Math.max(l, Math.max(m, n));
            l = MathHelper.lerp(o, l, l * p);
            m = MathHelper.lerp(o, m, m * p);
            n = MathHelper.lerp(o, n, n * p);
        }
        return new Vector4f(l, m, n, 1.0f);
    }

    public static boolean toggleFog() {
        fogEnabled = !fogEnabled;
        return fogEnabled;
    }

    public Vector4f applyFog(Camera camera, int viewDistance, boolean thick, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world) {
        float g = tickCounter.getTickProgress(false);
        Vector4f vector4f = this.getFogColor(camera, g, world, viewDistance, skyDarkness, thick);
        float h = viewDistance * 16;
        CameraSubmersionType lv = this.getCameraSubmersionType(camera, thick);
        Entity lv2 = camera.getFocusedEntity();
        FogData lv3 = new FogData();
        for (FogModifier lv4 : FOG_MODIFIERS) {
            if (!lv4.shouldApply(lv, lv2)) continue;
            lv4.applyStartEndModifier(lv3, lv2, camera.getBlockPos(), world, h, tickCounter);
            break;
        }
        float j = MathHelper.clamp(h / 10.0f, 4.0f, 64.0f);
        lv3.renderDistanceStart = h - j;
        lv3.renderDistanceEnd = h;
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.fogBuffer.getBlocking(), false, true);){
            this.applyFog(mappedView.data(), 0, vector4f, lv3.environmentalStart, lv3.environmentalEnd, lv3.renderDistanceStart, lv3.renderDistanceEnd, lv3.skyEnd, lv3.cloudEnd);
        }
        return vector4f;
    }

    private CameraSubmersionType getCameraSubmersionType(Camera camera, boolean thick) {
        CameraSubmersionType lv = camera.getSubmersionType();
        if (lv == CameraSubmersionType.NONE) {
            if (thick) {
                return CameraSubmersionType.DIMENSION_OR_BOSS;
            }
            return CameraSubmersionType.ATMOSPHERIC;
        }
        return lv;
    }

    private void applyFog(ByteBuffer buffer, int bufPos, Vector4f fogColor, float environmentalStart, float environmentalEnd, float renderDistanceStart, float renderDistanceEnd, float skyEnd, float cloudEnd) {
        buffer.position(bufPos);
        Std140Builder.intoBuffer(buffer).putVec4(fogColor).putFloat(environmentalStart).putFloat(environmentalEnd).putFloat(renderDistanceStart).putFloat(renderDistanceEnd).putFloat(skyEnd).putFloat(cloudEnd);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FogType {
        NONE,
        WORLD;

    }
}

