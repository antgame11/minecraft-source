/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.util.packrat.CursorExceptionType;
import net.minecraft.util.packrat.ParsingRule;
import net.minecraft.util.packrat.ParsingState;
import org.jetbrains.annotations.Nullable;

public abstract class NumeralParsingRule
implements ParsingRule<StringReader, String> {
    private final CursorExceptionType<CommandSyntaxException> invalidCharException;
    private final CursorExceptionType<CommandSyntaxException> unexpectedUnderscoreException;

    public NumeralParsingRule(CursorExceptionType<CommandSyntaxException> invalidCharException, CursorExceptionType<CommandSyntaxException> unexpectedUnderscoreException) {
        this.invalidCharException = invalidCharException;
        this.unexpectedUnderscoreException = unexpectedUnderscoreException;
    }

    @Override
    @Nullable
    public String parse(ParsingState<StringReader> arg) {
        int i;
        int j;
        StringReader stringReader = arg.getReader();
        stringReader.skipWhitespace();
        String string = stringReader.getString();
        for (j = i = stringReader.getCursor(); j < string.length() && this.accepts(string.charAt(j)); ++j) {
        }
        int k = j - i;
        if (k == 0) {
            arg.getErrors().add(arg.getCursor(), this.invalidCharException);
            return null;
        }
        if (string.charAt(i) == '_' || string.charAt(j - 1) == '_') {
            arg.getErrors().add(arg.getCursor(), this.unexpectedUnderscoreException);
            return null;
        }
        stringReader.setCursor(j);
        return string.substring(i, j);
    }

    protected abstract boolean accepts(char var1);

    @Override
    @Nullable
    public /* synthetic */ Object parse(ParsingState state) {
        return this.parse(state);
    }
}

