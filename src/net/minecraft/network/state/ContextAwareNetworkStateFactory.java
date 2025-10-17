/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.state;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.state.NetworkState;

public interface ContextAwareNetworkStateFactory<T extends PacketListener, B extends ByteBuf, C>
extends NetworkState.Factory {
    public NetworkState<T> bind(Function<ByteBuf, B> var1, C var2);
}

