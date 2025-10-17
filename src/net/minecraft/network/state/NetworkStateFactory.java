/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.state;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.state.NetworkState;

public interface NetworkStateFactory<T extends PacketListener, B extends ByteBuf>
extends NetworkState.Factory {
    public NetworkState<T> bind(Function<ByteBuf, B> var1);
}

