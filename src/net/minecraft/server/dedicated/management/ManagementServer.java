/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management;

import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.BearerAuthenticationHandler;
import net.minecraft.server.dedicated.management.network.JsonElementToWebSocketFrameEncoder;
import net.minecraft.server.dedicated.management.network.ManagementConnectionHandler;
import net.minecraft.server.dedicated.management.network.WebSocketFrameToJsonElementDecoder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ManagementServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final HostAndPort address;
    final BearerAuthenticationHandler authHandler;
    @Nullable
    private Channel channel;
    private final NioEventLoopGroup eventLoopGroup;
    private final Set<ManagementConnectionHandler> connectionHandlers = Sets.newIdentityHashSet();

    public ManagementServer(HostAndPort address, BearerAuthenticationHandler authHandler) {
        this.address = address;
        this.authHandler = authHandler;
        this.eventLoopGroup = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Management server IO #%d").setDaemon(true).build());
    }

    public ManagementServer(HostAndPort adress, BearerAuthenticationHandler authHandler, NioEventLoopGroup eventLoopGroup) {
        this.address = adress;
        this.authHandler = authHandler;
        this.eventLoopGroup = eventLoopGroup;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void onConnectionOpen(ManagementConnectionHandler handler) {
        Set<ManagementConnectionHandler> set = this.connectionHandlers;
        synchronized (set) {
            this.connectionHandlers.add(handler);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void onConnectionClose(ManagementConnectionHandler handler) {
        Set<ManagementConnectionHandler> set = this.connectionHandlers;
        synchronized (set) {
            this.connectionHandlers.remove(handler);
        }
    }

    public void listenUnencrypted(ManagementHandlerDispatcher dispatcher) {
        this.listen(dispatcher, null);
    }

    public void listenEncrypted(ManagementHandlerDispatcher dispatcher, SslContext sslContext) {
        this.listen(dispatcher, sslContext);
    }

    private void listen(final ManagementHandlerDispatcher dispatcher, final @Nullable SslContext sslContext) {
        final ManagementLogger lv = new ManagementLogger();
        ChannelFuture channelFuture = ((ServerBootstrap)((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().handler(new LoggingHandler(LogLevel.DEBUG))).channel(NioServerSocketChannel.class)).childHandler(new ChannelInitializer<Channel>(){

            @Override
            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException channelException) {
                    // empty catch block
                }
                ChannelPipeline channelPipeline = channel.pipeline();
                if (sslContext != null) {
                    channelPipeline.addLast(sslContext.newHandler(channel.alloc()));
                }
                channelPipeline.addLast(new HttpServerCodec()).addLast(new HttpObjectAggregator(65536)).addLast(ManagementServer.this.authHandler).addLast(new WebSocketServerProtocolHandler("/")).addLast(new WebSocketFrameToJsonElementDecoder()).addLast(new JsonElementToWebSocketFrameEncoder()).addLast(new ManagementConnectionHandler(channel, ManagementServer.this, dispatcher, lv));
            }
        }).group(this.eventLoopGroup).localAddress(this.address.getHost(), this.address.getPort())).bind();
        this.channel = channelFuture.channel();
        channelFuture.syncUninterruptibly();
        LOGGER.info("Json-RPC Management connection listening on {}:{}", (Object)this.address.getHost(), (Object)this.getPort());
    }

    public void stop(boolean shutdownEventLoop) throws InterruptedException {
        if (this.channel != null) {
            this.channel.close().sync();
            this.channel = null;
        }
        this.connectionHandlers.clear();
        if (shutdownEventLoop) {
            this.eventLoopGroup.shutdownGracefully().sync();
        }
    }

    public void processTimeouts() {
        this.forEachConnection(ManagementConnectionHandler::processTimeouts);
    }

    public int getPort() {
        return this.channel != null ? ((InetSocketAddress)this.channel.localAddress()).getPort() : this.address.getPort();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void forEachConnection(Consumer<ManagementConnectionHandler> task) {
        Set<ManagementConnectionHandler> set = this.connectionHandlers;
        synchronized (set) {
            this.connectionHandlers.forEach(task);
        }
    }
}

