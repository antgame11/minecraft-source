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
import net.minecraft.block.entity.BeamEmitter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BeaconBlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BeaconBlockEntityRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T, BeaconBlockEntityRenderState> {
    public static final Identifier BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/beacon_beam.png");
    public static final int MAX_BEAM_HEIGHT = 2048;
    private static final float field_56505 = 96.0f;
    public static final float field_56503 = 0.2f;
    public static final float field_56504 = 0.25f;

    @Override
    public BeaconBlockEntityRenderState createRenderState() {
        return new BeaconBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(T arg, BeaconBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        BeaconBlockEntityRenderer.updateBeaconRenderState(arg, arg2, f, arg3);
    }

    public static <T extends BlockEntity> void updateBeaconRenderState(T blockEntity, BeaconBlockEntityRenderState state, float tickProgress, Vec3d cameraPos) {
        state.beamRotationDegrees = blockEntity.getWorld() != null ? (float)Math.floorMod(blockEntity.getWorld().getTime(), 40) + tickProgress : 0.0f;
        state.beamSegments = ((BeamEmitter)((Object)blockEntity)).getBeamSegments().stream().map(beamSegment -> new BeaconBlockEntityRenderState.BeamSegment(beamSegment.getColor(), beamSegment.getHeight())).toList();
        float g = (float)cameraPos.subtract(state.pos.toCenterPos()).horizontalLength();
        ClientPlayerEntity lv = MinecraftClient.getInstance().player;
        state.beamScale = lv != null && lv.isUsingSpyglass() ? 1.0f : Math.max(1.0f, g / 96.0f);
    }

    @Override
    public void render(BeaconBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        int i = 0;
        for (int j = 0; j < arg.beamSegments.size(); ++j) {
            BeaconBlockEntityRenderState.BeamSegment lv = arg.beamSegments.get(j);
            BeaconBlockEntityRenderer.renderBeam(arg2, arg3, arg.beamScale, arg.beamRotationDegrees, i, j == arg.beamSegments.size() - 1 ? 2048 : lv.height(), lv.color());
            i += lv.height();
        }
    }

    private static void renderBeam(MatrixStack matrices, OrderedRenderCommandQueue queue, float scale, float rotationDegrees, int minHeight, int maxHeight, int color) {
        BeaconBlockEntityRenderer.renderBeam(matrices, queue, BEAM_TEXTURE, 1.0f, rotationDegrees, minHeight, maxHeight, color, 0.2f * scale, 0.25f * scale);
    }

    public static void renderBeam(MatrixStack matrices, OrderedRenderCommandQueue queue, Identifier textureId, float beamHeight, float beamRotationDegrees, int minHeight, int maxHeight, int color, float innerScale, float outerScale) {
        int m = minHeight + maxHeight;
        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);
        float n = maxHeight < 0 ? beamRotationDegrees : -beamRotationDegrees;
        float o = MathHelper.fractionalPart(n * 0.2f - (float)MathHelper.floor(n * 0.1f));
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(beamRotationDegrees * 2.25f - 45.0f));
        float p = 0.0f;
        float q = innerScale;
        float r = innerScale;
        float s = 0.0f;
        float t = -innerScale;
        float u = 0.0f;
        float v = 0.0f;
        float w = -innerScale;
        float x = 0.0f;
        float y = 1.0f;
        float z = -1.0f + o;
        float aa = (float)maxHeight * beamHeight * (0.5f / innerScale) + z;
        queue.submitCustom(matrices, RenderLayer.getBeaconBeam(textureId, false), (matricesEntry, vertexConsumer) -> BeaconBlockEntityRenderer.renderBeamLayer(matricesEntry, vertexConsumer, color, minHeight, m, 0.0f, q, r, 0.0f, t, 0.0f, 0.0f, w, 0.0f, 1.0f, aa, z));
        matrices.pop();
        p = -outerScale;
        q = -outerScale;
        r = outerScale;
        s = -outerScale;
        t = -outerScale;
        u = outerScale;
        v = outerScale;
        w = outerScale;
        x = 0.0f;
        y = 1.0f;
        z = -1.0f + o;
        aa = (float)maxHeight * beamHeight + z;
        queue.submitCustom(matrices, RenderLayer.getBeaconBeam(textureId, true), (matricesEntry, vertexConsumer) -> BeaconBlockEntityRenderer.renderBeamLayer(matricesEntry, vertexConsumer, ColorHelper.withAlpha(32, color), minHeight, m, p, q, r, s, t, u, v, w, 0.0f, 1.0f, aa, z));
        matrices.pop();
    }

    private static void renderBeamLayer(MatrixStack.Entry matricesEntry, VertexConsumer vertices, int color, int yOffset, int height, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float u1, float u2, float v1, float v2) {
        BeaconBlockEntityRenderer.renderBeamFace(matricesEntry, vertices, color, yOffset, height, x1, z1, x2, z2, u1, u2, v1, v2);
        BeaconBlockEntityRenderer.renderBeamFace(matricesEntry, vertices, color, yOffset, height, x4, z4, x3, z3, u1, u2, v1, v2);
        BeaconBlockEntityRenderer.renderBeamFace(matricesEntry, vertices, color, yOffset, height, x2, z2, x4, z4, u1, u2, v1, v2);
        BeaconBlockEntityRenderer.renderBeamFace(matricesEntry, vertices, color, yOffset, height, x3, z3, x1, z1, u1, u2, v1, v2);
    }

    private static void renderBeamFace(MatrixStack.Entry matrix, VertexConsumer vertices, int color, int yOffset, int height, float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2) {
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, color, height, x1, z1, u2, v1);
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, color, yOffset, x1, z1, u2, v2);
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, color, yOffset, x2, z2, u1, v2);
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, color, height, x2, z2, u1, v1);
    }

    private static void renderBeamVertex(MatrixStack.Entry matrix, VertexConsumer vertices, int color, int y, float x, float z, float u, float v) {
        vertices.vertex(matrix, x, (float)y, z).color(color).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(matrix, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return MinecraftClient.getInstance().options.getClampedViewDistance() * 16;
    }

    @Override
    public boolean isInRenderDistance(T blockEntity, Vec3d pos) {
        return Vec3d.ofCenter(((BlockEntity)blockEntity).getPos()).multiply(1.0, 0.0, 1.0).isInRange(pos.multiply(1.0, 0.0, 1.0), this.getRenderDistance());
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

