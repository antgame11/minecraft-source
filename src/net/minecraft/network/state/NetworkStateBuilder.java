/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.state;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.network.handler.SideValidatingDispatchingCodecBuilder;
import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.listener.ServerPacketListener;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.BundleSplitterPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketCodecModifier;
import net.minecraft.network.state.ContextAwareNetworkStateFactory;
import net.minecraft.network.state.NetworkState;
import net.minecraft.network.state.NetworkStateFactory;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

public class NetworkStateBuilder<T extends PacketListener, B extends ByteBuf, C> {
    final NetworkPhase phase;
    final NetworkSide side;
    private final List<PacketType<T, ?, B, C>> packetTypes = new ArrayList();
    @Nullable
    private PacketBundleHandler bundleHandler;

    public NetworkStateBuilder(NetworkPhase phase, NetworkSide side) {
        this.phase = phase;
        this.side = side;
    }

    public <P extends Packet<? super T>> NetworkStateBuilder<T, B, C> add(net.minecraft.network.packet.PacketType<P> type, PacketCodec<? super B, P> codec) {
        this.packetTypes.add(new PacketType(type, codec, null));
        return this;
    }

    public <P extends Packet<? super T>> NetworkStateBuilder<T, B, C> add(net.minecraft.network.packet.PacketType<P> type, PacketCodec<? super B, P> codec, PacketCodecModifier<B, P, C> modifier) {
        this.packetTypes.add(new PacketType(type, codec, modifier));
        return this;
    }

    public <P extends BundlePacket<? super T>, D extends BundleSplitterPacket<? super T>> NetworkStateBuilder<T, B, C> addBundle(net.minecraft.network.packet.PacketType<P> id, Function<Iterable<Packet<? super T>>, P> bundler, D splitter) {
        PacketCodec lv = PacketCodec.unit(splitter);
        net.minecraft.network.packet.PacketType<BundleSplitterPacket<? super T>> lv2 = splitter.getPacketType();
        this.packetTypes.add(new PacketType(lv2, lv, null));
        this.bundleHandler = PacketBundleHandler.create(id, bundler, splitter);
        return this;
    }

    PacketCodec<ByteBuf, Packet<? super T>> createCodec(Function<ByteBuf, B> bufUpgrader, List<PacketType<T, ?, B, C>> packetTypes, C context) {
        SideValidatingDispatchingCodecBuilder lv = new SideValidatingDispatchingCodecBuilder(this.side);
        for (PacketType packetType : packetTypes) {
            packetType.add(lv, bufUpgrader, context);
        }
        return lv.build();
    }

    private static NetworkState.Unbound createState(final NetworkPhase phase, final NetworkSide side, final List<? extends PacketType<?, ?, ?, ?>> types) {
        return new NetworkState.Unbound(){

            @Override
            public NetworkPhase phase() {
                return phase;
            }

            @Override
            public NetworkSide side() {
                return side;
            }

            @Override
            public void forEachPacketType(NetworkState.Unbound.PacketTypeConsumer callback) {
                for (int i = 0; i < types.size(); ++i) {
                    PacketType lv = (PacketType)types.get(i);
                    callback.accept(lv.type, i);
                }
            }
        };
    }

    public NetworkStateFactory<T, B> buildFactory(final C context) {
        final List<PacketType<T, ?, B, C>> list = List.copyOf(this.packetTypes);
        final PacketBundleHandler lv = this.bundleHandler;
        final NetworkState.Unbound lv2 = NetworkStateBuilder.createState(this.phase, this.side, list);
        return new NetworkStateFactory<T, B>(){

            @Override
            public NetworkState<T> bind(Function<ByteBuf, B> registryBinder) {
                return new NetworkStateImpl(NetworkStateBuilder.this.phase, NetworkStateBuilder.this.side, NetworkStateBuilder.this.createCodec(registryBinder, list, context), lv);
            }

            @Override
            public NetworkState.Unbound buildUnbound() {
                return lv2;
            }
        };
    }

