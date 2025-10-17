/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FilenameUtils;

public class RegistrySelectorArgumentType<T>
implements ArgumentType<Collection<RegistryEntry.Reference<T>>> {
    private static final Collection<String> EXAMPLES = List.of("minecraft:*", "*:asset", "*");
    public static final Dynamic2CommandExceptionType NOT_FOUND_EXCEPTION = new Dynamic2CommandExceptionType((selector, registryRef) -> Text.stringifiedTranslatable("argument.resource_selector.not_found", selector, registryRef));
    final RegistryKey<? extends Registry<T>> registryRef;
    private final RegistryWrapper<T> registry;

    RegistrySelectorArgumentType(CommandRegistryAccess registries, RegistryKey<? extends Registry<T>> registryRef) {
        this.registryRef = registryRef;
        this.registry = registries.getOrThrow(registryRef);
    }

    @Override
    public Collection<RegistryEntry.Reference<T>> parse(StringReader stringReader) throws CommandSyntaxException {
        String string = RegistrySelectorArgumentType.addNamespace(RegistrySelectorArgumentType.read(stringReader));
        List<RegistryEntry.Reference<T>> list = this.registry.streamEntries().filter(entry -> RegistrySelectorArgumentType.matches(string, entry.registryKey().getValue())).toList();
        if (list.isEmpty()) {
            throw NOT_FOUND_EXCEPTION.createWithContext(stringReader, string, this.registryRef.getValue());
        }
        return list;
    }

    public static <T> Collection<RegistryEntry.Reference<T>> select(StringReader reader, RegistryWrapper<T> registry) {
        String string = RegistrySelectorArgumentType.addNamespace(RegistrySelectorArgumentType.read(reader));
        return registry.streamEntries().filter(entry -> RegistrySelectorArgumentType.matches(string, entry.registryKey().getValue())).toList();
    }

    private static String read(StringReader reader) {
        int i = reader.getCursor();
        while (reader.canRead() && RegistrySelectorArgumentType.isSelectorChar(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(i, reader.getCursor());
    }

    private static boolean isSelectorChar(char c) {
        return Identifier.isCharValid(c) || c == '*' || c == '?';
    }

    private static String addNamespace(String path) {
        if (!path.contains(":")) {
            return "minecraft:" + path;
        }
        return path;
    }

    private static boolean matches(String selector, Identifier id) {
        return FilenameUtils.wildcardMatch(id.toString(), selector);
    }

    public static <T> RegistrySelectorArgumentType<T> selector(CommandRegistryAccess registries, RegistryKey<? extends Registry<T>> registryRef) {
        return new RegistrySelectorArgumentType<T>(registries, registryRef);
    }

    public static <T> Collection<RegistryEntry.Reference<T>> getEntries(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, Collection.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.listSuggestions(context, builder, this.registryRef, CommandSource.SuggestedIdType.ELEMENTS);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    public static class Serializer<T>
    implements ArgumentSerializer<RegistrySelectorArgumentType<T>, Properties> {
        @Override
        public void writePacket(Properties arg, PacketByteBuf arg2) {
            arg2.writeRegistryKey(arg.registryRef);
        }

        @Override
        public Properties fromPacket(PacketByteBuf arg) {
            return new Properties(arg.readRegistryRefKey());
        }

        @Override
        public void writeJson(Properties arg, JsonObject jsonObject) {
            jsonObject.addProperty("registry", arg.registryRef.getValue().toString());
        }

        @Override
        public Properties getArgumentTypeProperties(RegistrySelectorArgumentType<T> arg) {
            return new Properties(arg.registryRef);
        }

        @Override
        public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
            return this.fromPacket(buf);
        }

        public final class Properties
        implements ArgumentSerializer.ArgumentTypeProperties<RegistrySelectorArgumentType<T>> {
            final RegistryKey<? extends Registry<T>> registryRef;

            Properties(RegistryKey<? extends Registry<T>> registryRef) {
                this.registryRef = registryRef;
            }

            @Override
            public RegistrySelectorArgumentType<T> createType(CommandRegistryAccess arg) {
                return new RegistrySelectorArgumentType(arg, this.registryRef);
            }

            @Override
            public ArgumentSerializer<RegistrySelectorArgumentType<T>, ?> getSerializer() {
                return Serializer.this;
            }

            @Override
            public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
                return this.createType(commandRegistryAccess);
            }
        }
    }
}

