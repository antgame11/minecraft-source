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
import net.minecraft.client.render.model.Geometry;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface UnbakedModel {
    public static final String PARTICLE_TEXTURE = "particle";

    @Nullable
    default public Boolean ambientOcclusion() {
        return null;
    }

    @Nullable
    default public GuiLight guiLight() {
        return null;
    }

    @Nullable
    default public ModelTransformation transformations() {
        return null;
    }

    default public ModelTextures.Textures textures() {
        return ModelTextures.Textures.EMPTY;
    }

    @Nullable
    default public Geometry geometry() {
        return null;
    }

    @Nullable
    default public Identifier parent() {
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum GuiLight {
        ITEM("front"),
        BLOCK("side");

        private final String name;

        private GuiLight(String name) {
            this.name = name;
        }

        public static GuiLight byName(String value) {
            for (GuiLight lv : GuiLight.values()) {
                if (!lv.name.equals(value)) continue;
                return lv;
            }
            throw new IllegalArgumentException("Invalid gui light: " + value);
        }

        public boolean isSide() {
            return this == BLOCK;
        }
    }
}

