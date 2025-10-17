/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.StringReader;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.Suggestable;

public interface IdentifierSuggestable
extends Suggestable<StringReader> {
    public Stream<Identifier> possibleIds();

    @Override
    default public Stream<String> possibleValues(ParsingState<StringReader> arg) {
        return this.possibleIds().map(Identifier::toString);
    }
}

