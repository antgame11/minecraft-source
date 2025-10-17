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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.StructureBoxRendering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.StructureBlockBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class StructureBlockBlockEntityRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T, StructureBlockBlockEntityRenderState> {
    @Override
    public StructureBlockBlockEntityRenderState createRenderState() {
        return new StructureBlockBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(T arg, StructureBlockBlockEntityRenderState arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        StructureBlockBlockEntityRenderer.updateStructureBoxRenderState(arg, arg2);
    }

    public static <T extends BlockEntity> void updateStructureBoxRenderState(T blockEntity, StructureBlockBlockEntityRenderState state) {
        ClientPlayerEntity lv = MinecraftClient.getInstance().player;
        state.visible = lv.isCreativeLevelTwoOp() || lv.isSpectator();
        state.structureBox = ((StructureBoxRendering)((Object)blockEntity)).getStructureBox();
        state.renderMode = ((StructureBoxRendering)((Object)blockEntity)).getRenderMode();
        BlockPos lv2 = state.structureBox.localPos();
        Vec3i lv3 = state.structureBox.size();
        BlockPos lv4 = state.pos;
        BlockPos lv5 = lv4.add(lv2);
        if (state.visible && blockEntity.getWorld() != null && state.renderMode == StructureBoxRendering.RenderMode.BOX_AND_INVISIBLE_BLOCKS) {
            state.invisibleBlocks = new StructureBlockBlockEntityRenderState.InvisibleRenderType[lv3.getX() * lv3.getY() * lv3.getZ()];
            for (int i = 0; i < lv3.getX(); ++i) {
                for (int j = 0; j < lv3.getY(); ++j) {
                    for (int k = 0; k < lv3.getZ(); ++k) {
                        int l = k * lv3.getX() * lv3.getY() + j * lv3.getX() + i;
                        BlockState lv6 = blockEntity.getWorld().getBlockState(lv5.add(i, j, k));
                        if (lv6.isAir()) {
                            state.invisibleBlocks[l] = StructureBlockBlockEntityRenderState.InvisibleRenderType.AIR;
                            continue;
                        }
                        if (lv6.isOf(Blocks.STRUCTURE_VOID)) {
                            state.invisibleBlocks[l] = StructureBlockBlockEntityRenderState.InvisibleRenderType.STRUCUTRE_VOID;
                            continue;
                        }
                        if (lv6.isOf(Blocks.BARRIER)) {
                            state.invisibleBlocks[l] = StructureBlockBlockEntityRenderState.InvisibleRenderType.BARRIER;
                            continue;
                        }
                        if (!lv6.isOf(Blocks.LIGHT)) continue;
                        state.invisibleBlocks[l] = StructureBlockBlockEntityRenderState.InvisibleRenderType.LIGHT;
                    }
                }
            }
        } else {
            state.invisibleBlocks = null;
        }
        if (state.visible) {
            // empty if block
        }
        state.field_62682 = null;
    }

    @Override
    public void render(StructureBlockBlockEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        if (!arg.visible) {
            return;
        }
        StructureBoxRendering.RenderMode lv = arg.renderMode;
        if (lv == StructureBoxRendering.RenderMode.NONE) {
            return;
        }
        StructureBoxRendering.StructureBox lv2 = arg.structureBox;
        BlockPos lv3 = lv2.localPos();
        Vec3i lv4 = lv2.size();
        if (lv4.getX() < 1 || lv4.getY() < 1 || lv4.getZ() < 1) {
            return;
        }
        float f = 1.0f;
        float g = 0.9f;
        float h = 0.5f;
        BlockPos lv5 = lv3.add(lv4);
        arg3.submitCustom(arg2, RenderLayer.getLines(), (matricesEntry, vertexConsumer) -> VertexRendering.drawBox(matricesEntry, vertexConsumer, lv3.getX(), lv3.getY(), lv3.getZ(), lv5.getX(), lv5.getY(), lv5.getZ(), 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f));
        this.renderInvisibleBlocks(arg, lv3, lv4, arg3, arg2);
    }

    private void renderInvisibleBlocks(StructureBlockBlockEntityRenderState state, BlockPos pos, Vec3i size, OrderedRenderCommandQueue queue, MatrixStack matrices) {
        if (state.invisibleBlocks == null) {
            return;
        }
        BlockPos lv = state.pos;
        BlockPos lv2 = lv.add(pos);
        queue.submitCustom(matrices, RenderLayer.getLines(), (matricesEntry, vertexConsumer) -> {
            for (int i = 0; i < size.getX(); ++i) {
                for (int j = 0; j < size.getY(); ++j) {
                    for (int k = 0; k < size.getZ(); ++k) {
                        int l = k * size.getX() * size.getY() + j * size.getX() + i;
                        StructureBlockBlockEntityRenderState.InvisibleRenderType lv = arg2.invisibleBlocks[l];
                        if (lv == null) continue;
                        float f = lv == StructureBlockBlockEntityRenderState.InvisibleRenderType.AIR ? 0.05f : 0.0f;
                        double d = (float)(lv2.getX() + i - lv.getX()) + 0.45f - f;
                        double e = (float)(lv2.getY() + j - lv.getY()) + 0.45f - f;
                        double g = (float)(lv2.getZ() + k - lv.getZ()) + 0.45f - f;
                        double h = (float)(lv2.getX() + i - lv.getX()) + 0.55f + f;
                        double m = (float)(lv2.getY() + j - lv.getY()) + 0.55f + f;
                        double n = (float)(lv2.getZ() + k - lv.getZ()) + 0.55f + f;
                        if (lv == StructureBlockBlockEntityRenderState.InvisibleRenderType.AIR) {
                            VertexRendering.drawBox(matricesEntry, vertexConsumer, d, e, g, h, m, n, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, 0.5f, 1.0f);
                            continue;
                        }
                        if (lv == StructureBlockBlockEntityRenderState.InvisibleRenderType.STRUCUTRE_VOID) {
                            VertexRendering.drawBox(matricesEntry, vertexConsumer, d, e, g, h, m, n, 1.0f, 0.75f, 0.75f, 1.0f, 1.0f, 0.75f, 0.75f);
                            continue;
                        }
                        if (lv == StructureBlockBlockEntityRenderState.InvisibleRenderType.BARRIER) {
                            VertexRendering.drawBox(matricesEntry, vertexConsumer, d, e, g, h, m, n, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f);
                            continue;
                        }
                        if (lv != StructureBlockBlockEntityRenderState.InvisibleRenderType.LIGHT) continue;
                        VertexRendering.drawBox(matricesEntry, vertexConsumer, d, e, g, h, m, n, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f);
                    }
                }
            }
        });
    }

    private void renderStructureVoids(StructureBlockBlockEntityRenderState state, BlockPos pos, Vec3i size, VertexConsumer vertexConsumer, Matrix4f matrix4f) {
        if (state.field_62682 == null) {
            return;
        }
        BlockPos lv = state.pos;
        BitSetVoxelSet lv2 = new BitSetVoxelSet(size.getX(), size.getY(), size.getZ());
        for (int i = 0; i < size.getX(); ++i) {
            for (int j = 0; j < size.getY(); ++j) {
                for (int k = 0; k < size.getZ(); ++k) {
                    int l = k * size.getX() * size.getY() + j * size.getX() + i;
                    if (!state.field_62682[l]) continue;
                    ((VoxelSet)lv2).set(i, j, k);
                }
            }
        }
        lv2.forEachDirection((direction, x, y, z) -> {
            float f = 0.48f;
            float g = (float)(x + pos.getX() - lv.getX()) + 0.5f - 0.48f;
            float h = (float)(y + pos.getY() - lv.getY()) + 0.5f - 0.48f;
            float l = (float)(z + pos.getZ() - lv.getZ()) + 0.5f - 0.48f;
            float m = (float)(x + pos.getX() - lv.getX()) + 0.5f + 0.48f;
            float n = (float)(y + pos.getY() - lv.getY()) + 0.5f + 0.48f;
            float o = (float)(z + pos.getZ() - lv.getZ()) + 0.5f + 0.48f;
            VertexRendering.drawSide(matrix4f, vertexConsumer, direction, g, h, l, m, n, o, 0.75f, 0.75f, 1.0f, 0.2f);
        });
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 96;
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }

    private /* synthetic */ void method_73536(StructureBlockBlockEntityRenderState arg, BlockPos arg2, Vec3i arg3, MatrixStack.Entry arg4, VertexConsumer arg5) {
        this.renderStructureVoids(arg, arg2, arg3, arg5, arg4.getPositionMatrix());
    }
}

