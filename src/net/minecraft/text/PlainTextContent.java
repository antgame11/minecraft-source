/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextContent;

public interface PlainTextContent
extends TextContent {
    public static final MapCodec<PlainTextContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("text")).forGetter(PlainTextContent::string)).apply((Applicative<PlainTextContent, ?>)instance, PlainTextContent::of));
    public static final PlainTextContent EMPTY = new PlainTextContent(){

        public String toString() {
            return "empty";
        }

        @Override
        public String string() {
            return "";
        }
    };

    public static PlainTextContent of(String string) {
        return string.isEmpty() ? EMPTY : new Literal(string);
    }

    public String string();

    default public MapCodec<PlainTextContent> getCodec() {
        return CODEC;
    }

    public record Literal(String string) implements PlainTextContent
    {
        @Override
        public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
            return visitor.accept(this.string);
        }

        @Override
        public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
            return visitor.accept(style, this.string);
        }

        @Override
        public String toString() {
            return "literal{" + this.string + "}";
        }
    }
}

