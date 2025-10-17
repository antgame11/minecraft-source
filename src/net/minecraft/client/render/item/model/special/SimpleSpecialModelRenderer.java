/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model.special;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface SimpleSpecialModelRenderer
extends SpecialModelRenderer<Void> {
    @Override
    @Nullable
    default public Void getData(ItemStack arg) {
        return null;
    }

    @Override
    default public void render(@Nullable Void void_, ItemDisplayContext arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, int i, int j, boolean bl, int k) {
        this.render(arg, arg2, arg3, i, j, bl, k);
    }

    public void render(ItemDisplayContext var1, MatrixStack var2, OrderedRenderCommandQueue var3, int var4, int var5, boolean var6, int var7);

    @Override
    @Nullable
    default public /* synthetic */ Object getData(ItemStack stack) {
        return this.getData(stack);
    }
}

