/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ParticleEffectArgumentType
implements ArgumentType<ParticleEffect> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "particle{foo:bar}");
    public static final DynamicCommandExceptionType UNKNOWN_PARTICLE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("particle.notFound", id));
    public static final DynamicCommandExceptionType INVALID_OPTIONS_EXCEPTION = new DynamicCommandExceptionType(error -> Text.stringifiedTranslatable("particle.invalidOptions", error));
    private final RegistryWrapper.WrapperLookup registries;
    private static final StringNbtReader<?> SNBT_READER = StringNbtReader.fromOps(NbtOps.INSTANCE);

    public ParticleEffectArgumentType(CommandRegistryAccess registryAccess) {
        this.registries = registryAccess;
    }

    public static ParticleEffectArgumentType particleEffect(CommandRegistryAccess registryAccess) {
        return new ParticleEffectArgumentType(registryAccess);
    }

    public static ParticleEffect getParticle(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, ParticleEffect.class);
    }

    @Override
    public ParticleEffect parse(StringReader stringReader) throws CommandSyntaxException {
        return ParticleEffectArgumentType.readParameters(stringReader, this.registries);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static ParticleEffect readParameters(StringReader reader, RegistryWrapper.WrapperLookup registries) throws CommandSyntaxException {
        ParticleType<?> lv = ParticleEffectArgumentType.getType(reader, registries.getOrThrow(RegistryKeys.PARTICLE_TYPE));
        return ParticleEffectArgumentType.readParameters(SNBT_READER, reader, lv, registries);
    }

    private static ParticleType<?> getType(StringReader reader, RegistryWrapper<ParticleType<?>> registryWrapper) throws CommandSyntaxException {
        Identifier lv = Identifier.fromCommandInput(reader);
        RegistryKey<ParticleType<?>> lv2 = RegistryKey.of(RegistryKeys.PARTICLE_TYPE, lv);
        return registryWrapper.getOptional(lv2).orElseThrow(() -> UNKNOWN_PARTICLE_EXCEPTION.createWithContext(reader, lv)).value();
    }

    private static <T extends ParticleEffect, O> T readParameters(StringNbtReader<O> snbtReader, StringReader reader, ParticleType<T> particleType, RegistryWrapper.WrapperLookup registries) throws CommandSyntaxException {
        RegistryOps<O> lv = registries.getOps(snbtReader.getOps());
        Object object = reader.canRead() && reader.peek() == '{' ? snbtReader.readAsArgument(reader) : lv.emptyMap();
        return (T)((ParticleEffect)particleType.getCodec().codec().parse(lv, object).getOrThrow(INVALID_OPTIONS_EXCEPTION::create));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        RegistryEntryLookup lv = this.registries.getOrThrow(RegistryKeys.PARTICLE_TYPE);
        return CommandSource.suggestIdentifiers(lv.streamKeys().map(RegistryKey::getValue), builder);
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

