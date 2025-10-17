/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.EndLightFlashManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.dimension.DimensionType;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class LightmapTextureManager
implements AutoCloseable {
    public static final int MAX_LIGHT_COORDINATE = 0xF000F0;
    public static final int MAX_SKY_LIGHT_COORDINATE = 0xF00000;
    public static final int MAX_BLOCK_LIGHT_COORDINATE = 240;
    private static final int field_53098 = 16;
    private static final int UBO_SIZE = new Std140SizeCalculator().putFloat().putFloat().putFloat().putFloat().putFloat().putFloat().putFloat().putVec3().putVec3().get();
    private static final Vector3f ALTERNATE_SKY_LIGHT_COLOR = new Vector3f(0.9f, 0.5f, 1.0f);
    private final GpuTexture glTexture;
    private final GpuTextureView glTextureView;
    private boolean dirty;
    private float flickerIntensity;
    private final GameRenderer renderer;
    private final MinecraftClient client;
    private final MappableRingBuffer buffer;

    public LightmapTextureManager(GameRenderer renderer, MinecraftClient client) {
        this.renderer = renderer;
        this.client = client;
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.glTexture = gpuDevice.createTexture("Light Texture", 12, TextureFormat.RGBA8, 16, 16, 1, 1);
        this.glTexture.setTextureFilter(FilterMode.LINEAR, false);
        this.glTextureView = gpuDevice.createTextureView(this.glTexture);
        gpuDevice.createCommandEncoder().clearColorTexture(this.glTexture, -1);
        this.buffer = new MappableRingBuffer(() -> "Lightmap UBO", 130, UBO_SIZE);
    }

    public GpuTextureView getGlTextureView() {
        return this.glTextureView;
    }

    @Override
    public void close() {
        this.glTexture.close();
        this.glTextureView.close();
        this.buffer.close();
    }

    public void tick() {
        this.flickerIntensity += (float)((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
        this.flickerIntensity *= 0.9f;
        this.dirty = true;
    }

    public void disable() {
        RenderSystem.setShaderTexture(2, null);
    }

    public void enable() {
        RenderSystem.setShaderTexture(2, this.glTextureView);
    }

    private float getDarkness(LivingEntity entity, float factor, float tickProgress) {
        float h = 0.45f * factor;
        return Math.max(0.0f, MathHelper.cos(((float)entity.age - tickProgress) * (float)Math.PI * 0.025f) * h);
    }

    public void update(float tickProgress) {
        float i;
        float h;
        Vector3f vector3f;
        if (!this.dirty) {
            return;
        }
        this.dirty = false;
        Profiler lv = Profilers.get();
        lv.push("lightTex");
        ClientWorld lv2 = this.client.world;
        if (lv2 == null) {
            return;
        }
        float g = lv2.getSkyBrightness(1.0f);
        if (lv2.getDimensionEffects().hasAlternateSkyColor()) {
            vector3f = new Vector3f(0.99f, 1.12f, 1.0f);
            EndLightFlashManager lv3 = lv2.getEndLightFlashManager();
            if (lv3 != null && !this.client.options.getHideLightningFlashes().getValue().booleanValue()) {
                h = lv3.getSkyFactor(tickProgress);
                i = this.client.inGameHud.getBossBarHud().shouldThickenFog() ? h / 3.0f : h;
            } else {
                i = 0.0f;
            }
        } else {
            vector3f = new Vector3f(1.0f, 1.0f, 1.0f);
            i = lv2.getLightningTicksLeft() > 0 ? 1.0f : g * 0.95f + 0.05f;
        }
        float j = this.client.options.getDarknessEffectScale().getValue().floatValue();
        h = this.client.player.getEffectFadeFactor(StatusEffects.DARKNESS, tickProgress) * j;
        float k = this.getDarkness(this.client.player, h, tickProgress) * j;
        float l = this.client.player.getUnderwaterVisibility();
        float m = this.client.player.hasStatusEffect(StatusEffects.NIGHT_VISION) ? GameRenderer.getNightVisionStrength(this.client.player, tickProgress) : (l > 0.0f && this.client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER) ? l : 0.0f);
        Vector3f vector3f2 = lv2.getDimensionEffects().hasAlternateSkyColor() ? ALTERNATE_SKY_LIGHT_COLOR : new Vector3f(g, g, 1.0f).lerp(new Vector3f(1.0f, 1.0f, 1.0f), 0.35f);
        float n = this.flickerIntensity + 1.5f;
        float o = lv2.getDimension().ambientLight();
        float p = this.client.options.getGamma().getValue().floatValue();
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.buffer.getBlocking(), false, true);){
            Std140Builder.intoBuffer(mappedView.data()).putFloat(o).putFloat(i).putFloat(n).putFloat(m).putFloat(k).putFloat(this.renderer.getSkyDarkness(tickProgress)).putFloat(Math.max(0.0f, p - h)).putVec3(vector3f2).putVec3(vector3f);
        }
        try (RenderPass renderPass = commandEncoder.createRenderPass(() -> "Update light", this.glTextureView, OptionalInt.empty());){
            renderPass.setPipeline(RenderPipelines.BILT_SCREEN_LIGHTMAP);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("LightmapInfo", this.buffer.getBlocking());
            renderPass.draw(0, 3);
        }
        this.buffer.rotate();
        lv.pop();
    }

    public static float getBrightness(DimensionType type, int lightLevel) {
        return LightmapTextureManager.getBrightness(type.ambientLight(), lightLevel);
    }

    public static float getBrightness(float ambientLight, int lightLevel) {
        float g = (float)lightLevel / 15.0f;
        float h = g / (4.0f - 3.0f * g);
        return MathHelper.lerp(ambientLight, h, 1.0f);
    }

    public static int pack(int block, int sky) {
        return block << 4 | sky << 20;
    }

    public static int getBlockLightCoordinates(int light) {
        return light >>> 4 & 0xF;
    }

    public static int getSkyLightCoordinates(int light) {
        return light >>> 20 & 0xF;
    }

    public static int applyEmission(int light, int lightEmission) {
        if (lightEmission == 0) {
            return light;
        }
        int k = Math.max(LightmapTextureManager.getSkyLightCoordinates(light), lightEmission);
        int l = Math.max(LightmapTextureManager.getBlockLightCoordinates(light), lightEmission);
        return LightmapTextureManager.pack(l, k);
    }
}

