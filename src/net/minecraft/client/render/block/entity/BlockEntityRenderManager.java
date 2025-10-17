/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockEntityRenderManager
implements SynchronousResourceReloader {
    private Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>> renderers = ImmutableMap.of();
    private final TextRenderer textRenderer;
    private final Supplier<LoadedEntityModels> entityModelsGetter;
    private Vec3d cameraPos;
    private final BlockRenderManager blockRenderManager;
    private final ItemModelManager itemModelManager;
    private final ItemRenderer itemRenderer;
    private final EntityRenderManager entityRenderDispatcher;
    private final SpriteHolder spriteHolder;
    private final PlayerSkinCache playerSkinCache;

    public BlockEntityRenderManager(TextRenderer textRenderer, Supplier<LoadedEntityModels> entityModelsGetter, BlockRenderManager blockRenderManager, ItemModelManager itemModelManager, ItemRenderer itemRenderer, EntityRenderManager entityRenderDispatcher, SpriteHolder spriteHolder, PlayerSkinCache playerSkinCache) {
        this.itemRenderer = itemRenderer;
        this.itemModelManager = itemModelManager;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.textRenderer = textRenderer;
        this.entityModelsGetter = entityModelsGetter;
        this.blockRenderManager = blockRenderManager;
        this.spriteHolder = spriteHolder;
        this.playerSkinCache = playerSkinCache;
    }

    @Nullable
    public <E extends BlockEntity, S extends BlockEntityRenderState> BlockEntityRenderer<E, S> get(E blockEntity) {
        return this.renderers.get(blockEntity.getType());
    }

    @Nullable
    public <E extends BlockEntity, S extends BlockEntityRenderState> BlockEntityRenderer<E, S> getByRenderState(S renderState) {
        return this.renderers.get(renderState.type);
    }

    public void configure(Camera camera) {
        this.cameraPos = camera.getPos();
    }

    @Nullable
    public <E extends BlockEntity, S extends BlockEntityRenderState> S getRenderState(E blockEntity, float tickProgress, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer<E, S> lv = this.get(blockEntity);
        if (lv == null) {
            return null;
        }
        if (!blockEntity.hasWorld() || !blockEntity.getType().supports(blockEntity.getCachedState())) {
            return null;
        }
        if (!lv.isInRenderDistance(blockEntity, this.cameraPos)) {
            return null;
        }
        Vec3d lv2 = this.cameraPos;
        S lv3 = lv.createRenderState();
        lv.updateRenderState(blockEntity, lv3, tickProgress, lv2, crumblingOverlay);
        return lv3;
    }

    public <S extends BlockEntityRenderState> void render(S renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
        BlockEntityRenderer lv = this.getByRenderState(renderState);
        if (lv == null) {
            return;
        }
        try {
            lv.render(renderState, matrices, queue, cameraRenderState);
        } catch (Throwable throwable) {
            CrashReport lv2 = CrashReport.create(throwable, "Rendering Block Entity");
            CrashReportSection lv3 = lv2.addElement("Block Entity Details");
            renderState.populateCrashReport(lv3);
            throw new CrashException(lv2);
        }
    }

    @Override
    public void reload(ResourceManager manager) {
        BlockEntityRendererFactory.Context lv = new BlockEntityRendererFactory.Context(this, this.blockRenderManager, this.itemModelManager, this.itemRenderer, this.entityRenderDispatcher, this.entityModelsGetter.get(), this.textRenderer, this.spriteHolder, this.playerSkinCache);
        this.renderers = BlockEntityRendererFactories.reload(lv);
    }
}

