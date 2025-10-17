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
import net.minecraft.client.render.entity.state.ExperienceOrbEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class ExperienceOrbEntityRenderer
extends EntityRenderer<ExperienceOrbEntity, ExperienceOrbEntityRenderState> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/experience_orb.png");
    private static final RenderLayer LAYER = RenderLayer.getItemEntityTranslucentCull(TEXTURE);

    public ExperienceOrbEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.shadowRadius = 0.15f;
        this.shadowOpacity = 0.75f;
    }

    @Override
    protected int getBlockLight(ExperienceOrbEntity arg, BlockPos arg2) {
        return MathHelper.clamp(super.getBlockLight(arg, arg2) + 7, 0, 15);
    }

    @Override
    public void render(ExperienceOrbEntityRenderState arg, MatrixStack arg22, OrderedRenderCommandQueue arg32, CameraRenderState arg4) {
        arg22.push();
        int i = arg.size;
        float f = (float)(i % 4 * 16 + 0) / 64.0f;
        float g = (float)(i % 4 * 16 + 16) / 64.0f;
        float h = (float)(i / 4 * 16 + 0) / 64.0f;
        float j = (float)(i / 4 * 16 + 16) / 64.0f;
        float k = 1.0f;
        float l = 0.5f;
        float m = 0.25f;
        float n = 255.0f;
        float o = arg.age / 2.0f;
        int p = (int)((MathHelper.sin(o + 0.0f) + 1.0f) * 0.5f * 255.0f);
        int q = 255;
        int r = (int)((MathHelper.sin(o + 4.1887903f) + 1.0f) * 0.1f * 255.0f);
        arg22.translate(0.0f, 0.1f, 0.0f);
        arg22.multiply(arg4.orientation);
        float s = 0.3f;
        arg22.scale(0.3f, 0.3f, 0.3f);
        arg32.submitCustom(arg22, LAYER, (arg2, arg3) -> {
            ExperienceOrbEntityRenderer.vertex(arg3, arg2, -0.5f, -0.25f, p, 255, r, f, j, arg.light);
            ExperienceOrbEntityRenderer.vertex(arg3, arg2, 0.5f, -0.25f, p, 255, r, g, j, arg.light);
            ExperienceOrbEntityRenderer.vertex(arg3, arg2, 0.5f, 0.75f, p, 255, r, g, h, arg.light);
            ExperienceOrbEntityRenderer.vertex(arg3, arg2, -0.5f, 0.75f, p, 255, r, f, h, arg.light);
        });
        arg22.pop();
        super.render(arg, arg22, arg32, arg4);
    }

    private static void vertex(VertexConsumer vertexConsumer, MatrixStack.Entry matrix, float x, float y, int red, int green, int blue, float u, float v, int light) {
        vertexConsumer.vertex(matrix, x, y, 0.0f).color(red, green, blue, 128).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public ExperienceOrbEntityRenderState createRenderState() {
        return new ExperienceOrbEntityRenderState();
    }

    @Override
    public void updateRenderState(ExperienceOrbEntity arg, ExperienceOrbEntityRenderState arg2, float f) {
        super.updateRenderState(arg, arg2, f);
        arg2.size = arg.getOrbSize();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

