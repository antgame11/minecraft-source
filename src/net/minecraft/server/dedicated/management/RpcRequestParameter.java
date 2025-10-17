/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.dedicated.management.schema.RpcSchema;

public record RpcRequestParameter(String name, RpcSchema schema, boolean required) {
    public static final MapCodec<RpcRequestParameter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("name")).forGetter(RpcRequestParameter::name), ((MapCodec)RpcSchema.CODEC.fieldOf("schema")).forGetter(RpcRequestParameter::schema), ((MapCodec)Codec.BOOL.fieldOf("required")).forGetter(RpcRequestParameter::required)).apply((Applicative<RpcRequestParameter, ?>)instance, RpcRequestParameter::new));

    public RpcRequestParameter(String name, RpcSchema schema) {
        this(name, schema, true);
    }
}

