/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.tag;

import com.google.common.collect.Maps;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagFile;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public abstract class TagProvider<T>
implements DataProvider {
    protected final DataOutput.PathResolver pathResolver;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture;
    private final CompletableFuture<Void> registryLoadFuture = new CompletableFuture();
    private final CompletableFuture<TagLookup<T>> parentTagLookupFuture;
    protected final RegistryKey<? extends Registry<T>> registryRef;
    private final Map<Identifier, TagBuilder> tagBuilders = Maps.newLinkedHashMap();

    protected TagProvider(DataOutput output, RegistryKey<? extends Registry<T>> registryRef, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        this(output, registryRef, registriesFuture, CompletableFuture.completedFuture(TagLookup.empty()));
    }

    protected TagProvider(DataOutput output, RegistryKey<? extends Registry<T>> registryRef, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture, CompletableFuture<TagLookup<T>> parentTagLookupFuture) {
        this.pathResolver = output.getTagResolver(registryRef);
        this.registryRef = registryRef;
        this.parentTagLookupFuture = parentTagLookupFuture;
        this.registriesFuture = registriesFuture;
    }

    @Override
    public final String getName() {
        return "Tags for " + String.valueOf(this.registryRef.getValue());
    }

    protected abstract void configure(RegistryWrapper.WrapperLookup var1);

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        record RegistryInfo<T>(RegistryWrapper.WrapperLookup contents, TagLookup<T> parent) {
        }
        return ((CompletableFuture)((CompletableFuture)this.getRegistriesFuture().thenApply(registriesFuture -> {
            this.registryLoadFuture.complete(null);
            return registriesFuture;
        })).thenCombineAsync(this.parentTagLookupFuture, (registries, parent) -> new RegistryInfo((RegistryWrapper.WrapperLookup)registries, parent), (Executor)Util.getMainWorkerExecutor())).thenCompose(info -> {
            RegistryEntryLookup lv = info.contents.getOrThrow(this.registryRef);
            Predicate<Identifier> predicate = id -> ((RegistryWrapper.Impl)lv).getOptional(RegistryKey.of(this.registryRef, id)).isPresent();
            Predicate<Identifier> predicate2 = id -> this.tagBuilders.containsKey(id) || arg.parent.contains(TagKey.of(this.registryRef, id));
            return CompletableFuture.allOf((CompletableFuture[])this.tagBuilders.entrySet().stream().map(entry -> {
                Identifier lv = (Identifier)entry.getKey();
                TagBuilder lv2 = (TagBuilder)entry.getValue();
                List<TagEntry> list = lv2.build();
                List<TagEntry> list2 = list.stream().filter(tagEntry -> !tagEntry.canAdd(predicate, predicate2)).toList();
                if (!list2.isEmpty()) {
                    throw new IllegalArgumentException(String.format(Locale.ROOT, "Couldn't define tag %s as it is missing following references: %s", lv, list2.stream().map(Objects::toString).collect(Collectors.joining(","))));
                }
                Path path = this.pathResolver.resolveJson(lv);
                return DataProvider.writeCodecToPath(writer, arg2.contents, TagFile.CODEC, new TagFile(list, false), path);
            }).toArray(CompletableFuture[]::new));
        });
    }

    protected TagBuilder getTagBuilder(TagKey<T> tag) {
        return this.tagBuilders.computeIfAbsent(tag.id(), id -> TagBuilder.create());
    }

    public CompletableFuture<TagLookup<T>> getTagLookupFuture() {
        return this.registryLoadFuture.thenApply(void_ -> tag -> Optional.ofNullable(this.tagBuilders.get(tag.id())));
    }

    protected CompletableFuture<RegistryWrapper.WrapperLookup> getRegistriesFuture() {
        return this.registriesFuture.thenApply(registries -> {
            this.tagBuilders.clear();
            this.configure((RegistryWrapper.WrapperLookup)registries);
            return registries;
        });
    }

    @FunctionalInterface
    public static interface TagLookup<T>
    extends Function<TagKey<T>, Optional<TagBuilder>> {
        public static <T> TagLookup<T> empty() {
            return tag -> Optional.empty();
        }

        default public boolean contains(TagKey<T> tag) {
            return ((Optional)this.apply(tag)).isPresent();
        }
    }
}

