/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet;

import net.minecraft.network.codec.PacketCodec;

@FunctionalInterface
public interface PacketCodecModifier<B, V, C> {
    public PacketCodec<? super B, V> apply(PacketCodec<? super B, V> var1, C var2);
}

