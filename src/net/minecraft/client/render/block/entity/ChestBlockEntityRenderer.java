/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CopperChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.render.block.entity.model.ChestBlockModel;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.ChestBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChestBlockEntityRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T, ChestBlockEntityRenderState> {
    private final SpriteHolder materials;
    private final ChestBlockModel singleChest;
    private final ChestBlockModel doubleChestLeft;
    private final ChestBlockModel doubleChestRight;
    private final boolean christmas;

    public ChestBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.materials = context.spriteHolder();
        this.christmas = ChestBlockEntityRenderer.isAroundChristmas();
        this.singleChest = new ChestBlockModel(context.getLayerModelPart(EntityModelLayers.CHEST));
        this.doubleChestLeft = new ChestBlockModel(context.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_LEFT));
        this.doubleChestRight = new ChestBlockModel(context.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_RIGHT));
    }

    public static boolean isAroundChristmas() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26;
    }

    @Override
    public ChestBlockEntityRenderState createRenderState() {
        return new ChestBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(T arg, ChestBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        DoubleBlockProperties.PropertySource<Object> lv3;
        Block block;
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        boolean bl = ((BlockEntity)arg).getWorld() != null;
        BlockState lv = bl ? ((BlockEntity)arg).getCachedState() : (BlockState)Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);
        arg2.chestType = lv.contains(ChestBlock.CHEST_TYPE) ? lv.get(ChestBlock.CHEST_TYPE) : ChestType.SINGLE;
        arg2.yaw = lv.get(ChestBlock.FACING).getPositiveHorizontalDegrees();
        arg2.variant = this.getVariant((BlockEntity)arg, this.christmas);
        if (bl && (block = lv.getBlock()) instanceof ChestBlock) {
            ChestBlock lv2 = (ChestBlock)block;
            lv3 = lv2.getBlockEntitySource(lv, ((BlockEntity)arg).getWorld(), ((BlockEntity)arg).getPos(), true);
        } else {
            lv3 = DoubleBlockProperties.PropertyRetriever::getFallback;
        }
        arg2.lidAnimationProgress = lv3.apply(ChestBlock.getAnimationProgressRetriever((LidOpenable)arg)).get(f);
        if (arg2.chestType != ChestType.SINGLE) {
            arg2.lightmapCoordinates = ((Int2IntFunction)lv3.apply(new LightmapCoordinatesRetriever())).applyAsInt(arg2.lightmapCoordinates);
        }
    }

    @Override
    public void render(ChestBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        arg2.push();
        arg2.translate(0.5f, 0.5f, 0.5f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-arg.yaw));
        arg2.translate(-0.5f, -0.5f, -0.5f);
        float f = arg.lidAnimationProgress;
        f = 1.0f - f;
        f = 1.0f - f * f * f;
        SpriteIdentifier lv = TexturedRenderLayers.getChestTextureId(arg.variant, arg.chestType);
        RenderLayer lv2 = lv.getRenderLayer(RenderLayer::getEntityCutout);
        Sprite lv3 = this.materials.getSprite(lv);
        if (arg.chestType != ChestType.SINGLE) {
            if (arg.chestType == ChestType.LEFT) {
                arg3.submitModel(this.doubleChestLeft, Float.valueOf(f), arg2, lv2, arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, -1, lv3, 0, arg.crumblingOverlay);
            } else {
                arg3.submitModel(this.doubleChestRight, Float.valueOf(f), arg2, lv2, arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, -1, lv3, 0, arg.crumblingOverlay);
            }
        } else {
            arg3.submitModel(this.singleChest, Float.valueOf(f), arg2, lv2, arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, -1, lv3, 0, arg.crumblingOverlay);
        }
        arg2.pop();
    }

    private ChestBlockEntityRenderState.Variant getVariant(BlockEntity blockEntity, boolean christmas) {
        if (blockEntity instanceof EnderChestBlockEntity) {
            return ChestBlockEntityRenderState.Variant.ENDER_CHEST;
        }
        if (christmas) {
            return ChestBlockEntityRenderState.Variant.CHRISTMAS;
        }
        if (blockEntity instanceof TrappedChestBlockEntity) {
            return ChestBlockEntityRenderState.Variant.TRAPPED;
        }
        Block block = blockEntity.getCachedState().getBlock();
        if (block instanceof CopperChestBlock) {
            CopperChestBlock lv = (CopperChestBlock)block;
            return switch (lv.getOxidationLevel()) {
                default -> throw new MatchException(null, null);
                case Oxidizable.OxidationLevel.UNAFFECTED -> ChestBlockEntityRenderState.Variant.COPPER_UNAFFECTED;
                case Oxidizable.OxidationLevel.EXPOSED -> ChestBlockEntityRenderState.Variant.COPPER_EXPOSED;
                case Oxidizable.OxidationLevel.WEATHERED -> ChestBlockEntityRenderState.Variant.COPPER_WEATHERED;
                case Oxidizable.OxidationLevel.OXIDIZED -> ChestBlockEntityRenderState.Variant.COPPER_OXIDIZED;
            };
        }
        return ChestBlockEntityRenderState.Variant.REGULAR;
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

