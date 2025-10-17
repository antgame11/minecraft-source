/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedBytes;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.minecraft.nbt.SnbtOperation;
import net.minecraft.text.Text;
import net.minecraft.util.packrat.CursorExceptionType;
import net.minecraft.util.packrat.Literals;
import net.minecraft.util.packrat.NumeralParsingRule;
import net.minecraft.util.packrat.PackratParser;
import net.minecraft.util.packrat.ParseResults;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingRules;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.PatternParsingRule;
import net.minecraft.util.packrat.Symbol;
import net.minecraft.util.packrat.Term;
import net.minecraft.util.packrat.TokenParsingRule;
import net.minecraft.util.packrat.UnquotedStringParsingRule;
import org.jetbrains.annotations.Nullable;

public class SnbtParsing {
    private static final DynamicCommandExceptionType NUMBER_PARSE_FAILURE_EXCEPTION = new DynamicCommandExceptionType(value -> Text.stringifiedTranslatable("snbt.parser.number_parse_failure", value));
    static final DynamicCommandExceptionType EXPECTED_HEX_ESCAPE_EXCEPTION = new DynamicCommandExceptionType(length -> Text.stringifiedTranslatable("snbt.parser.expected_hex_escape", length));
    private static final DynamicCommandExceptionType INVALID_CODEPOINT_EXCEPTION = new DynamicCommandExceptionType(value -> Text.stringifiedTranslatable("snbt.parser.invalid_codepoint", value));
    private static final DynamicCommandExceptionType NO_SUCH_OPERATION_EXCEPTION = new DynamicCommandExceptionType(operation -> Text.stringifiedTranslatable("snbt.parser.no_such_operation", operation));
    static final CursorExceptionType<CommandSyntaxException> EXPECTED_INTEGER_TYPE_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.expected_integer_type")));
    private static final CursorExceptionType<CommandSyntaxException> EXPECTED_FLOAT_TYPE_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.expected_float_type")));
    static final CursorExceptionType<CommandSyntaxException> EXPECTED_NON_NEGATIVE_NUMBER_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.expected_non_negative_number")));
    private static final CursorExceptionType<CommandSyntaxException> INVALID_CHARACTER_NAME_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.invalid_character_name")));
    static final CursorExceptionType<CommandSyntaxException> INVALID_ARRAY_ELEMENT_TYPE_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.invalid_array_element_type")));
    private static final CursorExceptionType<CommandSyntaxException> INVALID_UNQUOTED_START_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.invalid_unquoted_start")));
    private static final CursorExceptionType<CommandSyntaxException> EXPECTED_UNQUOTED_STRING_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.expected_unquoted_string")));
    private static final CursorExceptionType<CommandSyntaxException> INVALID_STRING_CONTENTS_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.invalid_string_contents")));
    private static final CursorExceptionType<CommandSyntaxException> EXPECTED_BINARY_NUMERAL_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.expected_binary_numeral")));
    private static final CursorExceptionType<CommandSyntaxException> UNDERSCORE_NOT_ALLOWED_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.underscore_not_allowed")));
    private static final CursorExceptionType<CommandSyntaxException> EXPECTED_DECIMAL_NUMERAL_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.expected_decimal_numeral")));
    private static final CursorExceptionType<CommandSyntaxException> EXPECTED_HEX_NUMERAL_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.expected_hex_numeral")));
    private static final CursorExceptionType<CommandSyntaxException> EMPTY_KEY_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.empty_key")));
    private static final CursorExceptionType<CommandSyntaxException> LEADING_ZERO_NOT_ALLOWED_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.leading_zero_not_allowed")));
    private static final CursorExceptionType<CommandSyntaxException> INFINITY_NOT_ALLOWED_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType(Text.translatable("snbt.parser.infinity_not_allowed")));
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();
    private static final NumeralParsingRule BINARY_RULE = new NumeralParsingRule((CursorExceptionType)EXPECTED_BINARY_NUMERAL_EXCEPTION, (CursorExceptionType)UNDERSCORE_NOT_ALLOWED_EXCEPTION){

        @Override
        protected boolean accepts(char c) {
            return switch (c) {
                case '0', '1', '_' -> true;
                default -> false;
            };
        }
    };
    private static final NumeralParsingRule DECIMAL_RULE = new NumeralParsingRule((CursorExceptionType)EXPECTED_DECIMAL_NUMERAL_EXCEPTION, (CursorExceptionType)UNDERSCORE_NOT_ALLOWED_EXCEPTION){

        @Override
        protected boolean accepts(char c) {
            return switch (c) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' -> true;
                default -> false;
            };
        }
    };
    private static final NumeralParsingRule HEX_RULE = new NumeralParsingRule((CursorExceptionType)EXPECTED_HEX_NUMERAL_EXCEPTION, (CursorExceptionType)UNDERSCORE_NOT_ALLOWED_EXCEPTION){

        @Override
        protected boolean accepts(char c) {
            return switch (c) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', '_', 'a', 'b', 'c', 'd', 'e', 'f' -> true;
                default -> false;
            };
        }
    };
    private static final TokenParsingRule UNQUOTED_STRING_RULE = new TokenParsingRule(1, (CursorExceptionType)INVALID_STRING_CONTENTS_EXCEPTION){

        @Override
        protected boolean isValidChar(char c) {
            return switch (c) {
                case '\"', '\'', '\\' -> false;
                default -> true;
            };
        }
    };
    private static final Literals.CharacterLiteral DECIMAL_CHAR = new Literals.CharacterLiteral(CharList.of()){

        @Override
        protected boolean accepts(char c) {
            return SnbtParsing.isPartOfDecimal(c);
        }
    };
    private static final Pattern UNICODE_NAME_PATTERN = Pattern.compile("[-a-zA-Z0-9 ]+");

    static CursorExceptionType<CommandSyntaxException> toNumberParseFailure(NumberFormatException exception) {
        return CursorExceptionType.create(NUMBER_PARSE_FAILURE_EXCEPTION, exception.getMessage());
    }

    @Nullable
    public static String escapeSpecialChar(char c) {
        return switch (c) {
            case '\b' -> "b";
            case '\t' -> "t";
            case '\n' -> "n";
            case '\f' -> "f";
            case '\r' -> "r";
            default -> c < ' ' ? "x" + HEX_FORMAT.toHexDigits((byte)c) : null;
        };
    }

    private static boolean canUnquotedStringStartWith(char c) {
        return !SnbtParsing.isPartOfDecimal(c);
    }

    static boolean isPartOfDecimal(char c) {
        return switch (c) {
            case '+', '-', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> true;
            default -> false;
        };
    }

    static boolean containsUnderscore(String string) {
        return string.indexOf(95) != -1;
    }

    private static void skipUnderscoreAndAppend(StringBuilder builder, String value) {
        SnbtParsing.append(builder, value, SnbtParsing.containsUnderscore(value));
    }

    static void append(StringBuilder builder, String value, boolean skipUnderscore) {
        if (skipUnderscore) {
            for (char c : value.toCharArray()) {
                if (c == '_') continue;
                builder.append(c);
            }
        } else {
            builder.append(value);
        }
    }

    static short parseUnsignedShort(String value, int radix) {
        int j = Integer.parseInt(value, radix);
        if (j >> 16 == 0) {
            return (short)j;
        }
        throw new NumberFormatException("out of range: " + j);
    }

    @Nullable
    private static <T> T decodeFloat(DynamicOps<T> ops, Sign sign, @Nullable String intPart, @Nullable String fractionalPart, @Nullable SignedValue<String> exponent, @Nullable NumericType type, ParsingState<?> state) {
        StringBuilder stringBuilder = new StringBuilder();
        sign.append(stringBuilder);
        if (intPart != null) {
            SnbtParsing.skipUnderscoreAndAppend(stringBuilder, intPart);
        }
        if (fractionalPart != null) {
            stringBuilder.append('.');
            SnbtParsing.skipUnderscoreAndAppend(stringBuilder, fractionalPart);
        }
        if (exponent != null) {
            stringBuilder.append('e');
            exponent.sign().append(stringBuilder);
            SnbtParsing.skipUnderscoreAndAppend(stringBuilder, (String)exponent.value);
        }
        try {
            String string3 = stringBuilder.toString();
            NumericType numericType = type;
            int n = 0;
            return switch (SwitchBootstraps.enumSwitch("enumSwitch", new Object[]{"FLOAT", "DOUBLE"}, (NumericType)numericType, n)) {
                case 0 -> SnbtParsing.parseFiniteFloat(ops, state, string3);
                case 1 -> SnbtParsing.parseFiniteDouble(ops, state, string3);
                case -1 -> SnbtParsing.parseFiniteDouble(ops, state, string3);
                default -> {
                    state.getErrors().add(state.getCursor(), EXPECTED_FLOAT_TYPE_EXCEPTION);
                    yield null;
                }
            };
        } catch (NumberFormatException numberFormatException) {
            state.getErrors().add(state.getCursor(), SnbtParsing.toNumberParseFailure(numberFormatException));
            return null;
        }
    }

    @Nullable
    private static <T> T parseFiniteFloat(DynamicOps<T> ops, ParsingState<?> state, String value) {
        float f = Float.parseFloat(value);
        if (!Float.isFinite(f)) {
            state.getErrors().add(state.getCursor(), INFINITY_NOT_ALLOWED_EXCEPTION);
            return null;
        }
        return ops.createFloat(f);
    }

    @Nullable
    private static <T> T parseFiniteDouble(DynamicOps<T> ops, ParsingState<?> state, String value) {
        double d = Double.parseDouble(value);
        if (!Double.isFinite(d)) {
            state.getErrors().add(state.getCursor(), INFINITY_NOT_ALLOWED_EXCEPTION);
            return null;
        }
        return ops.createDouble(d);
    }

    private static String join(List<String> values) {
        return switch (values.size()) {
            case 0 -> "";
            case 1 -> values.getFirst();
            default -> String.join((CharSequence)"", values);
        };
    }

    public static <T> PackratParser<T> createParser(DynamicOps<T> ops) {
        Object object = ops.createBoolean(true);
        Object object2 = ops.createBoolean(false);
        Object object3 = ops.emptyMap();
        Object object4 = ops.emptyList();
        ParsingRules<StringReader> lv = new ParsingRules<StringReader>();
        Symbol lv2 = Symbol.of("sign");
        lv.set(lv2, Term.anyOf(Term.sequence(Literals.character('+'), Term.always(lv2, Sign.PLUS)), Term.sequence(Literals.character('-'), Term.always(lv2, Sign.MINUS))), results -> (Sign)((Object)((Object)results.getOrThrow(lv2))));
        Symbol lv3 = Symbol.of("integer_suffix");
        lv.set(lv3, Term.anyOf(Term.sequence(Literals.character('u', 'U'), Term.anyOf(Term.sequence(Literals.character('b', 'B'), Term.always(lv3, new NumberSuffix(Signedness.UNSIGNED, NumericType.BYTE))), Term.sequence(Literals.character('s', 'S'), Term.always(lv3, new NumberSuffix(Signedness.UNSIGNED, NumericType.SHORT))), Term.sequence(Literals.character('i', 'I'), Term.always(lv3, new NumberSuffix(Signedness.UNSIGNED, NumericType.INT))), Term.sequence(Literals.character('l', 'L'), Term.always(lv3, new NumberSuffix(Signedness.UNSIGNED, NumericType.LONG))))), Term.sequence(Literals.character('s', 'S'), Term.anyOf(Term.sequence(Literals.character('b', 'B'), Term.always(lv3, new NumberSuffix(Signedness.SIGNED, NumericType.BYTE))), Term.sequence(Literals.character('s', 'S'), Term.always(lv3, new NumberSuffix(Signedness.SIGNED, NumericType.SHORT))), Term.sequence(Literals.character('i', 'I'), Term.always(lv3, new NumberSuffix(Signedness.SIGNED, NumericType.INT))), Term.sequence(Literals.character('l', 'L'), Term.always(lv3, new NumberSuffix(Signedness.SIGNED, NumericType.LONG))))), Term.sequence(Literals.character('b', 'B'), Term.always(lv3, new NumberSuffix(null, NumericType.BYTE))), Term.sequence(Literals.character('s', 'S'), Term.always(lv3, new NumberSuffix(null, NumericType.SHORT))), Term.sequence(Literals.character('i', 'I'), Term.always(lv3, new NumberSuffix(null, NumericType.INT))), Term.sequence(Literals.character('l', 'L'), Term.always(lv3, new NumberSuffix(null, NumericType.LONG)))), results -> (NumberSuffix)results.getOrThrow(lv3));
        Symbol lv4 = Symbol.of("binary_numeral");
        lv.set(lv4, BINARY_RULE);
        Symbol lv5 = Symbol.of("decimal_numeral");
        lv.set(lv5, DECIMAL_RULE);
        Symbol lv6 = Symbol.of("hex_numeral");
        lv.set(lv6, HEX_RULE);
        Symbol lv7 = Symbol.of("integer_literal");
        ParsingRuleEntry lv8 = lv.set(lv7, Term.sequence(Term.optional(lv.term(lv2)), Term.anyOf(Term.sequence(Literals.character('0'), Term.cutting(), Term.anyOf(Term.sequence(Literals.character('x', 'X'), Term.cutting(), lv.term(lv6)), Term.sequence(Literals.character('b', 'B'), lv.term(lv4)), Term.sequence(lv.term(lv5), Term.cutting(), Term.fail(LEADING_ZERO_NOT_ALLOWED_EXCEPTION)), Term.always(lv5, "0"))), lv.term(lv5)), Term.optional(lv.term(lv3))), results -> {
            NumberSuffix lv = results.getOrDefault(lv3, NumberSuffix.DEFAULT);
            Sign lv2 = results.getOrDefault(lv2, Sign.PLUS);
            String string = (String)results.get(lv5);
            if (string != null) {
                return new IntValue(lv2, Radix.DECIMAL, string, lv);
            }
            String string2 = (String)results.get(lv6);
            if (string2 != null) {
                return new IntValue(lv2, Radix.HEX, string2, lv);
            }
            String string3 = (String)results.getOrThrow(lv4);
            return new IntValue(lv2, Radix.BINARY, string3, lv);
        });
        Symbol lv9 = Symbol.of("float_type_suffix");
        lv.set(lv9, Term.anyOf(Term.sequence(Literals.character('f', 'F'), Term.always(lv9, NumericType.FLOAT)), Term.sequence(Literals.character('d', 'D'), Term.always(lv9, NumericType.DOUBLE))), results -> (NumericType)((Object)((Object)results.getOrThrow(lv9))));
        Symbol lv10 = Symbol.of("float_exponent_part");
        lv.set(lv10, Term.sequence(Literals.character('e', 'E'), Term.optional(lv.term(lv2)), lv.term(lv5)), results -> new SignedValue<String>(results.getOrDefault(lv2, Sign.PLUS), (String)results.getOrThrow(lv5)));
        Symbol lv11 = Symbol.of("float_whole_part");
        Symbol lv12 = Symbol.of("float_fraction_part");
        Symbol lv13 = Symbol.of("float_literal");
        lv.set(lv13, Term.sequence(Term.optional(lv.term(lv2)), Term.anyOf(Term.sequence(lv.term(lv5, lv11), Literals.character('.'), Term.cutting(), Term.optional(lv.term(lv5, lv12)), Term.optional(lv.term(lv10)), Term.optional(lv.term(lv9))), Term.sequence(Literals.character('.'), Term.cutting(), lv.term(lv5, lv12), Term.optional(lv.term(lv10)), Term.optional(lv.term(lv9))), Term.sequence(lv.term(lv5, lv11), lv.term(lv10), Term.cutting(), Term.optional(lv.term(lv9))), Term.sequence(lv.term(lv5, lv11), Term.optional(lv.term(lv10)), lv.term(lv9)))), state -> {
            ParseResults lv = state.getResults();
            Sign lv2 = lv.getOrDefault(lv2, Sign.PLUS);
            String string = (String)lv.get(lv11);
            String string2 = (String)lv.get(lv12);
            SignedValue lv3 = (SignedValue)lv.get(lv10);
            NumericType lv4 = (NumericType)((Object)((Object)lv.get(lv9)));
            return SnbtParsing.decodeFloat(ops, lv2, string, string2, lv3, lv4, state);
        });
        Symbol lv14 = Symbol.of("string_hex_2");
        lv.set(lv14, new HexParsingRule(2));
        Symbol lv15 = Symbol.of("string_hex_4");
        lv.set(lv15, new HexParsingRule(4));
        Symbol lv16 = Symbol.of("string_hex_8");
        lv.set(lv16, new HexParsingRule(8));
        Symbol lv17 = Symbol.of("string_unicode_name");
        lv.set(lv17, new PatternParsingRule(UNICODE_NAME_PATTERN, INVALID_CHARACTER_NAME_EXCEPTION));
        Symbol lv18 = Symbol.of("string_escape_sequence");
        lv.set(lv18, Term.anyOf(Term.sequence(Literals.character('b'), Term.always(lv18, "\b")), Term.sequence(Literals.character('s'), Term.always(lv18, " ")), Term.sequence(Literals.character('t'), Term.always(lv18, "\t")), Term.sequence(Literals.character('n'), Term.always(lv18, "\n")), Term.sequence(Literals.character('f'), Term.always(lv18, "\f")), Term.sequence(Literals.character('r'), Term.always(lv18, "\r")), Term.sequence(Literals.character('\\'), Term.always(lv18, "\\")), Term.sequence(Literals.character('\''), Term.always(lv18, "'")), Term.sequence(Literals.character('\"'), Term.always(lv18, "\"")), Term.sequence(Literals.character('x'), lv.term(lv14)), Term.sequence(Literals.character('u'), lv.term(lv15)), Term.sequence(Literals.character('U'), lv.term(lv16)), Term.sequence(Literals.character('N'), Literals.character('{'), lv.term(lv17), Literals.character('}'))), state -> {
            int j;
            ParseResults lv = state.getResults();
            String string = (String)lv.getAny(lv18);
            if (string != null) {
                return string;
            }
            String string2 = (String)lv.getAny(lv14, lv15, lv16);
            if (string2 != null) {
                int i = HexFormat.fromHexDigits(string2);
                if (!Character.isValidCodePoint(i)) {
                    state.getErrors().add(state.getCursor(), CursorExceptionType.create(INVALID_CODEPOINT_EXCEPTION, String.format(Locale.ROOT, "U+%08X", i)));
                    return null;
                }
                return Character.toString(i);
            }
            String string3 = (String)lv.getOrThrow(lv17);
            try {
                j = Character.codePointOf(string3);
            } catch (IllegalArgumentException illegalArgumentException) {
                state.getErrors().add(state.getCursor(), INVALID_CHARACTER_NAME_EXCEPTION);
                return null;
            }
            return Character.toString(j);
        });
        Symbol lv19 = Symbol.of("string_plain_contents");
        lv.set(lv19, UNQUOTED_STRING_RULE);
        Symbol lv20 = Symbol.of("string_chunks");
        Symbol lv21 = Symbol.of("string_contents");
        Symbol lv22 = Symbol.of("single_quoted_string_chunk");
        ParsingRuleEntry lv23 = lv.set(lv22, Term.anyOf(lv.term(lv19, lv21), Term.sequence(Literals.character('\\'), lv.term(lv18, lv21)), Term.sequence(Literals.character('\"'), Term.always(lv21, "\""))), results -> (String)results.getOrThrow(lv21));
        Symbol lv24 = Symbol.of("single_quoted_string_contents");
        lv.set(lv24, Term.repeated(lv23, lv20), results -> SnbtParsing.join((List)results.getOrThrow(lv20)));
        Symbol lv25 = Symbol.of("double_quoted_string_chunk");
        ParsingRuleEntry lv26 = lv.set(lv25, Term.anyOf(lv.term(lv19, lv21), Term.sequence(Literals.character('\\'), lv.term(lv18, lv21)), Term.sequence(Literals.character('\''), Term.always(lv21, "'"))), results -> (String)results.getOrThrow(lv21));
        Symbol lv27 = Symbol.of("double_quoted_string_contents");
        lv.set(lv27, Term.repeated(lv26, lv20), results -> SnbtParsing.join((List)results.getOrThrow(lv20)));
        Symbol lv28 = Symbol.of("quoted_string_literal");
        lv.set(lv28, Term.anyOf(Term.sequence(Literals.character('\"'), Term.cutting(), Term.optional(lv.term(lv27, lv21)), Literals.character('\"')), Term.sequence(Literals.character('\''), Term.optional(lv.term(lv24, lv21)), Literals.character('\''))), results -> (String)results.getOrThrow(lv21));
        Symbol lv29 = Symbol.of("unquoted_string");
        lv.set(lv29, new UnquotedStringParsingRule(1, EXPECTED_UNQUOTED_STRING_EXCEPTION));
        Symbol lv30 = Symbol.of("literal");
        Symbol lv31 = Symbol.of("arguments");
        lv.set(lv31, Term.repeatWithPossiblyTrailingSeparator(lv.getOrCreate(lv30), lv31, Literals.character(',')), arg2 -> (List)arg2.getOrThrow(lv31));
        Symbol lv32 = Symbol.of("unquoted_string_or_builtin");
        lv.set(lv32, Term.sequence(lv.term(lv29), Term.optional(Term.sequence(Literals.character('('), lv.term(lv31), Literals.character(')')))), state -> {
            ParseResults lv = state.getResults();
            String string = (String)lv.getOrThrow(lv29);
            if (string.isEmpty() || !SnbtParsing.canUnquotedStringStartWith(string.charAt(0))) {
                state.getErrors().add(state.getCursor(), SnbtOperation.SUGGESTIONS, INVALID_UNQUOTED_START_EXCEPTION);
                return null;
            }
            List list = (List)lv.get(lv31);
            if (list != null) {
                SnbtOperation.Type lv2 = new SnbtOperation.Type(string, list.size());
                SnbtOperation.Operator lv3 = SnbtOperation.OPERATIONS.get(lv2);
                if (lv3 != null) {
                    return lv3.apply(ops, list, state);
                }
                state.getErrors().add(state.getCursor(), CursorExceptionType.create(NO_SUCH_OPERATION_EXCEPTION, lv2.toString()));
                return null;
            }
            if (string.equalsIgnoreCase("true")) {
                return object;
            }
            if (string.equalsIgnoreCase("false")) {
                return object2;
            }
            return ops.createString(string);
        });
        Symbol lv33 = Symbol.of("map_key");
        lv.set(lv33, Term.anyOf(lv.term(lv28), lv.term(lv29)), results -> (String)results.getAnyOrThrow(lv28, lv29));
        Symbol lv34 = Symbol.of("map_entry");
        ParsingRuleEntry lv35 = lv.set(lv34, Term.sequence(lv.term(lv33), Literals.character(':'), lv.term(lv30)), state -> {
            ParseResults lv = state.getResults();
            String string = (String)lv.getOrThrow(lv33);
            if (string.isEmpty()) {
                state.getErrors().add(state.getCursor(), EMPTY_KEY_EXCEPTION);
                return null;
            }
            Object object = lv.getOrThrow(lv30);
            return Map.entry(string, object);
        });
        Symbol lv36 = Symbol.of("map_entries");
        lv.set(lv36, Term.repeatWithPossiblyTrailingSeparator(lv35, lv36, Literals.character(',')), results -> (List)results.getOrThrow(lv36));
        Symbol lv37 = Symbol.of("map_literal");
        lv.set(lv37, Term.sequence(Literals.character('{'), lv.term(lv36), Literals.character('}')), results -> {
            List list = (List)results.getOrThrow(lv36);
            if (list.isEmpty()) {
                return object3;
            }
            ImmutableMap.Builder builder = ImmutableMap.builderWithExpectedSize(list.size());
            for (Map.Entry entry : list) {
                builder.put(ops.createString((String)entry.getKey()), entry.getValue());
            }
            return ops.createMap(builder.buildKeepingLast());
        });
        Symbol lv38 = Symbol.of("list_entries");
        lv.set(lv38, Term.repeatWithPossiblyTrailingSeparator(lv.getOrCreate(lv30), lv38, Literals.character(',')), results -> (List)results.getOrThrow(lv38));
        Symbol lv39 = Symbol.of("array_prefix");
        lv.set(lv39, Term.anyOf(Term.sequence(Literals.character('B'), Term.always(lv39, ArrayType.BYTE)), Term.sequence(Literals.character('L'), Term.always(lv39, ArrayType.LONG)), Term.sequence(Literals.character('I'), Term.always(lv39, ArrayType.INT))), results -> (ArrayType)((Object)((Object)results.getOrThrow(lv39))));
        Symbol lv40 = Symbol.of("int_array_entries");
        lv.set(lv40, Term.repeatWithPossiblyTrailingSeparator(lv8, lv40, Literals.character(',')), results -> (List)results.getOrThrow(lv40));
        Symbol lv41 = Symbol.of("list_literal");
        lv.set(lv41, Term.sequence(Literals.character('['), Term.anyOf(Term.sequence(lv.term(lv39), Literals.character(';'), lv.term(lv40)), lv.term(lv38)), Literals.character(']')), state -> {
            ParseResults lv = state.getResults();
            ArrayType lv2 = (ArrayType)((Object)((Object)lv.get(lv39)));
            if (lv2 != null) {
                List list = (List)lv.getOrThrow(lv40);
                return list.isEmpty() ? lv2.createEmpty(ops) : lv2.decode(ops, list, state);
            }
            List list = (List)lv.getOrThrow(lv38);
            return list.isEmpty() ? object4 : ops.createList(list.stream());
        });
        ParsingRuleEntry lv42 = lv.set(lv30, Term.anyOf(Term.sequence(Term.positiveLookahead(DECIMAL_CHAR), Term.anyOf(lv.term(lv13, lv30), lv.term(lv7))), Term.sequence(Term.positiveLookahead(Literals.character('\"', '\'')), Term.cutting(), lv.term(lv28)), Term.sequence(Term.positiveLookahead(Literals.character('{')), Term.cutting(), lv.term(lv37, lv30)), Term.sequence(Term.positiveLookahead(Literals.character('[')), Term.cutting(), lv.term(lv41, lv30)), lv.term(lv32, lv30)), state -> {
            ParseResults lv = state.getResults();
            String string = (String)lv.get(lv28);
            if (string != null) {
                return ops.createString(string);
            }
            IntValue lv2 = (IntValue)lv.get(lv7);
            if (lv2 != null) {
                return lv2.decode(ops, state);
            }
            return lv.getOrThrow(lv30);
        });
        return new PackratParser<Object>(lv, lv42);
    }

    static enum Sign {
        PLUS,
        MINUS;


        public void append(StringBuilder builder) {
            if (this == MINUS) {
                builder.append("-");
            }
        }
    }

    record SignedValue<T>(Sign sign, T value) {
    }

    static enum NumericType {
        FLOAT,
        DOUBLE,
        BYTE,
        SHORT,
        INT,
        LONG;

    }

    record NumberSuffix(@Nullable Signedness signed, @Nullable NumericType type) {
        public static final NumberSuffix DEFAULT = new NumberSuffix(null, null);

        @Nullable
        public Signedness signed() {
            return this.signed;
        }

        @Nullable
        public NumericType type() {
            return this.type;
        }
    }

    static enum Signedness {
        SIGNED,
        UNSIGNED;

    }

    static class HexParsingRule
    extends TokenParsingRule {
        public HexParsingRule(int length) {
            super(length, length, CursorExceptionType.create(EXPECTED_HEX_ESCAPE_EXCEPTION, String.valueOf(length)));
        }

        @Override
        protected boolean isValidChar(char c) {
            return switch (c) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f' -> true;
                default -> false;
            };
        }
    }

    static enum ArrayType {
        BYTE(NumericType.BYTE, new NumericType[0]){
            private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);

            @Override
            public <T> T createEmpty(DynamicOps<T> ops) {
                return ops.createByteList(EMPTY_BUFFER);
            }

            @Override
            @Nullable
            public <T> T decode(DynamicOps<T> ops, List<IntValue> values, ParsingState<?> state) {
                ByteArrayList byteList = new ByteArrayList();
                for (IntValue lv : values) {
                    Number number = this.decode(lv, state);
                    if (number == null) {
                        return null;
                    }
                    byteList.add(number.byteValue());
                }
                return ops.createByteList(ByteBuffer.wrap(byteList.toByteArray()));
            }
        }
        ,
        INT(NumericType.INT, new NumericType[]{NumericType.BYTE, NumericType.SHORT}){

            @Override
            public <T> T createEmpty(DynamicOps<T> ops) {
                return ops.createIntList(IntStream.empty());
            }

            @Override
            @Nullable
            public <T> T decode(DynamicOps<T> ops, List<IntValue> values, ParsingState<?> state) {
                IntStream.Builder builder = IntStream.builder();
                for (IntValue lv : values) {
                    Number number = this.decode(lv, state);
                    if (number == null) {
                        return null;
                    }
                    builder.add(number.intValue());
                }
                return ops.createIntList(builder.build());
            }
        }
        ,
        LONG(NumericType.LONG, new NumericType[]{NumericType.BYTE, NumericType.SHORT, NumericType.INT}){

            @Override
            public <T> T createEmpty(DynamicOps<T> ops) {
                return ops.createLongList(LongStream.empty());
            }

            @Override
            @Nullable
            public <T> T decode(DynamicOps<T> ops, List<IntValue> values, ParsingState<?> state) {
                LongStream.Builder builder = LongStream.builder();
                for (IntValue lv : values) {
                    Number number = this.decode(lv, state);
                    if (number == null) {
                        return null;
                    }
                    builder.add(number.longValue());
                }
                return ops.createLongList(builder.build());
            }
        };

        private final NumericType elementType;
        private final Set<NumericType> castableTypes;

        ArrayType(NumericType elementType, NumericType ... castableTypes) {
            this.castableTypes = Set.of(castableTypes);
            this.elementType = elementType;
        }

        public boolean isTypeAllowed(NumericType type) {
            return type == this.elementType || this.castableTypes.contains((Object)type);
        }

        public abstract <T> T createEmpty(DynamicOps<T> var1);

        @Nullable
        public abstract <T> T decode(DynamicOps<T> var1, List<IntValue> var2, ParsingState<?> var3);

        @Nullable
        protected Number decode(IntValue value, ParsingState<?> state) {
            NumericType lv = this.getType(value.suffix);
            if (lv == null) {
                state.getErrors().add(state.getCursor(), INVALID_ARRAY_ELEMENT_TYPE_EXCEPTION);
                return null;
            }
            return (Number)value.decode(JavaOps.INSTANCE, lv, state);
        }

        @Nullable
        private NumericType getType(NumberSuffix suffix) {
            NumericType lv = suffix.type();
            if (lv == null) {
                return this.elementType;
            }
            if (!this.isTypeAllowed(lv)) {
                return null;
            }
            return lv;
        }
    }

    record IntValue(Sign sign, Radix base, String digits, NumberSuffix suffix) {
        private Signedness getSignedness() {
            if (this.suffix.signed != null) {
                return this.suffix.signed;
            }
            return switch (this.base.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2 -> Signedness.UNSIGNED;
                case 1 -> Signedness.SIGNED;
            };
        }

        private String toString(Sign sign) {
            boolean bl = SnbtParsing.containsUnderscore(this.digits);
            if (sign == Sign.MINUS || bl) {
                StringBuilder stringBuilder = new StringBuilder();
                sign.append(stringBuilder);
                SnbtParsing.append(stringBuilder, this.digits, bl);
                return stringBuilder.toString();
            }
            return this.digits;
        }

        @Nullable
        public <T> T decode(DynamicOps<T> ops, ParsingState<?> state) {
            return this.decode(ops, Objects.requireNonNullElse(this.suffix.type, NumericType.INT), state);
        }

        @Nullable
        public <T> T decode(DynamicOps<T> ops, NumericType type, ParsingState<?> state) {
            boolean bl;
            boolean bl2 = bl = this.getSignedness() == Signedness.SIGNED;
            if (!bl && this.sign == Sign.MINUS) {
                state.getErrors().add(state.getCursor(), EXPECTED_NON_NEGATIVE_NUMBER_EXCEPTION);
                return null;
            }
            String string = this.toString(this.sign);
            int i = switch (this.base.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> 2;
                case 1 -> 10;
                case 2 -> 16;
            };
            try {
                if (bl) {
                    return switch (type.ordinal()) {
                        case 2 -> ops.createByte(Byte.parseByte(string, i));
                        case 3 -> ops.createShort(Short.parseShort(string, i));
                        case 4 -> ops.createInt(Integer.parseInt(string, i));
                        case 5 -> ops.createLong(Long.parseLong(string, i));
                        default -> {
                            state.getErrors().add(state.getCursor(), EXPECTED_INTEGER_TYPE_EXCEPTION);
                            yield null;
                        }
                    };
                }
                return switch (type.ordinal()) {
                    case 2 -> ops.createByte(UnsignedBytes.parseUnsignedByte(string, i));
                    case 3 -> ops.createShort(SnbtParsing.parseUnsignedShort(string, i));
                    case 4 -> ops.createInt(Integer.parseUnsignedInt(string, i));
                    case 5 -> ops.createLong(Long.parseUnsignedLong(string, i));
                    default -> {
                        state.getErrors().add(state.getCursor(), EXPECTED_INTEGER_TYPE_EXCEPTION);
                        yield null;
                    }
                };
            } catch (NumberFormatException numberFormatException) {
                state.getErrors().add(state.getCursor(), SnbtParsing.toNumberParseFailure(numberFormatException));
                return null;
            }
        }
    }

    static enum Radix {
        BINARY,
        DECIMAL,
        HEX;

    }
}

