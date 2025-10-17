/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.StructureBlockBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BeaconBlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.StructureBlockBlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.TestInstanceBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TestInstanceBlockEntityRenderer
implements BlockEntityRenderer<TestInstanceBlockEntity, TestInstanceBlockEntityRenderState> {
    private static final float field_62965 = 0.02f;
    private final BeaconBlockEntityRenderer<TestInstanceBlockEntity> beaconBlockEntityRenderer = new BeaconBlockEntityRenderer();
    private final StructureBlockBlockEntityRenderer<TestInstanceBlockEntity> structureBlockBlockEntityRenderer = new StructureBlockBlockEntityRenderer();
    private final TextRenderer textRenderer;
    private final EntityRenderManager entityRenderDispatcher;

    public TestInstanceBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.textRenderer = context.textRenderer();
        this.entityRenderDispatcher = context.entityRenderDispatcher();
    }

    @Override
    public TestInstanceBlockEntityRenderState createRenderState() {
        return new TestInstanceBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(TestInstanceBlockEntity arg, TestInstanceBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        arg2.beaconState = new BeaconBlockEntityRenderState();
        BlockEntityRenderState.updateBlockEntityRenderState(arg, arg2.beaconState, arg4);
        BeaconBlockEntityRenderer.updateBeaconRenderState(arg, arg2.beaconState, f, arg3);
        arg2.structureState = new StructureBlockBlockEntityRenderState();
        BlockEntityRenderState.updateBlockEntityRenderState(arg, arg2.structureState, arg4);
        StructureBlockBlockEntityRenderer.updateStructureBoxRenderState(arg, arg2.structureState);
        arg2.errors.clear();
        for (TestInstanceBlockEntity.Error lv : arg.getErrors()) {
            arg2.errors.add(new TestInstanceBlockEntity.Error(lv.pos().subtract(arg.getPos()), lv.text()));
        }
    }

    @Override
    public void render(TestInstanceBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        this.beaconBlockEntityRenderer.render(arg.beaconState, arg2, arg3, arg4);
        this.structureBlockBlockEntityRenderer.render(arg.structureState, arg2, arg3, arg4);
        for (TestInstanceBlockEntity.Error lv : arg.errors) {
            this.renderError(arg2, arg3, lv, arg4);
        }
    }

    private void renderError(MatrixStack matrices, OrderedRenderCommandQueue queue, TestInstanceBlockEntity.Error error, CameraRenderState cameraRenderState) {
        BlockPos lv = error.pos();
        queue.getBatchingQueue(1).submitCustom(matrices, RenderLayer.getDebugFilledBox(), (matricesEntry, vertexConsumer) -> {
            float f = (float)lv.getX() - 0.02f;
            float g = (float)lv.getY() - 0.02f;
            float h = (float)lv.getZ() - 0.02f;
            float i = (float)lv.getX() + 1.0f + 0.02f;
            float j = (float)lv.getY() + 1.0f + 0.02f;
            float k = (float)lv.getZ() + 1.0f + 0.02f;
            MatrixStack lv = new MatrixStack();
            lv.peek().copy(matricesEntry);
            VertexRendering.drawFilledBox(lv, vertexConsumer, f, g, h, i, j, k, 1.0f, 0.0f, 0.0f, 0.375f);
        });
        OrderedText lv2 = error.text().asOrderedText();
        int i = this.textRenderer.getWidth(lv2);
        float f = 0.01f;
        matrices.push();
        matrices.translate((float)lv.getX() + 0.5f, (float)lv.getY() + 1.2f, (float)lv.getZ() + 0.5f);
        matrices.multiply(cameraRenderState.orientation);
        matrices.scale(0.01f, -0.01f, 0.01f);
        queue.getBatchingQueue(2).submitText(matrices, (float)(-i) / 2.0f, 0.0f, lv2, false, TextRenderer.TextLayerType.SEE_THROUGH, LightmapTextureManager.MAX_LIGHT_COORDINATE, -1, 0, 0);
        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return this.beaconBlockEntityRenderer.rendersOutsideBoundingBox() || this.structureBlockBlockEntityRenderer.rendersOutsideBoundingBox();
    }

    @Override
    public int getRenderDistance() {
        return Math.max(this.beaconBlockEntityRenderer.getRenderDistance(), this.structureBlockBlockEntityRenderer.getRenderDistance());
    }

    @Override
    public boolean isInRenderDistance(TestInstanceBlockEntity arg, Vec3d arg2) {
        return this.beaconBlockEntityRenderer.isInRenderDistance(arg, arg2) || this.structureBlockBlockEntityRenderer.isInRenderDistance(arg, arg2);
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

