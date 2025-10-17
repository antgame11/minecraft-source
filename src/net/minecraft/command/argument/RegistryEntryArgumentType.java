/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtParsing;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.packrat.AnyIdParsingRule;
import net.minecraft.util.packrat.PackratParser;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingRules;
import net.minecraft.util.packrat.Symbol;
import net.minecraft.util.packrat.Term;
import org.jetbrains.annotations.Nullable;

public class RegistryEntryArgumentType<T>
implements ArgumentType<RegistryEntry<T>> {
    private static final Collection<String> EXAMPLES = List.of("foo", "foo:bar", "012", "{}", "true");
    public static final DynamicCommandExceptionType FAILED_TO_PARSE_EXCEPTION = new DynamicCommandExceptionType(argument -> Text.stringifiedTranslatable("argument.resource_or_id.failed_to_parse", argument));
    public static final Dynamic2CommandExceptionType NO_SUCH_ELEMENT_EXCEPTION = new Dynamic2CommandExceptionType((key, registryRef) -> Text.stringifiedTranslatable("argument.resource_or_id.no_such_element", key, registryRef));
    public static final DynamicOps<NbtElement> OPS = NbtOps.INSTANCE;
    private final RegistryWrapper.WrapperLookup registries;
    private final Optional<? extends RegistryWrapper.Impl<T>> registry;
    private final Codec<T> entryCodec;
    private final PackratParser<EntryParser<T, NbtElement>> parser;
    private final RegistryKey<? extends Registry<T>> registryRef;

    protected RegistryEntryArgumentType(CommandRegistryAccess registryAccess, RegistryKey<? extends Registry<T>> registry, Codec<T> entryCodec) {
        this.registries = registryAccess;
        this.registry = registryAccess.getOptional(registry);
        this.registryRef = registry;
        this.entryCodec = entryCodec;
        this.parser = RegistryEntryArgumentType.createParser(registry, OPS);
    }

    public static <T, O> PackratParser<EntryParser<T, O>> createParser(RegistryKey<? extends Registry<T>> key, DynamicOps<O> ops) {
        PackratParser<O> lv = SnbtParsing.createParser(ops);
        ParsingRules<StringReader> lv2 = new ParsingRules<StringReader>();
        Symbol lv3 = Symbol.of("result");
        Symbol lv4 = Symbol.of("id");
        Symbol lv5 = Symbol.of("value");
        lv2.set(lv4, AnyIdParsingRule.INSTANCE);
        lv2.set(lv5, lv.top().getRule());
        ParsingRuleEntry lv6 = lv2.set(lv3, Term.anyOf(lv2.term(lv4), lv2.term(lv5)), results -> {
            Identifier lv = (Identifier)results.get(lv4);
            if (lv != null) {
                return new ReferenceParser(RegistryKey.of(key, lv));
            }
            Object object = results.getOrThrow(lv5);
            return new DirectParser(object);
        });
        return new PackratParser<EntryParser<T, O>>(lv2, lv6);
    }

    public static LootTableArgumentType lootTable(CommandRegistryAccess registryAccess) {
        return new LootTableArgumentType(registryAccess);
    }

    public static RegistryEntry<LootTable> getLootTable(CommandContext<ServerCommandSource> context, String argument) throws CommandSyntaxException {
        return RegistryEntryArgumentType.getArgument(context, argument);
    }

    public static LootFunctionArgumentType lootFunction(CommandRegistryAccess registryAccess) {
        return new LootFunctionArgumentType(registryAccess);
    }

    public static RegistryEntry<LootFunction> getLootFunction(CommandContext<ServerCommandSource> context, String argument) {
        return RegistryEntryArgumentType.getArgument(context, argument);
    }

    public static LootConditionArgumentType lootCondition(CommandRegistryAccess registryAccess) {
        return new LootConditionArgumentType(registryAccess);
    }

    public static RegistryEntry<LootCondition> getLootCondition(CommandContext<ServerCommandSource> context, String argument) {
        return RegistryEntryArgumentType.getArgument(context, argument);
    }

    public static DialogArgumentType dialog(CommandRegistryAccess registryAccess) {
        return new DialogArgumentType(registryAccess);
    }

    public static RegistryEntry<Dialog> getDialog(CommandContext<ServerCommandSource> context, String argument) {
        return RegistryEntryArgumentType.getArgument(context, argument);
    }

    private static <T> RegistryEntry<T> getArgument(CommandContext<ServerCommandSource> context, String argument) {
        return context.getArgument(argument, RegistryEntry.class);
    }

    @Override
    @Nullable
    public RegistryEntry<T> parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader, this.parser, OPS);
    }

    @Nullable
    private <O> RegistryEntry<T> parse(StringReader reader, PackratParser<EntryParser<T, O>> parser, DynamicOps<O> ops) throws CommandSyntaxException {
        EntryParser<T, O> lv = parser.parse(reader);
        if (this.registry.isEmpty()) {
            return null;
        }
        return lv.parse(reader, this.registries, ops, this.entryCodec, this.registry.get());
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder suggestionsBuilder) {
        return CommandSource.listSuggestions(context, suggestionsBuilder, this.registryRef, CommandSource.SuggestedIdType.ELEMENTS);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    @Nullable
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static class LootTableArgumentType
    extends RegistryEntryArgumentType<LootTable> {
        protected LootTableArgumentType(CommandRegistryAccess registryAccess) {
            super(registryAccess, RegistryKeys.LOOT_TABLE, LootTable.CODEC);
        }

        @Override
        @Nullable
        public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static class LootFunctionArgumentType
    extends RegistryEntryArgumentType<LootFunction> {
        protected LootFunctionArgumentType(CommandRegistryAccess registryAccess) {
            super(registryAccess, RegistryKeys.ITEM_MODIFIER, LootFunctionTypes.CODEC);
        }

        @Override
        @Nullable
        public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static class LootConditionArgumentType
    extends RegistryEntryArgumentType<LootCondition> {
        protected LootConditionArgumentType(CommandRegistryAccess registryAccess) {
            super(registryAccess, RegistryKeys.PREDICATE, LootCondition.CODEC);
        }

        @Override
        @Nullable
        public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static class DialogArgumentType
    extends RegistryEntryArgumentType<Dialog> {
        protected DialogArgumentType(CommandRegistryAccess registryAccess) {
            super(registryAccess, RegistryKeys.DIALOG, Dialog.CODEC);
        }

        @Override
        @Nullable
        public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static sealed interface EntryParser<T, O>
    permits DirectParser, ReferenceParser {
        public RegistryEntry<T> parse(ImmutableStringReader var1, RegistryWrapper.WrapperLookup var2, DynamicOps<O> var3, Codec<T> var4, RegistryWrapper.Impl<T> var5) throws CommandSyntaxException;
    }

    public record ReferenceParser<T, O>(RegistryKey<T> key) implements EntryParser<T, O>
    {
        @Override
        public RegistryEntry<T> parse(ImmutableStringReader reader, RegistryWrapper.WrapperLookup registries, DynamicOps<O> ops, Codec<T> codec, RegistryWrapper.Impl<T> registryAccess) throws CommandSyntaxException {
            return registryAccess.getOptional(this.key).orElseThrow(() -> NO_SUCH_ELEMENT_EXCEPTION.createWithContext(reader, this.key.getValue(), this.key.getRegistry()));
        }
    }

    public record DirectParser<T, O>(O value) implements EntryParser<T, O>
    {
        @Override
        public RegistryEntry<T> parse(ImmutableStringReader reader, RegistryWrapper.WrapperLookup registries, DynamicOps<O> ops, Codec<T> codec, RegistryWrapper.Impl<T> registryAccess) throws CommandSyntaxException {
            return RegistryEntry.of(codec.parse(registries.getOps(ops), this.value).getOrThrow(error -> FAILED_TO_PARSE_EXCEPTION.createWithContext(reader, error)));
        }
    }
}

