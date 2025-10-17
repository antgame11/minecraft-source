/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientMannequinEntity;
import net.minecraft.client.network.ClientPlayerLikeEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class EntityRenderManager
implements SynchronousResourceReloader {
    private Map<EntityType<?>, EntityRenderer<?, ?>> renderers = ImmutableMap.of();
    private Map<PlayerSkinType, PlayerEntityRenderer<AbstractClientPlayerEntity>> playerRenderers = Map.of();
    private Map<PlayerSkinType, PlayerEntityRenderer<ClientMannequinEntity>> mannequinRenderers = Map.of();
    public final TextureManager textureManager;
    @Nullable
    public Camera camera;
    public Entity targetedEntity;
    private final ItemModelManager itemModelManager;
    private final MapRenderer mapRenderer;
    private final BlockRenderManager blockRenderManager;
    private final HeldItemRenderer heldItemRenderer;
    private final AtlasManager atlasManager;
    private final TextRenderer textRenderer;
    public final GameOptions gameOptions;
    private final Supplier<LoadedEntityModels> entityModelsGetter;
    private final EquipmentModelLoader equipmentModelLoader;
    private final PlayerSkinCache skinCache;

    public <E extends Entity> int getLight(E entity, float tickProgress) {
        return this.getRenderer((EntityRenderState)((Object)entity)).getLight(entity, tickProgress);
    }

    public EntityRenderManager(MinecraftClient client, TextureManager textureManager, ItemModelManager itemModelManager, ItemRenderer itemRenderer, MapRenderer mapRenderer, BlockRenderManager blockRenderManager, AtlasManager atlasManager, TextRenderer textRenderer, GameOptions gameOptions, Supplier<LoadedEntityModels> entityModelsGetter, EquipmentModelLoader equipmentModelLoader, PlayerSkinCache skinCache) {
        this.textureManager = textureManager;
        this.itemModelManager = itemModelManager;
        this.mapRenderer = mapRenderer;
        this.atlasManager = atlasManager;
        this.skinCache = skinCache;
        this.heldItemRenderer = new HeldItemRenderer(client, this, itemRenderer, itemModelManager);
        this.blockRenderManager = blockRenderManager;
        this.textRenderer = textRenderer;
        this.gameOptions = gameOptions;
        this.entityModelsGetter = entityModelsGetter;
        this.equipmentModelLoader = equipmentModelLoader;
    }

    public <T extends Entity> EntityRenderer<? super T, ?> getRenderer(T entity) {
        T t = entity;
        Objects.requireNonNull(t);
        T t2 = t;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AbstractClientPlayerEntity.class, ClientMannequinEntity.class}, t2, n)) {
            case 0 -> {
                AbstractClientPlayerEntity lv = (AbstractClientPlayerEntity)t2;
                yield this.getPlayerRenderer(this.playerRenderers, lv);
            }
            case 1 -> {
                ClientMannequinEntity lv2 = (ClientMannequinEntity)t2;
                yield this.getPlayerRenderer(this.mannequinRenderers, lv2);
            }
            default -> this.renderers.get(entity.getType());
        };
    }

    public PlayerEntityRenderer<AbstractClientPlayerEntity> getPlayerRenderer(AbstractClientPlayerEntity player) {
        return this.getPlayerRenderer(this.playerRenderers, player);
    }

    private <T extends PlayerLikeEntity> PlayerEntityRenderer<T> getPlayerRenderer(Map<PlayerSkinType, PlayerEntityRenderer<T>> skinTypeToRenderer, T player) {
        PlayerSkinType lv = ((ClientPlayerLikeEntity)((Object)player)).getSkin().model();
        PlayerEntityRenderer<T> lv2 = skinTypeToRenderer.get(lv);
        if (lv2 != null) {
            return lv2;
        }
        return skinTypeToRenderer.get(PlayerSkinType.WIDE);
    }

    public <S extends EntityRenderState> EntityRenderer<?, ? super S> getRenderer(S state) {
        if (state instanceof PlayerEntityRenderState) {
            PlayerEntityRenderState lv = (PlayerEntityRenderState)state;
            PlayerSkinType lv2 = lv.skinTextures.model();
            EntityRenderer lv3 = this.playerRenderers.get(lv2);
            if (lv3 != null) {
                return lv3;
            }
            return this.playerRenderers.get(PlayerSkinType.WIDE);
        }
        return this.renderers.get(state.entityType);
    }

    public void configure(Camera camera, Entity targetedEntity) {
        this.camera = camera;
        this.targetedEntity = targetedEntity;
    }

    public <E extends Entity> boolean shouldRender(E entity, Frustum frustum, double x, double y, double z) {
        EntityRenderer<?, E> lv = this.getRenderer((EntityRenderState)((Object)entity));
        return lv.shouldRender(entity, frustum, x, y, z);
    }

    public <E extends Entity> EntityRenderState getAndUpdateRenderState(E entity, float tickProgress) {
        EntityRenderer<?, E> lv = this.getRenderer((EntityRenderState)((Object)entity));
        try {
            return lv.getAndUpdateRenderState(entity, tickProgress);
        } catch (Throwable throwable) {
            CrashReport lv2 = CrashReport.create(throwable, "Extracting render state for an entity in world");
            CrashReportSection lv3 = lv2.addElement("Entity being extracted");
            entity.populateCrashReport(lv3);
            CrashReportSection lv4 = this.addRendererDetails(lv, lv2);
            lv4.add("Delta", Float.valueOf(tickProgress));
            throw new CrashException(lv2);
        }
    }

    public <S extends EntityRenderState> void render(S renderState, CameraRenderState arg2, double d, double e, double f, MatrixStack arg3, OrderedRenderCommandQueue arg4) {
        EntityRenderer<?, S> lv = this.getRenderer(renderState);
        try {
            Vec3d lv2 = lv.getPositionOffset(renderState);
            double g = d + lv2.getX();
            double h = e + lv2.getY();
            double i = f + lv2.getZ();
            arg3.push();
            arg3.translate(g, h, i);
            lv.render(renderState, arg3, arg4, arg2);
            if (renderState.onFire) {
                arg4.submitFire(arg3, renderState, MathHelper.rotateAround(MathHelper.Y_AXIS, arg2.orientation, new Quaternionf()));
            }
            if (renderState instanceof PlayerEntityRenderState) {
                arg3.translate(-lv2.getX(), -lv2.getY(), -lv2.getZ());
            }
            if (!renderState.shadowPieces.isEmpty()) {
                arg4.submitShadowPieces(arg3, renderState.shadowRadius, renderState.shadowPieces);
            }
            if (!(renderState instanceof PlayerEntityRenderState)) {
                arg3.translate(-lv2.getX(), -lv2.getY(), -lv2.getZ());
            }
            if (renderState.hitbox != null) {
                arg4.submitDebugHitbox(arg3, renderState, renderState.hitbox);
            }
            arg3.pop();
        } catch (Throwable throwable) {
            CrashReport lv3 = CrashReport.create(throwable, "Rendering entity in world");
            CrashReportSection lv4 = lv3.addElement("EntityRenderState being rendered");
            renderState.addCrashReportDetails(lv4);
            this.addRendererDetails(lv, lv3);
            throw new CrashException(lv3);
        }
    }

    private <S extends EntityRenderState> CrashReportSection addRendererDetails(EntityRenderer<?, S> renderer, CrashReport crashReport) {
        CrashReportSection lv = crashReport.addElement("Renderer details");
        lv.add("Assigned renderer", renderer);
        return lv;
    }

    public void clearCamera() {
        this.camera = null;
    }

    public double getSquaredDistanceToCamera(Entity entity) {
        return this.camera.getPos().squaredDistanceTo(entity.getEntityPos());
    }

    public HeldItemRenderer getHeldItemRenderer() {
        return this.heldItemRenderer;
    }

    @Override
    public void reload(ResourceManager manager) {
        EntityRendererFactory.Context lv = new EntityRendererFactory.Context(this, this.itemModelManager, this.mapRenderer, this.blockRenderManager, manager, this.entityModelsGetter.get(), this.equipmentModelLoader, this.atlasManager, this.textRenderer, this.skinCache);
        this.renderers = EntityRendererFactories.reloadEntityRenderers(lv);
        this.playerRenderers = EntityRendererFactories.reloadPlayerRenderers(lv);
        this.mannequinRenderers = EntityRendererFactories.reloadPlayerRenderers(lv);
    }
}