    public ContextAwareNetworkStateFactory<T, B, C> buildContextAwareFactory() {
        final List<PacketType<T, ?, B, C>> list = List.copyOf(this.packetTypes);
        final PacketBundleHandler lv = this.bundleHandler;
        final NetworkState.Unbound lv2 = NetworkStateBuilder.createState(this.phase, this.side, list);
        return new ContextAwareNetworkStateFactory<T, B, C>(){

            @Override
            public NetworkState<T> bind(Function<ByteBuf, B> registryBinder, C context) {
                return new NetworkStateImpl(NetworkStateBuilder.this.phase, NetworkStateBuilder.this.side, NetworkStateBuilder.this.createCodec(registryBinder, list, context), lv);
            }

            @Override
            public NetworkState.Unbound buildUnbound() {
                return lv2;
            }
        };
    }

    private static <L extends PacketListener, B extends ByteBuf> NetworkStateFactory<L, B> build(NetworkPhase type, NetworkSide side, Consumer<NetworkStateBuilder<L, B, Unit>> registrar) {
        NetworkStateBuilder lv = new NetworkStateBuilder(type, side);
        registrar.accept(lv);
        return lv.buildFactory(Unit.INSTANCE);
    }

    public static <T extends ServerPacketListener, B extends ByteBuf> NetworkStateFactory<T, B> c2s(NetworkPhase type, Consumer<NetworkStateBuilder<T, B, Unit>> registrar) {
        return NetworkStateBuilder.build(type, NetworkSide.SERVERBOUND, registrar);
    }

    public static <T extends ClientPacketListener, B extends ByteBuf> NetworkStateFactory<T, B> s2c(NetworkPhase type, Consumer<NetworkStateBuilder<T, B, Unit>> registrar) {
        return NetworkStateBuilder.build(type, NetworkSide.CLIENTBOUND, registrar);
    }

    private static <L extends PacketListener, B extends ByteBuf, C> ContextAwareNetworkStateFactory<L, B, C> buildContextAware(NetworkPhase type, NetworkSide side, Consumer<NetworkStateBuilder<L, B, C>> registrar) {
        NetworkStateBuilder lv = new NetworkStateBuilder(type, side);
        registrar.accept(lv);
        return lv.buildContextAwareFactory();
    }

    public static <T extends ServerPacketListener, B extends ByteBuf, C> ContextAwareNetworkStateFactory<T, B, C> contextAwareC2S(NetworkPhase type, Consumer<NetworkStateBuilder<T, B, C>> registrar) {
        return NetworkStateBuilder.buildContextAware(type, NetworkSide.SERVERBOUND, registrar);
    }

    public static <T extends ClientPacketListener, B extends ByteBuf, C> ContextAwareNetworkStateFactory<T, B, C> contextAwareS2C(NetworkPhase type, Consumer<NetworkStateBuilder<T, B, C>> registrar) {
        return NetworkStateBuilder.buildContextAware(type, NetworkSide.CLIENTBOUND, registrar);
    }

    record PacketType<T extends PacketListener, P extends Packet<? super T>, B extends ByteBuf, C>(net.minecraft.network.packet.PacketType<P> type, PacketCodec<? super B, P> codec, @Nullable PacketCodecModifier<B, P, C> modifier) {
        public void add(SideValidatingDispatchingCodecBuilder<ByteBuf, T> builder, Function<ByteBuf, B> bufUpgrader, C context) {
            PacketCodec<Object, P> lv = this.modifier != null ? this.modifier.apply(this.codec, context) : this.codec;
            PacketCodec<ByteBuf, P> lv2 = lv.mapBuf(bufUpgrader);
            builder.add(this.type, lv2);
        }

        @Nullable
        public PacketCodecModifier<B, P, C> modifier() {
            return this.modifier;
        }
    }

    record NetworkStateImpl<L extends PacketListener>(NetworkPhase id, NetworkSide side, PacketCodec<ByteBuf, Packet<? super L>> codec, @Nullable PacketBundleHandler bundleHandler) implements NetworkState<L>
    {
        @Override
        @Nullable
        public PacketBundleHandler bundleHandler() {
            return this.bundleHandler;
        }
    }
}

