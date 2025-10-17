/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.EntityGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class EntityGuiElementRenderer
extends SpecialGuiElementRenderer<EntityGuiElementRenderState> {
    private final EntityRenderManager entityRenderDispatcher;

    public EntityGuiElementRenderer(VertexConsumerProvider.Immediate vertexConsumers, EntityRenderManager entityRenderDispatcher) {
        super(vertexConsumers);
        this.entityRenderDispatcher = entityRenderDispatcher;
    }

    @Override
    public Class<EntityGuiElementRenderState> getElementClass() {
        return EntityGuiElementRenderState.class;
    }

    @Override
    protected void render(EntityGuiElementRenderState arg, MatrixStack arg2) {
        MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        Vector3f vector3f = arg.translation();
        arg2.translate(vector3f.x, vector3f.y, vector3f.z);
        arg2.multiply(arg.rotation());
        Quaternionf quaternionf = arg.overrideCameraAngle();
        RenderDispatcher lv = MinecraftClient.getInstance().gameRenderer.getEntityRenderDispatcher();
        CameraRenderState lv2 = new CameraRenderState();
        if (quaternionf != null) {
            lv2.orientation = quaternionf.conjugate(new Quaternionf()).rotateY((float)Math.PI);
        }
        this.entityRenderDispatcher.render(arg.renderState(), lv2, 0.0, 0.0, 0.0, arg2, lv.getQueue());
        lv.render();
    }

    @Override
    protected float getYOffset(int height, int windowScaleFactor) {
        return (float)height / 2.0f;
    }

    @Override
    protected String getName() {
        return "entity";
    }
}

