/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.BufferManager;
import net.minecraft.client.gl.CompiledShader;
import net.minecraft.client.gl.CompiledShaderPipeline;
import net.minecraft.client.gl.DebugLabelManager;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.GlCommandEncoder;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.gl.GlGpuBuffer;
import net.minecraft.client.gl.GlImportProcessor;
import net.minecraft.client.gl.GpuBufferManager;
import net.minecraft.client.gl.GpuDeviceInfo;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBufferManager;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.texture.GlTextureView;
import net.minecraft.client.util.TextureAllocationException;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class GlBackend
implements GpuDevice {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static boolean allowGlArbVABinding = true;
    protected static boolean allowGlKhrDebug = true;
    protected static boolean allowExtDebugLabel = true;
    protected static boolean allowGlArbDebugOutput = true;
    protected static boolean allowGlArbDirectAccess = true;
    protected static boolean allowGlBufferStorage = true;
    private final CommandEncoder commandEncoder;
    @Nullable
    private final GlDebug glDebug;
    private final DebugLabelManager debugLabelManager;
    private final int maxTextureSize;
    private final BufferManager bufferManager;
    private final BiFunction<Identifier, ShaderType, String> defaultShaderSourceGetter;
    private final Map<RenderPipeline, CompiledShaderPipeline> pipelineCompileCache = new IdentityHashMap<RenderPipeline, CompiledShaderPipeline>();
    private final Map<ShaderKey, CompiledShader> shaderCompileCache = new HashMap<ShaderKey, CompiledShader>();
    private final VertexBufferManager vertexBufferManager;
    private final GpuBufferManager gpuBufferManager;
    private final Set<String> usedGlCapabilities = new HashSet<String>();
    private final int uniformOffsetAlignment;

    public GlBackend(long contextId, int debugVerbosity, boolean sync, BiFunction<Identifier, ShaderType, String> shaderSourceGetter, boolean renderDebugLabels) {
        GLFW.glfwMakeContextCurrent(contextId);
        GLCapabilities gLCapabilities = GL.createCapabilities();
        int j = GlBackend.determineMaxTextureSize();
        GLFW.glfwSetWindowSizeLimits(contextId, -1, -1, j, j);
        GpuDeviceInfo lv = GpuDeviceInfo.get(this);
        this.glDebug = GlDebug.enableDebug(debugVerbosity, sync, this.usedGlCapabilities);
        this.debugLabelManager = DebugLabelManager.create(gLCapabilities, renderDebugLabels, this.usedGlCapabilities);
        this.vertexBufferManager = VertexBufferManager.create(gLCapabilities, this.debugLabelManager, this.usedGlCapabilities);
        this.gpuBufferManager = GpuBufferManager.create(gLCapabilities, this.usedGlCapabilities);
        this.bufferManager = BufferManager.create(gLCapabilities, this.usedGlCapabilities, lv);
        this.maxTextureSize = j;
        this.defaultShaderSourceGetter = shaderSourceGetter;
        this.commandEncoder = new GlCommandEncoder(this);
        this.uniformOffsetAlignment = GL11.glGetInteger(GL31.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
        GL11.glEnable(34895);
    }

    public DebugLabelManager getDebugLabelManager() {
        return this.debugLabelManager;
    }

    @Override
    public CommandEncoder createCommandEncoder() {
        return this.commandEncoder;
    }

    @Override
    public GpuTexture createTexture(@Nullable Supplier<String> supplier, int i, TextureFormat textureFormat, int j, int k, int l, int m) {
        return this.createTexture(this.debugLabelManager.isUsable() && supplier != null ? supplier.get() : null, i, textureFormat, j, k, l, m);
    }

    @Override
    public GpuTexture createTexture(@Nullable String string, int i, TextureFormat textureFormat, int j, int k, int l, int m) {
        int r;
        int o;
        boolean bl;
        if (m < 1) {
            throw new IllegalArgumentException("mipLevels must be at least 1");
        }
        if (l < 1) {
            throw new IllegalArgumentException("depthOrLayers must be at least 1");
        }
        boolean bl2 = bl = (i & 0x10) != 0;
        if (bl) {
            if (j != k) {
                throw new IllegalArgumentException("Cubemap compatible textures must be square, but size is " + j + "x" + k);
            }
            if (l % 6 != 0) {
                throw new IllegalArgumentException("Cubemap compatible textures must have a layer count with a multiple of 6, was " + l);
            }
            if (l > 6) {
                throw new UnsupportedOperationException("Array textures are not yet supported");
            }
        } else if (l > 1) {
            throw new UnsupportedOperationException("Array or 3D textures are not yet supported");
        }
        GlStateManager.clearGlErrors();
        int n = GlStateManager._genTexture();
        if (string == null) {
            string = String.valueOf(n);
        }
        if (bl) {
            GL11.glBindTexture(34067, n);
            o = 34067;
        } else {
            GlStateManager._bindTexture(n);
            o = GlConst.GL_TEXTURE_2D;
        }
        GlStateManager._texParameter(o, GL12.GL_TEXTURE_MAX_LEVEL, m - 1);
        GlStateManager._texParameter(o, GL12.GL_TEXTURE_MIN_LOD, 0);
        GlStateManager._texParameter(o, GL12.GL_TEXTURE_MAX_LOD, m - 1);
        if (textureFormat.hasDepthAspect()) {
            GlStateManager._texParameter(o, GlConst.GL_TEXTURE_COMPARE_MODE, 0);
        }
        if (bl) {
            for (int p : GlConst.CUBEMAP_TARGETS) {
                for (int q = 0; q < m; ++q) {
                    GlStateManager._texImage2D(p, q, GlConst.toGlInternalId(textureFormat), j >> q, k >> q, 0, GlConst.toGlExternalId(textureFormat), GlConst.toGlType(textureFormat), null);
                }
            }
        } else {
            for (int r2 = 0; r2 < m; ++r2) {
                GlStateManager._texImage2D(o, r2, GlConst.toGlInternalId(textureFormat), j >> r2, k >> r2, 0, GlConst.toGlExternalId(textureFormat), GlConst.toGlType(textureFormat), null);
            }
        }
        if ((r = GlStateManager._getError()) == GlConst.GL_OUT_OF_MEMORY) {
            throw new TextureAllocationException("Could not allocate texture of " + j + "x" + k + " for " + string);
        }
        if (r != 0) {
            throw new IllegalStateException("OpenGL error " + r);
        }
        GlTexture lv = new GlTexture(i, string, textureFormat, j, k, l, m, n);
        this.debugLabelManager.labelGlTexture(lv);
        return lv;
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture gpuTexture) {
        return this.createTextureView(gpuTexture, 0, gpuTexture.getMipLevels());
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture gpuTexture, int i, int j) {
        if (gpuTexture.isClosed()) {
            throw new IllegalArgumentException("Can't create texture view with closed texture");
        }
        if (i < 0 || i + j > gpuTexture.getMipLevels()) {
            throw new IllegalArgumentException(j + " mip levels starting from " + i + " would be out of range for texture with only " + gpuTexture.getMipLevels() + " mip levels");
        }
        return new GlTextureView((GlTexture)gpuTexture, i, j);
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> supplier, int i, int j) {
        if (j <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than zero");
        }
        GlStateManager.clearGlErrors();
        GlGpuBuffer lv = this.gpuBufferManager.createBuffer(this.bufferManager, supplier, i, j);
        int k = GlStateManager._getError();
        if (k == GlConst.GL_OUT_OF_MEMORY) {
            throw new TextureAllocationException("Could not allocate buffer of " + j + " for " + String.valueOf(supplier));
        }
        if (k != 0) {
            throw new IllegalStateException("OpenGL error " + k);
        }
        this.debugLabelManager.labelGlGpuBuffer(lv);
        return lv;
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> supplier, int i, ByteBuffer byteBuffer) {
        if (!byteBuffer.hasRemaining()) {
            throw new IllegalArgumentException("Buffer source must not be empty");
        }
        GlStateManager.clearGlErrors();
        long l = byteBuffer.remaining();
        GlGpuBuffer lv = this.gpuBufferManager.createBuffer(this.bufferManager, supplier, i, byteBuffer);
        int j = GlStateManager._getError();
        if (j == GlConst.GL_OUT_OF_MEMORY) {
            throw new TextureAllocationException("Could not allocate buffer of " + l + " for " + String.valueOf(supplier));
        }
        if (j != 0) {
            throw new IllegalStateException("OpenGL error " + j);
        }
        this.debugLabelManager.labelGlGpuBuffer(lv);
        return lv;
    }

    @Override
    public String getImplementationInformation() {
        if (GLFW.glfwGetCurrentContext() == 0L) {
            return "NO CONTEXT";
        }
        return GlStateManager._getString(GL11.GL_RENDERER) + " GL version " + GlStateManager._getString(GL11.GL_VERSION) + ", " + GlStateManager._getString(GL11.GL_VENDOR);
    }

    @Override
    public List<String> getLastDebugMessages() {
        return this.glDebug == null ? Collections.emptyList() : this.glDebug.collectDebugMessages();
    }

    @Override
    public boolean isDebuggingEnabled() {
        return this.glDebug != null;
    }

    @Override
    public String getRenderer() {
        return GlStateManager._getString(GL11.GL_RENDERER);
    }

    @Override
    public String getVendor() {
        return GlStateManager._getString(GL11.GL_VENDOR);
    }

    @Override
    public String getBackendName() {
        return "OpenGL";
    }

    @Override
    public String getVersion() {
        return GlStateManager._getString(GL11.GL_VERSION);
    }

    private static int determineMaxTextureSize() {
        int j;
        int i = GlStateManager._getInteger(GL11.GL_MAX_TEXTURE_SIZE);
        for (j = Math.max(32768, i); j >= 1024; j >>= 1) {
            GlStateManager._texImage2D(GlConst.GL_PROXY_TEXTURE_2D, 0, GlConst.GL_RGBA, j, j, 0, GlConst.GL_RGBA, GlConst.GL_UNSIGNED_BYTE, null);
            int k = GlStateManager._getTexLevelParameter(GlConst.GL_PROXY_TEXTURE_2D, 0, GlConst.GL_TEXTURE_WIDTH);
            if (k == 0) continue;
            return j;
        }
        j = Math.max(i, 1024);
        LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", (Object)j);
        return j;
    }

    @Override
    public int getMaxTextureSize() {
        return this.maxTextureSize;
    }

    @Override
    public int getUniformOffsetAlignment() {
        return this.uniformOffsetAlignment;
    }

    @Override
    public void clearPipelineCache() {
        for (CompiledShaderPipeline lv : this.pipelineCompileCache.values()) {
            if (lv.program() == ShaderProgram.INVALID) continue;
            lv.program().close();
        }
        this.pipelineCompileCache.clear();
        for (CompiledShader lv2 : this.shaderCompileCache.values()) {
            if (lv2 == CompiledShader.INVALID_SHADER) continue;
            lv2.close();
        }
        this.shaderCompileCache.clear();
        String string = GlStateManager._getString(GL11.GL_RENDERER);
        if (string.contains("AMD")) {
            GlBackend.applyAmdCleanupHack();
        }
    }

    private static void applyAmdCleanupHack() {
        int i = GlStateManager.glCreateShader(35633);
        int j = GlStateManager.glCreateProgram();
        GlStateManager.glAttachShader(j, i);
        GlStateManager.glDeleteShader(i);
        GlStateManager.glDeleteProgram(j);
    }

    @Override
    public List<String> getEnabledExtensions() {
        return new ArrayList<String>(this.usedGlCapabilities);
    }

    @Override
    public void close() {
        this.clearPipelineCache();
    }

    public BufferManager getBufferManager() {
        return this.bufferManager;
    }

    protected CompiledShaderPipeline compilePipelineCached(RenderPipeline pipeline) {
        return this.pipelineCompileCache.computeIfAbsent(pipeline, p -> this.compileRenderPipeline(pipeline, this.defaultShaderSourceGetter));
    }

    protected CompiledShader compileShader(Identifier id, ShaderType type, Defines defines, BiFunction<Identifier, ShaderType, String> sourceRetriever) {
        ShaderKey lv = new ShaderKey(id, type, defines);
        return this.shaderCompileCache.computeIfAbsent(lv, key -> this.compileShader(lv, sourceRetriever));
    }

    @Override
    public CompiledShaderPipeline precompilePipeline(RenderPipeline renderPipeline, @Nullable BiFunction<Identifier, ShaderType, String> biFunction) {
        BiFunction<Identifier, ShaderType, String> biFunction2 = biFunction == null ? this.defaultShaderSourceGetter : biFunction;
        return this.pipelineCompileCache.computeIfAbsent(renderPipeline, renderPipeline2 -> this.compileRenderPipeline(renderPipeline, biFunction2));
    }

    private CompiledShader compileShader(ShaderKey key, BiFunction<Identifier, ShaderType, String> sourceRetriever) {
        String string = sourceRetriever.apply(key.id, key.type);
        if (string == null) {
            LOGGER.error("Couldn't find source for {} shader ({})", (Object)key.type, (Object)key.id);
            return CompiledShader.INVALID_SHADER;
        }
        String string2 = GlImportProcessor.addDefines(string, key.defines);
        int i = GlStateManager.glCreateShader(GlConst.toGl(key.type));
        GlStateManager.glShaderSource(i, string2);
        GlStateManager.glCompileShader(i);
        if (GlStateManager.glGetShaderi(i, GlConst.GL_COMPILE_STATUS) == 0) {
            String string3 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
            LOGGER.error("Couldn't compile {} shader ({}): {}", key.type.getName(), key.id, string3);
            return CompiledShader.INVALID_SHADER;
        }
        CompiledShader lv = new CompiledShader(i, key.id, key.type);
        this.debugLabelManager.labelCompiledShader(lv);
        return lv;
    }

    private CompiledShaderPipeline compileRenderPipeline(RenderPipeline pipeline, BiFunction<Identifier, ShaderType, String> sourceRetriever) {
        ShaderProgram lv3;
        CompiledShader lv = this.compileShader(pipeline.getVertexShader(), ShaderType.VERTEX, pipeline.getShaderDefines(), sourceRetriever);
        CompiledShader lv2 = this.compileShader(pipeline.getFragmentShader(), ShaderType.FRAGMENT, pipeline.getShaderDefines(), sourceRetriever);
        if (lv == CompiledShader.INVALID_SHADER) {
            LOGGER.error("Couldn't compile pipeline {}: vertex shader {} was invalid", (Object)pipeline.getLocation(), (Object)pipeline.getVertexShader());
            return new CompiledShaderPipeline(pipeline, ShaderProgram.INVALID);
        }
        if (lv2 == CompiledShader.INVALID_SHADER) {
            LOGGER.error("Couldn't compile pipeline {}: fragment shader {} was invalid", (Object)pipeline.getLocation(), (Object)pipeline.getFragmentShader());
            return new CompiledShaderPipeline(pipeline, ShaderProgram.INVALID);
        }
        try {
            lv3 = ShaderProgram.create(lv, lv2, pipeline.getVertexFormat(), pipeline.getLocation().toString());
        } catch (ShaderLoader.LoadException lv4) {
            LOGGER.error("Couldn't compile program for pipeline {}: {}", (Object)pipeline.getLocation(), (Object)lv4);
            return new CompiledShaderPipeline(pipeline, ShaderProgram.INVALID);
        }
        lv3.set(pipeline.getUniforms(), pipeline.getSamplers());
        this.debugLabelManager.labelShaderProgram(lv3);
        return new CompiledShaderPipeline(pipeline, lv3);
    }

    public VertexBufferManager getVertexBufferManager() {
        return this.vertexBufferManager;
    }

    public GpuBufferManager getGpuBufferManager() {
        return this.gpuBufferManager;
    }

    public /* synthetic */ CompiledRenderPipeline precompilePipeline(RenderPipeline renderPipeline, @Nullable BiFunction biFunction) {
        return this.precompilePipeline(renderPipeline, biFunction);
    }

    @Environment(value=EnvType.CLIENT)
    record ShaderKey(Identifier id, ShaderType type, Defines defines) {
        @Override
        public String toString() {
            String string = String.valueOf(this.id) + " (" + String.valueOf((Object)this.type) + ")";
            if (!this.defines.isEmpty()) {
                return string + " with " + String.valueOf(this.defines);
            }
            return string;
        }
    }
}

