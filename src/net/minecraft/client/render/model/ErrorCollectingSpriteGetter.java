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
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.SimpleModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

@Environment(value=EnvType.CLIENT)
public interface ErrorCollectingSpriteGetter {
    public Sprite get(SpriteIdentifier var1, SimpleModel var2);

    public Sprite getMissing(String var1, SimpleModel var2);

    default public Sprite get(ModelTextures texture, String name, SimpleModel model) {
        SpriteIdentifier lv = texture.get(name);
        return lv != null ? this.get(lv, model) : this.getMissing(name, model);
    }
}

