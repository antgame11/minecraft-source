/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.List;

public class WebSocketFrameToJsonElementDecoder
extends MessageToMessageDecoder<TextWebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame, List<Object> list) {
        JsonElement jsonElement = JsonParser.parseString(textWebSocketFrame.text());
        list.add(jsonElement);
    }

    @Override
    protected /* synthetic */ void decode(ChannelHandlerContext context, Object frame, List out) throws Exception {
        this.decode(context, (TextWebSocketFrame)frame, (List<Object>)out);
    }
}

