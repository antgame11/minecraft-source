/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFutureListener;
import java.util.function.Supplier;
import net.minecraft.network.packet.Packet;
import org.slf4j.Logger;

public class PacketCallbacks {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ChannelFutureListener always(Runnable runnable) {
        return channelFuture -> {
            runnable.run();
            if (!channelFuture.isSuccess()) {
                channelFuture.channel().pipeline().fireExceptionCaught(channelFuture.cause());
            }
        };
    }

    public static ChannelFutureListener of(Supplier<Packet<?>> failurePacket) {
        return channelFuture -> {
            if (!channelFuture.isSuccess()) {
                Packet lv = (Packet)failurePacket.get();
                if (lv != null) {
                    LOGGER.warn("Failed to deliver packet, sending fallback {}", (Object)lv.getPacketType(), (Object)channelFuture.cause());
                    channelFuture.channel().writeAndFlush(lv, channelFuture.channel().voidPromise());
                } else {
                    channelFuture.channel().pipeline().fireExceptionCaught(channelFuture.cause());
                }
            }
        };
    }
}

