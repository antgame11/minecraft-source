/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.EndLightFlashManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public class SkyRendering
implements AutoCloseable {
    private static final Identifier SUN_TEXTURE = Identifier.ofVanilla("textures/environment/sun.png");
    private static final Identifier END_FLASH_TEXTURE = Identifier.ofVanilla("textures/environment/end_flash.png");
    private static final Identifier MOON_PHASES_TEXTURE = Identifier.ofVanilla("textures/environment/moon_phases.png");
    private static final Identifier END_SKY_TEXTURE = Identifier.ofVanilla("textures/environment/end_sky.png");
    private static final float field_53144 = 512.0f;
    private static final int field_57932 = 10;
    private static final int field_57933 = 1500;
    private static final float field_62950 = 30.0f;
    private static final float field_62951 = 100.0f;
    private static final float field_62952 = 20.0f;
    private static final float field_62953 = 100.0f;
    private static final int field_62954 = 16;
    private static final int field_57934 = 6;
    private static final float field_62955 = 100.0f;
    private static final float field_62956 = 60.0f;
    private final GpuBuffer starVertexBuffer;
    private final RenderSystem.ShapeIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
    private final GpuBuffer topSkyVertexBuffer;
    private final GpuBuffer bottomSkyVertexBuffer;
    private final GpuBuffer endSkyVertexBuffer;
    private final GpuBuffer sunVertexBuffer;
    private final GpuBuffer moonPhaseVertexBuffer;
    private final GpuBuffer sunRiseVertexBuffer;
    private final GpuBuffer endFlashVertexBuffer;
    private final RenderSystem.ShapeIndexBuffer indexBuffer2 = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
    @Nullable
    private AbstractTexture sunTexture;
    @Nullable
    private AbstractTexture moonPhasesTexture;
    @Nullable
    private AbstractTexture endSkyTexture;
    @Nullable
    private AbstractTexture endFlashTexture;
    private int starIndexCount;

    public SkyRendering() {
        this.starVertexBuffer = this.createStars();
        this.endSkyVertexBuffer = SkyRendering.createEndSky();
        this.endFlashVertexBuffer = this.createEndFlash();
        this.sunVertexBuffer = this.createSun();
        this.moonPhaseVertexBuffer = this.createMoonPhases();
        this.sunRiseVertexBuffer = this.createSunRise();
        try (BufferAllocator lv = BufferAllocator.fixedSized(10 * VertexFormats.POSITION.getVertexSize());){
            BufferBuilder lv2 = new BufferBuilder(lv, VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION);
            this.createSky(lv2, 16.0f);
            try (BuiltBuffer lv3 = lv2.end();){
                this.topSkyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Top sky vertex buffer", GpuBuffer.USAGE_VERTEX, lv3.getBuffer());
            }
            lv2 = new BufferBuilder(lv, VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION);
            this.createSky(lv2, -16.0f);
            lv3 = lv2.end();
            try {
                this.bottomSkyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Bottom sky vertex buffer", GpuBuffer.USAGE_VERTEX, lv3.getBuffer());
            } finally {
                if (lv3 != null) {
                    lv3.close();
                }
            }
        }
    }

    protected void method_74924() {
        this.endSkyTexture = this.bindTexture(END_SKY_TEXTURE);
        this.endFlashTexture = this.bindTexture(END_FLASH_TEXTURE);
        this.sunTexture = this.bindTexture(SUN_TEXTURE);
        this.moonPhasesTexture = this.bindTexture(MOON_PHASES_TEXTURE);
    }

    private AbstractTexture bindTexture(Identifier arg) {
        TextureManager lv = MinecraftClient.getInstance().getTextureManager();
        AbstractTexture lv2 = lv.getTexture(arg);
        lv2.setUseMipmaps(false);
        return lv2;
    }

    private GpuBuffer createSunRise() {
        int i = 18;
        int j = VertexFormats.POSITION_COLOR.getVertexSize();
        try (BufferAllocator lv = BufferAllocator.fixedSized(18 * j);){
            BufferBuilder lv2 = new BufferBuilder(lv, VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            int k = ColorHelper.getWhite(1.0f);
            int l = ColorHelper.getWhite(0.0f);
            lv2.vertex(0.0f, 100.0f, 0.0f).color(k);
            for (int m = 0; m <= 16; ++m) {
                float f = (float)m * ((float)Math.PI * 2) / 16.0f;
                float g = MathHelper.sin(f);
                float h = MathHelper.cos(f);
                lv2.vertex(g * 120.0f, h * 120.0f, -h * 40.0f).color(l);
            }
            BuiltBuffer lv3 = lv2.end();
            try {
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Sunrise/Sunset fan", GpuBuffer.USAGE_VERTEX, lv3.getBuffer());
                if (lv3 != null) {
                    lv3.close();
                }
                return gpuBuffer;
            } catch (Throwable throwable) {
                if (lv3 != null) {
                    try {
                        lv3.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    private GpuBuffer createSun() {
        try (BufferAllocator lv = BufferAllocator.fixedSized(4 * VertexFormats.POSITION_TEXTURE.getVertexSize());){
            BufferBuilder lv2 = new BufferBuilder(lv, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            Matrix4f matrix4f = new Matrix4f();
            lv2.vertex(matrix4f, -1.0f, 0.0f, -1.0f).texture(0.0f, 0.0f);
            lv2.vertex(matrix4f, 1.0f, 0.0f, -1.0f).texture(1.0f, 0.0f);
            lv2.vertex(matrix4f, 1.0f, 0.0f, 1.0f).texture(1.0f, 1.0f);
            lv2.vertex(matrix4f, -1.0f, 0.0f, 1.0f).texture(0.0f, 1.0f);
            BuiltBuffer lv3 = lv2.end();
            try {
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Sun quad", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, lv3.getBuffer());
                if (lv3 != null) {
                    lv3.close();
                }
                return gpuBuffer;
            } catch (Throwable throwable) {
                if (lv3 != null) {
                    try {
                        lv3.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    private GpuBuffer createMoonPhases() {
        int i = 8;
        int j = VertexFormats.POSITION_TEXTURE.getVertexSize();
        try (BufferAllocator lv = BufferAllocator.fixedSized(32 * j);){
            BufferBuilder lv2 = new BufferBuilder(lv, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            Matrix4f matrix4f = new Matrix4f();
            for (int k = 0; k < 8; ++k) {
                int l = k % 4;
                int m = k / 4 % 2;
                float f = (float)l / 4.0f;
                float g = (float)m / 2.0f;
                float h = (float)(l + 1) / 4.0f;
                float n = (float)(m + 1) / 2.0f;
                lv2.vertex(matrix4f, -1.0f, 0.0f, 1.0f).texture(h, n);
                lv2.vertex(matrix4f, 1.0f, 0.0f, 1.0f).texture(f, n);
                lv2.vertex(matrix4f, 1.0f, 0.0f, -1.0f).texture(f, g);
                lv2.vertex(matrix4f, -1.0f, 0.0f, -1.0f).texture(h, g);
            }
            BuiltBuffer lv3 = lv2.end();
            try {
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Moon phases", GpuBuffer.USAGE_VERTEX, lv3.getBuffer());
                if (lv3 != null) {
                    lv3.close();
                }
                return gpuBuffer;
            } catch (Throwable throwable) {
                if (lv3 != null) {
                    try {
                        lv3.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    private GpuBuffer createStars() {
        Random lv = Random.create(10842L);
        float f = 100.0f;
        try (BufferAllocator lv2 = BufferAllocator.fixedSized(VertexFormats.POSITION.getVertexSize() * 1500 * 4);){
            BufferBuilder lv3 = new BufferBuilder(lv2, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            for (int i = 0; i < 1500; ++i) {
                float g = lv.nextFloat() * 2.0f - 1.0f;
                float h = lv.nextFloat() * 2.0f - 1.0f;
                float j = lv.nextFloat() * 2.0f - 1.0f;
                float k = 0.15f + lv.nextFloat() * 0.1f;
                float l = MathHelper.magnitude(g, h, j);
                if (l <= 0.010000001f || l >= 1.0f) continue;
                Vector3f vector3f = new Vector3f(g, h, j).normalize(100.0f);
                float m = (float)(lv.nextDouble() * 3.1415927410125732 * 2.0);
                Matrix3f matrix3f = new Matrix3f().rotateTowards(new Vector3f(vector3f).negate(), new Vector3f(0.0f, 1.0f, 0.0f)).rotateZ(-m);
                lv3.vertex(new Vector3f(k, -k, 0.0f).mul(matrix3f).add(vector3f));
                lv3.vertex(new Vector3f(k, k, 0.0f).mul(matrix3f).add(vector3f));
                lv3.vertex(new Vector3f(-k, k, 0.0f).mul(matrix3f).add(vector3f));
                lv3.vertex(new Vector3f(-k, -k, 0.0f).mul(matrix3f).add(vector3f));
            }
            BuiltBuffer lv4 = lv3.end();
            try {
                this.starIndexCount = lv4.getDrawParameters().indexCount();
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Stars vertex buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, lv4.getBuffer());
                if (lv4 != null) {
                    lv4.close();
                }
                return gpuBuffer;
            } catch (Throwable throwable) {
                if (lv4 != null) {
                    try {
                        lv4.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    private void createSky(VertexConsumer vertexConsumer, float height) {
        float g = Math.signum(height) * 512.0f;
        vertexConsumer.vertex(0.0f, height, 0.0f);
        for (int i = -180; i <= 180; i += 45) {
            vertexConsumer.vertex(g * MathHelper.cos((float)i * ((float)Math.PI / 180)), height, 512.0f * MathHelper.sin((float)i * ((float)Math.PI / 180)));
        }
    }

    private static GpuBuffer createEndSky() {
        try (BufferAllocator lv = BufferAllocator.fixedSized(24 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());){
            BufferBuilder lv2 = new BufferBuilder(lv, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (int i = 0; i < 6; ++i) {
                Matrix4f matrix4f = new Matrix4f();
                switch (i) {
                    case 1: {
                        matrix4f.rotationX(1.5707964f);
                        break;
                    }
                    case 2: {
                        matrix4f.rotationX(-1.5707964f);
                        break;
                    }
                    case 3: {
                        matrix4f.rotationX((float)Math.PI);
                        break;
                    }
                    case 4: {
                        matrix4f.rotationZ(1.5707964f);
                        break;
                    }
                    case 5: {
                        matrix4f.rotationZ(-1.5707964f);
                    }
                }
                lv2.vertex(matrix4f, -100.0f, -100.0f, -100.0f).texture(0.0f, 0.0f).color(-14145496);
                lv2.vertex(matrix4f, -100.0f, -100.0f, 100.0f).texture(0.0f, 16.0f).color(-14145496);
                lv2.vertex(matrix4f, 100.0f, -100.0f, 100.0f).texture(16.0f, 16.0f).color(-14145496);
                lv2.vertex(matrix4f, 100.0f, -100.0f, -100.0f).texture(16.0f, 0.0f).color(-14145496);
            }
            BuiltBuffer lv3 = lv2.end();
            try {
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "End sky vertex buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, lv3.getBuffer());
                if (lv3 != null) {
                    lv3.close();
                }
                return gpuBuffer;
            } catch (Throwable throwable) {
                if (lv3 != null) {
                    try {
                        lv3.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    private GpuBuffer createEndFlash() {
        try (BufferAllocator lv = BufferAllocator.fixedSized(4 * VertexFormats.POSITION_TEXTURE.getVertexSize());){
            BufferBuilder lv2 = new BufferBuilder(lv, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            Matrix4f matrix4f = new Matrix4f();
            lv2.vertex(matrix4f, -1.0f, 0.0f, -1.0f).texture(0.0f, 0.0f);
            lv2.vertex(matrix4f, 1.0f, 0.0f, -1.0f).texture(1.0f, 0.0f);
            lv2.vertex(matrix4f, 1.0f, 0.0f, 1.0f).texture(1.0f, 1.0f);
            lv2.vertex(matrix4f, -1.0f, 0.0f, 1.0f).texture(0.0f, 1.0f);
            BuiltBuffer lv3 = lv2.end();
            try {
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "End flash quad", GpuBuffer.USAGE_VERTEX, lv3.getBuffer());
                if (lv3 != null) {
                    lv3.close();
                }
                return gpuBuffer;
            } catch (Throwable throwable) {
                if (lv3 != null) {
                    try {
                        lv3.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    public void renderTopSky(float red, float green, float blue) {
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(), new Vector4f(red, green, blue, 1.0f), new Vector3f(), new Matrix4f(), 0.0f);
        GpuTextureView gpuTextureView = MinecraftClient.getInstance().getFramebuffer().getColorAttachmentView();
        GpuTextureView gpuTextureView2 = MinecraftClient.getInstance().getFramebuffer().getDepthAttachmentView();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky disc", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.POSITION_SKY);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.topSkyVertexBuffer);
            renderPass.draw(0, 10);
        }
    }

    public void updateRenderState(ClientWorld world, float f, Vec3d pos, SkyRenderState state) {
        DimensionEffects lv = world.getDimensionEffects();
        state.skyType = lv.getSkyType();
        if (state.skyType == DimensionEffects.SkyType.NONE) {
            return;
        }
        if (state.skyType == DimensionEffects.SkyType.END) {
            EndLightFlashManager lv2 = world.getEndLightFlashManager();
            if (lv2 == null) {
                return;
            }
            state.endFlashIntensity = lv2.getSkyFactor(f);
            state.endFlashPitch = lv2.getPitch();
            state.endFlashYaw = lv2.getYaw();
            return;
        }
        state.solarAngle = world.getSkyAngleRadians(f);
        state.time = world.getSkyAngle(f);
        state.rainGradient = 1.0f - world.getRainGradient(f);
        state.starBrightness = world.getStarBrightness(f) * state.rainGradient;
        state.sunriseAndSunsetColor = lv.getSkyColor(state.time);
        state.moonPhase = world.getMoonPhase();
        state.skyColor = world.getSkyColor(pos, f);
        state.shouldRenderSkyDark = this.isSkyDark(f, world);
        state.isSunTransition = lv.isSunRisingOrSetting(state.time);
    }

    private boolean isSkyDark(float f, ClientWorld arg) {
        return MinecraftClient.getInstance().player.getCameraPosVec((float)f).y - arg.getLevelProperties().getSkyDarknessHeight(arg) < 0.0;
    }

    public void renderSkyDark() {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.translate(0.0f, 12.0f, 0.0f);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(matrix4fStack, new Vector4f(0.0f, 0.0f, 0.0f, 1.0f), new Vector3f(), new Matrix4f(), 0.0f);
        GpuTextureView gpuTextureView = MinecraftClient.getInstance().getFramebuffer().getColorAttachmentView();
        GpuTextureView gpuTextureView2 = MinecraftClient.getInstance().getFramebuffer().getDepthAttachmentView();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky dark", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.POSITION_SKY);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.bottomSkyVertexBuffer);
            renderPass.draw(0, 10);
        }
        matrix4fStack.popMatrix();
    }

    public void renderCelestialBodies(MatrixStack matrices, float f, int i, float g, float h) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f * 360.0f));
        this.renderSun(g, matrices);
        this.renderMoon(i, g, matrices);
        if (h > 0.0f) {
            this.renderStars(h, matrices);
        }
        matrices.pop();
    }

    private void renderSun(float alpha, MatrixStack matrices) {
        if (this.sunTexture == null) {
            return;
        }
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(matrices.peek().getPositionMatrix());
        matrix4fStack.translate(0.0f, 100.0f, 0.0f);
        matrix4fStack.scale(30.0f, 1.0f, 30.0f);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(matrix4fStack, new Vector4f(1.0f, 1.0f, 1.0f, alpha), new Vector3f(), new Matrix4f(), 0.0f);
        GpuTextureView gpuTextureView = MinecraftClient.getInstance().getFramebuffer().getColorAttachmentView();
        GpuTextureView gpuTextureView2 = MinecraftClient.getInstance().getFramebuffer().getDepthAttachmentView();
        GpuBuffer gpuBuffer = this.indexBuffer2.getIndexBuffer(6);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky sun", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.POSITION_TEX_COLOR_CELESTIAL);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.bindSampler("Sampler0", this.sunTexture.getGlTextureView());
            renderPass.setVertexBuffer(0, this.sunVertexBuffer);
            renderPass.setIndexBuffer(gpuBuffer, this.indexBuffer2.getIndexType());
            renderPass.drawIndexed(0, 0, 6, 1);
        }
        matrix4fStack.popMatrix();
    }

    private void renderMoon(int phase, float alpha, MatrixStack arg) {
        if (this.moonPhasesTexture == null) {
            return;
        }
        int j = phase & 7;
        int k = j * 4;
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(arg.peek().getPositionMatrix());
        matrix4fStack.translate(0.0f, -100.0f, 0.0f);
        matrix4fStack.scale(20.0f, 1.0f, 20.0f);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(matrix4fStack, new Vector4f(1.0f, 1.0f, 1.0f, alpha), new Vector3f(), new Matrix4f(), 0.0f);
        GpuTextureView gpuTextureView = MinecraftClient.getInstance().getFramebuffer().getColorAttachmentView();
        GpuTextureView gpuTextureView2 = MinecraftClient.getInstance().getFramebuffer().getDepthAttachmentView();
        GpuBuffer gpuBuffer = this.indexBuffer2.getIndexBuffer(6);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky moon", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.POSITION_TEX_COLOR_CELESTIAL);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.bindSampler("Sampler0", this.moonPhasesTexture.getGlTextureView());
            renderPass.setVertexBuffer(0, this.moonPhaseVertexBuffer);
            renderPass.setIndexBuffer(gpuBuffer, this.indexBuffer2.getIndexType());
            renderPass.drawIndexed(k, 0, 6, 1);
        }
        matrix4fStack.popMatrix();
    }

    private void renderStars(float brightness, MatrixStack matrices) {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(matrices.peek().getPositionMatrix());
        RenderPipeline renderPipeline = RenderPipelines.POSITION_STARS;
        GpuTextureView gpuTextureView = MinecraftClient.getInstance().getFramebuffer().getColorAttachmentView();
        GpuTextureView gpuTextureView2 = MinecraftClient.getInstance().getFramebuffer().getDepthAttachmentView();
        GpuBuffer gpuBuffer = this.indexBuffer.getIndexBuffer(this.starIndexCount);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(matrix4fStack, new Vector4f(brightness, brightness, brightness, brightness), new Vector3f(), new Matrix4f(), 0.0f);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Stars", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.starVertexBuffer);
            renderPass.setIndexBuffer(gpuBuffer, this.indexBuffer.getIndexType());
            renderPass.drawIndexed(0, 0, this.starIndexCount, 1);
        }
        matrix4fStack.popMatrix();
    }

    public void renderGlowingSky(MatrixStack matrices, float f, int i) {
        float g = ColorHelper.getAlphaFloat(i);
        if (g <= 0.001f) {
            return;
        }
        float h = ColorHelper.getRedFloat(i);
        float j = ColorHelper.getGreenFloat(i);
        float k = ColorHelper.getBlueFloat(i);
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        float l = MathHelper.sin(f) < 0.0f ? 180.0f : 0.0f;
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(l + 90.0f));
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(matrices.peek().getPositionMatrix());
        matrix4fStack.scale(1.0f, 1.0f, g);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(matrix4fStack, new Vector4f(h, j, k, g), new Vector3f(), new Matrix4f(), 0.0f);
        GpuTextureView gpuTextureView = MinecraftClient.getInstance().getFramebuffer().getColorAttachmentView();
        GpuTextureView gpuTextureView2 = MinecraftClient.getInstance().getFramebuffer().getDepthAttachmentView();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sunrise sunset", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.POSITION_COLOR_SUNRISE_SUNSET);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.sunRiseVertexBuffer);
            renderPass.draw(0, 18);
        }
        matrix4fStack.popMatrix();
        matrices.pop();
    }

    public void renderEndSky() {
        if (this.endSkyTexture == null) {
            return;
        }
        RenderSystem.ShapeIndexBuffer lv = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        GpuBuffer gpuBuffer = lv.getIndexBuffer(36);
        GpuTextureView gpuTextureView = MinecraftClient.getInstance().getFramebuffer().getColorAttachmentView();
        GpuTextureView gpuTextureView2 = MinecraftClient.getInstance().getFramebuffer().getDepthAttachmentView();
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector3f(), new Matrix4f(), 0.0f);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "End sky", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.POSITION_TEX_COLOR_END_SKY);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.bindSampler("Sampler0", this.endSkyTexture.getGlTextureView());
            renderPass.setVertexBuffer(0, this.endSkyVertexBuffer);
            renderPass.setIndexBuffer(gpuBuffer, lv.getIndexType());
            renderPass.drawIndexed(0, 0, 36, 1);
        }
    }

    public void drawEndLightFlash(MatrixStack arg, float f, float skyFactor, float pitch) {
        if (this.endFlashTexture == null) {
            return;
        }
        arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - pitch));
        arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f - skyFactor));
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(arg.peek().getPositionMatrix());
        matrix4fStack.translate(0.0f, 100.0f, 0.0f);
        matrix4fStack.scale(60.0f, 1.0f, 60.0f);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(matrix4fStack, new Vector4f(f, f, f, f), new Vector3f(), new Matrix4f(), 0.0f);
        GpuTextureView gpuTextureView = MinecraftClient.getInstance().getFramebuffer().getColorAttachmentView();
        GpuTextureView gpuTextureView2 = MinecraftClient.getInstance().getFramebuffer().getDepthAttachmentView();
        GpuBuffer gpuBuffer = this.indexBuffer2.getIndexBuffer(6);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "End flash", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.POSITION_TEX_COLOR_CELESTIAL);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.bindSampler("Sampler0", this.endFlashTexture.getGlTextureView());
            renderPass.setVertexBuffer(0, this.endFlashVertexBuffer);
            renderPass.setIndexBuffer(gpuBuffer, this.indexBuffer2.getIndexType());
            renderPass.drawIndexed(0, 0, 6, 1);
        }
        matrix4fStack.popMatrix();
    }

    @Override
    public void close() {
        this.sunVertexBuffer.close();
        this.moonPhaseVertexBuffer.close();
        this.starVertexBuffer.close();
        this.topSkyVertexBuffer.close();
        this.bottomSkyVertexBuffer.close();
        this.endSkyVertexBuffer.close();
        this.sunRiseVertexBuffer.close();
        this.endFlashVertexBuffer.close();
    }
}

