/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import net.minecraft.network.encryption.BearerToken;
import org.slf4j.Logger;

@ChannelHandler.Sharable
public class BearerAuthenticationHandler
extends ChannelInboundHandlerAdapter {
    private final Logger LOGGER = LogUtils.getLogger();
    private static final AttributeKey<Boolean> AUTHENTICATED_KEY = AttributeKey.valueOf("authenticated");
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private final BearerToken token;

    public BearerAuthenticationHandler(BearerToken token) {
        this.token = token;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
        Boolean boolean_;
        String string = this.getHostAddress(context);
        if (object instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest)object;
            Result lv = this.authenticate(httpRequest);
            if (lv.isSuccessful()) {
                context.channel().attr(AUTHENTICATED_KEY).set(true);
            } else {
                this.LOGGER.debug("Authentication rejected for connection with ip {}: {}", (Object)string, (Object)lv.getMessage());
                context.channel().attr(AUTHENTICATED_KEY).set(false);
                this.sendUnauthorizedError(context, lv.getMessage());
                return;
            }
        }
        if (Boolean.TRUE.equals(boolean_ = context.channel().attr(AUTHENTICATED_KEY).get())) {
            super.channelRead(context, object);
        } else {
            this.LOGGER.debug("Dropping unauthenticated connection with ip {}", (Object)string);
            context.close();
        }
    }

    private Result authenticate(HttpRequest request) {
        if (!this.isAuthenticated(request)) {
            return Result.failure("Invalid or missing API key");
        }
        return Result.success();
    }

    private boolean isAuthenticated(HttpRequest request) {
        String string = request.headers().get(AUTHORIZATION_HEADER);
        if (string == null || string.trim().isEmpty()) {
            return false;
        }
        if (!string.startsWith(BEARER_PREFIX)) {
            return false;
        }
        String string2 = string.substring(BEARER_PREFIX.length()).trim();
        return this.tokenMatches(string2);
    }

    public boolean tokenMatches(String requestToken) {
        if (requestToken == null || requestToken.isEmpty()) {
            return false;
        }
        byte[] bs = requestToken.getBytes(StandardCharsets.UTF_8);
        byte[] cs = this.token.secretKey().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(bs, cs);
    }

    private String getHostAddress(ChannelHandlerContext context) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress)context.channel().remoteAddress();
        return inetSocketAddress.getAddress().getHostAddress();
    }

    private void sendUnauthorizedError(ChannelHandlerContext context, String message) {
        String string2 = "{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        byte[] bs = string2.getBytes(StandardCharsets.UTF_8);
        DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED, Unpooled.wrappedBuffer(bs));
        defaultFullHttpResponse.headers().set((CharSequence)HttpHeaderNames.CONTENT_TYPE, (Object)"application/json");
        defaultFullHttpResponse.headers().set((CharSequence)HttpHeaderNames.CONTENT_LENGTH, (Object)bs.length);
        defaultFullHttpResponse.headers().set((CharSequence)HttpHeaderNames.CONNECTION, (Object)"close");
        context.writeAndFlush(defaultFullHttpResponse).addListener((GenericFutureListener<? extends Future<? super Void>>)((GenericFutureListener<Future>)future -> context.close()));
    }

    static class Result {
        private final boolean successful;
        private final String message;

        private Result(boolean successful, String message) {
            this.successful = successful;
            this.message = message;
        }

        public static Result success() {
            return new Result(true, null);
        }

        public static Result failure(String message) {
            return new Result(false, message);
        }

        public boolean isSuccessful() {
            return this.successful;
        }

        public String getMessage() {
            return this.message;
        }
    }
}

