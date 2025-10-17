/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.dedicated.management.MethodDefintionException;
import net.minecraft.server.dedicated.management.RpcMethodInfo;
import net.minecraft.server.dedicated.management.RpcRequestParameter;
import net.minecraft.server.dedicated.management.RpcResponseResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface OutgoingRpcMethod<Params, Result> {
    public static final String NOTIFICATION_PREFIX = "notification/";

    public RpcMethodInfo info();

    public Attributes attributes();

    @Nullable
    default public JsonElement encodeParams(Params params) {
        return null;
    }

    @Nullable
    default public Result decodeResult(JsonElement result) {
        return null;
    }

    public static Builder<Simple> createSimpleBuilder() {
        return new Builder<Simple>((methodInfo, attributes) -> {
            if (methodInfo.params().isPresent()) {
                throw new MethodDefintionException("Method defined as not having parameters but is describing them");
            }
            if (methodInfo.result().isPresent()) {
                throw new MethodDefintionException("Method defined as not having result but is describing it");
            }
            return new Simple(methodInfo, attributes);
        });
    }

    public static <Params> Builder<Notification<Params>> createNotificationBuilder(Codec<Params> paramsCodec) {
        return new Builder<Notification<Params>>((methodInfo, attributes) -> {
            if (methodInfo.params().isEmpty()) {
                throw new MethodDefintionException("Method defined as having parameters without describing them");
            }
            if (methodInfo.result().isPresent()) {
                throw new MethodDefintionException("Method defined as not having result but is describing it");
            }
            return new Notification(methodInfo, attributes, paramsCodec);
        });
    }

    public static <Result> Builder<Parameterless<Result>> createParameterlessBuilder(Codec<Result> resultCodec) {
        return new Builder<Parameterless<Result>>((methodInfo, attributes) -> {
            if (methodInfo.params().isPresent()) {
                throw new MethodDefintionException("Method defined as not having parameters but is describing them");
            }
            if (methodInfo.result().isEmpty()) {
                throw new MethodDefintionException("Method lacks result");
            }
            return new Parameterless(methodInfo, attributes, resultCodec);
        });
    }

    public static <Params, Result> Builder<Parameterized<Params, Result>> createParameterizedBuilder(Codec<Params> paramsCodec, Codec<Result> resultCodec) {
        return new Builder<Parameterized<Params, Result>>((methodInfo, attributes) -> {
            if (methodInfo.params().isEmpty()) {
                throw new MethodDefintionException("Method defined as having parameters without describing them");
            }
            if (methodInfo.result().isEmpty()) {
                throw new MethodDefintionException("Method lacks result");
            }
            return new Parameterized(methodInfo, attributes, paramsCodec, resultCodec);
        });
    }

    public static class Builder<T extends OutgoingRpcMethod<?, ?>> {
        public static final Attributes DEFAULT_ATTRIBUTES = new Attributes(true);
        private final Factory<T> factory;
        private String description = "";
        @Nullable
        private RpcRequestParameter requestParameter;
        @Nullable
        private RpcResponseResult responseResult;

        public Builder(Factory<T> factory) {
            this.factory = factory;
        }

        public Builder<T> description(String description) {
            this.description = description;
            return this;
        }

        public Builder<T> responseResult(RpcResponseResult responseResult) {
            this.responseResult = responseResult;
            return this;
        }

        public Builder<T> requestParameter(RpcRequestParameter requestParameter) {
            this.requestParameter = requestParameter;
            return this;
        }

        private T build() {
            RpcMethodInfo lv = new RpcMethodInfo(this.description, this.requestParameter, this.responseResult);
            return this.factory.create(lv, DEFAULT_ATTRIBUTES);
        }

        public RegistryEntry.Reference<T> buildAndRegisterVanilla(String path) {
            return this.buildAndRegister(Identifier.ofVanilla(OutgoingRpcMethod.NOTIFICATION_PREFIX + path));
        }

        private RegistryEntry.Reference<T> buildAndRegister(Identifier id) {
            return Registry.registerReference(Registries.OUTGOING_RPC_METHOD, id, this.build());
        }
    }

    @FunctionalInterface
    public static interface Factory<T extends OutgoingRpcMethod<?, ?>> {
        public T create(RpcMethodInfo var1, Attributes var2);
    }

    public record Parameterized<Params, Result>(RpcMethodInfo info, Attributes attributes, Codec<Params> paramsCodec, Codec<Result> resultCodec) implements OutgoingRpcMethod<Params, Result>
    {
        @Override
        @Nullable
        public JsonElement encodeParams(Params params) {
            return this.paramsCodec.encodeStart(JsonOps.INSTANCE, params).getOrThrow();
        }

        @Override
        public Result decodeResult(JsonElement result) {
            return (Result)this.resultCodec.parse(JsonOps.INSTANCE, result).getOrThrow();
        }
    }

    public record Attributes(boolean discoverable) {
    }

    public record Parameterless<Result>(RpcMethodInfo info, Attributes attributes, Codec<Result> resultCodec) implements OutgoingRpcMethod<Void, Result>
    {
        @Override
        public Result decodeResult(JsonElement result) {
            return (Result)this.resultCodec.parse(JsonOps.INSTANCE, result).getOrThrow();
        }
    }

    public record Notification<Params>(RpcMethodInfo info, Attributes attributes, Codec<Params> paramsCodec) implements OutgoingRpcMethod<Params, Void>
    {
        @Override
        @Nullable
        public JsonElement encodeParams(Params params) {
            return this.paramsCodec.encodeStart(JsonOps.INSTANCE, params).getOrThrow();
        }
    }

    public record Simple(RpcMethodInfo info, Attributes attributes) implements OutgoingRpcMethod<Void, Void>
    {
    }
}

