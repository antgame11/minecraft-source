/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.management;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class JsonRpc {
    public static final String JSON_RPC_VERSION = "2.0";
    public static final String field_62309 = "1.3.2";

    public static JsonObject encodeResult(JsonElement id, JsonElement result) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jsonrpc", JSON_RPC_VERSION);
        jsonObject.add("id", id);
        jsonObject.add("result", result);
        return jsonObject;
    }

    public static JsonObject encodeRequest(@Nullable Integer id, Identifier method, List<JsonElement> parameters) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jsonrpc", JSON_RPC_VERSION);
        if (id != null) {
            jsonObject.addProperty("id", id);
        }
        jsonObject.addProperty("method", method.toString());
        if (!parameters.isEmpty()) {
            JsonArray jsonArray = new JsonArray(parameters.size());
            for (JsonElement jsonElement : parameters) {
                jsonArray.add(jsonElement);
            }
            jsonObject.add("params", jsonArray);
        }
        return jsonObject;
    }

    public static JsonObject encodeError(JsonElement id, String message, int code, @Nullable String data) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jsonrpc", JSON_RPC_VERSION);
        jsonObject.add("id", id);
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("code", code);
        jsonObject2.addProperty("message", message);
        if (data != null && !data.isBlank()) {
            jsonObject2.addProperty("data", data);
        }
        jsonObject.add("error", jsonObject2);
        return jsonObject;
    }

    @Nullable
    public static JsonElement getId(JsonObject request) {
        return request.get("id");
    }

    @Nullable
    public static String getMethod(JsonObject request) {
        return JsonHelper.getString(request, "method", null);
    }

    @Nullable
    public static JsonElement getParameters(JsonObject request) {
        return request.get("params");
    }

    @Nullable
    public static JsonElement getResult(JsonObject response) {
        return response.get("result");
    }

    @Nullable
    public static JsonObject getError(JsonObject response) {
        return JsonHelper.getObject(response, "error", null);
    }
}

