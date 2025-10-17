/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.MapTextureManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Atlases;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class MapRenderer {
    private static final float field_53102 = -0.01f;
    private static final float field_53103 = -0.001f;
    public static final int DEFAULT_IMAGE_WIDTH = 128;
    public static final int DEFAULT_IMAGE_HEIGHT = 128;
    private final SpriteAtlasTexture decorationsAtlasManager;
    private final MapTextureManager textureManager;

    public MapRenderer(AtlasManager decorationsAtlasManager, MapTextureManager textureManager) {
        this.decorationsAtlasManager = decorationsAtlasManager.getAtlasTexture(Atlases.MAP_DECORATIONS);
        this.textureManager = textureManager;
    }

    public void draw(MapRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, boolean bl, int light) {
        queue.submitCustom(matrices, RenderLayer.getText(state.texture), (arg, arg2) -> {
            arg2.vertex(arg, 0.0f, 128.0f, -0.01f).color(Colors.WHITE).texture(0.0f, 1.0f).light(light);
            arg2.vertex(arg, 128.0f, 128.0f, -0.01f).color(Colors.WHITE).texture(1.0f, 1.0f).light(light);
            arg2.vertex(arg, 128.0f, 0.0f, -0.01f).color(Colors.WHITE).texture(1.0f, 0.0f).light(light);
            arg2.vertex(arg, 0.0f, 0.0f, -0.01f).color(Colors.WHITE).texture(0.0f, 0.0f).light(light);
        });
        int j = 0;
        for (MapRenderState.Decoration lv : state.decorations) {
            if (bl && !lv.alwaysRendered) continue;
            matrices.push();
            matrices.translate((float)lv.x / 2.0f + 64.0f, (float)lv.z / 2.0f + 64.0f, -0.02f);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(lv.rotation * 360) / 16.0f));
            matrices.scale(4.0f, 4.0f, 3.0f);
            matrices.translate(-0.125f, 0.125f, 0.0f);
            Sprite lv2 = lv.sprite;
            if (lv2 != null) {
                float f = (float)j * -0.001f;
                queue.submitCustom(matrices, RenderLayer.getText(lv2.getAtlasId()), (arg2, arg3) -> {
                    arg3.vertex(arg2, -1.0f, 1.0f, f).color(Colors.WHITE).texture(lv2.getMinU(), lv2.getMinV()).light(light);
                    arg3.vertex(arg2, 1.0f, 1.0f, f).color(Colors.WHITE).texture(lv2.getMaxU(), lv2.getMinV()).light(light);
                    arg3.vertex(arg2, 1.0f, -1.0f, f).color(Colors.WHITE).texture(lv2.getMaxU(), lv2.getMaxV()).light(light);
                    arg3.vertex(arg2, -1.0f, -1.0f, f).color(Colors.WHITE).texture(lv2.getMinU(), lv2.getMaxV()).light(light);
                });
                matrices.pop();
            }
            if (lv.name != null) {
                TextRenderer lv3 = MinecraftClient.getInstance().textRenderer;
                float g = lv3.getWidth(lv.name);
                float f = 25.0f / g;
                Objects.requireNonNull(lv3);
                float h = MathHelper.clamp(f, 0.0f, 6.0f / 9.0f);
                matrices.push();
                matrices.translate((float)lv.x / 2.0f + 64.0f - g * h / 2.0f, (float)lv.z / 2.0f + 64.0f + 4.0f, -0.025f);
                matrices.scale(h, h, -1.0f);
                matrices.translate(0.0f, 0.0f, 0.1f);
                queue.getBatchingQueue(1).submitText(matrices, 0.0f, 0.0f, lv.name.asOrderedText(), false, TextRenderer.TextLayerType.NORMAL, light, -1, Integer.MIN_VALUE, 0);
                matrices.pop();
            }
            ++j;
        }
    }

    public void update(MapIdComponent mapId, MapState mapState, MapRenderState renderState) {
        renderState.texture = this.textureManager.getTextureId(mapId, mapState);
        renderState.decorations.clear();
        for (MapDecoration lv : mapState.getDecorations()) {
            renderState.decorations.add(this.createDecoration(lv));
        }
    }

    private MapRenderState.Decoration createDecoration(MapDecoration decoration) {
        MapRenderState.Decoration lv = new MapRenderState.Decoration();
        lv.sprite = this.decorationsAtlasManager.getSprite(decoration.getAssetId());
        lv.x = decoration.x();
        lv.z = decoration.z();
        lv.rotation = decoration.rotation();
        lv.name = decoration.name().orElse(null);
        lv.alwaysRendered = decoration.isAlwaysRendered();
        return lv;
    }
}

