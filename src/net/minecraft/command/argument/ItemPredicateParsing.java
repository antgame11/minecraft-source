/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtParsingRule;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.packrat.AnyIdParsingRule;
import net.minecraft.util.packrat.IdentifiableParsingRule;
import net.minecraft.util.packrat.Literals;
import net.minecraft.util.packrat.PackratParser;
import net.minecraft.util.packrat.ParseResults;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingRules;
import net.minecraft.util.packrat.Symbol;
import net.minecraft.util.packrat.Term;

public class ItemPredicateParsing {
    public static <T, C, P> PackratParser<List<T>> createParser(Callbacks<T, C, P> callbacks) {
        Symbol lv = Symbol.of("top");
        Symbol lv2 = Symbol.of("type");
        Symbol lv3 = Symbol.of("any_type");
        Symbol lv4 = Symbol.of("element_type");
        Symbol lv5 = Symbol.of("tag_type");
        Symbol lv6 = Symbol.of("conditions");
        Symbol lv7 = Symbol.of("alternatives");
        Symbol lv8 = Symbol.of("term");
        Symbol lv9 = Symbol.of("negation");
        Symbol lv10 = Symbol.of("test");
        Symbol lv11 = Symbol.of("component_type");
        Symbol lv12 = Symbol.of("predicate_type");
        Symbol lv13 = Symbol.of("id");
        Symbol lv14 = Symbol.of("tag");
        ParsingRules<StringReader> lv15 = new ParsingRules<StringReader>();
        ParsingRuleEntry<StringReader, Identifier> lv16 = lv15.set(lv13, AnyIdParsingRule.INSTANCE);
        ParsingRuleEntry lv17 = lv15.set(lv, Term.anyOf(Term.sequence(lv15.term(lv2), Literals.character('['), Term.cutting(), Term.optional(lv15.term(lv6)), Literals.character(']')), lv15.term(lv2)), results -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            ((Optional)results.getOrThrow(lv2)).ifPresent(builder::add);
            List list = (List)results.get(lv6);
            if (list != null) {
                builder.addAll((Iterable)list);
            }
            return builder.build();
        });
        lv15.set(lv2, Term.anyOf(lv15.term(lv4), Term.sequence(Literals.character('#'), Term.cutting(), lv15.term(lv5)), lv15.term(lv3)), results -> Optional.ofNullable(results.getAny(lv4, lv5)));
        lv15.set(lv3, Literals.character('*'), results -> Unit.INSTANCE);
        lv15.set(lv4, new ItemParsingRule<T, C, P>(lv16, callbacks));
        lv15.set(lv5, new TagParsingRule<T, C, P>(lv16, callbacks));
        lv15.set(lv6, Term.sequence(lv15.term(lv7), Term.optional(Term.sequence(Literals.character(','), lv15.term(lv6)))), results -> {
            Object object = callbacks.anyOf((List)results.getOrThrow(lv7));
            return Optional.ofNullable((List)results.get(lv6)).map(predicates -> Util.withPrepended(object, predicates)).orElse(List.of(object));
        });
        lv15.set(lv7, Term.sequence(lv15.term(lv8), Term.optional(Term.sequence(Literals.character('|'), lv15.term(lv7)))), results -> {
            Object object = results.getOrThrow(lv8);
            return Optional.ofNullable((List)results.get(lv7)).map(predicates -> Util.withPrepended(object, predicates)).orElse(List.of(object));
        });
        lv15.set(lv8, Term.anyOf(lv15.term(lv10), Term.sequence(Literals.character('!'), lv15.term(lv9))), results -> results.getAnyOrThrow(lv10, lv9));
        lv15.set(lv9, lv15.term(lv10), results -> callbacks.negate(results.getOrThrow(lv10)));
        lv15.set(lv10, Term.anyOf(Term.sequence(lv15.term(lv11), Literals.character('='), Term.cutting(), lv15.term(lv14)), Term.sequence(lv15.term(lv12), Literals.character('~'), Term.cutting(), lv15.term(lv14)), lv15.term(lv11)), state -> {
            ParseResults lv = state.getResults();
            Object object = lv.get(lv12);
            try {
                if (object != null) {
                    Dynamic dynamic = (Dynamic)lv.getOrThrow(lv14);
                    return callbacks.subPredicatePredicate((ImmutableStringReader)state.getReader(), object, dynamic);
                }
                Object object2 = lv.getOrThrow(lv11);
                Dynamic dynamic2 = (Dynamic)lv.get(lv14);
                return dynamic2 != null ? callbacks.componentMatchPredicate((ImmutableStringReader)state.getReader(), object2, dynamic2) : callbacks.componentPresencePredicate((ImmutableStringReader)state.getReader(), object2);
            } catch (CommandSyntaxException commandSyntaxException) {
                state.getErrors().add(state.getCursor(), commandSyntaxException);
                return null;
            }
        });
        lv15.set(lv11, new ComponentParsingRule<T, C, P>(lv16, callbacks));
        lv15.set(lv12, new SubPredicateParsingRule<T, C, P>(lv16, callbacks));
        lv15.set(lv14, new NbtParsingRule<NbtElement>(NbtOps.INSTANCE));
        return new PackratParser<List<T>>(lv15, lv17);
    }

    static class ItemParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, T> {
        ItemParsingRule(ParsingRuleEntry<StringReader, Identifier> idParsingRule, Callbacks<T, C, P> callbacks) {
            super(idParsingRule, callbacks);
        }

        @Override
        protected T parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).itemMatchPredicate(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamItemIds();
        }
    }

    public static interface Callbacks<T, C, P> {
        public T itemMatchPredicate(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamItemIds();

        public T tagMatchPredicate(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamTags();

        public C componentCheck(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamComponentIds();

        public T componentMatchPredicate(ImmutableStringReader var1, C var2, Dynamic<?> var3) throws CommandSyntaxException;

        public T componentPresencePredicate(ImmutableStringReader var1, C var2);

        public P subPredicateCheck(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamSubPredicateIds();

        public T subPredicatePredicate(ImmutableStringReader var1, P var2, Dynamic<?> var3) throws CommandSyntaxException;

        public T negate(T var1);

        public T anyOf(List<T> var1);
    }

    static class TagParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, T> {
        TagParsingRule(ParsingRuleEntry<StringReader, Identifier> idParsingRule, Callbacks<T, C, P> callbacks) {
            super(idParsingRule, callbacks);
        }

        @Override
        protected T parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).tagMatchPredicate(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamTags();
        }
    }

    static class ComponentParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, C> {
        ComponentParsingRule(ParsingRuleEntry<StringReader, Identifier> idParsingRule, Callbacks<T, C, P> callbacks) {
            super(idParsingRule, callbacks);
        }

        @Override
        protected C parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).componentCheck(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamComponentIds();
        }
    }

    static class SubPredicateParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, P> {
        SubPredicateParsingRule(ParsingRuleEntry<StringReader, Identifier> idParsingRule, Callbacks<T, C, P> callbacks) {
            super(idParsingRule, callbacks);
        }

        @Override
        protected P parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).subPredicateCheck(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamSubPredicateIds();
        }
    }
}

