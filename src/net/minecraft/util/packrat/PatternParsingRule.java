/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.packrat.CursorExceptionType;
import net.minecraft.util.packrat.ParsingRule;
import net.minecraft.util.packrat.ParsingState;

public final class PatternParsingRule
implements ParsingRule<StringReader, String> {
    private final Pattern pattern;
    private final CursorExceptionType<CommandSyntaxException> exception;

    public PatternParsingRule(Pattern pattern, CursorExceptionType<CommandSyntaxException> exception) {
        this.pattern = pattern;
        this.exception = exception;
    }

    @Override
    public String parse(ParsingState<StringReader> arg) {
        StringReader stringReader = arg.getReader();
        String string = stringReader.getString();
        Matcher matcher = this.pattern.matcher(string).region(stringReader.getCursor(), string.length());
        if (!matcher.lookingAt()) {
            arg.getErrors().add(arg.getCursor(), this.exception);
            return null;
        }
        stringReader.setCursor(matcher.end());
        return matcher.group(0);
    }

    @Override
    public /* synthetic */ Object parse(ParsingState state) {
        return this.parse(state);
    }
}

