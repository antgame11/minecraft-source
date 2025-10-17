/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.render.state.special;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.util.profiler.ProfilerTiming;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ProfilerChartGuiElementRenderState(List<ProfilerTiming> chartData, int x1, int y1, int x2, int y2, @Nullable ScreenRect scissorArea, @Nullable ScreenRect bounds) implements SpecialGuiElementRenderState
{
    public ProfilerChartGuiElementRenderState(List<ProfilerTiming> chartData, int x1, int y1, int x2, int y2, @Nullable ScreenRect scissorArea) {
        this(chartData, x1, y1, x2, y2, scissorArea, SpecialGuiElementRenderState.createBounds(x1, y1, x2, y2, scissorArea));
    }

    @Override
    public float scale() {
        return 1.0f;
    }

    @Override
    @Nullable
    public ScreenRect scissorArea() {
        return this.scissorArea;
    }

    @Override
    @Nullable
    public ScreenRect bounds() {
        return this.bounds;
    }
}

