/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text.object;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.object.TextObjectContents;
import net.minecraft.util.Atlases;
import net.minecraft.util.Identifier;

public record AtlasTextObjectContents(Identifier atlas, Identifier sprite) implements TextObjectContents
{
    public static final Identifier DEFAULT_ATLAS = Atlases.BLOCKS;
    public static final MapCodec<AtlasTextObjectContents> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Identifier.CODEC.optionalFieldOf("atlas", DEFAULT_ATLAS).forGetter(AtlasTextObjectContents::atlas), ((MapCodec)Identifier.CODEC.fieldOf("sprite")).forGetter(AtlasTextObjectContents::sprite)).apply((Applicative<AtlasTextObjectContents, ?>)instance, AtlasTextObjectContents::new));

    public MapCodec<AtlasTextObjectContents> getCodec() {
        return CODEC;
    }

    @Override
    public StyleSpriteSource spriteSource() {
        return new StyleSpriteSource.Sprite(this.atlas, this.sprite);
    }

    private static String getShortIdString(Identifier id) {
        return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
    }

    @Override
    public String asText() {
        String string = AtlasTextObjectContents.getShortIdString(this.sprite);
        if (this.atlas.equals(DEFAULT_ATLAS)) {
            return "[" + string + "]";
        }
        return "[" + string + "@" + AtlasTextObjectContents.getShortIdString(this.atlas) + "]";
    }
}

