/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class SuggestionProviders {
    private static final Map<Identifier, SuggestionProvider<CommandSource>> REGISTRY = new HashMap<Identifier, SuggestionProvider<CommandSource>>();
    private static final Identifier ASK_SERVER_ID = Identifier.ofVanilla("ask_server");
    public static final SuggestionProvider<CommandSource> ASK_SERVER = SuggestionProviders.register(ASK_SERVER_ID, (context, builder) -> ((CommandSource)context.getSource()).getCompletions(context));
    public static final SuggestionProvider<CommandSource> AVAILABLE_SOUNDS = SuggestionProviders.register(Identifier.ofVanilla("available_sounds"), (context, builder) -> CommandSource.suggestIdentifiers(((CommandSource)context.getSource()).getSoundIds(), builder));
    public static final SuggestionProvider<CommandSource> SUMMONABLE_ENTITIES = SuggestionProviders.register(Identifier.ofVanilla("summonable_entities"), (context, builder) -> CommandSource.suggestFromIdentifier(Registries.ENTITY_TYPE.stream().filter(entityType -> entityType.isEnabled(((CommandSource)context.getSource()).getEnabledFeatures()) && entityType.isSummonable()), builder, EntityType::getId, EntityType::getName));

    public static <S extends CommandSource> SuggestionProvider<S> register(Identifier id, SuggestionProvider<CommandSource> provider) {
        SuggestionProvider<CommandSource> suggestionProvider2 = REGISTRY.putIfAbsent(id, provider);
        if (suggestionProvider2 != null) {
            throw new IllegalArgumentException("A command suggestion provider is already registered with the name '" + String.valueOf(id) + "'");
        }
        return new LocalProvider(id, provider);
    }

    public static <S extends CommandSource> SuggestionProvider<S> cast(SuggestionProvider<CommandSource> suggestionProvider) {
        return suggestionProvider;
    }

    public static <S extends CommandSource> SuggestionProvider<S> byId(Identifier id) {
        return SuggestionProviders.cast(REGISTRY.getOrDefault(id, ASK_SERVER));
    }

    public static Identifier computeId(SuggestionProvider<?> provider) {
        Identifier identifier;
        if (provider instanceof LocalProvider) {
            LocalProvider lv = (LocalProvider)provider;
            identifier = lv.id;
        } else {
            identifier = ASK_SERVER_ID;
        }
        return identifier;
    }

    record LocalProvider(Identifier id, SuggestionProvider<CommandSource> provider) implements SuggestionProvider<CommandSource>
    {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return this.provider.getSuggestions(context, builder);
        }
    }
}

