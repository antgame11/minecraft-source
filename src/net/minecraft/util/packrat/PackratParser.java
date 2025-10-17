/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.util.packrat.CursorExceptionType;
import net.minecraft.util.packrat.IdentifierSuggestable;
import net.minecraft.util.packrat.ParseError;
import net.minecraft.util.packrat.ParseErrorList;
import net.minecraft.util.packrat.Parser;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingRules;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.ReaderBackedParsingState;
import net.minecraft.util.packrat.Suggestable;

public record PackratParser<T>(ParsingRules<StringReader> rules, ParsingRuleEntry<StringReader, T> top) implements Parser<T>
{
    public PackratParser {
        arg.ensureBound();
    }

    public Optional<T> startParsing(ParsingState<StringReader> state) {
        return state.startParsing(this.top);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        Object r;
        ParseErrorList.Impl<StringReader> lv = new ParseErrorList.Impl<StringReader>();
        ReaderBackedParsingState lv2 = new ReaderBackedParsingState(lv, reader);
        Optional<T> optional = this.startParsing(lv2);
        if (optional.isPresent()) {
            return optional.get();
        }
        List<ParseError<StringReader>> list = lv.getErrors();
        List list2 = list.stream().mapMulti((error, callback) -> {
            Object object = error.reason();
            if (object instanceof CursorExceptionType) {
                CursorExceptionType lv = (CursorExceptionType)object;
                callback.accept(lv.create(reader.getString(), error.cursor()));
            } else {
                object = error.reason();
                if (object instanceof Exception) {
                    Exception exception = (Exception)object;
                    callback.accept(exception);
                }
            }
        }).toList();
        for (Exception exception : list2) {
            if (!(exception instanceof CommandSyntaxException)) continue;
            CommandSyntaxException commandSyntaxException = (CommandSyntaxException)exception;
            throw commandSyntaxException;
        }
        if (list2.size() == 1 && (r = list2.get(0)) instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException)r;
            throw runtimeException;
        }
        throw new IllegalStateException("Failed to parse: " + list.stream().map(ParseError::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        ParseErrorList.Impl<StringReader> lv = new ParseErrorList.Impl<StringReader>();
        ReaderBackedParsingState lv2 = new ReaderBackedParsingState(lv, stringReader);
        this.startParsing(lv2);
        List<ParseError<StringReader>> list = lv.getErrors();
        if (list.isEmpty()) {
            return builder.buildFuture();
        }
        SuggestionsBuilder suggestionsBuilder2 = builder.createOffset(lv.getCursor());
        for (ParseError<StringReader> lv3 : list) {
            Suggestable<StringReader> suggestable = lv3.suggestions();
            if (suggestable instanceof IdentifierSuggestable) {
                IdentifierSuggestable lv4 = (IdentifierSuggestable)suggestable;
                CommandSource.suggestIdentifiers(lv4.possibleIds(), suggestionsBuilder2);
                continue;
            }
            CommandSource.suggestMatching(lv3.suggestions().possibleValues(lv2), suggestionsBuilder2);
        }
        return suggestionsBuilder2.buildFuture();
    }
}

