/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.render.state.special;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

@Environment(value=EnvType.CLIENT)
public record OversizedItemGuiElementRenderState(ItemGuiElementRenderState guiItemRenderState, int x1, int y1, int x2, int y2) implements SpecialGuiElementRenderState
{
    @Override
    public float scale() {
        return 16.0f;
    }

    @Override
    public Matrix3x2f pose() {
        return this.guiItemRenderState.pose();
    }

    @Override
    @Nullable
    public ScreenRect scissorArea() {
        return this.guiItemRenderState.scissorArea();
    }

    @Override
    @Nullable
    public ScreenRect bounds() {
        return this.guiItemRenderState.bounds();
    }
}

