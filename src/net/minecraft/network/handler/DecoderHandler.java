/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.handler.NetworkStateTransitionHandler;
import net.minecraft.network.handler.PacketException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.state.NetworkState;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import org.slf4j.Logger;

public class DecoderHandler<T extends PacketListener>
extends ByteToMessageDecoder
implements NetworkStateTransitionHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NetworkState<T> state;

    public DecoderHandler(NetworkState<T> state) {
        this.state = state;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf buf, List<Object> objects) throws Exception {
        Packet lv;
        int i = buf.readableBytes();
        try {
            lv = (Packet)this.state.codec().decode(buf);
        } catch (Exception exception) {
            if (exception instanceof PacketException) {
                buf.skipBytes(buf.readableBytes());
            }
            throw exception;
        }
        PacketType lv2 = lv.getPacketType();
        FlightProfiler.INSTANCE.onPacketReceived(this.state.id(), lv2, context.channel().remoteAddress(), i);
        if (buf.readableBytes() > 0) {
            throw new IOException("Packet " + this.state.id().getId() + "/" + String.valueOf(lv2) + " (" + lv.getClass().getSimpleName() + ") was larger than I expected, found " + buf.readableBytes() + " bytes extra whilst reading packet " + String.valueOf(lv2));
        }
        objects.add(lv);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(ClientConnection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {} -> {} bytes", this.state.id().getId(), lv2, lv.getClass().getName(), i);
        }
        NetworkStateTransitionHandler.onDecoded(context, lv);
    }
}

