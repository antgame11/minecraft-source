/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.dedicated.management.RpcRequestParameter;
import net.minecraft.server.dedicated.management.RpcResponseResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record RpcMethodInfo(String description, Optional<RpcRequestParameter> params, Optional<RpcResponseResult> result) {
    public static final Codec<Optional<RpcRequestParameter>> PARAMETER_LIST_CODEC = RpcRequestParameter.CODEC.codec().listOf().xmap(params -> params.stream().findAny(), param -> param.map(List::of).orElse(List.of()));
    public static final MapCodec<RpcMethodInfo> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("description")).forGetter(RpcMethodInfo::description), ((MapCodec)PARAMETER_LIST_CODEC.fieldOf("params")).forGetter(RpcMethodInfo::params), RpcResponseResult.CODEC.codec().optionalFieldOf("result").forGetter(RpcMethodInfo::result)).apply((Applicative<RpcMethodInfo, ?>)instance, RpcMethodInfo::new));

    public RpcMethodInfo(String description, @Nullable RpcRequestParameter param, @Nullable RpcResponseResult result) {
        this(description, Optional.ofNullable(param), Optional.ofNullable(result));
    }

    public Entry toEntry(Identifier name) {
        return new Entry(name, this);
    }

    public record Entry(Identifier name, RpcMethodInfo contents) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("name")).forGetter(Entry::name), CODEC.forGetter(Entry::contents)).apply((Applicative<Entry, ?>)instance, Entry::new));
    }
}

