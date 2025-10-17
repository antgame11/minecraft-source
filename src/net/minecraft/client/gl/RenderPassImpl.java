/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gl.CompiledShaderPipeline;
import net.minecraft.client.gl.GlCommandEncoder;
import net.minecraft.client.gl.ScissorState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RenderPassImpl
implements RenderPass {
    protected static final int field_57866 = 1;
    public static final boolean IS_DEVELOPMENT = SharedConstants.isDevelopment;
    private final GlCommandEncoder resourceManager;
    private final boolean hasDepth;
    private boolean closed;
    @Nullable
    protected CompiledShaderPipeline pipeline;
    protected final GpuBuffer[] vertexBuffers = new GpuBuffer[1];
    @Nullable
    protected GpuBuffer indexBuffer;
    protected VertexFormat.IndexType indexType = VertexFormat.IndexType.INT;
    private final ScissorState scissorState = new ScissorState();
    protected final HashMap<String, GpuBufferSlice> simpleUniforms = new HashMap();
    protected final HashMap<String, GpuTextureView> samplerUniforms = new HashMap();
    protected final Set<String> setSimpleUniforms = new HashSet<String>();
    protected int debugGroupPushCount;

    public RenderPassImpl(GlCommandEncoder resourceManager, boolean hasDepth) {
        this.resourceManager = resourceManager;
        this.hasDepth = hasDepth;
    }

    public boolean hasDepth() {
        return this.hasDepth;
    }

    @Override
    public void pushDebugGroup(Supplier<String> supplier) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        ++this.debugGroupPushCount;
        this.resourceManager.getBackend().getDebugLabelManager().pushDebugGroup(supplier);
    }

    @Override
    public void popDebugGroup() {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (this.debugGroupPushCount == 0) {
            throw new IllegalStateException("Can't pop more debug groups than was pushed!");
        }
        --this.debugGroupPushCount;
        this.resourceManager.getBackend().getDebugLabelManager().popDebugGroup();
    }

    @Override
    public void setPipeline(RenderPipeline renderPipeline) {
        if (this.pipeline == null || this.pipeline.info() != renderPipeline) {
            this.setSimpleUniforms.addAll(this.simpleUniforms.keySet());
            this.setSimpleUniforms.addAll(this.samplerUniforms.keySet());
        }
        this.pipeline = this.resourceManager.getBackend().compilePipelineCached(renderPipeline);
    }

    @Override
    public void bindSampler(String string, @Nullable GpuTextureView gpuTextureView) {
        if (gpuTextureView == null) {
            this.samplerUniforms.remove(string);
        } else {
            this.samplerUniforms.put(string, gpuTextureView);
        }
        this.setSimpleUniforms.add(string);
    }

    @Override
    public void setUniform(String string, GpuBuffer gpuBuffer) {
        this.simpleUniforms.put(string, gpuBuffer.slice());
        this.setSimpleUniforms.add(string);
    }

    @Override
    public void setUniform(String string, GpuBufferSlice gpuBufferSlice) {
        int i = this.resourceManager.getBackend().getUniformOffsetAlignment();
        if (gpuBufferSlice.offset() % i > 0) {
            throw new IllegalArgumentException("Uniform buffer offset must be aligned to " + i);
        }
        this.simpleUniforms.put(string, gpuBufferSlice);
        this.setSimpleUniforms.add(string);
    }

    @Override
    public void enableScissor(int i, int j, int k, int l) {
        this.scissorState.enable(i, j, k, l);
    }

    @Override
    public void disableScissor() {
        this.scissorState.disable();
    }

    public boolean isScissorEnabled() {
        return this.scissorState.isEnabled();
    }

    public int getScissorX() {
        return this.scissorState.getX();
    }

    public int getScissorY() {
        return this.scissorState.getY();
    }

    public int getScissorWidth() {
        return this.scissorState.getWidth();
    }

    public int getScissorHeight() {
        return this.scissorState.getHeight();
    }

    @Override
    public void setVertexBuffer(int i, GpuBuffer gpuBuffer) {
        if (i < 0 || i >= 1) {
            throw new IllegalArgumentException("Vertex buffer slot is out of range: " + i);
        }
        this.vertexBuffers[i] = gpuBuffer;
    }

    @Override
    public void setIndexBuffer(@Nullable GpuBuffer gpuBuffer, VertexFormat.IndexType arg) {
        this.indexBuffer = gpuBuffer;
        this.indexType = arg;
    }

    @Override
    public void drawIndexed(int i, int j, int k, int l) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        this.resourceManager.drawBoundObjectWithRenderPass(this, i, j, k, this.indexType, l);
    }

    @Override
    public <T> void drawMultipleIndexed(Collection<RenderPass.RenderObject<T>> collection, @Nullable GpuBuffer gpuBuffer, @Nullable VertexFormat.IndexType arg, Collection<String> collection2, T object) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        this.resourceManager.drawObjectsWithRenderPass(this, collection, gpuBuffer, arg, collection2, object);
    }

    @Override
    public void draw(int i, int j) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        this.resourceManager.drawBoundObjectWithRenderPass(this, i, 0, j, null, 1);
    }

    @Override
    public void close() {
        if (!this.closed) {
            if (this.debugGroupPushCount > 0) {
                throw new IllegalStateException("Render pass had debug groups left open!");
            }
            this.closed = true;
            this.resourceManager.closePass();
        }
    }
}

