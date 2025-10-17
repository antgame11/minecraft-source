/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TooltipBackgroundRenderer {
    private static final Identifier DEFAULT_BACKGROUND_TEXTURE = Identifier.ofVanilla("tooltip/background");
    private static final Identifier DEFAULT_FRAME_TEXTURE = Identifier.ofVanilla("tooltip/frame");
    public static final int field_41688 = 12;
    private static final int field_41693 = 3;
    public static final int field_41689 = 3;
    public static final int field_41690 = 3;
    public static final int field_41691 = 3;
    public static final int field_41692 = 3;
    private static final int field_54153 = 9;

    public static void render(DrawContext context, int x, int y, int width, int height, @Nullable Identifier texture) {
        int m = x - 3 - 9;
        int n = y - 3 - 9;
        int o = width + 3 + 3 + 18;
        int p = height + 3 + 3 + 18;
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TooltipBackgroundRenderer.getBackgroundTexture(texture), m, n, o, p);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TooltipBackgroundRenderer.getFrameTexture(texture), m, n, o, p);
    }

    private static Identifier getBackgroundTexture(@Nullable Identifier texture) {
        if (texture == null) {
            return DEFAULT_BACKGROUND_TEXTURE;
        }
        return texture.withPath(name -> "tooltip/" + name + "_background");
    }

    private static Identifier getFrameTexture(@Nullable Identifier texture) {
        if (texture == null) {
            return DEFAULT_FRAME_TEXTURE;
        }
        return texture.withPath(name -> "tooltip/" + name + "_frame");
    }
}

