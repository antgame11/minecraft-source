/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.render.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.text.OrderedText;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

@Environment(value=EnvType.CLIENT)
public final class TextGuiElementRenderState
implements GuiElementRenderState {
    public final TextRenderer textRenderer;
    public final OrderedText orderedText;
    public final Matrix3x2f matrix;
    public final int x;
    public final int y;
    public final int color;
    public final int backgroundColor;
    public final boolean shadow;
    @Nullable
    public final ScreenRect clipBounds;
    @Nullable
    private TextRenderer.GlyphDrawable preparation;
    @Nullable
    private ScreenRect bounds;

    public TextGuiElementRenderState(TextRenderer textRenderer, OrderedText orderedText, Matrix3x2f matrix, int x, int y, int color, int backgroundColor, boolean shadow, @Nullable ScreenRect clipBounds) {
        this.textRenderer = textRenderer;
        this.orderedText = orderedText;
        this.matrix = matrix;
        this.x = x;
        this.y = y;
        this.color = color;
        this.backgroundColor = backgroundColor;
        this.shadow = shadow;
        this.clipBounds = clipBounds;
    }

    public TextRenderer.GlyphDrawable prepare() {
        if (this.preparation == null) {
            this.preparation = this.textRenderer.prepare(this.orderedText, (float)this.x, (float)this.y, this.color, this.shadow, this.backgroundColor);
            ScreenRect lv = this.preparation.getScreenRect();
            if (lv != null) {
                lv = lv.transformEachVertex(this.matrix);
                this.bounds = this.clipBounds != null ? this.clipBounds.intersection(lv) : lv;
            }
        }
        return this.preparation;
    }

    @Override
    @Nullable
    public ScreenRect bounds() {
        this.prepare();
        return this.bounds;
    }
}

