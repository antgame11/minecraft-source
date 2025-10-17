/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.DynamicUniforms;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.gl.ScissorState;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.tracy.TracyFrameCapturer;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeSupplier;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.DeobfuscateClass;
import net.minecraft.util.collection.ArrayListDeque;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
@DeobfuscateClass
public class RenderSystem {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
    public static final int PROJECTION_MATRIX_UBO_SIZE = new Std140SizeCalculator().putMat4f().get();
    @Nullable
    private static Thread renderThread;
    @Nullable
    private static GpuDevice DEVICE;
    private static double lastDrawTime;
    private static final ShapeIndexBuffer sharedSequential;
    private static final ShapeIndexBuffer sharedSequentialQuad;
    private static final ShapeIndexBuffer sharedSequentialLines;
    private static ProjectionType projectionType;
    private static ProjectionType savedProjectionType;
    private static final Matrix4fStack modelViewStack;
    private static Matrix4f textureMatrix;
    public static final int TEXTURE_COUNT = 12;
    private static final GpuTextureView[] shaderTextures;
    @Nullable
    private static GpuBufferSlice shaderFog;
    @Nullable
    private static GpuBufferSlice shaderLightDirections;
    @Nullable
    private static GpuBufferSlice projectionMatrixBuffer;
    @Nullable
    private static GpuBufferSlice savedProjectionMatrixBuffer;
    private static float shaderLineWidth;
    private static String apiDescription;
    private static final AtomicLong pollEventsWaitStart;
    private static final AtomicBoolean pollingEvents;
    private static final ArrayListDeque<Task> PENDING_FENCES;
    @Nullable
    public static GpuTextureView outputColorTextureOverride;
    @Nullable
    public static GpuTextureView outputDepthTextureOverride;
    @Nullable
    private static GpuBuffer globalSettingsUniform;
    @Nullable
    private static DynamicUniforms dynamicUniforms;
    private static ScissorState scissorStateForRenderTypeDraws;

    public static void initRenderThread() {
        if (renderThread != null) {
            throw new IllegalStateException("Could not initialize render thread");
        }
        renderThread = Thread.currentThread();
    }

    public static boolean isOnRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    public static void assertOnRenderThread() {
        if (!RenderSystem.isOnRenderThread()) {
            throw RenderSystem.constructThreadException();
        }
    }

    private static IllegalStateException constructThreadException() {
        return new IllegalStateException("Rendersystem called from wrong thread");
    }

    private static void pollEvents() {
        pollEventsWaitStart.set(Util.getMeasuringTimeMs());
        pollingEvents.set(true);
        GLFW.glfwPollEvents();
        pollingEvents.set(false);
    }

    public static boolean isFrozenAtPollEvents() {
        return pollingEvents.get() && Util.getMeasuringTimeMs() - pollEventsWaitStart.get() > 200L;
    }

    public static void flipFrame(Window window, @Nullable TracyFrameCapturer capturer) {
        RenderSystem.pollEvents();
        Tessellator.getInstance().clear();
        GLFW.glfwSwapBuffers(window.getHandle());
        if (capturer != null) {
            capturer.markFrame();
        }
        dynamicUniforms.clear();
        MinecraftClient.getInstance().worldRenderer.rotate();
        RenderSystem.pollEvents();
    }

    public static void limitDisplayFPS(int fps) {
        double d = lastDrawTime + 1.0 / (double)fps;
        double e = GLFW.glfwGetTime();
        while (e < d) {
            GLFW.glfwWaitEventsTimeout(d - e);
            e = GLFW.glfwGetTime();
        }
        lastDrawTime = e;
    }

    public static void setShaderFog(GpuBufferSlice shaderFog) {
        RenderSystem.shaderFog = shaderFog;
    }

    @Nullable
    public static GpuBufferSlice getShaderFog() {
        return shaderFog;
    }

    public static void setShaderLights(GpuBufferSlice shaderLightDirections) {
        RenderSystem.shaderLightDirections = shaderLightDirections;
    }

    @Nullable
    public static GpuBufferSlice getShaderLights() {
        return shaderLightDirections;
    }

    public static void lineWidth(float width) {
        RenderSystem.assertOnRenderThread();
        shaderLineWidth = width;
    }

    public static float getShaderLineWidth() {
        RenderSystem.assertOnRenderThread();
        return shaderLineWidth;
    }

    public static void enableScissorForRenderTypeDraws(int i, int j, int k, int l) {
        scissorStateForRenderTypeDraws.enable(i, j, k, l);
    }

    public static void disableScissorForRenderTypeDraws() {
        scissorStateForRenderTypeDraws.disable();
    }

    public static ScissorState getScissorStateForRenderTypeDraws() {
        return scissorStateForRenderTypeDraws;
    }

    public static String getBackendDescription() {
        return String.format(Locale.ROOT, "LWJGL version %s", GLX._getLWJGLVersion());
    }

    public static String getApiDescription() {
        return apiDescription;
    }

    public static TimeSupplier.Nanoseconds initBackendSystem() {
        return GLX._initGlfw()::getAsLong;
    }

