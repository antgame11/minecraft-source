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
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

@Environment(value=EnvType.CLIENT)
public final class ItemGuiElementRenderState
implements GuiElementRenderState {
    private final String name;
    private final Matrix3x2f pose;
    private final KeyedItemRenderState state;
    private final int x;
    private final int y;
    @Nullable
    private final ScreenRect scissorArea;
    @Nullable
    private final ScreenRect oversizedBounds;
    @Nullable
    private final ScreenRect bounds;

    public ItemGuiElementRenderState(String name, Matrix3x2f pose, KeyedItemRenderState state, int x, int y, @Nullable ScreenRect scissor) {
        this.name = name;
        this.pose = pose;
        this.state = state;
        this.x = x;
        this.y = y;
        this.scissorArea = scissor;
        this.oversizedBounds = this.state().isOversizedInGui() ? this.createOversizedBounds() : null;
        this.bounds = this.createBounds(this.oversizedBounds != null ? this.oversizedBounds : new ScreenRect(this.x, this.y, 16, 16));
    }

    @Nullable
    private ScreenRect createOversizedBounds() {
        Box lv = this.state.getModelBoundingBox();
        int i = MathHelper.ceil(lv.getLengthX() * 16.0);
        int j = MathHelper.ceil(lv.getLengthY() * 16.0);
        if (i > 16 || j > 16) {
            float f = (float)(lv.minX * 16.0);
            float g = (float)(lv.maxY * 16.0);
            int k = MathHelper.floor(f);
            int l = MathHelper.floor(g);
            int m = this.x + k + 8;
            int n = this.y - l + 8;
            return new ScreenRect(m, n, i, j);
        }
        return null;
    }

    @Nullable
    private ScreenRect createBounds(ScreenRect rect) {
        ScreenRect lv = rect.transformEachVertex(this.pose);
        return this.scissorArea != null ? this.scissorArea.intersection(lv) : lv;
    }

    public String name() {
        return this.name;
    }

    public Matrix3x2f pose() {
        return this.pose;
    }

    public KeyedItemRenderState state() {
        return this.state;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    @Nullable
    public ScreenRect scissorArea() {
        return this.scissorArea;
    }

    @Nullable
    public ScreenRect oversizedBounds() {
        return this.oversizedBounds;
    }

    @Override
    @Nullable
    public ScreenRect bounds() {
        return this.bounds;
    }
}

