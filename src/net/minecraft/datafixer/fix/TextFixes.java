/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.StrictJsonParser;

public class TextFixes {
    private static final String EMPTY_TEXT = TextFixes.text("");

    public static <T> Dynamic<T> text(DynamicOps<T> ops, String string) {
        String string2 = TextFixes.text(string);
        return new Dynamic<T>(ops, ops.createString(string2));
    }

    public static <T> Dynamic<T> empty(DynamicOps<T> ops) {
        return new Dynamic<T>(ops, ops.createString(EMPTY_TEXT));
    }

    public static String text(String string) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", string);
        return JsonHelper.toSortedString(jsonObject);
    }

    public static String translate(String string) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("translate", string);
        return JsonHelper.toSortedString(jsonObject);
    }

    public static <T> Dynamic<T> translate(DynamicOps<T> ops, String key) {
        String string2 = TextFixes.translate(key);
        return new Dynamic<T>(ops, ops.createString(string2));
    }

    public static String parseLenientJson(String json) {
        if (json.isEmpty() || json.equals("null")) {
            return EMPTY_TEXT;
        }
        char c = json.charAt(0);
        char d = json.charAt(json.length() - 1);
        if (c == '\"' && d == '\"' || c == '{' && d == '}' || c == '[' && d == ']') {
            try {
                JsonElement jsonElement = LenientJsonParser.parse(json);
                if (jsonElement.isJsonPrimitive()) {
                    return TextFixes.text(jsonElement.getAsString());
                }
                return JsonHelper.toSortedString(jsonElement);
            } catch (JsonParseException jsonParseException) {
                // empty catch block
            }
        }
        return TextFixes.text(json);
    }

    public static boolean method_74084(Dynamic<?> dynamic) {
        return dynamic.asString().result().filter(string -> {
            try {
                StrictJsonParser.parse(string);
                return true;
            } catch (JsonParseException jsonParseException) {
                return false;
            }
        }).isPresent();
    }

    public static Optional<String> getTranslate(String json) {
        try {
            JsonObject jsonObject;
            JsonElement jsonElement2;
            JsonElement jsonElement = LenientJsonParser.parse(json);
            if (jsonElement.isJsonObject() && (jsonElement2 = (jsonObject = jsonElement.getAsJsonObject()).get("translate")) != null && jsonElement2.isJsonPrimitive()) {
                return Optional.of(jsonElement2.getAsString());
            }
        } catch (JsonParseException jsonParseException) {
            // empty catch block
        }
        return Optional.empty();
    }
}

