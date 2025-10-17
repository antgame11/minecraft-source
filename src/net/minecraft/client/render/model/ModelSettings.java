/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemDisplayContext;

@Environment(value=EnvType.CLIENT)
public record ModelSettings(boolean usesBlockLight, Sprite particleIcon, ModelTransformation transforms) {
    public static ModelSettings resolveSettings(Baker baker, BakedSimpleModel model, ModelTextures textures) {
        Sprite lv = model.getParticleTexture(textures, baker);
        return new ModelSettings(model.getGuiLight().isSide(), lv, model.getTransformations());
    }

    public void addSettings(ItemRenderState.LayerRenderState state, ItemDisplayContext mode) {
        state.setUseLight(this.usesBlockLight);
        state.setParticle(this.particleIcon);
        state.setTransform(this.transforms.getTransformation(mode));
    }
}

