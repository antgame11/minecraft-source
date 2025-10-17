/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.util.Identifier;
import net.minecraft.util.packrat.ParsingRule;
import net.minecraft.util.packrat.ParsingState;
import org.jetbrains.annotations.Nullable;

public class AnyIdParsingRule
implements ParsingRule<StringReader, Identifier> {
    public static final ParsingRule<StringReader, Identifier> INSTANCE = new AnyIdParsingRule();

    private AnyIdParsingRule() {
    }

    @Override
    @Nullable
    public Identifier parse(ParsingState<StringReader> arg) {
        arg.getReader().skipWhitespace();
        try {
            return Identifier.fromCommandInputNonEmpty(arg.getReader());
        } catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    @Override
    @Nullable
    public /* synthetic */ Object parse(ParsingState state) {
        return this.parse(state);
    }
}

