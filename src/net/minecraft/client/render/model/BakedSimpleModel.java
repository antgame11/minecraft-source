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
import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.Geometry;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.SimpleModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface BakedSimpleModel
extends SimpleModel {
    public static final boolean DEFAULT_AMBIENT_OCCLUSION = true;
    public static final UnbakedModel.GuiLight DEFAULT_GUI_LIGHT = UnbakedModel.GuiLight.BLOCK;

    public UnbakedModel getModel();

    @Nullable
    public BakedSimpleModel getParent();

    public static ModelTextures getTextures(BakedSimpleModel model) {
        ModelTextures.Builder lv2 = new ModelTextures.Builder();
        for (BakedSimpleModel lv = model; lv != null; lv = lv.getParent()) {
            lv2.addLast(lv.getModel().textures());
        }
        return lv2.build(model);
    }

    default public ModelTextures getTextures() {
        return BakedSimpleModel.getTextures(this);
    }

    public static boolean getAmbientOcclusion(BakedSimpleModel model) {
        while (model != null) {
            Boolean boolean_ = model.getModel().ambientOcclusion();
            if (boolean_ != null) {
                return boolean_;
            }
            model = model.getParent();
        }
        return true;
    }

    default public boolean getAmbientOcclusion() {
        return BakedSimpleModel.getAmbientOcclusion(this);
    }

    public static UnbakedModel.GuiLight getGuiLight(BakedSimpleModel model) {
        while (model != null) {
            UnbakedModel.GuiLight lv = model.getModel().guiLight();
            if (lv != null) {
                return lv;
            }
            model = model.getParent();
        }
        return DEFAULT_GUI_LIGHT;
    }

    default public UnbakedModel.GuiLight getGuiLight() {
        return BakedSimpleModel.getGuiLight(this);
    }

    public static Geometry getGeometry(BakedSimpleModel model) {
        while (model != null) {
            Geometry lv = model.getModel().geometry();
            if (lv != null) {
                return lv;
            }
            model = model.getParent();
        }
        return Geometry.EMPTY;
    }

    default public Geometry getGeometry() {
        return BakedSimpleModel.getGeometry(this);
    }

    default public BakedGeometry bakeGeometry(ModelTextures textures, Baker baker, ModelBakeSettings settings) {
        return this.getGeometry().bake(textures, baker, settings, this);
    }

    public static Sprite getParticleTexture(ModelTextures textures, Baker baker, SimpleModel model) {
        return baker.getSpriteGetter().get(textures, "particle", model);
    }

    default public Sprite getParticleTexture(ModelTextures textures, Baker baker) {
        return BakedSimpleModel.getParticleTexture(textures, baker, this);
    }

    public static Transformation extractTransformation(BakedSimpleModel model, ItemDisplayContext mode) {
        while (model != null) {
            Transformation lv2;
            ModelTransformation lv = model.getModel().transformations();
            if (lv != null && (lv2 = lv.getTransformation(mode)) != Transformation.IDENTITY) {
                return lv2;
            }
            model = model.getParent();
        }
        return Transformation.IDENTITY;
    }

    public static ModelTransformation copyTransformations(BakedSimpleModel model) {
        Transformation lv = BakedSimpleModel.extractTransformation(model, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        Transformation lv2 = BakedSimpleModel.extractTransformation(model, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        Transformation lv3 = BakedSimpleModel.extractTransformation(model, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        Transformation lv4 = BakedSimpleModel.extractTransformation(model, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        Transformation lv5 = BakedSimpleModel.extractTransformation(model, ItemDisplayContext.HEAD);
        Transformation lv6 = BakedSimpleModel.extractTransformation(model, ItemDisplayContext.GUI);
        Transformation lv7 = BakedSimpleModel.extractTransformation(model, ItemDisplayContext.GROUND);
        Transformation lv8 = BakedSimpleModel.extractTransformation(model, ItemDisplayContext.FIXED);
        Transformation lv9 = BakedSimpleModel.extractTransformation(model, ItemDisplayContext.ON_SHELF);
        return new ModelTransformation(lv, lv2, lv3, lv4, lv5, lv6, lv7, lv8, lv9);
    }

    default public ModelTransformation getTransformations() {
        return BakedSimpleModel.copyTransformations(this);
    }
}

