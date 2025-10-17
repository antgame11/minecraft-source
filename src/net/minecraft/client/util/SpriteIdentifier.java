/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SpriteIdentifier {
    public static final Comparator<SpriteIdentifier> COMPARATOR = Comparator.comparing(SpriteIdentifier::getAtlasId).thenComparing(SpriteIdentifier::getTextureId);
    private final Identifier atlas;
    private final Identifier texture;
    @Nullable
    private RenderLayer layer;

    public SpriteIdentifier(Identifier atlas, Identifier texture) {
        this.atlas = atlas;
        this.texture = texture;
    }

    public Identifier getAtlasId() {
        return this.atlas;
    }

    public Identifier getTextureId() {
        return this.texture;
    }

    public RenderLayer getRenderLayer(Function<Identifier, RenderLayer> layerFactory) {
        if (this.layer == null) {
            this.layer = layerFactory.apply(this.atlas);
        }
        return this.layer;
    }

    public VertexConsumer getVertexConsumer(SpriteHolder arg, VertexConsumerProvider arg2, Function<Identifier, RenderLayer> function) {
        return arg.getSprite(this).getTextureSpecificVertexConsumer(arg2.getBuffer(this.getRenderLayer(function)));
    }

    public VertexConsumer getVertexConsumer(SpriteHolder arg, VertexConsumerProvider arg2, Function<Identifier, RenderLayer> function, boolean bl, boolean bl2) {
        return arg.getSprite(this).getTextureSpecificVertexConsumer(ItemRenderer.getItemGlintConsumer(arg2, this.getRenderLayer(function), bl, bl2));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SpriteIdentifier lv = (SpriteIdentifier)o;
        return this.atlas.equals(lv.atlas) && this.texture.equals(lv.texture);
    }

    public int hashCode() {
        return Objects.hash(this.atlas, this.texture);
    }

    public String toString() {
        return "Material{atlasLocation=" + String.valueOf(this.atlas) + ", texture=" + String.valueOf(this.texture) + "}";
    }
}

