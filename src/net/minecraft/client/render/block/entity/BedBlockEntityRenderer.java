/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BedBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.render.block.entity.state.BedBlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class BedBlockEntityRenderer
implements BlockEntityRenderer<BedBlockEntity, BedBlockEntityRenderState> {
    private final SpriteHolder materials;
    private final Model.SinglePartModel bedHead;
    private final Model.SinglePartModel bedFoot;

    public BedBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this(ctx.spriteHolder(), ctx.loadedEntityModels());
    }

    public BedBlockEntityRenderer(SpecialModelRenderer.BakeContext context) {
        this(context.spriteHolder(), context.entityModelSet());
    }

    public BedBlockEntityRenderer(SpriteHolder materials, LoadedEntityModels entityModelSet) {
        this.materials = materials;
        this.bedHead = new Model.SinglePartModel(entityModelSet.getModelPart(EntityModelLayers.BED_HEAD), RenderLayer::getEntitySolid);
        this.bedFoot = new Model.SinglePartModel(entityModelSet.getModelPart(EntityModelLayers.BED_FOOT), RenderLayer::getEntitySolid);
    }

    public static TexturedModelData getHeadTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(50, 6).cuboid(0.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f), ModelTransform.rotation(1.5707964f, 0.0f, 1.5707964f));
        lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(50, 18).cuboid(-16.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f), ModelTransform.rotation(1.5707964f, 0.0f, (float)Math.PI));
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getFootTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild("main", ModelPartBuilder.create().uv(0, 22).cuboid(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f), ModelTransform.NONE);
        lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(50, 0).cuboid(0.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f), ModelTransform.rotation(1.5707964f, 0.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(50, 12).cuboid(-16.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f), ModelTransform.rotation(1.5707964f, 0.0f, 4.712389f));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public BedBlockEntityRenderState createRenderState() {
        return new BedBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(BedBlockEntity arg, BedBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        arg2.dyeColor = arg.getColor();
        arg2.facing = (Direction)arg.getCachedState().get(BedBlock.FACING);
        boolean bl = arg2.headPart = arg.getCachedState().get(BedBlock.PART) == BedPart.HEAD;
        if (arg.getWorld() != null) {
            DoubleBlockProperties.PropertySource<BedBlockEntity> lv = DoubleBlockProperties.toPropertySource(BlockEntityType.BED, BedBlock::getBedPart, BedBlock::getOppositePartDirection, ChestBlock.FACING, arg.getCachedState(), arg.getWorld(), arg.getPos(), (world, pos) -> false);
            arg2.lightmapCoordinates = ((Int2IntFunction)lv.apply(new LightmapCoordinatesRetriever())).get(arg2.lightmapCoordinates);
        }
    }

    @Override
    public void render(BedBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        SpriteIdentifier lv = TexturedRenderLayers.getBedTextureId(arg.dyeColor);
        this.renderPart(arg2, arg3, arg.headPart ? this.bedHead : this.bedFoot, arg.facing, lv, arg.lightmapCoordinates, OverlayTexture.DEFAULT_UV, false, arg.crumblingOverlay, 0);
    }

    public void renderAsItem(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, SpriteIdentifier textureId, int k) {
        this.renderPart(matrices, queue, this.bedHead, Direction.SOUTH, textureId, light, overlay, false, null, k);
        this.renderPart(matrices, queue, this.bedFoot, Direction.SOUTH, textureId, light, overlay, true, null, k);
    }

    private void renderPart(MatrixStack matrices, OrderedRenderCommandQueue queue, Model.SinglePartModel model, Direction direction, SpriteIdentifier spriteId, int light, int overlay, boolean isFoot, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay, int k) {
        matrices.push();
        BedBlockEntityRenderer.setTransforms(matrices, isFoot, direction);
        queue.submitModel(model, Unit.INSTANCE, matrices, spriteId.getRenderLayer(RenderLayer::getEntitySolid), light, overlay, -1, this.materials.getSprite(spriteId), k, crumblingOverlay);
        matrices.pop();
    }

    private static void setTransforms(MatrixStack matrices, boolean isFoot, Direction direction) {
        matrices.translate(0.0f, 0.5625f, isFoot ? -1.0f : 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f + direction.getPositiveHorizontalDegrees()));
        matrices.translate(-0.5f, -0.5f, -0.5f);
    }

    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack lv = new MatrixStack();
        BedBlockEntityRenderer.setTransforms(lv, false, Direction.SOUTH);
        this.bedHead.getRootPart().collectVertices(lv, vertices);
        lv.loadIdentity();
        BedBlockEntityRenderer.setTransforms(lv, true, Direction.SOUTH);
        this.bedFoot.getRootPart().collectVertices(lv, vertices);
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

