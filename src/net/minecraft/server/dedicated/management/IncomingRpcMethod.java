/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.registry.Registry;
import net.minecraft.server.dedicated.management.MethodDefintionException;
import net.minecraft.server.dedicated.management.RpcEncodingException;
import net.minecraft.server.dedicated.management.RpcException;
import net.minecraft.server.dedicated.management.RpcMethodInfo;
import net.minecraft.server.dedicated.management.RpcRequestParameter;
import net.minecraft.server.dedicated.management.RpcResponseResult;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface IncomingRpcMethod {
    public RpcMethodInfo info();

    public Attributes attributes();

    public JsonElement handle(ManagementHandlerDispatcher var1, @Nullable JsonElement var2, ManagementConnectionId var3);

    public static <Result> Builder<Parameterless<Result>> createParameterlessBuilder(ParameterlessHandler<Result> handler, Codec<Result> resultCodec) {
        return new Builder<Parameterless<Result>>((methodInfo, attributes) -> {
            if (methodInfo.params().isPresent()) {
                throw new MethodDefintionException("Method defined as not having parameters but is describing them");
            }
            if (methodInfo.result().isEmpty()) {
                throw new MethodDefintionException("Method lacks result");
            }
            return new Parameterless(methodInfo, attributes, resultCodec, handler);
        });
    }

    public static <Params, Result> Builder<Parameterized<Params, Result>> createParameterizedBuilder(ParameterizedHandler<Params, Result> handler, Codec<Params> paramsCodec, Codec<Result> resultCodec) {
        return new Builder<Parameterized<Params, Result>>((methodInfo, attributes) -> {
            if (methodInfo.params().isEmpty()) {
                throw new MethodDefintionException("Method defined as having parameters without describing them");
            }
            if (methodInfo.result().isEmpty()) {
                throw new MethodDefintionException("Method lacks result");
            }
            return new Parameterized(methodInfo, attributes, paramsCodec, resultCodec, handler);
        });
    }

    public static <Result> Builder<Parameterless<Result>> createParameterlessBuilder(Function<ManagementHandlerDispatcher, Result> handler, Codec<Result> resultCodec) {
        return new Builder<Parameterless<Result>>((methodInfo, attributes) -> {
            if (methodInfo.params().isPresent()) {
                throw new MethodDefintionException("Method defined as not having parameters but is describing them");
            }
            if (methodInfo.result().isEmpty()) {
                throw new MethodDefintionException("Method lacks result");
            }
            return new Parameterless<Object>(methodInfo, attributes, resultCodec, (dispatcher, remote) -> handler.apply(dispatcher));
        });
    }

    public static class Builder<T extends IncomingRpcMethod> {
        private final Factory<T> factory;
        private String description = "";
        @Nullable
        private RpcRequestParameter params;
        @Nullable
        private RpcResponseResult result;
        private boolean runOnMainThread = true;
        private boolean discoverable = true;

        public Builder(Factory<T> factory) {
            this.factory = factory;
        }

        public Builder<T> description(String description) {
            this.description = description;
            return this;
        }

        public Builder<T> result(RpcResponseResult result) {
            this.result = result;
            return this;
        }

        public Builder<T> parameter(RpcRequestParameter parameter) {
            this.params = parameter;
            return this;
        }

        public Builder<T> noRequireMainThread() {
            this.runOnMainThread = false;
            return this;
        }

        public Builder<T> notDiscoverable() {
            this.discoverable = false;
            return this;
        }

        public T build() {
            RpcMethodInfo lv = new RpcMethodInfo(this.description, this.params, this.result);
            return this.factory.create(lv, new Attributes(this.discoverable, this.runOnMainThread));
        }

        public T buildAndRegisterVanilla(Registry<IncomingRpcMethod> registry, String path) {
            return this.buildAndRegister(registry, Identifier.ofVanilla(path));
        }

        private T buildAndRegister(Registry<IncomingRpcMethod> registry, Identifier id) {
            return (T)((IncomingRpcMethod)Registry.register(registry, id, this.build()));
        }
    }

    @FunctionalInterface
    public static interface ParameterlessHandler<Result> {
        public Result apply(ManagementHandlerDispatcher var1, ManagementConnectionId var2);
    }

    @FunctionalInterface
    public static interface Factory<T extends IncomingRpcMethod> {
        public T create(RpcMethodInfo var1, Attributes var2);
    }

    @FunctionalInterface
    public static interface ParameterizedHandler<Params, Result> {
        public Result apply(ManagementHandlerDispatcher var1, Params var2, ManagementConnectionId var3);
    }

    public record Parameterless<Result>(RpcMethodInfo info, Attributes attributes, Codec<Result> resultCodec, ParameterlessHandler<Result> handler) implements IncomingRpcMethod
    {
        @Override
        public JsonElement handle(ManagementHandlerDispatcher dispatcher, @Nullable JsonElement parameters, ManagementConnectionId remote) {
            if (!(parameters == null || parameters.isJsonArray() && parameters.getAsJsonArray().isEmpty())) {
                throw new RpcException("Expected no params, or an empty array");
            }
            if (this.info.params().isPresent()) {
                throw new IllegalArgumentException("Method defined as not having parameters but is describing them");
            }
            Result object = this.handler.apply(dispatcher, remote);
            return this.resultCodec.encodeStart(JsonOps.INSTANCE, object).getOrThrow(RpcException::new);
        }
    }

    public record Attributes(boolean runOnMainThread, boolean discoverable) {
    }

    public record Parameterized<Params, Result>(RpcMethodInfo info, Attributes attributes, Codec<Params> paramsCodec, Codec<Result> resultCodec, ParameterizedHandler<Params, Result> handler) implements IncomingRpcMethod
    {
        @Override
        public JsonElement handle(ManagementHandlerDispatcher dispatcher, @Nullable JsonElement parameters, ManagementConnectionId remote) {
            JsonElement jsonElement3;
            if (parameters == null || !parameters.isJsonArray() && !parameters.isJsonObject()) {
                throw new RpcException("Expected params as array or named");
            }
            if (this.info.params().isEmpty()) {
                throw new IllegalArgumentException("Method defined as having parameters without describing them");
            }
            if (parameters.isJsonObject()) {
                String string = this.info.params().get().name();
                JsonElement jsonElement2 = parameters.getAsJsonObject().get(string);
                if (jsonElement2 == null) {
                    throw new RpcException(String.format(Locale.ROOT, "Params passed by-name, but expected param [%s] does not exist", string));
                }
                jsonElement3 = jsonElement2;
            } else {
                JsonArray jsonArray = parameters.getAsJsonArray();
                if (jsonArray.isEmpty() || jsonArray.size() > 1) {
                    throw new RpcException("Expected exactly one element in the params array");
                }
                jsonElement3 = jsonArray.get(0);
            }
            Object object = this.paramsCodec.parse(JsonOps.INSTANCE, jsonElement3).getOrThrow(RpcException::new);
            Result object2 = this.handler.apply(dispatcher, object, remote);
            return this.resultCodec.encodeStart(JsonOps.INSTANCE, object2).getOrThrow(RpcEncodingException::new);
        }
    }
}

