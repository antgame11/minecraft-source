/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item.model;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.ContextSwapper;
import net.minecraft.util.HeldItemContext;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ItemModel {
    public void update(ItemRenderState var1, ItemStack var2, ItemModelManager var3, ItemDisplayContext var4, @Nullable ClientWorld var5, @Nullable HeldItemContext var6, int var7);

    @Environment(value=EnvType.CLIENT)
    public record BakeContext(Baker blockModelBaker, LoadedEntityModels entityModelSet, SpriteHolder spriteHolder, PlayerSkinCache playerSkinRenderCache, ItemModel missingItemModel, @Nullable ContextSwapper contextSwapper) implements SpecialModelRenderer.BakeContext
    {
        @Nullable
        public ContextSwapper contextSwapper() {
            return this.contextSwapper;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Unbaked
    extends ResolvableModel {
        public MapCodec<? extends Unbaked> getCodec();

        public ItemModel bake(BakeContext var1);
    }
}

