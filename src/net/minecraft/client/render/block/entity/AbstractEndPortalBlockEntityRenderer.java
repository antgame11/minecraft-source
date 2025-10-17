/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import java.util.EnumSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.EndPortalBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractEndPortalBlockEntityRenderer<T extends EndPortalBlockEntity, S extends EndPortalBlockEntityRenderState>
implements BlockEntityRenderer<T, S> {
    public static final Identifier SKY_TEXTURE = Identifier.ofVanilla("textures/environment/end_sky.png");
    public static final Identifier PORTAL_TEXTURE = Identifier.ofVanilla("textures/entity/end_portal.png");

    @Override
    public void updateRenderState(T arg, S arg2, float f, Vec3d arg3, @Nullable ModelCommandRenderer.CrumblingOverlayCommand arg4) {
        BlockEntityRenderer.super.updateRenderState(arg, arg2, f, arg3, arg4);
        ((EndPortalBlockEntityRenderState)arg2).sides.clear();
        for (Direction lv : Direction.values()) {
            if (!((EndPortalBlockEntity)arg).shouldDrawSide(lv)) continue;
            ((EndPortalBlockEntityRenderState)arg2).sides.add(lv);
        }
    }

    @Override
    public void render(S arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4) {
        arg3.submitCustom(arg2, this.getLayer(), (matricesEntry, vertexConsumer) -> this.renderSides(arg.sides, matricesEntry.getPositionMatrix(), vertexConsumer));
    }

    private void renderSides(EnumSet<Direction> sides, Matrix4f matrix, VertexConsumer vertexConsumer) {
        float f = this.getBottomYOffset();
        float g = this.getTopYOffset();
        this.renderSide(sides, matrix, vertexConsumer, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, Direction.SOUTH);
        this.renderSide(sides, matrix, vertexConsumer, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, Direction.NORTH);
        this.renderSide(sides, matrix, vertexConsumer, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.EAST);
        this.renderSide(sides, matrix, vertexConsumer, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.WEST);
        this.renderSide(sides, matrix, vertexConsumer, 0.0f, 1.0f, f, f, 0.0f, 0.0f, 1.0f, 1.0f, Direction.DOWN);
        this.renderSide(sides, matrix, vertexConsumer, 0.0f, 1.0f, g, g, 1.0f, 1.0f, 0.0f, 0.0f, Direction.UP);
    }

    private void renderSide(EnumSet<Direction> sides, Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, Direction side) {
        if (sides.contains(side)) {
            vertices.vertex(model, x1, y1, z1);
            vertices.vertex(model, x2, y1, z2);
            vertices.vertex(model, x2, y2, z3);
            vertices.vertex(model, x1, y2, z4);
        }
    }

    protected float getTopYOffset() {
        return 0.75f;
    }

    protected float getBottomYOffset() {
        return 0.375f;
    }

    protected RenderLayer getLayer() {
        return RenderLayer.getEndPortal();
    }
}

