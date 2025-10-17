/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.dedicated.management.schema.RpcSchema;

public record RpcResponseResult(String name, RpcSchema schema) {
    public static final MapCodec<RpcResponseResult> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("name")).forGetter(RpcResponseResult::name), ((MapCodec)RpcSchema.CODEC.fieldOf("schema")).forGetter(RpcResponseResult::schema)).apply((Applicative<RpcResponseResult, ?>)instance, RpcResponseResult::new));
}

