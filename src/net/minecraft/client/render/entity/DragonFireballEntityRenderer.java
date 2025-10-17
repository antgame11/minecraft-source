/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class DragonFireballEntityRenderer
extends EntityRenderer<DragonFireballEntity, EntityRenderState> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/enderdragon/dragon_fireball.png");
    private static final RenderLayer LAYER = RenderLayer.getEntityCutoutNoCull(TEXTURE);

    public DragonFireballEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
    }

    @Override
    protected int getBlockLight(DragonFireballEntity arg, BlockPos arg2) {
        return 15;
    }

    @Override
    public void render(EntityRenderState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.scale(2.0f, 2.0f, 2.0f);
        matrices.multiply(cameraState.orientation);
        queue.submitCustom(matrices, LAYER, (arg2, arg3) -> {
            DragonFireballEntityRenderer.produceVertex(arg3, arg2, arg.light, 0.0f, 0, 0, 1);
            DragonFireballEntityRenderer.produceVertex(arg3, arg2, arg.light, 1.0f, 0, 1, 1);
            DragonFireballEntityRenderer.produceVertex(arg3, arg2, arg.light, 1.0f, 1, 1, 0);
            DragonFireballEntityRenderer.produceVertex(arg3, arg2, arg.light, 0.0f, 1, 0, 0);
        });
        matrices.pop();
        super.render(renderState, matrices, queue, cameraState);
    }

    private static void produceVertex(VertexConsumer vertexConsumer, MatrixStack.Entry matrix, int light, float x, int z, int textureU, int textureV) {
        vertexConsumer.vertex(matrix, x - 0.5f, (float)z - 0.25f, 0.0f).color(Colors.WHITE).texture(textureU, textureV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}

