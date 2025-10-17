/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.component;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;

public interface ComponentPredicate {
    public static final Codec<Map<Type<?>, ComponentPredicate>> PREDICATES_MAP_CODEC = Codec.dispatchedMap(Registries.DATA_COMPONENT_PREDICATE_TYPE.getCodec(), Type::getPredicateCodec);
    public static final PacketCodec<RegistryByteBuf, Typed<?>> SINGLE_PREDICATE_PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.DATA_COMPONENT_PREDICATE_TYPE).dispatch(Typed::type, Type::getTypedPacketCodec);
    public static final PacketCodec<RegistryByteBuf, Map<Type<?>, ComponentPredicate>> PREDICATES_MAP_PACKET_CODEC = SINGLE_PREDICATE_PACKET_CODEC.collect(PacketCodecs.toList(64)).xmap(list -> list.stream().collect(Collectors.toMap(Typed::type, Typed::predicate)), map -> map.entrySet().stream().map(Typed::fromEntry).toList());

    public static MapCodec<Typed<?>> createCodec(String predicateFieldName) {
        return Registries.DATA_COMPONENT_PREDICATE_TYPE.getCodec().dispatchMap(predicateFieldName, Typed::type, Type::getTypedCodec);
    }

    public boolean test(ComponentsAccess var1);

    public record Typed<T extends ComponentPredicate>(Type<T> type, T predicate) {
        private static <T extends ComponentPredicate> Typed<T> fromEntry(Map.Entry<Type<?>, T> entry) {
            return new Typed<ComponentPredicate>(entry.getKey(), (ComponentPredicate)entry.getValue());
        }
    }

    public static final class Type<T extends ComponentPredicate> {
        private final Codec<T> predicateCodec;
        private final MapCodec<Typed<T>> typedCodec;
        private final PacketCodec<RegistryByteBuf, Typed<T>> typedPacketCodec;

        public Type(Codec<T> predicateCodec) {
            this.predicateCodec = predicateCodec;
            this.typedCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)predicateCodec.fieldOf("value")).forGetter(Typed::predicate)).apply((Applicative<Typed, ?>)instance, predicate -> new Typed<ComponentPredicate>(this, (ComponentPredicate)predicate)));
            this.typedPacketCodec = PacketCodecs.registryCodec(predicateCodec).xmap(predicate -> new Typed<ComponentPredicate>(this, (ComponentPredicate)predicate), Typed::predicate);
        }

        public Codec<T> getPredicateCodec() {
            return this.predicateCodec;
        }

        public MapCodec<Typed<T>> getTypedCodec() {
            return this.typedCodec;
        }

        public PacketCodec<RegistryByteBuf, Typed<T>> getTypedPacketCodec() {
            return this.typedPacketCodec;
        }
    }
}

