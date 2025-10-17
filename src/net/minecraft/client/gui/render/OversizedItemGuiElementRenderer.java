/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.render;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.OversizedItemGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OversizedItemGuiElementRenderer
extends SpecialGuiElementRenderer<OversizedItemGuiElementRenderState> {
    private boolean oversized;
    @Nullable
    private Object modelKey;

    public OversizedItemGuiElementRenderer(VertexConsumerProvider.Immediate arg) {
        super(arg);
    }

    public boolean isOversized() {
        return this.oversized;
    }

    public void clearOversized() {
        this.oversized = false;
    }

    public void clearModel() {
        this.modelKey = null;
    }

    @Override
    public Class<OversizedItemGuiElementRenderState> getElementClass() {
        return OversizedItemGuiElementRenderState.class;
    }

    @Override
    protected void render(OversizedItemGuiElementRenderState arg, MatrixStack arg2) {
        boolean bl;
        arg2.scale(1.0f, -1.0f, -1.0f);
        ItemGuiElementRenderState lv = arg.guiItemRenderState();
        ScreenRect lv2 = lv.oversizedBounds();
        Objects.requireNonNull(lv2);
        float f = (float)(lv2.getLeft() + lv2.getRight()) / 2.0f;
        float g = (float)(lv2.getTop() + lv2.getBottom()) / 2.0f;
        float h = (float)lv.x() + 8.0f;
        float i = (float)lv.y() + 8.0f;
        arg2.translate((h - f) / 16.0f, (g - i) / 16.0f, 0.0f);
        KeyedItemRenderState lv3 = lv.state();
        boolean bl2 = bl = !lv3.isSideLit();
        if (bl) {
            MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
        } else {
            MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_3D);
        }
        RenderDispatcher lv4 = MinecraftClient.getInstance().gameRenderer.getEntityRenderDispatcher();
        OrderedRenderCommandQueueImpl lv5 = lv4.getQueue();
        lv3.render(arg2, lv5, 0xF000F0, OverlayTexture.DEFAULT_UV, 0);
        lv4.render();
        this.modelKey = lv3.getModelKey();
    }

    @Override
    public void renderElement(OversizedItemGuiElementRenderState arg, GuiRenderState arg2) {
        super.renderElement(arg, arg2);
        this.oversized = true;
    }

    @Override
    public boolean shouldBypassScaling(OversizedItemGuiElementRenderState arg) {
        KeyedItemRenderState lv = arg.guiItemRenderState().state();
        return !lv.isAnimated() && lv.getModelKey().equals(this.modelKey);
    }

    @Override
    protected float getYOffset(int height, int windowScaleFactor) {
        return (float)height / 2.0f;
    }

    @Override
    protected String getName() {
        return "oversized_item";
    }
}

