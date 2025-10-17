/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MatrixUtil;

@Environment(value=EnvType.CLIENT)
public class ItemRenderer {
    public static final Identifier ENTITY_ENCHANTMENT_GLINT = Identifier.ofVanilla("textures/misc/enchanted_glint_armor.png");
    public static final Identifier ITEM_ENCHANTMENT_GLINT = Identifier.ofVanilla("textures/misc/enchanted_glint_item.png");
    public static final float field_60154 = 0.5f;
    public static final float field_60155 = 0.75f;
    public static final float field_60156 = 0.0078125f;
    public static final int NO_TINT = -1;

    public static void renderItem(ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, int[] tints, List<BakedQuad> quads, RenderLayer layer, ItemRenderState.Glint glint) {
        VertexConsumer lv2;
        if (glint == ItemRenderState.Glint.SPECIAL) {
            MatrixStack.Entry lv = matrices.peek().copy();
            if (displayContext == ItemDisplayContext.GUI) {
                MatrixUtil.scale(lv.getPositionMatrix(), 0.5f);
            } else if (displayContext.isFirstPerson()) {
                MatrixUtil.scale(lv.getPositionMatrix(), 0.75f);
            }
            lv2 = ItemRenderer.getSpecialItemGlintConsumer(vertexConsumers, layer, lv);
        } else {
            lv2 = ItemRenderer.getItemGlintConsumer(vertexConsumers, layer, true, glint != ItemRenderState.Glint.NONE);
        }
        ItemRenderer.renderBakedItemQuads(matrices, lv2, quads, tints, light, overlay);
    }

    private static VertexConsumer getSpecialItemGlintConsumer(VertexConsumerProvider consumers, RenderLayer layer, MatrixStack.Entry matrix) {
        return VertexConsumers.union((VertexConsumer)new OverlayVertexConsumer(consumers.getBuffer(ItemRenderer.useTransparentGlint(layer) ? RenderLayer.getGlintTranslucent() : RenderLayer.getGlint()), matrix, 0.0078125f), consumers.getBuffer(layer));
    }

    public static VertexConsumer getItemGlintConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint) {
        if (glint) {
            if (ItemRenderer.useTransparentGlint(layer)) {
                return VertexConsumers.union(vertexConsumers.getBuffer(RenderLayer.getGlintTranslucent()), vertexConsumers.getBuffer(layer));
            }
            return VertexConsumers.union(vertexConsumers.getBuffer(solid ? RenderLayer.getGlint() : RenderLayer.getEntityGlint()), vertexConsumers.getBuffer(layer));
        }
        return vertexConsumers.getBuffer(layer);
    }

    public static List<RenderLayer> getGlintRenderLayers(RenderLayer renderLayer, boolean solid, boolean glint) {
        if (glint) {
            if (ItemRenderer.useTransparentGlint(renderLayer)) {
                return List.of(renderLayer, RenderLayer.getGlintTranslucent());
            }
            return List.of(renderLayer, solid ? RenderLayer.getGlint() : RenderLayer.getEntityGlint());
        }
        return List.of(renderLayer);
    }

    private static boolean useTransparentGlint(RenderLayer renderLayer) {
        return MinecraftClient.isFabulousGraphicsOrBetter() && renderLayer == TexturedRenderLayers.getItemEntityTranslucentCull();
    }

    private static int getTint(int[] tints, int index) {
        if (index < 0 || index >= tints.length) {
            return -1;
        }
        return tints[index];
    }

    private static void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, int[] tints, int light, int overlay) {
        MatrixStack.Entry lv = matrices.peek();
        for (BakedQuad lv2 : quads) {
            float l;
            float h;
            float g;
            float f;
            if (lv2.hasTint()) {
                int k = ItemRenderer.getTint(tints, lv2.tintIndex());
                f = (float)ColorHelper.getAlpha(k) / 255.0f;
                g = (float)ColorHelper.getRed(k) / 255.0f;
                h = (float)ColorHelper.getGreen(k) / 255.0f;
                l = (float)ColorHelper.getBlue(k) / 255.0f;
            } else {
                f = 1.0f;
                g = 1.0f;
                h = 1.0f;
                l = 1.0f;
            }
            vertexConsumer.quad(lv, lv2, g, h, l, f, light, overlay);
        }
    }
}

