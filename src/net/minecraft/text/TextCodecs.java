/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.google.gson.JsonElement;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.NbtTextContent;
import net.minecraft.text.ObjectTextContent;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.ScoreTextContent;
import net.minecraft.text.SelectorTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;

public class TextCodecs {
    public static final Codec<Text> CODEC = Codec.recursive("Component", TextCodecs::createCodec);
    public static final PacketCodec<RegistryByteBuf, Text> REGISTRY_PACKET_CODEC = PacketCodecs.registryCodec(CODEC);
    public static final PacketCodec<RegistryByteBuf, Optional<Text>> OPTIONAL_PACKET_CODEC = REGISTRY_PACKET_CODEC.collect(PacketCodecs::optional);
    public static final PacketCodec<RegistryByteBuf, Text> UNLIMITED_REGISTRY_PACKET_CODEC = PacketCodecs.unlimitedRegistryCodec(CODEC);
    public static final PacketCodec<RegistryByteBuf, Optional<Text>> OPTIONAL_UNLIMITED_REGISTRY_PACKET_CODEC = UNLIMITED_REGISTRY_PACKET_CODEC.collect(PacketCodecs::optional);
    public static final PacketCodec<ByteBuf, Text> PACKET_CODEC = PacketCodecs.unlimitedCodec(CODEC);

    public static Codec<Text> withJsonLengthLimit(final int maxLength) {
        return new Codec<Text>(){

            @Override
            public <T> DataResult<Pair<Text, T>> decode(DynamicOps<T> ops, T value) {
                return CODEC.decode(ops, value).flatMap((? super R pair) -> {
                    if (this.isTooLarge(ops, (Text)pair.getFirst())) {
                        return DataResult.error(() -> "Component was too large: greater than max size " + maxLength);
                    }
                    return DataResult.success(pair);
                });
            }

            @Override
            public <T> DataResult<T> encode(Text arg, DynamicOps<T> dynamicOps, T object) {
                return CODEC.encodeStart(dynamicOps, arg);
            }

            private <T> boolean isTooLarge(DynamicOps<T> ops, Text text) {
                DataResult<JsonElement> dataResult = CODEC.encodeStart(1.toJsonOps(ops), text);
                return dataResult.isSuccess() && JsonHelper.isTooLarge(dataResult.getOrThrow(), maxLength);
            }

            private static <T> DynamicOps<JsonElement> toJsonOps(DynamicOps<T> ops) {
                if (ops instanceof RegistryOps) {
                    RegistryOps lv = (RegistryOps)ops;
                    return lv.withDelegate(JsonOps.INSTANCE);
                }
                return JsonOps.INSTANCE;
            }

            @Override
            public /* synthetic */ DataResult encode(Object input, DynamicOps ops, Object prefix) {
                return this.encode((Text)input, ops, prefix);
            }
        };
    }

    private static MutableText combine(List<Text> texts) {
        MutableText lv = texts.get(0).copy();
        for (int i = 1; i < texts.size(); ++i) {
            lv.append(texts.get(i));
        }
        return lv;
    }

    public static <T> MapCodec<T> dispatchingCodec(Codecs.IdMapper<String, MapCodec<? extends T>> idMapper, Function<T, MapCodec<? extends T>> typeToCodec, String typeKey) {
        FuzzyCodec<? extends T> mapCodec = new FuzzyCodec<T>(idMapper.values(), typeToCodec);
        MapCodec<T> mapCodec2 = idMapper.getCodec(Codec.STRING).dispatchMap(typeKey, typeToCodec, codec -> codec);
        DispatchingCodec<? extends T> mapCodec3 = new DispatchingCodec<T>(typeKey, mapCodec2, mapCodec);
        return Codecs.orCompressed(mapCodec3, mapCodec2);
    }

