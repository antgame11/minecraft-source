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
import net.minecraft.client.gui.render.state.special.BookModelGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class BookModelGuiElementRenderer
extends SpecialGuiElementRenderer<BookModelGuiElementRenderState> {
    public BookModelGuiElementRenderer(VertexConsumerProvider.Immediate arg) {
        super(arg);
    }

    @Override
    public Class<BookModelGuiElementRenderState> getElementClass() {
        return BookModelGuiElementRenderState.class;
    }

    @Override
    protected void render(BookModelGuiElementRenderState arg, MatrixStack arg2) {
        MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(25.0f));
        float f = arg.open();
        arg2.translate((1.0f - f) * 0.2f, (1.0f - f) * 0.1f, (1.0f - f) * 0.25f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-(1.0f - f) * 90.0f - 90.0f));
        arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
        float g = arg.flip();
        float h = MathHelper.clamp(MathHelper.fractionalPart(g + 0.25f) * 1.6f - 0.3f, 0.0f, 1.0f);
        float i = MathHelper.clamp(MathHelper.fractionalPart(g + 0.75f) * 1.6f - 0.3f, 0.0f, 1.0f);
        BookModel lv = arg.bookModel();
        lv.setAngles(new BookModel.BookModelState(0.0f, h, i, f));
        Identifier lv2 = arg.texture();
        VertexConsumer lv3 = this.vertexConsumers.getBuffer(lv.getLayer(lv2));
        lv.render(arg2, lv3, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
    }

    @Override
    protected float getYOffset(int height, int windowScaleFactor) {
        return 17 * windowScaleFactor;
    }

    @Override
    protected String getName() {
        return "book model";
    }
}