    public static void initRenderer(long windowHandle, int debugVerbosity, boolean sync, BiFunction<Identifier, ShaderType, String> shaderSourceGetter, boolean renderDebugLabels) {
        DEVICE = new GlBackend(windowHandle, debugVerbosity, sync, shaderSourceGetter, renderDebugLabels);
        apiDescription = RenderSystem.getDevice().getImplementationInformation();
        dynamicUniforms = new DynamicUniforms();
    }

    public static void setErrorCallback(GLFWErrorCallbackI callback) {
        GLX._setGlfwErrorCallback(callback);
    }

    public static void setupDefaultState() {
        modelViewStack.clear();
        textureMatrix.identity();
    }

    public static void setupOverlayColor(@Nullable GpuTextureView texture) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.setShaderTexture(1, texture);
    }

    public static void teardownOverlayColor() {
        RenderSystem.assertOnRenderThread();
        RenderSystem.setShaderTexture(1, null);
    }

    public static void setShaderTexture(int index, @Nullable GpuTextureView texture) {
        RenderSystem.assertOnRenderThread();
        if (index >= 0 && index < shaderTextures.length) {
            RenderSystem.shaderTextures[index] = texture;
        }
    }

    @Nullable
    public static GpuTextureView getShaderTexture(int index) {
        RenderSystem.assertOnRenderThread();
        if (index >= 0 && index < shaderTextures.length) {
            return shaderTextures[index];
        }
        return null;
    }

    public static void setProjectionMatrix(GpuBufferSlice projectionMatrixBuffer, ProjectionType projectionType) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.projectionMatrixBuffer = projectionMatrixBuffer;
        RenderSystem.projectionType = projectionType;
    }

    public static void setTextureMatrix(Matrix4f textureMatrix) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.textureMatrix = new Matrix4f(textureMatrix);
    }

    public static void resetTextureMatrix() {
        RenderSystem.assertOnRenderThread();
        textureMatrix.identity();
    }

    public static void backupProjectionMatrix() {
        RenderSystem.assertOnRenderThread();
        savedProjectionMatrixBuffer = projectionMatrixBuffer;
        savedProjectionType = projectionType;
    }

    public static void restoreProjectionMatrix() {
        RenderSystem.assertOnRenderThread();
        projectionMatrixBuffer = savedProjectionMatrixBuffer;
        projectionType = savedProjectionType;
    }

    @Nullable
    public static GpuBufferSlice getProjectionMatrixBuffer() {
        RenderSystem.assertOnRenderThread();
        return projectionMatrixBuffer;
    }

    public static Matrix4f getModelViewMatrix() {
        RenderSystem.assertOnRenderThread();
        return modelViewStack;
    }

    public static Matrix4fStack getModelViewStack() {
        RenderSystem.assertOnRenderThread();
        return modelViewStack;
    }

    public static Matrix4f getTextureMatrix() {
        RenderSystem.assertOnRenderThread();
        return textureMatrix;
    }

    public static ShapeIndexBuffer getSequentialBuffer(VertexFormat.DrawMode drawMode) {
        RenderSystem.assertOnRenderThread();
        return switch (drawMode) {
            case VertexFormat.DrawMode.QUADS -> sharedSequentialQuad;
            case VertexFormat.DrawMode.LINES -> sharedSequentialLines;
            default -> sharedSequential;
        };
    }

    public static void setGlobalSettingsUniform(GpuBuffer globalSettingsUniform) {
        RenderSystem.globalSettingsUniform = globalSettingsUniform;
    }

    @Nullable
    public static GpuBuffer getGlobalSettingsUniform() {
        return globalSettingsUniform;
    }

    public static ProjectionType getProjectionType() {
        RenderSystem.assertOnRenderThread();
        return projectionType;
    }

    public static void queueFencedTask(Runnable task) {
        PENDING_FENCES.addLast(new Task(task, RenderSystem.getDevice().createCommandEncoder().createFence()));
    }

    public static void executePendingTasks() {
        Task lv = PENDING_FENCES.peekFirst();
        while (lv != null) {
            if (lv.fence.awaitCompletion(0L)) {
                try {
                    lv.callback.run();
                } finally {
                    lv.fence.close();
                }
                PENDING_FENCES.removeFirst();
                lv = PENDING_FENCES.peekFirst();
                continue;
            }
            return;
        }
    }

    public static GpuDevice getDevice() {
        if (DEVICE == null) {
            throw new IllegalStateException("Can't getDevice() before it was initialized");
        }
        return DEVICE;
    }

    @Nullable
    public static GpuDevice tryGetDevice() {
        return DEVICE;
    }

    public static DynamicUniforms getDynamicUniforms() {
        if (dynamicUniforms == null) {
            throw new IllegalStateException("Can't getDynamicUniforms() before device was initialized");
        }
        return dynamicUniforms;
    }

    public static void bindDefaultUniforms(RenderPass pass) {
        GpuBufferSlice gpuBufferSlice3;
        GpuBuffer gpuBuffer;
        GpuBufferSlice gpuBufferSlice2;
        GpuBufferSlice gpuBufferSlice = RenderSystem.getProjectionMatrixBuffer();
        if (gpuBufferSlice != null) {
            pass.setUniform("Projection", gpuBufferSlice);
        }
        if ((gpuBufferSlice2 = RenderSystem.getShaderFog()) != null) {
            pass.setUniform("Fog", gpuBufferSlice2);
        }
        if ((gpuBuffer = RenderSystem.getGlobalSettingsUniform()) != null) {
            pass.setUniform("Globals", gpuBuffer);
        }
        if ((gpuBufferSlice3 = RenderSystem.getShaderLights()) != null) {
            pass.setUniform("Lighting", gpuBufferSlice3);
        }
    }

    static {
        lastDrawTime = Double.MIN_VALUE;
        sharedSequential = new ShapeIndexBuffer(1, 1, java.util.function.IntConsumer::accept);
        sharedSequentialQuad = new ShapeIndexBuffer(4, 6, (indexConsumer, firstVertexIndex) -> {
            indexConsumer.accept(firstVertexIndex);
            indexConsumer.accept(firstVertexIndex + 1);
            indexConsumer.accept(firstVertexIndex + 2);
            indexConsumer.accept(firstVertexIndex + 2);
            indexConsumer.accept(firstVertexIndex + 3);
            indexConsumer.accept(firstVertexIndex);
        });
        sharedSequentialLines = new ShapeIndexBuffer(4, 6, (indexConsumer, firstVertexIndex) -> {
            indexConsumer.accept(firstVertexIndex);
            indexConsumer.accept(firstVertexIndex + 1);
            indexConsumer.accept(firstVertexIndex + 2);
            indexConsumer.accept(firstVertexIndex + 3);
            indexConsumer.accept(firstVertexIndex + 2);
            indexConsumer.accept(firstVertexIndex + 1);
        });
        projectionType = ProjectionType.PERSPECTIVE;
        savedProjectionType = ProjectionType.PERSPECTIVE;
        modelViewStack = new Matrix4fStack(16);
        textureMatrix = new Matrix4f();
        shaderTextures = new GpuTextureView[12];
        shaderFog = null;
        shaderLineWidth = 1.0f;
        apiDescription = "Unknown";
        pollEventsWaitStart = new AtomicLong();
        pollingEvents = new AtomicBoolean(false);
        PENDING_FENCES = new ArrayListDeque();
        scissorStateForRenderTypeDraws = new ScissorState();
    }

    @Environment(value=EnvType.CLIENT)
    public static final class ShapeIndexBuffer {
        private final int vertexCountInShape;
        private final int vertexCountInTriangulated;
        private final Triangulator triangulator;
        @Nullable
        private GpuBuffer indexBuffer;
        private VertexFormat.IndexType indexType = VertexFormat.IndexType.SHORT;
        private int size;

        ShapeIndexBuffer(int vertexCountInShape, int vertexCountInTriangulated, Triangulator triangulator) {
            this.vertexCountInShape = vertexCountInShape;
            this.vertexCountInTriangulated = vertexCountInTriangulated;
            this.triangulator = triangulator;
        }

        public boolean isLargeEnough(int requiredSize) {
            return requiredSize <= this.size;
        }

        public GpuBuffer getIndexBuffer(int requiredSize) {
            this.grow(requiredSize);
            return this.indexBuffer;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void grow(int requiredSize) {
            if (this.isLargeEnough(requiredSize)) {
                return;
            }
            requiredSize = MathHelper.roundUpToMultiple(requiredSize * 2, this.vertexCountInTriangulated);
            LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", (Object)this.size, (Object)requiredSize);
            int j = requiredSize / this.vertexCountInTriangulated;
            int k = j * this.vertexCountInShape;
            VertexFormat.IndexType lv = VertexFormat.IndexType.smallestFor(k);
            int l = MathHelper.roundUpToMultiple(requiredSize * lv.size, 4);
            ByteBuffer byteBuffer = MemoryUtil.memAlloc(l);
            try {
                this.indexType = lv;
                IntConsumer intConsumer = this.getIndexConsumer(byteBuffer);
                for (int m = 0; m < requiredSize; m += this.vertexCountInTriangulated) {
                    this.triangulator.accept(intConsumer, m * this.vertexCountInShape / this.vertexCountInTriangulated);
                }
                byteBuffer.flip();
                if (this.indexBuffer != null) {
                    this.indexBuffer.close();
                }
                this.indexBuffer = RenderSystem.getDevice().createBuffer(() -> "Auto Storage index buffer", GpuBuffer.USAGE_INDEX, byteBuffer);
            } finally {
                MemoryUtil.memFree(byteBuffer);
            }
            this.size = requiredSize;
        }

        private IntConsumer getIndexConsumer(ByteBuffer indexBuffer) {
            switch (this.indexType) {
                case SHORT: {
                    return index -> indexBuffer.putShort((short)index);
                }
            }
            return indexBuffer::putInt;
        }

        public VertexFormat.IndexType getIndexType() {
            return this.indexType;
        }

        @Environment(value=EnvType.CLIENT)
        static interface Triangulator {
            public void accept(IntConsumer var1, int var2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Task(Runnable callback, GpuFence fence) {
    }
}

