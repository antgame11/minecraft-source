/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record GeometryBakedModel(BakedGeometry quads, boolean useAmbientOcclusion, Sprite particleSprite) implements BlockModelPart
{
    public static GeometryBakedModel create(Baker baker, Identifier id, ModelBakeSettings bakeSettings) {
        BakedSimpleModel lv = baker.getModel(id);
        ModelTextures lv2 = lv.getTextures();
        boolean bl = lv.getAmbientOcclusion();
        Sprite lv3 = lv.getParticleTexture(lv2, baker);
        BakedGeometry lv4 = lv.bakeGeometry(lv2, baker, bakeSettings);
        return new GeometryBakedModel(lv4, bl, lv3);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction side) {
        return this.quads.getQuads(side);
    }
}

