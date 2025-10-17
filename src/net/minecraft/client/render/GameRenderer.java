/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.runtime.SwitchBootstraps;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlobalSettings;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.render.BannerResultGuiElementRenderer;
import net.minecraft.client.gui.render.BookModelGuiElementRenderer;
import net.minecraft.client.gui.render.EntityGuiElementRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.PlayerSkinGuiElementRenderer;
import net.minecraft.client.gui.render.ProfilerChartGuiElementRenderer;
import net.minecraft.client.gui.render.SignGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.screen.DebugOptionsScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerLikeState;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.ProjectionMatrix3;
import net.minecraft.client.render.RawProjectionMatrix;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Pool;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.profiler.ScopedProfiler;
import net.minecraft.world.GameMode;
import net.minecraft.world.waypoint.TrackedWaypoint;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class GameRenderer
implements TrackedWaypoint.PitchProvider,
AutoCloseable {
    private static final Identifier BLUR_ID = Identifier.ofVanilla("blur");
    public static final int field_49904 = 10;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final float CAMERA_DEPTH = 0.05f;
    public static final float field_60107 = 100.0f;
    private static final float field_55869 = 20.0f;
    private static final float field_55870 = 7.0f;
    private final MinecraftClient client;
    private final Random random = Random.create();
    private float viewDistanceBlocks;
    public final HeldItemRenderer firstPersonRenderer;
    private final InGameOverlayRenderer overlayRenderer;
    private final BufferBuilderStorage buffers;
    private float nauseaEffectTime;
    private float nauseaEffectSpeed;
    private float fovMultiplier;
    private float lastFovMultiplier;
    private float skyDarkness;
    private float lastSkyDarkness;
    private boolean blockOutlineEnabled = true;
    private long lastWorldIconUpdate;
    private boolean hasWorldIcon;
    private long lastWindowFocusedTime = Util.getMeasuringTimeMs();
    private final LightmapTextureManager lightmapTextureManager;
    private final OverlayTexture overlayTexture = new OverlayTexture();
    private boolean renderingPanorama;
    protected final CubeMapRenderer panoramaRenderer = new CubeMapRenderer(Identifier.ofVanilla("textures/gui/title/background/panorama"));
    protected final RotatingCubeMapRenderer rotatingPanoramaRenderer = new RotatingCubeMapRenderer(this.panoramaRenderer);
    private final Pool pool = new Pool(3);
    private final FogRenderer fogRenderer = new FogRenderer();
    private final GuiRenderer guiRenderer;
    private final GuiRenderState guiState;
    private final WorldRenderState worldRenderState = new WorldRenderState();
    private final OrderedRenderCommandQueueImpl orderedRenderCommandQueue;
    private final RenderDispatcher entityRenderDispatcher;
    @Nullable
    private Identifier postProcessorId;
    private boolean postProcessorEnabled;
    private final Camera camera = new Camera();
    private final DiffuseLighting diffuseLighting = new DiffuseLighting();
    private final GlobalSettings globalSettings = new GlobalSettings();
    private final RawProjectionMatrix worldProjectionMatrix = new RawProjectionMatrix("level");
    private final ProjectionMatrix3 hudProjectionMatrix = new ProjectionMatrix3("3d hud", 0.05f, 100.0f);

    public GameRenderer(MinecraftClient client, HeldItemRenderer firstPersonHeldItemRenderer, BufferBuilderStorage buffers, BlockRenderManager blockRenderManager) {
        this.client = client;
        this.firstPersonRenderer = firstPersonHeldItemRenderer;
        this.lightmapTextureManager = new LightmapTextureManager(this, client);
        this.buffers = buffers;
        this.guiState = new GuiRenderState();
        VertexConsumerProvider.Immediate lv = buffers.getEntityVertexConsumers();
        AtlasManager lv2 = client.getAtlasManager();
        this.orderedRenderCommandQueue = new OrderedRenderCommandQueueImpl();
        this.entityRenderDispatcher = new RenderDispatcher(this.orderedRenderCommandQueue, blockRenderManager, lv, lv2, buffers.getOutlineVertexConsumers(), buffers.getEffectVertexConsumers(), client.textRenderer);
        this.guiRenderer = new GuiRenderer(this.guiState, lv, this.orderedRenderCommandQueue, this.entityRenderDispatcher, List.of(new EntityGuiElementRenderer(lv, client.getEntityRenderDispatcher()), new PlayerSkinGuiElementRenderer(lv), new BookModelGuiElementRenderer(lv), new BannerResultGuiElementRenderer(lv, lv2), new SignGuiElementRenderer(lv, lv2), new ProfilerChartGuiElementRenderer(lv)));
        this.overlayRenderer = new InGameOverlayRenderer(client, lv2, lv);
    }

    @Override
    public void close() {
        this.globalSettings.close();
        this.lightmapTextureManager.close();
        this.overlayTexture.close();
        this.pool.close();
        this.guiRenderer.close();
        this.worldProjectionMatrix.close();
        this.hudProjectionMatrix.close();
        this.diffuseLighting.close();
        this.panoramaRenderer.close();
        this.fogRenderer.close();
        this.entityRenderDispatcher.close();
    }

    public OrderedRenderCommandQueueImpl getEntityRenderCommandQueue() {
        return this.orderedRenderCommandQueue;
    }

    public RenderDispatcher getEntityRenderDispatcher() {
        return this.entityRenderDispatcher;
    }

    public WorldRenderState getEntityRenderStates() {
        return this.worldRenderState;
    }

    public void setBlockOutlineEnabled(boolean blockOutlineEnabled) {
        this.blockOutlineEnabled = blockOutlineEnabled;
    }

    public void setRenderingPanorama(boolean renderingPanorama) {
        this.renderingPanorama = renderingPanorama;
    }

    public boolean isRenderingPanorama() {
        return this.renderingPanorama;
    }

    public void clearPostProcessor() {
        this.postProcessorId = null;
        this.postProcessorEnabled = false;
    }

    public void togglePostProcessorEnabled() {
        this.postProcessorEnabled = !this.postProcessorEnabled;
    }

    public void onCameraEntitySet(@Nullable Entity arg) {
        Entity entity = arg;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{CreeperEntity.class, SpiderEntity.class, EndermanEntity.class}, (Object)entity, n)) {
            case 0: {
                CreeperEntity lv = (CreeperEntity)entity;
                this.setPostProcessor(Identifier.ofVanilla("creeper"));
                break;
            }
            case 1: {
                SpiderEntity lv2 = (SpiderEntity)entity;
                this.setPostProcessor(Identifier.ofVanilla("spider"));
                break;
            }
            case 2: {
                EndermanEntity lv3 = (EndermanEntity)entity;
                this.setPostProcessor(Identifier.ofVanilla("invert"));
                break;
            }
            default: {
                this.clearPostProcessor();
            }
        }
    }

    private void setPostProcessor(Identifier id) {
        this.postProcessorId = id;
        this.postProcessorEnabled = true;
    }

    public void renderBlur() {
        PostEffectProcessor lv = this.client.getShaderLoader().loadPostEffect(BLUR_ID, DefaultFramebufferSet.MAIN_ONLY);
        if (lv != null) {
            lv.render(this.client.getFramebuffer(), this.pool);
        }
    }

    public void preloadPrograms(ResourceFactory factory) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        BiFunction<Identifier, ShaderType, String> biFunction = (id, type) -> {
            String string;
            block8: {
                Identifier lv = type.idConverter().toResourcePath((Identifier)id);
                BufferedReader reader = factory.getResourceOrThrow(lv).getReader();
                try {
                    string = IOUtils.toString(reader);
                    if (reader == null) break block8;
                } catch (Throwable throwable) {
                    try {
                        if (reader != null) {
                            try {
                                ((Reader)reader).close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    } catch (IOException iOException) {
                        LOGGER.error("Coudln't preload {} shader {}: {}", type, id, iOException);
                        return null;
                    }
                }
                ((Reader)reader).close();
            }
            return string;
        };
        gpuDevice.precompilePipeline(RenderPipelines.GUI, biFunction);
        gpuDevice.precompilePipeline(RenderPipelines.GUI_TEXTURED, biFunction);
        if (TracyClient.isAvailable()) {
            gpuDevice.precompilePipeline(RenderPipelines.TRACY_BLIT, biFunction);
        }
    }

    public void tick() {
        this.updateFovMultiplier();
        this.lightmapTextureManager.tick();
        ClientPlayerEntity lv = this.client.player;
        if (this.client.getCameraEntity() == null) {
            this.client.setCameraEntity(lv);
        }
        this.camera.updateEyeHeight();
        this.firstPersonRenderer.updateHeldItems();
        float f = lv.nauseaIntensity;
        float g = lv.getEffectFadeFactor(StatusEffects.NAUSEA, 1.0f);
        if (f > 0.0f || g > 0.0f) {
            this.nauseaEffectSpeed = (f * 20.0f + g * 7.0f) / (f + g);
            this.nauseaEffectTime += this.nauseaEffectSpeed;
        } else {
            this.nauseaEffectSpeed = 0.0f;
        }
        if (!this.client.world.getTickManager().shouldTick()) {
            return;
        }
        this.lastSkyDarkness = this.skyDarkness;
        if (this.client.inGameHud.getBossBarHud().shouldDarkenSky()) {
            this.skyDarkness += 0.05f;
            if (this.skyDarkness > 1.0f) {
                this.skyDarkness = 1.0f;
            }
        } else if (this.skyDarkness > 0.0f) {
            this.skyDarkness -= 0.0125f;
        }
        this.overlayRenderer.tickFloatingItemTimer();
        Profiler lv2 = Profilers.get();
        lv2.push("levelRenderer");
        this.client.worldRenderer.tick(this.camera);
        lv2.pop();
    }

    @Nullable
    public Identifier getPostProcessorId() {
        return this.postProcessorId;
    }

    public void onResized(int width, int height) {
        this.pool.clear();
        this.client.worldRenderer.onResized(width, height);
    }

    public void updateCrosshairTarget(float tickProgress) {
        Entity entity;
        HitResult lv2;
        Entity lv = this.client.getCameraEntity();
        if (lv == null) {
            return;
        }
        if (this.client.world == null || this.client.player == null) {
            return;
        }
        Profilers.get().push("pick");
        double d = this.client.player.getBlockInteractionRange();
        double e = this.client.player.getEntityInteractionRange();
        this.client.crosshairTarget = lv2 = this.findCrosshairTarget(lv, d, e, tickProgress);
        if (lv2 instanceof EntityHitResult) {
            EntityHitResult lv3 = (EntityHitResult)lv2;
            entity = lv3.getEntity();
        } else {
            entity = null;
        }
        this.client.targetedEntity = entity;
        Profilers.get().pop();
    }

    private HitResult findCrosshairTarget(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickProgress) {
        double g = Math.max(blockInteractionRange, entityInteractionRange);
        double h = MathHelper.square(g);
        Vec3d lv = camera.getCameraPosVec(tickProgress);
        HitResult lv2 = camera.raycast(g, tickProgress, false);
        double i = lv2.getPos().squaredDistanceTo(lv);
        if (lv2.getType() != HitResult.Type.MISS) {
            h = i;
            g = Math.sqrt(h);
        }
        Vec3d lv3 = camera.getRotationVec(tickProgress);
        Vec3d lv4 = lv.add(lv3.x * g, lv3.y * g, lv3.z * g);
        float j = 1.0f;
        Box lv5 = camera.getBoundingBox().stretch(lv3.multiply(g)).expand(1.0, 1.0, 1.0);
        EntityHitResult lv6 = ProjectileUtil.raycast(camera, lv, lv4, lv5, EntityPredicates.CAN_HIT, h);
        if (lv6 != null && lv6.getPos().squaredDistanceTo(lv) < i) {
            return GameRenderer.ensureTargetInRange(lv6, lv, entityInteractionRange);
        }
        return GameRenderer.ensureTargetInRange(lv2, lv, blockInteractionRange);
    }

    private static HitResult ensureTargetInRange(HitResult hitResult, Vec3d cameraPos, double interactionRange) {
        Vec3d lv = hitResult.getPos();
        if (!lv.isInRange(cameraPos, interactionRange)) {
            Vec3d lv2 = hitResult.getPos();
            Direction lv3 = Direction.getFacing(lv2.x - cameraPos.x, lv2.y - cameraPos.y, lv2.z - cameraPos.z);
            return BlockHitResult.createMissed(lv2, lv3, BlockPos.ofFloored(lv2));
        }
        return hitResult;
    }

    private void updateFovMultiplier() {
        float g;
        Entity entity = this.client.getCameraEntity();
        if (entity instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity lv = (AbstractClientPlayerEntity)entity;
            GameOptions lv2 = this.client.options;
            boolean bl = lv2.getPerspective().isFirstPerson();
            float f = lv2.getFovEffectScale().getValue().floatValue();
            g = lv.getFovMultiplier(bl, f);
        } else {
            g = 1.0f;
        }
        this.lastFovMultiplier = this.fovMultiplier;
        this.fovMultiplier += (g - this.fovMultiplier) * 0.5f;
        this.fovMultiplier = MathHelper.clamp(this.fovMultiplier, 0.1f, 1.5f);
    }

    private float getFov(Camera camera, float tickProgress, boolean changingFov) {
        CameraSubmersionType lv2;
        LivingEntity lv;
        Entity entity;
        if (this.renderingPanorama) {
            return 90.0f;
        }
        float g = 70.0f;
        if (changingFov) {
            g = this.client.options.getFov().getValue().intValue();
            g *= MathHelper.lerp(tickProgress, this.lastFovMultiplier, this.fovMultiplier);
        }
        if ((entity = camera.getFocusedEntity()) instanceof LivingEntity && (lv = (LivingEntity)entity).isDead()) {
            float h = Math.min((float)lv.deathTime + tickProgress, 20.0f);
            g /= (1.0f - 500.0f / (h + 500.0f)) * 2.0f + 1.0f;
        }
        if ((lv2 = camera.getSubmersionType()) == CameraSubmersionType.LAVA || lv2 == CameraSubmersionType.WATER) {
            float h = this.client.options.getFovEffectScale().getValue().floatValue();
            g *= MathHelper.lerp(h, 1.0f, 0.85714287f);
        }
        return g;
    }

    private void tiltViewWhenHurt(MatrixStack matrices, float tickProgress) {
        Entity entity = this.client.getCameraEntity();
        if (entity instanceof LivingEntity) {
            float h;
            LivingEntity lv = (LivingEntity)entity;
            float g = (float)lv.hurtTime - tickProgress;
            if (lv.isDead()) {
                h = Math.min((float)lv.deathTime + tickProgress, 20.0f);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(40.0f - 8000.0f / (h + 200.0f)));
            }
            if (g < 0.0f) {
                return;
            }
            g /= (float)lv.maxHurtTime;
            g = MathHelper.sin(g * g * g * g * (float)Math.PI);
            h = lv.getDamageTiltYaw();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-h));
            float i = (float)((double)(-g) * 14.0 * this.client.options.getDamageTiltStrength().getValue());
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h));
        }
    }

    private void bobView(MatrixStack matrices, float tickProgress) {
        Entity entity = this.client.getCameraEntity();
        if (!(entity instanceof AbstractClientPlayerEntity)) {
            return;
        }
        AbstractClientPlayerEntity lv = (AbstractClientPlayerEntity)entity;
        ClientPlayerLikeState lv2 = lv.getState();
        float g = lv2.getReverseLerpedDistanceMoved(tickProgress);
        float h = lv2.lerpMovement(tickProgress);
        matrices.translate(MathHelper.sin(g * (float)Math.PI) * h * 0.5f, -Math.abs(MathHelper.cos(g * (float)Math.PI) * h), 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(g * (float)Math.PI) * h * 3.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(g * (float)Math.PI - 0.2f) * h) * 5.0f));
    }

    private void renderHand(float tickProgress, boolean sleeping, Matrix4f positionMatrix) {
        if (this.renderingPanorama) {
            return;
        }
        this.entityRenderDispatcher.render();
        this.buffers.getEntityVertexConsumers().draw();
        MatrixStack lv = new MatrixStack();
        lv.push();
        lv.multiplyPositionMatrix(positionMatrix.invert(new Matrix4f()));
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix().mul(positionMatrix);
        this.tiltViewWhenHurt(lv, tickProgress);
        if (this.client.options.getBobView().getValue().booleanValue()) {
            this.bobView(lv, tickProgress);
        }
        if (this.client.options.getPerspective().isFirstPerson() && !sleeping && !this.client.options.hudHidden && this.client.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
            this.lightmapTextureManager.enable();
            this.firstPersonRenderer.renderItem(tickProgress, lv, this.client.gameRenderer.getEntityRenderCommandQueue(), this.client.player, this.client.getEntityRenderDispatcher().getLight(this.client.player, tickProgress));
            this.lightmapTextureManager.disable();
        }
        matrix4fStack.popMatrix();
        lv.pop();
    }

    public Matrix4f getBasicProjectionMatrix(float fovDegrees) {
        Matrix4f matrix4f = new Matrix4f();
        return matrix4f.perspective(fovDegrees * ((float)Math.PI / 180), (float)this.client.getWindow().getFramebufferWidth() / (float)this.client.getWindow().getFramebufferHeight(), 0.05f, this.getFarPlaneDistance());
    }

    public float getFarPlaneDistance() {
        return Math.max(this.viewDistanceBlocks * 4.0f, (float)(this.client.options.getCloudRenderDistance().getValue() * 16));
    }

    public static float getNightVisionStrength(LivingEntity entity, float tickProgress) {
        StatusEffectInstance lv = entity.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (!lv.isDurationBelow(200)) {
            return 1.0f;
        }
        return 0.7f + MathHelper.sin(((float)lv.getDuration() - tickProgress) * (float)Math.PI * 0.2f) * 0.3f;
    }

    public void render(RenderTickCounter tickCounter, boolean tick) {
        if (this.client.isWindowFocused() || !this.client.options.pauseOnLostFocus || this.client.options.getTouchscreen().getValue().booleanValue() && this.client.mouse.wasRightButtonClicked()) {
            this.lastWindowFocusedTime = Util.getMeasuringTimeMs();
        } else if (Util.getMeasuringTimeMs() - this.lastWindowFocusedTime > 500L) {
            this.client.openGameMenu(false);
        }
        if (this.client.skipGameRender) {
            return;
        }
        this.globalSettings.set(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), this.client.options.getGlintStrength().getValue(), this.client.world == null ? 0L : this.client.world.getTime(), tickCounter, this.client.options.getMenuBackgroundBlurrinessValue());
        Profiler lv = Profilers.get();
        boolean bl2 = this.client.isFinishedLoading();
        int i = (int)this.client.mouse.getScaledX(this.client.getWindow());
        int j = (int)this.client.mouse.getScaledY(this.client.getWindow());
        if (bl2 && tick && this.client.world != null) {
            lv.push("world");
            this.renderWorld(tickCounter);
            this.updateWorldIcon();
            this.client.worldRenderer.drawEntityOutlinesFramebuffer();
            if (this.postProcessorId != null && this.postProcessorEnabled) {
                RenderSystem.resetTextureMatrix();
                PostEffectProcessor lv2 = this.client.getShaderLoader().loadPostEffect(this.postProcessorId, DefaultFramebufferSet.MAIN_ONLY);
                if (lv2 != null) {
                    lv2.render(this.client.getFramebuffer(), this.pool);
                }
            }
            lv.pop();
        }
        this.fogRenderer.rotate();
        Framebuffer lv3 = this.client.getFramebuffer();
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(lv3.getDepthAttachment(), 1.0);
        this.client.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_3D);
        this.guiState.clear();
        lv.push("guiExtraction");
        DrawContext lv4 = new DrawContext(this.client, this.guiState);
        if (bl2 && tick && this.client.world != null) {
            this.client.inGameHud.render(lv4, tickCounter);
        }
        if (this.client.getOverlay() != null) {
            try {
                this.client.getOverlay().render(lv4, i, j, tickCounter.getDynamicDeltaTicks());
            } catch (Throwable throwable) {
                CrashReport lv5 = CrashReport.create(throwable, "Rendering overlay");
                CrashReportSection lv6 = lv5.addElement("Overlay render details");
                lv6.add("Overlay name", () -> this.client.getOverlay().getClass().getCanonicalName());
                throw new CrashException(lv5);
            }
        }
        if (bl2 && this.client.currentScreen != null) {
            try {
                this.client.currentScreen.renderWithTooltip(lv4, i, j, tickCounter.getDynamicDeltaTicks());
            } catch (Throwable throwable) {
                CrashReport lv5 = CrashReport.create(throwable, "Rendering screen");
                CrashReportSection lv6 = lv5.addElement("Screen render details");
                lv6.add("Screen name", () -> this.client.currentScreen.getClass().getCanonicalName());
                this.client.mouse.addCrashReportSection(lv6, this.client.getWindow());
                throw new CrashException(lv5);
            }
            if (SharedConstants.CURSOR_POS) {
                this.client.mouse.drawScaledPos(this.client.textRenderer, lv4);
            }
            try {
                if (this.client.currentScreen != null) {
                    this.client.currentScreen.updateNarrator();
                }
            } catch (Throwable throwable) {
                CrashReport lv5 = CrashReport.create(throwable, "Narrating screen");
                CrashReportSection lv6 = lv5.addElement("Screen details");
                lv6.add("Screen name", () -> this.client.currentScreen.getClass().getCanonicalName());
                throw new CrashException(lv5);
            }
        }
        if (bl2 && tick && this.client.world != null) {
            this.client.inGameHud.renderAutosaveIndicator(lv4, tickCounter);
        }
        if (bl2) {
            try (ScopedProfiler lv7 = lv.scoped("toasts");){
                this.client.getToastManager().draw(lv4);
            }
        }
        if (!(this.client.currentScreen instanceof DebugOptionsScreen)) {
            this.client.inGameHud.renderDebugHud(lv4);
        }
        this.client.inGameHud.renderDeferredSubtitles();
        lv.swap("guiRendering");
        this.guiRenderer.render(this.fogRenderer.getFogBuffer(FogRenderer.FogType.NONE));
        this.guiRenderer.incrementFrame();
        lv.pop();
        lv4.applyCursorTo(this.client.getWindow());
        this.orderedRenderCommandQueue.onNextFrame();
        this.entityRenderDispatcher.endLayeredCustoms();
        this.pool.decrementLifespan();
    }

    private void updateWorldIcon() {
        if (this.hasWorldIcon || !this.client.isInSingleplayer()) {
            return;
        }
        long l = Util.getMeasuringTimeMs();
        if (l - this.lastWorldIconUpdate < 1000L) {
            return;
        }
        this.lastWorldIconUpdate = l;
        IntegratedServer lv = this.client.getServer();
        if (lv == null || lv.isStopped()) {
            return;
        }
        lv.getIconFile().ifPresent(path -> {
            if (Files.isRegularFile(path, new LinkOption[0])) {
                this.hasWorldIcon = true;
            } else {
                this.updateWorldIcon((Path)path);
            }
        });
    }

    private void updateWorldIcon(Path path) {
        if (this.client.worldRenderer.getCompletedChunkCount() > 10 && this.client.worldRenderer.isTerrainRenderComplete()) {
            ScreenshotRecorder.takeScreenshot(this.client.getFramebuffer(), screenshot -> Util.getIoWorkerExecutor().execute(() -> {
                int i = screenshot.getWidth();
                int j = screenshot.getHeight();
                int k = 0;
                int l = 0;
                if (i > j) {
                    k = (i - j) / 2;
                    i = j;
                } else {
                    l = (j - i) / 2;
                    j = i;
                }
                try (NativeImage lv = new NativeImage(64, 64, false);){
                    screenshot.resizeSubRectTo(k, l, i, j, lv);
                    lv.writeTo(path);
                } catch (IOException iOException) {
                    LOGGER.warn("Couldn't save auto screenshot", iOException);
                } finally {
                    screenshot.close();
                }
            }));
        }
    }

    private boolean shouldRenderBlockOutline() {
        boolean bl;
        if (!this.blockOutlineEnabled) {
            return false;
        }
        Entity lv = this.client.getCameraEntity();
        boolean bl2 = bl = lv instanceof PlayerEntity && !this.client.options.hudHidden;
        if (bl && !((PlayerEntity)lv).getAbilities().allowModifyWorld) {
            ItemStack lv2 = ((LivingEntity)lv).getMainHandStack();
            HitResult lv3 = this.client.crosshairTarget;
            if (lv3 != null && lv3.getType() == HitResult.Type.BLOCK) {
                BlockPos lv4 = ((BlockHitResult)lv3).getBlockPos();
                BlockState lv5 = this.client.world.getBlockState(lv4);
                if (this.client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
                    bl = lv5.createScreenHandlerFactory(this.client.world, lv4) != null;
                } else {
                    CachedBlockPosition lv6 = new CachedBlockPosition(this.client.world, lv4, false);
                    RegistryWrapper.Impl lv7 = this.client.world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK);
                    bl = !lv2.isEmpty() && (lv2.canBreak(lv6) || lv2.canPlaceOn(lv6));
                }
            }
        }
        return bl;
    }

    public void renderWorld(RenderTickCounter renderTickCounter) {
        float f = renderTickCounter.getTickProgress(true);
        ClientPlayerEntity lv = this.client.player;
        this.lightmapTextureManager.update(f);
        if (this.client.getCameraEntity() == null) {
            this.client.setCameraEntity(lv);
        }
        this.updateCrosshairTarget(f);
        Profiler lv2 = Profilers.get();
        lv2.push("center");
        boolean bl = this.shouldRenderBlockOutline();
        lv2.swap("camera");
        ClientPlayerEntity lv3 = this.client.getCameraEntity() == null ? lv : this.client.getCameraEntity();
        float g = this.client.world.getTickManager().shouldSkipTick(lv3) ? 1.0f : f;
        this.camera.update(this.client.world, lv3, !this.client.options.getPerspective().isFirstPerson(), this.client.options.getPerspective().isFrontView(), g);
        this.updateCameraState(f);
        this.viewDistanceBlocks = this.client.options.getClampedViewDistance() * 16;
        float h = this.getFov(this.camera, f, true);
        Matrix4f matrix4f = this.getBasicProjectionMatrix(h);
        MatrixStack lv4 = new MatrixStack();
        this.tiltViewWhenHurt(lv4, this.camera.getLastTickProgress());
        if (this.client.options.getBobView().getValue().booleanValue()) {
            this.bobView(lv4, this.camera.getLastTickProgress());
        }
        matrix4f.mul(lv4.peek().getPositionMatrix());
        float i = this.client.options.getDistortionEffectScale().getValue().floatValue();
        float j = MathHelper.lerp(f, lv.lastNauseaIntensity, lv.nauseaIntensity);
        float k = lv.getEffectFadeFactor(StatusEffects.NAUSEA, f);
        float l = Math.max(j, k) * (i * i);
        if (l > 0.0f) {
            float m = 5.0f / (l * l + 5.0f) - l * 0.04f;
            m *= m;
            Vector3f vector3f = new Vector3f(0.0f, MathHelper.SQUARE_ROOT_OF_TWO / 2.0f, MathHelper.SQUARE_ROOT_OF_TWO / 2.0f);
            float n = (this.nauseaEffectTime + f * this.nauseaEffectSpeed) * ((float)Math.PI / 180);
            matrix4f.rotate(n, vector3f);
            matrix4f.scale(1.0f / m, 1.0f, 1.0f);
            matrix4f.rotate(-n, vector3f);
        }
        RenderSystem.setProjectionMatrix(this.worldProjectionMatrix.set(matrix4f), ProjectionType.PERSPECTIVE);
        Quaternionf quaternionf = this.camera.getRotation().conjugate(new Quaternionf());
        Matrix4f matrix4f2 = new Matrix4f().rotation(quaternionf);
        lv2.swap("fog");
        boolean bl2 = this.client.world.getDimensionEffects().useThickFog(this.camera.getBlockPos().getX(), this.camera.getBlockPos().getZ()) || this.client.inGameHud.getBossBarHud().shouldThickenFog();
        Vector4f vector4f = this.fogRenderer.applyFog(this.camera, this.client.options.getClampedViewDistance(), bl2, renderTickCounter, this.getSkyDarkness(f), this.client.world);
        GpuBufferSlice gpuBufferSlice = this.fogRenderer.getFogBuffer(FogRenderer.FogType.WORLD);
        lv2.swap("level");
        this.client.worldRenderer.render(this.pool, renderTickCounter, bl, this.camera, matrix4f2, matrix4f, this.getProjectionMatrix(h), gpuBufferSlice, vector4f, !bl2);
        lv2.swap("hand");
        boolean bl3 = this.client.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.client.getCameraEntity()).isSleeping();
        RenderSystem.setProjectionMatrix(this.hudProjectionMatrix.set(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), this.getFov(this.camera, f, false)), ProjectionType.PERSPECTIVE);
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(this.client.getFramebuffer().getDepthAttachment(), 1.0);
        this.renderHand(f, bl3, matrix4f2);
        lv2.swap("screenEffects");
        VertexConsumerProvider.Immediate lv5 = this.buffers.getEntityVertexConsumers();
        this.overlayRenderer.renderOverlays(bl3, f, this.orderedRenderCommandQueue);
        this.entityRenderDispatcher.render();
        lv5.draw();
        lv2.pop();
        RenderSystem.setShaderFog(this.fogRenderer.getFogBuffer(FogRenderer.FogType.NONE));
        if (this.client.debugHudEntryList.isEntryVisible(DebugHudEntries.THREE_DIMENSIONAL_CROSSHAIR) && this.client.options.getPerspective().isFirstPerson() && !this.client.options.hudHidden) {
            this.client.getDebugHud().renderDebugCrosshair(this.camera);
        }
    }

    private void updateCameraState(float f) {
        CameraRenderState lv = this.worldRenderState.cameraRenderState;
        lv.initialized = this.camera.isReady();
        lv.pos = this.camera.getPos();
        lv.blockPos = this.camera.getBlockPos();
        lv.entityPos = this.camera.getFocusedEntity().getLerpedPos(f);
        lv.orientation = new Quaternionf(this.camera.getRotation());
    }

    private Matrix4f getProjectionMatrix(float f) {
        float g = Math.max(f, (float)this.client.options.getFov().getValue().intValue());
        return this.getBasicProjectionMatrix(g);
    }

    public void reset() {
        this.overlayRenderer.clearFloatingItem();
        this.client.getMapTextureManager().clear();
        this.camera.reset();
        this.hasWorldIcon = false;
    }

    public void showFloatingItem(ItemStack floatingItem) {
        this.overlayRenderer.setFloatingItem(floatingItem, this.random);
    }

    public MinecraftClient getClient() {
        return this.client;
    }

    public float getSkyDarkness(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastSkyDarkness, this.skyDarkness);
    }

    public float getViewDistanceBlocks() {
        return this.viewDistanceBlocks;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public LightmapTextureManager getLightmapTextureManager() {
        return this.lightmapTextureManager;
    }

    public OverlayTexture getOverlayTexture() {
        return this.overlayTexture;
    }

    @Override
    public Vec3d project(Vec3d sourcePos) {
        Matrix4f matrix4f = this.getBasicProjectionMatrix(this.getFov(this.camera, 0.0f, true));
        Quaternionf quaternionf = this.camera.getRotation().conjugate(new Quaternionf());
        Matrix4f matrix4f2 = new Matrix4f().rotation(quaternionf);
        Matrix4f matrix4f3 = matrix4f.mul(matrix4f2);
        Vec3d lv = this.camera.getPos();
        Vec3d lv2 = sourcePos.subtract(lv);
        Vector3f vector3f = matrix4f3.transformProject(lv2.toVector3f());
        return new Vec3d(vector3f);
    }

    @Override
    public double getPitch() {
        float f = this.camera.getPitch();
        if (f <= -90.0f) {
            return Double.NEGATIVE_INFINITY;
        }
        if (f >= 90.0f) {
            return Double.POSITIVE_INFINITY;
        }
        float g = this.getFov(this.camera, 0.0f, true);
        return Math.tan(f * ((float)Math.PI / 180)) / Math.tan(g / 2.0f * ((float)Math.PI / 180));
    }

    public GlobalSettings getGlobalSettings() {
        return this.globalSettings;
    }

    public DiffuseLighting getDiffuseLighting() {
        return this.diffuseLighting;
    }

    public void setWorld(@Nullable ClientWorld world) {
        if (world != null) {
            this.diffuseLighting.updateLevelBuffer(world.getDimensionEffects().isDarkened());
        }
    }

    public RotatingCubeMapRenderer getRotatingPanoramaRenderer() {
        return this.rotatingPanoramaRenderer;
    }
}

