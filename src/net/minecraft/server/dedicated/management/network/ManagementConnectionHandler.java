/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management.network;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.dedicated.management.IncomingRpcMethod;
import net.minecraft.server.dedicated.management.InvalidRpcRequestException;
import net.minecraft.server.dedicated.management.JsonRpc;
import net.minecraft.server.dedicated.management.ManagementError;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.ManagementServer;
import net.minecraft.server.dedicated.management.OutgoingRpcMethod;
import net.minecraft.server.dedicated.management.PendingResponse;
import net.minecraft.server.dedicated.management.RpcEncodingException;
import net.minecraft.server.dedicated.management.RpcException;
import net.minecraft.server.dedicated.management.RpcMethodNotFoundException;
import net.minecraft.server.dedicated.management.RpcRemoteErrorException;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ManagementConnectionHandler
extends SimpleChannelInboundHandler<JsonElement> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
    private final ManagementLogger managementLogger;
    private final ManagementConnectionId remote;
    private final ManagementServer managementServer;
    private final Channel channel;
    private final ManagementHandlerDispatcher handlerDispatcher;
    private final AtomicInteger OUTGOING_REQUEST_ID = new AtomicInteger();
    private final Int2ObjectMap<PendingResponse<?>> pendingResponses = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap());

    public ManagementConnectionHandler(Channel channel, ManagementServer managementServer, ManagementHandlerDispatcher handlerDispatcher, ManagementLogger managementLogger) {
        this.remote = ManagementConnectionId.of(CONNECTION_ID.incrementAndGet());
        this.managementServer = managementServer;
        this.handlerDispatcher = handlerDispatcher;
        this.channel = channel;
        this.managementLogger = managementLogger;
    }

    public void processTimeouts() {
        long l = Util.getMeasuringTimeMs();
        this.pendingResponses.int2ObjectEntrySet().removeIf(responseEntry -> {
            boolean bl = ((PendingResponse)responseEntry.getValue()).shouldTimeout(l);
            if (bl) {
                ((PendingResponse)responseEntry.getValue()).resultFuture().completeExceptionally(new ReadTimeoutException("RPC method " + String.valueOf(((PendingResponse)responseEntry.getValue()).method().registryKey().getValue()) + " timed out waiting for response"));
            }
            return bl;
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        this.managementLogger.logAction(this.remote, "Management connection opened for {}", this.channel.remoteAddress());
        super.channelActive(context);
        this.managementServer.onConnectionOpen(this);
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        this.managementLogger.logAction(this.remote, "Management connection closed for {}", this.channel.remoteAddress());
        super.channelInactive(context);
        this.managementServer.onConnectionClose(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable throwable) throws Exception {
        if (throwable.getCause() instanceof JsonParseException) {
            this.channel.writeAndFlush(ManagementError.PARSE_ERROR.encode(throwable.getMessage()));
            return;
        }
        super.exceptionCaught(context, throwable);
        this.channel.close().awaitUninterruptibly();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = this.handleMessage(jsonElement.getAsJsonObject());
            if (jsonObject != null) {
                this.channel.writeAndFlush(jsonObject);
            }
        } else if (jsonElement.isJsonArray()) {
            this.channel.writeAndFlush(this.handleEach(jsonElement.getAsJsonArray().asList()));
        } else {
            this.channel.writeAndFlush(ManagementError.INVALID_REQUEST.encode((String)null));
        }
    }

    private JsonArray handleEach(List<JsonElement> messages) {
        JsonArray jsonArray = new JsonArray();
        messages.stream().map(message -> this.handleMessage(message.getAsJsonObject())).filter(Objects::nonNull).forEach(jsonArray::add);
        return jsonArray;
    }

    public void sendNotification(RegistryEntry.Reference<? extends OutgoingRpcMethod<Void, ?>> method) {
        this.sendRequest(method, null, false);
    }

    public <Params> void sendNotification(RegistryEntry.Reference<? extends OutgoingRpcMethod<Params, ?>> method, Params params) {
        this.sendRequest(method, params, false);
    }

    public <Result> CompletableFuture<Result> sendRequest(RegistryEntry.Reference<? extends OutgoingRpcMethod<Void, Result>> method) {
        return this.sendRequest(method, null, true);
    }

    public <Params, Result> CompletableFuture<Result> sendRequest(RegistryEntry.Reference<? extends OutgoingRpcMethod<Params, Result>> method, Params params) {
        return this.sendRequest(method, params, true);
    }

    @Nullable
    @Contract(value="_,_,false->null;_,_,true->!null")
    private <Params, Result> CompletableFuture<Result> sendRequest(RegistryEntry.Reference<? extends OutgoingRpcMethod<Params, ? extends Result>> method, @Nullable Params params, boolean expectResponse) {
        List<JsonElement> list;
        List<JsonElement> list2 = list = params != null ? List.of(Objects.requireNonNull(method.value().encodeParams(params))) : List.of();
        if (expectResponse) {
            CompletableFuture completableFuture = new CompletableFuture();
            int i = this.OUTGOING_REQUEST_ID.incrementAndGet();
            long l = Util.nanoTimeSupplier.get(TimeUnit.MILLISECONDS);
            this.pendingResponses.put(i, (PendingResponse<?>)new PendingResponse(method, completableFuture, l + 5000L));
            this.channel.writeAndFlush(JsonRpc.encodeRequest(i, method.registryKey().getValue(), list));
            return completableFuture;
        }
        this.channel.writeAndFlush(JsonRpc.encodeRequest(null, method.registryKey().getValue(), list));
        return null;
    }

    @Nullable
    @VisibleForTesting
    JsonObject handleMessage(JsonObject request) {
        try {
            JsonElement jsonElement = JsonRpc.getId(request);
            String string = JsonRpc.getMethod(request);
            JsonElement jsonElement2 = JsonRpc.getResult(request);
            JsonElement jsonElement3 = JsonRpc.getParameters(request);
            JsonObject jsonObject2 = JsonRpc.getError(request);
            if (string != null && jsonElement2 == null && jsonObject2 == null) {
                if (jsonElement != null && !ManagementConnectionHandler.isValidRequestId(jsonElement)) {
                    return ManagementError.INVALID_REQUEST.encode("Invalid request id - only String, Number and NULL supported");
                }
                return this.handleRequest(jsonElement, string, jsonElement3);
            }
            if (string == null && jsonElement2 != null && jsonObject2 == null && jsonElement != null) {
                if (ManagementConnectionHandler.isValidResponseId(jsonElement)) {
                    this.handleResponse(jsonElement.getAsInt(), jsonElement2);
                } else {
                    LOGGER.warn("Received respose {} with id {} we did not request", (Object)jsonElement2, (Object)jsonElement);
                }
                return null;
            }
            if (string == null && jsonElement2 == null && jsonObject2 != null) {
                return this.handleError(jsonElement, jsonObject2);
            }
            return ManagementError.INVALID_REQUEST.encode(Objects.requireNonNullElse(jsonElement, JsonNull.INSTANCE));
        } catch (Exception exception) {
            LOGGER.error("Error while handling rpc request", exception);
            return ManagementError.INTERNAL_ERROR.encode("Unknown error handling request - check server logs for stack trace");
        }
    }

    private static boolean isValidRequestId(JsonElement json) {
        return json.isJsonNull() || JsonHelper.isNumber(json) || JsonHelper.isString(json);
    }

    private static boolean isValidResponseId(JsonElement json) {
        return JsonHelper.isNumber(json);
    }

    @Nullable
    private JsonObject handleRequest(@Nullable JsonElement json, String method, @Nullable JsonElement parameters) {
        boolean bl = json != null;
        try {
            JsonElement jsonElement3 = this.processRequest(method, parameters);
            if (jsonElement3 == null || !bl) {
                return null;
            }
            return JsonRpc.encodeResult(json, jsonElement3);
        } catch (RpcException lv) {
            LOGGER.debug("Invalid parameter invocation {}: {}, {}", method, parameters, lv.getMessage());
            return bl ? ManagementError.INVALID_PARAMS.encode(json, lv.getMessage()) : null;
        } catch (RpcEncodingException lv2) {
            LOGGER.error("Failed to encode json rpc response {}: {}", (Object)method, (Object)lv2.getMessage());
            return bl ? ManagementError.INTERNAL_ERROR.encode(json, lv2.getMessage()) : null;
        } catch (InvalidRpcRequestException lv3) {
            return bl ? ManagementError.INVALID_REQUEST.encode(json, lv3.getMessage()) : null;
        } catch (RpcMethodNotFoundException lv4) {
            return bl ? ManagementError.METHOD_NOT_FOUND.encode(json, lv4.getMessage()) : null;
        } catch (Exception exception) {
            LOGGER.error("Error while dispatching rpc method {}", (Object)method, (Object)exception);
            return bl ? ManagementError.INTERNAL_ERROR.encode(json) : null;
        }
    }

    @Nullable
    public JsonElement processRequest(String method, @Nullable JsonElement json) {
        Identifier lv = Identifier.tryParse(method);
        if (lv == null) {
            throw new InvalidRpcRequestException("Failed to parse method value: " + method);
        }
        Optional<IncomingRpcMethod> optional = Registries.INCOMING_RPC_METHOD.getOptionalValue(lv);
        if (optional.isEmpty()) {
            throw new RpcMethodNotFoundException("Method not found: " + method);
        }
        if (optional.get().attributes().runOnMainThread()) {
            try {
                return this.handlerDispatcher.submit(() -> ((IncomingRpcMethod)optional.get()).handle(this.handlerDispatcher, json, this.remote)).join();
            } catch (CompletionException completionException) {
                Throwable throwable = completionException.getCause();
                if (throwable instanceof RuntimeException) {
                    RuntimeException runtimeException = (RuntimeException)throwable;
                    throw runtimeException;
                }
                throw completionException;
            }
        }
        return optional.get().handle(this.handlerDispatcher, json, this.remote);
    }

    private void handleResponse(int id, JsonElement result) {
        PendingResponse lv = (PendingResponse)this.pendingResponses.remove(id);
        if (lv == null) {
            LOGGER.warn("Received unknown response (id: {}): {}", (Object)id, (Object)result);
        } else {
            lv.handleResponse(result);
        }
    }

    @Nullable
    private JsonObject handleError(@Nullable JsonElement json, JsonObject error) {
        PendingResponse lv;
        if (json != null && ManagementConnectionHandler.isValidResponseId(json) && (lv = (PendingResponse)this.pendingResponses.remove(json.getAsInt())) != null) {
            lv.resultFuture().completeExceptionally(new RpcRemoteErrorException(json, error));
        }
        LOGGER.error("Received error (id: {}): {}", (Object)json, (Object)error);
        return null;
    }

    @Override
    protected /* synthetic */ void channelRead0(ChannelHandlerContext context, Object in) throws Exception {
        this.channelRead0(context, (JsonElement)in);
    }
}

