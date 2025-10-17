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
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class CloudRenderer
extends SinglePreparationResourceReloader<Optional<CloudCells>>
implements AutoCloseable {
    private static final int field_60075 = 16;
    private static final int field_60076 = 32;
    private static final int field_60319 = 128;
    private static final float field_53043 = 12.0f;
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec3().putVec3().get();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier CLOUD_TEXTURE = Identifier.ofVanilla("textures/environment/clouds.png");
    private static final float field_53045 = 0.6f;
    private static final long field_53046 = 0L;
    private static final int field_53047 = 4;
    private static final int field_53048 = 3;
    private static final int field_53049 = 2;
    private static final int field_53050 = 1;
    private static final int field_53051 = 0;
    private boolean rebuild = true;
    private int centerX = Integer.MIN_VALUE;
    private int centerZ = Integer.MIN_VALUE;
    private ViewMode viewMode = ViewMode.INSIDE_CLOUDS;
    @Nullable
    private CloudRenderMode renderMode;
    @Nullable
    private CloudCells cells;
    private int instanceCount = 0;
    private final RenderSystem.ShapeIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
    private final MappableRingBuffer cloudInfoBuffer = new MappableRingBuffer(() -> "Cloud UBO", 130, UBO_SIZE);
    @Nullable
    private MappableRingBuffer cloudFacesBuffer;

    /*
     * Enabled aggressive exception aggregation
     */
    @Override
    protected Optional<CloudCells> prepare(ResourceManager arg, Profiler arg2) {
        try (InputStream inputStream = arg.open(CLOUD_TEXTURE);){
            NativeImage lv = NativeImage.read(inputStream);
            try {
                int i = lv.getWidth();
                int j = lv.getHeight();
                long[] ls = new long[i * j];
                for (int k = 0; k < j; ++k) {
                    for (int l = 0; l < i; ++l) {
                        int m = lv.getColorArgb(l, k);
                        if (CloudRenderer.isEmpty(m)) {
                            ls[l + k * i] = 0L;
                            continue;
                        }
                        boolean bl = CloudRenderer.isEmpty(lv.getColorArgb(l, Math.floorMod(k - 1, j)));
                        boolean bl2 = CloudRenderer.isEmpty(lv.getColorArgb(Math.floorMod(l + 1, j), k));
                        boolean bl3 = CloudRenderer.isEmpty(lv.getColorArgb(l, Math.floorMod(k + 1, j)));
                        boolean bl4 = CloudRenderer.isEmpty(lv.getColorArgb(Math.floorMod(l - 1, j), k));
                        ls[l + k * i] = CloudRenderer.packCloudCell(m, bl, bl2, bl3, bl4);
                    }
                }
                Optional<CloudCells> optional = Optional.of(new CloudCells(ls, i, j));
                if (lv != null) {
                    lv.close();
                }
                return optional;
            } catch (Throwable throwable) {
                if (lv != null) {
                    try {
                        lv.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        } catch (IOException iOException) {
            LOGGER.error("Failed to load cloud texture", iOException);
            return Optional.empty();
        }
    }

    private static int calcCloudBufferSize(int cloudRange) {
        int j = 4;
        int k = (cloudRange + 1) * 2 * ((cloudRange + 1) * 2) / 2;
        int l = k * 4 + 54;
        return l * 3;
    }

    @Override
    protected void apply(Optional<CloudCells> optional, ResourceManager arg, Profiler arg2) {
        this.cells = optional.orElse(null);
        this.rebuild = true;
    }

    private static boolean isEmpty(int color) {
        return ColorHelper.getAlpha(color) < 10;
    }

    private static long packCloudCell(int color, boolean borderNorth, boolean borderEast, boolean borderSouth, boolean borderWest) {
        return (long)color << 4 | (long)((borderNorth ? 1 : 0) << 3) | (long)((borderEast ? 1 : 0) << 2) | (long)((borderSouth ? 1 : 0) << 1) | (long)((borderWest ? 1 : 0) << 0);
    }

    private static boolean hasBorderNorth(long packed) {
        return (packed >> 3 & 1L) != 0L;
    }

    private static boolean hasBorderEast(long packed) {
        return (packed >> 2 & 1L) != 0L;
    }

    private static boolean hasBorderSouth(long packed) {
        return (packed >> 1 & 1L) != 0L;
    }

    private static boolean hasBorderWest(long packed) {
        return (packed >> 0 & 1L) != 0L;
    }

    public void renderClouds(int color, CloudRenderMode mode, float cloudHeight, Vec3d cameraPos, float cloudPhase) {
        GpuTextureView gpuTextureView2;
        GpuTextureView gpuTextureView;
        GpuBuffer.MappedView mappedView;
        RenderPipeline renderPipeline;
        float h;
        float m;
        if (this.cells == null) {
            return;
        }
        int j = Math.min(MinecraftClient.getInstance().options.getCloudRenderDistance().getValue(), 128) * 16;
        int k = MathHelper.ceil((float)j / 12.0f);
        int l = CloudRenderer.calcCloudBufferSize(k);
        if (this.cloudFacesBuffer == null || this.cloudFacesBuffer.getBlocking().size() != l) {
            if (this.cloudFacesBuffer != null) {
                this.cloudFacesBuffer.close();
            }
            this.cloudFacesBuffer = new MappableRingBuffer(() -> "Cloud UTB", 258, l);
        }
        ViewMode lv = (m = (h = (float)((double)cloudHeight - cameraPos.y)) + 4.0f) < 0.0f ? ViewMode.ABOVE_CLOUDS : (h > 0.0f ? ViewMode.BELOW_CLOUDS : ViewMode.INSIDE_CLOUDS);
        double d = cameraPos.x + (double)(cloudPhase * 0.030000001f);
        double e = cameraPos.z + (double)3.96f;
        double n = (double)this.cells.width * 12.0;
        double o = (double)this.cells.height * 12.0;
        d -= (double)MathHelper.floor(d / n) * n;
        e -= (double)MathHelper.floor(e / o) * o;
        int p = MathHelper.floor(d / 12.0);
        int q = MathHelper.floor(e / 12.0);
        float r = (float)(d - (double)((float)p * 12.0f));
        float s = (float)(e - (double)((float)q * 12.0f));
        boolean bl = mode == CloudRenderMode.FANCY;
        RenderPipeline renderPipeline2 = renderPipeline = bl ? RenderPipelines.CLOUDS : RenderPipelines.FLAT_CLOUDS;
        if (this.rebuild || p != this.centerX || q != this.centerZ || lv != this.viewMode || mode != this.renderMode) {
            this.rebuild = false;
            this.centerX = p;
            this.centerZ = q;
            this.viewMode = lv;
            this.renderMode = mode;
            this.cloudFacesBuffer.rotate();
            mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.cloudFacesBuffer.getBlocking(), false, true);
            try {
                this.buildCloudCells(lv, mappedView.data(), p, q, bl, k);
                this.instanceCount = mappedView.data().position() / 3;
            } finally {
                if (mappedView != null) {
                    mappedView.close();
                }
            }
        }
        if (this.instanceCount == 0) {
            return;
        }
        mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.cloudInfoBuffer.getBlocking(), false, true);
        try {
            Std140Builder.intoBuffer(mappedView.data()).putVec4(ColorHelper.getRedFloat(color), ColorHelper.getGreenFloat(color), ColorHelper.getBlueFloat(color), 1.0f).putVec3(-r, h, -s).putVec3(12.0f, 4.0f, 12.0f);
        } finally {
            if (mappedView != null) {
                mappedView.close();
            }
        }
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector3f(), new Matrix4f(), 0.0f);
        Framebuffer lv2 = MinecraftClient.getInstance().getFramebuffer();
        Framebuffer lv3 = MinecraftClient.getInstance().worldRenderer.getCloudsFramebuffer();
        RenderSystem.ShapeIndexBuffer lv4 = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        GpuBuffer gpuBuffer = lv4.getIndexBuffer(6 * this.instanceCount);
        if (lv3 != null) {
            gpuTextureView = lv3.getColorAttachmentView();
            gpuTextureView2 = lv3.getDepthAttachmentView();
        } else {
            gpuTextureView = lv2.getColorAttachmentView();
            gpuTextureView2 = lv2.getDepthAttachmentView();
        }
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Clouds", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setIndexBuffer(gpuBuffer, lv4.getIndexType());
            renderPass.setUniform("CloudInfo", this.cloudInfoBuffer.getBlocking());
            renderPass.setUniform("CloudFaces", this.cloudFacesBuffer.getBlocking());
            renderPass.drawIndexed(0, 0, 6 * this.instanceCount, 1);
        }
    }

    private void buildCloudCells(ViewMode viewMode, ByteBuffer byteBuffer, int x, int z, boolean bl, int k) {
        if (this.cells == null) {
            return;
        }
        long[] ls = this.cells.cells;
        int l = this.cells.width;
        int m = this.cells.height;
        for (int n = 0; n <= 2 * k; ++n) {
            for (int o = -n; o <= n; ++o) {
                int p = n - Math.abs(o);
                if (p < 0 || p > k || o * o + p * p > k * k) continue;
                if (p != 0) {
                    this.method_72155(viewMode, byteBuffer, x, z, bl, o, l, -p, m, ls);
                }
                this.method_72155(viewMode, byteBuffer, x, z, bl, o, l, p, m, ls);
            }
        }
    }

    private void method_72155(ViewMode arg, ByteBuffer byteBuffer, int i, int j, boolean bl, int k, int l, int m, int n, long[] ls) {
        int p;
        int o = Math.floorMod(i + k, l);
        long q = ls[o + (p = Math.floorMod(j + m, n)) * l];
        if (q == 0L) {
            return;
        }
        if (bl) {
            this.buildCloudCellFancy(arg, byteBuffer, k, m, q);
        } else {
            this.buildCloudCellFast(byteBuffer, k, m);
        }
    }

    private void buildCloudCellFast(ByteBuffer byteBuffer, int color, int x) {
        this.method_71098(byteBuffer, color, x, Direction.DOWN, 32);
    }

    private void method_71098(ByteBuffer byteBuffer, int i, int j, Direction arg, int k) {
        int l = arg.getIndex() | k;
        l |= (i & 1) << 7;
        byteBuffer.put((byte)(i >> 1)).put((byte)(j >> 1)).put((byte)(l |= (j & 1) << 6));
    }

    private void buildCloudCellFancy(ViewMode viewMode, ByteBuffer byteBuffer, int i, int j, long l) {
        boolean bl;
        if (viewMode != ViewMode.BELOW_CLOUDS) {
            this.method_71098(byteBuffer, i, j, Direction.UP, 0);
        }
        if (viewMode != ViewMode.ABOVE_CLOUDS) {
            this.method_71098(byteBuffer, i, j, Direction.DOWN, 0);
        }
        if (CloudRenderer.hasBorderNorth(l) && j > 0) {
            this.method_71098(byteBuffer, i, j, Direction.NORTH, 0);
        }
        if (CloudRenderer.hasBorderSouth(l) && j < 0) {
            this.method_71098(byteBuffer, i, j, Direction.SOUTH, 0);
        }
        if (CloudRenderer.hasBorderWest(l) && i > 0) {
            this.method_71098(byteBuffer, i, j, Direction.WEST, 0);
        }
        if (CloudRenderer.hasBorderEast(l) && i < 0) {
            this.method_71098(byteBuffer, i, j, Direction.EAST, 0);
        }
        boolean bl2 = bl = Math.abs(i) <= 1 && Math.abs(j) <= 1;
        if (bl) {
            for (Direction lv : Direction.values()) {
                this.method_71098(byteBuffer, i, j, lv, 16);
            }
        }
    }

    public void scheduleTerrainUpdate() {
        this.rebuild = true;
    }

    public void rotate() {
        this.cloudInfoBuffer.rotate();
    }

    @Override
    public void close() {
        this.cloudInfoBuffer.close();
        if (this.cloudFacesBuffer != null) {
            this.cloudFacesBuffer.close();
        }
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager manager, Profiler profiler) {
        return this.prepare(manager, profiler);
    }

    @Environment(value=EnvType.CLIENT)
    static enum ViewMode {
        ABOVE_CLOUDS,
        INSIDE_CLOUDS,
        BELOW_CLOUDS;

    }

    @Environment(value=EnvType.CLIENT)
    public record CloudCells(long[] cells, int width, int height) {
    }
}

