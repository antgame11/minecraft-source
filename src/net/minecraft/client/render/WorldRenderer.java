/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.DynamicUniforms;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.SimpleFramebufferFactory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BlockRenderLayerGroup;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.ChunkBuilderMode;
import net.minecraft.client.render.ChunkRenderingDataPreparer;
import net.minecraft.client.render.CloudRenderer;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.FramePass;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.SectionRenderState;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.SubmittableBatch;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.render.WorldBorderRendering;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.chunk.AbstractChunkRenderData;
import net.minecraft.client.render.chunk.Buffers;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRenderData;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.client.render.chunk.NormalizedRelativePos;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.debug.GameTestDebugRenderer;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.state.BreakingBlockRenderState;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.Handle;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WorldRenderer
implements SynchronousResourceReloader,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier TRANSPARENCY = Identifier.ofVanilla("transparency");
    private static final Identifier ENTITY_OUTLINE = Identifier.ofVanilla("entity_outline");
    public static final int SECTION_SIZE = 16;
    public static final int HALF_SECTION_SIZE = 8;
    public static final int NEARBY_SECTION_DISTANCE = 32;
    private static final int MIN_TRANSPARENT_SORT_COUNT = 15;
    private final MinecraftClient client;
    private final EntityRenderManager entityRenderManager;
    private final BlockEntityRenderManager blockEntityRenderManager;
    private final BufferBuilderStorage bufferBuilders;
    private final SkyRendering skyRendering = new SkyRendering();
    private final CloudRenderer cloudRenderer = new CloudRenderer();
    private final WorldBorderRendering worldBorderRendering = new WorldBorderRendering();
    private final WeatherRendering weatherRendering = new WeatherRendering();
    private final SubmittableBatch particleBatch = new SubmittableBatch();
    public final DebugRenderer debugRenderer = new DebugRenderer();
    public final GameTestDebugRenderer gameTestDebugRenderer = new GameTestDebugRenderer();
    @Nullable
    private ClientWorld world;
    private final ChunkRenderingDataPreparer chunkRenderingDataPreparer = new ChunkRenderingDataPreparer();
    private final ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks = new ObjectArrayList(10000);
    private final ObjectArrayList<ChunkBuilder.BuiltChunk> nearbyChunks = new ObjectArrayList(50);
    @Nullable
    private BuiltChunkStorage chunks;
    private int ticks;
    private final Int2ObjectMap<BlockBreakingInfo> blockBreakingInfos = new Int2ObjectOpenHashMap<BlockBreakingInfo>();
    private final Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions = new Long2ObjectOpenHashMap<SortedSet<BlockBreakingInfo>>();
    @Nullable
    private Framebuffer entityOutlineFramebuffer;
    private final DefaultFramebufferSet framebufferSet = new DefaultFramebufferSet();
    private int cameraChunkX = Integer.MIN_VALUE;
    private int cameraChunkY = Integer.MIN_VALUE;
    private int cameraChunkZ = Integer.MIN_VALUE;
    private double lastCameraX = Double.MIN_VALUE;
    private double lastCameraY = Double.MIN_VALUE;
    private double lastCameraZ = Double.MIN_VALUE;
    private double lastCameraPitch = Double.MIN_VALUE;
    private double lastCameraYaw = Double.MIN_VALUE;
    @Nullable
    private ChunkBuilder chunkBuilder;
    private int viewDistance = -1;
    private boolean captureFrustum;
    @Nullable
    private Frustum capturedFrustum;
    @Nullable
    private BlockPos lastTranslucencySortCameraPos;
    private int chunkIndex;
    private final WorldRenderState worldRenderState;
    private final OrderedRenderCommandQueueImpl entityRenderCommandQueue;
    private final RenderDispatcher entityRenderDispatcher;

    public WorldRenderer(MinecraftClient client, EntityRenderManager entityRenderManager, BlockEntityRenderManager blockEntityRenderManager, BufferBuilderStorage bufferBuilders, WorldRenderState worldRenderState, RenderDispatcher entityRenderDispatcher) {
        this.client = client;
        this.entityRenderManager = entityRenderManager;
        this.blockEntityRenderManager = blockEntityRenderManager;
        this.bufferBuilders = bufferBuilders;
        this.entityRenderCommandQueue = entityRenderDispatcher.getQueue();
        this.worldRenderState = worldRenderState;
        this.entityRenderDispatcher = entityRenderDispatcher;
    }

    @Override
    public void close() {
        if (this.entityOutlineFramebuffer != null) {
            this.entityOutlineFramebuffer.delete();
        }
        this.skyRendering.close();
        this.cloudRenderer.close();
    }

    @Override
    public void reload(ResourceManager manager) {
        this.loadEntityOutlinePostProcessor();
        this.skyRendering.method_74924();
    }

    public void loadEntityOutlinePostProcessor() {
        if (this.entityOutlineFramebuffer != null) {
            this.entityOutlineFramebuffer.delete();
        }
        this.entityOutlineFramebuffer = new SimpleFramebuffer("Entity Outline", this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), true);
    }

    @Nullable
    private PostEffectProcessor getTransparencyPostEffectProcessor() {
        if (!MinecraftClient.isFabulousGraphicsOrBetter()) {
            return null;
        }
        PostEffectProcessor lv = this.client.getShaderLoader().loadPostEffect(TRANSPARENCY, DefaultFramebufferSet.STAGES);
        if (lv == null) {
            this.client.options.getGraphicsMode().setValue(GraphicsMode.FANCY);
            this.client.options.write();
        }
        return lv;
    }

    public void drawEntityOutlinesFramebuffer() {
        if (this.canDrawEntityOutlines()) {
            this.entityOutlineFramebuffer.drawBlit(this.client.getFramebuffer().getColorAttachmentView());
        }
    }

    protected boolean canDrawEntityOutlines() {
        return !this.client.gameRenderer.isRenderingPanorama() && this.entityOutlineFramebuffer != null && this.client.player != null;
    }

    public void setWorld(@Nullable ClientWorld world) {
        this.cameraChunkX = Integer.MIN_VALUE;
        this.cameraChunkY = Integer.MIN_VALUE;
        this.cameraChunkZ = Integer.MIN_VALUE;
        this.world = world;
        if (world != null) {
            this.reload();
        } else {
            this.entityRenderManager.clearCamera();
            if (this.chunks != null) {
                this.chunks.clear();
                this.chunks = null;
            }
            if (this.chunkBuilder != null) {
                this.chunkBuilder.stop();
            }
            this.chunkBuilder = null;
            this.chunkRenderingDataPreparer.setStorage(null);
            this.clear();
        }
        this.gameTestDebugRenderer.clear();
    }

    private void clear() {
        this.builtChunks.clear();
        this.nearbyChunks.clear();
    }

    public void reload() {
        if (this.world == null) {
            return;
        }
        this.world.reloadColor();
        if (this.chunkBuilder == null) {
            this.chunkBuilder = new ChunkBuilder(this.world, this, Util.getMainWorkerExecutor(), this.bufferBuilders, this.client.getBlockRenderManager(), this.client.getBlockEntityRenderDispatcher());
        } else {
            this.chunkBuilder.setWorld(this.world);
        }
        this.cloudRenderer.scheduleTerrainUpdate();
        RenderLayers.setFancyGraphicsOrBetter(MinecraftClient.isFancyGraphicsOrBetter());
        this.viewDistance = this.client.options.getClampedViewDistance();
        if (this.chunks != null) {
            this.chunks.clear();
        }
        this.chunkBuilder.cancelAllTasks();
        this.chunks = new BuiltChunkStorage(this.chunkBuilder, this.world, this.client.options.getClampedViewDistance(), this);
        this.chunkRenderingDataPreparer.setStorage(this.chunks);
        this.clear();
        Camera lv = this.client.gameRenderer.getCamera();
        this.chunks.updateCameraPosition(ChunkSectionPos.from(lv.getPos()));
    }

    public void onResized(int width, int height) {
        this.scheduleTerrainUpdate();
        if (this.entityOutlineFramebuffer != null) {
            this.entityOutlineFramebuffer.resize(width, height);
        }
    }

    @Nullable
    public String getChunksDebugString() {
        if (this.chunks == null) {
            return null;
        }
        int i = this.chunks.chunks.length;
        int j = this.getCompletedChunkCount();
        return String.format(Locale.ROOT, "C: %d/%d %sD: %d, %s", j, i, this.client.chunkCullingEnabled ? "(s) " : "", this.viewDistance, this.chunkBuilder == null ? "null" : this.chunkBuilder.getDebugString());
    }

    @Nullable
    public ChunkBuilder getChunkBuilder() {
        return this.chunkBuilder;
    }

    public double getChunkCount() {
        return this.chunks == null ? 0.0 : (double)this.chunks.chunks.length;
    }

    public double getViewDistance() {
        return this.viewDistance;
    }

    public int getCompletedChunkCount() {
        int i = 0;
        for (ChunkBuilder.BuiltChunk lv : this.builtChunks) {
            if (!lv.getCurrentRenderData().hasData()) continue;
            ++i;
        }
        return i;
    }

    @Nullable
    public String getEntitiesDebugString() {
        if (this.world == null) {
            return null;
        }
        return "E: " + this.worldRenderState.entityRenderStates.size() + "/" + this.world.getRegularEntityCount() + ", SD: " + this.world.getSimulationDistance();
    }

    private void method_74752(Camera arg, Frustum arg2, boolean bl) {
        Vec3d lv = arg.getPos();
        if (this.client.options.getClampedViewDistance() != this.viewDistance) {
            this.reload();
        }
        Profiler lv2 = Profilers.get();
        lv2.push("repositionCamera");
        int i = ChunkSectionPos.getSectionCoord(lv.getX());
        int j = ChunkSectionPos.getSectionCoord(lv.getY());
        int k = ChunkSectionPos.getSectionCoord(lv.getZ());
        if (this.cameraChunkX != i || this.cameraChunkY != j || this.cameraChunkZ != k) {
            this.cameraChunkX = i;
            this.cameraChunkY = j;
            this.cameraChunkZ = k;
            this.chunks.updateCameraPosition(ChunkSectionPos.from(lv));
            this.worldBorderRendering.markBuffersDirty();
        }
        this.chunkBuilder.setCameraPosition(lv);
        double d = Math.floor(lv.x / 8.0);
        double e = Math.floor(lv.y / 8.0);
        double f = Math.floor(lv.z / 8.0);
        if (d != this.lastCameraX || e != this.lastCameraY || f != this.lastCameraZ) {
            this.chunkRenderingDataPreparer.scheduleTerrainUpdate();
        }
        this.lastCameraX = d;
        this.lastCameraY = e;
        this.lastCameraZ = f;
        lv2.pop();
        if (this.capturedFrustum == null) {
            boolean bl2 = this.client.chunkCullingEnabled;
            if (bl && this.world.getBlockState(arg.getBlockPos()).isOpaqueFullCube()) {
                bl2 = false;
            }
            lv2.push("updateSOG");
            this.chunkRenderingDataPreparer.updateSectionOcclusionGraph(bl2, arg, arg2, this.builtChunks, this.world.getChunkManager().getActiveSections());
            lv2.pop();
            double g = Math.floor(arg.getPitch() / 2.0f);
            double h = Math.floor(arg.getYaw() / 2.0f);
            if (this.chunkRenderingDataPreparer.updateFrustum() || g != this.lastCameraPitch || h != this.lastCameraYaw) {
                lv2.push("applyFrustum");
                this.applyFrustum(WorldRenderer.offsetFrustum(arg2));
                lv2.pop();
                this.lastCameraPitch = g;
                this.lastCameraYaw = h;
            }
        }
    }

    public static Frustum offsetFrustum(Frustum frustum) {
        return new Frustum(frustum).coverBoxAroundSetPosition(8);
    }

    private void applyFrustum(Frustum frustum) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
        }
        this.clear();
        this.chunkRenderingDataPreparer.collectChunks(frustum, this.builtChunks, this.nearbyChunks);
    }

    public void addBuiltChunk(ChunkBuilder.BuiltChunk chunk) {
        this.chunkRenderingDataPreparer.schedulePropagationFrom(chunk);
    }

    private Frustum setupFrustum(Matrix4f posMatrix, Matrix4f projMatrix, Vec3d pos) {
        Frustum lv;
        if (this.capturedFrustum != null && !this.captureFrustum) {
            lv = this.capturedFrustum;
        } else {
            lv = new Frustum(posMatrix, projMatrix);
            lv.setPosition(pos.getX(), pos.getY(), pos.getZ());
        }
        if (this.captureFrustum) {
            this.capturedFrustum = lv;
            this.captureFrustum = false;
        }
        return lv;
    }

    public void render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f2, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky) {
        Optional<Integer> optional;
        float f = tickCounter.getTickProgress(false);
        this.worldRenderState.clear();
        this.blockEntityRenderManager.configure(camera);
        this.entityRenderManager.configure(camera, this.client.targetedEntity);
        final Profiler lv = Profilers.get();
        lv.push("populateLightUpdates");
        this.world.runQueuedChunkUpdates();
        lv.swap("runLightUpdates");
        this.world.getChunkManager().getLightingProvider().doLightUpdates();
        lv.swap("prepareCullFrustum");
        Vec3d lv2 = camera.getPos();
        Frustum lv3 = this.setupFrustum(positionMatrix, projectionMatrix, lv2);
        lv.swap("cullTerrain");
        this.method_74752(camera, lv3, this.client.player.isSpectator());
        lv.swap("compileSections");
        this.updateChunks(camera);
        lv.swap("extract");
        lv.push("entities");
        this.fillEntityRenderStates(camera, lv3, tickCounter, this.worldRenderState);
        lv.swap("blockEntities");
        this.fillBlockEntityRenderStates(camera, f, this.worldRenderState);
        lv.swap("blockOutline");
        this.fillEntityOutlineRenderStates(camera, this.worldRenderState);
        lv.swap("blockBreaking");
        this.fillBlockBreakingProgressRenderState(camera, this.worldRenderState);
        lv.swap("weather");
        this.weatherRendering.buildPrecipitationPieces(this.world, this.ticks, f, lv2, this.worldRenderState.weatherRenderState);
        lv.swap("sky");
        this.skyRendering.updateRenderState(this.world, f, lv2, this.worldRenderState.skyRenderState);
        lv.swap("border");
        this.worldBorderRendering.updateRenderState(this.world.getWorldBorder(), lv2, this.client.options.getClampedViewDistance() * 16, this.worldRenderState.worldBorderRenderState);
        lv.pop();
        lv.swap("setupFrameGraph");
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(positionMatrix);
        FrameGraphBuilder lv4 = new FrameGraphBuilder();
        this.framebufferSet.mainFramebuffer = lv4.createObjectNode("main", this.client.getFramebuffer());
        int i = this.client.getFramebuffer().textureWidth;
        int j = this.client.getFramebuffer().textureHeight;
        SimpleFramebufferFactory lv5 = new SimpleFramebufferFactory(i, j, true, 0);
        PostEffectProcessor lv6 = this.getTransparencyPostEffectProcessor();
        if (lv6 != null) {
            this.framebufferSet.translucentFramebuffer = lv4.createResourceHandle("translucent", lv5);
            this.framebufferSet.itemEntityFramebuffer = lv4.createResourceHandle("item_entity", lv5);
            this.framebufferSet.particlesFramebuffer = lv4.createResourceHandle("particles", lv5);
            this.framebufferSet.weatherFramebuffer = lv4.createResourceHandle("weather", lv5);
            this.framebufferSet.cloudsFramebuffer = lv4.createResourceHandle("clouds", lv5);
        }
        if (this.entityOutlineFramebuffer != null) {
            this.framebufferSet.entityOutlineFramebuffer = lv4.createObjectNode("entity_outline", this.entityOutlineFramebuffer);
        }
        FramePass lv7 = lv4.createPass("clear");
        this.framebufferSet.mainFramebuffer = lv7.transfer(this.framebufferSet.mainFramebuffer);
        lv7.setRenderer(() -> {
            Framebuffer lv = this.client.getFramebuffer();
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(lv.getColorAttachment(), ColorHelper.fromFloats(0.0f, vector4f.x, vector4f.y, vector4f.z), lv.getDepthAttachment(), 1.0);
        });
        if (renderSky) {
            this.renderSky(lv4, camera, fogBuffer);
        }
        this.renderMain(lv4, lv3, positionMatrix, fogBuffer, renderBlockOutline, this.worldRenderState, tickCounter, lv);
        PostEffectProcessor lv8 = this.client.getShaderLoader().loadPostEffect(ENTITY_OUTLINE, DefaultFramebufferSet.MAIN_AND_ENTITY_OUTLINE);
        if (this.worldRenderState.hasOutline && lv8 != null) {
            lv8.render(lv4, i, j, this.framebufferSet);
        }
        this.client.particleManager.addToBatch(this.particleBatch, new Frustum(lv3).offset(-3.0f), camera, f);
        this.renderParticles(lv4, fogBuffer);
        CloudRenderMode lv9 = this.client.options.getCloudRenderModeValue();
        if (lv9 != CloudRenderMode.OFF && (optional = this.world.getDimension().cloudHeight()).isPresent()) {
            float g = (float)this.ticks + f;
            int k = this.world.getCloudsColor(f);
            this.renderClouds(lv4, lv9, this.worldRenderState.cameraRenderState.pos, g, k, (float)optional.get().intValue() + 0.33f);
        }
        this.renderWeather(lv4, this.worldRenderState.cameraRenderState.pos, fogBuffer);
        if (lv6 != null) {
            lv6.render(lv4, i, j, this.framebufferSet);
        }
        this.renderLateDebug(lv4, this.worldRenderState.cameraRenderState.pos, fogBuffer, lv3);
        lv.swap("executeFrameGraph");
        lv4.run(allocator, new FrameGraphBuilder.Profiler(){

            @Override
            public void push(String location) {
                lv.push(location);
            }

            @Override
            public void pop(String location) {
                lv.pop();
            }
        });
        this.framebufferSet.clear();
        matrix4fStack.popMatrix();
        lv.pop();
    }

    private void renderMain(FrameGraphBuilder frameGraphBuilder, Frustum frustum, Matrix4f posMatrix, GpuBufferSlice fogBuffer, boolean renderBlockOutline, WorldRenderState state, RenderTickCounter tickCounter, Profiler profiler) {
        FramePass lv = frameGraphBuilder.createPass("main");
        this.framebufferSet.mainFramebuffer = lv.transfer(this.framebufferSet.mainFramebuffer);
        if (this.framebufferSet.translucentFramebuffer != null) {
            this.framebufferSet.translucentFramebuffer = lv.transfer(this.framebufferSet.translucentFramebuffer);
        }
        if (this.framebufferSet.itemEntityFramebuffer != null) {
            this.framebufferSet.itemEntityFramebuffer = lv.transfer(this.framebufferSet.itemEntityFramebuffer);
        }
        if (this.framebufferSet.weatherFramebuffer != null) {
            this.framebufferSet.weatherFramebuffer = lv.transfer(this.framebufferSet.weatherFramebuffer);
        }
        if (state.hasOutline && this.framebufferSet.entityOutlineFramebuffer != null) {
            this.framebufferSet.entityOutlineFramebuffer = lv.transfer(this.framebufferSet.entityOutlineFramebuffer);
        }
        Handle<Framebuffer> lv2 = this.framebufferSet.mainFramebuffer;
        Handle<Framebuffer> lv3 = this.framebufferSet.translucentFramebuffer;
        Handle<Framebuffer> lv4 = this.framebufferSet.itemEntityFramebuffer;
        Handle<Framebuffer> lv5 = this.framebufferSet.entityOutlineFramebuffer;
        lv.setRenderer(() -> {
            RenderSystem.setShaderFog(fogBuffer);
            Vec3d lv = arg.cameraRenderState.pos;
            double d = lv.getX();
            double e = lv.getY();
            double f = lv.getZ();
            profiler.push("terrain");
            SectionRenderState lv2 = this.renderBlockLayers(posMatrix, d, e, f);
            lv2.renderSection(BlockRenderLayerGroup.OPAQUE);
            this.client.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.LEVEL);
            if (lv4 != null) {
                ((Framebuffer)lv4.get()).copyDepthFrom(this.client.getFramebuffer());
            }
            if (this.canDrawEntityOutlines() && lv5 != null) {
                Framebuffer lv3 = (Framebuffer)lv5.get();
                RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(lv3.getColorAttachment(), 0, lv3.getDepthAttachment(), 1.0);
            }
            MatrixStack lv4 = new MatrixStack();
            VertexConsumerProvider.Immediate lv5 = this.bufferBuilders.getEntityVertexConsumers();
            VertexConsumerProvider.Immediate lv6 = this.bufferBuilders.getEffectVertexConsumers();
            profiler.swap("submitEntities");
            this.pushEntityRenders(lv4, state, this.entityRenderCommandQueue);
            profiler.swap("submitBlockEntities");
            this.renderBlockEntities(lv4, state, this.entityRenderCommandQueue);
            profiler.swap("renderFeatures");
            this.entityRenderDispatcher.render();
            lv5.drawCurrentLayer();
            this.checkEmpty(lv4);
            lv5.draw(RenderLayer.getSolid());
            lv5.draw(RenderLayer.getEndPortal());
            lv5.draw(RenderLayer.getEndGateway());
            lv5.draw(TexturedRenderLayers.getEntitySolid());
            lv5.draw(TexturedRenderLayers.getEntityCutout());
            lv5.draw(TexturedRenderLayers.getBeds());
            lv5.draw(TexturedRenderLayers.getShulkerBoxes());
            lv5.draw(TexturedRenderLayers.getSign());
            lv5.draw(TexturedRenderLayers.getHangingSign());
            lv5.draw(TexturedRenderLayers.getChest());
            this.bufferBuilders.getOutlineVertexConsumers().draw();
            if (renderBlockOutline) {
                this.renderTargetBlockOutline(lv5, lv4, false, state);
            }
            profiler.swap("debug");
            this.debugRenderer.render(lv4, frustum, lv5, d, e, f, false);
            lv5.drawCurrentLayer();
            profiler.pop();
            this.gameTestDebugRenderer.renderMarkers(lv4, lv5);
            lv5.drawCurrentLayer();
            this.checkEmpty(lv4);
            lv5.draw(TexturedRenderLayers.getItemEntityTranslucentCull());
            lv5.draw(TexturedRenderLayers.getBannerPatterns());
            lv5.draw(TexturedRenderLayers.getShieldPatterns());
            lv5.draw(RenderLayer.getArmorEntityGlint());
            lv5.draw(RenderLayer.getGlint());
            lv5.draw(RenderLayer.getGlintTranslucent());
            lv5.draw(RenderLayer.getEntityGlint());
            profiler.push("destroyProgress");
            this.renderBlockDamage(lv4, lv6, state);
            lv6.draw();
            profiler.pop();
            this.checkEmpty(lv4);
            lv5.draw(RenderLayer.getWaterMask());
            lv5.draw();
            if (lv3 != null) {
                ((Framebuffer)lv3.get()).copyDepthFrom((Framebuffer)lv2.get());
            }
            profiler.push("translucent");
            lv2.renderSection(BlockRenderLayerGroup.TRANSLUCENT);
            profiler.swap("string");
            lv2.renderSection(BlockRenderLayerGroup.TRIPWIRE);
            if (renderBlockOutline) {
                this.renderTargetBlockOutline(lv5, lv4, true, state);
            }
            lv5.draw();
            profiler.pop();
        });
    }

    private void renderParticles(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice fogBuffer) {
        FramePass lv = frameGraphBuilder.createPass("particles");
        if (this.framebufferSet.particlesFramebuffer != null) {
            this.framebufferSet.particlesFramebuffer = lv.transfer(this.framebufferSet.particlesFramebuffer);
            lv.dependsOn(this.framebufferSet.mainFramebuffer);
        } else {
            this.framebufferSet.mainFramebuffer = lv.transfer(this.framebufferSet.mainFramebuffer);
        }
        Handle<Framebuffer> lv2 = this.framebufferSet.mainFramebuffer;
        Handle<Framebuffer> lv3 = this.framebufferSet.particlesFramebuffer;
        lv.setRenderer(() -> {
            RenderSystem.setShaderFog(fogBuffer);
            if (lv3 != null) {
                ((Framebuffer)lv3.get()).copyDepthFrom((Framebuffer)lv2.get());
            }
            this.particleBatch.submit(this.entityRenderCommandQueue, this.worldRenderState.cameraRenderState);
            this.entityRenderDispatcher.render();
            this.particleBatch.onFrameEnd();
        });
    }

    private void renderClouds(FrameGraphBuilder frameGraphBuilder, CloudRenderMode mode, Vec3d cameraPos, float cloudPhase, int color, float cloudHeight) {
        FramePass lv = frameGraphBuilder.createPass("clouds");
        if (this.framebufferSet.cloudsFramebuffer != null) {
            this.framebufferSet.cloudsFramebuffer = lv.transfer(this.framebufferSet.cloudsFramebuffer);
        } else {
            this.framebufferSet.mainFramebuffer = lv.transfer(this.framebufferSet.mainFramebuffer);
        }
        lv.setRenderer(() -> this.cloudRenderer.renderClouds(color, mode, cloudHeight, cameraPos, cloudPhase));
    }

    private void renderWeather(FrameGraphBuilder frameGraphBuilder, Vec3d cameraPos, GpuBufferSlice fogBuffer) {
        int i = this.client.options.getClampedViewDistance() * 16;
        float f = this.client.gameRenderer.getFarPlaneDistance();
        FramePass lv = frameGraphBuilder.createPass("weather");
        if (this.framebufferSet.weatherFramebuffer != null) {
            this.framebufferSet.weatherFramebuffer = lv.transfer(this.framebufferSet.weatherFramebuffer);
        } else {
            this.framebufferSet.mainFramebuffer = lv.transfer(this.framebufferSet.mainFramebuffer);
        }
        lv.setRenderer(() -> {
            RenderSystem.setShaderFog(fogBuffer);
            VertexConsumerProvider.Immediate lv = this.bufferBuilders.getEntityVertexConsumers();
            this.weatherRendering.renderPrecipitation(lv, cameraPos, this.worldRenderState.weatherRenderState);
            this.worldBorderRendering.render(this.worldRenderState.worldBorderRenderState, cameraPos, i, f);
            lv.draw();
        });
    }

    private void renderLateDebug(FrameGraphBuilder frameGraphBuilder, Vec3d pos, GpuBufferSlice fogBuffer, Frustum frustum) {
        FramePass lv = frameGraphBuilder.createPass("late_debug");
        this.framebufferSet.mainFramebuffer = lv.transfer(this.framebufferSet.mainFramebuffer);
        if (this.framebufferSet.itemEntityFramebuffer != null) {
            this.framebufferSet.itemEntityFramebuffer = lv.transfer(this.framebufferSet.itemEntityFramebuffer);
        }
        Handle<Framebuffer> lv2 = this.framebufferSet.mainFramebuffer;
        lv.setRenderer(() -> {
            RenderSystem.setShaderFog(fogBuffer);
            MatrixStack lv = new MatrixStack();
            VertexConsumerProvider.Immediate lv2 = this.bufferBuilders.getEntityVertexConsumers();
            RenderSystem.outputColorTextureOverride = ((Framebuffer)lv2.get()).getColorAttachmentView();
            RenderSystem.outputDepthTextureOverride = ((Framebuffer)lv2.get()).getDepthAttachmentView();
            this.debugRenderer.render(lv, frustum, lv2, arg.x, arg.y, arg.z, true);
            lv2.drawCurrentLayer();
            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
            this.checkEmpty(lv);
        });
    }

    private void fillEntityRenderStates(Camera camera, Frustum frustum, RenderTickCounter tickCounter, WorldRenderState renderStates) {
        Vec3d lv = camera.getPos();
        double d = lv.getX();
        double e = lv.getY();
        double f = lv.getZ();
        TickManager lv2 = this.client.world.getTickManager();
        boolean bl = this.canDrawEntityOutlines();
        Entity.setRenderDistanceMultiplier(MathHelper.clamp((double)this.client.options.getClampedViewDistance() / 8.0, 1.0, 2.5) * this.client.options.getEntityDistanceScaling().getValue());
        for (Entity lv3 : this.world.getEntities()) {
            BlockPos lv4;
            if (!this.entityRenderManager.shouldRender(lv3, frustum, d, e, f) && !lv3.hasPassengerDeep(this.client.player) || !this.world.isOutOfHeightLimit((lv4 = lv3.getBlockPos()).getY()) && !this.isRenderingReady(lv4) || lv3 == camera.getFocusedEntity() && !camera.isThirdPerson() && (!(camera.getFocusedEntity() instanceof LivingEntity) || !((LivingEntity)camera.getFocusedEntity()).isSleeping()) || lv3 instanceof ClientPlayerEntity && camera.getFocusedEntity() != lv3) continue;
            if (lv3.age == 0) {
                lv3.lastRenderX = lv3.getX();
                lv3.lastRenderY = lv3.getY();
                lv3.lastRenderZ = lv3.getZ();
            }
            float g = tickCounter.getTickProgress(!lv2.shouldSkipTick(lv3));
            EntityRenderState lv5 = this.getAndUpdateRenderState(lv3, g);
            renderStates.entityRenderStates.add(lv5);
            if (!lv5.hasOutline() || !bl) continue;
            renderStates.hasOutline = true;
        }
    }

    private void pushEntityRenders(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueue queue) {
        Vec3d lv = renderStates.cameraRenderState.pos;
        double d = lv.getX();
        double e = lv.getY();
        double f = lv.getZ();
        for (EntityRenderState lv2 : renderStates.entityRenderStates) {
            if (!renderStates.hasOutline) {
                lv2.outlineColor = 0;
            }
            this.entityRenderManager.render(lv2, renderStates.cameraRenderState, lv2.x - d, lv2.y - e, lv2.z - f, matrices, queue);
        }
    }

    private void fillBlockEntityRenderStates(Camera camera, float tickProgress, WorldRenderState renderStates) {
        Vec3d lv = camera.getPos();
        double d = lv.getX();
        double e = lv.getY();
        double g = lv.getZ();
        MatrixStack lv2 = new MatrixStack();
        for (ChunkBuilder.BuiltChunk lv3 : this.builtChunks) {
            List<BlockEntity> list = lv3.getCurrentRenderData().getBlockEntities();
            if (list.isEmpty()) continue;
            for (BlockEntity lv4 : list) {
                Object lv7;
                ModelCommandRenderer.CrumblingOverlayCommand lv6;
                BlockPos lv5 = lv4.getPos();
                SortedSet sortedSet = (SortedSet)this.blockBreakingProgressions.get(lv5.asLong());
                if (sortedSet == null || sortedSet.isEmpty()) {
                    lv6 = null;
                } else {
                    lv2.push();
                    lv2.translate((double)lv5.getX() - d, (double)lv5.getY() - e, (double)lv5.getZ() - g);
                    lv6 = new ModelCommandRenderer.CrumblingOverlayCommand(((BlockBreakingInfo)sortedSet.last()).getStage(), lv2.peek());
                    lv2.pop();
                }
                if ((lv7 = this.blockEntityRenderManager.getRenderState(lv4, tickProgress, lv6)) == null) continue;
                renderStates.blockEntityRenderStates.add((BlockEntityRenderState)lv7);
            }
        }
        Iterator<BlockEntity> iterator = this.world.getBlockEntities().iterator();
        while (iterator.hasNext()) {
            BlockEntity lv8 = iterator.next();
            if (lv8.isRemoved()) {
                iterator.remove();
                continue;
            }
            Object lv9 = this.blockEntityRenderManager.getRenderState(lv8, tickProgress, null);
            if (lv9 == null) continue;
            renderStates.blockEntityRenderStates.add((BlockEntityRenderState)lv9);
        }
    }

    private void renderBlockEntities(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueueImpl queue) {
        Vec3d lv = renderStates.cameraRenderState.pos;
        double d = lv.getX();
        double e = lv.getY();
        double f = lv.getZ();
        for (BlockEntityRenderState lv2 : renderStates.blockEntityRenderStates) {
            BlockPos lv3 = lv2.pos;
            matrices.push();
            matrices.translate((double)lv3.getX() - d, (double)lv3.getY() - e, (double)lv3.getZ() - f);
            this.blockEntityRenderManager.render(lv2, matrices, queue, renderStates.cameraRenderState);
            matrices.pop();
        }
    }

    private void fillBlockBreakingProgressRenderState(Camera camera, WorldRenderState renderStates) {
        Vec3d lv = camera.getPos();
        double d = lv.getX();
        double e = lv.getY();
        double f = lv.getZ();
        renderStates.breakingBlockRenderStates.clear();
        for (Long2ObjectMap.Entry entry : this.blockBreakingProgressions.long2ObjectEntrySet()) {
            SortedSet sortedSet;
            BlockPos lv2 = BlockPos.fromLong(entry.getLongKey());
            if (lv2.getSquaredDistanceFromCenter(d, e, f) > 1024.0 || (sortedSet = (SortedSet)entry.getValue()) == null || sortedSet.isEmpty()) continue;
            int i = ((BlockBreakingInfo)sortedSet.last()).getStage();
            renderStates.breakingBlockRenderStates.add(new BreakingBlockRenderState(this.world, lv2, i));
        }
    }

    private void renderBlockDamage(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, WorldRenderState renderStates) {
        Vec3d lv = renderStates.cameraRenderState.pos;
        double d = lv.getX();
        double e = lv.getY();
        double f = lv.getZ();
        for (BreakingBlockRenderState lv2 : renderStates.breakingBlockRenderStates) {
            matrices.push();
            BlockPos lv3 = lv2.entityBlockPos;
            matrices.translate((double)lv3.getX() - d, (double)lv3.getY() - e, (double)lv3.getZ() - f);
            MatrixStack.Entry lv4 = matrices.peek();
            OverlayVertexConsumer lv5 = new OverlayVertexConsumer(immediate.getBuffer(ModelBaker.BLOCK_DESTRUCTION_RENDER_LAYERS.get(lv2.breakProgress)), lv4, 1.0f);
            this.client.getBlockRenderManager().renderDamage(lv2.blockState, lv3, lv2, matrices, lv5);
            matrices.pop();
        }
    }

    private void fillEntityOutlineRenderStates(Camera camera, WorldRenderState renderStates) {
        renderStates.outlineRenderState = null;
        HitResult hitResult = this.client.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult lv = (BlockHitResult)hitResult;
        if (lv.getType() == HitResult.Type.MISS) {
            return;
        }
        BlockPos lv2 = lv.getBlockPos();
        BlockState lv3 = this.world.getBlockState(lv2);
        if (!lv3.isAir() && this.world.getWorldBorder().contains(lv2)) {
            boolean bl = RenderLayers.getBlockLayer(lv3).isTranslucent();
            boolean bl2 = this.client.options.getHighContrastBlockOutline().getValue();
            ShapeContext lv4 = ShapeContext.of(camera.getFocusedEntity());
            VoxelShape lv5 = lv3.getOutlineShape(this.world, lv2, lv4);
            if (SharedConstants.SHAPES) {
                VoxelShape lv6 = lv3.getCollisionShape(this.world, lv2, lv4);
                VoxelShape lv7 = lv3.getCullingShape();
                VoxelShape lv8 = lv3.getRaycastShape(this.world, lv2);
                renderStates.outlineRenderState = new OutlineRenderState(lv2, bl, bl2, lv5, lv6, lv7, lv8);
            } else {
                renderStates.outlineRenderState = new OutlineRenderState(lv2, bl, bl2, lv5);
            }
        }
    }

    private void renderTargetBlockOutline(VertexConsumerProvider.Immediate immediate, MatrixStack matrices, boolean renderBlockOutline, WorldRenderState renderStates) {
        VertexConsumer lv3;
        OutlineRenderState lv = renderStates.outlineRenderState;
        if (lv == null) {
            return;
        }
        if (lv.isTranslucent() != renderBlockOutline) {
            return;
        }
        Vec3d lv2 = renderStates.cameraRenderState.pos;
        if (lv.highContrast()) {
            lv3 = immediate.getBuffer(RenderLayer.getSecondaryBlockOutline());
            this.drawBlockOutline(matrices, lv3, lv2.x, lv2.y, lv2.z, lv, -16777216);
        }
        lv3 = immediate.getBuffer(RenderLayer.getLines());
        int i = lv.highContrast() ? Colors.CYAN : ColorHelper.withAlpha(102, Colors.BLACK);
        this.drawBlockOutline(matrices, lv3, lv2.x, lv2.y, lv2.z, lv, i);
        immediate.drawCurrentLayer();
    }

    private void checkEmpty(MatrixStack matrices) {
        if (!matrices.isEmpty()) {
            throw new IllegalStateException("Pose stack not empty");
        }
    }

    private EntityRenderState getAndUpdateRenderState(Entity entity, float tickProgress) {
        return this.entityRenderManager.getAndUpdateRenderState(entity, tickProgress);
    }

    private void translucencySort(Vec3d cameraPos) {
        if (this.builtChunks.isEmpty()) {
            return;
        }
        BlockPos lv = BlockPos.ofFloored(cameraPos);
        boolean bl = !lv.equals(this.lastTranslucencySortCameraPos);
        NormalizedRelativePos lv2 = new NormalizedRelativePos();
        for (ChunkBuilder.BuiltChunk lv3 : this.nearbyChunks) {
            this.scheduleChunkTranslucencySort(lv3, lv2, cameraPos, bl, true);
        }
        this.chunkIndex %= this.builtChunks.size();
        int i = Math.max(this.builtChunks.size() / 8, 15);
        while (i-- > 0) {
            int j = this.chunkIndex++ % this.builtChunks.size();
            this.scheduleChunkTranslucencySort(this.builtChunks.get(j), lv2, cameraPos, bl, false);
        }
        this.lastTranslucencySortCameraPos = lv;
    }

    private void scheduleChunkTranslucencySort(ChunkBuilder.BuiltChunk chunk, NormalizedRelativePos relativePos, Vec3d cameraPos, boolean needsUpdate, boolean ignoreCameraAlignment) {
        boolean bl4;
        relativePos.with(cameraPos, chunk.getSectionPos());
        boolean bl3 = chunk.getCurrentRenderData().hasPosition(relativePos);
        boolean bl = bl4 = needsUpdate && (relativePos.isOnCameraAxis() || ignoreCameraAlignment);
        if ((bl4 || bl3) && !chunk.isCurrentlySorting() && chunk.hasTranslucentLayer()) {
            chunk.scheduleSort(this.chunkBuilder);
        }
    }

    private SectionRenderState renderBlockLayers(Matrix4fc matrix, double cameraX, double cameraY, double cameraZ) {
        ListIterator objectListIterator = this.builtChunks.listIterator(0);
        EnumMap<BlockRenderLayer, List<RenderPass.RenderObject<GpuBufferSlice[]>>> enumMap = new EnumMap<BlockRenderLayer, List<RenderPass.RenderObject<GpuBufferSlice[]>>>(BlockRenderLayer.class);
        int i = 0;
        for (BlockRenderLayer lv : BlockRenderLayer.values()) {
            enumMap.put(lv, new ArrayList());
        }
        ArrayList<DynamicUniforms.UniformValue> list = new ArrayList<DynamicUniforms.UniformValue>();
        Vector4f vector4f = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        Matrix4f matrix4f = new Matrix4f();
        while (objectListIterator.hasNext()) {
            ChunkBuilder.BuiltChunk lv2 = (ChunkBuilder.BuiltChunk)objectListIterator.next();
            AbstractChunkRenderData lv3 = lv2.getCurrentRenderData();
            for (BlockRenderLayer lv4 : BlockRenderLayer.values()) {
                VertexFormat.IndexType lv6;
                GpuBuffer gpuBuffer;
                Buffers lv5 = lv3.getBuffersForLayer(lv4);
                if (lv5 == null) continue;
                if (lv5.getIndexBuffer() == null) {
                    if (lv5.getIndexCount() > i) {
                        i = lv5.getIndexCount();
                    }
                    gpuBuffer = null;
                    lv6 = null;
                } else {
                    gpuBuffer = lv5.getIndexBuffer();
                    lv6 = lv5.getIndexType();
                }
                BlockPos lv7 = lv2.getOrigin();
                int j = list.size();
                list.add(new DynamicUniforms.UniformValue(matrix, vector4f, new Vector3f((float)((double)lv7.getX() - cameraX), (float)((double)lv7.getY() - cameraY), (float)((double)lv7.getZ() - cameraZ)), matrix4f, 1.0f));
                enumMap.get((Object)lv4).add(new RenderPass.RenderObject<GpuBufferSlice[]>(0, lv5.getVertexBuffer(), gpuBuffer, lv6, 0, lv5.getIndexCount(), (gpuBufferSlices, arg) -> arg.upload("DynamicTransforms", gpuBufferSlices[j])));
            }
        }
        GpuBufferSlice[] gpuBufferSlices2 = RenderSystem.getDynamicUniforms().writeAll(list.toArray(new DynamicUniforms.UniformValue[0]));
        return new SectionRenderState(enumMap, i, gpuBufferSlices2);
    }

    public void rotate() {
        this.cloudRenderer.rotate();
    }

    public void captureFrustum() {
        this.captureFrustum = true;
    }

    public void killFrustum() {
        this.capturedFrustum = null;
    }

    public void tick(Camera camera) {
        if (this.world.getTickManager().shouldTick()) {
            ++this.ticks;
        }
        this.weatherRendering.addParticlesAndSound(this.world, camera, this.ticks, this.client.options.getParticles().getValue());
        this.updateBlockBreakingProgress();
    }

    private void updateBlockBreakingProgress() {
        if (this.ticks % 20 != 0) {
            return;
        }
        Iterator iterator = this.blockBreakingInfos.values().iterator();
        while (iterator.hasNext()) {
            BlockBreakingInfo lv = (BlockBreakingInfo)iterator.next();
            int i = lv.getLastUpdateTick();
            if (this.ticks - i <= 400) continue;
            iterator.remove();
            this.removeBlockBreakingInfo(lv);
        }
    }

    private void removeBlockBreakingInfo(BlockBreakingInfo info) {
        long l = info.getPos().asLong();
        Set set = (Set)this.blockBreakingProgressions.get(l);
        set.remove(info);
        if (set.isEmpty()) {
            this.blockBreakingProgressions.remove(l);
        }
    }

    private void renderSky(FrameGraphBuilder frameGraphBuilder, Camera camera, GpuBufferSlice fogBuffer) {
        CameraSubmersionType lv = camera.getSubmersionType();
        if (lv == CameraSubmersionType.POWDER_SNOW || lv == CameraSubmersionType.LAVA || this.hasBlindnessOrDarkness(camera)) {
            return;
        }
        SkyRenderState lv2 = this.worldRenderState.skyRenderState;
        if (lv2.skyType == DimensionEffects.SkyType.NONE) {
            return;
        }
        FramePass lv3 = frameGraphBuilder.createPass("sky");
        this.framebufferSet.mainFramebuffer = lv3.transfer(this.framebufferSet.mainFramebuffer);
        lv3.setRenderer(() -> {
            RenderSystem.setShaderFog(fogBuffer);
            if (arg.skyType == DimensionEffects.SkyType.END) {
                this.skyRendering.renderEndSky();
                if (arg.endFlashIntensity > 1.0E-5f) {
                    MatrixStack lv = new MatrixStack();
                    this.skyRendering.drawEndLightFlash(lv, arg.endFlashIntensity, arg.endFlashPitch, arg.endFlashYaw);
                }
                return;
            }
            MatrixStack lv = new MatrixStack();
            float f = ColorHelper.getRedFloat(arg.skyColor);
            float g = ColorHelper.getGreenFloat(arg.skyColor);
            float h = ColorHelper.getBlueFloat(arg.skyColor);
            this.skyRendering.renderTopSky(f, g, h);
            if (arg.isSunTransition) {
                this.skyRendering.renderGlowingSky(lv, arg.solarAngle, arg.sunriseAndSunsetColor);
            }
            this.skyRendering.renderCelestialBodies(lv, arg.time, arg.moonPhase, arg.rainGradient, arg.starBrightness);
            if (arg.shouldRenderSkyDark) {
                this.skyRendering.renderSkyDark();
            }
        });
    }

    private boolean hasBlindnessOrDarkness(Camera camera) {
        Entity entity = camera.getFocusedEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            return lv.hasStatusEffect(StatusEffects.BLINDNESS) || lv.hasStatusEffect(StatusEffects.DARKNESS);
        }
        return false;
    }

    private void updateChunks(Camera camera) {
        Profiler lv = Profilers.get();
        lv.push("populateSectionsToCompile");
        ChunkRendererRegionBuilder lv2 = new ChunkRendererRegionBuilder();
        BlockPos lv3 = camera.getBlockPos();
        ArrayList<ChunkBuilder.BuiltChunk> list = Lists.newArrayList();
        for (ChunkBuilder.BuiltChunk lv4 : this.builtChunks) {
            if (!lv4.needsRebuild() || lv4.getCurrentRenderData() == ChunkRenderData.HIDDEN && !lv4.shouldBuild()) continue;
            boolean bl = false;
            if (this.client.options.getChunkBuilderMode().getValue() == ChunkBuilderMode.NEARBY) {
                BlockPos lv5 = ChunkSectionPos.from(lv4.getSectionPos()).getCenterPos();
                bl = lv5.getSquaredDistance(lv3) < 768.0 || lv4.needsImportantRebuild();
            } else if (this.client.options.getChunkBuilderMode().getValue() == ChunkBuilderMode.PLAYER_AFFECTED) {
                bl = lv4.needsImportantRebuild();
            }
            if (bl) {
                lv.push("compileSectionSynchronously");
                this.chunkBuilder.rebuild(lv4, lv2);
                lv4.cancelRebuild();
                lv.pop();
                continue;
            }
            list.add(lv4);
        }
        lv.swap("uploadSectionMeshes");
        this.chunkBuilder.upload();
        lv.swap("scheduleAsyncCompile");
        for (ChunkBuilder.BuiltChunk lv4 : list) {
            lv4.scheduleRebuild(lv2);
            lv4.cancelRebuild();
        }
        lv.swap("scheduleTranslucentResort");
        this.translucencySort(camera.getPos());
        lv.pop();
    }

    private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, double x, double y, double z, OutlineRenderState state, int i) {
        BlockPos lv = state.pos();
        if (SharedConstants.SHAPES) {
            VertexRendering.drawOutline(matrices, vertexConsumer, state.shape(), (double)lv.getX() - x, (double)lv.getY() - y, (double)lv.getZ() - z, ColorHelper.fromFloats(1.0f, 1.0f, 1.0f, 1.0f));
            if (state.collisionShape() != null) {
                VertexRendering.drawOutline(matrices, vertexConsumer, state.collisionShape(), (double)lv.getX() - x, (double)lv.getY() - y, (double)lv.getZ() - z, ColorHelper.fromFloats(0.4f, 0.0f, 0.0f, 0.0f));
            }
            if (state.occlusionShape() != null) {
                VertexRendering.drawOutline(matrices, vertexConsumer, state.occlusionShape(), (double)lv.getX() - x, (double)lv.getY() - y, (double)lv.getZ() - z, ColorHelper.fromFloats(0.4f, 0.0f, 1.0f, 0.0f));
            }
            if (state.interactionShape() != null) {
                VertexRendering.drawOutline(matrices, vertexConsumer, state.interactionShape(), (double)lv.getX() - x, (double)lv.getY() - y, (double)lv.getZ() - z, ColorHelper.fromFloats(0.4f, 0.0f, 0.0f, 1.0f));
            }
        } else {
            VertexRendering.drawOutline(matrices, vertexConsumer, state.shape(), (double)lv.getX() - x, (double)lv.getY() - y, (double)lv.getZ() - z, i);
        }
    }

    public void updateBlock(BlockView world, BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        this.scheduleSectionRender(pos, (flags & 8) != 0);
    }

    private void scheduleSectionRender(BlockPos pos, boolean important) {
        for (int i = pos.getZ() - 1; i <= pos.getZ() + 1; ++i) {
            for (int j = pos.getX() - 1; j <= pos.getX() + 1; ++j) {
                for (int k = pos.getY() - 1; k <= pos.getY() + 1; ++k) {
                    this.scheduleChunkRender(ChunkSectionPos.getSectionCoord(j), ChunkSectionPos.getSectionCoord(k), ChunkSectionPos.getSectionCoord(i), important);
                }
            }
        }
    }

    public void scheduleBlockRenders(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int o = minZ - 1; o <= maxZ + 1; ++o) {
            for (int p = minX - 1; p <= maxX + 1; ++p) {
                for (int q = minY - 1; q <= maxY + 1; ++q) {
                    this.scheduleChunkRender(ChunkSectionPos.getSectionCoord(p), ChunkSectionPos.getSectionCoord(q), ChunkSectionPos.getSectionCoord(o));
                }
            }
        }
    }

    public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
        if (this.client.getBakedModelManager().shouldRerender(old, updated)) {
            this.scheduleBlockRenders(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public void scheduleChunkRenders3x3x3(int x, int y, int z) {
        this.scheduleChunkRenders(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    public void scheduleChunkRenders(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int o = minZ; o <= maxZ; ++o) {
            for (int p = minX; p <= maxX; ++p) {
                for (int q = minY; q <= maxY; ++q) {
                    this.scheduleChunkRender(p, q, o);
                }
            }
        }
    }

    public void scheduleChunkRender(int chunkX, int chunkY, int chunkZ) {
        this.scheduleChunkRender(chunkX, chunkY, chunkZ, false);
    }

    private void scheduleChunkRender(int x, int y, int z, boolean important) {
        this.chunks.scheduleRebuild(x, y, z, important);
    }

    public void onChunkUnload(long sectionPos) {
        ChunkBuilder.BuiltChunk lv = this.chunks.getRenderedChunk(sectionPos);
        if (lv != null) {
            this.chunkRenderingDataPreparer.schedulePropagationFrom(lv);
        }
    }

    public void setBlockBreakingInfo(int entityId, BlockPos pos, int stage) {
        if (stage < 0 || stage >= 10) {
            BlockBreakingInfo lv = (BlockBreakingInfo)this.blockBreakingInfos.remove(entityId);
            if (lv != null) {
                this.removeBlockBreakingInfo(lv);
            }
        } else {
            BlockBreakingInfo lv = (BlockBreakingInfo)this.blockBreakingInfos.get(entityId);
            if (lv != null) {
                this.removeBlockBreakingInfo(lv);
            }
            if (lv == null || lv.getPos().getX() != pos.getX() || lv.getPos().getY() != pos.getY() || lv.getPos().getZ() != pos.getZ()) {
                lv = new BlockBreakingInfo(entityId, pos);
                this.blockBreakingInfos.put(entityId, lv);
            }
            lv.setStage(stage);
            lv.setLastUpdateTick(this.ticks);
            this.blockBreakingProgressions.computeIfAbsent(lv.getPos().asLong(), l -> Sets.newTreeSet()).add(lv);
        }
    }

    public boolean isTerrainRenderComplete() {
        return this.chunkBuilder.isEmpty();
    }

    public void scheduleNeighborUpdates(ChunkPos chunkPos) {
        this.chunkRenderingDataPreparer.addNeighbors(chunkPos);
    }

    public void scheduleTerrainUpdate() {
        this.chunkRenderingDataPreparer.scheduleTerrainUpdate();
        this.cloudRenderer.scheduleTerrainUpdate();
    }

    public static int getLightmapCoordinates(BlockRenderView world, BlockPos pos) {
        return WorldRenderer.getLightmapCoordinates(BrightnessGetter.DEFAULT, world, world.getBlockState(pos), pos);
    }

    public static int getLightmapCoordinates(BrightnessGetter brightnessGetter, BlockRenderView world, BlockState state, BlockPos pos) {
        int k;
        if (state.hasEmissiveLighting(world, pos)) {
            return 0xF000F0;
        }
        int i = brightnessGetter.packedBrightness(world, pos);
        int j = LightmapTextureManager.getBlockLightCoordinates(i);
        if (j < (k = state.getLuminance())) {
            int l = LightmapTextureManager.getSkyLightCoordinates(i);
            return LightmapTextureManager.pack(k, l);
        }
        return i;
    }

    public boolean isRenderingReady(BlockPos pos) {
        ChunkBuilder.BuiltChunk lv = this.chunks.getRenderedChunk(pos);
        return lv != null && lv.currentRenderData.get() != ChunkRenderData.HIDDEN;
    }

    @Nullable
    public Framebuffer getEntityOutlinesFramebuffer() {
        return this.framebufferSet.entityOutlineFramebuffer != null ? this.framebufferSet.entityOutlineFramebuffer.get() : null;
    }

    @Nullable
    public Framebuffer getTranslucentFramebuffer() {
        return this.framebufferSet.translucentFramebuffer != null ? this.framebufferSet.translucentFramebuffer.get() : null;
    }

    @Nullable
    public Framebuffer getEntityFramebuffer() {
        return this.framebufferSet.itemEntityFramebuffer != null ? this.framebufferSet.itemEntityFramebuffer.get() : null;
    }

    @Nullable
    public Framebuffer getParticlesFramebuffer() {
        return this.framebufferSet.particlesFramebuffer != null ? this.framebufferSet.particlesFramebuffer.get() : null;
    }

    @Nullable
    public Framebuffer getWeatherFramebuffer() {
        return this.framebufferSet.weatherFramebuffer != null ? this.framebufferSet.weatherFramebuffer.get() : null;
    }

    @Nullable
    public Framebuffer getCloudsFramebuffer() {
        return this.framebufferSet.cloudsFramebuffer != null ? this.framebufferSet.cloudsFramebuffer.get() : null;
    }

    @Debug
    public ObjectArrayList<ChunkBuilder.BuiltChunk> getBuiltChunks() {
        return this.builtChunks;
    }

    @Debug
    public ChunkRenderingDataPreparer getChunkRenderingDataPreparer() {
        return this.chunkRenderingDataPreparer;
    }

    @Nullable
    public Frustum getCapturedFrustum() {
        return this.capturedFrustum;
    }

    public CloudRenderer getCloudRenderer() {
        return this.cloudRenderer;
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface BrightnessGetter {
        public static final BrightnessGetter DEFAULT = (world, pos) -> {
            int i = world.getLightLevel(LightType.SKY, pos);
            int j = world.getLightLevel(LightType.BLOCK, pos);
            return Brightness.pack(j, i);
        };

        public int packedBrightness(BlockRenderView var1, BlockPos var2);
    }
}

