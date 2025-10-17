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
import net.minecraft.client.gui.render.state.special.BannerResultGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.math.MatrixStack;

@Environment(value=EnvType.CLIENT)
public class BannerResultGuiElementRenderer
extends SpecialGuiElementRenderer<BannerResultGuiElementRenderState> {
    private final SpriteHolder sprite;

    public BannerResultGuiElementRenderer(VertexConsumerProvider.Immediate immediate, SpriteHolder sprite) {
        super(immediate);
        this.sprite = sprite;
    }

    @Override
    public Class<BannerResultGuiElementRenderState> getElementClass() {
        return BannerResultGuiElementRenderState.class;
    }

    @Override
    protected void render(BannerResultGuiElementRenderState arg, MatrixStack arg2) {
        MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
        arg2.translate(0.0f, 0.25f, 0.0f);
        RenderDispatcher lv = MinecraftClient.getInstance().gameRenderer.getEntityRenderDispatcher();
        OrderedRenderCommandQueueImpl lv2 = lv.getQueue();
        BannerBlockEntityRenderer.renderCanvas(this.sprite, arg2, lv2, 0xF000F0, OverlayTexture.DEFAULT_UV, arg.flag(), Float.valueOf(0.0f), ModelBaker.BANNER_BASE, true, arg.baseColor(), arg.resultBannerPatterns(), false, null, 0);
        lv.render();
    }

    @Override
    protected String getName() {
        return "banner result";
    }
}