    private static Codec<Text> createCodec(Codec<Text> selfCodec) {
        Codecs.IdMapper lv = new Codecs.IdMapper();
        TextCodecs.registerTypes(lv);
        MapCodec<TextContent> mapCodec = TextCodecs.dispatchingCodec(lv, TextContent::getCodec, "type");
        Codec codec2 = RecordCodecBuilder.create(instance -> instance.group(mapCodec.forGetter(Text::getContent), Codecs.nonEmptyList(selfCodec.listOf()).optionalFieldOf("extra", List.of()).forGetter(Text::getSiblings), Style.Codecs.MAP_CODEC.forGetter(Text::getStyle)).apply((Applicative<Text, ?>)instance, MutableText::new));
        return Codec.either(Codec.either(Codec.STRING, Codecs.nonEmptyList(selfCodec.listOf())), codec2).xmap(either -> either.map(either2 -> either2.map(Text::literal, TextCodecs::combine), text -> text), text -> {
            String string = text.getLiteralString();
            return string != null ? Either.left(Either.left(string)) : Either.right(text);
        });
    }

    private static void registerTypes(Codecs.IdMapper<String, MapCodec<? extends TextContent>> idMapper) {
        idMapper.put("text", PlainTextContent.CODEC);
        idMapper.put("translatable", TranslatableTextContent.CODEC);
        idMapper.put("keybind", KeybindTextContent.CODEC);
        idMapper.put("score", ScoreTextContent.CODEC);
        idMapper.put("selector", SelectorTextContent.CODEC);
        idMapper.put("nbt", NbtTextContent.CODEC);
        idMapper.put("object", ObjectTextContent.CODEC);
    }

    static class FuzzyCodec<T>
    extends MapCodec<T> {
        private final Collection<MapCodec<? extends T>> codecs;
        private final Function<T, ? extends MapEncoder<? extends T>> codecGetter;

        public FuzzyCodec(Collection<MapCodec<? extends T>> codecs, Function<T, ? extends MapEncoder<? extends T>> codecGetter) {
            this.codecs = codecs;
            this.codecGetter = codecGetter;
        }

        @Override
        public <S> DataResult<T> decode(DynamicOps<S> ops, MapLike<S> input) {
            for (MapDecoder mapDecoder : this.codecs) {
                DataResult dataResult = mapDecoder.decode(ops, input);
                if (!dataResult.result().isPresent()) continue;
                return dataResult;
            }
            return DataResult.error(() -> "No matching codec found");
        }

        @Override
        public <S> RecordBuilder<S> encode(T input, DynamicOps<S> ops, RecordBuilder<S> prefix) {
            MapEncoder<S> mapEncoder = this.codecGetter.apply(input);
            return mapEncoder.encode(input, ops, prefix);
        }

        @Override
        public <S> Stream<S> keys(DynamicOps<S> ops) {
            return this.codecs.stream().flatMap((? super T codec) -> codec.keys(ops)).distinct();
        }

        public String toString() {
            return "FuzzyCodec[" + String.valueOf(this.codecs) + "]";
        }
    }

    static class DispatchingCodec<T>
    extends MapCodec<T> {
        private final String dispatchingKey;
        private final MapCodec<T> withKeyCodec;
        private final MapCodec<T> withoutKeyCodec;

        public DispatchingCodec(String dispatchingKey, MapCodec<T> withKeyCodec, MapCodec<T> withoutKeyCodec) {
            this.dispatchingKey = dispatchingKey;
            this.withKeyCodec = withKeyCodec;
            this.withoutKeyCodec = withoutKeyCodec;
        }

        @Override
        public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
            if (input.get(this.dispatchingKey) != null) {
                return this.withKeyCodec.decode(ops, input);
            }
            return this.withoutKeyCodec.decode(ops, input);
        }

        @Override
        public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
            return this.withoutKeyCodec.encode(input, ops, prefix);
        }

        @Override
        public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
            return Stream.concat(this.withKeyCodec.keys(ops), this.withoutKeyCodec.keys(ops)).distinct();
        }
    }
}

